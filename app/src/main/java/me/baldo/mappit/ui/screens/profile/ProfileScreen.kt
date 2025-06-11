package me.baldo.mappit.ui.screens.profile

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import me.baldo.mappit.R
import me.baldo.mappit.ui.composables.AutoResizedText
import me.baldo.mappit.utils.rememberImageLauncher

@Composable
fun ProfileScreen(
    state: ProfileState,
    actions: ProfileActions,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Loading(modifier)
    } else {
        Profile(state, actions, modifier)
    }
}

@Composable
private fun Profile(
    state: ProfileState,
    actions: ProfileActions,
    modifier: Modifier = Modifier
) {
    var showBackupImage by remember { mutableStateOf(false) }

    var newProfileImage by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberImageLauncher { uri ->
        newProfileImage = uri
        showBackupImage = false
    }

    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    fun isErrorFullName(): Boolean {
        return state.editFullName.isBlank()
    }

    fun isErrorUsername(): Boolean {
        return state.editUsername.isBlank() || state.editUsername.contains(Regex("\\s"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        state.profile?.let { profile ->
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        IconButton(
                            onClick = actions::onLogout,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.Logout,
                                stringResource(R.string.profile_sign_out)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Spacer(Modifier.weight(1f))
                        if (state.isEditing) {
                            IconButton(
                                onClick = {
                                    if (!isErrorFullName() && !isErrorUsername()) {
                                        newProfileImage?.let { actions.onAvatarChanged(it) }
                                        actions.onSaveProfile()
                                    }
                                },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(Icons.Outlined.Save, stringResource(R.string.profile_save))
                            }
                        } else {
                            IconButton(
                                onClick = actions::onEditProfile,
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(Icons.Outlined.Edit, stringResource(R.string.profile_edit))
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(enabled = state.isEditing) {
                                    imageLauncher.selectImage()
                                }
                                .then(
                                    if (state.isEditing) Modifier.border(
                                        width = 4.dp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                        ) {
                            AsyncImage(
                                alpha = if (state.isEditing) 0.5f else DefaultAlpha,
                                placeholder = placeholder,
                                error = placeholder,
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(newProfileImage ?: profile.avatarUrl)
                                    .data(
                                        if (!showBackupImage) newProfileImage ?: profile.avatarUrl
                                        else "https://ui-avatars.com/api/?name=${profile.username ?: profile.email}&background=random&size=256"
                                    )
                                    .listener(
                                        onError = { _, _ -> showBackupImage = true }
                                    )
                                    .build(),
                                contentDescription = stringResource(R.string.profile_avatar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.isEditing) {
                                OutlinedTextField(
                                    value = state.editFullName,
                                    onValueChange = actions::onFullNameChanged,
                                    label = { Text(stringResource(R.string.profile_full_name)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Next
                                    ),
                                    isError = isErrorFullName()
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = state.editUsername,
                                    onValueChange = actions::onUsernameChanged,
                                    prefix = {
                                        Text(
                                            text = "@",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.profile_username)) },
                                    singleLine = true,
                                    isError = isErrorUsername()
                                )
                            } else {
                                Text(
                                    text = profile.fullName.toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${profile.username}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatCard(
                                stringResource(R.string.profile_joined_on),
                                state.joinedOn,
                                Modifier.weight(1f)
                            )
                            ProfileStatCard(
                                stringResource(R.string.profile_pins),
                                state.pins,
                                Modifier.weight(1f)
                            )
                            ProfileStatCard(
                                stringResource(R.string.profile_likes),
                                state.likes,
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            AutoResizedText(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = title,
                color = contentColorFor(MaterialTheme.colorScheme.surfaceContainer).copy(alpha = 0.67f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        LoadingIndicator()
    }
}
