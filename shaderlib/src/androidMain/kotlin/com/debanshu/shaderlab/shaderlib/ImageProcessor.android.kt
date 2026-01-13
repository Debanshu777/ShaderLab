package com.debanshu.shaderlab.shaderlib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import java.io.ByteArrayOutputStream

actual fun applyShaderToImage(
    imageBytes: ByteArray,
    spec: ShaderSpec,
    width: Float,
    height: Float
): ByteArray? {
    return try {
        val sourceBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return null
        
        val imageWidth = sourceBitmap.width
        val imageHeight = sourceBitmap.height

        val effectWidth = if (width > 0) width else imageWidth.toFloat()
        val effectHeight = if (height > 0) height else imageHeight.toFloat()

        val resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)

        val success = applyWithRuntimeShader(sourceBitmap, resultBitmap, spec, effectWidth, effectHeight)
        
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
    spec: ShaderSpec,
    width: Float,
    height: Float
): Boolean {
    return try {
        val renderEffect = createRenderEffectFromSpec(spec, width, height)

        val node = RenderNode("effect")
        node.setPosition(0, 0, source.width, source.height)
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

private fun createRenderEffectFromSpec(spec: ShaderSpec, width: Float, height: Float): RenderEffect {
    if (spec is NativeBlurSpec) {
        val radiusPx = spec.radius.coerceAtLeast(0.1f)
        return RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
    }
    
    val shader = RuntimeShader(spec.shaderCode)
    val uniforms = spec.buildUniforms(width, height)
    
    uniforms.forEach { uniform ->
        when (uniform) {
            is UniformSpec.Floats -> shader.setFloatUniform(uniform.name, uniform.values)
            is UniformSpec.Ints -> shader.setIntUniform(uniform.name, uniform.values)
        }
    }
    
    return RenderEffect.createRuntimeShaderEffect(shader, "content")
}
