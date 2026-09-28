package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assessment
import com.example.ui.components.HeroGradientCard
import com.example.ui.components.MetricStatBox
import com.example.ui.components.StatusPill
import com.example.ui.theme.MedicalBadgeBg
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
import com.example.ui.theme.MedicalSubtleBg
import com.example.ui.theme.MedicalSurface
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTextMuted
import com.example.ui.theme.MedicalTextPrimary
import com.example.ui.theme.MedicalTextSecondary
import com.example.ui.theme.RiskGreen
import com.example.ui.theme.RiskGreenBg
import com.example.ui.theme.RiskGreenBorder
import com.example.ui.theme.RiskOrange
import com.example.ui.theme.RiskOrangeBg
import com.example.ui.theme.RiskOrangeBorder
import com.example.ui.theme.RiskRed
import com.example.ui.theme.RiskRedBg
import com.example.ui.theme.RiskRedBorder
import com.example.ui.theme.StatusPink
import com.example.ui.theme.StatusPinkBg
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.RiskAssessmentViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    riskViewModel: RiskAssessmentViewModel? = null,
    onNavigateToPatients: () -> Unit,
    onNavigateToAssessment: (Long) -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val patientCount by viewModel.patientCount.collectAsState()
    val totalAssessments by viewModel.totalAssessments.collectAsState()
    val highRisk by viewModel.highRiskCount.collectAsState()
    val moderateRisk by viewModel.moderateRiskCount.collectAsState()
    val lowRisk by viewModel.lowRiskCount.collectAsState()
    val recentReports by viewModel.recentAssessments.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CardioNeuro Sentinel",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "Multimodal Clinical AI Decision Support",
                        fontSize = 12.sp,
                        color = MedicalTextSecondary
                    )
                }
                Row {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Status",
                            tint = MedicalTeal
                        )
                    }
                    IconButton(onClick = onNavigateToReports) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Analytics",
                            tint = MedicalTeal
                        )
                    }
                }
            }
        }

        // Hero Gradient Card
        item {
            HeroGradientCard(
                title = "Independent Clinical AI Assessment",
                subtitle = "XGBoost • Random Forest • 2D CNN • 1D-CNN • GNN • Federated DP",
                trailingIcon = Icons.Default.Psychology,
                actionButton = {
                    val isDark = isAppInDarkTheme()
                    Button(
                        onClick = onNavigateToPatients,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("patient_directory_button")
                    ) {
                        Text(
                            text = "Patient Directory ($patientCount)",
                            color = if (isDark) MaterialTheme.colorScheme.primary else MedicalTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        }

        // 4 Metric Grid Cards (2 rows of 2): High, Moderate, Low, Federated Node
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatBox(
                        title = "Critical / High Risk",
                        value = "$highRisk Cases",
                        indicatorColor = RiskRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        title = "Moderate Risk",
                        value = "$moderateRisk Cases",
                        indicatorColor = RiskOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatBox(
                        title = "Low Risk",
                        value = "$lowRisk Cases",
                        indicatorColor = RiskGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        title = "Federated Node",
                        value = "${viewModel.federatedEpsilon}\n(${viewModel.federatedStatus})",
                        indicatorColor = RiskGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Model Architecture Inventory
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MedicalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Model Architecture Inventory",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                            Text(
                                text = "Verified ONNX inference pipeline",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    ArchitectureRow("Tabular Vitals & Biomarkers", "XGBoost + Random Forest", MedicalBlue, MedicalBadgeBg)
                    ArchitectureRow("CT/MRI Stroke Classification", "2D Convolutional Neural Net", RiskGreen, RiskGreenBg)
                    ArchitectureRow("ECG & EEG Telemetry", "1D-CNN Spectral Classifier", StatusPink, StatusPinkBg)
                    ArchitectureRow("Heart-Brain Axis Crosstalk", "Graph Neural Network (GNN)", MedicalTeal, MedicalBadgeBg)
                    ArchitectureRow("Privacy & Weight Sync", "Federated DP (FedAvg)", MedicalTeal, MedicalBadgeBg)
                }
            }
        }

        // Recent AI Diagnostic Reports Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Recent AI Diagnostic Reports",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "$totalAssessments total assessments",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                }
                TextButton(onClick = onNavigateToReports) {
                    Text(
                        text = "View Analytics",
                        color = MedicalBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Recent Reports List or Empty State
        if (recentReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No assessments yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No risk data available. Run an on-device multimodal assessment for a patient to populate clinical predictions.",
                            fontSize = 12.sp,
                            color = MedicalTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(recentReports) { assessment ->
                RecentReportItem(
                    assessment = assessment,
                    onClick = { onNavigateToAssessment(assessment.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ArchitectureRow(
    label: String,
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MedicalTextSecondary,
            modifier = Modifier.weight(1f)
        )
        StatusPill(
            text = badgeText,
            textColor = badgeColor,
            bgColor = badgeBg
        )
    }
}

@Composable
fun RecentReportItem(
    assessment: Assessment,
    onClick: () -> Unit
) {
    val isHighRisk = assessment.clinicalRiskCategory.equals("HIGH", ignoreCase = true) || 
                     assessment.clinicalRiskCategory.contains("HIGH", ignoreCase = true)
    val isModerateRisk = assessment.clinicalRiskCategory.equals("MODERATE", ignoreCase = true) || 
                         assessment.clinicalRiskCategory.contains("MODERATE", ignoreCase = true) ||
                         assessment.clinicalRiskCategory.contains("MEDIUM", ignoreCase = true)

    val riskTextColor = when {
        isHighRisk -> RiskRed
        isModerateRisk -> RiskOrange
        else -> RiskGreen
    }
    val riskBgColor = when {
        isHighRisk -> RiskRedBg
        isModerateRisk -> RiskOrangeBg
        else -> RiskGreenBg
    }
    val riskBorderColor = when {
        isHighRisk -> RiskRedBorder
        isModerateRisk -> RiskOrangeBorder
        else -> RiskGreenBorder
    }
    val riskText = when {
        isHighRisk -> "High Risk"
        isModerateRisk -> "Moderate Risk"
        else -> "Low Risk"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("report_item_${assessment.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedicalBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assessment.patientName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = if (assessment.ecgValidationStatus.name == "UNSUPPORTED")
                            "Quantitative inference requires raw digital signals. Image inputs are unsupported."
                        else "ECG Telemetry: ${assessment.ecgValidationStatus.name}",
                        fontSize = 10.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 13.sp
                    )
                }
                StatusPill(
                    text = riskText,
                    textColor = riskTextColor,
                    bgColor = riskBgColor,
                    borderColor = riskBorderColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Column Chips/Blocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Combined Risk
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalSubtleBg)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "Combined Risk", fontSize = 10.sp, color = MedicalTextSecondary)
                        val combinedScore = assessment.combinedRiskScorePct
                        val pctText = if (combinedScore != null) "${(combinedScore * 100).toInt()}%" else "N/A"
                        val pctColor = when {
                            combinedScore == null -> MedicalTextPrimary
                            combinedScore >= 0.65 -> RiskRed
                            combinedScore >= 0.30 -> RiskOrange
                            else -> RiskGreen
                        }
                        Text(
                            text = pctText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = pctColor
                        )
                    }
                }

                // TOAST Subtype
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalSubtleBg)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "TOAST Subtype\n${assessment.toastSubtype}",
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            color = MedicalTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // GNN Crosstalk
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalSubtleBg)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "GNN Crosstalk", fontSize = 10.sp, color = MedicalTextSecondary)
                        Text(
                            text = assessment.gnnCrosstalk ?: "Baseline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalBlue
                        )
                    }
                }
            }
        }
    }
}
