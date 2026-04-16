package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val playerState: PlayerState? = null, // Null means still loading from DB
    val activeCollection: CardCollection = CardDataSource.genesisCardCollection,
    val packDefinition: PackDefinition = PackDataSource.basicPack,
    // Pack opening session state
    val isOpeningPack: Boolean = false,
    val drawnCards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val surprisePackId: String? = null,   // non-null when Surprise Event triggered
    val showSummary: Boolean = false,
    val sessionSaved: Boolean = false
) {
    val currentCard: Card? get() = drawnCards.getOrNull(currentCardIndex)
    val isLastCard: Boolean get() = currentCardIndex >= drawnCards.lastIndex
    
    // Logic: Only show setup if we finished loading AND username is blank
    val isFirstLaunch: Boolean get() = playerState != null && playerState.username.isBlank()
    val isLoading: Boolean get() = playerState == null
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePlayerState().collect { playerState ->
                val now = System.currentTimeMillis()
                // Auto-refresh packs when state is observed
                val refreshedState = GameEngine.refreshPackCount(playerState, now)
                if (refreshedState != playerState) {
                    repository.savePlayerState(refreshedState)
                }
                _uiState.update { it.copy(playerState = refreshedState) }
            }
        }
    }

    /** 
     * Opens a specific pack. 
     * @param customPack If non-null, opens this instead of the default basic pack (used for Free Pack).
     */
    fun openPack(customPack: PackDefinition? = null) {
        val state = _uiState.value
        val player = state.playerState ?: return
        val pack = customPack ?: state.packDefinition
        val collection = state.activeCollection

        if (customPack == null && player.availablePacks <= 0) return

        // Check surprise event (only applies to default BASIC packs)
        val surpriseId: String? = if (customPack == null && pack.type == PackType.BASIC) {
            GameEngine.checkSurpriseEvent()
        } else null

        val effectivePack = if (surpriseId != null) {
            resolveSurprisePack(surpriseId, pack) ?: pack
        } else pack

        val cards = GameEngine.rollPack(effectivePack, collection)

        _uiState.update {
            it.copy(
                isOpeningPack = true,
                drawnCards = cards,
                currentCardIndex = 0,
                surprisePackId = surpriseId,
                showSummary = false,
                sessionSaved = false
            )
        }
    }

    fun revealNextCard() {
        val state = _uiState.value
        if (state.isLastCard) {
            _uiState.update { it.copy(showSummary = true) }
        } else {
            _uiState.update { it.copy(currentCardIndex = state.currentCardIndex + 1) }
        }
    }

    fun saveSession() {
        val state = _uiState.value
        val player = state.playerState ?: return
        if (state.sessionSaved) return

        viewModelScope.launch {
            val updated = GameEngine.applyPackResult(
                state = player,
                cards = state.drawnCards,
                xpReward = state.packDefinition.xpReward,
                isFreePack = state.surprisePackId == null && state.drawnCards.size == 3 // Simple check for free pack
            )
            repository.savePlayerState(updated)
            _uiState.update {
                it.copy(
                    sessionSaved = true,
                    isOpeningPack = false,
                    showSummary = false,
                    drawnCards = emptyList(),
                    currentCardIndex = 0,
                    surprisePackId = null
                )
            }
        }
    }

    fun setUsername(username: String) {
        val player = _uiState.value.playerState ?: return
        viewModelScope.launch {
            val updated = GameEngine.setUsername(player, username)
            repository.savePlayerState(updated)
        }
    }

    private fun resolveSurprisePack(surpriseId: String, fallback: PackDefinition): PackDefinition? {
        return when (surpriseId) {
            "type_themed"     -> PackDataSource.typeThemedPacks.random()
            "rarity_boosted"  -> PackDataSource.rareBoostedPack
            "lucky_epic"      -> PackDataSource.luckyEpicPack
            "lucky_special"   -> PackDataSource.luckySpecialPack
            "lucky_legendary" -> PackDataSource.luckyLegendaryPack
            else              -> null
        }
    }
}
