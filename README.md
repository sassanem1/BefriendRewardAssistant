# Befriend Reward Assistant (starter project)

Android Studio project targeting Android 13+ (min Android 8). Package watched: `com.guppir.swipr`.

## What it does
- Provides a small floating control panel with Start/Stop and Hide.
- Uses Android Accessibility to look for clearly labelled ad close/dismiss controls when the active window is not Befriend.
- When Befriend is active, looks for clearly labelled `Claim`, `Collect`, `Claim reward`, `Collect reward`, `Claim gems`, or `Collect gems` controls.
- Writes a small activity log in app preferences.

It does **not** skip an ad before its close control is made available, fake an ad view, alter Befriend, or guarantee a reward. The ad close control must be exposed through Android accessibility. Some ad SDKs draw controls in a way that accessibility cannot inspect. In that case this starter intentionally does nothing rather than guessing screen coordinates.

## Build
1. Open this folder in Android Studio (recent version with Android Gradle Plugin 8.7.3 support).
2. Let Gradle sync and install the Android SDK Platform 35 if prompted.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install the debug APK on the Android 13 phone.

This environment did not have the Android SDK/Gradle installed, so no APK was compiled here. The source project is provided for building in Android Studio.

## Enable
1. Open the app.
2. Tap **Enable Accessibility Service** and enable Befriend Reward Assistant in Android Settings.
3. Return to the app and tap **Start automation**.
4. Open Befriend and test with one ad while watching the phone. Use the floating Start/Stop control to pause it.

## Important limitations / tuning
- Android does not provide a universal, trusted `rewarded ad completed` signal to unrelated apps. This implementation treats the appearance of a clearly labelled close/dismiss control as the ad SDK's indication that closing is available.
- Reward button labels and ad close controls vary. If Befriend uses icon-only controls or custom-rendered buttons, accessibility may not expose them. The matcher can then be extended using observed accessibility node text/resource IDs; do not add blind coordinate taps.
- Accessibility access is powerful. Enable this service only if you trust this source code, and disable it in Android Settings when you are not using it.
- This is an initial test build, not a verified Befriend-compatible release. Verify the reward balance manually after each test.
