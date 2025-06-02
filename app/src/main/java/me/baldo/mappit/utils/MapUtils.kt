package me.baldo.mappit.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun calculateDistance(p1: LatLng, p2: LatLng): Double {
    val lat1 = p1.latitude
    val lon1 = p1.longitude
    val lat2 = p2.latitude
    val lon2 = p2.longitude
    val earthRadius = 6371e3 // Earth radius in meters

    val radLat1 = Math.toRadians(lat1)
    val radLat2 = Math.toRadians(lat2)
    val deltaLat = Math.toRadians(lat2 - lat1)
    val deltaLon = Math.toRadians(lon2 - lon1)

    val a = sin(deltaLat / 2).pow(2) +
            cos(radLat1) * cos(radLat2) *
            sin(deltaLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // Distance in meters
}