# Build the APK online with GitHub Actions

This project includes `.github/workflows/android-apk.yml`.

1. Create a GitHub account and a **new repository**.
2. Extract this ZIP on your computer.
3. In the new GitHub repository, choose **Add file → Upload files**.
4. Upload the *contents* of the `BefriendRewardAssistant` folder (including the hidden `.github` folder). Commit to the `main` branch.
5. Open the repository's **Actions** tab. If GitHub asks, enable workflows.
6. Select **Build Befriend Reward Assistant APK** and click **Run workflow** (or let the first push trigger it).
7. When the run finishes successfully, open that run and download the artifact named `BefriendRewardAssistant-debug`.
8. Extract the downloaded artifact and install `app-debug.apk` on your Android 13 phone. If prompted, allow installation from that browser/file manager.

This is a debug APK for personal testing, not a Play Store release. Keep the repository private if you do not want the source code public. Never put signing keys or passwords in the repository.

The build workflow uses Java 17 and Gradle 8.9 in GitHub's cloud runner. It does not require Android Studio or a local Android SDK.
