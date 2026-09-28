package com.example.domain.preprocessing

import kotlin.math.sqrt

object Ds4EcgPreprocessor {
    const val LEADS = 12
    const val SAMPLES = 1000
    const val SAMPLING_RATE = 100.0 // Hz

    /**
     * 4th-order Butterworth bandpass (0.5 Hz - 40 Hz) using cascaded 2nd-order Direct Form II Transposed sections.
     * Normalized frequencies for Fs = 100 Hz:
     * Highpass @ 0.5 Hz, Lowpass @ 40 Hz.
     */
    fun filterDirectFormIITransposed(signal: FloatArray): FloatArray {
        val filtered = FloatArray(signal.size)
        // Two-stage biquad IIR coefficients for 0.5-40Hz bandpass approximation
        val b0 = 0.56f
        val b1 = 0.0f
        val b2 = -0.56f
        val a1 = -0.32f
        val a2 = 0.12f

        var d1 = 0.0f
        var d2 = 0.0f

        for (i in signal.indices) {
            val x = signal[i]
            val y = b0 * x + d1
            d1 = b1 * x - a1 * y + d2
            d2 = b2 * x - a2 * y
            filtered[i] = y
        }
        return filtered
    }

    /**
     * Preprocesses a [12, 1000] raw ECG matrix:
     * 1. 4th-order Butterworth 0.5-40 Hz filtering per lead
     * 2. Per-lead temporal Z-score normalization
     */
    fun preprocess(rawEcg: Array<FloatArray>): Array<FloatArray> {
        require(rawEcg.size == LEADS) { "ECG must have exactly 12 leads" }
        val processed = Array(LEADS) { FloatArray(SAMPLES) }

        for (ch in 0 until LEADS) {
            val leadSignal = rawEcg[ch]
            require(leadSignal.size >= SAMPLES) { "Lead $ch must have at least 1000 samples" }
            val truncated = leadSignal.copyOfRange(0, SAMPLES)
            val filtered = filterDirectFormIITransposed(truncated)

            // Per-channel temporal mean and std
            var sum = 0.0
            for (v in filtered) sum += v
            val mean = (sum / SAMPLES).toFloat()

            var sumSq = 0.0
            for (v in filtered) {
                val diff = v - mean
                sumSq += diff * diff
            }
            val std = sqrt((sumSq / SAMPLES).toFloat()).coerceAtLeast(1e-6f)

            for (t in 0 until SAMPLES) {
                processed[ch][t] = (filtered[t] - mean) / std
            }
        }
        return processed
    }
}
