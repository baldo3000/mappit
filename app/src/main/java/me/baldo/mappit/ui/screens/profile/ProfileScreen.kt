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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import me.baldo.mappit.R
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.ui.composables.AutoResizedText
import me.baldo.mappit.utils.getPrettyFormatDay
import me.baldo.mappit.utils.rememberImageLauncher

@Composable
fun ProfileScreen(
    state: ProfileState,
    actions: ProfileActions,
    onPinClick: (Pin) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Loading(modifier)
    } else {
        Profile(state, actions, onPinClick, modifier)
    }
}

@Composable
private fun Profile(
    state: ProfileState,
    actions: ProfileActions,
    onPinClick: (Pin) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = actions::refreshProfile,
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing
            )
        },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            state.profile?.let { profile ->
                item {
                    ProfileCard(profile, state, actions)
                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
                item {
                    Text(
                        text = stringResource(R.string.profile_your_pins),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                }
                items(state.pins) { pin ->
                    PinRow(
                        pin = pin,
                        profile = profile,
                        onPinClick = onPinClick
                    )
                }
            } ?: run {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.profile_error),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    state: ProfileState,
    actions: ProfileActions,
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
                        .clickable(
                            enabled = state.isEditing,
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.profile_avatar_edit)
                        ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            value = state.editFullName,
                            onValueChange = actions::onFullNameChanged,
                            label = { Text(stringResource(R.string.profile_full_name)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            isError = isErrorFullName()
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
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
                        state.pinsNumber,
                        Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        stringResource(R.string.profile_likes),
                        state.likesNumber,
                        Modifier.weight(1f)
                    )
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
private fun PinRow(
    pin: Pin,
    profile: Profile,
    onPinClick: (Pin) -> Unit = {}
) {
    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    var showBackupImage by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.pin_info_open)
                ) { onPinClick(pin) }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                error = placeholder,
                placeholder = placeholder,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        if (!showBackupImage) profile.avatarUrl
                        else "https://ui-avatars.com/api/?name=${profile.username ?: profile.email}&background=random&size=256"
                    )
                    .listener(
                        onError = { _, _ -> showBackupImage = true }
                    )
                    .build(),
                contentDescription = stringResource(R.string.profile_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = pin.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "@${profile.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = pin.createdAt.getPrettyFormatDay(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        HorizontalDivider()
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
