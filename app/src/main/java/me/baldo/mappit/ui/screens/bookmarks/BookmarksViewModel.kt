package me.baldo.mappit.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.repositories.BookmarksRepository
import me.baldo.mappit.data.repositories.UsersRepository
import kotlin.uuid.Uuid

data class BookmarksState(
    val bookmarks: Map<Pin, Profile> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

interface BookmarksActions {
    fun refreshBookmarks()
    fun refreshBookmarksSilent()
}

class BookmarksViewModel(
    private val userId: Uuid,
    private val bookmarksRepository: BookmarksRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BookmarksState())
    val state = _state.asStateFlow()

    val actions = object : BookmarksActions {
        override fun refreshBookmarks() {
            viewModelScope.launch {
                _state.update { it.copy(isRefreshing = true) }
                val bookmarks = bookmarksRepository.getBookmarksOfUser(userId)
                    .mapNotNull { pin ->
                        usersRepository.getUser(pin.userId)?.let { profile -> pin to profile }
                    }
                    .sortedByDescending { it.first.createdAt }
                    .toMap()
                _state.update {
                    it.copy(
                        bookmarks = bookmarks,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }

        override fun refreshBookmarksSilent() {
            if (!_state.value.isLoading) {
                viewModelScope.launch {
                    delay(100)
                    val bookmarks = bookmarksRepository.getBookmarksOfUser(userId)
                        .mapNotNull { pin ->
                            usersRepository.getUser(pin.userId)?.let { profile -> pin to profile }
                        }
                        .sortedByDescending { it.first.createdAt }
                        .toMap()
                    _state.update { it.copy(bookmarks = bookmarks) }
                }
            }
        }
    }

    init {
        actions.refreshBookmarks()
    }
}