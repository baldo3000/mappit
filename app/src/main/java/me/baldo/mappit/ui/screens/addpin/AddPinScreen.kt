package me.baldo.mappit.ui.screens.addpin

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import me.baldo.mappit.R
import me.baldo.mappit.utils.LocationService
import me.baldo.mappit.utils.rememberImageLauncher

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

    val imageLauncher = rememberImageLauncher { uri ->
        addPinActions.onImageChanged(uri)
    }


    if (addPinState.addState is AddState.Success) {
        onPinAdd()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            label = { Text(stringResource(R.string.add_pin_title)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true,
                imeAction = ImeAction.Next
            ),
            isError = addPinState.title.isBlank()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = addPinState.description,
            onValueChange = addPinActions::onUpdateDescription,
            minLines = 5,
            maxLines = 5,
            label = { Text(stringResource(R.string.add_pin_description)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrectEnabled = true
            )
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.padding(horizontal = 64.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(addPinState.image)
                    .build(),
                contentDescription = stringResource(R.string.add_pin_image),
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
            )
        }
        if (addPinState.image != Uri.EMPTY) {
            Spacer(Modifier.height(8.dp))
        }
        if (addPinState.addState is AddState.Error) {
            Text(
                text = stringResource(R.string.add_pin_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.padding(horizontal = 64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {}
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    enabled = !addPinState.isSaving && addPinState.title.isNotEmpty(),
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        scope.launch {
                            addPinActions.setIsSaving()
                            locationService.getCurrentLocation(true)?.let { position ->
                                addPinActions.addPin(position)
                            }
                        }
                    }
                ) {
                    Text(stringResource(if (addPinState.isSaving) R.string.add_pin_adding else R.string.add_pin_add))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { imageLauncher.selectImage() },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Attachment,
                        contentDescription = stringResource(R.string.add_pin_add_image)
                    )
                }
            }
        }
        Spacer(Modifier.height(64.dp))
    }
}