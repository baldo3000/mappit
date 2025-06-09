package me.baldo.mappit.data.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Bookmark
import kotlin.uuid.Uuid

class BookmarksRepository(
    private val postgrest: Postgrest
) {
    companion object {
        private const val BOOKMARKS_TABLE = "bookmarks"
    }

    suspend fun addBookmark(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(BOOKMARKS_TABLE).insert(Bookmark(userId, pinId))
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error adding bookmark: ${e.message}")
                false
            }
        }
    }

    suspend fun removeBookmark(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(BOOKMARKS_TABLE).delete {
                    filter {
                        Bookmark::userId eq userId
                        Bookmark::pinId eq pinId
                    }
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error deleting bookmark: ${e.message}")
                false
            }
        }
    }

    suspend fun getBookmarksOfUser(userId: Uuid): List<Bookmark> {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(BOOKMARKS_TABLE).select {
                    filter { Bookmark::userId eq userId }
                }.decodeList<Bookmark>()
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching bookmarks: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun isBookmarked(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(BOOKMARKS_TABLE).select {
                    filter {
                        Bookmark::userId eq userId
                        Bookmark::pinId eq pinId
                    }
                }.decodeSingleOrNull<Bookmark>() != null
            } catch (e: Exception) {
                Log.i("TAG", "Error checking bookmark: ${e.message}")
                false
            }
        }
    }
}