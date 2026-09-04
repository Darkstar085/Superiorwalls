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

    companion object {
        private const val PREFERENCES_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
