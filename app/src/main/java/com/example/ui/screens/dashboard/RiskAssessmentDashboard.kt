package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusPill
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
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
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusPurpleBg
import com.example.ui.viewmodel.ModalityCategory
import com.example.ui.viewmodel.MultimodalInput
import com.example.ui.viewmodel.RiskAssessmentViewModel
import com.example.ui.viewmodel.RiskLevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Composable dashboard UI displaying:
 * 1. Calculated Risk Percentage
 * 2. Severity Gauge (Low / Moderate / High) with visual multi-segment indicator
 * 3. Summary list of Multimodal Input Status (Normal / Abnormal) with quick interactive toggles
 */
@Composable
fun RiskAssessmentDashboard(
    viewModel: RiskAssessmentViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    val primaryRiskColor = when (state.riskLevel) {
        RiskLevel.LOW -> RiskGreen
        RiskLevel.MODERATE -> RiskOrange
        RiskLevel.HIGH -> RiskRed
    }

    val primaryRiskBg = when (state.riskLevel) {
        RiskLevel.LOW -> RiskGreenBg
        RiskLevel.MODERATE -> RiskOrangeBg
        RiskLevel.HIGH -> RiskRedBg
    }

    val primaryRiskBorder = when (state.riskLevel) {
        RiskLevel.LOW -> RiskGreenBorder
        RiskLevel.MODERATE -> RiskOrangeBorder
        RiskLevel.HIGH -> RiskRedBorder
    }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MedicalTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Column {
                        Text(
                            text = "Cardio-Cerebrovascular Risk",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        Text(
                            text = "Multimodal Clinical Stratification Engine",
                            fontSize = 12.sp,
                            color = MedicalTextSecondary
                        )
                    }
                }
                IconButton(onClick = { viewModel.setAllNormalPreset() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset All to Normal",
                        tint = MedicalTeal
                    )
                }
            }
        }

        // 1. Severity Gauge & Risk Percentage Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryRiskBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = primaryRiskColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Overall Risk Gauge",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MedicalTextSecondary
                            )
                        }

                        StatusPill(
                            text = state.riskLevel.label,
                            textColor = primaryRiskColor,
                            bgColor = primaryRiskBg,
                            borderColor = primaryRiskBorder,
                            modifier = Modifier.testTag("risk_severity_badge")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Arc Severity Gauge (Canvas)
                    SeverityGaugeArc(
                        riskPercentage = state.overallRiskPct.toFloat(),
                        riskLevel = state.riskLevel,
                        modifier = Modifier.size(width = 240.dp, height = 145.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Severity Description
                    Text(
                        text = state.riskLevel.description,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MedicalTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sub-Risk Breakdowns (Cardiovascular vs Cerebrovascular)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cardiovascular Box
                        RiskBreakdownBox(
                            title = "Cardiovascular Risk",
                            percentage = state.cardioRiskPct,
                            icon = Icons.Default.Favorite,
                            color = MedicalBlue,
                            modifier = Modifier.weight(1f)
                        )

                        // Cerebrovascular Stroke Box
                        RiskBreakdownBox(
                            title = "Cerebrovascular Risk",
                            percentage = state.cerebrovascularRiskPct,
                            icon = Icons.Default.Psychology,
                            color = StatusPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical Counting Rule Indicator Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MedicalTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rule: 0–1 Abnormal = Low (<30%) • 2 Abnormal = Moderate (30–64%) • >50% = High (65–100%)",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Scenario Presets
        item {
            Column {
                Text(
                    text = "Quick Clinical Scenarios",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        label = "All Normal (Low)",
                        isSelected = state.abnormalCount == 0,
                        onClick = { viewModel.setAllNormalPreset() }
                    )
                    PresetChip(
                        label = "2 Abnormal (Moderate)",
                        isSelected = state.abnormalCount == 2,
                        onClick = { viewModel.setModerateRiskPreset() }
                    )
                    PresetChip(
                        label = "High Risk (>50%)",
                        isSelected = state.abnormalCount >= 5,
                        onClick = { viewModel.setHighRiskPreset() }
                    )
                    PresetChip(
                        label = "Cardiovascular Focus",
                        isSelected = state.activePresetName == "Cardiovascular Dominant",
                        onClick = { viewModel.setCardiacDominantPreset() }
                    )
                    PresetChip(
                        label = "Cerebrovascular Focus",
                        isSelected = state.activePresetName == "Cerebrovascular Dominant",
                        onClick = { viewModel.setCerebrovascularDominantPreset() }
                    )
                }
            }
        }

        // 3. Summary List of Multimodal Input Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Multimodal Input Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "${state.abnormalCount} Abnormal • ${state.totalCount - state.abnormalCount} Normal (${state.totalCount} evaluated)",
                        fontSize = 12.sp,
                        color = MedicalTextSecondary
                    )
                }

                StatusPill(
                    text = "${state.abnormalCount}/${state.totalCount} Abnormal",
                    textColor = if (state.abnormalCount == 0) RiskGreen else if (state.abnormalCount <= 2) RiskOrange else RiskRed,
                    bgColor = if (state.abnormalCount == 0) RiskGreenBg else if (state.abnormalCount <= 2) RiskOrangeBg else RiskRedBg
                )
            }
        }

        // Items for each multimodal input
        items(state.inputs, key = { it.id }) { input ->
            MultimodalInputCard(
                input = input,
                onToggle = { viewModel.toggleInputStatus(input.id) }
            )
        }

        // 4. Heart-Brain Axis GNN Synthesis & Clinical Recommendation
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
                            tint = MedicalTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Heart-Brain Axis Multimodal Synthesis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = state.gnnAxisSynthesis,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MedicalTeal
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = state.clinicalSummary,
                        fontSize = 12.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Visual Arc Severity Gauge displaying the calculated percentage and 3 colored severity zones.
 */
