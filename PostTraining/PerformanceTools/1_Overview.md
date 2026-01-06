# Resources:
The suggested learning order is from top to bottom.
This overview page should be read first to provide context and overview of performance tools in Android.

> The notes are meant to subsidize or summarize the resources provided - not replace them. 
> So if you do not understand something from the notes, please refer to the original resources for more context.

---

## What is "Performance" in Android?

In this context, **performance** means how fast and smoothly your app feels to users across several axes:
- **Startup time** – how quickly the app launches and becomes interactive.
- **Responsiveness & smooth UI** – how fluid scrolling, gestures, and animations feel (no visible pauses or jank).
- **Memory stability** – avoiding leaks and OOMs so the app doesn’t slow down or crash over time.
- **Network latency & throughput** – how quickly data is fetched/sent, and how efficient payloads are.
- **Battery & energy usage** – how much power the app consumes in foreground and background.

> Almost every tool in this overview targets one or more of these areas. 
> When debugging, start from the **symptom** (e.g., slow startup, jank, OOM, battery drain) and then pick the appropriate tool.

---

## Quick Glossary

- **AOT (Ahead-Of-Time) compilation**: code is precompiled before installation, leading to faster startup and lower runtime overhead.
- **JIT (Just-In-Time) compilation**: code is compiled on-device at runtime, which can cause pauses during execution.
- **Jank**: a visible stutter or hitch in the UI when frames miss their deadline (e.g., >16ms at 60 Hz), causing non‑smooth scrolling or animations.
- **ANR (Application Not Responding)**: a system dialog shown when the app’s main thread is blocked too long (e.g., ~5 seconds without processing input), usually due to heavy work on the UI thread.
- **Cold / Warm / Hot start**:
  - **Cold**: app process not in memory (fresh launch).
  - **Warm**: process in memory but Activity needs to be recreated.
  - **Hot**: process and Activity already in memory, just brought to foreground.
- **P50 / P90 / P95 / P99** (percentiles):
  - **P50**: median – 50% of samples are faster, 50% are slower.
  - **P90 / P95**: “tail” performance – only 10% or 5% of samples are worse.
  - **P99**: worst‑case tail – 1% of samples are slower; useful for spotting outliers that hurt user experience.
- **Heap dump**: a snapshot of the app’s memory at a point in time, showing all live objects and references; useful for diagnosing memory leaks.
- **GC (Garbage Collection)**: automatic memory management process that frees up memory by removing objects no longer in use; can cause pauses if frequent or long.
- **Overdraw**: when the same pixel is drawn multiple times in a single frame (e.g., due to overlapping views), leading to wasted GPU work and potential jank.
- **Wake lock**: a mechanism to keep the CPU or screen awake for background tasks; excessive wake locks can drain battery.
- **Telemetry / APM (Application Performance Monitoring)**: tools and SDKs that collect performance data from real users in production, helping identify issues that may not appear in lab testing.
- **Bitmap pooling**: reusing bitmap memory buffers to reduce allocations and GC overhead, especially useful in image-heavy apps.
- **Hardware bitmaps**: bitmaps stored in GPU memory instead of the Java heap, reducing memory pressure and improving rendering performance.
- **Traces**: detailed logs of app and system activity over time, useful for diagnosing complex performance issues.

---

- **How to use this file**
  - Read this overview once to understand the **landscape** of tools and how they map to common performance symptoms.
  - Then follow the numbered sections in order (1 → 16), jumping into each linked note file when you’re ready to go deeper.

---

## Foundation: Must-Know Basics
### 1. Android Profiling
[Notes](/PostTraining/PerformanceTools/2_AndroidProfiler.md)

### 2. Strict Mode
[Notes](/PostTraining/PerformanceTools/3_StrictMode.md)

### 3. LeakCanary
[Notes](/PostTraining/PerformanceTools/4_LeakCanary.md)

---

## Day-to-Day Performance Tools
### 4. Layout Inspector / Compose
[Notes](/PostTraining/PerformanceTools/5_LayoutInspector_Compose.md)

### 5. Network Inspector/Profiler
[Notes](/PostTraining/PerformanceTools/6_NetworkInspector.md)

### 6. Battery Historian / Power
[Notes](/PostTraining/PerformanceTools/7_BatteryHistorian.md)

---

## Benchmarking & Startup Optimizations
### 7. Jetpack Micro/Macro benchmark
[Notes](/PostTraining/PerformanceTools/8_MicroMacroBenchmark.md)

