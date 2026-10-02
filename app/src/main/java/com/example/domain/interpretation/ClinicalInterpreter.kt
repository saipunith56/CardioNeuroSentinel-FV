package com.example.domain.interpretation

import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import com.example.data.model.ModelExecutionStatus

data class ClinicalInterpretation(
    val summaryLines: List<String>,
    val clinicalPrecautions: List<String>,
    val lifestyleMeasures: List<String>,
    val medicationContraindication: String,
    val whyThisResultRationale: String,
    val emergencyFastWarning: String
)

object ClinicalInterpreter {

    fun interpret(assessment: Assessment): ClinicalInterpretation {
        val summary = mutableListOf<String>()

        val cvProbStr = if (assessment.ds2Probability != null) {
            val impNote = if (assessment.ds2ImputationBacked) " (Imputation-backed)" else ""
            val pct = (assessment.ds2Probability * 100).toInt()
            summary.add("• DS2 (Cardiovascular Risk): Structured clinical input → DS2 executed → Model Output: $pct%$impNote")
            "$pct%"
        } else {
            summary.add("• DS2 (Cardiovascular Risk): NOT AVAILABLE — Insufficient clinical vitals")
            "NOT AVAILABLE"
        }

        val strokeProbStr = if (assessment.ds3Probability != null) {
            val strokeFormatted = if (assessment.ds3Probability < 0.01) "<1%" else "${(assessment.ds3Probability * 100).toInt()}%"
            summary.add("• DS3 (Stroke Risk): Structured clinical input → DS3 executed → Model Output: $strokeFormatted")
            strokeFormatted
        } else {
            summary.add("• DS3 (Stroke Risk): NOT AVAILABLE — Insufficient demographic/clinical input")
            "NOT AVAILABLE"
        }

        if (assessment.ds1Status == ModelExecutionStatus.EXECUTED) {
            val mriFinding = if (assessment.ds1IschemicProb != null && assessment.ds1IschemicProb > 0.5) "Abnormal (acute ischemic lesion / infarction identified)" else "Normal (symmetrical cerebral parenchyma)"
            summary.add("• MRI Brain: Evaluated — $mriFinding")
        } else if (assessment.mriValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) {
            summary.add("• MRI: Uploaded file rejected — Invalid or non-neuroimaging image")
        } else {
            summary.add("• MRI: Not provided")
        }

        if (assessment.ecgValidationStatus == ModalityValidationStatus.REAL_ENTERED) {
            val note = if (assessment.ds4MiProb != null && assessment.ds4MiProb > 0.3) "Abnormal findings / arrhythmia pattern" else "Normal rhythm"
            summary.add("• ECG Report: Evaluated ($note)")
        } else if (assessment.ecgValidationStatus == ModalityValidationStatus.RAW_SIGNAL_VALIDATED) {
            summary.add("• ECG: Raw 12-lead waveform evaluated")
        } else if (assessment.ecgValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) {
            summary.add("• ECG: Uploaded file rejected — Invalid ECG image")
        } else if (assessment.ecgValidationStatus == ModalityValidationStatus.UNSUPPORTED) {
            summary.add("• ECG: Uploaded — ECG image unsupported (requires raw 12-lead signal)")
        }

        if (assessment.eegValidationStatus == ModalityValidationStatus.REAL_ENTERED) {
            val note = if (assessment.ds5SeizureProb != null && assessment.ds5SeizureProb > 0.4) "Abnormal telemetry / focal slowing" else "Normal background activity"
            summary.add("• EEG Report: Evaluated ($note)")
        } else if (assessment.eegValidationStatus == ModalityValidationStatus.RAW_SIGNAL_VALIDATED) {
            summary.add("• EEG: Raw 23-channel telemetry evaluated")
        } else if (assessment.eegValidationStatus == ModalityValidationStatus.MODALITY_REJECTED) {
            summary.add("• EEG: Uploaded file rejected — Invalid EEG image")
        } else if (assessment.eegValidationStatus == ModalityValidationStatus.UNSUPPORTED) {
            summary.add("• EEG: Uploaded — EEG image unsupported (requires raw 23-channel EEG)")
        }

        val rawScore = assessment.combinedRiskScorePct
        val combinedVal = when {
            rawScore != null && rawScore > 100.0 -> (rawScore / 100.0).toInt().coerceIn(1, 100)
            rawScore != null && rawScore <= 1.0 -> (rawScore * 100.0).toInt().coerceIn(1, 100)
            rawScore != null -> rawScore.toInt().coerceIn(1, 100)
            else -> null
        }
        if (combinedVal != null) {
            summary.add("• Multimodal Joint Synthesis: $combinedVal% (${assessment.clinicalRiskCategory} RISK) — Comprehensive synthesis integrating cardiovascular, cerebrovascular, and diagnostic evidence.")
        } else {
            summary.add("Multimodal risk calculation unavailable — insufficient combined model data. Risk scores are presented as independent per-modality model results.")
        }

        val precautions = mutableListOf<String>()
        val lifestyle = mutableListOf<String>()

        val hasElevatedBp = assessment.systolicBp >= 120.0 || assessment.diastolicBp >= 80.0
        val hasHighBp = assessment.systolicBp >= 130.0 || assessment.diastolicBp >= 85.0
        val hasStage2Bp = assessment.systolicBp >= 140.0 || assessment.diastolicBp >= 90.0

        val hasHighGlucose = assessment.fastingGlucose >= 100.0
        val hasDiabeticGlucose = assessment.fastingGlucose >= 126.0

        val hasHighCholesterol = assessment.cholesterol >= 200.0
        val isSmoker = assessment.isSmoker
        val hasHighBmi = assessment.bmi >= 25.0
        val hasLowBmi = assessment.bmi < 18.5
        val hasTachycardia = assessment.heartRate > 100.0
        val hasBradycardia = assessment.heartRate < 50.0
        val hasHighTroponin = assessment.troponin > 0.04
        val hasNihss = assessment.nihss > 0
        val hasCvHistory = assessment.familyCvHistory
        val hasStrokeHistory = assessment.familyStrokeHistory

        val complaint = assessment.chiefComplaint.lowercase().trim()

        val mentionsDizziness = complaint.contains("dizz") || complaint.contains("vertigo") || complaint.contains("lighthead")
        val mentionsSmoking = isSmoker || complaint.contains("smok") || complaint.contains("tobacco") || complaint.contains("nicotine") || complaint.contains("cig") || complaint.contains("vape")
        val mentionsPalpitations = complaint.contains("palpitat") || complaint.contains("flutter") || complaint.contains("racing")
        val mentionsChestPain = complaint.contains("chest") || complaint.contains("angina") || complaint.contains("pressure") || complaint.contains("tightness")
        val mentionsShortnessOfBreath = complaint.contains("breath") || complaint.contains("dyspnea") || complaint.contains("short of breath")
        val mentionsHeadache = complaint.contains("headache") || complaint.contains("migraine")
        val mentionsNumbness = complaint.contains("numb") || complaint.contains("weak") || complaint.contains("facial") || complaint.contains("droop") || complaint.contains("slur") || complaint.contains("speech")
        val mentionsFatigue = complaint.contains("fatigue") || complaint.contains("tired") || complaint.contains("exhaust")
        val mentionsStress = complaint.contains("stress") || complaint.contains("anxiet") || complaint.contains("panic")
        val mentionsAlcohol = complaint.contains("alcohol") || complaint.contains("beer") || complaint.contains("wine") || complaint.contains("drink")
        val mentionsSleep = complaint.contains("sleep") || complaint.contains("insomnia")
        val mentionsEdema = complaint.contains("swelling") || complaint.contains("edema")
        val mentionsNausea = complaint.contains("nausea") || complaint.contains("vomit")
        val mentionsVision = complaint.contains("vision") || complaint.contains("blur") || complaint.contains("blind")

        val hasAnySymptom = mentionsDizziness || mentionsPalpitations || mentionsChestPain ||
                mentionsShortnessOfBreath || mentionsHeadache || mentionsNumbness ||
                mentionsFatigue || mentionsStress || mentionsAlcohol || mentionsSleep ||
                mentionsEdema || mentionsNausea || mentionsVision ||
                (complaint.isNotEmpty() && !complaint.contains("routine") && !complaint.contains("annual") && !complaint.contains("normal") && !complaint.contains("none") && !complaint.contains("asymptomatic"))

        val isAllNormal = !hasElevatedBp && !hasHighGlucose && !hasHighCholesterol &&
                !isSmoker && !hasHighBmi && !hasLowBmi && !hasTachycardia && !hasBradycardia &&
                !hasHighTroponin && !hasNihss && !hasCvHistory && !hasStrokeHistory &&
                !hasAnySymptom &&
                (assessment.ds2Probability == null || assessment.ds2Probability < 0.10) &&
                (assessment.ds3Probability == null || assessment.ds3Probability < 0.05)

        if (isAllNormal) {
            // If all values entered in clinical data with symptoms are normal -> Suggest ONLY Mediterranean Diet
            precautions.add("Maintain routine annual preventative cardiovascular, metabolic, and neurological health checkups.")
            precautions.add("Sustain regular moderate physical activity (minimum 150 minutes of aerobic exercise weekly).")
            precautions.add("Continue routine annual blood pressure, glycemic, and lipid profile tracking.")

            lifestyle.add("Vascular & Brain-Protective Mediterranean Diet: Prioritize extra-virgin olive oil as primary dietary fat, abundant seasonal colorful vegetables, fresh fruits, whole grains, legumes, and unsalted nuts.")
            lifestyle.add("Incorporate omega-3 fatty acid rich seafood (wild salmon, mackerel, sardines) at least 2 times per week.")
            lifestyle.add("Avoid ultra-processed convenience foods, refined carbohydrates, and sugary beverages to preserve vascular integrity.")
        } else {
            // VARIES: Precautions and diet tailored to clinical vitals and reported patient symptoms

            // High Blood Pressure -> Reduce salt
            if (hasElevatedBp || hasHighBp || hasStage2Bp) {
                precautions.add("Prompt blood pressure monitoring (maintain home log twice daily) and evaluation for antihypertensive therapy/titration.")
                lifestyle.add("Elevated Blood Pressure — Reduce Salt: Strictly restrict dietary sodium to under 1,500–2,000 mg/day (less than 1 level teaspoon of salt daily). Avoid processed meats, salted snacks, canned soups, soy sauce, and added table salt. Adopt DASH diet eating principles rich in potassium from leafy vegetables and whole fruits.")
            }

            // High Blood Glucose -> Reduce sugar
            if (hasHighGlucose) {
                val sugarNote = if (hasDiabeticGlucose) "Diabetic-range" else "Elevated"
                precautions.add("Fasting glycemic follow-up ($sugarNote glucose: ${assessment.fastingGlucose.toInt()} mg/dL), HbA1c testing, and endocrinology/primary care evaluation.")
                lifestyle.add("High Blood Glucose — Reduce Sugar: Strictly eliminate sugar-sweetened beverages, fruit juices, candy, desserts, and refined carbohydrates. Choose low-glycemic index foods, high-fiber legumes, and whole vegetables to prevent postprandial glucose spikes.")
            }

            // Dizziness / Vertigo -> More sleep & hydration
            if (mentionsDizziness) {
                precautions.add("Dizziness Fall Precaution: Rise slowly from supine or seated positions; avoid abrupt neck rotation; refrain from operating heavy machinery during dizzy spells.")
                lifestyle.add("Dizziness Management — Prioritize Sleep & Hydration: Ensure 8–9 hours of regular, restorative sleep nightly, as fatigue aggravates vestibular instability. Drink 2.0–2.5 L of water daily with balanced electrolytes to maintain cerebral perfusion.")
            }

            // Smoking -> Advice on ways to reduce addiction / cessation
            if (mentionsSmoking) {
                precautions.add("Smoking Vascular Risk: Active smoking accelerates arterial plaque instability, endothelial injury, and doubles stroke risk.")
                lifestyle.add("Ways to Reduce Nicotine Addiction & Cessation Guidance: (1) Practice the '4-D' delay method (Delay 5–10 min when craving strikes, Deep breathe, Drink water, Distract with activity). (2) Consult physician regarding Nicotine Replacement Therapy (NRT patches/gum/lozenges) or prescribed varenicline/bupropion. (3) Identify personal smoking triggers (after meals, stress, driving) and substitute with healthy oral habits. (4) Join a structured tobacco quitline counseling program.")
            }

            // High Cholesterol -> Reduce saturated and trans fats
            if (hasHighCholesterol) {
                precautions.add("Comprehensive lipid panel follow-up (LDL-C, HDL, triglycerides) and atherosclerotic cardiovascular risk assessment.")
                lifestyle.add("High Cholesterol — Lipid Management: Minimize saturated fats (< 6% of daily calories), eliminate trans fats, and consume 10–25g daily of soluble fiber (oats, barley, psyllium, lentils) to lower circulating LDL.")
            }

            // Weight / BMI
            if (hasHighBmi) {
                lifestyle.add("Weight Optimization: Target a gradual caloric deficit of 500 kcal/day for a 5–10% weight reduction (target BMI < 25 kg/m²) to decrease cardiovascular workload.")
            } else if (hasLowBmi) {
                lifestyle.add("Nutritional Fortification: Incorporate nutrient-dense calories and healthy fats (avocados, nut butters, whole grains) to prevent frailty.")
            }

            // Palpitations / Tachycardia
            if (mentionsPalpitations || hasTachycardia) {
                precautions.add("Cardiac Arrhythmia Surveillance: Prompt 12-lead ECG and Holter ambulatory telemetry evaluation for rhythm disturbances.")
                lifestyle.add("Palpitations Management: Completely eliminate caffeine, energy drinks, pre-workout stimulants, and alcohol. Practice calming vagal breathing techniques.")
            }

            // Shortness of breath
            if (mentionsShortnessOfBreath) {
                precautions.add("Cardiopulmonary Precaution: Avoid strenuous exertion until cleared by cardiology; seek immediate emergency care if breathlessness occurs at rest or when lying flat.")
            }

            // Chest pain
            if (mentionsChestPain) {
                precautions.add("EMERGENCY CARDIAC ALERT: Any chest discomfort, squeezing, or pain radiating to the jaw, neck, back, or arm requires immediate emergency medical services (Call 911 / EMS).")
            }

            // Headache / Migraine
            if (mentionsHeadache) {
                lifestyle.add("Headache Prevention: Maintain rigid sleep schedules (7–8 hours), consistent meal times to prevent hypoglycemia, robust hydration, and avoid dietary triggers (MSG, aged cheeses, excess caffeine).")
            }

            // Numbness / Weakness / Facial Asymmetry / Speech Alteration
            if (mentionsNumbness) {
                precautions.add("URGENT BE-FAST STROKE ALERT: Facial droop, arm weakness, or speech changes require immediate emergency medical transport. Time is brain.")
            }

            // Fatigue / Sleep Issues
            if (mentionsFatigue || mentionsSleep) {
                lifestyle.add("Sleep Optimization: Establish strict 8-hour sleep hygiene with dark/cool room environment; eliminate evening screen exposure; evaluate for obstructive sleep apnea.")
            }

            // Stress / Anxiety
            if (mentionsStress) {
                lifestyle.add("Stress Reduction: Practice daily diaphragmatic box breathing (4-4-4-4 count), progressive muscle relaxation, and 30 minutes of low-intensity outdoor walking.")
            }

            // Alcohol
            if (mentionsAlcohol) {
                lifestyle.add("Alcohol Harm Reduction: Strictly limit or eliminate alcohol consumption (< 1 standard drink/day or complete abstinence) to prevent hypertensive surges and arrhythmias.")
            }

            // Other symptoms (Edema, Nausea, Vision)
            if (mentionsEdema) {
                precautions.add("Peripheral Edema Precaution: Elevate lower extremities when sitting; strictly minimize dietary sodium to prevent fluid retention.")
            }
            if (mentionsNausea) {
                lifestyle.add("Nausea Care: Consume small, frequent, bland meals (BRAT diet principles); sip oral electrolyte solutions.")
            }
            if (mentionsVision) {
                precautions.add("Visual Alert: Urgent ophthalmologic and neurovascular evaluation to rule out transient ischemic attacks (amaurosis fugax).")
            }

            // Core nutritional foundation
            lifestyle.add("Core Nutritional Foundation: Follow the Mediterranean dietary pattern (extra-virgin olive oil, abundant colorful vegetables, legumes, whole grains, and fish twice weekly) adapted to the specific salt/sugar restrictions above.")

            if (assessment.ds2Probability != null && assessment.ds2Probability >= 0.20) {
                precautions.add("Prompt professional cardiological evaluation recommended.")
            }
            precautions.add("Follow stroke-directed secondary and/or primary prevention guidelines.")
        }

        val contraindication = "No medication is prescribed by CardioNeuro-Sentinel. Treatment decisions must be made by a qualified healthcare professional reviewing the patient's clinical history, imaging, laboratory results, and current medications.\n\nMedication decisions must be made by a qualified physician."

        val rationale = buildString {
            append("Primary clinical assessment risk category: ${assessment.clinicalRiskCategory}. ")
            append("Key contributing clinical risk factors from documented data: ")
            val factors = mutableListOf<String>()
            if (assessment.isSmoker) factors.add("Active smoker")
            if (assessment.systolicBp >= 140.0 || assessment.diastolicBp >= 90.0) factors.add("Elevated BP (${assessment.systolicBp.toInt()}/${assessment.diastolicBp.toInt()} mmHg)")
            if (assessment.fastingGlucose >= 100.0) factors.add("Elevated Glucose (${assessment.fastingGlucose.toInt()} mg/dL)")
            if (assessment.cholesterol >= 200.0) factors.add("Elevated Cholesterol (${assessment.cholesterol.toInt()} mg/dL)")
            if (assessment.bmi >= 25.0) factors.add("Elevated BMI (${assessment.bmi})")
            if (mentionsDizziness) factors.add("Reported Dizziness")
            if (assessment.familyCvHistory) factors.add("Family CV history")
            if (assessment.familyStrokeHistory) factors.add("Family stroke history")
            if (factors.isEmpty()) factors.add("Baseline normal clinical parameters")
            append(factors.joinToString(", "))
            append(". Per-modality risk scores: • DS2 cardiovascular risk: $cvProbStr • DS3 cerebrovascular risk: $strokeProbStr. ")
            append("Multimodal fusion: UNAVAILABLE (no validated joint model). Risk ranking: ${assessment.clinicalRiskCategory}.")
        }

        val fastWarning = "BE-FAST Protocol Warning: Seek emergency medical care immediately for sudden face drooping, arm/leg weakness (especially single-sided), speech difficulty, vision loss, or severe sudden headache."

        return ClinicalInterpretation(
            summaryLines = summary,
            clinicalPrecautions = precautions,
            lifestyleMeasures = lifestyle,
            medicationContraindication = contraindication,
            whyThisResultRationale = rationale,
            emergencyFastWarning = fastWarning
        )
    }
}
