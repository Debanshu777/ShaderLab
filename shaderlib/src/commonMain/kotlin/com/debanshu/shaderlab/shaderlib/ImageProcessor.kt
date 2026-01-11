package com.debanshu.shaderlab.shaderlib

/**
 * Applies the specified shader effect to an image and returns the processed bytes.
 * 
 * @param imageBytes The raw image bytes (PNG, JPEG, WebP, etc.)
 * @param effect The shader effect to apply
 * @return The processed image bytes as PNG, or null if processing fails
 */
expect fun applyShaderToImage(imageBytes: ByteArray, effect: ShaderEffectType): ByteArray?

