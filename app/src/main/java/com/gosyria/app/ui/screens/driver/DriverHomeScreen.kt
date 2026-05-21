package com.gosyria.app.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen() {
    var isOnline by remember { mutableStateOf(false) }

    val damascus = LatLng(33.5138, 36.2765)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(damascus, 13f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Go Syria – سائق", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
            ) {
                Marker(state = MarkerState(position = damascus), title = "موقعك")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            if (isOnline) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null,
                            tint = if (isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            if (isOnline) "أنت متصل – في انتظار الطلبات" else "أنت غير متصل",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { isOnline = !isOnline },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        ),
                    ) {
                        Text(if (isOnline) "قطع الاتصال" else "الاتصال والبدء", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
