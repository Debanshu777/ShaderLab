package com.debanshu.shaderlab.shaderlib

/**
 * Self-describing shader source that encapsulates both the shader code
 * and its uniform specifications. This allows platform factories to
 * apply shaders generically without knowing implementation details.
 */
sealed class ShaderSource {
    /**
     * The AGSL/SkSL shader source code.
     */
    abstract val code: String
    
    /**
     * List of uniforms to bind to the shader.
     */
    abstract val uniforms: List<UniformSpec>
    
    /**
     * Grayscale effect with adjustable intensity.
     */
    data class Grayscale(val intensity: Float) : ShaderSource() {
        override val code: String = ShaderConstants.GRAYSCALE
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float1("intensity", intensity)
        )
    }
    
    /**
     * Sepia tone effect with adjustable intensity.
     */
    data class Sepia(val intensity: Float) : ShaderSource() {
        override val code: String = ShaderConstants.SEPIA
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float1("intensity", intensity)
        )
    }
    
    /**
     * Color inversion (negative) effect.
     */
    data object ColorInversion : ShaderSource() {
        override val code: String = ShaderConstants.COLOR_INVERSION
        override val uniforms: List<UniformSpec> = emptyList()
    }
    
    /**
     * Vignette effect with adjustable radius and intensity.
     */
    data class Vignette(
        val width: Float,
        val height: Float,
        val radius: Float,
        val intensity: Float
    ) : ShaderSource() {
        override val code: String = ShaderConstants.VIGNETTE
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float2("resolution", width, height),
            UniformSpec.Float1("radius", radius),
            UniformSpec.Float1("intensity", intensity)
        )
    }
    
    /**
     * Native blur effect. Uses platform-optimized blur rather than custom shader.
     */
    data class Blur(val radius: Float) : ShaderSource() {
        override val code: String = "" // Uses native blur, not custom shader
        override val uniforms: List<UniformSpec> = emptyList()
    }
    
    /**
     * Pixelation/mosaic effect.
     */
    data class Pixelation(
        val width: Float,
        val height: Float,
        val pixelSize: Float
    ) : ShaderSource() {
        override val code: String = ShaderConstants.PIXELATION
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float2("resolution", width, height),
            UniformSpec.Float1("pixelSize", pixelSize.coerceAtLeast(1f))
        )
    }
    
    /**
     * Chromatic aberration (RGB channel offset) effect.
     */
    data class ChromaticAberration(
        val width: Float,
        val height: Float,
        val offset: Float
    ) : ShaderSource() {
        override val code: String = ShaderConstants.CHROMATIC_ABERRATION
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float2("resolution", width, height),
            UniformSpec.Float1("offset", offset)
        )
    }
    
    /**
     * Wave distortion effect with animation support.
     */
    data class WaveDistortion(
        val width: Float,
        val height: Float,
        val amplitude: Float,
        val frequency: Float,
        val time: Float
    ) : ShaderSource() {
        override val code: String = ShaderConstants.WAVE_DISTORTION
        override val uniforms: List<UniformSpec> = listOf(
            UniformSpec.Float2("resolution", width, height),
            UniformSpec.Float1("amplitude", amplitude),
            UniformSpec.Float1("frequency", frequency),
            UniformSpec.Float1("time", time)
        )
    }
}

/**
 * Extension function to convert ShaderEffectType to ShaderSource.
 * This bridges the user-facing effect types with the internal shader representation.
 */
fun ShaderEffectType.toShaderSource(width: Float, height: Float): ShaderSource {
    return when (this) {
        is ShaderEffectType.Grayscale -> ShaderSource.Grayscale(intensity)
        is ShaderEffectType.Sepia -> ShaderSource.Sepia(intensity)
        is ShaderEffectType.ColorInversion -> ShaderSource.ColorInversion
        is ShaderEffectType.Vignette -> ShaderSource.Vignette(width, height, radius, intensity)
        is ShaderEffectType.Blur -> ShaderSource.Blur(radius)
        is ShaderEffectType.Pixelation -> ShaderSource.Pixelation(width, height, pixelSize)
        is ShaderEffectType.ChromaticAberration -> ShaderSource.ChromaticAberration(width, height, offset)
        is ShaderEffectType.WaveDistortion -> ShaderSource.WaveDistortion(width, height, amplitude, frequency, time)
    }
}
