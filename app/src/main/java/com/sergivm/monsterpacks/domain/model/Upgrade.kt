package com.sergivm.monsterpacks.domain.model

/**
 * Represents a single tier of a player upgrade available in the Shop.
 *
 * @param id            Unique identifier (e.g. "bonus_cooldown_t1").
 * @param name          Display name.
 * @param description   Short description of what this upgrade does.
 * @param currentValue  Human-readable current value (e.g. "10:00 min").
 * @param nextValue     Human-readable next value (e.g. "9:30 min").
 * @param cost          Resource cost to purchase.
 * @param requiredLevel Player level required to unlock this upgrade.
 * @param tier          Tier number (1-based).
 * @param maxTier       Total number of tiers for this upgrade.
 */
data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val currentValue: String,
    val nextValue: String,
    val cost: PackCost,
    val requiredLevel: Int,
    val tier: Int,
    val maxTier: Int
) {
    val isMaxTier: Boolean get() = tier >= maxTier
}

/**
 * The result of the player attempting to purchase an upgrade.
 */
sealed class UpgradeResult {
    object Success : UpgradeResult()
    object InsufficientFunds : UpgradeResult()
    object LevelTooLow : UpgradeResult()
    object AlreadyMaxTier : UpgradeResult()
}
