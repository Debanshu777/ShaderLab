package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.parameter.PixelParameter
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Applies a chromatic aberration effect simulating lens distortion.
 *
 * Separates color channels radially from the center of the image.
 *
 * @property offset Distance in pixels to offset red and blue channels
 */
@Immutable
public data class ChromaticAberrationEffect(
    public val offset: Float = 5f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Chromatic"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float2 resolution;
        uniform float offset;

        half4 main(float2 fragCoord) {
            // Calculate direction from center
            float2 center = resolution * 0.5;
            float2 dir = normalize(fragCoord - center);

            // Sample each color channel with offset — reuse center sample for g and a (L2 fix)
            half4 center_sample = content.eval(fragCoord);
            float r = content.eval(fragCoord + dir * offset).r;
            float b = content.eval(fragCoord - dir * offset).b;

            return half4(r, center_sample.g, b, center_sample.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_OFFSET to
                floatHandler<ChromaticAberrationEffect>(
                    spec =
                        PixelParameter(
                            id = PARAM_OFFSET,
                            label = "Offset",
                            range = 0f..20f,
                            defaultValue = 5f,
                        ),
                    read = { it.offset },
                    write = { e, v -> e.copy(offset = v) },
                ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<Uniform> =
        listOf(
            FloatUniform("resolution", width, height),
            FloatUniform("offset", offset),
        )

    public companion object {
        public const val ID: String = "chromatic_aberration"
        public const val PARAM_OFFSET: String = "offset"
    }
}
