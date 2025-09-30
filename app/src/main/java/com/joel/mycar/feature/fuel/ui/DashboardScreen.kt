@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.joel.mycar.feature.fuel.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    fuelVM: RefuelViewModel,
    toAddFuel: () -> Unit,
    toListFuel: () -> Unit
) {
    val lastFull by fuelVM.lastFullToFullKmPerL.collectAsState()
    val months by fuelVM.months.collectAsState()
    val monthlyStats by fuelVM.monthlyStats.collectAsState()

    var selectedMonth by remember(months) { mutableStateOf(months.firstOrNull()) }
    val monthStat = monthlyStats.firstOrNull { it.monthKey == selectedMonth }

    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MyCar — Resumen", style = MaterialTheme.typography.titleLarge)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Último rendimiento (full-to-full):")
                Text(lastFull?.let { String.format(Locale.US, "%.2f km/L", it) } ?: "—")
            }
        }

        Text("Resumen mensual", style = MaterialTheme.typography.titleMedium)

        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedMonth ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Mes") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                months.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m) },
                        onClick = { selectedMonth = m; expanded = false }
                    )
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gasto total del mes:")
                Text(monthStat?.let { currency.format(it.totalSpent) } ?: "—")

                Text("Litros abastecidos:")
                Text(monthStat?.totalLiters?.let { String.format(Locale.US, "%.2f L", it) } ?: "—")

                Text("Distancia (km) estimada:")
                Text(monthStat?.totalDistanceKm?.let { String.format(Locale.US, "%.0f km", it) } ?: "—")

                Text("Promedio del mes:")
                Text(monthStat?.avgKmPerL?.let { String.format(Locale.US, "%.2f km/L", it) } ?: "—")
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = toAddFuel, modifier = Modifier.weight(1f)) { Text("Agregar carga") }
            OutlinedButton(onClick = toListFuel, modifier = Modifier.weight(1f)) { Text("Ver lista") }
        }
    }
}
