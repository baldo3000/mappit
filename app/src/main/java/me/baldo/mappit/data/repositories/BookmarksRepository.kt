package me.baldo.mappit.data.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Bookmark
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.remote.Tables
import javax.inject.Inject
import kotlin.uuid.Uuid

class BookmarksRepository @Inject constructor(
    private val postgrest: Postgrest
) {
    suspend fun addBookmark(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.BOOKMARKS).insert(Bookmark(userId, pinId))
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
                postgrest.from(Tables.BOOKMARKS).delete {
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

    suspend fun getBookmarksOfUser(userId: Uuid): List<Pin> {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.BOOKMARKS).select(Columns.raw("${Tables.PINS}(*)")) {
                    filter { Bookmark::userId eq userId }
                }.decodeList<Map<String, Pin>>().mapNotNull { it[Tables.PINS] }
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching bookmarks: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun isBookmarked(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.BOOKMARKS).select {
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