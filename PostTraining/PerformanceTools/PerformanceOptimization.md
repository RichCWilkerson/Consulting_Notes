# Performance Optimization — Detailed Breakdown

Goal: Ship smooth, responsive, and efficient mobile apps.

Domains
- UI/UX latency and jank
- Memory management
- Battery usage

---

## UI/UX Latency and Jank
Symptoms
- Scroll stutter, delayed taps, long cold/warm starts

Diagnostics
- Android: Profile with Android Studio Profiler, Macrobenchmark, Systrace/Perfetto
  - TODO: pros/cons of Profiler vs Macrobenchmark vs Systrace/Perfetto? when to use each?
- iOS: Instruments (Time Profiler, Core Animation), os_signpost
- Cross-platform: frame timelines, GPU overdraw, Network profiler

Fix Strategies
- Main-thread discipline: keep heavy work off UI thread
- Rendering: reduce overdraw; flatten view hierarchies; prefer Compose lazy lists with item keys
  - TODO: what is overdraw? how to measure/reduce it?
  - TODO: what does flatten view hierarchies mean?
- Images: use appropriate sizes, caching, prefetching; decode off-thread
  - TODO: decode off-thread: how to do this? 
- Scheduling: debounce/throttle; batch updates; use coroutines/WorkManager for background
  - TODO: essentially update less frequently? but in bigger batches? 
  - TODO: debounce/throttle: how to do this in Android?
- Start-up: lazy-init, App Startup library, avoid contentProviders, defer DI graphs
  - TODO: avoid contentProviders: why? 
  - TODO: defer DI graphs: I know DI, how is DI graph different? why defer it? how?
  - TODO: what are we trying to lazy-init here? are we just saying use `by lazy` for non-critical things? examples?

Verification
- Add perf budgets (e.g., <120ms TTI, 60fps median, 0 frozen frames)
  - TODO: what does any of this mean? TTI? are we using a tool that measures this?
- Macrobenchmarks in CI to catch regressions

## Memory Inefficiencies
Diagnostics
- Heap dumps, LeakCanary (Android), Instruments Leaks (iOS)
  - TODO: what are heap dumps? how to get/use them?
- Track allocations during navigation flows; watch for bitmaps and context leaks
  - TODO: how to track allocations during navigation flows?
  - TODO: why watch for bitmaps?
  - TODO: context leaks: what are they? how to spot/fix them?

Fix Strategies
- Avoid static references to context/views; use applicationContext where safe
  - TODO: static references to context/views: examples?
  - TODO: when is it safe to use applicationContext?
- Unregister listeners/receivers in lifecycle methods
- Cancel coroutines on lifecycle owner; use viewModelScope
- Prefer immutable data; reuse buffers; use pooling cautiously
  - TODO: reuse buffers: examples?
  - TODO: pooling cautiously: what does this mean? like using unconfined coroutines?
- Image caching with limits; downsample large images; use WebP/AVIF when possible

Verification
- Memory benchmarks; alerts for OOM/restarts
  - TODO: how to set up memory benchmarks? what tools?
- Track retained objects over time with LeakCanary/Instruments

## Battery Drain
Diagnostics
- Android: Battery Historian, adb bugreport, Power Profiler
- iOS: Energy Log

Fix Strategies
- Batch network calls; use HTTP/2, compression; cache aggressively
- Schedule background work with constraints (charging/unmetered)
  - TODO: constraints (charging/unmetered): how to set this up?
- Avoid wake locks; respect Doze/App Standby; use foreground services sparingly
  - TODO: what are wake locks? how to avoid them?
  - TODO: what does it mean to respect Doze/App Standby? can developers accidentally violate this?
- Sensor/GPS: reduce frequency, geofencing, significant-change, fused provider
  - TODO: fused provider: what is this?

Verification
- Define power budget per feature; measure before/after

## Tooling and Guardrails
- Add strict mode/ANR watchdogs in debug
- Use tracing APIs (androidx.tracing, os_signpost) around hot paths
  - TODO: what are tracing APIs? how to use them?
  - TODO: what are hot paths? examples?
- Integrate Crashlytics + performance monitoring; add custom keys for perf events
  - TODO: what are perf events?

## Playbooks
TODO: what are playbooks in this context?
- Slow list scroll: profile, check item recomposition, prefetch images, measure jank
- App cold start: defer heavy DI, preload critical data asynchronously, splash minimal
- Periodic ANRs: review main-thread I/O, large bitmaps on main, blocking binder calls

---

## Android Engineer Notes
- Compose: watch recomposition counts, use derivedStateOf and remember wisely; key lazy list items; avoid heavy modifiers per frame.
  - TODO: what are heavy modifiers per frame? examples?
- Network-on-main audits: enable StrictMode in debug and log violations; fix sync disk/network on UI thread.
  - TODO: this just means to ensure no network or disk I/O on main thread, right?
- Establish macrobenchmarks for cold start and scrolling; gate merges on no regression beyond tolerance.
- Image handling: prefer Coil/Glide configs that downsample and respect lifecycle; cache cautiously; prefetch in lists.
  - TODO: are COIL/Glide handling the downsampling automatically, or do we need to configure something specific?
- Leaks: keep LeakCanary on in debug/CI instrumentation; fix lifecycle/coroutine scope leaks promptly.
  - TODO: is there an github action for LeakCanary we can use in CI? or do i need to write custom scripts?
