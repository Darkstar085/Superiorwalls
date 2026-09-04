package com.sipun.superiorwalls.data.repository

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun observeThemeMode(): Flow<ThemeMode> = callbackFlow {
        trySend(themeMode())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME_MODE) trySend(themeMode())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun themeMode(): ThemeMode = preferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }
        ?: ThemeMode.SYSTEM

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun observeInterfaceSettings(): Flow<InterfaceSettings> = callbackFlow {
        trySend(interfaceSettings())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in INTERFACE_KEYS) trySend(interfaceSettings())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun interfaceSettings(): InterfaceSettings = InterfaceSettings(
        amoledTheme = preferences.getBoolean(KEY_AMOLED_THEME, false),
        materialYou = preferences.getBoolean(KEY_MATERIAL_YOU, true),
        colorNavigationBar = preferences.getBoolean(KEY_COLOR_NAVIGATION_BAR, true),
        animationsEnabled = preferences.getBoolean(KEY_ANIMATIONS, true),
    )

    fun setAmoledTheme(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AMOLED_THEME, enabled).apply()
    }

    fun setMaterialYou(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MATERIAL_YOU, enabled).apply()
    }

    fun setColorNavigationBar(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_COLOR_NAVIGATION_BAR, enabled).apply()
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    fun observeStorageSettings(): Flow<StorageSettings> = callbackFlow {
        trySend(storageSettings())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in STORAGE_KEYS) trySend(storageSettings())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun storageSettings(): StorageSettings = StorageSettings(
        highQualityThumbnails = preferences.getBoolean(KEY_HIGH_QUALITY_THUMBNAILS, false),
        downloadOnWifiOnly = preferences.getBoolean(KEY_DOWNLOAD_ON_WIFI_ONLY, true),
        scaleToFit = preferences.getBoolean(KEY_SCALE_TO_FIT, true),
    )

    fun setHighQualityThumbnails(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HIGH_QUALITY_THUMBNAILS, enabled).apply()
    }

    fun setDownloadOnWifiOnly(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DOWNLOAD_ON_WIFI_ONLY, enabled).apply()
    }

    fun setScaleToFit(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SCALE_TO_FIT, enabled).apply()
    }

    fun observeNotificationSettings(): Flow<NotificationSettings> = callbackFlow {
        trySend(notificationSettings())
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in NOTIFICATION_KEYS) trySend(notificationSettings())
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun notificationSettings(): NotificationSettings = NotificationSettings(
        enabled = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AMOLED_THEME = "amoled_theme"
        private const val KEY_MATERIAL_YOU = "material_you"
        private const val KEY_COLOR_NAVIGATION_BAR = "color_navigation_bar"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_HIGH_QUALITY_THUMBNAILS = "high_quality_thumbnails"
        private const val KEY_DOWNLOAD_ON_WIFI_ONLY = "download_on_wifi_only"
        private const val KEY_SCALE_TO_FIT = "scale_to_fit"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private val INTERFACE_KEYS = setOf(
            KEY_AMOLED_THEME,
            KEY_MATERIAL_YOU,
            KEY_COLOR_NAVIGATION_BAR,
            KEY_ANIMATIONS,
        )
        private val STORAGE_KEYS = setOf(
            KEY_HIGH_QUALITY_THUMBNAILS,
            KEY_DOWNLOAD_ON_WIFI_ONLY,
            KEY_SCALE_TO_FIT,
        )
        private val NOTIFICATION_KEYS = setOf(KEY_NOTIFICATIONS_ENABLED)
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class InterfaceSettings(
    val amoledTheme: Boolean,
    val materialYou: Boolean,
    val colorNavigationBar: Boolean,
    val animationsEnabled: Boolean,
)

data class StorageSettings(
    val highQualityThumbnails: Boolean,
    val downloadOnWifiOnly: Boolean,
    val scaleToFit: Boolean,
)

data class NotificationSettings(
    val enabled: Boolean,
)
