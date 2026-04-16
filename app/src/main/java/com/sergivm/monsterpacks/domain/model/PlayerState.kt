package com.sergivm.monsterpacks.domain.model

/**
 * The full mutable state of the player. Persisted via Room.
 */
data class PlayerState(
    val username: String = "",
    val usernameChanged: Boolean = false,
    val coins: Long = 0L,
    val gems: Long = 0L,
    val xp: Long = 0L,
    val level: Int = 1,
    val cardCopies: Map<Int, Int> = emptyMap(),
    
    // Pack Limit System
    val availablePacks: Int = 50,
    val maxPacks: Int = 50,
    val lastPackRegenTimeMs: Long = System.currentTimeMillis(),
    
    // Free Pack System
    val freePackReadyAtMs: Long? = null,
    
    // ── Upgrade Levels ──
    
    // Basic Pack
    val basicPackRarityLevel: Int = 0,
    val basicPackCapacityLevel: Int = 0,
    val basicPackCardCountLevel: Int = 0,
    val basicPackRegenLevel: Int = 0,
    val basicPackXpLevel: Int = 0,
    val xpMultiplierLevel: Int = 0,    // NEW
    val bulkOpenLevel: Int = 0,       // NEW (0: Locked, 1: Unlocked x5)
    
    // Free Pack
    val freePackCooldownLevel: Int = 0,
    val freePackGemYieldLevel: Int = 0,
    val freePackCoinYieldLevel: Int = 0,
    val freePackCardCountLevel: Int = 0,
    val freePackStoredLevel: Int = 0,
    val freePackRarityLevel: Int = 0
) {
    /** Returns true if the player has not yet set a username (first launch). */
    val needsUsername: Boolean get() = username.isBlank()

    /** Returns how many copies the player owns of the given card. */
    fun copiesOf(cardId: Int): Int = cardCopies[cardId] ?: 0

    /** Returns true if the player owns at least one copy of the given card. */
    fun hasCard(cardId: Int): Boolean = copiesOf(cardId) > 0

    /** Returns true if the Free Pack is currently available to open. */
    fun isFreePackReady(nowMs: Long): Boolean =
        freePackReadyAtMs == null || nowMs >= freePackReadyAtMs

    /** Returns milliseconds remaining until Free Pack is ready. 0 if already ready. */
    fun freePackCooldownRemainingMs(nowMs: Long): Long =
        if (isFreePackReady(nowMs)) 0L
        else (freePackReadyAtMs!! - nowMs).coerceAtLeast(0L)
        
    /** Returns milliseconds remaining until next basic pack is regenerated. */
    fun nextPackRegenRemainingMs(nowMs: Long, regenIntervalMs: Long): Long {
        if (availablePacks >= maxPacks) return 0L
        val elapsed = nowMs - lastPackRegenTimeMs
        return (regenIntervalMs - (elapsed % regenIntervalMs)).coerceAtLeast(0L)
    }

    /** Returns true if Bulk Open feature is unlocked. */
    val isBulkOpenUnlocked: Boolean get() = bulkOpenLevel > 0
}
