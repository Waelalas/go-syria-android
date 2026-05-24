package com.gosyria.app.data.remote.dto

data class DriverStatusResponse(val online: Boolean)
data class DriverLocationBody(val lat: Double, val lng: Double)
data class DriverProfileBody(val vehicle_make: String, val vehicle_plate: String)
data class AcceptRideBody(val ride_id: String)
data class GenericResponse(val message: String = "")
data class FcmTokenBody(val token: String)

data class IncomingRideMsg(
    val type: String = "",
    val ride_id: String = "",
    val pickup: String = "",
    val dest: String = "",
    val fare: Double = 0.0,
    val rider: String = "",
)
