package com.example.postmark.ui.map

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postmark.ui.components.PostmarkOverflowMenu
import com.example.postmark.ui.list.EntriesViewModel
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.Parchment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    onOpenEntry: (String) -> Unit,
    onNewEntry: () -> Unit,
    onSwitchToList: () -> Unit,
    vm: EntriesViewModel = viewModel()
) {
    val entries by vm.entries.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }

    // Default camera somewhere central; in production, pan to fit user's pins.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    Box(modifier = Modifier.fillMaxSize().background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("YOUR MAP", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                    Spacer(Modifier.height(4.dp))
                    val n = entries.count { it.geo != null }
                    Text(
                        "$n ${if (n == 1) "place" else "places"}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = InkBlack
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = InkBlack)
                    }
                    PostmarkOverflowMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        isOnListView = false,
                        onSwitchView = onSwitchToList,
                        onFilter = { /* TODO */ },
                        onDeleteAll = { vm.deleteAll() }
                    )
                }
            }

            // Map area with paper-style border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    entries.forEach { entry ->
                        val geo = entry.geo ?: return@forEach
                        Marker(
                            state = MarkerState(LatLng(geo.latitude, geo.longitude)),
                            title = entry.location,
                            snippet = entry.date,
                            onClick = {
                                onOpenEntry(entry.id)
                                true
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewEntry,
            containerColor = InkBlack,
            contentColor = Parchment,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(56.dp)
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = "New entry")
        }
    }
}
