package com.sipun.superiorwalls.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
