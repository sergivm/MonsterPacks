package com.sergivm.monsterpacks.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sergivm.monsterpacks.data.db.entity.PlayerStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStateDao {

    @Query("SELECT * FROM player_state WHERE id = 1")
    fun observe(): Flow<PlayerStateEntity?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun get(): PlayerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlayerStateEntity)

    @Query("DELETE FROM player_state")
    suspend fun deleteAll()
}
