package com.bugbounty.academy.data.local.dao

import androidx.room.*
import com.bugbounty.academy.data.local.entity.SavedReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM saved_reports ORDER BY updatedAt DESC")
    fun getAllReports(): Flow<List<SavedReportEntity>>

    @Query("SELECT * FROM saved_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): SavedReportEntity?

    @Query("SELECT * FROM saved_reports WHERE id = :id LIMIT 1")
    fun observeReportById(id: Long): Flow<SavedReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReportEntity): Long

    @Update
    suspend fun updateReport(report: SavedReportEntity)

    @Delete
    suspend fun deleteReport(report: SavedReportEntity)

    @Query("DELETE FROM saved_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_reports")
    fun getReportsCount(): Flow<Int>
}
