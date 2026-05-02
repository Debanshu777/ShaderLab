package com.debanshu.shaderlab.shaderx.effect

import androidx.compose.runtime.Stable
import com.debanshu.shaderlab.shaderx.parameter.ParameterSpec
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue

/**
 * Base interface for all shader effects in the library.
 *
 * This interface defines the common contract for shader effects, including
 * identification, display information, and parameter management.
 *
 * Implementations should be immutable data classes that return new instances
 * when parameters are modified via [withParameter] or [withTypedParameter].
 *
 * @see RuntimeShaderEffect for effects using custom shader code
 * @see NativeEffect for platform-optimized effects
 */
@Stable
public sealed interface ShaderEffect {
    /**
     * Unique identifier for this effect type.
     */
    public val id: String

    /**
     * Human-readable name for display in UI.
     */
    public val displayName: String

    /**
     * List of configurable parameters for this effect.
     * Each parameter defines its type, range, and default value.
     */
    public val parameters: List<ParameterSpec>

    /**
     * Creates a new effect instance with the specified typed parameter value updated.
     *
     * This is the canonical parameter update method. Each implementation must handle
     * all its declared parameter types and throw [IllegalArgumentException] if the
     * supplied value type is incompatible with the target parameter.
     *
     * @param parameterId The ID of the parameter to update
     * @param value The new typed value for the parameter
     * @return A new [ShaderEffect] instance with the updated parameter
     * @throws IllegalArgumentException if [value]'s type is incompatible with [parameterId]
     */
    public fun withTypedParameter(
        parameterId: String,
        value: ParameterValue,
    ): ShaderEffect

    /**
     * Creates a new effect instance with the specified float parameter value updated.
     *
     * Delegates to [withTypedParameter] with a [ParameterValue.FloatValue] wrapper.
     *
     * @param parameterId The ID of the parameter to update
     * @param value The new float value for the parameter
     * @return A new [ShaderEffect] instance with the updated parameter
     */
    public fun withParameter(
        parameterId: String,
        value: Float,
    ): ShaderEffect = withTypedParameter(parameterId, ParameterValue.FloatValue(value))

    /**
     * Gets the current value of a parameter.
     *
     * @param parameterId The ID of the parameter to retrieve
     * @return The current value, or the default value if not set
     */
    public fun getParameterValue(parameterId: String): Float =
        parameters.find { it.id == parameterId }?.defaultValue ?: 0f

    /**
     * Gets the current typed value of a parameter.
     *
     * @param parameterId The ID of the parameter to retrieve
     * @return The current typed value, or the default typed value if not set
     */
    public fun getTypedParameterValue(parameterId: String): ParameterValue? =
        parameters.find { it.id == parameterId }?.getTypedDefaultValue()
}
