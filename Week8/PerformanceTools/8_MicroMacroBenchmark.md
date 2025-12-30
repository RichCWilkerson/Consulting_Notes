# Resources:
- [PL - Youtube](https://www.youtube.com/watch?v=XHz_cFwdfoM&pp=ygUacGhpbGlwcCBsYWNrbmVyIG1hY3JvYmVuY2g%3D)
- [Medium](https://medium.com/@sivavishnu0705/boost-your-apps-performance-with-android-macrobenchmark-ab7c2e566b4a)
- [V3 - Medium](https://medium.com/@gadagool.krishna/android-app-performance-benchmarking-with-macrobenchmark-v3-c07035d5f14e)
- [Medium](https://ahmedomara1.medium.com/benchmarking-in-android-48afc5054510)
- [Medium](https://medium.com/metakratos-studio/easy-benchmarking-with-jetpack-benchmark-library-macro-and-micro-benchmarks-7074750d1b47)
- [Developer Docs - Microbenchmark](https://developer.android.com/topic/performance/benchmarking/microbenchmark-overview)
- [Developer Docs - Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)



# Macrobenchmark
## Philipp Lackner - Youtube
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
