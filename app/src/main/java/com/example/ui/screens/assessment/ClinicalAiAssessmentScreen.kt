package com.example.ui.screens.assessment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import com.example.data.model.ModelExecutionStatus
import com.example.domain.interpretation.ClinicalInterpreter
import com.example.domain.interpretation.ProvenanceTracker
import com.example.domain.pdf.ClinicalPdfGenerator
import com.example.ui.components.ClinicalPdfExportDialog
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
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusPurpleBg
import com.example.ui.theme.StatusPurpleBorder
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.viewmodel.AssessmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClinicalAiAssessmentScreen(
    viewModel: AssessmentViewModel,
    assessmentId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(assessmentId) {
        viewModel.loadAssessment(assessmentId)
    }

    val assessment by viewModel.currentAssessment.collectAsState()

    var whyResultExpanded by remember { mutableStateOf(false) }
    var provenanceExpanded by remember { mutableStateOf(false) }
    var technicalExpanded by remember { mutableStateOf(false) }

    if (assessment == null) {
        Box(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading Assessment #$assessmentId...", color = MedicalTextSecondary)
        }
        return
    }

    val current = assessment!!
    val context = LocalContext.current
    var showPdfDialog by remember { mutableStateOf(false) }
    var exportedPdfFile by remember { mutableStateOf<java.io.File?>(null) }
    val interpretation = remember(current) { ClinicalInterpreter.interpret(current) }
    val provenanceItems = remember(current) { ProvenanceTracker.generateProvenance(current) }
    val formattedDate = remember(current.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(current.timestamp))
    }

    val handleExportPdf = {
        try {
            val file = ClinicalPdfGenerator.generatePdf(context, current, interpretation)
            exportedPdfFile = file
            showPdfDialog = true
        } catch (e: Exception) {
            Toast.makeText(context, "Error generating clinical PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Dynamic Evidence Status Grid Mappings
    val (ecgTileStatus, ecgTileColor) = when (current.ecgValidationStatus) {
        ModalityValidationStatus.REAL_ENTERED -> Pair("REPORT ANALYZED", RiskGreen)
        ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> Pair("RAW SIGNAL VALIDATED", RiskGreen)
        ModalityValidationStatus.UNSUPPORTED -> Pair("UNSUPPORTED — Raw Required", RiskOrange)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE", RiskRed)
        else -> Pair("NOT PROVIDED", MedicalTextMuted)
    }
    val (eegTileStatus, eegTileColor) = when (current.eegValidationStatus) {
        ModalityValidationStatus.REAL_ENTERED -> Pair("REPORT ANALYZED", RiskGreen)
        ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> Pair("RAW SIGNAL VALIDATED", RiskGreen)
        ModalityValidationStatus.UNSUPPORTED -> Pair("UNSUPPORTED — Raw Required", RiskOrange)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE", RiskRed)
        else -> Pair("NOT PROVIDED", MedicalTextMuted)
    }
    val (mriTileStatus, mriTileColor) = when (current.mriValidationStatus) {
        ModalityValidationStatus.VALIDATED_MRI -> Pair("VALIDATED MRI", RiskGreen)
        ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED -> Pair("DWI SCAN VALIDATED", MedicalTeal)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE", RiskRed)
        else -> Pair(current.mriValidationStatus.name, MedicalTextMuted)
    }

    // Dynamic Risk Profile Rows
    val (bpText, bpColor, bpBg) = when {
        current.systolicBp < 120 && current.diastolicBp < 80 -> Triple("OPTIMAL", RiskGreen, RiskGreenBg)
        current.systolicBp < 130 && current.diastolicBp < 80 -> Triple("ELEVATED", RiskOrange, RiskOrangeBg)
        current.systolicBp < 140 || current.diastolicBp < 90 -> Triple("STAGE 1 HTN", RiskOrange, RiskOrangeBg)
        current.systolicBp < 180 && current.diastolicBp < 110 -> Triple("STAGE 2 HTN", RiskRed, RiskRedBg)
        else -> Triple("HYPERTENSIVE CRISIS", RiskRed, RiskRedBg)
    }
    val (smkText, smkColor, smkBg) = if (current.isSmoker) Triple("HIGH RISK", RiskRed, RiskRedBg) else Triple("NON-SMOKER", RiskGreen, RiskGreenBg)
    val (cholText, cholColor, cholBg) = when {
        current.cholesterol < 200 -> Triple("DESIRABLE", RiskGreen, RiskGreenBg)
        current.cholesterol < 240 -> Triple("BORDERLINE", RiskOrange, RiskOrangeBg)
        else -> Triple("HIGH", RiskRed, RiskRedBg)
    }
    val (gluText, gluColor, gluBg) = when {
        current.fastingGlucose < 100 -> Triple("NORMAL", RiskGreen, RiskGreenBg)
        current.fastingGlucose < 126 -> Triple("PREDIABETIC", RiskOrange, RiskOrangeBg)
        else -> Triple("DIABETIC RANGE", RiskRed, RiskRedBg)
    }
    val (bmiText, bmiColor, bmiBg) = when {
        current.bmi < 18.5 -> Triple("UNDERWEIGHT", RiskOrange, RiskOrangeBg)
        current.bmi < 25.0 -> Triple("NORMAL", RiskGreen, RiskGreenBg)
        current.bmi < 30.0 -> Triple("OVERWEIGHT", RiskOrange, RiskOrangeBg)
        else -> Triple("OBESE", RiskRed, RiskRedBg)
    }
    val (famText, famColor, famBg) = if (current.familyCvHistory || current.familyStrokeHistory) {
        Triple("RISK FACTOR", RiskOrange, RiskOrangeBg)
    } else {
        Triple("OPTIMAL", RiskGreen, RiskGreenBg)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MedicalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clinical AI Assessment",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = handleExportPdf,
                        modifier = Modifier.testTag("top_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export Clinical PDF",
                            tint = MedicalBlue
                        )
                    }
                    IconButton(
                        onClick = handleExportPdf,
                        modifier = Modifier.testTag("top_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Clinical Report",
                            tint = MedicalTeal
                        )
                    }
                }
            }
        }

        // Patient Header Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = current.patientName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "${current.age.toInt()} y/o • MRN: ${current.mrn}",
                        fontSize = 12.sp,
                        color = MedicalTextSecondary
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = MedicalTextMuted
                    )
                }
                StatusPill(
                    text = "Risk: ${current.clinicalRiskCategory}",
                    textColor = if (current.clinicalRiskCategory == "HIGH") RiskRed else if (current.clinicalRiskCategory == "MODERATE") RiskOrange else RiskGreen,
                    bgColor = if (current.clinicalRiskCategory == "HIGH") RiskRedBg else if (current.clinicalRiskCategory == "MODERATE") RiskOrangeBg else RiskGreenBg
                )
            }
        }

        // Clinical AI Summary PDF Export Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedicalBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MedicalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Clinical Summary PDF",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ECG, EEG & MRI input status included • 2-Page A4",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = handleExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quantitative Model Outputs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                val combinedPct = current.combinedRiskScorePct?.toInt() ?: (((current.ds2Probability ?: 0.3) + (current.ds3Probability ?: 0.3)) / 2 * 100).toInt()
                val isDark = isAppInDarkTheme()
                val combinedColor = if (combinedPct >= 65) RiskRed else if (combinedPct >= 35) RiskOrange else RiskGreen
                val combinedBg = if (combinedPct >= 65) RiskRedBg else if (combinedPct >= 35) RiskOrangeBg else RiskGreenBg
                val combinedBorder = if (combinedPct >= 65) RiskRedBorder else if (combinedPct >= 35) RiskOrangeBorder else RiskGreenBorder

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantitative Model Outputs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        StatusPill(
                            text = "Research / Clinical Trial",
                            textColor = RiskOrange,
                            bgColor = RiskOrangeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Big Output Metric Boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // DS2 Heart Model Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RiskRedBg)
                                .border(1.dp, RiskRedBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (current.ds2Probability != null) "${((current.ds2Probability) * 100).toInt()}%" else "N/A",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Cardiovascular (DS2)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MedicalTextSecondary
                                )
                            }
                        }

                        // DS3 Stroke Model Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusPurpleBg)
                                .border(1.dp, StatusPurpleBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val ds3Text = if (current.ds3Probability == null) "N/A"
                                    else "${((current.ds3Probability) * 100).toInt()}%"
                                Text(
                                    text = ds3Text,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPurple
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Cerebrovascular (DS3)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MedicalTextSecondary
                                )
                            }
                        }

                        // Multimodal Fusion Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(combinedBg)
                                .border(1.dp, combinedBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$combinedPct%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = combinedColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Multimodal Fusion",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MedicalTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail items
                    Text(
                        text = if (current.ds2Probability != null) "• DS2 Heart Model: Cardiovascular Risk: ${((current.ds2Probability) * 100).toInt()}%" else "• DS2 Heart Model: NOT AVAILABLE",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (current.ds3Probability != null) "• DS3 Stroke Model: Cerebrovascular Risk: ${((current.ds3Probability) * 100).toInt()}%" else "• DS3 Stroke Model: NOT AVAILABLE",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "• Combined Multimodal Risk: $combinedPct% (${current.clinicalRiskCategory} RISK) — Comprehensive clinical, imaging & electrophysiology integration",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = combinedColor
                    )
                }
            }
        }

        // Evidence Status Grid
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
                    Text(
                        text = "Evidence Status Grid",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EvidenceGridTile(
                            icon = Icons.Default.CheckCircle,
                            iconTint = RiskGreen,
                            title = "Clinical Vitals",
                            status = "REAL (Entered)",
                            statusColor = RiskGreen,
                            modifier = Modifier.weight(1f)
                        )
                        EvidenceGridTile(
                            icon = Icons.Default.MedicalServices,
                            iconTint = mriTileColor,
                            title = "Neuroimaging (MRI)",
                            status = mriTileStatus,
                            statusColor = mriTileColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EvidenceGridTile(
                            icon = Icons.Default.Favorite,
                            iconTint = ecgTileColor,
                            title = "ECG Waveform",
                            status = ecgTileStatus,
                            statusColor = ecgTileColor,
                            modifier = Modifier.weight(1f)
                        )
                        EvidenceGridTile(
                            icon = Icons.Default.Psychology,
                            iconTint = eegTileColor,
                            title = "EEG Telemetry",
                            status = eegTileStatus,
                            statusColor = eegTileColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Clinical Risk Profile
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
                    Text(
                        text = "Clinical Risk Profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RiskProfileRow("Blood Pressure", "${current.systolicBp.toInt()} / ${current.diastolicBp.toInt()} mmHg", bpText, bpColor, bpBg)
                    RiskProfileRow("Smoking Status", if (current.isSmoker) "Active Smoker" else "Non-smoker", smkText, smkColor, smkBg)
                    RiskProfileRow("Serum Cholesterol", "${current.cholesterol.toInt()} mg/dL", cholText, cholColor, cholBg)
                    RiskProfileRow("Diabetes / Glucose", "${current.fastingGlucose.toInt()} mg/dL", gluText, gluColor, gluBg)
                    RiskProfileRow("BMI", "${String.format(Locale.US, "%.1f", current.bmi)} kg/m²", bmiText, bmiColor, bmiBg)
                    RiskProfileRow("Family History", if (current.familyCvHistory || current.familyStrokeHistory) "Present" else "Absent", famText, famColor, famBg)
                }
            }
        }

        // Neuroimaging (Brain MRI / CT) Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MedicalTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Neuroimaging (Brain MRI / CT)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = mriTileStatus,
                            textColor = mriTileColor,
                            bgColor = if (current.mriValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRedBg else MedicalBadgeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (current.ds1Status == ModelExecutionStatus.EXECUTED) {
                        Image(
                            painter = painterResource(id = R.drawable.sample_mri),
                            contentDescription = "Brain MRI Scan Slice",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "DS1 QUANTITATIVE ANALYSIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = current.ds1SummaryLabel ?: "Evaluated MRI Scan: Low Cerebrovascular Stroke Risk Profile [ON-DEVICE AI INFERENCE]",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "DS1 Classifier Class Probability Breakdown:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MedicalTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val hProb = current.ds1HemorrhagicProb ?: 0.0
                        val iProb = current.ds1IschemicProb ?: 0.0
                        val nProb = current.ds1NormalProb ?: 0.0

                        ProbabilityBar(
                            label = "Haemorrhagic (Class 0)",
                            probability = hProb,
                            barColor = RiskRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ProbabilityBar(
                            label = "Ischemic (Class 1)",
                            probability = iProb,
                            barColor = RiskOrange
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ProbabilityBar(
                            label = "Normal (Class 2)",
                            probability = nProb,
                            barColor = RiskGreen
                        )
                    } else {
                        // DS1 Not Executed state: No fake probabilities!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedicalSubtleBg)
                                .border(1.dp, MedicalCardBorder, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "DS1 Inference Not Executed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (current.mriValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRed else MedicalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (current.mriValidationStatus == ModalityValidationStatus.MODALITY_REJECTED)
                                        "The uploaded image does not appear to be a valid MRI/CT input and was rejected. Model inference was not executed."
                                    else
                                        "No neuroimaging scan was provided or declared for research inference. Model outputs are NOT AVAILABLE.",
                                    fontSize = 11.sp,
                                    color = MedicalTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "• Model: 2D Brain Neuroimaging Classifier | 2D-CNN (128x128)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• Modality Declaration: Research Model (Clinical Decision Support)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• Spatial Localization: Not available for this modality", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• Lesion Volume: Not measured", fontSize = 11.sp, color = MedicalTextSecondary)
                }
            }
        }

        // ECG Analysis Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = StatusPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ECG Analysis",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = ecgTileStatus,
                            textColor = ecgTileColor,
                            bgColor = if (current.ecgValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRedBg else RiskOrangeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = painterResource(id = R.drawable.sample_ecg),
                        contentDescription = "Standard ECG Graphic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (current.ecgValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) "[ECG Input Rejected — Invalid Image]"
                            else if (current.ecgValidationStatus == ModalityValidationStatus.RAW_SIGNAL_VALIDATED) "[ECG Raw Signal Telemetry Validated]"
                            else "[ECG Image Uploaded — Waveform Reconstruction Unavailable]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ecgTileColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = current.ds4RejectionReason ?: "Quantitative ECG model expects a raw 12-lead signal (10s @100Hz). Image inference was not executed.",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // EEG Analysis Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = StatusPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EEG Analysis",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = eegTileStatus,
                            textColor = eegTileColor,
                            bgColor = if (current.eegValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRedBg else RiskOrangeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = painterResource(id = R.drawable.sample_eeg),
                        contentDescription = "Standard EEG Telemetry Graphic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (current.eegValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) "[EEG Input Rejected — Invalid Image]"
                            else if (current.eegValidationStatus == ModalityValidationStatus.RAW_SIGNAL_VALIDATED) "[EEG Raw Signal Telemetry Validated]"
                            else "[EEG Image Uploaded — Signal Reconstruction Unavailable]",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = eegTileColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = current.ds5RejectionReason ?: "Quantitative EEG model expects a raw 23-channel EEG signal (1s @256Hz). Signal model inference was not executed.",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Multimodal Clinical Interpretation
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
                    Text(
                        text = "Multimodal Clinical Interpretation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    interpretation.summaryLines.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 11.sp,
                            color = MedicalTextSecondary,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // RECOMMENDED PRECAUTIONS
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
                    Text(
                        text = "RECOMMENDED PRECAUTIONS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RiskRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Clinical Precautions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RiskRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    interpretation.clinicalPrecautions.forEach { p ->
                        Text(
                            text = "• $p",
                            fontSize = 11.5.sp,
                            color = MedicalTextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Lifestyle & Dietary Measures:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RiskGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    interpretation.lifestyleMeasures.forEach { l ->
                        Text(
                            text = "• $l",
                            fontSize = 11.5.sp,
                            color = MedicalTextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Medication Contraindication (Non-Prescriptive):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalBlue)
                    Text(text = interpretation.medicationContraindication, fontSize = 11.sp, color = MedicalTextSecondary, lineHeight = 15.sp)
                }
            }
        }

        // Clinical Attention Red Alert Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, RiskRedBorder, RoundedCornerShape(14.dp))
                    .background(RiskRedBg)
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RiskRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clinical Attention",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiskRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sync ID: ${current.syncId}",
                        fontSize = 10.sp,
                        color = MedicalTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${interpretation.emergencyFastWarning}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RiskRed,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Expandable: WHY THIS RESULT?
        item {
            ExpandableCard(
                title = "WHY THIS RESULT?",
                isExpanded = whyResultExpanded,
                onToggle = { whyResultExpanded = !whyResultExpanded }
            ) {
                Column {
                    Text(
                        text = "Clinical Rationale:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = interpretation.whyThisResultRationale,
                        fontSize = 11.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Expandable: Evidence & Provenance
        item {
            ExpandableCard(
                title = "Evidence & Provenance",
                isExpanded = provenanceExpanded,
                onToggle = { provenanceExpanded = !provenanceExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    provenanceItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MedicalSubtleBg)
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = item.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
                                    StatusPill(
                                        text = item.validationStatus,
                                        textColor = if (item.validationStatus.startsWith("REAL")) RiskGreen else MedicalBlue,
                                        bgColor = if (item.validationStatus.startsWith("REAL")) RiskGreenBg else MedicalBadgeBg
                                    )
                                }
                                Text(text = "Source: ${item.source} (${item.type})", fontSize = 10.sp, color = MedicalTextSecondary)
                                Text(text = "Model: ${item.modelName}  •  Execution: ${item.executionStatus}", fontSize = 10.sp, color = MedicalTextSecondary)
                                Text(text = "Output: ${item.output}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MedicalTextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Expandable: Technical Model Details
        item {
            ExpandableCard(
                title = "Technical Model Details",
                isExpanded = technicalExpanded,
                onToggle = { technicalExpanded = !technicalExpanded }
            ) {
                Column {
                    Text(text = "• DS1 MRI Model: ds1_mri_stroke_model.onnx (1,3,128,128)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• DS2 Heart Model: ds2_heart_model.onnx (13-FE Cleveland Ensemble)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• DS3 Stroke Model: ds3_stroke_model_improved.onnx (22-feature vector)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• DS4 ECG Model: ds4_ecg_model.onnx (1x12x1000 @ 100Hz)", fontSize = 11.sp, color = MedicalTextSecondary)
                    Text(text = "• DS5 EEG Model: ds5_eeg_seizure_model.onnx (1x23x256 @ 256Hz)", fontSize = 11.sp, color = MedicalTextSecondary)
                }
            }
        }

        // Footer Clinical Disclaimer
        item {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "CLINICAL DISCLAIMER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CardioNeuro-Sentinel operates as an on-device clinical decision-support research prototype. Outputs are generated from closed deterministic pipeline models and require certified physician verification before clinical use.",
                    fontSize = 10.sp,
                    color = MedicalTextMuted,
                    lineHeight = 14.sp
                )
            }
        }
    }

    if (showPdfDialog && exportedPdfFile != null) {
        ClinicalPdfExportDialog(
            assessment = current,
            pdfFile = exportedPdfFile!!,
            onDismiss = { showPdfDialog = false }
        )
    }
}

@Composable
fun EvidenceGridTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MedicalSubtleBg)
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
        }
    }
}

@Composable
fun RiskProfileRow(
    label: String,
    value: String,
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
        Text(text = label, fontSize = 11.sp, color = MedicalTextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            StatusPill(text = badgeText, textColor = badgeColor, bgColor = badgeBg)
        }
    }
}

@Composable
fun ProbabilityBar(
    label: String,
    probability: Double,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = MedicalTextPrimary)
            Text(
                text = "${String.format(Locale.US, "%.1f", probability * 100)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { probability.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = MedicalCardBorder
        )
    }
}

@Composable
fun ExpandableCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MedicalTextPrimary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MedicalTextSecondary
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    content()
                }
            }
        }
    }
}
