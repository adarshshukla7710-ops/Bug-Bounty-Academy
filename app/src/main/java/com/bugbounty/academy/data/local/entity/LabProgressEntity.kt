package com.bugbounty.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_progress")
data class LabProgressEntity(
    @PrimaryKey
    val labId: String,
    val isCompleted: Boolean,
    val flagCaptured: String? = null,
    val attemptsCount: Int = 0,
    val completedAt: Long? = null,
    val lastSubmittedPayload: String? = null
)
