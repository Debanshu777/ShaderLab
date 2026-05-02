package com.debanshu.shaderlab.shaderx

import com.debanshu.shaderlab.shaderx.factory.LruCache
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheTest {
    @Test
    fun cache_getOrPut_returnsCachedValue() {
        val cache = LruCache<String, String>(maxSize = 5)
        val first = cache.getOrPut("key") { "value" }
        val second = cache.getOrPut("key") { error("should not recompute") }
        assertEquals("value", first)
        assertEquals("value", second)
    }

    @Test
    fun cache_evictsAtCapacity() {
        val cache = LruCache<String, String>(maxSize = 3)
        repeat(10) { cache.getOrPut("key$it") { "val$it" } }
        assertEquals(3, cache.size)
    }

    @Test
    fun cache_isAccessOrder_notFifo() {
        val cache = LruCache<String, String>(maxSize = 3)
        cache.getOrPut("A") { "valA" }
        cache.getOrPut("B") { "valB" }
        cache.getOrPut("C") { "valC" }

        // Access A again — should move it to most-recently-used position
        cache.getOrPut("A") { error("A should still be cached") }

        // Insert D — should evict B (oldest unread entry), NOT A
        cache.getOrPut("D") { "valD" }

        assertEquals(3, cache.size)
        // A is still in the cache (recently accessed)
        cache.getOrPut("A") { error("A should still be cached after D inserted") }
        // D is the new entry
        cache.getOrPut("D") { error("D should be cached") }
    }

    @Test
    fun cache_clear_removesAllEntries() {
        val cache = LruCache<String, String>(maxSize = 5)
        repeat(5) { cache.getOrPut("key$it") { "val$it" } }
        assertEquals(5, cache.size)

        cache.clear()
        assertEquals(0, cache.size)
    }

    @Test
    fun cache_clear_callsOnEvictForEachEntry() {
        val cache = LruCache<String, String>(maxSize = 5)
        val evicted = mutableListOf<String>()
        repeat(3) { cache.getOrPut("key$it") { "val$it" } }

        cache.clear(onEvict = { evicted.add(it) })

        assertEquals(3, evicted.size)
        assertEquals(0, cache.size)
    }

    @Test
    fun cache_sizeZero_handledCorrectly() {
        // maxSize of 1 — each insert evicts the previous entry
        val cache = LruCache<String, String>(maxSize = 1)
        cache.getOrPut("A") { "valA" }
        cache.getOrPut("B") { "valB" }
        assertEquals(1, cache.size)
        // A should have been evicted by B
        var aRecomputed = false
        cache.getOrPut("A") {
            aRecomputed = true
            "valA2"
        }
        assertEquals(true, aRecomputed)
    }
}
