# Common Practices to Measure Performance in Android

Performance work in Android usually starts with a **symptom** (slow screen, janky scroll, OOM) and then moves through **measurement → diagnosis → fix → verification**. These tools help at each step.

---

## Core Tools & When You’d Use Them

### Development Environment (local profiling & testing)
- **Android Profiler (Android Studio)**  
  - Real-time CPU, memory, network, and energy graphs while you interact with the app.  
  - Use it when: you notice **slow image loading, high CPU, memory spikes, or jank** and want a quick visual of what’s going on.

- **Memory Profiler**  
  - Drill into allocations, take heap dumps, and find **leaks or large bitmap usage**.  
  - Use it when: you see OOMs, high memory in Android Vitals, or constant GC pauses in logs.

- **Network Profiler**  
  - Shows each network request, payload size, and timing.  
  - Use it when: screens feel slow due to **API latency or large payloads** (e.g., uncompressed images, chatty APIs).

- **Layout Inspector & Layout Validation**  
  - Visualize view hierarchies, constraints, and overdraw.  
  - Use it when: you have **slow rendering** or deeply nested/inefficient layouts.

- **StrictMode**  
  - Flags disk and network access on the main thread and other bad patterns.  
  - Use it when: you want to **catch accidental main-thread work** early in development.

- **Jetpack Benchmark (Microbenchmark)**  
  - Measures the performance of **small, focused pieces of code** (sorting, parsing, custom logic).  
  - Use it when: you need to compare implementations (e.g., custom JSON parsing vs. library) in a stable, repeatable way.

- **Macrobenchmark**  
  - Automates launching the app and performing user flows to measure **startup, scroll, and frame time** under controlled conditions.  
  - Use it when: you want deterministic numbers for **startup time, list scroll smoothness, or navigation performance**.

- **Systrace / System Tracing**  
  - Low-level timeline of CPU, rendering, and system events.  
  - Use it when: you need deep analysis of **jank, frame drops, or scheduler issues** that don’t show clearly in Profiler.

- **LeakCanary**  
  - Debug-only library that automatically detects memory leaks and shows leak traces.  
  - Use it when: your app gets **slower over time or after several navigations**, suggesting leaked Activities/Fragments.

- **Custom Logging & Tracing**  
  - Add timestamps (e.g., `t0`, `t1`, `t2`) around critical flows like **login, image loading, checkout**.  
  - Use it when: you need to track performance of a specific feature or experiment without heavy tooling.

- **Continuous Performance Checks in CI**  
  - Run micro/macrobenchmarks on CI devices and fail the build if performance regresses beyond a threshold.  
  - Use it when: you want to **prevent regressions** as the team adds features.

### Production Environment (real user monitoring)

- **Firebase Performance Monitoring (or similar APM)**  
  - Collects **startup time, network latency, traces, and custom metrics** from real users.  
  - Use it when: you want to see **how the app behaves in the wild** across devices, networks, and regions.

- **User Feedback & Store Reviews**  
  - Direct feedback about **“slow,” “laggy,” or “unresponsive”** screens that might not show up in automated tests.  
  - Use it when: validating that your performance fixes actually improve perceived performance for users.

---

## Example Interview Story: Fixing Slow Product List & Image Loading

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

#### 2. Optimize Image Loading on the Client (Coil)
- Migrated and tuned image loading to **Coil**, focusing on:
  - **Specifying target size** in `ImageRequest` so decoding happens at (or near) view size, not full size.  
  - Enabling proper **disk and memory caching** with `CachePolicy` to prefer cached results where possible.  
  - Allowing **hardware bitmaps** (when safe) and leveraging **bitmap pooling** to reuse buffers.
    - TODO: what does hardware bitmaps mean here? 
    - TODO: what does bitmap pooling mean here?
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

