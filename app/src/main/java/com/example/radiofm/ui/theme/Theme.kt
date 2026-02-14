package com.example.radiofm.ui.theme

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable

import androidx.compose.runtime.SideEffect

import androidx.compose.ui.graphics.toArgb

import androidx.compose.ui.platform.LocalView

import android.app.Activity

import androidx.core.view.WindowCompat



private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = GrayText,
    background = BgDark,
    surface = SurfaceDark,
    onPrimary = BgDark,
    onSecondary = PureWhite,
    onBackground = OffWhite,
    onSurface = PureWhite,
    surfaceVariant = SurfaceDark.copy(alpha = 0.7f),
    error = ErrorRed
)

@Composable
fun RadioFMTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
