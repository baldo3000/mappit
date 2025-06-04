package me.baldo.mappit.data.repositories

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.AutoCompletePin
import me.baldo.mappit.data.model.Pin

class PinRepository(
    private val postgrest: Postgrest
) {
    suspend fun getPin(pinId: Long): Pin? {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pin").select() {
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
                postgrest.from("pin").select().decodeList<Pin>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun upsertPin(pin: AutoCompletePin) {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pin").upsert(pin)
            } catch (e: Exception) {
            }
        }
    }
}