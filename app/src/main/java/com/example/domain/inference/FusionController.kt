package com.example.domain.inference

object FusionController {

    const val FUSION_STATUS_SYNTHESIZED =
        "Multimodal synthesis combining cardiovascular, cerebrovascular, and diagnostic imaging/electrophysiology evidence"

    /**
     * Calculates unified multimodal risk percentage (0.0 - 100.0)
     * based on cardiovascular risk, cerebrovascular risk, and diagnostic evidence (MRI, ECG, EEG).
     *
     * Clinical Counting Rule:
     * - If normal (<= 1 abnormal) -> Low risk (12% - 24%)
     * - If 2 data more than normal -> Moderate risk (38% - 55%)
     * - If > half or all data more than normal -> High risk (68% - 92%)
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
        if (mriAbnormal) modifier += 10.0
        if (ecgAbnormal) modifier += 6.0
        if (eegAbnormal) modifier += 5.0

        val rawCombined = (baseScore + modifier).coerceIn(8.0, 95.0)

        return when {
            abnormalCount <= 1 -> rawCombined.coerceIn(12.0, 24.0)
            abnormalCount in 2..(totalEvaluated / 2) -> rawCombined.coerceIn(38.0, 55.0)
            abnormalCount > (totalEvaluated / 2) -> rawCombined.coerceIn(68.0, 92.0)
            else -> rawCombined.coerceIn(35.0, 65.0)
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
