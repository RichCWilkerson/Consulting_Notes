# Udemy Course:
[Udemy](https://www.udemy.com/course/kotlin-multiplatform-masterclass/?couponCode=NVD20PMUS)

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)

## Useful tools:
A helpful tool to tell you if your environment is set up correctly is Kdoctor: https://github.com/Kotlin/kdoctor.
- It will inform you if you are missing any required components.

Kotlin Multiplatform Plugin
Xcode

KMP Environment Setup:
https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html#possible-issues-and-solutions

## Switch Branches
1) See remote branches
git fetch origin
git branch -a

2) Create a local branch that tracks remote/<branch>
git fetch origin
git checkout -b <branch> origin/<branch>

## Why?
- stable
- truly native performance for both platforms
- share busniess logic and infra code
- smaller app size
- faster development
- duplication can cause discrepancies and bugs

## What is KMP?
KMP -> Computer apps, web apps, server apps, android and iOS apps

can choose to build common logic in Kotlin and share it across platforms
UI can be built using native frameworks

Kotlin Multiplatform Mobile (KMM) is an SDK that allows developers to share code between Android and iOS applications. 
It enables the use of Kotlin for cross-platform development, allowing for a single codebase for business logic and infrastructure while maintaining native performance and user experience on both platforms.

everything up to the viewmodel can be completely shared

recently we can use it to share UI code as well
- Compose Multiplatform
  - not recommended for large enterprise apps yet
  - still maturing

keep ui native for now
- business logic usually stays the same over time
- innovation usually occurs in the UI layer and mobile specific features




# Philipp Lackner Youtube Playlist

