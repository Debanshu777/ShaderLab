package com.debanshu.shaderlab.imagelib

/**
 * Configuration for image export.
 */
data class ExportConfig(
    val format: ImageFormat = ImageFormat.PNG,
    val quality: Int = 100 // 0-100 for JPEG
)

/**
 * Supported image export formats.
 */
enum class ImageFormat {
    PNG,
    JPEG,
    WEBP
}

/**
 * Result of an image export operation.
 */
sealed class ExportResult {
    /**
     * Image was successfully exported.
     * @param path The path where the image was saved (may be null on some platforms)
     */
    data class Success(val path: String?) : ExportResult()
    
    /**
     * An error occurred during export.
     */
    data class Error(val message: String) : ExportResult()
    
    /**
     * Image export is not supported on this platform.
     */
    data object NotSupported : ExportResult()
}

/**
 * Interface for exporting images to the file system or gallery.
 */
interface ImageExporter {
    /**
     * Export an image to the file system or gallery.
     * 
     * @param imageBytes The raw image bytes to export
     * @param fileName The desired filename (without extension)
     * @param config Export configuration (format, quality)
     * @return The result of the export operation
     */
    suspend fun exportImage(
        imageBytes: ByteArray,
        fileName: String,
        config: ExportConfig = ExportConfig()
    ): ExportResult
    
    /**
     * Whether image export is supported on this platform.
     */
    val isSupported: Boolean
}

/**
 * Create a platform-specific image exporter.
 */
expect fun createImageExporter(): ImageExporter

