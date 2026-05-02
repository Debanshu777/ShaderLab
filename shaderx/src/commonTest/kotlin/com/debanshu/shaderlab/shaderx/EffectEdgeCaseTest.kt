package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.impl.ChromaticAberrationEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.InvertEffect
import com.debanshu.shaderlab.shaderx.effect.impl.NativeBlurEffect
import com.debanshu.shaderlab.shaderx.effect.impl.PixelateEffect
import com.debanshu.shaderlab.shaderx.effect.impl.SepiaEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Effect edge-case tests:
 * - NaN parameter values pass through [AbstractRuntimeShaderEffect] (known footgun)
 * - Infinity is properly clamped
 * - Toggle 0.5f boundary (strict > threshold)
 * - Unknown parameter ID handling
 * - Immutability guarantees
 * - [InvertEffect.Default] singleton
 * - [WaveEffect] time updates
 * - [NativeBlurEffect] radius clamping
 */
class EffectEdgeCaseTest {
    // ── NaN footgun: NaN is NOT clamped by coerceIn ───────────────────────────

    /**
     * IMPORTANT documented behaviour: Float.NaN passes through parameter validation
     * unchanged because `NaN.coerceIn(range)` returns NaN (NaN comparisons always false).
     *
     * Callers must validate user input before passing values to [ShaderEffect.withParameter].
     */
    @Test
    fun withParameter_nanPassesThroughValidation() {
        val effect = GrayscaleEffect().withParameter("intensity", Float.NaN) as GrayscaleEffect
        assertTrue(effect.intensity.isNaN(), "NaN bypasses coerceIn — passes through unchanged")
    }

    @Test
    fun withParameter_nan_vignette_passesThrough() {
        val effect = VignetteEffect().withParameter("radius", Float.NaN) as VignetteEffect
        assertTrue(effect.radius.isNaN())
    }

    // ── Infinity IS clamped by coerceIn ──────────────────────────────────────

    @Test
    fun withParameter_positiveInfinity_clampedToRangeMax() {
        val effect =
            GrayscaleEffect()
                .withParameter("intensity", Float.POSITIVE_INFINITY) as GrayscaleEffect
        assertEquals(1f, effect.intensity, "POSITIVE_INFINITY is clamped to range max (1f)")
    }

    @Test
    fun withParameter_negativeInfinity_clampedToRangeMin() {
        val effect =
            GrayscaleEffect()
                .withParameter("intensity", Float.NEGATIVE_INFINITY) as GrayscaleEffect
        assertEquals(0f, effect.intensity, "NEGATIVE_INFINITY is clamped to range min (0f)")
    }

    @Test
    fun withParameter_positiveInfinity_pixelate_clampedToMax() {
        val effect =
            PixelateEffect()
                .withParameter("pixelSize", Float.POSITIVE_INFINITY) as PixelateEffect
        assertEquals(
            100f,
            effect.pixelSize,
            "POSITIVE_INFINITY clamped to PixelParameter max (100f)",
        )
    }

    // ── Toggle handler 0.5f boundary (strict > threshold) ────────────────────

    /**
     * The toggle handler uses `pv.value > 0.5f` (strict inequality).
     * Exactly 0.5f maps to false; anything above 0.5f maps to true.
     */
    @Test
    fun toggleHandler_exactlyHalfFloat_mapsFalse() {
        val effect =
            WaveEffect(animate = true)
                .withParameter("animate", 0.5f) as WaveEffect
        assertFalse(effect.animate, "0.5f uses strict '>' so 0.5f → false")
    }

    @Test
    fun toggleHandler_slightlyAboveHalf_mapsTrue() {
        val effect =
            WaveEffect(animate = false)
                .withParameter("animate", 0.5001f) as WaveEffect
        assertTrue(effect.animate, "0.5001f > 0.5f → true")
    }

    @Test
    fun toggleHandler_zero_mapsFalse() {
        val effect = WaveEffect(animate = true).withParameter("animate", 0f) as WaveEffect
        assertFalse(effect.animate)
    }

    @Test
    fun toggleHandler_one_mapsTrue() {
        val effect = WaveEffect(animate = false).withParameter("animate", 1f) as WaveEffect
        assertTrue(effect.animate)
    }

    // ── Unknown parameter ID handling ─────────────────────────────────────────

