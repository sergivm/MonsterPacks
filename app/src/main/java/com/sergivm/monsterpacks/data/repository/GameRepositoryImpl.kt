package com.sergivm.monsterpacks.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sergivm.monsterpacks.data.db.dao.PlayerStateDao
import com.sergivm.monsterpacks.data.db.entity.PlayerStateEntity
import com.sergivm.monsterpacks.domain.model.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val dao: PlayerStateDao,
    private val gson: Gson
) : GameRepository {

    private val mapType = object : TypeToken<Map<Int, Int>>() {}.type

    override fun observePlayerState(): Flow<PlayerState> =
        dao.observe().map { entity -> entity?.toDomain() ?: PlayerState() }

    override suspend fun getPlayerState(): PlayerState = withContext(Dispatchers.IO) {
        dao.get()?.toDomain() ?: PlayerState()
    }

    override suspend fun savePlayerState(state: PlayerState) = withContext(Dispatchers.IO) {
        dao.upsert(state.toEntity())
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun PlayerStateEntity.toDomain() = PlayerState(
        username = username,
        usernameChanged = usernameChanged,
        coins = coins,
        gems = gems,
        xp = xp,
        level = level,
        cardCopies = gson.fromJson(cardCopiesJson, mapType) ?: emptyMap(),
        availablePacks = availablePacks,
        maxPacks = maxPacks,
        lastPackRegenTimeMs = lastPackRegenTimeMs,
        freePackReadyAtMs = freePackReadyAtMs,
        basicPackRarityLevel = basicPackRarityLevel,
        basicPackCapacityLevel = basicPackCapacityLevel,
        basicPackCardCountLevel = basicPackCardCountLevel,
        basicPackRegenLevel = basicPackRegenLevel,
        basicPackXpLevel = basicPackXpLevel,
        xpMultiplierLevel = xpMultiplierLevel,
        bulkOpenLevel = bulkOpenLevel,
        freePackCooldownLevel = freePackCooldownLevel,
        freePackGemYieldLevel = freePackGemYieldLevel,
        freePackCoinYieldLevel = freePackCoinYieldLevel,
        freePackCardCountLevel = freePackCardCountLevel,
        freePackStoredLevel = freePackStoredLevel,
        freePackRarityLevel = freePackRarityLevel
    )

    private fun PlayerState.toEntity() = PlayerStateEntity(
        id = 1,
        username = username,
        usernameChanged = usernameChanged,
        coins = coins,
        gems = gems,
        xp = xp,
        level = level,
        cardCopiesJson = gson.toJson(cardCopies),
        availablePacks = availablePacks,
        maxPacks = maxPacks,
        lastPackRegenTimeMs = lastPackRegenTimeMs,
        freePackReadyAtMs = freePackReadyAtMs,
        basicPackRarityLevel = basicPackRarityLevel,
        basicPackCapacityLevel = basicPackCapacityLevel,
        basicPackCardCountLevel = basicPackCardCountLevel,
        basicPackRegenLevel = basicPackRegenLevel,
        basicPackXpLevel = basicPackXpLevel,
        xpMultiplierLevel = xpMultiplierLevel,
        bulkOpenLevel = bulkOpenLevel,
        freePackCooldownLevel = freePackCooldownLevel,
        freePackGemYieldLevel = freePackGemYieldLevel,
        freePackCoinYieldLevel = freePackCoinYieldLevel,
        freePackCardCountLevel = freePackCardCountLevel,
        freePackStoredLevel = freePackStoredLevel,
        freePackRarityLevel = freePackRarityLevel
    )
}
