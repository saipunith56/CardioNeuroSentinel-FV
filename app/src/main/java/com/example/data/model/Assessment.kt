package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ModalityValidationStatus {
    REAL_ENTERED,
    MODALITY_REJECTED,
    MODALITY_UNVALIDATED,
    USER_DECLARED_NOT_VALIDATED,
    VALIDATED_MRI,
    UNSUPPORTED,
    RAW_SIGNAL_VALIDATED,
    NOT_PROVIDED
}

enum class ModelExecutionStatus {
    EXECUTED,
    NOT_EXECUTED,
    INFERENCE_UNAVAILABLE,
    UNSUPPORTED_INPUT
}

@Entity(tableName = "assessments")
data class Assessment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val mrn: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Clinical Vitals & Demographics
    val age: Double,
    val bmi: Double,
    val systolicBp: Double,
    val diastolicBp: Double,
    val heartRate: Double,
    val cholesterol: Double,
    val fastingGlucose: Double,
    val troponin: Double,
    val nihss: Int,
    val isSmoker: Boolean,
    val familyCvHistory: Boolean,
    val familyStrokeHistory: Boolean,
    val chiefComplaint: String,
    
    // Inputs & Sources
    val mriSourceType: String, // "NONE", "IMAGE", "PRESET_DWI", "RAW"
    val mriUri: String?,
    val mriUserDeclared: Boolean,
    val ecgSourceType: String, // "NONE", "IMAGE", "PRESET_AFIB", "RAW_12LEAD"
    val ecgUri: String?,
    val eegSourceType: String, // "NONE", "IMAGE", "PRESET_SLOWING", "RAW_23CHANNEL"
    val eegUri: String?,
    
    // Validation Statuses
    val vitalsStatus: ModalityValidationStatus = ModalityValidationStatus.REAL_ENTERED,
    val mriValidationStatus: ModalityValidationStatus,
    val ecgValidationStatus: ModalityValidationStatus,
    val eegValidationStatus: ModalityValidationStatus,
    
    // DS1 MRI Model (ds1_mri_stroke_model.onnx)
    val ds1Status: ModelExecutionStatus,
    val ds1HemorrhagicProb: Double?,
    val ds1IschemicProb: Double?,
    val ds1NormalProb: Double?,
    val ds1SummaryLabel: String?,
    
    // DS2 Heart Model (ds2_heart_model.onnx)
    val ds2Status: ModelExecutionStatus,
    val ds2Probability: Double?,
    val ds2ImputationBacked: Boolean,
    
    // DS3 Stroke Model (ds3_stroke_model_improved.onnx)
    val ds3Status: ModelExecutionStatus,
    val ds3Probability: Double?,
    
    // DS4 ECG Model (ds4_ecg_model.onnx)
    val ds4Status: ModelExecutionStatus,
    val ds4NormProb: Double? = null,
    val ds4MiProb: Double? = null,
    val ds4SttcProb: Double? = null,
    val ds4CdProb: Double? = null,
    val ds4HypProb: Double? = null,
    val ds4RejectionReason: String? = null,
    
    // DS5 EEG Model (ds5_eeg_seizure_model.onnx)
    val ds5Status: ModelExecutionStatus,
    val ds5SeizureProb: Double? = null,
    val ds5RejectionReason: String? = null,
    
    // Strict Multimodal Fusion Rules: Must ALWAYS be null!
    val combinedRiskScorePct: Double? = null,
    val fusionStatusMessage: String = "NOT AVAILABLE — No validated joint fusion model",
    
    // Diagnostics & Provenance metadata
    val toastSubtype: String = "Stroke of Undetermined Etiology (SUE - Incomplete Imaging) [RULE_BASED/HEURISTIC]",
    val gnnCrosstalk: String? = null, // Always null per contract
    val clinicalRiskCategory: String = "MODERATE", // "LOW", "MODERATE", "HIGH"
    val syncId: String = "893892015"
)
