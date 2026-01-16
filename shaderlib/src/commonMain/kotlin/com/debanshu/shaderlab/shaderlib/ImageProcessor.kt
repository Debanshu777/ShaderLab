package com.debanshu.shaderlab.shaderlib

expect fun applyShaderToImage(
    imageBytes: ByteArray,
    spec: ShaderSpec,
    width: Float = 0f,
    height: Float = 0f,
): ByteArray?
