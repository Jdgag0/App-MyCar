package com.joel.mycar.feature.fuel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun EditRefuelScreen(
    viewModel: RefuelViewModel,
    id: Long?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf<RefuelForm?>(null) }

    LaunchedEffect(id) {
        if (id != null) {
            form = viewModel.loadForm(id)   // 👈 usa loadForm del VM
        }
    }

    val f = form ?: run { Text("Cargando…"); return }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Editar carga", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(f.odometerKm, { s -> form = f.copy(odometerKm = s) },
            label = { Text("Odómetro (km)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.liters, { s -> form = f.copy(liters = s) },
            label = { Text("Litros") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.pricePerLiter, { s -> form = f.copy(pricePerLiter = s) },
            label = { Text("Precio por litro (MXN/L)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.totalCost, { s -> form = f.copy(totalCost = s) },
            label = { Text("Costo total (MXN)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.station, { s -> form = f.copy(station = s) },
            label = { Text("Gasolinera") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.fuelType, { s -> form = f.copy(fuelType = s) },
            label = { Text("Tipo de combustible") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(f.notes, { s -> form = f.copy(notes = s) },
            label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())

        Row {
            Checkbox(f.fullTank, onCheckedChange = { c -> form = f.copy(fullTank = c) })
            Text("Tanque lleno")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { scope.launch { viewModel.save(f); onDone() } },
                modifier = Modifier.weight(1f)
            ) { Text("Guardar cambios") }

            OutlinedButton(
                onClick = { scope.launch { f.id?.let { viewModel.delete(it) }; onDone() } },
                modifier = Modifier.weight(1f)
            ) { Text("Eliminar") }
        }
    }
}
