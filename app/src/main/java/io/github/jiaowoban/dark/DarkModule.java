package io.github.jiaowoban.dark;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.Map;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Hooks exist only inside Jiaowoban processes, even if an incorrect scope is selected. */
public final class DarkModule implements IXposedHookLoadPackage {
    public static final String TARGET = "edu.sjtu.infoplus.taskcenter";
    private static final String TAG = "JiaowobanDark";
    private final Set<Class<?>> settingsClasses = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<WebView> webViews = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<TextView, ColorStateList> originalTextColors = new WeakHashMap<>();
    private final Map<Window, BarState> originalBars = new WeakHashMap<>();
    private final PaymentCardTheme paymentCardTheme = new PaymentCardTheme();
    private volatile Context appContext;
    private boolean webSettingsFailureLogged;
    private boolean textFailureLogged;

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam pkg) {
        if (!TARGET.equals(pkg.packageName)) return;
        log("loaded v0.1.0 in " + pkg.processName + " sdk=" + Build.VERSION.SDK_INT);
        installRendererHooks(pkg.classLoader);
        XposedHelpers.findAndHookMethod(View.class, "onAttachedToWindow", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                View view = (View) param.thisObject;
                prepareView(view);
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Activity activity = (Activity) param.thisObject;
                if (!TARGET.equals(activity.getPackageName())) return;
                appContext = activity.getApplicationContext();
                prepareWindow(activity.getWindow());
                activity.getWindow().getDecorView().post(() -> prepareWindow(activity.getWindow()));
                log("resumed " + activity.getClass().getSimpleName() + " night=" + night(activity));
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onConfigurationChanged", Configuration.class,
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        Activity activity = (Activity) param.thisObject;
                        activity.getWindow().getDecorView().post(() -> prepareWindow(activity.getWindow()));
                    }
                });
        XposedBridge.hookAllConstructors(WebView.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                WebView webView = (WebView) param.thisObject;
                if (webView.getContext() == null) return;
                appContext = webView.getContext().getApplicationContext();
                if (webViews.add(webView)) {
                    configureWebView(webView);
                    log("WebView configured: " + webView.getClass().getSimpleName());
                }
            }
        });
        XposedHelpers.findAndHookMethod(WebView.class, "getSettings", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                if (param.getResult() instanceof WebSettings settings) {
                    configureSettings(settings, ((WebView) param.thisObject).getContext());
                }
            }
        });
    }

    private void installRendererHooks(ClassLoader loader) {
        // Android 15+ moved the force-dark decision into ViewRootImpl and uses an int enum.
        Class<?> root = XposedHelpers.findClass("android.view.ViewRootImpl", loader);
        try {
            XposedHelpers.findAndHookMethod(root, "determineForceDarkType", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if (night(context)) param.setResult(1); // ForceDarkType.FORCE_DARK
                }
            });
            log("renderer: ViewRootImpl.determineForceDarkType");
        } catch (Throwable missingMethod) {
            // Android 10–14 and OEM variants pass a boolean to HardwareRenderer.
            try {
                XposedHelpers.findAndHookMethod("android.graphics.HardwareRenderer", loader,
                        "setForceDark", boolean.class, new XC_MethodHook() {
                            @Override protected void beforeHookedMethod(MethodHookParam param) {
                                if (night(appContext)) param.args[0] = true;
                            }
                        });
                log("renderer: HardwareRenderer.setForceDark(boolean)");
            } catch (Throwable failure) {
                log("renderer hook unavailable: " + failure);
            }
        }
    }

    private void prepareWindow(Window window) {
        if (window == null) return;
        View decor = window.getDecorView();
        decor.setForceDarkAllowed(true);
        prepareTree(decor);
        // Native Force Dark does not recolor system bar surfaces.
        if (night(decor.getContext())) {
            if (!originalBars.containsKey(window)) originalBars.put(window, new BarState(window));
            window.setNavigationBarColor(0xff121212);
            window.setNavigationBarDividerColor(0xff121212);
            decor.setSystemUiVisibility(decor.getSystemUiVisibility()
                    & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            BarState original = originalBars.remove(window);
            if (original != null) {
                window.setNavigationBarColor(original.navigation);
                window.setNavigationBarDividerColor(original.divider);
                int mask = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decor.setSystemUiVisibility((decor.getSystemUiVisibility() & ~mask) | (original.flags & mask));
            }
        }
        decor.invalidate();
    }

    private void prepareView(View view) {
        if (paymentCardTheme.apply(view, night(view.getContext()))) return;
        boolean codeRegion = protectedCodeRegion(view);
        view.setForceDarkAllowed(!(view instanceof WebView) && !protectedSurface(view) && !codeRegion);
        if (codeRegion) {
            if (view instanceof TextView text) {
                ColorStateList original = originalTextColors.remove(text);
                if (original != null) text.setTextColor(original);
            }
            return;
        }
        // OEM Force Dark can miss labels on image-backed empty states. Keep all selector
        // states and their opacity while lifting only dark neutral text on transparent views.
        if (!(view instanceof TextView text) || text.getBackground() != null) return;
        try {
            ColorStateList original = originalTextColors.get(text);
            if (original == null) original = text.getTextColors();
            int[] colors = ((int[]) XposedHelpers.getObjectField(original, "mColors")).clone();
            int[][] states = (int[][]) XposedHelpers.getObjectField(original, "mStateSpecs");
            boolean changed = false;
            for (int i = 0; i < colors.length; i++) {
                boolean enabled = true;
                for (int state : states[i]) {
                    if (state == -android.R.attr.state_enabled) enabled = false;
                }
                int lifted = TextContrast.forDarkBackground(colors[i], enabled);
                changed |= lifted != colors[i];
                colors[i] = lifted;
            }
            if (!changed) return;
            originalTextColors.put(text, original);
            if (night(text.getContext())) {
                text.setTextColor(new ColorStateList(states, colors));
                text.setForceDarkAllowed(false);
            } else {
                text.setTextColor(original);
            }
        } catch (Throwable failure) {
            if (!textFailureLogged) {
                textFailureLogged = true;
                log("text contrast adjustment unavailable: " + failure);
            }
        }
    }

    private static boolean protectedCodeRegion(View view) {
        for (View node = view; node != null;) {
            String name = resourceName(node);
            if (name.equals("clcode") || name.equals("ivcode") || name.equals("iv_code")
                    || name.contains("qrcode") || name.contains("qr_code")
                    || name.contains("barcode") || name.contains("bar_code")) return true;
            node = node.getParent() instanceof View parent ? parent : null;
        }
        return false;
    }

    private static String resourceName(View view) {
        try {
            if (view.getId() != View.NO_ID) return view.getResources().getResourceEntryName(view.getId())
                    .toLowerCase(java.util.Locale.ROOT);
        } catch (Resources.NotFoundException ignored) { }
        return "";
    }

    private static boolean protectedSurface(View view) {
        if (view instanceof SurfaceView || view instanceof TextureView) return true;
        if (!(view instanceof ImageView)) return false;
        Context context = view.getContext();
        while (context instanceof ContextWrapper wrapper && !(context instanceof Activity)) {
            Context base = wrapper.getBaseContext();
            if (base == context) break;
            context = base;
        }
        String owner = context.getClass().getSimpleName();
        return owner.contains("CampusCard") || owner.contains("PayCode") || owner.contains("Totp");
    }

    private static final class BarState {
        final int navigation;
        final int divider;
        final int flags;
        BarState(Window window) {
            navigation = window.getNavigationBarColor();
            divider = window.getNavigationBarDividerColor();
            flags = window.getDecorView().getSystemUiVisibility();
        }
    }

    private void prepareTree(View view) {
        prepareView(view);
        if (view instanceof WebView webView) {
            configureWebView(webView);
            return;
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) prepareTree(group.getChildAt(i));
        }
    }

    private void configureWebView(WebView view) {
        try {
            // Chromium handles images and CSS separately; do not invert the WebView as a bitmap.
            view.setForceDarkAllowed(false);
            configureSettings(view.getSettings(), view.getContext());
        } catch (Throwable failure) {
            logWebFailure(failure);
        }
    }

    @SuppressWarnings("deprecation")
    private void configureSettings(WebSettings settings, Context context) {
        try {
            hookSettingsClass(settings.getClass());
            // Jiaowoban 3.6.5 targets API 30: forceDark remains the applicable WebView API.
            settings.setForceDark(night(context) ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
            if (Build.VERSION.SDK_INT >= 33) settings.setAlgorithmicDarkeningAllowed(night(context));
        } catch (Throwable failure) {
            logWebFailure(failure);
        }
    }

    private void hookSettingsClass(Class<?> type) {
        if (!settingsClasses.add(type)) return;
        XposedBridge.hookAllMethods(type, "setForceDark", new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam param) {
                param.args[0] = night(appContext) ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF;
            }
        });
        if (Build.VERSION.SDK_INT >= 33) {
            XposedBridge.hookAllMethods(type, "setAlgorithmicDarkeningAllowed", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    param.args[0] = night(appContext);
                }
            });
        }
    }

    private static boolean night(Context context) {
        Resources resources = context == null ? Resources.getSystem() : context.getResources();
        return (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void logWebFailure(Throwable failure) {
        if (!webSettingsFailureLogged) {
            webSettingsFailureLogged = true;
            log("WebView settings unavailable: " + failure);
        }
    }

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
        android.util.Log.i(TAG, message);
    }
}
