package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.CompositeEffect
import com.debanshu.shaderlab.shaderx.effect.CompositeEffect.Companion.DELIMITER
import com.debanshu.shaderlab.shaderx.effect.RuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.InvertEffect
import com.debanshu.shaderlab.shaderx.effect.impl.NativeBlurEffect
import com.debanshu.shaderlab.shaderx.effect.impl.PixelateEffect
import com.debanshu.shaderlab.shaderx.effect.impl.SepiaEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect
import com.debanshu.shaderlab.shaderx.effect.plus
import com.debanshu.shaderlab.shaderx.parameter.FloatParameter
import com.debanshu.shaderlab.shaderx.parameter.ParameterSpec
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.uniform.Uniform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Edge cases for [CompositeEffect]:
 * - Reserved delimiter in child parameter ID triggers [IllegalArgumentException]
 * - Nested CompositeEffect is exempt from the delimiter check
 * - Large effect chains work correctly
 * - Same effect twice with independent parameter namespacing
 * - Out-of-range index returns zero / equal instance
 * - Multi-level nested parameter routing
 * - Plus-operator flattening combinations
 */
class CompositeEffectEdgeCaseTest {
    // ── Helper: custom effect with delimiter in parameter ID ─────────────────
    // Implements RuntimeShaderEffect directly (avoiding AbstractRuntimeShaderEffect
    // which has an internal-only abstract property).
    private class DelimiterParamEffect : RuntimeShaderEffect {
        override val id = "delim_test"
        override val displayName = "Delimiter Test"
        override val shaderSource =
            "uniform shader content; half4 main(float2 fc) { return content.eval(fc); }"
        override val parameters: List<ParameterSpec> =
            listOf(
                FloatParameter("bad${DELIMITER}id", "Bad Param", 0f..1f, 0.5f),
            )

        override fun buildUniforms(
            width: Float,
            height: Float,
        ): List<Uniform> = emptyList()

        override fun withTypedParameter(
            parameterId: String,
            value: ParameterValue,
        ): RuntimeShaderEffect = this
    }

    // ── Delimiter validation in init block ────────────────────────────────────

    @Test
    fun compositeEffect_childWithDelimiterInParamId_throwsOnConstruction() {
        assertFailsWith<IllegalArgumentException> {
            CompositeEffect.of(DelimiterParamEffect())
        }
    }

    @Test
    fun compositeEffect_multipleChildren_oneWithDelimiter_throws() {
        assertFailsWith<IllegalArgumentException> {
            CompositeEffect.of(GrayscaleEffect(), DelimiterParamEffect())
        }
    }

    @Test
    fun compositeEffect_delimiterFirst_throws() {
        assertFailsWith<IllegalArgumentException> {
            CompositeEffect(listOf(DelimiterParamEffect(), VignetteEffect()))
        }
    }

    @Test
    fun compositeEffect_nestedCompositeWithDelimiterNamespacedParams_isAllowed() {
        // Inner composite has params like "0\u001Fintensity" due to its own namespacing.
        // Those are CompositeEffect children and are exempt from the delimiter check.
        val inner = GrayscaleEffect() + VignetteEffect() // produces namespaced IDs with DELIMITER
        // Wrapping in outer composite must NOT throw
        val outer = CompositeEffect.of(inner, PixelateEffect())
        assertEquals(2, outer.size)
    }

    // ── Large effect chains ───────────────────────────────────────────────────

    @Test
    fun compositeEffect_tenEffects_allParametersAccessible() {
        val effects = List(10) { i -> GrayscaleEffect(intensity = i * 0.1f) }
        val composite = CompositeEffect.of(effects)

        assertEquals(10, composite.size)
        assertEquals(10, composite.parameters.size) // each has 1 "intensity" param

        // Verify each namespaced parameter is readable
        for (i in 0 until 10) {
            val paramId = "$i${DELIMITER}intensity"
            assertEquals(
                i * 0.1f,
                composite.getParameterValue(paramId),
                0.001f,
                "Effect $i intensity mismatch",
            )
        }
    }

    @Test
    fun compositeEffect_tenEffects_withParameter_updatesOnlyTargetEffect() {
        val effects = List(10) { GrayscaleEffect(intensity = 0.5f) }
        val composite = CompositeEffect.of(effects)
        val updated = composite.withParameter("5${DELIMITER}intensity", 0.9f)

        assertEquals(0.9f, updated.getParameterValue("5${DELIMITER}intensity"), 0.001f)
        assertEquals(0.5f, updated.getParameterValue("0${DELIMITER}intensity"), 0.001f)
        assertEquals(0.5f, updated.getParameterValue("9${DELIMITER}intensity"), 0.001f)
    }

    // ── Same effect twice ─────────────────────────────────────────────────────

