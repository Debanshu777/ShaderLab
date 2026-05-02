package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.CompositeEffect
import com.debanshu.shaderlab.shaderx.effect.CompositeEffect.Companion.DELIMITER
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.NativeBlurEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.effect.plus
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CompositeEffectTest {
    @Test
    fun compositeEffect_plusOperator_combinesTwoEffects() {
        val effect = GrayscaleEffect() + VignetteEffect()

        assertTrue(effect is CompositeEffect)
        assertEquals(2, effect.size)
        assertTrue(effect[0] is GrayscaleEffect)
        assertTrue(effect[1] is VignetteEffect)
    }

    @Test
    fun compositeEffect_plusOperator_chainsMultipleEffects() {
        val effect = GrayscaleEffect() + VignetteEffect() + NativeBlurEffect()

        assertEquals(3, effect.size)
        assertTrue(effect[0] is GrayscaleEffect)
        assertTrue(effect[1] is VignetteEffect)
        assertTrue(effect[2] is NativeBlurEffect)
    }

    @Test
    fun compositeEffect_of_createsFromVarargs() {
        val effect =
            CompositeEffect.of(
                GrayscaleEffect(),
                VignetteEffect(),
                NativeBlurEffect(),
            )

        assertEquals(3, effect.size)
    }

    @Test
    fun compositeEffect_of_createsFromList() {
        val effects = listOf(GrayscaleEffect(), VignetteEffect())
        val effect = CompositeEffect.of(effects)

        assertEquals(2, effect.size)
    }

    @Test
    fun compositeEffect_id_combinesEffectIds() {
        val effect = GrayscaleEffect() + VignetteEffect()

        assertTrue(effect.id.contains("grayscale"))
        assertTrue(effect.id.contains("vignette"))
    }

    @Test
    fun compositeEffect_displayName_combinesNames() {
        val effect = GrayscaleEffect() + VignetteEffect()

        assertEquals("Grayscale + Vignette", effect.displayName)
    }

    @Test
    fun compositeEffect_parameters_prefixesWithIndex() {
        val effect = GrayscaleEffect() + VignetteEffect()

        // GrayscaleEffect has "intensity" -> "0${DELIMITER}intensity"
        // VignetteEffect has "radius" and "intensity" -> "1${DELIMITER}radius", "1${DELIMITER}intensity"
        val paramIds = effect.parameters.map { it.id }

        assertTrue(paramIds.contains("0${DELIMITER}intensity"))
        assertTrue(paramIds.contains("1${DELIMITER}radius"))
        assertTrue(paramIds.contains("1${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_withParameter_updatesCorrectEffect() {
        val effect = GrayscaleEffect(intensity = 0.5f) + VignetteEffect(radius = 0.5f)

        val updated = effect.withParameter("1${DELIMITER}radius", 0.8f)

        assertNotEquals(effect, updated)
        assertEquals(0.8f, updated.getParameterValue("1${DELIMITER}radius"))
        assertEquals(0.5f, updated.getParameterValue("0${DELIMITER}intensity"))
    }

    @Test
    fun compositeEffect_withTypedParameter_updatesCorrectEffect() {
        val effect = GrayscaleEffect(intensity = 0.5f) + VignetteEffect(radius = 0.5f)

        val updated =
            effect.withTypedParameter(
                "0${DELIMITER}intensity",
                ParameterValue.FloatValue(0.9f),
            )

        assertNotEquals(effect, updated)

        val value = updated.getTypedParameterValue("0${DELIMITER}intensity")
        assertTrue(value is ParameterValue.FloatValue)
        assertEquals(0.9f, (value as ParameterValue.FloatValue).value)
    }

    @Test
    fun compositeEffect_getParameterValue_returnsCorrectValue() {
        val effect = GrayscaleEffect(intensity = 0.7f) + VignetteEffect(radius = 0.3f)

        assertEquals(0.7f, effect.getParameterValue("0${DELIMITER}intensity"))
        assertEquals(0.3f, effect.getParameterValue("1${DELIMITER}radius"))
    }

    @Test
    fun compositeEffect_getParameterValue_returnsZeroForUnknown() {
        val effect = GrayscaleEffect() + VignetteEffect()

        // Both contain no DELIMITER so parseParameterId returns null → 0f
        assertEquals(0f, effect.getParameterValue("invalidparam"))
        assertEquals(
            0f,
            effect.getParameterValue("99${DELIMITER}intensity"),
        ) // valid format but out of range index
    }

    @Test
    fun compositeEffect_plusComposite_flattensEffects() {
        val first = GrayscaleEffect() + VignetteEffect()
        val second = NativeBlurEffect() + GrayscaleEffect()

        val combined = first + second

        assertEquals(4, combined.size)
    }

    @Test
    fun compositeEffect_indexOperator_accessesEffect() {
        val effect = GrayscaleEffect() + VignetteEffect() + NativeBlurEffect()

        assertTrue(effect[0] is GrayscaleEffect)
        assertTrue(effect[1] is VignetteEffect)
        assertTrue(effect[2] is NativeBlurEffect)
    }

    @Test
    fun compositeEffect_requiresAtLeastOneEffect() {
        assertFailsWith<IllegalArgumentException> {
            CompositeEffect(emptyList())
        }
    }

    @Test
    fun compositeEffect_singleEffect_works() {
        val effect = CompositeEffect.of(GrayscaleEffect())

        assertEquals(1, effect.size)
        assertTrue(effect[0] is GrayscaleEffect)
    }

    @Test
    fun compositeEffect_preservesEffectState() {
        val grayscale = GrayscaleEffect(intensity = 0.3f)
        val vignette = VignetteEffect(radius = 0.7f, intensity = 0.4f)

        val composite = grayscale + vignette

        // Verify the contained effects maintain their state
        val containedGrayscale = composite[0] as GrayscaleEffect
        val containedVignette = composite[1] as VignetteEffect

        assertEquals(grayscale, containedGrayscale)
        assertEquals(vignette, containedVignette)
    }
}
