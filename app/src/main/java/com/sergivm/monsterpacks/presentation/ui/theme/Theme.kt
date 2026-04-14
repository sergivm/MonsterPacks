package com.sergivm.monsterpacks.presentation.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = AccentGold,
    onPrimary        = Color.Black,
    primaryContainer = AccentGoldDim,
    background       = BackgroundDark,
    onBackground     = OnSurface,
    surface          = SurfaceDark,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariant,
    secondary        = GemColor,
    onSecondary      = Color.White
)

@Composable
fun MonsterPacksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = MonsterPacksTypography,
        content     = content
    )
}
