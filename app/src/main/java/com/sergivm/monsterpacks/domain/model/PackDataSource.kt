package com.sergivm.monsterpacks.domain.model

/**
 * Static definitions for all pack types in the game.
 */
object PackDataSource {

    /** 
     * Default roll table for "free" slots. 
     * Uses weights from Rarity.kt (God 0.5%, Leg 2.5%, Spec 5%, Epic 15%, Rare 37%, Common 40%).
     */
    private val defaultFreeRollTable = Rarity.values().associateWith { it.baseDropWeight }

    // ── Basic Pack ────────────────────────────────────────────────────────────
    val basicPack = PackDefinition(
        id = "pack_basic",
        name = "Basic Pack",
        type = PackType.BASIC,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, guaranteedRarity = Rarity.COMMON),
            SlotRule(slot = 2, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 3, rollTable = defaultFreeRollTable),
            SlotRule(slot = 4, rollTable = defaultFreeRollTable),
            SlotRule(slot = 5, rollTable = defaultFreeRollTable)
        ),
        cost = null,
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_basic",
        backgroundRes = "pack_bg_genesis_basic",
        xpReward = 10,
        cardCount = 5
    )

    // ── Free Pack (formerly Bonus Pack) ───────────────────────────────────────
    val freePack = PackDefinition(
        id = "pack_free",
        name = "Free Pack",
        type = PackType.BONUS,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, rollTable = defaultFreeRollTable),
            SlotRule(slot = 2, rollTable = defaultFreeRollTable),
            SlotRule(slot = 3, rollTable = defaultFreeRollTable)
        ),
        cost = null,
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_free",
        backgroundRes = "pack_bg_genesis_free",
        xpReward = 5,
        cardCount = 3
    )

    // ── Type-Themed Packs (one per CardType) ──────────────────────────────────
    val typeThemedPacks: List<PackDefinition> = CardType.values().map { cardType ->
        PackDefinition(
            id = "pack_type_${cardType.name.lowercase()}",
            name = "${cardType.displayName} Pack",
            type = PackType.TYPE_THEMED,
            collectionId = CardDataSource.COLLECTION_GENESIS,
            cardTypeFilter = listOf(cardType),
            slotRules = listOf(
                SlotRule(slot = 1, guaranteedRarity = Rarity.COMMON),
                SlotRule(slot = 2, guaranteedRarity = Rarity.RARE),
                SlotRule(slot = 3, rollTable = defaultFreeRollTable),
                SlotRule(slot = 4, rollTable = defaultFreeRollTable),
                SlotRule(slot = 5, rollTable = defaultFreeRollTable)
            ),
            cost = PackCost.Coins(500L),
            coverRes = "collection_genesis_cover",
            designRes = "pack_design_type_${cardType.name.lowercase()}",
            backgroundRes = "pack_bg_${cardType.name.lowercase()}",
            xpReward = 20,
            cardCount = 5
        )
    }

    // ── Rarity-Boosted Packs ──────────────────────────────────────────────────
    val rareBoostedPack = PackDefinition(
        id = "pack_boosted_rare",
        name = "Rare Boosted Pack",
        type = PackType.RARITY_BOOSTED,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, guaranteedRarity = Rarity.COMMON),
            SlotRule(slot = 2, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 3, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 4, rollTable = mapOf(
                Rarity.COMMON to 30f, Rarity.RARE to 60f, Rarity.EPIC to 8f,
                Rarity.SPECIAL to 1.5f, Rarity.LEGENDARY to 0.4f, Rarity.GOD to 0.1f)),
            SlotRule(slot = 5, rollTable = mapOf(
                Rarity.COMMON to 30f, Rarity.RARE to 60f, Rarity.EPIC to 8f,
                Rarity.SPECIAL to 1.5f, Rarity.LEGENDARY to 0.4f, Rarity.GOD to 0.1f))
        ),
        cost = PackCost.Coins(1_000L),
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_boosted_rare",
        backgroundRes = "pack_bg_genesis_boosted",
        xpReward = 20,
        cardCount = 5
    )

    val epicBoostedPack = PackDefinition(
        id = "pack_boosted_epic",
        name = "Epic Boosted Pack",
        type = PackType.RARITY_BOOSTED,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 2, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 3, guaranteedRarity = Rarity.EPIC),
            SlotRule(slot = 4, rollTable = mapOf(
                Rarity.COMMON to 20f, Rarity.RARE to 40f, Rarity.EPIC to 30f,
                Rarity.SPECIAL to 8f, Rarity.LEGENDARY to 1.5f, Rarity.GOD to 0.5f)),
            SlotRule(slot = 5, rollTable = mapOf(
                Rarity.COMMON to 20f, Rarity.RARE to 40f, Rarity.EPIC to 30f,
                Rarity.SPECIAL to 8f, Rarity.LEGENDARY to 1.5f, Rarity.GOD to 0.5f))
        ),
        cost = PackCost.Both(coins = 0L, gems = 50L),
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_boosted_epic",
        backgroundRes = "pack_bg_genesis_boosted",
        xpReward = 20,
        cardCount = 5
    )

    // ── Lucky Packs ───────────────────────────────────────────────────────────
    val luckyEpicPack = PackDefinition(
        id = "pack_lucky_epic",
        name = "Lucky Epic Pack",
        type = PackType.LUCKY,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = List(5) { i ->
            SlotRule(slot = i + 1, minimumRarity = Rarity.EPIC, rollTable = mapOf(
                Rarity.EPIC to 80f, Rarity.SPECIAL to 15f,
                Rarity.LEGENDARY to 4.5f, Rarity.GOD to 0.5f))
        },
        cost = PackCost.Both(coins = 0L, gems = 150L),
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_lucky_epic",
        backgroundRes = "pack_bg_genesis_lucky",
        xpReward = 20,
        cardCount = 5
    )

    val luckySpecialPack = PackDefinition(
        id = "pack_lucky_special",
        name = "Lucky Special Pack",
        type = PackType.LUCKY,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = List(5) { i ->
            SlotRule(slot = i + 1, minimumRarity = Rarity.SPECIAL, rollTable = mapOf(
                Rarity.SPECIAL to 85f, Rarity.LEGENDARY to 13f, Rarity.GOD to 2f))
        },
        cost = PackCost.Both(coins = 0L, gems = 500L),
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_lucky_special",
        backgroundRes = "pack_bg_genesis_lucky",
        xpReward = 20,
        cardCount = 5
    )

    val luckyLegendaryPack = PackDefinition(
        id = "pack_lucky_legendary",
        name = "Lucky Legendary Pack",
        type = PackType.LUCKY,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = List(5) { i ->
            SlotRule(slot = i + 1, minimumRarity = Rarity.LEGENDARY, rollTable = mapOf(
                Rarity.LEGENDARY to 92f, Rarity.GOD to 8f))
        },
        cost = PackCost.Both(coins = 0L, gems = 2_000L),
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_lucky_legendary",
        backgroundRes = "pack_bg_genesis_lucky",
        xpReward = 20,
        cardCount = 5
    )

    val allPacks: List<PackDefinition> =
        listOf(basicPack, freePack) +
        typeThemedPacks +
        listOf(rareBoostedPack, epicBoostedPack, luckyEpicPack, luckySpecialPack, luckyLegendaryPack)
}
