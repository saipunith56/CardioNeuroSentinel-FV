package com.example.ui.screens.assessment

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ModalityValidationStatus
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
import com.example.ui.theme.RiskOrange
import com.example.ui.theme.RiskOrangeBg
import com.example.ui.theme.RiskRed
import com.example.ui.theme.RiskRedBg
import com.example.ui.theme.StatusPink
import com.example.ui.theme.StatusPinkBg
import com.example.ui.theme.StatusPinkBorder
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusPurpleBg
import com.example.ui.theme.StatusPurpleBorder
import com.example.ui.viewmodel.AssessmentViewModel

@Composable
fun NewDiagnosticAssessmentScreen(
    viewModel: AssessmentViewModel,
    onNavigateBack: () -> Unit,
    onAssessmentGenerated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val activePatient by viewModel.activePatient.collectAsState()
    val patientName = activePatient?.name ?: "johan libert"

    val age by viewModel.age.collectAsState()
    val bmi by viewModel.bmi.collectAsState()
    val bp by viewModel.bloodPressure.collectAsState()
    val hr by viewModel.heartRate.collectAsState()
    val chol by viewModel.cholesterol.collectAsState()
    val glucose by viewModel.fastingGlucose.collectAsState()
    val troponin by viewModel.troponin.collectAsState()
    val nihss by viewModel.nihss.collectAsState()
    val isSmoker by viewModel.isSmoker.collectAsState()
    val familyCv by viewModel.familyCvHistory.collectAsState()
    val familyStroke by viewModel.familyStrokeHistory.collectAsState()
    val chiefComplaint by viewModel.chiefComplaint.collectAsState()

    // MRI Modality State
    val mriSourceType by viewModel.mriSourceType.collectAsState()
    val mriFileName by viewModel.mriFileName.collectAsState()
    val mriBitmap by viewModel.mriBitmap.collectAsState()
    val mriValidationStatus by viewModel.mriValidationStatus.collectAsState()
    val mriValidationMessage by viewModel.mriValidationMessage.collectAsState()
    val mriDeclared by viewModel.mriUserDeclared.collectAsState()

    // ECG Modality State
    val ecgSourceType by viewModel.ecgSourceType.collectAsState()
    val ecgFileName by viewModel.ecgFileName.collectAsState()
    val ecgBitmap by viewModel.ecgBitmap.collectAsState()
    val ecgValidationStatus by viewModel.ecgValidationStatus.collectAsState()
    val ecgValidationMessage by viewModel.ecgValidationMessage.collectAsState()

    // EEG Modality State
    val eegSourceType by viewModel.eegSourceType.collectAsState()
    val eegFileName by viewModel.eegFileName.collectAsState()
    val eegBitmap by viewModel.eegBitmap.collectAsState()
    val eegValidationStatus by viewModel.eegValidationStatus.collectAsState()
    val eegValidationMessage by viewModel.eegValidationMessage.collectAsState()

    val isGenerating by viewModel.isGenerating.collectAsState()
    val advancedExpanded by viewModel.advancedPresetsExpanded.collectAsState()

    // Real File Launchers
    val mriLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onMriFileSelected(context, uri)
        }
    }

    val ecgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onEcgFileSelected(context, uri)
        }
    }

    val eegLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onEegFileSelected(context, uri)
        }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MedicalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "New Diagnostic Assessment",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = patientName,
                        fontSize = 12.sp,
                        color = MedicalBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section 1 Header: Patient Clinical Data
        item {
            SectionHeader(
                icon = Icons.Default.Favorite,
                iconTint = MedicalBlue,
                title = "Patient Clinical Data",
                subtitle = "Vitals, anthropometrics, and labs"
            )
        }

        // Vitals & Demographics Card
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
                        text = "Patient Clinical Data",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: Age & BMI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalInputField(
                            label = "Age (Years)",
                            value = age,
                            onValueChange = { viewModel.age.value = it },
                            modifier = Modifier.weight(1f).testTag("input_age")
                        )
                        ClinicalInputField(
                            label = "BMI (kg/m²)",
                            value = bmi,
                            onValueChange = { viewModel.bmi.value = it },
                            modifier = Modifier.weight(1f).testTag("input_bmi")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Blood Pressure & Heart Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalInputField(
                            label = "Blood Pressure (mmHg)",
                            value = bp,
                            onValueChange = { viewModel.bloodPressure.value = it },
                            modifier = Modifier.weight(1f).testTag("input_bp")
                        )
                        ClinicalInputField(
                            label = "Heart Rate (BPM)",
                            value = hr,
                            onValueChange = { viewModel.heartRate.value = it },
                            modifier = Modifier.weight(1f).testTag("input_hr")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Total Cholesterol & Fasting Glucose
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalInputField(
                            label = "Total Cholesterol (mg/dL)",
                            value = chol,
                            onValueChange = { viewModel.cholesterol.value = it },
                            modifier = Modifier.weight(1f).testTag("input_chol")
                        )
                        ClinicalInputField(
                            label = "Fasting Glucose (mg/dL)",
                            value = glucose,
                            onValueChange = { viewModel.fastingGlucose.value = it },
                            modifier = Modifier.weight(1f).testTag("input_glucose")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 4: Troponin & NIHSS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalInputField(
                            label = "Troponin I (ng/mL)",
                            value = troponin,
                            onValueChange = { viewModel.troponin.value = it },
                            modifier = Modifier.weight(1f).testTag("input_troponin")
                        )
                        ClinicalInputField(
                            label = "NIH Stroke Scale (0-42)",
                            value = nihss,
                            onValueChange = { viewModel.nihss.value = it },
                            modifier = Modifier.weight(1f).testTag("input_nihss")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Smoker Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current Smoker",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                        Switch(
                            checked = isSmoker,
                            onCheckedChange = { viewModel.isSmoker.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MedicalBlue
                            ),
                            modifier = Modifier.testTag("toggle_smoker")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Family History Checkbox 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = familyCv,
                            onCheckedChange = { viewModel.familyCvHistory.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = MedicalBlue),
                            modifier = Modifier.testTag("check_family_cv")
                        )
                        Text(
                            text = "Family History of Cardiovascular Disease",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                    }

                    // Family History Checkbox 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = familyStroke,
                            onCheckedChange = { viewModel.familyStrokeHistory.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = MedicalBlue),
                            modifier = Modifier.testTag("check_family_stroke")
                        )
                        Text(
                            text = "Family History of Cerebrovascular Stroke",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chief Complaint
                    ClinicalInputField(
                        label = "Chief Complaint / Reported Symptoms",
                        value = chiefComplaint,
                        onValueChange = { viewModel.chiefComplaint.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_chief_complaint")
                    )
                }
            }
        }

        // Section 2 Header: Cardiac Telemetry (12-Lead ECG)
        item {
            SectionHeader(
                icon = Icons.Default.Favorite,
                iconTint = MedicalBlue,
                title = "Cardiac Telemetry (12-Lead ECG)",
                subtitle = "Upload ECG image, report photo, or waveform"
            )
        }

        // 12-Lead ECG Card
        item {
            ModalityUploadCard(
                icon = Icons.Default.Favorite,
                iconBg = StatusPinkBg,
                iconTint = StatusPink,
                title = "12-Lead ECG Analysis",
                validationStatus = ecgValidationStatus,
                validationMessage = ecgValidationMessage,
                fileName = ecgFileName,
                bitmap = ecgBitmap,
                sourceType = ecgSourceType,
                containerBorderColor = StatusPinkBorder,
                uploadTypeLabel = "ECG Image / Report Photo",
                buttonText = "Upload ECG Image or Report",
                buttonTag = "upload_ecg_button",
                footerNote = "Accepts ECG image tracings and reports. Automatically evaluates rhythm, ST elevation, and arrhythmia patterns.",
                onUploadClick = {
                    ecgLauncher.launch("image/*")
                },
                onClearClick = {
                    viewModel.clearEcgFile()
                }
            )
        }

        // Section 3 Header: Neurological & Neuroimaging
        item {
            SectionHeader(
                icon = Icons.Default.Psychology,
                iconTint = MedicalBlue,
                title = "Neurological & Neuroimaging",
                subtitle = "Upload EEG and Brain MRI/CT scan or report"
            )
        }

        // EEG Card
        item {
            ModalityUploadCard(
                icon = Icons.Default.Psychology,
                iconBg = StatusPurpleBg,
                iconTint = StatusPurple,
                title = "EEG Electroencephalography",
                validationStatus = eegValidationStatus,
                validationMessage = eegValidationMessage,
                fileName = eegFileName,
                bitmap = eegBitmap,
                sourceType = eegSourceType,
                containerBorderColor = StatusPurpleBorder,
                uploadTypeLabel = "EEG Image / Report Photo",
                buttonText = "Upload EEG Image or Report",
                buttonTag = "upload_eeg_button",
                footerNote = "Accepts EEG image tracings and reports. Automatically evaluates background activity, slowing, and epileptiform spikes.",
                onUploadClick = {
                    eegLauncher.launch("image/*")
                },
                onClearClick = {
                    viewModel.clearEegFile()
                }
            )
        }

        // Neuroimaging MRI / CT Card
        item {
            ModalityUploadCard(
                icon = Icons.Default.MedicalServices,
                iconBg = MedicalBadgeBg,
                iconTint = MedicalTeal,
                title = "Neuroimaging (MRI / CT)",
                validationStatus = mriValidationStatus,
                validationMessage = mriValidationMessage,
                fileName = mriFileName,
                bitmap = mriBitmap,
                sourceType = mriSourceType,
                containerBorderColor = MedicalCardBorder,
                uploadTypeLabel = "MRI / CT Image or Report",
                buttonText = "Upload MRI / CT Image or Report",
                buttonTag = "upload_mri_button",
                footerNote = "Accepts 2D Brain MRI slices (DWI, T1, T2) or brain CT. Color photos and unrelated screenshots are strictly rejected.",
                onUploadClick = {
                    mriLauncher.launch("image/*")
                },
                onClearClick = {
                    viewModel.clearMriFile()
                },
                extraContent = {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = mriDeclared,
                            onCheckedChange = { checked ->
                                viewModel.mriUserDeclared.value = checked
                                if (mriBitmap != null && mriSourceType == "IMAGE") {
                                    val reval = com.example.domain.validation.ModalityValidator.validateImage(mriBitmap, checked)
                                    viewModel.mriValidationStatus.value = reval.status
                                    viewModel.mriValidationMessage.value = reval.rejectionReason ?: if (reval.canExecuteDs1) "Valid Neuroimaging Input" else "Unvalidated Modality"
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MedicalTeal),
                            modifier = Modifier.testTag("check_mri_user_declared")
                        )
                        Text(
                            text = "I declare and confirm this image is a 2D brain MRI scan slice (Research use)",
                            fontSize = 11.sp,
                            color = MedicalTextSecondary
                        )
                    }
                }
            )
        }

        // Section 4: Advanced / Raw Data & Simulation Presets (Expandable Accordion)
        item {
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
                            .clickable { viewModel.advancedPresetsExpanded.value = !advancedExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MedicalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Advanced / Raw Data & Simulation Presets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        Icon(
                            imageVector = if (advancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MedicalTextSecondary
                        )
                    }

                    AnimatedVisibility(visible = advancedExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Text(
                                text = "Telemetry & Sensor Data Modes (For Research & Testing):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // ECG Preset Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "ECG Source Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
                                    Text(text = "Current: $ecgSourceType", fontSize = 10.sp, color = MedicalTextSecondary)
                                }
                                Row {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.ecgSourceType.value = "RAW_12LEAD"
                                            viewModel.ecgValidationStatus.value = ModalityValidationStatus.RAW_SIGNAL_VALIDATED
                                            viewModel.ecgValidationMessage.value = "Raw 12-lead digital telemetry signal loaded (10s @100Hz)."
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Raw 12-Lead", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.ecgSourceType.value = "PRESET_AFIB"
                                            viewModel.ecgValidationStatus.value = ModalityValidationStatus.UNSUPPORTED
                                            viewModel.ecgValidationMessage.value = com.example.domain.validation.SignalValidator.ECG_IMAGE_UNSUPPORTED_MSG
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Preset", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // EEG Preset Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "EEG Source Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
                                    Text(text = "Current: $eegSourceType", fontSize = 10.sp, color = MedicalTextSecondary)
                                }
                                Row {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.eegSourceType.value = "RAW_23CHANNEL"
                                            viewModel.eegValidationStatus.value = ModalityValidationStatus.RAW_SIGNAL_VALIDATED
                                            viewModel.eegValidationMessage.value = "Raw 23-channel digital telemetry loaded (1s @256Hz)."
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Raw 23-Ch", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.eegSourceType.value = "PRESET_SLOWING"
                                            viewModel.eegValidationStatus.value = ModalityValidationStatus.UNSUPPORTED
                                            viewModel.eegValidationMessage.value = com.example.domain.validation.SignalValidator.EEG_IMAGE_UNSUPPORTED_MSG
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Preset", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA: Generate Prediction
        item {
            Button(
                onClick = {
                    viewModel.generateAiPrediction { assessmentId ->
                        onAssessmentGenerated(assessmentId)
                    }
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_prediction_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Executing ONNX Inference Pipeline...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate AI Prediction",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Verification Caption
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ⓘ Clinical Decision Support System. Requires certified physician verification.",
                    fontSize = 11.sp,
                    color = MedicalTextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MedicalTextSecondary
            )
        }
    }
}

@Composable
fun ClinicalInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MedicalSurface,
            unfocusedContainerColor = MedicalSurface,
            focusedBorderColor = MedicalBlue,
            unfocusedBorderColor = MedicalCardBorder,
            focusedLabelColor = MedicalBlue,
            unfocusedLabelColor = MedicalTextSecondary,
            focusedTextColor = MedicalTextPrimary,
            unfocusedTextColor = MedicalTextPrimary,
            cursorColor = MedicalBlue
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MedicalTextPrimary
        ),
        modifier = modifier
    )
}

