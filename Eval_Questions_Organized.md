# Android Senior Interview – Organized Q&A

> This file is the **organized, de-duplicated master version** of `Eval_Questions.md`.
> For each topic:
> - **Master Question** – what they might ask.
> - **High-Level (Student)** – simple explanation to ground yourself.
> - **Interviewer Details** – tradeoffs, differences, and examples.
> - **Succinct Interview Answer** – what you can actually say out loud.
>
> Use this file to practice answers. Use the original `Eval_Questions.md` as raw notes/code.

---

## 1. Kotlin Fundamentals & Language Features

### 1.1 Null Safety & Nullable Handling

**Master Question**  
How does Kotlin help you avoid `NullPointerException` compared to Java, and how do you handle nulls idiomatically?

**High-Level (Student)**
- Kotlin has **nullable** (`String?`) and **non-nullable** (`String`) types.
- Compiler forces you to handle `String?` safely using things like `?.`, `?:`, and `let`.
- `lateinit` and `lazy` handle cases where you can’t initialize immediately.

**Interviewer Details**
- **Type system**:
  - `String` cannot hold null; `String?` can.
  - Any access on `String?` requires explicit handling.
- **Operators & patterns**:
  - Safe call: `name?.length` – returns `null` instead of throwing.
  - Elvis: `displayName = user?.name ?: "Guest"` – default on null.
  - Not-null assertion: `user!!` – throws NPE if actually null; treat as last resort.
  - Scope functions: `name?.let { /* it is non-null */ }` keeps null logic localized.
- **Initialization helpers**:
  - `lateinit var x: Foo`: non-null property initialized later (no primitives, must be `var`).
  - `val x by lazy { expensiveInit() }`: computed on first access and then cached.
- **API design**:
  - Prefer non-null parameters/returns.
  - Only use nullable types where `null` is a real, meaningful state.

**Succinct Interview Answer**
> Kotlin moves null safety into the type system. 
> Types are either nullable, like `String?`, or not, like `String`, and the compiler forces you to handle the nullable ones with things like safe calls and the Elvis operator. 
> I try to keep null-handling very local using `let` and avoid `!!` as much as possible. 
> For initialization cases, I’ll use `lateinit` or `lazy` instead of propagating nulls. 
> Designing APIs to be non-null by default means most potential NPEs are caught at compile time instead of in production.

---

### 1.2 Enums vs Sealed Classes

**Master Question**  
When do you choose an `enum class` vs a `sealed class` in Kotlin?

**High-Level (Student)**
- **Enum**: simple, fixed set of constants that are all the same shape.
- **Sealed class**: fixed set of **different** variants, each with its own data.

**Interviewer Details**
- **Enum**:
  - Great for flags or modes: days of week, theme, sort order.
  - All enum entries share the same constructor parameters.
  - Can have methods/properties and implement interfaces; cannot be extended like a class hierarchy.
  - `when(enumValue)` can be exhaustive, but payload per case is limited.

- **Sealed class / interface**:
  - Defines a **closed hierarchy** – all subclasses in the same module or file.
  - Each subclass can have distinct properties and behavior.
  - `when` over a sealed type can be fully exhaustive without an `else`.
  - Typical examples: `UiState` (`Loading`, `Success(data)`, `Error(message)`), `Result`, navigation destinations, error types.

**Succinct Interview Answer**
> I use enums when I just need a simple fixed set of constants, like a theme or sort order, where every value is the same shape. 
> I use sealed classes when the variants need different data or behavior, like `UiState.Loading`, `UiState.Success(data)`, and `UiState.Error(message)`. 
> Sealed classes give me exhaustive `when` checks and strongly typed payloads, which is ideal for modeling states and results.

---

### 1.3 Scope Functions (`let`, `run`, `apply`, `also`, `with`)

**Master Question**  
What are Kotlin scope functions and when do you use `let`, `run`, `also`, `apply`, and `with`?

**High-Level (Student)**
- They’re helper functions to execute a block with an object in scope.
- Differ by **receiver name** (`this` vs `it`) and **return value** (object vs lambda result).

**Interviewer Details**
- **`let`**: object is `it`, returns lambda result.
  - Use for null-checks or short, chained transformations.
- **`run`**: object is `this`, returns lambda result.
  - Use for executing a block and producing a result, often initialization.
- **`also`**: object is `it`, returns the original object.
  - Use for side effects like logging or debugging in a fluent chain.
- **`apply`**: object is `this`, returns the original object.
  - Use for configuring objects, e.g., builder-style initialization.
- **`with`**: takes the object as an argument, `this` inside, returns lambda result.
  - Use when you already have the object and want to call multiple methods on it.

**Succinct Interview Answer**
> I think of scope functions as a quick way to run a block with an object in scope. 
> `let` and `run` return the lambda result, whereas `apply` and `also` return the original object. 
> I reach for `let` for null-safe chains, `apply` to configure an object, `also` for side effects like logging, and `run` or `with` when I want to compute a value while using `this` instead of repeating the receiver.

---

### 1.4 Lambdas & Higher-Order Functions

**Master Question**  
What are lambdas and higher-order functions in Kotlin, and why are they useful?

**High-Level (Student)**
- **Lambda**: anonymous function you can pass around.
- **Higher-order function**: takes a function as a parameter or returns one.

**Interviewer Details**
- Encourages **functional style**: mapping, filtering, composing behavior.
- Used heavily in Kotlin APIs (`map`, `filter`, `let`, `onClick` handlers, coroutines builders).
- Enables concise DSLs (e.g., Gradle Kotlin DSL, Ktor, Compose).

**Succinct Interview Answer**
> Lambdas are just anonymous functions you can pass as values, and higher-order functions are functions that take or return other functions. 
> They’re a big part of Kotlin’s expressiveness; you see them in collections APIs, coroutines, and Compose all the time. 
> They let you write behavior as data, which simplifies things like callbacks, transformations, and configuration DSLs.

---

