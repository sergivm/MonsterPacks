package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ShopUiState(
    val playerState: PlayerState = PlayerState(),
    val upgrades: List<Upgrade> = emptyList(),
    val freePackCooldownRemainingMs: Long = 0L,
    val isFreePackReady: Boolean = false,
    val purchaseResult: UpgradeResult? = null
)

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

        // Tick cooldown timer every second
        viewModelScope.launch {
            while (true) {
                val nowMs = System.currentTimeMillis()
                val state = _uiState.value.playerState
                _uiState.update {
                    it.copy(
                        freePackCooldownRemainingMs = state.freePackCooldownRemainingMs(nowMs),
                        isFreePackReady = state.isFreePackReady(nowMs)
                    )
                }
                delay(1_000L)
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

    // ── Free Pack ─────────────────────────────────────────────────────────────

    fun claimFreePack() {
        val state = _uiState.value.playerState
        if (!state.isFreePackReady(System.currentTimeMillis())) return
        viewModelScope.launch {
            // TODO: In a real implementation, this would trigger navigation to Pack Opening
            // For now, we update the player state (award 5-15 gems as per GDD point 2/15)
            val gemReward = 5L + state.freePackGemYieldLevel * 2
            val cooldownMs = freeCooldownMs(state.freePackCooldownLevel)
            
            val updated = state.copy(
                gems = state.gems + gemReward,
                freePackReadyAtMs = System.currentTimeMillis() + cooldownMs
            )
            repository.savePlayerState(updated)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
            upgrade.id.startsWith("free_cards")    -> baseState.copy(freePackCardCountLevel = state.freePackCardCountLevel + 1)
            upgrade.id.startsWith("free_stored")   -> baseState.copy(freePackStoredLevel = state.freePackStoredLevel + 1)
            
            upgrade.id.startsWith("basic_rarity")  -> baseState.copy(basicPackRarityLevel = state.basicPackRarityLevel + 1)
            upgrade.id.startsWith("basic_capacity")-> baseState.copy(basicPackCapacityLevel = state.basicPackCapacityLevel + 1, maxPacks = state.maxPacks + 10)
            upgrade.id.startsWith("basic_count")   -> baseState.copy(basicPackCardCountLevel = state.basicPackCardCountLevel + 1)
            upgrade.id.startsWith("basic_xp")      -> baseState.copy(basicPackXpLevel = state.basicPackXpLevel + 1)
            
            else -> baseState
        }
    }

    private fun buildUpgradeList(state: PlayerState): List<Upgrade> {
        val upgrades = mutableListOf<Upgrade>()
        
        // ── BASIC PACK UPGRADES (Point 4) ─────────────────────────────────────
        val bRarity = state.basicPackRarityLevel
        upgrades.add(Upgrade(
            id = "basic_rarity_t${bRarity + 1}",
            name = "Basic Pack Rarity",
            description = "Slightly increases the chance of Epic, Special and Legendary cards.",
            currentValue = "Tier $bRarity",
            nextValue = "Tier ${bRarity + 1}",
            cost = PackCost.Coins(1000L + bRarity * 500L),
            requiredLevel = 5,
            tier = bRarity + 1,
            maxTier = 5
        ))

        val bCapacity = state.basicPackCapacityLevel
        upgrades.add(Upgrade(
            id = "basic_capacity_t${bCapacity + 1}",
            name = "Pack Storage",
            description = "Increases the maximum number of packs you can hold.",
            currentValue = "${state.maxPacks} packs",
            nextValue = "${state.maxPacks + 10} packs",
            cost = PackCost.Coins(300L + bCapacity * 200L),
            requiredLevel = 2,
            tier = bCapacity + 1,
            maxTier = 5
        ))

        val bCount = state.basicPackCardCountLevel
        upgrades.add(Upgrade(
            id = "basic_count_t${bCount + 1}",
            name = "Cards per Pack",
            description = "Increases cards in a basic pack (max 10).",
            currentValue = "${5 + bCount} cards",
            nextValue = "${5 + bCount + 1} cards",
            cost = PackCost.Gems(50L + bCount * 25L),
            requiredLevel = 8,
            tier = bCount + 1,
            maxTier = 5
        ))

        // ── FREE PACK UPGRADES ────────────────────────────────────────────────
        val fCooldown = state.freePackCooldownLevel
        upgrades.add(Upgrade(
            id = "free_cooldown_t${fCooldown + 1}",
            name = "Free Pack Cooldown",
            description = "Reduces the time between free packs.",
            currentValue = formatCooldown(freeCooldownMs(fCooldown)),
            nextValue = formatCooldown(freeCooldownMs(fCooldown + 1)),
            cost = PackCost.Both(coins = 200L, gems = 10L),
            requiredLevel = 3,
            tier = fCooldown + 1,
            maxTier = 5
        ))

        val fGems = state.freePackGemYieldLevel
        upgrades.add(Upgrade(
            id = "free_gems_t${fGems + 1}",
            name = "Free Pack Gem Yield",
            description = "Increases gems earned from each Free Pack.",
            currentValue = "${5 + fGems * 2} gems",
            nextValue = "${5 + (fGems + 1) * 2} gems",
            cost = PackCost.Coins(500L),
            requiredLevel = 4,
            tier = fGems + 1,
            maxTier = 5
        ))

        return upgrades
    }

    private fun freeCooldownMs(tier: Int): Long = maxOf(TimeUnit.MINUTES.toMillis(1), TimeUnit.HOURS.toMillis(4) - tier * TimeUnit.MINUTES.toMillis(30))

    private fun formatCooldown(ms: Long): String {
        val totalSeconds = ms / 1000
        val totalMinutes = totalSeconds / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "$minutes min"
    }
}
