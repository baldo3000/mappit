package me.baldo.mappit.data.repositories

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.baldo.mappit.data.model.Pin

class PinRepository(
    private val postgrest: Postgrest
) {
    suspend fun getPins(): List<Pin> {
        return withContext(Dispatchers.IO) {
            postgrest.from("pin").select().decodeList<Pin>()
        }
    }
}