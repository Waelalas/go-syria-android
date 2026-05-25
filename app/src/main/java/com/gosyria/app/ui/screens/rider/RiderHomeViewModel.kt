package com.gosyria.app.ui.screens.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.gosyria.app.data.model.Location
import com.gosyria.app.data.model.RideOffer
import com.gosyria.app.data.repository.AuthRepository
import com.gosyria.app.data.repository.RideRepository
import com.gosyria.app.util.GeocodingService
import com.gosyria.app.util.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiderHomeState(
    val destination: String = "",
    val isSearchingOffers: Boolean = false,
    val offers: List<RideOffer> = emptyList(),
    val currentRideId: String? = null,
    val currentLocation: LatLng? = null,
    val error: String? = null,
)

private val DAMASCUS_CENTER = LatLng(33.5138, 36.2765)

@HiltViewModel
class RiderHomeViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val rideRepo: RideRepository,
    private val locationService: LocationService,
    private val geocodingService: GeocodingService,
) : ViewModel() {

    private val _state = MutableStateFlow(RiderHomeState())
    val state = _state.asStateFlow()

    init {
        updateCurrentLocation()
    }

    fun updateCurrentLocation() {
        viewModelScope.launch {
            locationService.getCurrentLocation()?.let { loc ->
                _state.update { it.copy(currentLocation = LatLng(loc.latitude, loc.longitude)) }
            }
        }
    }

    fun onDestinationChange(v: String) = _state.update { it.copy(destination = v, error = null) }

    fun requestRide(onOffersReady: () -> Unit) {
        if (state.value.destination.isBlank()) return
        viewModelScope.launch {
            val currentLoc = state.value.currentLocation ?: DAMASCUS_CENTER
            _state.update { it.copy(isSearchingOffers = true, error = null, offers = emptyList()) }

            val pickup = Location(currentLoc.latitude, currentLoc.longitude, "موقعك الحالي")
            val destLatLng = geocodingService.geocode(state.value.destination)
            if (destLatLng == null) {
                _state.update { it.copy(isSearchingOffers = false, error = "لم يُعثر على العنوان، جرّب كتابته بشكل أوضح") }
                return@launch
            }
            val dest = Location(destLatLng.latitude, destLatLng.longitude, state.value.destination)

            rideRepo.requestRide(pickup, dest)
                .onSuccess { ride ->
                    rideRepo.getOffers(ride.id)
                        .onSuccess { offers ->
                            _state.update { it.copy(isSearchingOffers = false, offers = offers, currentRideId = ride.id) }
                            onOffersReady()
                        }
                        .onFailure { _state.update { s -> s.copy(isSearchingOffers = false, error = it.message) } }
                }
                .onFailure { _state.update { s -> s.copy(isSearchingOffers = false, error = it.message) } }
        }
    }

    fun acceptOffer(driverId: String, onAccepted: (String) -> Unit) {
        val rideId = state.value.currentRideId ?: return
        viewModelScope.launch {
            rideRepo.acceptOffer(rideId, driverId)
                .onSuccess { onAccepted(rideId) }
                .onFailure { _state.update { s -> s.copy(error = it.message) } }
        }
    }

    fun switchRole(onSwitched: () -> Unit) = onSwitched()

    fun dismissOffers() = _state.update { it.copy(offers = emptyList(), currentRideId = null) }
}
