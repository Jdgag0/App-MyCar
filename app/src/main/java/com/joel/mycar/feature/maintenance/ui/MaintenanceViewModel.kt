package com.joel.mycar.feature.maintenance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joel.mycar.feature.maintenance.domain.MaintenanceRepository
import com.joel.mycar.feature.maintenance.domain.MaintenanceTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val repository: MaintenanceRepository
) : ViewModel() {

    val tasks: StateFlow<List<MaintenanceTask>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun taskFlow(id: Long): Flow<MaintenanceTask?> = repository.task(id)

    fun save(task: MaintenanceTask, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.save(task)
            onComplete()
        }
    }

    fun delete(task: MaintenanceTask, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(task)
            onComplete()
        }
    }
}

class MaintenanceVMFactory(
    private val repository: MaintenanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
            return MaintenanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
