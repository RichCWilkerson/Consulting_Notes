# Android Developer Crosswalk — Flutter, React Native, KMM

Purpose: Map familiar Android (Kotlin) concepts to Flutter, React Native, and KMM to speed up learning.

UI Layer
- Android: Activities/Fragments, Views, Jetpack Compose
- Flutter: Widgets (Stateless/Stateful), Composition via widget tree; analogous to Compose but with different lifecycle and rendering
- React Native: Components (Function + Hooks), JSX; layout via Flexbox; platform-native bridges
- KMM: No UI; share domain/data; UI remains native (Compose/SwiftUI)

Navigation
- Android: Jetpack Navigation / Compose Navigation
- Flutter: go_router or Navigator 2.0
- React Native: React Navigation (native-stack)
- KMM: Use platform navigation (Android Navigation, iOS Coordinators/SwiftUI Navigation)

State Management
- Android: ViewModel + StateFlow/LiveData; DI scoping
- Flutter: Provider/Riverpod/BLoC/Cubit; ValueNotifier; inherited widgets
- React Native: Hooks local state; Redux Toolkit/Zustand/Recoil for app state; React Query for server cache
- KMM: Flows from shared to platform ViewModels; map to Compose State/SwiftUI ObservedObject

Networking & Data
- Android: Retrofit + OkHttp + Moshi/Kotlinx Serialization; Room/SQLDelight; Paging 3
- Flutter: Dio/http + json_serializable/freezed; Drift (Moor) or Floor for DB; infinite_scroll/pagination libs
- React Native: fetch/Axios + zod/io-ts/yup for validation; SQLite/WatermelonDB/MMKV; React Query for caching
- KMM: Ktor client; kotlinx.serialization; SQLDelight multiplatform; expose repositories to platforms

Async/Concurrency
- Android: Coroutines/Flows; WorkManager
- Flutter: async/await + Streams; Isolates for CPU work
- React Native: Promise/async-await; native threads via JSI/TurboModules; Background tasks via native modules
  - TODO: native threads vs native modules?
- KMM: Coroutines across platforms (new memory model); platform schedulers

Dependency Injection
- Android: Hilt/Koin
- Flutter: get_it + injectable or rely on Riverpod providers
- React Native: DI is manual/context-based; hook composition; InversifyJS optional
- KMM: Koin/Kodein in shared; provide platform-specific modules

Build & Packaging
- Android: Gradle, modules, flavors
- Flutter: flutter build, flavors via build flavors; pubspec for deps
  - TODO: what are pubspec and deps?
- React Native: Gradle + Xcode projects under the hood; Metro bundler; yarn/npm for deps
- KMM: Gradle multiplatform; publish shared as Android AAR + iOS Framework

Native Interop
- Android: JNI/NDK; platform services/APIs
- Flutter: Platform Channels (or newer FFI for some cases)
- React Native: Native Modules (TurboModules/JSI) and Fabric for UI
- KMM: expect/actual; platform-specific source sets; direct Android/iOS APIs

Testing
- Android: JUnit, Espresso/Compose UI, MockWebServer, Robolectric
- Flutter: flutter test, widget tests, integration_test
- React Native: Jest + React Testing Library; Detox for E2E
- KMM: Common tests in shared; androidTest/iOS tests on platforms

Performance Focus
- Android: avoid main-thread work, profile with Studio/Perfetto; startup budgets
- Flutter: minimize rebuilds; leverage const, keys, isolates; profile with DevTools
- React Native: minimize re-renders; memoization; Hermes; native-stack; Flipper
- KMM: shared logic speed is native; mind bridging overhead and allocations

Security
- Android: Keystore, Network Security Config, pinning with OkHttp
- Flutter: use platform keystores via plugins; HTTP pinning (dio_certificate_pin)
- React Native: Keychain/Keystore via libraries; SSL pinning libs; secure storage
- KMM: do security-sensitive storage on platform; expose safe APIs from shared

