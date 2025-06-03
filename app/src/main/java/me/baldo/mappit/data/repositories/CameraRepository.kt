package me.baldo.mappit.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.map

data class CameraPositionDto(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float,
    val bearing: Float
)

class CameraRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LAST_LATITUDE_KEY = doublePreferencesKey("last_latitude")
        private val LAST_LONGITUDE_KEY = doublePreferencesKey("last_longitude")
        private val LAST_ZOOM_KEY = floatPreferencesKey("Last_zoom")
        private val LAST_BEARING_KEY = floatPreferencesKey("Last_bearing")
    }

    val lastLatitude = dataStore.data.map { it[LAST_LATITUDE_KEY] ?: 0.0 }
    suspend fun setLastLatitude(latitude: Double) =
        dataStore.edit { it[LAST_LATITUDE_KEY] = latitude }

    val lastLongitude = dataStore.data.map { it[LAST_LONGITUDE_KEY] ?: 0.0 }
    suspend fun setLastLongitude(longitude: Double) =
        dataStore.edit { it[LAST_LONGITUDE_KEY] = longitude }

    val lastZoom = dataStore.data.map { it[LAST_ZOOM_KEY] ?: 0f }
    suspend fun setLastZoom(zoom: Float) =
        dataStore.edit { it[LAST_ZOOM_KEY] = zoom }

    val lastBearing = dataStore.data.map { it[LAST_BEARING_KEY] ?: 0f }
    suspend fun setLastBearing(bearing: Float) =
        dataStore.edit { it[LAST_BEARING_KEY] = bearing }

    val cameraPosition = dataStore.data.map { preferences ->
        CameraPositionDto(
            latitude = preferences[LAST_LATITUDE_KEY] ?: 0.0,
            longitude = preferences[LAST_LONGITUDE_KEY] ?: 0.0,
            zoom = preferences[LAST_ZOOM_KEY] ?: 0f,
            bearing = preferences[LAST_BEARING_KEY] ?: 0f
        )
    }

    suspend fun setCameraPosition(position: CameraPositionDto) {
        setLastLatitude(position.latitude)
        setLastLongitude(position.longitude)
        setLastZoom(position.zoom)
        setLastBearing(position.bearing)
    }
}