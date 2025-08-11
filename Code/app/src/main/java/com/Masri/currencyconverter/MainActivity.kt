package com.Masri.currencyconverter

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.Masri.currencyconverter.ui.screens.CurrencyConverterApp
import com.Masri.currencyconverter.ui.screens.RatesActivity
import com.Masri.currencyconverter.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var locationHelper: LocationHelper
    private var selectedCurrency: String = "USD"

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) fetchAndSetCurrencyFromLocation()
            else Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationHelper = LocationHelper(this)

        // Request location permission and initialize
        checkLocationPermission()

        // Set up the composable content
        setContent {
            CurrencyConverterApp(
                onRatesButtonClick = { navigateToRatesScreen() }, initialCurrency = selectedCurrency
            )
        }
    }

    private fun checkLocationPermission() {
        locationHelper.requestLocationPermission(onPermissionGranted = { fetchAndSetCurrencyFromLocation() },
            onPermissionDenied = { locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION) })
    }

    private fun fetchAndSetCurrencyFromLocation() {
        locationHelper.fetchCurrentLocation(onLocationFetched = { latitude, longitude ->
            lifecycleScope.launch(Dispatchers.IO) {
                val countryCode = locationHelper.getCountryFromCoordinates(latitude, longitude)
                val currency = locationHelper.getCurrencyForCountry(countryCode)
                updateCurrency(currency)
            }
        }, onError = { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateCurrency(currency: String) {
        selectedCurrency = currency
        // Notify the UI about the new currency
        setContent {
            CurrencyConverterApp(
                onRatesButtonClick = { navigateToRatesScreen() }, initialCurrency = selectedCurrency
            )
        }
    }

    private fun navigateToRatesScreen() {
        val intent = Intent(this, RatesActivity::class.java)
        startActivity(intent)
    }
}