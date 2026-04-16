package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.Config
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import java.util.Locale

data class ShopUiState(
    val playerState: PlayerState = PlayerState(),
    val upgrades: List<Upgrade> = emptyList(),
    val freePackCooldownRemainingMs: Long = 0L,
    val isFreePackReady: Boolean = false,
    val purchaseResult: UpgradeResult? = null,
    val nextPackRegenRemainingMs: Long = 0L,
    val isOpeningFreePack: Boolean = false,
    val drawnCards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val showSummary: Boolean = false
) {
    val currentCard: Card? get() = drawnCards.getOrNull(currentCardIndex)
    val isLastCard: Boolean get() = currentCardIndex >= drawnCards.lastIndex
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        repository.observePlayerState()
            .onEach { playerState ->
                _uiState.update {
                    it.copy(
                        playerState = playerState,
                        upgrades = buildUpgradeList(playerState)
                    )
                }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val nowMs = System.currentTimeMillis()
                val state = _uiState.value.playerState
                val regenInterval = GameEngine.getPackRegenIntervalMs(state.basicPackRegenLevel)
                
                _uiState.update {
                    it.copy(
                        freePackCooldownRemainingMs = state.freePackCooldownRemainingMs(nowMs),
                        isFreePackReady = state.isFreePackReady(nowMs),
                        nextPackRegenRemainingMs = state.nextPackRegenRemainingMs(nowMs, regenInterval)
                    )
                }
            }
        }
    }

    fun purchaseUpgrade(upgrade: Upgrade) {
        viewModelScope.launch {
            val state = _uiState.value.playerState
            val result = tryPurchase(state, upgrade)
            _uiState.update { it.copy(purchaseResult = result) }
            if (result is UpgradeResult.Success) {
                val updated = applyUpgrade(state, upgrade)
                repository.savePlayerState(updated)
            }
        }
    }

    fun clearPurchaseResult() = _uiState.update { it.copy(purchaseResult = null) }

    fun claimFreePack() {
        val state = _uiState.value
        if (!state.playerState.isFreePackReady(System.currentTimeMillis())) return
        val cards = GameEngine.rollPack(
            pack = PackDataSource.freePack,
            collection = CardDataSource.genesisCardCollection,
            playerState = state.playerState
        )
        _uiState.update { it.copy(isOpeningFreePack = true, drawnCards = cards, currentCardIndex = 0, showSummary = false) }
    }

    fun nextFreeCard() {
        _uiState.update { 
            if (it.isLastCard) it.copy(showSummary = true)
            else it.copy(currentCardIndex = it.currentCardIndex + 1)
        }
    }

    fun finishFreePack() {
        val state = _uiState.value
        viewModelScope.launch {
            val multiplier = if (Config.DEV_BOOST) 100 else 1
            val gemReward = (5L + state.playerState.freePackGemYieldLevel * 5) * multiplier
            val coinReward = (state.playerState.freePackCoinYieldLevel * 100L) * multiplier
            val cooldownMs = freeCooldownMs(state.playerState.freePackCooldownLevel)
            
            val updated = GameEngine.applyPackResult(
                state = state.playerState.copy(
                    gems = state.playerState.gems + gemReward,
                    coins = state.playerState.coins + coinReward,
                    freePackReadyAtMs = System.currentTimeMillis() + cooldownMs
                ),
                cards = state.drawnCards,
                xpReward = PackDataSource.freePack.xpReward
            )
            repository.savePlayerState(updated)
            _uiState.update { it.copy(isOpeningFreePack = false, drawnCards = emptyList(), currentCardIndex = 0, showSummary = false) }
        }
    }

    private fun tryPurchase(state: PlayerState, upgrade: Upgrade): UpgradeResult {
        if (upgrade.isMaxTier) return UpgradeResult.AlreadyMaxTier
        if (state.level < upgrade.requiredLevel) return UpgradeResult.LevelTooLow
        return if (hasEnoughResources(state, upgrade.cost)) UpgradeResult.Success else UpgradeResult.InsufficientFunds
    }

    private fun hasEnoughResources(state: PlayerState, cost: PackCost): Boolean = when (cost) {
        is PackCost.Coins -> state.coins >= cost.amount
        is PackCost.Gems  -> state.gems >= cost.amount
        is PackCost.Both  -> state.coins >= cost.coins && state.gems >= cost.gems
    }

    private fun applyUpgrade(state: PlayerState, upgrade: Upgrade): PlayerState {
        val newCoins = when (val c = upgrade.cost) {
            is PackCost.Coins -> state.coins - c.amount
            is PackCost.Both  -> state.coins - c.coins
            else              -> state.coins
        }
        val newGems = when (val c = upgrade.cost) {
            is PackCost.Gems -> state.gems - c.amount
            is PackCost.Both -> state.gems - c.gems
            else             -> state.gems
        }
        
        val baseState = state.copy(coins = newCoins, gems = newGems)
        
        return when {
            upgrade.id.startsWith("free_cooldown") -> baseState.copy(freePackCooldownLevel = state.freePackCooldownLevel + 1)
            upgrade.id.startsWith("free_gems")     -> baseState.copy(freePackGemYieldLevel = state.freePackGemYieldLevel + 1)
            upgrade.id.startsWith("free_coins")    -> baseState.copy(freePackCoinYieldLevel = state.freePackCoinYieldLevel + 1)
            upgrade.id.startsWith("free_count")    -> baseState.copy(freePackCardCountLevel = state.freePackCardCountLevel + 1)
            upgrade.id.startsWith("free_rarity")   -> baseState.copy(freePackRarityLevel = state.freePackRarityLevel + 1)
            
            upgrade.id.startsWith("basic_capacity")-> baseState.copy(basicPackCapacityLevel = state.basicPackCapacityLevel + 1, maxPacks = state.maxPacks + 10)
            upgrade.id.startsWith("basic_regen")   -> baseState.copy(basicPackRegenLevel = state.basicPackRegenLevel + 1)
            upgrade.id.startsWith("basic_count")   -> baseState.copy(basicPackCardCountLevel = state.basicPackCardCountLevel + 1)
            upgrade.id.startsWith("basic_rarity")  -> baseState.copy(basicPackRarityLevel = state.basicPackRarityLevel + 1)
            
            upgrade.id.startsWith("global_xp")     -> baseState.copy(xpMultiplierLevel = state.xpMultiplierLevel + 1)
            upgrade.id.startsWith("global_bulk")   -> baseState.copy(bulkOpenLevel = state.bulkOpenLevel + 1)
            
            else -> baseState
        }
    }

    private fun buildUpgradeList(state: PlayerState): List<Upgrade> {
        val upgrades = mutableListOf<Upgrade>()
        
        // ── GLOBAL UPGRADES ──
        val xpLvl = state.xpMultiplierLevel
        upgrades.add(Upgrade(
            id = "global_xp_t${xpLvl + 1}",
            name = "XP Multiplier",
            description = "Earn more Player XP from every pack.",
            currentValue = "${String.format(Locale.US, "%.1f", GameEngine.getXpMultiplier(xpLvl))}x",
            nextValue = "${String.format(Locale.US, "%.1f", GameEngine.getXpMultiplier(xpLvl + 1))}x",
            cost = PackCost.Coins(2000L + xpLvl * 1000L),
            requiredLevel = 5, tier = xpLvl + 1, maxTier = 10
        ))

        val bulkLvl = state.bulkOpenLevel
        upgrades.add(Upgrade(
            id = "global_bulk_t${bulkLvl + 1}",
            name = "Bulk Opening",
            description = "Unlocks the ability to open 5 packs at once.",
            currentValue = if (bulkLvl > 0) "Unlocked" else "Locked",
            nextValue = "Unlock x5",
            cost = PackCost.Gems(200L),
            requiredLevel = 5, tier = bulkLvl, maxTier = 1 // Set to level 5, fixed tier logic
        ))

        // ── BASIC PACK UPGRADES ──
        val bRegen = state.basicPackRegenLevel
        upgrades.add(Upgrade(
            id = "basic_regen_t${bRegen + 1}",
            name = "Basic Pack Regen Speed",
            description = "Reduces time to generate new basic packs.",
            currentValue = "${GameEngine.getPackRegenIntervalMs(bRegen) / 60000} min",
            nextValue = "${GameEngine.getPackRegenIntervalMs(bRegen + 1) / 60000} min",
            cost = PackCost.Coins(500L + bRegen * 300L),
            requiredLevel = 3, tier = bRegen + 1, maxTier = 5
        ))

        val bCapacity = state.basicPackCapacityLevel
        upgrades.add(Upgrade(
            id = "basic_capacity_t${bCapacity + 1}",
            name = "Basic Pack Storage",
            description = "Increases max basic pack capacity.",
            currentValue = "${state.maxPacks}",
            nextValue = "${state.maxPacks + 10}",
            cost = PackCost.Coins(300L + bCapacity * 200L),
            requiredLevel = 2, tier = bCapacity + 1, maxTier = 10
        ))

        val bCount = state.basicPackCardCountLevel
        upgrades.add(Upgrade(
            id = "basic_count_t${bCount + 1}",
            name = "Basic Pack Cards Count",
            description = "Increases cards per basic pack.",
            currentValue = "${5 + bCount} cards",
            nextValue = "${5 + bCount + 1} cards",
            cost = PackCost.Gems(50L + bCount * 50L),
            requiredLevel = 6, tier = bCount + 1, maxTier = 5
        ))

        // ── FREE PACK UPGRADES ──
        val fCooldown = state.freePackCooldownLevel
        upgrades.add(Upgrade(
            id = "free_cooldown_t${fCooldown + 1}",
            name = "Free Cooldown",
            description = "Reduces time between free packs.",
            currentValue = formatCooldown(freeCooldownMs(fCooldown)),
            nextValue = formatCooldown(freeCooldownMs(fCooldown + 1)),
            cost = PackCost.Both(coins = 200L, gems = 10L),
            requiredLevel = 3, tier = fCooldown + 1, maxTier = 5
        ))

        val fGems = state.freePackGemYieldLevel
        upgrades.add(Upgrade(
            id = "free_gems_t${fGems + 1}",
            name = "Free Gem Yield",
            description = "Increases gem rewards from free packs.",
            currentValue = "+${5 + fGems * 5} 💎",
            nextValue = "+${5 + (fGems + 1) * 5} 💎",
            cost = PackCost.Coins(1000L),
            requiredLevel = 4, tier = fGems + 1, maxTier = 5
        ))

        val fCount = state.freePackCardCountLevel
        upgrades.add(Upgrade(
            id = "free_count_t${fCount + 1}",
            name = "Bonus Pack Cards Count",
            description = "Increases cards per bonus pack.",
            currentValue = "${3 + fCount} cards",
            nextValue = "${3 + fCount + 1} cards",
            cost = PackCost.Gems(30L + fCount * 30L),
            requiredLevel = 5, tier = fCount + 1, maxTier = 5
        ))

        return upgrades
    }

    private fun freeCooldownMs(tier: Int): Long {
        if (Config.DEV_BOOST) return TimeUnit.MINUTES.toMillis(1)
        return maxOf(TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(10) - tier * TimeUnit.MINUTES.toMillis(1))
    }

    private fun formatCooldown(ms: Long): String {
        val totalMinutes = ms / 60000
        return "$totalMinutes min"
    }
}
