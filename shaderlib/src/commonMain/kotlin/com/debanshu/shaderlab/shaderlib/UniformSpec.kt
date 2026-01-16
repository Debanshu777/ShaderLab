package com.debanshu.shaderlab.shaderlib

sealed class UniformSpec {
    abstract val name: String

    data class Floats(
        override val name: String,
        val values: FloatArray,
    ) : UniformSpec() {
        constructor(name: String, v1: Float) : this(name, floatArrayOf(v1))
        constructor(name: String, v1: Float, v2: Float) : this(name, floatArrayOf(v1, v2))
        constructor(name: String, v1: Float, v2: Float, v3: Float) : this(name, floatArrayOf(v1, v2, v3))
        constructor(name: String, v1: Float, v2: Float, v3: Float, v4: Float) : this(name, floatArrayOf(v1, v2, v3, v4))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Floats) return false
            return name == other.name && values.contentEquals(other.values)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + values.contentHashCode()
            return result
        }
    }

    data class Ints(
        override val name: String,
        val values: IntArray,
    ) : UniformSpec() {
        constructor(name: String, v1: Int) : this(name, intArrayOf(v1))
        constructor(name: String, v1: Int, v2: Int) : this(name, intArrayOf(v1, v2))
        constructor(name: String, v1: Int, v2: Int, v3: Int) : this(name, intArrayOf(v1, v2, v3))
        constructor(name: String, v1: Int, v2: Int, v3: Int, v4: Int) : this(name, intArrayOf(v1, v2, v3, v4))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Ints) return false
            return name == other.name && values.contentEquals(other.values)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + values.contentHashCode()
            return result
        }
    }
}
