package com.debanshu.shaderlab.shaderx.factory

public actual fun ShaderFactory.Companion.create(maxCacheSize: Int): ShaderFactory =
    SkiaShaderFactory(maxCacheSize)

public actual fun ImageProcessor.Companion.create(factory: ShaderFactory): ImageProcessor {
    require(factory is SkiaShaderFactory) {
        "On iOS, factory must be created via ShaderFactory.create(). " +
            "Custom ShaderFactory implementations cannot be paired with ImageProcessor on Skia targets."
    }
    return SkiaImageProcessor(factory)
}
