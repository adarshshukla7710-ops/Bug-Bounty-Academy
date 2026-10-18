package com.bugbounty.academy.ui.screens.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.local.entity.QuizResultEntity
import com.bugbounty.academy.data.repository.QuizRepository
import com.bugbounty.academy.domain.model.Quiz
import com.bugbounty.academy.domain.model.QuizSubmissionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    val allQuizResults: StateFlow<List<QuizResultEntity>> = quizRepository.observeAllResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSubmission = MutableStateFlow<QuizSubmissionResult?>(null)
    val currentSubmission: StateFlow<QuizSubmissionResult?> = _currentSubmission.asStateFlow()

    fun getAllQuizzes(): List<Quiz> = quizRepository.getAllQuizzes()

    fun getQuizById(quizId: String): Quiz? = quizRepository.getQuizById(quizId)

    fun submitQuiz(quizId: String, selectedAnswers: Map<String, String>) {
        viewModelScope.launch {
            val result = quizRepository.submitQuiz(quizId, selectedAnswers)
            _currentSubmission.value = result
        }
    }

    fun resetSubmission() {
        _currentSubmission.value = null
    }
}
