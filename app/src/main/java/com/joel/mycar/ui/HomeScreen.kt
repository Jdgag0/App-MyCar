package com.joel.mycar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    toFuel: () -> Unit,
    toMaintenance: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MyCar", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Elige el módulo que quieres administrar",
            style = MaterialTheme.typography.bodyMedium
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Combustible", style = MaterialTheme.typography.titleMedium)
                Text("Registra cargas, consulta estadísticas y edita tus registros de combustible.")
                Button(onClick = toFuel, modifier = Modifier.align(Alignment.End)) {
                    Text("Abrir")
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mantenimiento", style = MaterialTheme.typography.titleMedium)
                Text("Lleva un historial de tareas, costos, materiales y recordatorios de servicio.")
                Button(onClick = toMaintenance, modifier = Modifier.align(Alignment.End)) {
                    Text("Abrir")
                }
            }
        }
    }
}
