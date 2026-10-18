package com.bugbounty.academy.domain.model

data class Module(
    val id: String,
    val order: Int,
    val title: String,
    val category: ModuleCategory,
    val description: String,
    val iconName: String,
    val estimatedMinutes: Int,
    val lessons: List<Lesson>
)

enum class ModuleCategory(val displayName: String) {
    FOUNDATIONS("Foundations"),
    RECONNAISSANCE("Recon & OSINT"),
    OWASP_CORE("OWASP Core"),
    API_AND_AUTH("API & Authentication"),
    ADVANCED_EXPLOITATION("Advanced Exploitation"),
    DEFENSE_AND_ETHICS("Defense & Professional Practice")
}

data class Lesson(
    val id: String,
    val moduleId: String,
    val title: String,
    val summary: String,
    val contentMarkdown: String,
    val keyTakeaways: List<String>,
    val cweInfo: List<CWEInfo> = emptyList(),
    val references: List<LearningReference> = emptyList(),
    val associatedLabId: String? = null,
    val associatedQuizId: String? = null
)

data class CWEInfo(
    val cweId: String,
    val name: String,
    val description: String,
    val url: String
)

data class LearningReference(
    val title: String,
    val organization: String,
    val url: String,
    val referenceType: ReferenceType
)

enum class ReferenceType {
    OWASP,
    PORTSWIGGER,
    RFC,
    NIST,
    HACKERONE,
    BUGCROWD,
    CVE
}
