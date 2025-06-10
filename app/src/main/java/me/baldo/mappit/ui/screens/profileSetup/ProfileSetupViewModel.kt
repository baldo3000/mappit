package me.baldo.mappit.ui.screens.profileSetup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.repositories.UsersRepository
import kotlin.uuid.Uuid

data class ProfileSetupState(
    val fullName: String = "",
    val username: String = "",
    val avatar: Uri = Uri.EMPTY,
    val isSaving: Boolean = false,
    val done: Boolean = false
)

interface ProfileSetupActions {
    fun onSaveProfile()
    fun onFullNameChanged(fullName: String)
    fun onUsernameChanged(username: String)
    fun onAvatarChanged(image: Uri)
}

class ProfileSetupViewModel(
    private val userId: Uuid,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileSetupState())
    val state = _state.asStateFlow()


    val actions = object : ProfileSetupActions {
        override fun onSaveProfile() {
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                usersRepository.getUser(userId)?.let {
                    usersRepository.updateUser(
                        it.copy(
                            username = _state.value.username,
                            fullName = _state.value.fullName
                        )
                    )
                    usersRepository.updateUserAvatar(it, _state.value.avatar)
                }
                _state.update { it.copy(done = true) }
            }
        }

        override fun onFullNameChanged(fullName: String) {
            _state.update { it.copy(fullName = fullName) }
        }

        override fun onUsernameChanged(username: String) {
            _state.update { it.copy(username = username) }
        }

        override fun onAvatarChanged(image: Uri) {
            _state.update { it.copy(avatar = image) }
        }
    }
}