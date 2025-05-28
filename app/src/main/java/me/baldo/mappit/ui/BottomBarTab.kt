package me.baldo.mappit.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import me.baldo.mappit.R

sealed interface BottomBarTab {
    val icon: ImageVector
    val iconSelected: ImageVector
    val titleResID: Int
    val screen: MappItRoute

    data object Home : BottomBarTab {
        override val icon = Icons.Outlined.Home
        override val iconSelected = Icons.Filled.Home
        override val titleResID = R.string.screen_home
        override val screen = MappItRoute.Home
    }

    data object Discovery : BottomBarTab {
        override val icon = Icons.Outlined.Search
        override val iconSelected = Icons.Filled.Search
        override val titleResID = R.string.screen_discovery
        override val screen = MappItRoute.Discovery
    }

    data object Profile : BottomBarTab {
        override val icon = Icons.Outlined.AccountCircle
        override val iconSelected = Icons.Filled.AccountCircle
        override val titleResID = R.string.screen_profile
        override val screen = MappItRoute.Profile
    }

    companion object {
        val tabs = listOf(
            Home,
            Discovery,
            Profile
        )
    }
}