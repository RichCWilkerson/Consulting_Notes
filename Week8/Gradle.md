# Gradle
- Gradle is the build system and task runner for Android and many JVM projects. It automates building, testing, linting, packaging, and deploying your code.
- Uses a domain-specific language (DSL) based on Groovy or Kotlin to define build scripts.
- Think of Gradle as the orchestration layer between your source code, tools (Kotlin compiler, Android plugin, ProGuard/R8, etc.), and the final artifacts (AAB/APK, JAR, AAR).


# Gradle Groovy vs Gradle Kotlin DSL
- Groovy DSL is the original syntax for Gradle build scripts, while Kotlin DSL is a newer alternative that uses Kotlin syntax (`build.gradle.kts`).
- Kotlin DSL offers better IDE support (code completion, navigation), type safety, and improved readability for developers familiar with Kotlin.
- Groovy DSL is a bit more concise and still more common in older Android projects; Kotlin DSL is increasingly preferred for new projects.
- Both DSLs are functionally equivalent — they configure the same Gradle model, just with different syntax.


# Gradle Terminal
- `./gradlew tasks` – lists all available tasks in the project.
- `./gradlew build` – compiles the code, runs tests, and creates build artifacts (AAB/APK, JAR, etc.).
- `./gradlew clean` – deletes the `build/` directories, forcing a fresh build.
- `./gradlew test` – runs the unit tests for the project.
- `./gradlew assembleDebug` – builds the debug variant of the app.
- `./gradlew :app:dependencies` – shows dependency graph for the `app` module (useful for debugging conflicts).


# Gradle Wrapper
- Gradle Wrapper (`gradlew` / `gradlew.bat`) lets you run Gradle without installing it globally.
- Ensures the project uses a specific Gradle version (defined in `gradle/wrapper/gradle-wrapper.properties`), keeping builds reproducible across machines and CI.
- You should always run Gradle via the wrapper (`./gradlew`) in Android projects.


# Project-level vs Module-level `build.gradle[.kts]`
- **Project-level** `build.gradle` / `build.gradle.kts`
  - Located in the root directory of the project.
  - Defines global configuration like repositories, plugin versions, and convention plugins.
  - Often contains `plugins {}` and/or legacy `buildscript {}` blocks.
- **Module-level** `build.gradle` / `build.gradle.kts`
  - Located in each module’s directory (e.g., `app/build.gradle.kts`, `feature/auth/build.gradle.kts`).
  - Defines Android-specific configuration (`android {}` block), module dependencies, and module-scoped plugins.


# Plugins
- Plugins extend Gradle by adding new tasks, conventions, and configuration blocks (e.g., `com.android.application`, `org.jetbrains.kotlin.android`).
- Many Android features (Compose, Hilt, KSP, etc.) are exposed through plugins.

inside build.gradle:
```kotlin
plugins {
    // why 'apply false'?
    // - prevents the plugin from being applied to the root project
    // - allows subprojects to apply the plugin individually as needed
    // the root app project is global, we'd rather have each module decide which plugins to use
    // but we need the classpath available for the subprojects to use
    alias(libs.plugins.android.application) apply false
}

// will run the print when the project is built
// because gradle runs when the project is built
apply<MyPlugin>()

class MyPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        println("Hello from MyPlugin!")
    }
}

android {
    namespace = "com.example.myapp"
    compileSdk = 34 // target sdk version
    
    defaultConfig {
        applicationId = "com.example.myapp" // for google play store - must stay the same to update the app in the play store - if name changes, it's a new app
        minSdk = 21
        targetSdk = 34 // 
        versionCode = 1 // google play store uses this to determine if an update is available
        versionName = "1.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
}
```

- can use custom plugins to automate repetitive tasks, enforce coding standards, or integrate with other tools and services

# Build Types
- Build types define how the app is built for different stages of the development lifecycle (e.g., debug vs release).
- Common build types:
  - **debug** – easier to debug, often larger, with logging and debugging tools enabled.
  - **release** – optimized for performance, minified/obfuscated, logging often reduced or disabled.
  - **staging/UAT** – optional type, usually similar to release but debuggable and pointed at test/staging backends.

