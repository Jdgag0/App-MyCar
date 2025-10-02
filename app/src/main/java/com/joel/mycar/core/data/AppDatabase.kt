package com.joel.mycar.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joel.mycar.feature.fuel.data.MonthlyStatDao
import com.joel.mycar.feature.fuel.data.RefuelDao
import com.joel.mycar.feature.fuel.domain.MonthlyStat
import com.joel.mycar.feature.fuel.domain.Refuel
import com.joel.mycar.feature.maintenance.data.MaintenanceTaskDao
import com.joel.mycar.feature.maintenance.domain.MaintenanceTask

@Database(
    entities = [
        Refuel::class,
        MonthlyStat::class,
        MaintenanceTask::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
    abstract fun monthlyStatDao(): MonthlyStatDao
    abstract fun maintenanceTaskDao(): MaintenanceTaskDao
}
