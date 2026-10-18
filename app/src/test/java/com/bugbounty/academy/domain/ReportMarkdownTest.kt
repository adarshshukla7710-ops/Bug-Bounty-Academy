package com.bugbounty.academy.domain

import com.bugbounty.academy.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class ReportMarkdownTest {

    @Test
    fun testGenerateMarkdownFormatting() {
        val report = VulnerabilityReport(
            id = 1,
            title = "Test BOLA in API Documents",
            targetAsset = "api.target.com",
            vulnerabilityType = "Broken Object Level Authorization",
            cweId = "CWE-639",
            cvssVector = CvssVector(),
            cvssScore = 7.5,
            severity = SeverityLevel.HIGH,
            summary = "Summary text",
            technicalDescription = "Technical details",
            stepsToReproduce = "1. Step one\n2. Step two",
            businessImpact = "High financial impact",
            remediation = "Enforce tenant ownership",
            httpProofOfConcept = "GET /api/v1/doc HTTP/1.1"
        )

        val markdown = report.generateMarkdown()

        assertTrue(markdown.contains("# Vulnerability Report: Test BOLA in API Documents"))
        assertTrue(markdown.contains("| **Target Asset** | `api.target.com` |"))
        assertTrue(markdown.contains("| **Severity** | **HIGH** (7.5) |"))
        assertTrue(markdown.contains("## 3. Steps to Reproduce (PoC)"))
        assertTrue(markdown.contains("```http\nGET /api/v1/doc HTTP/1.1\n```"))
    }
}
