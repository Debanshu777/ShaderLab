package com.debanshu.shaderlab.shaderlib

actual fun applyShaderToImage(
    imageBytes: ByteArray,
    spec: ShaderSpec,
    width: Float,
    height: Float
): ByteArray? {
    return SkiaImageProcessor.process(imageBytes, spec, width, height)
}
