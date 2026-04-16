package com.sergivm.monsterpacks.domain.model

/**
 * A collection groups all cards that can appear together in a pack pool.
 * Initially there is only one collection. Future versions will add more.
 *
 * @param id        Unique identifier (e.g. "collection_genesis").
 * @param name      Display name shown in the Collection screen header and on the pack cover.
 * @param coverRes  Drawable resource name for the collection cover art shown on the pack face.
 * @param cards     All cards belonging to this collection, including variants.
 */
data class CardCollection(
    val id: String,
    val name: String,
    val coverRes: String,
    val cards: List<Card>
) {
    /** All cards grouped by rarity, used to build drop pools. */
    val cardsByRarity: Map<Rarity, List<Card>> by lazy {
        cards.groupBy { it.rarity }
    }

    /** Total card count per rarity, used in the CardCollection screen progress counters. */
    fun totalByRarity(rarity: Rarity): Int = cardsByRarity[rarity]?.size ?: 0
}
