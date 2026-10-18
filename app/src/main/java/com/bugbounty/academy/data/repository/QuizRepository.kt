package com.bugbounty.academy.data.repository

import com.bugbounty.academy.data.local.dao.QuizResultDao
import com.bugbounty.academy.data.local.entity.QuizResultEntity
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.domain.model.Quiz
import com.bugbounty.academy.domain.model.QuizOption
import com.bugbounty.academy.domain.model.QuizQuestion
import com.bugbounty.academy.domain.model.QuizSubmissionResult
import kotlinx.coroutines.flow.Flow

class QuizRepository(
    private val quizResultDao: QuizResultDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val quizzesList: List<Quiz> = listOf(
        Quiz(
            id = "quiz_http_fundamentals",
            moduleId = "mod_http_fundamentals",
            title = "HTTP Protocol & Web Foundations Quiz",
            description = "Test your knowledge on HTTP methods, headers, Same-Origin Policy, CORS, and cookie security flags.",
            passPercentage = 70,
            questions = listOf(
                QuizQuestion(
                    id = "q_http_1",
                    question = "Which HTTP method is defined by RFC 9110 as safe and idempotent, meaning it must never alter server-side state?",
                    options = listOf(
                        QuizOption("opt_1a", "POST", false),
                        QuizOption("opt_1b", "GET", true),
                        QuizOption("opt_1c", "PATCH", false),
                        QuizOption("opt_1d", "PUT", false)
                    ),
                    explanation = "GET is designated as both safe and idempotent. POST creates resources (non-idempotent), while PUT and DELETE modify or remove resources.",
                    referenceTag = "RFC 9110 Section 9.2.1"
                ),
                QuizQuestion(
                    id = "q_http_2",
                    question = "Under the browser's Same-Origin Policy (SOP), which two URLs share the exact same origin?",
                    options = listOf(
                        QuizOption("opt_2a", "http://example.com:80 and https://example.com:443", false),
                        QuizOption("opt_2b", "https://api.example.com and https://example.com", false),
                        QuizOption("opt_2c", "https://example.com:443/app and https://example.com:443/login", true),
                        QuizOption("opt_2d", "https://example.com:8080 and https://example.com:80", false)
                    ),
                    explanation = "An origin is determined strictly by the tuple (scheme, host, port). Paths do not affect origin comparison.",
                    referenceTag = "RFC 6454 (The Web Origin Concept)"
                ),
                QuizQuestion(
                    id = "q_http_3",
                    question = "What is the primary security benefit of setting the 'HttpOnly' flag on a session cookie?",
                    options = listOf(
                        QuizOption("opt_3a", "It forces transmission exclusively over HTTPS.", false),
                        QuizOption("opt_3b", "It prevents client-side scripts (JavaScript) from accessing document.cookie.", true),
                        QuizOption("opt_3c", "It encrypts the cookie payload on the client machine.", false),
                        QuizOption("opt_3d", "It automatically invalidates the cookie upon browser tab closure.", false)
                    ),
                    explanation = "HttpOnly blocks JavaScript from reading document.cookie, mitigating session token theft via Cross-Site Scripting (XSS). The 'Secure' flag enforces HTTPS.",
                    referenceTag = "OWASP Session Management Cheat Sheet"
                ),
                QuizQuestion(
                    id = "q_http_4",
                    question = "When is a CORS configuration considered critically vulnerable to cross-origin data theft?",
                    options = listOf(
                        QuizOption("opt_4a", "When Access-Control-Allow-Origin is set to '*' for public static assets.", false),
                        QuizOption("opt_4b", "When the server dynamically reflects the incoming Origin and sets Access-Control-Allow-Credentials: true.", true),
                        QuizOption("opt_4c", "When Access-Control-Allow-Methods includes OPTIONS and HEAD.", false),
                        QuizOption("opt_4d", "When the Max-Age preflight cache header is set to 3600 seconds.", false)
                    ),
                    explanation = "Reflecting arbitrary Origins with Access-Control-Allow-Credentials: true allows rogue websites to issue authenticated credentialed requests and read private user responses.",
                    referenceTag = "CWE-942"
                )
            )
        ),
        Quiz(
            id = "quiz_owasp_core",
            moduleId = "mod_owasp_core",
            title = "OWASP Top 10 Core Vulnerabilities Quiz",
            description = "Validate your understanding of SQL Injection, Cross-Site Scripting, and Access Control flaws.",
            passPercentage = 75,
            questions = listOf(
                QuizQuestion(
                    id = "q_owasp_1",
                    question = "What is the definitive defense against SQL Injection in modern application development?",
                    options = listOf(
                        QuizOption("opt_o1a", "WAF regex filtering of single quotes and dashes", false),
                        QuizOption("opt_o1b", "Parameterized queries (PreparedStatements) or ORMs with parameterized binding", true),
                        QuizOption("opt_o1c", "Suppressing database error output to the client", false),
                        QuizOption("opt_o1d", "Restricting database user passwords to 16 characters", false)
                    ),
                    explanation = "Prepared statements ensure that the database engine treats input strictly as literal data rather than executable SQL syntax, rendering injection impossible.",
                    referenceTag = "CWE-89 / OWASP SQLi Cheat Sheet"
                ),
                QuizQuestion(
                    id = "q_owasp_2",
                    question = "In Stored (Persistent) XSS, where is the malicious payload permanently held before execution?",
                    options = listOf(
                        QuizOption("opt_o2a", "In the client's URL hash fragment", false),
                        QuizOption("opt_o2b", "In the backend database or server-side persistent datastore", true),
                        QuizOption("opt_o2c", "In the browser's volatile RAM memory", false),
                        QuizOption("opt_o2d", "In the DNS resolver cache", false)
                    ),
                    explanation = "Stored XSS payloads are stored permanently in the database (e.g. comments, usernames) and rendered to any victim visiting the affected resource.",
                    referenceTag = "CWE-79"
                ),
                QuizQuestion(
                    id = "q_owasp_3",
                    question = "Which header provides a powerful defense-in-depth barrier against XSS by restricting origins from which scripts can be loaded and executed?",
                    options = listOf(
                        QuizOption("opt_o3a", "X-Frame-Options: DENY", false),
                        QuizOption("opt_o3b", "Content-Security-Policy (CSP)", true),
                        QuizOption("opt_o3c", "X-Content-Type-Options: nosniff", false),
                        QuizOption("opt_o3d", "Strict-Transport-Security", false)
                    ),
                    explanation = "Content-Security-Policy (CSP) instructs the browser to restrict script execution, nonces, and network connections to authorized sources only.",
                    referenceTag = "W3C CSP Level 3"
                ),
                QuizQuestion(
                    id = "q_owasp_4",
                    question = "An attacker notices that changing `/api/v1/invoices/100` to `/api/v1/invoices/101` downloads another customer's invoice. What vulnerability is this?",
                    options = listOf(
                        QuizOption("opt_o4a", "Server-Side Request Forgery (SSRF)", false),
                        QuizOption("opt_o4b", "Insecure Direct Object Reference (IDOR) / BOLA", true),
                        QuizOption("opt_o4c", "Cross-Site Request Forgery (CSRF)", false),
                        QuizOption("opt_o4d", "XML External Entity (XXE)", false)
                    ),
                    explanation = "IDOR (or BOLA) occurs when an application exposes a reference to an internal object without verifying that the requesting user owns that object.",
                    referenceTag = "CWE-639 / OWASP API1:2023"
                )
            )
        ),
        Quiz(
            id = "quiz_api_security",
            moduleId = "mod_api_security",
            title = "API Security & Microservices Quiz",
            description = "Assess your skills on Mass Assignment, Broken Object Level Authorization, and API rate limiting.",
            passPercentage = 75,
            questions = listOf(
                QuizQuestion(
                    id = "q_api_1",
                    question = "What causes Mass Assignment vulnerabilities in modern web frameworks?",
                    options = listOf(
                        QuizOption("opt_a1a", "Failing to enable SSL/TLS on API endpoints", false),
                        QuizOption("opt_a1b", "Automatically deserializing and binding request JSON keys directly into internal domain models or database entities", true),
                        QuizOption("opt_a1c", "Using REST instead of GraphQL", false),
                        QuizOption("opt_a1d", "Exceeding the maximum payload size limit in HTTP headers", false)
                    ),
                    explanation = "Mass Assignment occurs when frameworks auto-bind user-controlled request properties (e.g. is_admin, role) to domain entities without filtering via strict DTO allowlists.",
                    referenceTag = "CWE-915 / OWASP API3:2023"
                ),
                QuizQuestion(
                    id = "q_api_2",
                    question = "Why is relying solely on client IP addresses (e.g. from X-Forwarded-For) dangerous for API rate limiting?",
                    options = listOf(
                        QuizOption("opt_a2a", "IPv6 addresses cannot be hashed efficiently", false),
                        QuizOption("opt_a2b", "Attackers can spoof untrusted upstream headers like X-Forwarded-For or Client-IP to rotate perceived IPs", true),
                        QuizOption("opt_a2c", "DNS lookups are blocked by standard web browsers", false),
                        QuizOption("opt_a2d", "TCP handshakes require client certificates", false)
                    ),
                    explanation = "Unless the edge reverse proxy explicitly strips and rewrites incoming proxy headers, attackers can spoof arbitrary IP headers to evade rate limits.",
                    referenceTag = "CWE-799"
                )
            )
        ),
        Quiz(
            id = "quiz_oauth_jwt",
            moduleId = "mod_auth_oauth_jwt",
            title = "OAuth 2.0 & JWT Security Quiz",
            description = "Test your understanding of JWT signature verification, algorithm confusion, and OAuth authorization code flows.",
            passPercentage = 75,
            questions = listOf(
                QuizQuestion(
                    id = "q_jwt_1",
                    question = "What is the security flaw associated with the JWT 'none' algorithm?",
                    options = listOf(
                        QuizOption("opt_j1a", "It prevents tokens from expiring properly.", false),
                        QuizOption("opt_j1b", "Vulnerable libraries accept unsigned tokens without verifying any cryptographic signature.", true),
                        QuizOption("opt_j1c", "It reveals the private RSA key in plaintext.", false),
                        QuizOption("opt_j1d", "It forces symmetric encryption instead of asymmetric signatures.", false)
                    ),
                    explanation = "RFC 7519 defines 'none' for unsecured tokens; flawed verifiers that do not whitelist expected algorithms accept forged tokens with empty signatures.",
                    referenceTag = "RFC 7519 Section 6"
                ),
                QuizQuestion(
                    id = "q_jwt_2",
                    question = "In an OAuth 2.0 authorization code flow, what is the crucial purpose of the 'state' parameter?",
                    options = listOf(
                        QuizOption("opt_j2a", "To compress the token payload for mobile bandwidth", false),
                        QuizOption("opt_j2b", "To bind the authorization request to the user's browser session, preventing Cross-Site Request Forgery (CSRF)", true),
                        QuizOption("opt_j2c", "To store the client's database password securely", false),
                        QuizOption("opt_j2d", "To encrypt the authorization code using AES-GCM", false)
                    ),
                    explanation = "The state parameter acts as an anti-CSRF token that binds the client request to the IdP callback, thwarting account takeover attacks.",
                    referenceTag = "RFC 6749 Section 10.12"
                )
            )
        ),
        Quiz(
            id = "quiz_business_logic",
            moduleId = "mod_business_logic_race",
            title = "Business Logic & Concurrency Quiz",
            description = "Evaluate your comprehension of TOCTOU race conditions and transactional workflows.",
            passPercentage = 70,
            questions = listOf(
                QuizQuestion(
                    id = "q_bl_1",
                    question = "What does TOCTOU stand for in the context of race conditions?",
                    options = listOf(
                        QuizOption("opt_bl1a", "Time-Of-Check to Time-Of-Use", true),
                        QuizOption("opt_bl1b", "Transaction-Order-Control to Operation-Under-Use", false),
                        QuizOption("opt_bl1c", "Transfer-Origin-Certificate to Token-User-Unit", false),
                        QuizOption("opt_bl1d", "Thread-Overload-Check to Timeout-Utilization", false)
                    ),
                    explanation = "TOCTOU describes the window between inspecting a resource condition (e.g. checking balance) and executing the state change (e.g. withdrawing).",
                    referenceTag = "CWE-362"
                ),
                QuizQuestion(
                    id = "q_bl_2",
                    question = "How can a developer prevent race conditions during account balance deductions?",
                    options = listOf(
                        QuizOption("opt_bl2a", "Using HTTP Keep-Alive connections", false),
                        QuizOption("opt_bl2b", "Using atomic database updates or pessimistic row locking (SELECT ... FOR UPDATE)", true),
                        QuizOption("opt_bl2c", "Adding client-side debouncing on the submit button", false),
                        QuizOption("opt_bl2d", "Storing account balances inside browser localStorage", false)
                    ),
                    explanation = "Atomic database operations (e.g. UPDATE Wallet SET balance = balance - 100 WHERE balance >= 100) and row locks prevent parallel execution threads from double-spending.",
                    referenceTag = "OWASP Concurrency Cheat Sheet"
                )
            )
        ),
        Quiz(
            id = "quiz_ethics_reporting",
            moduleId = "mod_ethics_reporting",
            title = "Responsible Disclosure & Reporting Quiz",
            description = "Test your understanding of bug bounty rules of engagement, Safe Harbor, and report quality.",
            passPercentage = 75,
            questions = listOf(
                QuizQuestion(
                    id = "q_eth_1",
                    question = "Under standard Bug Bounty Safe Harbor policies, what should you do immediately if you uncover access to sensitive user PII?",
                    options = listOf(
                        QuizOption("opt_e1a", "Download the entire database to prove the maximum possible business impact.", false),
                        QuizOption("opt_e1b", "Stop testing immediately, document minimal evidence showing the flaw exists, and report it responsibly.", true),
                        QuizOption("opt_e1c", "Post screenshots on social media to prompt the security team to act quickly.", false),
                        QuizOption("opt_e1d", "Demand a specific ransom bounty before disclosing the vulnerability location.", false)
                    ),
                    explanation = "Safe Harbor requires good-faith security researchers to stop immediately once access is demonstrated, minimizing data exposure.",
                    referenceTag = "HackerOne Safe Harbor Policy"
                ),
                QuizQuestion(
                    id = "q_eth_2",
                    question = "Which CVSS v3.1 metric evaluates whether an attacker requires prior authentication or system credentials to trigger a vulnerability?",
                    options = listOf(
                        QuizOption("opt_e2a", "Attack Vector (AV)", false),
                        QuizOption("opt_e2b", "Attack Complexity (AC)", false),
                        QuizOption("opt_e2c", "Privileges Required (PR)", true),
                        QuizOption("opt_e2d", "User Interaction (UI)", false)
                    ),
                    explanation = "Privileges Required (PR: None, Low, High) captures the level of access an attacker must possess prior to successfully exploiting the vulnerability.",
                    referenceTag = "FIRST CVSS v3.1 Specification"
                )
            )
        )
    )

    fun getAllQuizzes(): List<Quiz> = quizzesList

    fun getQuizById(quizId: String): Quiz? = quizzesList.firstOrNull { it.id == quizId }

    fun observeAllResults(): Flow<List<QuizResultEntity>> = quizResultDao.getAllQuizResults()

    fun observeQuizResult(quizId: String): Flow<QuizResultEntity?> = quizResultDao.observeQuizResult(quizId)

    fun getPassedQuizzesCount(): Flow<Int> = quizResultDao.getPassedQuizzesCount()

    suspend fun submitQuiz(quizId: String, selectedAnswers: Map<String, String>): QuizSubmissionResult {
        val quiz = getQuizById(quizId) ?: throw IllegalArgumentException("Quiz not found")
        var correctCount = 0

        for (q in quiz.questions) {
            val selectedOptionId = selectedAnswers[q.id]
            val correctOption = q.options.firstOrNull { it.isCorrect }
            if (selectedOptionId != null && correctOption?.id == selectedOptionId) {
                correctCount++
            }
        }

        val total = quiz.questions.size
        val percentage = if (total > 0) (correctCount * 100) / total else 0
        val isPassed = percentage >= quiz.passPercentage
        val xpEarned = if (isPassed) 80 else 20

        val existing = quizResultDao.getQuizResult(quizId)
        val bestPercent = maxOf(existing?.bestPercentage ?: 0, percentage)
        val attempts = (existing?.attempts ?: 0) + 1

        quizResultDao.insertOrUpdate(
            QuizResultEntity(
                quizId = quizId,
                score = correctCount,
                total = total,
                isPassed = isPassed || (existing?.isPassed == true),
                bestPercentage = bestPercent,
                attempts = attempts,
                completedAt = System.currentTimeMillis()
            )
        )

        if (isPassed && existing?.isPassed != true) {
            userPreferencesDataStore.addXp(xpEarned)
        }
        userPreferencesDataStore.recordActivityToday()

        return QuizSubmissionResult(
            quizId = quizId,
            score = correctCount,
            total = total,
            percentage = percentage,
            isPassed = isPassed,
            answersMap = selectedAnswers,
            xpEarned = xpEarned
        )
    }
}
