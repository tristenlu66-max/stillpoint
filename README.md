# 守一 / Stillpoint

> 一个安静的 Android 专注白名单工具。 / A quiet Android focus whitelist app.

[中文](#中文) · [English](#english)

---

<a id="中文"></a>

## 中文

**守一**帮助你在一段专注时间里，只保留真正需要的应用。开始专注后，白名单外的应用会被温和地遮住，提醒你回到当下。

### 功能

- 专注白名单：仅允许你选择的应用正常使用。
- 应用搜索与按字母快速定位。
- 双通道守护：无障碍服务监听窗口变化，前台守护服务作为后备检查。
- 简洁的湖水蓝界面与低存在感的点状图标。
- 专注期间显示常驻通知，减少后台被系统回收的概率。
- 自动放行必要系统组件：桌面、设置、权限弹窗、键盘、文件选择器、相机与电话等。

### 下载与安装

当前版本：**v0.2.6**（Android 8.0 / API 26 及以上）

下载：[守一 v0.2.6 APK](https://github.com/tristenlu66-max/stillpoint/releases/tag/v0.2.6)

在部分 Android / vivo 设备上，侧载应用的无障碍服务可能被标记为“受限制的设置”。请在系统的应用详情页允许“受限制的设置”后，再开启 **守一专注拦截**。这是 Android 的安全机制，应用不能自行绕过或自行开启无障碍权限。

### 首次使用

1. 安装 APK，打开守一。
2. 授予“使用情况访问”权限，并开启 **守一专注拦截** 无障碍服务。
3. 搜索并选择专注时仍可使用的应用。
4. 设置任务与时长，点击“开始守一”。

### 隐私与边界

- 所有专注状态和白名单仅保存在本机。
- 不使用 VPN，不修改 DNS、Wi-Fi、移动网络或其他应用的配置。
- 不杀后台进程，不使用 Device Owner、Device Admin、Kiosk 或 Lock Task。
- 无障碍权限仅用于判断当前前台应用，并在专注时显示本应用的拦截浮层。

### 构建

用 Android Studio 打开项目根目录并同步 Gradle；或在已配置 Android SDK 与 JDK 17 的环境中运行：

```bash
./gradlew :app:assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

> 这是个人侧载使用的项目。若计划发布到 Google Play，请先根据 Google Play Accessibility API 政策完成权限必要性审查、隐私披露与用户同意流程。

[回到顶部](#守一--stillpoint)

---

<a id="english"></a>

## English

**Stillpoint** is a quiet Android focus whitelist app. During a focus session, only the apps you intentionally allow remain usable; other apps are gently covered with a reminder to return to the present task.

### Features

- Focus whitelist: only selected apps stay available.
- App search and an alphabetical quick-jump rail.
- Two protection paths: an Accessibility Service watches window changes, while a foreground guard service provides a fallback check.
- A restrained lake-blue interface and a minimal dot icon.
- An ongoing notification during a session to reduce background termination.
- Necessary system surfaces stay available: Home, Settings, permission dialogs, keyboards, document pickers, camera, and phone apps.

### Download and install

Current version: **v0.2.6** (Android 8.0 / API 26+)

Download: [Stillpoint v0.2.6 APK](https://github.com/tristenlu66-max/stillpoint/releases/tag/v0.2.6)

On some Android / vivo devices, an Accessibility Service from a sideloaded app may be marked as a restricted setting. In the system app-details screen, allow restricted settings first, then enable **Stillpoint Focus Blocker**. This is an Android security safeguard; the app cannot enable or bypass this permission by itself.

### First use

1. Install the APK and open Stillpoint.
2. Grant Usage Access and enable the **Stillpoint Focus Blocker** Accessibility Service.
3. Search for and select the apps you need during focus.
4. Set a task and duration, then tap “Start Stillpoint.”

### Privacy and boundaries

- Focus state and the whitelist remain on-device.
- The app does not use VPN, or modify DNS, Wi-Fi, mobile networking, or other apps’ settings.
- It does not kill background processes or use Device Owner, Device Admin, kiosk, or Lock Task modes.
- Accessibility is used only to identify the foreground app and show this app’s blocking overlay during an active session.

### Build

Open the project root in Android Studio and sync Gradle, or run the following in an environment configured with the Android SDK and JDK 17:

```bash
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

> This is a personal sideloading project. Before distributing through Google Play, review the Accessibility API policy, necessity of permissions, privacy disclosures, and consent flow.

[Back to top](#守一--stillpoint)
