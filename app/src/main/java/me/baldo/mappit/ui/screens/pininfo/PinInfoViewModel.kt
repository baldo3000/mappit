package me.baldo.mappit.ui.screens.pininfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.BookmarksRepository
import me.baldo.mappit.data.repositories.LikesRepository
import me.baldo.mappit.data.repositories.PinsRepository
import me.baldo.mappit.data.repositories.UsersRepository
import kotlin.uuid.Uuid

data class PinInfoState(
    val pin: Pin? = null,
    val profile: Profile? = null,
    val imageUrl: String = "",
    val isLoading: Boolean = true,

    val likes: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

interface PinInfoActions {
    fun toggleLike(liked: Boolean)
    fun toggleBookmark(bookmarked: Boolean)
    fun deletePin(pin: Pin)
    fun deleteAvailable(): Boolean
}

class PinInfoViewModel(
    private val pinId: Uuid,
    private val pinsRepository: PinsRepository,
    private val usersRepository: UsersRepository,
    private val authenticationRepository: AuthenticationRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val likesRepository: LikesRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PinInfoState>(PinInfoState())
    val state = _state.asStateFlow()

    val actions = object : PinInfoActions {
        override fun toggleLike(liked: Boolean) {
            authenticationRepository.user?.let { user ->
                _state.update { it.copy(isLiked = liked, likes = it.likes + if (liked) 1 else -1) }
                if (liked) {
                    viewModelScope.launch {
                        likesRepository.addLike(
                            Uuid.parse(user.id),
                            pinId
                        )
                    }
                } else {
                    viewModelScope.launch {
                        likesRepository.removeLike(
                            Uuid.parse(user.id),
                            pinId
                        )
                    }
                }
            }
        }

        override fun toggleBookmark(bookmarked: Boolean) {
            authenticationRepository.user?.let { user ->
                _state.update { it.copy(isBookmarked = bookmarked) }
                if (bookmarked) {
                    viewModelScope.launch {
                        bookmarksRepository.addBookmark(
                            Uuid.parse(user.id),
                            pinId
                        )
                    }
                } else {
                    viewModelScope.launch {
                        bookmarksRepository.removeBookmark(
                            Uuid.parse(user.id),
                            pinId
                        )
                    }
                }
            }
        }

        override fun deletePin(pin: Pin) {
            viewModelScope.launch {
                pinsRepository.deletePin(pin)
            }
        }

        override fun deleteAvailable(): Boolean {
            return authenticationRepository.user?.id == state.value.pin?.userId?.toString()
        }
    }

    init {
        viewModelScope.launch {
            val pin = pinsRepository.getPin(pinId)
            val profile = pin?.userId?.let { userId ->
                usersRepository.getUser(userId)
            }
            val image = pin?.let { pin ->
                pinsRepository.getPinImageUrl(pin.id)
            } ?: ""

            val isBookmarked = authenticationRepository.user?.let { user ->
                bookmarksRepository.isBookmarked(Uuid.parse(user.id), pinId)
            } == true

            val isLiked = authenticationRepository.user?.let { user ->
                likesRepository.isLiked(Uuid.parse(user.id), pinId)
            } == true

            val likes = likesRepository.getLikesOfPin(pinId).size

            _state.update {
                it.copy(
                    pin = pin,
                    profile = profile,
                    imageUrl = image,
                    isLoading = false,
                    likes = likes,
                    isLiked = isLiked,
                    isBookmarked = isBookmarked
                )
            }
        }
    }
}