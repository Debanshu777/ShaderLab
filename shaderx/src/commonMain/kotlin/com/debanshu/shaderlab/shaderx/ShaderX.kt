@file:Suppress("unused")

package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.effect.ShaderEffect
import com.debanshu.shaderlab.shaderx.effect.impl.ChromaticAberrationEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GradientEffect
import com.debanshu.shaderlab.shaderx.effect.impl.GrayscaleEffect
import com.debanshu.shaderlab.shaderx.effect.impl.InvertEffect
import com.debanshu.shaderlab.shaderx.effect.impl.NativeBlurEffect
import com.debanshu.shaderlab.shaderx.effect.impl.PixelateEffect
import com.debanshu.shaderlab.shaderx.effect.impl.SepiaEffect
import com.debanshu.shaderlab.shaderx.effect.impl.VignetteEffect
import com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect

/**
 * ShaderX - A Kotlin Multiplatform library for GPU shader effects.
 *
 * ## Quick Start
 *
 * ### Apply a simple effect to an image
 * ```kotlin
 * Image(
 *     painter = painterResource("photo.png"),
 *     modifier = Modifier.shaderEffect(GrayscaleEffect())
 * )
 * ```
 *
 * ### Use animated effects
 * ```kotlin
 * val waveEffect = rememberShaderEffect(WaveEffect(amplitude = 10f))
 * Image(
 *     painter = painterResource("photo.png"),
 *     modifier = Modifier.shaderEffect(waveEffect)
 * )
 * ```
 *
 * ## Supported Platforms
 * - Android (API 33+) using AGSL
 * - iOS using Skia
 * - Desktop/JVM using Skia
 *
 * @see [com.debanshu.shaderlab.shaderx.factory.ShaderFactory] for manual effect creation
 */
public object ShaderX {
    /**
     * Cached list of built-in effects.
     *
     * Effects are instantiated once and reused to avoid repeated allocations.
     * For effects that need fresh instances (e.g., animated effects with different
     * initial states), create them directly instead of using this list.
     */
    private val cachedBuiltInEffects: List<ShaderEffect> by lazy {
        listOf(
            GrayscaleEffect(),
            SepiaEffect(),
            GradientEffect(),
            VignetteEffect(),
            PixelateEffect(),
            ChromaticAberrationEffect(),
            InvertEffect(),
            WaveEffect(),
            NativeBlurEffect(),
        )
    }

    /**
     * Returns all built-in effects.
     *
     * **Shared list:** Every call returns the same cached [List] instance — the list itself
     * is unmodifiable. The effect instances inside are [androidx.compose.runtime.Immutable]:
     * calling [com.debanshu.shaderlab.shaderx.effect.ShaderEffect.withParameter] or
     * [com.debanshu.shaderlab.shaderx.effect.ShaderEffect.withTypedParameter] always returns a
     * new instance and never mutates the cached one. It is safe to use these instances as starting
     * points for parameter customisation.
     *
     * **Animated effects:** [com.debanshu.shaderlab.shaderx.effect.impl.WaveEffect] in the list
     * starts with `time = 0f` and `animate = true`. Pass it through
     * [com.debanshu.shaderlab.shaderx.compose.rememberShaderEffect] in a composable to drive
     * the animation loop.
     *
     * For effects that need different initial parameters, create them directly instead of
     * modifying items from this list:
     * ```kotlin
     * val softBlur = NativeBlurEffect(radius = 3f)      // custom default
     * val builtIn  = ShaderX.builtInEffects()           // shared cached instances
     * ```
     */
    public fun builtInEffects(): List<ShaderEffect> = cachedBuiltInEffects
}
