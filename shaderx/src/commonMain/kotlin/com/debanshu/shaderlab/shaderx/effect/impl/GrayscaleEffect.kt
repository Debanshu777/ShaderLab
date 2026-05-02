package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Converts an image to grayscale using luminance weights.
 *
 * Uses the standard luminance formula: 0.299R + 0.587G + 0.114B
 *
 * @property intensity Blend amount between original (0.0) and grayscale (1.0)
 */
@Immutable
public data class GrayscaleEffect(
    public val intensity: Float = 1f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Grayscale"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float intensity;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            float gray = dot(color.rgb, half3(0.299, 0.587, 0.114));
            half3 grayscaleColor = half3(gray, gray, gray);
            half3 result = mix(color.rgb, grayscaleColor, intensity);
            return half4(result, color.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_INTENSITY to
                floatHandler<GrayscaleEffect>(
                    spec =
                        PercentageParameter(
                            id = PARAM_INTENSITY,
                            label = "Intensity",
                            defaultValue = 1f,
                        ),
                    read = { it.intensity },
                    write = { e, v -> e.copy(intensity = v) },
                ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<Uniform> = listOf(FloatUniform("intensity", intensity))

    public companion object {
        public const val ID: String = "grayscale"
        public const val PARAM_INTENSITY: String = "intensity"
    }
}
