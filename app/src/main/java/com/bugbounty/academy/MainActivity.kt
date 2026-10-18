package com.bugbounty.academy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bugbounty.academy.ui.navigation.BugBountyBottomNavBar
import com.bugbounty.academy.ui.navigation.BugBountyNavGraph
import com.bugbounty.academy.ui.navigation.Screen
import com.bugbounty.academy.ui.screens.curriculum.CurriculumViewModel
import com.bugbounty.academy.ui.screens.dashboard.DashboardViewModel
import com.bugbounty.academy.ui.screens.labs.LabViewModel
import com.bugbounty.academy.ui.screens.profile.ProfileViewModel
import com.bugbounty.academy.ui.screens.quizzes.QuizViewModel
import com.bugbounty.academy.ui.screens.report.ReportViewModel
import com.bugbounty.academy.ui.screens.tools.ToolsViewModel
import com.bugbounty.academy.ui.theme.BugBountyAcademyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as BugBountyApplication

        setContent {
            val profileViewModel = ProfileViewModel(
                userProgressRepository = app.userProgressRepository,
                userPreferencesDataStore = app.userPreferences,
                userProgressDao = app.database.userProgressDao(),
                labProgressDao = app.database.labProgressDao(),
                quizResultDao = app.database.quizResultDao()
            )

            val themeMode by profileViewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            BugBountyAcademyTheme(darkTheme = isDark) {
                BugBountyAppMain(app = app, profileViewModel = profileViewModel)
            }
        }
    }
}

@Composable
fun BugBountyAppMain(
    app: BugBountyApplication,
    profileViewModel: ProfileViewModel
) {
    val navController = rememberNavController()

    val dashboardViewModel = DashboardViewModel(
        userProgressRepository = app.userProgressRepository,
        curriculumRepository = app.curriculumRepository,
        labRepository = app.labRepository
    )

    val curriculumViewModel = CurriculumViewModel(
        curriculumRepository = app.curriculumRepository
    )

    val labViewModel = LabViewModel(
        labRepository = app.labRepository
    )

    val quizViewModel = QuizViewModel(
        quizRepository = app.quizRepository
    )

    val toolsViewModel = ToolsViewModel(
        toolsRepository = app.toolsRepository
    )

    val reportViewModel = ReportViewModel(
        reportRepository = app.reportRepository
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Curriculum.route,
        Screen.Labs.route,
        Screen.Quizzes.route,
        Screen.Tools.route,
        Screen.ReportBuilder.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BugBountyBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        BugBountyNavGraph(
            navController = navController,
            dashboardViewModel = dashboardViewModel,
            curriculumViewModel = curriculumViewModel,
            labViewModel = labViewModel,
            quizViewModel = quizViewModel,
            toolsViewModel = toolsViewModel,
            reportViewModel = reportViewModel,
            profileViewModel = profileViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