### 1.5 `lateinit` vs `lazy`

**Master Question**  
What’s the difference between `lateinit` and `lazy`, and when do you use each?

**High-Level (Student)**
- `lateinit` = “I’ll initialize this non-null var later, but before use.”
- `lazy` = “I’ll compute this val once, when it’s first needed.”

**Interviewer Details**
- **`lateinit var`**:
  - Non-null, mutable property initialized after construction (e.g., DI, view binding).
  - Not allowed for primitives.
  - Access before init throws `UninitializedPropertyAccessException`.

- **`val x by lazy { ... }`**:
  - Read-only, lazily initialized on first access.
  - Can specify thread-safety mode if needed.
  - Ideal for expensive computations or resources you may not need.

**Succinct Interview Answer**
> I use `lateinit` for non-null vars that can’t be initialized in the constructor, like injected dependencies or Android view bindings. 
> I use `lazy` for read-only values that are expensive to create and should only be initialized when first accessed. 
> So `lateinit` is about “I promise to set this before I use it,” while `lazy` is about “compute this when needed and then cache it.”

---

## 2. Coroutines, Flows, and Concurrency

### 2.1 What is a Coroutine (vs Threads)?

**Master Question**  
How do you explain coroutines to someone familiar with threads? Why are they useful on Android?

**High-Level (Student)**
- Threads are OS-level; coroutines are **lightweight units** of work managed in software.
- Coroutines can **suspend and resume** without blocking a thread.

**Interviewer Details**
- **Threads**:
  - Map to OS resources, more expensive to create/switch.
  - Blocking a thread (e.g., network call on main thread) can cause ANR.

- **Coroutines**:
  - Run on top of thread pools and can be thousands per thread.
  - Suspending a coroutine frees its thread to do other work.
  - Concepts:
    - **Scope**: how long a coroutine should live (`viewModelScope`, `lifecycleScope`, custom scopes).
    - **Dispatcher**: which threads to use (`Main`, `IO`, `Default`, `Unconfined`).
    - **Builders**: how to start work (`launch` for side effects, `async` for concurrent results, `runBlocking` only for tests/CLI).
    - **Structured concurrency**: child coroutines are tied to parents; cancellations propagate predictably.

**Succinct Interview Answer**
> I explain coroutines as lightweight, cooperative tasks that can suspend without blocking a thread. 
> Threads are OS-level and expensive, whereas you can run thousands of coroutines on a small thread pool. 
> On Android that matters because we can move blocking work off the main thread and still write code that looks sequential, with suspend functions instead of callbacks. 
> Scopes, dispatchers, and structured concurrency give us control over where work runs and how it’s canceled.

---

### 2.2 Coroutine Scopes (`viewModelScope`, `lifecycleScope`, custom)

**Master Question**  
What coroutine scopes do you use on Android and when (e.g., `viewModelScope`, `lifecycleScope`, `rememberCoroutineScope`, custom scopes)?

**High-Level (Student)**
- Scopes define the **lifetime** of coroutines.
- Use **ViewModel scope** for screen logic, **lifecycle scope** for UI side effects, **custom** scopes for long-running background work.

**Interviewer Details**
- **`viewModelScope`**:
  - Tied to ViewModel lifecycle; cancelled when ViewModel is cleared.
  - Default for loading data, combining repos, updating `UiState`.

- **`lifecycleScope`** (Activity/Fragment):
  - Tied to LifecycleOwner; cancelled on destruction.
  - Good for XML-based UI, dialogs, permission flows, and collecting Flows.

- **`rememberCoroutineScope`** (Compose):
  - Tied to composable’s lifespan.
  - Great for UI events like snackbars or animations.

- **Custom `CoroutineScope`**:
  - E.g., `CoroutineScope(SupervisorJob() + Dispatchers.IO)` in a repo.
  - Use for long-lived background tasks independent of UI lifecycle, but manage cancellation explicitly.

**Succinct Interview Answer**
> I treat scopes as ownership of work. 
> `viewModelScope` owns anything tied to screen state, 
> `lifecycleScope` or `rememberCoroutineScope` own UI-related work, 
> and I’ll create a custom scope with a `SupervisorJob` in something like a repository if I really need work to outlive the UI. 
> That way, when a ViewModel or Activity is destroyed, all its work is automatically canceled, which avoids leaks and wasted work.

---

### 2.3 `launch` vs `async` vs `withContext`

**Master Question**  
What’s the difference between `launch`, `async`, and `withContext`, and when would you use each?

**High-Level (Student)**
- `launch` → start a coroutine for **side effects**, no direct result.
- `async` → start a coroutine that **returns a result** via `Deferred`.
- `withContext` → **switch dispatcher** inside a suspend function.

**Interviewer Details**
- **`launch`**:
  - Returns a `Job` – you can cancel or `join` it.
    - `join` -> blocks until the coroutine completes.
  - Use for tasks that update state, show toasts, write to DB, etc.

- **`async`**:
  - Returns a `Deferred<T>` – call `await()` to get result.
  - Best when you need to run things concurrently and then combine results.
    - e.g. dashboard loading multiple data sources in parallel.
  - Should be used under structured contexts (e.g., `coroutineScope { ... }`).
    - structured contexts = scope blocks where async and await are handled properly.

- **`withContext`**:
  - Doesn’t create a new coroutine; it suspends and resumes on a different dispatcher.
  - Ideal pattern in repos: `withContext(Dispatchers.IO) { api.getStuff() }`.

**Succinct Interview Answer**
> I use `launch` for fire-and-forget side effects like updating state or writing to disk. 
> When I need to run several operations concurrently and then combine the results, I’ll use `async` and `await` inside a parent coroutine or `coroutineScope`. 
> For simple blocking work inside a suspend function, I prefer `withContext(Dispatchers.IO)` to hop off the main thread without creating extra Jobs that I have to manage.

---

### 2.4 Flows: StateFlow, SharedFlow, Cold Flow