@Composable
fun ModalityUploadCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    validationStatus: ModalityValidationStatus,
    validationMessage: String?,
    fileName: String?,
    bitmap: Bitmap?,
    sourceType: String,
    containerBorderColor: Color,
    uploadTypeLabel: String,
    buttonText: String,
    buttonTag: String,
    footerNote: String,
    onUploadClick: () -> Unit,
    onClearClick: () -> Unit,
    extraContent: @Composable (() -> Unit)? = null
) {
    // Map status to badge label & color
    val (statusLabel, statusTextColor, statusBgColor) = when (validationStatus) {
        ModalityValidationStatus.REAL_ENTERED -> Triple(if (sourceType.startsWith("PRESET")) "PRESET REPORT" else "REPORT ANALYZED", RiskGreen, RiskGreenBg)
        ModalityValidationStatus.VALIDATED_MRI -> Triple("VALIDATED MRI", RiskGreen, RiskGreenBg)
        ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED -> Triple(if (sourceType.startsWith("PRESET")) "PRESET" else "USER DECLARED", MedicalTeal, MedicalBadgeBg)
        ModalityValidationStatus.UNSUPPORTED -> Triple("UNSUPPORTED — RAW REQUIRED", RiskOrange, RiskOrangeBg)
        ModalityValidationStatus.MODALITY_REJECTED -> Triple("INVALID IMAGE", RiskRed, RiskRedBg)
        ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> Triple("RAW SIGNAL VALIDATED", RiskGreen, RiskGreenBg)
        ModalityValidationStatus.MODALITY_UNVALIDATED -> Triple("UNVALIDATED", RiskOrange, RiskOrangeBg)
        else -> Triple("NOT PROVIDED", MedicalTextMuted, MedicalSubtleBg)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with Title and Status pill
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
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                }
                StatusPill(
                    text = statusLabel,
                    textColor = statusTextColor,
                    bgColor = statusBgColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Upload Box Container with Tinted Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, if (validationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRed else containerBorderColor, RoundedCornerShape(12.dp))
                    .background(MedicalSubtleBg)
                    .padding(14.dp)
            ) {
                Column {
                    if (bitmap != null) {
                        // Display decoded image preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Uploaded scan preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fileName ?: "Uploaded Image",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${bitmap.width}x${bitmap.height} px",
                                    fontSize = 11.sp,
                                    color = MedicalTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row {
                                    OutlinedButton(
                                        onClick = onUploadClick,
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Replace", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = onClearClick,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RiskRed),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else if (fileName != null && sourceType != "NONE") {
                        // Preset / Selected without decoded preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = fileName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedicalTextPrimary)
                                    Text(text = "Source: $sourceType", fontSize = 10.sp, color = MedicalTextSecondary)
                                }
                            }
                            Row {
                                OutlinedButton(
                                    onClick = onUploadClick,
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Change", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedButton(
                                    onClick = onClearClick,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RiskRed),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Clear", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        // Empty state: Upload button
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uploadTypeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onUploadClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = iconTint),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag(buttonTag)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Validation message / Explanation if present
                    if (!validationMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (validationStatus == ModalityValidationStatus.MODALITY_REJECTED) Icons.Default.Error
                                else if (validationStatus == ModalityValidationStatus.UNSUPPORTED) Icons.Default.Warning
                                else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (validationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRed
                                else if (validationStatus == ModalityValidationStatus.UNSUPPORTED) RiskOrange
                                else RiskGreen,
                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = validationMessage,
                                fontSize = 11.sp,
                                color = if (validationStatus == ModalityValidationStatus.MODALITY_REJECTED) RiskRed else MedicalTextSecondary,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            if (extraContent != null) {
                extraContent()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer note
            Text(
                text = footerNote,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MedicalTextMuted,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
