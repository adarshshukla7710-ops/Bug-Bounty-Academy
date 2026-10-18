# BugBounty Academy 🛡️🐞

**BugBounty Academy** is a production-grade, native Android cybersecurity learning application built using **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM architecture**, **Room offline storage**, and **DataStore preferences**.

Designed by senior application security specialists and mobile architects, BugBounty Academy delivers a structured beginner-to-advanced curriculum, **32 deterministic, safe local practice labs**, interactive quizzes with answer explanations, a security tools learning center, and a standardized **Vulnerability Report Builder** with a live CVSS v3.1 calculator.

---

## 🏛️ Architecture & Tech Stack

```
                   ┌──────────────────────────────────────────────┐
                   │               JETPACK COMPOSE                │
                   │    (Material 3, Dark Cyber Theme, Navigation)│
                   └──────────────────────┬───────────────────────┘
                                          │
                   ┌──────────────────────▼───────────────────────┐
                   │             MVVM VIEWMODELS                  │
                   │  (StateFlow, Coroutines, State Ingestion)    │
                   └──────┬────────────────┬───────────────┬──────┘
                          │                │               │
        ┌─────────────────▼─────┐ ┌────────▼────────┐ ┌────▼─────────────────┐
        │ SAFE DETERMINISTIC    │ │ DOMAIN MODELS   │ │ REPOSITORIES          │
        │ LAB ENGINE (Sandboxed)│ │ (Curriculum,    │ │ (Curriculum, Lab,     │
        │ - SQL parser          │ │  Labs, Quizzes, │ │  Quiz, Tools, Report, │
        │ - XSS & DOM detector  │ │  CVSS v3.1,     │ │  UserProgress)        │
        │ - JWT/OAuth validator │ │  SecurityTools) │ └──────┬────────┬───────┘
        │ - TOCTOU simulator    │ └─────────────────┘        │        │
        └───────────────────────┘                            │        │
                                                ┌────────────▼──┐ ┌───▼────────────┐
                                                │ ROOM DATABASE │ │ DATASTORE      │
                                                │ (Offline DB)  │ │ (Streak & XP)  │
                                                └───────────────┘ └────────────────┘
```

- **Language**: Kotlin 2.0.0 (Java 17 target)
- **UI Framework**: Jetpack Compose (BOM 2024.08.00) with Material 3 Design
- **Architecture**: Clean Architecture + MVVM (Model-View-ViewModel) with reactive `StateFlow`
- **Offline Storage**: Room Database 2.6.1 with TypeConverters and DAOs
- **Preferences**: Jetpack DataStore Preferences for streaks, XP, and settings
- **Navigation**: Jetpack Navigation Compose with type-safe route patterns
- **Deterministic Lab Engine**: Pure Kotlin sandboxed vulnerability simulator supporting 32 interactive labs

---

## 📚 Curriculum Syllabus

