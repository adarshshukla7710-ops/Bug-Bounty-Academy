package com.bugbounty.academy.ui.screens.labs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.local.entity.LabProgressEntity
import com.bugbounty.academy.data.repository.LabRepository
import com.bugbounty.academy.domain.model.Lab
import com.bugbounty.academy.domain.model.LabCategory
import com.bugbounty.academy.domain.model.LabDifficulty
import com.bugbounty.academy.domain.model.LabExecutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LabViewModel(
    private val labRepository: LabRepository
) : ViewModel() {

    val allLabProgress: StateFlow<List<LabProgressEntity>> = labRepository.observeAllLabProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _lastExecutionResult = MutableStateFlow<LabExecutionResult?>(null)
    val lastExecutionResult: StateFlow<LabExecutionResult?> = _lastExecutionResult.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    private val _flagSubmissionMessage = MutableStateFlow<String?>(null)
    val flagSubmissionMessage: StateFlow<String?> = _flagSubmissionMessage.asStateFlow()

    fun getAllLabs(): List<Lab> = labRepository.getAllLabs()

    fun getLabById(labId: String): Lab? = labRepository.getLabById(labId)

    fun filterLabs(
        category: LabCategory?,
        difficulty: LabDifficulty?,
        query: String
    ): List<Lab> {
        return labRepository.getAllLabs().filter { lab ->
            val matchCategory = category == null || lab.category == category
            val matchDifficulty = difficulty == null || lab.difficulty == difficulty
            val matchQuery = query.isBlank() ||
                    lab.title.contains(query, ignoreCase = true) ||
                    lab.description.contains(query, ignoreCase = true) ||
                    lab.cwe.contains(query, ignoreCase = true) ||
                    lab.vulnerabilityType.contains(query, ignoreCase = true)
            matchCategory && matchDifficulty && matchQuery
        }
    }

    fun executeLab(
        labId: String,
        method: String,
        endpoint: String,
        headers: Map<String, String>,
        body: String
    ) {
        viewModelScope.launch {
            _isExecuting.value = true
            try {
                // Extract query params from endpoint string if any
                val queryParams = mutableMapOf<String, String>()
                if (endpoint.contains("?")) {
                    val queryString = endpoint.substringAfter("?")
                    queryString.split("&").forEach { param ->
                        val parts = param.split("=", limit = 2)
                        if (parts.size == 2) queryParams[parts[0]] = parts[1]
                    }
                }

                val result = labRepository.executeLab(
                    labId = labId,
                    method = method,
                    endpoint = endpoint,
                    headers = headers,
                    body = body,
                    queryParams = queryParams
                )
                _lastExecutionResult.value = result
            } finally {
                _isExecuting.value = false
            }
        }
    }

    fun submitFlag(labId: String, submittedFlag: String) {
        viewModelScope.launch {
            val isValid = labRepository.submitManualFlag(labId, submittedFlag)
            _flagSubmissionMessage.value = if (isValid) {
                "Flag Accepted! Vulnerability successfully conquered."
            } else {
                "Invalid Flag. Review the execution output and scenario context."
            }
        }
    }

    fun clearExecutionState() {
        _lastExecutionResult.value = null
        _flagSubmissionMessage.value = null
    }
}
