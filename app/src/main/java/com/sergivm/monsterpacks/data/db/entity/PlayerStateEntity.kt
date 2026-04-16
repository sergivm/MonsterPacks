package com.sergivm.monsterpacks.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that persists the player's full state.
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
    val cardCopiesJson: String = "{}",
    
    // Pack Limit System
    val availablePacks: Int = 50,
    val maxPacks: Int = 50,
    val lastPackRegenTimeMs: Long = 0L,
    
    // Free Pack System
    val freePackReadyAtMs: Long? = null,
    
    // Upgrade Levels
    val basicPackRarityLevel: Int = 0,
    val basicPackCapacityLevel: Int = 0,
    val basicPackCardCountLevel: Int = 0,
    val basicPackRegenLevel: Int = 0,
    val basicPackXpLevel: Int = 0,
    val xpMultiplierLevel: Int = 0,
    val bulkOpenLevel: Int = 0,
    
    val freePackCooldownLevel: Int = 0,
    val freePackGemYieldLevel: Int = 0,
    val freePackCoinYieldLevel: Int = 0,
    val freePackCardCountLevel: Int = 0,
    val freePackStoredLevel: Int = 0,
    val freePackRarityLevel: Int = 0
)
