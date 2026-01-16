package com.debanshu.shaderlab.shaderlib

import kotlin.math.pow
import kotlin.math.roundToInt

sealed interface ShaderParameter {
    val id: String
    val label: String
    val defaultValue: Float
    val range: ClosedFloatingPointRange<Float>
    val formatValue: (Float) -> String

    data class FloatParam(
        override val id: String,
        override val label: String,
        override val range: ClosedFloatingPointRange<Float>,
        override val defaultValue: Float,
        override val formatValue: (Float) -> String = { formatFloat(it, 1) },
    ) : ShaderParameter

    data class PercentageParam(
        override val id: String,
        override val label: String,
        override val defaultValue: Float = 1f,
        override val formatValue: (Float) -> String = { "${(it * 100).toInt()}%" },
    ) : ShaderParameter {
        override val range: ClosedFloatingPointRange<Float> = 0f..1f
    }

    data class PixelParam(
        override val id: String,
        override val label: String,
        override val range: ClosedFloatingPointRange<Float>,
        override val defaultValue: Float,
        override val formatValue: (Float) -> String = { "${it.toInt()}px" },
    ) : ShaderParameter

    data class ToggleParam(
        override val id: String,
        override val label: String,
        val isEnabledByDefault: Boolean = false,
        override val formatValue: (Float) -> String = { if (it > 0.5f) "On" else "Off" },
    ) : ShaderParameter {
        override val range: ClosedFloatingPointRange<Float> = 0f..1f
        override val defaultValue: Float = if (isEnabledByDefault) 1f else 0f
    }
}

internal fun formatFloat(
    value: Float,
    decimals: Int,
): String {
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
