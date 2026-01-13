package com.debanshu.shaderlab.shaderlib

/**
 * Represents a shader uniform specification with type-safe value binding.
 * This abstraction allows factories to apply uniforms generically without
 * knowing the specific shader implementation details.
 */
sealed class UniformSpec {
    abstract val name: String
    
    /**
     * A single float uniform value.
     */
    data class Float1(
        override val name: String,
        val value: Float
    ) : UniformSpec()
    
    /**
     * A float2/vec2 uniform value (e.g., resolution, offset).
     */
    data class Float2(
        override val name: String,
        val x: Float,
        val y: Float
    ) : UniformSpec()
    
    /**
     * A float3/vec3 uniform value (e.g., color RGB).
     */
    data class Float3(
        override val name: String,
        val x: Float,
        val y: Float,
        val z: Float
    ) : UniformSpec()
    
    /**
     * A float4/vec4 uniform value (e.g., color RGBA).
     */
    data class Float4(
        override val name: String,
        val x: Float,
        val y: Float,
        val z: Float,
        val w: Float
    ) : UniformSpec()
}

