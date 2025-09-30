package com.joel.mycar.feature.fuel.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refuels")
data class Refuel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,      // LocalDate.toEpochDay()
    val odometerKm: Double,      // km
    val liters: Double?,         // puede faltar si no hubo ticket
    val pricePerLiter: Double?,  // MXN/L
    val totalCost: Double?,      // MXN
    val station: String? = null,
    val fuelType: String? = null,
    val notes: String? = null,
    val fullTank: Boolean = true,
    val seeded: Boolean = false
)