```kotlin
android {
    buildTypes {
        debug {
            isMinifyEnabled = false // disables code shrinking for debug builds
            applicationIdSuffix = ".debug" // app ID becomes com.example.myapp.debug
            versionNameSuffix = "-debug" // version becomes e.g. 1.0-debug
            isDebuggable = true // enables debugging
            // Example: point debug builds to a test/UAT environment
            buildConfigField(
                "String",
                "API_URL",
                "\"https://api.debug.example.com/\""
            )
        }
        create("staging") {
            initWith(buildTypes.getByName("release")) // clone release config as a base
            matchingFallbacks += listOf("release") // use release resources if staging-specific ones are missing
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = true
            buildConfigField(
                "String",
                "API_URL",
                "\"https://api.staging.example.com/\""
            )
        }
        release {
            isMinifyEnabled = true // enables code shrinking and obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true // generates BuildConfig so you can access BuildConfig.API_URL in code
    }
}

// MainActivity.kt
val apiUrl = BuildConfig.API_URL // value depends on the active build type
```

## Product Flavors
- Product flavors let you create different variants of your app from the same codebase.
- Common use cases: free vs paid, white-label apps, region-specific builds, B2B vs B2C, etc.
- Flavors are grouped by **flavor dimensions**, and Gradle creates a variant for each combination of flavor + build type.

```kotlin
android {
    flavorDimensions += listOf("paid_status", "gender")

    productFlavors {
        create("free") {
            applicationId = "com.example.myapp.free"
            versionNameSuffix = "-free"
            buildConfigField("boolean", "ADS_ENABLED", "true")
            dimension = "paid_status"
        }
        create("paid") {
            applicationId = "com.example.myapp.paid"
            versionNameSuffix = "-paid"
            buildConfigField("boolean", "ADS_ENABLED", "false")
            dimension = "paid_status"
        }

        create("men") {
            applicationId = "com.example.myapp.men"
            versionNameSuffix = "-men"
            buildConfigField("String", "THEME_COLOR", "\"blue\"")
            dimension = "gender"
        }
        create("women") {
            applicationId = "com.example.myapp.women"
            versionNameSuffix = "-women"
            buildConfigField("String", "THEME_COLOR", "\"pink\"")
            dimension = "gender"
        }
    }
}
```

## Source Sets
- A **source set** is a logical group of code and resources for a specific build variant scope.
- Typical Android source sets:
  - `src/main/` – shared across all variants.
  - `src/debug/`, `src/release/` – build-type-specific code/resources.
  - `src/free/`, `src/paid/` – flavor-specific code/resources.
  - `src/freeDebug/` – combination of flavor + build type.
  - `src/test/`, `src/androidTest/` – unit and instrumentation tests.
- Source sets let you override implementations or resources per build type/flavor (e.g., different API URLs, branding, or feature flags).


## Obfuscation (R8/ProGuard)
- Obfuscation renames classes, methods, and fields to meaningless names, making reverse engineering harder.
- R8 (which replaces ProGuard in modern Android builds) also performs code shrinking and optimization.
- You control behavior via ProGuard/R8 rules (e.g., `proguard-rules.pro`), keeping important classes (like reflection-based libraries, models used by Gson/Moshi, etc.) from being removed or renamed incorrectly.


## Build Variants in Android Studio
- The **Build Variants** window lets you choose which variant to run (e.g., `freeDebug`, `paidRelease`).
- A build variant is the combination of:
  - one build type (debug/release/staging), and
  - one flavor from each flavor dimension.


# `libs.versions.toml` (Version Catalogs)
- `libs.versions.toml` centralizes dependency versions for the whole Gradle project.
- Benefits:
  - Single source of truth for versions and coordinates.
  - Easier upgrades and consistency across modules.
  - Works well with Gradle’s `versionCatalogs` feature.

Example `libs.versions.toml`:
```toml
[versions]
kotlin = "1.8.10"
coroutines = "1.6.4"
compose = "1.3.0"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }

[plugins]
android-application = { id = "com.android.application", version = "7.4.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```


# Library Modules
- A **library module** is a Gradle module that produces a reusable artifact (AAR/JAR) consumed by other modules.
- In Android, you typically have:
  - `com.android.application` for app modules (produce APK/AAB).
  - `com.android.library` for Android libraries (produce AAR).
- Feature modules (e.g., `:feature:auth`) are often libraries consumed by the main `:app` module.


