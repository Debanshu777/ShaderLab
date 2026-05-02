package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.factory.LruCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Edge cases for [LruCache] that complement the existing [CacheTest]:
 * - clear on empty cache (no-op, no throw)
 * - getOrPut when compute throws (miss stays uncached)
 * - Very large maxSize (no eviction)
 * - Eviction ordering via clear callback
 * - Stable return value on first insert regardless of eviction
 */
class LruCacheEdgeCaseTest {
    // ── clear on empty cache ──────────────────────────────────────────────────

    @Test
    fun cache_clear_onEmptyCache_isNoOp() {
        val cache = LruCache<String, String>(maxSize = 5)
        cache.clear() // must not throw
        assertEquals(0, cache.size)
    }

    @Test
    fun cache_clear_onEmptyCache_onEvictNotCalled() {
        val cache = LruCache<String, String>(maxSize = 5)
        var callCount = 0
        cache.clear(onEvict = { callCount++ })
        assertEquals(0, callCount)
    }

    // ── compute throwing ──────────────────────────────────────────────────────

    @Test
    fun cache_getOrPut_computeThrows_exceptionPropagates() {
        val cache = LruCache<String, String>(maxSize = 5)
        assertFailsWith<RuntimeException> {
            cache.getOrPut("key") { throw RuntimeException("compute failed") }
        }
    }

    @Test
    fun cache_getOrPut_afterComputeThrows_keyStillMissing() {
        val cache = LruCache<String, String>(maxSize = 5)
        try {
            cache.getOrPut("key") { throw RuntimeException("failed") }
        } catch (_: RuntimeException) {
        }

        // Key was not cached due to compute failure; next call must recompute
        var recomputed = false
        cache.getOrPut("key") {
            recomputed = true
            "recovered"
        }
        assertTrue(recomputed, "Key should require recomputation after compute failure")
    }

    // ── Large maxSize prevents eviction ───────────────────────────────────────

    @Test
    fun cache_largeCap_noEviction() {
        val cache = LruCache<Int, Int>(maxSize = 1000)
        repeat(100) { cache.getOrPut(it) { it * 2 } }
        assertEquals(100, cache.size, "All 100 entries should fit without eviction")
    }

    @Test
    fun cache_largeCap_allEntriesRetained() {
        val cache = LruCache<Int, Int>(maxSize = 1000)
        repeat(100) { cache.getOrPut(it) { it * 2 } }

        // All entries should be accessible without recomputing
        for (i in 0 until 100) {
            var recomputed = false
            val value =
                cache.getOrPut(i) {
                    recomputed = true
                    -1
                }
            assertFalse(recomputed, "Entry $i should still be cached")
            assertEquals(i * 2, value)
        }
    }

    // ── Eviction order via onEvict callback ───────────────────────────────────

    @Test
    fun cache_clear_onEvict_calledForAllEntries() {
        val cache = LruCache<String, Int>(maxSize = 5)
        cache.getOrPut("a") { 1 }
        cache.getOrPut("b") { 2 }
        cache.getOrPut("c") { 3 }

        val evicted = mutableListOf<Int>()
        cache.clear(onEvict = { evicted.add(it) })

        assertEquals(3, evicted.size)
        assertTrue(1 in evicted)
        assertTrue(2 in evicted)
        assertTrue(3 in evicted)
        assertEquals(0, cache.size)
    }

    @Test
    fun cache_clear_afterClear_acceptsNewEntries() {
        val cache = LruCache<String, String>(maxSize = 5)
        cache.getOrPut("a") { "value_a" }
        cache.clear()

        // Should be able to insert again after clear
        val result = cache.getOrPut("a") { "new_value" }
        assertEquals("new_value", result)
        assertEquals(1, cache.size)
    }

    // ── Return value correctness ──────────────────────────────────────────────

    @Test
    fun cache_getOrPut_alwaysReturnsComputedValue_onMiss() {
        val cache = LruCache<String, String>(maxSize = 3)
        val result = cache.getOrPut("key") { "computed" }
        assertEquals("computed", result)
    }

    @Test
    fun cache_getOrPut_returnsCachedValue_onHit() {
        val cache = LruCache<String, Int>(maxSize = 5)
        cache.getOrPut("x") { 42 }
        val result = cache.getOrPut("x") { 99 } // should not recompute
        assertEquals(42, result)
    }

    // ── Repeated same-key inserts ─────────────────────────────────────────────

    @Test
    fun cache_getOrPut_sameKey_computeCalledOnlyOnce() {
        val cache = LruCache<String, String>(maxSize = 5)
        var computeCount = 0

        repeat(10) {
            cache.getOrPut("key") {
                computeCount++
                "value"
            }
        }

        assertEquals(1, computeCount, "compute should only be called once for the same key")
    }

    // ── Access-order: recently accessed entries survive eviction ──────────────

    @Test
    fun cache_lru_accessOrder_recentlyReadSurvivesEviction() {
        val cache = LruCache<String, String>(maxSize = 2)
        cache.getOrPut("A") { "a" }
        cache.getOrPut("B") { "b" }
        // Access A to make it most-recently-used
        cache.getOrPut("A") { error("A should be cached") }
        // Insert C — should evict B (LRU), not A
        cache.getOrPut("C") { "c" }

        assertEquals(2, cache.size)
        // A should still be in cache
        var aRecomputed = false
        cache.getOrPut("A") {
            aRecomputed = true
            "new_a"
        }
        assertFalse(aRecomputed, "A should still be in cache after B was evicted")
    }

    @Test
    fun cache_lru_accessOrder_oldestNonAccessedIsEvicted() {
        val cache = LruCache<String, String>(maxSize = 2)
        cache.getOrPut("X") { "x" }
        cache.getOrPut("Y") { "y" }
        // Insert Z — should evict X (oldest, never re-accessed)
        cache.getOrPut("Z") { "z" }

        var xRecomputed = false
        cache.getOrPut("X") {
            xRecomputed = true
            "new_x"
        }
        assertTrue(xRecomputed, "X should have been evicted when Z was inserted")
    }
}
