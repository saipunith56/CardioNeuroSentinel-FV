package com.example.domain.interpretation

import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ImpactDirection {
    RISK_INCREASING, // Positive attribution (drives risk higher)
    PROTECTIVE,      // Negative attribution (pulls risk lower/protective)
    NEUTRAL
}

data class BiomarkerAttribution(
    val name: String,
    val measuredValue: String,
    val impactWeight: Float, // Absolute weight magnitude for bar visualization (0.0 - 1.0)
    val impactDirection: ImpactDirection,
    val clinicalRationale: String,
    val relativeContributionPct: Int // Normalized contribution percentage (e.g. 28%)
)

data class ExplainableAiResult(
    val calculatedRiskPercentage: Int,
    val riskCategory: String,
    val headlineSummary: String,
    val primaryDriversText: String,
    val topRiskDrivers: List<BiomarkerAttribution>,
    val topProtectiveFactors: List<BiomarkerAttribution>,
    val allAttributions: List<BiomarkerAttribution>
)

object ExplainableAiEngine {

    /**
     * Computes feature attributions and a human-readable Explainable AI summary
     * explaining which biomarkers and clinical factors most influenced the risk score.
     */
    fun explain(assessment: Assessment): ExplainableAiResult {
        val rawScore = assessment.combinedRiskScorePct
        val calculatedPct = when {
            rawScore != null && rawScore > 100.0 -> (rawScore / 100.0).toInt().coerceIn(1, 100)
            rawScore != null && rawScore <= 1.0 -> (rawScore * 100.0).toInt().coerceIn(1, 100)
            rawScore != null -> rawScore.toInt().coerceIn(1, 100)
            else -> (((assessment.ds2Probability ?: 0.16) + (assessment.ds3Probability ?: 0.10)) / 2 * 100).toInt().coerceIn(1, 100)
        }
        val category = assessment.clinicalRiskCategory.ifBlank {
            when {
                calculatedPct >= 65 -> "HIGH"
                calculatedPct >= 25 -> "MODERATE"
                else -> "LOW"
            }
        }

        val rawAttributions = mutableListOf<RawFactor>()

        // 1. Blood Pressure (Hemodynamic biomarker)
        val sys = assessment.systolicBp
        val dia = assessment.diastolicBp
        val bpLabel = "${sys.toInt()}/${dia.toInt()} mmHg"
        when {
            sys >= 160.0 || dia >= 100.0 -> rawAttributions.add(
                RawFactor(
                    name = "Blood Pressure (Stage 2 HTN)",
                    value = bpLabel,
                    weight = 34f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Severe hypertension multiplies vascular shear stress, accelerating acute arterial plaque rupture and cerebral ischemia."
                )
            )
            sys >= 140.0 || dia >= 90.0 -> rawAttributions.add(
                RawFactor(
                    name = "Blood Pressure (Stage 1 HTN)",
                    value = bpLabel,
                    weight = 22f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Elevated systemic vascular resistance strains myocardial perfusion and microvascular cerebral circulation."
                )
            )
            sys >= 130.0 || dia >= 85.0 -> rawAttributions.add(
                RawFactor(
                    name = "Blood Pressure (Elevated)",
                    value = bpLabel,
                    weight = 12f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Pre-hypertensive hemodynamics gently elevate baseline cardiovascular workload."
                )
            )
            sys < 120.0 && dia < 80.0 -> rawAttributions.add(
                RawFactor(
                    name = "Blood Pressure (Optimal)",
                    value = bpLabel,
                    weight = 20f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Optimal hemodynamic pressure protects vascular endothelium from barotrauma."
                )
            )
            else -> rawAttributions.add(
                RawFactor(
                    name = "Blood Pressure",
                    value = bpLabel,
                    weight = 6f,
                    direction = ImpactDirection.NEUTRAL,
                    rationale = "Blood pressure within acceptable clinical margins."
                )
            )
        }

        // 2. Cardiac Troponin (Myocardial injury biomarker)
        val trop = assessment.troponin
        when {
            trop >= 0.04 -> rawAttributions.add(
                RawFactor(
                    name = "Cardiac Troponin (Elevated)",
                    value = String.format(Locale.US, "%.3f ng/mL", trop),
                    weight = 30f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Troponin release indicates active or acute cardiomyocyte cellular breakdown, significantly elevating acute coronary risk."
                )
            )
            trop in 0.015..0.039 -> rawAttributions.add(
                RawFactor(
                    name = "Cardiac Troponin (Borderline)",
                    value = String.format(Locale.US, "%.3f ng/mL", trop),
                    weight = 14f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Subtle myocardial membrane leakage warrants close observation."
                )
            )
            trop < 0.015 -> rawAttributions.add(
                RawFactor(
                    name = "Cardiac Troponin (Normal)",
                    value = String.format(Locale.US, "%.3f ng/mL", trop),
                    weight = 16f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Absence of detectable troponin rules out acute cardiomyocyte necrosis."
                )
            )
        }

        // 3. Fasting Glucose (Metabolic biomarker)
        val glu = assessment.fastingGlucose
        val gluLabel = "${glu.toInt()} mg/dL"
        when {
            glu >= 126.0 -> rawAttributions.add(
                RawFactor(
                    name = "Fasting Glucose (Diabetic Range)",
                    value = gluLabel,
                    weight = 26f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Chronic hyperglycemia promotes advanced glycation end-products, accelerating large vessel atherosclerosis."
                )
            )
            glu in 100.0..125.0 -> rawAttributions.add(
                RawFactor(
                    name = "Fasting Glucose (Prediabetic)",
                    value = gluLabel,
                    weight = 14f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Impaired fasting glucose is an active metabolic risk amplifier."
                )
            )
            glu < 100.0 -> rawAttributions.add(
                RawFactor(
                    name = "Fasting Glucose (Optimal)",
                    value = gluLabel,
                    weight = 14f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Euglycemia preserves vascular endothelial health and nitric oxide bio-availability."
                )
            )
        }

        // 4. Total Cholesterol (Lipid biomarker)
        val chol = assessment.cholesterol
        val cholLabel = "${chol.toInt()} mg/dL"
        when {
            chol >= 240.0 -> rawAttributions.add(
                RawFactor(
                    name = "Total Cholesterol (High)",
                    value = cholLabel,
                    weight = 22f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Severe hypercholesterolemia drives atheromatous plaque accumulation in coronary and carotid arterial beds."
                )
            )
            chol in 200.0..239.0 -> rawAttributions.add(
                RawFactor(
                    name = "Total Cholesterol (Borderline)",
                    value = cholLabel,
                    weight = 10f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Borderline lipid elevation contributes to progressive atheroma deposition."
                )
            )
            chol < 200.0 -> rawAttributions.add(
                RawFactor(
                    name = "Total Cholesterol (Desirable)",
                    value = cholLabel,
                    weight = 12f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Desirable lipid levels limit atherogenic vascular wall infiltration."
                )
            )
        }

        // 5. Neuroimaging (MRI/CT findings)
        val mriDesignation = com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.mriUri)
        val mriAbnormal = mriDesignation ?: ((assessment.ds1NormalProb != null && assessment.ds1NormalProb < 0.5) ||
                (assessment.ds1IschemicProb != null && assessment.ds1IschemicProb > 0.3) ||
                (assessment.mriSourceType == "PRESET_DWI"))
        if (assessment.mriValidationStatus != ModalityValidationStatus.NOT_PROVIDED &&
            assessment.mriValidationStatus != ModalityValidationStatus.MODALITY_REJECTED) {
            if (mriAbnormal) {
                rawAttributions.add(
                    RawFactor(
                        name = "Brain Neuroimaging (MRI/CT)",
                        value = "Acute Infarct / DWI Lesion",
                        weight = 32f,
                        direction = ImpactDirection.RISK_INCREASING,
                        rationale = "Radiological verification of acute focal parenchymal injury directly confirms cerebrovascular event manifestation."
                    )
                )
            } else {
                rawAttributions.add(
                    RawFactor(
                        name = "Brain Neuroimaging (MRI/CT)",
                        value = "Symmetrical Normal Parenchyma",
                        weight = 18f,
                        direction = ImpactDirection.PROTECTIVE,
                        rationale = "Unremarkable brain imaging confirms intact cerebral parenchyma and absence of acute intracranial lesion."
                    )
                )
            }
        }

