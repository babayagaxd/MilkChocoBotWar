# MILK CHOCO BOT WAR

A native Android app-slot launcher with a chocolate-themed 2×2 dashboard.

## Features
- Four independent app slots.
- Pick any visible launchable installed app.
- App name and icon shown in each assigned slot.
- Launch, replace, and remove each slot independently.
- Assignments persist using SharedPreferences.
- Reset all four slots without uninstalling apps.
- Responsive Compose layout and resizable activity support.

## Build
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder in Android Studio.
3. Allow Gradle sync to finish.
4. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
5. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

Or from a machine with Gradle and Android SDK configured:
`gradle assembleDebug`

## Android limitations
This app is an app launcher/dashboard. Android does not provide a public API for an ordinary app to embed arbitrary third-party app activities inside four custom tiles. Standard phone split-screen is normally two apps; four simultaneous app windows require OEM/device freeform or desktop-windowing support. The dashboard cannot force other apps into four windows.

`QUERY_ALL_PACKAGES` is included to let the sideloaded app enumerate installed launchable apps. Google Play treats broad installed-app visibility as sensitive and restricts this permission; a Play Store release may need a narrower app-picker design or policy approval.

## Identity
Application ID: `com.milkchoco.botwar`
Version: `1.0`
Minimum Android: 6.0 (API 23)
Target SDK: 35


