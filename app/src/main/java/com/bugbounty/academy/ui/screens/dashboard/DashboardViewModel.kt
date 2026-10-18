package com.bugbounty.academy.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.repository.CurriculumRepository
import com.bugbounty.academy.data.repository.LabRepository
import com.bugbounty.academy.data.repository.UserProgressRepository
import com.bugbounty.academy.domain.model.Lab
import com.bugbounty.academy.domain.model.Module
import com.bugbounty.academy.domain.model.UserStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val userStats: UserStats = UserStats(),
    val featuredModules: List<Module> = emptyList(),
    val recommendedLabs: List<Lab> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val userProgressRepository: UserProgressRepository,
    private val curriculumRepository: CurriculumRepository,
    private val labRepository: LabRepository
) : ViewModel() {

    val userStats: StateFlow<UserStats> = userProgressRepository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    fun getFeaturedModules(): List<Module> = curriculumRepository.getAllModules().take(3)

    fun getRecommendedLabs(): List<Lab> = labRepository.getAllLabs().take(4)
}
