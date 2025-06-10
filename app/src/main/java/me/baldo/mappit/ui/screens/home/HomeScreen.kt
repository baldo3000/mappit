package me.baldo.mappit.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.baldo.mappit.R
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.repositories.CameraPositionDto
import me.baldo.mappit.ui.screens.settings.Theme
import me.baldo.mappit.utils.LocationService
import me.baldo.mappit.utils.PermissionStatus
import me.baldo.mappit.utils.calculateDistance
import me.baldo.mappit.utils.getDynamicMapStyle
import me.baldo.mappit.utils.isLocationEnabled
import me.baldo.mappit.utils.isOnline
import me.baldo.mappit.utils.openLocationSettings
import me.baldo.mappit.utils.openWirelessSettings
import me.baldo.mappit.utils.rememberMultiplePermissions
import java.time.format.TextStyle
import java.util.Locale
import kotlin.uuid.Uuid

private const val INTERACTION_DISTANCE = 100.0

@Composable
fun HomeScreen(
    homeState: HomeState,
    homeActions: HomeActions,
    onAddPin: () -> Unit,
    onPinInfo: (pinId: Uuid) -> Unit,
    theme: Theme,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    val locationPermissions = rememberMultiplePermissions(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    ) { statuses ->
        homeActions.disableAllWarnings()
        when {
            statuses.any { it.value == PermissionStatus.Granted } -> {}
            statuses.all { it.value == PermissionStatus.PermanentlyDenied } -> {
                homeActions.setShowLocationPermissionPermanentlyDeniedWarning(true)
            }

            else -> {
                homeActions.setShowLocationPermissionDeniedWarning(true)
            }
        }
    }

    fun update() {
        homeActions.setShowNoInternetConnectivityWarning(!isOnline(ctx))
        homeActions.setShowLocationDisabledWarning(!isLocationEnabled(ctx))
        locationPermissions.updateStatuses()
        if (locationPermissions.statuses.all { !it.value.isGranted }) {
            if (locationPermissions.shouldShowRequestPermissionRationale()) {
                homeActions.setShowLocationPermissionDeniedWarning(true)
            } else {
                homeActions.setShowLocationPermissionPermanentlyDeniedWarning(true)
            }
        } else {
            homeActions.setShowLocationPermissionDeniedWarning(false)
            homeActions.setShowLocationPermissionPermanentlyDeniedWarning(false)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        locationPermissions.launchPermissionRequest()
        update()
        homeActions.setLoading(false)
    }

    when {
        homeState.showLoading -> {}

        homeState.showLocationPermissionDeniedWarning ->
            Warning(
                icon = Icons.Outlined.GpsOff,
                title = stringResource(R.string.home_location_permissions_missing),
                description = stringResource(R.string.home_location_permissions_missing_explanation),
                buttonText = stringResource(R.string.home_location_permissions_missing_button),
                modifier = modifier
            ) { locationPermissions.launchPermissionRequest() }

        homeState.showLocationPermissionPermanentlyDeniedWarning ->
            Warning(
                icon = Icons.Outlined.GpsOff,
                title = stringResource(R.string.home_location_permissions_missing_permanently),
                description = stringResource(R.string.home_location_permissions_missing_permanently_explanation),
                buttonText = stringResource(R.string.home_location_permissions_missing_permanently_button),
                modifier = modifier
            ) {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", ctx.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }

        homeState.showLocationDisabledWarning ->
            Warning(
                icon = Icons.Outlined.GpsOff,
                title = stringResource(R.string.home_location_disabled),
                description = stringResource(R.string.home_location_disabled_explanation),
                buttonText = stringResource(R.string.home_location_disabled_button),
                modifier = modifier
            ) { openLocationSettings(ctx) }

        homeState.showNoInternetConnectivityWarning ->
            Warning(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.home_internet_disabled),
                description = stringResource(R.string.home_internet_disabled_explanation),
                buttonText = stringResource(R.string.home_internet_disabled_button),
                modifier = modifier
            ) { openWirelessSettings(ctx) }

        else ->
            MapOverlay(
                pins = homeState.pins,
                savedCameraPosition = homeState.savedCameraPosition,
                saveCameraPosition = homeActions::saveCameraPosition,
                onAddPin = onAddPin,
                onPinInfo = onPinInfo,
                theme = theme,
                modifier = modifier
            )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MapOverlay(
    pins: List<Pin>,
    savedCameraPosition: CameraPositionDto,
    saveCameraPosition: (CameraPositionDto) -> Unit,
    onAddPin: () -> Unit,
    onPinInfo: (pinId: Uuid) -> Unit,
    theme: Theme,
    modifier: Modifier = Modifier
) {
    var visualInclined by rememberSaveable { mutableStateOf(true) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(
            LatLng(savedCameraPosition.latitude, savedCameraPosition.longitude),
            savedCameraPosition.zoom,
            60f,
            savedCameraPosition.bearing
        )
    }
    val scope = rememberCoroutineScope()
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        saveCameraPosition(
            CameraPositionDto(
                cameraPositionState.position.target.latitude,
                cameraPositionState.position.target.longitude,
                cameraPositionState.position.zoom,
                cameraPositionState.position.bearing
            )
        )
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            Column {
                if (!visualInclined) {
                    InclineCameraFAB {
                        val position = CameraPosition(
                            cameraPositionState.position.target,
                            cameraPositionState.position.zoom,
                            60f,
                            cameraPositionState.position.bearing
                        )
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    position
                                )
                            )
                        }
                        visualInclined = true
                    }
                } else {
                    StraightenCameraFAB {
                        val position = CameraPosition(
                            cameraPositionState.position.target,
                            cameraPositionState.position.zoom,
                            0f,
                            cameraPositionState.position.bearing
                        )
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    position
                                )
                            )
                        }
                        visualInclined = false
                    }
                }
                Spacer(Modifier.height(16.dp))
                AddPinFAB { onAddPin() }
            }
        },
    ) {
        Map(pins, onPinInfo, cameraPositionState, theme)
    }
}

