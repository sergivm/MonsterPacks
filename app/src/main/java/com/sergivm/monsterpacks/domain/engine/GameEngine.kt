package com.sergivm.monsterpacks.domain.engine

import com.sergivm.monsterpacks.domain.model.*
import kotlin.random.Random
import java.util.concurrent.TimeUnit

/**
 * Pure game logic engine.
 */
object GameEngine {

    const val SURPRISE_EVENT_PROBABILITY = 0.005f
    private val BASE_PACK_REGEN_TIME_MS = TimeUnit.MINUTES.toMillis(30)

    val SURPRISE_EVENT_WEIGHTS: Map<String, Float> = mapOf(
        "type_themed"      to 50f,
        "rarity_boosted"   to 30f,
        "lucky_epic"       to 12f,
        "lucky_special"    to 6f,
        "lucky_legendary"  to 2f
    )

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

    fun checkSurpriseEvent(random: Random = Random.Default): String? {
        if (random.nextFloat() > SURPRISE_EVENT_PROBABILITY) return null
        return weightedRandom(SURPRISE_EVENT_WEIGHTS, random)
    }

    fun getPackRegenIntervalMs(regenLevel: Int): Long {
        return (BASE_PACK_REGEN_TIME_MS - (regenLevel * TimeUnit.MINUTES.toMillis(2)))
            .coerceAtLeast(TimeUnit.MINUTES.toMillis(1))
    }

    /**
     * Stable refresh logic. 
     * returns the same instance if no packs were added, preventing save loops.
     */
    fun refreshPackCount(state: PlayerState, nowMs: Long): PlayerState {
        if (state.availablePacks >= state.maxPacks) return state

        val interval = getPackRegenIntervalMs(state.basicPackRegenLevel)
        val timePassed = nowMs - state.lastPackRegenTimeMs
        
        if (timePassed < interval) return state

        val packsToAdd = (timePassed / interval).toInt()
        if (packsToAdd <= 0) return state

        val newCount = (state.availablePacks + packsToAdd).coerceAtMost(state.maxPacks)
        
        // If capped, reset timer to now. If not, move timer forward by used intervals.
        val newRegenTime = if (newCount >= state.maxPacks) {
            nowMs
        } else {
            state.lastPackRegenTimeMs + (packsToAdd * interval)
        }

        return state.copy(
            availablePacks = newCount,
            lastPackRegenTimeMs = newRegenTime
        )
    }

    fun consumePack(state: PlayerState, nowMs: Long): PlayerState {
        val newCount = (state.availablePacks - 1).coerceAtLeast(0)
        // If we were full, the countdown for the empty slot starts EXACTLY now.
        val newRegenTime = if (state.availablePacks >= state.maxPacks) nowMs else state.lastPackRegenTimeMs
        
        return state.copy(
            availablePacks = newCount,
            lastPackRegenTimeMs = newRegenTime
        )
    }

    fun applyPackResult(state: PlayerState, cards: List<Card>, xpReward: Int): PlayerState {
        var coins = state.coins
        var gems = state.gems
        val copies = state.cardCopies.toMutableMap()

        for (card in cards) {
            coins += card.coinReward
            gems += card.gemReward
            copies[card.id] = (copies[card.id] ?: 0) + 1
        }

        val newXp = state.xp + xpReward
        return state.copy(
            coins = coins,
            gems = gems,
            xp = newXp,
            level = LevelSystem.levelForXp(newXp),
            cardCopies = copies
        )
    }

    fun setUsername(state: PlayerState, username: String) = state.copy(username = username.trim())
    fun changeUsername(state: PlayerState, newUsername: String) = 
        if (state.usernameChanged) state else state.copy(username = newUsername.trim(), usernameChanged = true)

    fun sortForCollection(cards: List<Card>) = cards.sortedWith(
        compareBy({ it.rarity.ordinal }, { it.type.ordinal }, { it.collectionNumber })
    )

    private fun resolveRarity(rule: SlotRule?, random: Random): Rarity {
        if (rule?.guaranteedRarity != null) return rule.guaranteedRarity
        val defaults = Rarity.values().associateWith { it.baseDropWeight }
        val table = rule?.rollTable ?: defaults
        val min = rule?.minimumRarity
        val effectiveTable = if (min != null) table.filter { it.key >= min } else table
        return weightedRandom(effectiveTable, random)
    }

    private fun buildPool(col: CardCollection, rarity: Rarity, filter: List<CardType>?) =
        col.cardsByRarity[rarity]?.filter { filter == null || it.type in filter } ?: emptyList()

    private fun drawUniqueCard(pool: List<Card>, drawn: Set<Int>, random: Random): Card? {
        val avail = pool.filter { it.id !in drawn }
        return if (avail.isEmpty()) null else avail[random.nextInt(avail.size)]
    }

    private fun <T> weightedRandom(weights: Map<T, Float>, random: Random): T {
        val total = weights.values.sum()
        var roll = random.nextFloat() * total
        for ((key, weight) in weights) {
            roll -= weight
            if (roll <= 0f) return key
        }
        return weights.keys.last()
    }
}
