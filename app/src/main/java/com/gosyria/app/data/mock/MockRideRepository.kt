package com.gosyria.app.data.mock

import com.gosyria.app.data.model.*
import com.gosyria.app.data.repository.RideRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRideRepository @Inject constructor() : RideRepository {

    private val mockDrivers = listOf(
        Driver("d1", "محمد الأحمد", "0991234567", 4.8, "كيا سبورتاج", "أ ب 1234", Location(33.51, 36.30)),
        Driver("d2", "أحمد السعيد", "0981234567", 4.6, "هيونداي توسان", "أ ج 5678", Location(33.52, 36.29)),
        Driver("d3", "خالد المصري", "0971234567", 4.9, "تويوتا كامري", "ب أ 9012", Location(33.50, 36.31)),
    )

    override suspend fun requestRide(pickup: Location, destination: Location): Result<RideRequest> {
        delay(1000)
        val ride = RideRequest(
            id = UUID.randomUUID().toString(),
            riderId = "user_001",
            pickup = pickup,
            destination = destination,
            estimatedFare = 2500.0,
            estimatedMinutes = 8,
            status = RideStatus.SEARCHING,
        )
        return Result.success(ride)
    }

    override fun observeRide(rideId: String): Flow<RideRequest> = flow {
        val statuses = listOf(
            RideStatus.DRIVER_FOUND,
            RideStatus.DRIVER_EN_ROUTE,
            RideStatus.IN_PROGRESS,
            RideStatus.COMPLETED,
        )
        for (status in statuses) {
            delay(4000)
            emit(
                RideRequest(
                    id = rideId,
                    riderId = "user_001",
                    pickup = Location(33.5138, 36.2765, "دمشق - الميدان"),
                    destination = Location(33.5024, 36.3172, "دمشق - الجسر الأبيض"),
                    estimatedFare = 2500.0,
                    estimatedMinutes = if (status == RideStatus.IN_PROGRESS) 12 else 5,
                    status = status,
                )
            )
        }
    }

    override suspend fun getOffers(rideId: String): Result<List<RideOffer>> {
        delay(2000)
        val offers = mockDrivers.map { driver ->
            RideOffer(driver = driver, fare = (2000..3500).random().toDouble(), etaMinutes = (3..10).random())
        }
        return Result.success(offers)
    }

    override suspend fun acceptOffer(rideId: String, driverId: String): Result<RideRequest> {
        delay(500)
        return Result.success(
            RideRequest(
                id = rideId,
                riderId = "user_001",
                pickup = Location(33.5138, 36.2765, "موقعك الحالي"),
                destination = Location(33.5024, 36.3172, "الوجهة"),
                estimatedFare = 2500.0,
                estimatedMinutes = 8,
                status = RideStatus.DRIVER_FOUND,
            )
        )
    }

    override suspend fun cancelRide(rideId: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }
}