@Composable
fun SeverityGaugeArc(
    riskPercentage: Float,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val animatedPct by animateFloatAsState(
        targetValue = riskPercentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GaugePercentage"
    )

    val currentRiskColor = when (riskLevel) {
        RiskLevel.LOW -> RiskGreen
        RiskLevel.MODERATE -> RiskOrange
        RiskLevel.HIGH -> RiskRed
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.width.coerceAtMost(size.height * 2f) - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, strokeWidth / 2f)

            val startAngle = 160f
            val totalSweep = 220f

            // Low Zone: 0% - 30% (sweep = totalSweep * 0.30)
            val lowSweep = totalSweep * 0.30f
            // Moderate Zone: 30% - 65% (sweep = totalSweep * 0.35)
            val modSweep = totalSweep * 0.35f
            // High Zone: 65% - 100% (sweep = totalSweep * 0.35)
            val highSweep = totalSweep * 0.35f

            // Background Track with 3 colored zones
            drawArc(
                color = Color(0xFFD1FAE5).copy(alpha = 0.6f),
                startAngle = startAngle,
                sweepAngle = lowSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                startAngle = startAngle + lowSweep,
                sweepAngle = modSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = Color(0xFFFEE2E2).copy(alpha = 0.6f),
                startAngle = startAngle + lowSweep + modSweep,
                sweepAngle = highSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Value Arc
            val activeSweep = (animatedPct / 100f) * totalSweep
            drawArc(
                color = currentRiskColor,
                startAngle = startAngle,
                sweepAngle = activeSweep.coerceAtLeast(1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Needle / Indicator Pin
            val needleAngle = (startAngle + activeSweep) * (PI / 180.0)
            val radius = diameter / 2f
            val centerX = size.width / 2f
            val centerY = (strokeWidth / 2f) + radius
            val pinX = (centerX + (radius - 2.dp.toPx()) * cos(needleAngle)).toFloat()
            val pinY = (centerY + (radius - 2.dp.toPx()) * sin(needleAngle)).toFloat()

            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(pinX, pinY)
            )
            drawCircle(
                color = currentRiskColor,
                radius = 5.dp.toPx(),
                center = Offset(pinX, pinY)
            )
        }

        // Center Percentage Value Readout
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${animatedPct.toInt()}%",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = currentRiskColor,
                modifier = Modifier.testTag("calculated_risk_percentage")
            )
            Text(
                text = "Cardio-Cerebrovascular Risk",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MedicalTextSecondary
            )
        }
    }
}

/**
 * Sub-risk card displaying cardiovascular or cerebrovascular percentage.
 */
@Composable
fun RiskBreakdownBox(
    title: String,
    percentage: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MedicalTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Filter chip for switching presets.
 */
@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MedicalTeal else MedicalTextSecondary
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) Color(0xFFE2F4F7) else MedicalSurface
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = if (isSelected) MedicalTeal else MedicalCardBorder
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * Card representing an individual multimodal input in the summary list.
 * Includes interactive toggle switch to quickly test normal vs abnormal states.
 */
@Composable
fun MultimodalInputCard(
    input: MultimodalInput,
    onToggle: () -> Unit
) {
    val statusColor = if (input.isAbnormal) RiskRed else RiskGreen
    val statusBg = if (input.isAbnormal) RiskRedBg else RiskGreenBg
    val statusBorder = if (input.isAbnormal) RiskRedBorder else RiskGreenBorder

    val icon = when (input.id) {
        "ecg" -> Icons.Default.Favorite
        "eeg" -> Icons.Default.Psychology
        "mri" -> Icons.Default.MedicalServices
        "bp" -> Icons.Default.Speed
        "glucose" -> Icons.Default.Bloodtype
        "cholesterol" -> Icons.Default.WaterDrop
        "smoking" -> Icons.Default.SmokingRooms
        "bmi" -> Icons.Default.FitnessCenter
        "family_hx" -> Icons.Default.Groups
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (input.isAbnormal) RiskRedBorder else MedicalCardBorder,
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Icon + Title + Category + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = input.name,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = input.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        Text(
                            text = "${input.category.displayName} • Weight ${input.weight}x",
                            fontSize = 11.sp,
                            color = MedicalTextSecondary
                        )
                    }
                }

                StatusPill(
                    text = input.statusLabel,
                    textColor = statusColor,
                    bgColor = statusBg,
                    borderColor = statusBorder,
                    modifier = Modifier.testTag("status_pill_${input.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Finding description box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (input.isAbnormal) Color(0xFFFFF1F2) else Color(0xFFF8FAFC))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (input.isAbnormal) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = input.currentFinding,
                        fontSize = 12.sp,
                        color = if (input.isAbnormal) Color(0xFF991B1B) else MedicalTextPrimary,
                        fontWeight = if (input.isAbnormal) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Switch / Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (input.isAbnormal) "Flagged as Abnormal" else "Within Normal Baseline",
                    fontSize = 11.sp,
                    color = MedicalTextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggle() }
                ) {
                    Text(
                        text = if (input.isAbnormal) "Set Normal" else "Set Abnormal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (input.isAbnormal) MedicalBlue else RiskRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = input.isAbnormal,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RiskRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = RiskGreen
                        ),
                        modifier = Modifier.testTag("toggle_${input.id}")
                    )
                }
            }
        }
    }
}
