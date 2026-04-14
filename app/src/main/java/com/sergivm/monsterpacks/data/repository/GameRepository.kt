package com.sergivm.monsterpacks.data.repository

import com.sergivm.monsterpacks.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

/**
 * Contract for loading and persisting PlayerState.
 * The domain layer depends on this interface — never on the Room implementation directly.
 */
interface GameRepository {
    fun observePlayerState(): Flow<PlayerState>
    suspend fun getPlayerState(): PlayerState
    suspend fun savePlayerState(state: PlayerState)
}
