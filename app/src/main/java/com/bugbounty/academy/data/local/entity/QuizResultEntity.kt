package com.bugbounty.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey
    val quizId: String,
    val score: Int,
    val total: Int,
    val isPassed: Boolean,
    val bestPercentage: Int,
    val attempts: Int = 1,
    val completedAt: Long
)
