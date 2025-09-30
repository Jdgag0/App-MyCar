package com.joel.mycar.feature.fuel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun RefuelListScreen(
    viewModel: RefuelViewModel,
    onEdit: (Long) -> Unit = {}     // 👈 callback para navegar a editar
) {
    val items by viewModel.items.collectAsState()

    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
        items(items) { it ->
            val r = it.refuel
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // Datos del registro
                Text(
                    LocalDate.ofEpochDay(r.dateEpochDay).toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Odómetro: ${r.odometerKm} km")
                r.liters?.let { v -> Text("Litros: $v") }
                r.pricePerLiter?.let { v -> Text("Precio/L: $v") }
                r.totalCost?.let { v -> Text("Costo: $v MXN") }
                Text("Gasolinera: ${r.station ?: "—"}")
                Text("Tanque lleno: ${if (r.fullTank) "Sí" else "No"}")
                it.kmPerLiter?.let { v -> Text("Rendimiento (km/L): ${"%.2f".format(v)}") }
                it.lPer100km?.let { v -> Text("Consumo (L/100km): ${"%.2f".format(v)}") }

                Spacer(Modifier.height(8.dp))

                // Acciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { onEdit(r.id) }) {
                        Text("Editar")
                    }
                    OutlinedButton(onClick = { viewModel.delete(r.id) }) {
                        Text("Eliminar")
                    }
                }

                Divider(Modifier.padding(top = 12.dp))
            }
        }
    }
}
