package com.bugbounty.academy.data.repository

import com.bugbounty.academy.data.local.dao.LabProgressDao
import com.bugbounty.academy.data.local.dao.QuizResultDao
import com.bugbounty.academy.data.local.dao.ReportDao
import com.bugbounty.academy.data.local.dao.UserProgressDao
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.domain.model.Badge
import com.bugbounty.academy.domain.model.BountyRank
import com.bugbounty.academy.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class UserProgressRepository(
    private val userProgressDao: UserProgressDao,
    private val labProgressDao: LabProgressDao,
    private val quizResultDao: QuizResultDao,
    private val reportDao: ReportDao,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val totalLessonsCount: Int,
    private val totalLabsCount: Int,
    private val totalQuizzesCount: Int
) {
    val userStats: Flow<UserStats> = combine(
        combine(
            userPreferencesDataStore.userXp,
            userPreferencesDataStore.currentStreak,
            userProgressDao.getCompletedLessonsCount()
        ) { xp, streak, lessonsDone -> Triple(xp, streak, lessonsDone) },
        combine(
            labProgressDao.getCompletedLabsCount(),
            quizResultDao.getPassedQuizzesCount(),
            reportDao.getReportsCount()
        ) { labsDone, quizzesDone, reportsCount -> Triple(labsDone, quizzesDone, reportsCount) }
    ) { (xp, streak, lessonsDone), (labsDone, quizzesDone, reportsCount) ->
        val rank = BountyRank.fromXp(xp)
        val badges = computeBadges(xp, streak, lessonsDone, labsDone, quizzesDone, reportsCount)

        UserStats(
            totalXp = xp,
            rank = rank,
            completedLessonsCount = lessonsDone,
            totalLessonsCount = totalLessonsCount,
            completedLabsCount = labsDone,
            totalLabsCount = totalLabsCount,
            passedQuizzesCount = quizzesDone,
            totalQuizzesCount = totalQuizzesCount,
            reportsGeneratedCount = reportsCount,
            currentStreakDays = streak,
            badges = badges
        )
    }

    private fun computeBadges(
        xp: Int,
        streak: Int,
        lessons: Int,
        labs: Int,
        quizzes: Int,
        reports: Int
    ): List<Badge> {
        return listOf(
            Badge(
                id = "badge_first_blood",
                name = "First Blood",
                description = "Complete your first practice lab and capture a flag",
                icon = "flag",
                isUnlocked = labs >= 1
            ),
            Badge(
                id = "badge_recon_initiate",
                name = "Recon Initiate",
                description = "Complete 3 curriculum lessons",
                icon = "school",
                isUnlocked = lessons >= 3
            ),
            Badge(
                id = "badge_quiz_master",
                name = "Quiz Ace",
                description = "Pass 3 certification quizzes with distinction",
                icon = "military_tech",
                isUnlocked = quizzes >= 3
            ),
            Badge(
                id = "badge_lab_veteran",
                name = "Lab Veteran",
                description = "Solve 10 or more deterministic practice labs",
                icon = "science",
                isUnlocked = labs >= 10
            ),
            Badge(
                id = "badge_reporter",
                name = "Report Artisan",
                description = "Generate and save a vulnerability report",
                icon = "description",
                isUnlocked = reports >= 1
            ),
            Badge(
                id = "badge_streak_hero",
                name = "Consistency Flame",
                description = "Maintain a 3-day learning streak",
                icon = "local_fire_department",
                isUnlocked = streak >= 3
            )
        )
    }
}
