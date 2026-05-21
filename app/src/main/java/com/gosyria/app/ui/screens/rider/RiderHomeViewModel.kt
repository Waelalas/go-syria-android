package com.gosyria.app.ui.screens.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.model.Location
import com.gosyria.app.data.model.RideOffer
import com.gosyria.app.data.repository.RideRepository
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
    val error: String? = null,
)

@HiltViewModel
class RiderHomeViewModel @Inject constructor(
    private val rideRepo: RideRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RiderHomeState())
    val state = _state.asStateFlow()

    fun onDestinationChange(v: String) = _state.update { it.copy(destination = v, error = null) }

    fun requestRide(onOffersReady: () -> Unit) {
        if (state.value.destination.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSearchingOffers = true, error = null, offers = emptyList()) }
            val pickup = Location(33.5138, 36.2765, "موقعك الحالي")
            val dest   = Location(33.5024, 36.3172, state.value.destination)
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

    fun dismissOffers() = _state.update { it.copy(offers = emptyList(), currentRideId = null) }
}
