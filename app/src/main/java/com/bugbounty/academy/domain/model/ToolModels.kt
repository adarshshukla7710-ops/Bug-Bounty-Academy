package com.bugbounty.academy.domain.model

enum class ToolCategory(val title: String) {
    PROXY_AND_INTERCEPTION("Interception & Proxy"),
    NETWORK_SCANNING("Network & Port Scanning"),
    WEB_FUZZING("Content Discovery & Fuzzing"),
    OSINT_AND_RECON("OSINT & Asset Discovery"),
    EXPLOITATION_AND_POC("Testing & Exploitation"),
    PACKET_ANALYSIS("Traffic & Packet Analysis")
}

data class ToolFlag(
    val flag: String,
    val description: String,
    val exampleValue: String? = null
)

data class ToolCommandTemplate(
    val title: String,
    val description: String,
    val template: String, // e.g. "nmap -sV -sC -p- -T4 {target}"
    val parameters: List<String>
)

data class SecurityTool(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val summary: String,
    val description: String,
    val documentationUrl: String,
    val keyFlags: List<ToolFlag>,
    val commandTemplates: List<ToolCommandTemplate>,
    val ethicalConsiderations: List<String>,
    val commonMistakes: List<String>
)
