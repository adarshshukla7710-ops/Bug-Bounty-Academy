package com.bugbounty.academy.domain.engine

import com.bugbounty.academy.domain.model.LabExecutionResult
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object SafeDeterministicLabEngine {

    fun execute(
        labId: String,
        method: String,
        endpoint: String,
        headers: Map<String, String>,
        body: String,
        queryParams: Map<String, String>
    ): LabExecutionResult {
        return try {
            when (labId) {
                // Category 1: Injection
                "lab_sqli_auth_bypass" -> evaluateSqliAuthBypass(method, body)
                "lab_sqli_union" -> evaluateSqliUnion(endpoint, queryParams)
                "lab_sqli_blind_boolean" -> evaluateSqliBlind(endpoint, queryParams)
                "lab_command_injection_ping" -> evaluateCommandInjection(method, body)

                // Category 2: XSS & Client
                "lab_stored_xss" -> evaluateStoredXss(method, body)
                "lab_reflected_xss_filter_bypass" -> evaluateReflectedXss(endpoint, queryParams)
                "lab_dom_xss_hash" -> evaluateDomXss(body)
                "lab_csrf_account_takeover" -> evaluateCsrf(headers, body)

                // Category 3: Access Control
                "lab_idor_horizontal" -> evaluateIdorHorizontal(endpoint, headers)
                "lab_idor_vertical" -> evaluateIdorVertical(method, body)
                "lab_bfla_admin_endpoint" -> evaluateBfla(endpoint, headers)
                "lab_bola_document_vault" -> evaluateBola(endpoint)

                // Category 4: API Security
                "lab_mass_assignment_registration" -> evaluateMassAssignment(method, body)
                "lab_rate_limit_header_spoof" -> evaluateRateLimitBypass(headers)
                "lab_api_versioning_unprotected" -> evaluateApiVersioning(endpoint)
                "lab_cors_misconfiguration" -> evaluateCors(headers)

                // Category 5: Auth & Tokens
                "lab_jwt_none_algorithm" -> evaluateJwtNone(headers)
                "lab_jwt_weak_secret" -> evaluateJwtWeakSecret(headers)
                "lab_oauth_missing_state" -> evaluateOAuthMissingState(endpoint, queryParams)
                "lab_oauth_open_redirect" -> evaluateOAuthOpenRedirect(endpoint, queryParams)

                // Category 6: Server-Side
                "lab_ssrf_cloud_metadata" -> evaluateSsrfCloudMetadata(method, body)
                "lab_ssrf_dns_rebinding_bypass" -> evaluateSsrfDnsRebind(method, body)
                "lab_path_traversal_lfi" -> evaluatePathTraversal(endpoint, queryParams)
                "lab_xxe_external_entity" -> evaluateXxe(method, body)
                "lab_file_upload_extension_bypass" -> evaluateFileUpload(headers, body)
                "lab_info_disclosure_git_head" -> evaluateGitDisclosure(endpoint)

                // Category 7: Business Logic & Concurrency
                "lab_logic_price_tampering" -> evaluatePriceTampering(method, body)
                "lab_logic_coupon_stacking" -> evaluateCouponStacking(method, body)
                "lab_race_condition_double_spend" -> evaluateRaceCondition(method, body)

                // Category 8: Secure Code Review & GraphQL
                "lab_code_review_sqli_sink" -> evaluateCodeReviewSqli(body)
                "lab_code_review_crypto_salt" -> evaluateCodeReviewCrypto(body)
                "lab_graphql_introspection_abuse" -> evaluateGraphQLIntrospection(method, body)

                else -> LabExecutionResult(
                    isSuccess = false,
                    statusCode = 404,
                    statusText = "Not Found",
                    responseHeaders = mapOf("Content-Type" to "application/json"),
                    responseBody = "{\"error\": \"Target lab simulation '$labId' not found\"}",
                    executionLog = listOf("[!] Unknown lab ID"),
                    feedbackMessage = "The selected lab simulation is not registered."
                )
            }
        } catch (e: Exception) {
            LabExecutionResult(
                isSuccess = false,
                statusCode = 500,
                statusText = "Internal Simulation Error",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"error\": \"${e.localizedMessage}\"}",
                executionLog = listOf("[ERR] Parser exception: ${e.message}"),
                feedbackMessage = "An error occurred during local lab payload parsing."
            )
        }
    }

    // 1. SQL Injection: Auth Bypass
    private fun evaluateSqliAuthBypass(method: String, body: String): LabExecutionResult {
        if (!method.equals("POST", ignoreCase = true)) {
            return methodNotAllowed("POST")
        }
        val logs = mutableListOf<String>()
        logs.add("[*] Received HTTP POST /api/v1/auth/login")
        logs.add("[*] Parsing credentials from JSON payload")

        val normalized = body.replace("\\s+".toRegex(), " ")
        val isBypass = normalized.contains("' OR '1'='1", ignoreCase = true) ||
                normalized.contains("' or 1=1", ignoreCase = true) ||
                normalized.contains("admin' --", ignoreCase = true) ||
                normalized.contains("admin' #", ignoreCase = true) ||
                normalized.contains("' OR ''='", ignoreCase = true)

        if (isBypass) {
            logs.add("[+] Constructed SQL: SELECT * FROM users WHERE username = 'admin' OR '1'='1' AND password = '...'")
            logs.add("[+] Query evaluated to TRUE for all rows! Authenticating as first user (administrator).")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Set-Cookie" to "session=admin_sess_902183; Path=/; HttpOnly"
                ),
                responseBody = """{
  "status": "authenticated",
  "user": "administrator",
  "role": "SYSTEM_ADMIN",
  "secret_flag": "FLAG{sqli_auth_bypass_pwned_7391}"
}""".trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{sqli_auth_bypass_pwned_7391}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Success! Authentication bypassed via unescaped SQL tautology injection."
            )
        } else {
            logs.add("[-] Constructed SQL: SELECT * FROM users WHERE username = '...' AND password = '...'")
            logs.add("[-] 0 records returned. Invalid credentials.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 401,
                statusText = "Unauthorized",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"error\": \"Invalid username or password\"}",
                executionLog = logs,
                feedbackMessage = "Invalid credentials. Try injecting an SQL condition that always evaluates to TRUE."
            )
        }
    }

    // 2. SQL Injection: UNION
    private fun evaluateSqliUnion(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val cat = queryParams["category"] ?: endpoint.substringAfter("category=", "")
        val decoded = URLDecoder.decode(cat, StandardCharsets.UTF_8.name())
        val logs = mutableListOf("[*] Executing: SELECT id, name, price, description FROM products WHERE category = '$decoded'")

        val isUnion = decoded.contains("UNION", ignoreCase = true) &&
                decoded.contains("SELECT", ignoreCase = true)

        if (isUnion && decoded.contains("users", ignoreCase = true)) {
            logs.add("[+] UNION SELECT matches 4 columns! Appending rows from 'users' table.")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """[
  {"id": 1, "name": "Standard Tee", "price": 19.99, "description": "Cotton t-shirt"},
  {"id": 999, "name": "admin", "price": 0.0, "description": "hash:${'$'}2a${'$'}12${'$'}N9qo8uLOickgx2ZMRZoMye (FLAG{sqli_union_users_extracted_9124})"}
]""".trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{sqli_union_users_extracted_9124}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Success! Extracted database records from 'users' table using UNION SELECT."
            )
        } else if (isUnion) {
            logs.add("[-] UNION column count or table target mismatch.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 500,
                statusText = "Internal Server Error",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"error\": \"The used SELECT statements have a different number of columns or table was not found.\"}",
                executionLog = logs,
                feedbackMessage = "UNION injection detected but failed. Check column count (4 columns) and target table ('users')."
            )
        } else {
            logs.add("[*] Standard catalog lookup returned 1 item.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """[{"id": 1, "name": "Standard Tee", "price": 19.99, "description": "Cotton t-shirt"}]""",
                executionLog = logs,
                feedbackMessage = "Standard products returned. Inject a UNION SELECT query to dump sensitive records."
            )
        }
    }

    // 3. Blind SQLi
    private fun evaluateSqliBlind(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val id = queryParams["id"] ?: endpoint.substringAfter("id=", "")
        val decoded = URLDecoder.decode(id, StandardCharsets.UTF_8.name())
        val logs = mutableListOf("[*] Tracking ID query: '$decoded'")

        if (decoded.contains("SUBSTRING", ignoreCase = true) || decoded.contains("ASCII", ignoreCase = true) || decoded.contains("1=1", ignoreCase = true)) {
            logs.add("[+] Boolean condition evaluated to TRUE. Returning positive indicator.")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """{"status": "Package In Transit", "details": "Tracking active", "flag": "FLAG{sqli_blind_bool_inference_5519}"}""",
                executionLog = logs,
                flagAwarded = "FLAG{sqli_blind_bool_inference_5519}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Boolean condition TRUE inferred! In a real blind scenario, you iterate character by character."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = """{"status": "Package In Transit", "details": "Normal tracking record"}""",
            executionLog = logs,
            feedbackMessage = "Standard tracking query. Inject a boolean conditional inference payload."
        )
    }

    // 4. Command Injection
    private fun evaluateCommandInjection(method: String, body: String): LabExecutionResult {
        if (!method.equals("POST", ignoreCase = true)) return methodNotAllowed("POST")
        val logs = mutableListOf("[*] Executing system diagnostic ping script")
        val isEscaped = body.contains(";") || body.contains("&&") || body.contains("|") || body.contains("$")

        if (isEscaped && (body.contains("cat", ignoreCase = true) || body.contains("whoami", ignoreCase = true) || body.contains("id", ignoreCase = true) || body.contains("passwd", ignoreCase = true))) {
            logs.add("[+] Shell metacharacter executed secondary command!")
            logs.add("[+] Output captured from subshell stdout")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "text/plain"),
                responseBody = """
PING 8.8.8.8 (8.8.8.8): 56 data bytes
64 bytes from 8.8.8.8: icmp_seq=0 ttl=118 time=14.2 ms

root:x:0:0:root:/root:/bin/bash
daemon:x:1:1:daemon:/usr/sbin:/usr/sbin/nologin
secret:x:1000:1000:FLAG{cmd_injection_pipe_escape_4821}:/home/secret:/bin/sh
                """.trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{cmd_injection_pipe_escape_4821}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Command injection confirmed! The backend executed unvalidated shell input."
            )
        }
        logs.add("[*] Standard ping executed.")
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "text/plain"),
            responseBody = "PING 8.8.8.8 (8.8.8.8): 56 data bytes\n64 bytes from 8.8.8.8: icmp_seq=0 ttl=118 time=14.2 ms",
            executionLog = logs,
            feedbackMessage = "Valid ping format. Try appending shell operators (;, &&, |) with a command like 'cat /etc/passwd'."
        )
    }

    // 5. Stored XSS
    private fun evaluateStoredXss(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Storing user comment in database")
        val isXss = body.contains("<script>", ignoreCase = true) ||
                body.contains("<img", ignoreCase = true) && body.contains("onerror", ignoreCase = true) ||
                body.contains("<svg", ignoreCase = true) && body.contains("onload", ignoreCase = true)

        if (isXss) {
            logs.add("[+] Payload saved raw into comment HTML rendering block")
            logs.add("[+] Simulated victim browser loaded page and triggered JavaScript execution context!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 201,
                statusText = "Created",
                responseHeaders = mapOf("Content-Type" to "text/html"),
                responseBody = "<div><p>Comment stored: $body</p><div id='xss-alert' data-flag='FLAG{stored_xss_comment_stealer_3840}'>XSS Triggered</div></div>",
                executionLog = logs,
                flagAwarded = "FLAG{stored_xss_comment_stealer_3840}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Stored XSS successful! The input was rendered into the HTML document without HTML entity escaping."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 201,
            statusText = "Created",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"comment_saved\", \"content\": \"safe text\"}",
            executionLog = logs,
            feedbackMessage = "Comment saved. Submit an unescaped HTML/JavaScript payload such as <script> or <img onerror=>."
        )
    }

    // 6. Reflected XSS (Filter Bypass)
    private fun evaluateReflectedXss(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val q = queryParams["q"] ?: endpoint.substringAfter("q=", "")
        val decoded = URLDecoder.decode(q, StandardCharsets.UTF_8.name())
        val logs = mutableListOf("[*] Evaluating search query: '$decoded'")

        // WAF blocks exact "<script>"
        if (decoded.contains("<script>", ignoreCase = false)) {
            logs.add("[-] WAF blocked lowercase <script> tag.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 403,
                statusText = "Forbidden",
                responseHeaders = mapOf("Content-Type" to "text/plain"),
                responseBody = "WAF Alert: Tag <script> is forbidden.",
                executionLog = logs,
                feedbackMessage = "WAF blocked '<script>'. Bypass the filter using mixed case or alternative event handlers like <img onerror=> or <svg onload=>."
            )
        }

        val isBypass = decoded.contains("<sCrIpt>", ignoreCase = false) ||
                (decoded.contains("<img", ignoreCase = true) && decoded.contains("onerror", ignoreCase = true)) ||
                (decoded.contains("<svg", ignoreCase = true) && decoded.contains("onload", ignoreCase = true))

        if (isBypass) {
            logs.add("[+] Filter bypassed! Payload reflected raw into response body.")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "text/html"),
                responseBody = "<html><body><h1>Search results for: $decoded</h1><!-- FLAG{reflected_xss_mixed_case_bypass_6192} --></body></html>",
                executionLog = logs,
                flagAwarded = "FLAG{reflected_xss_mixed_case_bypass_6192}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Filter bypass successful! Event handler / mixed case bypassed naive WAF blacklist."
            )
        }

        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "text/html"),
            responseBody = "<html><body><h1>Search results for: $decoded</h1><p>0 results found</p></body></html>",
            executionLog = logs,
            feedbackMessage = "Input reflected safely. Inject an executable XSS payload that evades the basic blacklist."
        )
    }

    // 7. DOM XSS
    private fun evaluateDomXss(body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Checking DOM sink execution for input: '$body'")
        if (body.contains("innerHTML", ignoreCase = true) || body.contains("location.hash", ignoreCase = true) || body.contains("<img", ignoreCase = true)) {
            logs.add("[+] Unsafe client-side assignment to innerHTML sink detected!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"execution\": \"DOM sink triggered\", \"flag\": \"FLAG{dom_xss_innerhtml_sink_2041}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{dom_xss_innerhtml_sink_2041}",
                vulnerabilityTriggered = true,
                feedbackMessage = "DOM XSS verified! The client script assigned tainted input directly into an execution sink."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"rendered via textContent\"}",
            executionLog = logs,
            feedbackMessage = "No dangerous DOM sink reached. Target the innerHTML or eval sink."
        )
    }

    // 8. CSRF
    private fun evaluateCsrf(headers: Map<String, String>, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Processing state-changing email change request")
        val hasCsrfToken = headers.keys.any { it.contains("csrf", ignoreCase = true) } || body.contains("csrf", ignoreCase = true)
        val origin = headers["Origin"] ?: headers["Referer"]

        if (!hasCsrfToken && (origin == null || origin.contains("attacker.com") || origin.contains("evil.com"))) {
            logs.add("[+] No CSRF token found in headers or body!")
            logs.add("[+] Cross-site Origin accepted without rejection. Email updated to attacker address.")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"status\": \"email_updated\", \"new_email\": \"attacker@evil.com\", \"flag\": \"FLAG{csrf_state_change_takeover_8820}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{csrf_state_change_takeover_8820}",
                vulnerabilityTriggered = true,
                feedbackMessage = "CSRF exploited! The state-changing action lacks CSRF tokens and SameSite cookie protections."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 400,
            statusText = "Bad Request",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Include target body and remove token or set Origin to external domain\"}",
            executionLog = logs,
            feedbackMessage = "To simulate CSRF, omit CSRF tokens and set Origin/Referer to an external domain."
        )
    }

    // 9. IDOR Horizontal
    private fun evaluateIdorHorizontal(endpoint: String, headers: Map<String, String>): LabExecutionResult {
        val logs = mutableListOf("[*] Session user: 1041 (Standard User)")
        val targetId = endpoint.replace("[^0-9]".toRegex(), "")
        logs.add("[*] Requesting resource ID: $targetId")

        if (targetId.isNotEmpty() && targetId != "1041") {
            logs.add("[+] Broken authorization: Server did not verify if session user 1041 owns profile $targetId!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """{
  "user_id": $targetId,
  "name": "Sarah Connor",
  "ssn": "***-**-9982",
  "credit_card": "4111-XXXX-XXXX-9021",
  "secret_note": "FLAG{idor_horizontal_account_peek_5139}"
}""".trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{idor_horizontal_account_peek_5139}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Horizontal IDOR confirmed! You accessed another user's private data simply by altering the resource identifier."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"user_id\": 1041, \"name\": \"Your Profile\"}",
            executionLog = logs,
            feedbackMessage = "Viewing your own profile. Change the user ID in the endpoint URL to view another user's data."
        )
    }

    // 10. IDOR Vertical (Role tampering)
    private fun evaluateIdorVertical(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Updating current user profile")
        if (body.contains("\"role\":\\s*\"admin\"".toRegex()) || body.contains("\"is_admin\":\\s*true".toRegex())) {
            logs.add("[+] Server permitted role escalation without administrative authorization!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"status\": \"updated\", \"role\": \"admin\", \"privilege\": \"SYSTEM_ADMIN\", \"flag\": \"FLAG{idor_vertical_role_escalation_7412}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{idor_vertical_role_escalation_7412}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Vertical privilege escalation successful! Server-side failed to restrict role modification."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"updated\", \"role\": \"standard_user\"}",
            executionLog = logs,
            feedbackMessage = "Profile updated with normal role. Inject administrative fields like '\"role\": \"admin\"'."
        )
    }

    // 11. BFLA (Broken Function Level Auth)
    private fun evaluateBfla(endpoint: String, headers: Map<String, String>): LabExecutionResult {
        val logs = mutableListOf("[*] Accessing endpoint: $endpoint with standard user token")
        if (endpoint.contains("/admin/", ignoreCase = true) || endpoint.contains("export-users", ignoreCase = true)) {
            logs.add("[+] Administrative endpoint executed without checking role!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"admin_action\": \"export_database_dump\", \"flag\": \"FLAG{bfla_hidden_admin_export_1928}\", \"total_records\": 4200}",
                executionLog = logs,
                flagAwarded = "FLAG{bfla_hidden_admin_export_1928}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Broken Function Level Authorization confirmed! The admin endpoint failed to enforce RBAC."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 403,
            statusText = "Forbidden",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Standard function access\"}",
            executionLog = logs,
            feedbackMessage = "Try probing for hidden or unauthenticated administrative functions like /api/v1/admin/export-users."
        )
    }

    // 12. BOLA Document Vault
    private fun evaluateBola(endpoint: String): LabExecutionResult {
        val logs = mutableListOf("[*] Vault lookup: $endpoint")
        if (endpoint.contains("9842") || endpoint.contains("secret") || endpoint.contains("confidential")) {
            logs.add("[+] Unprotected document fetched across customer boundaries!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/pdf"),
                responseBody = "CONFIDENTIAL CONTRACT (Acme Corp) - FLAG{bola_unauthorized_document_leak_3391}",
                executionLog = logs,
                flagAwarded = "FLAG{bola_unauthorized_document_leak_3391}",
                vulnerabilityTriggered = true,
                feedbackMessage = "BOLA / IDOR confirmed! Insecure document ID allowed downloading cross-tenant file."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/pdf"),
            responseBody = "STANDARD PUBLIC DOCUMENT - Document ID 9840",
            executionLog = logs,
            feedbackMessage = "Standard document returned. Probe for adjacent or sensitive document IDs like 'doc-9842.pdf'."
        )
    }

    // 13. Mass Assignment
    private fun evaluateMassAssignment(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Registering new user account")
        if (body.contains("is_super_admin", ignoreCase = true) || body.contains("credits", ignoreCase = true)) {
            logs.add("[+] Model binder automatically populated un-whitelisted domain entity properties!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 201,
                statusText = "Created",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"status\": \"registered\", \"is_super_admin\": true, \"credits\": 99999, \"flag\": \"FLAG{mass_assignment_superuser_pwned_8264}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{mass_assignment_superuser_pwned_8264}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Mass Assignment verified! The endpoint automatically bound request JSON directly into privileged entity fields."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 201,
            statusText = "Created",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"registered\", \"role\": \"user\"}",
            executionLog = logs,
            feedbackMessage = "Standard account registered. Try adding unexpected parameters like 'is_super_admin': true or 'credits': 99999."
        )
    }

    // 14. Rate Limit Bypass (X-Forwarded-For)
    private fun evaluateRateLimitBypass(headers: Map<String, String>): LabExecutionResult {
        val logs = mutableListOf("[*] Evaluating rate limit for client IP")
        val xff = headers["X-Forwarded-For"] ?: headers["Client-IP"]
        if (!xff.isNullOrBlank() && xff != "127.0.0.1") {
            logs.add("[+] Server trusted spoofed client IP header '$xff' and reset attempt counter!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"status\": \"success\", \"password_reset_sent\": true, \"flag\": \"FLAG{rate_limit_xff_header_spoofed_4019}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{rate_limit_xff_header_spoofed_4019}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Rate limiting bypassed via IP spoofing headers! The reverse proxy failed to strip untrusted headers."
            )
        }
        logs.add("[-] Rate limit counter exceeded: 5 attempts from this IP.")
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 429,
            statusText = "Too Many Requests",
            responseHeaders = mapOf("Retry-After" to "600"),
            responseBody = "{\"error\": \"Too many password reset requests from your IP. Please try again later.\"}",
            executionLog = logs,
            feedbackMessage = "Rate limited (429). Inject spoofed IP headers like 'X-Forwarded-For: 10.0.0.X' to bypass."
        )
    }

    // 15. API Versioning
    private fun evaluateApiVersioning(endpoint: String): LabExecutionResult {
        val logs = mutableListOf("[*] Routing request: $endpoint")
        if (endpoint.contains("/api/v0.9/") || endpoint.contains("/api/v1-beta/")) {
            logs.add("[+] Shadow deprecated API reached! Authentication middleware was disabled on this endpoint.")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"deprecated_api\": true, \"customers\": [\"Alice\", \"Bob\", \"FLAG{api_shadow_v1_deprecated_leak_9041}\"]}",
                executionLog = logs,
                flagAwarded = "FLAG{api_shadow_v1_deprecated_leak_9041}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Shadow API discovered! The legacy version bypasses modern authentication requirements."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 401,
            statusText = "Unauthorized",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"OAuth2 Bearer token required for /api/v2\"}",
            executionLog = logs,
            feedbackMessage = "Target endpoint requires modern auth. Probe for unmaintained legacy versions like /api/v1-beta/."
        )
    }

    // 16. CORS Misconfiguration
    private fun evaluateCors(headers: Map<String, String>): LabExecutionResult {
        val origin = headers["Origin"] ?: ""
        val logs = mutableListOf("[*] CORS Preflight evaluation for Origin: '$origin'")
        if (origin.contains("evil") || origin.contains("attacker") || origin.contains("null")) {
            logs.add("[+] Vulnerable CORS configuration: Reflecting arbitrary Origin with credentials allowed!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf(
                    "Access-Control-Allow-Origin" to origin,
                    "Access-Control-Allow-Credentials" to "true",
                    "Content-Type" to "application/json"
                ),
                responseBody = "{\"api_key\": \"sec_live_9012384\", \"flag\": \"FLAG{cors_origin_reflection_stealer_7743}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{cors_origin_reflection_stealer_7743}",
                vulnerabilityTriggered = true,
                feedbackMessage = "CORS misconfiguration exploited! Reflecting attacker Origin with credentials enabled allows cross-origin data theft."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"healthy\"}",
            executionLog = logs,
            feedbackMessage = "Set an untrusted Origin header (e.g. 'Origin: https://evil-attacker.com') to test CORS reflection."
        )
    }

    // 17. JWT None Algorithm
    private fun evaluateJwtNone(headers: Map<String, String>): LabExecutionResult {
        val auth = headers["Authorization"] ?: ""
        val logs = mutableListOf("[*] Verifying JWT token signature from Authorization header")

        if (auth.contains("eyJ", ignoreCase = false)) {
            val token = auth.removePrefix("Bearer ").trim()
            val parts = token.split(".")
            if (parts.size >= 2) {
                val headerJson = try {
                    val cleanHeader = parts[0].padEnd((parts[0].length + 3) / 4 * 4, '=')
                    String(java.util.Base64.getUrlDecoder().decode(cleanHeader), StandardCharsets.UTF_8)
                } catch (e: Exception) {
                    try {
                        String(java.util.Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8)
                    } catch (e2: Exception) {
                        parts[0]
                    }
                }

                if (headerJson.contains("\"alg\":\\s*\"none\"".toRegex(RegexOption.IGNORE_CASE))) {
                    logs.add("[+] JWT algorithm set to 'none'!")
                    logs.add("[+] Server accepted unsigned token and authenticated as administrator!")
                    return LabExecutionResult(
                        isSuccess = true,
                        statusCode = 200,
                        statusText = "OK",
                        responseHeaders = mapOf("Content-Type" to "application/json"),
                        responseBody = "{\"authenticated\": true, \"role\": \"admin\", \"flag\": \"FLAG{jwt_alg_none_admin_bypass_6628}\"}",
                        executionLog = logs,
                        flagAwarded = "FLAG{jwt_alg_none_admin_bypass_6628}",
                        vulnerabilityTriggered = true,
                        feedbackMessage = "JWT None algorithm vulnerability exploited! The server omitted signature validation."
                    )
                }
            }
        }
        logs.add("[-] Invalid signature or unsupported token.")
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 401,
            statusText = "Unauthorized",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Invalid JWT token\"}",
            executionLog = logs,
            feedbackMessage = "Forge a JWT token with header '{\"alg\": \"none\", \"typ\": \"JWT\"}' and payload '{\"sub\": \"admin\"}'."
        )
    }

    // 18. JWT Weak Secret
    private fun evaluateJwtWeakSecret(headers: Map<String, String>): LabExecutionResult {
        val auth = headers["Authorization"] ?: ""
        val logs = mutableListOf("[*] Parsing HMAC-SHA256 signature")
        if (auth.contains("eyJ") && auth.contains("secret123", ignoreCase = true) || auth.contains("admin")) {
            logs.add("[+] Token forged using dictionary secret 'secret123'!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"license\": \"ACTIVATED\", \"licensed_to\": \"auditor\", \"flag\": \"FLAG{jwt_weak_secret_cracked_hmac_1289}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{jwt_weak_secret_cracked_hmac_1289}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Weak JWT secret cracked and forged successfully!"
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 401,
            statusText = "Unauthorized",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Signature validation failed\"}",
            executionLog = logs,
            feedbackMessage = "The JWT was signed with a weak secret ('secret123'). Forge an HMAC token for 'auditor'."
        )
    }

    // 19. OAuth Missing State
    private fun evaluateOAuthMissingState(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val logs = mutableListOf("[*] Validating OAuth 2.0 authorization callback")
        val hasCode = queryParams.containsKey("code") || endpoint.contains("code=")
        val hasState = queryParams.containsKey("state") || endpoint.contains("state=")

        if (hasCode && !hasState) {
            logs.add("[+] Callback accepted authorization code without state CSRF token!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"status\": \"account_linked\", \"flag\": \"FLAG{oauth_state_omission_ato_5832}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{oauth_state_omission_ato_5832}",
                vulnerabilityTriggered = true,
                feedbackMessage = "OAuth Account Takeover demonstrated! Omission of state allows CSRF login attacks."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 400,
            statusText = "Bad Request",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"State parameter present or code missing\"}",
            executionLog = logs,
            feedbackMessage = "Send the OAuth callback with 'code=AUTH_CODE_XYZ' but omit the 'state' parameter."
        )
    }

    // 20. OAuth Open Redirect
    private fun evaluateOAuthOpenRedirect(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val redirectUri = queryParams["redirect_uri"] ?: endpoint.substringAfter("redirect_uri=", "")
        val logs = mutableListOf("[*] Checking redirect_uri parameter: '$redirectUri'")

        if (redirectUri.contains("attacker.com") || redirectUri.contains(".evil.com")) {
            logs.add("[+] Regex flaw: allowed attacker host because it started with 'acme.com'!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 302,
                statusText = "Found",
                responseHeaders = mapOf(
                    "Location" to "$redirectUri?code=LEAKED_AUTH_CODE",
                    "Content-Type" to "text/plain"
                ),
                responseBody = "Redirecting... Token leaked to attacker server. FLAG{oauth_redirect_uri_regex_bypass_9145}",
                executionLog = logs,
                flagAwarded = "FLAG{oauth_redirect_uri_regex_bypass_9145}",
                vulnerabilityTriggered = true,
                feedbackMessage = "OAuth code theft confirmed! Loose redirect_uri validation leaked credentials to an untrusted domain."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "text/plain"),
            responseBody = "OAuth dialog displayed",
            executionLog = logs,
            feedbackMessage = "Provide a redirect_uri targeting an attacker domain (e.g. 'https://acme.com.attacker.com/cb')."
        )
    }

    // 21. SSRF Cloud Metadata
    private fun evaluateSsrfCloudMetadata(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Fetching external asset specified in request")
        if (body.contains("169.254.169.254")) {
            logs.add("[+] Target reached cloud metadata service link-local address!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """{
  "Code": "Success",
  "Type": "AWS-HMAC",
  "AccessKeyId": "ASIAQTESTIAMEXFIL99",
  "SecretAccessKey": "vPz+SecretKeyIAMLeakedSSRF",
  "Token": "FLAG{ssrf_cloud_metadata_iam_leaked_3498}"
}""".trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{ssrf_cloud_metadata_iam_leaked_3498}",
                vulnerabilityTriggered = true,
                feedbackMessage = "SSRF confirmed! Cloud instance metadata service exfiltrated."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "image/jpeg"),
            responseBody = "[Binary Image Data]",
            executionLog = logs,
            feedbackMessage = "Fetched normal image. Point the URL parameter to the cloud metadata address (169.254.169.254)."
        )
    }

    // 22. SSRF DNS Rebinding & Filter Bypass
    private fun evaluateSsrfDnsRebind(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Testing webhook destination")
        val isBypass = body.contains("0x7f.0.0.1") || body.contains("[::1]") || body.contains("127.1") || body.contains("2130706433")

        if (isBypass) {
            logs.add("[+] Alternative IP representation bypassed naive string filter!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"internal_admin_console\": \"active\", \"flag\": \"FLAG{ssrf_ip_representation_filter_bypass_2719}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{ssrf_ip_representation_filter_bypass_2719}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Filter bypass successful! Hexadecimal, octal, or IPv6 representation evaded blacklist."
            )
        } else if (body.contains("127.0.0.1") || body.contains("localhost")) {
            logs.add("[-] Blocked by blacklist: '127.0.0.1' or 'localhost' is prohibited.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 403,
                statusText = "Forbidden",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"error\": \"Loopback addresses are forbidden by security filter\"}",
                executionLog = logs,
                feedbackMessage = "Blacklist blocked '127.0.0.1'. Use an alternative encoding like '0x7f.0.0.1' or '[::1]'."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"status\": \"webhook_delivered\"}",
            executionLog = logs,
            feedbackMessage = "Normal webhook sent. Test internal loopback targets with filter bypass techniques."
        )
    }

    // 23. Path Traversal
    private fun evaluatePathTraversal(endpoint: String, queryParams: Map<String, String>): LabExecutionResult {
        val file = queryParams["file"] ?: endpoint.substringAfter("file=", "")
        val logs = mutableListOf("[*] Reading file parameter: '$file'")

        if (file.contains("..") && (file.contains("passwd") || file.contains("etc"))) {
            logs.add("[+] Directory traversal sequence resolved to root filesystem!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "text/plain"),
                responseBody = """
root:x:0:0:root:/root:/bin/bash
bin:x:1:1:bin:/bin:/sbin/nologin
secret:x:1001:1001:FLAG{path_traversal_passwd_leak_8302}:/home/app:/bin/sh
                """.trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{path_traversal_passwd_leak_8302}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Path traversal confirmed! The application lacks filename path validation."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "text/plain"),
            responseBody = "Terms and Conditions 2026",
            executionLog = logs,
            feedbackMessage = "Standard file served. Inject traversal sequences like '../../../../etc/passwd'."
        )
    }

    // 24. XXE
    private fun evaluateXxe(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Parsing XML invoice data")
        if (body.contains("<!ENTITY", ignoreCase = true) && body.contains("SYSTEM", ignoreCase = true)) {
            logs.add("[+] External entity declaration parsed by unhardened XML parser!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/xml"),
                responseBody = "<response><invoiceId>prod-web-node-01.acme.corp (FLAG{xxe_file_entity_disclosure_4910})</invoiceId></response>",
                executionLog = logs,
                flagAwarded = "FLAG{xxe_file_entity_disclosure_4910}",
                vulnerabilityTriggered = true,
                feedbackMessage = "XXE confirmed! The XML parser processed external general entities."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/xml"),
            responseBody = "<response><status>Invoice parsed</status></response>",
            executionLog = logs,
            feedbackMessage = "Standard XML parsed. Inject an external DTD entity using '<!ENTITY xxe SYSTEM ...>'."
        )
    }

    // 25. File Upload Extension Bypass
    private fun evaluateFileUpload(headers: Map<String, String>, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Handling multipart file upload")
        if (body.contains(".php\"", ignoreCase = true)) {
            logs.add("[-] Blocked by extension blacklist: '.php' is not allowed.")
            return LabExecutionResult(
                isSuccess = false,
                statusCode = 400,
                statusText = "Bad Request",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"error\": \".php files are forbidden\"}",
                executionLog = logs,
                feedbackMessage = "The server blacklists '.php'. Try alternative executable extensions like '.phtml', '.php5', or '.phar'."
            )
        }

        if (body.contains(".phtml", ignoreCase = true) || body.contains(".php5", ignoreCase = true) || body.contains(".phar", ignoreCase = true)) {
            logs.add("[+] File with executable alternative extension saved to /uploads/!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 201,
                statusText = "Created",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"url\": \"/uploads/shell.phtml\", \"status\": \"executed\", \"output\": \"FLAG{file_upload_phtml_exec_9051}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{file_upload_phtml_exec_9051}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Extension blacklist bypassed! The web server treats .phtml as PHP executable code."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 201,
            statusText = "Created",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"url\": \"/uploads/avatar.png\"}",
            executionLog = logs,
            feedbackMessage = "Safe image uploaded. Attempt to upload an executable script with an alternative extension."
        )
    }

    // 26. Git Disclosure
    private fun evaluateGitDisclosure(endpoint: String): LabExecutionResult {
        val logs = mutableListOf("[*] Inspecting server root directory: $endpoint")
        if (endpoint.contains("/.git/HEAD") || endpoint.contains("/.git/") || endpoint.contains("/.env")) {
            logs.add("[+] Sensitive dotfile/directory exposed via public web server!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "text/plain"),
                responseBody = "ref: refs/heads/main\ncommit 4a9f82c: Add DB credentials (FLAG{git_exposure_secrets_found_1409})",
                executionLog = logs,
                flagAwarded = "FLAG{git_exposure_secrets_found_1409}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Information disclosure confirmed! Exposed .git directory reveals source code and commit history."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 404,
            statusText = "Not Found",
            responseHeaders = mapOf("Content-Type" to "text/html"),
            responseBody = "<h1>404 Not Found</h1>",
            executionLog = logs,
            feedbackMessage = "Probe for sensitive directories like '/.git/HEAD' or '/.env'."
        )
    }

    // 27. Price Tampering
    private fun evaluatePriceTampering(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Calculating checkout total")
        val isNegativeQty = body.contains("\"quantity\":\\s*-\\d+".toRegex())
        val isPriceZero = body.contains("\"unit_price\":\\s*0(\\.\\d+)?".toRegex()) || body.contains("\"price\":\\s*0(\\.\\d+)?".toRegex())

        if (isNegativeQty || isPriceZero) {
            logs.add("[+] Logic flaw: Client-supplied price/negative quantity accepted without validation!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"order_id\": 9128, \"total_charged\": \"$0.01\", \"status\": \"PAID\", \"flag\": \"FLAG{logic_negative_quantity_cash_leak_6318}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{logic_negative_quantity_cash_leak_6318}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Business logic flaw exploited! The server trusted client-side prices or failed to reject negative quantities."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"order_id\": 9128, \"total_charged\": \"$500.00\", \"status\": \"PENDING_PAYMENT\"}",
            executionLog = logs,
            feedbackMessage = "Normal \$500 order. Tamper with the 'unit_price' or 'quantity' fields in the request body."
        )
    }

    // 28. Coupon Stacking
    private fun evaluateCouponStacking(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Applying discount coupon")
        if (body.contains("WELCOME20") && body.contains("apply_count") || body.contains("repeat")) {
            logs.add("[+] Applied coupon multiple times in single cart session!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"discount_applied\": \"100%\", \"total\": \"$0.00\", \"flag\": \"FLAG{logic_coupon_stacking_infinite_discount_4210}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{logic_coupon_stacking_infinite_discount_4210}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Coupon stacking verified! The application failed to mark the single-use coupon as consumed."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"discount_applied\": \"20%\", \"total\": \"$80.00\"}",
            executionLog = logs,
            feedbackMessage = "Coupon applied once. Simulate re-applying the coupon repeatedly in the request body."
        )
    }

    // 29. Race Condition
    private fun evaluateRaceCondition(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Wallet balance: \$100.00")
        val isBurst = body.contains("concurrency_burst") || body.contains("burst") || body.contains("\"threads\":")

        if (isBurst) {
            logs.add("[+] Dispatching 5 concurrent withdrawal requests simultaneously")
            logs.add("[+] TOCTOU flaw: All threads read balance = \$100 before first deduction completed!")
            logs.add("[+] Total transferred: \$500.00 from \$100.00 starting balance!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"withdrawn\": 500.0, \"initial_balance\": 100.0, \"flag\": \"FLAG{race_condition_toctou_double_spend_7781}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{race_condition_toctou_double_spend_7781}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Race condition exploited! Lack of database row locking permitted double-spending."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"withdrawn\": 100.0, \"new_balance\": 0.0}",
            executionLog = logs,
            feedbackMessage = "Single transfer completed. Simulate concurrent requests using 'concurrency_burst': 5."
        )
    }

    // 30. Secure Code Review: SQLi
    private fun evaluateCodeReviewSqli(body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Analyzing code review submission")
        if (body.contains("14") && (body.contains("PreparedStatement", ignoreCase = true) || body.contains("SQL", ignoreCase = true))) {
            logs.add("[+] Accurately identified raw string concatenation SQL sink on line 14!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"review_status\": \"APPROVED\", \"audit_score\": 100, \"flag\": \"FLAG{code_review_sqli_sink_identified_3108}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{code_review_sqli_sink_identified_3108}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Code review audit passed! Line 14 concatenates user input directly into SQL statement."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 400,
            statusText = "Bad Request",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Incorrect vulnerable line or remediation strategy.\"}",
            executionLog = logs,
            feedbackMessage = "Inspect line 14: string concatenation in query. Recommend parameterized queries."
        )
    }

    // 31. Secure Code Review: Crypto
    private fun evaluateCodeReviewCrypto(body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Analyzing cryptographic implementation code")
        if (body.contains("8") || body.contains("KeyStore", ignoreCase = true) || body.contains("HARDCODED", ignoreCase = true)) {
            logs.add("[+] Correctly flagged hardcoded encryption key and static IV on line 8!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = "{\"audit_result\": \"PASSED\", \"flag\": \"FLAG{code_review_hardcoded_crypto_key_5521}\"}",
                executionLog = logs,
                flagAwarded = "FLAG{code_review_hardcoded_crypto_key_5521}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Crypto flaw identified! Keys must never be hardcoded; use Android KeyStore or secure secrets managers."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 400,
            statusText = "Bad Request",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Audit response does not match the critical finding.\"}",
            executionLog = logs,
            feedbackMessage = "Examine line 8: static symmetric AES key. State the vulnerability and proper KeyStore remediation."
        )
    }

    // 32. GraphQL Introspection
    private fun evaluateGraphQLIntrospection(method: String, body: String): LabExecutionResult {
        val logs = mutableListOf("[*] Executing GraphQL query")
        if (body.contains("__schema") || body.contains("__type")) {
            logs.add("[+] Introspection schema query resolved!")
            return LabExecutionResult(
                isSuccess = true,
                statusCode = 200,
                statusText = "OK",
                responseHeaders = mapOf("Content-Type" to "application/json"),
                responseBody = """{
  "data": {
    "__schema": {
      "types": [
        {"name": "Query", "fields": [{"name": "publicPosts"}]},
        {"name": "Mutation", "fields": [{"name": "deleteUserDatabase", "args": [{"name": "secretAuthKey"}]}]}
      ]
    },
    "debug_flag": "FLAG{graphql_introspection_hidden_schema_8830}"
  }
}""".trimIndent(),
                executionLog = logs,
                flagAwarded = "FLAG{graphql_introspection_hidden_schema_8830}",
                vulnerabilityTriggered = true,
                feedbackMessage = "Introspection enabled in production! Uncovered hidden administrative mutations and fields."
            )
        }
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 200,
            statusText = "OK",
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "{\"data\": {\"publicPosts\": [{\"title\": \"Welcome to GraphQL\"}]}}",
            executionLog = logs,
            feedbackMessage = "Standard query returned. Query '{ __schema { types { name } } }' to introspect hidden schemas."
        )
    }

    private fun methodNotAllowed(expected: String): LabExecutionResult {
        return LabExecutionResult(
            isSuccess = false,
            statusCode = 405,
            statusText = "Method Not Allowed",
            responseHeaders = mapOf("Allow" to expected, "Content-Type" to "application/json"),
            responseBody = "{\"error\": \"Method not allowed. Use $expected\"}",
            executionLog = listOf("[-] HTTP method rejected. Expected $expected"),
            feedbackMessage = "Invalid HTTP method. Expected $expected."
        )
    }
}
