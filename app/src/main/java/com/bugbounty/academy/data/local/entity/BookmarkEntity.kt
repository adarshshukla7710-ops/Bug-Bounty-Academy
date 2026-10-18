package com.bugbounty.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val type: String, // LESSON, LAB, TOOL
    val title: String,
    val subtitle: String,
    val savedAt: Long = System.currentTimeMillis()
)
