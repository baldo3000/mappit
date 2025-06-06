package me.baldo.mappit.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.UsersRepository
import kotlin.uuid.Uuid

data class ProfileState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,

    val isEditing: Boolean = false,
    val editUsername: String = ""
)

interface ProfileActions {
    fun onEditProfile()
    fun onSaveProfile()
    fun onLogout()
    fun onUsernameChanged(username: String)
    fun onAvatarChanged(image: Uri)
}

class ProfileViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    val actions = object : ProfileActions {
        override fun onEditProfile() {
            _state.update { it.copy(isEditing = true) }
        }

        override fun onSaveProfile() {
            viewModelScope.launch {
                _state.update { state ->
                    state.copy(
                        profile = state.profile?.copy(username = state.editUsername),
                        isEditing = false
                    )
                }
                _state.value.profile?.let {
                    usersRepository.updateUser(it)
                }
            }
        }

        override fun onLogout() {
            viewModelScope.launch {
                authenticationRepository.signOut()
            }
        }

        override fun onUsernameChanged(username: String) {
            _state.update { it.copy(editUsername = username) }
        }

        override fun onAvatarChanged(image: Uri) {
            _state.value.profile?.let { profile ->
                viewModelScope.launch {
                    usersRepository.updateUserAvatar(profile.id, image)
                }
            }
        }
    }

    init {
        authenticationRepository.user?.let { user ->
            viewModelScope.launch {
                val profile = usersRepository.getUser(Uuid.parse(user.id))?.let {
                    if (it.avatarUrl.isNullOrEmpty()) it.copy(
                        avatarUrl = usersRepository.getUserAvatarUrl(
                            it.id,
                            it.username ?: it.email
                        )
                    ) else it
                }
                // Log.i("TAG", "Fetched profile: $profile")
                _state.update {
                    it.copy(
                        profile = profile,
                        editUsername = profile?.username ?: "",
                        isLoading = false
                    )
                }
            }
        }
    }
}