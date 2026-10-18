package com.bugbounty.academy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bugbounty.academy.data.local.converter.Converters
import com.bugbounty.academy.data.local.dao.*
import com.bugbounty.academy.data.local.entity.*

@Database(
    entities = [
        LessonProgressEntity::class,
        LabProgressEntity::class,
        QuizResultEntity::class,
        SavedReportEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BugBountyDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun labProgressDao(): LabProgressDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun reportDao(): ReportDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: BugBountyDatabase? = null

        fun getInstance(context: Context): BugBountyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BugBountyDatabase::class.java,
                    "bugbounty_academy.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
