# 交我办深色

为交我办提供跟随系统的深色模式的 LSPosed / Xposed 模块。作用域仅为 `edu.sjtu.infoplus.taskcenter`，包含该应用的子进程。

## 使用

1. 安装构建生成的 APK。
2. 在 LSPosed 中启用「交我办深色」，作用域只勾选「交我办」。
3. 结束交我办进程后重新打开。系统开启深色模式时生效，切回浅色时恢复。

本项目调试约束：**禁止硬重启，只允许软重启**。当前手机通过重新启动交我办进程即可应用变更，无需重启系统。

## 适配方法

- 原生界面：在交我办进程内启用 Android Force Dark，覆盖延迟加载、列表及弹窗的视图。
- 网页子模块：对所有标准 Android WebView 配置 Chromium 深色渲染，并拦截应用随后关闭深色渲染的调用；不替换原有 WebViewClient。
- 保留主题色和图片；不对整页截图取反。思源码采用统一深灰卡片与支付栏，二维码单独保留圆角白底和留白；相机预览不参与强制变色。
- 修复 OEM 深色渲染遗漏的低对比度中性文字，并保留选中、禁用等状态。
- 没有网络权限，不读取账号、Cookie，不修改网页存储或用户的社区主题设置。

水源社区通过交我办内的「更多 → 搜索水源 → 水源社区」实际验证。更多测试状态见 [实机验证](docs/device-validation.md)。通用 WebView 适配不等于已逐一验证服务大厅中的全部服务；无法加载的业务页单独记录，不计为通过。

## 构建

需要 JDK 17 或 21、Android SDK 36、Build Tools 36.0.0。Gradle Wrapper 自带校验值。本机的 JDK 21 已配置于 `~/.local/opt/jdk-21`，不改变系统默认 Java。

在 `local.properties` 配置本机 SDK：

```properties
sdk.dir=/path/to/Android/Sdk
```

```sh
JAVA_HOME="$HOME/.local/opt/jdk-21" ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`。当前交付为开发调试签名，不包含正式发布的私钥。

Xposed API 仅用于编译，不打进 APK，来源与校验值见 [third_party](third_party/README.md)。

## 实机调试辅助

`tools/phone_ui.py` 需要明确设备序列号，可读取可访问性界面，或点击唯一匹配的文字、描述、资源 ID。使用资源 ID 可避免同名父子节点歧义：

```sh
python3 tools/phone_ui.py --serial YOUR_DEVICE
python3 tools/phone_ui.py --serial YOUR_DEVICE --tap edu.sjtu.infoplus.taskcenter:id/nav_todo
```

测试 APK 内的 `BrowseInstrumentation` 仅用于向当前交我办搜索框输入中文，不更换手机输入法，不包含在正式模块 APK 中：

```sh
./gradlew :app:assembleDebugAndroidTest
adb -s YOUR_DEVICE install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s YOUR_DEVICE shell am instrument -w -e text 水源 io.github.jiaowoban.dark.test/io.github.jiaowoban.dark.BrowseInstrumentation
```

## 兼容范围

已验证的应用版本和手机系统见实机记录。Android 10–14 的布尔型渲染接口保留兼容路径，尚未进行实机验证。若交我办未来提升 targetSdk 至 33 或以上，网页主题行为需要重新验证。

## 实现依据

- [Xposed 加载包回调](https://api.xposed.info/reference/de/robv/android/xposed/IXposedHookLoadPackage.html)
- [Android 16 ViewRootImpl](https://github.com/aosp-mirror/platform_frameworks_base/blob/android16-release/core/java/android/view/ViewRootImpl.java)
- [Android WebView 深色主题](https://developer.android.com/develop/ui/views/layout/webapps/dark-theme)
