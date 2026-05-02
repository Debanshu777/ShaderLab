package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractNativeEffect
import com.debanshu.shaderlab.shaderx.effect.BlurEffect
import com.debanshu.shaderlab.shaderx.effect.NativeParamHandler
import com.debanshu.shaderlab.shaderx.effect.nativeFloatHandler
import com.debanshu.shaderlab.shaderx.parameter.ParameterValue
import com.debanshu.shaderlab.shaderx.parameter.PixelParameter

/**
 * Native blur effect using platform-optimized implementations.
 *
 * On Android, uses `RenderEffect.createBlurEffect()`.
 * On iOS/Desktop, uses Skia's `ImageFilter.makeBlur()`.
 *
 * @property radius Blur radius in pixels (minimum 0.1)
 */
@Immutable
public data class NativeBlurEffect(
    override val radius: Float = 10f,
) : AbstractNativeEffect(),
    BlurEffect {
    override val id: String = ID
    override val displayName: String = "Blur"

    override val parameterHandlers: Map<String, NativeParamHandler<*>> =
        mapOf(
            PARAM_RADIUS to
                nativeFloatHandler<NativeBlurEffect>(
                    spec =
                        PixelParameter(
                            id = PARAM_RADIUS,
                            label = "Radius",
                            range = 0f..50f,
                            defaultValue = 10f,
                        ),
                    read = { it.radius },
                    write = { e, v -> e.copy(radius = v) },
                ),
        )

    // Covariant return type — base class dispatch handles the actual logic.
    override fun withTypedParameter(
        parameterId: String,
        value: ParameterValue,
    ): NativeBlurEffect = super.withTypedParameter(parameterId, value) as NativeBlurEffect

    // Covariant return type for the Float convenience path.
    override fun withParameter(
        parameterId: String,
        value: Float,
    ): NativeBlurEffect = withTypedParameter(parameterId, ParameterValue.FloatValue(value))

    public companion object {
        public const val ID: String = "blur"
        public const val PARAM_RADIUS: String = "radius"
    }
}
