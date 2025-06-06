package me.baldo.mappit.ui.composables

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

data class ExtraAction(
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun MainTopBar(
    title: String,
    extraActions: List<ExtraAction> = emptyList(),
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            extraActions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.description
                    )
                }
            }
        }
    )
}