    @Test
    fun withParameter_unknownId_returnsEqualInstance() {
        val original = GrayscaleEffect(intensity = 0.7f)
        val result = original.withParameter("nonExistentParam", 0.5f)
        assertEquals(original, result)
    }

    @Test
    fun getParameterValue_unknownId_returnsZero() {
        assertEquals(0f, GrayscaleEffect().getParameterValue("nonExistent"))
        assertEquals(0f, SepiaEffect().getParameterValue("nothing"))
        assertEquals(0f, VignetteEffect().getParameterValue("unknown"))
        assertEquals(0f, WaveEffect().getParameterValue("nothere"))
    }

    @Test
    fun getTypedParameterValue_unknownId_returnsNull() {
        assertNull(GrayscaleEffect().getTypedParameterValue("nonExistent"))
        assertNull(SepiaEffect().getTypedParameterValue("nothing"))
        assertNull(VignetteEffect().getTypedParameterValue("unknown"))
        assertNull(WaveEffect().getTypedParameterValue("nothere"))
    }

    @Test
    fun getTypedParameterValue_knownIds_returnsCorrectTypes() {
        val grayscale = GrayscaleEffect(intensity = 0.6f)
        val intensity = grayscale.getTypedParameterValue("intensity")
        assertTrue(intensity is ParameterValue.FloatValue)
        assertEquals(0.6f, (intensity as ParameterValue.FloatValue).value)
    }

    // ── Immutability: withParameter always returns a new instance ─────────────

    @Test
    fun grayscaleEffect_withParameter_doesNotMutateOriginal() {
        val original = GrayscaleEffect(intensity = 0.5f)
        val updated = original.withParameter("intensity", 0.9f) as GrayscaleEffect

        assertNotSame(original, updated)
        assertEquals(0.5f, original.intensity, "Original must not be mutated")
        assertEquals(0.9f, updated.intensity)
    }

    @Test
    fun vignetteEffect_withParameter_doesNotMutateOriginal() {
        val original = VignetteEffect(radius = 0.3f, intensity = 0.4f)
        original.withParameter("radius", 0.9f)
        assertEquals(0.3f, original.radius, "Original radius must not be mutated")
    }

    @Test
    fun waveEffect_withTime_doesNotMutateOriginal() {
        val original = WaveEffect(time = 0f)
        original.withTime(99f)
        assertEquals(0f, original.time, "Original time must not be mutated")
    }

    // ── InvertEffect.Default singleton ────────────────────────────────────────

    @Test
    fun invertEffect_default_isSameReferenceEveryTime() {
        assertSame(InvertEffect.Default, InvertEffect.Default)
    }

    @Test
    fun invertEffect_default_equalsNewInstance() {
        assertEquals(InvertEffect(), InvertEffect.Default)
    }

    @Test
    fun invertEffect_hasNoParameters_withParameterIsNoOp() {
        val effect = InvertEffect()
        val result = effect.withParameter("anything", 1f)
        assertEquals(effect, result)
    }

    // ── WaveEffect time bounds ────────────────────────────────────────────────

    @Test
    fun waveEffect_withTime_negativeValue_accepted() {
        val effect = WaveEffect().withTime(-5f)
        assertEquals(-5f, effect.time)
    }

    @Test
    fun waveEffect_withTime_veryLargeValue_accepted() {
        val effect = WaveEffect().withTime(Float.MAX_VALUE)
        assertEquals(Float.MAX_VALUE, effect.time)
    }

    @Test
    fun waveEffect_withTime_zero_accepted() {
        val effect = WaveEffect(time = 1f).withTime(0f)
        assertEquals(0f, effect.time)
    }

    // ── NativeBlurEffect radius clamping ─────────────────────────────────────

    @Test
    fun nativeBlurEffect_withParameter_negativeRadius_clampedToZero() {
        // PixelParameter range is 0f..50f — negative clamped to 0
        val effect = NativeBlurEffect().withParameter("radius", -5f) as NativeBlurEffect
        assertEquals(0f, effect.radius)
    }

    @Test
    fun nativeBlurEffect_withParameter_excessiveRadius_clampedToMax() {
        val effect = NativeBlurEffect().withParameter("radius", 1000f) as NativeBlurEffect
        assertEquals(50f, effect.radius, "Radius clamped to PixelParameter max (50f)")
    }

    @Test
    fun nativeBlurEffect_zeroRadius_storedAsZero() {
        // Effect stores 0f; factory applies MIN_BLUR_RADIUS (0.1f) at render time
        val effect = NativeBlurEffect(radius = 0f)
        assertEquals(0f, effect.radius)
    }

