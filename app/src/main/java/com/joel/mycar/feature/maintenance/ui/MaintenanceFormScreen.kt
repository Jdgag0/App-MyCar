package com.joel.mycar.feature.maintenance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joel.mycar.feature.maintenance.domain.MaintenanceTask
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    viewModel: MaintenanceViewModel,
    taskId: Long?,
    onDone: () -> Unit
) {
    var form by remember { mutableStateOf(MaintenanceForm()) }

    val taskFlow = remember(taskId) { taskId?.let { viewModel.taskFlow(it) } }
    val existing = taskFlow?.collectAsState(initial = null)?.value

    LaunchedEffect(existing?.id) {
        if (existing != null) {
            form = MaintenanceForm.from(existing)
        } else if (taskId == null) {
            form = MaintenanceForm()
        }
    }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (taskId == null) "Nuevo mantenimiento" else "Editar mantenimiento",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = form.title,
            onValueChange = { form = form.copy(title = it) },
            label = { Text("Tarea *") },
            modifier = Modifier.fillMaxWidth(),
            isError = form.title.isBlank(),
            supportingText = { if (form.title.isBlank()) Text("Campo obligatorio") }
        )

        OutlinedTextField(
            value = form.date,
            onValueChange = { form = form.copy(date = it) },
            label = { Text("Fecha (YYYY-MM-DD) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.dateValid,
            supportingText = { if (!form.dateValid) Text("Formato de fecha inválido") }
        )

        OutlinedTextField(
            value = form.odometerKm,
            onValueChange = { form = form.copy(odometerKm = it) },
            label = { Text("Odómetro (km)") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.odometerValid,
            supportingText = { if (!form.odometerValid) Text("Número inválido") }
        )

        OutlinedTextField(
            value = form.cost,
            onValueChange = { form = form.copy(cost = it) },
            label = { Text("Costo") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.costValid,
            supportingText = { if (!form.costValid) Text("Número inválido") }
        )

        OutlinedTextField(
            value = form.materials,
            onValueChange = { form = form.copy(materials = it) },
            label = { Text("Materiales utilizados") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.details,
            onValueChange = { form = form.copy(details = it) },
            label = { Text("Detalles / notas") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text("Recordatorio próximo servicio", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = form.nextDate,
            onValueChange = { form = form.copy(nextDate = it) },
            label = { Text("Fecha objetivo (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.nextDateValid,
            supportingText = { if (!form.nextDateValid) Text("Formato de fecha inválido") }
        )

        OutlinedTextField(
            value = form.nextOdometer,
            onValueChange = { form = form.copy(nextOdometer = it) },
            label = { Text("Odómetro objetivo (km)") },
            modifier = Modifier.fillMaxWidth(),
            isError = !form.nextOdometerValid,
            supportingText = { if (!form.nextOdometerValid) Text("Número inválido") }
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.save(form.toEntity()) { onDone() }
            },
            enabled = form.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }

        if (taskId != null && existing != null) {
            TextButton(
                onClick = {
                    viewModel.delete(existing) { onDone() }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

data class MaintenanceForm(
    val id: Long? = null,
    val title: String = "",
    val date: String = LocalDate.now().toString(),
    val odometerKm: String = "",
    val cost: String = "",
    val materials: String = "",
    val details: String = "",
    val nextDate: String = "",
    val nextOdometer: String = ""
) {
    val dateValid: Boolean get() = runCatching { LocalDate.parse(date) }.isSuccess
    val odometerValid: Boolean get() = odometerKm.isBlank() || odometerKm.toDoubleOrNull() != null
    val costValid: Boolean get() = cost.isBlank() || cost.toDoubleOrNull() != null
    val nextDateValid: Boolean get() = nextDate.isBlank() || runCatching { LocalDate.parse(nextDate) }.isSuccess
    val nextOdometerValid: Boolean get() = nextOdometer.isBlank() || nextOdometer.toDoubleOrNull() != null
    val canSave: Boolean get() = title.isNotBlank() && dateValid && odometerValid && costValid && nextDateValid && nextOdometerValid

    fun toEntity(): MaintenanceTask = MaintenanceTask(
        id = id ?: 0L,
        title = title.trim(),
        details = details.ifBlank { null },
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        odometerKm = odometerKm.toDoubleOrNull(),
        cost = cost.toDoubleOrNull(),
        materials = materials.ifBlank { null },
        nextDueEpochDay = nextDate.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it).toEpochDay() },
        nextDueOdometerKm = nextOdometer.toDoubleOrNull()
    )

    companion object {
        fun from(task: MaintenanceTask): MaintenanceForm = MaintenanceForm(
            id = task.id,
            title = task.title,
            date = LocalDate.ofEpochDay(task.dateEpochDay).toString(),
            odometerKm = task.odometerKm?.toString().orEmpty(),
            cost = task.cost?.toString().orEmpty(),
            materials = task.materials.orEmpty(),
            details = task.details.orEmpty(),
            nextDate = task.nextDueEpochDay?.let { LocalDate.ofEpochDay(it).toString() }.orEmpty(),
            nextOdometer = task.nextDueOdometerKm?.toString().orEmpty()
        )
    }
}
