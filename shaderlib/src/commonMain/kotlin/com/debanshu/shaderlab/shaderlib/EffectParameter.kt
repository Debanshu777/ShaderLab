package com.debanshu.shaderlab.shaderlib

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Formats a float value with the specified number of decimal places.
 * This is a multiplatform-compatible alternative to String.format().
 */
internal fun formatFloat(value: Float, decimals: Int): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = (value * multiplier).roundToInt() / multiplier
    return if (decimals == 0) {
        rounded.toInt().toString()
    } else {
        val str = rounded.toString()
        val dotIndex = str.indexOf('.')
        if (dotIndex == -1) {
            "$str.${"0".repeat(decimals)}"
        } else {
            val currentDecimals = str.length - dotIndex - 1
            if (currentDecimals < decimals) {
                str + "0".repeat(decimals - currentDecimals)
            } else {
                str.take(dotIndex + decimals + 1)
            }
        }
    }
}

/**
 * Sealed interface representing different types of effect parameters.
 * Each parameter type defines how the UI should display and format the value.
 */
sealed interface EffectParameter {
    val label: String
    val valueRange: ClosedFloatingPointRange<Float>
    val currentValue: Float
    val formatValue: (Float) -> String
    
    /**
     * A generic float parameter with custom value range.
     */
    data class FloatParam(
        override val label: String,
        override val valueRange: ClosedFloatingPointRange<Float>,
        override val currentValue: Float,
        override val formatValue: (Float) -> String = { formatFloat(it, 1) }
    ) : EffectParameter
    
    /**
     * A percentage parameter (0-100%).
     */
    data class PercentageParam(
        override val label: String,
        override val currentValue: Float,
        override val formatValue: (Float) -> String = { "${(it * 100).toInt()}%" }
    ) : EffectParameter {
        override val valueRange: ClosedFloatingPointRange<Float> = 0f..1f
    }
    
    /**
     * A pixel-based parameter displaying value as "Npx".
     */
    data class PixelParam(
        override val label: String,
        override val valueRange: ClosedFloatingPointRange<Float>,
        override val currentValue: Float,
        override val formatValue: (Float) -> String = { "${it.toInt()}px" }
    ) : EffectParameter
    
    /**
     * A boolean toggle parameter (0 = off, 1 = on).
     */
    data class ToggleParam(
        override val label: String,
        val isEnabled: Boolean,
        override val formatValue: (Float) -> String = { if (it > 0.5f) "On" else "Off" }
    ) : EffectParameter {
        override val valueRange: ClosedFloatingPointRange<Float> = 0f..1f
        override val currentValue: Float = if (isEnabled) 1f else 0f
    }
}

