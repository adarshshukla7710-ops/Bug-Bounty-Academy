package com.bugbounty.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bugbounty.academy.domain.model.*

@Entity(tableName = "saved_reports")
data class SavedReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAsset: String,
    val vulnerabilityType: String,
    val cweId: String,
    val cvssVectorString: String,
    val cvssScore: Double,
    val severity: String,
    val summary: String,
    val technicalDescription: String,
    val stepsToReproduce: String,
    val businessImpact: String,
    val remediation: String,
    val httpProofOfConcept: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): VulnerabilityReport {
        return VulnerabilityReport(
            id = id,
            title = title,
            targetAsset = targetAsset,
            vulnerabilityType = vulnerabilityType,
            cweId = cweId,
            cvssVector = parseVectorString(cvssVectorString),
            cvssScore = cvssScore,
            severity = SeverityLevel.valueOf(severity),
            summary = summary,
            technicalDescription = technicalDescription,
            stepsToReproduce = stepsToReproduce,
            businessImpact = businessImpact,
            remediation = remediation,
            httpProofOfConcept = httpProofOfConcept,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(model: VulnerabilityReport): SavedReportEntity {
            return SavedReportEntity(
                id = model.id,
                title = model.title,
                targetAsset = model.targetAsset,
                vulnerabilityType = model.vulnerabilityType,
                cweId = model.cweId,
                cvssVectorString = model.cvssVector.toVectorString(),
                cvssScore = model.cvssScore,
                severity = model.severity.name,
                summary = model.summary,
                technicalDescription = model.technicalDescription,
                stepsToReproduce = model.stepsToReproduce,
                businessImpact = model.businessImpact,
                remediation = model.remediation,
                httpProofOfConcept = model.httpProofOfConcept,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }

        private fun parseVectorString(vector: String): CvssVector {
            // Fallback default if unparseable
            var av = AttackVector.NETWORK
            var ac = AttackComplexity.LOW
            var pr = PrivilegesRequired.NONE
            var ui = UserInteraction.NONE
            var sc = Scope.UNCHANGED
            var c = ImpactMetric.HIGH
            var i = ImpactMetric.HIGH
            var a = ImpactMetric.NONE

            val parts = vector.split("/")
            for (part in parts) {
                val kv = part.split(":")
                if (kv.size == 2) {
                    when (kv[0]) {
                        "AV" -> av = AttackVector.entries.firstOrNull { it.code == kv[1] } ?: av
                        "AC" -> ac = AttackComplexity.entries.firstOrNull { it.code == kv[1] } ?: ac
                        "PR" -> pr = PrivilegesRequired.entries.firstOrNull { it.code == kv[1] } ?: pr
                        "UI" -> ui = UserInteraction.entries.firstOrNull { it.code == kv[1] } ?: ui
                        "S" -> sc = Scope.entries.firstOrNull { it.code == kv[1] } ?: sc
                        "C" -> c = ImpactMetric.entries.firstOrNull { it.code == kv[1] } ?: c
                        "I" -> i = ImpactMetric.entries.firstOrNull { it.code == kv[1] } ?: i
                        "A" -> a = ImpactMetric.entries.firstOrNull { it.code == kv[1] } ?: a
                    }
                }
            }
            return CvssVector(av, ac, pr, ui, sc, c, i, a)
        }
    }
}
