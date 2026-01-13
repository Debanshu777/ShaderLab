package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal object SkiaShaderFactory {
    fun createEffect(source: ShaderSource): RenderEffect {
        if (source is ShaderSource.Blur) {
            return createNativeBlur(source.radius)
        }

        val effect = RuntimeEffect.makeForShader(source.code)
        val builder = RuntimeShaderBuilder(effect)

        applyUniforms(builder, source.uniforms)
        
        return ImageFilter.makeRuntimeShader(builder, "content", null)
            .asComposeRenderEffect()
    }

    private fun createNativeBlur(radius: Float): RenderEffect {
        val radiusPx = radius.coerceAtLeast(0.1f)
        return ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
            .asComposeRenderEffect()
    }

    private fun applyUniforms(builder: RuntimeShaderBuilder, uniforms: List<UniformSpec>) {
        uniforms.forEach { uniform ->
            when (uniform) {
                is UniformSpec.Float1 -> builder.uniform(uniform.name, uniform.value)
                is UniformSpec.Float2 -> builder.uniform(uniform.name, uniform.x, uniform.y)
                is UniformSpec.Float3 -> builder.uniform(uniform.name, uniform.x, uniform.y, uniform.z)
                is UniformSpec.Float4 -> builder.uniform(uniform.name, uniform.x, uniform.y, uniform.z, uniform.w)
            }
        }
    }
}
