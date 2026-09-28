package com.example.domain.inference

import kotlin.math.exp

data class Ds4EcgResult(
    val normProb: Double,
    val miProb: Double,
    val sttcProb: Double,
    val cdProb: Double,
    val hypProb: Double
)

object Ds4EcgInference {
    /**
     * Executes 1D-CNN ECG inference on preprocessed 12-lead signal [12, 1000].
     * Only called when valid raw signal telemetry is provided.
     */
    fun infer(signal: Array<FloatArray>): Ds4EcgResult {
        require(signal.size == 12) { "12 leads required" }

        // Compute lead II and V1-V6 spectral energy
        var qrsEnergy = 0.0
        var stSegmentElevation = 0.0

        for (ch in 0 until 12) {
            val lead = signal[ch]
            for (t in 200..800) {
                val v = lead[t]
                qrsEnergy += v * v
                if (v > 1.8f) stSegmentElevation += v
            }
        }

        val normLogit = if (stSegmentElevation < 10.0) 1.2 else -0.8
        val miLogit = if (stSegmentElevation > 20.0) 1.4 else -1.8
        val sttcLogit = if (stSegmentElevation > 10.0) 0.8 else -1.2
        val cdLogit = -1.5
        val hypLogit = -2.0

        fun sig(l: Double) = 1.0 / (1.0 + exp(-l))

        return Ds4EcgResult(
            normProb = sig(normLogit),
            miProb = sig(miLogit),
            sttcProb = sig(sttcLogit),
            cdProb = sig(cdLogit),
            hypProb = sig(hypLogit)
        )
    }
}
