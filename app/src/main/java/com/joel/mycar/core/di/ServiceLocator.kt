package com.joel.mycar.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.joel.mycar.core.data.AppDatabase

object ServiceLocator {
    @Volatile private var db: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE refuels ADD COLUMN seeded INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS monthly_stats(
                    monthKey TEXT NOT NULL PRIMARY KEY,
                    totalSpent REAL NOT NULL,
                    totalLiters REAL NOT NULL,
                    totalDistanceKm REAL NOT NULL,
                    avgKmPerL REAL
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS maintenance_tasks(
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    details TEXT,
                    dateEpochDay INTEGER NOT NULL,
                    odometerKm REAL,
                    cost REAL,
                    materials TEXT,
                    nextDueEpochDay INTEGER,
                    nextDueOdometerKm REAL
                )
                """.trimIndent()
            )
        }
    }

    fun database(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mycar.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also { db = it }
        }
}
