package com.gosyria.app.ui.screens.rider

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gosyria.app.data.model.RideOffer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RiderHomeScreen(
    onRideStarted: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: RiderHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showOffers by remember { mutableStateOf(false) }

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }

    val damascus = LatLng(33.5138, 36.2765)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(damascus, 13f)
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Go Syria", fontWeight = FontWeight.Bold) },
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
                Marker(
                    state = MarkerState(position = state.currentLocation ?: damascus),
                    title = "موقعك الحالي"
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("إلى أين تريد الذهاب؟", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.destination,
                        onValueChange = viewModel::onDestinationChange,
                        placeholder = { Text("مثال: ساحة الأمويين أو باب توما") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.error?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.requestRide { showOffers = true } },
                        enabled = !state.isSearchingOffers && state.destination.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    ) {
                        AnimatedContent(targetState = state.isSearchingOffers, label = "") { searching ->
                            if (searching) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(12.dp))
                                    Text("جاري البحث...")
                                }
                            } else {
                                Text("طلب رحلة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOffers && state.offers.isNotEmpty()) {
        OffersBottomSheet(
            offers = state.offers,
            onAccept = { driverId ->
                showOffers = false
                viewModel.acceptOffer(driverId) { rideId -> onRideStarted(rideId) }
            },
            onDismiss = {
                showOffers = false
                viewModel.dismissOffers()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OffersBottomSheet(
    offers: List<RideOffer>,
    onAccept: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("اختر سائقاً", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(offers) { offer ->
                    OfferCard(offer = offer, onAccept = { onAccept(offer.driver.id) })
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfferCard(offer: RideOffer, onAccept: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.driver.name, fontWeight = FontWeight.Bold)
                Text("${offer.driver.vehicleMake} · ${offer.driver.vehiclePlate}", style = MaterialTheme.typography.bodySmall)
                Text("⭐ ${offer.driver.rating}  ·  ${offer.etaMinutes} دقيقة", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${offer.fare.toInt()} pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Button(onClick = onAccept, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    Text("اختيار", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
