package com.bugbounty.academy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugbounty.academy.domain.model.LabExecutionResult
import com.bugbounty.academy.ui.theme.CyberCyan
import com.bugbounty.academy.ui.theme.CyberGreen
import com.bugbounty.academy.ui.theme.CyberRed

@Composable
fun HttpRequestResponseInspector(
    initialMethod: String,
    initialEndpoint: String,
    initialHeaders: Map<String, String>,
    initialBody: String,
    onExecute: (method: String, endpoint: String, headers: Map<String, String>, body: String) -> Unit,
    lastResult: LabExecutionResult?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var method by remember(initialMethod) { mutableStateOf(initialMethod) }
    var endpoint by remember(initialEndpoint) { mutableStateOf(initialEndpoint) }
    var body by remember(initialBody) { mutableStateOf(initialBody) }
    var headersText by remember(initialHeaders) {
        mutableStateOf(initialHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" })
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Request, 1: Response, 2: Logs

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Method & Endpoint Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Method Selector
                var expandedMethodMenu by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { expandedMethodMenu = true }
                    ) {
                        Text(
                            text = method,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    DropdownMenu(
                        expanded = expandedMethodMenu,
                        onDismissRequest = { expandedMethodMenu = false }
                    ) {
                        listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH").forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    method = m
                                    expandedMethodMenu = false
                                }
                            )
                        }
                    }
                }

                // Endpoint TextField
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    placeholder = { Text("/api/v1/endpoint", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs for Request / Response / Logs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyberCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Request Payload", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val statusBadge = lastResult?.let { " (${it.statusCode})" } ?: ""
                        Text("Response$statusBadge", fontSize = 12.sp)
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Sandbox Logs", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // Headers Section
                    Text(
                        text = "Headers (Key: Value):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = headersText,
                        onValueChange = { headersText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Body Section
                    Text(
                        text = "Body / Payload:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
                1 -> {
                    if (lastResult == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Execute request to observe server response",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val is2xx = lastResult.statusCode in 200..299
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (is2xx) CyberGreen.copy(alpha = 0.2f) else CyberRed.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${lastResult.statusCode} ${lastResult.statusText}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (is2xx) CyberGreen else CyberRed
                                    )
                                }

                                if (lastResult.flagAwarded != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CyberGreen
                                    ) {
                                        Text(
                                            text = "FLAG CAPTURED!",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            CodeViewer(
                                code = buildString {
                                    lastResult.responseHeaders.forEach { (k, v) ->
                                        appendLine("$k: $v")
                                    }
                                    appendLine()
                                    append(lastResult.responseBody)
                                },
                                language = "HTTP RESPONSE"
                            )
                        }
                    }
                }
                2 -> {
                    TerminalOutput(logs = lastResult?.executionLog ?: emptyList())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = {
                    val parsedHeaders = headersText.lines()
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                        }.toMap()

                    onExecute(method, endpoint, parsedHeaders, body)
                    selectedTab = 1 // Switch to response tab
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "Executing in Sandbox..." else "Send Request",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