**Master Question**  
How do you compare cold `Flow`, `StateFlow`, and `SharedFlow`, and where would you use each in an Android app?

**High-Level (Student)**
- Cold `Flow` = pipeline that runs when you collect.
- `StateFlow` = current state holder, always has a value.
- `SharedFlow` = configurable broadcast for events or shared streams.

**Interviewer Details**
- **Cold Flow:**
  - No work until you call `collect()`.
  - Producer runs per collector.
  - Good for one-off or request/response style streams.
  - performance for lazily fetching data when requested

- **StateFlow:**
  - Hot, always has **current value**.
  - New collectors immediately get **latest** value.
    - count from 1 to 10 with 2 sec delay (network) between counts, if a new value is produced every 1 sec delay (computation), you will see only the latest value (10)
      - because previous values are cancelled when a new one is produced
  - Perfect for UI state from ViewModels, can be easily exposed to Compose or Views.

- **SharedFlow:**
  - Hot, optional replay buffer and configurable backpressure.
    - replay buffer = how many previous values to cache for new subscribers
    - backpressure = what to do when buffer is full (suspend, drop oldest, drop latest)
  - Ideal for **events** (navigation, toasts), multi-subscriber streams, or complex broadcast scenarios.
  - Uses `emit()`/`tryEmit()` to send events.
  - in the count from 1-10 example you will see all values (queued) because SharedFlow does not cancel previous emissions when a new one is produced

**Succinct Interview Answer**
> I use cold Flows when I want a pipeline that only runs on collection, like a database query or network call. 
> `StateFlow` is my go-to for UI state: it always has a current value and new collectors get that immediately. 
> `SharedFlow` is what I use for one-time events or broadcasts, configured with replay and buffer as needed. 
> In a ViewModel that usually looks like `StateFlow<UiState>` plus a `SharedFlow<UiEvent>` that the UI collects.

---

### 2.5 `SupervisorJob` vs `Job`

**Master Question**  
What’s the difference between `Job` and `SupervisorJob` and when do you use a supervisor?

**High-Level (Student)**
- Normal `Job`: if one child fails, all siblings and the parent are canceled.
- `SupervisorJob`: children can fail **independently**.

**Interviewer Details**
- **Job**:
  - Default behavior in structured concurrency.
    - concurrency = managing multiple coroutines running at the same time
  - Good when children’s success/failure are tightly coupled.
    - only when running the coroutine in the same scope

```kotlin
// these do not cancel each other if one fails
viewModelScope.launch {
    // get data
}
viewModelScope.launch {
    // log analytics
}

// these cancel each other if one fails
viewModelScope.launch {
    coroutineScope {
        launch {
            // get data
        }
        launch {
            // log analytics
        }
    }
}
```

- **SupervisorJob**:
  - Use when you want to **isolate failures**.
  - Example: dashboard loads header, list, and badge in parallel. If badge fails, you still want header and list.

```kotlin
// these do not cancel each other if one fails
viewModelScope.launch {
    supervisorScope {
        launch {
            // load header
        }
        launch {
            // load list
        }
        launch {
            // load badge
        }
    }
}
```

**Succinct Interview Answer**
> With a normal `Job`, if one child coroutine throws an exception the whole scope is canceled, including its siblings. 
> A `SupervisorJob` changes that so children can fail independently. 
> I use supervisors where partial failure is acceptable, like loading several panels on a screen in parallel—if loading one panel fails, the others can still complete and render.

---

### 2.6 Flow vs Coroutine (Single vs Stream)

**Master Question**  
When would you use a simple coroutine returning a single value vs a `Flow` returning a stream of values?

**High-Level (Student)**
- Use a **suspend function** for a single result.
- Use `Flow` for **multiple values over time**.

**Interviewer Details**
- **Suspend functions**:
  - Suitable for API calls, single DB queries, or computations that return exactly one result.

- **Flows**:
  - Good for continuous updates: DB change streams, sensor updates, search input, pagination.
  - Composable with operators like `map`, `filter`, `debounce`, `combine`.

**Succinct Interview Answer**
> If I just need a single response—like fetching a user profile—I’ll use a suspend function and return the result directly. If I’m modeling a stream of values over time, like database changes, search suggestions, or paginated data, I’ll use a Flow because it can emit multiple values and compose nicely with operators like `debounce` and `combine`.

---

### 2.7 Coroutine Coding Challenge

