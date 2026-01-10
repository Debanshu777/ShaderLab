package com.debanshu.shaderlab.shaderlib

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Android implementation of shader effect creation.
 * Requires API 33+ (Tiramisu) for RuntimeShader support.
 */
actual fun createShaderEffect(type: ShaderEffectType, width: Float, height: Float): RenderEffect? {
    if (!areShadersSupported()) return null
    
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
 * Android requires API 33+ for RuntimeShader support.
 */
actual fun areShadersSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

private fun createGrayscaleEffect(intensity: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.GRAYSCALE)
    shader.setFloatUniform("intensity", intensity)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createSepiaEffect(intensity: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.SEPIA)
    shader.setFloatUniform("intensity", intensity)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createColorInversionEffect(): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.COLOR_INVERSION)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createVignetteEffect(width: Float, height: Float, radius: Float, intensity: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.VIGNETTE)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("radius", radius)
    shader.setFloatUniform("intensity", intensity)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createBlurEffect(radius: Float): RenderEffect? {
    // Use native blur for better performance
    val radiusPx = radius.coerceAtLeast(0.1f)
    return AndroidRenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
        .asComposeRenderEffect()
}

private fun createPixelationEffect(width: Float, height: Float, pixelSize: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.PIXELATION)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("pixelSize", pixelSize.coerceAtLeast(1f))
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createChromaticAberrationEffect(width: Float, height: Float, offset: Float): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.CHROMATIC_ABERRATION)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("offset", offset)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

private fun createWaveDistortionEffect(
    width: Float,
    height: Float,
    amplitude: Float,
    frequency: Float,
    time: Float
): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    
    val shader = RuntimeShader(ShaderConstants.WAVE_DISTORTION)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("amplitude", amplitude)
    shader.setFloatUniform("frequency", frequency)
    shader.setFloatUniform("time", time)
    
    return AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
        .asComposeRenderEffect()
}

