package com.gosyria.app.util

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Suppress("DEPRECATION")
    suspend fun geocode(address: String): LatLng? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            val query = address.trim().withSyriaScope()
            val results = Geocoder(context, Locale.getDefault()).getFromLocationName(
                query,
                5,
                SyriaGeo.MIN_LAT,
                SyriaGeo.MIN_LNG,
                SyriaGeo.MAX_LAT,
                SyriaGeo.MAX_LNG,
            )

            results
                ?.asSequence()
                ?.map { LatLng(it.latitude, it.longitude) }
                ?.firstOrNull(SyriaGeo::contains)
        } catch (e: Exception) {
            null
        }
    }

    private fun String.withSyriaScope(): String {
        val lower = lowercase(Locale.ROOT)
        return if ("سوريا" in this || "syria" in lower) this else "$this, سوريا"
    }
}
