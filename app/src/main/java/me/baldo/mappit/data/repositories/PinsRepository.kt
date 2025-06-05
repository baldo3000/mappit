package me.baldo.mappit.data.repositories

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.AutoCompletePin
import me.baldo.mappit.data.model.Pin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PinsRepository(
    private val postgrest: Postgrest
) {
    @OptIn(ExperimentalUuidApi::class)
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

    suspend fun upsertPin(pin: AutoCompletePin) {
        return withContext(Dispatchers.IO) {
            try {
                postgrest.from("pins").upsert(pin)
            } catch (e: Exception) {
            }
        }
    }
}