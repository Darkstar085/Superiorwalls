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

    companion object {
        private const val PREFERENCES_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AMOLED_THEME = "amoled_theme"
        private const val KEY_MATERIAL_YOU = "material_you"
        private const val KEY_COLOR_NAVIGATION_BAR = "color_navigation_bar"
        private const val KEY_ANIMATIONS = "animations"
        private val INTERFACE_KEYS = setOf(
            KEY_AMOLED_THEME,
            KEY_MATERIAL_YOU,
            KEY_COLOR_NAVIGATION_BAR,
            KEY_ANIMATIONS,
        )
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
