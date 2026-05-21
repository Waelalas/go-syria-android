package com.gosyria.app.ui.screens.rider

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gosyria.app.data.model.RideStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    rideId: String,
    onRideCompleted: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(rideId) { viewModel.startTracking(rideId) }
    LaunchedEffect(state.isCompleted) { if (state.isCompleted) onRideCompleted() }

    val pickup = state.ride?.pickup
    val mapCenter = if (pickup != null) LatLng(pickup.lat, pickup.lng) else LatLng(33.5138, 36.2765)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, 14f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تتبع الرحلة", fontWeight = FontWeight.Bold) },
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
                pickup?.let {
                    Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = "نقطة الاستقلال")
                }
                state.ride?.destination?.let {
                    Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = "الوجهة")
                }
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
                    val statusText = when (state.ride?.status) {
                        RideStatus.SEARCHING       -> "جاري البحث عن سائق..."
                        RideStatus.DRIVER_FOUND    -> "تم العثور على سائق!"
                        RideStatus.DRIVER_EN_ROUTE -> "السائق في الطريق إليك"
                        RideStatus.IN_PROGRESS     -> "الرحلة جارية"
                        RideStatus.COMPLETED       -> "اكتملت الرحلة"
                        RideStatus.CANCELLED       -> "تم إلغاء الرحلة"
                        null                       -> "جاري التحميل..."
                    }

                    if (state.ride?.status == RideStatus.COMPLETED) {
                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(statusText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)

                    state.ride?.let { ride ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "الوجهة: ${ride.destination.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "السعر: ${ride.estimatedFare.toInt()} ل.س",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
