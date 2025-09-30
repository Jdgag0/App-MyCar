package com.joel.mycar.feature.fuel.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefuelScreen(
    viewModel: RefuelViewModel,
    onSaved: () -> Unit
) {
    var form by remember { mutableStateOf(RefuelForm()) }

    // dropdowns
    var stationExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    val stations by viewModel.stations.collectAsState()
    val fuels by viewModel.fuelTypes.collectAsState()

    LaunchedEffect(Unit) {
        val (lastStation, lastFuel) = viewModel.loadLastDefaults()
        form = form.copy(
            station = form.station.ifBlank { lastStation.orEmpty() },
            fuelType = form.fuelType.ifBlank { lastFuel.orEmpty() }
        )
    }

    fun updateAndAutofill(update: (RefuelForm) -> RefuelForm) {
        val updated = update(form)
        form = viewModel.autoFill(updated)
    }

    // 👇 Scroll + padding para no quedar oculto por barra/teclado
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva carga", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = form.odometerKm,
            onValueChange = { s -> updateAndAutofill { it.copy(odometerKm = s) } },
            label = { Text("Odómetro (km) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.odometerValid || form.odometerKm.isBlank(),
            supportingText = {
                if (form.odometerKm.isBlank()) Text("Campo obligatorio")
                else if (!form.odometerValid) Text("Ingrese un número válido")
            }
        )

        OutlinedTextField(
            value = form.liters,
            onValueChange = { s -> updateAndAutofill { it.copy(liters = s) } },
            label = { Text("Litros") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.litersValid,
            supportingText = { if (!form.litersValid) Text("Número inválido (≥ 0)") }
        )

        OutlinedTextField(
            value = form.pricePerLiter,
            onValueChange = { s -> updateAndAutofill { it.copy(pricePerLiter = s) } },
            label = { Text("Precio por litro (MXN/L)") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.priceValid,
            supportingText = { if (!form.priceValid) Text("Número inválido (≥ 0)") }
        )

        OutlinedTextField(
            value = form.totalCost,
            onValueChange = { }, // no editable
            label = { Text("Costo total (MXN)") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        // Gasolinera (dropdown editable)
        ExposedDropdownMenuBox(
            expanded = stationExpanded,
            onExpandedChange = { stationExpanded = !stationExpanded }
        ) {
            OutlinedTextField(
                value = form.station,
                onValueChange = { s -> form = form.copy(station = s) },
                label = { Text("Gasolinera") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stationExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = stationExpanded,
                onDismissRequest = { stationExpanded = false }
            ) {
                stations.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            form = form.copy(station = option)
                            stationExpanded = false
                        }
                    )
                }
            }
        }

        // Tipo de combustible (dropdown editable)
        ExposedDropdownMenuBox(
            expanded = fuelExpanded,
            onExpandedChange = { fuelExpanded = !fuelExpanded }
        ) {
            OutlinedTextField(
                value = form.fuelType,
                onValueChange = { s -> form = form.copy(fuelType = s) },
                label = { Text("Tipo de combustible") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = fuelExpanded,
                onDismissRequest = { fuelExpanded = false }
            ) {
                fuels.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            form = form.copy(fuelType = option)
                            fuelExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.notes,
            onValueChange = { s -> form = form.copy(notes = s) },
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = form.fullTank,
                onCheckedChange = { c -> form = form.copy(fullTank = c) }
            )
            Text("Tanque lleno")
        }

        // Empuja el botón al final del scroll
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.save(form); onSaved() },
            modifier = Modifier.fillMaxWidth(),
            enabled = form.canSave
        ) {
            Text("Guardar")
        }
        Spacer(Modifier.height(8.dp)) // un poco extra por si la nav bar es alta
    }
}
