package com.debanshu.shaderlab.shaderlib

import androidx.compose.ui.graphics.RenderEffect

expect fun createShaderEffect(spec: ShaderSpec, width: Float, height: Float): RenderEffect?

expect fun areShadersSupported(): Boolean
