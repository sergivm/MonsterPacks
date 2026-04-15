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
    val playerState: PlayerState = PlayerState(),
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
    val isFirstLaunch: Boolean get() = playerState.needsUsername
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
                _uiState.update { it.copy(playerState = playerState) }
            }
        }
    }

    /** Called when the player taps "Open a Pack". Checks for Surprise Event, then rolls cards. */
    fun openPack() {
        val pack = _uiState.value.packDefinition
        val collection = _uiState.value.activeCollection

        // Check surprise event (only applies to BASIC packs)
        val surpriseId: String? = if (pack.type == PackType.BASIC) {
            GameEngine.checkSurpriseEvent()
        } else null

        // Determine effective pack definition
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

    /** Called when the player taps to reveal the next card. */
    fun revealNextCard() {
        val state = _uiState.value
        if (state.isLastCard) {
            _uiState.update { it.copy(showSummary = true) }
        } else {
            _uiState.update { it.copy(currentCardIndex = state.currentCardIndex + 1) }
        }
    }

    /** Called when the player taps "Save" on the summary screen. */
    fun saveSession() {
        val state = _uiState.value
        if (state.sessionSaved) return

        viewModelScope.launch {
            val updated = GameEngine.applyPackResult(
                state = state.playerState,
                cards = state.drawnCards,
                xpReward = state.packDefinition.xpReward
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

    /** Sets username on first launch. */
    fun setUsername(username: String) {
        viewModelScope.launch {
            val updated = GameEngine.setUsername(_uiState.value.playerState, username)
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
