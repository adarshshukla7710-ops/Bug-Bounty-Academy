package com.bugbounty.academy.data.local.dao

import androidx.room.*
import com.bugbounty.academy.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId LIMIT 1")
    suspend fun getQuizResult(quizId: String): QuizResultEntity?

    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId LIMIT 1")
    fun observeQuizResult(quizId: String): Flow<QuizResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(result: QuizResultEntity)

    @Query("SELECT COUNT(*) FROM quiz_results WHERE isPassed = 1")
    fun getPassedQuizzesCount(): Flow<Int>

    @Query("DELETE FROM quiz_results")
    suspend fun clearAll()
}
