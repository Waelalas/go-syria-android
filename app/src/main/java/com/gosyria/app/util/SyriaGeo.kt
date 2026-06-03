package com.gosyria.app.util

import com.google.android.gms.maps.model.LatLng

object SyriaGeo {
    val CENTER = LatLng(34.8021, 38.9968)

    const val DEFAULT_ZOOM = 6f
    const val CITY_ZOOM = 13f

    const val MIN_LAT = 32.0
    const val MAX_LAT = 37.5
    const val MIN_LNG = 35.5
    const val MAX_LNG = 42.5

    fun contains(point: LatLng): Boolean =
        point.latitude in MIN_LAT..MAX_LAT && point.longitude in MIN_LNG..MAX_LNG
}
