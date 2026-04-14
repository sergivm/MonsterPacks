package com.sergivm.monsterpacks.domain.engine

import com.sergivm.monsterpacks.domain.model.*
import kotlin.random.Random

/**
 * Pure game logic engine. No Android or Compose imports allowed.
 * All methods are deterministic given a [Random] instance — fully testable with JUnit.
 *
 * Responsibilities:
 * - Rolling cards for a pack session
 * - Applying pack results to PlayerState
 * - Checking and applying the Pack Upgrade Event (Surprise Pack)
 * - Levelling up
 * - Sorting cards in ascending rarity order for reveal
 */
object GameEngine {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Base probability (0..1) that a Basic Pack triggers a Surprise upgrade event. */
    const val SURPRISE_EVENT_PROBABILITY = 0.005f

    /** Weighted chances for each surprise result. Weights are relative, not percentages. */
    val SURPRISE_EVENT_WEIGHTS: Map<String, Float> = mapOf(
        "type_themed"      to 50f,
        "rarity_boosted"   to 30f,
        "lucky_epic"       to 12f,
        "lucky_special"    to 6f,
        "lucky_legendary"  to 2f
    )

    // ── Pack Rolling ──────────────────────────────────────────────────────────

    /**
     * Rolls [PackDefinition.cardCount] cards for the given pack and collection.
     *
     * - Applies per-slot [SlotRule] (guaranteed rarity / minimum rarity / custom roll table).
     * - No card can appear twice in the same session (re-rolls on duplicate).
     * - Returns cards sorted ascending by rarity ordinal (lowest first, highest last).
     */
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

            // Build pool filtered by type if TYPE_THEMED
            val pool = buildPool(collection, rarity, pack.cardTypeFilter)
            if (pool.isEmpty()) continue

            // Draw a card, avoiding duplicates within this session
            val card = drawUniqueCard(pool, drawnIds, random) ?: continue
            drawnIds.add(card.id)
            result.add(card)
        }

        // Sort ascending by rarity (lowest ordinal first → player sees least rare first)
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
     * Applies the result of a pack opening to [PlayerState].
     * Awards coins, gems, XP. Updates card copies. Recalculates level.
     *
     * Does NOT mutate state — returns a new [PlayerState].
     */
    fun applyPackResult(
        state: PlayerState,
        cards: List<Card>,
        xpReward: Int
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

        return state.copy(
            coins = coins,
            gems = gems,
            xp = newXp,
            level = newLevel,
            cardCopies = copies
        )
    }

    /**
     * Attempts to set the username on first launch.
     * Returns an updated [PlayerState] or throws if username is blank.
     */
    fun setUsername(state: PlayerState, username: String): PlayerState {
        require(username.isNotBlank()) { "Username must not be blank." }
        return state.copy(username = username.trim())
    }

    /**
     * Attempts to change the username. Only allowed once.
     * Returns updated state or the existing state unchanged if already changed.
     */
    fun changeUsername(state: PlayerState, newUsername: String): PlayerState {
        if (state.usernameChanged) return state
        require(newUsername.isNotBlank()) { "Username must not be blank." }
        return state.copy(username = newUsername.trim(), usernameChanged = true)
    }

    // ── Collection Sorting ────────────────────────────────────────────────────

    /**
     * Sorts cards for the Collection screen according to GDD spec:
     * 1. Common by type order
     * 2. Rare by type order
     * 3. Epic by type order
     * 4. Special by type order
     * 5. Legendary by type order
     * 6. God by type order
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
