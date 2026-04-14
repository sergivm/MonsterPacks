package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CollectionUiState(
    val playerState: PlayerState = PlayerState(),
    val collection: Collection = CardDataSource.genesisCollection,
    val sortedCards: List<Card> = emptyList(),
    val filteredCards: List<Card> = emptyList(),
    val activeRarityFilter: Set<Rarity> = emptySet(),
    val activeTypeFilter: Set<CardType> = emptySet(),
    val selectedCard: Card? = null
) {
    /** X/Y progress per rarity for the collection header counters. */
    val progressByRarity: Map<Rarity, Pair<Int, Int>> get() =
        Rarity.values().associateWith { rarity ->
            val total = collection.totalByRarity(rarity)
            val owned = sortedCards.count { it.rarity == rarity && playerState.hasCard(it.id) }
            owned to total
        }
}

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        repository.observePlayerState()
            .onEach { playerState ->
                val sorted = GameEngine.sortForCollection(CardDataSource.genesisCollection.cards)
                _uiState.update { state ->
                    state.copy(
                        playerState = playerState,
                        sortedCards = sorted,
                        filteredCards = applyFilters(
                            sorted,
                            state.activeRarityFilter,
                            state.activeTypeFilter
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleRarityFilter(rarity: Rarity) {
        _uiState.update { state ->
            val updated = state.activeRarityFilter.toMutableSet().apply {
                if (contains(rarity)) remove(rarity) else add(rarity)
            }
            state.copy(
                activeRarityFilter = updated,
                filteredCards = applyFilters(state.sortedCards, updated, state.activeTypeFilter)
            )
        }
    }

    fun toggleTypeFilter(type: CardType) {
        _uiState.update { state ->
            val updated = state.activeTypeFilter.toMutableSet().apply {
                if (contains(type)) remove(type) else add(type)
            }
            state.copy(
                activeTypeFilter = updated,
                filteredCards = applyFilters(state.sortedCards, state.activeRarityFilter, updated)
            )
        }
    }

    fun clearFilters() {
        _uiState.update { state ->
            state.copy(
                activeRarityFilter = emptySet(),
                activeTypeFilter = emptySet(),
                filteredCards = state.sortedCards
            )
        }
    }

    fun selectCard(card: Card) = _uiState.update { it.copy(selectedCard = card) }
    fun dismissCard() = _uiState.update { it.copy(selectedCard = null) }

    private fun applyFilters(
        cards: List<Card>,
        rarityFilter: Set<Rarity>,
        typeFilter: Set<CardType>
    ): List<Card> {
        var result = cards
        if (rarityFilter.isNotEmpty()) result = result.filter { it.rarity in rarityFilter }
        if (typeFilter.isNotEmpty()) result = result.filter { it.type in typeFilter }
        return result
    }
}
