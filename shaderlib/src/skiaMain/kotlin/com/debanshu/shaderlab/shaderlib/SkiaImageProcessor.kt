package com.debanshu.shaderlab.shaderlib

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Surface

internal object SkiaImageProcessor {
    fun process(imageBytes: ByteArray, effect: ShaderEffectType): ByteArray? {
        return try {
            val image = Image.makeFromEncoded(imageBytes)
            val width = image.width
            val height = image.height

            val source = effect.toShaderSource(width.toFloat(), height.toFloat())

            val imageFilter = createImageFilter(source)
                ?: return imageBytes

            val surface = Surface.makeRasterN32Premul(width, height)
            val canvas = surface.canvas

            val paint = Paint().apply {
                this.imageFilter = imageFilter
            }
            canvas.drawImage(image, 0f, 0f, paint)
            val resultImage = surface.makeImageSnapshot()
            val data = resultImage.encodeToData(EncodedImageFormat.PNG)
            
            data?.bytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createImageFilter(source: ShaderSource): ImageFilter? {
        if (source is ShaderSource.Blur) {
            val radiusPx = source.radius.coerceAtLeast(0.1f)
            return ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
        }

        val runtimeEffect = RuntimeEffect.makeForShader(source.code)
        val builder = RuntimeShaderBuilder(runtimeEffect)

        applyUniforms(builder, source.uniforms)
        
        return ImageFilter.makeRuntimeShader(builder, "content", null)
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
