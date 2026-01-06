# Resources:
- [Baseline Profiles - Youtube](https://www.youtube.com/watch?v=hqYnZ5qCw8Y&pp=ygUbcGhpbGlwcCBsYWNrbmVyIHBlcmZvcm1hbmNl)
- [Medium](https://medium.com/@sivavishnu0705/the-blueprint-for-speed-a-deep-dive-into-android-baseline-profiles-d968f085ba16)
- [Medium](https://medium.com/@sahar.asadian90/baseline-profile-in-android-15c756619f5e)




# Baseline Profiles
## Philipp Lackner - Youtube
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