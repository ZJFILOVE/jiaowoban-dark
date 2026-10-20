package io.github.jiaowoban.dark;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

/** An in-app appearance page, instantiated only by the module's explicit menu action. */
public final class AppearanceActivity extends Activity {
    private final ThemeController controller;
    private boolean dark;
    private int ink, muted, surface, outline;
    private static final int BLUE = 0xff016aed;

    AppearanceActivity(ThemeController controller) { this.controller = controller; }

    @Override public void onCreate(Bundle state) {
        setTheme(android.R.style.Theme_Material_Light_NoActionBar);
        super.onCreate(state);
        setTitle("外观");
        render();
    }

    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        render();
    }

    private void render() {
        dark = controller.night(this);
        ink = dark ? 0xffeeeeee : 0xff262626;
        muted = dark ? 0xffaaaaaa : 0xff777777;
        surface = dark ? 0xff202124 : 0xffffffff;
        outline = dark ? 0xff35363a : 0xffe5e7eb;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(BLUE);
        getWindow().setNavigationBarColor(dark ? 0xff121212 : 0xfff6f6f6);
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = column();
        root.setTag(ThemeController.ROW_TAG);
        root.setForceDarkAllowed(false);
        root.setBackgroundColor(dark ? 0xff121212 : 0xfff6f6f6);
        FrameLayout toolbar = new FrameLayout(this);
        toolbar.setBackgroundColor(BLUE);
        TextView title = text("外观", 20, 0xffffffff);
        title.setGravity(Gravity.CENTER);
        title.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        toolbar.addView(title, new FrameLayout.LayoutParams(-1, -1));
        ImageButton back = new ImageButton(this);
        Path path = new Path();
        path.moveTo(15, 4); path.lineTo(7, 12); path.lineTo(15, 20);
        ShapeDrawable arrow = new ShapeDrawable(new PathShape(path, 24, 24));
        arrow.setIntrinsicWidth(dp(24)); arrow.setIntrinsicHeight(dp(24));
        arrow.getPaint().setColor(0xffffffff);
        arrow.getPaint().setStyle(Paint.Style.STROKE);
        arrow.getPaint().setStrokeWidth(2);
        back.setImageDrawable(arrow);
        back.setContentDescription("返回");
        back.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33ffffff), null, null));
        back.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(dp(56), -1, Gravity.START);
        toolbar.addView(back, backParams);
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(56)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = column();
        content.setPadding(dp(16), dp(24), dp(16), dp(24));
        TextView heading = text("显示模式", 14, muted);
        heading.setAccessibilityHeading(true);
        heading.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2);
        headingParams.setMargins(dp(4), 0, 0, dp(12));
        content.addView(heading, headingParams);

        LinearLayout system = new LinearLayout(this);
        system.setGravity(Gravity.CENTER_VERTICAL);
        system.setPadding(dp(18), dp(16), dp(14), dp(16));
        LinearLayout systemText = column();
        systemText.addView(text("跟随系统", 16, ink));
        TextView subtitle = text("自动匹配系统的深浅色", 13, muted);
        subtitle.setPadding(0, dp(5), 0, 0);
        systemText.addView(subtitle);
        system.addView(systemText, new LinearLayout.LayoutParams(0, -2, 1));
        system.addView(radio(controller.mode() == 0));
        configureChoice(system, 0, "跟随系统，自动匹配系统的深浅色");
        content.addView(system, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout cards = new LinearLayout(this);
        cards.setBaselineAligned(false);
        LinearLayout.LayoutParams cardsParams = new LinearLayout.LayoutParams(-1, -2);
        cardsParams.topMargin = dp(16);
        content.addView(cards, cardsParams);
        for (int mode = 1; mode <= 2; mode++) {
            String label = mode == 1 ? "浅色" : "深色";
            LinearLayout card = column();
            card.setPadding(dp(12), dp(12), dp(12), dp(8));
            card.addView(new Preview(mode == 2), new LinearLayout.LayoutParams(-1, dp(128)));
            LinearLayout caption = new LinearLayout(this);
            caption.setGravity(Gravity.CENTER_VERTICAL);
            TextView labelView = text(label, 16, ink);
            caption.addView(labelView, new LinearLayout.LayoutParams(0, -2, 1));
            caption.addView(radio(controller.mode() == mode));
            LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-1, -2);
            captionParams.topMargin = dp(6);
            card.addView(caption, captionParams);
            configureChoice(card, mode, label);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, -2, 1);
            if (mode == 1) cardParams.setMarginEnd(dp(12));
            cards.addView(card, cardParams);
        }
        TextView note = text("外观设置仅对交我办生效，选择后立即应用。", 13, muted);
        note.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        note.setLineSpacing(dp(3), 1);
        note.setPadding(dp(4), dp(18), dp(4), 0);
        content.addView(note);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -1));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void configureChoice(View view, int mode, String label) {
        boolean checked = controller.mode() == mode;
        GradientDrawable background = new GradientDrawable();
        background.setColor(surface);
        background.setCornerRadius(dp(12));
        background.setStroke(dp(checked ? 2 : 1), checked ? BLUE : outline);
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x18016aed), background, null));
        view.setFocusable(true);
        view.setContentDescription(label);
        view.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(RadioButton.class.getName());
                info.setCheckable(true);
                info.setChecked(checked);
            }
        });
        view.setOnClickListener(v -> {
            controller.choose(mode);
            render();
        });
    }

    private RadioButton radio(boolean selected) {
        RadioButton radio = new RadioButton(this);
        radio.setButtonTintList(ColorStateList.valueOf(selected ? BLUE : muted));
        radio.setChecked(selected);
        radio.setClickable(false);
        radio.setFocusable(false);
        radio.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        radio.setMinWidth(dp(32));
        radio.setMinHeight(dp(48));
        radio.setPadding(0, 0, 0, 0);
        return radio;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value); text.setTextSize(size); text.setTextColor(color);
        text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return text;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    /** Static UI previews stay light/dark independently of the currently selected mode. */
    private final class Preview extends View {
        private final boolean previewDark;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Preview(boolean dark) { super(AppearanceActivity.this); previewDark = dark; }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int save = canvas.save();
            canvas.scale(getWidth() / 144f, getHeight() / 128f);
            block(canvas, 0, 0, 144, 128, 7, previewDark ? 0xff101114 : 0xfff2f3f7);
            block(canvas, 0, 0, 144, 30, 7, BLUE);
            block(canvas, 0, 20, 144, 30, 0, BLUE);
            block(canvas, 12, 12, 20, 16, 2, 0xccffffff);
            block(canvas, 49, 12, 95, 16, 2, 0xffffffff);
            int card = previewDark ? 0xff25272d : 0xffffffff;
            block(canvas, 10, 40, 134, 76, 5, card);
            block(canvas, 18, 49, 35, 66, 4, previewDark ? 0xff143561 : 0xffe3edfe);
            block(canvas, 44, 49, 107, 54, 2, previewDark ? 0xffdadce0 : 0xff4b5059);
            block(canvas, 44, 61, 85, 65, 2, previewDark ? 0xff747981 : 0xffc2c6ce);
            block(canvas, 10, 84, 68, 116, 5, card);
            block(canvas, 76, 84, 134, 116, 5, card);
            block(canvas, 18, 96, 52, 101, 2, previewDark ? 0xff747981 : 0xffc2c6ce);
            block(canvas, 84, 96, 118, 101, 2, previewDark ? 0xff747981 : 0xffc2c6ce);
            canvas.restoreToCount(save);
        }
        private void block(Canvas c, float l, float t, float r, float b, float radius, int color) {
            paint.setColor(color); c.drawRoundRect(l, t, r, b, radius, radius, paint);
        }
    }
}
