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
    val playerState: PlayerState? = null,
    val activeCollection: CardCollection = CardDataSource.genesisCardCollection,
    val packDefinition: PackDefinition = PackDataSource.basicPack,
    val isOpeningPack: Boolean = false,
    val drawnCards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val surprisePackId: String? = null,
    val showSummary: Boolean = false,
    val sessionSaved: Boolean = false
) {
    val currentCard: Card? get() = drawnCards.getOrNull(currentCardIndex)
    val isLastCard: Boolean get() = currentCardIndex >= drawnCards.lastIndex
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
                val refreshedState = GameEngine.refreshPackCount(playerState, now)
                if (refreshedState != playerState) {
                    repository.savePlayerState(refreshedState)
                }
                _uiState.update { it.copy(playerState = refreshedState) }
            }
        }
    }

    fun openPack() {
        val state = _uiState.value
        val player = state.playerState ?: return
        val collection = state.activeCollection

        if (player.availablePacks <= 0) return

        val surpriseId: String? = if (state.packDefinition.type == PackType.BASIC) {
            GameEngine.checkSurpriseEvent()
        } else null

        val effectivePack = if (surpriseId != null) {
            resolveSurprisePack(surpriseId, state.packDefinition) ?: state.packDefinition
        } else state.packDefinition

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
                isFreePack = false // Basic packs always consume
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
