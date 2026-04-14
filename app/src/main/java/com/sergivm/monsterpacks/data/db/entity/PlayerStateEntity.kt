package com.sergivm.monsterpacks.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that persists the player's full state.
 * cardCopiesJson stores the cardCopies map serialised as JSON via Gson.
 *
 * There is always exactly one row (id = 1).
 */
@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "",
    val usernameChanged: Boolean = false,
    val coins: Long = 0L,
    val gems: Long = 0L,
    val xp: Long = 0L,
    val level: Int = 1,
    val cardCopiesJson: String = "{}",   // Gson: Map<Int, Int>
    val bonusPackReadyAtMs: Long? = null,
    val basicPackUpgradeLevel: Int = 0,
    val bonusPackUpgradeLevel: Int = 0
)