@Composable
private fun Map(
    pins: List<Pin>,
    onPinInfo: (pinId: Uuid) -> Unit,
    cameraPositionState: CameraPositionState,
    theme: Theme,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val locationService = remember { LocationService(ctx) }
    val scope = rememberCoroutineScope()
    var selectedPin by remember { mutableStateOf<Pin?>(null) }
    val fusedLocationClient = remember { getFusedLocationProviderClient(ctx) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations.reversed()) {
                    if (location != null) {
                        val position = CameraPosition(
                            LatLng(location.latitude, location.longitude),
                            cameraPositionState.position.zoom,
                            cameraPositionState.position.tilt,
                            cameraPositionState.position.bearing
                        )
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    position
                                )
                            )
                        }
                        return
                    }
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        scope.launch {
            locationService.getCurrentLocation()?.let { newPosition ->
                cameraPositionState.position =
                    CameraPosition(
                        LatLng(newPosition.latitude, newPosition.longitude),
                        cameraPositionState.position.zoom,
                        cameraPositionState.position.tilt,
                        cameraPositionState.position.bearing
                    )
            }
        }
    }

    // Start location updates when entering the Map route
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.Builder(1000L).build(),
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // Stop location updates when exiting the Map route
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Update camera when either location or bearing changes
    var bearing by remember { mutableFloatStateOf(cameraPositionState.position.bearing) }
    LaunchedEffect(bearing) {
        val position = CameraPosition(
            cameraPositionState.position.target,
            cameraPositionState.position.zoom,
            cameraPositionState.position.tilt,
            bearing
        )
        cameraPositionState.position = position
    }
    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val screenCenterY = size.height / 2
                    val screenCenterX = size.width / 2
                    val isAboveCenter = change.position.y < screenCenterY
                    val isLeftOfCenter = change.position.x < screenCenterX

                    bearing = if (isAboveCenter) {
                        (bearing - dragAmount.x * 0.1f) % 360f
                    } else {
                        (bearing + dragAmount.x * 0.1f) % 360f
                    }

                    bearing = if (isLeftOfCenter) {
                        (bearing + dragAmount.y * 0.1f) % 360f
                    } else {
                        (bearing - dragAmount.y * 0.1f) % 360f
                    }
                }
            },
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isBuildingEnabled = true,
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL,
            mapStyleOptions = getDynamicMapStyle(),
            minZoomPreference = 12f,
            maxZoomPreference = 20f
        ),
        uiSettings = MapUiSettings(
            rotationGesturesEnabled = false,
            zoomGesturesEnabled = false,
            scrollGesturesEnabled = false,
            tiltGesturesEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = true
        ),
        mapColorScheme = when (theme) {
            Theme.LIGHT -> ComposeMapColorScheme.LIGHT
            Theme.DARK -> ComposeMapColorScheme.DARK
            Theme.SYSTEM -> ComposeMapColorScheme.FOLLOW_SYSTEM
        }
    ) {
        for (pin in pins) {
            MarkerInfoWindowComposable(
                state = rememberUpdatedMarkerState(LatLng(pin.latitude, pin.longitude)),
                onClick = {
                    if (calculateDistance(
                            cameraPositionState.position.target,
                            LatLng(pin.latitude, pin.longitude)
                        ) <= INTERACTION_DISTANCE
                    ) {
                        selectedPin = pin
                    } else {
                        Toast.makeText(ctx, R.string.home_pin_get_closer, Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.PinDrop,
                    contentDescription = "${stringResource(R.string.home_pin_id)} ${pin.id}",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                        .padding(4.dp)
                )
            }
        }

        // Draw a black circle around the user's location
        Circle(
            center = cameraPositionState.position.target,
            radius = 4.0,
            fillColor = MaterialTheme.colorScheme.onTertiary,
            strokeColor = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 12f
        )
        Circle(
            center = cameraPositionState.position.target,
            radius = INTERACTION_DISTANCE,
            fillColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
            strokeColor = MaterialTheme.colorScheme.onSurfaceVariant,
            strokeWidth = 3f
        )
    }

    selectedPin?.let {
        PinInfoDialog(
            pin = it,
            onDismiss = { selectedPin = null },
            onPinInfo = {
                onPinInfo(it.id)
                selectedPin = null
            }
        )
    }
}

@Composable
private fun PinInfoDialog(
    pin: Pin,
    onDismiss: () -> Unit,
    onPinInfo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = pin.title)
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    onClick = onDismiss,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        stringResource(R.string.home_pin_close)
                    )
                }
            }
        },
        text = {
            Text(
                text = pin.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    .let { dt ->
                        val time = "%02d:%02d".format(dt.hour, dt.minute)
                        val day = "%02d".format(dt.dayOfMonth)
                        val month = dt.month.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                        val year = "%02d".format(dt.year % 100)
                        "$time · $day $month $year"
                    },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            val text = stringResource(R.string.home_pin_info)
            Button(
                onClick = onPinInfo,
                shapes = ButtonDefaults.shapes()
            ) {
                Icon(
                    Icons.Outlined.Info,
                    stringResource(R.string.home_pin_info)
                )
                Spacer(Modifier.width(8.dp))
                Text(text)
            }
        }
    )
}

@Composable
private fun InclineCameraFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Icon(
            Icons.Outlined.CloseFullscreen,
            stringResource(R.string.home_incline_visual)
        )
    }
}

@Composable
private fun StraightenCameraFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Icon(
            Icons.Outlined.OpenInFull,
            stringResource(R.string.home_straighten_visual)
        )
    }
}

@Composable
private fun AddPinFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            Icons.Outlined.Add,
            stringResource(R.string.home_add_pin)
        )
    }
}

@Composable
private fun Warning(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    onAction: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(onAction) {
            Text(buttonText)
        }
    }
}