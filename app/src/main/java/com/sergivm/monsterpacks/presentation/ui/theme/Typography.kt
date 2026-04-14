package com.sergivm.monsterpacks.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Replace with a fantasy-undertone font (e.g. Cinzel, Rajdhani, or Exo 2)
//       Add the font files to res/font/ and load via FontFamily before shipping.
val MonsterPacksTypography = Typography(
    // Screen titles ("SHOP", "SETTINGS", "COLLECTION TITLE")
    headlineLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = 2.sp
    ),
    // Section or card name headers
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.sp
    ),
    // Sub-headings, upgrade names
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Body / descriptions
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    // Small labels (copy count, card number, cooldown)
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.4.sp
    )
)
