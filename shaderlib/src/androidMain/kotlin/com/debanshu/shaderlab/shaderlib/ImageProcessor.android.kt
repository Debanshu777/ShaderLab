package com.debanshu.shaderlab.shaderlib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import java.io.ByteArrayOutputStream

actual fun applyShaderToImage(imageBytes: ByteArray, effect: ShaderEffectType): ByteArray? {
    return try {
        val sourceBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return null
        
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val source = effect.toShaderSource(width.toFloat(), height.toFloat())

        val success = applyWithRuntimeShader(sourceBitmap, resultBitmap, source, width, height)
        
        if (!success) {
            sourceBitmap.recycle()
            resultBitmap.recycle()
            return imageBytes
        }

        val outputStream = ByteArrayOutputStream()
        resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        sourceBitmap.recycle()
        resultBitmap.recycle()
        
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun applyWithRuntimeShader(
    source: Bitmap,
    result: Bitmap,
    shaderSource: ShaderSource,
    width: Int,
    height: Int
): Boolean {
    return try {
        val renderEffect = createRenderEffect(shaderSource)

        val node = RenderNode("effect")
        node.setPosition(0, 0, width, height)
        node.setRenderEffect(renderEffect)
        
        val canvas = node.beginRecording()
        canvas.drawBitmap(source, 0f, 0f, null)
        node.endRecording()

        val resultCanvas = Canvas(result)
        if (resultCanvas.isHardwareAccelerated) {
            resultCanvas.drawRenderNode(node)
        } else {
            resultCanvas.drawBitmap(source, 0f, 0f, null)
        }
        
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun createRenderEffect(source: ShaderSource): RenderEffect {
    if (source is ShaderSource.Blur) {
        val radiusPx = source.radius.coerceAtLeast(0.1f)
        return RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
    }
    
    val shader = RuntimeShader(source.code)
    
    source.uniforms.forEach { uniform ->
        when (uniform) {
            is UniformSpec.Float1 -> shader.setFloatUniform(uniform.name, uniform.value)
            is UniformSpec.Float2 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y)
            is UniformSpec.Float3 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y, uniform.z)
            is UniformSpec.Float4 -> shader.setFloatUniform(uniform.name, uniform.x, uniform.y, uniform.z, uniform.w)
        }
    }
    
    return RenderEffect.createRuntimeShaderEffect(shader, "content")
}
