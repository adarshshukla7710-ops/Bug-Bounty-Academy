package com.bugbounty.academy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bugbounty.academy.ui.screens.curriculum.CurriculumScreen
import com.bugbounty.academy.ui.screens.curriculum.CurriculumViewModel
import com.bugbounty.academy.ui.screens.curriculum.LessonDetailScreen
import com.bugbounty.academy.ui.screens.dashboard.DashboardScreen
import com.bugbounty.academy.ui.screens.dashboard.DashboardViewModel
import com.bugbounty.academy.ui.screens.labs.LabDetailScreen
import com.bugbounty.academy.ui.screens.labs.LabListScreen
import com.bugbounty.academy.ui.screens.labs.LabViewModel
import com.bugbounty.academy.ui.screens.profile.ProfileScreen
import com.bugbounty.academy.ui.screens.profile.ProfileViewModel
import com.bugbounty.academy.ui.screens.quizzes.QuizListScreen
import com.bugbounty.academy.ui.screens.quizzes.QuizPlayScreen
import com.bugbounty.academy.ui.screens.quizzes.QuizViewModel
import com.bugbounty.academy.ui.screens.report.ReportBuilderScreen
import com.bugbounty.academy.ui.screens.report.ReportViewModel
import com.bugbounty.academy.ui.screens.report.SavedReportsScreen
import com.bugbounty.academy.ui.screens.tools.ToolDetailScreen
import com.bugbounty.academy.ui.screens.tools.ToolsListScreen
import com.bugbounty.academy.ui.screens.tools.ToolsViewModel

@Composable
fun BugBountyNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    curriculumViewModel: CurriculumViewModel,
    labViewModel: LabViewModel,
    quizViewModel: QuizViewModel,
    toolsViewModel: ToolsViewModel,
    reportViewModel: ReportViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToCurriculum = { navController.navigate(Screen.Curriculum.route) },
                onNavigateToLabs = { navController.navigate(Screen.Labs.route) },
                onNavigateToQuizzes = { navController.navigate(Screen.Quizzes.route) },
                onNavigateToReportBuilder = { navController.navigate(Screen.ReportBuilder.route) },
                onNavigateToLesson = { lessonId ->
                    navController.navigate(Screen.LessonDetail.createRoute(lessonId))
                },
                onNavigateToLab = { labId ->
                    navController.navigate(Screen.LabDetail.createRoute(labId))
                }
            )
        }

        // Curriculum
        composable(Screen.Curriculum.route) {
            CurriculumScreen(
                viewModel = curriculumViewModel,
                onNavigateToLesson = { lessonId ->
                    navController.navigate(Screen.LessonDetail.createRoute(lessonId))
                }
            )
        }

        composable(
            route = Screen.LessonDetail.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            LessonDetailScreen(
                lessonId = lessonId,
                viewModel = curriculumViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLab = { labId ->
                    navController.navigate(Screen.LabDetail.createRoute(labId))
                },
                onNavigateToQuiz = { quizId ->
                    navController.navigate(Screen.QuizPlay.createRoute(quizId))
                }
            )
        }

        // Practice Labs
        composable(Screen.Labs.route) {
            LabListScreen(
                viewModel = labViewModel,
                onNavigateToLab = { labId ->
                    navController.navigate(Screen.LabDetail.createRoute(labId))
                }
            )
        }

        composable(
            route = Screen.LabDetail.route,
            arguments = listOf(navArgument("labId") { type = NavType.StringType })
        ) { backStackEntry ->
            val labId = backStackEntry.arguments?.getString("labId") ?: ""
            LabDetailScreen(
                labId = labId,
                viewModel = labViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Quizzes
        composable(Screen.Quizzes.route) {
            QuizListScreen(
                viewModel = quizViewModel,
                onNavigateToQuiz = { quizId ->
                    navController.navigate(Screen.QuizPlay.createRoute(quizId))
                }
            )
        }

        composable(
            route = Screen.QuizPlay.route,
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            QuizPlayScreen(
                quizId = quizId,
                viewModel = quizViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Tools
        composable(Screen.Tools.route) {
            ToolsListScreen(
                viewModel = toolsViewModel,
                onNavigateToTool = { toolId ->
                    navController.navigate(Screen.ToolDetail.createRoute(toolId))
                }
            )
        }

        composable(
            route = Screen.ToolDetail.route,
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
            ToolDetailScreen(
                toolId = toolId,
                viewModel = toolsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Report Builder
        composable(Screen.ReportBuilder.route) {
            ReportBuilderScreen(
                viewModel = reportViewModel,
                onNavigateToSavedReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }

        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                viewModel = reportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = profileViewModel)
        }
    }
}
