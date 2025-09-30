package com.joel.mycar.feature.fuel.domain

import com.joel.mycar.feature.fuel.data.RefuelDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

data class RefuelWithDerived(
    val refuel: Refuel,
    val distanceSincePrev: Double?, // km
    val kmPerLiter: Double?,        // km/L (full-to-full)
    val lPer100km: Double?,         // L/100km
    val costPerKm: Double?          // MXN/km
)

class RefuelRepository(private val dao: RefuelDao) {

    fun observeWithDerived(): Flow<List<RefuelWithDerived>> =
        dao.observeAll().map { desc ->
            val asc = desc.sortedBy { it.dateEpochDay }
            val out = mutableListOf<RefuelWithDerived>()

            // 1) Valores base por registro (distancia/costo/km)
            for (i in asc.indices) {
                val cur = asc[i]
                val prev = asc.getOrNull(i - 1)
                val distance = if (prev != null) max(0.0, cur.odometerKm - prev.odometerKm) else null
                val totalCost = cur.totalCost ?: cur.pricePerLiter?.let { ppl -> (cur.liters ?: 0.0) * ppl }

                out += RefuelWithDerived(
                    refuel = cur,
                    distanceSincePrev = distance,
                    kmPerLiter = null,
                    lPer100km = null,
                    costPerKm = if (distance != null && distance > 0 && totalCost != null) totalCost / distance else null
                )
            }

            // 2) Ventanas entre tanques llenos (full-to-full)
            var lastFullIndex: Int? = null
            var litersSinceFull = 0.0
            for ((idx, r) in asc.withIndex()) {
                if (lastFullIndex == null) {
                    if (r.fullTank) lastFullIndex = idx
                    continue
                } else {
                    litersSinceFull += r.liters ?: 0.0
                    if (r.fullTank) {
                        val prev = asc[lastFullIndex]
                        val distance = max(0.0, r.odometerKm - prev.odometerKm)
                        val kmPerL = if (litersSinceFull > 0) distance / litersSinceFull else null
                        val lPer100 = kmPerL?.let { if (it > 0) 100.0 / it else null }

                        val iOut = out.indexOfFirst { it.refuel.id == r.id && it.refuel.dateEpochDay == r.dateEpochDay }
                        if (iOut >= 0) out[iOut] = out[iOut].copy(kmPerLiter = kmPerL, lPer100km = lPer100)

                        lastFullIndex = idx
                        litersSinceFull = 0.0
                    }
                }
            }
            out.sortedByDescending { it.refuel.dateEpochDay }
        }
}
