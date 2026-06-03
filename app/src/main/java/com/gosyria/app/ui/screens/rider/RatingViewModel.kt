package com.gosyria.app.ui.screens.rider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val rideRepo: RideRepository,
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    fun submitRating(rideId: String, rating: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            rideRepo.rateRide(rideId, rating)
            _isSubmitting.value = false
            onDone()
        }
    }
}
