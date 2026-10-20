# Xposed API 82

`xposed-api-82.jar` is the original compile-only Xposed API; it is never packaged in the APK.

- Source: https://api.xposed.info/de/robv/android/xposed/api/82/api-82.jar
- SHA-256: f48c635f1c7469fdec0e00ad2ea0b7a6b2f5b55065784a35b7ca3a84615e8e25
- Project: https://github.com/rovo89/XposedBridge
- Upstream Apache-2.0 notice: https://github.com/rovo89/XposedBridge/blob/art/NOTICE.txt
- Preserved copy: [NOTICE-Xposed.txt](NOTICE-Xposed.txt)

The Gradle Wrapper is distributed by Gradle under Apache-2.0; its existing copyright notices are preserved in the wrapper scripts. See https://github.com/gradle/gradle/blob/v8.11.1/LICENSE.

## Rootless integrated releases

The optional complete APK embeds the unmodified runtime from [JingMatrix/LSPatch v1.2 (487)](https://github.com/JingMatrix/LSPatch/releases/tag/v1.2), built from its [v1.2 source tree](https://github.com/JingMatrix/LSPatch/tree/v1.2).

- Patcher: `lspatch-v1.2-487-release.jar` (downloaded separately; not checked in).
- SHA-256: `d238fdc414d121b7fa454d8b4ccf420df3a8c97d563761861ff92bd9c5da2165`.
- License: [GNU GPL v3](https://github.com/JingMatrix/LSPatch/blob/v1.2/LICENSE); a copy is included as `LSPatch-GPL-3.0.txt` in the release assets.
- Build procedure: [rootless guide](../docs/rootless.md), using `tools/build_rootless.py` with a user-supplied original APK.

Original module code remains Apache-2.0. The upstream Jiaowoban application and resources remain the property of their respective rights holders. No application signing private keys or device account data are included in the release.
