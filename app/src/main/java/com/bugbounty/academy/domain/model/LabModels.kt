package com.bugbounty.academy.domain.model

enum class LabDifficulty(val label: String, val xpReward: Int) {
    BEGINNER("Beginner", 50),
    INTERMEDIATE("Intermediate", 100),
    ADVANCED("Advanced", 175),
    EXPERT("Expert", 250)
}

enum class LabCategory(val displayName: String) {
    INJECTION("Injection Attacks"),
    XSS_AND_CLIENT("XSS & Client-Side"),
    ACCESS_CONTROL("Access Control & IDOR"),
    API_SECURITY("API & Microservices"),
    AUTH_AND_TOKENS("Authentication, OAuth & JWT"),
    SERVER_SIDE("Server-Side (SSRF, XXE, Traversal)"),
    BUSINESS_LOGIC("Business Logic & Concurrency"),
    SECURE_CODE_REVIEW("Secure Code Review & Recon")
}

data class Lab(
    val id: String,
    val title: String,
    val category: LabCategory,
    val difficulty: LabDifficulty,
    val vulnerabilityType: String,
    val cwe: String,
    val description: String,
    val scenarioContext: String,
    val simulatedEndpoint: String,
    val defaultMethod: String = "GET",
    val defaultHeaders: Map<String, String> = mapOf("Content-Type" to "application/json"),
    val defaultBody: String = "",
    val defaultQueryParams: Map<String, String> = emptyMap(),
    val hints: List<String>,
    val flag: String,
    val learningObjectives: List<String>,
    val remediationCodeSnippet: String,
    val remediationExplanation: String
)

data class LabExecutionResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val statusText: String,
    val responseHeaders: Map<String, String>,
    val responseBody: String,
    val executionLog: List<String>,
    val flagAwarded: String? = null,
    val vulnerabilityTriggered: Boolean = false,
    val feedbackMessage: String
)
