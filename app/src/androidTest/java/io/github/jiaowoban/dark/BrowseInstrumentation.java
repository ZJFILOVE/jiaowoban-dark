package io.github.jiaowoban.dark;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

/** A debug-only UI helper for entering Chinese search terms without replacing the user's IME. */
public final class BrowseInstrumentation extends Instrumentation {
    private Bundle arguments;
    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.arguments = arguments;
        start();
    }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            UiAutomation automation = getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
            AccessibilityNodeInfo root = automation.getRootInActiveWindow();
            if (root == null) throw new IllegalStateException("No active window");
            String id = arguments.getString("id", "edu.sjtu.infoplus.taskcenter:id/editSearch");
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes.isEmpty()) throw new IllegalStateException("Node absent: " + id);
            Bundle text = new Bundle();
            text.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    arguments.getString("text", "水源"));
            if (!nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, text)) {
                throw new IllegalStateException("Set text refused");
            }
            result.putString("stream", "Search text entered through accessibility\n");
            finish(ActivityResult.OK, result);
        } catch (Throwable error) {
            result.putString("stream", error.toString() + "\n");
            finish(ActivityResult.FAILED, result);
        }
    }
    private static final class ActivityResult {
        static final int OK = -1;
        static final int FAILED = 0;
    }
}
