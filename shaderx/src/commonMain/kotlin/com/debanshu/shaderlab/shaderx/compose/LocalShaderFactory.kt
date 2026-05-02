package com.debanshu.shaderlab.shaderx.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.debanshu.shaderlab.shaderx.factory.ShaderFactory
import com.debanshu.shaderlab.shaderx.factory.create

/**
 * [CompositionLocal] that provides the [ShaderFactory] used by [Modifier.shaderEffect] and friends.
 *
 * The default value is a **process-wide singleton** created lazily with the default cache size.
 * It is never closed — its lifetime matches the process. Override at any scope with
 * [rememberShaderFactory] to get a composition-scoped cache that closes on leave:
 *
 * ```kotlin
 * val factory = rememberShaderFactory(maxCacheSize = 20)
 * CompositionLocalProvider(LocalShaderFactory provides factory) {
 *     LazyColumn { /* all items share `factory` */ }
 * }
 * ```
 *
 * Uses [staticCompositionLocalOf] so downstream composables do **not** recompose when the
 * local is provided further up — the value rarely changes, and static is the correct choice.
 */
public val LocalShaderFactory: ProvidableCompositionLocal<ShaderFactory> =
    staticCompositionLocalOf { defaultShaderFactory }

private val defaultShaderFactory: ShaderFactory by lazy { ShaderFactory.create() }

/**
 * Creates a [ShaderFactory] scoped to the current composition.
 *
 * The factory is created once (keyed by [maxCacheSize]) and **closed automatically** when the
 * composable that calls this function leaves the composition. Use this with
 * [androidx.compose.runtime.CompositionLocalProvider] to give a subtree its own bounded cache:
 *
 * ```kotlin
 * @Composable
 * fun GalleryScreen() {
 *     val factory = rememberShaderFactory(maxCacheSize = 20)
 *     CompositionLocalProvider(LocalShaderFactory provides factory) {
 *         LazyColumn { items(photos) { photo -> PhotoCard(photo) } }
 *     }
 * }
 * ```
 *
 * Do **not** call [ShaderFactory.close] manually on the returned factory — the [DisposableEffect]
 * handles it.
 *
 * @param maxCacheSize Maximum number of compiled shaders to retain. Defaults to 50.
 */
@Composable
public fun rememberShaderFactory(maxCacheSize: Int = 50): ShaderFactory {
    val factory = remember(maxCacheSize) { ShaderFactory.create(maxCacheSize) }
    DisposableEffect(factory) {
        onDispose { factory.close() }
    }
    return factory
}
