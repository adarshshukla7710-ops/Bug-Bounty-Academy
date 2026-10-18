package com.bugbounty.academy.ui.screens.curriculum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.local.entity.LessonProgressEntity
import com.bugbounty.academy.data.repository.CurriculumRepository
import com.bugbounty.academy.domain.model.Lesson
import com.bugbounty.academy.domain.model.Module
import com.bugbounty.academy.domain.model.ModuleCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CurriculumViewModel(
    private val curriculumRepository: CurriculumRepository
) : ViewModel() {

    val completedLessons: StateFlow<List<LessonProgressEntity>> = curriculumRepository.observeCompletedLessons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getAllModules(): List<Module> = curriculumRepository.getAllModules()

    fun getModulesByCategory(category: ModuleCategory): List<Module> {
        return curriculumRepository.getAllModules().filter { it.category == category }
    }

    fun getLessonById(lessonId: String): Lesson? = curriculumRepository.getLessonById(lessonId)

    fun markLessonCompleted(lessonId: String, moduleId: String) {
        viewModelScope.launch {
            curriculumRepository.markLessonCompleted(lessonId, moduleId)
        }
    }
}