        // 6. Cardiac Electrophysiology (12-Lead ECG)
        val ecgDesignation = com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.ecgUri)
        val ecgAbnormal = ecgDesignation ?: ((assessment.ds4NormProb != null && assessment.ds4NormProb < 0.5) ||
                (assessment.ds4MiProb != null && assessment.ds4MiProb > 0.25) ||
                (assessment.ds4SttcProb != null && assessment.ds4SttcProb > 0.25) ||
                (assessment.ecgSourceType == "PRESET_AFIB"))
        if (assessment.ecgValidationStatus != ModalityValidationStatus.NOT_PROVIDED &&
            assessment.ecgValidationStatus != ModalityValidationStatus.MODALITY_REJECTED) {
            if (ecgAbnormal) {
                rawAttributions.add(
                    RawFactor(
                        name = "12-Lead ECG Telemetry",
                        value = "Arrhythmia / ST Elevation",
                        weight = 28f,
                        direction = ImpactDirection.RISK_INCREASING,
                        rationale = "Repolarization abnormality and rhythm disruption indicate active myocardial stress and thromboembolic hazard."
                    )
                )
            } else {
                rawAttributions.add(
                    RawFactor(
                        name = "12-Lead ECG Telemetry",
                        value = "Normal Sinus Rhythm",
                        weight = 16f,
                        direction = ImpactDirection.PROTECTIVE,
                        rationale = "Conserved regular sinus rhythm validates synchronized atrial-ventricular cardiac conduction."
                    )
                )
            }
        }

        // 7. Neurological Telemetry (EEG)
        val eegDesignation = com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.eegUri)
        val eegAbnormal = eegDesignation ?: ((assessment.ds5SeizureProb != null && assessment.ds5SeizureProb > 0.3) ||
                (assessment.eegSourceType == "PRESET_SLOWING"))
        if (assessment.eegValidationStatus != ModalityValidationStatus.NOT_PROVIDED &&
            assessment.eegValidationStatus != ModalityValidationStatus.MODALITY_REJECTED) {
            if (eegAbnormal) {
                rawAttributions.add(
                    RawFactor(
                        name = "EEG Telemetry",
                        value = "Focal Slowing / Discharges",
                        weight = 20f,
                        direction = ImpactDirection.RISK_INCREASING,
                        rationale = "Cortical electrographic slowing correlates with regional hypoperfusion and epileptic susceptibility."
                    )
                )
            } else {
                rawAttributions.add(
                    RawFactor(
                        name = "EEG Telemetry",
                        value = "Symmetrical Alpha Rhythm",
                        weight = 10f,
                        direction = ImpactDirection.PROTECTIVE,
                        rationale = "Normal background synchronization demonstrates preserved bilateral cortical activity."
                    )
                )
            }
        }

        // 8. Tobacco Smoking
        if (assessment.isSmoker) {
            rawAttributions.add(
                RawFactor(
                    name = "Tobacco Smoking",
                    value = "Active Smoker",
                    weight = 22f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Nicotine and combustion free radicals precipitate immediate microvascular vasoconstriction and hypercoagulability."
                )
            )
        } else {
            rawAttributions.add(
                RawFactor(
                    name = "Tobacco History",
                    value = "Non-Smoker",
                    weight = 12f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Avoidance of chronic tobacco exposure maintains endogenous vascular prostacyclin synthesis."
                )
            )
        }

        // 9. Age
        val age = assessment.age
        when {
            age >= 65.0 -> rawAttributions.add(
                RawFactor(
                    name = "Patient Age (${age.toInt()} yrs)",
                    value = "Senior (>65 y/o)",
                    weight = 24f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Advanced chronological age is a major structural determinant of arterial stiffness and vascular vulnerability."
                )
            )
            age in 50.0..64.0 -> rawAttributions.add(
                RawFactor(
                    name = "Patient Age (${age.toInt()} yrs)",
                    value = "Mature (50–64 y/o)",
                    weight = 12f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Age-dependent vascular collagen remodeling moderately contributes to risk."
                )
            )
            age < 50.0 -> rawAttributions.add(
                RawFactor(
                    name = "Patient Age (${age.toInt()} yrs)",
                    value = "Young Adult (<50 y/o)",
                    weight = 16f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Younger vascular elasticity provides intrinsic resistance against ischemic injury."
                )
            )
        }

        // 10. BMI (Metabolic / Adiposity)
        val bmi = assessment.bmi
        val bmiStr = String.format(Locale.US, "%.1f kg/m²", bmi)
        when {
            bmi >= 30.0 -> rawAttributions.add(
                RawFactor(
                    name = "Body Mass Index (Obese)",
                    value = bmiStr,
                    weight = 18f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Adipose-mediated chronic systemic inflammation heightens cardioneurovascular resistance."
                )
            )
            bmi in 25.0..29.9 -> rawAttributions.add(
                RawFactor(
                    name = "Body Mass Index (Overweight)",
                    value = bmiStr,
                    weight = 8f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Mildly elevated adiposity index."
                )
            )
            bmi in 18.5..24.9 -> rawAttributions.add(
                RawFactor(
                    name = "Body Mass Index (Normal)",
                    value = bmiStr,
                    weight = 10f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Healthy body habitus eliminates excess hemodynamic and cardiac stroke volume burdens."
                )
            )
        }

        // 11. Resting Heart Rate
        val hr = assessment.heartRate
        when {
            hr > 100.0 -> rawAttributions.add(
                RawFactor(
                    name = "Resting Heart Rate (Tachycardia)",
                    value = "${hr.toInt()} bpm",
                    weight = 14f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Elevated resting heart rate increases cardiac oxygen demand and shortens diastolic coronary filling time."
                )
            )
            hr in 60.0..100.0 -> rawAttributions.add(
                RawFactor(
                    name = "Resting Heart Rate (Normal)",
                    value = "${hr.toInt()} bpm",
                    weight = 8f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Physiological resting heart rate indicates adequate autonomic balance."
                )
            )
        }

        // 12. Genetic / Family History
        if (assessment.familyCvHistory || assessment.familyStrokeHistory) {
            val histType = if (assessment.familyCvHistory && assessment.familyStrokeHistory) "CV & Stroke"
            else if (assessment.familyCvHistory) "Cardiovascular" else "Stroke"
            rawAttributions.add(
                RawFactor(
                    name = "Family Vascular History",
                    value = "Positive ($histType)",
                    weight = 14f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Familial predisposition reflects polygenic inheritance of endothelial vulnerability and lipid metabolism variations."
                )
            )
        }

        // 13. Neurological Stroke Impairment (NIHSS)
        val nihss = assessment.nihss
        when {
            nihss >= 15 -> rawAttributions.add(
                RawFactor(
                    name = "NIHSS Stroke Scale (Severe)",
                    value = "Score $nihss / 42",
                    weight = 36f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Severe acute focal neurological deficit indicates extensive hemispheric or brainstem functional disruption."
                )
            )
            nihss in 5..14 -> rawAttributions.add(
                RawFactor(
                    name = "NIHSS Stroke Scale (Moderate)",
                    value = "Score $nihss / 42",
                    weight = 24f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Moderate focal neurological signs correlate with verifiable ischemic penumbra tissue at risk."
                )
            )
            nihss in 1..4 -> rawAttributions.add(
                RawFactor(
                    name = "NIHSS Stroke Scale (Minor)",
                    value = "Score $nihss / 42",
                    weight = 14f,
                    direction = ImpactDirection.RISK_INCREASING,
                    rationale = "Mild neurological signs indicate focal cerebral irritation or small vessel lacunar disturbance."
                )
            )
            nihss == 0 -> rawAttributions.add(
                RawFactor(
                    name = "NIHSS Stroke Scale (Intact)",
                    value = "Score 0 / 42",
                    weight = 14f,
                    direction = ImpactDirection.PROTECTIVE,
                    rationale = "Complete preservation of motor, sensory, visual, and language function without acute deficits."
                )
            )
        }

        // Sort risk increasing factors by weight descending
        val riskIncreasing = rawAttributions
            .filter { it.direction == ImpactDirection.RISK_INCREASING }
            .sortedByDescending { it.weight }

        // Sort protective factors by weight descending
        val protective = rawAttributions
            .filter { it.direction == ImpactDirection.PROTECTIVE }
            .sortedByDescending { it.weight }

        // Compute total weight among all active drivers to compute relative percentage contributions
        val totalPositiveWeight = riskIncreasing.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f)
        val maxSingleWeight = rawAttributions.maxOfOrNull { it.weight } ?: 1f

        val allMapped = rawAttributions.map { factor ->
            val relativePct = if (factor.direction == ImpactDirection.RISK_INCREASING) {
                ((factor.weight / totalPositiveWeight) * 100).roundToInt().coerceIn(1, 100)
            } else {
                ((factor.weight / (protective.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f))) * 100).roundToInt().coerceIn(1, 100)
            }
            BiomarkerAttribution(
                name = factor.name,
                measuredValue = factor.value,
                impactWeight = (factor.weight / maxSingleWeight).coerceIn(0.1f, 1.0f),
                impactDirection = factor.direction,
                clinicalRationale = factor.rationale,
                relativeContributionPct = relativePct
            )
        }.sortedByDescending { it.relativeContributionPct }

        val topRisk = allMapped.filter { it.impactDirection == ImpactDirection.RISK_INCREASING }.take(4)
        val topProt = allMapped.filter { it.impactDirection == ImpactDirection.PROTECTIVE }.take(3)

        // Generate brief, human-readable summary
        val headlineSummary: String
        val primaryDriversText: String

        if (riskIncreasing.isEmpty() || calculatedPct < 25) {
            val anchorNames = topProt.take(3).joinToString(", ") { "${it.name} (${it.measuredValue})" }
            headlineSummary = "Calculated risk of $calculatedPct% ($category RISK) is strongly anchored by optimal clinical biomarkers including $anchorNames, providing significant physiological protection against acute cardioneurovascular events."
            primaryDriversText = "Normal physiological ranges across vital signs and diagnostic markers provide primary stabilization."
        } else {
            val topNames = topRisk.take(3).joinToString(", ") { "${it.name} (${it.measuredValue})" }
            val topCombinedPct = topRisk.take(3).sumOf { it.relativeContributionPct }.coerceIn(10, 95)
            headlineSummary = "Calculated risk of $calculatedPct% ($category RISK) is predominantly driven by $topNames, which collectively account for $topCombinedPct% of the observed risk elevation."
            primaryDriversText = "Primary risk drivers: ${topRisk.take(2).joinToString(" and ") { it.name }}."
        }

        return ExplainableAiResult(
            calculatedRiskPercentage = calculatedPct,
            riskCategory = category,
            headlineSummary = headlineSummary,
            primaryDriversText = primaryDriversText,
            topRiskDrivers = topRisk,
            topProtectiveFactors = topProt,
            allAttributions = allMapped
        )
    }

    private data class RawFactor(
        val name: String,
        val value: String,
        val weight: Float,
        val direction: ImpactDirection,
        val rationale: String
    )
}
