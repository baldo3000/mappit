package me.baldo.mappit.ui.screens.addpin

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.AutoCompletePin
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.PinsRepository
import kotlin.uuid.Uuid

interface AddState {
    data object Editing : AddState
    data object Error : AddState
    data object Success : AddState
}

data class AddPinState(
    val title: String = "",
    val description: String = "",
    val addState: AddState = AddState.Editing,
    val isSaving: Boolean = false,
    val image: Uri = Uri.EMPTY
)

interface AddPinActions {
    fun onUpdateTitle(title: String)
    fun onUpdateDescription(description: String)
    fun addPin(position: LatLng)
    fun onImageChanged(image: Uri)
}

class AddPinViewModel(
    private val pinsRepository: PinsRepository,
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
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                _state.update { state ->
                    authenticationRepository.user?.let { user ->
                        val pin = AutoCompletePin(
                            title = state.title,
                            description = state.description,
                            latitude = position.latitude,
                            longitude = position.longitude,
                            userId = Uuid.parse(user.id)
                        )
                        Log.i("TAG", "Adding pin: $pin")
                        pinsRepository.upsertPin(pin)?.let { addedPin ->
                            if (state.image == Uri.EMPTY || pinsRepository.updatePinImage(
                                    addedPin.id,
                                    state.image
                                )
                            ) {
                                state.copy(addState = AddState.Success)
                            } else {
                                pinsRepository.deletePin(addedPin)
                                state.copy(addState = AddState.Error, isSaving = false)
                            }
                        } ?: run {
                            state.copy(addState = AddState.Error, isSaving = false)
                        }
                    } ?: run {
                        state.copy(addState = AddState.Error, isSaving = false)
                    }
                }
            }
        }

        override fun onImageChanged(image: Uri) {
            _state.update { it.copy(image = image) }
        }
    }
}