### 8. Baseline Profiles
[Notes](/PostTraining/PerformanceTools/9_BaselineProfiles.md)

---

## System-level & GPU Deep Dives
### 9. Systrace / Perfetto
[Notes](/PostTraining/PerformanceTools/10_Systrace_Perfetto.md)

### 10. GPU Profiling
[Notes](/PostTraining/PerformanceTools/11_GPU_Profiling.md)

---

## Production Monitoring & Observability 
### 11. Firebase Performance Monitoring
[Notes](/PostTraining/PerformanceTools/12_FirebasePerformanceMonitoring.md)

### 12. Crashlytics / ANR Reports
[Notes](/PostTraining/PerformanceTools/13_Crashlytics.md)

### 13. OpenTelemetry - Pre-built Telemetry SDK
[Notes](/PostTraining/PerformanceTools/14_OpenTelemetry.md)

### 14. CI Performance Checks
[Notes](/PostTraining/PerformanceTools/15_CI_Performance.md)

--- 

## Build / Size Optimization Tools
### 15. APK Analyzer
[Notes](/PostTraining/PerformanceTools/16_APK_Analyzer.md)

### 16. R8 Optimizer
[Notes](/PostTraining/PerformanceTools/17_R8_Optimizer.md)

---

## Quick Resource → Symptom Map

| Symptom / Axis        | Primary Tools                                                                                  |
|-----------------------|------------------------------------------------------------------------------------------------|
| **Startup**           | Macrobenchmark (StartupTimingMetric), Baseline Profiles, Android Profiler, Systrace/Perfetto  |
| **UI jank (CPU/GPU)** | Android Profiler (CPU), CPU Profiler, Macrobenchmark (FrameTimingMetric), AGI, Layout Inspector, Compose Recomposition Highlighter, Systrace/Perfetto |
| **Memory / Leaks**    | Memory Profiler, LeakCanary, Android Profiler (Memory), Systrace/Perfetto (GC events)         |
| **Network**           | Network Profiler, Android Profiler (Network), Firebase Performance (network traces)           |
| **Battery / Energy**  | Energy Profiler, Battery Historian, Firebase Performance, OpenTelemetry/telemetry             |
| **System-wide**       | Systrace / System Tracing, Perfetto, AGI                                                      |
| **Production**        | Firebase Performance, Crashlytics/ANR reports, CI performance checks, Store reviews           |

Use this table as a quick mental index, then jump to the detailed sections below for when and how to apply each tool.

---

# Common Practices to Measure Performance in Android

Performance work in Android usually starts with a **symptom** (slow screen, janky scroll, OOM) and then moves through **measurement → diagnosis → fix → verification**. These tools help at each step.

Below is a symptom‑oriented view of the tools you can reach for.

---

## App Startup & Overall Responsiveness

**Symptoms:**
- Cold start feels slow.
- App takes long to become interactive.
- General sluggishness when navigating between screens.

**Tools:**
- **Android Studio Profiler (overview)**
  - Real-time CPU, memory, network, and energy graphs while you interact with the app.
  - Use it when you notice **slow screen transitions or heavy work during startup** and want a quick visual of what’s going on.

- **Macrobenchmark (StartupTimingMetric)**
  - Measures **cold/warm/hot startup time** and overall responsiveness in repeatable tests.
  - Use it when you want hard numbers for **time‑to‑initial‑display** and to compare before/after optimizations.
    - e.g. time to first frame, time to fully interactive with percentiles.

- **Baseline Profiles**
  - Precompiles hot code paths (AOT) to reduce JIT work at startup and during common flows.
  - Use with Macrobenchmark to measure **startup improvements** with/without profiles.

- **StrictMode**
  - Catches disk and network access on the main thread.
  - Use it early to prevent **accidental heavy work on the UI thread** that slows startup and navigation.

- **Jetpack Benchmark (Microbenchmark)**
  - For small, hot code paths that run on startup (JSON parsing, config loading, etc.).
  - Use it to benchmark **critical pieces of initialization** and pick faster implementations.

- **Systrace / Perfetto (startup traces)**
  - System‑level timeline showing what the app and system are doing during startup.
  - Use when startup is still slow after basic profiling and you need to see **exactly which methods/threads block first frame**.

- **CI Performance Checks (startup focused)**
  - Run Macrobenchmarks on CI, fail builds if startup regresses.
  - Use it when you want to **guard against slowdowns over time**.

