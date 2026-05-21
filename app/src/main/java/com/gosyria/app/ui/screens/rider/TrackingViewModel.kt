package com.gosyria.app.ui.screens.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.model.RideRequest
import com.gosyria.app.data.model.RideStatus
import com.gosyria.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingState(
    val ride: RideRequest? = null,
    val isCompleted: Boolean = false,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val rideRepo: RideRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TrackingState())
    val state = _state.asStateFlow()

    fun startTracking(rideId: String) {
        viewModelScope.launch {
            rideRepo.observeRide(rideId).collect { ride ->
                _state.update { it.copy(ride = ride, isCompleted = ride.status == RideStatus.COMPLETED) }
            }
        }
    }
}
