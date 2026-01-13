package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect

actual fun createShaderEffect(type: ShaderEffectType, width: Float, height: Float): RenderEffect? {
    val source = type.toShaderSource(width, height)
    return SkiaShaderFactory.createEffect(source)
}

actual fun areShadersSupported(): Boolean = true
