package me.baldo.mappit.data.repositories

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Profile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UsersRepository(
    private val postgrest: Postgrest
) {
    @OptIn(ExperimentalUuidApi::class)
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
}