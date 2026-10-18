package com.bugbounty.academy

import android.app.Application
import com.bugbounty.academy.data.local.BugBountyDatabase
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.data.repository.*

class BugBountyApplication : Application() {

    lateinit var database: BugBountyDatabase
        private set

    lateinit var userPreferences: UserPreferencesDataStore
        private set

    lateinit var curriculumRepository: CurriculumRepository
        private set

    lateinit var labRepository: LabRepository
        private set

    lateinit var quizRepository: QuizRepository
        private set

    lateinit var toolsRepository: ToolsRepository
        private set

    lateinit var reportRepository: ReportRepository
        private set

    lateinit var userProgressRepository: UserProgressRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = BugBountyDatabase.getInstance(this)
        userPreferences = UserPreferencesDataStore(this)

        curriculumRepository = CurriculumRepository(
            userProgressDao = database.userProgressDao(),
            userPreferencesDataStore = userPreferences
        )

        labRepository = LabRepository(
            labProgressDao = database.labProgressDao(),
            userPreferencesDataStore = userPreferences
        )

        quizRepository = QuizRepository(
            quizResultDao = database.quizResultDao(),
            userPreferencesDataStore = userPreferences
        )

        toolsRepository = ToolsRepository()

        reportRepository = ReportRepository(
            reportDao = database.reportDao()
        )

        val totalLessons = curriculumRepository.getAllModules().sumOf { it.lessons.size }
        val totalLabs = labRepository.getAllLabs().size
        val totalQuizzes = quizRepository.getAllQuizzes().size

        userProgressRepository = UserProgressRepository(
            userProgressDao = database.userProgressDao(),
            labProgressDao = database.labProgressDao(),
            quizResultDao = database.quizResultDao(),
            reportDao = database.reportDao(),
            userPreferencesDataStore = userPreferences,
            totalLessonsCount = totalLessons,
            totalLabsCount = totalLabs,
            totalQuizzesCount = totalQuizzes
        )
    }

    companion object {
        lateinit var instance: BugBountyApplication
            private set
    }
}
