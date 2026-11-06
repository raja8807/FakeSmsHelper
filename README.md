# FakeSmsHelper CallLog (Android 11+)

Build this project with GitHub Actions (no Android Studio required).

## Build
Upload the project files to a GitHub repository on `main`. Actions runs automatically.
Download the `FakeSmsHelper-CallLog-debug-apk` artifact from the run summary.

## Install
Extract the artifact and install:
`adb install -r app-debug.apk`

Launch:
`adb shell am start -n com.example.fakesmshelper/.MainActivity`

Tap **Set as default dialer** and approve the Android role prompt. If Android prompts for Call Log permissions, allow them. Then tap one of the test-call buttons.

This inserts local call-log records only; it does not place calls and does not require a SIM. Android/MIUI may restrict call-log writing even when the role is granted. Use only for clearly labeled test data on your own device.
