package me.baldo.mappit.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.LikesRepository
import me.baldo.mappit.data.repositories.PinsRepository
import me.baldo.mappit.data.repositories.UsersRepository
import me.baldo.mappit.utils.getPrettyFormatDay
import kotlin.uuid.Uuid

data class ProfileState(
    val profile: Profile? = null,
    val pins: List<Pin> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,

    val isEditing: Boolean = false,
    val editUsername: String = "",
    val editFullName: String = "",

    val joinedOn: String = "-",
    val pinsNumber: String = "-",
    val likesNumber: String = "-"
)

interface ProfileActions {
    fun onEditProfile()
    fun onSaveProfile()
    fun onLogout()
    fun onUsernameChanged(username: String)
    fun onFullNameChanged(fullName: String)
    fun onAvatarChanged(image: Uri)

    fun refreshProfile()
    fun refreshProfileSilent()
}

class ProfileViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val usersRepository: UsersRepository,
    private val pinsRepository: PinsRepository,
    private val likesRepository: LikesRepository
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
                        profile = state.profile?.copy(
                            username = state.editUsername,
                            fullName = state.editFullName
                        ),
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

        override fun onFullNameChanged(fullName: String) {
            _state.update { it.copy(editFullName = fullName) }
        }

        override fun onAvatarChanged(image: Uri) {
            _state.value.profile?.let { profile ->
                viewModelScope.launch {
                    usersRepository.updateUserAvatar(profile, image)
                }
            }
        }

        override fun refreshProfile() {
            authenticationRepository.user?.let { user ->
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    val profile = usersRepository.getUser(Uuid.parse(user.id))
                    val pins = profile?.let {
                        pinsRepository.getPinsOfUser(it.id).sortedByDescending { it.createdAt }
                    } ?: emptyList()
                    _state.update {
                        it.copy(
                            profile = profile,
                            pins = pins,
                            editUsername = profile?.username ?: "",
                            editFullName = profile?.fullName ?: "",
                            joinedOn = profile?.createdAt?.getPrettyFormatDay() ?: "-",
                            pinsNumber = if (pins.isNotEmpty()) pins.size.toString() else "-",
                            likesNumber = profile?.let {
                                likesRepository.getLikesOfUser(it.id)
                                    .let { if (it >= 0) it.toString() else "-" }
                            } ?: "-",
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            }
        }

        override fun refreshProfileSilent() {
            if (!_state.value.isLoading) {
                authenticationRepository.user?.let { user ->
                    viewModelScope.launch {
                        delay(100)
                        val profile = usersRepository.getUser(Uuid.parse(user.id))
                        val pins = profile?.let {
                            pinsRepository.getPinsOfUser(it.id).sortedByDescending { it.createdAt }
                        } ?: emptyList()
                        _state.update {
                            it.copy(
                                profile = profile,
                                pins = pins,
                                editUsername = profile?.username ?: "",
                                editFullName = profile?.fullName ?: "",
                                joinedOn = profile?.createdAt?.getPrettyFormatDay() ?: "-",
                                pinsNumber = if (pins.isNotEmpty()) pins.size.toString() else "-",
                                likesNumber = profile?.let {
                                    likesRepository.getLikesOfUser(it.id)
                                        .let { if (it >= 0) it.toString() else "-" }
                                } ?: "-"
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        actions.refreshProfile()
    }
}