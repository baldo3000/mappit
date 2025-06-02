package me.baldo.mappit.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationService(private val ctx: Context) {
    private val fusedLocationClient = getFusedLocationProviderClient(ctx)

    private val _coordinates = MutableStateFlow<LatLng?>(null)
    val coordinates = _coordinates.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation = _isLoadingLocation.asStateFlow()

    suspend fun getCurrentLocation(usePreciseLocation: Boolean = false): LatLng? {
        val locationEnabled = isLocationEnabled(ctx)
        if (!locationEnabled) throw IllegalStateException("Location is disabled")

        val permissionGranted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) throw SecurityException("Location permission not granted")

        _isLoadingLocation.value = true
        val location = withContext(Dispatchers.IO) {
            fusedLocationClient.getCurrentLocation(
                if (usePreciseLocation) Priority.PRIORITY_HIGH_ACCURACY
                else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await()
        }
        _isLoadingLocation.value = false

        _coordinates.value =
            if (location != null) LatLng(location.latitude, location.longitude)
            else null
        return coordinates.value
    }
}

fun isLocationEnabled(ctx: Context): Boolean {
    val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun openLocationSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(ctx.packageManager) != null) {
        ctx.startActivity(intent)
    }
}