package com.gosyria.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosyria.app.data.model.RideHistoryItem
import com.gosyria.app.data.repository.RideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RideHistoryState(
    val rides: List<RideHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RideHistoryViewModel @Inject constructor(
    private val rideRepo: RideRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RideHistoryState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            rideRepo.getMyRides()
                .onSuccess { rides -> _state.update { it.copy(rides = rides, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
