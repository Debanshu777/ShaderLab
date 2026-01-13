package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

/**
 * Vignette shader effect.
 * Darkens the edges of the image, creating a focus effect.
 */
data class VignetteShader(
    private val radius: Float = 0.5f,
    private val intensity: Float = 0.5f
) : ShaderSpec {
    
    override val id: String = "vignette"
    
    override val displayName: String = "Vignette"
    
    override val shaderCode: String = """
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
    """
    
    override val parameters: List<ShaderParameter> = listOf(
        ShaderParameter.PercentageParam(
            id = "radius",
            label = "Radius",
            defaultValue = radius
        ),
        ShaderParameter.PercentageParam(
            id = "intensity",
            label = "Intensity",
            defaultValue = intensity
        )
    )
    
    override fun buildUniforms(width: Float, height: Float): List<UniformSpec> = listOf(
        UniformSpec.Floats("resolution", width, height),
        UniformSpec.Floats("radius", radius),
        UniformSpec.Floats("intensity", intensity)
    )
    
    override fun withParameterValue(parameterId: String, value: Float): ShaderSpec {
        return when (parameterId) {
            "radius" -> copy(radius = value)
            "intensity" -> copy(intensity = value)
            else -> this
        }
    }
}

