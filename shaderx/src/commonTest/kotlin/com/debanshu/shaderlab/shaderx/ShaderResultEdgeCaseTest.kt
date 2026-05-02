package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.result.ShaderError
import com.debanshu.shaderlab.shaderx.result.ShaderException
import com.debanshu.shaderlab.shaderx.result.ShaderResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Edge cases for [ShaderResult] and [ShaderError] that complement [ResultTest]:
 * - flatMap on failure propagates the error
 * - recover on success returns the same instance
 * - getOrElse on success returns the value, not the default
 * - chained combinators
 * - runCatching with null-message exceptions
 * - all [ShaderError] subtypes exercise their properties
 */
class ShaderResultEdgeCaseTest {
    // ── flatMap edge cases ────────────────────────────────────────────────────

    @Test
    fun flatMap_failure_propagatesError() {
        val error = ShaderError.CompilationError("compile fail")
        val result: ShaderResult<Int> = ShaderResult.failure(error)
        val chained = result.flatMap { ShaderResult.success(it * 2) }

        assertTrue(chained is ShaderResult.Failure)
        assertSame(error, (chained as ShaderResult.Failure).error)
    }

    @Test
    fun flatMap_success_thenFailure_propagatesSecondError() {
        val result =
            ShaderResult
                .success(10)
                .flatMap { ShaderResult.failure<Int>(ShaderError.CompilationError("inner fail")) }

        assertTrue(result is ShaderResult.Failure)
        assertTrue((result as ShaderResult.Failure).error is ShaderError.CompilationError)
    }

    @Test
    fun flatMap_chainThreeOperations_allSuccess_returnsLast() {
        val result =
            ShaderResult
                .success(1)
                .flatMap { ShaderResult.success(it + 1) } // 2
                .flatMap { ShaderResult.success(it * 3) } // 6
                .flatMap { ShaderResult.success(it - 1) } // 5
        assertEquals(5, result.getOrNull())
    }

    @Test
    fun flatMap_firstOperationFails_restSkipped() {
        var secondCalled = false
        var thirdCalled = false
        ShaderResult
            .failure<Int>(ShaderError.PlatformNotSupported("nope"))
            .flatMap {
                secondCalled = true
                ShaderResult.success(it + 1)
            }.flatMap {
                thirdCalled = true
                ShaderResult.success(it + 1)
            }
        assertTrue(!secondCalled && !thirdCalled)
    }

    // ── recover edge cases ────────────────────────────────────────────────────

    @Test
    fun recover_onSuccess_returnsSameInstance() {
        val original: ShaderResult<String> = ShaderResult.success("value")
        val recovered = original.recover { "fallback" }
        assertSame(original, recovered)
    }

    @Test
    fun recover_onFailure_convertsToSuccess() {
        val result: ShaderResult<Int> =
            ShaderResult
                .failure<Int>(ShaderError.PlatformNotSupported("no gpu"))
                .recover { -1 }
        assertTrue(result is ShaderResult.Success)
        assertEquals(-1, (result as ShaderResult.Success).value)
    }

    @Test
    fun recover_receivesTheError() {
        var receivedError: ShaderError? = null
        val error = ShaderError.Unknown("oops")
        ShaderResult
            .failure<String>(error)
            .recover { e ->
                receivedError = e
                "default"
            }
        assertSame(error, receivedError)
    }

    // ── getOrElse edge cases ──────────────────────────────────────────────────

    @Test
    fun getOrElse_onSuccess_returnsValue_notDefault() {
        val result = ShaderResult.success(42)
        val value = result.getOrElse { -1 }
        assertEquals(42, value)
    }

    @Test
    fun getOrElse_onFailure_invokesDefaultWithError() {
        var receivedError: ShaderError? = null
        val error = ShaderError.CompilationError("bad shader")
        val result: ShaderResult<Int> = ShaderResult.failure(error)

        result.getOrElse { e ->
            receivedError = e
            -1
        }

        assertSame(error, receivedError)
    }

    // ── map edge cases ────────────────────────────────────────────────────────

    @Test
    fun map_transformsTypeCorrectly() {
        val result: ShaderResult<Int> = ShaderResult.success(5)
        val mapped: ShaderResult<String> = result.map { it.toString() }
        assertEquals("5", mapped.getOrNull())
    }

    @Test
    fun map_onFailure_preservesErrorType() {
        val error = ShaderError.UnsupportedEffect("not supported", "my_effect")
        val result: ShaderResult<Int> = ShaderResult.failure(error)
        val mapped = result.map { it * 2 }
        assertTrue(mapped is ShaderResult.Failure)
        assertSame(error, (mapped as ShaderResult.Failure).error)
    }

    // ── fold edge cases ───────────────────────────────────────────────────────

