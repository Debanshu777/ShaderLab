package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

/**
 * Chromatic aberration shader effect.
 * Creates color fringing by offsetting RGB channels.
 */
data class ChromaticAberrationShader(
    private val offset: Float = 5f
) : ShaderSpec {
    
    override val id: String = "chromatic_aberration"
    
    override val displayName: String = "Chromatic"
    
    override val shaderCode: String = """
        uniform shader content;
        uniform float2 resolution;
        uniform float offset;
        
        half4 main(float2 fragCoord) {
            // Calculate direction from center
            float2 center = resolution * 0.5;
            float2 dir = normalize(fragCoord - center);
            
            // Sample each color channel with offset
            float r = content.eval(fragCoord + dir * offset).r;
            float g = content.eval(fragCoord).g;
            float b = content.eval(fragCoord - dir * offset).b;
            float a = content.eval(fragCoord).a;
            
            return half4(r, g, b, a);
        }
    """
    
    override val parameters: List<ShaderParameter> = listOf(
        ShaderParameter.PixelParam(
            id = "offset",
            label = "Offset",
            range = 0f..20f,
            defaultValue = offset
        )
    )
    
    override fun buildUniforms(width: Float, height: Float): List<UniformSpec> = listOf(
        UniformSpec.Floats("resolution", width, height),
        UniformSpec.Floats("offset", offset)
    )
    
    override fun withParameterValue(parameterId: String, value: Float): ShaderSpec {
        return when (parameterId) {
            "offset" -> copy(offset = value)
            else -> this
        }
    }
}

