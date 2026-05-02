package com.debanshu.shaderlab.shaderx.effect

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.parameter.ParameterSpec
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.BooleanValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.ColorValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.FloatValue

/**
 * Typed accessor descriptor for a single parameter on an [AbstractRuntimeShaderEffect].
 *
 * Captures everything needed to:
 * - Declare the parameter in [ParameterSpec] form.
 * - Read the current value from an effect instance.
 * - Write a new value and return an updated effect instance.
 * - Convert to/from [ParameterValue].
 *
 * Use the factory helpers ([floatHandler], [colorHandler], [toggleHandler]) rather than
 * constructing this class directly.
 *
 * @param T The Kotlin type of the parameter's value (e.g. [Float], [Long], [Boolean]).
 */
internal class ParamHandler<T : Any>(
    val spec: ParameterSpec,
    val read: (AbstractRuntimeShaderEffect) -> T,
    val write: (AbstractRuntimeShaderEffect, T) -> AbstractRuntimeShaderEffect,
    val typeOf: (ParameterValue) -> T?,
    val toFloat: (T) -> Float,
    val toTyped: (T) -> ParameterValue,
)

/**
 * Abstract base class for [RuntimeShaderEffect] implementations.
 *
 * Eliminates the per-effect dispatcher boilerplate by routing all parameter reads and writes
 * through a [parameterHandlers] map of typed [ParamHandler] instances. Subclasses only need to:
 *
 * 1. Declare their parameters via `parameterHandlers`.
 * 2. Implement `shaderSource` and `buildUniforms`.
 * 3. (Optional) Override `id` / `displayName`.
 *
 * Parameter range validation via [ParameterSpec.validateValue] is applied automatically in
 * [withTypedParameter] — out-of-range float values are clamped; wrong-type values throw
 * [IllegalArgumentException].
 */
@Immutable
public abstract class AbstractRuntimeShaderEffect : RuntimeShaderEffect {
    /** Ordered map of parameter-ID → handler, declaring this effect's configurable parameters. */
    internal abstract val parameterHandlers: Map<String, ParamHandler<*>>

    final override val parameters: List<ParameterSpec>
        get() = parameterHandlers.values.map { it.spec }

    override fun withTypedParameter(
        parameterId: String,
        value: ParameterValue,
    ): AbstractRuntimeShaderEffect {
        val handler =
            parameterHandlers[parameterId]
                ?: return this // unknown ID → silently ignore (no declared parameter with this id)

        @Suppress("UNCHECKED_CAST")
        val h = handler as ParamHandler<Any>

        // Extract strongly-typed value — null means wrong ParameterValue subtype
        val typed =
            h.typeOf(value)
                ?: throw IllegalArgumentException(
                    "Effect '$id' parameter '$parameterId' does not accept ${value::class.simpleName}",
                )

        // Clamp / validate via ParameterSpec
        val validated = handler.spec.validateValue(FloatValue(h.toFloat(typed)))
        val finalTyped = if (validated != null) h.typeOf(validated) ?: typed else typed

        return h.write(this, finalTyped)
    }

    final override fun getParameterValue(parameterId: String): Float {
        val handler = parameterHandlers[parameterId] ?: return 0f

        @Suppress("UNCHECKED_CAST")
        val h = handler as ParamHandler<Any>
        return h.toFloat(h.read(this))
    }

    final override fun getTypedParameterValue(parameterId: String): ParameterValue? {
        val handler = parameterHandlers[parameterId] ?: return null

        @Suppress("UNCHECKED_CAST")
        val h = handler as ParamHandler<Any>
        return h.toTyped(h.read(this))
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Factory helpers for common handler types
// ──────────────────────────────────────────────────────────────────────────────

/** Handler for [com.debanshu.shaderlab.shaderx.parameter.FloatParameter]-backed parameters. */
internal fun <S : AbstractRuntimeShaderEffect> floatHandler(
    spec: ParameterSpec,
    read: (S) -> Float,
    write: (S, Float) -> S,
): ParamHandler<Float> =
    ParamHandler(
        spec = spec,
        read = {
            @Suppress("UNCHECKED_CAST")
            read(it as S)
        },
        write = { e, v ->
            @Suppress("UNCHECKED_CAST")
            write(e as S, v)
        },
        typeOf = { (it as? FloatValue)?.value },
        toFloat = { it },
        toTyped = { FloatValue(it) },
    )

/** Handler for [com.debanshu.shaderlab.shaderx.parameter.ColorParameter]-backed parameters. */
internal fun <S : AbstractRuntimeShaderEffect> colorHandler(
    spec: ParameterSpec,
    read: (S) -> Long,
    write: (S, Long) -> S,
): ParamHandler<Long> =
    ParamHandler(
        spec = spec,
        read = {
            @Suppress("UNCHECKED_CAST")
            read(it as S)
        },
        write = { e, v ->
            @Suppress("UNCHECKED_CAST")
            write(e as S, v)
        },
        typeOf = { (it as? ColorValue)?.color },
        toFloat = { 0f }, // color has no float representation
        toTyped = { ColorValue(it) },
    )

/** Handler for [com.debanshu.shaderlab.shaderx.parameter.ToggleParameter]-backed parameters. */
internal fun <S : AbstractRuntimeShaderEffect> toggleHandler(
    spec: ParameterSpec,
    read: (S) -> Boolean,
    write: (S, Boolean) -> S,
): ParamHandler<Boolean> =
    ParamHandler(
        spec = spec,
        read = {
            @Suppress("UNCHECKED_CAST")
            read(it as S)
        },
        write = { e, v ->
            @Suppress("UNCHECKED_CAST")
            write(e as S, v)
        },
        typeOf = { pv ->
            when (pv) {
                is BooleanValue -> pv.enabled

                is FloatValue -> pv.value > 0.5f

                // coerce float to boolean
                is ColorValue -> null
            }
        },
        toFloat = { if (it) 1f else 0f },
        toTyped = { BooleanValue(it) },
    )
