package com.debanshu.shaderlab.imagelib

import androidx.compose.runtime.Composable

/**
 * Result of an image picking operation.
 */
sealed class PickResult {
    /**
     * Image was successfully picked.
     * @param uri Platform-specific URI or path to the image
     * @param bytes The raw image bytes
     */
    data class Success(val uri: String, val bytes: ByteArray) : PickResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return uri == other.uri && bytes.contentEquals(other.bytes)
        }
        
        override fun hashCode(): Int {
            var result = uri.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
    
    /**
     * User cancelled the image picker.
     */
    data object Cancelled : PickResult()
    
    /**
     * An error occurred while picking the image.
     */
    data class Error(val message: String) : PickResult()
}

/**
 * Interface for launching the platform-specific image picker.
 */
interface ImagePickerLauncher {
    /**
     * Launch the image picker UI.
     */
    fun launch()
}

/**
 * Remember a platform-specific image picker launcher.
 * 
 * @param onResult Callback invoked when the picking operation completes
 * @return An ImagePickerLauncher that can be used to trigger the picker
 */
@Composable
expect fun rememberImagePickerLauncher(onResult: (PickResult) -> Unit): ImagePickerLauncher

