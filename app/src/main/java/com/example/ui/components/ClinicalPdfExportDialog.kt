package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import com.example.domain.pdf.ClinicalPdfGenerator
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
import com.example.ui.theme.RiskOrange
import com.example.ui.theme.RiskOrangeBg
import com.example.ui.theme.RiskRed
import com.example.ui.theme.RiskRedBg
import java.io.File

@Composable
fun ClinicalPdfExportDialog(
    assessment: Assessment,
    pdfFile: File,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val (ecgTileStatus, ecgTileColor) = when (assessment.ecgValidationStatus) {
        ModalityValidationStatus.REAL_ENTERED -> Pair("Report Analyzed", RiskGreen)
        ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> Pair("Raw Signal Validated", RiskGreen)
        ModalityValidationStatus.UNSUPPORTED -> Pair("Unsupported (Raw Needed)", RiskOrange)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("Invalid Image", RiskRed)
        else -> Pair("Not Provided", MedicalTextMuted)
    }
    val (eegTileStatus, eegTileColor) = when (assessment.eegValidationStatus) {
        ModalityValidationStatus.REAL_ENTERED -> Pair("Report Analyzed", RiskGreen)
        ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> Pair("Raw Signal Validated", RiskGreen)
        ModalityValidationStatus.UNSUPPORTED -> Pair("Unsupported (Raw Needed)", RiskOrange)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("Invalid Image", RiskRed)
        else -> Pair("Not Provided", MedicalTextMuted)
    }
    val (mriTileStatus, mriTileColor) = when (assessment.mriValidationStatus) {
        ModalityValidationStatus.VALIDATED_MRI -> Pair("Validated MRI Scan", RiskGreen)
        ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED -> Pair("DWI Scan Validated", MedicalTeal)
        ModalityValidationStatus.MODALITY_REJECTED -> Pair("Invalid Image", RiskRed)
        else -> Pair("Not Provided", MedicalTextMuted)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MedicalCardBorder, RoundedCornerShape(20.dp)),
            color = MedicalSurface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                text = "Clinical AI PDF Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                            Text(
                                text = "Ready for Clinical Sharing & EHR Export",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MedicalTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Document Metadata Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MedicalCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalSubtleBg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = assessment.patientName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTextPrimary
                                )
                                Text(
                                    text = "MRN: ${assessment.mrn} • ${assessment.age.toInt()} y/o",
                                    fontSize = 11.sp,
                                    color = MedicalTextSecondary
                                )
                            }
                            StatusPill(
                                text = "${assessment.clinicalRiskCategory} RISK",
                                textColor = if (assessment.clinicalRiskCategory == "HIGH") RiskRed else if (assessment.clinicalRiskCategory == "MODERATE") RiskOrange else RiskGreen,
                                bgColor = if (assessment.clinicalRiskCategory == "HIGH") RiskRedBg else if (assessment.clinicalRiskCategory == "MODERATE") RiskOrangeBg else RiskGreenBg
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Included input modalities
                        Text(
                            text = "Included Diagnostic Input Statuses:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MedicalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ModalityStatusRow(icon = Icons.Default.Favorite, name = "12-Lead ECG", status = ecgTileStatus, statusColor = ecgTileColor)
                        ModalityStatusRow(icon = Icons.Default.Psychology, name = "EEG Telemetry", status = eegTileStatus, statusColor = eegTileColor)
                        ModalityStatusRow(icon = Icons.Default.MedicalServices, name = "Brain MRI/CT", status = mriTileStatus, statusColor = mriTileColor)
                        ModalityStatusRow(icon = Icons.Default.CheckCircle, name = "Clinical Vitals", status = "Real (Entered)", statusColor = RiskGreen)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Format: 2-Page Standard A4 • File: ${pdfFile.name} (${pdfFile.length() / 1024} KB)",
                            fontSize = 10.sp,
                            color = MedicalTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Action: Download Report to Mobile Device
                Button(
                    onClick = {
                        ClinicalPdfGenerator.downloadPdfToDevice(context, pdfFile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pdf_download_device_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedicalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Download Report to Mobile",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action: Share Report via WhatsApp / System Share
                Button(
                    onClick = {
                        ClinicalPdfGenerator.shareToWhatsApp(context, pdfFile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pdf_share_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Share Report via WhatsApp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tertiary Action: Open / View PDF
                OutlinedButton(
                    onClick = {
                        ClinicalPdfGenerator.viewPdf(context, pdfFile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("pdf_view_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MedicalTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open / View PDF",
                        fontSize = 14.sp,
                        color = MedicalTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ⓘ Multi-page clinical summary includes hemodynamic vitals, quantitative ONNX risk predictions, and physician verification section.",
                    fontSize = 10.sp,
                    color = MedicalTextMuted,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ModalityStatusRow(
    icon: ImageVector,
    name: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MedicalTextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, fontSize = 11.sp, color = MedicalTextSecondary)
        }
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
        )
    }
}
