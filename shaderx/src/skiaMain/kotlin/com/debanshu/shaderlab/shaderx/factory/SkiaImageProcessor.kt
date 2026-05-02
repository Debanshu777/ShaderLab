package com.debanshu.shaderlab.shaderx.factory

import com.debanshu.shaderlab.shaderx.ShaderConstants
import com.debanshu.shaderlab.shaderx.effect.BlurEffect
import com.debanshu.shaderlab.shaderx.effect.CompositeEffect
import com.debanshu.shaderlab.shaderx.effect.NativeEffect
import com.debanshu.shaderlab.shaderx.effect.RuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ShaderEffect
import com.debanshu.shaderlab.shaderx.result.ShaderError
import com.debanshu.shaderlab.shaderx.result.ShaderResult
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Surface

/**
 * Skia-based implementation of [ImageProcessor] for iOS and Desktop platforms.
 *
 * Shader compilation is delegated to the provided [SkiaShaderFactory] so that
 * the compiled RuntimeEffect cache is shared with the Compose rendering path.
 *
 * All per-call Skia [Surface], [Paint], and [Image] objects are closed after use
 * to release their off-heap native memory.
 */
internal class SkiaImageProcessor(
    private val factory: SkiaShaderFactory,
) : ImageProcessor {
    override fun process(
        imageBytes: ByteArray,
        effect: ShaderEffect,
        width: Float,
        height: Float,
    ): ShaderResult<ByteArray> {
        return try {
            val image = Image.makeFromEncoded(imageBytes)
            val imageWidth = image.width
            val imageHeight = image.height

            val effectWidth = if (width > 0) width else imageWidth.toFloat()
            val effectHeight = if (height > 0) height else imageHeight.toFloat()

            val imageFilterResult = createImageFilter(effect, effectWidth, effectHeight)
            val imageFilter = when (imageFilterResult) {
                is ShaderResult.Success -> imageFilterResult.value

                is ShaderResult.Failure -> {
                    image.close()
                    return ShaderResult.failure(imageFilterResult.error)
                }
            }

            // Use explicit try/finally instead of .use {} — Skia's Managed objects have
            // close() on all targets but do not implement AutoCloseable on WasmJS.
            val surface = Surface.makeRasterN32Premul(imageWidth, imageHeight)
            var result: ByteArray? = null
            try {
                val paint = Paint()
                try {
                    paint.imageFilter = imageFilter
                    surface.canvas.drawImage(image, 0f, 0f, paint)
                } finally {
                    paint.close()
                }
                val snapshot = surface.makeImageSnapshot()
                val data = snapshot.encodeToData(EncodedImageFormat.PNG)
                snapshot.close()
                result = data?.bytes?.also { data.close() }
            } finally {
                surface.close()
            }

            image.close()

            if (result != null) {
                ShaderResult.success(result)
            } else {
                ShaderResult.failure(
                    ShaderError.ProcessingError("Failed to encode result image"),
                )
            }
        } catch (e: Exception) {
            ShaderResult.failure(
                ShaderError.ProcessingError("Image processing failed: ${e.message}", e),
            )
        }
    }

    private fun createImageFilter(
        effect: ShaderEffect,
        width: Float,
        height: Float,
    ): ShaderResult<ImageFilter> =
        when (effect) {
            is BlurEffect -> {
                ShaderResult.runCatching {
                    val radiusPx = effect.radius.coerceAtLeast(ShaderConstants.MIN_BLUR_RADIUS)
                    ImageFilter.makeBlur(radiusPx, radiusPx, FilterTileMode.CLAMP)
                }
            }

            is NativeEffect -> {
                ShaderResult.failure(
                    ShaderError.UnsupportedEffect(
                        "Native effect not supported for offline processing: ${effect::class.simpleName}",
                        effect.id,
                    ),
                )
            }

            is RuntimeShaderEffect -> {
                try {
                    // Reuse the factory's cached RuntimeEffect — no recompilation
                    val runtimeEffect = factory.getOrCreateEffect(effect.shaderSource)
                    val builder = RuntimeShaderBuilder(runtimeEffect)
                    val uniforms = effect.buildUniforms(width, height)
                    SkiaShaderFactory.applyUniforms(builder, uniforms)
                    ShaderResult.success(
                        ImageFilter.makeRuntimeShader(
                            builder,
                            ShaderConstants.CONTENT_UNIFORM_NAME,
                            null,
                        ),
                    )
                } catch (e: Exception) {
                    ShaderResult.failure(
                        ShaderError.CompilationError(
                            "Failed to compile shader for image processing: ${e.message}",
                            effect.shaderSource,
                        ),
                    )
                }
            }

            is CompositeEffect -> {
                ShaderResult.failure(
                ShaderError.UnsupportedEffect(
                    "Composite effects are not supported for offline image processing",
                    effect.id,
                ),
            )
            }
        }
}
