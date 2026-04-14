package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val playerState: PlayerState = PlayerState(),
    val upgrades: List<Upgrade> = emptyList(),
    val bonusPackCooldownRemainingMs: Long = 0L,
    val isBonusPackReady: Boolean = false,
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
                        bonusPackCooldownRemainingMs = state.bonusPackCooldownRemainingMs(nowMs),
                        isBonusPackReady = state.isBonusPackReady(nowMs)
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

    // ── Bonus Pack ────────────────────────────────────────────────────────────

    fun claimBonusPack() {
        val state = _uiState.value.playerState
        if (!state.isBonusPackReady(System.currentTimeMillis())) return
        viewModelScope.launch {
            // TODO: Roll bonus pack cards and present them (navigate to card reveal)
            // For now, just reset the cooldown
            val cooldownMs = bonusCooldownMs(state.bonusPackUpgradeLevel)
            val updated = state.copy(
                bonusPackReadyAtMs = System.currentTimeMillis() + cooldownMs
            )
            repository.savePlayerState(updated)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun tryPurchase(state: PlayerState, upgrade: Upgrade): UpgradeResult {
        if (upgrade.isMaxTier) return UpgradeResult.AlreadyMaxTier
        if (state.level < upgrade.requiredLevel) return UpgradeResult.LevelTooLow
        return when (val cost = upgrade.cost) {
            is PackCost.Coins -> if (state.coins >= cost.amount) UpgradeResult.Success else UpgradeResult.InsufficientFunds
            is PackCost.Gems  -> if (state.gems >= cost.amount) UpgradeResult.Success else UpgradeResult.InsufficientFunds
            is PackCost.Both  -> if (state.coins >= cost.coins && state.gems >= cost.gems) UpgradeResult.Success else UpgradeResult.InsufficientFunds
        }
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
        return when {
            upgrade.id.startsWith("bonus_") -> state.copy(
                coins = newCoins, gems = newGems,
                bonusPackUpgradeLevel = state.bonusPackUpgradeLevel + 1
            )
            upgrade.id.startsWith("basic_") -> state.copy(
                coins = newCoins, gems = newGems,
                basicPackUpgradeLevel = state.basicPackUpgradeLevel + 1
            )
            else -> state.copy(coins = newCoins, gems = newGems)
        }
    }

    /**
     * Builds the list of upgrades shown in the Shop.
     * Values are illustrative — tune during playtesting.
     * TODO: Extract upgrade definitions to a data source once values are finalised.
     */
    private fun buildUpgradeList(state: PlayerState): List<Upgrade> {
        val bonusTier = state.bonusPackUpgradeLevel
        val basicTier = state.basicPackUpgradeLevel
        val cooldownBase = 10 * 60 * 1000L // 10 minutes in ms

        return listOf(
            Upgrade(
                id = "bonus_cooldown_t${bonusTier + 1}",
                name = "Bonus Pack Cooldown",
                description = "Reduces the time between free Bonus Packs.",
                currentValue = formatCooldown(bonusCooldownMs(bonusTier)),
                nextValue = formatCooldown(bonusCooldownMs(bonusTier + 1)),
                cost = PackCost.Both(coins = 200L, gems = 10L),
                requiredLevel = 2,
                tier = bonusTier + 1,
                maxTier = 5
            ),
            Upgrade(
                id = "bonus_gems_t${bonusTier + 1}",
                name = "Bonus Pack Gem Yield",
                description = "Increases gems earned from each Bonus Pack.",
                currentValue = "${5 + bonusTier * 2} gems",
                nextValue = "${5 + (bonusTier + 1) * 2} gems",
                cost = PackCost.Coins(500L),
                requiredLevel = 4,
                tier = bonusTier + 1,
                maxTier = 5
            ),
            Upgrade(
                id = "bonus_cards_t${bonusTier + 1}",
                name = "Bonus Pack Card Count",
                description = "Adds an extra card to each Bonus Pack.",
                currentValue = "${3 + bonusTier} cards",
                nextValue = "${3 + bonusTier + 1} cards",
                cost = PackCost.Gems(30L),
                requiredLevel = 6,
                tier = bonusTier + 1,
                maxTier = 3
            ),
            Upgrade(
                id = "bonus_stored_t${bonusTier + 1}",
                name = "Bonus Pack Storage",
                description = "Allows more Bonus Packs to be stored at once.",
                currentValue = "${1 + bonusTier} stored",
                nextValue = "${1 + bonusTier + 1} stored",
                cost = PackCost.Coins(300L),
                requiredLevel = 5,
                tier = bonusTier + 1,
                maxTier = 4
            ),
            Upgrade(
                id = "basic_xp_t${basicTier + 1}",
                name = "Basic Pack XP Reward",
                description = "Earn more XP each time you open a Basic Pack.",
                currentValue = "${10 + basicTier * 5} XP",
                nextValue = "${10 + (basicTier + 1) * 5} XP",
                cost = PackCost.Coins(400L),
                requiredLevel = 3,
                tier = basicTier + 1,
                maxTier = 5
            )
        )
    }

    private fun bonusCooldownMs(tier: Int): Long = maxOf(60_000L, 10 * 60 * 1000L - tier * 30_000L)

    private fun formatCooldown(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (seconds == 0L) "$minutes min" else "$minutes:${seconds.toString().padStart(2, '0')} min"
    }
}
