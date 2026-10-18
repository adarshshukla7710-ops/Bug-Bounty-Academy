package com.bugbounty.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey
    val lessonId: String,
    val moduleId: String,
    val isCompleted: Boolean,
    val completedAt: Long
)
