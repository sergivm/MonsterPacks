package com.sergivm.monsterpacks.domain.model

/**
 * Static definitions for all pack types in the game.
 * Slot rules implement the probability system defined in the GDD.
 *
 * TODO: Replace coverRes / designRes / backgroundRes strings with actual drawable resource names.
 */
object PackDataSource {

    // ── Free roll table (used by Slots 4 & 5 of Basic Pack) ──────────────────
    private val freeRollTable = mapOf(
        Rarity.COMMON    to 58f,
        Rarity.RARE      to 35f,
        Rarity.EPIC      to 5f,
        Rarity.SPECIAL   to 1f,
        Rarity.LEGENDARY to 0.1f,
        Rarity.GOD       to 0.01f
    )

    // ── Basic Pack ────────────────────────────────────────────────────────────
    val basicPack = PackDefinition(
        id = "pack_basic",
        name = "Basic Pack",
        type = PackType.BASIC,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, guaranteedRarity = Rarity.COMMON),
            SlotRule(slot = 2, guaranteedRarity = Rarity.COMMON),
            SlotRule(slot = 3, guaranteedRarity = Rarity.RARE),
            SlotRule(slot = 4, rollTable = freeRollTable),
            SlotRule(slot = 5, rollTable = freeRollTable)
        ),
        cost = null,
        coverRes = "collection_genesis_cover",        // TODO: final art
        designRes = "pack_design_basic",              // TODO: final art
        backgroundRes = "pack_bg_genesis_basic",      // TODO: final art
        xpReward = 10,
        cardCount = 5
    )

    // ── Bonus Pack ────────────────────────────────────────────────────────────
    val bonusPack = PackDefinition(
        id = "pack_bonus",
        name = "Bonus Pack",
        type = PackType.BONUS,
        collectionId = CardDataSource.COLLECTION_GENESIS,
        slotRules = listOf(
            SlotRule(slot = 1, rollTable = freeRollTable),
            SlotRule(slot = 2, rollTable = freeRollTable),
            SlotRule(slot = 3, rollTable = freeRollTable)
        ),
        cost = null,
        coverRes = "collection_genesis_cover",
        designRes = "pack_design_bonus",
        backgroundRes = "pack_bg_genesis_bonus",
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
                SlotRule(slot = 2, guaranteedRarity = Rarity.COMMON),
                SlotRule(slot = 3, guaranteedRarity = Rarity.RARE),
                SlotRule(slot = 4, rollTable = freeRollTable),
                SlotRule(slot = 5, rollTable = freeRollTable)
            ),
            cost = PackCost.Coins(500L),
            coverRes = "collection_genesis_cover",
            designRes = "pack_design_type_${cardType.name.lowercase()}", // TODO: final art
            backgroundRes = "pack_bg_${cardType.name.lowercase()}",      // TODO: final art
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
                Rarity.COMMON to 38f, Rarity.RARE to 55f, Rarity.EPIC to 6f,
                Rarity.SPECIAL to 1f, Rarity.LEGENDARY to 0.1f, Rarity.GOD to 0.01f)),
            SlotRule(slot = 5, rollTable = mapOf(
                Rarity.COMMON to 38f, Rarity.RARE to 55f, Rarity.EPIC to 6f,
                Rarity.SPECIAL to 1f, Rarity.LEGENDARY to 0.1f, Rarity.GOD to 0.01f))
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
                Rarity.COMMON to 33f, Rarity.RARE to 40f, Rarity.EPIC to 20f,
                Rarity.SPECIAL to 6f, Rarity.LEGENDARY to 0.9f, Rarity.GOD to 0.1f)),
            SlotRule(slot = 5, rollTable = mapOf(
                Rarity.COMMON to 33f, Rarity.RARE to 40f, Rarity.EPIC to 20f,
                Rarity.SPECIAL to 6f, Rarity.LEGENDARY to 0.9f, Rarity.GOD to 0.1f))
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
        listOf(basicPack, bonusPack) +
        typeThemedPacks +
        listOf(rareBoostedPack, epicBoostedPack, luckyEpicPack, luckySpecialPack, luckyLegendaryPack)
}
