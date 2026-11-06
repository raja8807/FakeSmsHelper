# Fake SMS Helper (Android 11 / MIUI)

This Android Studio sample is intended for inserting clearly labeled test SMS records on your own device, without root.

## Build
1. Open this folder in Android Studio.
2. Allow Gradle sync (JDK 17 recommended; Android Gradle Plugin 8.6.1).
3. Build > Build APK(s).
4. APK path: `app/build/outputs/apk/debug/app-debug.apk`

## Install and grant role
From Windows CMD, in this project folder:

```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.example.fakesmshelper/.MainActivity
```

On the phone, tap **Set as default SMS app** and approve the system prompt. This temporarily replaces MIUI Messages as default. Restore MIUI Messages afterward in Settings > Apps > Default apps > SMS app.

## Insert test SMS via ADB
The helper Activity accepts an explicit action and extras:

```cmd
adb shell am start -n com.example.fakesmshelper/.MainActivity -a com.example.fakesmshelper.INSERT_TEST_SMS --es address 9876543210 --es body "TEST ONLY: Hello from ADB" --el date 1793940000000 --ez read false
```

The timestamp is milliseconds since Unix epoch. `read false` means unread. Verify:

```cmd
adb shell content query --uri content://sms/inbox --projection _id:address:body:date:type --where "address='9876543210'"
```

## Notes
- Android may require role eligibility components and OEM-specific handling. This sample provides SMS_DELIVER receiver and RESPOND_VIA_MESSAGE service declarations, but it is a minimal helper and may need adjustment if MIUI rejects the role request.
- This app does not send SMS over the cellular network; it inserts a local test record only.
- Android role takeover can affect the usual Messages app until the original default is restored.
