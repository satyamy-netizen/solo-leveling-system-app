package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SoloColorScheme = darkColorScheme(
    primary = SoloBlueAccent,
    secondary = SoloPurpleAccent,
    background = SoloBlack,
    surface = SoloCardBg,
    onPrimary = SoloBlack,
    onSecondary = SoloTextPrimary,
    onBackground = SoloTextPrimary,
    onSurface = SoloTextPrimary,
    tertiary = SoloGold
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode for extreme Solo Leveling theme feel!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SoloColorScheme,
        typography = Typography,
        content = content
    )
}