    @Test
    fun compositeEffect_sameEffectTwice_parametersAreIndependent() {
        val e1 = GrayscaleEffect(intensity = 0.2f)
        val e2 = GrayscaleEffect(intensity = 0.8f)
        val composite = e1 + e2

        val paramIds = composite.parameters.map { it.id }
        assertTrue("0${DELIMITER}intensity" in paramIds)
        assertTrue("1${DELIMITER}intensity" in paramIds)

        assertEquals(0.2f, composite.getParameterValue("0${DELIMITER}intensity"))
        assertEquals(0.8f, composite.getParameterValue("1${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_sameEffectTwice_updatingOneDoesNotAffectOther() {
        val composite = GrayscaleEffect(intensity = 0.3f) + GrayscaleEffect(intensity = 0.7f)
        val updated = composite.withParameter("0${DELIMITER}intensity", 0.99f)

        assertEquals(0.99f, updated.getParameterValue("0${DELIMITER}intensity"), 0.001f)
        assertEquals(0.7f, updated.getParameterValue("1${DELIMITER}intensity"), 0.001f)
    }

    // ── Out-of-range and malformed parameter IDs ──────────────────────────────

    @Test
    fun compositeEffect_withParameter_outOfRangeIndex_returnsEqualInstance() {
        val composite = GrayscaleEffect() + VignetteEffect()
        val updated = composite.withParameter("99${DELIMITER}intensity", 0.5f)
        // Index 99 doesn't exist — no-op, returns data copy
        assertEquals(composite, updated)
    }

    @Test
    fun compositeEffect_getParameterValue_outOfRangeIndex_returnsZero() {
        val composite = GrayscaleEffect() + VignetteEffect()
        assertEquals(0f, composite.getParameterValue("99${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_getParameterValue_noDelimiter_returnsZero() {
        val composite = GrayscaleEffect() + VignetteEffect()
        assertEquals(0f, composite.getParameterValue("intensity")) // no delimiter → parse fails
    }

    @Test
    fun compositeEffect_getParameterValue_nonNumericIndex_returnsZero() {
        val composite = GrayscaleEffect() + VignetteEffect()
        assertEquals(0f, composite.getParameterValue("abc${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_getTypedParameterValue_outOfRangeIndex_returnsNull() {
        val composite = GrayscaleEffect() + VignetteEffect()
        assertEquals(null, composite.getTypedParameterValue("99${DELIMITER}intensity"))
    }

    // ── Deeply nested composite parameter routing ─────────────────────────────

    @Test
    fun compositeEffect_innerComposite_paramRouting() {
        val inner = GrayscaleEffect(intensity = 0.15f) + VignetteEffect(radius = 0.45f)
        val outer = CompositeEffect.of(inner, PixelateEffect(pixelSize = 20f))

        // inner is at index 0 in outer; its "0\u001Fintensity" becomes outer's "0\u001F0\u001Fintensity"
        val grayscaleId = "0${DELIMITER}0${DELIMITER}${GrayscaleEffect.PARAM_INTENSITY}"
        val vignetteId = "0${DELIMITER}1${DELIMITER}${VignetteEffect.PARAM_RADIUS}"
        val pixelateId = "1${DELIMITER}${PixelateEffect.PARAM_PIXEL_SIZE}"

        assertEquals(0.15f, outer.getParameterValue(grayscaleId), 0.001f)
        assertEquals(0.45f, outer.getParameterValue(vignetteId), 0.001f)
        assertEquals(20f, outer.getParameterValue(pixelateId), 0.001f)
    }

    @Test
    fun compositeEffect_innerComposite_withParameter_updatesCorrectly() {
        val inner = GrayscaleEffect(intensity = 0.1f) + VignetteEffect(radius = 0.2f)
        val outer = CompositeEffect.of(inner, SepiaEffect(intensity = 0.9f))

        val grayscaleId = "0${DELIMITER}0${DELIMITER}${GrayscaleEffect.PARAM_INTENSITY}"
        val updated = outer.withParameter(grayscaleId, 0.5f)

        assertEquals(0.5f, updated.getParameterValue(grayscaleId), 0.001f)
        // Other params unchanged
        assertEquals(
            0.2f,
            updated.getParameterValue("0${DELIMITER}1${DELIMITER}${VignetteEffect.PARAM_RADIUS}"),
            0.001f,
        )
        assertEquals(
            0.9f,
            updated.getParameterValue("1${DELIMITER}${SepiaEffect.PARAM_INTENSITY}"),
            0.001f,
        )
    }

    // ── Plus-operator flattening ──────────────────────────────────────────────

    @Test
    fun compositeEffect_plusComposite_flattensToSingleLevel() {
        val a = GrayscaleEffect() + SepiaEffect()
        val b = VignetteEffect() + PixelateEffect()
        val combined = a + b
        assertEquals(4, combined.size)
    }

    @Test
    fun compositeEffect_tripleChainViaPlus_flattens() {
        val c1 = GrayscaleEffect() + SepiaEffect()
        val c2 = c1 + VignetteEffect() // CompositeEffect + ShaderEffect
        val c3 = c2 + PixelateEffect()
        assertEquals(4, c3.size)
    }

    @Test
    fun compositeEffect_of_thenPlus_flattens() {
        val c = CompositeEffect.of(GrayscaleEffect(), SepiaEffect(), VignetteEffect())
        val extended = c + PixelateEffect()
        assertEquals(4, extended.size)
        assertTrue(extended[3] is PixelateEffect)
    }

    @Test
    fun compositeEffect_plusOperator_runtimePlusNative_bothPresent() {
        val composite = GrayscaleEffect() + NativeBlurEffect()
        assertEquals(2, composite.size)
        assertTrue(composite[0] is GrayscaleEffect)
        assertTrue(composite[1] is NativeBlurEffect)
    }

    @Test
    fun compositeEffect_nativeFirst_runtimeSecond_bothPresent() {
        val composite = NativeBlurEffect() + GrayscaleEffect()
        assertEquals(2, composite.size)
        assertTrue(composite[0] is NativeBlurEffect)
        assertTrue(composite[1] is GrayscaleEffect)
    }

    // ── Composite size and id properties ─────────────────────────────────────

    @Test
    fun compositeEffect_displayName_includesAllEffectNames() {
        val composite = GrayscaleEffect() + VignetteEffect() + SepiaEffect()
        assertEquals("Grayscale + Vignette + Sepia", composite.displayName)
    }

    @Test
    fun compositeEffect_id_includesAllEffectIds() {
        val composite = GrayscaleEffect() + SepiaEffect()
        assertTrue(composite.id.contains(GrayscaleEffect.ID))
        assertTrue(composite.id.contains(SepiaEffect.ID))
    }

    // ── Parameter count integrity ─────────────────────────────────────────────

    @Test
    fun compositeEffect_parameterCount_sumOfChildParams() {
        val grayscale = GrayscaleEffect() // 1 param: intensity
        val wave = WaveEffect() // 3 params: amplitude, frequency, animate
        val invert = InvertEffect() // 0 params
        val gradient = GradientEffect() // 3 params: color1, color2, intensity

        val composite = CompositeEffect.of(grayscale, wave, invert, gradient)
        assertEquals(1 + 3 + 0 + 3, composite.parameters.size)
    }

    @Test
    fun compositeEffect_allParamIds_uniqueEvenWithDuplicateEffects() {
        val composite = GrayscaleEffect() + GrayscaleEffect() + GrayscaleEffect()
        val ids = composite.parameters.map { it.id }
        // All 3 intensity params have different prefixes
        assertEquals(ids.size, ids.toSet().size, "All composite parameter IDs must be unique")
    }

    // ── withTypedParameter for composite with typed values ────────────────────

    @Test
    fun compositeEffect_withTypedParameter_colorParam_updatesGradient() {
        val composite = GrayscaleEffect() + GradientEffect()
        val colorParamId = "1${DELIMITER}${GradientEffect.PARAM_COLOR_1}"

        val updated =
            composite.withTypedParameter(
                colorParamId,
                ParameterValue.ColorValue(0xFF0000FFL),
            )

        val value = updated.getTypedParameterValue(colorParamId)
        assertTrue(value is ParameterValue.ColorValue)
        assertEquals(0xFF0000FFL, (value as ParameterValue.ColorValue).color)
    }

    @Test
    fun compositeEffect_withTypedParameter_booleanForToggle_updatesWave() {
        val composite = GrayscaleEffect() + WaveEffect(animate = true)
        val animateParamId = "1${DELIMITER}${WaveEffect.PARAM_ANIMATE}"

        val updated =
            composite.withTypedParameter(
                animateParamId,
                ParameterValue.BooleanValue(false),
            )

        val value = updated.getTypedParameterValue(animateParamId)
        assertTrue(value is ParameterValue.BooleanValue)
        assertFalse((value as ParameterValue.BooleanValue).enabled)
    }

    // ── CompositeEffect preserves effect immutability ─────────────────────────

    @Test
    fun compositeEffect_withParameter_originalUnchanged() {
        val original = GrayscaleEffect(intensity = 0.4f) + VignetteEffect(radius = 0.6f)
        original.withParameter("0${DELIMITER}intensity", 0.9f)

        // Original must be unchanged
        assertEquals(0.4f, original.getParameterValue("0${DELIMITER}intensity"))
        assertNotEquals(
            0.9f,
            original.getParameterValue("0${DELIMITER}intensity"),
            "Composite must not be mutated by withParameter",
        )
    }
}
