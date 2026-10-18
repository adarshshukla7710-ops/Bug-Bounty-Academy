package com.bugbounty.academy.data.repository

import com.bugbounty.academy.domain.model.*

class ToolsRepository {
    private val toolsList: List<SecurityTool> = listOf(
        SecurityTool(
            id = "tool_burp_suite",
            name = "Burp Suite",
            category = ToolCategory.PROXY_AND_INTERCEPTION,
            summary = "The industry-standard web vulnerability scanner, intercepting HTTP proxy, repeater, and intruder.",
            description = "Burp Suite acts as a Man-in-the-Middle (MitM) proxy between your browser and the target server. It lets you inspect, tamper with, and replay HTTP/HTTPS requests in real-time.",
            documentationUrl = "https://portswigger.net/burp/documentation",
            keyFlags = listOf(
                ToolFlag("Proxy Listener", "Configures local port forwarding (default 127.0.0.1:8080) for capturing browser traffic"),
                ToolFlag("Repeater", "Allows modifying and resending individual HTTP requests to analyze responses manually"),
                ToolFlag("Intruder", "Automated custom payload injection and parameter fuzzing engine"),
                ToolFlag("Match and Replace", "Rule-based engine for dynamically altering headers or parameters on the fly")
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Manual Testing Proxy Setup",
                    description = "Configure Firefox / Chrome to route traffic through local Burp proxy",
                    template = "Host: 127.0.0.1, Port: 8080 (Install Burp CA Certificate at http://burp/cert)",
                    parameters = listOf("127.0.0.1", "8080")
                )
            ),
            ethicalConsiderations = listOf(
                "Never run Burp Intruder or active scans against un-scoped assets.",
                "Throttle Intruder threads to avoid causing unintended Denial of Service (DoS) on production backends."
            ),
            commonMistakes = listOf(
                "Forgetting to import the Burp CA certificate into Android/iOS trust stores when intercepting mobile app traffic.",
                "Failing to exclude third-party analytics domains (Google Analytics, Sentry) from your target scope."
            )
        ),
        SecurityTool(
            id = "tool_nmap",
            name = "Nmap (Network Mapper)",
            category = ToolCategory.NETWORK_SCANNING,
            summary = "The preeminent open-source utility for network exploration, port discovery, and service fingerprinting.",
            description = "Nmap sends raw IP packets in novel ways to discover live hosts, open ports, running services, operating systems, and firewall configurations.",
            documentationUrl = "https://nmap.org/book/man.html",
            keyFlags = listOf(
                ToolFlag("-sS", "TCP SYN Stealth Scan (half-open scan, fast and stealthy)", null),
                ToolFlag("-sV", "Service version detection (probes open ports to determine service names and versions)", null),
                ToolFlag("-sC", "Runs default Nmap Scripting Engine (NSE) vulnerability detection scripts", null),
                ToolFlag("-p-", "Scans all 65,535 TCP ports rather than only the top 1,000", null),
                ToolFlag("-T4", "Sets timing template to Aggressive (speeds up scans on reliable networks)", null)
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Standard Vulnerability & Service Audit",
                    description = "Comprehensive scan with service fingerprinting and default scripts",
                    template = "nmap -sV -sC -T4 -p 1-10000 {target}",
                    parameters = listOf("target")
                ),
                ToolCommandTemplate(
                    title = "All-Port Quick Scan",
                    description = "Identify non-standard open ports across full TCP range",
                    template = "nmap -p- -T4 --min-rate 1000 {target}",
                    parameters = listOf("target")
                )
            ),
            ethicalConsiderations = listOf(
                "Scanning port ranges on government or enterprise IPs without explicit bug bounty scope authorization is illegal in many jurisdictions.",
                "NSE scripts marked 'intrusive' or 'dos' can crash fragile production servers."
            ),
            commonMistakes = listOf(
                "Scanning only default top 1,000 ports and missing internal administration panels on ports 8080, 8443, 8888, or 9000.",
                "Setting scan speeds too high (--min-rate 10000), resulting in dropped packets and false negatives."
            )
        ),
        SecurityTool(
            id = "tool_ffuf",
            name = "ffuf (Fast Web Fuzzer)",
            category = ToolCategory.WEB_FUZZING,
            summary = "High-speed web fuzzer written in Go for discovering hidden directories, files, parameters, and virtual hosts.",
            description = "ffuf sends thousands of requests per second against web applications using custom wordlists, filtering results by HTTP status code, response size, or word count.",
            documentationUrl = "https://github.com/ffuf/ffuf",
            keyFlags = listOf(
                ToolFlag("-u", "Target URL with keyword 'FUZZ' indicating payload injection point", "https://target.com/FUZZ"),
                ToolFlag("-w", "Path to wordlist file", "/usr/share/seclists/Discovery/Web-Content/raft-medium-directories.txt"),
                ToolFlag("-mc", "Match HTTP status codes", "200,301,302,403"),
                ToolFlag("-fs", "Filter response size to suppress false positive default error pages", "420"),
                ToolFlag("-H", "Custom header (useful for authenticated fuzzing or Host header injection)", "\"Authorization: Bearer token\"")
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Directory & File Discovery",
                    description = "Fuzz common hidden paths filtering out 404s",
                    template = "ffuf -u {target}/FUZZ -w {wordlist} -mc 200,301,302,403 -t 40",
                    parameters = listOf("target", "wordlist")
                ),
                ToolCommandTemplate(
                    title = "Virtual Host (VHost) Enumeration",
                    description = "Discover unlinked internal subdomains hosted on same IP",
                    template = "ffuf -u http://{target_ip} -H \"Host: FUZZ.{domain}\" -w {subdomains_wordlist} -fs {default_size}",
                    parameters = listOf("target_ip", "domain", "subdomains_wordlist", "default_size")
                )
            ),
            ethicalConsiderations = listOf(
                "Aggressive fuzzing without rate limiting (-t 200) can saturate bandwidth or trigger DDoS alerts.",
                "Always abide by target rate limit and robot exclusions stated in the bug bounty brief."
            ),
            commonMistakes = listOf(
                "Not filtering out soft 404 pages using `-fs` or `-fw`, which floods your terminal with useless results.",
                "Forgetting to fuzz file extensions like `-e .php,.json,.bak,.old,.txt`."
            )
        ),
        SecurityTool(
            id = "tool_sqlmap",
            name = "SQLMap",
            category = ToolCategory.EXPLOITATION_AND_POC,
            summary = "Automated SQL injection detection and database takeover tool.",
            description = "SQLMap automates the process of detecting and exploiting SQL injection flaws and taking over database servers with support for all major SQL engines.",
            documentationUrl = "https://sqlmap.org/",
            keyFlags = listOf(
                ToolFlag("-u", "Target URL with injection parameter", "http://target.com/item.php?id=1"),
                ToolFlag("-r", "Load full HTTP request from Burp Suite text file", "request.txt"),
                ToolFlag("--dbs", "Enumerate database names", null),
                ToolFlag("--current-user", "Retrieve current database user identity", null),
                ToolFlag("--batch", "Non-interactive mode (automatically accepts default choices)", null),
                ToolFlag("--level", "Depth of tests (1-5; level 5 tests HTTP headers like Cookie and User-Agent)", "3")
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Test Parameter from Saved Burp Request",
                    description = "Safe proof-of-concept verification from intercepted request file",
                    template = "sqlmap -r {request_file} -p {parameter} --current-user --batch",
                    parameters = listOf("request_file", "parameter")
                )
            ),
            ethicalConsiderations = listOf(
                "Never dump entire production customer databases (`--dump-all`); stop once database user/version is confirmed.",
                "Be cautious with `--os-shell` or `--file-write` on live production infrastructure."
            ),
            commonMistakes = listOf(
                "Running automated SQLMap without configuring session cookies, resulting in testing login redirection pages rather than target API logic."
            )
        ),
        SecurityTool(
            id = "tool_nuclei",
            name = "Nuclei",
            category = ToolCategory.EXPLOITATION_AND_POC,
            summary = "Fast and customizable vulnerability scanner based on community-curated YAML templates.",
            description = "Nuclei sends targeted protocol-level requests across multiple protocols (HTTP, DNS, TCP, SSL) using declarative YAML templates to scan for known CVEs, misconfigurations, and exposures.",
            documentationUrl = "https://docs.projectdiscovery.io/tools/nuclei",
            keyFlags = listOf(
                ToolFlag("-u", "Target URL or host to scan", "https://target.com"),
                ToolFlag("-t", "Path to templates directory or specific template", "cves/"),
                ToolFlag("-severity", "Filter templates by severity (info, low, medium, high, critical)", "medium,high,critical"),
                ToolFlag("-rl", "Maximum requests per second to prevent rate limiting", "50")
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Scan for High and Critical Known Exposures",
                    description = "Run verified templates against target with safe rate limits",
                    template = "nuclei -u {target} -severity high,critical -rl 30",
                    parameters = listOf("target")
                )
            ),
            ethicalConsiderations = listOf(
                "Do not run intrusive templates that execute destructive state changes or exploit chained RCEs."
            ),
            commonMistakes = listOf(
                "Running outdated templates without updating (`nuclei -ut`)."
            )
        ),
        SecurityTool(
            id = "tool_curl",
            name = "cURL & HTTPie",
            category = ToolCategory.PROXY_AND_INTERCEPTION,
            summary = "Command-line data transfer tools and HTTP clients for precision vulnerability crafting.",
            description = "cURL is the Swiss Army knife of web testing, enabling exact byte-level control over HTTP methods, headers, user agents, cookies, and proxy routing.",
            documentationUrl = "https://curl.se/docs/manpage.html",
            keyFlags = listOf(
                ToolFlag("-i", "Include HTTP response headers in output", null),
                ToolFlag("-v", "Verbose mode (displays full TLS handshake and outgoing HTTP headers)", null),
                ToolFlag("-X", "Specify custom HTTP request method", "POST"),
                ToolFlag("-H", "Add custom request header", "\"Authorization: Bearer xyz\""),
                ToolFlag("-d", "HTTP POST request data body", "'{\"test\": 123}'"),
                ToolFlag("-x", "Route request through proxy (e.g. Burp Suite)", "http://127.0.0.1:8080")
            ),
            commandTemplates = listOf(
                ToolCommandTemplate(
                    title = "Test API Endpoint via Burp Proxy",
                    description = "Issue POST request with custom header routed through Burp Suite",
                    template = "curl -i -x http://127.0.0.1:8080 -X POST {url} -H \"Content-Type: application/json\" -d '{payload}'",
                    parameters = listOf("url", "payload")
                )
            ),
            ethicalConsiderations = listOf(
                "Ensure user agent identifying headers reflect your bug bounty platform profile when required by program policies."
            ),
            commonMistakes = listOf(
                "Failing to URL-encode special characters in GET parameter query strings."
            )
        )
    )

    fun getAllTools(): List<SecurityTool> = toolsList

    fun getToolById(toolId: String): SecurityTool? = toolsList.firstOrNull { it.id == toolId }

    fun getToolsByCategory(category: ToolCategory): List<SecurityTool> = toolsList.filter { it.category == category }
}
