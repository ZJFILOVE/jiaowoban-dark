package io.github.jiaowoban.dark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding * 2, padding, padding);
        TextView title = new TextView(this);
        title.setText("交我办深色");
        title.setTextSize(28);
        layout.addView(title);
        TextView body = new TextView(this);
        body.setText("独立模块：在 LSPosed 中启用，作用域仅勾选「交我办」。\n\n免 root 完整版：深色模块已内置，直接打开交我办即可。\n\n在交我办个人菜单的「外观」中选择跟随系统、浅色或深色，默认跟随系统。\n\n外观选择保存在交我办内，不会改变手机的系统主题。");
        body.setTextSize(16);
        body.setPadding(0, padding, 0, padding);
        layout.addView(body);
        Button launch = new Button(this);
        launch.setText("打开交我办");
        launch.setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage(DarkModule.TARGET);
            if (intent != null) startActivity(intent);
        });
        layout.addView(launch);
        setContentView(layout);
    }
}
