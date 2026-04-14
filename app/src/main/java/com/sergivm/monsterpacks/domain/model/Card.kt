package com.sergivm.monsterpacks.domain.model

/**
 * A card definition. This is a static entity — it is never created at runtime.
 * The player's progress is tracked via copy count in PlayerState, not by mutating Card.
 *
 * @param id               Unique identifier across all collections.
 * @param collectionId     The collection this card belongs to.
 * @param collectionNumber Display number within the collection (e.g. #042).
 * @param name             Display name.
 * @param type             Thematic family — determines background color.
 * @param rarity           Drop probability, frame, animation tier, rewards.
 * @param description      Short flavour text (1–2 sentences). Only present for
 *                         Common, Rare, and Epic cards. Null for Special/Legendary/God.
 * @param imageRes         Drawable resource name. Use placeholder until final art is ready.
 * @param isVariant        True if this is a Special/Legendary/God redesign of a base card.
 * @param baseCardId       ID of the base card if [isVariant] is true, null otherwise.
 */
data class Card(
    val id: Int,
    val collectionId: String,
    val collectionNumber: Int,
    val name: String,
    val type: CardType,
    val rarity: Rarity,
    val description: String?,
    val imageRes: String,
    val isVariant: Boolean = false,
    val baseCardId: Int? = null
) {
    val coinReward: Int get() = rarity.coinReward
    val gemReward: Int get() = rarity.gemReward
}
