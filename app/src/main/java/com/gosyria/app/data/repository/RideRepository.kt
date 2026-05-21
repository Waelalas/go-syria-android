package com.gosyria.app.data.repository

import com.gosyria.app.data.model.Location
import com.gosyria.app.data.model.RideOffer
import com.gosyria.app.data.model.RideRequest
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    suspend fun requestRide(pickup: Location, destination: Location): Result<RideRequest>
    fun observeRide(rideId: String): Flow<RideRequest>
    suspend fun cancelRide(rideId: String): Result<Unit>
    suspend fun getOffers(rideId: String): Result<List<RideOffer>>
    suspend fun acceptOffer(rideId: String, driverId: String): Result<RideRequest>
}
