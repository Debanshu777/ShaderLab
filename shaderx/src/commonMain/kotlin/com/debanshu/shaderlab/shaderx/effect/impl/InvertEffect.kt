package com.debanshu.shaderlab.shaderx.effect.impl

import androidx.compose.runtime.Immutable
import com.debanshu.shaderlab.shaderx.effect.AbstractRuntimeShaderEffect
import com.debanshu.shaderlab.shaderx.effect.ParamHandler
import com.debanshu.shaderlab.shaderx.uniform.Uniform

/**
 * Inverts all colors in the image.
 *
 * Each color channel is inverted: newValue = 1.0 - originalValue
 * Alpha channel is preserved.
 *
 * Has no configurable parameters.
 */
@Immutable
public data class InvertEffect(
    @Suppress("unused") private val _unused: Unit = Unit,
) : AbstractRuntimeShaderEffect() {
    override val id: String = ID
    override val displayName: String = "Invert"

    override val shaderSource: String =
        """
        uniform shader content;

        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            return half4(1.0 - color.rgb, color.a);
        }
        """.trimIndent()

    override val parameterHandlers: Map<String, ParamHandler<*>> = emptyMap()

    override fun buildUniforms(
        width: Float,
        height: Float,
    ): List<Uniform> = emptyList()

    public companion object {
        public const val ID: String = "color_inversion"

        /** Singleton instance for convenience — equivalent to `InvertEffect()`. */
        public val Default: InvertEffect = InvertEffect()
    }
}