---

## UI Smoothness & Jank (CPU + GPU + Compose)

**Symptoms:**
- Janky scroll.
- Stutters during animations or transitions.
- Compose UI feels laggy when state updates.
- Energy spikes - battery drain due to CPU/GPU work.

**Tools:**
- **Android Profiler (CPU)**
  - Gives a high‑level view of CPU spikes while you scroll or animate.
  - Use it to confirm that **expensive work (parsing, allocations)** lines up with jank.

- **CPU Profiler**
  - Method‑level profiling to see which functions are heavy during UI interactions.
  - Use it when you need to identify **hot paths** that should move off the main thread or be optimized.

- **Macrobenchmark (FrameTimingMetric)**
  - Measures frame times and jank (P50/P90/P95/P99) during scripted scrolls and flows.
  - Use it to objectively measure **scroll smoothness and animation performance**.

- **GPU Profiler / Android GPU Inspector (AGI)**
  - Analyzes GPU frame rendering, overdraw, and GPU bottlenecks.
  - Use it when animations are slow even after CPU work is reduced – suspect **GPU overdraw, too many layers, or heavy shaders**.

- **Layout Inspector / Layout Validation**
  - Visualizes view hierarchies and constraints, shows overdraw and complex layouts.
  - Use it when UI is complex or deeply nested and you suspect **layout/render complexity** is causing jank.

- **Compose Tooling & Recomposition Highlighter**
  - Shows which composables are recomposing and how often.
  - Use it when Compose UIs feel laggy and you want to find **over‑recomposing components, unstable data structures, or poor state hoisting**.

- **Systrace / Perfetto (frame timeline)**
  - Shows which frames miss their deadline and why (CPU vs GPU vs I/O).
  - Use it when you need **frame‑by‑frame analysis** and the high‑level tools don’t pinpoint the issue.

- **Custom logging/tracing around UI events**
  - Add timestamps around scroll handlers, navigation, heavy UI work.
  - Use it to correlate **user actions** with **spikes** seen in Profiler or traces.

---

## Memory & Leaks

**Symptoms:**
- OOM (OutOfMemoryError) crashes.
- App slows down over time or after many screen navigations.
- Frequent GC pauses - due to high memory churn or leaks.

**Tools:**
- **Memory Profiler (Android Studio)**
  - Shows live heap size, allocations, and GC events.
  - Take heap dumps and inspect which objects are retained.
  - Use it when you see **memory spikes or OOMs** and want to understand which objects are causing growth.

- **LeakCanary**
  - Automatically detects Activity/Fragment/View leaks in debug builds.
  - Shows leak traces and retained object graphs.
  - Use it when the app **gets slower the longer it runs**, or when navigating back and forth between screens increases memory.

- **Android Profiler (Memory)**
  - Quick overview of allocations during user flows.
  - Use it as a first pass before deep‑dive heap analysis.

- **Systrace / Perfetto (GC & memory events)**
  - Shows when GCs happen relative to jank or slow interactions.
  - Use it when you suspect **GC pauses** are contributing to poor performance.

---

## Network Performance

**Symptoms:**
- Screens feel slow due to data loading.
- Long spinners / skeletons, especially on mobile networks.
- Timeouts or large payloads.

**Tools:**
- **Network Profiler (Android Studio)**
  - Shows each request, size, timing, and response codes.
  - Use it when you want to see **which calls are slow**, which are repeated unnecessarily, or return very large payloads.

- **Android Profiler (Network tab)**
  - High‑level graph of network throughput over time.
  - Use it to spot **bursts** of network usage or unexpected background activity.

- **Firebase Performance Monitoring (Network traces)**
  - Collects latency and payload metrics from real users.
  - Use it when you need to see how **network performance varies across regions, carriers, and device classes**.

- **Custom logging around API calls**
  - Add timestamps and correlation IDs for key endpoints.
  - Use it when debugging **specific flows** or correlating backend logs with client behavior.

---

## Battery & Energy

**Symptoms:**
- App is flagged in Android Vitals for **high battery usage**.
  - flagged by Google Play Console -> Google can remove your app if battery usage is too high
- Users complain the app “drains battery” even when in background.

**Tools:**
- **Energy Profiler (Android Studio)**
  - Shows energy impact of CPU, network, and GPS while you exercise the app.
  - Use it when debugging **battery drain** during known flows (e.g., location tracking, sync, media playback).

