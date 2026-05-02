package com.debanshu.shaderlab.shaderx.effect

import androidx.compose.ui.graphics.Color
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue

/**
 * Creates a new effect instance with the specified color parameter updated.
 *
 * Convenience extension for effects that have
 * [com.debanshu.shaderlab.shaderx.parameter.ColorParameter]-backed parameters.
 * Equivalent to:
 * ```kotlin
 * withTypedParameter(parameterId, ParameterValue.ColorValue(color.toArgbLong()))
 * ```
 *
 * ## Usage
 * ```kotlin
 * val tinted = effect.withColorParameter("tintColor", Color.Red)
 * val gradient = GradientEffect().withColorParameter("color1", Color.Blue)
 * ```
 *
 * @param parameterId The ID of the [com.debanshu.shaderlab.shaderx.parameter.ColorParameter] to update.
 * @param color The new color value.
 * @return A new [ShaderEffect] instance with the updated color parameter.
 */
public fun ShaderEffect.withColorParameter(
    parameterId: String,
    color: Color,
): ShaderEffect = withTypedParameter(parameterId, ParameterValue.ColorValue(color.toArgbLong()))

/**
 * Creates a new effect instance with the specified color parameter updated using an ARGB Long.
 *
 * @param parameterId The ID of the [com.debanshu.shaderlab.shaderx.parameter.ColorParameter] to update.
 * @param colorArgb The new color in ARGB Long format (e.g., `0xFFFF5733L`).
 * @return A new [ShaderEffect] instance with the updated color parameter.
 */
public fun ShaderEffect.withColorParameter(
    parameterId: String,
    colorArgb: Long,
): ShaderEffect = withTypedParameter(parameterId, ParameterValue.ColorValue(colorArgb))

/**
 * Converts a Compose [Color] to an ARGB [Long] (0xAARRGGBB format).
 *
 * Shared by [withColorParameter] and effect implementations that store colors as Long.
 */
internal fun Color.toArgbLong(): Long {
    val a = (alpha * 255).toInt() and 0xFF
    val r = (red * 255).toInt() and 0xFF
    val g = (green * 255).toInt() and 0xFF
    val b = (blue * 255).toInt() and 0xFF
    return (a.toLong() shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
}
