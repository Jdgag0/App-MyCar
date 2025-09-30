package com.joel.mycar.feature.fuel.data

import androidx.room.*
import com.joel.mycar.feature.fuel.domain.MonthlyStat
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MonthlyStat>)

    @Query("DELETE FROM monthly_stats")
    suspend fun clearAll()

    @Query("SELECT * FROM monthly_stats ORDER BY monthKey DESC")
    fun observeAll(): Flow<List<MonthlyStat>>

    @Query("SELECT * FROM monthly_stats WHERE monthKey = :key LIMIT 1")
    fun observeByKey(key: String): Flow<MonthlyStat?>
}
