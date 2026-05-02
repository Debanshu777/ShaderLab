package com.debanshu.shaderlab.shaderx

import androidx.compose.ui.graphics.Color
import com.debanshu.shaderlab.shaderx.uniform.ColorUniform
import com.debanshu.shaderlab.shaderx.uniform.FloatUniform
import com.debanshu.shaderlab.shaderx.uniform.IntUniform
import com.debanshu.shaderlab.shaderx.uniform.MatrixUniform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Edge cases for uniform types that complement [UniformTest]:
 * - [IntUniform] vec3 and vec4 constructors
 * - [FloatUniform] with direct FloatArray constructor
 * - [ColorUniform] edge cases (transparent black, white, gray)
 * - [MatrixUniform] in the uniform test context (quick sanity, full coverage in MatrixUniformTest)
 */
class UniformEdgeCaseTest {
    // ── IntUniform: vec3 and vec4 constructors ─────────────────────────────────

    @Test
    fun intUniform_vec3_createsCorrectArray() {
        val uniform = IntUniform("rgb", 255, 128, 0)
        assertEquals("rgb", uniform.name)
        assertEquals(3, uniform.values.size)
        assertEquals(255, uniform.values[0])
        assertEquals(128, uniform.values[1])
        assertEquals(0, uniform.values[2])
    }

    @Test
    fun intUniform_vec4_createsCorrectArray() {
        val uniform = IntUniform("rgba", 255, 128, 64, 32)
        assertEquals(4, uniform.values.size)
        assertEquals(255, uniform.values[0])
        assertEquals(128, uniform.values[1])
        assertEquals(64, uniform.values[2])
        assertEquals(32, uniform.values[3])
    }

    @Test
    fun intUniform_vec3_equality() {
        val u1 = IntUniform("test", 1, 2, 3)
        val u2 = IntUniform("test", 1, 2, 3)
        val u3 = IntUniform("test", 1, 2, 4)
        assertEquals(u1, u2)
        assertEquals(u1.hashCode(), u2.hashCode())
        assertNotEquals(u1, u3)
    }

    @Test
    fun intUniform_vec4_equality() {
        val u1 = IntUniform("test", 1, 2, 3, 4)
        val u2 = IntUniform("test", 1, 2, 3, 4)
        val u3 = IntUniform("test", 1, 2, 3, 5)
        assertEquals(u1, u2)
        assertNotEquals(u1, u3)
    }

    @Test
    fun intUniform_toString_containsNameAndValues() {
        val uniform = IntUniform("mode", 2, 3)
        val str = uniform.toString()
        assertTrue(str.contains("mode"))
        assertTrue(str.contains("2"))
        assertTrue(str.contains("3"))
    }

    // ── FloatUniform: direct FloatArray constructor ───────────────────────────

    @Test
    fun floatUniform_directArray_storesValuesCorrectly() {
        val values = floatArrayOf(1f, 2f, 3f, 4f)
        val uniform = FloatUniform("quad", values)
        assertEquals(4, uniform.values.size)
        assertEquals(1f, uniform.values[0])
        assertEquals(4f, uniform.values[3])
    }

    @Test
    fun floatUniform_directArray_namePreserved() {
        val uniform = FloatUniform("myUniform", floatArrayOf(0.5f))
        assertEquals("myUniform", uniform.name)
    }

    @Test
    fun floatUniform_singleViaArray_equalToConvenienceConstructor() {
        val via1 = FloatUniform("t", 0.5f)
        val via2 = FloatUniform("t", floatArrayOf(0.5f))
        assertEquals(via1, via2)
    }

    @Test
    fun floatUniform_vec2_equalToArrayConstructor() {
        val via2 = FloatUniform("t", 1f, 2f)
        val viaArray = FloatUniform("t", floatArrayOf(1f, 2f))
        assertEquals(via2, viaArray)
    }

    @Test
    fun floatUniform_equality_differsByOnlyOneName() {
        val u1 = FloatUniform("a", 1f, 2f)
        val u2 = FloatUniform("b", 1f, 2f)
        assertNotEquals(u1, u2)
    }

    // ── ColorUniform edge cases ────────────────────────────────────────────────

    @Test
    fun colorUniform_transparentBlack_allZero() {
        val uniform = ColorUniform("t", 0x00000000L) // alpha=0, r=0, g=0, b=0
        assertEquals(0f, uniform.red, 0.01f)
        assertEquals(0f, uniform.green, 0.01f)
        assertEquals(0f, uniform.blue, 0.01f)
        assertEquals(0f, uniform.alpha, 0.01f)
    }

