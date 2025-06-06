package me.baldo.mappit.ui.screens.profileSetup

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import me.baldo.mappit.R

@Composable
fun ProfileSetupScreen(
    state: ProfileSetupState,
    actions: ProfileSetupActions,
    onContinue: () -> Unit
) {
    val pickAvatar = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            actions.onAvatarChanged(uri)
        } else {
            Log.d("TAG", "No media selected")
        }
    }

    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    if (state.done) {
        onContinue()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clickable {
                    pickAvatar.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                }
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                alpha = 0.5f,
                placeholder = placeholder,
                error = placeholder,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(state.avatar)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = stringResource(R.string.profile_setup_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = actions::onUsernameChanged,
            label = { Text(stringResource(R.string.profile_setup_username)) }
        )
        Spacer(Modifier.height(8.dp))
        Button(
            enabled = !state.isSaving,
            onClick = {
                actions.onSaveProfile()
            },
            shapes = ButtonDefaults.shapes()
        ) { Text(stringResource(R.string.profile_setup_save)) }
    }
}