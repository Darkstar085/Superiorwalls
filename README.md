<h1 align="center">Superiorwalls</h1>

<p align="center">
  A modern rebuild of Superiorwalls for Android, powered by Jetpack Compose.
</p>

## Introduction

**Superiorwalls** is a modern Android rebuild of the original [Superiorwalls](https://github.com/Darkstar085/Superiorwalls-deprecated) application, created for a clean and familiar wallpaper browsing experience.

The original application is used as a behavioral reference while the new implementation is rebuilt from scratch with a modern Android architecture.

## Features

- Material 3 interface
- Jetpack Compose UI
- Material You and dynamic theming support
- System, light, and dark themes
- Cloud-based wallpaper browsing
- Offline browsing with local caching
- Pull-to-refresh
- Wallpaper collections and categories
- Favorites
- Wallpaper details and metadata
- Set wallpapers directly from the app
- Save wallpapers to the device gallery
- Share wallpaper URLs
- Import images through Android image intents
- Adaptive navigation for phones and tablets

Live wallpapers and Muzei integration are intentionally out of scope for this rebuild.

## Installation

Download the latest APK from the [Releases](https://github.com/Darkstar085/Superiorwalls-next/releases) section and install it on an Android device.

To set a wallpaper from the app:

1. Browse the available wallpapers
2. Select the wallpaper you want
3. Open the wallpaper details
4. Tap **Set Wallpaper**

## Build

The project includes the Gradle wrapper, so Gradle does not need to be installed separately.

### Linux / macOS

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

### Windows

```bat
gradlew.bat assembleDebug
```

The generated APK can be found under:

```text
app/build/outputs/apk/debug/
```

For a release build:

```bash
./gradlew assembleRelease
```

## Requirements

- Android Studio with support for the project's Android Gradle Plugin version
- JDK 17
- Android SDK 37
- Android SDK Build Tools for API 37
- Android 8.0 (API 26) or newer

## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Android Gradle Plugin 9.4.0**
- **Gradle 9.7.1**
- **Kotlin 2.4.10**
- **Room 3.0.2**
- **Coil 3.6.1**
- **Retrofit 3.0.0**
- **KSP**

## Contributions

Contributions are welcome!

If you would like to improve the application, fix a bug, or add a useful feature, feel free to open an issue or submit a pull request.

Please keep changes focused and follow the existing Kotlin and Jetpack Compose conventions.

## License

Superiorwalls is licensed under the [MIT License](LICENSE).

---

<p align="center">
  <b>Superiorwalls</b> — a clean, modern rebuild of Superiorwalls for Android.
</p>
