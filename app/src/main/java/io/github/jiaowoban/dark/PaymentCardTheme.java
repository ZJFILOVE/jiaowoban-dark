package io.github.jiaowoban.dark;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Map;
import java.util.WeakHashMap;
import de.robv.android.xposed.XposedHelpers;

/** Keep only the QR square white, with a consistent dark card and readable payment controls. */
final class PaymentCardTheme {
    private static final int SURFACE = 0xff1b2028;
    private final Map<View, State> originals = new WeakHashMap<>();
    private final Map<View, Boolean> observed = new WeakHashMap<>();

    boolean apply(View view, boolean dark) {
        if (!owner(view).contains("PayCode")) return false;
        View card = null;
        for (View node = view; node != null;) {
            if (name(node).equals("cardview")) { card = node; break; }
            node = node.getParent() instanceof View parent ? parent : null;
        }
        if (card == null) return false;
        view.setForceDarkAllowed(false);
        String id = name(view);
        if (view == card && observed.put(view, true) == null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                boolean night = (view.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
                applyTree(view, night);
            });
        }
        if (!(view instanceof TextView) && !id.equals("cardview") && !id.equals("clcode")
                && !id.equals("clchoosecard") && !id.equals("ivcode") && !id.equals("viewline")) return true;
        State original = originals.get(view);
        if (original == null) {
            original = new State(view, id.equals("cardview"));
            originals.put(view, original);
        }
        if (id.equals("cardview")) {
            XposedHelpers.callMethod(view, "setCardBackgroundColor",
                    dark ? ColorStateList.valueOf(SURFACE) : original.cardColors);
        } else if (id.equals("ivcode")) {
            if (dark) {
                GradientDrawable white = new GradientDrawable();
                white.setColor(0xffffffff);
                white.setCornerRadius(10 * view.getResources().getDisplayMetrics().density);
                view.setBackground(white);
                int quietZone = (int) (10 * view.getResources().getDisplayMetrics().density);
                view.setPadding(quietZone, quietZone, quietZone, quietZone);
                view.setClipToOutline(true);
            } else {
                original.restoreBackground(view);
                view.setPadding(original.left, original.top, original.right, original.bottom);
                view.setClipToOutline(original.clip);
            }
        } else if (id.equals("clcode") || id.equals("clchoosecard") || id.equals("viewline")) {
            if (dark) view.setBackgroundColor(id.equals("viewline") ? 0xff303741 : SURFACE);
            else original.restoreBackground(view);
        }
        if (view instanceof TextView text) {
            if (dark) {
                int[] colors = ((int[]) XposedHelpers.getObjectField(original.textColors, "mColors")).clone();
                int[][] states = (int[][]) XposedHelpers.getObjectField(original.textColors, "mStateSpecs");
                for (int i = 0; i < colors.length; i++) {
                    boolean enabled = true;
                    for (int state : states[i]) if (state == -android.R.attr.state_enabled) enabled = false;
                    colors[i] = TextContrast.forDarkBackground(colors[i], enabled);
                }
                text.setTextColor(new ColorStateList(states, colors));
            } else text.setTextColor(original.textColors);
        }
        return true;
    }

    private void applyTree(View view, boolean dark) {
        apply(view, dark);
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) applyTree(group.getChildAt(i), dark);
        }
    }

    private static String owner(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper wrapper && !(context instanceof Activity)) {
            Context base = wrapper.getBaseContext();
            if (base == context) break;
            context = base;
        }
        return context.getClass().getSimpleName();
    }

    private static String name(View view) {
        try { return view.getResources().getResourceEntryName(view.getId()).toLowerCase(java.util.Locale.ROOT); }
        catch (android.content.res.Resources.NotFoundException ignored) { return ""; }
    }

    private static final class State {
        final Drawable.ConstantState background;
        final ColorStateList cardColors;
        final ColorStateList textColors;
        final int left, top, right, bottom;
        final boolean clip;
        State(View view, boolean card) {
            Drawable.ConstantState source = view.getBackground() == null ? null : view.getBackground().getConstantState();
            background = source == null ? null : source.newDrawable(view.getResources()).mutate().getConstantState();
            cardColors = card ? (ColorStateList) XposedHelpers.callMethod(view, "getCardBackgroundColor") : null;
            textColors = view instanceof TextView text ? text.getTextColors() : null;
            left = view.getPaddingLeft(); top = view.getPaddingTop();
            right = view.getPaddingRight(); bottom = view.getPaddingBottom();
            clip = view.getClipToOutline();
        }
        void restoreBackground(View view) {
            view.setBackground(background == null ? null : background.newDrawable(view.getResources()).mutate());
        }
    }
}
