package me.baldo.mappit.ui.screens.profileSetup

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import me.baldo.mappit.R
import me.baldo.mappit.utils.rememberImageLauncher

@Composable
fun ProfileSetupScreen(
    state: ProfileSetupState,
    actions: ProfileSetupActions,
    onContinue: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val imageLauncher = rememberImageLauncher { uri ->
        actions.onAvatarChanged(uri)
    }

    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    if (state.done) {
        onContinue()
    }

    fun isErrorFullName(): Boolean {
        return state.fullName.isBlank()
    }

    fun isErrorUsername(): Boolean {
        return state.username.isBlank() || state.username.contains(Regex("\\s"))
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
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.profile_setup_avatar_edit)
                ) {
                    imageLauncher.selectImage()
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
            value = state.fullName,
            onValueChange = actions::onFullNameChanged,
            label = { Text(stringResource(R.string.profile_setup_full_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            isError = isErrorFullName()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = actions::onUsernameChanged,
            label = { Text(stringResource(R.string.profile_setup_username)) },
            singleLine = true,
            prefix = {
                Text(
                    text = "@",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    keyboardController?.hide()
                    actions.onSaveProfile()
                }
            ),
            isError = isErrorUsername()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            enabled = !state.isSaving && !isErrorFullName() && !isErrorUsername(),
            onClick = {
                actions.onSaveProfile()
            },
            shapes = ButtonDefaults.shapes()
        ) { Text(stringResource(if (state.isSaving) R.string.profile_setup_saving else R.string.profile_setup_save)) }
    }
}