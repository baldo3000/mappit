package me.baldo.mappit.ui.screens.addpin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.baldo.mappit.R
import me.baldo.mappit.utils.LocationService

@Composable
fun AddPinScreen(
    addPinState: AddPinState,
    addPinActions: AddPinActions,
    onPinAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService(ctx) }

    if (addPinState.addState == AddState.Success) {
        onPinAdd()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.add_pin_header),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = addPinState.title,
            onValueChange = addPinActions::onUpdateTitle,
            singleLine = true,
            label = { Text(stringResource(R.string.add_pin_title)) }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = addPinState.description,
            onValueChange = addPinActions::onUpdateDescription,
            minLines = 5,
            label = { Text(stringResource(R.string.add_pin_description)) }
        )
        Spacer(Modifier.height(8.dp))
        if (addPinState.isError) {
            Text(
                text = stringResource(R.string.add_pin_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
        }
        Button(
            enabled = addPinState.addState != AddState.Sending,
            onClick = {
                scope.launch {
                    locationService.getCurrentLocation(true)?.let { position ->
                        addPinActions.addPin(position)
                    }
                }
            }
        ) {
            Text(stringResource(R.string.add_pin_add))
        }
    }
}