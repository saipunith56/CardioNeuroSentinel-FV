package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Assessment
import com.example.data.model.Patient
import com.example.data.repository.AssessmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Severity categories for Cardio-Cerebrovascular risk.
 */
enum class RiskLevel(
    val label: String,
    val description: String,
    val minPct: Double,
    val maxPct: Double
) {
    LOW(
        label = "LOW RISK",
        description = "0 to 1 abnormal input — Optimal cardio-cerebrovascular profile",
        minPct = 0.0,
        maxPct = 29.9
    ),
    MODERATE(
        label = "MODERATE RISK",
        description = "2 abnormal inputs (or up to half) — Elevated surveillance recommended",
        minPct = 30.0,
        maxPct = 64.9
    ),
    HIGH(
        label = "HIGH RISK",
        description = "More than half of inputs abnormal — Urgent multidisciplinary intervention",
        minPct = 65.0,
        maxPct = 100.0
    )
}

/**
 * Organ/System categories for multimodal clinical evidence.
 */
enum class ModalityCategory(val displayName: String) {
    CARDIAC("Cardiovascular"),
    CEREBROVASCULAR("Cerebrovascular & Neuro"),
    SYSTEMIC("Systemic Biomarkers & Vitals")
}

/**
 * Individual clinical or diagnostic input evaluated for Cardio-Cerebrovascular risk.
 */
data class MultimodalInput(
    val id: String,
    val name: String,
    val shortName: String,
    val category: ModalityCategory,
    val isAbnormal: Boolean,
    val normalFinding: String,
    val abnormalFinding: String,
    val weight: Double, // Overall contribution weight (1.0 - 2.0)
    val cardiotropicWeight: Double, // Weight towards cardiovascular risk
    val neurotropicWeight: Double   // Weight towards stroke/cerebrovascular risk
) {
    val currentFinding: String
        get() = if (isAbnormal) abnormalFinding else normalFinding

    val statusLabel: String
        get() = if (isAbnormal) "ABNORMAL" else "NORMAL"
}

/**
 * Reactive UI state for the Risk Assessment Dashboard.
 */
data class RiskDashboardUiState(
    val overallRiskPct: Double = 14.0,
    val cardioRiskPct: Double = 15.0,
    val cerebrovascularRiskPct: Double = 12.0,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val abnormalCount: Int = 0,
    val totalCount: Int = 9,
    val inputs: List<MultimodalInput> = emptyList(),
    val clinicalSummary: String = "",
    val activePresetName: String? = "All Normal (Baseline)",
    val gnnAxisSynthesis: String = "Coupled Heart-Brain Axis Synchronized"
)

/**
 * ViewModel implementing the clinical counting and weighted score logic:
 * - If all normal or <= 1 abnormal -> Low Risk (percentage < 30%)
 * - If 2 abnormal inputs (or up to half) -> Moderate Risk (percentage in 30%..64%)
 * - If more than half (or all) abnormal inputs -> High Risk (percentage in 65%..100%)
 *
 * Utilizes a weighted scoring system reflecting overall Cardio-Cerebrovascular risk.
 */
