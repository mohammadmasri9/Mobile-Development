package com.Masri.currencyconverter.utils

import fetchLiveRates
import kotlinx.coroutines.runBlocking
import kotlin.collections.contains

fun handleConversion(
    amount: String,
    fromCurrency: String,
    toCurrency: String
): String {
    val amountValue = amount.toDoubleOrNull()
    return when {
        amount.isBlank() -> "Amount cannot be empty. Please enter a value."
        amount.endsWith(".") -> "Incomplete number. Please complete the value."
        amountValue == null -> "Invalid amount. Ensure the input is numeric and valid."
        else -> {
            return try {
                // Use a coroutine to fetch live rates asynchronously
                runBlocking {
                    val rates = fetchLiveRates()  // Asynchronous call
                    if (fromCurrency !in rates) return@runBlocking "Conversion rate for '$fromCurrency' is not available."
                    if (toCurrency !in rates) return@runBlocking "Conversion rate for '$toCurrency' is not available."

                    val fromRate = rates[fromCurrency]
                        ?: return@runBlocking "Rate for '$fromCurrency' not found."
                    val toRate =
                        rates[toCurrency] ?: return@runBlocking "Rate for '$toCurrency' not found."

                    val convertedValue = (amountValue / fromRate) * toRate
                    "Converted $amount $fromCurrency to %.2f $toCurrency".format(convertedValue)
                }
            } catch (e: Exception) {
                "Error fetching conversion rates: ${e.message}"
            }
        }
    }
}