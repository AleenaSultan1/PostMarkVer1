package com.example.postmark.ui.entry

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postmark.ui.components.formatIsoDate
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.Parchment

@Composable
fun NewEntryScreen(
    onDone: () -> Unit,
    vm: NewEntryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) vm.fetchCurrentLocation(context) }

    LaunchedEffect(Unit) {
        // Auto-detect location on screen open. Saves the user typing.
        locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Box(modifier = Modifier.fillMaxSize().background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar — back, format icons, Done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 48.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDone) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Cancel", tint = InkBlack)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { /* TODO: text format */ }) {
                        Icon(Icons.Outlined.TextFields, contentDescription = "Text format", tint = InkBlack)
                    }
                    IconButton(onClick = { /* TODO: photo album */ }) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Photo album", tint = InkBlack)
                    }
                    IconButton(onClick = { /* TODO: camera */ }) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = InkBlack)
                    }
                }
                TextButton(onClick = vm::save, enabled = !state.saving && state.body.isNotBlank()) {
                    Text(
                        "DONE",
                        style = MaterialTheme.typography.labelLarge,
                        color = InkBlack,
                        letterSpacing = 2.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MutedStone))
            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
                Text("NEW ENTRY", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                Spacer(Modifier.height(4.dp))
                Text(formatIsoDate(state.date), style = MaterialTheme.typography.headlineMedium, color = InkBlack)
                Spacer(Modifier.height(4.dp))

                BasicTextField(
                    value = state.location,
                    onValueChange = vm::onLocationChange,
                    textStyle = TextStyle(
                        fontStyle = FontStyle.Italic,
                        color = MutedStone,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (state.location.isEmpty()) {
                            Text(
                                "Detecting location…",
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = MutedStone,
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                )

                Spacer(Modifier.height(24.dp))

                BasicTextField(
                    value = state.body,
                    onValueChange = vm::onBodyChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = InkBlack),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { inner ->
                        if (state.body.isEmpty()) {
                            Text(
                                "What happened today?",
                                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                color = MutedStone
                            )
                        }
                        inner()
                    }
                )
            }
        }
    }
}
