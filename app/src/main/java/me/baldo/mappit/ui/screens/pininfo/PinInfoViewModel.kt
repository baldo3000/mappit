package me.baldo.mappit.ui.screens.pininfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.repositories.PinsRepository
import me.baldo.mappit.data.repositories.UsersRepository
import kotlin.uuid.Uuid

data class PinInfoState(
    val pin: Pin? = null,
    val profile: Profile? = null,
    val isLoading: Boolean = true
)

interface PinInfoActions {

}

class PinInfoViewModel(
    private val pinId: Uuid,
    private val pinsRepository: PinsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PinInfoState>(PinInfoState())
    val state = _state.asStateFlow()

    val actions = object : PinInfoActions {

    }

    init {
        viewModelScope.launch {
            val pin = pinsRepository.getPin(pinId)
            val profile = pin?.userId?.let { userId ->
                usersRepository.getUser(userId)?.let {
                    if (it.avatarUrl.isNullOrEmpty()) it.copy(
                        avatarUrl = usersRepository.getUserAvatarUrl(
                            it.id,
                            it.username ?: it.email
                        )
                    ) else it
                }
            }

            _state.update {
                it.copy(
                    pin = pin,
                    profile = profile,
                    isLoading = false
                )
            }
        }
    }
}