    @Test
    fun colorUniform_pureWhite_allOne() {
        val uniform = ColorUniform("t", 0xFFFFFFFFL)
        assertEquals(1f, uniform.red, 0.01f)
        assertEquals(1f, uniform.green, 0.01f)
        assertEquals(1f, uniform.blue, 0.01f)
        assertEquals(1f, uniform.alpha, 0.01f)
    }

    @Test
    fun colorUniform_midGray_allHalf() {
        // 0xFF808080 — mid gray, alpha=1
        val uniform = ColorUniform("gray", 0xFF808080L)
        // 0x80 = 128, 128/255 ≈ 0.502
        assertEquals(128f / 255f, uniform.red, 0.005f)
        assertEquals(128f / 255f, uniform.green, 0.005f)
        assertEquals(128f / 255f, uniform.blue, 0.005f)
        assertEquals(1f, uniform.alpha, 0.01f)
    }

    @Test
    fun colorUniform_toFloatArray_order_RGBA() {
        val uniform = ColorUniform("t", 0.1f, 0.2f, 0.3f, 0.4f)
        val array = uniform.toFloatArray()
        assertEquals(4, array.size)
        assertEquals(0.1f, array[0], 0.001f) // red
        assertEquals(0.2f, array[1], 0.001f) // green
        assertEquals(0.3f, array[2], 0.001f) // blue
        assertEquals(0.4f, array[3], 0.001f) // alpha
    }

    @Test
    fun colorUniform_fromLong_andComponentConstructor_produce_sameResult() {
        // 0xFF7F3F1F: alpha=FF(1.0), R=7F(127≈0.498), G=3F(63≈0.247), B=1F(31≈0.122)
        val fromLong = ColorUniform("t", 0xFF7F3F1FL)
        val fromComponents =
            ColorUniform(
                "t",
                127f / 255f,
                63f / 255f,
                31f / 255f,
                1f,
            )
        assertEquals(fromLong.red, fromComponents.red, 0.01f)
        assertEquals(fromLong.green, fromComponents.green, 0.01f)
        assertEquals(fromLong.blue, fromComponents.blue, 0.01f)
        assertEquals(fromLong.alpha, fromComponents.alpha, 0.01f)
    }

    @Test
    fun colorUniform_fromColor_transparent_alphaZero() {
        val uniform = ColorUniform.fromColor("t", Color.Transparent)
        assertEquals(0f, uniform.alpha, 0.01f)
    }

    @Test
    fun colorUniform_fromColor_black_allZeroExceptAlpha() {
        val uniform = ColorUniform.fromColor("t", Color.Black)
        assertEquals(0f, uniform.red, 0.01f)
        assertEquals(0f, uniform.green, 0.01f)
        assertEquals(0f, uniform.blue, 0.01f)
        assertEquals(1f, uniform.alpha, 0.01f)
    }

    @Test
    fun colorUniform_defaultAlpha_isOne() {
        val uniform = ColorUniform("t", 1f, 0f, 0f) // no alpha arg
        assertEquals(1f, uniform.alpha)
    }

    @Test
    fun colorUniform_equality_differsByAlpha_notEqual() {
        val opaque = ColorUniform("t", 1f, 0f, 0f, 1f)
        val transparent = ColorUniform("t", 1f, 0f, 0f, 0f)
        assertNotEquals(opaque, transparent)
    }

    // ── MatrixUniform: sanity that it works as a Uniform ──────────────────────

    @Test
    fun matrixUniform_isSubtypeOfUniform() {
        val matrix = MatrixUniform.identity3x3("transform")
        // Compile-time check: MatrixUniform extends Uniform
        val asUniform: com.debanshu.shaderlab.shaderx.uniform.Uniform = matrix
        assertEquals("transform", asUniform.name)
    }

    @Test
    fun matrixUniform_canBeIncludedInUniformList() {
        val uniforms: List<com.debanshu.shaderlab.shaderx.uniform.Uniform> =
            listOf(
                FloatUniform("intensity", 1f),
                MatrixUniform.identity4x4("mvp"),
                IntUniform("mode", 0),
            )
        assertEquals(3, uniforms.size)
        assertEquals("mvp", uniforms[1].name)
    }
}
