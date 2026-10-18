package com.bugbounty.academy.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bugbounty.academy.data.repository.ReportRepository
import com.bugbounty.academy.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReportFormState(
    val id: Long = 0,
    val title: String = "",
    val targetAsset: String = "",
    val vulnerabilityType: String = "Broken Object Level Authorization (BOLA)",
    val cweId: String = "CWE-639",
    val cvssVector: CvssVector = CvssVector(),
    val summary: String = "",
    val technicalDescription: String = "",
    val stepsToReproduce: String = "",
    val businessImpact: String = "",
    val remediation: String = "",
    val httpProofOfConcept: String = ""
) {
    fun toDomainModel(): VulnerabilityReport {
        val score = cvssVector.calculateScore()
        val severity = SeverityLevel.fromScore(score)
        return VulnerabilityReport(
            id = id,
            title = title.ifBlank { "Untitled Vulnerability Report" },
            targetAsset = targetAsset.ifBlank { "example.com" },
            vulnerabilityType = vulnerabilityType,
            cweId = cweId,
            cvssVector = cvssVector,
            cvssScore = score,
            severity = severity,
            summary = summary,
            technicalDescription = technicalDescription,
            stepsToReproduce = stepsToReproduce,
            businessImpact = businessImpact,
            remediation = remediation,
            httpProofOfConcept = httpProofOfConcept
        )
    }

    companion object {
        fun fromDomain(report: VulnerabilityReport): ReportFormState {
            return ReportFormState(
                id = report.id,
                title = report.title,
                targetAsset = report.targetAsset,
                vulnerabilityType = report.vulnerabilityType,
                cweId = report.cweId,
                cvssVector = report.cvssVector,
                summary = report.summary,
                technicalDescription = report.technicalDescription,
                stepsToReproduce = report.stepsToReproduce,
                businessImpact = report.businessImpact,
                remediation = report.remediation,
                httpProofOfConcept = report.httpProofOfConcept
            )
        }
    }
}

class ReportViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    val savedReports: StateFlow<List<VulnerabilityReport>> = reportRepository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(ReportFormState())
    val formState: StateFlow<ReportFormState> = _formState.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    fun updateForm(updater: (ReportFormState) -> ReportFormState) {
        _formState.update(updater)
    }

    fun loadTemplate(template: VulnerabilityReport) {
        _formState.value = ReportFormState.fromDomain(template.copy(id = 0))
    }

    fun loadReport(report: VulnerabilityReport) {
        _formState.value = ReportFormState.fromDomain(report)
    }

    fun resetForm() {
        _formState.value = ReportFormState()
        _saveStatusMessage.value = null
    }

    fun saveReport() {
        viewModelScope.launch {
            val report = _formState.value.toDomainModel()
            val newId = reportRepository.saveReport(report)
            _formState.update { it.copy(id = newId) }
            _saveStatusMessage.value = "Report saved to offline database!"
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            reportRepository.deleteReport(id)
            if (_formState.value.id == id) {
                resetForm()
            }
        }
    }

    fun getTemplates(): List<VulnerabilityReport> = reportRepository.getReportTemplates()
}
