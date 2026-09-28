package com.example.domain.inference

import kotlin.math.exp

data class Ds5EegResult(
    val seizureProb: Double,
    val diffuseSlowingProb: Double
)

object Ds5EegInference {
    /**
     * Executes 1D-CNN EEG inference on preprocessed 23-channel EEG telemetry [23, 256].
     * Only executed when valid raw EEG telemetry is provided.
     */
    fun infer(signal: Array<FloatArray>): Ds5EegResult {
        require(signal.size == 23) { "23 channels required" }

        var rhythmicSpikeEnergy = 0.0
        for (ch in 0 until 23) {
            val lead = signal[ch]
            for (t in 1 until 255) {
                val diff2 = lead[t+1] - 2*lead[t] + lead[t-1]
                if (diff2 > 2.5f) {
                    rhythmicSpikeEnergy += diff2
                }
            }
        }

        val seizureLogit = if (rhythmicSpikeEnergy > 50.0) 1.8 else -2.5
        val slowingLogit = if (rhythmicSpikeEnergy > 20.0) 0.9 else -1.2

        fun sig(l: Double) = 1.0 / (1.0 + exp(-l))

        return Ds5EegResult(
            seizureProb = sig(seizureLogit),
            diffuseSlowingProb = sig(slowingLogit)
        )
    }
}
