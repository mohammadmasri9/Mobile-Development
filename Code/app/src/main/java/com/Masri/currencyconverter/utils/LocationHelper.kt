package com.Masri.currencyconverter.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

@Suppress("DEPRECATION")
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun requestLocationPermission(
        onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) onPermissionGranted()
        else onPermissionDenied()
    }

    fun fetchCurrentLocation(
        onLocationFetched: (latitude: Double, longitude: Double) -> Unit, onError: (String) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Permission denied")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) onLocationFetched(location.latitude, location.longitude)
            else onError("Unable to fetch location")
        }.addOnFailureListener {
            onError("Failed to get location: ${it.message}")
        }
    }

    fun getCountryFromCoordinates(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses?.firstOrNull()?.countryCode ?: "US" // Default to "US"
    }

    fun getCurrencyForCountry(countryCode: String): String {
        return when (countryCode) {
            "US" -> "USD"
            "GB" -> "GBP"
            "JP" -> "JPY"
            "SE" -> "SEK"
            else -> "USD" // Default to USD
        }
    }
}