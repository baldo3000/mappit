package me.baldo.mappit.ui.screens.pininfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.repositories.PinRepository

data class PinInfoState(
    val pin: Pin? = null,
    val isLoading: Boolean = true
)

interface PinInfoActions {

}

class PinInfoViewModel(
    private val pinId: Long,
    private val pinRepository: PinRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PinInfoState>(PinInfoState())
    val state = _state.asStateFlow()

    val actions = object : PinInfoActions {

    }

    init {
        viewModelScope.launch {
            _state.update { it.copy(pin = pinRepository.getPin(pinId), isLoading = false) }
        }
    }
}