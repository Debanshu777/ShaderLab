package com.debanshu.shaderlab.shaderx.factory

/**
 * JVM/Desktop implementation delegates to the shared Skia factory.
 */
public actual fun ShaderFactory.Companion.create(maxCacheSize: Int): ShaderFactory =
    SkiaShaderFactory(maxCacheSize)

/**
 * JVM/Desktop implementation delegates to the shared Skia image processor.
 */
public actual fun ImageProcessor.Companion.create(factory: ShaderFactory): ImageProcessor {
    require(factory is SkiaShaderFactory) {
        "On JVM/Desktop, factory must be created via ShaderFactory.create(). " +
            "Custom ShaderFactory implementations cannot be paired with ImageProcessor on Skia targets."
    }
    return SkiaImageProcessor(factory)
}