class RiskAssessmentViewModel(
    private val assessmentRepository: AssessmentRepository? = null
) : ViewModel() {

    private val defaultInputs = listOf(
        MultimodalInput(
            id = "ecg",
            name = "12-Lead ECG Tracing",
            shortName = "ECG",
            category = ModalityCategory.CARDIAC,
            isAbnormal = false,
            normalFinding = "Normal sinus rhythm (HR 72 bpm, normal PR/QT intervals)",
            abnormalFinding = "Atrial Fibrillation / ST-T Wave Ischemia & Arrhythmia",
            weight = 1.8,
            cardiotropicWeight = 2.0,
            neurotropicWeight = 1.3
        ),
        MultimodalInput(
            id = "eeg",
            name = "EEG Telemetry",
            shortName = "EEG",
            category = ModalityCategory.CEREBROVASCULAR,
            isAbnormal = false,
            normalFinding = "Normal Alpha/Beta activity (no epileptiform or focal slowing)",
            abnormalFinding = "Focal Temporal Slowing / Lateralized Periodic Discharges",
            weight = 1.5,
            cardiotropicWeight = 0.4,
            neurotropicWeight = 2.0
        ),
        MultimodalInput(
            id = "mri",
            name = "Brain MRI / Neuroimaging",
            shortName = "Brain MRI",
            category = ModalityCategory.CEREBROVASCULAR,
            isAbnormal = false,
            normalFinding = "No acute ischemic lesion; normal cerebral parenchyma",
            abnormalFinding = "Acute Diffusion Restriction / Microvascular White Matter Infarct",
            weight = 2.0,
            cardiotropicWeight = 0.5,
            neurotropicWeight = 2.3
        ),
        MultimodalInput(
            id = "bp",
            name = "Blood Pressure (Vitals)",
            shortName = "Blood Pressure",
            category = ModalityCategory.SYSTEMIC,
            isAbnormal = false,
            normalFinding = "118 / 76 mmHg (Optimal Normotensive)",
            abnormalFinding = "164 / 98 mmHg (Stage 2 Hypertension)",
            weight = 1.6,
            cardiotropicWeight = 1.8,
            neurotropicWeight = 1.7
        ),
        MultimodalInput(
            id = "glucose",
            name = "Fasting Blood Glucose",
            shortName = "Glucose",
            category = ModalityCategory.SYSTEMIC,
            isAbnormal = false,
            normalFinding = "88 mg/dL (Normal Glycemic Control)",
            abnormalFinding = "168 mg/dL (Diabetic Hyperglycemia)",
            weight = 1.4,
            cardiotropicWeight = 1.3,
            neurotropicWeight = 1.5
        ),
        MultimodalInput(
            id = "cholesterol",
            name = "Serum Total Cholesterol",
            shortName = "Cholesterol",
            category = ModalityCategory.CARDIAC,
            isAbnormal = false,
            normalFinding = "172 mg/dL (Desirable Lipid Profile)",
            abnormalFinding = "264 mg/dL (Severe Hypercholesterolemia)",
            weight = 1.3,
            cardiotropicWeight = 1.7,
            neurotropicWeight = 0.9
        ),
        MultimodalInput(
            id = "smoking",
            name = "Smoking / Nicotine Status",
            shortName = "Smoking",
            category = ModalityCategory.SYSTEMIC,
            isAbnormal = false,
            normalFinding = "Non-Smoker (Zero tobacco exposure)",
            abnormalFinding = "Active Smoker (Daily cigarette / tobacco use)",
            weight = 1.2,
            cardiotropicWeight = 1.5,
            neurotropicWeight = 1.4
        ),
        MultimodalInput(
            id = "bmi",
            name = "Body Mass Index (BMI)",
            shortName = "BMI",
            category = ModalityCategory.SYSTEMIC,
            isAbnormal = false,
            normalFinding = "22.8 kg/m² (Normal Weight)",
            abnormalFinding = "33.4 kg/m² (Class I Obesity)",
            weight = 1.0,
            cardiotropicWeight = 1.2,
            neurotropicWeight = 1.1
        ),
        MultimodalInput(
            id = "family_hx",
            name = "Family Cardio/Stroke History",
            shortName = "Family History",
            category = ModalityCategory.SYSTEMIC,
            isAbnormal = false,
            normalFinding = "Absent (No premature CAD or stroke in first-degree relatives)",
            abnormalFinding = "Present (Positive early stroke and myocardial infarction)",
            weight = 1.1,
            cardiotropicWeight = 1.3,
            neurotropicWeight = 1.3
        )
    )

    private val _uiState = MutableStateFlow(calculateState(defaultInputs, "All Normal (Baseline)"))
    val uiState: StateFlow<RiskDashboardUiState> = _uiState.asStateFlow()

    init {
        // If assessment repository is provided, we can optionally load the latest assessment
        viewModelScope.launch {
            assessmentRepository?.latestAssessment?.collect { latest ->
                if (latest != null && _uiState.value.activePresetName == "All Normal (Baseline)") {
                    loadFromAssessment(latest)
                }
            }
        }
    }

    /**
     * Toggles a single multimodal input between Normal and Abnormal.
     */
    fun toggleInputStatus(inputId: String) {
        _uiState.update { current ->
            val updatedInputs = current.inputs.map { input ->
                if (input.id == inputId) {
                    input.copy(isAbnormal = !input.isAbnormal)
                } else {
                    input
                }
            }
            calculateState(updatedInputs, activePresetName = null)
        }
    }

    /**
     * Explicitly sets a specific input status.
     */
    fun setInputStatus(inputId: String, isAbnormal: Boolean) {
        _uiState.update { current ->
            val updatedInputs = current.inputs.map { input ->
                if (input.id == inputId) {
                    input.copy(isAbnormal = isAbnormal)
                } else {
                    input
                }
            }
            calculateState(updatedInputs, activePresetName = null)
        }
    }

    /**
     * Preset: All inputs Normal (Baseline) -> Low Risk.
     */
    fun setAllNormalPreset() {
        val updated = defaultInputs.map { it.copy(isAbnormal = false) }
        _uiState.value = calculateState(updated, "All Normal (Low Risk)")
    }

    /**
     * Preset: Exactly 2 inputs abnormal -> Moderate Risk.
     * (E.g. Blood Pressure elevated and ECG abnormal).
     */
    fun setModerateRiskPreset() {
        val updated = defaultInputs.map { input ->
            when (input.id) {
                "bp", "ecg" -> input.copy(isAbnormal = true)
                else -> input.copy(isAbnormal = false)
            }
        }
        _uiState.value = calculateState(updated, "2 Abnormal (Moderate Risk)")
    }

    /**
     * Preset: More than half of inputs abnormal (>50%) -> High Risk.
     * (E.g. ECG, MRI, EEG, BP, and Glucose abnormal -> 5 of 9).
     */
    fun setHighRiskPreset() {
        val updated = defaultInputs.map { input ->
            when (input.id) {
                "ecg", "mri", "eeg", "bp", "glucose" -> input.copy(isAbnormal = true)
                else -> input.copy(isAbnormal = false)
            }
        }
        _uiState.value = calculateState(updated, "High Risk (>50% Abnormal)")
    }

    /**
     * Preset: Cardiac Dominant Profile.
     */
    fun setCardiacDominantPreset() {
        val updated = defaultInputs.map { input ->
            when (input.id) {
                "ecg", "bp", "cholesterol", "smoking" -> input.copy(isAbnormal = true)
                else -> input.copy(isAbnormal = false)
            }
        }
        _uiState.value = calculateState(updated, "Cardiovascular Dominant")
    }

    /**
     * Preset: Cerebrovascular / Stroke Dominant Profile.
     */
    fun setCerebrovascularDominantPreset() {
        val updated = defaultInputs.map { input ->
            when (input.id) {
                "mri", "eeg", "bp", "glucose" -> input.copy(isAbnormal = true)
                else -> input.copy(isAbnormal = false)
            }
        }
        _uiState.value = calculateState(updated, "Cerebrovascular Dominant")
    }

    /**
     * Loads patient clinical values from an Assessment entity.
     */
    fun loadFromAssessment(assessment: Assessment) {
        val updated = defaultInputs.map { input ->
            val isAbnormal = when (input.id) {
                "ecg" -> (assessment.ecgSourceType == "PRESET_AFIB") ||
                        (assessment.ds4NormProb != null && assessment.ds4NormProb < 0.5) ||
                        (assessment.ds4MiProb != null && assessment.ds4MiProb > 0.3)
                "eeg" -> (assessment.eegSourceType == "PRESET_SLOWING") ||
                        (assessment.ds5SeizureProb != null && assessment.ds5SeizureProb > 0.3)
                "mri" -> (assessment.mriSourceType == "PRESET_DWI") ||
                        (assessment.ds1IschemicProb != null && assessment.ds1IschemicProb > 0.4) ||
                        (assessment.ds1SummaryLabel != null && !assessment.ds1SummaryLabel.contains("NORMAL", ignoreCase = true))
                "bp" -> assessment.systolicBp >= 140 || assessment.diastolicBp >= 90
                "glucose" -> assessment.fastingGlucose >= 126
                "cholesterol" -> assessment.cholesterol >= 240
                "smoking" -> assessment.isSmoker
                "bmi" -> assessment.bmi >= 30.0
                "family_hx" -> assessment.familyCvHistory || assessment.familyStrokeHistory
                else -> false
            }
            input.copy(isAbnormal = isAbnormal)
        }
        _uiState.value = calculateState(updated, "Patient: ${assessment.patientName}")
    }

    /**
     * Core weighted score calculation matching clinical counting rules:
     * - <= 1 abnormal -> Low Risk (percentage < 30%)
     * - 2 abnormal (up to half) -> Moderate Risk (percentage in 30%..64%)
     * - > half abnormal -> High Risk (percentage in 65%..100%)
     */
    companion object {
        fun calculateRisk(inputs: List<MultimodalInput>): Triple<Double, Double, Double> {
            val totalCount = inputs.size
            val abnormalInputs = inputs.filter { it.isAbnormal }
            val abnormalCount = abnormalInputs.size

            val totalOverallWeight = inputs.sumOf { it.weight }.coerceAtLeast(1.0)
            val abnormalOverallWeight = abnormalInputs.sumOf { it.weight }
            val weightedOverallRatio = abnormalOverallWeight / totalOverallWeight

            val totalCardioWeight = inputs.sumOf { it.cardiotropicWeight }.coerceAtLeast(1.0)
            val abnormalCardioWeight = abnormalInputs.sumOf { it.cardiotropicWeight }
            val cardioRatio = abnormalCardioWeight / totalCardioWeight

            val totalNeuroWeight = inputs.sumOf { it.neurotropicWeight }.coerceAtLeast(1.0)
            val abnormalNeuroWeight = abnormalInputs.sumOf { it.neurotropicWeight }
            val neuroRatio = abnormalNeuroWeight / totalNeuroWeight

            // Apply clinical rule brackets
            val halfThreshold = totalCount / 2 // for 9 inputs, half is 4. > half is 5+

            val overallRiskPct = when {
                abnormalCount <= 1 -> {
                    // Low risk bracket: 10% - 24%
                    (10.0 + (weightedOverallRatio * 14.0)).coerceIn(10.0, 24.0)
                }
                abnormalCount in 2..halfThreshold -> {
                    // Moderate risk bracket: 36% - 56%
                    (36.0 + (weightedOverallRatio * 20.0)).coerceIn(36.0, 56.0)
                }
                else -> {
                    // High risk bracket: 68% - 94%
                    (68.0 + (weightedOverallRatio * 26.0)).coerceIn(68.0, 94.0)
                }
            }

            // Cardiac Risk Percentage (scaled according to cardioRatio and bounded by category)
            val cardioRiskPct = when {
                abnormalCount <= 1 -> (12.0 + (cardioRatio * 12.0)).coerceIn(10.0, 25.0)
                abnormalCount in 2..halfThreshold -> (34.0 + (cardioRatio * 26.0)).coerceIn(32.0, 62.0)
                else -> (65.0 + (cardioRatio * 30.0)).coerceIn(65.0, 95.0)
            }

            // Cerebrovascular Risk Percentage (scaled according to neuroRatio and bounded by category)
            val cerebrovascularRiskPct = when {
                abnormalCount <= 1 -> (10.0 + (neuroRatio * 12.0)).coerceIn(8.0, 24.0)
                abnormalCount in 2..halfThreshold -> (32.0 + (neuroRatio * 26.0)).coerceIn(30.0, 60.0)
                else -> (65.0 + (neuroRatio * 30.0)).coerceIn(65.0, 94.0)
            }

            return Triple(
                (overallRiskPct * 10.0).roundToInt() / 10.0,
                (cardioRiskPct * 10.0).roundToInt() / 10.0,
                (cerebrovascularRiskPct * 10.0).roundToInt() / 10.0
            )
        }

        private fun calculateState(
            inputs: List<MultimodalInput>,
            activePresetName: String?
        ): RiskDashboardUiState {
            val (overallRisk, cardioRisk, neuroRisk) = calculateRisk(inputs)
            val abnormalCount = inputs.count { it.isAbnormal }
            val totalCount = inputs.size

            val riskLevel = when {
                abnormalCount <= 1 -> RiskLevel.LOW
                abnormalCount in 2..(totalCount / 2) -> RiskLevel.MODERATE
                else -> RiskLevel.HIGH
            }

            val clinicalSummary = when (riskLevel) {
                RiskLevel.LOW ->
                    "Optimal cardio-cerebrovascular profile ($abnormalCount of $totalCount abnormal). Low risk of heart disease or cerebrovascular stroke. Maintain routine wellness screening."
                RiskLevel.MODERATE ->
                    "Moderate risk stratification ($abnormalCount of $totalCount abnormal inputs). Elevated cardio-cerebrovascular risk detected. Targeted clinical surveillance and secondary prevention indicated."
                RiskLevel.HIGH ->
                    "High risk stratification ($abnormalCount of $totalCount abnormal inputs). Critical risk of stroke or acute cardiac event. Multidisciplinary Cardio-Neuro evaluation strongly recommended."
            }

            val gnnSynthesis = when (riskLevel) {
                RiskLevel.LOW -> "GNN Axis Coupled • Stable Neuro-Cardiac Synchrony"
                RiskLevel.MODERATE -> "GNN Axis Coupled • Moderate Neuro-Cardiac Crosstalk Perturbation"
                RiskLevel.HIGH -> "GNN Axis Coupled • High-Risk Synergistic Neuro-Cardiac Axis Dysfunction"
            }

            return RiskDashboardUiState(
                overallRiskPct = overallRisk,
                cardioRiskPct = cardioRisk,
                cerebrovascularRiskPct = neuroRisk,
                riskLevel = riskLevel,
                abnormalCount = abnormalCount,
                totalCount = totalCount,
                inputs = inputs,
                clinicalSummary = clinicalSummary,
                activePresetName = activePresetName,
                gnnAxisSynthesis = gnnSynthesis
            )
        }
    }

    class Factory(
        private val assessmentRepository: AssessmentRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RiskAssessmentViewModel(assessmentRepository) as T
        }
    }
}
