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
    // Syria bounding box: SW(32.3, 35.7) NE(37.3, 42.4)
    @Suppress("DEPRECATION")
    suspend fun geocode(address: String): LatLng? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            val results = Geocoder(context, Locale("ar")).getFromLocationName(
                address, 1,
                32.3, 35.7, 37.3, 42.4,
            )
            results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }
}
