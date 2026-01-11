package com.debanshu.shaderlab.shaderlib

import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Surface

/**
 * iOS implementation of shader image processing using Skia.
 */
actual fun applyShaderToImage(imageBytes: ByteArray, effect: ShaderEffectType): ByteArray? {
    return try {
        // Decode the image
        val image = Image.makeFromEncoded(imageBytes)
        val width = image.width
        val height = image.height
        
        // Create the image filter for the effect
        val imageFilter = createImageFilter(effect, width.toFloat(), height.toFloat())
            ?: return imageBytes // No filter, return original
        
        // Create a surface to draw onto
        val surface = Surface.makeRasterN32Premul(width, height)
        val canvas = surface.canvas
        
        // Draw the image with the filter applied
        val paint = Paint().apply {
            this.imageFilter = imageFilter
        }
        canvas.drawImage(image, 0f, 0f, paint)
        
        // Encode the result as PNG
        val resultImage = surface.makeImageSnapshot()
        val data = resultImage.encodeToData(EncodedImageFormat.PNG)
        
        data?.bytes
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Creates a Skia ImageFilter for the given shader effect type.
 */
private fun createImageFilter(effect: ShaderEffectType, width: Float, height: Float): ImageFilter? {
    return when (effect) {
        is ShaderEffectType.Grayscale -> createGrayscaleFilter(effect.intensity)
        is ShaderEffectType.Sepia -> createSepiaFilter(effect.intensity)
        is ShaderEffectType.ColorInversion -> createColorInversionFilter()
        is ShaderEffectType.Vignette -> createVignetteFilter(width, height, effect.radius, effect.intensity)
        is ShaderEffectType.Blur -> createBlurFilter(effect.radius)
        is ShaderEffectType.Pixelation -> createPixelationFilter(width, height, effect.pixelSize)
        is ShaderEffectType.ChromaticAberration -> createChromaticAberrationFilter(width, height, effect.offset)
        is ShaderEffectType.WaveDistortion -> createWaveDistortionFilter(width, height, effect.amplitude, effect.frequency, effect.time)
    }
}

private fun createGrayscaleFilter(intensity: Float): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.GRAYSCALE) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("intensity", intensity)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createSepiaFilter(intensity: Float): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.SEPIA) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("intensity", intensity)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createColorInversionFilter(): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.COLOR_INVERSION) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createVignetteFilter(width: Float, height: Float, radius: Float, intensity: Float): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.VIGNETTE) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("resolution", width, height)
    builder.uniform("radius", radius)
    builder.uniform("intensity", intensity)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createBlurFilter(radius: Float): ImageFilter {
    val radiusPx = radius.coerceAtLeast(0.1f)
    return ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
}

private fun createPixelationFilter(width: Float, height: Float, pixelSize: Float): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.PIXELATION) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("resolution", width, height)
    builder.uniform("pixelSize", pixelSize.coerceAtLeast(1f))
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createChromaticAberrationFilter(width: Float, height: Float, offset: Float): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.CHROMATIC_ABERRATION) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("resolution", width, height)
    builder.uniform("offset", offset)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

private fun createWaveDistortionFilter(
    width: Float,
    height: Float,
    amplitude: Float,
    frequency: Float,
    time: Float
): ImageFilter? {
    val runtimeEffect = RuntimeEffect.makeForShader(ShaderConstants.WAVE_DISTORTION) ?: return null
    val builder = RuntimeShaderBuilder(runtimeEffect)
    builder.uniform("resolution", width, height)
    builder.uniform("amplitude", amplitude)
    builder.uniform("frequency", frequency)
    builder.uniform("time", time)
    return ImageFilter.makeRuntimeShader(builder, "content", null)
}

