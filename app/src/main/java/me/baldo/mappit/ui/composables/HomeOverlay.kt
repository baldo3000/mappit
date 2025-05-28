package me.baldo.mappit.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import me.baldo.mappit.R
import me.baldo.mappit.ui.BottomBarTab
import me.baldo.mappit.ui.MappItRoute

@Composable
fun HomeOverlay(
    selectedTab: BottomBarTab,
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            MainTopBar(
                title = selectedTab.toString(),
                extraActions = listOf(
                    ExtraAction(
                        icon = Icons.Outlined.Settings,
                        description = stringResource(R.string.screen_settings),
                        onClick = {
                            navController.navigate(MappItRoute.Settings)
                        }
                    )
                )
            )
        },
        bottomBar = { BottomBar(selectedTab, navController) }
    ) { innerPadding ->
        content(innerPadding)
    }
}