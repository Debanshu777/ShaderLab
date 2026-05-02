package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Applies a sepia tone effect for a vintage photograph look.
 *
 * Uses the standard sepia transformation matrix.
 *
 * @property intensity Blend amount between original (0.0) and sepia (1.0)
 */
@Immutable
public data class SepiaEffect(
    public val intensity: Float = 1f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Sepia"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float intensity;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);

            // Sepia matrix transformation
            float r = color.r * 0.393 + color.g * 0.769 + color.b * 0.189;
            float g = color.r * 0.349 + color.g * 0.686 + color.b * 0.168;
            float b = color.r * 0.272 + color.g * 0.534 + color.b * 0.131;

            half3 sepiaColor = half3(r, g, b);
            half3 result = mix(color.rgb, sepiaColor, intensity);
            return half4(result, color.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_INTENSITY to
                floatHandler<SepiaEffect>(
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
        public const val ID: String = "sepia"
        public const val PARAM_INTENSITY: String = "intensity"
    }
}
