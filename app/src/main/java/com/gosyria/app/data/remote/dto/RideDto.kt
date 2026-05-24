package com.gosyria.app.data.remote.dto

data class RequestRideBody(
    val pickup_lat: Double,
    val pickup_lng: Double,
    val pickup_address: String,
    val dest_lat: Double,
    val dest_lng: Double,
    val dest_address: String,
)

data class RideOut(
    val id: String,
    val rider_id: String,
    val driver_id: String?,
    val pickup_address: String,
    val dest_address: String,
    val status: String,
    val fare: Double,
)
