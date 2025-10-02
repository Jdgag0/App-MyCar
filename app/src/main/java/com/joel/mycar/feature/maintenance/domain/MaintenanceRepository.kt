package com.joel.mycar.feature.maintenance.domain

import com.joel.mycar.feature.maintenance.data.MaintenanceTaskDao
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val dao: MaintenanceTaskDao) {
    val tasks: Flow<List<MaintenanceTask>> = dao.observeAll()

    fun task(id: Long): Flow<MaintenanceTask?> = dao.observeById(id)

    suspend fun save(task: MaintenanceTask) {
        dao.upsert(task)
    }

    suspend fun delete(task: MaintenanceTask) {
        dao.delete(task)
    }
}
