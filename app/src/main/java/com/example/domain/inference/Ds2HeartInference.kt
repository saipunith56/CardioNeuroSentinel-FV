package com.example.domain.inference

import kotlin.math.exp

object Ds2HeartInference {
    // Model weights trained on Cleveland-13 dataset
    val WEIGHTS = floatArrayOf(
        -0.082f, // 0: age
        0.312f,  // 1: sex
        0.285f,  // 2: cp
        0.114f,  // 3: trestbps
        0.092f,  // 4: chol
        -0.054f, // 5: fbs
        0.088f,  // 6: restecg
        -0.245f, // 7: thalach
        0.320f,  // 8: exang
        0.264f,  // 9: oldpeak
        0.158f,  // 10: slope
        0.380f,  // 11: ca
        0.210f   // 12: thal
    )
    const val BIAS = -1.90f

    /**
     * Executes DS2 Cardiovascular Model inference.
     * Computes linear logit over standardized features, followed by sigmoid activation.
     * Returns raw probability strictly between 0.0 and 1.0.
     */
    fun infer(standardizedFeatures: FloatArray): Double {
        require(standardizedFeatures.size == 13) { "DS2 requires exactly 13 standardized features" }

        var logit = BIAS.toDouble()
        for (i in 0 until 13) {
            logit += WEIGHTS[i] * standardizedFeatures[i]
        }

        val prob = 1.0 / (1.0 + exp(-logit))
        return prob
    }
}
