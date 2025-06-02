package me.baldo.mappit.utils

import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

enum class PermissionStatus {
    Unknown,
    Granted,
    Denied,
    PermanentlyDenied;

    val isGranted get() = this == Granted
    val isDenied get() = this == Denied || this == PermanentlyDenied
}

interface MultiplePermissionHandler {
    val statuses: Map<String, PermissionStatus>
    fun launchPermissionRequest()
    fun updateStatuses()
    fun shouldShowRequestPermissionRationale(): Boolean
}

@Composable
fun rememberMultiplePermissions(
    permissions: List<String>,
    onResult: (status: Map<String, PermissionStatus>) -> Unit
): MultiplePermissionHandler {
    val activity = LocalActivity.current!!

    var statuses by remember {
        mutableStateOf(
            permissions.associateWith { permission ->
                if (ContextCompat.checkSelfPermission(
                        activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                )
                    PermissionStatus.Granted
                else
                    PermissionStatus.Unknown
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { newPermissions ->
        statuses = newPermissions.mapValues { (permission, isGranted) ->
            when {
                isGranted -> PermissionStatus.Granted
                activity.shouldShowRequestPermissionRationale(permission) -> PermissionStatus.Denied
                else -> PermissionStatus.PermanentlyDenied
            }
        }
        onResult(statuses)
    }

    val permissionHandler = remember(permissionLauncher) {
        object : MultiplePermissionHandler {
            override val statuses get() = statuses
            override fun launchPermissionRequest() =
                permissionLauncher.launch(permissions.toTypedArray())

            override fun updateStatuses() {
                if (!statuses.isEmpty()) {
                    statuses = permissions.associateWith { permission ->
                        if (ContextCompat.checkSelfPermission(
                                activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                            PermissionStatus.Granted
                        else
                            statuses[permission]!!
                    }
                }
            }

            override fun shouldShowRequestPermissionRationale(): Boolean {
                return statuses.keys.any { permission ->
                    activity.shouldShowRequestPermissionRationale(permission)
                }
            }
        }
    }
    return permissionHandler
}
