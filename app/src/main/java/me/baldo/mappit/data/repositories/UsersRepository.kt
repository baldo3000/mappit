package me.baldo.mappit.data.repositories

import android.net.Uri
import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.data.remote.Buckets
import me.baldo.mappit.data.remote.Tables
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid

class UsersRepository(
    private val postgrest: Postgrest,
    private val storage: Storage
) {
    suspend fun getUser(id: Uuid): Profile? {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.PROFILES).select {
                    filter {
                        Profile::id eq id
                    }
                }.decodeSingleOrNull<Profile>()
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching user: ${e.message}")
                null
            }
        }
    }

    suspend fun updateUser(profile: Profile): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from(Tables.PROFILES).update(profile) {
                    filter {
                        Profile::id eq profile.id
                    }
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error updating user: ${e.message}")
                false
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun updateUserAvatar(profile: Profile, newImage: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val newImageName = "${profile.id}-${Clock.System.now()}.jpg"
                val newUrl = getUserAvatarUrl(newImageName, profile.username ?: profile.email)
                storage.from(Buckets.AVATARS).upload(newImageName, newImage) {
                    upsert = true
                }
                updateUser(profile.copy(avatarUrl = newUrl))
                profile.avatarUrl?.let { oldAvatarUrl ->
                    val oldImageName = oldAvatarUrl.substringAfterLast('/')
                    deleteUserAvatar(oldImageName)
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error updating user avatar: ${e.message}")
                false
            }
        }
    }

    suspend fun getUserAvatarUrl(imageName: String, placeholderName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                storage.from(Buckets.AVATARS).publicUrl(imageName)
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching user avatar: ${e.message}")
                "https://ui-avatars.com/api/?name=$placeholderName&background=random&size=512"
            }
        }
    }

    suspend fun deleteUserAvatar(imageName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                storage.from(Buckets.AVATARS).delete(imageName)
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error deleting user avatar: ${e.message}")
                false
            }
        }
    }
}