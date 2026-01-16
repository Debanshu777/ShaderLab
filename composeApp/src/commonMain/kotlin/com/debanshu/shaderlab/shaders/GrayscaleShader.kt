package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

data class GrayscaleShader(
    private val intensity: Float = 1f,
) : ShaderSpec {
    override val id: String = "grayscale"

    override val displayName: String = "Grayscale"

    override val shaderCode: String = """
        uniform shader content;
        uniform float intensity;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            float gray = dot(color.rgb, half3(0.299, 0.587, 0.114));
            half3 grayscaleColor = half3(gray, gray, gray);
            half3 result = mix(color.rgb, grayscaleColor, intensity);
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
