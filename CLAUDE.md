# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project overview

**ShaderX** is a Kotlin Multiplatform library that exposes GPU shader effects through Compose
Multiplatform `Modifier` extensions. Targets: **Android (API 33+ via AGSL)**, **iOS**, **JVM/Desktop
**, and **Wasm/JS** (the last three via Skia's `RuntimeEffect`). Published to Maven Central as
`io.github.debanshu777:shaderx`. The repo also contains demo apps under `samples/` (`ShaderLab`,
`ASCIICamera`, `VerticalCarousel`) that consume the library via `projects.shaderx`.

## Common commands

JDK 21 is required (CI uses Temurin 21; `:shaderx` Android target compiles to `JVM_21`).

**Library**

- Build all targets: `./gradlew :shaderx:build`
- Run all checks/tests: `./gradlew :shaderx:check`
- JVM-only test source set: `./gradlew :shaderx:jvmTest`
- Single test class: `./gradlew :shaderx:jvmTest --tests "FQCN"`
- Publish locally for consumers: `./gradlew :shaderx:publishToMavenLocal`

**Sample apps** (module paths verified against `settings.gradle.kts`)

- Android demo: `./gradlew :samples:ShaderLab:androidApp:installDebug`
- Desktop demo: `./gradlew :samples:ShaderLab:composeApp:run`
- iOS framework, then open `samples/ShaderLab/iosApp/iosApp.xcodeproj` in Xcode:
  `./gradlew :samples:ShaderLab:composeApp:linkDebugFrameworkIosSimulatorArm64`
- ASCIICamera Android: `./gradlew :samples:ASCIICamera:androidApp:installDebug`

## Architecture

### Source-set hierarchy (`shaderx/build.gradle.kts`)

The KMP layout is the central thing to understand before editing anything in `:shaderx`:

- `commonMain` defines abstractions and effect implementations only — no rendering.
- A custom intermediate `skiaMain` source set is `dependsOn`'d by `iosMain`, `jvmMain`, and
  `wasmJsMain`. All three of those platforms share one Skia-based factory.
- `androidMain` does **not** depend on `skiaMain`. It uses Android's `RuntimeShader` (AGSL)
  directly.

So the working rule is: when adding/changing effect rendering, you usually touch `commonMain` (
definition) + `skiaMain` (Skia path) + `androidMain` (AGSL path). Don't duplicate logic into each
non-Android platform individually — put it in `skiaMain`.

### Effect type hierarchy (`commonMain/.../effect/`)

- `ShaderEffect` — **sealed** root interface (`@Stable`). Carries `id`, `displayName`, `parameters`,
  and `withTypedParameter` (canonical) / `withParameter` (Float convenience wrapper) for immutable
  updates.
- `RuntimeShaderEffect` — supplies AGSL/SkSL `shaderSource` plus `buildUniforms(width, height)`.
  Concrete effects live in `effect/impl/`.
- `AbstractRuntimeShaderEffect` — abstract class that implements the `withTypedParameter`/
  `getParameterValue`/`getTypedParameterValue` dispatch via a
  `parameterHandlers: Map<String, ParamHandler<*>>`. Prefer this base for new effects — it
  eliminates ~70 lines of boilerplate. Parameter range validation is automatic.
- `NativeEffect` — wraps a platform-native effect (e.g. `BlurEffect`). Routed to per-platform native
  APIs instead of compiling shader source.
- `AnimatedShaderEffect` — adds `withTime(seconds)`; driven from `rememberShaderEffect` via
  `withFrameMillis`. `WaveEffect` is the canonical impl — its `equals`/`hashCode` **intentionally
  exclude `time`** so the animation loop doesn't tear down `LaunchedEffect` every frame.
- `CompositeEffect` — chains effects. Parameter IDs are namespaced as
  `"${index}\u001F${originalId}"` (U+001F delimiter, not underscore, to avoid collision with effect
  IDs containing `_`). The `+` operator on `ShaderEffect` builds composites.

### Factory layer (`factory/ShaderFactory.kt`)

- `ShaderFactory : AutoCloseable` — call `close()` to release compiled GPU resources when the
  factory is no longer needed.
- `BaseShaderFactory` (in `commonMain`) routes by effect type. Composite chaining returns
  `ShaderError.UnsupportedEffect` on non-Android platforms — this is a documented error, not a
  silent partial render.
- `expect fun ShaderFactory.Companion.create(maxCacheSize: Int = DEFAULT_CACHE_SIZE)` is implemented
  by:
    - `androidMain` → `AndroidShaderFactory` (real access-order LRU `RuntimeShader` cache,
      thread-safe).
    - `skiaMain` → `SkiaShaderFactory` (`RuntimeEffect` cache, closes Skia `Managed` objects on
      eviction).
- Both factories use a real access-order `LruCache` (see `factory/LruCache.kt` expect/actual).
  Compilation is the expensive step; updating uniforms each frame is cheap. Don't bypass the cache.

### Compose surface (`compose/ShaderModifier.kt`)

Public entry points: `Modifier.shaderEffect(effect)`, `Modifier.shaderEffectWithResult`,
`rememberShaderEffect`, `rememberRenderEffect`. The default `factory` parameter is
`LocalShaderFactory.current` — a process-wide singleton. Callers who need a scoped cache use
`rememberShaderFactory()` + `CompositionLocalProvider(LocalShaderFactory provides factory)`.

### Composite chaining

Composite effect chaining (`CompositeEffect` with >1 effects) is **only supported on Android API 31+
**. On other platforms, `factory.createRenderEffect(composite, w, h)` returns
`ShaderResult.Failure(ShaderError.UnsupportedEffect(...))`. Handle errors via `onFailure` or `fold`.

### ImageProcessor

`ImageProcessor.create(factory: ShaderFactory)` — the factory is required so the processor shares
the factory's compiled-shader cache. Don't construct without a factory.

### Result type

All factory operations return `ShaderResult<T>` (`result/ShaderResult.kt`) with a `ShaderError`
sealed type. Combinators: `map`, `flatMap`, `fold`, `recover`, `onSuccess`, `onFailure`,
`getOrNull`, `getOrElse`. Reuse these variants for new failure paths instead of throwing.

## Conventions specific to this repo

- `:shaderx` enables `kotlin { explicitApi() }` — every new public declaration in the library needs
  an explicit `public` modifier or it won't compile. Keep helpers `internal`.
- The build calls `applyDefaultHierarchyTemplate()` plus the manually-created `skiaMain` source set.
  Shared non-Android logic goes in `skiaMain`.
- Dependencies belong in the version catalog at `gradle/libs.versions.toml`, not inline in module
  `build.gradle.kts` files.
- `settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS`, so sample modules import the library
  as `implementation(projects.shaderx)`.

## Adding a new effect (the most common task)

1. Create `shaderx/src/commonMain/kotlin/com/debanshu/shaderlab/shaderx/effect/impl/MyEffect.kt`
   extending `AbstractRuntimeShaderEffect` (or `NativeEffect` if delegating to a platform API).
   Declare `id`, `displayName`, `shaderSource`, `buildUniforms`, and `parameterHandlers`. See
   `GrayscaleEffect` for a minimal example.
2. If it's a `NativeEffect`, add a branch to `AndroidShaderFactory.createNativeEffect` **and**
   `SkiaShaderFactory.createNativeEffect`.
3. To surface it in `ShaderX.builtInEffects()`, register it in `cachedBuiltInEffects` in
   `shaderx/src/commonMain/.../ShaderX.kt`.
4. AGSL/SkSL source must declare `uniform shader content;` and a `half4 main(float2 fragCoord)`
   entry point — see the format documented at the top of `RuntimeShaderEffect.kt`.
5. Add `@Immutable` to the class and annotate each parameter in `parameterHandlers` with the correct
   handler helper (`floatHandler`, `colorHandler`, `toggleHandler`).
