package com.sergivm.monsterpacks.domain.engine

import com.sergivm.monsterpacks.domain.model.*
import kotlin.random.Random
import java.util.concurrent.TimeUnit

/**
 * Pure game logic engine. No Android or Compose imports allowed.
 */
object GameEngine {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Base probability (0..1) that a Basic Pack triggers a Surprise upgrade event. */
    const val SURPRISE_EVENT_PROBABILITY = 0.005f

    /** Base time to regenerate one pack (30 minutes). */
    private val BASE_PACK_REGEN_TIME_MS = TimeUnit.MINUTES.toMillis(30)

    /** Weighted chances for each surprise result. Weights are relative, not percentages. */
    val SURPRISE_EVENT_WEIGHTS: Map<String, Float> = mapOf(
        "type_themed"      to 50f,
        "rarity_boosted"   to 30f,
        "lucky_epic"       to 12f,
        "lucky_special"    to 6f,
        "lucky_legendary"  to 2f
    )

    // ── Pack Rolling ──────────────────────────────────────────────────────────

    fun rollPack(
        pack: PackDefinition,
        collection: CardCollection,
        random: Random = Random.Default
    ): List<Card> {
        val drawnIds = mutableSetOf<Int>()
        val result = mutableListOf<Card>()

        for (slotIndex in 0 until pack.cardCount) {
            val rule = pack.slotRules.getOrNull(slotIndex)
            val rarity = resolveRarity(rule, random)

            val pool = buildPool(collection, rarity, pack.cardTypeFilter)
            if (pool.isEmpty()) continue

            val card = drawUniqueCard(pool, drawnIds, random) ?: continue
            drawnIds.add(card.id)
            result.add(card)
        }

        return result.sortedBy { it.rarity.ordinal }
    }

    /**
     * Checks whether the Pack Upgrade Event fires for this Basic Pack opening.
     * Returns the ID key of the upgraded pack type, or null if no event occurs.
     */
    fun checkSurpriseEvent(random: Random = Random.Default): String? {
        if (random.nextFloat() > SURPRISE_EVENT_PROBABILITY) return null
        return weightedRandom(SURPRISE_EVENT_WEIGHTS, random)
    }

    // ── PlayerState Mutations ─────────────────────────────────────────────────

    /**
     * Returns the current regeneration interval based on upgrade level.
     * Each level reduces the time by 2 minutes (30, 28, 26, 24, 22, 20).
     */
    fun getPackRegenIntervalMs(regenLevel: Int): Long {
        return BASE_PACK_REGEN_TIME_MS - (regenLevel * TimeUnit.MINUTES.toMillis(2))
    }

    /**
     * Updates [PlayerState] by calculating regenerated packs since [lastPackRegenTimeMs].
     */
    fun refreshPackCount(state: PlayerState, nowMs: Long): PlayerState {
        if (state.availablePacks >= state.maxPacks) {
            return state.copy(lastPackRegenTimeMs = nowMs)
        }

        val interval = getPackRegenIntervalMs(state.basicPackRegenLevel)
        val timePassed = nowMs - state.lastPackRegenTimeMs
        val packsToAdd = (timePassed / interval).toInt()
        
        if (packsToAdd <= 0) return state

        val newCount = (state.availablePacks + packsToAdd).coerceAtMost(state.maxPacks)
        val leftoverTime = timePassed % interval
        val newRegenTime = nowMs - leftoverTime

        return state.copy(
            availablePacks = newCount,
            lastPackRegenTimeMs = newRegenTime
        )
    }

    /**
     * Applies the result of a pack opening to [PlayerState].
     * Awards coins, gems, XP. Updates card copies. Consumes one pack if it's a basic pack.
     */
    fun applyPackResult(
        state: PlayerState,
        cards: List<Card>,
        xpReward: Int,
        isFreePack: Boolean = false
    ): PlayerState {
        var coins = state.coins
        var gems = state.gems
        val copies = state.cardCopies.toMutableMap()

        for (card in cards) {
            coins += card.coinReward
            gems += card.gemReward
            copies[card.id] = (copies[card.id] ?: 0) + 1
        }

        val newXp = state.xp + xpReward
        val newLevel = LevelSystem.levelForXp(newXp)

        // Consume a pack if not free
        val newAvailablePacks = if (!isFreePack) {
            (state.availablePacks - 1).coerceAtLeast(0)
        } else {
            state.availablePacks
        }

        return state.copy(
            coins = coins,
            gems = gems,
            xp = newXp,
            level = newLevel,
            cardCopies = copies,
            availablePacks = newAvailablePacks
        )
    }

    /**
     * Attempts to set the username on first launch.
     */
    fun setUsername(state: PlayerState, username: String): PlayerState {
        require(username.isNotBlank()) { "Username must not be blank." }
        return state.copy(username = username.trim())
    }

    /**
     * Attempts to change the username. Only allowed once.
     */
    fun changeUsername(state: PlayerState, newUsername: String): PlayerState {
        if (state.usernameChanged) return state
        require(newUsername.isNotBlank()) { "Username must not be blank." }
        return state.copy(username = newUsername.trim(), usernameChanged = true)
    }

    // ── Collection Sorting ────────────────────────────────────────────────────

    /**
     * Sorts cards for the Collection screen.
     */
    fun sortForCollection(cards: List<Card>): List<Card> {
        return cards.sortedWith(
            compareBy({ it.rarity.ordinal }, { it.type.ordinal }, { it.collectionNumber })
        )
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private fun resolveRarity(rule: SlotRule?, random: Random): Rarity {
        if (rule == null) return weightedRarityRoll(null, null, random)
        if (rule.guaranteedRarity != null) return rule.guaranteedRarity
        return weightedRarityRoll(rule.rollTable, rule.minimumRarity, random)
    }

    private fun weightedRarityRoll(
        table: Map<Rarity, Float>?,
        minimum: Rarity?,
        random: Random
    ): Rarity {
        val effectiveTable: Map<Rarity, Float> = if (table != null) {
            if (minimum != null) table.filter { it.key >= minimum } else table
        } else {
            val defaults = Rarity.values().associate { it to it.baseDropWeight }
            if (minimum != null) defaults.filter { it.key >= minimum } else defaults
        }

        return weightedRandom(effectiveTable, random)
    }

    private fun buildPool(
        collection: CardCollection,
        rarity: Rarity,
        typeFilter: List<CardType>?
    ): List<Card> {
        val byRarity = collection.cardsByRarity[rarity] ?: return emptyList()
        return if (typeFilter != null) byRarity.filter { it.type in typeFilter } else byRarity
    }

    private fun drawUniqueCard(
        pool: List<Card>,
        drawnIds: Set<Int>,
        random: Random,
        maxAttempts: Int = 20
    ): Card? {
        val available = pool.filter { it.id !in drawnIds }
        if (available.isEmpty()) return null
        return available[random.nextInt(available.size)]
    }

    private fun <T> weightedRandom(weights: Map<T, Float>, random: Random): T {
        val totalWeight = weights.values.sum()
        var roll = random.nextFloat() * totalWeight
        for ((key, weight) in weights) {
            roll -= weight
            if (roll <= 0f) return key
        }
        return weights.keys.last()
    }
}
