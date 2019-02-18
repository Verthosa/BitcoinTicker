# BitcoinTicker
Native Android demo application for cryptocurrency ticker and news notifications with android auto support

To make it work with android auto, the application needs to mimick an android auto app. This is because android auto apps only supports notifications for messaging applications such as WhatsApp/Messenger/SMS - for safety reasons obviously. This is also the reason that, at the moment, i cannot get this app in the Play Store. 

We can however install it as an android vending app and then it works. 

Use following steps to force the app working:

1) build as release APK
2) adb push mobile-release.apk /sdcard/app.apk
3) adb shell pm install -i "com.android.vending" -r /sdcard/app.apk
4) adb shell rm /sdcard/app.apk

The com.android.vending part of the installation mimics the fact that you downloaded the app from the Play Store. This way it will be supported by Android Auto. Now your notifications should be shown on your car display.
