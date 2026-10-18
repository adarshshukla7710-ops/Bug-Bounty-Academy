package com.bugbounty.academy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugbounty.academy.domain.model.*
import com.bugbounty.academy.ui.theme.CyberCyan
import com.bugbounty.academy.ui.theme.parseHexColor

@Composable
fun CvssScorePicker(
    cvssVector: CvssVector,
    onVectorChange: (CvssVector) -> Unit,
    modifier: Modifier = Modifier
) {
    val score = cvssVector.calculateScore()
    val severity = SeverityLevel.fromScore(score)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Live Score & Severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CVSS v3.1 CALCULATOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cvssVector.toVectorString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = parseHexColor(severity.hexColor)
                ) {
                    Text(
                        text = "${severity.label.uppercase()} $score",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Attack Vector (AV)
            CvssMetricRow(
                label = "Attack Vector (AV)",
                options = AttackVector.entries.map { it.name to it },
                selected = cvssVector.attackVector,
                onSelect = { onVectorChange(cvssVector.copy(attackVector = it)) }
            )

            // Attack Complexity (AC)
            CvssMetricRow(
                label = "Attack Complexity (AC)",
                options = AttackComplexity.entries.map { it.name to it },
                selected = cvssVector.attackComplexity,
                onSelect = { onVectorChange(cvssVector.copy(attackComplexity = it)) }
            )

            // Privileges Required (PR)
            CvssMetricRow(
                label = "Privileges Required (PR)",
                options = PrivilegesRequired.entries.map { it.name to it },
                selected = cvssVector.privilegesRequired,
                onSelect = { onVectorChange(cvssVector.copy(privilegesRequired = it)) }
            )

            // User Interaction (UI)
            CvssMetricRow(
                label = "User Interaction (UI)",
                options = UserInteraction.entries.map { it.name to it },
                selected = cvssVector.userInteraction,
                onSelect = { onVectorChange(cvssVector.copy(userInteraction = it)) }
            )

            // Scope (S)
            CvssMetricRow(
                label = "Scope (S)",
                options = Scope.entries.map { it.name to it },
                selected = cvssVector.scope,
                onSelect = { onVectorChange(cvssVector.copy(scope = it)) }
            )

            // Confidentiality (C)
            CvssMetricRow(
                label = "Confidentiality Impact (C)",
                options = ImpactMetric.entries.map { it.name to it },
                selected = cvssVector.confidentiality,
                onSelect = { onVectorChange(cvssVector.copy(confidentiality = it)) }
            )

            // Integrity (I)
            CvssMetricRow(
                label = "Integrity Impact (I)",
                options = ImpactMetric.entries.map { it.name to it },
                selected = cvssVector.integrity,
                onSelect = { onVectorChange(cvssVector.copy(integrity = it)) }
            )

            // Availability (A)
            CvssMetricRow(
                label = "Availability Impact (A)",
                options = ImpactMetric.entries.map { it.name to it },
                selected = cvssVector.availability,
                onSelect = { onVectorChange(cvssVector.copy(availability = it)) }
            )
        }
    }
}

@Composable
private fun <T> CvssMetricRow(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (name, value) ->
                val isSelected = selected == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) CyberCyan.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelect(value) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
