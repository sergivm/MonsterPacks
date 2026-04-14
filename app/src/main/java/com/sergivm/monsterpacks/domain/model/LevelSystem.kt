package com.sergivm.monsterpacks.domain.model

/**
 * Defines the XP thresholds for each player level and what they unlock.
 * Values are initial proposals — tune during playtesting.
 */
object LevelSystem {

    data class LevelThreshold(
        val level: Int,
        val xpRequired: Long,   // Cumulative XP needed to reach this level
        val unlocksDescription: String
    )

    val thresholds: List<LevelThreshold> = listOf(
        LevelThreshold(1,       0L,     "Starting level. No upgrades available."),
        LevelThreshold(2,     100L,     "Bonus Pack cooldown upgrade (Tier 1)"),
        LevelThreshold(3,     300L,     "Basic Pack XP upgrade (Tier 1)"),
        LevelThreshold(4,     700L,     "Bonus Pack gem yield upgrade (Tier 1)"),
        LevelThreshold(5,   1_400L,     "Bonus Pack stored count upgrade (Tier 1)"),
        LevelThreshold(6,   2_600L,     "Bonus Pack card count upgrade (Tier 1)"),
        LevelThreshold(7,   4_600L,     "Bonus Pack cooldown upgrade (Tier 2)"),
        LevelThreshold(8,   7_600L,     "Basic Pack XP upgrade (Tier 2)"),
        LevelThreshold(9,  12_100L,     "Bonus Pack gem yield upgrade (Tier 2)"),
        LevelThreshold(10, 18_600L,     "All Tier 3 upgrades unlocked"),
    )

    /** Returns the level corresponding to the given cumulative XP. */
    fun levelForXp(xp: Long): Int {
        var currentLevel = 1
        for (threshold in thresholds) {
            if (xp >= threshold.xpRequired) currentLevel = threshold.level
            else break
        }
        return currentLevel
    }

    /** Returns XP required to reach the next level, or null if at max defined level. */
    fun xpForNextLevel(currentLevel: Int): Long? {
        return thresholds.firstOrNull { it.level == currentLevel + 1 }?.xpRequired
    }

    /** Returns XP progress within the current level band (0..xpNeeded). */
    fun xpProgressInLevel(totalXp: Long, currentLevel: Int): Long {
        val currentLevelXp = thresholds.firstOrNull { it.level == currentLevel }?.xpRequired ?: 0L
        return totalXp - currentLevelXp
    }

    /** Returns XP needed to go from current level to next level. */
    fun xpNeededForNextLevel(currentLevel: Int): Long? {
        val current = thresholds.firstOrNull { it.level == currentLevel }?.xpRequired ?: return null
        val next = thresholds.firstOrNull { it.level == currentLevel + 1 }?.xpRequired ?: return null
        return next - current
    }
}
