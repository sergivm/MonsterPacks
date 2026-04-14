package com.sergivm.monsterpacks.domain.model

// ─── Pack Type ────────────────────────────────────────────────────────────────

enum class PackType {
    /** Free, unlimited. The core pack. */
    BASIC,
    /** Free, cooldown-based. Available in the Shop. */
    BONUS,
    /** Paid. Restricts pool to a single CardType. */
    TYPE_THEMED,
    /** Paid. Shifts the rarity probability table upward. */
    RARITY_BOOSTED,
    /** Paid. Guarantees all slots meet a minimum rarity. */
    LUCKY
}

// ─── Slot Rule ────────────────────────────────────────────────────────────────

/**
 * Defines how a single slot in a pack is resolved.
 *
 * Priority: [guaranteedRarity] > [minimumRarity] > [rollTable] > free roll.
 *
 * @param slot              Slot index, 1-based.
 * @param guaranteedRarity  If set, this slot always yields exactly this rarity.
 * @param minimumRarity     If set, roll cannot produce below this rarity (Lucky Packs).
 * @param rollTable         Custom weight table for free-roll slots. If null, uses the
 *                          default Rarity.baseDropWeight values.
 */
data class SlotRule(
    val slot: Int,
    val guaranteedRarity: Rarity? = null,
    val minimumRarity: Rarity? = null,
    val rollTable: Map<Rarity, Float>? = null
)

// ─── Pack Cost ────────────────────────────────────────────────────────────────

sealed class PackCost {
    data class Coins(val amount: Long) : PackCost()
    data class Gems(val amount: Long) : PackCost()
    data class Both(val coins: Long, val gems: Long) : PackCost()
}

// ─── Pack Definition ──────────────────────────────────────────────────────────

/**
 * Static definition of a pack type. Never created at runtime — defined in the data layer.
 *
 * @param id            Unique identifier (e.g. "pack_basic", "pack_infernal").
 * @param name          Display name.
 * @param type          Category — controls behaviour and UI treatment.
 * @param collectionId  The collection whose card pool is used.
 * @param cardTypeFilter If non-null, restricts pool to cards of these types (TYPE_THEMED).
 * @param slotRules     Per-slot rarity rules. Must cover all slots.
 * @param cost          Null for free packs.
 * @param coverRes      Drawable resource for the collection cover shown on the pack face.
 * @param designRes     Drawable resource for the pack border/forms (varies by pack type).
 * @param backgroundRes Drawable resource for the background shown during pack opening.
 * @param xpReward      XP granted to the player on opening.
 * @param cardCount     Number of cards revealed per opening (5 for most packs, 3 for BONUS).
 */
data class PackDefinition(
    val id: String,
    val name: String,
    val type: PackType,
    val collectionId: String,
    val cardTypeFilter: List<CardType>? = null,
    val slotRules: List<SlotRule>,
    val cost: PackCost? = null,
    val coverRes: String,
    val designRes: String,
    val backgroundRes: String,
    val xpReward: Int = 10,
    val cardCount: Int = 5
)
