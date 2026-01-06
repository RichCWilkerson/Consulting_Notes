# Cross-Platform Mobile — Detailed Breakdown

Goal: Pick the right stack per product and be effective in day-to-day engineering.

Contents
- Flutter (Dart)
- React Native (TypeScript)
- Kotlin Multiplatform Mobile (KMM)
- Decision guidance and migration notes

---

## Flutter (Dart)
What/Why
- Single codebase; pixel-precise rendering via Skia; great for greenfield and custom UI.
  - TODO: what is greenfield?
  - TODO: what is Skia?
- Strong tooling (Hot Reload, DevTools), predictable UI with declarative widgets.

Core Concepts
- Widget tree; Stateless vs Stateful widgets (like composables but with different lifecycle)
- State management: Provider, Riverpod, BLoC/Cubit, GetX
  - TODO: Provider equiv to Android ViewModel?
  - TODO: Riverpod equiv to Android Hilt?
  - TODO: BLoC/Cubit equiv to Android LiveData?
- Navigation: go_router, Navigator 2.0
  - TODO: Navigator 2.0 equiv to Android NavController?
- Platform channels for native integrations

Tooling
- flutter SDK, Android Studio/IntelliJ/VSCode
- DevTools (CPU, memory, widget rebuilds)
- flutter analyze: static analysis
- flutter format: code formatting
- flutter test: unit/widget tests

Performance Checklist
- Minimize widget rebuilds (keys, const constructors)
  - recomposition
  - TODO: equivalent to remember in Jetpack Compose?
- Avoid heavy work on main isolate; use Isolates/compute
  - TODO: is this equivalent to coroutines in Android? Don't run on Main thread/dispatchers.Main?
- Defer image decoding, cache images (cached_network_image)
  - TODO: equivalent to Coil/Glide in Android?
- Use Impeller (iOS) if applicable; profile jank with frame chart
  - TODO: what is Impeller? Is it equivalent to anything in Android?

Pitfalls
- Larger bundle size than pure native
- Platform channels add complexity for advanced native APIs
- Web and desktop support improving but not parity with mobile

Learning Plan (1–2 weeks)
- Day 1–2: Dart syntax, async/await, isolates
- Day 3–5: Widgets, state management (pick Riverpod or BLoC), navigation
- Day 6–7: HTTP, persistence, DI (get_it or riverpod), theming
- Day 8–10: Platform channels, testing, profiling

---

## React Native (TypeScript)
What/Why
- Leverage web/JS/TS ecosystem; fast iteration; wide library support.
- New Architecture (Fabric + TurboModules + JSI) improves performance and native interop.

TODO: Android equivalents for the following?
Core Concepts
- Functional components; Hooks (useState/useEffect/useMemo)
- State management: Redux Toolkit, Zustand, Recoil, MobX
- Navigation: React Navigation, native-stack
- Native modules: TurboModules/JSI for perf

Tooling
- TypeScript + ESLint + Prettier
- Metro bundler; Hermes engine
- Flipper for debugging (network, layout, perf)

Performance Checklist
- Prefer Hermes; enable inline requires; use production JS minifier
- Avoid heavy renders; FlatList with proper keys/windowing; memoize components
- Offload compute to native or worker threads (react-native-reanimated/JSI)
- Use native-stack navigation; avoid unnecessary re-renders via memo/useCallback

Pitfalls
- Upgrades can be painful; keep close to latest
- Native modules sometimes unmaintained — verify support before adopting

Learning Plan (1–2 weeks)
- Day 1–2: TypeScript essentials, RN project structure
- Day 3–5: Components, hooks, navigation, forms
- Day 6–7: Data fetching, caching (React Query), theming
- Day 8–10: Native modules, performance, testing (Jest, Detox)

---

## Kotlin Multiplatform Mobile (KMM)
What/Why
- Share business logic (data/domain) across Android/iOS; keep native UI per platform.
- Keep native look/feel and native performance.

Core Concepts
- commonMain/shared module; expect/actual for platform-specific APIs
- Networking (Ktor), serialization (kotlinx.serialization)
- Persistence (SQLDelight, realm-kotlin) with multiplatform drivers
- DI: Koin or Kodein
- Concurrency: Kotlin Coroutines and flows (consider new memory model)

iOS Interop
- Export shared as a Framework via CocoaPods or SPM
- Swift/ObjC callers use generated APIs; design Swift-friendly APIs

Android Interop
- Use as a regular Gradle module dependency
- Compose/View UI consumes shared via Repository/UseCase interfaces

Testing
- Common tests in commonTest
- Platform-specific tests in androidTest and ios tests in Xcode

Performance Checklist
- Use immutable DTOs; avoid passing large graphs across boundaries
- Consider background workers on each platform for long-running tasks
- Carefully design concurrency (freeze not needed in new memory model)

Pitfalls
- Tooling evolves; pin plugin/Gradle versions
- iOS crash stacks from shared code need symbolization; invest in crash reporting

Learning Plan (1–2 weeks)
- Day 1–2: Set up KMM sample; expect/actual, basic Ktor/serialization
- Day 3–5: SQLDelight, Repository pattern, DI
- Day 6–7: iOS/Swift integration; Cocoapods/SPM
- Day 8–10: Testing, CI, versioning strategy

---

## Decision Guidance
Choose Flutter when
- You control both apps and want fully shared UI with high design fidelity
- You need to move fast with a cohesive UI toolkit

Choose React Native when
- You already have web/JS expertise or shared business logic in TS
- You need lots of 3rd-party libraries and quick iteration

Choose KMM when
- You want native UI with shared domain/data logic
- You care about platform-specific polish and performance

Migration Notes
- Start with a vertical slice; agree on architecture and module layout
- Define API boundaries early (what is shared vs platform-specific)
- Set up CI, code style, and dependency update cadence from day 1

---

## Android Engineer Notes
- In React Native, keep business logic in TypeScript modules and isolate native bindings. 
  - Reach for JSI/TurboModules only for perf-critical paths.
    - TODO: what is JSI/TurboModules?
    - TODO: what is perf-critical paths?
- For KMM, start by sharing only DTOs, networking, and repositories; keep DI boundaries clear and design Swift-friendly APIs (value types, minimal nullable usage).
- Prefer vertical slices: build the same "list → detail → edit" flow in all three stacks to compare ergonomics and performance.
- Establish perf/testing baselines upfront: cold start, 60fps scroll, unit/E2E counts. Re-measure after changes.
  - TODO: what does "cold start" mean?
  - TODO: what does "60fps scroll" mean?
  - TODO: what does "unit/E2E counts" mean?
