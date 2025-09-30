package com.joel.mycar.feature.fuel.data

import androidx.room.*
import com.joel.mycar.feature.fuel.domain.Refuel
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(refuel: Refuel): Long

    @Delete
    suspend fun delete(refuel: Refuel)

    @Query("SELECT * FROM refuels ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<Refuel>>

    @Query("SELECT * FROM refuels ORDER BY dateEpochDay ASC, id ASC")
    suspend fun allAscending(): List<Refuel>

    @Query("SELECT * FROM refuels WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Refuel?

    @Query("DELETE FROM refuels WHERE seeded = 0")
    suspend fun deleteNonSeeded()

    @Query("SELECT * FROM refuels ORDER BY dateEpochDay DESC, id DESC LIMIT 1")
    suspend fun lastRefuel(): Refuel?

    @Query("SELECT DISTINCT station FROM refuels WHERE station IS NOT NULL AND station <> '' ORDER BY station ASC")
    fun observeStations(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query("SELECT DISTINCT fuelType FROM refuels WHERE fuelType IS NOT NULL AND fuelType <> '' ORDER BY fuelType ASC")
    fun observeFuelTypes(): kotlinx.coroutines.flow.Flow<List<String>>

}
