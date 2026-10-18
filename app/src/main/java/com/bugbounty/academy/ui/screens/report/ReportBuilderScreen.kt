package com.bugbounty.academy.ui.screens.report

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
import com.bugbounty.academy.ui.components.CvssScorePicker
import com.bugbounty.academy.ui.components.CyberTopAppBar
import com.bugbounty.academy.ui.components.MarkdownPreviewCard
import com.bugbounty.academy.ui.theme.CyberCyan
import com.bugbounty.academy.ui.theme.CyberGreen

@Composable
fun ReportBuilderScreen(
    viewModel: ReportViewModel,
    onNavigateToSavedReports: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val saveMessage by viewModel.saveStatusMessage.collectAsState()
    val templates = remember { viewModel.getTemplates() }

    val currentReport = remember(formState) { formState.toDomainModel() }
    val generatedMarkdown = remember(currentReport) { currentReport.generateMarkdown() }

    Scaffold(
        topBar = {
            CyberTopAppBar(
                title = "Vulnerability Report Builder",
                actions = {
                    IconButton(onClick = onNavigateToSavedReports) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Saved Reports",
                            tint = CyberCyan
                        )
                    }
                    IconButton(onClick = { viewModel.resetForm() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Form",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            // Pre-built Templates Quick Loader
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "QUICK REPORT TEMPLATES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        templates.forEach { template ->
                            OutlinedButton(
                                onClick = { viewModel.loadTemplate(template) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = template.vulnerabilityType.split(" ").firstOrNull() ?: "Template",
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Core Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. TARGET & VULNERABILITY CLASSIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    OutlinedTextField(
                        value = formState.title,
                        onValueChange = { viewModel.updateForm { s -> s.copy(title = it) } },
                        label = { Text("Report Title") },
                        placeholder = { Text("[BOLA] Unauthorized profile access via /api/v1/users/{id}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = formState.targetAsset,
                        onValueChange = { viewModel.updateForm { s -> s.copy(targetAsset = it) } },
                        label = { Text("Target Asset / Domain / Host") },
                        placeholder = { Text("api.example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formState.vulnerabilityType,
                            onValueChange = { viewModel.updateForm { s -> s.copy(vulnerabilityType = it) } },
                            label = { Text("Vulnerability Type") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formState.cweId,
                            onValueChange = { viewModel.updateForm { s -> s.copy(cweId = it) } },
                            label = { Text("CWE ID") },
                            placeholder = { Text("CWE-639") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Interactive CVSS v3.1 Calculator
            CvssScorePicker(
                cvssVector = formState.cvssVector,
                onVectorChange = { newVector ->
                    viewModel.updateForm { it.copy(cvssVector = newVector) }
                }
            )

            // Technical Findings & Reproduction
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. TECHNICAL DESCRIPTION & REPRODUCTION (POC)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    OutlinedTextField(
                        value = formState.summary,
                        onValueChange = { viewModel.updateForm { s -> s.copy(summary = it) } },
                        label = { Text("Executive Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = formState.technicalDescription,
                        onValueChange = { viewModel.updateForm { s -> s.copy(technicalDescription = it) } },
                        label = { Text("Technical Deep Dive") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = formState.stepsToReproduce,
                        onValueChange = { viewModel.updateForm { s -> s.copy(stepsToReproduce = it) } },
                        label = { Text("Step-by-Step Proof of Concept (PoC)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    OutlinedTextField(
                        value = formState.httpProofOfConcept,
                        onValueChange = { viewModel.updateForm { s -> s.copy(httpProofOfConcept = it) } },
                        label = { Text("Raw HTTP Request & Response PoC") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }

            // Impact & Remediation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. BUSINESS IMPACT & REMEDIATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )

                    OutlinedTextField(
                        value = formState.businessImpact,
                        onValueChange = { viewModel.updateForm { s -> s.copy(businessImpact = it) } },
                        label = { Text("Real-World Business Impact") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = formState.remediation,
                        onValueChange = { viewModel.updateForm { s -> s.copy(remediation = it) } },
                        label = { Text("Remediation & Fix Recommendation") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            // Live Markdown Report Preview
            MarkdownPreviewCard(markdown = generatedMarkdown)

            // Save Action
            Button(
                onClick = { viewModel.saveReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (formState.id != 0L) "Update Saved Report" else "Save Report to Local Database",
                    fontWeight = FontWeight.Bold
                )
            }

            if (saveMessage != null) {
                Text(
                    text = saveMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyberGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
