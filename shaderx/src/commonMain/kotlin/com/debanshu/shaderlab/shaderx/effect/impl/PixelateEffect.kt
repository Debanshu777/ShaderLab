package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.effect.floatHandler
import com.debanshu.shaderlab.shaderx.parameter.PixelParameter
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Applies a pixelation effect by reducing image resolution.
 *
 * Creates a retro, mosaic-like appearance.
 *
 * @property pixelSize Size of each pixel block in pixels (clamped to ≥ 1)
 */
@Immutable
public data class PixelateEffect(
    public val pixelSize: Float = 10f,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Pixelate"

    override val shaderSource: String =
        """
        uniform shader content;
        uniform float2 resolution;
        uniform float pixelSize;

        half4 main(float2 fragCoord) {
            // Snap coordinates to pixel grid
            float2 pixelCoord = floor(fragCoord / pixelSize) * pixelSize + pixelSize * 0.5;

            // Clamp to valid range
            pixelCoord = clamp(pixelCoord, float2(0.0), resolution);

            return content.eval(pixelCoord);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> =
        mapOf(
            PARAM_PIXEL_SIZE to
                floatHandler<PixelateEffect>(
                    spec =
                        PixelParameter(
                            id = PARAM_PIXEL_SIZE,
                            label = "Pixel Size",
                            range = 1f..100f,
                            defaultValue = 10f,
                        ),
                    read = { it.pixelSize },
                    write = { e, v -> e.copy(pixelSize = v.coerceAtLeast(1f)) },
                ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<Uniform> =
        listOf(
            FloatUniform("resolution", width, height),
            FloatUniform("pixelSize", pixelSize.coerceAtLeast(1f)),
        )

    public companion object {
        public const val ID: String = "pixelation"
        public const val PARAM_PIXEL_SIZE: String = "pixelSize"
    }
}
