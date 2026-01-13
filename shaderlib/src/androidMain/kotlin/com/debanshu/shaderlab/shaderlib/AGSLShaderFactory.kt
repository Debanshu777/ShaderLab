package com.debanshu.shaderlab.shaderlib

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

internal object AGSLShaderFactory {
    fun createEffect(source: ShaderSource): RenderEffect {
        if (source is ShaderSource.Blur) {
            return createNativeBlur(source.radius)
        }
        
        val shader = RuntimeShader(source.code)

        applyUniforms(shader, source.uniforms)
        
        return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }

    private fun createNativeBlur(radius: Float): RenderEffect {
        val radiusPx = radius.coerceAtLeast(0.1f)
        return AndroidRenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
            .asComposeRenderEffect()
    }

    private fun applyUniforms(shader: RuntimeShader, uniforms: List<UniformSpec>) {
        uniforms.forEach { uniform ->
            when (uniform) {
                is UniformSpec.Float1 -> shader.setFloatUniform(uniform.name, uniform.value)
                is UniformSpec.Float2 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y)
                is UniformSpec.Float3 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y, uniform.z)
                is UniformSpec.Float4 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y, uniform.z, uniform.w)
            }
        }
    }
}
