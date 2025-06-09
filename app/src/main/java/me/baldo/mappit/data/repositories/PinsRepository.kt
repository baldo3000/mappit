package me.baldo.mappit.data.repositories

import android.net.Uri
import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.AutoCompletePin
import me.baldo.mappit.data.model.Pin
import kotlin.uuid.Uuid

class PinsRepository(
    private val postgrest: Postgrest,
    private val storage: Storage
) {
    suspend fun getPin(pinId: Uuid): Pin? {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").select() {
                    filter {
                        Pin::id eq pinId
                    }
                }.decodeSingleOrNull<Pin>()
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPins(): List<Pin> {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").select().decodeList<Pin>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getPinsOfUser(userId: Uuid): List<Pin> {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").select() {
                    filter {
                        Pin::userId eq userId
                    }
                }.decodeList<Pin>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun upsertPin(pin: AutoCompletePin): Pin? {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").upsert(pin) { select() }.decodeSingleOrNull<Pin>()
            } catch (e: Exception) {
                Log.i("TAG", "Error upserting pin: $e")
                null
            }
        }
    }

    suspend fun deletePin(pin: Pin) {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").delete {
                    filter {
                        Pin::id eq pin.id
                    }
                }
            } catch (e: Exception) {
                Log.i("TAG", "Error deleting pin: ${e.message}")
            }
        }
    }

    suspend fun updatePinImage(pinId: Uuid, image: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                storage.from("pins").upload("$pinId.jpg", image) {
                    upsert = true
                }
                true
            } catch (e: Exception) {
                Log.i("TAG", "Error uploading pin image: ${e.message}")
                false
            }
        }
    }

    suspend fun getPinImageUrl(pinId: Uuid): String {
        return withContext(Dispatchers.IO) {
            try {
                storage.from("pins").publicUrl("$pinId.jpg")
            } catch (e: Exception) {
                Log.i("TAG", "Error fetching user avatar: ${e.message}")
                ""
            }
        }
    }
}