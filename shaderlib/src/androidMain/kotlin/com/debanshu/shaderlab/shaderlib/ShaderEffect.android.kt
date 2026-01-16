package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect

actual fun createShaderEffect(
    spec: ShaderSpec,
    width: Float,
    height: Float,
): RenderEffect? = AGSLShaderFactory.createEffect(spec, width, height)

actual fun areShadersSupported(): Boolean = true
