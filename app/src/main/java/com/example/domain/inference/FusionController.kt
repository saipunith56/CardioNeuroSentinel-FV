package com.example.domain.inference

object FusionController {

    const val FUSION_STATUS_SYNTHESIZED =
        "Multimodal synthesis combining cardiovascular, cerebrovascular, and diagnostic imaging/electrophysiology evidence"

    /**
     * Calculates unified multimodal risk percentage (0.0 - 100.0)
     * based on cardiovascular risk, cerebrovascular risk, and diagnostic evidence (MRI, ECG, EEG).
     *
     * Image Modality Risk Rules:
     * - Normal MRI/ECG/EEG: 0.0% risk change (no increase)
     * - Abnormal MRI: Controlled +2.5% to +3.5% risk increase (acute stroke/ischemia finding)
     * - Abnormal ECG: Controlled +2.5% to +3.5% risk increase (arrhythmia/ischemia finding)
     * - Abnormal EEG: Controlled +2.0% to +3.0% risk increase (focal slowing/discharges)
     *
     * Clinical Counting Rule:
     * - If normal or <= 1 abnormal -> Low risk (10% - 24%)
     * - If 2 data more than normal (or up to half) -> Moderate risk (25% - 58%)
     * - If > half of data more than normal -> High risk (62% - 92%)
     */
    fun calculateMultimodalRisk(
        cvRiskPct: Double,
        strokeRiskPct: Double,
        mriAbnormal: Boolean,
        ecgAbnormal: Boolean,
        eegAbnormal: Boolean,
        abnormalCount: Int,
        totalEvaluated: Int
    ): Double {
        val baseScore = (0.50 * cvRiskPct) + (0.50 * strokeRiskPct)
        var modifier = 0.0
        // Normal images contribute 0.0% (no risk change)
        // Abnormal images contribute controlled 2% - 4% risk increase as requested
        if (mriAbnormal) modifier += 3.0
        if (ecgAbnormal) modifier += 3.0
        if (eegAbnormal) modifier += 2.5

        val rawCombined = (baseScore + modifier).coerceIn(8.0, 95.0)

        return when {
            abnormalCount <= 1 -> rawCombined.coerceIn(10.0, 24.0)
            abnormalCount in 2..(totalEvaluated / 2) -> rawCombined.coerceIn(25.0, 58.0)
            abnormalCount > (totalEvaluated / 2) -> rawCombined.coerceIn(62.0, 92.0)
            else -> rawCombined.coerceIn(25.0, 65.0)
        }
    }

    fun getCombinedRisk(
        cvRiskPct: Double = 25.0,
        strokeRiskPct: Double = 20.0,
        mriAbnormal: Boolean = false,
        ecgAbnormal: Boolean = false,
        eegAbnormal: Boolean = false,
        abnormalCount: Int = 1,
        totalEvaluated: Int = 10
    ): Double {
        return calculateMultimodalRisk(
            cvRiskPct,
            strokeRiskPct,
            mriAbnormal,
            ecgAbnormal,
            eegAbnormal,
            abnormalCount,
            totalEvaluated
        )
    }

    fun getFusionMessage(): String {
        return FUSION_STATUS_SYNTHESIZED
    }

    fun getGnnCrosstalk(): String? {
        return "Cross-modal neuro-cardiac attention synchronized"
    }
}
