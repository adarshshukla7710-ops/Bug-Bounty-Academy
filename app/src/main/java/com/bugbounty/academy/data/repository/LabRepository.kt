package com.bugbounty.academy.data.repository

import com.bugbounty.academy.data.local.dao.LabProgressDao
import com.bugbounty.academy.data.local.entity.LabProgressEntity
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.domain.engine.SafeDeterministicLabEngine
import com.bugbounty.academy.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LabRepository(
    private val labProgressDao: LabProgressDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val labsList: List<Lab> = listOf(
        // Category 1: INJECTION
        Lab(
            id = "lab_sqli_auth_bypass",
            title = "SQL Injection: Authentication Bypass",
            category = LabCategory.INJECTION,
            difficulty = LabDifficulty.BEGINNER,
            vulnerabilityType = "CWE-89: Improper Neutralization of Special Elements used in an SQL Command",
            cwe = "CWE-89",
            description = "Bypass the administrative login authentication gate by injecting an SQL tautology into the username field.",
            scenarioContext = "Acme Corp uses an internal login portal where SQL query concatenation occurs directly in the backend handler.",
            simulatedEndpoint = "/api/v1/auth/login",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"username\": \"admin' OR '1'='1\", \"password\": \"test\"}",
            hints = listOf(
                "Notice how single quotes break the string literal context.",
                "Inject a condition that always evaluates to true, like `' OR '1'='1` or `admin' --`.",
                "Observe the SQL query constructed in the execution log."
            ),
            flag = "FLAG{sqli_auth_bypass_pwned_7391}",
            learningObjectives = listOf(
                "Understand how dynamic string concatenation in SQL queries leads to syntax injection.",
                "Learn how authentication queries can be subverted using boolean tautologies.",
                "Review remediation with parameterized prepared statements."
            ),
            remediationCodeSnippet = """// SECURE REMEDIATION:
val sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?"
val statement = connection.prepareStatement(sql)
statement.setString(1, username)
statement.setString(2, hashedPassword)
val rs = statement.executeQuery()""",
            remediationExplanation = "Never concatenate user input directly into SQL strings. Always utilize parameterized PreparedStatements or an ORM that safely parameterizes inputs."
        ),
        Lab(
            id = "lab_sqli_union",
            title = "SQL Injection: UNION-Based Data Extraction",
            category = LabCategory.INJECTION,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-89: UNION SQL Injection",
            cwe = "CWE-89",
            description = "Extract sensitive user credentials from the backend database by appending a UNION SELECT payload to the product category filter.",
            scenarioContext = "An online store displays products filtered by category. The query selects 4 columns: id, name, price, description.",
            simulatedEndpoint = "/api/v1/products?category=apparel' UNION SELECT 1, username, password, 4 FROM users--",
            defaultMethod = "GET",
            defaultQueryParams = mapOf("category" to "apparel' UNION SELECT 1, username, password, 4 FROM users--"),
            hints = listOf(
                "A UNION query must have the exact same number of columns as the original query (4 columns).",
                "Column data types must match or be compatible with the original result set.",
                "Target the 'users' table to extract user credentials."
            ),
            flag = "FLAG{sqli_union_users_extracted_9124}",
            learningObjectives = listOf(
                "Determine column count using ORDER BY or UNION SELECT null techniques.",
                "Exfiltrate cross-table database records using UNION operators."
            ),
            remediationCodeSnippet = """val query = "SELECT id, name, price, description FROM products WHERE category = ?"
val stmt = db.prepareStatement(query)
stmt.setString(1, category)""",
            remediationExplanation = "Parameterizing the input ensures that the database driver treats the value strictly as literal data rather than executable SQL syntax."
        ),
        Lab(
            id = "lab_sqli_blind_boolean",
            title = "Blind SQL Injection: Boolean Inference",
            category = LabCategory.INJECTION,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-89: Blind Boolean SQL Injection",
            cwe = "CWE-89",
            description = "Infer sensitive token values character-by-character by observing true/false response differences from the tracking status endpoint.",
            scenarioContext = "A package tracking API does not return raw SQL errors or reflected data, but changes its response text depending on whether the query returns rows.",
            simulatedEndpoint = "/api/v1/tracking?id=102 AND SUBSTRING((SELECT secret FROM tokens LIMIT 1),1,4)='FLAG'",
            defaultMethod = "GET",
            defaultQueryParams = mapOf("id" to "102 AND 1=1"),
            hints = listOf(
                "Check how the server response differs when testing 'AND 1=1' vs 'AND 1=2'.",
                "Use SQL substring functions to test characters of hidden tokens."
            ),
            flag = "FLAG{sqli_blind_bool_inference_5519}",
            learningObjectives = listOf(
                "Master blind boolean inference techniques.",
                "Understand why error suppression alone does not eliminate SQL injection risk."
            ),
            remediationCodeSnippet = """val query = "SELECT status, details FROM tracking WHERE id = ?"
val stmt = db.prepareStatement(query)
stmt.setInt(1, id.toIntOrNull() ?: return error())""",
            remediationExplanation = "Enforce strong integer type casting and parameterization."
        ),
        Lab(
            id = "lab_command_injection_ping",
            title = "OS Command Injection: Diagnostic Ping Escape",
            category = LabCategory.INJECTION,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-78: Improper Neutralization of Special Elements used in an OS Command",
            cwe = "CWE-78",
            description = "Execute arbitrary operating system commands on the hosting server by chaining shell metacharacters in the diagnostic host parameter.",
            scenarioContext = "A network diagnostic utility allows administrators to ping IP addresses. The backend passes the host directly to `/bin/sh -c`.",
            simulatedEndpoint = "/api/v1/tools/ping",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"host\": \"8.8.8.8; cat /etc/passwd\"}",
            hints = listOf(
                "Shell metacharacters such as ';', '&&', and '|' allow chaining multiple commands.",
                "Try reading system files or running 'whoami' or 'cat /etc/passwd'."
            ),
            flag = "FLAG{cmd_injection_pipe_escape_4821}",
            learningObjectives = listOf(
                "Recognize vulnerable patterns that invoke system shells.",
                "Learn how input validation and native platform APIs prevent command injection."
            ),
            remediationCodeSnippet = """// SECURE: Use native InetAddress API instead of shell execution
val address = InetAddress.getByName(validatedIp)
val reachable = address.isReachable(3000)""",
            remediationExplanation = "Avoid calling shell interpreters entirely. When system execution is unavoidable, use ProcessBuilder with discrete arguments and strict regex allowlists."
        ),

        // Category 2: XSS_AND_CLIENT
        Lab(
            id = "lab_stored_xss",
            title = "Stored XSS: Unfiltered Comment Section",
            category = LabCategory.XSS_AND_CLIENT,
            difficulty = LabDifficulty.BEGINNER,
            vulnerabilityType = "CWE-79: Improper Neutralization of Input During Web Page Generation",
            cwe = "CWE-79",
            description = "Inject persistent JavaScript into a public discussion board that triggers in the browser context of any user who loads the page.",
            scenarioContext = "A blog comment system stores comments in the database and renders them into HTML without entity encoding.",
            simulatedEndpoint = "/api/v1/comments",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"author\": \"Attacker\", \"comment\": \"<script>alert(document.cookie)</script>\"}",
            hints = listOf(
                "The application does not filter `<script>` tags or HTML event handlers.",
                "Submit an executable script tag or an `<img src=x onerror=...>` payload."
            ),
            flag = "FLAG{stored_xss_comment_stealer_3840}",
            learningObjectives = listOf(
                "Understand the mechanics of stored (second-order) XSS.",
                "Differentiate between input validation, output encoding, and Content Security Policy (CSP)."
            ),
            remediationCodeSnippet = """// SECURE: Context-aware HTML entity encoding
val safeComment = HtmlEscape.escapeHtml4(comment)
// Enforce CSP header: Content-Security-Policy: default-src 'self'""",
            remediationExplanation = "Always apply context-aware HTML entity encoding before rendering untrusted input, and enforce a strict Content Security Policy."
        ),
        Lab(
            id = "lab_reflected_xss_filter_bypass",
            title = "Reflected XSS: Case-Insensitive Filter Bypass",
            category = LabCategory.XSS_AND_CLIENT,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-79: Reflected XSS with Filter Evasion",
            cwe = "CWE-79",
            description = "Bypass a naive blacklist filter that blocks `<script>` by utilizing mixed casing or alternative HTML event handlers.",
            scenarioContext = "The search feature checks for exact lowercase `<script>`, but leaves other tags and event handlers uninspected.",
            simulatedEndpoint = "/search?q=<IMG SRC=x OnErRoR=alert(1)>",
            defaultMethod = "GET",
            defaultQueryParams = mapOf("q" to "<IMG SRC=x OnErRoR=alert(1)>"),
            hints = listOf(
                "The WAF rejects lowercase `<script>`.",
                "Try mixed case `<sCrIpt>` or tag event handlers like `<img src=x onerror=...>` or `<svg onload=...>`."
            ),
            flag = "FLAG{reflected_xss_mixed_case_bypass_6192}",
            learningObjectives = listOf(
                "Understand why blacklist-based filtering frequently fails.",
                "Learn why context-aware output encoding is superior to input filtering."
            ),
            remediationCodeSnippet = """val safeSearch = HtmlEscapers.htmlEscaper().escape(query)""",
            remediationExplanation = "Never rely on naive keyword blacklists. Encode characters like `<`, `>`, `\"`, `'`, and `&` to their respective HTML entities."
        ),
        Lab(
            id = "lab_dom_xss_hash",
            title = "DOM-Based XSS: Unsafe innerHTML Sink",
            category = LabCategory.XSS_AND_CLIENT,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-79: DOM-Based XSS",
            cwe = "CWE-79",
            description = "Exploit client-side JavaScript that directly writes URL hash fragments into the DOM using `innerHTML`.",
            scenarioContext = "A single-page application reads `location.hash` and dynamically updates an alert banner using `document.getElementById('banner').innerHTML = location.hash`.",
            simulatedEndpoint = "/api/v1/simulate/dom",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"sink\": \"innerHTML\", \"payload\": \"<img src=1 onerror=alert(document.cookie)>\"}",
            hints = listOf(
                "Client-side DOM sinks like `innerHTML`, `outerHTML`, and `document.write` execute inline HTML and scripts.",
                "Target the `innerHTML` sink with an image onerror payload."
            ),
            flag = "FLAG{dom_xss_innerhtml_sink_2041}",
            learningObjectives = listOf(
                "Identify client-side sources and execution sinks.",
                "Replace unsafe DOM sinks with safe alternatives like `textContent`."
            ),
            remediationCodeSnippet = """// SECURE: Use textContent instead of innerHTML
element.textContent = window.location.hash.substring(1);
// Or use DOMPurify: element.innerHTML = DOMPurify.sanitize(input);""",
            remediationExplanation = "Use `textContent` or `innerText` to prevent HTML interpretation, or sanitize with DOMPurify if HTML formatting is required."
        ),
        Lab(
            id = "lab_csrf_account_takeover",
            title = "CSRF: State-Changing Email Update",
            category = LabCategory.XSS_AND_CLIENT,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-352: Cross-Site Request Forgery (CSRF)",
            cwe = "CWE-352",
            description = "Forge an authenticated state-changing POST request to change the account email without anti-CSRF token verification.",
            scenarioContext = "The profile management API updates the user's registered email using cookie-based session authentication without CSRF tokens.",
            simulatedEndpoint = "/api/v1/account/change-email",
            defaultMethod = "POST",
            defaultHeaders = mapOf(
                "Content-Type" to "application/json",
                "Origin" to "https://attacker.com"
            ),
            defaultBody = "{\"email\": \"attacker@evil.com\"}",
            hints = listOf(
                "Notice the complete absence of `X-CSRF-Token` headers.",
                "The server accepts cross-site requests from external Origins without rejecting them."
            ),
            flag = "FLAG{csrf_state_change_takeover_8820}",
            learningObjectives = listOf(
                "Understand how ambient authentication (cookies) allows cross-site request forgery.",
                "Implement Synchronizer Token Pattern and `SameSite=Lax/Strict` cookies."
            ),
            remediationCodeSnippet = """// SECURE: Validate CSRF token and set SameSite cookies
Set-Cookie: session=xyz; Secure; HttpOnly; SameSite=Strict
// Check request header:
val requestToken = request.getHeader("X-CSRF-Token")
if (requestToken != session.csrfToken) throw SecurityException("Invalid CSRF token")""",
            remediationExplanation = "Enforce `SameSite=Strict` or `Lax` on session cookies and require cryptographically random synchronizer tokens on all state-changing endpoints."
        ),

        // Category 3: ACCESS_CONTROL
        Lab(
            id = "lab_idor_horizontal",
            title = "Horizontal IDOR: User Profile Data Leak",
            category = LabCategory.ACCESS_CONTROL,
            difficulty = LabDifficulty.BEGINNER,
            vulnerabilityType = "CWE-639: Authorization Bypass Through User-Controlled Key",
            cwe = "CWE-639",
            description = "Access the private profile, SSN, and credit card data of another user by tampering with the numeric user ID in the URL path.",
            scenarioContext = "You are authenticated as user ID 1041. The API retrieves profiles by URL ID parameter without verifying account ownership.",
            simulatedEndpoint = "/api/v1/users/1042/profile",
            defaultMethod = "GET",
            hints = listOf(
                "You are logged in as user 1041.",
                "Change the ID in `/api/v1/users/1041/profile` to `1042` to read Sarah Connor's profile."
            ),
            flag = "FLAG{idor_horizontal_account_peek_5139}",
            learningObjectives = listOf(
                "Understand Broken Object Level Authorization (BOLA / IDOR).",
                "Implement ownership checks based on authenticated session context."
            ),
            remediationCodeSnippet = """// SECURE: Enforce resource ownership check
val currentUser = session.currentUser
val requestedUserId = pathParams["userId"]?.toIntOrNull()
if (currentUser.id != requestedUserId && !currentUser.isAdmin) {
    throw ForbiddenException("Access Denied to resource")
}""",
            remediationExplanation = "Always verify that the authenticated subject owns or is explicitly authorized to access the requested object ID."
        ),
        Lab(
            id = "lab_idor_vertical",
            title = "Vertical Privilege Escalation: Role Tampering",
            category = LabCategory.ACCESS_CONTROL,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-269: Improper Privilege Management",
            cwe = "CWE-269",
            description = "Escalate from a standard user to a system administrator by adding an unvalidated `role: admin` property to the profile update request.",
            scenarioContext = "A user settings API accepts JSON updates and directly saves the role field to the database without permission checks.",
            simulatedEndpoint = "/api/v1/users/me",
            defaultMethod = "PUT",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"name\": \"Tester\", \"role\": \"admin\"}",
            hints = listOf(
                "Inspect the fields accepted during profile update.",
                "Try supplying `\"role\": \"admin\"` or `\"is_admin\": true`."
            ),
            flag = "FLAG{idor_vertical_role_escalation_7412}",
            learningObjectives = listOf(
                "Understand vertical privilege escalation.",
                "Separate self-service user profiles from administrative authorization models."
            ),
            remediationCodeSnippet = """// SECURE: Use a restricted DTO for profile updates
data class UserProfileUpdateDto(val displayName: String, val bio: String)
// Never bind 'role', 'permissions', or 'isAdmin' from user-submitted DTOs.""",
            remediationExplanation = "Never allow clients to set their own role or privileges. Keep authorization changes restricted to dedicated administrative controllers."
        ),
        Lab(
            id = "lab_bfla_admin_endpoint",
            title = "Broken Function Level Authorization (BFLA)",
            category = LabCategory.ACCESS_CONTROL,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-285: Improper Authorization",
            cwe = "CWE-285",
            description = "Discover and access an unauthenticated administrative export endpoint while authenticated only as a standard user.",
            scenarioContext = "The frontend hides administrative buttons, but the backend endpoint `/api/v1/admin/export-users` lacks role-based access control.",
            simulatedEndpoint = "/api/v1/admin/export-users",
            defaultMethod = "GET",
            hints = listOf(
                "Developers often assume that hidden UI buttons provide security (security by obscurity).",
                "Try accessing `/api/v1/admin/export-users` directly with your standard user session."
            ),
            flag = "FLAG{bfla_hidden_admin_export_1928}",
            learningObjectives = listOf(
                "Differentiate between UI hiding and backend authorization enforcement.",
                "Implement declarative Role-Based Access Control (RBAC) on all administrative routes."
            ),
            remediationCodeSnippet = """// SECURE: Annotate controller with strict role check
@PreAuthorize("hasRole('ROLE_ADMIN')")
@GetMapping("/api/v1/admin/export-users")
fun exportUsers(): ResponseEntity<ExportData> { ... }""",
            remediationExplanation = "All backend endpoints must explicitly verify user roles and permissions on every single request, regardless of whether they appear in the UI."
        ),
        Lab(
            id = "lab_bola_document_vault",
            title = "BOLA: Cross-Tenant Document Exfiltration",
            category = LabCategory.ACCESS_CONTROL,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-639: Broken Object Level Authorization",
            cwe = "CWE-639",
            description = "Enumerate sequential document IDs to download confidential contracts belonging to other enterprise tenants.",
            scenarioContext = "A document management service uses sequential document IDs (`doc-9840.pdf`, `doc-9842.pdf`) without tenant scoping.",
            simulatedEndpoint = "/api/v1/documents/doc-9842.pdf",
            defaultMethod = "GET",
            hints = listOf(
                "Your company document is `doc-9840.pdf`.",
                "Modify the path to request `doc-9842.pdf`."
            ),
            flag = "FLAG{bola_unauthorized_document_leak_3391}",
            learningObjectives = listOf(
                "Understand why multi-tenant architectures require explicit tenant ID checks in SQL WHERE clauses.",
                "Use unguessable UUIDv4 identifiers in combination with access control lists."
            ),
            remediationCodeSnippet = """// SECURE: Scope database query to current tenant
val doc = documentRepo.findByIdAndTenantId(docId, currentTenant.id)
    ?: throw NotFoundException("Document not found")""",
            remediationExplanation = "Always enforce multi-tenant isolation by including the tenant/organization identifier in the database lookup query."
        ),

        // Category 4: API_SECURITY
        Lab(
            id = "lab_mass_assignment_registration",
            title = "Mass Assignment: Auto-Binding SuperAdmin",
            category = LabCategory.API_SECURITY,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-915: Improperly Controlled Modification of Dynamically-Determined Object Attributes",
            cwe = "CWE-915",
            description = "Register a new account while injecting privileged attributes (`is_super_admin: true`) to gain immediate superuser access.",
            scenarioContext = "A Spring Boot / Node.js registration controller deserializes JSON input directly into the database `User` entity without a DTO whitelist.",
            simulatedEndpoint = "/api/v1/auth/register",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"username\": \"attacker\", \"password\": \"SecPass123!\", \"is_super_admin\": true, \"credits\": 99999}",
            hints = listOf(
                "Framework model binders often bind all matching JSON keys to Java/Kotlin entity properties.",
                "Inject attributes like `is_super_admin: true` or `credits: 99999`."
            ),
            flag = "FLAG{mass_assignment_superuser_pwned_8264}",
            learningObjectives = listOf(
                "Understand how automatic object-relational mapping causes mass assignment vulnerabilities.",
                "Enforce strict Request DTOs with explicit field mappings."
            ),
            remediationCodeSnippet = """// SECURE: Explicit DTO mapping
data class RegisterRequestDto(val username: String, val passwordHash: String)

fun register(dto: RegisterRequestDto): User {
    // Manually map allowed fields only. Never bind entity directly from HTTP body!
    return User(username = dto.username, passwordHash = hash(dto.passwordHash), isSuperAdmin = false)
}""",
            remediationExplanation = "Never deserialize HTTP request bodies directly into internal domain models or ORM entities. Always utilize dedicated input DTOs."
        ),
        Lab(
            id = "lab_rate_limit_header_spoof",
            title = "Rate Limit Bypass: X-Forwarded-For Spoofing",
            category = LabCategory.API_SECURITY,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-799: Improper Control of Interaction Frequency",
            cwe = "CWE-799",
            description = "Bypass password reset rate limiting by injecting client-controlled `X-Forwarded-For` IP headers to evade IP-based throttling.",
            scenarioContext = "An API limits password reset requests to 5 per IP address. The rate limiter trusts the client-provided `X-Forwarded-For` header.",
            simulatedEndpoint = "/api/v1/auth/password-reset",
            defaultMethod = "POST",
            defaultHeaders = mapOf(
                "Content-Type" to "application/json",
                "X-Forwarded-For" to "10.0.0.42"
            ),
            defaultBody = "{\"username\": \"victim@corp.com\"}",
            hints = listOf(
                "The server returns 429 Too Many Requests when the limit is reached.",
                "Add an `X-Forwarded-For` or `Client-IP` header with an arbitrary IP address to reset the bucket."
            ),
            flag = "FLAG{rate_limit_xff_header_spoofed_4019}",
            learningObjectives = listOf(
                "Understand reverse proxy header handling and IP spoofing risks.",
                "Configure edge proxies to strip or overwrite untrusted upstream proxy headers."
            ),
            remediationCodeSnippet = """// SECURE: Configure NGINX to overwrite X-Forwarded-For with true remote address
proxy_set_header X-Forwarded-For ${'$'}remote_addr;
// In application: Rate limit by account identity + trusted client IP""",
            remediationExplanation = "Never trust client-supplied IP headers unless set by an authenticated upstream edge proxy. Rate limit by account identity as well as verified IP."
        ),
        Lab(
            id = "lab_api_versioning_unprotected",
            title = "API Versioning: Deprecated v1-Beta Exposure",
            category = LabCategory.API_SECURITY,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-1059: Insufficient Technical Documentation / Shadow APIs",
            cwe = "CWE-1059",
            description = "Discover a neglected legacy API version that has modern authentication middlewares disabled, exposing sensitive customer records.",
            scenarioContext = "The current API `/api/v2/customers` enforces OAuth2 tokens, but the deprecated `/api/v1-beta/export` was never decommissioned.",
            simulatedEndpoint = "/api/v1-beta/export",
            defaultMethod = "GET",
            hints = listOf(
                "API routes often retain unmaintained legacy versions (e.g. /v1/, /v1-beta/, /v0.9/).",
                "Try requesting `/api/v1-beta/export`."
            ),
            flag = "FLAG{api_shadow_v1_deprecated_leak_9041}",
            learningObjectives = listOf(
                "Understand the security risks of Shadow APIs and legacy endpoint sprawl.",
                "Implement API inventory tracking and automated decommissioning lifecycles."
            ),
            remediationCodeSnippet = """// SECURE: Decommission deprecated routes at API Gateway level
route("/api/v1-beta/**") {
    respond(status = HttpStatusCode.Gone, "API version decommissioned")
}""",
            remediationExplanation = "Decommission obsolete API versions promptly and enforce centralized authentication across all routes at the API gateway layer."
        ),
        Lab(
            id = "lab_cors_misconfiguration",
            title = "CORS Misconfiguration: Arbitrary Origin Reflection",
            category = LabCategory.API_SECURITY,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-942: Permissive Cross-Domain Policy with Credentials",
            cwe = "CWE-942",
            description = "Exploit a backend CORS filter that dynamically reflects any incoming `Origin` header while setting `Access-Control-Allow-Credentials: true`.",
            scenarioContext = "A private data endpoint reflects the caller's Origin in `Access-Control-Allow-Origin` and enables credentials, allowing rogue websites to read user data.",
            simulatedEndpoint = "/api/v1/user/sensitive-data",
            defaultMethod = "GET",
            defaultHeaders = mapOf(
                "Origin" to "https://evil-attacker.com",
                "Cookie" to "session=authenticated_user_token"
            ),
            hints = listOf(
                "Notice the response headers when sending `Origin: https://evil-attacker.com`.",
                "If `Access-Control-Allow-Origin: https://evil-attacker.com` and `Access-Control-Allow-Credentials: true` are both present, cross-origin reading is permitted."
            ),
            flag = "FLAG{cors_origin_reflection_stealer_7743}",
            learningObjectives = listOf(
                "Understand the security boundaries of Cross-Origin Resource Sharing (CORS).",
                "Learn why reflecting arbitrary Origins with credentials defeats the Same-Origin Policy."
            ),
            remediationCodeSnippet = """// SECURE: Maintain an exact whitelist of trusted partner origins
val allowedOrigins = setOf("https://portal.acme.corp", "https://app.acme.corp")
val origin = request.getHeader("Origin")
if (allowedOrigins.contains(origin)) {
    response.setHeader("Access-Control-Allow-Origin", origin)
    response.setHeader("Access-Control-Allow-Credentials", "true")
}""",
            remediationExplanation = "Never blindly reflect the `Origin` header when `Access-Control-Allow-Credentials` is true. Use a strict, static allowlist."
        ),

        // Category 5: AUTH_AND_TOKENS
        Lab(
            id = "lab_jwt_none_algorithm",
            title = "JWT Security: None Algorithm Signature Bypass",
            category = LabCategory.AUTH_AND_TOKENS,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-347: Improper Verification of Cryptographic Signature",
            cwe = "CWE-347",
            description = "Forge an administrative JWT by setting the algorithm header to `none`, stripping the signature, and escalating to system administrator.",
            scenarioContext = "A microservice authentication middleware trusts the `alg` specified in the JWT header without restricting accepted algorithms.",
            simulatedEndpoint = "/api/v1/protected/dashboard",
            defaultMethod = "POST",
            defaultHeaders = mapOf(
                "Authorization" to "Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJhZG1pbiJ9."
            ),
            hints = listOf(
                "In JWT format `header.payload.signature`, the header `{\"alg\": \"none\"}` indicates no cryptographic signature is required.",
                "Encode `{\"alg\":\"none\",\"typ\":\"JWT\"}` in Base64URL (`eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0`).",
                "End the token with a trailing period `.` and an empty signature."
            ),
            flag = "FLAG{jwt_alg_none_admin_bypass_6628}",
            learningObjectives = listOf(
                "Understand the internals of JSON Web Tokens (RFC 7519).",
                "Learn why libraries must strictly enforce expected signing algorithms."
            ),
            remediationCodeSnippet = """// SECURE: Enforce expected algorithm strictly during verification
val verifier = JWT.require(Algorithm.HMAC256(secureKey))
    .build() // Will immediately reject 'none' or mismatched algorithms
val decodedJwt = verifier.verify(token)""",
            remediationExplanation = "Explicitly specify the allowed signing algorithms when verifying JWTs. Reject the `none` algorithm unconditionally in production."
        ),
        Lab(
            id = "lab_jwt_weak_secret",
            title = "JWT Security: Weak HMAC Secret Forgery",
            category = LabCategory.AUTH_AND_TOKENS,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-522: Insufficiently Protected Credentials / Weak Key",
            cwe = "CWE-522",
            description = "Crack a weak dictionary HMAC-SHA256 secret key (`secret123`) and forge a valid license token for user `auditor`.",
            scenarioContext = "A license verification endpoint validates tokens signed using a weak, guessable HMAC key.",
            simulatedEndpoint = "/api/v1/license/activate",
            defaultMethod = "POST",
            defaultHeaders = mapOf(
                "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhdWRpdG9yIiwibGljZW5zZWQiOnRydWV9.secret123"
            ),
            hints = listOf(
                "Symmetric HMAC signatures rely entirely on secret entropy.",
                "Tools like `jwt_tool` or `hashcat` quickly crack short secrets like `secret123`."
            ),
            flag = "FLAG{jwt_weak_secret_cracked_hmac_1289}",
            learningObjectives = listOf(
                "Learn how offline dictionary attacks are performed on symmetric HMAC keys.",
                "Enforce minimum key entropy (256+ bits of cryptographically secure random bytes)."
            ),
            remediationCodeSnippet = """// SECURE: Generate minimum 256-bit cryptographically secure key
val secureKey = ByteArray(32).also { SecureRandom().nextBytes(it) }""",
            remediationExplanation = "Use high-entropy random keys of at least 256 bits for HMAC-SHA256, or use asymmetric algorithms like RS256 / EdDSA."
        ),
        Lab(
            id = "lab_oauth_missing_state",
            title = "OAuth 2.0: Missing State Parameter (CSRF ATO)",
            category = LabCategory.AUTH_AND_TOKENS,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-352: Cross-Site Request Forgery in OAuth Authorization",
            cwe = "CWE-352",
            description = "Complete an OAuth authorization flow that omits the anti-CSRF `state` parameter, demonstrating account takeover risks.",
            scenarioContext = "An OAuth callback handler exchanges authorization codes for session tokens without verifying a unique client state token.",
            simulatedEndpoint = "/auth/callback?code=AUTH_CODE_VICTIM_PWN",
            defaultMethod = "GET",
            defaultQueryParams = mapOf("code" to "AUTH_CODE_VICTIM_PWN"),
            hints = listOf(
                "The OAuth 2.0 specification (RFC 6749) mandates using the `state` parameter to prevent CSRF.",
                "Send the callback request without a `state` parameter."
            ),
            flag = "FLAG{oauth_state_omission_ato_5832}",
            learningObjectives = listOf(
                "Understand the OAuth 2.0 authorization code grant flow.",
                "Learn how state token binding prevents cross-site code injection and account linking attacks."
            ),
            remediationCodeSnippet = """// SECURE: Generate and validate cryptographically secure random state
val expectedState = session.oauthStateToken
val receivedState = params["state"]
if (expectedState.isNullOrEmpty() || expectedState != receivedState) {
    throw SecurityException("Invalid OAuth state parameter. Possible CSRF attack.")
}""",
            remediationExplanation = "Always generate a cryptographically random `state` token bound to the user's session prior to redirecting to the IdP, and verify it upon return."
        ),
        Lab(
            id = "lab_oauth_open_redirect",
            title = "OAuth 2.0: Weak redirect_uri Regex Bypass",
            category = LabCategory.AUTH_AND_TOKENS,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-601: URL Redirection to Untrusted Site",
            cwe = "CWE-601",
            description = "Bypass a flawed `redirect_uri` regular expression filter to steal authorization codes by redirecting to an attacker-controlled domain.",
            scenarioContext = "The authorization server validates `redirect_uri` using `startsWith(\"https://acme.com\")`, allowing `https://acme.com.attacker.com`.",
            simulatedEndpoint = "/oauth/authorize?client_id=acme_app&redirect_uri=https://acme.com.attacker.com/cb&response_type=code",
            defaultMethod = "GET",
            defaultQueryParams = mapOf(
                "client_id" to "acme_app",
                "redirect_uri" to "https://acme.com.attacker.com/cb",
                "response_type" to "code"
            ),
            hints = listOf(
                "The server checks if the URI starts with `https://acme.com` without requiring an exact domain match.",
                "Append attacker domain suffixes like `.attacker.com/cb`."
            ),
            flag = "FLAG{oauth_redirect_uri_regex_bypass_9145}",
            learningObjectives = listOf(
                "Understand authorization code theft via open redirection.",
                "Enforce strict string-equality matching for registered redirect URIs."
            ),
            remediationCodeSnippet = """// SECURE: Enforce exact string equality against pre-registered redirect URIs
val registeredUris = clientApp.registeredRedirectUris
if (!registeredUris.contains(requestedUri)) {
    throw InvalidRedirectUriException("Redirect URI is not registered")
}""",
            remediationExplanation = "Never use partial regex or prefix matching for OAuth redirect URIs. Validate against an exact, pre-registered allowlist."
        ),

        // Category 6: SERVER_SIDE
        Lab(
            id = "lab_ssrf_cloud_metadata",
            title = "SSRF: Cloud Metadata Exfiltration (AWS/GCP)",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-918: Server-Side Request Forgery (SSRF)",
            cwe = "CWE-918",
            description = "Coerce the server to make an HTTP request to the link-local cloud metadata IP (`169.254.169.254`) and exfiltrate temporary IAM credentials.",
            scenarioContext = "An avatar import feature downloads images from a user-supplied URL without blocking internal or link-local network ranges.",
            simulatedEndpoint = "/api/v1/fetch-avatar",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"url\": \"http://169.254.169.254/latest/meta-data/iam/security-credentials/admin-role\"}",
            hints = listOf(
                "Cloud instance metadata is exposed at link-local address `169.254.169.254`.",
                "Target the IAM security credentials path to retrieve access tokens."
            ),
            flag = "FLAG{ssrf_cloud_metadata_iam_leaked_3498}",
            learningObjectives = listOf(
                "Understand Server-Side Request Forgery mechanics in cloud environments.",
                "Enforce IMDSv2 (session-oriented tokens) and IP blocklists."
            ),
            remediationCodeSnippet = """// SECURE: Resolve IP and reject private / link-local addresses
val inetAddress = InetAddress.getByName(uri.host)
if (inetAddress.isLinkLocalAddress || inetAddress.isSiteLocalAddress || inetAddress.isLoopbackAddress) {
    throw SecurityException("Access to internal IP ranges is prohibited")
}
// Also enforce AWS IMDSv2 (requires PUT token)""",
            remediationExplanation = "Resolve domain names and verify that the destination IP does not belong to loopback, RFC 1918, or cloud metadata (169.254.169.254) ranges."
        ),
        Lab(
            id = "lab_ssrf_dns_rebinding_bypass",
            title = "SSRF: IP Whitelist Filter Bypass (Hex/IPv6)",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.EXPERT,
            vulnerabilityType = "CWE-918: SSRF Filter Evasion",
            cwe = "CWE-918",
            description = "Bypass a naive string filter that blocks `127.0.0.1` and `localhost` by using alternative hexadecimal, integer, or IPv6 notations.",
            scenarioContext = "A webhook testing utility blocks literal strings `127.0.0.1` and `localhost`, but parses hexadecimal IP addresses (`0x7f.0.0.1`) or IPv6 (`[::1]`).",
            simulatedEndpoint = "/api/v1/webhook/test",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"target\": \"http://0x7f.0.0.1/admin\"}",
            hints = listOf(
                "Try hexadecimal IP representation: `0x7f.0.0.1`.",
                "Try IPv6 loopback: `[::1]` or integer notation: `2130706433`."
            ),
            flag = "FLAG{ssrf_ip_representation_filter_bypass_2719}",
            learningObjectives = listOf(
                "Explore IP encoding variants that evade string-based filters.",
                "Understand canonical IP resolution and socket-level filtering."
            ),
            remediationCodeSnippet = """// SECURE: Parse using standardized parser and check canonical IP
val addr = InetAddresses.forString(host)
if (addr.isLoopbackAddress || addr.isAnyLocalAddress) throw SecurityException()""",
            remediationExplanation = "Never rely on string matching to detect loopback or private addresses. Parse and normalize the canonical IP address before socket connection."
        ),
        Lab(
            id = "lab_path_traversal_lfi",
            title = "Path Traversal: Local File Inclusion (LFI)",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-22: Improper Limitation of a Pathname to a Restricted Directory",
            cwe = "CWE-22",
            description = "Traverse above the intended public document folder by injecting `../` dot-dot-slash sequences to read `/etc/passwd`.",
            scenarioContext = "A document download endpoint appends the `file` parameter directly to the `/var/www/uploads/` path.",
            simulatedEndpoint = "/api/v1/download?file=../../../../etc/passwd",
            defaultMethod = "GET",
            defaultQueryParams = mapOf("file" to "../../../../etc/passwd"),
            hints = listOf(
                "Use `../` sequences to navigate up the directory hierarchy.",
                "Target standard Unix files like `/etc/passwd` or `/etc/hosts`."
            ),
            flag = "FLAG{path_traversal_passwd_leak_8302}",
            learningObjectives = listOf(
                "Understand canonical pathname resolution.",
                "Prevent path traversal using Path.normalize() and base directory boundary checks."
            ),
            remediationCodeSnippet = """// SECURE: Canonical path verification
val basePath = File("/var/www/uploads/").canonicalFile.toPath()
val requestedPath = basePath.resolve(userFilename).normalize()
if (!requestedPath.startsWith(basePath)) {
    throw SecurityException("Path traversal attempt detected")
}""",
            remediationExplanation = "Normalize paths using `toPath().normalize()` and verify that the target path begins strictly with the allowed base directory."
        ),
        Lab(
            id = "lab_xxe_external_entity",
            title = "XML External Entity (XXE) Injection",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-611: Improper Restriction of XML External Entity Reference",
            cwe = "CWE-611",
            description = "Exploit a misconfigured XML invoice parser that processes external DTD entity declarations to read local server files.",
            scenarioContext = "An invoice ingestion endpoint accepts XML payloads with external entity processing enabled by default.",
            simulatedEndpoint = "/api/v1/xml/parse-invoice",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/xml"),
            defaultBody = "<?xml version=\"1.0\"?><!DOCTYPE invoice [<!ENTITY xxe SYSTEM \"file:///etc/hostname\">]><invoice><id>&xxe;</id></invoice>",
            hints = listOf(
                "Declare an internal DTD with `<!DOCTYPE ...>` and `<!ENTITY xxe SYSTEM \"file:///etc/hostname\">`.",
                "Reference the entity `&xxe;` within one of the XML elements."
            ),
            flag = "FLAG{xxe_file_entity_disclosure_4910}",
            learningObjectives = listOf(
                "Understand how XML parsers resolve external general entities.",
                "Safely configure XML parsers to disable DTDs entirely (`disallow-doctype-decl`)."
            ),
            remediationCodeSnippet = """// SECURE: Disable DTDs and External Entities in XML Parser
val dbf = DocumentBuilderFactory.newInstance()
dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
dbf.isXIncludeAware = false
dbf.isExpandEntityReferences = false""",
            remediationExplanation = "Disable DOCTYPE declarations (`disallow-doctype-decl: true`) and external entity resolution in all XML parsers."
        ),
        Lab(
            id = "lab_file_upload_extension_bypass",
            title = "Insecure File Upload: Extension Blacklist Evasion",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-434: Unrestricted Upload of File with Dangerous Type",
            cwe = "CWE-434",
            description = "Bypass a naive `.php` blacklist filter by uploading an alternative executable PHP extension (`.phtml` or `.php5`) to execute code.",
            scenarioContext = "A user profile upload endpoint prevents `.php` files, but Apache is configured to execute `.phtml` and `.php5` files.",
            simulatedEndpoint = "/api/v1/upload",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "multipart/form-data"),
            defaultBody = "Content-Disposition: form-data; name=\"file\"; filename=\"shell.phtml\"\n\n<?php echo file_get_contents('/flag.txt'); ?>",
            hints = listOf(
                "The server checks if filename ends with `.php`.",
                "Try alternative executable extensions like `.phtml`, `.php5`, or `.phar`."
            ),
            flag = "FLAG{file_upload_phtml_exec_9051}",
            learningObjectives = listOf(
                "Understand why extension blacklists fail in web server environments.",
                "Implement strict allowlist extension checking and store uploads outside the webroot."
            ),
            remediationCodeSnippet = """// SECURE: Whitelist allowed extensions and randomize storage filename
val allowedExtensions = setOf("png", "jpg", "jpeg", "webp")
val extension = file.extension.lowercase()
if (!allowedExtensions.contains(extension)) throw InvalidFileTypeException()
val storageKey = UUID.randomUUID().toString() + "." + extension
// Store in private object storage (S3), never in executable webroot!""",
            remediationExplanation = "Use strict extension allowlists, rename uploaded files to random UUIDs, and store them outside the public webroot."
        ),
        Lab(
            id = "lab_info_disclosure_git_head",
            title = "Information Disclosure: Exposed .git Directory",
            category = LabCategory.SERVER_SIDE,
            difficulty = LabDifficulty.BEGINNER,
            vulnerabilityType = "CWE-538: Insertion of Sensitive Information into Externally-Accessible Directory",
            cwe = "CWE-538",
            description = "Discover a publicly exposed `.git/HEAD` file resulting from an improper web server deployment, uncovering commit history and credentials.",
            scenarioContext = "The web server root contains the live Git repository because the developer deployed using `git clone` into `/var/www/html`.",
            simulatedEndpoint = "/.git/HEAD",
            defaultMethod = "GET",
            hints = listOf(
                "Common deployment misconfigurations leave dotfiles like `/.git/HEAD` or `/.env` accessible.",
                "Request `/.git/HEAD` directly."
            ),
            flag = "FLAG{git_exposure_secrets_found_1409}",
            learningObjectives = listOf(
                "Understand risks of source code leakage through version control artifacts.",
                "Configure web servers (NGINX/Apache) to deny access to hidden files (`.*`)."
            ),
            remediationCodeSnippet = """# SECURE: NGINX block to deny all hidden files
location ~ /\. {
    deny all;
    return 404;
}""",
            remediationExplanation = "Configure web server rules to deny access to all hidden dotfiles and directories (`~ /\\.`), and avoid deploying `.git` folders to production."
        ),

        // Category 7: BUSINESS_LOGIC
        Lab(
            id = "lab_logic_price_tampering",
            title = "Business Logic: Negative Quantity & Price Tampering",
            category = LabCategory.BUSINESS_LOGIC,
            difficulty = LabDifficulty.BEGINNER,
            vulnerabilityType = "CWE-840: Business Logic Errors",
            cwe = "CWE-840",
            description = "Manipulate the checkout order parameters by supplying a negative quantity or modified unit price to acquire items for free.",
            scenarioContext = "An e-commerce checkout handler relies on client-submitted prices and fails to assert that `quantity > 0`.",
            simulatedEndpoint = "/api/v1/cart/checkout",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"item_id\": 99, \"quantity\": -1, \"unit_price\": 500}",
            hints = listOf(
                "Notice that the client sends `unit_price` in the JSON request body.",
                "Try changing `quantity` to a negative integer or `unit_price` to `0.01`."
            ),
            flag = "FLAG{logic_negative_quantity_cash_leak_6318}",
            learningObjectives = listOf(
                "Understand why prices and monetary calculations must always be authoritative on the server.",
                "Enforce positive integer constraints on quantities."
            ),
            remediationCodeSnippet = """// SECURE: Retrieve authoritative price from database
val item = itemRepository.findById(request.itemId) ?: throw NotFoundException()
if (request.quantity <= 0 || request.quantity > 100) throw BadRequestException("Invalid quantity")
val total = item.authoritativePrice.multiply(BigDecimal(request.quantity))""",
            remediationExplanation = "Never trust client-submitted prices. Always calculate totals server-side using authoritative database pricing and validate that quantities are positive."
        ),
        Lab(
            id = "lab_logic_coupon_stacking",
            title = "Business Logic: Infinite Coupon Stacking Replay",
            category = LabCategory.BUSINESS_LOGIC,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-840: Business Logic Error - Replay",
            cwe = "CWE-840",
            description = "Replay the single-use `WELCOME20` discount coupon multiple times within the same cart session to reduce the order total to \$0.00.",
            scenarioContext = "The coupon application handler applies a 20% discount without checking whether the coupon has already been applied in the session.",
            simulatedEndpoint = "/api/v1/cart/apply-discount",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"coupon\": \"WELCOME20\", \"repeat\": true, \"apply_count\": 5}",
            hints = listOf(
                "The discount endpoint applies 20% each time it is invoked.",
                "Simulate re-applying the coupon repeatedly until the discount reaches 100%."
            ),
            flag = "FLAG{logic_coupon_stacking_infinite_discount_4210}",
            learningObjectives = listOf(
                "Identify state machine flaws in transactional workflows.",
                "Implement state tracking and idempotent coupon application."
            ),
            remediationCodeSnippet = """// SECURE: Verify coupon hasn't been applied to cart
if (cart.appliedCoupons.contains(coupon.code)) {
    throw ConflictException("Coupon code has already been applied to this cart")
}
cart.appliedCoupons.add(coupon.code)""",
            remediationExplanation = "Store applied coupons in the cart state and assert uniqueness to prevent multiple applications of the same promo code."
        ),
        Lab(
            id = "lab_race_condition_double_spend",
            title = "Race Condition: Limit-Overrun Double Spend",
            category = LabCategory.BUSINESS_LOGIC,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-362: Concurrent Execution using Shared Resource with Improper Synchronization ('Race Condition')",
            cwe = "CWE-362",
            description = "Exploit a Time-of-Check to Time-of-Use (TOCTOU) gap by issuing concurrent transfer requests to drain more funds than the account balance.",
            scenarioContext = "A digital wallet checks account balance (`balance >= amount`), pauses for network verification, and then deducts the balance without database row locking.",
            simulatedEndpoint = "/api/v1/wallet/transfer",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"recipient\": \"attacker\", \"amount\": 100, \"concurrency_burst\": 5}",
            hints = listOf(
                "When multiple requests are executed in parallel, both may pass the check phase before either completes the deduction.",
                "Send requests with `concurrency_burst: 5` to trigger concurrent execution."
            ),
            flag = "FLAG{race_condition_toctou_double_spend_7781}",
            learningObjectives = listOf(
                "Understand Time-of-Check to Time-of-Use (TOCTOU) concurrency vulnerabilities.",
                "Implement pessimistic database locking (`SELECT ... FOR UPDATE`) or atomic balance deductions."
            ),
            remediationCodeSnippet = """// SECURE: Use atomic database update with balance guard
@Modifying
@Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.id = :id AND w.balance >= :amount")
fun debitAtomic(@Param("id") id: Long, @Param("amount") amount: BigDecimal): Int
// Returns 1 if successful, 0 if insufficient funds""",
            remediationExplanation = "Use atomic database operations or pessimistic row locks (`SELECT ... FOR UPDATE`) to prevent race conditions during financial transactions."
        ),

        // Category 8: SECURE_CODE_REVIEW
        Lab(
            id = "lab_code_review_sqli_sink",
            title = "Secure Code Review: Unescaped SQL Sink Identification",
            category = LabCategory.SECURE_CODE_REVIEW,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-89: Source Code Analysis of SQL Query Concatenation",
            cwe = "CWE-89",
            description = "Review a Java backend repository implementation, identify the exact vulnerable line containing an unescaped SQL sink, and specify remediation.",
            scenarioContext = "You are performing a secure code review of an account lookup service. Line 14 concatenates `req.getParameter(\"id\")` into a raw query string.",
            simulatedEndpoint = "/api/v1/code-review/audit-1",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"vulnerable_line\": 14, \"vulnerability\": \"SQL_INJECTION\", \"remediation\": \"PreparedStatement\"}",
            hints = listOf(
                "Review the snippet: line 14 uses `\"SELECT * FROM accounts WHERE id = \" + req.getParameter(\"id\")`.",
                "Submit line `14` with remediation `PreparedStatement`."
            ),
            flag = "FLAG{code_review_sqli_sink_identified_3108}",
            learningObjectives = listOf(
                "Perform source-to-sink taint analysis in application source code.",
                "Identify insecure database drivers and recommend prepared statements."
            ),
            remediationCodeSnippet = """// SECURE REWRITE:
val sql = "SELECT * FROM accounts WHERE id = ?"
connection.prepareStatement(sql).use { stmt ->
    stmt.setString(1, req.getParameter("id"))
    return stmt.executeQuery()
}""",
            remediationExplanation = "Always flag string concatenation in SQL queries during code reviews and enforce parameterized query standards."
        ),
        Lab(
            id = "lab_code_review_crypto_salt",
            title = "Secure Code Review: Hardcoded Encryption Keys",
            category = LabCategory.SECURE_CODE_REVIEW,
            difficulty = LabDifficulty.INTERMEDIATE,
            vulnerabilityType = "CWE-321: Use of Hard-coded Cryptographic Key",
            cwe = "CWE-321",
            description = "Audit an Android security class, identify hardcoded symmetric cryptographic keys and static IVs on line 8, and propose KeyStore migration.",
            scenarioContext = "A client app developer hardcoded an AES secret key `\"MySecretKey12345\"` on line 8 of `CryptoHelper.java`.",
            simulatedEndpoint = "/api/v1/code-review/audit-2",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"vulnerable_line\": 8, \"vulnerability\": \"HARDCODED_KEY\", \"remediation\": \"KeyStore\"}",
            hints = listOf(
                "Static keys compiled into bytecode can be extracted trivially with JADX.",
                "Line 8 declares the hardcoded byte array key."
            ),
            flag = "FLAG{code_review_hardcoded_crypto_key_5521}",
            learningObjectives = listOf(
                "Recognize hardcoded secrets in mobile codebases.",
                "Implement hardware-backed cryptographic keys with Android KeyStore."
            ),
            remediationCodeSnippet = """// SECURE: Use Android KeyStore for hardware-backed keys
val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
val keyGenParameterSpec = KeyGenParameterSpec.Builder(
    "master_key",
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
 .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
 .build()
keyGenerator.init(keyGenParameterSpec)
keyGenerator.generateKey()""",
            remediationExplanation = "Never hardcode cryptographic keys in code. Use the Android KeyStore provider to store keys in hardware-backed secure elements."
        ),
        Lab(
            id = "lab_graphql_introspection_abuse",
            title = "GraphQL: Introspection & Hidden Field Discovery",
            category = LabCategory.SECURE_CODE_REVIEW,
            difficulty = LabDifficulty.ADVANCED,
            vulnerabilityType = "CWE-200: Information Exposure via GraphQL Introspection",
            cwe = "CWE-200",
            description = "Execute a GraphQL introspection query against a production endpoint to map out hidden queries, sensitive fields, and administrative mutations.",
            scenarioContext = "A production GraphQL API has introspection enabled. Probing `__schema` reveals secret administrative mutations and debug API tokens.",
            simulatedEndpoint = "/graphql",
            defaultMethod = "POST",
            defaultHeaders = mapOf("Content-Type" to "application/json"),
            defaultBody = "{\"query\": \"{ __schema { types { name fields { name } } } }\"}",
            hints = listOf(
                "Query `__schema` or `__type` meta-fields to retrieve the complete schema definition.",
                "Notice the administrative mutation `deleteUserDatabase` uncovered in the schema."
            ),
            flag = "FLAG{graphql_introspection_hidden_schema_8830}",
            learningObjectives = listOf(
                "Understand GraphQL introspection and attack surface mapping.",
                "Disable introspection in production and enforce query depth/cost limits."
            ),
            remediationCodeSnippet = """// SECURE: Disable introspection in production builds
val graphQL = GraphQL.newGraphQL(schema)
    .instrumentation(if (isProduction) MaxQueryDepthInstrumentation(5) else null)
    .build()
// In Apollo / GraphQL Java: disable IntrospectionExecutionRule in production""",
            remediationExplanation = "Disable GraphQL schema introspection in production environments and enforce query depth and complexity limits."
        )
    )

    fun getAllLabs(): List<Lab> = labsList

    fun getLabById(labId: String): Lab? = labsList.firstOrNull { it.id == labId }

    fun getLabsByCategory(category: LabCategory): List<Lab> = labsList.filter { it.category == category }

    fun observeLabProgress(labId: String): Flow<LabProgressEntity?> = labProgressDao.observeLabProgress(labId)

    fun observeAllLabProgress(): Flow<List<LabProgressEntity>> = labProgressDao.getAllLabProgress()

    fun getCompletedLabsCount(): Flow<Int> = labProgressDao.getCompletedLabsCount()

    suspend fun executeLab(
        labId: String,
        method: String,
        endpoint: String,
        headers: Map<String, String>,
        body: String,
        queryParams: Map<String, String>
    ): LabExecutionResult {
        val result = SafeDeterministicLabEngine.execute(
            labId = labId,
            method = method,
            endpoint = endpoint,
            headers = headers,
            body = body,
            queryParams = queryParams
        )

        val existing = labProgressDao.getLabProgress(labId)
        val currentAttempts = (existing?.attemptsCount ?: 0) + 1

        if (result.flagAwarded != null && existing?.isCompleted != true) {
            val lab = getLabById(labId)
            val xpReward = lab?.difficulty?.xpReward ?: 100

            labProgressDao.insertOrUpdate(
                LabProgressEntity(
                    labId = labId,
                    isCompleted = true,
                    flagCaptured = result.flagAwarded,
                    attemptsCount = currentAttempts,
                    completedAt = System.currentTimeMillis(),
                    lastSubmittedPayload = body.ifBlank { endpoint }
                )
            )
            userPreferencesDataStore.addXp(xpReward)
            userPreferencesDataStore.recordActivityToday()
        } else {
            labProgressDao.insertOrUpdate(
                LabProgressEntity(
                    labId = labId,
                    isCompleted = existing?.isCompleted ?: false,
                    flagCaptured = existing?.flagCaptured,
                    attemptsCount = currentAttempts,
                    completedAt = existing?.completedAt,
                    lastSubmittedPayload = body.ifBlank { endpoint }
                )
            )
        }

        return result
    }

    suspend fun submitManualFlag(labId: String, submittedFlag: String): Boolean {
        val lab = getLabById(labId) ?: return false
        val cleanFlag = submittedFlag.trim()
        val isValid = cleanFlag == lab.flag

        val existing = labProgressDao.getLabProgress(labId)
        val currentAttempts = (existing?.attemptsCount ?: 0) + 1

        if (isValid && existing?.isCompleted != true) {
            labProgressDao.insertOrUpdate(
                LabProgressEntity(
                    labId = labId,
                    isCompleted = true,
                    flagCaptured = cleanFlag,
                    attemptsCount = currentAttempts,
                    completedAt = System.currentTimeMillis(),
                    lastSubmittedPayload = cleanFlag
                )
            )
            userPreferencesDataStore.addXp(lab.difficulty.xpReward)
            userPreferencesDataStore.recordActivityToday()
        }
        return isValid
    }
}
