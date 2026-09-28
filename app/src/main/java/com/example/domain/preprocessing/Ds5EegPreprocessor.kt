package com.example.domain.preprocessing

import kotlin.math.sqrt

object Ds5EegPreprocessor {
    const val CHANNELS = 23
    const val SAMPLES = 256

    /**
     * Preprocesses [23, 256] raw EEG:
     * 1. Scale Volts to Microvolts (* 1e6)
     * 2. Channel-wise Z-score
     */
    fun preprocess(rawEeg: Array<FloatArray>): Array<FloatArray> {
        require(rawEeg.size == CHANNELS) { "EEG must have exactly 23 channels" }
        val processed = Array(CHANNELS) { FloatArray(SAMPLES) }

        for (ch in 0 until CHANNELS) {
            val channelData = rawEeg[ch]
            require(channelData.size >= SAMPLES) { "Channel $ch must have at least 256 samples" }

            // 1. Scale to microvolts
            val uV = FloatArray(SAMPLES)
            var sum = 0.0
            for (t in 0 until SAMPLES) {
                uV[t] = channelData[t] * 1e6f
                sum += uV[t]
            }
            val mean = (sum / SAMPLES).toFloat()

            var sumSq = 0.0
            for (t in 0 until SAMPLES) {
                val diff = uV[t] - mean
                sumSq += diff * diff
            }
            val std = sqrt((sumSq / SAMPLES).toFloat()).coerceAtLeast(1e-6f)

            // 2. Channel Z-score
            for (t in 0 until SAMPLES) {
                processed[ch][t] = (uV[t] - mean) / std
            }
        }
        return processed
    }
}
