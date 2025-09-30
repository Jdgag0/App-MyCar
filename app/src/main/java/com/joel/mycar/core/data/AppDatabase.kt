package com.joel.mycar.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joel.mycar.feature.fuel.data.MonthlyStatDao
import com.joel.mycar.feature.fuel.data.RefuelDao
import com.joel.mycar.feature.fuel.domain.MonthlyStat
import com.joel.mycar.feature.fuel.domain.Refuel

@Database(
    entities = [
        Refuel::class,
        MonthlyStat::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
    abstract fun monthlyStatDao(): MonthlyStatDao
}
