package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.uniform.MatrixUniform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Full coverage of [MatrixUniform]: constructors, rowMajor factory, identity helpers,
 * validation (invalid sizes), equality/hashCode, and toString.
 */
class MatrixUniformTest {
    // ── Direct column-major array constructor ────────────────────────────────

    @Test
    fun matrixUniform_mat2_accepts4Elements() {
        val m = MatrixUniform("transform", floatArrayOf(1f, 0f, 0f, 1f))
        assertEquals("transform", m.name)
        assertEquals(4, m.values.size)
    }

    @Test
    fun matrixUniform_mat3_accepts9Elements() {
        val m = MatrixUniform("m", floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f))
        assertEquals(9, m.values.size)
    }

    @Test
    fun matrixUniform_mat4_accepts16Elements() {
        // 4x4 identity in column-major
        val m = MatrixUniform("m", FloatArray(16) { if (it % 5 == 0) 1f else 0f })
        assertEquals(16, m.values.size)
    }

    @Test
    fun matrixUniform_size3_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", floatArrayOf(1f, 2f, 3f))
        }
    }

    @Test
    fun matrixUniform_size5_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", FloatArray(5))
        }
    }

    @Test
    fun matrixUniform_size8_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", FloatArray(8))
        }
    }

    @Test
    fun matrixUniform_emptyArray_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", FloatArray(0))
        }
    }

    @Test
    fun matrixUniform_size10_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", FloatArray(10))
        }
    }

    @Test
    fun matrixUniform_size17_throwsIllegalArgument() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform("m", FloatArray(17))
        }
    }

    @Test
    fun matrixUniform_storesValuesInOrder() {
        val values = floatArrayOf(1f, 2f, 3f, 4f)
        val m = MatrixUniform("t", values)
        assertEquals(1f, m.values[0])
        assertEquals(2f, m.values[1])
        assertEquals(3f, m.values[2])
        assertEquals(4f, m.values[3])
    }

    // ── rowMajor factory ─────────────────────────────────────────────────────

    @Test
    fun rowMajor_2x2_identity_samAsColumnMajorIdentity() {
        // Row-major [1,0,0,1] identity → column-major [1,0,0,1] (symmetric)
        val m = MatrixUniform.rowMajor("m", 1f, 0f, 0f, 1f)
        assertEquals(4, m.values.size)
        assertEquals(1f, m.values[0]) // col0, row0
        assertEquals(0f, m.values[1]) // col0, row1
        assertEquals(0f, m.values[2]) // col1, row0
        assertEquals(1f, m.values[3]) // col1, row1
    }

    @Test
    fun rowMajor_2x2_asymmetric_transposesCorrectly() {
        // Row-major: [a=1, b=2] / [c=3, d=4]
        // Column-major: col0=[a,c]=[1,3], col1=[b,d]=[2,4]
        val m = MatrixUniform.rowMajor("m", 1f, 2f, 3f, 4f)
        assertEquals(1f, m.values[0]) // col0 row0 = a
        assertEquals(3f, m.values[1]) // col0 row1 = c
        assertEquals(2f, m.values[2]) // col1 row0 = b
        assertEquals(4f, m.values[3]) // col1 row1 = d
    }

    @Test
    fun rowMajor_3x3_identity_matchesIdentity3x3() {
        val fromRowMajor =
            MatrixUniform.rowMajor(
                "m",
                1f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                1f,
            )
        val identity = MatrixUniform.identity3x3("m")
        assertEquals(fromRowMajor, identity)
    }

    @Test
    fun rowMajor_3x3_asymmetric_transposesCorrectly() {
        // Row-major 3x3:
        // [1 2 3]
        // [4 5 6]
        // [7 8 9]
        // Column-major: col0=[1,4,7], col1=[2,5,8], col2=[3,6,9]
        val m = MatrixUniform.rowMajor("m", 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        // col0
        assertEquals(1f, m.values[0])
        assertEquals(4f, m.values[1])
        assertEquals(7f, m.values[2])
        // col1
        assertEquals(2f, m.values[3])
        assertEquals(5f, m.values[4])
        assertEquals(8f, m.values[5])
        // col2
        assertEquals(3f, m.values[6])
        assertEquals(6f, m.values[7])
        assertEquals(9f, m.values[8])
    }

    @Test
    fun rowMajor_4x4_identity_matchesIdentity4x4() {
        val fromRowMajor =
            MatrixUniform.rowMajor(
                "m",
                1f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                1f,
            )
        val identity = MatrixUniform.identity4x4("m")
        assertEquals(fromRowMajor, identity)
    }

    @Test
    fun rowMajor_invalidSize3_throws() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform.rowMajor("m", 1f, 2f, 3f)
        }
    }

    @Test
    fun rowMajor_invalidSize7_throws() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform.rowMajor("m", *FloatArray(7))
        }
    }

    @Test
    fun rowMajor_invalidSize0_throws() {
        assertFailsWith<IllegalArgumentException> {
            MatrixUniform.rowMajor("m")
        }
    }

    // ── Identity factory helpers ──────────────────────────────────────────────

    @Test
    fun identity2x2_hasCorrectValues() {
        val m = MatrixUniform.identity2x2("id2")
        assertEquals(4, m.values.size)
        // Column-major: [1,0,0,1]
        assertEquals(1f, m.values[0])
        assertEquals(0f, m.values[1])
        assertEquals(0f, m.values[2])
        assertEquals(1f, m.values[3])
    }

    @Test
    fun identity3x3_hasCorrectValues() {
        val m = MatrixUniform.identity3x3("id3")
        assertEquals(9, m.values.size)
        // Diagonal at indices 0, 4, 8
        for (i in 0 until 9) {
            val expected = if (i % 4 == 0) 1f else 0f
            assertEquals(expected, m.values[i], "values[$i]")
        }
    }

    @Test
    fun identity4x4_hasCorrectValues() {
        val m = MatrixUniform.identity4x4("id4")
        assertEquals(16, m.values.size)
        // Diagonal at indices 0, 5, 10, 15
        for (i in 0 until 16) {
            val expected = if (i % 5 == 0) 1f else 0f
            assertEquals(expected, m.values[i], "values[$i]")
        }
    }

    @Test
    fun identity2x2_preservesName() {
        val m = MatrixUniform.identity2x2("myMatrix")
        assertEquals("myMatrix", m.name)
    }

    // ── equals / hashCode ─────────────────────────────────────────────────────

    @Test
    fun matrixUniform_equality_sameNameAndValues_areEqual() {
        val m1 = MatrixUniform("t", floatArrayOf(1f, 0f, 0f, 1f))
        val m2 = MatrixUniform("t", floatArrayOf(1f, 0f, 0f, 1f))
        assertEquals(m1, m2)
        assertEquals(m1.hashCode(), m2.hashCode())
    }

    @Test
    fun matrixUniform_equality_differentValues_notEqual() {
        val m1 = MatrixUniform("t", floatArrayOf(1f, 0f, 0f, 1f))
        val m2 = MatrixUniform("t", floatArrayOf(1f, 0f, 0f, 2f))
        assertNotEquals(m1, m2)
    }

    @Test
    fun matrixUniform_equality_differentName_notEqual() {
        val m1 = MatrixUniform("a", floatArrayOf(1f, 0f, 0f, 1f))
        val m2 = MatrixUniform("b", floatArrayOf(1f, 0f, 0f, 1f))
        assertNotEquals(m1, m2)
    }

    @Test
    fun matrixUniform_selfEquality() {
        val m = MatrixUniform("t", floatArrayOf(1f, 0f, 0f, 1f))
        assertEquals(m, m)
    }

    @Test
    fun matrixUniform_differentSizeMatrices_notEqual() {
        // mat2 vs mat3 with different element counts cannot be equal
        val mat2 = MatrixUniform.identity2x2("id")
        val mat3 = MatrixUniform.identity3x3("id")
        assertNotEquals(mat2, mat3)
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Test
    fun matrixUniform_toString_containsName() {
        val m = MatrixUniform("mvp", floatArrayOf(1f, 0f, 0f, 1f))
        assertTrue(m.toString().contains("mvp"))
    }

    @Test
    fun matrixUniform_toString_containsSize() {
        val m = MatrixUniform("t", FloatArray(9) { it.toFloat() })
        assertTrue(m.toString().contains("9"))
    }
}
