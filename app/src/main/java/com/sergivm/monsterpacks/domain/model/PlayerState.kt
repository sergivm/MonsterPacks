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
    
    // Upgrade Levels
    val basicPackRarityLevel: Int = 0,    // Increases drop weights for EPIC+
    val basicPackCapacityLevel: Int = 0,  // Increases maxPacks (Initial 50)
    val basicPackCardCountLevel: Int = 0, // Increases cards per pack (Initial 5, Max 10)
    val basicPackXpLevel: Int = 0,        // Increases XP from basic packs
    
    val freePackCooldownLevel: Int = 0,
    val freePackGemYieldLevel: Int = 0,
    val freePackCardCountLevel: Int = 0,
    val freePackStoredLevel: Int = 0
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
}
