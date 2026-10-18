package com.bugbounty.academy.ui.screens.quizzes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugbounty.academy.domain.model.Quiz
import com.bugbounty.academy.domain.model.QuizQuestion
import com.bugbounty.academy.ui.components.CyberTopAppBar
import com.bugbounty.academy.ui.theme.CyberCyan
import com.bugbounty.academy.ui.theme.CyberGreen
import com.bugbounty.academy.ui.theme.CyberRed

@Composable
fun QuizPlayScreen(
    quizId: String,
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit
) {
    val quiz = remember(quizId) { viewModel.getQuizById(quizId) }
    val submissionResult by viewModel.currentSubmission.collectAsState()

    var selectedAnswers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    if (quiz == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Quiz not found", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Scaffold(
        topBar = {
            CyberTopAppBar(
                title = quiz.title,
                canNavigateBack = true,
                onNavigateBack = {
                    viewModel.resetSubmission()
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Results Banner if submitted
            if (submissionResult != null) {
                val res = submissionResult!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isPassed) CyberGreen.copy(alpha = 0.15f)
                        else CyberRed.copy(alpha = 0.15f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (res.isPassed) CyberGreen else CyberRed
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (res.isPassed) "EXAMINATION PASSED!" else "PASS THRESHOLD NOT REACHED",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (res.isPassed) CyberGreen else CyberRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Score: ${res.score} / ${res.total} (${res.percentage}%) • Earned +${res.xpEarned} XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Questions
            quiz.questions.forEachIndexed { index, question ->
                val selectedOptionId = selectedAnswers[question.id]
                val submitted = submissionResult != null
                val correctOption = question.options.firstOrNull { it.isCorrect }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "QUESTION ${index + 1} OF ${quiz.questions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                            if (question.referenceTag != null) {
                                Text(
                                    text = question.referenceTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options
                        question.options.forEach { option ->
                            val isSelected = selectedOptionId == option.id
                            val isThisCorrect = option.isCorrect

                            val (bgColor, borderColor, textColor) = when {
                                submitted && isThisCorrect -> Triple(
                                    CyberGreen.copy(alpha = 0.2f),
                                    CyberGreen,
                                    CyberGreen
                                )
                                submitted && isSelected && !isThisCorrect -> Triple(
                                    CyberRed.copy(alpha = 0.2f),
                                    CyberRed,
                                    CyberRed
                                )
                                isSelected -> Triple(
                                    CyberCyan.copy(alpha = 0.15f),
                                    CyberCyan,
                                    CyberCyan
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !submitted) {
                                        selectedAnswers = selectedAnswers + (question.id to option.id)
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (submitted && isThisCorrect) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(18.dp))
                                    } else if (submitted && isSelected && !isThisCorrect) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = CyberRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Explanation after submission
                        if (submitted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "EXPLANATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = question.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action
            if (submissionResult == null) {
                Button(
                    onClick = { viewModel.submitQuiz(quiz.id, selectedAnswers) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = selectedAnswers.size == quiz.questions.size,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text(
                        text = "Submit Examination (${selectedAnswers.size}/${quiz.questions.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        viewModel.resetSubmission()
                        selectedAnswers = emptyMap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retake Quiz", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