- **Battery Historian**
  - Offline tool that parses bugreports to show app and system battery usage over time.
  - Use it when investigating **long‑term battery impact** and correlating with wake locks, jobs, network, etc.

- **Android Profiler (energy indicators)**
  - Quick view of CPU/network/GPS use while the app is foreground/background.
  - Use it as a first pass for coarse energy analysis.

- **Firebase Performance Monitoring / APM**
  - While not purely a battery tool, it helps identify **expensive traces and long‑running operations** that may hurt battery in production.

- **OpenTelemetry / custom telemetry**
  - For advanced setups, instrument long‑running operations and background tasks.
  - Use it when you want **end‑to‑end visibility** into operations that might be energy‑heavy.

---

## System‑wide / Deep Dives

**Symptoms:**
- Performance issues that don’t show clearly in app‑only tools.
- Jank or ANRs that seem tied to system services, I/O, or contention.

**Tools:**
- **Systrace / System Tracing**
  - Captures a system‑wide timeline: CPU scheduling, disk I/O, input, rendering.
  - Use it when you need to see **how your app interacts with the OS**, and why frames or operations are delayed.

- **Perfetto**
  - Modern system tracing tool that can record detailed CPU/GPU/scheduler/track data.
  - Use it for deep analysis of **complex stalls, contention, or multi‑process interactions**.

- **GPU Inspector / AGI**
  - Deep dives into GPU performance and shaders.
  - Use it when Perfetto suggests a **GPU bottleneck** and you need to tune rendering at a low level.
    - Perfetto can show you when GPU is the bottleneck, but AGI helps you understand why (e.g., overdraw, expensive shaders).

---

## Production Monitoring

**Symptoms:**
- Differences between lab and real‑world performance.
- Performance regressions over app versions that weren’t caught locally.

**Tools:**
- **Firebase Performance Monitoring (and similar APM tools)**
  - Collects startup time, network latency, custom traces, slow/frozen frames from real users.
  - Use it to understand **how performance behaves across devices, OS versions, and regions** and to validate that fixes help real users.

- **Crashlytics / ANR reports**
  - Highlight hotspots and problematic code paths causing ANRs or performance‑related crashes.
  - Use it alongside profiling data to prioritize what to fix.

- **User Feedback & Store Reviews**
  - Surfaces perceived issues like “slow”, “laggy”, or “freezes” that metrics may not fully capture.
  - Use it to pick targets for performance work and to verify that changes improved **perceived** performance.

- **CI Performance Checks (Macro/Microbenchmark in CI)**
  - Runs benchmarks automatically on changes.
  - Use it to **catch regressions before release**, then confirm in production via APM and reviews.



---



# Example Interview Story: Fixing Slow Product List & Image Loading

### Problem
- Worked on a **web-based e-commerce app (Neiman Marcus) that was not optimized for mobile**.  
- The **product-list screen** had:
  - Long **time-to-interactive** and visible **jank on first scroll**.  
  - Jank = UI isn’t rendering smoothly (frames dropping below 60/120 fps).
- Symptoms we saw:
  - **Slow image loading** and placeholders visible for too long.  
  - **Jank on older Android devices and tablets** during scroll and screen transitions.  
  - **High memory usage** and occasional **OOM crashes** on older Samsung devices.  
  - **Slow startup** because image libraries were doing heavy initialization on the main thread.

### Investigation (How we used the tools)
- **Android Studio Profiler (CPU + Memory)**  
  - Showed spikes in CPU and allocations when product images were first loaded.  
  - We noticed **large bitmap allocations** and frequent garbage collection pauses.

- **System Tracing / Systrace**  
  - Confirmed that **image decoding and downscaling were happening on, or blocking, the main thread**, causing frame drops during scroll.

- **Macrobenchmark**  
  - Set up scenarios for **cold start** and **first scroll through the product list**.  
  - Baseline measurements: startup time, frames with jank, and time to load above-the-fold items.

- **Firebase Performance Monitoring (Production)**  
  - Validated that users on **mid-range/older devices** had worse startup and screen load times.  
  - Helped us see improvements after deploying optimizations.

### Root Cause
- The app was **downloading large, desktop-sized images** and then repeatedly **downscaling them on-device** for mobile.  
- Images were often **decoded at full resolution**, causing:
  - Large bitmaps in memory → **OOM risk** and more GC.  
  - Extra CPU work to downscale → **jank** on scroll and during transitions.  
