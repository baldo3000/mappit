package me.baldo.mappit.ui.screens.bookmarks

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import me.baldo.mappit.R
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.model.Profile
import me.baldo.mappit.utils.getPrettyFormatDay

@Composable
fun BookmarksScreen(
    state: BookmarksState,
    actions: BookmarksActions,
    onPinClick: (Pin) -> Unit,
    onProfileClick: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Loading(modifier)
    } else {
        Bookmarks(state, actions, onPinClick, onProfileClick, modifier)
    }
}

@Composable
private fun Bookmarks(
    state: BookmarksState,
    actions: BookmarksActions,
    onPinClick: (Pin) -> Unit,
    onProfileClick: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = actions::refreshBookmarks,
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Spacer(Modifier.height(16.dp))
            }

            items(state.bookmarks.entries.toList()) { (pin, profile) ->
                BookmarkRow(
                    pin = pin,
                    profile = profile,
                    onPinClick = onPinClick,
                    onProfileClick = onProfileClick
                )
            }
        }
    }
}

@Composable
private fun BookmarkRow(
    pin: Pin,
    profile: Profile,
    onPinClick: (Pin) -> Unit = {},
    onProfileClick: (Profile) -> Unit = {}
) {
    val placeholder: Painter = rememberVectorPainter(
        image = Icons.Filled.AccountCircle,
    )

    var showBackupImage by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable() { onPinClick(pin) }
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
                    .clickable() { onProfileClick(profile) }
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
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
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
