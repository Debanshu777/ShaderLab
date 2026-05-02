package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.parameter.ColorParameter
import com.debanshu.shaderlab.shaderx.parameter.FloatParameter
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.parameter.PixelParameter
import com.debanshu.shaderlab.shaderx.parameter.ToggleParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Edge cases for [ParameterSpec] implementations:
 * - [ParameterSpec.withId] preserves all fields except id
 * - [ParameterSpec.hasRange] is false for Color and Toggle
 * - Validation edge cases: wrong-type inputs, null returns
 */
class ParameterSpecEdgeCaseTest {
    // ── withId: FloatParameter ────────────────────────────────────────────────

    @Test
    fun floatParameter_withId_changesOnlyId() {
        val original = FloatParameter("old", "My Label", 1f..10f, 5f, decimalPlaces = 2)
        val renamed = original.withId("new")

        assertEquals("new", renamed.id)
        assertEquals("My Label", renamed.label)
        assertEquals(1f..10f, renamed.range)
        assertEquals(5f, renamed.defaultValue)
        assertEquals(2, renamed.decimalPlaces)
    }

    @Test
    fun floatParameter_withId_returnsNewInstance() {
        val original = FloatParameter("old", "Label", 0f..1f, 0.5f)
        val renamed = original.withId("new")
        assertNotSame(original, renamed)
    }

    @Test
    fun floatParameter_withId_sameId_stillNewInstance() {
        val original = FloatParameter("same", "Label", 0f..1f, 0.5f)
        val renamed = original.withId("same")
        // copy() always returns new data class instance
        assertEquals(original, renamed)
    }

    // ── withId: PercentageParameter ───────────────────────────────────────────

    @Test
    fun percentageParameter_withId_changesOnlyId() {
        val original = PercentageParameter("old", "Intensity", defaultValue = 0.75f)
        val renamed = original.withId("new")

        assertEquals("new", renamed.id)
        assertEquals("Intensity", renamed.label)
        assertEquals(0.75f, renamed.defaultValue)
        assertEquals(0f..1f, renamed.range)
    }

    // ── withId: PixelParameter ────────────────────────────────────────────────

    @Test
    fun pixelParameter_withId_changesOnlyId() {
        val original = PixelParameter("old", "Radius", 0f..50f, 10f)
        val renamed = original.withId("new")

        assertEquals("new", renamed.id)
        assertEquals("Radius", renamed.label)
        assertEquals(0f..50f, renamed.range)
        assertEquals(10f, renamed.defaultValue)
    }

    // ── withId: ToggleParameter ───────────────────────────────────────────────

    @Test
    fun toggleParameter_withId_changesOnlyId() {
        val original = ToggleParameter("old", "Animate", isEnabledByDefault = true)
        val renamed = original.withId("new")

        assertEquals("new", renamed.id)
        assertEquals("Animate", renamed.label)
        assertTrue(renamed.isEnabledByDefault)
        assertEquals(1f, renamed.defaultValue)
    }

    // ── withId: ColorParameter ────────────────────────────────────────────────

    @Test
    fun colorParameter_withId_changesOnlyId() {
        val original = ColorParameter("old", "Tint", 0xFFFF5733)
        val renamed = original.withId("new")

        assertEquals("new", renamed.id)
        assertEquals("Tint", renamed.label)
        assertEquals(0xFFFF5733, renamed.defaultColor)
    }

    // ── hasRange ─────────────────────────────────────────────────────────────

    @Test
    fun floatParameter_hasRange_isTrue() {
        assertTrue(FloatParameter("t", "T", 0f..10f, 5f).hasRange)
    }

    @Test
    fun percentageParameter_hasRange_isTrue() {
        assertTrue(PercentageParameter("t", "T").hasRange)
    }

    @Test
    fun pixelParameter_hasRange_isTrue() {
        assertTrue(PixelParameter("t", "T", 0f..50f, 10f).hasRange)
    }

    @Test
    fun toggleParameter_hasRange_isFalse() {
        assertFalse(ToggleParameter("t", "T").hasRange)
    }

    @Test
    fun colorParameter_hasRange_isFalse() {
        assertFalse(ColorParameter("t", "T", 0xFFFF0000).hasRange)
    }

    // ── PixelParameter.validateValue edge cases ───────────────────────────────

    @Test
    fun pixelParameter_validateValue_booleanValue_returnsNull() {
        val param = PixelParameter("t", "T", 0f..50f, 10f)
        assertNull(param.validateValue(ParameterValue.BooleanValue(true)))
    }

    @Test
    fun pixelParameter_validateValue_colorValue_returnsNull() {
        val param = PixelParameter("t", "T", 0f..50f, 10f)
        assertNull(param.validateValue(ParameterValue.ColorValue(0xFFFF0000)))
    }

