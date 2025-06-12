package me.baldo.mappit.ui.screens.pininfo

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.baldo.mappit.R
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.utils.copyToClipboard
import me.baldo.mappit.utils.getPrettyFormat
import me.baldo.mappit.utils.shareText
import java.util.Locale

@Composable
fun PinInfoScreen(
    state: PinInfoState,
    actions: PinInfoActions,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Loading(modifier)
    } else {
        PinInfo(state, actions, goBack, modifier)
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

@Composable
private fun PinInfo(
    state: PinInfoState,
    actions: PinInfoActions,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    state.pin?.let { pin ->
        state.profile?.let { profile ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                PinCard(
                    pin = pin,
                    profile = profile,
                    imageUrl = state.imageUrl,
                    likes = state.likes,
                    isLiked = state.isLiked,
                    isBookmarked = state.isBookmarked,
                    actions = actions,
                    goBack = goBack
                )
            }
        } ?: run {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.pin_info_error),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    } ?: run {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.pin_info_error),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PinCard(
    pin: Pin,
    profile: Profile,
    imageUrl: String = "",
    likes: Int,
    isLiked: Boolean,
    isBookmarked: Boolean,
    goBack: () -> Unit,
    actions: PinInfoActions
) {
    val ctx = LocalContext.current

    val scope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileSection(profile)
                    BodySection(pin.title, pin.description)
                    ImageSection(imageUrl)
                    DetailsSection(pin.createdAt, pin.latitude, pin.longitude)
                    ActionsSection(
                        likes = likes,
                        isLiked = isLiked,
                        toggleLike = actions::toggleLike,
                        isBookmarked = isBookmarked,
                        toggleBookmark = actions::toggleBookmark,
                        onShare = {
                            shareText(
                                context = ctx,
                                title = ctx.getString(R.string.pin_info_share_title),
                                text = ctx.getString(
                                    R.string.pin_info_share_message,
                                    pin.title,
                                    profile.username,
                                    pin.description
                                )
                            )
                        },
                        onCopyId = {
                            copyToClipboard(
                                ctx,
                                ctx.getString(R.string.pin_info_id),
                                pin.id.toString()
                            )
                        },
                        onDelete = {
                            actions.deletePin(pin)
                            scope.launch {
                                delay(100)
                                goBack()
                            }
                        },
                        deleteAvailable = actions.deleteAvailable()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    profile: Profile
) {
    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    var showBackupImage by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            placeholder = placeholder,
            error = placeholder,
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
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = profile.fullName ?: stringResource(R.string.pin_info_author_anon),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun BodySection(
    title: String,
    description: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ImageSection(
    imageUrl: String
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .build(),
        contentDescription = stringResource(R.string.pin_info_image),
        contentScale = ContentScale.Inside,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
    )
}

@Composable
private fun DetailsSection(
    createdAt: Instant,
    latitude: Double,
    longitude: Double
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = createdAt.getPrettyFormat(),
            style = MaterialTheme.typography.labelLargeEmphasized
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = String.format(Locale.UK, "%.4f, %.4f", latitude, longitude),
            style = MaterialTheme.typography.labelLargeEmphasized
        )
    }
}

@Composable
private fun ActionsSection(
    likes: Int = 0,
    isLiked: Boolean = false,
    toggleLike: (Boolean) -> Unit = {},
    isBookmarked: Boolean = false,
    toggleBookmark: (Boolean) -> Unit = {},
    onShare: () -> Unit = {},
    onCopyId: () -> Unit = {},
    onDelete: () -> Unit = {},
    deleteAvailable: Boolean
) {
    val ctx = LocalContext.current

    Column {
        HorizontalDivider(
            color = contentColorFor(MaterialTheme.colorScheme.surfaceContainer).copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { toggleLike(!isLiked) },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.pin_info_like),
                    tint = if (isLiked) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            }
            Text(
                text = likes.toString(),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { toggleBookmark(!isBookmarked) },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(R.string.pin_info_bookmark),
                    tint = if (isBookmarked) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                )
            }
            IconButton(
                onClick = {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.coming_soon),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = stringResource(R.string.pin_info_chat)
                )
            }
            Spacer(Modifier.weight(13f))
            OptionsDropDownMenu(onShare, onCopyId, onDelete, deleteAvailable)
        }
    }
}

@Composable
private fun OptionsDropDownMenu(
    onShare: () -> Unit = {},
    onCopyId: () -> Unit = {},
    onDelete: () -> Unit = {},
    deleteAvailable: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.pin_info_more)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.pin_info_share)) },
                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                onClick = {
                    expanded = false
                    onShare()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.pin_info_copy_id)) },
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                onClick = {
                    expanded = false
                    onCopyId()
                }
            )
            if (deleteAvailable) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.pin_info_delete)) },
                    leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        expanded = false
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                showDeleteDialog = false
                onDelete()
            }
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.pin_info_delete_message_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.pin_info_delete_message_confirm))
            }
        },
        title = { Text(stringResource(R.string.pin_info_delete_message_title)) },
        text = { Text(stringResource(R.string.pin_info_delete_message_body)) },
        icon = { Icon(Icons.Outlined.DeleteOutline, null) }
    )
}
