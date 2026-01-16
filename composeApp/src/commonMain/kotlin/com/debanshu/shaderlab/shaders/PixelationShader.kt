package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

data class PixelationShader(
    private val pixelSize: Float = 10f,
) : ShaderSpec {
    override val id: String = "pixelation"

    override val displayName: String = "Pixelate"

    override val shaderCode: String = """
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
    """

    override val parameters: List<ShaderParameter> =
        listOf(
            ShaderParameter.PixelParam(
                id = "pixelSize",
                label = "Pixel Size",
                range = 1f..100f,
                defaultValue = pixelSize,
            ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<UniformSpec> =
        listOf(
            UniformSpec.Floats("resolution", width, height),
            UniformSpec.Floats("pixelSize", pixelSize.coerceAtLeast(1f)),
        )

    override fun withParameterValue(
        parameterId: String,
        value: Float,
    ): ShaderSpec =
        when (parameterId) {
            "pixelSize" -> copy(pixelSize = value.coerceAtLeast(1f))
            else -> this
        }
}