**Master Question**
What is the printed output of the following code snippet?

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // my understanding is a is printed first because lifecycleScope is tied to the main thread and has already been started by onCreate
    lifecycleScope.launch { 
        println("a")
    }
    
    GlobalScope.launch { 
        println("b")
    }
    
    CourotineScope(Dispatchers.Main).launch {
        println("c")
    }
    
    println("d")
}
```
**OUTPUT**: d, a, c, b (b may appear earlier or later depending on thread scheduling)
**REASONING**: 
- coroutines launch work to be done asynchronously when the main thread is free
- d prints first since it is on the main thread and synchronous
- a and c print next since they are launched on the main thread's dispatcher
- b prints last since GlobalScope uses a background thread pool (Default) and may be delayed

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
        val job = launch {
            println("a")
        }
        job.join()
        println("b")
    }

    println("c")
}
```
**OUTPUT**: c, a, b
**REASONING**:
- c prints first since it is on the main thread and synchronous
- a prints next since job.join() waits for a to finish before proceeding
- b prints last after a is complete

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val scope = CoroutineScope(Dispatchers.Main)
    val job1 = scope.launch {
        println("1")
    }
    job1.join() // blocks until job1 is complete (true for any dispatcher)
    
    lifecycleScope.launch {
        println("a") // runs on Main

        val job = launch(Dispatchers.IO) {
            println("b") // runs on IO thread
        }

        job.join()      // wait for b to finish

        withContext(Dispatchers.Main) {
            println("c") // back on Main
        }
    }

    println("d")
}
```
**OUTPUT**: 1, d, a, b, c
**REASONING**:
- 1 prints first since job1.join() waits for it to finish before proceeding

---

## 3. State Management & Compose

### 3.1 State vs Stateless Composables

**Master Question**  
What’s the difference between stateful and stateless composables, and how do you decide where state lives?

**High-Level (Student)**
- **Stateful** composable: owns and manages its own state.
- **Stateless** composable: gets all data and callbacks as parameters.

> NOTE: Recomposition in Compose is driven by where the state is read, not by whether the composable is stateful or stateless.
> 

**Interviewer Details**
- **Stateful**:
  - Uses `remember { mutableStateOf(...) }` inside.
  - Simpler to use but harder to reuse or test.

- **Stateless**:
  - Parameters: `value` and `onValueChange`.
  - Easier to reuse, test, preview, and control from parents.

- **Hoist state**:
  - Start with stateless building blocks and then have a parent composable or ViewModel own the state.
  - Only the child(ren) depending on the changed state are actually recomposed.

- **EXAMPLE**:
  - When onNameChange updates state to a new UiState, the Parent call will be re-run.
  - Compose then compares arguments to each child:
    - ChildA sees name changed → it recomposes.
    - ChildB sees description is the same value as before → it will be skipped (no recomposition body run), even though Parent itself re-entered.

  - If instead you pass the whole state object down to both children (e.g., ChildB(state = state)), then any change to state means its parameter is “different”, so ChildB will recompose too, even if it only uses description.
```kotlin
@Composable
fun Parent(state: UiState, onNameChange: (String) -> Unit) {
    ChildA( // uses name + color; can change name
        name = state.name,
        color = state.color,
        onNameChange = onNameChange
    )

    ChildB( // uses description only
        description = state.description
    )
}
```

**Succinct Interview Answer**
> In Compose I try to keep components stateless whenever possible: they take the current value and an `onChange` callback and don’t own any internal state. 
> That makes them easy to reuse and test. Something higher up—usually a parent composable or ViewModel—owns the actual `mutableStateOf` or `StateFlow`. 
> I only make a composable stateful for truly local concerns like a temporary toggle that never needs to leave that composable.

---

### 3.2 `mutableStateOf` vs `derivedStateOf`

**Master Question**  
What’s the difference between `mutableStateOf` and `derivedStateOf` in Compose, and why would you use `derivedStateOf`?

**High-Level (Student)**
- `mutableStateOf` = directly mutated state.
- `derivedStateOf` = computed value based on other states.

**Interviewer Details**
- **`mutableStateOf`**:
  - Direct, independent piece of state.
  - Changing it triggers recomposition where it’s read.

- **`derivedStateOf`**:
  - Wraps a computation that reads other states.
  - Only recomputes when inputs change.
  - Helps avoid redundant work inside compositions, especially in lists or expensive calculations.
  - Note if the Child compose does not need to recompose, derivedStateOf is not necessary. 
    - e.g. above example with ChildB only using description, if name changes, ChildB will not recompose, so derivedStateOf is not needed.

- **EXAMPLE**:
  - filteredItems recomputes only when `a.value` or `filter.value` change, not on every recomposition of the parent.
  - text recomputes only when `state.value` changes.
```kotlin
val a = collectAsState() // Flow<List<Item>>
val filter = mutableStateOf("")
val filteredItems = derivedStateOf {
    a.value.filter { it.name.contains(filter.value, ignoreCase = true) }
}
val state = collectAsState() // Flow<String>
val text = derivedStateOf {
    "This is ${state.value}"
}
```

**Succinct Interview Answer**
> I use `mutableStateOf` for independent pieces of state, like the current text or selected item. 
> When I have a value that can be derived from other states—like a filtered list or a total count—I’ll wrap it in `derivedStateOf` so it only recomputes when the inputs change. 
> That keeps recomposition cheaper and avoids re-running expensive calculations unnecessarily.

---

### 3.3 Recomposition & Effects (`remember`, `LaunchedEffect`, `SideEffect`, `DisposableEffect`, `rememberCoroutineScope`)

**Master Question**  
What is recomposition in Compose, and how do `remember` and the effect APIs help you control behavior?

**High-Level (Student)**
- **Recomposition** = re-running composables when state they depend on changes.
- `remember` caches values; effect APIs control side effects across recompositions.

**Interviewer Details**
- **Recomposition**:
  - Driven by state reads.
  - Compose does diffing to update only what’s needed.

- **`remember` / `rememberSaveable`**:
  - `remember`: survives recompositions while in composition.
  - `rememberSaveable`: also survives configuration changes by saving to `Bundle`.
    - don't use for large objects or non-serializable types
    - use for primitives, Strings, Parcelable, Serializable, or custom Savers

- **Effects**:
  - `LaunchedEffect(key)`: run suspend code and restart when key changes.
  - `SideEffect`: non-suspending, runs after every successful recomposition.
  - `DisposableEffect(key)`: has `onDispose` to cleanup when leaving composition.
    - non-suspending setup/teardown tied to lifecycle.
  - **`rememberCoroutineScope`**:
    - Provides a coroutine scope tied to the composable’s lifecycle for launching coroutines in response to UI events.
    - Useful for things like showing snackbars or animations.
    - Scope is cancelled when the composable leaves composition.

**Succinct Interview Answer**
> Recomposition is just Compose re-running your composable functions when the state they read changes, and then updating only the affected parts of the UI. 
> I use `remember` to keep local state or expensive objects across recompositions, 
> `rememberSaveable` when they should survive configuration changes, 
> `LaunchedEffect` to run suspend work like loading data or collecting a Flow, 
> `SideEffect` for non-suspending updates after the UI is applied, and 
> `DisposableEffect` when I need setup and teardown tied to the composable’s lifecycle.
> `rememberCoroutineScope` gives me a scope to launch coroutines for UI events that’s automatically cancelled when the composable leaves composition.

---

### 3.4 Avoiding Unnecessary Recomposition

**Master Question**  
How do you avoid unnecessary recompositions in Jetpack Compose?

**High-Level (Student)**
- Use **stable/immutable data**, `remember`, and proper state scoping.
- Break UI into smaller composables.

**Interviewer Details**
- Prefer **immutable data classes**, potentially annotate with `@Immutable`/`@Stable` when appropriate.
- Hoist state to the right level; avoid passing frequently-changing lambdas or objects unnecessarily.
- Use `derivedStateOf` for computed values.
- Provide keys in `LazyColumn` and avoid heavy work in `item` lambdas.

**Annotations**
- `@Immutable` 
  - Use on data classes that are truly immutable (all val, no internal mutation).
  - Tells Compose: if the instance reference is the same, none of its fields changed → skip recomposition.
- `@Stable`
  - Use on classes whose observable properties are stable and whose behavior won’t break skipping.
  - Typically applied to holder/manager types that expose State or other stable properties.
    - e.g., ViewModels, repositories, controllers.
    - don't typically pass ViewModels directly to composables; instead, pass only the needed state or callbacks

**EXAMPLE**:
```kotlin
@Immutable
data class User(val name: String, val age: Int)