# `settings.gradle[.kts]`
- Defines which modules are included in the Gradle build and some global repository/plugin management.
- Located in the root project.

Example `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyMultiModuleProject"
include(":app")
include(":library1")
```


# Common Gradle Pitfalls (Android)
- **Misconfigured minSdk/targetSdk/compileSdk**
  - Using libraries that require a higher `minSdk` than your app supports.
  - Forgetting to bump `compileSdk` when adopting new AndroidX/Compose versions.
- **Dependency conflicts (version clashes)**
  - Different modules or libraries depending on incompatible versions of the same artifact.
  - Symptoms: weird runtime crashes, `NoSuchMethodError`, or build failures.
  - Tools: `./gradlew :app:dependencies`, Gradle’s dependency insights, version catalogs.
- **Overusing `implementation` vs `api`**
  - `api` leaks transitive dependencies across modules; prefer `implementation` for better encapsulation and faster builds.
- **Huge build times**
  - Too many modules without a good reason.
  - Applying heavy plugins to every module instead of only where needed.
  - Not using Gradle configuration cache / build cache.
- **Incorrect ProGuard/R8 rules**
  - Over-aggressive shrinking/obfuscation can break reflection-based libraries (Moshi, Gson, Retrofit, Koin, etc.).
  - Under-optimizing: never enabling minify for release results in larger APK/AAB sizes.
- **Hardcoding environment URLs in code**
  - Better: use `BuildConfig` fields or per-flavor resource values so environments are controlled by Gradle, not by manual code changes.
- **Mixing Groovy and Kotlin DSL without understanding**
  - Both can coexist, but be mindful of syntax differences when copying snippets from docs.


# Gradle-focused Interview Talking Points / Questions

## Conceptual
- **“Explain the difference between build types and product flavors.”**
  - Build types: how you build (debug vs release vs staging – logging, minify, debuggable).
  - Flavors: what you build (free vs paid, brand A vs brand B, region-specific variants).
  - A build variant = build type × flavors.

- **“How do you manage different environments (dev/stage/prod)?”**
  - Use build types or flavors with different `BuildConfig` fields (API URLs, feature flags).
  - Keep secrets out of source code; use CI or secure config where possible.

- **“What’s the difference between `implementation` and `api`?”**
  - `implementation`: dependency is internal to the module; consumers don’t see it.
  - `api`: dependency is exposed transitively to modules that depend on this module.
  - Prefer `implementation` to reduce coupling and build times.

- **“How do you reduce APK/AAB size and improve build performance?”**
  - Enable R8/ProGuard and resource shrinking in release.
  - Use `implementation` instead of `api` where possible.
  - Remove unused dependencies and resources.
  - Use Gradle build cache, configuration cache, and parallel execution.

## Practical / Scenario-based
- **“How would you add a new feature module to an existing app?”**
  - Add a new module in `settings.gradle` and create a `com.android.library` module.
  - Configure its own `build.gradle[.kts]` with only required plugins/dependencies.
  - Expose a clean API (e.g., navigation entry points, interfaces) to avoid tight coupling.

- **“How do you configure different app IDs or app names for QA vs Production?”**
  - Use `applicationIdSuffix`, `versionNameSuffix`, and/or flavor-specific `resValue`/resources (e.g., `app_name`) in Gradle.

- **“Tell me about a time Gradle caused issues and how you debugged it.”**
  - Talk about dependency conflicts, build time regressions, or ProGuard/R8 issues.
  - Mention tools like `./gradlew dependencies`, Gradle build scans, and systematically toggling plugins/options.

- **“How do you handle secrets or API keys with Gradle?”**
  - Don’t hardcode in source or commit to VCS.
  - Use `local.properties`, environment variables, or CI-injected Gradle properties.
  - Pass them into the app via `buildConfigField` or resource values, while keeping them out of git.


# Summary
- Gradle is central to how Android apps are built, packaged, and configured.
- Key levers you should know as a senior Android dev:
  - Build types, product flavors, and variants.
  - Dependency management (version catalogs, `implementation` vs `api`).
  - Obfuscation/shrinking with R8 & ProGuard.
  - Multi-module setup and plugin usage.
- In interviews, focus on **how** you’ve used Gradle to support:
  - multi-environment builds,
  - modularization and clean architecture,
  - performance & size optimizations,
  - and reliable CI/CD pipelines.