    @Test
    fun fold_success_callsOnSuccess_notOnFailure() {
        var onSuccessCalled = false
        var onFailureCalled = false
        ShaderResult.success("x").fold(
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true },
        )
        assertTrue(onSuccessCalled)
        assertTrue(!onFailureCalled)
    }

    @Test
    fun fold_failure_callsOnFailure_notOnSuccess() {
        var onSuccessCalled = false
        var onFailureCalled = false
        ShaderResult.failure<String>(ShaderError.PlatformNotSupported("no")).fold(
            onSuccess = { onSuccessCalled = true },
            onFailure = { onFailureCalled = true },
        )
        assertTrue(!onSuccessCalled)
        assertTrue(onFailureCalled)
    }

    @Test
    fun fold_returnsCorrectValue() {
        val successResult =
            ShaderResult.success(10).fold(
                onSuccess = { it * 2 },
                onFailure = { -1 },
            )
        val failureResult =
            ShaderResult.failure<Int>(ShaderError.PlatformNotSupported("nope")).fold(
                onSuccess = { it * 2 },
                onFailure = { -1 },
            )
        assertEquals(20, successResult)
        assertEquals(-1, failureResult)
    }

    // ── onSuccess / onFailure chaining ────────────────────────────────────────

    @Test
    fun onSuccess_returnsSameResult_forChaining() {
        val original: ShaderResult<String> = ShaderResult.success("value")
        val returned = original.onSuccess { /* side effect */ }
        assertSame(original, returned)
    }

    @Test
    fun onFailure_returnsSameResult_forChaining() {
        val original: ShaderResult<String> = ShaderResult.failure(ShaderError.Unknown("x"))
        val returned = original.onFailure { /* side effect */ }
        assertSame(original, returned)
    }

    @Test
    fun chain_onSuccessAndOnFailure_onlySuccessExecutes() {
        var successValue: Int? = null
        var failureMessage: String? = null

        ShaderResult
            .success(99)
            .onSuccess { successValue = it }
            .onFailure { failureMessage = it.message }

        assertEquals(99, successValue)
        assertNull(failureMessage)
    }

    @Test
    fun chain_onSuccessAndOnFailure_onlyFailureExecutes() {
        var successValue: Int? = null
        var failureMessage: String? = null

        ShaderResult
            .failure<Int>(ShaderError.CompilationError("shader error"))
            .onSuccess { successValue = it }
            .onFailure { failureMessage = it.message }

        assertNull(successValue)
        assertEquals("shader error", failureMessage)
    }

    // ── runCatching edge cases ────────────────────────────────────────────────

    @Test
    fun runCatching_exceptionWithNullMessage_wrapsAsUnknown() {
        val result =
            ShaderResult.runCatching<String> {
                throw RuntimeException(null as String?) // null message
            }
        assertTrue(result is ShaderResult.Failure)
        assertTrue((result as ShaderResult.Failure).error is ShaderError.Unknown)
        // Message falls back to "Unknown error"
        assertEquals("Unknown error", result.error.message)
    }

    @Test
    fun runCatching_exceptionWithMessage_preservesMessage() {
        val result =
            ShaderResult.runCatching<Int> {
                throw IllegalStateException("state error")
            }
        assertTrue(result is ShaderResult.Failure)
        assertEquals("state error", (result as ShaderResult.Failure).error.message)
    }

    // ── ShaderError subtypes ──────────────────────────────────────────────────

    @Test
    fun shaderError_compilationError_properties() {
        val error = ShaderError.CompilationError("bad syntax", "uniform float x;")
        assertEquals("bad syntax", error.message)
        assertEquals("uniform float x;", error.shaderSource)
    }

    @Test
    fun shaderError_compilationError_nullSource() {
        val error = ShaderError.CompilationError("bad shader")
        assertNull(error.shaderSource)
    }

    @Test
    fun shaderError_unsupportedEffect_properties() {
        val error = ShaderError.UnsupportedEffect("not on this platform", "my_effect_id")
        assertEquals("not on this platform", error.message)
        assertEquals("my_effect_id", error.effectId)
    }

    @Test
    fun shaderError_processingError_properties() {
        val cause = RuntimeException("io error")
        val error = ShaderError.ProcessingError("processing failed", cause)
        assertEquals("processing failed", error.message)
        assertSame(cause, error.cause)
    }

    @Test
    fun shaderError_processingError_nullCause() {
        val error = ShaderError.ProcessingError("no cause")
        assertNull(error.cause)
    }

    @Test
    fun shaderError_platformNotSupported_properties() {
        val error = ShaderError.PlatformNotSupported("Wasm doesn't support X")
        assertEquals("Wasm doesn't support X", error.message)
    }

    @Test
    fun shaderError_unknown_properties() {
        val cause = Throwable("underlying")
        val error = ShaderError.Unknown("unknown problem", cause)
        assertEquals("unknown problem", error.message)
        assertSame(cause, error.cause)
    }

    @Test
    fun shaderError_unknown_nullCause() {
        val error = ShaderError.Unknown("no cause")
        assertNull(error.cause)
    }

    // ── ShaderException wrapping ──────────────────────────────────────────────

    @Test
    fun getOrThrow_failure_throwsShaderException_withCorrectError() {
        val error = ShaderError.CompilationError("broken")
        val result: ShaderResult<Int> = ShaderResult.failure(error)
        val ex = assertFailsWith<ShaderException> { result.getOrThrow() }
        assertSame(error, ex.error)
        assertEquals("broken", ex.message)
    }

    @Test
    fun shaderException_messageMatchesError() {
        val error = ShaderError.UnsupportedEffect("unsupported", "my_effect")
        val ex = ShaderException(error)
        assertEquals("unsupported", ex.message)
        assertSame(error, ex.error)
    }

    // ── Companion factory methods ──────────────────────────────────────────────

    @Test
    fun companion_success_createsSuccessResult() {
        val result = ShaderResult.success(123)
        assertTrue(result is ShaderResult.Success)
        assertEquals(123, (result as ShaderResult.Success).value)
    }

    @Test
    fun companion_failure_createsFailureResult() {
        val error = ShaderError.PlatformNotSupported("test")
        val result = ShaderResult.failure<Int>(error)
        assertTrue(result is ShaderResult.Failure)
        assertSame(error, (result as ShaderResult.Failure).error)
    }
}
