package com.sergivm.monsterpacks.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Card rarity. Controls drop probability, frame design, animation tier, and rewards.
 * Cards in a pack are always revealed in ascending rarity order (ordinal ascending).
 */
enum class Rarity(
    val displayName: String,
    val icon: String,
    val color: Color,
    val coinReward: Int,
    val gemReward: Int,
    // Base drop weight for free-roll slots. Normalised at runtime.
    val baseDropWeight: Float
) {
    COMMON(
        displayName = "Common",
        icon = "❇️",
        color = Color(0xFF6C757D),
        coinReward = 1,
        gemReward = 0,
        baseDropWeight = 58f
    ),
    RARE(
        displayName = "Rare",
        icon = "⚡",
        color = Color(0xFF2874A6),
        coinReward = 2,
        gemReward = 0,
        baseDropWeight = 35f
    ),
    EPIC(
        displayName = "Epic",
        icon = "💥",
        color = Color(0xFF7D3C98),
        coinReward = 10,
        gemReward = 0,
        baseDropWeight = 5f
    ),
    SPECIAL(
        displayName = "Special",
        icon = "⭐",
        color = Color(0xFFB7950B),
        coinReward = 100,
        gemReward = 10,
        baseDropWeight = 1f
    ),
    LEGENDARY(
        displayName = "Legendary",
        icon = "☄️",
        color = Color(0xFFC0392B),
        coinReward = 500,
        gemReward = 50,
        baseDropWeight = 0.1f
    ),
    GOD(
        displayName = "God",
        icon = "🪐",
        color = Color(0xFF1A5276),
        coinReward = 2000,
        gemReward = 200,
        baseDropWeight = 0.01f
    );

    /** Returns true for rarities that can have a text description on the card. */
    val hasDescription: Boolean get() = this <= EPIC

    /** Returns true for variant cards (Special and above). */
    val isVariant: Boolean get() = this >= SPECIAL
}