| Module | Title | Category | Key Topics & Standards |
|---|---|---|---|
| **1** | **HTTP Protocol & Web Foundations** | Foundations | RFC 9110, RFC 9112, Methods, Headers, Same-Origin Policy (SOP), CORS, Cookie Flags (`HttpOnly`, `SameSite`, `Secure`) |
| **2** | **Networking & Target Infrastructure** | Foundations | DNS, Certificate Transparency (`crt.sh`), TCP 3-Way Handshake, TLS 1.3 Handshake, WAFs, Reverse Proxies |
| **3** | **Linux for Hackers & CLI Masterclass** | Foundations | Filesystem hierarchy, Bash pipelines, regex, `grep`, `sed`, `awk`, `curl`, `jq`, automated one-liners |
| **4** | **Reconnaissance & OSINT** | Recon & OSINT | ASN mapping, passive DNS, subdomain brute-forcing, permutations, JavaScript endpoint scraping |
| **5** | **OWASP Top 10 Web Vulnerabilities** | OWASP Core | SQL Injection (In-band, UNION, Blind), XSS (Stored, Reflected, DOM), CSRF, SSRF, IDOR, XXE, Command Injection |
| **6** | **API Security & Microservices** | API & Auth | OWASP API Top 10, BOLA (API1), Mass Assignment (API3), BFLA (API5), rate limit bypasses, IP spoofing |
| **7** | **OAuth 2.0 & JWT Deep-Dive** | API & Auth | RFC 7519, JWT `none` algorithm exploit, weak HMAC cracking, algorithm confusion, OAuth state CSRF, open redirect |
| **8** | **Business Logic & Concurrency Flaws** | Advanced | Negative quantity/price tampering, multi-step state skipping, coupon replay, TOCTOU race conditions, double spending |
| **9** | **GraphQL Attacks & Defense** | Advanced | Introspection schema extraction, circular query depth DoS, batching attacks, field-level authorization directives |
| **10** | **Secure Code Review & Remediation** | Defense | Source-to-sink taint analysis, ReDoS, hardcoded secrets, parameterized queries, Android KeyStore |
| **11** | **Responsible Disclosure & Reporting** | Defense | Safe Harbor, Rules of Engagement, CVSS v3.1 vector calculation, HackerOne/Bugcrowd report formatting |

---

## 🧪 32 Deterministic Local Practice Labs

Every lab operates **locally and deterministically** within an isolated simulation engine—no external scanning or network connections are ever made.

### Category 1: Injection Attacks
1. `lab_sqli_auth_bypass`: **SQLi Authentication Bypass** (`' OR '1'='1`)
2. `lab_sqli_union`: **UNION-Based SQLi Data Extraction** (Extracting credentials from `users` table)
3. `lab_sqli_blind_boolean`: **Blind Boolean SQLi Inference** (Inferring tokens character-by-character)
4. `lab_command_injection_ping`: **OS Command Injection** (Chaining shell metacharacters `;`, `&&`, `|`)

### Category 2: XSS & Client-Side
5. `lab_stored_xss`: **Stored XSS in Comment System** (Persistent execution in victim context)
6. `lab_reflected_xss_filter_bypass`: **Reflected XSS with WAF Filter Evasion** (Case-insensitivity & `<img onerror>`)
7. `lab_dom_xss_hash`: **DOM-Based XSS** (Unsafe `innerHTML` assignment from `location.hash`)
8. `lab_csrf_account_takeover`: **CSRF State-Changing Email Update** (Omission of anti-CSRF token)

### Category 3: Access Control & IDOR
9. `lab_idor_horizontal`: **Horizontal IDOR User Profile Leak** (Cross-user numeric ID tampering)
10. `lab_idor_vertical`: **Vertical Privilege Escalation** (Tampering with `role: admin` in profile update)
11. `lab_bfla_admin_endpoint`: **Broken Function Level Authorization** (Accessing `/api/v1/admin/export-users`)
12. `lab_bola_document_vault`: **BOLA Document Exfiltration** (Cross-tenant contract access via sequential ID)

### Category 4: API Security
13. `lab_mass_assignment_registration`: **Mass Assignment on User Registration** (`is_super_admin: true`)
14. `lab_rate_limit_header_spoof`: **Rate Limit Bypass via IP Spoofing** (`X-Forwarded-For: 10.0.0.X`)
15. `lab_api_versioning_unprotected`: **Shadow API Deprecated Version Exposure** (`/api/v1-beta/export`)
16. `lab_cors_misconfiguration`: **Arbitrary Origin Reflection with Credentials** (`Access-Control-Allow-Credentials`)

### Category 5: Authentication, OAuth & JWT
17. `lab_jwt_none_algorithm`: **JWT None Algorithm Signature Bypass** (`{"alg": "none"}`)
18. `lab_jwt_weak_secret`: **JWT Weak HMAC Secret Forgery** (Cracking low-entropy HMAC secret `secret123`)
19. `lab_oauth_missing_state`: **OAuth 2.0 Missing State Parameter** (CSRF account linking attack)
20. `lab_oauth_open_redirect`: **OAuth 2.0 Weak redirect_uri Regex Bypass** (Code theft to rogue subdomain)

