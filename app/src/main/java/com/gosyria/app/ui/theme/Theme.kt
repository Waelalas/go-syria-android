package com.gosyria.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary          = Blue40,
    onPrimary        = White,
    primaryContainer = Blue80,
    secondary        = Green40,
    onSecondary      = White,
    background       = Surface,
    surface          = White,
)

private val DarkColors = darkColorScheme(
    primary          = Blue80,
    onPrimary        = Black,
    primaryContainer = Blue40,
    secondary        = Green80,
    onSecondary      = Black,
    background       = SurfaceDark,
    surface          = Color(0xFF2C2C2E),
)

@Composable
fun GoSyriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
