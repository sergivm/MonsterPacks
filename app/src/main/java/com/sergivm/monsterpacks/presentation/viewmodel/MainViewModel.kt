package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val sessionSaved: Boolean = false,
    val setupComplete: Boolean = false 
) {
    val currentCard: Card? get() = drawnCards.getOrNull(currentCardIndex)
    val isLastCard: Boolean get() = currentCardIndex >= drawnCards.lastIndex
    val isFirstLaunch: Boolean get() = playerState != null && playerState.username.isBlank() && !setupComplete
    val isLoading: Boolean get() = playerState == null
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Observe player state from DB
        repository.observePlayerState()
            .onEach { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
            }
            .launchIn(viewModelScope)

        // Real-time Pack Regeneration Timer (Point 1)
        viewModelScope.launch {
            while (true) {
                delay(1000L) // Check every second
                val state = _uiState.value.playerState ?: continue
                val now = System.currentTimeMillis()
                
                val refreshed = GameEngine.refreshPackCount(state, now)
                if (refreshed !== state) {
                    repository.savePlayerState(refreshed)
                    // The onEach collector above will update the UI automatically
                }
            }
        }
    }

    fun openPack(count: Int = 1) {
        val state = _uiState.value
        val player = state.playerState ?: return
        if (player.availablePacks < count) return

        val now = System.currentTimeMillis()
        val consumedPlayer = GameEngine.consumePack(player, now, count)
        
        // Rolling cards for 'count' packs
        val allCards = mutableListOf<Card>()
        var totalXpReward = 0
        
        repeat(count) {
            val surpriseId: String? = if (state.packDefinition.type == PackType.BASIC) {
                GameEngine.checkSurpriseEvent()
            } else null

            val effectivePack = if (surpriseId != null) {
                resolveSurprisePack(surpriseId, state.packDefinition) ?: state.packDefinition
            } else state.packDefinition

            val cards = GameEngine.rollPack(
                pack = effectivePack,
                collection = state.activeCollection,
                playerState = player
            )
            allCards.addAll(cards)
            totalXpReward += effectivePack.xpReward
        }

        viewModelScope.launch {
            repository.savePlayerState(consumedPlayer)
        }

        // We use a custom pack definition for the summary if we open multiple
        // but for the opening sequence we just show the cards one by one
        _uiState.update {
            it.copy(
                playerState = consumedPlayer,
                isOpeningPack = true,
                drawnCards = allCards,
                currentCardIndex = 0,
                surprisePackId = if (count == 1) it.surprisePackId else null, // Hide specific surprise info if bulk
                showSummary = false,
                sessionSaved = false
            )
        }
    }

    fun revealNextCard() {
        _uiState.update { 
            if (it.isLastCard) it.copy(showSummary = true)
            else it.copy(currentCardIndex = it.currentCardIndex + 1)
        }
    }

    fun saveSession() {
        val state = _uiState.value
        val player = state.playerState ?: return
        if (state.sessionSaved) return

        viewModelScope.launch {
            // XP reward needs to handle bulk
            val baseReward = state.packDefinition.xpReward
            val totalXp = if (state.drawnCards.size > state.packDefinition.cardCount) {
                // Approximate total XP for bulk open
                val packSize = state.packDefinition.cardCount + player.basicPackCardCountLevel
                val packsOpened = state.drawnCards.size / packSize
                baseReward * packsOpened
            } else baseReward

            val updated = GameEngine.applyPackResult(player, state.drawnCards, totalXp)
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
            _uiState.update { it.copy(setupComplete = true) }
        }
    }

    private fun resolveSurprisePack(surpriseId: String, fallback: PackDefinition): PackDefinition? =
        when (surpriseId) {
            "type_themed"     -> PackDataSource.typeThemedPacks.random()
            "rarity_boosted"  -> PackDataSource.rareBoostedPack
            "lucky_epic"      -> PackDataSource.luckyEpicPack
            "lucky_special"   -> PackDataSource.luckySpecialPack
            "lucky_legendary" -> PackDataSource.luckyLegendaryPack
            else              -> null
        }
}
