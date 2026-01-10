package com.debanshu.shaderlab.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debanshu.shaderlab.imagelib.ExportConfig
import com.debanshu.shaderlab.imagelib.ExportResult
import com.debanshu.shaderlab.imagelib.ImageExporter
import com.debanshu.shaderlab.imagelib.PickResult
import com.debanshu.shaderlab.shaderlib.ShaderEffectType
import com.debanshu.shaderlab.shaderlib.areShadersSupported
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Represents the source of an image in the app.
 */
sealed class ImageSource {
    /**
     * A bundled sample image from app resources.
     */
    data class Bundled(val resourceName: String) : ImageSource()
    
    /**
     * An image picked from the device.
     */
    data class Picked(
        val path: String,
        val bytes: ByteArray?
    ) : ImageSource() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Picked) return false
            return path == other.path && bytes.contentEquals(other.bytes)
        }
        
        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + (bytes?.contentHashCode() ?: 0)
            return result
        }
    }
}

/**
 * UI state for the ShaderLab app.
 */
data class ShaderLabUiState(
    val selectedImage: ImageSource = ImageSource.Bundled("sample_landscape"),
    val activeEffect: ShaderEffectType? = null,
    val showBeforeAfter: Boolean = false,
    val isDarkTheme: Boolean = true,
    val sampleImages: List<String> = listOf(
        "sample_landscape",
        "sample_portrait",
        "sample_nature",
        "sample_abstract"
    ),
    val pickedImages: List<ImageSource.Picked> = emptyList(),
    val shadersSupported: Boolean = true,
    val animationTime: Float = 0f,
    val isExporting: Boolean = false,
    val exportMessage: String? = null
)

/**
 * ViewModel for the ShaderLab app.
 */
class ShaderLabViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShaderLabUiState(
        shadersSupported = areShadersSupported()
    ))
    val uiState: StateFlow<ShaderLabUiState> = _uiState.asStateFlow()
    
    /**
     * Select an image to display and apply effects to.
     */
    fun selectImage(source: ImageSource) {
        _uiState.update { it.copy(selectedImage = source) }
    }
    
    /**
     * Set the active shader effect.
     */
    fun setActiveEffect(effect: ShaderEffectType?) {
        _uiState.update { it.copy(activeEffect = effect) }
    }
    
    /**
     * Update a parameter of the active effect.
     */
    fun updateEffectParameter(parameterIndex: Int, value: Float) {
        _uiState.update { state ->
            val currentEffect = state.activeEffect ?: return@update state
            val updatedEffect = currentEffect.withParameter(parameterIndex, value)
            state.copy(activeEffect = updatedEffect)
        }
    }
    
    /**
     * Toggle before/after comparison mode.
     */
    fun toggleBeforeAfter() {
        _uiState.update { it.copy(showBeforeAfter = !it.showBeforeAfter) }
    }
    
    /**
     * Toggle dark/light theme.
     */
    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }
    
    /**
     * Handle the result of an image pick operation.
     */
    fun onImagePicked(result: PickResult) {
        when (result) {
            is PickResult.Success -> {
                val picked = ImageSource.Picked(result.uri, result.bytes)
                _uiState.update { state ->
                    state.copy(
                        pickedImages = state.pickedImages + picked,
                        selectedImage = picked
                    )
                }
            }
            is PickResult.Error -> {
                // Could show an error message
            }
            is PickResult.Cancelled -> {
                // User cancelled, do nothing
            }
        }
    }
    
    /**
     * Update the animation time for animated effects.
     */
    fun updateAnimationTime(time: Float) {
        _uiState.update { state ->
            val currentEffect = state.activeEffect
            if (currentEffect is ShaderEffectType.WaveDistortion && currentEffect.animate) {
                state.copy(
                    animationTime = time,
                    activeEffect = currentEffect.withTime(time)
                )
            } else {
                state.copy(animationTime = time)
            }
        }
    }
    
    /**
     * Export the current image with applied effect.
     */
    fun exportImage(exporter: ImageExporter, imageBytes: ByteArray?) {
        if (imageBytes == null) {
            _uiState.update { it.copy(exportMessage = "No image to export") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            
            val randomSuffix = Random.nextInt(100000, 999999)
            val fileName = "shaderlab_$randomSuffix"
            
            val result = exporter.exportImage(imageBytes, fileName, ExportConfig())
            
            val message = when (result) {
                is ExportResult.Success -> "Image saved successfully"
                is ExportResult.Error -> "Export failed: ${result.message}"
                is ExportResult.NotSupported -> "Export not supported on this platform"
            }
            
            _uiState.update { it.copy(isExporting = false, exportMessage = message) }
        }
    }
    
    /**
     * Clear the export message.
     */
    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
    
    /**
     * Set whether shaders are supported on this device.
     */
    fun setShadersSupported(supported: Boolean) {
        _uiState.update { it.copy(shadersSupported = supported) }
    }
}

