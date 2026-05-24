package com.gosyria.app.ui.screens.driver

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverTripState(
    val pickupAddress: String = "",
    val destAddress: String = "",
    val fare: Double = 0.0,
    val status: String = "DRIVER_FOUND",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DriverTripViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val rideId: String = checkNotNull(savedStateHandle["rideId"])

    private val _state = MutableStateFlow(DriverTripState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { api.getRide(rideId) }
                .onSuccess { ride ->
                    _state.update {
                        it.copy(
                            pickupAddress = ride.pickup_address,
                            destAddress = ride.dest_address,
                            fare = ride.fare,
                            status = ride.status,
                        )
                    }
                }
        }
    }

    fun arrivedAtPickup() = updateStatus("IN_PROGRESS")
    fun completeTrip()    = updateStatus("COMPLETED")

    private fun updateStatus(newStatus: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { api.updateRideStatus(rideId, newStatus) }
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            status = newStatus,
                            isLoading = false,
                            isCompleted = newStatus == "COMPLETED",
                        )
                    }
                }
                .onFailure { e -> _state.update { s -> s.copy(isLoading = false, error = e.message) } }
        }
    }
}
