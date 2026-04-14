package com.sergivm.monsterpacks.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Thematic family of a card.
 * Determines the background color shown on the card and in the collection grid.
 */
enum class CardType(
    val displayName: String,
    val color: Color
) {
    INFERNAL("Infernal", Color(0xFFC0392B)),
    ABYSSAL("Abyssal", Color(0xFF1A3A5C)),
    SPECTRAL("Spectral", Color(0xFF4A235A)),
    CELESTIAL("Celestial", Color(0xFFD4AC0D)),
    STORM("Storm", Color(0xFF2874A6)),
    VERDANT("Verdant", Color(0xFF1E8449)),
    FROST("Frost", Color(0xFF5DADE2)),
    ARCANE("Arcane", Color(0xFF7D3C98)),
    SAVAGE("Savage", Color(0xFFD35400)),
    MYTHIC("Mythic", Color(0xFFB7950B))
}