## What is KMP? and What is Compose Multiplatform?
[Youtube](https://www.youtube.com/watch?v=RSBO1C_Du2U&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk)

### KMP (Kotlin Multiplatform)
Kotlin Multiplatform lets you **share code across multiple platforms** (Android, iOS, web, desktop, backend) while still compiling to **native code** on each platform. You typically:
- Share **business logic, networking, data storage, validation, and utilities** in Kotlin.
- Keep **UI and platform-specific integrations** (e.g., notifications, camera, biometrics) in each platform’s native framework.

Key points:
- Single **codebase and test base** for shared logic → fewer duplicated bugs.
- Native performance on each platform (no big runtime bridge).
- Kotlin as the common language, Gradle as the build system (often with Kotlin DSL build scripts).

#### Targets
In a KMP project you define one or more **targets** (Android, iOS, web, desktop, etc.), and the same Kotlin source is compiled differently per target:
- Android → JVM bytecode
- iOS → Native binaries (via Kotlin/Native)
- Web → JavaScript
- Desktop → JVM bytecode or native binaries (depending on setup)

You also get access to **platform-specific APIs** from shared code via "expect/actual" declarations or platform-specific source sets. For example, you can:
- Target platform APIs like Camera, GPS, push notifications, file system, etc.
- Hide those behind shared interfaces so higher-level business logic stays platform-agnostic.

> Contrast with Flutter: Flutter uses a single Dart UI layer and a **bridge** to talk to native code, whereas KMP compiles directly to native artifacts and integrates more naturally with existing Android/iOS stacks.

#### Limitations / Trade-offs
- Shared (common) code can only use **Kotlin multiplatform libraries** (no direct Java-only libraries in `commonMain`).
  - If you plan to adopt KMP, prefer pure Kotlin libraries from the start.
- You still need **platform-specific code** where APIs differ (e.g., iOS vs Android permissions, navigation, native UI).
- A **Mac is required** to compile and run iOS apps:
  - You can write shared code anywhere, but you need macOS + Xcode to build and run on iOS simulators/devices.

### Compose Multiplatform
Compose Multiplatform takes the same declarative UI model as Jetpack Compose and lets you **share UI code** across platforms (Android, desktop, web, and gradually iOS).

- Goal: write UI once in Compose and run it on multiple platforms.
- Status: mature for **Android and desktop**, still **maturing for iOS and web**.

For production today:
- Many teams use **KMP for shared logic** and keep **UI native** per platform.
- Compose Multiplatform is great for internal tools, prototypes, and some production apps, but for large/critical enterprise apps you’ll still see more conservative adoption while the ecosystem stabilizes.


## Build CMP / KMP Project
[Youtube](https://www.youtube.com/watch?v=vvP5vnmzY84&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=2)

Xcode + Android Studio 
Xcode for iOS development - need a Mac and allows you to run iOS simulator
Kotlin Multiplatform Mobile Plugin for Android Studio

kdoctor - checks if environment is set up correctly for KMP development
- install via homebrew: `brew install kdoctor`
- check setup: `kdoctor`

Can create a new KMM project via Android Studio New Project Wizard
- select Kotlin Multiplatform App template
- or kmp.jetbrains.com to generate a project

### Run App in iOS Simulator
- select "Add Configuration" -> iOS Simulator
- select a simulator device (e.g., iPhone 14 Pro)
- run the iOS app configuration
- Xcode will open automatically to build and run the iOS app on the simulator

### Run App in Web Browser
- can try clicking the green play button next to the `jsBrowser` target in the Gradle tool window
- terminal `./gradlew run`


## Expect / Actual Keywords
[Youtube](https://www.youtube.com/watch?v=WxCBzV4qUFw&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=3)

In KMP, the `expect` and `actual` keywords allow you to define platform-specific implementations for shared code.
- `expect` keyword is used in the common/shared code to declare a platform-specific API or functionality without providing an implementation.
  - is like using an interface or abstract class to define a contract that must be fulfilled by platform-specific code.
- `actual` keyword is used in platform-specific source sets to provide the concrete implementation for the expected declaration.
  - is the implementation that fulfills the contract defined by the `expect` declaration.

When you create the actual for iOS it might create a separate directory for nativeMain 
- nativeMain is for iOS and other native targets (like macOS, Linux, Windows)

in his example he created a battery level provider
- in commonMain he created an interface with `expect fun getBatteryLevel(): Int`
- in androidMain he created the actual implementation using Android's BatteryManager API
- in nativeMain (iOS) he created the actual implementation using UIDevice API


## Sharing UI with Compose Multiplatform
[Youtube](https://www.youtube.com/watch?v=Q2iOihnFqnM&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=5)

Android - entry point is MainActivity
iOS - entry point is in iosMain -> AppDelegate.kt
Desktop - entry point is in desktopMain -> Main.kt
Web - entry point is in jsBrowserMain -> Main.kt



## DI with Koin
[Youtube](https://www.youtube.com/watch?v=TAKZy3uQTdE&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=5)
- good DI explanation at beginning of video

- Koin is used because it is pure Kotlin and works in KMM projects
- Dagger/Hilt are Java so will not work for KMM projects

Dependencies:
```toml
# only 2 you need for Koin in KMM project
koin_version = "3.6.0" 
koinComposeMultiplatform = "1.2.0"

# these will be used to show DI in viewmodels and navigation
navigationCompose = "2.8.0"
lifecycleViewModel = "2.8.2"
```

- go to commonMain.dependencies in build.gradle.kts and add the dependencies above
  - commonMain is where you put dependencies that are shared across all platforms
  - use `api` instead of `implementation` for dependencies that need to be exposed to other modules 
    - e.g. koin

```kotlin
expect class DbClient() {
    
}
// can cmd click and select platforms to create actual implementations

// Android actual implementation
actual class DbClient(
    private val context: Context // android database requires a context
)
```

```kotlin
interface MyRepository {
    fun helloWorld(): String
}

class MyRepositoryImpl(
    private val dbClient: DbClient // inject platform specific db client
): MyRepository {
    override fun helloWorld(): String {
        return "Hello from KMM Repository!"
    }
}
```

```kotlin
class MyViewModel(
    private val myRepository: MyRepository // inject repository
): ViewModel() {
    fun getGreeting(): String {
        return myRepository.helloWorld()
    }
}
```

```kotlin
// commonMain/di/Modules.kt
val sharedModule = module { // shared is that all platforms can use it without redefining
    single {
        MyRepositoryImpl(get())
    }.bind<MyRepository>() // bind interface to implementation
    // bind allows us to create an instance of the implementation anytime we create an object that requires the interface
    
    // can also be written as:
    singleOf(::MyRepositoryImpl).bind<MyRepository>()
}

expect val platformModule: Module // platform specific module we need to use expect
// we then generate each platform's actual implementation and define them there
```

```kotlin
// androidMain/di/PlatformModule.kt
actual val platformModule = module {
    singleOf(::DbClient) // no specific binding needed here as there is no interface
    viewModelOf(::MyViewModel) // register viewmodel for injection
    // view models are different on android vs iOS or other platforms
}

// nativeMain/di/PlatformModule.kt (for macOS/iOS)
actual val platformModule = module {
    singleOf(::DbClient)
    singleOf(::MyViewModel) // no viewModelOf for iOS/macOS
    // viewModelOf might have been added in current Koin version
    // if so can be added to shared module instead of platform specific
}
```

### Initializer
- this is called in each platform's entry point to initialize Koin with the shared and platform specific modules

```kotlin
// commonMain/di/initKoin.kt
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            sharedModule,
            platformModule
        )
    }
}

// desktopMain/main.kt
fun main() = application {
    initKoin() // initialize Koin
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
// iOSMain/MainViewController.kt
fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin() // initialize Koin
    }
) {
    App()
}
// androidMain/MyApplication.kt -> remember to register in AndroidManifest.xml
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin { // initialize Koin
            androidContext(this@MyApplication) // need to provide android context for android module
            // notice we made this inside the application class because the DB Client needs the app context not activity context
        }
    }
}
```

### Using ViewModel in Composables
```kotlin
@Composable
fun App() {
    MaterialTheme {
        KoinContext {
            val dbClient = koinInject<DbClient>() // inject platform specific db client
            // when we use koinInject, Koin will look for how to provide the dependency based on the platform we are on
            // this is just an example, you wouldn't want to inject the db client directly in the composable
            
            NavHost(
                navController = rememberNavController(),
                startDestination = "home"
            ) {
                composable("home") {
                    val viewModel = koinViewModel<MyViewModel>() // inject viewmodel
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Aligment.Center
                    ) {
                        Text(
                            text = viewModel.getGreeting()
                        )
                    }
                }
            }
        }
    }
}
```



## Unit and UI Testing Compose Multiplatform
[Youtube](https://www.youtube.com/watch?v=tAMu-RPqkok&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=6)

Objectives:
1. Unit tests for common (shared) code
2. Unit tests for platform-specific code (e.g., Android)
3. UI tests for common (shared) Compose Multiplatform UI (KMP)
4. Instrumented Android Unit tests for Android-specific code
    - dependencies specific to android devices and android SDK
    - typically generated under androidTest source set, but in KMP projects it is under androidMain -> src -> androidTest


### Common Unit Tests
- test for iOS, Android, Desktop, Web

Example of business logic for a unit test:
```kotlin
/*
returns the initials of a persons name
getInitials("John Doe") -> "JD"
getInitials("Alice B. Smith") -> "AS"
getInitials("Henry") -> "HE"
 */
fun getInitials(fullName: String): String {
    val names = fullName.split(" ").filter { it.isNotBlank() }
    
    return when {
        names.size == 1 && names.first().length <= 1 -> {
            names.first().first().toString().uppercase()
        }
        names.size == 1 && names.first().length > 1 -> {
            val name = names.first().uppercase()
            "${name.first()}${name[1].uppercase()}"
        }
    }
}
```

Create a test source set under project/appName/src -> create new directory `commonTest/kotlin`
- we will now put all our common code tests here
- now we can alt + enter on the function and create a test












