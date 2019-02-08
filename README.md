# BitcoinTicker
Sample application for cryptocurrency ticker and news notifications with android auto support

To make it work with android auto, the application needs to mimick an android auto app. Use following steps:

1) build as release APK
2) adb push mobile-release.apk /sdcard/app.apk
3) adb shell pm install -i "com.android.vending" -r /sdcard/app.apk
4) adb shell rm /sdcard/app.apk

Now your notifications will also be shown on Android Auto dashboard.
