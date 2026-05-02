package com.debanshu.shaderlab.shaderx

import androidx.compose.ui.graphics.Color
import com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.withColorParameter
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.uniform.ColorUniform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for [ShaderEffect.withColorParameter] extension functions.
 *
 * Covers the Compose Color and ARGB Long overloads, delegation to withTypedParameter,
 * color accuracy in produced uniforms, and interactions with effects that have no color params.
 */
class ShaderEffectExtensionsTest {
    // ── withColorParameter(Color) ─────────────────────────────────────────────

    @Test
    fun withColorParameter_composeRed_updatesColor1() {
        val effect = GradientEffect()
        val updated = effect.withColorParameter("color1", Color.Red) as GradientEffect
        assertNotEquals(effect.color1, updated.color1)
    }

    @Test
    fun withColorParameter_composeRed_producesCorrectUniform() {
        val updated =
            GradientEffect()
                .withColorParameter("color1", Color.Red) as GradientEffect
        val uniforms = updated.buildUniforms(100f, 100f)
        val colorUniform = uniforms.filterIsInstance<ColorUniform>().find { it.name == "color1" }!!

        assertEquals(1f, colorUniform.red, 0.01f)
        assertEquals(0f, colorUniform.green, 0.01f)
        assertEquals(0f, colorUniform.blue, 0.01f)
    }

    @Test
    fun withColorParameter_composeBlue_producesCorrectUniform() {
        val updated =
            GradientEffect()
                .withColorParameter("color2", Color.Blue) as GradientEffect
        val uniforms = updated.buildUniforms(100f, 100f)
        val colorUniform = uniforms.filterIsInstance<ColorUniform>().find { it.name == "color2" }!!

        assertEquals(0f, colorUniform.red, 0.01f)
        assertEquals(0f, colorUniform.green, 0.01f)
        assertEquals(1f, colorUniform.blue, 0.01f)
    }

    @Test
    fun withColorParameter_semiTransparent_preservesAlpha() {
        val halfAlphaRed = Color(red = 1f, green = 0f, blue = 0f, alpha = 0.5f)
        val updated =
            GradientEffect()
                .withColorParameter("color1", halfAlphaRed) as GradientEffect
        val uniforms = updated.buildUniforms(100f, 100f)
        val colorUniform = uniforms.filterIsInstance<ColorUniform>().find { it.name == "color1" }!!

        assertEquals(0.5f, colorUniform.alpha, 0.01f)
    }

