package com.bugbounty.academy.domain.model

import kotlin.math.ceil
import kotlin.math.min

enum class SeverityLevel(val label: String, val hexColor: String) {
    INFO("Info", "#3B82F6"),
    LOW("Low", "#10B981"),
    MEDIUM("Medium", "#F59E0B"),
    HIGH("High", "#EF4444"),
    CRITICAL("Critical", "#881337");

    companion object {
        fun fromScore(score: Double): SeverityLevel {
            return when {
                score == 0.0 -> INFO
                score < 4.0 -> LOW
                score < 7.0 -> MEDIUM
                score < 9.0 -> HIGH
                else -> CRITICAL
            }
        }
    }
}

enum class AttackVector(val code: String, val score: Double) {
    NETWORK("N", 0.85),
    ADJACENT("A", 0.62),
    LOCAL("L", 0.55),
    PHYSICAL("P", 0.20)
}

enum class AttackComplexity(val code: String, val score: Double) {
    LOW("L", 0.77),
    HIGH("H", 0.44)
}

enum class PrivilegesRequired(val code: String, val scoreUnchanged: Double, val scoreChanged: Double) {
    NONE("N", 0.85, 0.85),
    LOW("L", 0.62, 0.68),
    HIGH("H", 0.27, 0.50)
}

enum class UserInteraction(val code: String, val score: Double) {
    NONE("N", 0.85),
    REQUIRED("R", 0.62)
}

enum class Scope(val code: String) {
    UNCHANGED("U"),
    CHANGED("C")
}

enum class ImpactMetric(val code: String, val score: Double) {
    NONE("N", 0.0),
    LOW("L", 0.22),
    HIGH("H", 0.56)
}

data class CvssVector(
    val attackVector: AttackVector = AttackVector.NETWORK,
    val attackComplexity: AttackComplexity = AttackComplexity.LOW,
    val privilegesRequired: PrivilegesRequired = PrivilegesRequired.NONE,
    val userInteraction: UserInteraction = UserInteraction.NONE,
    val scope: Scope = Scope.UNCHANGED,
    val confidentiality: ImpactMetric = ImpactMetric.HIGH,
    val integrity: ImpactMetric = ImpactMetric.HIGH,
    val availability: ImpactMetric = ImpactMetric.NONE
) {
    fun toVectorString(): String {
        return "CVSS:3.1/AV:${attackVector.code}/AC:${attackComplexity.code}/PR:${privilegesRequired.code}/UI:${userInteraction.code}/S:${scope.code}/C:${confidentiality.code}/I:${integrity.code}/A:${availability.code}"
    }

    fun calculateScore(): Double {
        val iss = 1.0 - ((1.0 - confidentiality.score) * (1.0 - integrity.score) * (1.0 - availability.score))
        val prScore = if (scope == Scope.CHANGED) privilegesRequired.scoreChanged else privilegesRequired.scoreUnchanged
        val exploitability = 8.22 * attackVector.score * attackComplexity.score * prScore * userInteraction.score

        if (iss <= 0.0) return 0.0

        val impact = if (scope == Scope.UNCHANGED) {
            6.42 * iss
        } else {
            7.52 * (iss - 0.029) - 3.25 * Math.pow((iss - 0.02), 15.0)
        }

        val baseScore = if (scope == Scope.UNCHANGED) {
            min(impact + exploitability, 10.0)
        } else {
            min(1.08 * (impact + exploitability), 10.0)
        }

        // Round up to 1 decimal place as per CVSS v3.1 specification (ceil to 1 decimal)
        return ceil(baseScore * 10.0) / 10.0
    }
}

data class VulnerabilityReport(
    val id: Long = 0,
    val title: String,
    val targetAsset: String,
    val vulnerabilityType: String,
    val cweId: String,
    val cvssVector: CvssVector,
    val cvssScore: Double,
    val severity: SeverityLevel,
    val summary: String,
    val technicalDescription: String,
    val stepsToReproduce: String,
    val businessImpact: String,
    val remediation: String,
    val httpProofOfConcept: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun generateMarkdown(): String {
        return buildString {
            appendLine("# Vulnerability Report: $title")
            appendLine()
            appendLine("| Field | Detail |")
            appendLine("|---|---|")
            appendLine("| **Target Asset** | `$targetAsset` |")
            appendLine("| **Vulnerability Type** | $vulnerabilityType |")
            appendLine("| **CWE** | $cweId |")
            appendLine("| **Severity** | **${severity.label.uppercase()}** ($cvssScore) |")
            appendLine("| **CVSS v3.1 Vector** | `${cvssVector.toVectorString()}` |")
            appendLine()
            appendLine("## 1. Summary")
            appendLine(summary)
            appendLine()
            appendLine("## 2. Technical Description")
            appendLine(technicalDescription)
            appendLine()
            appendLine("## 3. Steps to Reproduce (PoC)")
            appendLine(stepsToReproduce)
            appendLine()
            if (httpProofOfConcept.isNotBlank()) {
                appendLine("## 4. HTTP Proof of Concept")
                appendLine("```http")
                appendLine(httpProofOfConcept)
                appendLine("```")
                appendLine()
            }
            appendLine("## 5. Business Impact")
            appendLine(businessImpact)
            appendLine()
            appendLine("## 6. Remediation & Fix Recommendation")
            appendLine(remediation)
            appendLine()
            appendLine("---")
            appendLine("*Generated via BugBounty Academy Report Builder*")
        }
    }
}
