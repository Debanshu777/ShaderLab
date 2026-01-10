package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/**
 * iOS implementation of shader effect creation using Skia.
 */
actual fun createShaderEffect(type: ShaderEffectType, width: Float, height: Float): RenderEffect? {
    return when (type) {
        is ShaderEffectType.Grayscale -> createGrayscaleEffect(type.intensity)
        is ShaderEffectType.Sepia -> createSepiaEffect(type.intensity)
        is ShaderEffectType.ColorInversion -> createColorInversionEffect()
        is ShaderEffectType.Vignette -> createVignetteEffect(width, height, type.radius, type.intensity)
        is ShaderEffectType.Blur -> createBlurEffect(type.radius)
        is ShaderEffectType.Pixelation -> createPixelationEffect(width, height, type.pixelSize)
        is ShaderEffectType.ChromaticAberration -> createChromaticAberrationEffect(width, height, type.offset)
        is ShaderEffectType.WaveDistortion -> createWaveDistortionEffect(width, height, type.amplitude, type.frequency, type.time)
    }
}

/**
 * Skia shaders are always supported on iOS.
 */
actual fun areShadersSupported(): Boolean = true

private fun createGrayscaleEffect(intensity: Float): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.GRAYSCALE) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("intensity", intensity)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createSepiaEffect(intensity: Float): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.SEPIA) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("intensity", intensity)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createColorInversionEffect(): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.COLOR_INVERSION) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createVignetteEffect(width: Float, height: Float, radius: Float, intensity: Float): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.VIGNETTE) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("resolution", width, height)
    builder.uniform("radius", radius)
    builder.uniform("intensity", intensity)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createBlurEffect(radius: Float): RenderEffect? {
    // Use native Skia blur for better performance
    val radiusPx = radius.coerceAtLeast(0.1f)
    return ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
        .asComposeRenderEffect()
}

private fun createPixelationEffect(width: Float, height: Float, pixelSize: Float): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.PIXELATION) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("resolution", width, height)
    builder.uniform("pixelSize", pixelSize.coerceAtLeast(1f))
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createChromaticAberrationEffect(width: Float, height: Float, offset: Float): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.CHROMATIC_ABERRATION) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("resolution", width, height)
    builder.uniform("offset", offset)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

private fun createWaveDistortionEffect(
    width: Float,
    height: Float,
    amplitude: Float,
    frequency: Float,
    time: Float
): RenderEffect? {
    val effect = RuntimeEffect.makeForShader(ShaderConstants.WAVE_DISTORTION) ?: return null
    val builder = RuntimeShaderBuilder(effect)
    builder.uniform("resolution", width, height)
    builder.uniform("amplitude", amplitude)
    builder.uniform("frequency", frequency)
    builder.uniform("time", time)
    
    return ImageFilter.makeRuntimeShader(builder, "content", null)
        .asComposeRenderEffect()
}

