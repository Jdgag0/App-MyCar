package com.joel.mycar.feature.fuel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joel.mycar.feature.fuel.data.MonthlyStatDao
import com.joel.mycar.feature.fuel.data.RefuelDao
import com.joel.mycar.feature.fuel.domain.MonthlyStat
import com.joel.mycar.feature.fuel.domain.Refuel
import com.joel.mycar.feature.fuel.domain.RefuelRepository
import com.joel.mycar.feature.fuel.domain.RefuelWithDerived
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

// ---------- FORM + VALIDACIONES ----------
data class RefuelForm(
    val id: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val odometerKm: String = "",
    val liters: String = "",
    val pricePerLiter: String = "",
    val totalCost: String = "",
    val station: String = "",
    val fuelType: String = "",
    val notes: String = "",
    val fullTank: Boolean = true
) {
    val odometerValid: Boolean get() = odometerKm.toDoubleOrNull() != null
    val litersValid: Boolean get() = liters.isBlank() || (liters.toDoubleOrNull()?.let { it >= 0.0 } == true)
    val priceValid: Boolean get() = pricePerLiter.isBlank() || (pricePerLiter.toDoubleOrNull()?.let { it >= 0.0 } == true)
    val totalValid: Boolean get() = totalCost.isBlank() || (totalCost.toDoubleOrNull()?.let { it >= 0.0 } == true)
    val canSave: Boolean get() = odometerKm.isNotBlank() && odometerValid && litersValid && priceValid && totalValid
}

