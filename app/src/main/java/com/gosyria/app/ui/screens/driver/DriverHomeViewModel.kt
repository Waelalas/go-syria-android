package com.gosyria.app.ui.screens.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.gosyria.app.data.local.TokenStore
import com.gosyria.app.data.remote.ApiService
import com.gosyria.app.data.remote.dto.AcceptRideBody
import com.gosyria.app.data.remote.dto.DriverLocationBody
import com.gosyria.app.data.remote.dto.DriverProfileBody
import com.gosyria.app.data.remote.dto.FcmTokenBody
import com.gosyria.app.data.remote.dto.IncomingRideMsg
import com.gosyria.app.data.repository.AuthRepository
import com.gosyria.app.util.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.HttpException
import javax.inject.Inject

data class DriverHomeState(
    val isOnline: Boolean = false,
    val isLoading: Boolean = false,
    val isReconnecting: Boolean = false,
    val error: String? = null,
    val incomingRide: IncomingRideMsg? = null,
    val vehicleMake: String = "",
    val vehiclePlate: String = "",
    val showProfileDialog: Boolean = false,
    val currentLocation: LatLng? = null,
)

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val okHttpClient: OkHttpClient,
    private val locationService: LocationService,
) : ViewModel() {

    private val _state = MutableStateFlow(DriverHomeState())
    val state = _state.asStateFlow()

    private var ws: WebSocket? = null
    private var locationJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectDelay = 1_000L
    private val gson = Gson()

    init {
        viewModelScope.launch {
            locationService.getCurrentLocation()?.let { loc ->
                _state.update { it.copy(currentLocation = LatLng(loc.latitude, loc.longitude)) }
            }
        }
    }

    fun onVehicleMakeChange(v: String) = _state.update { it.copy(vehicleMake = v) }
    fun onVehiclePlateChange(v: String) = _state.update { it.copy(vehiclePlate = v) }
    fun dismissError() = _state.update { it.copy(error = null) }

    fun toggleOnline() {
        if (state.value.isOnline) goOffline() else goOnline()
    }

    private fun goOnline() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            locationService.getCurrentLocation()?.let { loc ->
                _state.update { it.copy(currentLocation = LatLng(loc.latitude, loc.longitude)) }
                runCatching { api.updateLocation(DriverLocationBody(loc.latitude, loc.longitude)) }
            }
            runCatching { api.driverOnline() }
                .onSuccess {
                    _state.update { s -> s.copy(isOnline = true, isLoading = false) }
                    openWebSocket()
                    startLocationUpdates()
                    sendFcmToken()
                }
                .onFailure { e ->
                    if (e is HttpException && (e.code() == 404 || e.code() == 422)) {
                        _state.update { s -> s.copy(isLoading = false, showProfileDialog = true) }
                    } else {
                        _state.update { s -> s.copy(isLoading = false, error = e.message) }
                    }
                }
        }
    }

    private fun goOffline() {
        _state.update { it.copy(isOnline = false, isReconnecting = false) }
        reconnectJob?.cancel(); reconnectJob = null
        reconnectDelay = 1_000L
        ws?.close(1000, null); ws = null
        stopLocationUpdates()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { api.driverOffline() }
                .onSuccess { _state.update { s -> s.copy(isLoading = false) } }
                .onFailure { _state.update { s -> s.copy(isLoading = false) } }
        }
    }

    private fun startLocationUpdates() {
        locationJob = viewModelScope.launch {
            while (true) {
                locationService.getCurrentLocation()?.let { loc ->
                    _state.update { it.copy(currentLocation = LatLng(loc.latitude, loc.longitude)) }
                    runCatching { api.updateLocation(DriverLocationBody(loc.latitude, loc.longitude)) }
                }
                delay(5_000)
            }
        }
    }

    private fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    private fun sendFcmToken() {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                tokenStore.fcmToken = fcmToken
                runCatching { api.updateFcmToken(FcmTokenBody(fcmToken)) }
            } catch (_: Exception) {}
        }
    }

    fun saveProfile() {
        val make  = state.value.vehicleMake.trim()
        val plate = state.value.vehiclePlate.trim()
        if (make.isBlank() || plate.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { api.setDriverProfile(DriverProfileBody(make, plate)) }
                .onSuccess { _state.update { s -> s.copy(showProfileDialog = false) }; goOnline() }
                .onFailure { e -> _state.update { s -> s.copy(isLoading = false, error = e.message) } }
        }
    }

    fun acceptRide(rideId: String, onAccepted: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { api.acceptRide(AcceptRideBody(rideId)) }
                .onSuccess {
                    reconnectJob?.cancel(); reconnectJob = null
                    ws?.close(1000, null); ws = null
                    stopLocationUpdates()
                    _state.update { s -> s.copy(incomingRide = null, isOnline = false, isReconnecting = false) }
                    onAccepted(rideId)
                }
                .onFailure { e -> _state.update { s -> s.copy(error = e.message) } }
        }
    }

    fun switchRole(onSwitched: () -> Unit) {
        reconnectJob?.cancel(); reconnectJob = null
        ws?.close(1000, null); ws = null
        stopLocationUpdates()
        viewModelScope.launch {
            runCatching { api.driverOffline() }
            _state.update { it.copy(isOnline = false, isReconnecting = false) }
            onSwitched()
        }
    }

    fun dismissRide() = _state.update { it.copy(incomingRide = null) }

    private fun openWebSocket() {
        val token = tokenStore.token ?: return
        val request = Request.Builder()
            .url("wss://gosyria-backend-614870773808.europe-west3.run.app/drivers/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()
        ws = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                reconnectDelay = 1_000L
                _state.update { it.copy(isReconnecting = false) }
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val msg = gson.fromJson(text, IncomingRideMsg::class.java)
                    if (msg.type == "NEW_RIDE_REQUEST") {
                        _state.update { s -> s.copy(incomingRide = msg) }
                    }
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                if (!_state.value.isOnline) return
                _state.update { it.copy(isReconnecting = true) }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            delay(reconnectDelay)
            reconnectDelay = minOf(reconnectDelay * 2, 30_000L)
            if (_state.value.isOnline) openWebSocket()
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        reconnectJob?.cancel()
        ws?.close(1000, null)
    }
}
