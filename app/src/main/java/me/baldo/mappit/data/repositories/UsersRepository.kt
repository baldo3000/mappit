package me.baldo.mappit.data.repositories

import android.net.Uri
import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Profile
import kotlin.uuid.Uuid

class UsersRepository(
    private val postgrest: Postgrest,
    private val storage: Storage
) {
    suspend fun getUser(id: Uuid): Profile? {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("profiles").select() {
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

    /*
    supabase.from("characters").update(
    {
       Country::name setTo "Han Solo"
       //or
       set("name", "Han Solo")
    }
) {
    filter {
        Character::id eq 1
        //or
        eq("id", 1)
    }
}
     */

    suspend fun updateUser(profile: Profile): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("profiles").update({
                    Profile::username setTo profile.username
                }) {
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

    suspend fun updateUserAvatar(userId: Uuid, image: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                storage.from("avatars").upload("$userId.jpg", image) {
                    upsert = true
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error updating user avatar: ${e.message}")
                false
            }
        }
    }

    suspend fun getUserAvatarUrl(userId: Uuid, placeholderName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                storage.from("avatars").publicUrl("$userId.jpg")
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching user avatar: ${e.message}")
                "https://ui-avatars.com/api/?name=$placeholderName&background=random&size=512"
            }
        }
    }
}