- The image library initialization was **eager and on the main thread**, adding to startup cost.

### Fixes (Technical Changes)

#### 1. Coordinate with Backend for Mobile-Optimized Images
- Collaborated with **backend/CDN teams** to introduce additional **image breakpoints** better suited for mobile:
  - Previously: mostly **900 px, 1200 px, 1920 px** variants (desktop-focused).  
  - Added: **300 px, 600 px, 1600 px** variants for mobile and high-density tablets.
- Strategy:
  - **Request the smallest size that satisfies the device/viewport** and avoid fetching full desktop images on mobile.  
  - **Downsample images on the server/CDN**, reducing bytes over the network and client-side work.  
  - Prefer **WebP** (or AVIF where supported) for better compression and quality vs. JPEG/PNG.
    - Downsampling is a form of lossy compression that reduces image dimensions and file size, which is especially effective when done server-side before delivery to clients.

#### 2. Optimize Image Loading on the Client (Coil)
- Migrated and tuned image loading to **Coil**, focusing on:
  - **Specifying target size** in `ImageRequest` so decoding happens at (or near) view size, not full size.  
  - Enabling proper **disk and memory caching** with `CachePolicy` to prefer cached results where possible.  
  - Allowing **hardware bitmaps** (when safe) and leveraging **bitmap pooling** to reuse buffers.
    - **Hardware bitmaps**: bitmaps stored in GPU memory (`Bitmap.Config.HARDWARE`) instead of the Java heap.
      - Pros: reduce heap pressure and GC frequency; can be faster for rendering because the GPU can use them directly.
      - Cons: read‑only and not suitable for all transformations; avoid when you need to frequently mutate pixels.
        - e.g. when the image is static and just displayed, hardware bitmaps are great; but if you need to apply filters or transformations, use software bitmaps.
    - **Bitmap pooling**: reusing existing bitmap instances instead of allocating new ones for every decode.
      - Reduces GC overhead and heap churn by keeping a pool of reusable buffers.
      - Particularly useful when scrolling lists of images where bitmaps are constantly created and discarded.
- Outcome:  
  - Less main-thread work for decoding and scaling.  
  - Fewer and smaller bitmap allocations → reduced GC pressure and OOM risk.

#### 3. Reduce Main-Thread Work and Expensive Initialization
- Moved heavy initialization **off the main thread** and **lazy-initialized** noncritical services:
  - Deferred image-library preloading and large config parsing using **coroutines** or **WorkManager**.  
  - Kept only minimal, essential startup work on the main thread.

#### 4. Improve List Rendering with Paging & Stable Keys
- Integrated **Paging 3** with **prefetching** to load data incrementally:
  - Reduced initial bind work by only loading what’s needed above the fold.  
  - Enabled **prefetch distance** so upcoming items are ready before the user scrolls to them.
- Ensured **stable item keys** in the list adapter/composable:
  - Avoided unnecessary rebinds and recompositions.  
  - Improved scroll smoothness because items weren’t constantly recreated.

### Results (How We Measured Improvement)

- After adding new image breakpoints and optimizing Coil:
  - **~30% faster image load times** on average for the product list on mid-range Android devices.  
  - **~25% improvement in Macrobenchmark results** for first meaningful content and initial scroll.
- Jank & memory improvements:
  - **Fewer janky frames** during scroll on older devices (roughly **10% reduction** in janky frames in Macrobenchmark runs).  
  - Lower peak memory usage due to smaller, better-managed bitmaps.
- Stability improvements:
  - **Crash-free sessions** on older devices improved from **~94% → ~99%**, largely due to fewer **OOM crashes** and better bitmap management.  
  - Backed by **Firebase Performance** and crash reports.

### Key Talking Points for Interviews

- Start with the **symptom** (slow screen, jank, OOM), then describe:
  1. **How you measured it**: Profiler, Macrobenchmark, Systrace, Firebase Performance.  
  2. **What you found**: large images, main-thread decoding, heavy startup work.  
  3. **What you changed**: backend image sizes, Coil config, Paging 3, moving work off main thread.  
  4. **How you proved it worked**: before/after metrics + improved crash-free rate.

- Emphasize:
  - Collaborating with **backend, product, and design** to balance quality vs. performance.  
  - Using **both lab tools** (Profiler, Macrobenchmark) and **production data** (Firebase) to get a full picture.  
  - Focusing on **perceived performance** (placeholders/skeletons) as well as raw metrics.


---
