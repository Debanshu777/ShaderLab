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
    fun process(imageBytes: ByteArray, spec: ShaderSpec, width: Float, height: Float): ByteArray? {
        return try {
            val image = Image.makeFromEncoded(imageBytes)
            val imageWidth = image.width
            val imageHeight = image.height

            val effectWidth = if (width > 0) width else imageWidth.toFloat()
            val effectHeight = if (height > 0) height else imageHeight.toFloat()

            val imageFilter = createImageFilter(spec, effectWidth, effectHeight)
                ?: return imageBytes

            val surface = Surface.makeRasterN32Premul(imageWidth, imageHeight)
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

    private fun createImageFilter(spec: ShaderSpec, width: Float, height: Float): ImageFilter? {
        if (spec is NativeBlurSpec) {
            val radiusPx = spec.radius.coerceAtLeast(0.1f)
            return ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
        }

        val runtimeEffect = RuntimeEffect.makeForShader(spec.shaderCode)
        val builder = RuntimeShaderBuilder(runtimeEffect)
        val uniforms = spec.buildUniforms(width, height)

        applyUniforms(builder, uniforms)
        
        return ImageFilter.makeRuntimeShader(builder, "content", null)
    }

    private fun applyUniforms(builder: RuntimeShaderBuilder, uniforms: List<UniformSpec>) {
        uniforms.forEach { uniform ->
            when (uniform) {
                is UniformSpec.Floats -> builder.uniform(uniform.name, uniform.values)
                is UniformSpec.Ints -> builder.uniform(uniform.name, uniform.values.first())
            }
        }
    }
}
