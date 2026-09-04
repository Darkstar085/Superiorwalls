package com.sipun.superiorwalls.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

val LocalAnimationsEnabled = compositionLocalOf { true }

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F5D92),
    secondary = Color(0xFF5B5F71),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    secondary = Color(0xFFC4C6DD),
)

@Composable
fun SuperiorwallsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useMaterialYou: Boolean = true,
    useAmoledTheme: Boolean = false,
    colorNavigationBar: Boolean = true,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val dynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && useMaterialYou
    val colorScheme = when {
        dynamicColors && darkTheme -> dynamicDarkColorScheme(context).let { scheme ->
            if (useAmoledTheme) scheme.copy(background = Color.Black, surface = Color.Black) else scheme
        }
        dynamicColors -> dynamicLightColorScheme(context)
        darkTheme && useAmoledTheme -> DarkColors.copy(background = Color.Black, surface = Color.Black)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme) {
        val activity = context as? Activity
        val navigationBarColor = if (colorNavigationBar) colorScheme.surface.toArgb() else Color.Transparent.toArgb()
        SideEffect {
            activity?.window?.navigationBarColor = navigationBarColor
        }
        CompositionLocalProvider(LocalAnimationsEnabled provides animationsEnabled) {
            content()
        }
    }
}
