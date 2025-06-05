package me.baldo.mappit.ui.composables

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.baldo.mappit.ui.BottomBarTab
import me.baldo.mappit.ui.MappItRoute

@Composable
fun BottomBar(
    selectedTab: BottomBarTab,
    navController: NavHostController
) {
    FlexibleBottomAppBar {
        BottomBarTab.tabs.forEach { tab ->
            val selected = selectedTab == tab
            val title = stringResource(id = tab.titleResID)
            NavigationBarItem(
                modifier = Modifier.offset(y = (2).dp),
                icon = {
                    Icon(
                        imageVector = if (selected) tab.iconSelected else tab.icon,
                        contentDescription = title
                    )
                },
                label = { Text(text = title) },
                selected = selected,
                onClick = {
                    if (tab != selectedTab) {
                        navController.navigate(tab.screen) {
                            popUpTo(MappItRoute.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}