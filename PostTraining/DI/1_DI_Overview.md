# Dependency Injection (DI) – Overview

## Resources
- [Hilt (official docs)](https://developer.android.com/training/dependency-injection/hilt-android)
- [Dagger 2 (official docs)](https://dagger.dev/dev-guide/)
- [Koin](https://insert-koin.io/)

- [Hilt vs Koin vs Manual - Youtube](https://www.youtube.com/watch?v=_qb87PN7jlI)

---

## What is Dependency Injection (DI)?

### Dependency Injection
Replaceable components without changing dependent code
- action figure with interchangeable arms (sword, shield, etc)

Attempts to implement Inversion of Control (IoC) principle (DIP - Dependency Inversion Principle - SOLID)
- high-level modules should not depend on low-level modules; both should depend on abstractions
- abstractions should not depend on details; details should depend on abstractions

**Short definition**
- *Dependency Injection* is a way to **provide an object with the things it depends on (dependencies) from the outside**, instead of the object creating them itself.
- "Don't call `new` inside the class for important collaborators; have them passed in."

**Example (no DI)**
```kotlin
class LoginRepository {
    private val api = Retrofit.Builder()  // tightly coupled
        .baseUrl("https://example.com")
        .build()
        .create(LoginApi::class.java)
}
```
- `LoginRepository` **decides** how to construct `LoginApi` → hard to test, hard to swap.

**Example (with DI)**
```kotlin
class LoginRepository(
    private val api: LoginApi
)
```
- Construction is done elsewhere (manual wiring, Hilt, Koin, etc.).
- `LoginRepository` only *declares* what it needs.

---

## Why use Dependency Injection?

Core motivations:
- **Testability**
  - Easily pass **fakes/mocks** instead of real implementations.
  - Unit tests don't need real databases, network, or Android framework.
- **Loosely coupled design**
  - Classes depend on **interfaces** or abstractions, not concrete implementations.
  - Easier to swap implementations (e.g., `RealPaymentService` vs `FakePaymentService`).
- **Single Responsibility Principle**
  - Classes focus on *behavior*, not object creation and configuration.
- **Configurability**
  - You can change wiring per build type (debug vs release), per environment (dev/stage/prod).
- **Scalability**
  - As projects grow, a DI framework helps manage object graphs and lifecycles.

How to phrase in an interview:
> “DI lets me design classes that state *what* they need, while something else decides *how* to build those dependencies. 
> It improves testability, reduces coupling, and keeps construction logic centralized instead of scattered through the codebase.”

---

## Quick Glossary of Terms

- **Dependency**: Any object your class needs to do its work (e.g., `UserRepository` depends on `UserApi`, `UserDao`).
- **Injection**: The act of passing those dependencies into a class (via constructor, function, or field).
- **Injector / Container**: The thing that knows **how to build and provide** dependencies (Dagger, Hilt, Koin, manual factory).
- **Object Graph**: The full set of objects + their relationships in your app.
- **Service Locator**: A central registry you call to get dependencies (e.g., `ServiceLocator.get<UserRepository>()`). Similar goal to DI but **pull-based** instead of **push-based**.
- **Module (DI module)**: Code that describes **how to construct** and **scope** dependencies (Hilt modules, Koin modules, etc.).
- **Scope**: Lifetime of an object (e.g., app‑scope singleton, activity‑scope, fragment‑scope, request‑scope).
- **Reflection vs Code Generation**:
  - Reflection: Inspecting types and creating objects at runtime (slower, used by some DI frameworks).
  - Code Generation: Generating code at compile time to wire dependencies (faster, used by Dagger/Hilt).

---

## Types of DI (Patterns)

### 1. Constructor Injection (preferred)

```kotlin
class UserViewModel(
    private val repository: UserRepository,
    private val logger: Logger
)
```
- Dependencies are required and clearly visible.
- Easiest to test.
- Works well with DI frameworks and with manual wiring.

### 2. Method / Parameter Injection

```kotlin
fun syncUsers(repository: UserRepository) {
    // repository is injected at call site
}
```
- Dependency is provided **per call**.
- Good for "one‑off" dependencies or utility functions.

### 3. Property / Field Injection

```kotlin
class AnalyticsTracker {
    lateinit var logger: Logger // to be injected later
}
```
- Common in Java and some frameworks.
- In Kotlin/Android, used when frameworks create instances (Activities, Fragments) so you can't control the constructor.
- More error‑prone (lateinit, nullability), so prefer constructor injection where possible.
- hard to test without a DI framework.

### 4. Manual DI (Pure Kotlin, No Framework)

```kotlin
object AppContainer {
    val retrofit: Retrofit = Retrofit.Builder().build()
    val api: UserApi = retrofit.create(UserApi::class.java)
    val repository: UserRepository = UserRepository(api)
}

class UserViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(AppContainer.repository) as T
    }
}
```
- Simple, explicit, and great for **small apps** or learning.
- Becomes difficult to maintain as the graph grows.

### 5. Service Locator (anti‑pattern adjacent)
Anti‑pattern because it hides dependencies.

```kotlin
object ServiceLocator {
    lateinit var repository: UserRepository
}

class UserViewModel { 
    private val repository = ServiceLocator.repository
}
```
- **Pull-based**: classes go ask the locator for dependencies.
- Easier to get started but **harder to test** and hides dependencies.
- Many teams treat this as an **anti‑pattern** and prefer constructor injection + DI framework.

---

## Technologies for DI in Android

You don't need to master every library. As a modern Android dev, you should:
- **Be fluent with one major framework** (Hilt or Koin).
- **Understand conceptually** what Dagger 2 does under the hood.

### Hilt (Dagger on top, Google‑blessed)
- Built on top of **Dagger 2**, but with Android‑specific integrations and annotations.
- Tight integration with:
  - `Application`, `Activity`, `Fragment`, `ViewModel`, `Service` lifecycles.
  - Navigation, WorkManager, and other Jetpack libs via extensions.
- **Pros:**
  - Officially recommended by Google.
  - Compile‑time validation and codegen → very fast at runtime.
  - Strong tooling and documentation.
- **Cons:**
  - Annotation‑heavy and can feel "magic" at first.
  - Build times can increase on very large graphs.

### Dagger 2 (low‑level, general DI)
- Powerful, compile‑time DI framework; Hilt is a layer on top.
- More boilerplate and manual wiring than Hilt.
- Today, most **new Android projects use Hilt instead of raw Dagger 2**.

### Koin (pure Kotlin, runtime DI)
- **DSL‑based**: you declare modules in Kotlin code.
- Runtime resolution (no codegen).
- **Pros:**
  - Easy to get started, very readable.
  - Great for prototypes, small/medium apps, or teams that dislike heavy annotation processing.
  - Pure Kotlin, no Java interop needed. Meaning we can use:
    - Kotlin features like coroutines, extension functions, etc. seamlessly.
    - KMP - Hilt cannot be used in KMP
- **Cons:**
  - Errors detected at runtime instead of compile time.
    - THIS IS A MAJOR TRADE-OFF.
  - Slight runtime overhead vs generated code.

### Others (historical/less common today)
- **Dagger 1** – legacy, replaced by Dagger 2.
- **Kodein** – runtime DI with a nice DSL; less common in new projects.

Interview framing:
> “On modern Android I prefer Hilt for production apps because it’s the Google‑supported layer on top of Dagger with strong lifecycle integration. 
> For smaller or experimental apps I might use Koin or manual DI to keep things simple.”

---

## When to Use Dependency Injection

Use DI (and usually a DI framework) when:
- Your app has **non‑trivial business logic** or multiple data sources (network, DB, cache).
- You want **good test coverage**, especially for ViewModels, repositories, and use cases.
- You support **multiple environments** (dev/stage/prod) or feature flags.
- Many classes share cross‑cutting dependencies (logging, analytics, config, auth, etc.).

You can start with **manual DI** and migrate to Hilt/Koin as the project grows.

---

## When Not to Use Heavy DI

- Very **small apps** or throwaway prototypes where testability and long‑term maintainability are not important.
- Tiny utilities or samples where DI would add more noise than value.
- In these cases, you can still **apply DI principles** (constructor injection, interfaces) without pulling in a full framework.

---

## Benefits of Dependency Injection

From an engineering / interview perspective:
- **Improved Testability**
  - Swap out real implementations with fakes/mocks.
  - Run fast unit tests without hitting network or disk.
- **Decoupling & Flexibility**
  - Classes focus on *what* they do, not *how* dependencies are built.
  - Easier refactors and implementation swaps.
- **Clear Architecture Boundaries**
  - Helps enforce layers (UI → domain → data).
  - Makes it obvious where cross‑cutting concerns live (auth, logging, analytics).
- **Lifecycle Management**
  - DI containers manage scopes (Application, Activity, Fragment, ViewModel) for you.
- **Reusability**
  - Modules can be shared across apps or libraries (e.g., a core networking module).

---

## Drawbacks of Dependency Injection

- **Learning curve**
  - Frameworks like Hilt/Dagger introduce new annotations, concepts, and build steps.
- **Indirection / "Magic"**
  - Dependencies are wired in config/modules instead of directly in code.
  - Can be harder for newcomers to trace where something comes from.
- **Build Complexity**
  - Annotation processing (Dagger/Hilt) can slow builds on large codebases.
- **Over‑engineering Risk**
  - Using a full DI framework for a very small app can be unnecessary.

In interviews, acknowledge these trade‑offs and show that you **use DI pragmatically**, not dogmatically.

---

## Common Pitfalls (Android‑Specific)

- **Overusing singletons**
  - Making everything a singleton via DI defeats proper scoping and can cause leaks.
- **Injecting Android framework types everywhere**
  - Prefer injecting **abstractions** (e.g., `Clock`, `Navigator`, `ResourceProvider`) instead of `Context`/`Activity`.
- **Hidden dependencies**
  - Long chains of modules and qualifiers can make it unclear what a class really needs.
  - Good practice: keep constructor parameters explicit and small.
- **Leaking scopes**
  - Injecting a long‑lived dependency (Application scope) into a short‑lived one is fine.
  - The reverse (short‑lived into long‑lived) can cause leaks and subtle bugs.
- **Runtime crashes with runtime DI (Koin, Service Locator)**
  - Misconfigured modules show up only at runtime instead of compile time.
- **Tight coupling to the DI framework**
  - Spreading Hilt/Koin annotations and APIs into business logic instead of keeping them at the edges.

---

## Best Practices

- **Favor constructor injection**
  - Make dependencies explicit and required.
- **Depend on interfaces, not implementations**
  - e.g., `UserRepository` interface, with `RealUserRepository` and `FakeUserRepository` implementations.
- **Keep DI wiring at the boundaries**
  - Activities, Fragments, and Application classes know about the DI framework.
  - Domain and data layers should mostly know only about plain Kotlin interfaces.
- **Use scopes intentionally**
  - Application‑scope for things like Retrofit, databases, and global loggers.
  - Activity/Fragment/ViewModel scopes for UI‑related objects.
- **Separate modules by feature or layer**
  - e.g., `NetworkModule`, `DatabaseModule`, `AuthModule`, `FeatureXModule`.
- **Write tests that bypass or minimize the DI framework**
  - Use constructor injection so tests can create objects directly without needing the whole container.

---

## Interview Questions & Talking Points

Use this section to practice short, clear answers.

1. **Explain dependency injection to a junior dev.**
> “Instead of classes creating their own collaborators, we pass those collaborators in. 
> This makes the code easier to test and swap implementations.”

2. **How does DI improve testability?**
> “I can inject fakes/mocks instead of real network/database. 
> That lets me unit test logic in isolation without slow or flaky external dependencies.”

3. **Constructor vs field injection – which do you prefer and why?**
> “Constructor injection is my default because it makes dependencies explicit and required. 
> Field injection is reserved for framework‑owned types like Activities or when the framework controls instantiation.”

4. **Have you used Hilt / Koin / Dagger? How did you structure modules?**
> Mention: app‑wide modules (network, database, logging), feature‑specific modules, and how you scoped dependencies (Application vs Activity vs ViewModel).

5. **What’s the difference between DI and a Service Locator?**
> “With DI, dependencies are pushed into a class from the outside (constructor). 
> With a service locator, the class pulls dependencies from a global registry, which hides dependencies and makes testing harder.”

6. **When would you avoid introducing a DI framework?**
> “Very small or short‑lived apps, where manual constructor wiring is enough and a full framework would just add complexity.”

---

> Use this overview as your mental model. 
> Individual files like `Hilt.md` or `Koin.md` can then focus on the *syntax and setup* for each technology, 
> but your core DI story in interviews should be about **testability, decoupling, and pragmatic use of frameworks.**
