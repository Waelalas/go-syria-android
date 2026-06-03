package com.gosyria.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gosyria.app.data.local.TokenStore
import com.gosyria.app.data.model.*
import com.gosyria.app.data.remote.ApiService
import com.gosyria.app.data.remote.dto.RateRideBody
import com.gosyria.app.data.remote.dto.RequestRideBody
import com.gosyria.app.data.remote.dto.RideOut
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class HttpRideRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    private val okHttpClient: OkHttpClient,
) : RideRepository {

    private val gson = Gson()

    override suspend fun requestRide(pickup: Location, destination: Location): Result<RideRequest> =
        runCatching {
            api.requestRide(
                RequestRideBody(
                    pickup_lat = pickup.lat, pickup_lng = pickup.lng, pickup_address = pickup.address,
                    dest_lat = destination.lat, dest_lng = destination.lng, dest_address = destination.address,
                )
            ).toModel()
        }

    override fun observeRide(rideId: String): Flow<RideRequest> = callbackFlow {
        val token = tokenStore.token ?: run { close(); return@callbackFlow }
        val currentRide = AtomicReference<RideRequest?>(null)
        val scope = this

        // Initial fetch to get current ride state
        runCatching { api.getRide(rideId).toModel() }.onSuccess {
            currentRide.set(it)
            trySend(it)
        }

        val connectJob = scope.launch {
            var reconnectDelay = 1_000L
            while (true) {
                val failure = CompletableDeferred<Unit>()
                val request = Request.Builder()
                    .url("wss://gosyria-backend-614870773808.europe-west3.run.app/riders/ws")
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val ws = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                        reconnectDelay = 1_000L
                    }
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        runCatching {
                            val obj = gson.fromJson(text, JsonObject::class.java)
                            when (obj.get("type")?.asString) {
                                "RIDE_STATUS_UPDATE" -> {
                                    val statusStr = obj.get("status")?.asString ?: return@runCatching
                                    val newStatus = mapStatus(statusStr) ?: return@runCatching
                                    val updated = currentRide.get()?.copy(status = newStatus) ?: return@runCatching
                                    currentRide.set(updated)
                                    trySend(updated)
                                }
                                "DRIVER_FOUND" -> {
                                    val driverLat = obj.get("driver_lat")?.asDouble
                                    val driverLng = obj.get("driver_lng")?.asDouble
                                    scope.launch {
                                        runCatching { api.getRide(rideId).toModel() }.onSuccess { ride ->
                                            val updated = if (driverLat != null && driverLng != null)
                                                ride.copy(driverLocation = Location(driverLat, driverLng))
                                            else ride
                                            currentRide.set(updated)
                                            trySend(updated)
                                        }
                                    }
                                }
                                "DRIVER_LOCATION_UPDATE" -> {
                                    val lat = obj.get("lat")?.asDouble ?: return@runCatching
                                    val lng = obj.get("lng")?.asDouble ?: return@runCatching
                                    val updated = currentRide.get()?.copy(
                                        driverLocation = Location(lat, lng)
                                    ) ?: return@runCatching
                                    currentRide.set(updated)
                                    trySend(updated)
                                }
                            }
                        }
                    }
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                        failure.complete(Unit)
                    }
                })
                try {
                    failure.await()
                } finally {
                    ws.close(1000, null)
                }
                delay(reconnectDelay)
                reconnectDelay = minOf(reconnectDelay * 2, 30_000L)
            }
        }

        awaitClose { connectJob.cancel() }
    }

    override suspend fun getOffers(rideId: String): Result<List<RideOffer>> = runCatching {
        repeat(90) { // 90 × 2ثانية = 3 دقائق
            val ride = api.getRide(rideId)
            if (ride.driver_id != null) {
                val driverInfo = runCatching { api.getDriverPublic(ride.driver_id) }.getOrNull()
                return@runCatching listOf(
                    RideOffer(
                        driver = Driver(
                            id = ride.driver_id,
                            name = driverInfo?.name ?: "سائق",
                            phone = "",
                            rating = driverInfo?.rating ?: 5.0,
                            vehicleMake = driverInfo?.vehicle_make ?: "",
                            vehiclePlate = driverInfo?.vehicle_plate ?: "",
                            location = Location(0.0, 0.0),
                        ),
                        fare = ride.fare,
                        etaMinutes = 5,
                    )
                )
            }
            delay(2_000)
        }
        emptyList()
    }

    override suspend fun acceptOffer(rideId: String, driverId: String): Result<RideRequest> =
        runCatching { api.getRide(rideId).toModel() }

    override suspend fun cancelRide(rideId: String): Result<Unit> =
        runCatching { api.updateRideStatus(rideId, "CANCELLED"); Unit }

    override suspend fun rateRide(rideId: String, rating: Int): Result<Unit> =
        runCatching { api.rateRide(rideId, RateRideBody(rating)); Unit }

    private fun mapStatus(s: String): RideStatus? = when (s) {
        "SEARCHING"       -> RideStatus.SEARCHING
        "DRIVER_FOUND"    -> RideStatus.DRIVER_FOUND
        "DRIVER_EN_ROUTE" -> RideStatus.DRIVER_EN_ROUTE
        "IN_PROGRESS"     -> RideStatus.IN_PROGRESS
        "COMPLETED"       -> RideStatus.COMPLETED
        "CANCELLED"       -> RideStatus.CANCELLED
        else              -> null
    }

    private fun RideOut.toModel() = RideRequest(
        id = id,
        riderId = rider_id,
        pickup = Location(pickup_lat, pickup_lng, pickup_address),
        destination = Location(dest_lat, dest_lng, dest_address),
        estimatedFare = fare,
        estimatedMinutes = 10,
        status = mapStatus(status) ?: RideStatus.SEARCHING,
    )
}
