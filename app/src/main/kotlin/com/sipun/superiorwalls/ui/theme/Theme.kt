package com.sipun.superiorwalls.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sipun.superiorwalls.R

val LocalAnimationsEnabled = compositionLocalOf { true }

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F5D92),
    secondary = Color(0xFF5B5F71),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    secondary = Color(0xFFC4C6DD),
)

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

private val Syne = FontFamily(
    Font(R.font.syne_regular, FontWeight.Normal),
    Font(R.font.syne_semibold, FontWeight.SemiBold),
    Font(R.font.syne_bold, FontWeight.Bold),
)

private val SuperiorwallsTypography = Typography(
    displayLarge = TextStyle(fontFamily = Syne, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = Syne, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontFamily = Syne, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = Syne, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = Syne, fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    headlineSmall = TextStyle(fontFamily = Syne, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 18.sp),
    bodyMedium = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodySmall = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    labelMedium = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 12.sp),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SuperiorwallsTypography,
    ) {
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
