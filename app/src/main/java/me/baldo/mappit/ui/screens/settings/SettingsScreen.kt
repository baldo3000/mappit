package me.baldo.mappit.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import me.baldo.mappit.R

@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    settingsActions: SettingsActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Category(stringResource(R.string.settings_category_UI))
        ThemeRadioRow(
            currentTheme = settingsState.theme,
            onThemeChanged = settingsActions::onThemeChanged
        )
    }
}

@Composable
private fun Category(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    )
}

@Composable
private fun SwitchRowWithDescription(
    text: String,
    description: String? = null,
    onLabel: String,
    offLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Switch,
                onClickLabel = if (checked) offLabel else onLabel,
                onClick = { onCheckedChange(!checked) })
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
            )
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun ThemeRadioRow(
    currentTheme: Theme,
    onThemeChanged: (Theme) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Switch,
                onClickLabel = stringResource(R.string.settings_theme_choose),
                onClick = { showDialog = true }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = currentTheme.toResourceString(LocalContext.current),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    if (showDialog) {
        ThemeRadioDialog(
            currentTheme = currentTheme,
            onThemeChanged = {
                onThemeChanged(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ThemeRadioDialog(
    currentTheme: Theme,
    onThemeChanged: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.settings_theme_cancel)) } },
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column(Modifier.selectableGroup()) {
                Theme.entries.forEach { theme ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = currentTheme == theme,
                                onClick = { onThemeChanged(theme) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp)
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = null
                        )
                        Text(
                            text = theme.toResourceString(LocalContext.current),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    )
}
