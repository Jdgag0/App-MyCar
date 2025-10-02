package com.joel.mycar.feature.maintenance.data

import androidx.room.*
import com.joel.mycar.feature.maintenance.domain.MaintenanceTask
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceTaskDao {
    @Query("SELECT * FROM maintenance_tasks ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE id = :id")
    fun observeById(id: Long): Flow<MaintenanceTask?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: MaintenanceTask)

    @Delete
    suspend fun delete(task: MaintenanceTask)
}
