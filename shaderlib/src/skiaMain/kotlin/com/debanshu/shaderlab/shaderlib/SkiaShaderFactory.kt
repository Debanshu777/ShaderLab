package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

internal object SkiaShaderFactory {
    fun createEffect(
        spec: ShaderSpec,
        width: Float,
        height: Float,
    ): RenderEffect {
        if (spec is NativeBlurSpec) {
            return createNativeBlur(spec.radius)
        }

        val effect = RuntimeEffect.makeForShader(spec.shaderCode)
        val builder = RuntimeShaderBuilder(effect)
        val uniforms = spec.buildUniforms(width, height)

        applyUniforms(builder, uniforms)

        return ImageFilter
            .makeRuntimeShader(builder, "content", null)
            .asComposeRenderEffect()
    }

    private fun createNativeBlur(radius: Float): RenderEffect {
        val radiusPx = radius.coerceAtLeast(0.1f)
        return ImageFilter
            .makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
            .asComposeRenderEffect()
    }

    private fun applyUniforms(
        builder: RuntimeShaderBuilder,
        uniforms: List<UniformSpec>,
    ) {
        uniforms.forEach { uniform ->
            when (uniform) {
                is UniformSpec.Floats -> builder.uniform(uniform.name, uniform.values)
                is UniformSpec.Ints -> builder.uniform(uniform.name, uniform.values.first())
            }
        }
    }
}
