package com.bugbounty.academy.ui.screens.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugbounty.academy.ui.components.*
import com.bugbounty.academy.ui.theme.CyberCyan
import com.bugbounty.academy.ui.theme.CyberGreen
import com.bugbounty.academy.ui.theme.CyberRed

@Composable
fun LabDetailScreen(
    labId: String,
    viewModel: LabViewModel,
    onNavigateBack: () -> Unit
) {
    val lab = remember(labId) { viewModel.getLabById(labId) }
    val progressList by viewModel.allLabProgress.collectAsState()
    val labProgress = remember(progressList, labId) { progressList.firstOrNull { it.labId == labId } }
    val lastResult by viewModel.lastExecutionResult.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val flagMessage by viewModel.flagSubmissionMessage.collectAsState()

    var manualFlagInput by remember { mutableStateOf("") }
    var expandedHints by remember { mutableStateOf(false) }

    if (lab == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Lab not found", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Scaffold(
        topBar = {
            CyberTopAppBar(
                title = lab.title,
                canNavigateBack = true,
                onNavigateBack = {
                    viewModel.clearExecutionState()
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lab Header & Meta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DifficultyBadge(difficulty = lab.difficulty)
                        Text(
                            text = lab.cwe,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = lab.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = lab.scenarioContext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (labProgress?.isCompleted == true) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberGreen.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "LAB SOLVED • FLAG CAPTURED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen
                                    )
                                    Text(
                                        text = labProgress.flagCaptured ?: lab.flag,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Progressive Hints Accordion
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFD54F))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HINTS & GUIDANCE (${lab.hints.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(onClick = { expandedHints = !expandedHints }) {
                            Text(if (expandedHints) "Hide" else "Reveal", color = CyberCyan)
                        }
                    }

                    if (expandedHints) {
                        Spacer(modifier = Modifier.height(8.dp))
                        lab.hints.forEachIndexed { i, hint ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Text("${i + 1}. ", color = CyberCyan, fontWeight = FontWeight.Bold)
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Interactive HTTP Request & Response Inspector
            HttpRequestResponseInspector(
                initialMethod = lab.defaultMethod,
                initialEndpoint = lab.simulatedEndpoint,
                initialHeaders = lab.defaultHeaders,
                initialBody = lab.defaultBody,
                onExecute = { m, ep, h, b ->
                    viewModel.executeLab(
                        labId = lab.id,
                        method = m,
                        endpoint = ep,
                        headers = h,
                        body = b
                    )
                },
                lastResult = lastResult,
                isLoading = isExecuting
            )

            // Manual Flag Submission Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MANUAL FLAG SUBMISSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualFlagInput,
                            onValueChange = { manualFlagInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("FLAG{...}", fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )

                        Button(
                            onClick = { viewModel.submitFlag(lab.id, manualFlagInput) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black)
                        ) {
                            Text("Submit", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (flagMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val isSuccess = flagMessage!!.contains("Accepted")
                        Text(
                            text = flagMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSuccess) CyberGreen else CyberRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Remediation & Secure Code Fix
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURE REMEDIATION ARCHITECTURE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = lab.remediationExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CodeViewer(code = lab.remediationCodeSnippet, language = "SECURE KOTLIN/JAVA FIX")
                }
            }
        }
    }
}
