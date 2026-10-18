package com.bugbounty.academy.data.repository

import com.bugbounty.academy.data.local.dao.UserProgressDao
import com.bugbounty.academy.data.local.entity.LessonProgressEntity
import com.bugbounty.academy.data.preferences.UserPreferencesDataStore
import com.bugbounty.academy.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurriculumRepository(
    private val userProgressDao: UserProgressDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val modulesList: List<Module> = listOf(
        Module(
            id = "mod_http_fundamentals",
            order = 1,
            title = "HTTP Protocol & Web Foundations",
            category = ModuleCategory.FOUNDATIONS,
            description = "Master the stateless HTTP protocol, request/response cycles, header injection, cookie attributes, Same-Origin Policy, and CORS.",
            iconName = "http",
            estimatedMinutes = 45,
            lessons = listOf(
                Lesson(
                    id = "lesson_http_mechanics",
                    moduleId = "mod_http_fundamentals",
                    title = "HTTP Request/Response Architecture & Methods",
                    summary = "Examine the wire format of HTTP/1.1 and HTTP/2, idempotency of methods (GET, POST, PUT, DELETE, PATCH, OPTIONS), and header mechanics.",
                    contentMarkdown = """# HTTP Request/Response Architecture & Methods

The Hypertext Transfer Protocol (HTTP) is the foundational application-layer protocol powering the World Wide Web. Understanding its precise wire format is essential for any application security tester.

### 1. HTTP/1.1 Message Structure
An HTTP request consists of:
- **Request Line**: `METHOD /path HTTP/1.1`
- **Headers**: Key-value pairs separated by colons (e.g., `Host: target.com`, `User-Agent: ...`)
- **Empty Line (CRLF `\r\n`)**: Signals the boundary between headers and message body
- **Optional Body**: Payload data (JSON, XML, form-urlencoded, or multipart)

### 2. Method Semantics & Idempotency
- **GET**: Safe and idempotent. Must not trigger state changes on the server.
- **POST**: Non-idempotent. Creates subordinate resources or processes transactions.
- **PUT**: Idempotent resource replacement.
- **PATCH**: Partial resource modification.
- **DELETE**: Idempotent deletion.
- **OPTIONS**: Returns supported HTTP methods for CORS preflight checks.
- **HEAD**: Identical to GET but omits the response body (useful for asset inspection).

### 3. Security Considerations
- **HTTP Parameter Pollution (HPP)**: Exploiting variations in how web servers (Apache vs IIS vs Express) handle duplicate query parameters.
- **Header Injection & Smuggling**: Injecting carriage returns and line feeds (`\r\n`) into headers when proxies and origin servers disagree on `Content-Length` vs `Transfer-Encoding: chunked`.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "HTTP is stateless; sessions are maintained via cookies or Bearer tokens.",
                        "GET requests must never cause server-side state transitions.",
                        "Discrepancies in HTTP parsing between reverse proxies and backends lead to request smuggling."
                    ),
                    references = listOf(
                        LearningReference("RFC 9110 - HTTP Semantics", "IETF", "https://www.rfc-editor.org/rfc/rfc9110.html", ReferenceType.RFC),
                        LearningReference("PortSwigger: HTTP Request Smuggling", "PortSwigger", "https://portswigger.net/web-security/request-smuggling", ReferenceType.PORTSWIGGER)
                    ),
                    associatedQuizId = "quiz_http_fundamentals"
                ),
                Lesson(
                    id = "lesson_cookies_cors_sop",
                    moduleId = "mod_http_fundamentals",
                    title = "Same-Origin Policy, CORS & Cookie Hardening",
                    summary = "Deep dive into browser security boundaries: Same-Origin Policy (SOP), Cross-Origin Resource Sharing (CORS), and cookie protection flags.",
                    contentMarkdown = """# Same-Origin Policy, CORS & Cookie Hardening

The browser's primary security guarantee is the **Same-Origin Policy (SOP)**.

### 1. Same-Origin Policy (SOP)
An origin is defined strictly by the tuple: **(Scheme, Host, Port)**.
- `https://example.com:443` and `http://example.com:80` are **different origins** (different scheme/port).
- `https://app.example.com` and `https://api.example.com` are **different origins** (different host).

SOP prevents client-side scripts loaded from Origin A from reading arbitrary responses from Origin B, mitigating cross-tenant data theft.

### 2. Cross-Origin Resource Sharing (CORS)
CORS provides an explicit relaxation mechanism for SOP via HTTP response headers:
- `Access-Control-Allow-Origin: <origin>`
- `Access-Control-Allow-Methods: GET, POST, OPTIONS`
- `Access-Control-Allow-Headers: Authorization, Content-Type`
- `Access-Control-Allow-Credentials: true`

**Critical Vulnerability**: Setting `Access-Control-Allow-Origin: *` with credentials enabled is prohibited by browsers, but servers often misconfigure this by reflecting the incoming `Origin` header dynamically!

### 3. Cookie Hardening Flags
- `Secure`: Cookie is transmitted exclusively over encrypted TLS connections.
- `HttpOnly`: Blocks client-side JavaScript (`document.cookie`) from accessing the cookie, thwarting XSS-based session hijacking.
- `SameSite`: Controls whether cookies are sent on cross-site requests:
  - `Strict`: Never sent on cross-site requests.
  - `Lax`: Sent on top-level GET navigations (default in modern Chromium).
  - `None`: Sent on all cross-site requests (requires `Secure`).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Same-Origin Policy enforces isolation between different origins based on scheme, host, and port.",
                        "CORS does not protect endpoints from receiving requests; it controls browser read permissions.",
                        "Always apply `Secure`, `HttpOnly`, and `SameSite=Lax` or `Strict` to authentication cookies."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-942", "Overly Permissive Cross-Domain Policy", "Web server permits unauthorized cross-origin data access", "https://cwe.mitre.org/data/definitions/942.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP Cookie Security Guide", "OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html", ReferenceType.OWASP),
                        LearningReference("PortSwigger CORS Vulnerabilities", "PortSwigger", "https://portswigger.net/web-security/cors", ReferenceType.PORTSWIGGER)
                    ),
                    associatedLabId = "lab_cors_misconfiguration"
                )
            )
        ),
        Module(
            id = "mod_networking_infra",
            order = 2,
            title = "Networking & Target Infrastructure",
            category = ModuleCategory.FOUNDATIONS,
            description = "DNS resolution, TCP/UDP transport, TLS 1.3 handshake, reverse proxies, CDNs, and Web Application Firewalls (WAF).",
            iconName = "lan",
            estimatedMinutes = 40,
            lessons = listOf(
                Lesson(
                    id = "lesson_dns_tls_tcp",
                    moduleId = "mod_networking_infra",
                    title = "DNS, TCP Handshakes & TLS 1.3 Inspection",
                    summary = "Analyze the transport and cryptographic layers underlying secure web communication and understand certificate transparency.",
                    contentMarkdown = """# DNS, TCP Handshakes & TLS 1.3 Inspection

Security researchers must understand the lower levels of the network stack to recognize origin IP disclosures, DNS rebinding, and TLS misconfigurations.

### 1. DNS & Certificate Transparency (CT)
- DNS maps human-readable domain names to IPv4 (`A`) or IPv6 (`AAAA`) addresses.
- **Certificate Transparency logs** (`crt.sh`) record every publicly issued SSL/TLS certificate, making them an indispensable OSINT source for discovering forgotten subdomains and internal staging environments.

### 2. TCP 3-Way Handshake
Before an HTTP request can be transmitted, a TCP connection is established:
1. Client sends `SYN` (Synchronize sequence numbers)
2. Server responds with `SYN-ACK`
3. Client acknowledges with `ACK`

Port scanners like **Nmap** exploit this state machine:
- **SYN Stealth Scan (`-sS`)**: Sends `SYN`. If server replies `SYN-ACK`, the port is open; client immediately sends `RST` to tear down the connection before full logging occurs.

### 3. TLS 1.3 Handshake & Cipher Suites
TLS 1.3 optimizes the handshake to 1-RTT (Round Trip Time) and mandates forward secrecy:
- Client Hello includes supported cipher suites and key share.
- Server Hello selects cipher suite, issues certificate, and computes ephemeral Diffie-Hellman shared key.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Certificate Transparency logs provide exhaustive public records of subdomains.",
                        "TCP SYN scans identify open ports without completing the full 3-way handshake.",
                        "TLS ensures confidentiality, integrity, and server authentication."
                    ),
                    references = listOf(
                        LearningReference("RFC 8446 - TLS 1.3", "IETF", "https://www.rfc-editor.org/rfc/rfc8446.html", ReferenceType.RFC),
                        LearningReference("OWASP Transport Layer Protection", "OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html", ReferenceType.OWASP)
                    )
                )
            )
        ),
        Module(
            id = "mod_linux_cli",
            order = 3,
            title = "Linux for Hackers & CLI Masterclass",
            category = ModuleCategory.FOUNDATIONS,
            description = "Linux directory hierarchy, core utilities, pipeline automation, grep, sed, awk, regex, and file permissions.",
            iconName = "terminal",
            estimatedMinutes = 50,
            lessons = listOf(
                Lesson(
                    id = "lesson_bash_recon_pipelines",
                    moduleId = "mod_linux_cli",
                    title = "Bash Pipelines, Regex & Recon Automation",
                    summary = "Build powerful shell pipelines combining curl, jq, grep, awk, and sed to parse thousands of subdomains, endpoints, and secrets.",
                    contentMarkdown = """# Bash Pipelines, Regex & Recon Automation

Top bug bounty hunters automate tedious data collection using high-speed Unix pipelines.

### 1. Essential Unix Tools for Bug Bounty
- `curl -s`: Silent HTTP requests for programmatic inspection.
- `jq`: Command-line JSON processor for querying API responses.
- `grep -E`: Extended regular expressions for pattern matching (API keys, tokens, emails).
- `awk`: Column-oriented text processing and formatting.
- `sed`: Stream editor for text substitution and cleaning.
- `sort -u`: Sorting and deduplicating wordlists and targets.

### 2. Practical Recon One-Liners
Extract unique alive subdomains from crt.sh:
```bash
curl -s "https://crt.sh/?q=%25.example.com&output=json" | jq -r '.[].name_value' | sed 's/\*\.//g' | sort -u > subdomains.txt
```

Extract all JavaScript endpoints from target HTML:
```bash
curl -s "https://target.com" | grep -Eo '(https?://[^"]+|\/[^" >]+\.js)' | sort -u
```
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Unix pipelines allow combining lightweight CLI utilities into scalable reconnaissance workflows.",
                        "`jq` is the standard tool for filtering JSON responses in bug bounty automation.",
                        "Regex pattern matching uncovers secrets and tokens in client-side bundles."
                    ),
                    references = listOf(
                        LearningReference("Linux Documentation Project", "TLDP", "https://tldp.org/LDP/Bash-Beginners-Guide/html/", ReferenceType.OWASP)
                    )
                )
            )
        ),
        Module(
            id = "mod_recon_osint",
            order = 4,
            title = "Reconnaissance & OSINT",
            category = ModuleCategory.RECONNAISSANCE,
            description = "Subdomain enumeration, ASN discovery, port scanning, Wayback Machine mining, Shodan asset mapping, and JavaScript secret extraction.",
            iconName = "radar",
            estimatedMinutes = 60,
            lessons = listOf(
                Lesson(
                    id = "lesson_recon_attack_surface",
                    moduleId = "mod_recon_osint",
                    title = "Mapping the Attack Surface & Asset Discovery",
                    summary = "Combine passive DNS, Certificate Transparency, ASN exploration, and active DNS brute-forcing to identify shadow assets.",
                    contentMarkdown = """# Mapping the Attack Surface & Asset Discovery

The wider your reconnaissance, the less crowded your targets. The most critical vulnerabilities are often discovered on forgotten staging servers, obsolete microservices, and shadow IT infrastructure.

### 1. Autonomous System Numbers (ASN) & IP Ranges
Before hunting subdomains, identify the target organization's registered network IP blocks:
- Query Hurricane Electric (`bgp.he.net`) or Amass to find ASNs.
- Enumerate CIDR ranges belonging to the enterprise.

### 2. Subdomain Enumeration Methodology
- **Passive Enumeration**: Query public aggregation sources (SecurityTrails, AlienVault OTX, crt.sh, VirusTotal, Shodan) without sending traffic directly to the target.
- **Active Enumeration**: High-speed DNS brute-forcing using massive wordlists (SecLists) with tools like `shuffledns` or `puredns`.
- **Permutation & Mutation**: Generating smart permutations of discovered hosts (`admin-dev.target.com`, `api-staging.target.com`) using `altdns`.

### 3. JavaScript Analysis
Modern single-page applications leak backend endpoints, secret API keys, and internal routes inside client-side JS bundles:
- Extract URLs from JS files using tools like `LinkFinder` or `katana`.
- Search for exposed keys (`AKIA...`, `Bearer eyJ...`, `AIzaSy...`).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Passive recon gathers intelligence without triggering IDS/WAF alerts on target systems.",
                        "JavaScript bundles often expose unauthenticated staging endpoints and developer notes.",
                        "Always verify that discovered subdomains are strictly within the bug bounty program scope."
                    ),
                    references = listOf(
                        LearningReference("OWASP WSTG - Conduct Search Engine Discovery", "OWASP", "https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/01-Information_Gathering/01-Conduct_Search_Engine_Discovery_Reconnaissance_for_Information_Leakage", ReferenceType.OWASP),
                        LearningReference("Bugcrowd Vulnerability Rating Taxonomy", "Bugcrowd", "https://bugcrowd.com/vulnerability-rating-taxonomy", ReferenceType.BUGCROWD)
                    )
                )
            )
        ),
        Module(
            id = "mod_owasp_core",
            order = 5,
            title = "OWASP Top 10 Web Vulnerabilities",
            category = ModuleCategory.OWASP_CORE,
            description = "In-depth exploitation and defense for SQLi, XSS, CSRF, SSRF, IDOR, XXE, Command Injection, and Path Traversal.",
            iconName = "bug_report",
            estimatedMinutes = 90,
            lessons = listOf(
                Lesson(
                    id = "lesson_sqli_deepdive",
                    moduleId = "mod_owasp_core",
                    title = "SQL Injection (SQLi) Mechanics & Exploitation",
                    summary = "Analyze In-Band (UNION/Error), Inferential (Boolean/Time), and Out-of-Band SQL injection techniques.",
                    contentMarkdown = """# SQL Injection (SQLi) Mechanics & Exploitation

SQL Injection occurs when untrusted user input is directly concatenated into dynamic database queries, altering the execution logic of the database engine.

### 1. In-Band SQLi (UNION-Based)
When the application reflects database query results directly in the HTTP response:
1. Determine the number of columns returned:
   `' ORDER BY 1--`, `' ORDER BY 2--` until an error occurs.
2. Determine compatible data types:
   `' UNION SELECT 'test', null, null--`
3. Extract data from system tables:
   - PostgreSQL/MySQL: `SELECT table_name FROM information_schema.tables`
   - SQLite: `SELECT tbl_name FROM sqlite_master`

### 2. Blind SQLi (Boolean & Time-Based)
When no error messages or query results are reflected:
- **Boolean Inference**: `AND 1=1` vs `AND 1=2`. If response body length changes, infer true/false.
- **Time-Based Inference**: `AND (SELECT SLEEP(5))` or `pg_sleep(5)`. If the response pauses for 5 seconds, the condition evaluated to true.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "SQLi allows reading database tables, bypassing authentication, and sometimes executing OS commands (`xp_cmdshell`).",
                        "Prepared statements with bind parameters completely eliminate SQL injection.",
                        "Suppression of error messages does not prevent blind SQL injection."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-89", "Improper Neutralization of Special Elements used in an SQL Command", "Direct SQL concatenation enables database subversion", "https://cwe.mitre.org/data/definitions/89.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP SQL Injection Prevention Cheat Sheet", "OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html", ReferenceType.OWASP),
                        LearningReference("PortSwigger SQL Injection Guide", "PortSwigger", "https://portswigger.net/web-security/sql-injection", ReferenceType.PORTSWIGGER)
                    ),
                    associatedLabId = "lab_sqli_auth_bypass",
                    associatedQuizId = "quiz_owasp_core"
                ),
                Lesson(
                    id = "lesson_xss_deepdive",
                    moduleId = "mod_owasp_core",
                    title = "Cross-Site Scripting (XSS): Stored, Reflected & DOM",
                    summary = "Master client-side payload crafting, context-aware escaping, cookie theft, DOM sinks, and CSP bypasses.",
                    contentMarkdown = """# Cross-Site Scripting (XSS): Stored, Reflected & DOM

Cross-Site Scripting allows attackers to execute malicious JavaScript in the victim's browser within the security context of the vulnerable application.

### 1. The Three Flavors of XSS
- **Reflected XSS**: Payload is delivered via HTTP request parameters and immediately reflected in the response page.
- **Stored XSS (Persistent)**: Payload is permanently stored in the database (e.g. comment, profile bio) and served to anyone viewing the resource.
- **DOM-Based XSS**: The vulnerability resides entirely in client-side code; an unsafe source (e.g. `location.hash`) is passed into an execution sink (e.g. `innerHTML`, `eval`).

### 2. Impact & Exploitation
- Session Hijacking via `document.cookie` (if `HttpOnly` is missing).
- Keystroke logging and fake credential prompt injection.
- Client-side actions performed on behalf of the victim (CSRF via XSS).
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "XSS executes in the user's browser, bypassing network firewalls and perimeters.",
                        "Context matters: HTML body, attribute values, and script blocks require different encoding strategies.",
                        "Content Security Policy (CSP) acts as a defense-in-depth barrier against XSS."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-79", "Improper Neutralization of Input During Web Page Generation", "Unsanitized user input rendered into executable browser context", "https://cwe.mitre.org/data/definitions/79.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP XSS Prevention Cheat Sheet", "OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html", ReferenceType.OWASP)
                    ),
                    associatedLabId = "lab_stored_xss"
                )
            )
        ),
        Module(
            id = "mod_api_security",
            order = 6,
            title = "API Security & Microservices",
            category = ModuleCategory.API_AND_AUTH,
            description = "RESTful vulnerabilities, OWASP API Security Top 10, BOLA, BFLA, mass assignment, unauthenticated routes, and rate-limiting bypasses.",
            iconName = "api",
            estimatedMinutes = 60,
            lessons = listOf(
                Lesson(
                    id = "lesson_owasp_api_top10",
                    moduleId = "mod_api_security",
                    title = "OWASP API Security Top 10 & Broken Object Authorization",
                    summary = "Understand Broken Object Level Authorization (API1:2023 BOLA) and Broken Function Level Authorization (API5:2023 BFLA).",
                    contentMarkdown = """# OWASP API Security Top 10 & Broken Object Authorization

Modern applications rely on APIs as their communication backbone. APIs frequently decouple presentation from logic, creating unique attack surfaces.

### 1. API1:2023 Broken Object Level Authorization (BOLA)
BOLA occurs when an endpoint accepts a user-supplied object identifier (e.g. `/api/v1/users/{id}`) without validating that the authenticated caller has permission to access that specific object.
- **Root Cause**: Reliance on client state and missing database-level ownership checks (`WHERE id = ? AND tenant_id = ?`).

### 2. API3:2023 Broken Object Property Level Authorization
Includes **Mass Assignment** and excessive data exposure:
- Clients submit extra fields in JSON (e.g. `is_admin: true`, `role: "vip"`) that frameworks automatically bind into internal models.

### 3. API4:2023 Unrestricted Resource Consumption
- Lack of rate limiting on sensitive workflows (SMS OTP generation, password resets, export jobs).
- Missing execution timeouts or memory bounds.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "BOLA/IDOR is the #1 most reported and highest-paid vulnerability on bug bounty platforms.",
                        "Never trust IDs in request parameters without verifying ownership against session credentials.",
                        "Use strict Data Transfer Objects (DTOs) to block mass assignment."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-639", "Authorization Bypass Through User-Controlled Key", "Insecure direct object reference exposes cross-tenant records", "https://cwe.mitre.org/data/definitions/639.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP API Security Top 10", "OWASP", "https://owasp.org/www-project-api-security/", ReferenceType.OWASP)
                    ),
                    associatedLabId = "lab_idor_horizontal",
                    associatedQuizId = "quiz_api_security"
                )
            )
        ),
        Module(
            id = "mod_auth_oauth_jwt",
            order = 7,
            title = "OAuth 2.0 & JWT Security",
            category = ModuleCategory.API_AND_AUTH,
            description = "OAuth 2.0 grant types, state CSRF, redirect URI hijacking, PKCE, and JWT vulnerabilities (None algorithm, key confusion, weak secrets).",
            iconName = "vpn_key",
            estimatedMinutes = 65,
            lessons = listOf(
                Lesson(
                    id = "lesson_jwt_oauth_mastery",
                    moduleId = "mod_auth_oauth_jwt",
                    title = "JWT Attack Vectors & OAuth 2.0 Account Takeover",
                    summary = "Analyze the structure of JSON Web Tokens, cryptographic signature attacks, and OAuth flow hijack techniques.",
                    contentMarkdown = """# JWT Attack Vectors & OAuth 2.0 Account Takeover

Decentralized authentication relies heavily on signed tokens and delegated authorization protocols.

### 1. JWT Structure (RFC 7519)
A JWT consists of three Base64URL-encoded parts separated by dots:
`Header.Payload.Signature`
- **Header**: Declares algorithm (`alg`) and token type (`typ`).
- **Payload**: Claims (e.g. `sub`, `exp`, `role`).
- **Signature**: Cryptographic proof of authenticity.

### 2. Primary JWT Vulnerabilities
1. **The `none` Algorithm Flaw**: Modifying the header to `{"alg": "none"}` and stripping the signature. Vulnerable libraries accept unsigned tokens as valid!
2. **Weak HMAC Secrets**: If a symmetric secret has low entropy (`secret`, `password123`), attackers can crack it offline using Hashcat and forge arbitrary tokens.
3. **Algorithm Confusion (HMAC vs RSA)**: If the backend uses RS256, attackers sign a forged token using HMAC-SHA256 with the server's public key as the symmetric secret!

### 3. OAuth 2.0 Flaws
- **Missing `state` Parameter**: Allows CSRF attacks that trick victims into associating attacker accounts or consuming attacker authorization codes.
- **Flawed `redirect_uri` Validation**: Open redirection or regex flaws allow exfiltrating authorization codes to attacker-controlled hosts.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "JWT signatures only protect integrity if the server enforces strict algorithm checks.",
                        "OAuth state parameters are non-optional security controls required to defeat CSRF.",
                        "Always validate OAuth redirect URIs against an exact whitelist."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-347", "Improper Verification of Cryptographic Signature", "Accepting unverified or forged token signatures", "https://cwe.mitre.org/data/definitions/347.html")
                    ),
                    references = listOf(
                        LearningReference("RFC 7519 - JSON Web Token (JWT)", "IETF", "https://www.rfc-editor.org/rfc/rfc7519.html", ReferenceType.RFC),
                        LearningReference("PortSwigger: JWT Attacks", "PortSwigger", "https://portswigger.net/web-security/jwt", ReferenceType.PORTSWIGGER)
                    ),
                    associatedLabId = "lab_jwt_none_algorithm",
                    associatedQuizId = "quiz_oauth_jwt"
                )
            )
        ),
        Module(
            id = "mod_business_logic_race",
            order = 8,
            title = "Business Logic & Concurrency Flaws",
            category = ModuleCategory.ADVANCED_EXPLOITATION,
            description = "Price tampering, multi-step state bypasses, coupon stacking, Time-of-Check to Time-of-Use (TOCTOU) race conditions, and limit overruns.",
            iconName = "psychology",
            estimatedMinutes = 55,
            lessons = listOf(
                Lesson(
                    id = "lesson_race_conditions_logic",
                    moduleId = "mod_business_logic_race",
                    title = "Exploiting Logic Flaws & Concurrency Race Conditions",
                    summary = "Learn how to find flaws that scanners cannot detect: logical flaws in business rules and microsecond race conditions.",
                    contentMarkdown = """# Exploiting Logic Flaws & Concurrency Race Conditions

Business logic vulnerabilities exist in the application's unique operational workflow. Because automated scanners have no understanding of business rules, these bugs are predominantly discovered by human researchers.

### 1. Classic Business Logic Vulnerabilities
- **Negative Quantities**: Ordering `-5` items to receive a credit refund or zero out the cart total.
- **Workflow State Skipping**: Accessing `/checkout/confirmation` directly without passing through `/checkout/payment`.
- **Coupon Stacking**: Replaying a one-time promo code simultaneously across multiple browser tabs.

### 2. Concurrency & Race Conditions (TOCTOU)
A **Time-of-Check to Time-of-Use (TOCTOU)** race condition occurs when there is a delay between verifying a condition and performing the associated state transition.
- **Scenario**: You have \$100 in your account. You send 10 concurrent requests to withdraw \$100 within a 5-millisecond window.
- If the server reads the balance for all 10 threads before writing the new balance to the database, multiple withdrawals succeed!
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Automated tools fail to detect logic flaws; manual testing is required.",
                        "Never trust client-side prices, discounts, or quantity assertions.",
                        "Mitigate race conditions using database transactions with row-level locking or idempotent keys."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-362", "Improper Synchronization (Race Condition)", "Concurrent operations leading to invalid states or double spend", "https://cwe.mitre.org/data/definitions/362.html")
                    ),
                    references = listOf(
                        LearningReference("PortSwigger Race Conditions Guide", "PortSwigger", "https://portswigger.net/web-security/race-conditions", ReferenceType.PORTSWIGGER)
                    ),
                    associatedLabId = "lab_race_condition_double_spend",
                    associatedQuizId = "quiz_business_logic"
                )
            )
        ),
        Module(
            id = "mod_graphql_security",
            order = 9,
            title = "GraphQL Attacks & Defense",
            category = ModuleCategory.ADVANCED_EXPLOITATION,
            description = "Introspection abuse, circular query depth DoS, batching attacks, field-level authorization bypass, and mutation security.",
            iconName = "hub",
            estimatedMinutes = 50,
            lessons = listOf(
                Lesson(
                    id = "lesson_graphql_mechanics",
                    moduleId = "mod_graphql_security",
                    title = "GraphQL Introspection, Depth Attacks & Field Authorization",
                    summary = "Analyze the single-endpoint architecture of GraphQL and understand how to discover hidden schema objects and abuse query nesting.",
                    contentMarkdown = """# GraphQL Introspection, Depth Attacks & Field Authorization

GraphQL provides a declarative, strongly-typed query language over a single endpoint (typically `/graphql`). While flexible, it introduces novel security challenges.

### 1. Schema Introspection
When introspection is enabled, clients can query the `__schema` and `__type` meta-fields to retrieve every query, mutation, subscription, type, and field defined in the system.
- Uncovers internal administrative actions (e.g. `deleteUserDatabase`).
- Exposes private API tokens and deprecated debug fields.

### 2. Query Depth & Circular Relationship DoS
GraphQL schemas often define circular references (e.g., `Thread -> Author -> Threads -> Author`). An attacker can nest queries hundreds of levels deep:
```graphql
query {
  user {
    friends {
      friends {
        friends {
          friends { id }
        }
      }
    }
  }
}
```
Without query depth limiting, this exhausts backend CPU and memory.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Disable GraphQL introspection in production environments.",
                        "Enforce query depth analysis and maximum query complexity calculators.",
                        "Enforce authorization at the individual resolver/field level."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-200", "Exposure of Sensitive Information via Introspection", "Schema introspection exposes private operational structures", "https://cwe.mitre.org/data/definitions/200.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP GraphQL Cheat Sheet", "OWASP", "https://cheatsheetseries.owasp.org/cheatsheets/GraphQL_Cheat_Sheet.html", ReferenceType.OWASP)
                    ),
                    associatedLabId = "lab_graphql_introspection_abuse"
                )
            )
        ),
        Module(
            id = "mod_secure_code_review",
            order = 10,
            title = "Secure Code Review & Remediation",
            category = ModuleCategory.DEFENSE_AND_ETHICS,
            description = "Static code analysis, identifying sources and sinks, regex DoS (ReDoS), insecure deserialization, and defense-in-depth engineering.",
            iconName = "code",
            estimatedMinutes = 60,
            lessons = listOf(
                Lesson(
                    id = "lesson_taint_analysis_review",
                    moduleId = "mod_secure_code_review",
                    title = "Source-to-Sink Taint Analysis & Code Remediation",
                    summary = "Trace untrusted user inputs through code paths to dangerous execution sinks in Kotlin, Java, Python, and JavaScript.",
                    contentMarkdown = """# Source-to-Sink Taint Analysis & Code Remediation

White-box security testing and secure code review involve auditing source code to detect vulnerabilities before deployment.

### 1. Sources and Sinks
- **Source**: Any entry point where external untrusted data enters the program (`request.getParameter()`, `headers`, `body`, `cookies`, RPC args).
- **Sanitizer / Validator**: Functions that encode, type-check, or validate inputs.
- **Sink**: Dangerous functions or APIs that execute data as code:
  - SQL Execution: `Statement.execute(query)`
  - Command Execution: `Runtime.getRuntime().exec()`, `ProcessBuilder`
  - Code Evaluation: `eval()`, `scriptEngine.eval()`
  - HTML DOM Sink: `innerHTML`, `outerHTML`, `document.write`
  - Deserialization: `ObjectInputStream.readObject()`, `pickle.loads()`

### 2. Defense-in-Depth Remediation
Remediation is not just filtering; it requires architectural resilience:
- Prefer type-safe compile-time abstractions over runtime string manipulation.
- Principle of Least Privilege: Restrict database accounts and OS daemon permissions.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Taint analysis tracks untrusted sources to dangerous sinks.",
                        "Always parameterize SQL, encode HTML output, and avoid system shell invocations.",
                        "Never deserialize untrusted byte streams without cryptographic integrity verification."
                    ),
                    cweInfo = listOf(
                        CWEInfo("CWE-502", "Deserialization of Untrusted Data", "Insecure object deserialization leads to remote code execution", "https://cwe.mitre.org/data/definitions/502.html")
                    ),
                    references = listOf(
                        LearningReference("OWASP Code Review Guide", "OWASP", "https://owasp.org/www-project-code-review-guide/", ReferenceType.OWASP)
                    ),
                    associatedLabId = "lab_code_review_sqli_sink"
                )
            )
        ),
        Module(
            id = "mod_ethics_reporting",
            order = 11,
            title = "Responsible Disclosure & Report Writing",
            category = ModuleCategory.DEFENSE_AND_ETHICS,
            description = "Rules of engagement, Safe Harbor protections, CVSS v3.1 scoring, writing impactful reports, and triage etiquette.",
            iconName = "verified_user",
            estimatedMinutes = 45,
            lessons = listOf(
                Lesson(
                    id = "lesson_report_craft_ethics",
                    moduleId = "mod_ethics_reporting",
                    title = "Crafting High-Impact Reports & Triage Etiquette",
                    summary = "Learn how to structure vulnerability submissions on HackerOne and Bugcrowd to maximize payouts and minimize triage friction.",
                    contentMarkdown = """# Crafting High-Impact Reports & Triage Etiquette

Finding a vulnerability is only half the battle. Your report is what security teams, triagers, and engineering leads evaluate when determining severity and bounty rewards.

### 1. Report Anatomy
A high-quality bug bounty report contains:
1. **Title**: Clear, descriptive summary (e.g. `[BOLA] Unauthorized access to customer invoices via /api/v1/documents/{id}`).
2. **Severity & CVSS**: Justified CVSS v3.1 vector calculation.
3. **Summary**: High-level executive overview of what was discovered.
4. **Step-by-Step Reproduction (PoC)**: Clear, minimal steps that allow any engineer to reproduce the issue in under 5 minutes. Include exact curl commands and sample HTTP dumps.
5. **Business Impact**: Explain the real-world operational and financial risk to the company (data breach, compliance fine, account takeover).
6. **Remediation**: Actionable engineering recommendations to fix the flaw permanently.

### 2. Rules of Engagement & Safe Harbor
- **Scope Compliance**: Never test assets listed under "Out of Scope".
- **Data Privacy**: If you discover sensitive user data, stop immediately. Accessing more data than necessary to prove the flaw invalidates Safe Harbor!
- **Do No Harm**: Avoid denial of service, data destruction, or altering production user records.
                    """.trimIndent(),
                    keyTakeaways = listOf(
                        "Clear reproduction steps and realistic impact statements lead to faster triage and higher bounty payouts.",
                        "Adhere strictly to program scope and Safe Harbor rules of engagement.",
                        "Never leak vulnerability details publicly prior to mutual agreement and remediation."
                    ),
                    references = listOf(
                        LearningReference("HackerOne Vulnerability Disclosure Guidelines", "HackerOne", "https://www.hackerone.com/disclosure-guidelines", ReferenceType.HACKERONE),
                        LearningReference("FIRST CVSS v3.1 Specification", "FIRST", "https://www.first.org/cvss/v3.1/specification-document", ReferenceType.NIST)
                    ),
                    associatedQuizId = "quiz_ethics_reporting"
                )
            )
        )
    )

    fun getAllModules(): List<Module> = modulesList

    fun getModuleById(moduleId: String): Module? = modulesList.firstOrNull { it.id == moduleId }

    fun getLessonById(lessonId: String): Lesson? {
        for (module in modulesList) {
            val found = module.lessons.firstOrNull { it.id == lessonId }
            if (found != null) return found
        }
        return null
    }

    fun observeCompletedLessons(): Flow<List<LessonProgressEntity>> = userProgressDao.getAllLessonProgress()

    fun getCompletedLessonsCount(): Flow<Int> = userProgressDao.getCompletedLessonsCount()

    suspend fun markLessonCompleted(lessonId: String, moduleId: String) {
        val existing = userProgressDao.getLessonProgress(lessonId)
        if (existing?.isCompleted != true) {
            userProgressDao.insertOrUpdate(
                LessonProgressEntity(
                    lessonId = lessonId,
                    moduleId = moduleId,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
            )
            userPreferencesDataStore.addXp(40)
            userPreferencesDataStore.recordActivityToday()
        }
    }
}
