package me.baldo.mappit.data.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Like
import kotlin.uuid.Uuid

class LikesRepository(
    private val postgrest: Postgrest
) {
    companion object {
        private const val LIKES_TABLE = "likes"
    }

    suspend fun addLike(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(LIKES_TABLE).insert(Like(userId, pinId))
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error adding like: ${e.message}")
                false
            }
        }
    }

    suspend fun removeLike(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(LIKES_TABLE).delete {
                    filter {
                        Like::userId eq userId
                        Like::pinId eq pinId
                    }
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error deleting like: ${e.message}")
                false
            }
        }
    }

    suspend fun getLikesOfPin(pinId: Uuid): List<Like> {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(LIKES_TABLE).select {
                    filter { Like::pinId eq pinId }
                }.decodeList<Like>()
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching likes: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun isLiked(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(LIKES_TABLE).select {
                    filter {
                        Like::userId eq userId
                        Like::pinId eq pinId
                    }
                }.decodeSingleOrNull<Like>() != null
            } catch (e: Exception) {
                Log.i("TAG", "Error checking like: ${e.message}")
                false
            }
        }
    }
}