@Stable
class UserViewModel {
    private val _userState = mutableStateOf(User("Alice", 30))
    val userState: State<User> = _userState
}
```

**Succinct Interview Answer**
> My main tools are keeping data models immutable and stable, hoisting state to the right level, using `remember` and `derivedStateOf` for expensive or derived values, and breaking large composables into smaller ones. In lists I use keys and avoid doing heavy work in the `item` lambda so that scrolling doesn’t cause excessive recomposition.

---

### 3.5 CompositionLocal

**Master Question**  
What is `CompositionLocal` and when would you use it?

**High-Level (Student)**
- It’s like a **scoped global** for a subtree of your composable hierarchy.
- Useful for theming, localization, or cross-cutting values.

**Interviewer Details**
- Define with `compositionLocalOf` or `staticCompositionLocalOf`.
- Provide a value with `CompositionLocalProvider(LocalX provides value) { ... }`.
- Read with `LocalX.current` inside children.
- Avoid using it for everything; prefer explicit parameters for regular data.

**Succinct Interview Answer**
> `CompositionLocal` lets me provide a value high up in the Compose tree and have any child read it without threading it through every function parameter. It’s perfect for things like themes, localization, or other cross-cutting concerns. I use it sparingly—mostly for framework-level concerns—so my regular screen data still flows explicitly through parameters.

---

### 3.6 Why Jetpack Compose?

**Master Question**  
Why did you or would you adopt Jetpack Compose instead of the traditional View system?

**High-Level (Student)**
- Fewer XMLs, more Kotlin.
- Declarative, easier to reason about.

**Interviewer Details**
- **Benefits**:
  - Declarative UI → UI = function of state.
  - Less boilerplate: no XML + Adapter + ViewHolder triangles.
  - Strong Kotlin integration: coroutines, Flows, extensions.
  - Easy state-driven animations and theming.
  - Interoperable with Views to support incremental migration.

**Succinct Interview Answer**
> I like Compose because it gives me a fully declarative, Kotlin-first way to build UIs. The UI is just a function of state, which matches how we already design ViewModels with Flows. It cuts a lot of boilerplate compared to XML plus Adapters, and makes things like animations, theming, and state handling feel much more natural. Plus it coexists with Views, so you can migrate screens gradually.

---

## 4. Architecture & State in Android

### 4.1 MVVM vs MVP vs MVI

**Master Question**  
Compare MVVM, MVP, and MVI. Which do you prefer and why?

**High-Level (Student)**
- **MVP**: Presenter pushes commands to View.
- **MVVM**: View observes ViewModel state.
- **MVI**: Intents in, state out, strong UDF.

**Interviewer Details**
- **MVP**:
  - View ↔ Presenter references; Presenter calls `view.showX()`.
  - Manual attach/detach for lifecycles, more boilerplate, risk of leaks.

- **MVVM**:
  - View observes `LiveData`/`StateFlow` from ViewModel.
  - ViewModel is lifecycle-aware, no direct reference to View.
  - Fits nicely with Compose and Flows.

- **MVI**:
  - View sends **intents**, ViewModel reduces them into a new **state**.
  - Strong UDF, good tracing of state transitions, more boilerplate.

**Succinct Interview Answer**
> For Android I generally prefer MVVM with Clean Architecture principles. The View observes state from the ViewModel, the ViewModel exposes Flows or LiveData and doesn’t know about the View, and repositories and use cases sit underneath. It’s simple, testable, and a good fit for both Views and Compose. For very complex state machines I’ve also used MVI patterns, where the UI sends intents and the ViewModel reduces them into a new immutable state, but that’s usually more structure than I need for typical screens.

---

### 4.2 Unidirectional Data Flow (UDF)

**Master Question**  
What is Unidirectional Data Flow and how do you implement it in your Android apps?

**High-Level (Student)**
- **State down, events up**: single source of truth, no circular state modifications.

**Interviewer Details**
- Typically: `UiState` in ViewModel, `UiEvent`/Intents from View.
- View never mutates state directly; it only triggers events.
- ViewModel handles business logic and updates state.

**Succinct Interview Answer**
> Unidirectional Data Flow means the ViewModel is the one source of truth for state, the View just renders that state, and any user actions flow back up as events. The View never mutates shared state directly. In Android that usually looks like a `StateFlow<UiState>` from the ViewModel plus functions like `onAction()` that the View calls. It keeps state changes predictable, easier to debug, and aligns with Compose’s declarative model.

---

### 4.3 Architecture Choice for Apps

**Master Question**  
How do you choose an architecture for a new Android app or feature?

**High-Level (Student)**
- Consider **team skills**, **app complexity**, **testability**, and **longevity**.

**Interviewer Details**
- For small/simple apps: lightweight MVVM, maybe minimal layers.
- For medium/large apps: MVVM + use cases + repos (Clean-ish split).
- Consider integration points with backend, other platforms, and team preferences.

**Succinct Interview Answer**
> I usually start with MVVM and layer in Clean Architecture concepts as complexity grows: View/Compose for UI, ViewModel for presentation logic, use cases for business rules, and repositories for data access. The choice depends on the team’s familiarity and the app’s long-term needs. I avoid over-engineering for simple apps but plan enough structure so it’s still testable and can grow without becoming a ball of mud.

---

### 4.4 ViewModel State from Multiple Sources (`combine`)

**Master Question**  
How can a ViewModel collect and merge UI updates from multiple data sources?

**High-Level (Student)**
- Use `combine` (and similar operators) to merge multiple Flows into one `UiState`.

**Interviewer Details**
- Example: repository streams from DB, network status, and feature toggles.
- `combine(flowA, flowB) { a, b -> UiState(a, b) }`.
- ViewModel exposes a single `StateFlow<UiState>` to UI.

**Succinct Interview Answer**
> When my UI depends on several sources, I expose them as Flows from the repositories and use the Flow `combine` operator in the ViewModel to merge them into a single `UiState`. That way the UI only has to observe one state object, and any change in any source is reflected consistently in the combined state.

---

## 5. Android Components & Platform

### 5.1 Core Android Components

**Master Question**  
What are the main Android app components and when would you use each?

**High-Level (Student)**
- Activities, Fragments, Services, Broadcast Receivers, Content Providers.

**Interviewer Details**
- **Activity**: entry point for user interaction, manages a screen.
- **Fragment**: modular portion of UI logic that lives inside an Activity.
- **Service**: background work without UI, often foreground for user-visible long tasks.
- **BroadcastReceiver**: responds to system or app broadcasts.
- **ContentProvider**: surface structured data to other apps.

**Succinct Interview Answer**
> Activities and Fragments handle screens and UI, Services handle background or long-running work, BroadcastReceivers listen for system or app-wide events, and ContentProviders expose structured data across apps. In modern apps I try to keep Activities fairly thin, push logic into Fragments or ViewModels, and use foreground Services only when users expect ongoing work, like playback or navigation.

---

### 5.2 Context in Android

**Master Question**  
What is `Context` in Android, and what are the different types?

**High-Level (Student)**
- It’s a handle to the Android system – resources, services, app information.

**Interviewer Details**
- **Application Context**:
  - Lives as long as the app process.
  - Good for long-lived singletons, DI graphs, and things that must outlive activities.

- **Activity Context**:
  - Tied to Activity lifecycle.
  - Use for anything UI-related: inflating layouts, showing dialogs, theming.

- **Service Context**:
  - Tied to Service lifecycle, similar to Application but for background work.

**Succinct Interview Answer**
> Context is how you talk to the Android framework: resources, system services, starting activities, etc. I use the Activity context for UI-related work like inflating views or showing dialogs, and the Application context for long-lived things like singletons or DI so I don’t leak activities by accident.

---

### 5.3 Storage Options (SharedPreferences, DataStore, Room, Files, Keystore)

**Master Question**  
What local storage options are available on Android, and when would you use SharedPreferences vs DataStore vs Room vs file storage vs Keystore?

**High-Level (Student)**
- **Preferences/DataStore**: small key-value settings.
- **Room**: structured relational data.
- **Files**: arbitrary binary or text.
- **Keystore**: secure keys and secrets.

**Interviewer Details**
- **SharedPreferences**:
  - Synchronous I/O, potential main-thread blocking, risk of data corruption.
  - Legacy choice for small simple key-value configs.

- **DataStore** (Preferences/Proto):
  - Coroutine-based, asynchronous, more robust.
  - Recommended for new key-value storage.

- **Room**:
  - ORM over SQLite; compile-time checked queries.
  - Exposes Flows for reactive queries.

- **File Storage**:
  - Internal: private to app; good for sensitive or app-specific files.
  - External: shared, needs runtime permissions; good for user media.

- **Keystore**:
  - Hardware-backed on many devices.
  - Stores encryption keys; use keys to encrypt sensitive data rather than storing secrets directly.

**Succinct Interview Answer**
> For simple settings like theme or language I use DataStore instead of SharedPreferences because it’s asynchronous and safer. For structured relational data I use Room, which gives me compile-time checked SQL and Flows. If I need to store large binary data like images I use file storage, usually internal unless it’s user-facing. And for anything security-sensitive, like encryption keys, I use the Android Keystore so keys are hardware-protected whenever possible.

---

### 5.4 Apollo vs Retrofit vs Ktor (GraphQL vs REST)

**Master Question**  
How do Retrofit, Apollo, and Ktor compare for networking, and when would you use each?

**High-Level (Student)**
- **Retrofit**: REST client.
- **Apollo**: GraphQL client.
- **Ktor client**: general-purpose HTTP, great for multiplatform.

**Interviewer Details**
- **Retrofit**:
  - Map endpoints to interface methods.
  - Easy JSON mapping with converters.
  - Ideal for RESTful APIs.

- **Apollo**:
  - Strong GraphQL support: queries, mutations, subscriptions.
  - Generates types from schema.

- **Ktor client**:
  - Kotlin multiplatform, flexible pipeline.
  - Good for KMP projects or when you need more control.

**Succinct Interview Answer**
> For typical REST APIs I reach for Retrofit because it’s simple and battle-tested. For GraphQL backends I prefer Apollo since it understands the schema and generates types from queries. If I’m working on a multiplatform project or need lower-level control, I’ll use Ktor’s HTTP client because it works across Android, iOS, and desktop.

---

### 5.5 Serialization vs Parcelable

**Master Question**  
What’s the difference between serialization (e.g., Kotlinx Serialization) and `Parcelable` in Android, and when do you use each?

**High-Level (Student)**
- **Parcelable**: optimized for Android inter-process and inter-component passing.
- **Serialization**: general encoding for storage and network.

**Interviewer Details**
- **Parcelable**:
  - Implemented for passing data via Intents, Bundles.
  - Faster and more compact for Android IPC.

- **Serialization (Kotlinx, JSON, etc.)**:
  - Converts objects to/from formats like JSON, ProtoBuf.
  - Used for network calls, local persistence, caches.

**Succinct Interview Answer**
> I use Parcelable when I need to pass rich objects between Android components, like sending a user object in an Intent. For network and long-term storage I use a serialization library like Kotlinx Serialization or Moshi to convert objects to JSON or binary formats. Parcelable is optimized for Android IPC, while serialization is about encoding data for storage and transport.

---

### 5.6 ListView vs RecyclerView

**Master Question**  
Why is RecyclerView preferred over ListView in modern Android apps?

**High-Level (Student)**
- RecyclerView is more flexible, more efficient, and more customizable.

**Interviewer Details**
- RecyclerView:
  - Uses ViewHolder pattern strictly.
  - Supports multiple layout managers and item animations.
  - More control over item decorations, animations, and performance.

- ListView:
  - Older, limited to vertical lists.
  - Less flexible, deprecated in modern UI design.

**Succinct Interview Answer**
> RecyclerView gives me more control and better performance than ListView. It enforces the ViewHolder pattern, supports different layout managers and item animations, and is the foundation for modern libraries like Paging. ListView is simpler but much less flexible, so I stick with RecyclerView for anything non-trivial.

---

### 5.7 R8 vs ProGuard

**Master Question**  
What’s the difference between R8 and ProGuard in Android builds?

**High-Level (Student)**
- Both shrink and obfuscate code.
- **R8** is the modern default, more integrated and generally faster.

**Interviewer Details**
- R8 replaces ProGuard by default in recent Android Gradle Plugin versions.
- It uses ProGuard rules syntax for configuration.
- Performs extra optimizations like inlining and aggressive dead code elimination.

**Succinct Interview Answer**
> R8 is the modern code shrinker and obfuscator for Android that effectively replaces ProGuard. It’s integrated into the Android Gradle plugin, uses the same rule syntax, and usually gives better performance and smaller APKs thanks to extra optimizations like inlining and dead code elimination.

---

## 6. Background Work & Scheduling

### 6.1 WorkManager vs JobScheduler vs AlarmManager

**Master Question**  
Compare WorkManager, JobScheduler, and AlarmManager. When is each appropriate?

**High-Level (Student)**
- WorkManager = recommended for most deferrable, guaranteed background tasks.
- JobScheduler = underlying API on newer Android.
- AlarmManager = exact time-based alarms.

**Interviewer Details**
- **WorkManager**:
  - Handles constraints, retries, and chaining.
  - Uses JobScheduler/AlarmManager/FirebaseJobDispatcher under the hood.
  - Survives app/device restarts.

- **JobScheduler**:
  - System API for background jobs with constraints.
  - Used directly in some lower-level or older codebases.

- **AlarmManager**:
  - Schedule alarms at specific times.
  - Sensitive to Doze/battery optimizations.

- **Services vs WorkManager**:
  - Foreground Service: user-visible ongoing work (music, navigation).
  - WorkManager: deferrable “fire when constraints are right” work.

**Succinct Interview Answer**
> For background tasks that must be guaranteed and respect constraints like network or charging, I use WorkManager; it chooses the right scheduler under the hood and survives app restarts. If I need an exact clock-based alarm, I’ll still use AlarmManager with the understanding that Doze can affect it. For user-visible, ongoing work like media playback or navigation I use a foreground Service rather than WorkManager.

---

## 7. Networking, GraphQL, and Distribution

### 7.1 GraphQL vs REST

**Master Question**  
How would you explain the difference between REST and GraphQL in terms of API design and client usage?

**High-Level (Student)**
- REST: fixed endpoints and resources.
- GraphQL: single endpoint, flexible queries.

**Interviewer Details**
- **REST**:
  - Multiple endpoints for resources.
  - Over-fetching/under-fetching can be common.

- **GraphQL**:
  - Client defines the exact shape of the data it wants.
  - Single endpoint with strongly-typed schema.

**Succinct Interview Answer**
> With REST I hit different endpoints to get different shapes of data, whereas with GraphQL I send a query that describes exactly what fields I need and get them back in one response. That reduces over-fetching and number of roundtrips at the cost of a slightly more complex server and client setup. On Android I’d use Retrofit for REST and Apollo for GraphQL.

---

## 8. Testing & Tooling

### 8.1 Compose Testing Advantages

**Master Question**  
What are the advantages of testing UIs written in Compose compared to the View system?

**High-Level (Student)**
- Tests focus on **state and behavior**, not implementation details.

**Interviewer Details**
- **Declarative model**:
  - You can set up a certain state and assert on the resulting UI tree.

- **Testing APIs**:
  - Compose Testing library provides semantics-based queries (`onNodeWithText`, etc.).

- **Isolation**:
  - Many tests can execute on JVM or with minimal Android dependencies.

**Succinct Interview Answer**
> Compose’s declarative model makes testing nicer because I can just set a specific state, render the composable, and then assert on what’s on screen using semantics rather than IDs. The official testing APIs are quite expressive, and I can run many of these tests quickly without spinning up a full instrumentation environment.

---

### 8.2 MockK vs Mockito

**Master Question**  
Compare MockK and Mockito for testing Kotlin code.

**High-Level (Student)**
- Mockito is older and Java-first.
- MockK is designed for Kotlin.

**Interviewer Details**
- MockK has better support for:
  - Coroutines, extension functions, top-level functions, default parameters.
  - Often more concise syntax in Kotlin.

**Succinct Interview Answer**
> Mockito works fine for basic mocking, but it was built for Java. For Kotlin-specific features—like coroutines, extension functions, data classes—I find MockK feels more natural and requires fewer workarounds, so that’s usually what I prefer in a Kotlin-heavy project.

---

## 9. Kotlin Multiplatform & Compose Multiplatform

### 9.1 Kotlin Multiplatform / Compose Multiplatform Overview

**Master Question**  
What is Kotlin Multiplatform and how does Compose Multiplatform fit into it?

**High-Level (Student)**
- Kotlin can target **Android, JVM, JS, and native**.
- Compose Multiplatform lets you share UI across desktop, web, and Android.

**Interviewer Details**
- You share common logic in `commonMain` (domain, networking, etc.).
- Platforms provide their own specific implementations where needed.
- Compose Multiplatform reuses the declarative Compose model on multiple platforms.

**Succinct Interview Answer**
> Kotlin Multiplatform lets me share one codebase across Android, backend, web, and native targets, especially for domain and data layers. Compose Multiplatform extends that idea to UI, so I can use the same declarative components on Android, desktop, and web. That can significantly reduce duplication for apps that need to run on multiple platforms.

---

## 10. Leadership & Behavioral

### 10.1 Role and Responsibilities as a Lead

**Master Question**  
What are your responsibilities as a senior or lead Android engineer day to day?

**High-Level (Student)**
- Mix of **technical decision-making**, **mentoring**, **collaboration**, and **delivery**.

**Interviewer Details**
- **Technical leadership**: architecture guidance, code reviews, standards.
- **Mentorship**: supporting juniors, pairing, sharing context.
- **Project coordination**: working with backend, iOS, design, QA, PM.
- **Execution**: still writing code and unblocking critical paths.

**Succinct Interview Answer**
> As a senior engineer I still write a lot of code, but a big part of my role is enabling the rest of the team to move faster and safer. That means helping shape architecture, doing thoughtful code reviews, pairing with juniors, and working closely with product, design, backend, and QA to clarify requirements and unblock dependencies. I try to keep an eye on both the day-to-day delivery and the longer-term health of the codebase.

---

### 10.2 Daily Routine Example (Senior Android)

**Master Question**  
What does a typical day look like for you as a senior Android engineer?

**High-Level (Student)**
- Blend of **meetings**, **reviews**, **hands-on coding**, and **helping others**.

**Interviewer Details**
- Morning: standups, aligning with offshore or cross-functional teams.
- Midday: backlog grooming, planning, code reviews.
- Afternoon: focused coding time, ad-hoc help, mentoring.

**Succinct Interview Answer**
> A typical day starts with a standup where we align on what we’re doing and any blockers, often with offshore teams if they’re involved. I’ll spend a block of time on backlog grooming and planning with product and design so the team has clear, well-defined work. I reserve time for code reviews and pair programming, especially for complex changes or junior developers. The rest of the day is focused coding, plus fielding questions and helping remove blockers so the whole team can keep moving.

---

## 11. Migration: Java to Kotlin / Legacy to Modern

### 11.1 Migrating from Java to Kotlin

**Master Question**  
How would you approach migrating an existing Java Android project to Kotlin and to more modern libraries (e.g., Room, coroutines)?

**High-Level (Student)**
- Start with tooling (Kotlin plugin) and **incremental** conversion.
- Carefully review nullability and new features.

**Interviewer Details**
- Use IDE’s “Convert Java to Kotlin” feature, then 
  - Fix nullability annotations,
  - Introduce data classes and idiomatic code slowly.
- Consider **jumping tech** where it’s clearly better (e.g., Anko → Room).
- Keep public APIs stable while rewriting internals.

**Succinct Interview Answer**
> I’d start by enabling Kotlin in the build and converting small, low-risk classes using the IDE’s converter, then manually cleaning up nullability and introducing data classes and idiomatic features. For infrastructure like persistence, it’s often worth jumping straight from something like Anko to Room instead of doing a series of small migrations. I try to keep external interfaces stable so I can rewrite internals behind them, and I treat this as an incremental process rather than a big bang rewrite.

---

## 12. Miscellaneous Topics

### 12.1 DI vs Dependency Inversion Principle

**Master Question**  
What’s the difference between Dependency Injection and the Dependency Inversion Principle?

**High-Level (Student)**
- DIP is a **principle**.
- DI is a **pattern/tool** used to implement that principle.

**Interviewer Details**
- DIP: high-level modules depend on abstractions, not concrete implementations.
- DI: mechanisms like Hilt/Dagger/Koin or manual construction that provide those abstractions to consumers.

**Succinct Interview Answer**
> The Dependency Inversion Principle says high-level code should depend on abstractions rather than concrete classes. Dependency Injection is how we implement that: instead of a class creating its own dependencies, they’re provided from the outside, often by a framework like Hilt. So DIP is the design guideline, DI is the pattern we use to follow it.

---

### 12.2 Flow of Data Loading (Init, `LaunchedEffect`, Flow lifecycle)

**Master Question**  
How can you trigger initial data loading in a screen, and what trade-offs exist between doing it in the ViewModel `init` vs in the UI layer?

**High-Level (Student)**
- Option 1: Call load in ViewModel `init`.
- Option 2: Trigger from UI (`LaunchedEffect`).
- Option 3: Use Flow lifecycle (`onStart`, `stateIn`).

**Interviewer Details**
- **ViewModel `init`**: simple, but harder to test because side-effects start automatically.
- **UI-triggered (`LaunchedEffect`)**: more explicit, but can be repeated if not keyed carefully.
- **Flow lifecycle**: use `.onStart { load() }` and `stateIn` to tie loading to subscriptions.

**Succinct Interview Answer**
> I’ve used all three approaches: firing a load from the ViewModel `init`, triggering it from the UI in a `LaunchedEffect`, and tying it to Flow subscription with `onStart`. These choices trade off simplicity vs testability. Lately I prefer either a clearly keyed `LaunchedEffect` from the UI or using Flow’s `onStart` and `stateIn` so it’s obvious when loading starts and tests can control it more easily.

---

This organized file is meant as your **primary study guide**. As you encounter new interview questions, you can:
- Attach them to an existing **Master Question** here, or
- Add a new section using the same structure: *Master Question → High-Level → Details → Succinct Answer*.