    // ── GradientEffect double-update consistency ──────────────────────────────

    @Test
    fun gradientEffect_chainingWithParameter_appliesInOrder() {
        val effect =
            GradientEffect(intensity = 0.5f)
                .withParameter("intensity", 0.7f) as GradientEffect

        assertEquals(0.7f, effect.intensity)
    }

    @Test
    fun gradientEffect_withColor1Long_thenIntensity_bothApplied() {
        val effect = GradientEffect()
        val step1 =
            effect.withTypedParameter(
                "color1",
                ParameterValue.ColorValue(0xFF0000FFL),
            ) as GradientEffect
        val step2 = step1.withParameter("intensity", 0.8f) as GradientEffect

        assertEquals(0xFF0000FFL, step2.color1)
        assertEquals(0.8f, step2.intensity)
    }

    // ── ChromaticAberrationEffect offset clamping ─────────────────────────────

    @Test
    fun chromaticAberration_excessiveOffset_clampedToMax() {
        val effect =
            ChromaticAberrationEffect()
                .withParameter("offset", 999f) as ChromaticAberrationEffect
        assertEquals(20f, effect.offset, "offset clamped to PixelParameter max (20f)")
    }

    @Test
    fun chromaticAberration_negativeOffset_clampedToZero() {
        val effect =
            ChromaticAberrationEffect()
                .withParameter("offset", -10f) as ChromaticAberrationEffect
        assertEquals(0f, effect.offset, "negative offset clamped to 0f")
    }

    // ── buildUniforms consistency across effects ──────────────────────────────

    @Test
    fun allRuntimeEffects_buildUniforms_neverReturnsNullElements() {
        val effects =
            listOf(
                GrayscaleEffect(),
                SepiaEffect(),
                VignetteEffect(),
                PixelateEffect(),
                ChromaticAberrationEffect(),
                InvertEffect(),
                WaveEffect(),
                GradientEffect(),
            )
        effects.forEach { effect ->
            val uniforms = effect.buildUniforms(500f, 500f)
            uniforms.forEach { uniform ->
                assertTrue(
                    uniform.name.isNotBlank(),
                    "${effect.id}: uniform name must not be blank",
                )
            }
        }
    }

    @Test
    fun pixelateEffect_buildUniforms_pixelSizeIsAtLeastOne() {
        // Even if constructed with a sub-minimum value, buildUniforms coerces it
        val effect = PixelateEffect(pixelSize = 0.5f)
        val uniforms = effect.buildUniforms(100f, 100f)
        val pixelSizeUniform =
            uniforms.find { it.name == "pixelSize" }
                as com.debanshu.shaderlab.shaderx.uniform.FloatUniform
        assertTrue(pixelSizeUniform.values[0] >= 1f, "pixelSize uniform must be at least 1f")
    }

    @Test
    fun nativeBlurEffect_hasNoShaderUniforms() {
        // NativeBlurEffect is not a RuntimeShaderEffect — has no shaderSource/buildUniforms
        val effect = NativeBlurEffect()
        // Verify it's a NativeEffect and has correct properties
        assertTrue(effect is com.debanshu.shaderlab.shaderx.effect.NativeEffect)
        assertEquals("blur", effect.id)
    }

    // ── ShaderX.builtInEffects type check ─────────────────────────────────────

    @Test
    fun shaderX_builtInEffects_containsExpectedTypes() {
        val effects = ShaderX.builtInEffects()
        assertTrue(effects.any { it is GrayscaleEffect })
        assertTrue(effects.any { it is SepiaEffect })
        assertTrue(effects.any { it is GradientEffect })
        assertTrue(effects.any { it is VignetteEffect })
        assertTrue(effects.any { it is PixelateEffect })
        assertTrue(effects.any { it is ChromaticAberrationEffect })
        assertTrue(effects.any { it is InvertEffect })
        assertTrue(effects.any { it is WaveEffect })
        assertTrue(effects.any { it is NativeBlurEffect })
    }

    @Test
    fun shaderX_builtInEffects_waveEffectHasAnimateEnabled() {
        val wave = ShaderX.builtInEffects().filterIsInstance<WaveEffect>().first()
        assertTrue(
            wave.isAnimating,
            "Default WaveEffect in builtInEffects should have animate=true",
        )
    }
}
