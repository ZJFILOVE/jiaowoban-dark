# 交我办深色

为交我办提供浅色、深色和跟随系统三种外观模式的 LSPosed / Xposed 模块。作用域仅为 `edu.sjtu.infoplus.taskcenter`，包含该应用的子进程。也可将模块内置到原版 APK，生成免 root 的完整安装包。

[源码仓库](https://github.com/ZJFILOVE/jiaowoban-dark) · [下载安装包](https://github.com/ZJFILOVE/jiaowoban-dark/releases) · [反馈问题](https://github.com/ZJFILOVE/jiaowoban-dark/issues)

这是独立开发的主题适配模块，与交我办及上海交通大学官方无隶属关系。

## 下载与安装

当前版本：**[v0.2.1 测试版](https://github.com/ZJFILOVE/jiaowoban-dark/releases/tag/v0.2.1)**，适配交我办 **3.6.5**。

| 版本 | 下载 | 适用情况 |
| --- | --- | --- |
| 免 root 完整版 | [下载完整 APK（约 225 MB）](https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/jiaowoban-3.6.5-dark-rootless.apk) | 无需 root、LSPosed、Shizuku 或另外安装模块；首次安装通常需卸载官方版 |
| LSPosed 独立模块 | [下载模块 APK（约 50 KB）](https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/jiaowoban-dark-0.2.1-debug.apk) | 已使用 LSPosed，可保留官方交我办及其数据 |

[文件校验值 SHA-256](https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/SHA256SUMS) · [全部发布版本](https://github.com/ZJFILOVE/jiaowoban-dark/releases)

### 免 root 完整版

1. 保存重要本地内容，并确认可以重新登录交我办。
2. 下载完整 APK。由于它与官方版的安装签名不同，**通常需要先卸载官方版再安装，卸载会清除本地数据和登录状态**。
3. 安装后直接打开交我办，在首页右上角进入个人菜单，点击「外观」。

完整版已内置深色模块，使用 [JingMatrix LSPatch v1.2](https://github.com/JingMatrix/LSPatch/releases/tag/v1.2) 的集成模式，不需要单独安装管理器。本包已通过签名和文件完整性检查，**尚未在无 root 设备上完成安装、登录验证**；第三方登录、支付、推送等功能仍需验证。完整说明、更新方法及自行构建步骤见 [免 root 使用说明](docs/rootless.md)。

### LSPosed 独立模块

1. 下载并安装模块 APK。
2. 在 LSPosed 中启用「交我办深色」，作用域只勾选「交我办」。
3. 结束交我办进程后重新打开，无需重启手机。
4. 首页右上角进入个人菜单，点击「外观」。

当前模块使用开发调试签名。已安装本项目旧版发布模块的用户可尝试直接更新；如提示签名冲突，只需处理独立模块，不要卸载官方交我办。

## 外观设置

支持三种模式，**默认跟随系统**。选择后立即生效，重开应用后保留，外观设置仅作用于交我办。

| 模式 | 效果 |
| --- | --- |
| 跟随系统 | 随手机系统切换浅色和深色 |
| 浅色 | 始终使用浅色 |
| 深色 | 始终使用深色 |

个人菜单加入与原有项目对齐的双色月亮图标。独立外观页沿用交我办蓝色标题栏，提供浅深色预览卡片、单选反馈和返回按钮。

<img src="https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/appearance-preview.png" alt="交我办外观设置：跟随系统、浅色和深色" width="320" />

菜单、外观页、三种模式及选择保留已在 PKJ110 / Android 16 上通过 LSPosed 验证；该结果不代表免 root 集成运行时已实测。详细范围见 [本轮验证说明](docs/rootless.md#验证范围) 和 [原版模块实机记录](docs/device-validation.md)。

## 适配方法

- 原生界面：在交我办进程内启用 Android Force Dark，覆盖延迟加载、列表及弹窗的视图。
- 网页子模块：对所有标准 Android WebView 配置 Chromium 深色渲染，并拦截应用随后关闭深色渲染的调用；不替换原有 WebViewClient。
- 保留主题色和图片；不对整页截图取反。思源码采用统一深灰卡片与支付栏，二维码单独保留圆角白底和留白；相机预览不参与强制变色。
- 修复 OEM 深色渲染遗漏的低对比度中性文字，并保留选中、禁用等状态。
- 模块自身不声明网络权限，不读取账号、Cookie，不修改网页存储或用户的社区主题设置。完整版保留交我办原有业务权限。

水源社区通过交我办内的「更多 → 搜索水源 → 水源社区」实际验证。更多测试状态见 [实机验证](docs/device-validation.md)。通用 WebView 适配不等于已逐一验证服务大厅中的全部服务；无法加载的业务页单独记录，不计为通过。

## 构建

需要 JDK 17 或 21、Android SDK 36、Build Tools 36.0.0。Gradle Wrapper 自带校验值，通过 `JAVA_HOME` 指定合适的 JDK。

在 `local.properties` 配置本机 SDK：

```properties
sdk.dir=/path/to/Android/Sdk
```

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
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

当前基于交我办 3.6.5（versionCode 85，targetSdk 30），模块使用 Android 10 起的界面 API，不支持 Android 10 以下系统。已验证的应用版本和手机系统见实机记录。Android 10–14 的布尔型渲染接口保留兼容路径，尚未进行实机验证。若交我办未来提升 targetSdk 至 33 或以上，网页主题行为需要重新验证。

## 实现依据

- [Xposed 加载包回调](https://api.xposed.info/reference/de/robv/android/xposed/IXposedHookLoadPackage.html)
- [Android 16 ViewRootImpl](https://github.com/aosp-mirror/platform_frameworks_base/blob/android16-release/core/java/android/view/ViewRootImpl.java)
- [Android WebView 深色主题](https://developer.android.com/develop/ui/views/layout/webapps/dark-theme)

## 许可证

原创模块代码采用 [Apache-2.0](LICENSE) 许可证。LSPatch 运行时采用 GPL-3.0，完整包随发布提供对应许可证及上游源码链接。交我办原始代码和资源的权利归原权利人所有。第三方组件说明见 [第三方说明](third_party/README.md)。
