package me.baldo.mappit.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import me.baldo.mappit.R
import java.io.File

interface ImageLauncher {
    val selectedImageUri: Uri
    fun selectImage()
}

@Composable
fun rememberImageLauncher(
    onImageSelected: (Uri) -> Unit = {}
): ImageLauncher {
    val ctx = LocalContext.current

    var selectedImageUri by remember { mutableStateOf(Uri.EMPTY) }

    val cameraImageUri = remember {
        val imageFile = File.createTempFile("tmp_image", ".jpg", ctx.externalCacheDir)
        FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", imageFile)
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    selectedImageUri = uri
                    onImageSelected(uri)
                } ?: run {
                    selectedImageUri = cameraImageUri
                    onImageSelected(cameraImageUri)
                }
            }
        }

    val chooserText = stringResource(R.string.profile_chooser)

    val imageLauncher = remember {
        object : ImageLauncher {
            override val selectedImageUri: Uri
                get() = selectedImageUri

            override fun selectImage() {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                }
                val photoPickerIntent =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                            Build.VERSION_CODES.R
                        ) >= 2
                    ) {
                        Intent(MediaStore.ACTION_PICK_IMAGES)
                    } else {
                        Intent(Intent.ACTION_PICK).apply {
                            type = "image/*"
                        }
                    }
                val chooserIntent =
                    Intent.createChooser(photoPickerIntent, chooserText).apply {
                        putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            arrayOf(cameraIntent, photoPickerIntent)
                        )
                    }
                launcher.launch(chooserIntent)
            }
        }
    }
    return imageLauncher
}

fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
    val bitmap = when {
        Build.VERSION.SDK_INT < 28 -> {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }

        else -> {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }
    }
    return bitmap
}
