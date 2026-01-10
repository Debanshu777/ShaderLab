package com.debanshu.shaderlab.shaderlib

/**
 * Sealed class representing all available shader effect types.
 * Each effect is self-describing with its display name and configurable parameters.
 */
sealed class ShaderEffectType {
    abstract val displayName: String
    abstract val parameters: List<EffectParameter>
    
    /**
     * Returns a new instance with the parameter at the given index updated to the new value.
     */
    abstract fun withParameter(index: Int, value: Float): ShaderEffectType
    
    /**
     * Grayscale effect using ITU-R BT.601 luminance formula.
     */
    data class Grayscale(val intensity: Float = 1f) : ShaderEffectType() {
        override val displayName = "Grayscale"
        override val parameters = listOf(
            EffectParameter.PercentageParam("Intensity", intensity)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(intensity = value)
            else -> this
        }
    }
    
    /**
     * Sepia tone effect with adjustable intensity.
     */
    data class Sepia(val intensity: Float = 1f) : ShaderEffectType() {
        override val displayName = "Sepia"
        override val parameters = listOf(
            EffectParameter.PercentageParam("Intensity", intensity)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(intensity = value)
            else -> this
        }
    }
    
    /**
     * Color inversion effect (negative).
     */
    data object ColorInversion : ShaderEffectType() {
        override val displayName = "Invert"
        override val parameters = emptyList<EffectParameter>()
        override fun withParameter(index: Int, value: Float) = this
    }
    
    /**
     * Vignette effect with adjustable radius and intensity.
     */
    data class Vignette(
        val radius: Float = 0.5f,
        val intensity: Float = 0.5f
    ) : ShaderEffectType() {
        override val displayName = "Vignette"
        override val parameters = listOf(
            EffectParameter.PercentageParam("Radius", radius),
            EffectParameter.PercentageParam("Intensity", intensity)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(radius = value)
            1 -> copy(intensity = value)
            else -> this
        }
    }
    
    /**
     * Blur effect using native blur for better performance.
     */
    data class Blur(val radius: Float = 10f) : ShaderEffectType() {
        override val displayName = "Blur"
        override val parameters = listOf(
            EffectParameter.PixelParam("Radius", 0f..50f, radius)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(radius = value)
            else -> this
        }
    }
    
    /**
     * Pixelation/mosaic effect.
     */
    data class Pixelation(val pixelSize: Float = 10f) : ShaderEffectType() {
        override val displayName = "Pixelate"
        override val parameters = listOf(
            EffectParameter.PixelParam("Pixel Size", 1f..100f, pixelSize)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(pixelSize = value.coerceAtLeast(1f))
            else -> this
        }
    }
    
    /**
     * Chromatic aberration (RGB channel offset) effect.
     */
    data class ChromaticAberration(val offset: Float = 5f) : ShaderEffectType() {
        override val displayName = "Chromatic"
        override val parameters = listOf(
            EffectParameter.PixelParam("Offset", 0f..20f, offset)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(offset = value)
            else -> this
        }
    }
    
    /**
     * Wave distortion effect with optional animation.
     */
    data class WaveDistortion(
        val amplitude: Float = 10f,
        val frequency: Float = 5f,
        val animate: Boolean = true,
        val time: Float = 0f
    ) : ShaderEffectType() {
        override val displayName = "Wave"
        override val parameters = listOf(
            EffectParameter.PixelParam("Amplitude", 0f..50f, amplitude),
            EffectParameter.FloatParam("Frequency", 1f..20f, frequency) { formatFloat(it, 1) },
            EffectParameter.ToggleParam("Animate", animate)
        )
        override fun withParameter(index: Int, value: Float) = when (index) {
            0 -> copy(amplitude = value)
            1 -> copy(frequency = value)
            2 -> copy(animate = value > 0.5f)
            else -> this
        }
        
        /**
         * Returns a copy with updated time for animation.
         */
        fun withTime(newTime: Float) = copy(time = newTime)
    }
    
    companion object {
        /**
         * Returns all available effect types with their default parameters.
         */
        fun allEffects(): List<ShaderEffectType> = listOf(
            Grayscale(),
            Sepia(),
            ColorInversion,
            Vignette(),
            Blur(),
            Pixelation(),
            ChromaticAberration(),
            WaveDistortion()
        )
    }
}

