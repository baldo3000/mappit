package me.baldo.mappit.data.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Like
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.remote.Tables
import kotlin.uuid.Uuid

class LikesRepository(
    private val postgrest: Postgrest
) {
    suspend fun addLike(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.LIKES).insert(Like(userId, pinId))
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
                postgrest.from(Tables.LIKES).delete {
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
                postgrest.from(Tables.LIKES).select {
                    filter { Like::pinId eq pinId }
                }.decodeList<Like>()
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching likes: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getLikesOfUser(userId: Uuid): Int {
        return withContext(Dispatchers.IO) {
            try {
                val pinsOfUser =
                    postgrest.from(Tables.PINS).select { filter { Pin::userId eq userId } }
                        .decodeList<Pin>().map { it.id }
                val likesOfUser =
                    postgrest.from(Tables.LIKES).select { filter { Like::pinId isIn pinsOfUser } }
                        .decodeList<Like>()
                likesOfUser.size
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching likes: ${e.message}")
                0
            }
        }
    }

    suspend fun isLiked(userId: Uuid, pinId: Uuid): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.LIKES).select {
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