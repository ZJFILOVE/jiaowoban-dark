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
        body.setText("在 LSPosed 中启用本模块，作用域仅勾选「交我办」。\n\n跟随系统深色模式，适配原生界面及内嵌网页。切换主题后重新打开交我办即可。\n\n安装此应用不代表模块已启用，请在 LSPosed 中确认状态。");
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
