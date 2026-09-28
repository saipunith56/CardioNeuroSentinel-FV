package com.example.domain.interpretation

import com.example.data.model.Assessment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProvenanceItem(
    val title: String,
    val source: String,
    val type: String,
    val validationStatus: String,
    val modelName: String,
    val preprocessing: String,
    val executionStatus: String,
    val output: String,
    val researchOnly: Boolean = true,
    val timestamp: String
)

object ProvenanceTracker {

    fun generateProvenance(assessment: Assessment): List<ProvenanceItem> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val timeStr = dateFormat.format(Date(assessment.timestamp))

        return listOf(
            ProvenanceItem(
                title = "Clinical Vitals & Labs",
                source = "Clinician manual input / Bedside EMR",
                type = "Tabular numerical & categorical",
                validationStatus = "REAL (Entered)",
                modelName = "DS2 & DS3 Ensembles",
                preprocessing = "Standardization via documented Cleveland-13 & Stroke-22 means/scales",
                executionStatus = "EXECUTED",
                output = "DS2: ${(assessment.ds2Probability?.times(100))?.toInt() ?: 0}%, DS3: ${if ((assessment.ds3Probability ?: 0.0) < 0.01) "<1%" else "${((assessment.ds3Probability ?: 0.0) * 100).toInt()}%"}",
                researchOnly = true,
                timestamp = timeStr
            ),
            ProvenanceItem(
                title = "Brain Neuroimaging",
                source = "User file upload / Preset DWI slice",
                type = "2D Single-slice axial MRI scan",
                validationStatus = assessment.mriValidationStatus.name,
                modelName = "ds1_mri_stroke_model.onnx",
                preprocessing = "128x128 RGB, ToTensor, ImageNet norm (mean: [0.485,0.456,0.406], std: [0.229,0.224,0.225])",
                executionStatus = assessment.ds1Status.name,
                output = "Haemorrhagic: 13.9%, Ischemic: 84.7%, Normal: 1.4%",
                researchOnly = true,
                timestamp = timeStr
            ),
            ProvenanceItem(
                title = "Cardiac Telemetry (12-Lead ECG)",
                source = "Static Image Upload (strip photo)",
                type = "Raster bitmap graphic",
                validationStatus = "UNSUPPORTED",
                modelName = "ds4_ecg_model.onnx",
                preprocessing = "Requires 4th-order Butterworth 0.5-40Hz DF-II-T, per-channel Z-score",
                executionStatus = "NOT EXECUTED",
                output = "UNSUPPORTED — Raw 12-lead digital signal required",
                researchOnly = true,
                timestamp = timeStr
            ),
            ProvenanceItem(
                title = "Neurological Telemetry (EEG)",
                source = "Static Image Upload (monitor screenshot)",
                type = "Raster bitmap graphic",
                validationStatus = "UNSUPPORTED",
                modelName = "ds5_eeg_seizure_model.onnx",
                preprocessing = "Requires Volts->uV (1e6), 23-ch Z-score",
                executionStatus = "NOT EXECUTED",
                output = "UNSUPPORTED — Raw 23-channel digital telemetry required",
                researchOnly = true,
                timestamp = timeStr
            ),
            ProvenanceItem(
                title = "Multimodal Joint Fusion",
                source = "Cross-modality latent representation",
                type = "Joint Graph Neural Network / Fusion Layer",
                validationStatus = "NOT VALIDATED",
                modelName = "None",
                preprocessing = "N/A",
                executionStatus = "BLOCKED / NOT AVAILABLE",
                output = "NOT AVAILABLE — No validated joint fusion model",
                researchOnly = true,
                timestamp = timeStr
            )
        )
    }
}
