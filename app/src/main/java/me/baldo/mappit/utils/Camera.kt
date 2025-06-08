package me.baldo.mappit.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

interface CameraLauncher {
    val capturedImageUri: Uri
    fun captureImage()
}

@Composable
fun rememberCameraLauncher(
    onPictureTaken: (Uri) -> Unit = {}
): ImageLauncher {
    val ctx = LocalContext.current

    val imageUri = remember {
        val imageFile = File.createTempFile("tmp_image", ".jpg", ctx.externalCacheDir)
        FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", imageFile)
    }
    var capturedImageUri by remember { mutableStateOf(Uri.EMPTY) }

    val cameraActivityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { pictureTaken ->
            if (pictureTaken) {
                capturedImageUri = imageUri
                onPictureTaken(capturedImageUri)
            }
        }

    val cameraLauncher = remember {
        object : ImageLauncher {
            override val selectedImageUri: Uri
                get() = capturedImageUri

            override fun selectImage() {
                cameraActivityLauncher.launch(imageUri)
            }

        }
    }
    return cameraLauncher
}