package com.bugbounty.academy.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.local.dao.LabProgressDao
import com.bugbounty.academy.data.local.dao.QuizResultDao
import com.bugbounty.academy.data.local.dao.UserProgressDao
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.data.repository.UserProgressRepository
import com.bugbounty.academy.domain.model.UserStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userProgressRepository: UserProgressRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val userProgressDao: UserProgressDao,
    private val labProgressDao: LabProgressDao,
    private val quizResultDao: QuizResultDao
) : ViewModel() {

    val userStats: StateFlow<UserStats> = userProgressRepository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    val themeMode: StateFlow<String> = userPreferencesDataStore.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "DARK"
        )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesDataStore.setThemeMode(mode)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            userPreferencesDataStore.resetAllPreferences()
            userProgressDao.clearAll()
            labProgressDao.clearAll()
            quizResultDao.clearAll()
        }
    }
}
