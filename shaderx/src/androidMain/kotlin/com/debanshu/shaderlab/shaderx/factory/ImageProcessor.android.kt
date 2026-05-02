package com.debanshu.shaderlab.shaderx.factory

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RenderNode
import com.debanshu.shaderlab.shaderx.effect.ShaderEffect
import com.debanshu.shaderlab.shaderx.result.ShaderError
import com.debanshu.shaderlab.shaderx.result.ShaderResult
import java.io.ByteArrayOutputStream
import android.graphics.RenderEffect as AndroidRenderEffect

/**
 * Android implementation of [ImageProcessor] for applying shader effects to images.
 *
 * Shader compilation is delegated to the provided [ShaderFactory] so that
 * the compiled shader cache is shared with the Compose rendering path.
 */
internal class AndroidImageProcessor(
    private val factory: ShaderFactory,
) : ImageProcessor {
    override fun process(
        imageBytes: ByteArray,
        effect: ShaderEffect,
        width: Float,
        height: Float,
    ): ShaderResult<ByteArray> {
        return try {
            val sourceBitmap =
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return ShaderResult.failure(
                        ShaderError.ProcessingError("Failed to decode image bytes"),
                    )

            val imageWidth = sourceBitmap.width
            val imageHeight = sourceBitmap.height
            val effectWidth = if (width > 0) width else imageWidth.toFloat()
            val effectHeight = if (height > 0) height else imageHeight.toFloat()

            val renderEffectResult = factory.createRenderEffect(effect, effectWidth, effectHeight)
            val composeRenderEffect =
                when (renderEffectResult) {
                    is ShaderResult.Success -> renderEffectResult.value
                    is ShaderResult.Failure -> return ShaderResult.failure(renderEffectResult.error)
                }
            val androidRenderEffect = composeRenderEffect.asAndroidRenderEffect()

            val resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            val success = applyEffectToBitmap(sourceBitmap, resultBitmap, androidRenderEffect)

            if (!success) {
                sourceBitmap.recycle()
                resultBitmap.recycle()
                return ShaderResult.failure(
                    ShaderError.ProcessingError(
                        "RenderNode-based image processing requires a hardware-accelerated canvas. " +
                            "Software bitmaps always produce a software canvas — this path is " +
                            "unsupported. Effect '${effect.id}' was not applied.",
                    ),
                )
            }

            val outputStream = ByteArrayOutputStream()
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            sourceBitmap.recycle()
            resultBitmap.recycle()

            ShaderResult.success(outputStream.toByteArray())
        } catch (e: Exception) {
            ShaderResult.failure(
                ShaderError.ProcessingError("Image processing failed: ${e.message}", e),
            )
        }
    }

    private fun applyEffectToBitmap(
        source: Bitmap,
        result: Bitmap,
        renderEffect: AndroidRenderEffect,
    ): Boolean =
        try {
            val node = RenderNode("effect")
            node.setPosition(0, 0, source.width, source.height)
            node.setRenderEffect(renderEffect)

            val canvas = node.beginRecording()
            canvas.drawBitmap(source, 0f, 0f, null)
            node.endRecording()

            val resultCanvas = Canvas(result)
            if (!resultCanvas.isHardwareAccelerated) {
                // RenderNode.drawRenderNode requires a hardware-accelerated canvas.
                // Software bitmaps (Bitmap.createBitmap) always produce a software canvas —
                // returning false so the caller emits ShaderResult.Failure rather than
                // silently returning unprocessed source bytes as a "success".
                return false
            }
            resultCanvas.drawRenderNode(node)
            true
        } catch (e: Exception) {
            false
        }
}

public actual fun ImageProcessor.Companion.create(factory: ShaderFactory): ImageProcessor = AndroidImageProcessor(factory)
