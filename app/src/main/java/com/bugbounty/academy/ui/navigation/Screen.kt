package com.bugbounty.academy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Bottom Bar Destinations
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Curriculum : Screen("curriculum", "Curriculum", Icons.Default.School)
    object Labs : Screen("labs", "Practice Labs", Icons.Default.Science)
    object Quizzes : Screen("quizzes", "Quizzes", Icons.Default.Quiz)
    object Tools : Screen("tools", "Tools", Icons.Default.Build)
    object ReportBuilder : Screen("report", "Report Builder", Icons.Default.Description)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Detail & Sub-Screens
    object LessonDetail : Screen("lesson_detail/{lessonId}", "Lesson Detail") {
        fun createRoute(lessonId: String) = "lesson_detail/$lessonId"
    }

    object LabDetail : Screen("lab_detail/{labId}", "Lab Detail") {
        fun createRoute(labId: String) = "lab_detail/$labId"
    }

    object QuizPlay : Screen("quiz_play/{quizId}", "Quiz") {
        fun createRoute(quizId: String) = "quiz_play/$quizId"
    }

    object ToolDetail : Screen("tool_detail/{toolId}", "Tool Guide") {
        fun createRoute(toolId: String) = "tool_detail/$toolId"
    }

    object SavedReports : Screen("saved_reports", "Saved Reports")

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Curriculum,
            Labs,
            Quizzes,
            Tools,
            ReportBuilder,
            Profile
        )
    }
}
