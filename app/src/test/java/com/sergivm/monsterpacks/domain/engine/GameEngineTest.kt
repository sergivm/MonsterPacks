package com.sergivm.monsterpacks.domain.engine

import com.sergivm.monsterpacks.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private val collection = CardDataSource.genesisCardCollection
    private val basicPack  = PackDataSource.basicPack

    // ── rollPack ──────────────────────────────────────────────────────────────

    @Test
    fun `rollPack returns correct card count`() {
        val cards = GameEngine.rollPack(basicPack, collection, Random(42))
        assertEquals(basicPack.cardCount, cards.size)
    }

    @Test
    fun `rollPack returns no duplicate cards`() {
        repeat(50) { seed ->
            val cards = GameEngine.rollPack(basicPack, collection, Random(seed.toLong()))
            val ids = cards.map { it.id }
            assertEquals("Duplicate found with seed $seed", ids.distinct().size, ids.size)
        }
    }

    @Test
    fun `rollPack slot 1 and 2 are always Common`() {
        repeat(30) { seed ->
            val cards = GameEngine.rollPack(basicPack, collection, Random(seed.toLong()))
            assertEquals(Rarity.COMMON, cards[0].rarity)
            assertEquals(Rarity.COMMON, cards[1].rarity)
        }
    }

    @Test
    fun `rollPack slot 3 is always Rare`() {
        repeat(30) { seed ->
            val cards = GameEngine.rollPack(basicPack, collection, Random(seed.toLong()))
            assertEquals(Rarity.RARE, cards[2].rarity)
        }
    }

    @Test
    fun `rollPack cards are sorted ascending by rarity`() {
        repeat(50) { seed ->
            val cards = GameEngine.rollPack(basicPack, collection, Random(seed.toLong()))
            val ordinals = cards.map { it.rarity.ordinal }
            assertEquals("Not sorted with seed $seed", ordinals.sorted(), ordinals)
        }
    }

    @Test
    fun `rollPack TYPE_THEMED pack only returns cards of the specified type`() {
        val infernalPack = PackDataSource.typeThemedPacks.first { it.cardTypeFilter == listOf(CardType.INFERNAL) }
        repeat(20) { seed ->
            val cards = GameEngine.rollPack(infernalPack, collection, Random(seed.toLong()))
            cards.forEach { card ->
                assertEquals("Non-Infernal card found with seed $seed", CardType.INFERNAL, card.type)
            }
        }
    }

    @Test
    fun `rollPack LUCKY epic pack returns no cards below Epic`() {
        repeat(30) { seed ->
            val cards = GameEngine.rollPack(PackDataSource.luckyEpicPack, collection, Random(seed.toLong()))
            cards.forEach { card ->
                assertTrue(
                    "Card below Epic found with seed $seed: ${card.name} (${card.rarity})",
                    card.rarity >= Rarity.EPIC
                )
            }
        }
    }

    // ── applyPackResult ───────────────────────────────────────────────────────

    @Test
    fun `applyPackResult increments copy counts correctly`() {
        val initialState = PlayerState()
        val cards = GameEngine.rollPack(basicPack, collection, Random(1))
        val updated = GameEngine.applyPackResult(initialState, cards, xpReward = 10)

        cards.forEach { card ->
            assertEquals(1, updated.copiesOf(card.id))
        }
    }

    @Test
    fun `applyPackResult adds correct coins`() {
        val initialState = PlayerState(coins = 0L)
        val cards = GameEngine.rollPack(basicPack, collection, Random(1))
        val expectedCoins = cards.sumOf { it.coinReward }.toLong()
        val updated = GameEngine.applyPackResult(initialState, cards, xpReward = 10)
        assertEquals(expectedCoins, updated.coins)
    }

    @Test
    fun `applyPackResult adds XP and updates level`() {
        val initialState = PlayerState(xp = 90L, level = 1)
        val cards = GameEngine.rollPack(basicPack, collection, Random(1))
        val updated = GameEngine.applyPackResult(initialState, cards, xpReward = 10)
        assertTrue(updated.xp >= 100L)
        assertTrue(updated.level >= 2)
    }

    // ── Username ──────────────────────────────────────────────────────────────

    @Test
    fun `setUsername sets username correctly`() {
        val state = GameEngine.setUsername(PlayerState(), "TestPlayer")
        assertEquals("TestPlayer", state.username)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setUsername throws on blank input`() {
        GameEngine.setUsername(PlayerState(), "   ")
    }

    @Test
    fun `changeUsername succeeds when not yet changed`() {
        val state = PlayerState(username = "Old", usernameChanged = false)
        val updated = GameEngine.changeUsername(state, "New")
        assertEquals("New", updated.username)
        assertTrue(updated.usernameChanged)
    }

    @Test
    fun `changeUsername is blocked when already changed`() {
        val state = PlayerState(username = "Old", usernameChanged = true)
        val updated = GameEngine.changeUsername(state, "New")
        assertEquals("Old", updated.username)
        assertTrue(updated.usernameChanged)
    }

    // ── Collection sort ───────────────────────────────────────────────────────

    @Test
    fun `sortForCollection groups by rarity then type`() {
        val sorted = GameEngine.sortForCollection(collection.cards)
        // First card must be Common
        assertEquals(Rarity.COMMON, sorted.first().rarity)
        // Last card must be God
        assertEquals(Rarity.GOD, sorted.last().rarity)
        // No Common appears after a Rare
        val commonIndices = sorted.indices.filter { sorted[it].rarity == Rarity.COMMON }
        val rareIndices   = sorted.indices.filter { sorted[it].rarity == Rarity.RARE }
        assertTrue(commonIndices.max() < rareIndices.min())
    }

    // ── Surprise event ────────────────────────────────────────────────────────

    @Test
    fun `checkSurpriseEvent returns null most of the time`() {
        val results = (0..9999).map { GameEngine.checkSurpriseEvent(Random(it.toLong())) }
        val nullCount = results.count { it == null }
        // Expect ~99.5% null — allow generous margin
        assertTrue("Too many surprise events triggered", nullCount > 9800)
    }

    @Test
    fun `checkSurpriseEvent returns valid pack type ids when triggered`() {
        val validIds = setOf("type_themed", "rarity_boosted", "lucky_epic", "lucky_special", "lucky_legendary")
        val nonNullResults = (0..99999)
            .mapNotNull { GameEngine.checkSurpriseEvent(Random(it.toLong())) }
        assertTrue("No events triggered in 100k attempts — probability may be broken", nonNullResults.isNotEmpty())
        nonNullResults.forEach { id ->
            assertTrue("Unknown surprise ID: $id", id in validIds)
        }
    }

    // ── LevelSystem ───────────────────────────────────────────────────────────

    @Test
    fun `levelForXp returns level 1 at 0 XP`() {
        assertEquals(1, LevelSystem.levelForXp(0L))
    }

    @Test
    fun `levelForXp returns level 2 at exactly 100 XP`() {
        assertEquals(2, LevelSystem.levelForXp(100L))
    }

    @Test
    fun `levelForXp returns correct level at various thresholds`() {
        assertEquals(3, LevelSystem.levelForXp(300L))
        assertEquals(4, LevelSystem.levelForXp(700L))
        assertEquals(10, LevelSystem.levelForXp(18_600L))
    }

    @Test
    fun `xpNeededForNextLevel returns correct value`() {
        assertEquals(100L, LevelSystem.xpNeededForNextLevel(1))
        assertEquals(200L, LevelSystem.xpNeededForNextLevel(2))
    }
}
