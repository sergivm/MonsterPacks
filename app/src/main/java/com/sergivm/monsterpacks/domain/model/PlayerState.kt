package com.sergivm.monsterpacks.domain.model

/**
 * The full mutable state of the player. Persisted via Room.
 *
 * @param username                Player name entered on first launch.
 * @param usernameChanged         True once the username has been changed. Blocks further changes.
 * @param coins                   Current coin balance.
 * @param gems                    Current gem balance.
 * @param xp                      Total accumulated XP.
 * @param level                   Current player level (derived from XP at load time).
 * @param cardCopies              Map of cardId -> copy count. A count of 0 means locked.
 * @param bonusPackReadyAtMs      Epoch milliseconds when the next Bonus Pack is ready.
 *                                Null if no cooldown has started.
 * @param basicPackUpgradeLevel   Current upgrade tier for the Basic Pack.
 * @param bonusPackUpgradeLevel   Current upgrade tier for the Bonus Pack.
 */
data class PlayerState(
    val username: String = "",
    val usernameChanged: Boolean = false,
    val coins: Long = 0L,
    val gems: Long = 0L,
    val xp: Long = 0L,
    val level: Int = 1,
    val cardCopies: Map<Int, Int> = emptyMap(),
    val bonusPackReadyAtMs: Long? = null,
    val basicPackUpgradeLevel: Int = 0,
    val bonusPackUpgradeLevel: Int = 0
) {
    /** Returns true if the player has not yet set a username (first launch). */
    val needsUsername: Boolean get() = username.isBlank()

    /** Returns how many copies the player owns of the given card. */
    fun copiesOf(cardId: Int): Int = cardCopies[cardId] ?: 0

    /** Returns true if the player owns at least one copy of the given card. */
    fun hasCard(cardId: Int): Boolean = copiesOf(cardId) > 0

    /** Returns true if the Bonus Pack is currently available to open. */
    fun isBonusPackReady(nowMs: Long): Boolean =
        bonusPackReadyAtMs == null || nowMs >= bonusPackReadyAtMs

    /** Returns milliseconds remaining until Bonus Pack is ready. 0 if already ready. */
    fun bonusPackCooldownRemainingMs(nowMs: Long): Long =
        if (isBonusPackReady(nowMs)) 0L
        else (bonusPackReadyAtMs!! - nowMs).coerceAtLeast(0L)
}
