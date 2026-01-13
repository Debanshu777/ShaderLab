package com.debanshu.shaderlab.shaderlib

interface ShaderSpec {
    val id: String
    val displayName: String
    val shaderCode: String
    val parameters: List<ShaderParameter>
    val usesNativeEffect: Boolean
        get() = false
    fun buildUniforms(width: Float, height: Float): List<UniformSpec>
    fun withParameterValue(parameterId: String, value: Float): ShaderSpec

    fun getParameterValue(parameterId: String): Float {
        return parameters.find { it.id == parameterId }?.defaultValue ?: 0f
    }
}

interface AnimatableShaderSpec : ShaderSpec {
    val isAnimating: Boolean
    val time: Float
    fun withTime(newTime: Float): AnimatableShaderSpec
}

interface NativeBlurSpec : ShaderSpec {
    val radius: Float

    override val usesNativeEffect: Boolean
        get() = true

    override val shaderCode: String
        get() = ""

    override fun buildUniforms(width: Float, height: Float): List<UniformSpec> = emptyList()
}

