package com.Masri.currencyconverter.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.Masri.currencyconverter.ui.theme.AppColors

import fetchLiveRates
import kotlinx.coroutines.launch

@Composable
fun DropdownField(
    label: String,
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var currencies by remember { mutableStateOf<List<String>>(emptyList()) }
    val customColor = AppColors.CustomColor
    val coroutineScope = rememberCoroutineScope()

    // Fetch available currencies dynamically
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val rates = fetchLiveRates() // Fetch rates from API
                currencies = rates.keys.toList().sorted() // Convert keys to sorted list
            } catch (e: Exception) {
                println("Error fetching currencies: ${e.message}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = { expanded = true }) {
            Text(text = "$label: $selectedCurrency", color = customColor)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            currencies.forEach { currency ->
                DropdownMenuItem(text = { Text(currency) }, onClick = {
                    onCurrencyChange(currency)
                    expanded = false
                })
            }
        }
    }
}
