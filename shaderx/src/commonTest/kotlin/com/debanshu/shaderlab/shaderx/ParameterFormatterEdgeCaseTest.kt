package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.parameter.ColorParameter
import com.debanshu.shaderlab.shaderx.parameter.FloatParameter
import com.debanshu.shaderlab.shaderx.parameter.ParameterFormatter
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.parameter.PixelParameter
import com.debanshu.shaderlab.shaderx.parameter.ToggleParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Edge cases for [ParameterFormatter] that complement [ParameterTest]:
 * - formatFloat: 0 decimals, negative values, large values, rounding
 * - formatColor: black, white, fully transparent, mid-gray
 * - formatTyped cross-type fallback paths (non-matching value types)
 * - format with all parameter types
 */
class ParameterFormatterEdgeCaseTest {
    // ── formatFloat edge cases ────────────────────────────────────────────────

    @Test
    fun formatFloat_zeroDecimals_returnsInteger() {
        assertEquals("5", ParameterFormatter.formatFloat(5f, 0))
        assertEquals("0", ParameterFormatter.formatFloat(0f, 0))
        assertEquals("100", ParameterFormatter.formatFloat(100f, 0))
    }

    @Test
    fun formatFloat_zeroDecimals_roundsHalf() {
        assertEquals("4", ParameterFormatter.formatFloat(4.4f, 0))
        assertEquals("5", ParameterFormatter.formatFloat(4.6f, 0))
    }

    @Test
    fun formatFloat_threeDecimals_correctPrecision() {
        assertEquals("3.142", ParameterFormatter.formatFloat(3.14159f, 3))
    }

    @Test
    fun formatFloat_negativeValue_includesSign() {
        val result = ParameterFormatter.formatFloat(-5f, 1)
        assertTrue(result.startsWith("-"), "Negative value should have minus sign, got: $result")
    }

    @Test
    fun formatFloat_exactlyZero_returnsZeroString() {
        assertEquals("0.0", ParameterFormatter.formatFloat(0f, 1))
        assertEquals("0.00", ParameterFormatter.formatFloat(0f, 2))
    }

    @Test
    fun formatFloat_paddingWithZeros() {
        // "5.0" formatted to 2 decimals should become "5.00"
        assertEquals("5.00", ParameterFormatter.formatFloat(5f, 2))
        assertEquals("1.10", ParameterFormatter.formatFloat(1.1f, 2))
    }

    @Test
    fun formatFloat_largeValue() {
        val result = ParameterFormatter.formatFloat(1000f, 1)
        assertEquals("1000.0", result)
    }

    @Test
    fun formatFloat_valueExactlyAtDecimalBoundary() {
        assertEquals("1.5", ParameterFormatter.formatFloat(1.5f, 1))
        assertEquals("2.0", ParameterFormatter.formatFloat(2.0f, 1))
    }

    // ── formatColor edge cases ────────────────────────────────────────────────

    @Test
    fun formatColor_black_returnsHexBlack() {
        // 0x00000000 — alpha=0, but only RGB is shown
        assertEquals("#000000", ParameterFormatter.formatColor(0x00000000L))
    }

    @Test
    fun formatColor_white_returnsHexWhite() {
        assertEquals("#FFFFFF", ParameterFormatter.formatColor(0xFFFFFFFFL))
    }

    @Test
    fun formatColor_pureRed_returnsHexRed() {
        assertEquals("#FF0000", ParameterFormatter.formatColor(0xFFFF0000L))
    }

    @Test
    fun formatColor_pureGreen_returnsHexGreen() {
        assertEquals("#00FF00", ParameterFormatter.formatColor(0xFF00FF00L))
    }

    @Test
    fun formatColor_pureBlue_returnsHexBlue() {
        assertEquals("#0000FF", ParameterFormatter.formatColor(0xFF0000FFL))
    }

    @Test
    fun formatColor_isUpperCase() {
        val result = ParameterFormatter.formatColor(0xFFAABBCCL)
        assertEquals("#AABBCC", result)
    }

    @Test
    fun formatColor_lowValueComponentsPaddedToTwoDigits() {
        // R=0x0F (15), G=0x01, B=0x00 — all need leading-zero padding
        assertEquals("#0F0100", ParameterFormatter.formatColor(0xFF0F0100L))
    }

    // ── formatTyped cross-type fallbacks ──────────────────────────────────────

