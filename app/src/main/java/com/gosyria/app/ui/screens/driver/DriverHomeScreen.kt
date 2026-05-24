package com.gosyria.app.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gosyria.app.data.remote.dto.IncomingRideMsg

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DriverHomeScreen(
    onLogout: () -> Unit,
    onRideAccepted: (String) -> Unit,
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )
    LaunchedEffect(Unit) { locationPermissionState.launchMultiplePermissionRequest() }

    val mapCenter = state.currentLocation ?: LatLng(33.5138, 36.2765)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, 13f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Go Syria – سائق", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { viewModel.switchRole { onLogout() } }) {
                        Icon(Icons.Filled.Logout, contentDescription = "تسجيل الخروج", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true),
            ) {
                state.currentLocation?.let {
                    Marker(state = MarkerState(position = it), title = "موقعك")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.incomingRide?.let { ride ->
                    IncomingRideCard(
                        ride = ride,
                        onAccept = { viewModel.acceptRide(ride.ride_id) { rideId -> onRideAccepted(rideId) } },
                        onReject = { viewModel.dismissRide() },
                    )
                }

                state.error?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                if (state.isOnline) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (state.isOnline) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                if (state.isOnline) "أنت متصل – في انتظار الطلبات" else "أنت غير متصل",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.toggleOnline() },
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isOnline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            ),
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onSecondary)
                            } else {
                                Text(if (state.isOnline) "قطع الاتصال" else "الاتصال والبدء", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showProfileDialog) {
        VehicleProfileDialog(
            make = state.vehicleMake,
            plate = state.vehiclePlate,
            isLoading = state.isLoading,
            onMakeChange = viewModel::onVehicleMakeChange,
            onPlateChange = viewModel::onVehiclePlateChange,
            onSave = { viewModel.saveProfile() },
        )
    }
}

@Composable
private fun IncomingRideCard(
    ride: IncomingRideMsg,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("طلب رحلة جديد!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(8.dp))
            Text("الراكب: ${ride.rider}", style = MaterialTheme.typography.bodyMedium)
            Text("من: ${ride.pickup}", style = MaterialTheme.typography.bodyMedium)
            Text("إلى: ${ride.dest}", style = MaterialTheme.typography.bodyMedium)
            Text("السعر: ${ride.fare.toInt()} ل.س", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text("قبول", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text("رفض")
                }
            }
        }
    }
}

@Composable
private fun VehicleProfileDialog(
    make: String,
    plate: String,
    isLoading: Boolean,
    onMakeChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("بيانات السيارة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("يجب إدخال بيانات سيارتك قبل الاتصال", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = make,
                    onValueChange = onMakeChange,
                    label = { Text("نوع السيارة") },
                    placeholder = { Text("مثال: كيا سيراتو") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = onPlateChange,
                    label = { Text("رقم اللوحة") },
                    placeholder = { Text("مثال: 12345 ب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isLoading && make.isNotBlank() && plate.isNotBlank(),
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("حفظ والاتصال", fontWeight = FontWeight.Bold)
            }
        },
    )
}
