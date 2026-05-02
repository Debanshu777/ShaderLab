package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.PixelateEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for Phase 5: H5 — parameter validation via AbstractRuntimeShaderEffect.
 */
class ParameterValidationTest {
    @Test
    fun pixelateEffect_clampsPixelSizeBelowMinimum() {
        val effect =
            PixelateEffect().withParameter(PixelateEffect.PARAM_PIXEL_SIZE, -10f) as PixelateEffect
        assertTrue(
            effect.pixelSize >= 1f,
            "pixelSize should be clamped to minimum 1f, got ${effect.pixelSize}",
        )
    }

    @Test
    fun pixelateEffect_clampsPixelSizeAboveMaximum() {
        val effect =
            PixelateEffect().withParameter(PixelateEffect.PARAM_PIXEL_SIZE, 999f) as PixelateEffect
        assertTrue(
            effect.pixelSize <= 100f,
            "pixelSize should be clamped to maximum 100f, got ${effect.pixelSize}",
        )
    }

    @Test
    fun vignetteEffect_clampsRadiusBelowMinimum() {
        val effect =
            VignetteEffect().withParameter(VignetteEffect.PARAM_RADIUS, -5f) as VignetteEffect
        assertTrue(
            effect.radius >= 0f,
            "radius should be clamped to minimum 0f, got ${effect.radius}",
        )
    }

    @Test
    fun vignetteEffect_clampsIntensityAboveMaximum() {
        val effect =
            VignetteEffect().withParameter(VignetteEffect.PARAM_INTENSITY, 999f) as VignetteEffect
        assertTrue(
            effect.intensity <= 1f,
            "intensity should be clamped to maximum 1f, got ${effect.intensity}",
        )
    }

    @Test
    fun grayscaleEffect_clampsIntensityToRange() {
        val tooHigh =
            GrayscaleEffect().withParameter(GrayscaleEffect.PARAM_INTENSITY, 2f) as GrayscaleEffect
        val tooLow =
            GrayscaleEffect().withParameter(GrayscaleEffect.PARAM_INTENSITY, -1f) as GrayscaleEffect
        assertTrue(
            tooHigh.intensity <= 1f,
            "intensity should be clamped to 1f, got ${tooHigh.intensity}",
        )
        assertTrue(
            tooLow.intensity >= 0f,
            "intensity should be clamped to 0f, got ${tooLow.intensity}",
        )
    }

    @Test
    fun wrongTypeForFloatParam_throws() {
        assertFailsWith<IllegalArgumentException> {
            PixelateEffect().withTypedParameter(
                PixelateEffect.PARAM_PIXEL_SIZE,
                ParameterValue.ColorValue(0xFF000000L),
            )
        }
    }

    @Test
    fun wrongTypeForColorParam_throws() {
        assertFailsWith<IllegalArgumentException> {
            com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect().withTypedParameter(
                com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect.PARAM_COLOR_1,
                ParameterValue.FloatValue(0.5f),
            )
        }
    }
}
