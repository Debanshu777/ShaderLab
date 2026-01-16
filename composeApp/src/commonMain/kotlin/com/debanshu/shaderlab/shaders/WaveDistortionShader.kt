package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.AnimatableShaderSpec
import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec
import com.debanshu.shaderlab.shaderlib.UniformSpec

data class WaveDistortionShader(
    private val amplitude: Float = 10f,
    private val frequency: Float = 5f,
    private val animate: Boolean = true,
    override val time: Float = 0f,
) : AnimatableShaderSpec {
    override val id: String = "wave_distortion"

    override val displayName: String = "Wave"

    override val isAnimating: Boolean = animate

    override val shaderCode: String = """
        uniform shader content;
        uniform float2 resolution;
        uniform float amplitude;
        uniform float frequency;
        uniform float time;
        
        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            
            // Apply wave distortion
            float xOffset = sin(uv.y * frequency + time) * amplitude;
            float yOffset = cos(uv.x * frequency + time) * amplitude;
            
            float2 distortedCoord = fragCoord + float2(xOffset, yOffset);
            
            // Clamp to valid range
            distortedCoord = clamp(distortedCoord, float2(0.0), resolution);
            
            return content.eval(distortedCoord);
        }
    """

    override val parameters: List<ShaderParameter> =
        listOf(
            ShaderParameter.PixelParam(
                id = "amplitude",
                label = "Amplitude",
                range = 0f..50f,
                defaultValue = amplitude,
            ),
            ShaderParameter.FloatParam(
                id = "frequency",
                label = "Frequency",
                range = 1f..20f,
                defaultValue = frequency,
            ),
            ShaderParameter.ToggleParam(
                id = "animate",
                label = "Animate",
                isEnabledByDefault = animate,
            ),
        )

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<UniformSpec> =
        listOf(
            UniformSpec.Floats("resolution", width, height),
            UniformSpec.Floats("amplitude", amplitude),
            UniformSpec.Floats("frequency", frequency),
            UniformSpec.Floats("time", time),
        )

    override fun withParameterValue(
        parameterId: String,
        value: Float,
    ): ShaderSpec =
        when (parameterId) {
            "amplitude" -> copy(amplitude = value)
            "frequency" -> copy(frequency = value)
            "animate" -> copy(animate = value > 0.5f)
            else -> this
        }

    override fun withTime(newTime: Float): AnimatableShaderSpec = copy(time = newTime)
}
