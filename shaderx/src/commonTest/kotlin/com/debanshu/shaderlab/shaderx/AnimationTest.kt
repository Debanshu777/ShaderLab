package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for Phase 4: H4 — animated effect equality excludes time.
 */
class AnimationTest {
    @Test
    fun waveEffect_equalityExcludesTime() {
        val a = WaveEffect(amplitude = 10f, frequency = 5f, animate = true, time = 0f)
        val b = WaveEffect(amplitude = 10f, frequency = 5f, animate = true, time = 1.5f)
        assertEquals(
            a,
            b,
            "WaveEffect instances with same params but different time should be equal",
        )
        assertEquals(a.hashCode(), b.hashCode(), "hashCode should be consistent with equals")
    }

    @Test
    fun waveEffect_equalityHonorsAmplitude() {
        val a = WaveEffect(amplitude = 10f, time = 0f)
        val b = WaveEffect(amplitude = 20f, time = 0f)
        assertNotEquals(a, b, "Different amplitude should produce different equality")
    }

    @Test
    fun waveEffect_equalityHonorsFrequency() {
        val a = WaveEffect(frequency = 5f, time = 0f)
        val b = WaveEffect(frequency = 10f, time = 0f)
        assertNotEquals(a, b, "Different frequency should produce different equality")
    }

    @Test
    fun waveEffect_equalityHonorsAnimate() {
        val a = WaveEffect(animate = true, time = 0f)
        val b = WaveEffect(animate = false, time = 0f)
        assertNotEquals(a, b, "Different animate flag should produce different equality")
    }

    @Test
    fun waveEffect_withTime_returnsNewInstanceSameEquality() {
        val original = WaveEffect(amplitude = 10f)
        val updated = original.withTime(99f)
        assertEquals(original, updated, "withTime should not change equality")
        assertEquals(99f, updated.time, "withTime should update the time field")
    }

    /**
     * Contract guard: any AnimatedShaderEffect implementation must exclude [time] from
     * equality so that [rememberShaderEffect]'s LaunchedEffect key stays stable across
     * frames. If time were included, the key would change every ~16 ms, tearing down
     * and recreating the animation coroutine on every frame.
     *
     * This test uses WaveEffect as the canonical reference implementation.
     * New AnimatedShaderEffect implementations must pass an equivalent test.
     */
    @Test
    fun animatedEffect_timeExclusion_preventsCoroutineRestartOnEveryFrame() {
        val frame0 = WaveEffect(amplitude = 10f, time = 0f)
        val frame1 = WaveEffect(amplitude = 10f, time = 0.016f)
        val frame60 = WaveEffect(amplitude = 10f, time = 1.0f)

        // All three frames are equal — a stable LaunchedEffect key sees no change.
        assertEquals(frame0, frame1, "Frame 0 and frame 1 must be equal (time excluded)")
        assertEquals(frame0, frame60, "Frame 0 and frame 60 must be equal (time excluded)")
        assertEquals(frame0.hashCode(), frame1.hashCode(), "hashCode must be stable across frames")
    }
}
