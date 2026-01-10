package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect

/**
 * Creates a platform-specific RenderEffect for the given shader effect type.
 * 
 * @param type The shader effect type to create
 * @param width The width of the image in pixels
 * @param height The height of the image in pixels
 * @return A RenderEffect that can be applied via graphicsLayer, or null if shaders are not supported
 */
expect fun createShaderEffect(type: ShaderEffectType, width: Float, height: Float): RenderEffect?

/**
 * Returns true if GPU shader effects are supported on the current platform.
 * On Android, this requires API 33+ (Tiramisu) for RuntimeShader.
 * On iOS and Desktop (JVM), shaders are always supported via Skia.
 */
expect fun areShadersSupported(): Boolean

