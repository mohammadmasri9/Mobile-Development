    @file:Suppress("DEPRECATION")

    package com.Masri.currencyconverter.ui.screens

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.material.icons.filled.Search
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.text.input.TextFieldValue
    import com.Masri.currencyconverter.ui.theme.CurrencyConverterTheme
    import fetchLiveRates
    import kotlinx.coroutines.launch

    class RatesActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                CurrencyConverterTheme {
                    RatesScreen(onBackClick = { finish() })
                }
            }
        }
    }

    @Composable
    fun RatesScreen(onBackClick: () -> Unit) {
        val customColor = Color(0xFF2d2d3e) // RGB(103, 79, 163)

        var conversionRates by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
        var filteredRates by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
        var loading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Search query state
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var selectedCurrency by remember { mutableStateOf("") }

        // Define list of supported currencies to display
        val supportedCurrencies = listOf("EUR", "USD", "GBP", "JPY", "CNY", "KRW", "SEK" )

        val scope = rememberCoroutineScope()

        // Fetch exchange rates when the screen is displayed
        LaunchedEffect(true) {
            scope.launch {
                try {
                    val rates = fetchLiveRates() // Fetch rates with EUR as base
                    conversionRates = rates
                    loading = false
                } catch (e: Exception) {
                    errorMessage = "Error fetching exchange rates: ${e.message}"
                    loading = false
                }
            }
        }

        // Update selected currency and filter the conversion rates based on the search query
        LaunchedEffect(searchQuery.text) {
            val query = searchQuery.text.uppercase()
            selectedCurrency = if (query.isEmpty()) "" else query
            filteredRates = if (query.isEmpty() || !conversionRates.containsKey(query)) {
                emptyMap() // Don't show anything if the search is empty or invalid
            } else {
                // Calculate the conversion rates from the searched currency
                conversionRates.filter { it.key != query && supportedCurrencies.contains(it.key) }
                    .mapValues { (_, rate) ->
                        val searchedCurrencyRate = conversionRates[query] ?: 1.0
                        rate / searchedCurrencyRate // Calculate rate from searched currency
                    }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back button with an Icon
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = customColor // Matching the button color with the theme
                )
            }

            // Title
            Text(
                text = "Conversion Rates",
                style = MaterialTheme.typography.headlineMedium,
                color = customColor,  // Apply the color directly to the Text
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                customColor = customColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show loading or error message if necessary
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red,  // Apply color directly here too
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                // Display exchange rates from the selected base currency to others
                if (filteredRates.isNotEmpty()) {
                    filteredRates.forEach { (currency, rate) ->
                        RateCard(currency = currency, rate = rate, customColor = customColor, baseCurrency = selectedCurrency)
                    }
                } else if (selectedCurrency.isNotEmpty()) {
                    // If no rates found, show a "No results" message
                    Text(
                        text = "No rates found for $selectedCurrency",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchBar(
        query: TextFieldValue,
        onQueryChange: (TextFieldValue) -> Unit,
        customColor: Color
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search Base Currency") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = customColor, // Set focused border color
                unfocusedBorderColor = Color.Transparent, // Remove the border when not focused
                focusedLabelColor = customColor, // Set focused label color
                cursorColor = customColor, // Set cursor color
                containerColor = Color.White // Set background color to white
            ),
            leadingIcon = {
                IconButton(onClick = { /* Handle icon click if necessary */ }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium, // Apply rounded corners
            isError = query.text.isNotEmpty() && query.text.length < 3 // Optional: set error if input is too short
        )
    }
    @Composable
    fun RateCard(currency: String, rate: Double, customColor: Color, baseCurrency: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(240, 240, 240)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$baseCurrency to $currency",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black, // Apply color directly here
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rate: ${"%.4f".format(rate)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = customColor, // Apply color directly here
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }