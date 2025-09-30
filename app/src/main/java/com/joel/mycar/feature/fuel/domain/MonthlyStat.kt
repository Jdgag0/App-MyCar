package com.joel.mycar.feature.fuel.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * monthKey: "YYYY-MM" (ej. 2025-09)
 * Guardamos agregados por mes: total gastado, litros, distancia y promedio km/L
 */
@Entity(tableName = "monthly_stats")
data class MonthlyStat(
    @PrimaryKey val monthKey: String, // "YYYY-MM"
    val totalSpent: Double,           // MXN
    val totalLiters: Double,          // L
    val totalDistanceKm: Double,      // km (desde ventanas entre full-tanks dentro del mes)
    val avgKmPerL: Double?            // promedio ponderado (o simple) del mes
)