# Philipp Lackner
## Macrobenchmark
[Youtube](https://www.youtube.com/watch?v=XHz_cFwdfoM)
- measuring the scrolling performance using jetpack macrobenchmark library
  - cold start, scroll performance, startup time

- first create a new module in your project - type Benchmark
  - select macrobenchmark and our app module we want to benchmark
  - this will generate an example benchmark test class for us

- NOTE: it is best to use what the users are using:
  - not a debug build -> use an APK that is as close to production as possible (R8, Proguard, etc. enabled)
  - go to app and create a new file in the top directory called benchmark-rules.pro
    - add `-dontobfuscate` to this file
  - in the benchmark module build.gradle file, add this line to the android block:
```yaml
...
buildTypes {
// This benchmark buildType is used for benchmarking, and should function like your
// release build (for example, with minification on). It's a signed with a debug key
// for easy local/CI benchmark testing.
  benchmark {
    debuggable = true
    signingConfig = signingConfigs.debug
    matchingFallbacks = ['release']
    proguardFiles("benchmark-rules.pro") // add this line to set proguard rules
    }
}
```
- should try and use a real physical device for benchmarking if possible
  - emulators can have variable performance characteristics

```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "com.example.yourapp",
        // metrics is a list of different metrics we want to capture
        // here we are interested in startup time
        metrics = listOf(StartupTimingMetric()), 
        // how many times we want to repeat the measurement for averaging 
        iterations = 5,
        setupBlock = {
            // Navigate to the screen we want to benchmark
        },
        // we want to measure cold startup (nothing is in memory/cache)
        // WARM and HOT are other options
        startupMode = StartupMode.COLD
    ) {
        // Action to perform, e.g., start the activity
        // pressHome()
        startActivityAndWait()
    }
}
```

- select the correct build variant for the benchmark module
  - go to top tab "Build" -> "Select Build Variant"
  - change the app module to benchmark

- need Baseline profiles -> need dependency in app build.gradle file
    - implementation "androidx.profileinstaller:profileinstaller:1.2.0"

- now we can run the benchmark test
  - click on the green play button next to the test function
  - this will install the app on the connected device/emulator and run the benchmark
  - after the test completes, Android Studio will show the results in the Run window
  - ExampleStartupBenchmark#startup: 
  - timeToInitialDisplayMs
    - min: 1234ms
    - median: 1300ms
    - max: 1400ms
  - Traces:
    - Iteration 0 1 2 3 4 

### Now we want to test scroll performance

```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollAndNavigate() = benchmarkRule.measureRepeated(
        packageName = "com.example.yourapp",
        // metrics is a list of different metrics we want to capture
        // here we are interested in frame timing for smoothness
        // measure Jankiness during scrolling
        metrics = listOf(FrameTimingMetric()),
        // how many times we want to repeat the measurement for averaging 
        iterations = 5,
        setupBlock = {
            // Navigate to the screen we want to benchmark
        },
        // we want to measure cold startup (nothing is in memory/cache)
        // WARM and HOT are other options
        startupMode = StartupMode.COLD
    ) {
        // Action to perform, e.g., start the activity
        pressHome()
        startActivityAndWait()

        // Now perform scrolling action we define below
        addElementsAndScrollDown()
    }
}

fun MacrobenchmarkScope.addElementsAndScrollDown() {
    // device is a UiDevice instance provided by MacrobenchmarkScope
    // helps us interact with the device UI
    val button = device.findObject(By.text("Click me"))
    val list = device.findObject(By.res("item_list"))

    repeat(30) {
        button.click()
    }
    device.waitForIdle()

    // Now perform the scroll action
    // Scroll down the list by a certain margin
    list.setGestureMargin(device.displayWidth / 10)
    list.fling(Direction.DOWN)

    // click on item with text "Element #29"
    device.findObject(By.text("Element #29")).click()

    // Wait for the detail screen to load
    // timeout after 5 seconds
    device.wait(Until.hasObject(By.text("Detail: Element #29")), 5_000)
}


// Compose example for scrolling
// NEED TO ADD semantics testTagsAsResourceId = true for testing with Macrobenchmark library
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                var counter by remember { mutableStateOf(0) }
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "start",
                    modifier = Modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }
                ) {
                    composable("start") {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("item_list")

                        ) {
                            item {
                                Button(onClick = { counter++ }) {
                                    Text("Click me")
                                }
                            }
                            items(counter) {
                                val text = "Element #$it"
                                Text(
                                    text = text,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .clickable {
                                            navController.navigate("detail/$text")
                                        }
                                )
                            }
                        }
                    }
                    composable(
                        route = "detail/{itemText}",
                        arguments = listOf(
                            navArgument("itemText") { 
                                type = NavType.StringType 
                            }
                        )
                    ) {
                        val text = it.arguments?.getString("itemText") ?: "Default"
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Detail: $text")
                        }
                    }
                }
            }
        }
    }
}
```
- this will then produce results:
  - frameDurationCpuMs -> measures how long each frame took to render on the CPU
  - frameOverrunMs -> measures how much time each frame exceeded the target frame time (16ms for 60fps)
    - negative values mean the frame was rendered within the target time
    - bigger positive values mean more jank
  - P50, P90, P95, P99
    - these are percentiles of frame durations during the scroll action


## Baseline Profiles
[Youtube](https://www.youtube.com/watch?v=hqYnZ5qCw8Y)

- Baseline profiles have been around since Android 7.0 (Nougat)
    - became more accessible with Jetpack Profile Installer library
    - now easy to integrate into existing apps with minimal effort
- Overall, baseline profiles are a powerful tool to improve app performance, especially for cold starts and frequently used code paths
    - combined with Macrobenchmarking, they provide a robust framework for measuring and optimizing Android app performance

- how are libraries compiled? in JIT (Just in Time)
- baseline profiles help with AOT (Ahead of Time) compilation
  - helps the app start faster and run smoother by precompiling frequently used code paths

- need a rooted device or emulator to generate baseline profiles
    - can go to build.gradle file and add this line to android block
```yaml
# add import -> provides access to ManagedVirtualDevice which helps us create and manage virtual devices for testing
import com.android.build.api.dsl.ManagedVirtualDevice

android {
    testOptions {
        managedDevices {
            devices {
                pixel2Api31(ManagedVirtualDevice) {
                    device = "Pixel 2"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }
}
```

- setup gradle task to use the device we created above
    - can either run in terminal or from android studio
    - in your emulator selection dropdown, you select "Edit Configurations..."
    - then click on the "+" button and select "Gradle"
    - then put in the gradle terminal command in the Run field 
      - `:benchmark:connectedPixel2Api31DebugAndroidTest --rerun-tasks -P android.testInstrumentationRunnerArguments.class=com.plcoding.benchmark.BaselineProfileGenerator`

Extend on Benchmarking module from previous section
- create new class `BaselineProfileGenerator.kt`
```kotlin
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineRule.collectBaselineProfile(
        packageName = "com.example.yourapp"
    ) {
        // Start the main activity
        pressHome()
        startActivityAndWait()

        // Perform typical user interactions to capture important code paths
        
        // we will use same scrolling function from previous section
        addElementsAndScrollDown()
    }
}
```

- after we run the gradle task ->
  - generates a file located at:
    - `/benchmark/build/outputs/managed_device_android_test_additional_output/pixel2Api31`
- To use this profile in our app, we need to copy it to the main directory of the app module
  - rename the file to `baseline-prof.txt` so it is recognized by the build system
- now when we build the app, the baseline profile will be included and used for AOT compilation

- To test the effectiveness of the baseline profile, we can use Macrobenchmark again to measure startup time and scroll performance before and after adding the profile
  - should see improvements in startup time and smoother scrolling due to reduced JIT compilation overhead
- NOTE: we need to change the test to use the CompileMode

```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun startUpCompilationModeNone() = startup(CompilationMode.None())
    @Test
    fun startUpCompilationModePartial() = startup(CompilationMode.Partial())

    @Test
    fun scrollAndNavigateCompilationModeNone() = scrollAndNavigate(CompilationMode.None())
    @Test
    fun scrollAndNavigateCompilationModePartial() = scrollAndNavigate(CompilationMode.Partial())
    
    fun startup(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.example.yourapp",
        metrics = listOf(StartupTimingMetric()), 
        iterations = 5,
        setupBlock = {
            // Navigate to the screen we want to benchmark
        },
        startupMode = StartupMode.COLD,
        compilationMode = mode
    ) {
        pressHome()
        startActivityAndWait()
    }
    
    
    fun scrollAndNavigate(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "com.example.yourapp",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        setupBlock = {
            // Navigate to the screen we want to benchmark
        },
        startupMode = StartupMode.COLD,
        compilationMode = mode
    ) {
        // Action to perform, e.g., start the activity
        pressHome()
        startActivityAndWait()

        // Now perform scrolling action we define below
        addElementsAndScrollDown()
    }
}
```

- can add to CI pipeline to ensure baseline profiles are always up to date
  - helps maintain optimal performance as the app evolves
  - can help recognize trends in performance over time