    @Test
    fun withColorParameter_composeColor_delegatesToWithTypedParameter() {
        // Both paths should produce the same result
        val viaExtension =
            GradientEffect()
                .withColorParameter("color1", Color.Green) as GradientEffect
        val viaTyped =
            GradientEffect()
                .withTypedParameter(
                    "color1",
                    ParameterValue.ColorValue(
                        Color.Green.run {
                            val a = (alpha * 255).toInt() and 0xFF
                            val r = (red * 255).toInt() and 0xFF
                            val g = (green * 255).toInt() and 0xFF
                            val b = (blue * 255).toInt() and 0xFF
                            (a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
                        },
                    ),
                ) as GradientEffect

        assertEquals(viaExtension, viaTyped)
    }

    @Test
    fun withColorParameter_preservesOtherParameters() {
        val effect = GradientEffect(intensity = 0.75f)
        val updated = effect.withColorParameter("color1", Color.Red) as GradientEffect
        assertEquals(0.75f, updated.intensity)
    }

    @Test
    fun withColorParameter_unknownId_returnsUnchanged() {
        // GradientEffect's withTypedParameter returns this for unknown IDs
        val effect = GradientEffect()
        val updated = effect.withColorParameter("nonExistentParam", Color.Red)
        assertEquals(effect, updated)
    }

    @Test
    fun withColorParameter_onEffectWithNoColorParams_returnsUnchanged() {
        // GrayscaleEffect has no color parameter; withColorParameter should no-op
        val effect = GrayscaleEffect()
        val updated = effect.withColorParameter("color1", Color.Red)
        assertEquals(effect, updated)
    }

    // ── withColorParameter(Long) ──────────────────────────────────────────────

    @Test
    fun withColorParameterLong_blue_updatesColor1() {
        val effect = GradientEffect()
        val updated = effect.withColorParameter("color1", 0xFF0000FFL) as GradientEffect
        assertNotEquals(effect.color1, updated.color1)
        assertEquals(0xFF0000FFL, updated.color1)
    }

    @Test
    fun withColorParameterLong_blue_producesCorrectUniform() {
        val updated =
            GradientEffect()
                .withColorParameter("color2", 0xFF0000FFL) as GradientEffect
        val uniforms = updated.buildUniforms(100f, 100f)
        val colorUniform = uniforms.filterIsInstance<ColorUniform>().find { it.name == "color2" }!!

        assertEquals(0f, colorUniform.red, 0.01f)
        assertEquals(0f, colorUniform.green, 0.01f)
        assertEquals(1f, colorUniform.blue, 0.01f)
        assertEquals(1f, colorUniform.alpha, 0.01f)
    }

    @Test
    fun withColorParameterLong_transparentColor_alphaIsZero() {
        // 0x00FF0000 = transparent red (alpha = 0)
        val updated =
            GradientEffect()
                .withColorParameter("color1", 0x00FF0000L) as GradientEffect
        val uniforms = updated.buildUniforms(100f, 100f)
        val colorUniform = uniforms.filterIsInstance<ColorUniform>().find { it.name == "color1" }!!

        assertEquals(0f, colorUniform.alpha, 0.01f)
        assertEquals(1f, colorUniform.red, 0.01f)
    }

    @Test
    fun withColorParameterLong_preservesOtherParameters() {
        val effect = GradientEffect(intensity = 0.3f)
        val updated = effect.withColorParameter("color1", 0xFFFF0000L) as GradientEffect
        assertEquals(0.3f, updated.intensity)
    }

    @Test
    fun withColorParameterLong_color2_updatesCorrectly() {
        val effect = GradientEffect()
        val updated = effect.withColorParameter("color2", 0xFFFFFF00L) as GradientEffect // Yellow
        assertEquals(0xFFFFFF00L, updated.color2)
    }

    // ── Round-trip consistency ────────────────────────────────────────────────

    @Test
    fun withColorParameter_composeAndLong_overloads_agreeForOpaquePureRed() {
        // 0xFFFF0000 = opaque pure red in ARGB Long
        val fromLong =
            GradientEffect()
                .withColorParameter("color1", 0xFFFF0000L) as GradientEffect
        val fromCompose =
            GradientEffect()
                .withColorParameter("color1", Color.Red) as GradientEffect

        val uniforms1 = fromLong.buildUniforms(100f, 100f)
        val uniforms2 = fromCompose.buildUniforms(100f, 100f)

        val cu1 = uniforms1.filterIsInstance<ColorUniform>().find { it.name == "color1" }!!
        val cu2 = uniforms2.filterIsInstance<ColorUniform>().find { it.name == "color1" }!!

        // Both should produce the same RGBA components (within floating point tolerance)
        assertEquals(cu1.red, cu2.red, 0.01f)
        assertEquals(cu1.green, cu2.green, 0.01f)
        assertEquals(cu1.blue, cu2.blue, 0.01f)
        assertEquals(cu1.alpha, cu2.alpha, 0.01f)
    }

    @Test
    fun withColorParameter_isImmutable_originalUnchanged() {
        val original = GradientEffect(color1 = GradientEffect.DEFAULT_COLOR_1)
        val updated = original.withColorParameter("color1", Color.Blue)

        // Original is unchanged
        assertEquals(GradientEffect.DEFAULT_COLOR_1, original.color1)
        assertTrue(original !== updated)
    }
}
