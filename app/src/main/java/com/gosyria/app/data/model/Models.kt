package com.gosyria.app.data.model

enum class UserRole { RIDER, DRIVER }

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val role: UserRole,
)

data class Location(
    val lat: Double,
    val lng: Double,
    val address: String = "",
)

enum class RideStatus {
    SEARCHING,
    DRIVER_FOUND,
    DRIVER_EN_ROUTE,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
}

data class RideRequest(
    val id: String,
    val riderId: String,
    val pickup: Location,
    val destination: Location,
    val estimatedFare: Double,
    val estimatedMinutes: Int,
    val status: RideStatus = RideStatus.SEARCHING,
)

data class Driver(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double,
    val vehicleMake: String,
    val vehiclePlate: String,
    val location: Location,
)

data class RideOffer(
    val driver: Driver,
    val fare: Double,
    val etaMinutes: Int,
)
