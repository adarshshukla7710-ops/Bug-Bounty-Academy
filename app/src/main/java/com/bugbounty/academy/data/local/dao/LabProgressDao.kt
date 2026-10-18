package com.bugbounty.academy.data.local.dao

import androidx.room.*
import com.bugbounty.academy.data.local.entity.LabProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabProgressDao {
    @Query("SELECT * FROM lab_progress")
    fun getAllLabProgress(): Flow<List<LabProgressEntity>>

    @Query("SELECT * FROM lab_progress WHERE labId = :labId LIMIT 1")
    suspend fun getLabProgress(labId: String): LabProgressEntity?

    @Query("SELECT * FROM lab_progress WHERE labId = :labId LIMIT 1")
    fun observeLabProgress(labId: String): Flow<LabProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LabProgressEntity)

    @Query("SELECT COUNT(*) FROM lab_progress WHERE isCompleted = 1")
    fun getCompletedLabsCount(): Flow<Int>

    @Query("DELETE FROM lab_progress")
    suspend fun clearAll()
}
