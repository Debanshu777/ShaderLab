package com.debanshu.shaderlab.shaderlib

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

internal object AGSLShaderFactory {
    fun createEffect(spec: ShaderSpec, width: Float, height: Float): RenderEffect {
        if (spec is NativeBlurSpec) {
            return createNativeBlur(spec.radius)
        }
        
        val shader = RuntimeShader(spec.shaderCode)
        val uniforms = spec.buildUniforms(width, height)
        
        applyUniforms(shader, uniforms)
        
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
                is UniformSpec.Floats -> shader.setFloatUniform(uniform.name, uniform.values)
                is UniformSpec.Ints -> shader.setIntUniform(uniform.name, uniform.values)
            }
        }
    }
}
