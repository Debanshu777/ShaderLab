package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

data class SepiaShader(
    private val intensity: Float = 1f,
) : ShaderSpec {
    override val id: String = "sepia"

    override val displayName: String = "Sepia"

    override val shaderCode: String = """
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
    """

    override val parameters: List<ShaderParameter> =
        listOf(
            ShaderParameter.PercentageParam(
                id = "intensity",
                label = "Intensity",
                defaultValue = intensity,
            ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<UniformSpec> =
        listOf(
            UniformSpec.Floats("intensity", intensity),
        )

    override fun withParameterValue(
        parameterId: String,
        value: Float,
    ): ShaderSpec =
        when (parameterId) {
            "intensity" -> copy(intensity = value)
            else -> this
        }
}
