package com.debanshu.shaderlab.shaderlib

actual fun applyShaderToImage(imageBytes: ByteArray, effect: ShaderEffectType): ByteArray? {
    return SkiaImageProcessor.process(imageBytes, effect)
}
