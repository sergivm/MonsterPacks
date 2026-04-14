package com.sergivm.monsterpacks.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergivm.monsterpacks.data.repository.GameRepository
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val playerState: PlayerState = PlayerState(),
    val usernameChangeSuccess: Boolean? = null  // null = idle, true = success, false = blocked
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        repository.observePlayerState()
            .onEach { playerState -> _uiState.update { it.copy(playerState = playerState) } }
            .launchIn(viewModelScope)
    }

    /**
     * Attempts to change the username. Only succeeds if [PlayerState.usernameChanged] is false.
     * Updates [SettingsUiState.usernameChangeSuccess] with the result.
     */
    fun changeUsername(newUsername: String) {
        val state = _uiState.value.playerState
        if (state.usernameChanged) {
            _uiState.update { it.copy(usernameChangeSuccess = false) }
            return
        }
        viewModelScope.launch {
            val updated = GameEngine.changeUsername(state, newUsername)
            repository.savePlayerState(updated)
            _uiState.update { it.copy(usernameChangeSuccess = true) }
        }
    }

    fun clearUsernameResult() = _uiState.update { it.copy(usernameChangeSuccess = null) }
}