    @Test
    fun formatTyped_percentageParam_withBooleanTrue_givesHundredPercent() {
        val param = PercentageParameter("t", "T")
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(true))
        assertEquals("100%", result)
    }

    @Test
    fun formatTyped_percentageParam_withBooleanFalse_givesZeroPercent() {
        val param = PercentageParameter("t", "T")
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(false))
        assertEquals("0%", result)
    }

    @Test
    fun formatTyped_pixelParam_withBooleanTrue_givesOnePx() {
        val param = PixelParameter("t", "T", 0f..50f, 10f)
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(true))
        assertEquals("1px", result)
    }

    @Test
    fun formatTyped_pixelParam_withBooleanFalse_givesZeroPx() {
        val param = PixelParameter("t", "T", 0f..50f, 10f)
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(false))
        assertEquals("0px", result)
    }

    @Test
    fun formatTyped_colorParam_withFloatValue_returnsDefaultColor() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        val result = ParameterFormatter.formatTyped(param, ParameterValue.FloatValue(0.5f))
        assertEquals("#FF0000", result)
    }

    @Test
    fun formatTyped_colorParam_withBooleanValue_returnsDefaultColor() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(true))
        assertEquals("#FF0000", result)
    }

    @Test
    fun formatTyped_toggleParam_withColorValue_returnsOff() {
        val param = ToggleParameter("t", "T")
        val result = ParameterFormatter.formatTyped(param, ParameterValue.ColorValue(0xFFFF0000))
        assertEquals("Off", result)
    }

    @Test
    fun formatTyped_floatParam_withBooleanTrue_coercedToFloat() {
        val param = FloatParameter("t", "T", 0f..10f, 5f)
        // BooleanValue(true).toFloat() = 1f
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(true))
        assertEquals("1.0", result)
    }

    @Test
    fun formatTyped_floatParam_withBooleanFalse_coercedToFloat() {
        val param = FloatParameter("t", "T", 0f..10f, 5f)
        // BooleanValue(false).toFloat() = 0f
        val result = ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(false))
        assertEquals("0.0", result)
    }

    // ── format (float overload) for all types ─────────────────────────────────

    @Test
    fun format_floatParam_withFloat() {
        val param = FloatParameter("t", "T", 0f..10f, 5f, decimalPlaces = 2)
        assertEquals("3.14", ParameterFormatter.format(param, 3.14f))
    }

    @Test
    fun format_percentageParam_withOneHundred() {
        val param = PercentageParameter("t", "T")
        assertEquals("100%", ParameterFormatter.format(param, 1f))
    }

    @Test
    fun format_percentageParam_withZero() {
        val param = PercentageParameter("t", "T")
        assertEquals("0%", ParameterFormatter.format(param, 0f))
    }

    @Test
    fun format_pixelParam_truncatesDecimalPart() {
        val param = PixelParameter("t", "T", 0f..100f, 50f)
        // 12.9f → 12px (int cast truncates)
        assertEquals("12px", ParameterFormatter.format(param, 12.9f))
    }

    @Test
    fun format_toggleParam_atBoundary() {
        val param = ToggleParameter("t", "T")
        assertEquals("Off", ParameterFormatter.format(param, 0.5f)) // not > 0.5
        assertEquals("On", ParameterFormatter.format(param, 0.6f))
        assertEquals("Off", ParameterFormatter.format(param, 0.4f))
    }

    @Test
    fun format_colorParam_usesDefaultColor() {
        // format(ColorParameter, float) uses the default color since float has no color meaning
        val param = ColorParameter("t", "T", 0xFF00FF00)
        val result = ParameterFormatter.format(param, 0.5f)
        assertEquals("#00FF00", result)
    }

    // ── formatTyped for ColorParameter with ColorValue ────────────────────────

    @Test
    fun formatTyped_colorParam_withColorValue_showsCorrectHex() {
        val param = ColorParameter("t", "T", 0xFFFF0000) // default red
        val result =
            ParameterFormatter.formatTyped(
                param,
                ParameterValue.ColorValue(0xFF0000FF), // blue
            )
        assertEquals("#0000FF", result)
    }

    @Test
    fun formatTyped_colorParam_withDifferentColorValue_notDefaultColor() {
        val param = ColorParameter("t", "T", 0xFFFF0000)
        // Provide green as current value
        val result =
            ParameterFormatter.formatTyped(
                param,
                ParameterValue.ColorValue(0xFF00FF00),
            )
        assertEquals("#00FF00", result)
    }

    // ── ToggleParameter formatTyped ────────────────────────────────────────────

    @Test
    fun formatTyped_toggleParam_withBooleanValue_onOff() {
        val param = ToggleParameter("t", "T")
        assertEquals("On", ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(true)))
        assertEquals(
            "Off",
            ParameterFormatter.formatTyped(param, ParameterValue.BooleanValue(false)),
        )
    }

    @Test
    fun formatTyped_toggleParam_withFloatAboveHalf_on() {
        val param = ToggleParameter("t", "T")
        assertEquals("On", ParameterFormatter.formatTyped(param, ParameterValue.FloatValue(0.6f)))
    }

    @Test
    fun formatTyped_toggleParam_withFloatBelowHalf_off() {
        val param = ToggleParameter("t", "T")
        assertEquals("Off", ParameterFormatter.formatTyped(param, ParameterValue.FloatValue(0.4f)))
    }
}
