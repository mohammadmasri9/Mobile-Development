@file:Suppress("DEPRECATION")

package com.Masri.currencyconverter.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Masri.currencyconverter.components.DropdownField
import com.Masri.currencyconverter.utils.handleConversion
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle

// New color scheme

object NewAppColors {
    val PrimaryBlue = Color(0xFF1976D2)
    val SecondaryTeal = Color(0xFF1976D2)
    val AccentOrange = Color(0xFF1976D2)
    val BackgroundGray = Color(0xFFF5F5F5)
    val CardWhite = Color(0xFFFFFFFF)
    val TextDark = Color(0xFF212121)
    val SuccessGreen = Color(0xFF4CAF50)
}

class CurrencyConverterViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    var amount by mutableStateOf(TextFieldValue(savedStateHandle.get<String>("amount") ?: ""))
    var fromCurrency by mutableStateOf(savedStateHandle.get<String>("fromCurrency") ?: "USD")
    var toCurrency by mutableStateOf(savedStateHandle.get<String>("toCurrency") ?: "EUR")
    var result by mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterApp(
    onRatesButtonClick: () -> Unit,
    initialCurrency: String,
    viewModel: CurrencyConverterViewModel = viewModel()
) {
    Scaffold(
        containerColor = NewAppColors.BackgroundGray,
        contentColor = NewAppColors.TextDark,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enhanced Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = NewAppColors.CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💱 Currency Exchange",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NewAppColors.PrimaryBlue
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Real-time conversion rates",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NewAppColors.SecondaryTeal
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NewAppColors.CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    var inputError by remember { mutableStateOf<String?>(null) }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    Text(
                        text = "Amount to Convert",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NewAppColors.PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.amount,
                        onValueChange = {
                            val validAmountRegex = Regex("^(([1-9]\\d*)|(0))?(\\.\\d{0,2})?\$")
                            if (validAmountRegex.matches(it.text)) {
                                viewModel.amount = it
                                inputError = null
                                viewModel.result = handleConversion(
                                    viewModel.amount.text,
                                    viewModel.fromCurrency,
                                    viewModel.toCurrency
                                )
                            } else {
                                inputError = "Invalid input. Only numeric values are allowed."
                            }
                        },
                        label = { Text("Enter Amount", color = NewAppColors.SecondaryTeal) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        isError = inputError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewAppColors.PrimaryBlue,
                            unfocusedBorderColor = NewAppColors.SecondaryTeal,
                            cursorColor = NewAppColors.PrimaryBlue
                        )
                    )

                    inputError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Currency Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NewAppColors.CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Currency Selection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = NewAppColors.PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DropdownField(
                        label = "From Currency",
                        selectedCurrency = viewModel.fromCurrency
                    ) { selectedCurrency ->
                        viewModel.fromCurrency = selectedCurrency
                        viewModel.result = handleConversion(
                            viewModel.amount.text,
                            viewModel.fromCurrency,
                            viewModel.toCurrency
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Swap Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = viewModel.fromCurrency
                                viewModel.fromCurrency = viewModel.toCurrency
                                viewModel.toCurrency = temp
                                viewModel.result = handleConversion(
                                    viewModel.amount.text,
                                    viewModel.fromCurrency,
                                    viewModel.toCurrency
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NewAppColors.AccentOrange),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {

                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DropdownField(
                        label = "To Currency",
                        selectedCurrency = viewModel.toCurrency
                    ) { selectedCurrency ->
                        viewModel.toCurrency = selectedCurrency
                        viewModel.result = handleConversion(
                            viewModel.amount.text,
                            viewModel.fromCurrency,
                            viewModel.toCurrency
                        )
                    }
                }
            }

            // Result Display Card
            if (viewModel.result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NewAppColors.SuccessGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Conversion Result",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.result,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Refresh Button
                ElevatedButton(
                    onClick = {
                        viewModel.result = handleConversion(
                            viewModel.amount.text,
                            viewModel.fromCurrency,
                            viewModel.toCurrency
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = NewAppColors.SecondaryTeal,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }

                // View Rates Button
                ElevatedButton(
                    onClick = onRatesButtonClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = NewAppColors.PrimaryBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {

                    Text("View Rates")
                }
            }

            // Clear Button
            OutlinedButton(
                onClick = {
                    viewModel.amount = TextFieldValue("")
                    viewModel.result = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NewAppColors.AccentOrange
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NewAppColors.AccentOrange)
                )
            ) {
                Text("Clear All", fontWeight = FontWeight.Medium)
            }
        }
    }
}
