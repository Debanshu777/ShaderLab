package com.debanshu.shaderlab.shaderx.factory

/**
 * A bounded, access-order Least-Recently-Used cache.
 *
 * Thread-safety guarantees depend on the platform actual:
 * - Android/JVM: fully synchronized via `java.util.LinkedHashMap(accessOrder=true)`.
 * - Skia (iOS/Desktop/Wasm): insertion-order map with manual move-to-tail on read;
 *   safe for single-threaded Compose rendering; callers must synchronize externally
 *   if accessed from multiple threads on those targets.
 *
 * @param maxSize Maximum number of entries to retain. When exceeded, the least-recently-used
 *   entry is evicted automatically.
 */
internal expect class LruCache<K : Any, V : Any>(
    maxSize: Int,
) {
    val size: Int

    /**
     * Returns the cached value for [key] if present, or computes and caches a new value
     * via [compute] on a cache miss. The returned value is marked as most-recently used.
     */
    fun getOrPut(
        key: K,
        compute: () -> V,
    ): V

    /**
     * Removes all entries. [onEvict] is called for each evicted value, allowing callers
     * to release platform resources (e.g. close Skia `Managed` objects).
     */
    fun clear(onEvict: (V) -> Unit = {})
}
