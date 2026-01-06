# Resources:
- [Koin](https://insert-koin.io/)

- [3 tips - Youtube](https://www.youtube.com/watch?v=ORg3ZYQNuJg)
- [Full Guide for KMM - Youtube](https://www.youtube.com/watch?v=TAKZy3uQTdE)


# Koin
## Overview
- Lightweight, purely Kotlin DI framework: no code generation, no annotation processing.
- DSL-based: you declare definitions in `module { ... }` blocks instead of using annotations.
- Runtime graph: definitions are resolved at runtime using type and qualifiers.
- Great fit for KMM / multiplatform: same DI DSL in `commonMain`, platform modules provide platform-specific bindings.
- Works well with: Android (Application, Activities, Fragments, ViewModels, Compose), Ktor, desktop, iOS.

### Core concepts
- **Module**: a collection of definitions (how to create types). Usually split by feature or layer.
- **Definition kinds**:
  - `single { ... }`: one instance per Koin container, lazy by default.
    - also have `singleOf(::MyType)` shorthand.
  - `factory { ... }`: new instance on each resolution.
  - `viewModel { ... }` / `viewModelOf(::MyViewModel)`: AndroidX ViewModel integration.
    - allows injection into ViewModels with lifecycle handling.
  - `scoped { ... }`: one instance per scope (e.g., per screen or flow).
- **Resolution**:
  - Constructor injection via `get()` parameters in definitions.
  - Field / property injection via `by inject()` or `by viewModel()` delegates.
  - Qualifiers (`named("..."))` or custom qualifiers to disambiguate multiple bindings of same type.
    - e.g. two retrofit instances (e.g., for different APIs).
- **Startup**:
  - `startKoin { modules(appModule, featureModule, ...) }` in Android `Application`.
  - Multiplatform: shared `module` in `commonMain`, `platformModule` in each `*Main` source set, combined in shared initializer (as in your notes below).

---

## Steps to implement Koin in Android using Koin Library:

High-level flow for a typical Android-only app:

1. **Add dependencies**
   - Core: `io.insert-koin:koin-core`.
   - Android: `io.insert-koin:koin-android` (for `androidContext`, components, etc.).
   - AndroidX ViewModel: `io.insert-koin:koin-androidx-viewmodel` (or `koin-androidx-compose` for Compose).
   - Testing: `io.insert-koin:koin-test`, `koin-test-junit4`/`junit5`.

2. **Create modules**
   - Group by layer or feature: `networkModule`, `databaseModule`, `repositoryModule`, `featureXModule`.
   - Use `single` for long-lived dependencies (Retrofit, database, repositories).
   - Use `factory` for short-lived dependencies (use-case objects, mappers if they hold state).
   - Use `viewModel`/`viewModelOf` for `ViewModel`s.

3. **Start Koin in your `Application`**
   - Override `onCreate` and call `startKoin { ... }` once.
   - Provide the Android context via `androidContext(this@MyApplication)`.
   - Register all the app-level modules.

4. **Inject in Android components**
   - Classic view system: use `by viewModel()` in Activities/Fragments or `by inject()` for services, managers.
   - Compose: use `koinViewModel()` / `getKoin().get()` / `koinInject<T>()`.
   - Avoid injecting directly in `Composable` parameters when possible; favor view models that own the injected dependencies.

5. **Wire up tests**
   - In instrumented/unit tests, start a dedicated Koin context with test modules.
   - Use `declareMock` / `declare` to override definitions for mocks.

You already captured KMM-specific steps in the Phillip Lackner section. For a senior-level Android dev, key additions are **module design, scopes, qualifiers, and testing**.

### Example Implementation (Android-only sketch)

```kotlin
// AppModule.kt
val appModule = module {
    // Network
    single { createOkHttpClient(get()) }
    single { createRetrofit(get()) }
    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }

    // Database
    single { createDatabase(androidContext()) }

    // Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // Use cases
    factory { GetUserUseCase(get()) }

    // ViewModels
    viewModelOf(::UserViewModel)
}
```

> NOTE: `createOkHttpClient`, `createRetrofit`, and `createDatabase` are helper functions you would define to set up those components. 
```kotlin
// createOKHttpClient.kt
fun createOkHttpClient(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        // configure client (interceptors, timeouts, etc.)
        .build()
}
```

```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule /*, other modules */)
        }
    }
}
```

```kotlin
// In an Activity (View system)
class UserActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // use viewModel
    }
}
```

```kotlin
// In Compose
@Composable
fun UserScreen() {
    val viewModel: UserViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    // render UI
}
```

Key senior-level considerations:
- Group modules by **feature or layer**, not by type only.
- Keep module APIs stable and small so features can be moved to dynamic feature modules later if needed.
- Use qualifiers when there are multiple backends or configurations (e.g., staging vs production, in-memory vs real repository).

---

## Common Pitfalls

### 1. Overusing `single` (global state everywhere)
- Symptom: everything is a singleton; hard to test, hidden coupling.
- Better:
  - Use `factory` for stateless use cases, helpers, and objects that don't need to be singletons.
  - Reserve `single` for resources that must be shared (DB, retrofit, long-lived caches).

### 2. Leaking Android context
- Symptom: storing `Activity` or `Fragment` in singletons or long-lived objects.
- Best practices:
  - Use `androidContext()` (Application context) in definitions that need context.
  - Never inject `Activity`/`Fragment` into singletons.
  - Inject `Context` where truly needed and keep the scope small.

### 3. Hidden graph creation order bugs
- Starting Koin too late (e.g., first access from a `ContentProvider` before `Application` `onCreate`).
- Multiple `startKoin` calls creating multiple contexts.
- Fix:
  - Initialize Koin **once** in `Application.onCreate` (or shared init on multiplatform).
  - Avoid static initializers that call `get()` before Koin is started.

### 4. Missing or ambiguous bindings
- Symptom: `NoBeanDefFoundException` or wrong implementation resolved.
- Use qualifiers when:
  - You have more than one binding of the same type.
  - Example:

    ```kotlin
    val dataModule = module {
        single(qualifier = named("remote")) { RemoteUserDataSource(get()) }
        single(qualifier = named("local")) { LocalUserDataSource(get()) }
    }

    class UserRepository(
        @Named("remote") private val remote: UserDataSource,
        @Named("local") private val local: UserDataSource,
    )
    ```

- In KMM, be explicit about what lives in `sharedModule` vs `platformModule` to avoid platform-specific types ending up in common code.

### 5. Over-injecting vs passing parameters
- Not everything needs DI:
  - Avoid injecting simple values that can be passed as parameters (IDs, configuration objects created at entry points).
  - Prefer constructor parameters for ephemeral values (e.g., nav arguments) and DI for shared collaborators.

### 6. Complex module wiring
- Massive, monolithic module with all bindings is hard to maintain.
- Split into feature or layer modules: `coreModule`, `networkModule`, `featureXModule`, etc.
- Compose them in a single list when starting Koin.

---

## Best Practices

### Module design
- **Per feature**: e.g., `homeModule`, `detailModule`, `authModule`. Each owns its ViewModels, use cases, repositories.
- **Per layer** when appropriate: `networkModule`, `databaseModule`, `analyticsModule` that shared features can depend on.
- Keep module boundaries reflecting the real architecture so they can map to Gradle modules later.

### Scopes and lifetimes
- Use `single` for app-wide services: logger, analytics, DAOs, Retrofit, database, repositories with caches.
- Use `factory` for transient objects and use cases.
- Use `scoped` for lifetimes such as:
  - Per navigation graph (using Koin scopes + nav graph IDs).
  - Per screen if you need more than what the ViewModel provides.
- On Android, prefer `viewModel`/`viewModelOf` as the main screen-level scope.

### Compose integration
- Prefer `koinViewModel()` in Composables to keep DI hidden behind the UI boundary.
- Pass only data and callbacks down the Composable tree; avoid DI on leaf composables.
- Use `KoinContext` or `KoinApplication` composable properly for multiplatform/desktop targets (as in your notes).

### Testing with Koin
- Start a dedicated Koin context in test setup:

  ```kotlin
  @Before
  fun setUp() {
      startKoin {
          modules(testModule)
      }
  }

  @After
  fun tearDown() {
      stopKoin()
  }
  ```

- Use `declareMock` or `declare { single { FakeRepository() } }` to override definitions.
- In instrumented tests, use the same pattern but with Android-specific modules if needed.
- Keep modules small and focused so it’s easy to replace just what tests need.

### Multiplatform specifics
- Keep `commonMain` modules free of Android classes; depend only on expected interfaces or pure Kotlin types.
- Provide `expect`/`actual` for platform-specific services (`DbClient`, logging, file system, etc.), as you captured in your notes.
- Compose modules in a single initialization point (`initKoin`) and call it in each platform entry point.

### Team / project hygiene
- Document module responsibilities in a short comment at the top of each module file.
- Enforce a convention: new features must provide a `featureXModule` and be added in one place in the app’s DI setup.
- Avoid using `GlobalContext.get()` or `getKoin()` from deep inside business logic; prefer constructor injection so dependencies are explicit.

---

## Interview Questions

Senior-level prompts that relate to the concepts above:

- Compare Koin with Dagger/Hilt in terms of:
  - Performance (runtime vs compile-time DI).
  - Tooling / error detection.
  - Suitability for KMM / multiplatform.
- How do you structure Koin modules in a large, modularized Android app?
- When would you use `single` vs `factory` vs `scoped` vs `viewModel`? Give concrete examples.
- How do you handle multiple implementations of the same interface in Koin?
- How do you avoid memory leaks when injecting Android `Context` or components?
- How would you write tests for a Koin-based module? How do you override definitions for tests?
- How do you integrate Koin with Jetpack Compose and navigation?
- What are typical problems you’ve seen with DI in KMM projects, and how does Koin help or hinder?
- How does Koin resolve dependencies at runtime? What are the trade-offs compared to code-generated DI?





---




# Phillip Lackner - Koin for Dependency Injection - Youtube
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
    // viewModelOf might have been added in current Koin version
    // if so can be added to shared module instead of platform specific
}
```

```kotlin
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

### Using Accessing Koin ViewModel in Navigation Composable
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