package com.joel.mycar.feature.maintenance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joel.mycar.feature.maintenance.domain.MaintenanceTask
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListScreen(
    viewModel: MaintenanceViewModel,
    onAdd: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val currency = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormatter = rememberDateFormatter()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar mantenimiento")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = onAdd
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    MaintenanceTaskCard(
                        task = task,
                        currency = currency,
                        dateFormatter = dateFormatter,
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, onAdd: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aún no hay mantenimientos registrados",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Agrega tus tareas de mantenimiento para llevar un historial y recordatorios.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(onClick = onAdd, modifier = Modifier.padding(top = 16.dp)) {
            Text("Registrar mantenimiento")
        }
    }
}

@Composable
private fun MaintenanceTaskCard(
    task: MaintenanceTask,
    currency: NumberFormat,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Realizado: ${dateFormatter.format(LocalDate.ofEpochDay(task.dateEpochDay))}")
            task.odometerKm?.let { Text(String.format(Locale.US, "Odómetro: %.0f km", it)) }
            task.cost?.let { Text("Costo: ${currency.format(it)}") }
            task.materials?.takeIf { it.isNotBlank() }?.let {
                Text("Materiales: $it")
            }
            task.details?.takeIf { it.isNotBlank() }?.let {
                Text(it)
            }
            if (task.nextDueEpochDay != null || task.nextDueOdometerKm != null) {
                Text(
                    buildString {
                        append("Próximo recordatorio: ")
                        task.nextDueEpochDay?.let {
                            append(dateFormatter.format(LocalDate.ofEpochDay(it)))
                        }
                        if (task.nextDueOdometerKm != null) {
                            if (task.nextDueEpochDay != null) append(" o ")
                            append(String.format(Locale.US, "%.0f km", task.nextDueOdometerKm))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
