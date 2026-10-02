package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assessment
import com.example.domain.interpretation.BiomarkerAttribution
import com.example.domain.interpretation.ExplainableAiEngine
import com.example.domain.interpretation.ExplainableAiResult
import com.example.domain.interpretation.ImpactDirection
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

@Composable
fun ExplainableAiCard(
    assessment: Assessment,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    val xaiResult = remember(assessment) { ExplainableAiEngine.explain(assessment) }
    var showAllFactors by remember { mutableStateOf(initiallyExpanded) }

    val riskColor = when (xaiResult.riskCategory) {
        "HIGH" -> RiskRed
        "MODERATE" -> RiskOrange
        else -> RiskGreen
    }
    val riskBg = when (xaiResult.riskCategory) {
        "HIGH" -> RiskRedBg
        "MODERATE" -> RiskOrangeBg
        else -> RiskGreenBg
    }
    val riskBorder = when (xaiResult.riskCategory) {
        "HIGH" -> RiskRedBorder
        "MODERATE" -> RiskOrangeBorder
        else -> RiskGreenBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp))
            .testTag("explainable_ai_card")
            .testTag("explainable_ai_module"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MedicalBadgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = "Explainable AI Insights",
                            tint = MedicalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Explainable AI (XAI)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        Text(
                            text = "Biomarker Attribution & Interpretability",
                            fontSize = 11.sp,
                            color = MedicalTextSecondary
                        )
                    }
                }
                StatusPill(
                    text = "SHAP-Aligned",
                    textColor = MedicalTeal,
                    bgColor = MedicalBadgeBg
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calculated Risk Percentage alongside Human-Readable Summary Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(riskBg)
                    .border(1.dp, riskBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("explainable_ai_summary_box")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Calculated Percentage Metric
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MedicalSurface)
                            .border(1.dp, riskBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("explainable_ai_percentage_box"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${xaiResult.calculatedRiskPercentage}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor,
                            modifier = Modifier.testTag("explainable_ai_percentage_text")
                        )
                        Text(
                            text = "${xaiResult.riskCategory} RISK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Multimodal",
                            fontSize = 9.sp,
                            color = MedicalTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Human-Readable Summary
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (xaiResult.riskCategory == "HIGH") Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = riskColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Biomarker Influence Summary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = xaiResult.headlineSummary,
                            fontSize = 11.sp,
                            color = MedicalTextPrimary,
                            lineHeight = 15.sp,
                            modifier = Modifier.testTag("explainable_ai_summary_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subheader for Top Influencing Biomarkers
            Text(
                text = "Key Biomarkers Driving This Assessment:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MedicalTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // List of Top Influencing Biomarkers
            val displayedFactors = if (showAllFactors) xaiResult.allAttributions
                else (xaiResult.topRiskDrivers.ifEmpty { xaiResult.topProtectiveFactors })

            Column(
                modifier = Modifier.testTag("explainable_ai_biomarkers_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedFactors.forEach { factor ->
                    BiomarkerAttributionRow(attribution = factor)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expand / Collapse Full Breakdown Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MedicalSubtleBg)
                    .clickable { showAllFactors = !showAllFactors }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .testTag("explainable_ai_toggle_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showAllFactors) "Collapse Detailed Attribution"
                           else "View All Evaluated Factors (${xaiResult.allAttributions.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MedicalBlue
                )
                Icon(
                    imageVector = if (showAllFactors) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MedicalBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BiomarkerAttributionRow(
    attribution: BiomarkerAttribution,
    modifier: Modifier = Modifier
) {
    val isRiskIncreasing = attribution.impactDirection == ImpactDirection.RISK_INCREASING
    val isProtective = attribution.impactDirection == ImpactDirection.PROTECTIVE

    val badgeColor = when {
        isRiskIncreasing -> RiskRed
        isProtective -> RiskGreen
        else -> MedicalTextSecondary
    }
    val badgeBg = when {
        isRiskIncreasing -> RiskRedBg
        isProtective -> RiskGreenBg
        else -> MedicalBadgeBg
    }
    val badgeText = when {
        isRiskIncreasing -> "+${attribution.relativeContributionPct}% Influence"
        isProtective -> "Protective Anchor"
        else -> "Neutral"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MedicalSubtleBg)
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attribution.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "Recorded: ${attribution.measuredValue}",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                }
                StatusPill(
                    text = badgeText,
                    textColor = badgeColor,
                    bgColor = badgeBg
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Attribution Bar
            LinearProgressIndicator(
                progress = { attribution.impactWeight },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = badgeColor,
                trackColor = MedicalCardBorder
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = attribution.clinicalRationale,
                fontSize = 10.sp,
                color = MedicalTextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
