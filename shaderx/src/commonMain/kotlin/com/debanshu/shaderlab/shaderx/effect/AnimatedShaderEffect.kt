package com.debanshu.shaderlab.shaderx.effect

import androidx.compose.runtime.Stable
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue

/**
 * Interface for shader effects that support animation over time.
 *
 * Animated effects receive a `time` value that can be used in shader code
 * to create dynamic, time-varying effects like waves, pulses, or transitions.
 *
 * ## Usage
 * The animation loop should call [withTime] with incrementing time values:
 *
 * ```kotlin
 * LaunchedEffect(effect) {
 *     if (effect is AnimatedShaderEffect && effect.isAnimating) {
 *         while (isActive) {
 *             val time = withFrameMillis { it / 1000f }
 *             viewModel.updateEffect(effect.withTime(time))
 *         }
 *     }
 * }
 * ```
 *
 * ## Equality contract — implementations MUST exclude [time]
 *
 * [rememberShaderEffect] drives the animation coroutine via
 * `LaunchedEffect(effect.id, effect.isAnimating)`. Those keys are stable across frames.
 * If [time] is included in [equals] or [hashCode], every call to [withTime] produces a
 * new effect identity. Any caller that keys a `LaunchedEffect` on the effect instance
 * directly will tear down and recreate the coroutine every ~16 ms — causing visible
 * animation glitches and unnecessary allocations.
 *
 * Implementations must override [equals] and [hashCode] to exclude [time]:
 *
 * ```kotlin
 * // ✅ Correct — time excluded
 * override fun equals(other: Any?): Boolean {
 *     if (other !is MyAnimatedEffect) return false
 *     return speed == other.speed && amplitude == other.amplitude
 *     // time NOT included
 * }
 * override fun hashCode(): Int = 31 * speed.hashCode() + amplitude.hashCode()
 *
 * // ❌ Wrong — data class default equality includes time
 * data class MyAnimatedEffect(val speed: Float, override val time: Float = 0f) : ...
 * ```
 */
@Stable
public interface AnimatedShaderEffect : RuntimeShaderEffect {
    /**
     * Whether this effect is currently animating.
     * When false, the effect behaves as a static shader.
     */
    public val isAnimating: Boolean

    /**
     * Current animation time in seconds.
     * Used in shader code for time-based calculations.
     */
    public val time: Float

    /**
     * Creates a new effect instance with the updated time value.
     *
     * @param newTime The new time value in seconds
     * @return A new [AnimatedShaderEffect] instance with the updated time
     */
    public fun withTime(newTime: Float): AnimatedShaderEffect

    override fun withTypedParameter(
        parameterId: String,
        value: ParameterValue,
    ): AnimatedShaderEffect
}
