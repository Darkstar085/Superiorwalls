# Superiorwalls

A clean rebuild of Superiorwalls for modern Android and Jetpack Compose.

The new implementation uses the original application as a behavioral reference while rebuilding the architecture from scratch.

## Build with Android Studio

1. Open the repository root in Android Studio.
2. Use a recent stable Android Studio release compatible with Android Gradle Plugin 9.4.0.
3. Make sure JDK 17 is selected for Gradle.
4. Install Android SDK API 37 and the Android 37 build tools when Android Studio prompts you.
5. Sync the project with Gradle files.
6. Select the `app` run configuration and launch it on an emulator or connected device.

The project uses Gradle 9.6.1, Kotlin 2.4.10, Jetpack Compose, Material 3, Room, Coil, and Retrofit.

## Included behavior

- Cloud wallpaper browsing with offline Room-backed caching
- Pull-to-refresh and cached/offline states
- Collections and collection browsing
- Favorites
- Wallpaper details and metadata
- Set wallpaper and save to gallery
- Share wallpaper URLs
- Import images from Android image intents
- Persistent System / Light / Dark appearance settings
- Adaptive phone and tablet navigation

Live wallpapers and Muzei integration are intentionally out of scope for this rebuild.