### Category 6: Server-Side Vulnerabilities
21. `lab_ssrf_cloud_metadata`: **SSRF Cloud Metadata Exfiltration** (`169.254.169.254` IAM credential theft)
22. `lab_ssrf_dns_rebinding_bypass`: **SSRF Whitelist & Filter Evasion** (`0x7f.0.0.1`, `[::1]`)
23. `lab_path_traversal_lfi`: **Directory Path Traversal** (`../../../../etc/passwd`)
24. `lab_xxe_external_entity`: **XML External Entity (XXE) Injection** (`<!ENTITY xxe SYSTEM "file:///etc/hostname">`)
25. `lab_file_upload_extension_bypass`: **Insecure File Upload Extension Bypass** (`.phtml` / `.php5` evasion)
26. `lab_info_disclosure_git_head`: **Information Disclosure via Exposed `.git/HEAD`**

### Category 7: Business Logic & Concurrency
27. `lab_logic_price_tampering`: **Negative Quantity & Price Tampering** (`quantity: -1` cash leak)
28. `lab_logic_coupon_stacking`: **Infinite Coupon Replay Stacking** (`WELCOME20` replay)
29. `lab_race_condition_double_spend`: **TOCTOU Limit-Overrun Race Condition** (Multi-thread concurrent burst)

### Category 8: Secure Code Review & Recon
30. `lab_code_review_sqli_sink`: **Code Review: Unescaped SQL Query Concatenation** (Line audit & PreparedStatement)
31. `lab_code_review_crypto_salt`: **Code Review: Hardcoded AES Secret & Static IV** (Android KeyStore migration)
32. `lab_graphql_introspection_abuse`: **GraphQL Introspection & Hidden Field Discovery** (`__schema` inspection)

---

## 🧰 Tools Learning Center

Interactive reference cards with CLI syntax generators, key flags, ethical guidelines, and common pitfalls for:
- **Burp Suite**: Proxy, Repeater, Intruder, Match & Replace, CA certificate installation.
- **Nmap**: TCP SYN stealth (`-sS`), version detection (`-sV`), NSE scripts (`-sC`), timing (`-T4`).
- **ffuf**: High-speed fuzzing, directory discovery, virtual host matching, size filtering (`-fs`).
- **SQLMap**: Safe parameter auditing from Burp request files (`-r`), `--current-user`, `--batch`.
- **Nuclei**: Declarative YAML template scanning, severity filtering, rate limit protection.
- **cURL & HTTPie**: Precision request craft, custom headers, proxy routing (`-x 127.0.0.1:8080`).

---

## 📝 Vulnerability Report Builder

Follows HackerOne and Bugcrowd industry-standard formats:
- **Interactive CVSS v3.1 Calculator**: Real-time Base Score calculation across all metrics (AV, AC, PR, UI, S, C, I, A).
- **Structured Fields**: Title, Target Asset, CWE Classification, Executive Summary, Technical Deep Dive, Step-by-Step PoC, HTTP Dump, Business Impact, and Remediation.
- **Offline Persistence**: Save, edit, and manage draft reports in the Room Database.
- **Export & Markdown**: Live Markdown generation with one-tap clipboard copy.
- **Pre-built Templates**: One-click autofill for BOLA, Stored XSS, and SQLi reports.

---

## 🔒 Security & Safe Harbor Commitment

- **Zero Public Scanning**: The app never sends unauthorized probes or network packets to public hosts.
- **No Embedded Secrets**: No real API keys, tokens, or credentials exist in the source code or builds.
- **Synthetic Data**: All labs run locally against simulated backends using deterministic string and regex parsers.

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 34 (minSdk 26, targetSdk 34)

### Command-Line Build
```bash
# Clone or navigate to the workspace
cd "/Users/apple/Desktop/untitled folder"

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install to connected device or emulator
./gradlew installDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
