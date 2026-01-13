package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

object ColorInversionShader : ShaderSpec {
    
    override val id: String = "color_inversion"
    
    override val displayName: String = "Invert"
    
    override val shaderCode: String = """
        uniform shader content;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            return half4(1.0 - color.rgb, color.a);
        }
    """
    
    override val parameters: List<ShaderParameter> = emptyList()
    
    override fun buildUniforms(width: Float, height: Float): List<UniformSpec> = emptyList()
    
    override fun withParameterValue(parameterId: String, value: Float): ShaderSpec = this
}

