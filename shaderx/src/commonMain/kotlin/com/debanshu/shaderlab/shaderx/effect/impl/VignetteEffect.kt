package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.parameter.PercentageParameter
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Applies a vignette effect that darkens the edges of an image.
 *
 * Creates a smooth gradient from center to edges.
 *
 * @property radius Distance from center where darkening begins (0.0 to 1.0)
 * @property intensity Strength of the darkening effect (0.0 to 1.0)
 */
@Immutable
public data class VignetteEffect(
    public val radius: Float = 0.5f,
    public val intensity: Float = 0.5f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Vignette"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float2 resolution;
        uniform float radius;
        uniform float intensity;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);

            // Normalize coordinates to center
            float2 uv = fragCoord / resolution;
            float2 center = float2(0.5, 0.5);
            float dist = distance(uv, center);

            // Calculate vignette factor
            float vignette = smoothstep(radius, radius - intensity, dist);

            return half4(color.rgb * vignette, color.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_RADIUS to
                floatHandler<VignetteEffect>(
                    spec = PercentageParameter(id = PARAM_RADIUS, label = "Radius", defaultValue = 0.5f),
                    read = { it.radius },
                    write = { e, v -> e.copy(radius = v) },
                ),
            PARAM_INTENSITY to
                floatHandler<VignetteEffect>(
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
            FloatUniform("radius", radius),
            FloatUniform("intensity", intensity),
        )

    public companion object {
        public const val ID: String = "vignette"
        public const val PARAM_RADIUS: String = "radius"
        public const val PARAM_INTENSITY: String = "intensity"
    }
}
