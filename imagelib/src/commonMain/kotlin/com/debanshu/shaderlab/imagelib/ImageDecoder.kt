package com.debanshu.shaderlab.imagelib

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decode raw image bytes into a Compose ImageBitmap.
 * 
 * @param bytes The raw image bytes (PNG, JPEG, WebP, etc.)
 * @return An ImageBitmap, or null if decoding fails
 */
expect fun decodeImageBytes(bytes: ByteArray): ImageBitmap?

