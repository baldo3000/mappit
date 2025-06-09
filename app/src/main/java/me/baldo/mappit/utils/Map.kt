package me.baldo.mappit.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
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

@Composable
fun getDynamicMapStyle(): MapStyleOptions {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()
    val elementsColor = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val elementsBorderColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val textColorDim = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f).toArgb()
    val parkColor = MaterialTheme.colorScheme.tertiaryContainer.toArgb()
    val parkStrokeColor = MaterialTheme.colorScheme.onTertiaryContainer.toArgb()

    val mapStyleJson = """
        [
          {
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#${Integer.toHexString(surfaceColor)}"
              }
            ]
          },
          {
            "elementType": "labels.icon",
            "stylers": [
              {
                "visibility": "off"
              }
            ]
          },
          {
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#${Integer.toHexString(textColor)}"
              }
            ]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [
              {
                "color": "#${Integer.toHexString(backgroundColor)}"
              }
            ]
          },
          {
            "featureType": "administrative",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#757575"
              }
            ]
          },
          {
            "featureType": "administrative.land_parcel",
            "stylers": [
              {
                "visibility": "off"
              }
            ]
          },
          {
            "featureType": "poi",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#${Integer.toHexString(backgroundColor)}"
              }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#${Integer.toHexString(parkColor)}"
              }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#${Integer.toHexString(parkColor)}"
              }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "labels.text.stroke",
            "stylers": [
              {
                "color": "#${Integer.toHexString(parkStrokeColor)}"
              }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry.fill",
            "stylers": [
              {
                "color": "#${Integer.toHexString(elementsColor)}"
              }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry.stroke",
            "stylers": [
              {
                "color": "#${Integer.toHexString(elementsBorderColor)}"
              }
            ]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#${Integer.toHexString(backgroundColor)}"
              }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#${Integer.toHexString(textColorDim)}"
              }
            ]
          }
        ]
    """.trimIndent()

    return MapStyleOptions(mapStyleJson)
}