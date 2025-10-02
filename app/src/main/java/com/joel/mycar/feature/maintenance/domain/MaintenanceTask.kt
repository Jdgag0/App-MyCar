package com.joel.mycar.feature.maintenance.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_tasks")
data class MaintenanceTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val details: String? = null,
    val dateEpochDay: Long,
    val odometerKm: Double? = null,
    val cost: Double? = null,
    val materials: String? = null,
    val nextDueEpochDay: Long? = null,
    val nextDueOdometerKm: Double? = null
)
