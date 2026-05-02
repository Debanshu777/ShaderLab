package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.colorHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.effect.toArgbLong
import com.debanshu.shaderlab.shaderx.parameter.ColorParameter
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.uniform.ColorUniform
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Applies a gradient overlay effect to the image.
 *
 * Creates a smooth gradient between two colors that blends with the original image.
 * The gradient is calculated based on normalized distance from the bottom-left corner,
 * clamped to [0, 1] to avoid `mix()` extrapolation (L3 fix).
 *
 * @property color1 First gradient color in ARGB format (bottom-left)
 * @property color2 Second gradient color in ARGB format (top-right)
 * @property intensity Blend amount between original (0.0) and gradient (1.0)
 */
@Immutable
public data class GradientEffect(
    public val color1: Long = DEFAULT_COLOR_1,
    public val color2: Long = DEFAULT_COLOR_2,
    public val intensity: Float = 0.5f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Gradient"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float2 resolution;
        layout(color) uniform half4 color1;
        layout(color) uniform half4 color2;
        uniform float intensity;

        half4 main(float2 fragCoord) {
            half4 originalColor = content.eval(fragCoord);

            // Normalize coordinates and clamp mix to [0,1] to avoid extrapolation (L3 fix)
            float2 uv = fragCoord / resolution;
            float mixValue = clamp(distance(uv, vec2(0.0, 1.0)) / sqrt(2.0), 0.0, 1.0);

            // Interpolate between the two colors
            half4 gradientColor = mix(color1, color2, mixValue);

            // Blend gradient with original image
            half3 blendedRgb = mix(originalColor.rgb, originalColor.rgb * gradientColor.rgb, intensity);

            return half4(blendedRgb, originalColor.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_COLOR_1 to
                colorHandler<GradientEffect>(
                    spec =
                        ColorParameter(
                            id = PARAM_COLOR_1,
                            label = "Color 1",
                            defaultColor = DEFAULT_COLOR_1,
                        ),
                    read = { it.color1 },
                    write = { e, v -> e.copy(color1 = v) },
                ),
            PARAM_COLOR_2 to
                colorHandler<GradientEffect>(
                    spec =
                        ColorParameter(
                            id = PARAM_COLOR_2,
                            label = "Color 2",
                            defaultColor = DEFAULT_COLOR_2,
                        ),
                    read = { it.color2 },
                    write = { e, v -> e.copy(color2 = v) },
                ),
            PARAM_INTENSITY to
                floatHandler<GradientEffect>(
                    spec =
                        PercentageParameter(
                            id = PARAM_INTENSITY,
                            label = "Intensity",
                            defaultValue = 0.5f,
                        ),
                    read = { it.intensity },
                    write = { e, v -> e.copy(intensity = v) },
                ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<Uniform> =
        listOf(
            FloatUniform("resolution", width, height),
            ColorUniform("color1", color1),
            ColorUniform("color2", color2),
            FloatUniform("intensity", intensity),
        )

    /**
     * Creates a new effect with the first color updated.
     *
     * @param color Color in ARGB Long format
     */
    public fun withColor1(color: Long): GradientEffect = copy(color1 = color)

    /**
     * Creates a new effect with the first color updated.
     *
     * @param color Compose Color value
     */
    public fun withColor1(color: Color): GradientEffect = copy(color1 = color.toArgbLong())

    /**
     * Creates a new effect with the second color updated.
     *
     * @param color Color in ARGB Long format
     */
    public fun withColor2(color: Long): GradientEffect = copy(color2 = color)

    /**
     * Creates a new effect with the second color updated.
     *
     * @param color Compose Color value
     */
    public fun withColor2(color: Color): GradientEffect = copy(color2 = color.toArgbLong())

    public companion object {
        public const val ID: String = "gradient_overlay"
        public const val PARAM_COLOR_1: String = "color1"
        public const val PARAM_COLOR_2: String = "color2"
        public const val PARAM_INTENSITY: String = "intensity"

        /** Coral color - default for color1 */
        public const val DEFAULT_COLOR_1: Long = 0xFFF3A397

        /** Light yellow - default for color2 */
        public const val DEFAULT_COLOR_2: Long = 0xFFF8EE94
    }
}
