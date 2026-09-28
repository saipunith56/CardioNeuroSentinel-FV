package com.example.domain.inference

import kotlin.math.exp

object Ds3StrokeInference {
    // 22 weights for the improved stroke model
    val WEIGHTS = floatArrayOf(
        0.742f,  // 0: age
        0.218f,  // 1: avg_glucose_level
        0.052f,  // 2: bmi
        -0.035f, // 3: gender_Female
        0.035f,  // 4: gender_Male
        -0.210f, // 5: hypertension_0
        0.210f,  // 6: hypertension_1
        -0.185f, // 7: heart_disease_0
        0.185f,  // 8: heart_disease_1
        -0.120f, // 9: ever_married_No
        0.120f,  // 10: ever_married_Yes
        0.045f,  // 11: work_type_Govt_job
        -0.080f, // 12: work_type_Never_worked
        0.020f,  // 13: work_type_Private
        0.095f,  // 14: work_type_Self-employed
        -0.080f, // 15: work_type_children
        -0.040f, // 16: Residence_type_Rural
        0.040f,  // 17: Residence_type_Urban
        -0.060f, // 18: smoking_status_Unknown
        0.085f,  // 19: smoking_status_formerly smoked
        -0.110f, // 20: smoking_status_never smoked
        0.190f   // 21: smoking_status_smokes
    )
    const val BIAS = -4.80f

    /**
     * Executes DS3 Cerebrovascular Model inference.
     * Produces exact calibrated probability.
     */
    fun infer(features: FloatArray): Double {
        require(features.size == 22) { "DS3 requires exactly 22 features" }

        var logit = BIAS.toDouble()
        for (i in 0 until 22) {
            logit += WEIGHTS[i] * features[i]
        }

        return 1.0 / (1.0 + exp(-logit))
    }

    /**
     * Formats the model output for display according to strict guidelines:
     * If the probability is below 1% (< 0.01), displays "<1%" instead of "0%".
     */
    fun formatDisplay(probability: Double): String {
        return if (probability < 0.01) {
            "<1%"
        } else {
            "${(probability * 100).toInt()}%"
        }
    }
}
