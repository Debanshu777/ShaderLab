package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.CompositeEffect
import com.debanshu.shaderlab.shaderx.effect.CompositeEffect.Companion.DELIMITER
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.PixelateEffect
import com.debanshu.shaderlab.shaderx.effect.impl.SepiaEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect
import com.debanshu.shaderlab.shaderx.effect.plus
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Contract tests for the [ShaderEffect] parameter API and [CompositeEffect] namespacing.
 *
 * Covers:
 * - [ShaderEffect.withTypedParameter] rejects wrong-type values with [IllegalArgumentException]
 * - FloatValue is accepted for toggle parameters (coerced to boolean)
 * - [ShaderEffect.withParameter] (Float) delegates to [ShaderEffect.withTypedParameter]
 * - Composite parameter IDs use the U+001F [DELIMITER], not underscores
 * - Nested composite parameter routing works correctly
 * - All built-in effects are known sealed subtypes of [ShaderEffect]
 */
class ShaderEffectContractTest {
    // ── withTypedParameter: wrong-type rejection ──────────────────────────────

    @Test
    fun grayscaleEffect_wrongType_throws() {
        assertFailsWith<IllegalArgumentException> {
            GrayscaleEffect().withTypedParameter(
                GrayscaleEffect.PARAM_INTENSITY,
                ParameterValue.ColorValue(0xFF000000L),
            )
        }
    }

    @Test
    fun sepiaEffect_wrongType_throws() {
        assertFailsWith<IllegalArgumentException> {
            SepiaEffect().withTypedParameter(
                SepiaEffect.PARAM_INTENSITY,
                ParameterValue.BooleanValue(true),
            )
        }
    }

    @Test
    fun vignetteEffect_wrongType_throws() {
        assertFailsWith<IllegalArgumentException> {
            VignetteEffect().withTypedParameter(
                VignetteEffect.PARAM_RADIUS,
                ParameterValue.ColorValue(0xFFFFFFFFL),
            )
        }
    }

    @Test
    fun waveEffect_colorValueForAnimate_throws() {
        assertFailsWith<IllegalArgumentException> {
            WaveEffect().withTypedParameter(
                WaveEffect.PARAM_ANIMATE,
                ParameterValue.ColorValue(0xFF000000L),
            )
        }
    }

    @Test
    fun waveEffect_floatValueForToggle_allowed() {
        // FloatValue is intentionally accepted for toggle params (coerced to boolean)
        val updated =
            WaveEffect(animate = true).withTypedParameter(
                WaveEffect.PARAM_ANIMATE,
                ParameterValue.FloatValue(0.0f),
            )
        assertIs<WaveEffect>(updated)
        assertEquals(false, updated.isAnimating)
    }

    @Test
    fun withParameter_float_delegates_to_withTypedParameter() {
        val viaFloat = GrayscaleEffect().withParameter(GrayscaleEffect.PARAM_INTENSITY, 0.42f)
        val viaTyped =
            GrayscaleEffect().withTypedParameter(
                GrayscaleEffect.PARAM_INTENSITY,
                ParameterValue.FloatValue(0.42f),
            )
        assertEquals(viaFloat, viaTyped)
    }

    // ── CompositeEffect: parameter ID delimiter (U+001F) ──────────────────────

    @Test
    fun compositeEffect_parameterIds_useDelimiter_notUnderscore() {
        val composite = GrayscaleEffect() + VignetteEffect()
        val paramIds = composite.parameters.map { it.id }

        assertTrue(
            paramIds.all { DELIMITER in it },
            "Expected all composite param IDs to contain U+001F delimiter, got: $paramIds",
        )
        assertTrue(
            paramIds.none { it.startsWith("0_") || it.startsWith("1_") },
            "Old underscore prefix found in composite param IDs: $paramIds",
        )
    }

    @Test
    fun compositeEffect_nestedComposite_paramRoundTrip() {
        // (Grayscale + Vignette) + Pixelate — flattened to 3 effects by the + operator
        val composite = GrayscaleEffect() + VignetteEffect() + PixelateEffect()
        val paramId = "2${DELIMITER}${PixelateEffect.PARAM_PIXEL_SIZE}"
        val updated = composite.withParameter(paramId, 20f)

        assertEquals(20f, updated.getParameterValue(paramId))
        // Grayscale param unchanged
        assertEquals(1f, updated.getParameterValue("0${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_innerComposite_paramRoutes() {
        // Inner CompositeEffect as a member of outer CompositeEffect
        val inner = GrayscaleEffect() + VignetteEffect()
        val outer = CompositeEffect(listOf(inner, PixelateEffect()))

        val paramId = "0${DELIMITER}0${DELIMITER}${GrayscaleEffect.PARAM_INTENSITY}"
        val updated = outer.withParameter(paramId, 0.25f)

        assertEquals(0.25f, updated.getParameterValue(paramId))
    }

    // ── Sealed hierarchy: all built-in effects are known subtypes ──────────────

    @Test
    fun builtInEffects_allAreKnownSealedSubtypes() {
        val effects = ShaderX.builtInEffects()
        effects.forEach { effect ->
            val isKnownSubtype =
                effect is com.debanshu.shaderlab.shaderx.effect.RuntimeShaderEffect ||
                    effect is com.debanshu.shaderlab.shaderx.effect.NativeEffect ||
                    effect is CompositeEffect
            assertTrue(
                isKnownSubtype,
                "Effect '${effect.id}' (${effect::class.simpleName}) is not a known sealed subtype",
            )
        }
    }
}
