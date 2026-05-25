package com.gosyria.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    secondary = SecondaryGold,
    onSecondary = Color.Black,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryDark,
    secondary = SecondaryGold,
    onSecondary = Color.Black,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorRed,
)

@Composable
fun GoSyriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // We stick to our brand colors for consistency across devices
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
