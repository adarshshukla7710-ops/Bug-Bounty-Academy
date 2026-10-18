package com.bugbounty.academy.domain.model

data class Quiz(
    val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val passPercentage: Int = 70,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<QuizOption>,
    val explanation: String,
    val referenceTag: String? = null
)

data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

data class QuizSubmissionResult(
    val quizId: String,
    val score: Int,
    val total: Int,
    val percentage: Int,
    val isPassed: Boolean,
    val answersMap: Map<String, String>, // questionId -> selectedOptionId
    val xpEarned: Int
)
