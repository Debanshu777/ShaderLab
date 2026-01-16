package com.debanshu.shaderlab.shaders

import com.debanshu.shaderlab.shaderlib.NativeBlurSpec
import com.debanshu.shaderlab.shaderlib.ShaderParameter
import com.debanshu.shaderlab.shaderlib.ShaderSpec

data class BlurShader(
    override val radius: Float = 10f,
) : NativeBlurSpec {
    override val id: String = "blur"

    override val displayName: String = "Blur"

    override val parameters: List<ShaderParameter> =
        listOf(
            ShaderParameter.PixelParam(
                id = "radius",
                label = "Radius",
                range = 0f..50f,
                defaultValue = radius,
            ),
        )

    override fun withParameterValue(
        parameterId: String,
        value: Float,
    ): ShaderSpec =
        when (parameterId) {
            "radius" -> copy(radius = value)
            else -> this
        }
}