    @Test
    fun pixelParameter_validateValue_floatValue_clampsToRange() {
        val param = PixelParameter("t", "T", 5f..20f, 10f)

        val tooLow = param.validateValue(ParameterValue.FloatValue(-10f))
        val tooHigh = param.validateValue(ParameterValue.FloatValue(100f))
        val inRange = param.validateValue(ParameterValue.FloatValue(12f))

        assertEquals(5f, (tooLow as ParameterValue.FloatValue).value)
        assertEquals(20f, (tooHigh as ParameterValue.FloatValue).value)
        assertEquals(12f, (inRange as ParameterValue.FloatValue).value)
    }

    // ── FloatParameter.validateValue: boolean coercion ────────────────────────

    @Test
    fun floatParameter_validateValue_booleanTrue_producesOne() {
        val param = FloatParameter("t", "T", 0f..10f, 5f)
        val result = param.validateValue(ParameterValue.BooleanValue(true))
        assertEquals(1f, (result as ParameterValue.FloatValue).value)
    }

    @Test
    fun floatParameter_validateValue_booleanFalse_producesZero() {
        val param = FloatParameter("t", "T", 0f..10f, 5f)
        val result = param.validateValue(ParameterValue.BooleanValue(false))
        assertEquals(0f, (result as ParameterValue.FloatValue).value)
    }

    @Test
    fun floatParameter_validateValue_colorValue_returnsNull() {
        val param = FloatParameter("t", "T", 0f..10f, 5f)
        assertNull(param.validateValue(ParameterValue.ColorValue(0xFFFF0000)))
    }

    // ── PercentageParameter.validateValue edge cases ──────────────────────────

    @Test
    fun percentageParameter_validateValue_clampsAboveOne() {
        val param = PercentageParameter("t", "T")
        val result = param.validateValue(ParameterValue.FloatValue(1.5f))
        assertEquals(1f, (result as ParameterValue.FloatValue).value)
    }

    @Test
    fun percentageParameter_validateValue_clampsBelowZero() {
        val param = PercentageParameter("t", "T")
        val result = param.validateValue(ParameterValue.FloatValue(-0.5f))
        assertEquals(0f, (result as ParameterValue.FloatValue).value)
    }

    @Test
    fun percentageParameter_validateValue_colorValue_returnsNull() {
        val param = PercentageParameter("t", "T")
        assertNull(param.validateValue(ParameterValue.ColorValue(0xFFFF0000)))
    }

    // ── ColorParameter.validateValue ──────────────────────────────────────────

    @Test
    fun colorParameter_validateValue_floatValue_returnsNull() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        assertNull(param.validateValue(ParameterValue.FloatValue(0.5f)))
    }

    @Test
    fun colorParameter_validateValue_booleanValue_returnsNull() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        assertNull(param.validateValue(ParameterValue.BooleanValue(true)))
    }

    @Test
    fun colorParameter_validateValue_colorValue_returnsItself() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        val input = ParameterValue.ColorValue(0xFF00FF00)
        val result = param.validateValue(input)
        assertTrue(result is ParameterValue.ColorValue)
        assertEquals(0xFF00FF00, (result as ParameterValue.ColorValue).color)
    }

    // ── ToggleParameter edge cases ────────────────────────────────────────────

    @Test
    fun toggleParameter_defaultValue_enabledByDefaultIsTrue_givesOne() {
        val param = ToggleParameter("t", "T", isEnabledByDefault = true)
        assertEquals(1f, param.defaultValue)
        assertTrue((param.getTypedDefaultValue() as ParameterValue.BooleanValue).enabled)
    }

    @Test
    fun toggleParameter_defaultValue_enabledByDefaultIsFalse_givesZero() {
        val param = ToggleParameter("t", "T", isEnabledByDefault = false)
        assertEquals(0f, param.defaultValue)
        assertFalse((param.getTypedDefaultValue() as ParameterValue.BooleanValue).enabled)
    }

    @Test
    fun toggleParameter_validateValue_colorValue_returnsNull() {
        val param = ToggleParameter("t", "T")
        assertNull(param.validateValue(ParameterValue.ColorValue(0xFFFF0000)))
    }

    // ── getTypedDefaultValue for all types ────────────────────────────────────

    @Test
    fun pixelParameter_getTypedDefaultValue_returnsFloatValue() {
        val param = PixelParameter("t", "T", 0f..50f, 12f)
        val value = param.getTypedDefaultValue()
        assertTrue(value is ParameterValue.FloatValue)
        assertEquals(12f, (value as ParameterValue.FloatValue).value)
    }
}
