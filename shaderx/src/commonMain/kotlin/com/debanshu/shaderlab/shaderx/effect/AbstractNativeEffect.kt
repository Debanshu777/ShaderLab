package com.debanshu.shaderlab.shaderx.effect

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.parameter.ParameterSpec
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.BooleanValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.ColorValue
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue.FloatValue

/**
 * Typed accessor descriptor for a single parameter on an [AbstractNativeEffect].
 *
 * Mirrors [ParamHandler] for runtime-shader effects — captures everything needed to
 * declare, read, write, and convert a parameter without per-effect `when` dispatch.
 *
 * Use the factory helpers ([nativeFloatHandler]) rather than constructing directly.
 *
 * @param T The Kotlin type of the parameter's value (e.g. [Float]).
 */
internal class NativeParamHandler<T : Any>(
    val spec: ParameterSpec,
    val read: (AbstractNativeEffect) -> T,
    val write: (AbstractNativeEffect, T) -> AbstractNativeEffect,
    val typeOf: (ParameterValue) -> T?,
    val toFloat: (T) -> Float,
    val toTyped: (T) -> ParameterValue,
)

/**
 * Abstract base class for [NativeEffect] implementations.
 *
 * Eliminates per-effect parameter dispatch boilerplate by routing all reads and writes
 * through a [parameterHandlers] map of typed [NativeParamHandler] instances.
 * Subclasses only need to:
 *
 * 1. Declare their parameters via `parameterHandlers`.
 * 2. Override `id`, `displayName`, and any effect-specific properties.
 * 3. Provide a one-line `withTypedParameter` override for the covariant return type:
 *    ```kotlin
 *    override fun withTypedParameter(parameterId: String, value: ParameterValue): MyEffect =
 *        super.withTypedParameter(parameterId, value) as MyEffect
 *    ```
 *
 * Parameter range validation via [ParameterSpec.validateValue] is applied automatically
 * in [withTypedParameter] — out-of-range float values are clamped; wrong-type values
 * throw [IllegalArgumentException].
 */
@Immutable
public abstract class AbstractNativeEffect : NativeEffect {
    /** Ordered map of parameter-ID → handler, declaring this effect's configurable parameters. */
    internal abstract val parameterHandlers: Map<String, NativeParamHandler<*>>

    final override val parameters: List<ParameterSpec>
        get() = parameterHandlers.values.map { it.spec }

    override fun withTypedParameter(
        parameterId: String,
        value: ParameterValue,
    ): AbstractNativeEffect {
        val handler =
            parameterHandlers[parameterId]
                ?: return this // unknown ID → silently ignore

        @Suppress("UNCHECKED_CAST")
        val h = handler as NativeParamHandler<Any>

        val typed =
            h.typeOf(value)
                ?: throw IllegalArgumentException(
                    "Effect '$id' parameter '$parameterId' does not accept ${value::class.simpleName}",
                )

        val validated = handler.spec.validateValue(FloatValue(h.toFloat(typed)))
        val finalTyped = if (validated != null) h.typeOf(validated) ?: typed else typed

        return h.write(this, finalTyped)
    }

    final override fun getParameterValue(parameterId: String): Float {
        val handler = parameterHandlers[parameterId] ?: return 0f

        @Suppress("UNCHECKED_CAST")
        val h = handler as NativeParamHandler<Any>
        return h.toFloat(h.read(this))
    }

    final override fun getTypedParameterValue(parameterId: String): ParameterValue? {
        val handler = parameterHandlers[parameterId] ?: return null

        @Suppress("UNCHECKED_CAST")
        val h = handler as NativeParamHandler<Any>
        return h.toTyped(h.read(this))
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Factory helpers for common handler types
// ──────────────────────────────────────────────────────────────────────────────

/** Handler for [com.debanshu.shaderlab.shaderx.parameter.FloatParameter]-backed native parameters. */
internal fun <N : AbstractNativeEffect> nativeFloatHandler(
    spec: ParameterSpec,
    read: (N) -> Float,
    write: (N, Float) -> N,
): NativeParamHandler<Float> =
    NativeParamHandler(
        spec = spec,
        read = {
            @Suppress("UNCHECKED_CAST")
            read(it as N)
        },
        write = { e, v ->
            @Suppress("UNCHECKED_CAST")
            write(e as N, v)
        },
        typeOf = { (it as? FloatValue)?.value },
        toFloat = { it },
        toTyped = { FloatValue(it) },
    )

/** Handler for [com.debanshu.shaderlab.shaderx.parameter.ToggleParameter]-backed native parameters. */
internal fun <N : AbstractNativeEffect> nativeToggleHandler(
    spec: ParameterSpec,
    read: (N) -> Boolean,
    write: (N, Boolean) -> N,
): NativeParamHandler<Boolean> =
    NativeParamHandler(
        spec = spec,
        read = {
            @Suppress("UNCHECKED_CAST")
            read(it as N)
        },
        write = { e, v ->
            @Suppress("UNCHECKED_CAST")
            write(e as N, v)
        },
        typeOf = { pv ->
            when (pv) {
                is BooleanValue -> pv.enabled
                is FloatValue -> pv.value > 0.5f
                is ColorValue -> null
            }
        },
        toFloat = { if (it) 1f else 0f },
        toTyped = { BooleanValue(it) },
    )