class RefuelViewModel(
    private val dao: RefuelDao,
    private val monthlyDao: MonthlyStatDao
) : ViewModel() {

    private val repo = RefuelRepository(dao)

    // Lista con derivados para UI
    val items: StateFlow<List<RefuelWithDerived>> =
        repo.observeWithDerived().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Último full-to-full
    val lastFullToFullKmPerL: StateFlow<Double?> =
        items.map { list -> list.firstOrNull { it.kmPerLiter != null }?.kmPerLiter }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Sugerencias (dropdowns)
    val stations: StateFlow<List<String>> =
        dao.observeStations()
            .map { it.map(String::trim).filter { s -> s.isNotBlank() }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelTypes: StateFlow<List<String>> =
        dao.observeFuelTypes()
            .map { it.map(String::trim).filter { s -> s.isNotBlank() }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun loadLastDefaults(): Pair<String?, String?> {
        val last = dao.lastRefuel()
        return Pair(last?.station, last?.fuelType)
    }

    // ---------- AUTOCÁLCULO ----------
    fun autoFill(form: RefuelForm): RefuelForm {
        val l = form.liters.toDoubleOrNull()
        val p = form.pricePerLiter.toDoubleOrNull()
        return if (l != null && p != null) {
            form.copy(totalCost = String.format("%.2f", l * p))
        } else {
            form.copy(totalCost = "")
        }
    }

    // ---------- MÉTRICAS MENSUALES ----------
    val monthlyStats: StateFlow<List<MonthlyStat>> =
        monthlyDao.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val months: StateFlow<List<String>> =
        monthlyStats.map { list -> list.map { it.monthKey } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun recomputeMonthlyStats() {
        val all = dao.allAscending()

        data class Segment(val endDay: Long, val distanceKm: Double, val liters: Double, val cost: Double?)
        val segments = mutableListOf<Segment>()

        var lastFullIndex: Int? = null
        var accLiters = 0.0
        var accCost = 0.0
        var haveAnyCost = false

        for ((idx, r) in all.withIndex()) {
            if (lastFullIndex == null) {
                if (r.fullTank) lastFullIndex = idx
                continue
            } else {
                val liters = r.liters ?: 0.0
                accLiters += liters
                val c = r.totalCost ?: r.pricePerLiter?.let { ppl -> liters * ppl }
                if (c != null) { accCost += c; haveAnyCost = true }
                if (r.fullTank) {
                    val prev = all[lastFullIndex!!]
                    val dist = max(0.0, r.odometerKm - prev.odometerKm)
                    if (accLiters > 0.0 && dist > 0.0) {
                        segments += Segment(r.dateEpochDay, dist, accLiters, if (haveAnyCost) accCost else null)
                    }
                    lastFullIndex = idx
                    accLiters = 0.0
                    accCost = 0.0
                    haveAnyCost = false
                }
            }
        }

        val byMonth = segments.groupBy { seg ->
            val d = LocalDate.ofEpochDay(seg.endDay)
            YearMonth.of(d.year, d.month.value)
        }

        val monthly = byMonth.map { (ym, segs) ->
            val totalLiters = segs.sumOf { it.liters }
            val totalDist = segs.sumOf { it.distanceKm }
            val totalSpent = segs.sumOf { it.cost ?: 0.0 }
            val avgKmPerL = if (totalLiters > 0) totalDist / totalLiters else null
            MonthlyStat(
                monthKey = "%04d-%02d".format(ym.year, ym.monthValue),
                totalSpent = totalSpent,
                totalLiters = totalLiters,
                totalDistanceKm = totalDist,
                avgKmPerL = avgKmPerL
            )
        }

        monthlyDao.clearAll()
        monthlyDao.upsertAll(monthly)
    }

    // ---------- CRUD ----------
    fun save(form: RefuelForm) = viewModelScope.launch {
        val entity = Refuel(
            id = form.id ?: 0L,
            dateEpochDay = form.date.toEpochDay(),
            odometerKm = form.odometerKm.toDoubleOrNull() ?: return@launch,
            liters = form.liters.toDoubleOrNull(),
            pricePerLiter = form.pricePerLiter.toDoubleOrNull(),
            totalCost = form.totalCost.toDoubleOrNull(),
            station = form.station.ifBlank { null },
            fuelType = form.fuelType.ifBlank { null },
            notes = form.notes.ifBlank { null },
            fullTank = form.fullTank
        )
        dao.upsert(entity)
        recomputeMonthlyStats()
    }

    suspend fun loadForm(id: Long): RefuelForm? {
        val r = dao.getById(id) ?: return null
        return RefuelForm(
            id = r.id,
            date = LocalDate.ofEpochDay(r.dateEpochDay),
            odometerKm = r.odometerKm.toString(),
            liters = r.liters?.toString().orEmpty(),
            pricePerLiter = r.pricePerLiter?.toString().orEmpty(),
            totalCost = r.totalCost?.toString().orEmpty(),
            station = r.station.orEmpty(),
            fuelType = r.fuelType.orEmpty(),
            notes = r.notes.orEmpty(),
            fullTank = r.fullTank
        )
    }

    fun delete(id: Long) = viewModelScope.launch {
        dao.getById(id)?.let { dao.delete(it) }
        recomputeMonthlyStats()
    }

    fun clearUserAdded() = viewModelScope.launch {
        dao.deleteNonSeeded()
        recomputeMonthlyStats()
    }

    fun importCsv(lines: List<String>) = viewModelScope.launch {
        if (lines.isEmpty()) return@launch
        lines.drop(1).forEach { raw ->
            if (raw.isBlank()) return@forEach
            val cols = raw.split(',')
            if (cols.size < 9) return@forEach

            val date = LocalDate.parse(cols[0].trim())
            val odo = cols[1].trim().toDouble()
            val liters = cols[2].trim().ifBlank { null }?.toDouble()
            val ppl = cols[3].trim().ifBlank { null }?.toDouble()
            val total = cols[4].trim().ifBlank { null }?.toDouble()
            val station = cols[5].trim().ifBlank { null }
            val fuelType = cols[6].trim().ifBlank { null }
            val notes = cols[7].trim().ifBlank { null }
            val full = cols[8].trim().equals("true", ignoreCase = true)

            dao.upsert(
                Refuel(
                    dateEpochDay = date.toEpochDay(),
                    odometerKm = odo,
                    liters = liters,
                    pricePerLiter = ppl,
                    totalCost = total,
                    station = station,
                    fuelType = fuelType,
                    notes = notes,
                    fullTank = full,
                    seeded = true
                )
            )
        }
        recomputeMonthlyStats()
    }
}

class RefuelVMFactory(
    private val dao: RefuelDao,
    private val monthlyDao: MonthlyStatDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RefuelViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return RefuelViewModel(dao, monthlyDao) as T
    }
}
