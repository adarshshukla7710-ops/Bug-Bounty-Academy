package com.bugbounty.academy.data.repository

import com.bugbounty.academy.data.local.dao.ReportDao
import com.bugbounty.academy.data.local.entity.SavedReportEntity
import com.bugbounty.academy.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepository(
    private val reportDao: ReportDao
) {
    val allReports: Flow<List<VulnerabilityReport>> = reportDao.getAllReports().map { list ->
        list.map { it.toDomainModel() }
    }

    val reportsCount: Flow<Int> = reportDao.getReportsCount()

    suspend fun getReportById(id: Long): VulnerabilityReport? {
        return reportDao.getReportById(id)?.toDomainModel()
    }

    suspend fun saveReport(report: VulnerabilityReport): Long {
        val entity = SavedReportEntity.fromDomainModel(report)
        return if (report.id == 0L) {
            reportDao.insertReport(entity)
        } else {
            reportDao.updateReport(entity)
            report.id
        }
    }

    suspend fun deleteReport(id: Long) {
        reportDao.deleteReportById(id)
    }

    fun getReportTemplates(): List<VulnerabilityReport> = listOf(
        VulnerabilityReport(
            id = 0,
            title = "Broken Object Level Authorization (BOLA) Exposing Sensitive Documents",
            targetAsset = "api.target.com",
            vulnerabilityType = "Broken Object Level Authorization (IDOR)",
            cweId = "CWE-639",
            cvssVector = CvssVector(
                attackVector = AttackVector.NETWORK,
                attackComplexity = AttackComplexity.LOW,
                privilegesRequired = PrivilegesRequired.LOW,
                userInteraction = UserInteraction.NONE,
                scope = Scope.UNCHANGED,
                confidentiality = ImpactMetric.HIGH,
                integrity = ImpactMetric.NONE,
                availability = ImpactMetric.NONE
            ),
            cvssScore = 6.5,
            severity = SeverityLevel.MEDIUM,
            summary = "An insecure direct object reference in the `/api/v1/documents/{id}` endpoint allows authenticated users to access confidential contracts belonging to other tenants without authorization.",
            technicalDescription = "The endpoint accepts a numeric document ID in the URL path. The backend executes `SELECT * FROM documents WHERE id = ?` without verifying that the requesting user belongs to the owning organization.",
            stepsToReproduce = """1. Log in as standard user A (Org 1).
2. Note that your legitimate document ID is `doc-1041.pdf`.
3. Issue a GET request modifying the document ID to `doc-1042.pdf`:
   ```bash
   curl -i -H "Authorization: Bearer <token_user_A>" https://api.target.com/api/v1/documents/doc-1042.pdf
   ```
4. Observe HTTP 200 OK with full contract details for Org 2.""",
            businessImpact = "Confidential cross-tenant contracts, personal customer records, and enterprise trade secrets can be bulk-exfiltrated by any registered user.",
            remediation = "Scope database queries by current authenticated tenant ID: `SELECT * FROM documents WHERE id = ? AND tenant_id = ?`.",
            httpProofOfConcept = """GET /api/v1/documents/doc-1042.pdf HTTP/1.1
Host: api.target.com
Authorization: Bearer eyJhbGciOi...

HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="Acme_Corp_NDA_2026.pdf"

[Confidential Enterprise Contract Data]"""
        ),
        VulnerabilityReport(
            id = 0,
            title = "Stored Cross-Site Scripting (XSS) in User Profile Bio",
            targetAsset = "app.target.com",
            vulnerabilityType = "Cross-Site Scripting (Stored)",
            cweId = "CWE-79",
            cvssVector = CvssVector(
                attackVector = AttackVector.NETWORK,
                attackComplexity = AttackComplexity.LOW,
                privilegesRequired = PrivilegesRequired.LOW,
                userInteraction = UserInteraction.REQUIRED,
                scope = Scope.CHANGED,
                confidentiality = ImpactMetric.LOW,
                integrity = ImpactMetric.LOW,
                availability = ImpactMetric.NONE
            ),
            cvssScore = 5.4,
            severity = SeverityLevel.MEDIUM,
            summary = "Unsanitized user bio allows arbitrary JavaScript execution in the browser context of any user viewing the public profile.",
            technicalDescription = "The user bio field accepts unencoded HTML tags. When viewed by other users at `https://app.target.com/users/{id}`, the content is rendered into the DOM without HTML entity escaping.",
            stepsToReproduce = """1. Navigate to Profile Settings.
2. Insert payload into Bio: `<img src=x onerror=alert(document.domain)>`.
3. Save changes.
4. As another user, browse to the public profile URL.
5. The alert modal pops up demonstrating JavaScript execution.""",
            businessImpact = "Attackers can perform actions on behalf of visiting users, hijack sessions lacking HttpOnly flags, or inject phishing prompts.",
            remediation = "Apply context-aware HTML entity encoding on all user-submitted profile fields before rendering into HTML templates, and enforce a strict Content Security Policy (CSP).",
            httpProofOfConcept = """POST /api/v1/profile HTTP/1.1
Host: app.target.com
Content-Type: application/json
Cookie: session=xyz

{"bio": "<img src=x onerror=alert(document.domain)>"}"""
        )
    )
}
