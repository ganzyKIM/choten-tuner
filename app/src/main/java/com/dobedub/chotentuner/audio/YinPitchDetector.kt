package com.dobedub.chotentuner.audio

import kotlin.math.abs

/**
 * Monophonic pitch detector based on the YIN algorithm
 * (de Cheveigné & Kawahara, 2002), with parabolic interpolation
 * for sub-sample precision.
 *
 * The integration window is bufferSize/2, so the lowest detectable
 * frequency is sampleRate / (bufferSize/2) — e.g. ~21.5 Hz for a
 * 4096-sample buffer at 44.1 kHz.
 *
 * Instances are not thread safe; use from a single audio thread.
 */
class YinPitchDetector(
    private val sampleRate: Int,
    private val bufferSize: Int,
    private val threshold: Float = 0.15f,
) {

    private val tauMax = bufferSize / 2
    private val diff = FloatArray(tauMax)
    private val cmnd = FloatArray(tauMax)

    /**
     * @param buffer exactly [bufferSize] mono samples in -1..1
     * @return detected fundamental frequency in Hz, or -1f when no clear pitch
     */
    fun detect(buffer: FloatArray): Float {
        require(buffer.size == bufferSize) { "expected $bufferSize samples, got ${buffer.size}" }

        // 1. difference function d(tau) over a window of tauMax samples
        for (tau in 1 until tauMax) {
            var sum = 0f
            for (i in 0 until tauMax) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // 2. cumulative mean normalized difference d'(tau)
        cmnd[0] = 1f
        var runningSum = 0f
        for (tau in 1 until tauMax) {
            runningSum += diff[tau]
            cmnd[tau] = if (runningSum > 0f) diff[tau] * tau / runningSum else 1f
        }

        // 3. absolute threshold: first dip below threshold, then walk to its local minimum
        var tauEstimate = -1
        var tau = 2
        while (tau < tauMax) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 < tauMax && cmnd[tau + 1] < cmnd[tau]) tau++
                tauEstimate = tau
                break
            }
            tau++
        }
        if (tauEstimate == -1) return -1f

        // 4. parabolic interpolation around the minimum for sub-sample precision
        val betterTau = parabolicInterpolation(tauEstimate)
        if (betterTau <= 0f) return -1f

        val freq = sampleRate / betterTau
        return if (freq in 20f..5000f) freq else -1f
    }

    private fun parabolicInterpolation(tau: Int): Float {
        if (tau <= 0 || tau >= tauMax - 1) return tau.toFloat()
        val s0 = cmnd[tau - 1]
        val s1 = cmnd[tau]
        val s2 = cmnd[tau + 1]
        val denom = 2f * (2f * s1 - s2 - s0)
        if (abs(denom) < 1e-12f) return tau.toFloat()
        return tau + (s2 - s0) / denom
    }
}
