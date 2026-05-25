package com.gosyria.app.data.remote

import com.gosyria.app.data.remote.dto.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/google")
    suspend fun googleSignIn(@Body body: GoogleSignInRequest): TokenResponse

    @POST("rides/request")
    suspend fun requestRide(@Body body: RequestRideBody): RideOut

    @GET("rides/{id}")
    suspend fun getRide(@Path("id") id: String): RideOut

    @POST("rides/accept")
    suspend fun acceptRide(@Body body: AcceptRideBody): RideOut

    @POST("drivers/online")
    suspend fun driverOnline(): DriverStatusResponse

    @POST("drivers/offline")
    suspend fun driverOffline(): DriverStatusResponse

    @POST("drivers/location")
    suspend fun updateLocation(@Body body: DriverLocationBody): DriverLocationBody

    @POST("drivers/profile")
    suspend fun setDriverProfile(@Body body: DriverProfileBody): GenericResponse

    @POST("rides/{id}/status")
    suspend fun updateRideStatus(
        @Path("id") id: String,
        @Query("status") status: String,
    ): GenericResponse

    @POST("drivers/fcm-token")
    suspend fun updateFcmToken(@Body body: FcmTokenBody): GenericResponse
}
