package me.baldo.mappit.ui.screens.profileSetup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel(assistedFactory = ProfileSetupViewModel.ProfileSetupViewModelFactory::class)
class ProfileSetupViewModel @AssistedInject constructor(
    @Assisted private val userId: Uuid,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileSetupState())
    val state = _state.asStateFlow()

    val actions = object : ProfileSetupActions {
        override fun onSaveProfile() {
            _state.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                usersRepository.getUser(userId)?.let {
                    val updatedUser =
                        it.copy(username = _state.value.username, fullName = _state.value.fullName)
                    if (_state.value.avatar == Uri.EMPTY) {
                        usersRepository.updateUser(updatedUser)
                    } else {
                        usersRepository.updateUserAvatar(updatedUser, _state.value.avatar)
                    }
                    _state.update { it.copy(done = true) }
                } ?: run {
                    _state.update { it.copy(isSaving = false) }
                }
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

    @AssistedFactory
    interface ProfileSetupViewModelFactory {
        fun create(userId: Uuid): ProfileSetupViewModel
    }
}
