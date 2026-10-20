package io.github.jiaowoban.dark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.robv.android.xposed.XposedHelpers;

/** Stores appearance inside the target app's own sandbox, including integrated builds. */
final class ThemeController {
    static final String ROW_TAG = "io.github.jiaowoban.dark.appearance";
    static final String PAGE_EXTRA = "io.github.jiaowoban.dark.APPEARANCE";
    private static final String[] LABELS = {"跟随系统", "浅色", "深色"};
    private SharedPreferences preferences;
    private Class<?> delegate;
    private final Runnable onChanged;

    ThemeController(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    void attach(Context context, ClassLoader loader) {
        if (preferences != null) return;
        preferences = context.getSharedPreferences("jiaowoban_dark_appearance", Context.MODE_PRIVATE);
        delegate = XposedHelpers.findClassIfExists("androidx.appcompat.app.AppCompatDelegate", loader);
        applyAppCompatMode();
    }

    int mode() {
        int saved = preferences == null ? 0 : preferences.getInt("mode", 0);
        return saved >= 0 && saved < LABELS.length ? saved : 0;
    }

    void choose(int selected) {
        if (selected < 0 || selected >= LABELS.length || selected == mode() || preferences == null) return;
        preferences.edit().putInt("mode", selected).apply();
        applyAppCompatMode();
        onChanged.run();
    }

    boolean night(Context context) {
        int selected = mode();
        if (selected != 0) return selected == 2;
        // Resources.getSystem() reflects device configuration even when AppCompat
        // gives an Activity an app-local override. AUTO means sunset, not follow-system.
        return (Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void applyAppCompatMode() {
        if (delegate != null) {
            int selected = mode();
            try {
                XposedHelpers.callStaticMethod(delegate, "setDefaultNightMode", selected == 0 ? -1 : selected);
            } catch (Throwable failure) {
                android.util.Log.w("JiaowobanDark", "AppCompat appearance update unavailable", failure);
            }
        }
    }

    void installMenu(Activity activity) {
        String owner = activity.getClass().getName();
        boolean personalMenu = owner.equals("edu.sjtu.infoplus.taskcenter.act.MineAct");
        if (!personalMenu && !owner.equals("edu.sjtu.infoplus.taskcenter.act.SettingsAct")) return;
        View existing = activity.getWindow().getDecorView().findViewWithTag(ROW_TAG);
        if (existing instanceof LinearLayout row) {
            bind(row);
            return;
        }
        // Place beside the existing font setting; resolve by resource name so IDs
        // can move between app releases. Do not alter the existing row's listener.
        int fontId = activity.getResources().getIdentifier(personalMenu ? "rl_custom" : "rl_fontSize",
                "id", activity.getPackageName());
        View font = fontId == 0 ? null : activity.findViewById(fontId);
        if (font == null || !(font.getParent() instanceof LinearLayout group)) return;
        LinearLayout row = new LinearLayout(activity);
        row.setTag(ROW_TAG);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPaddingRelative(dp(activity, 20), dp(activity, 16), dp(activity, 20), dp(activity, 16));
        row.setMinimumHeight(dp(activity, 60));
        {
            ImageView icon = new ImageView(activity);
            icon.setImageDrawable(new AppearanceIcon(night(activity)));
            icon.setTag(ROW_TAG + ".icon");
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(activity, 32), dp(activity, 32));
            iconParams.setMarginEnd(dp(activity, 24));
            row.addView(icon, iconParams);
        }
        TextView title = new TextView(activity);
        title.setTag(ROW_TAG + ".title");
        title.setText("外观");
        title.setTextSize(16);
        row.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView value = new TextView(activity);
        value.setTag(ROW_TAG + ".value");
        value.setTextSize(14);
        row.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        android.util.TypedValue selectable = new android.util.TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectable, true)
                && selectable.resourceId != 0) row.setForeground(activity.getDrawable(selectable.resourceId));
        row.setFocusable(true);
        row.setOnClickListener(v -> activity.startActivity(new Intent()
                .setClassName(activity.getPackageName(), "edu.sjtu.infoplus.taskcenter.act.SettingsAct")
                .putExtra(PAGE_EXTRA, true)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(activity, personalMenu ? 1 : 10);
        group.addView(row, group.indexOfChild(font) + 1, params);
        bind(row);
    }

    private void bind(LinearLayout row) {
        boolean dark = night(row.getContext());
        row.setForceDarkAllowed(false);
        row.setBackgroundColor(dark ? 0xff000000 : 0xffffffff);
        ((TextView) row.findViewWithTag(ROW_TAG + ".title")).setTextColor(dark ? 0xfff1f1f1 : 0xff262626);
        ImageView icon = row.findViewWithTag(ROW_TAG + ".icon");
        if (icon != null) icon.setImageDrawable(new AppearanceIcon(dark));
        TextView value = row.findViewWithTag(ROW_TAG + ".value");
        value.setText(LABELS[mode()] + "  ›");
        value.setTextColor(dark ? 0xffb8b8b8 : 0xff666666);
        row.setContentDescription("外观，" + LABELS[mode()]);
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static boolean ownsView(View view) {
        for (View node = view; node != null;) {
            if (ROW_TAG.equals(node.getTag())) return true;
            node = node.getParent() instanceof View parent ? parent : null;
        }
        return false;
    }
}
