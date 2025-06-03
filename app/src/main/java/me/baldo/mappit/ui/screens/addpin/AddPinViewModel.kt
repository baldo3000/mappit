package me.baldo.mappit.ui.screens.addpin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.AutoCompletePin
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.PinRepository

interface AddState {
    data object Editing : AddState
    data object Sending : AddState
    data object Success : AddState
}

data class AddPinState(
    val title: String = "",
    val description: String = "",
    val isError: Boolean = false,
    val addState: AddState = AddState.Editing
)

interface AddPinActions {
    fun onUpdateTitle(title: String)
    fun onUpdateDescription(description: String)
    fun addPin(position: LatLng)
}

class AddPinViewModel(
    private val pinRepository: PinRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddPinState())
    val state = _state.asStateFlow()

    val actions = object : AddPinActions {
        override fun onUpdateTitle(title: String) {
            _state.update { it.copy(title = title) }
        }

        override fun onUpdateDescription(description: String) {
            _state.update { it.copy(description = description) }
        }

        override fun addPin(position: LatLng) {
            _state.update { it.copy(addState = AddState.Sending) }
            viewModelScope.launch {
                val userId = authenticationRepository.userId
                if (userId != null) {
                    _state.update { it.copy(addState = AddState.Success) }
                    val pin = AutoCompletePin(
                        title = _state.value.title,
                        description = _state.value.description,
                        latitude = position.latitude,
                        longitude = position.longitude,
                        userId = authenticationRepository.userId!!
                    )
                    pinRepository.upsertPin(pin)
                } else {
                    _state.update { it.copy(isError = true, addState = AddState.Editing) }
                }
            }
        }
    }
}