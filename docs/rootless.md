# 免 root 完整版与外观设置

日期：2026-10-21，Asia/Shanghai。

## 使用方式

完整安装包：[下载 v0.2.1 免 root APK](https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/jiaowoban-3.6.5-dark-rootless.apk)。[发布说明和文件校验值](https://github.com/ZJFILOVE/jiaowoban-dark/releases/tag/v0.2.1)。本地构建输出为 `artifacts/rootless/jiaowoban-3.6.5-dark-rootless.apk`。

这是交我办 3.6.5（versionCode 85）与交我办深色模块 0.2.1 的个人集成构建。安装后直接打开交我办，首页右上角进入个人菜单，再点击「外观」。无需另装模块、管理器、Shizuku，也不需要 root。模块代码使用 Android 10 起的界面 API；Android 10 以下不支持。

- 跟随系统：默认选项，随手机系统的深浅色切换。
- 浅色：交我办保持浅色。
- 深色：交我办保持深色。

三种选择保存在交我办私有设置中，立即生效并在重开后保留，不更改系统主题。独立外观页使用蓝色标题栏、浅深色预览卡片及单选状态；菜单有与原有图标尺寸及文字对齐的双颜色月亮图标。

## 安装与更新

完整包的安装签名与官方版本不同。通常需要卸载官方版本后再安装本包，卸载会清除该应用的本地数据及登录状态。请先保存重要本地内容并确认可以重新登录；不要直接尝试覆盖官方版本。此次没有卸载或替换已连接手机上的原版交我办。

同一台构建电脑保留了个人签名密钥，后续用同一密钥构建的包可尝试覆盖本包。不能直接安装官方更新覆盖本包；恢复官方版也需要先卸载本包再安装官方版。第三方登录、支付、推送、应用更新等依赖签名的功能，需要安装后单独验证。

已经使用 LSPosed 的手机可更新 [v0.2.1 独立模块](https://github.com/ZJFILOVE/jiaowoban-dark/releases/download/v0.2.1/jiaowoban-dark-0.2.1-debug.apk)，继续保留官方交我办。

## 构建

1. 使用 JDK 17 或 21、SDK 36 和 Build Tools 36.0.0，先执行 `./gradlew :app:assembleDebug`。
2. 从用户自己的设备取得原版 APK，保存在 Git 忽略的 `artifacts/rootless-inputs/` 下。
3. 从 [JingMatrix/LSPatch v1.2](https://github.com/JingMatrix/LSPatch/releases/tag/v1.2) 下载 `lspatch-v1.2-487-release.jar`。
4. 执行：

```sh
python3 tools/build_rootless.py \
  artifacts/rootless-inputs/jiaowoban-3.6.5-original.apk \
  artifacts/rootless-inputs/lspatch-v1.2-487-release.jar
```

脚本核对补丁器 SHA-256，仅接受交我办包名，将模块内置为集成模式，并执行 ZIP 对齐和重新签名。原版、工具、签名密钥和密码仅保存在忽略目录中；密钥和密码文件以受限权限写入。保管好整个 `artifacts/rootless-inputs/signing/`，后续更新需要复用。

补丁器 SHA-256：`d238fdc414d121b7fa454d8b4ccf420df3a8c97d563761861ff92bd9c5da2165`。

运行时使用 LSPatch 的集成模式（`useManager=false`），签名兼容级别 2。该兼容选项不会让 Android 将个人签名视为官方签名。LSPatch 原始来源与 GPL-3.0 许可证见上述发布及其 [v1.2 源码](https://github.com/JingMatrix/LSPatch/tree/v1.2)；本项目原创模块仍采用 Apache-2.0。APK 通过 GitHub Releases 提供，不写入 Git 历史；签名私钥、密码和原始设备记录不发布。

## 验证范围

- 新增菜单、独立外观页、三种选择的单选状态，已在 PKJ110 / Android 16 上通过现有 LSPosed 加载同一模块代码验证。
- 浅色选项在应用重开后恢复，且系统仍为深色；之后测试深色、浅色和跟随系统，最后恢复跟随系统。
- 外观页的浅色、深色均检查过实际截图，检查返回个人菜单与入口图标。没有执行硬重启或系统软重启。
- 模块构建和原有 4 项对比度测试通过；Lint 无错误。少量警告涉及工具更新、动态查找宿主资源、中文字符串拼接和现有备份设置。
- 完整包执行 APK 签名验证、16 KiB ZIP 对齐检查、ZIP CRC 检查，并核对内置模块和原版 APK 的字节一致性，以及 `useManager=false`。
- **免 root 完整包尚未在无 root 设备上安装和登录验证。** 上述实机功能验证使用既有 LSPosed 模块加载方式，不代表已验证集成运行时、完整包登录、支付、推送或所有业务服务。后续验收应使用另一台设备，或在用户自行处理原版数据后安装完整包。

截图、UI 记录、原版 APK 和完整日志均不提交到 Git。
