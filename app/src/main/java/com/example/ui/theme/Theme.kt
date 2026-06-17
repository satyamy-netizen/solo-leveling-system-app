package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val currentTheme = ThemeHolder.activeTheme
    val isLight = currentTheme == "heaven" || currentTheme == "heven"

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = SoloBlueAccent,
            secondary = SoloPurpleAccent,
            background = SoloDarkGrey, // Mapped to Light Celestic soft grey/cream in heaven theme
            surface = SoloCardBg, // Mapped to pure translucent white in heaven theme
            onPrimary = Color.White,
            onSecondary = SoloTextPrimary,
            onBackground = SoloTextPrimary,
            onSurface = SoloTextPrimary,
            tertiary = SoloGold
        )
    } else {
        darkColorScheme(
            primary = SoloBlueAccent,
            secondary = SoloPurpleAccent,
            background = SoloBlack,
            surface = SoloCardBg,
            onPrimary = Color.Black,
            onSecondary = SoloTextPrimary,
            onBackground = SoloTextPrimary,
            onSurface = SoloTextPrimary,
            tertiary = SoloGold
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
