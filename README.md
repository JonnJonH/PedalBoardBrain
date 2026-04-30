# Pedalboard Recreator App

An Android app for musicians to document and recreate their analogue guitar pedalboards with precise visual accuracy.

## Features
- **Session Management**: Group pedalboards by Song, Section, and Part.
- **Pedal Capture**: Use CameraX to capture pedalboard overviews and individual pedals.
- **Cropping & UI**: Drag and crop pedal images.
- **Signal Chain**: Support for strictly linear Pre-Amp routing and complex branching (Stereo/Parallel) in the FX Loop.
- **Export & Import**: Export sessions (with JSON and zipped image directories) and import them losslessly.

## How to open in Android Studio
1. Open Android Studio.
2. Select **File** -> **Open**.
3. Navigate to this project folder (the folder containing uild.gradle.kts and settings.gradle.kts).
4. Select the folder and click **OK**.
5. Android Studio will automatically recognize the pp module and run a Gradle Sync.

## How to run on device
1. Connect your device (e.g. Pixel 9 Pro) via USB and ensure **USB Debugging** is enabled in Developer Options.
2. In Android Studio, select your device from the deployment target dropdown in the top toolbar.
3. Click the green **▶ Run** button (or press Shift + F10). The app will compile and install automatically.

## How to build APK
1. In Android Studio, go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2. Once the build finishes, a popup will appear in the bottom right corner with a link to "locate" the APK file.
3. Alternatively, to build a release APK for sideloading, go to **Build** > **Generate Signed Bundle / APK...**, select APK, create or provide a keystore, and click Finish.

## Project Structure
- pp/src/main/java/com/pedalboard/recreator/data: Room DB, Entities, and Daos.
- pp/src/main/java/com/pedalboard/recreator/ui: Jetpack Compose Screens and Theming.
- pp/src/main/java/com/pedalboard/recreator/utils: ZIP management for Export/Import.
