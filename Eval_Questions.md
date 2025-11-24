# Week 1 and 2: Interview Notes and Answers

- familiarize with Kotlin Playground - interviewers may ask you to write code snippets in Kotlin

## How to handle null (Kotlin)
- Kotlin has nullable types (`String?`) and non-nullable types (`String`). Prefer non-nullable types where possible.
- Safe-call operator `?.` lets you call methods on nullable values without throwing NPE. Example: `val len = name?.length`.
- Elvis operator `?:` provides a default: `val display = name ?: "Unknown"`.
- Not-null assertion `!!` should be avoided except when you are 100% sure the value is not null — it throws NPE otherwise.
- Use `let`, `run`, `also`, `takeIf` to scope nullable handling and avoid deep nesting.
- For initialization-time nulls, prefer `lateinit var` for non-primitive types or `by lazy` for safe lazy initialization.
  - `lateinit` cannot be used with nullable types or primitive types.
- Talking points: compile-time null safety, fewer runtime crashes, prefer designing APIs with non-nullable types.

---

## All the lifecycle methods and when to use them (Android Activity / Fragment)
- Activity lifecycle (core callbacks): `onCreate` (initial setup), `onStart` (visible), `onResume` (foreground & interactable), `onPause` (commit changes, lightweight cleanup), `onStop` (heavy cleanup, release resources), `onDestroy` (final cleanup), `onSaveInstanceState` (persist transient UI state).
- Fragment adds `onAttach`, `onCreateView` (inflate UI), `onViewCreated` (view setup), `onDestroyView` (tear down view-related resources) — important distinction: fragment instance can outlive its view.
- When to use: allocate UI and adapters in `onCreateView`/`onViewCreated`; register listeners in `onStart`/`onResume`; unregister in `onPause`/`onStop`; free expensive resources in `onStop`/`onDestroy`.
- For long-running background work use `ViewModel` + `lifecycleScope` to avoid lifecycle-tied leaks.
- Talking points: separation of concerns, view vs data lifecycle, avoid memory leaks by cleaning up view-bound references in `onDestroyView` for fragments.

### Activity Lifecycle Order:
onCreate -> onStart -> onResume -> (active) -> onPause -> onStop -> onRestart -> onDestroy

### Fragment Lifecycle Order:
onAttach -> onCreate -> onCreateView -> onViewCreated -> onStart -> onResume -> (active) -> onPause -> onStop -> onRestart -> onDestroyView -> onDestroy -> onDetach

---

## Data Binding vs View Binding
- View Binding:
  - Generates a binding class for each XML layout. Type-safe access to views: `binding.textView`.
  - Simple, safer replacement for `findViewById`.
  - No XML-to-code expressions; lighter weight.
- Data Binding:
  - Supports binding expressions in XML, two-way binding, and `@BindingAdapter`s.
  - Useful when you want to bind UI directly to observable data or use simple expressions in layout.
  - Slightly more setup and build-time cost.
- When to pick which: prefer View Binding by default for clarity and speed; use Data Binding when you need expression-binding, two-way binding, or want to push UI logic into XML (careful with testability).
- Talking points: testability, build performance, maintainability.

---

## Difference between enum and sealed class (Kotlin)
- enum class:
  - Use for a fixed set of constant values (e.g., DAYS_OF_WEEK).
    - whatever parameters defined in the enum constructor are the same for all instances.
  - Can have properties and methods; instances are singletons.
  - Cannot extend enums -> `when` statements are exhaustive by default.
  - TODO: can enums implement interfaces but cannot inherit from other classes? 
- sealed class / sealed interface:
  - Use for restricted class hierarchies where each subclass represents a different type/state (algebraic data types).
  - Enables exhaustive `when` checks at compile time without `else` when all cases are covered.
  - Each subclass can hold different data.
- When to use: enums for simple enumerations; sealed classes for modeling variants with different associated data (e.g., Result.Success<T>, Result.Error).
- Example talking point: sealed classes are preferred when you need typed payloads per case and exhaustive pattern matching.

```kotlin
enum class CalculatorButtons(val symbol: String, val color: Int) {
    ADD("+", Color.GREEN),
    SUBTRACT("-", Color.RED),
    MULTIPLY("*", Color.BLUE),
    DIVIDE("/", Color.YELLOW),
  
  // no functions or properties that differ per instance
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Item>) : UiState()
    data class Error(val message: String) : UiState()
  
  // notice we can have a function inside a sealed class
  fun isSuccessful(): Boolean = this is Success
}
```

---

## Difference between SharedFlow and StateFlow
- Both are Kotlin Flow primitives used for broadcasting events from producers to consumers.
  - “Broadcast” means one producer can emit values that multiple independent collectors (consumers) can receive.
  - Producers: ViewModel, Repository, Service, or lower layers that push updates.
  - Consumers: UI (Fragments/Activities/Composables), other services, analytics, or background workers that collect the flow.
- Both are hot streams (active even without subscribers) and support multiple subscribers.
  - hot means they emit values regardless of whether there are active collectors.
    - Hot: emits regardless of collectors (StateFlow/SharedFlow). 
    - Cold: starts producing only when collected (regular Flow) -> like lazy sequences.
    - Emit (suspending) or tryEmit/trySend (non-suspending) pushes a value into a MutableStateFlow / MutableSharedFlow
      - Emitting is how producers(VM, Repo, Service) send values into flows(StateFlow/SharedFlow).
      - Collecting is how consumers(UI, other services) receive those values.
- StateFlow:
  - Represents a state, always has a current value (`state.value`).
  - Hot stream; subscribers immediately receive the current value.
  - Good for UI state in ViewModel because it models the current state and replays the latest value.
  - preferable with coroutine-based UIs (Compose, etc).
  - save data like form inputs, toggle states, loaded data.
- SharedFlow:
  - More general-purpose; can be configured to replay 0..N values, and does not necessarily hold a single current value.
  - Good for one-time events, multi-consumer events, or when you need custom replay/buffering behavior.
    - events like navigation commands, toasts, or analytics events. ("load more", network error)
    - do not hold page streams (data that changes over time) in SharedFlow.
    - use Paging 3 for pagination (Flow<PagingData<T>>)
- Talking points: use StateFlow for state, SharedFlow for events or custom broadcasting. Avoid using single-live-event anti-patterns; prefer properly-configured SharedFlow for events.
### Other Options: 
- LiveData: lifecycle-aware, but less flexible than Flow; consider migrating to Flow for new code.
  - Android-specific, while Flow is multiplatform (kotlin).
  - good for VM to UI layer communication.

```kotlin
// ViewModel snippet: StateFlow for state, SharedFlow for one-time events
class MyViewModel : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  val events = _events.asSharedFlow()

  fun load() {
    viewModelScope.launch {
      val data = repository.getData() // suspend
      _uiState.value = UiState.Success(data)
    }
  }

  fun onItemClicked(id: String) {
    // emit a one-time navigation event
    viewModelScope.launch { _events.emit(UiEvent.Navigate(id)) }
  }

  // non-suspending emit (e.g., from background thread without coroutine) use tryEmit
  fun nonSuspendingEmit(e: UiEvent) { _events.tryEmit(e) }
}
```

---

## State vs StateFlow vs LiveData
- State (Compose):
  - A Compose primitive for holding state in composables.
  - Triggers recomposition when updated.
  - Scoped to the composable lifecycle.
- StateFlow:
  - A Kotlin Flow primitive for holding state in ViewModels or repositories.
  - Hot stream with a current value; suitable for UI state management.
  - Works well with Compose via `collectAsState()`.
  - Reasons to prefer over State:
    - Decouples ViewModel from Compose runtime.
    - Supports operators (map, combine, debounce, etc.).
    - Easier to share with non-UI layers and tests.
    - Clear initial value, avoids accidental uninitialized State.
- LiveData:
  - Lifecycle-aware observable data holder.
  - Automatically manages subscriptions based on lifecycle state.
  - Less flexible than Flow; consider migrating to Flow for new code.
- When to use:
  - Use State in composables for local UI state.
    - Single screen only & not reused: prefer local state (mutableStateOf)
    - quick fields like text input, toggle states, visibility
    - temporary state that doesn't need to survive configuration changes (e.g., dialog visibility, input focus, hover states)
  - Use StateFlow in ViewModels for app-wide state management.
    - Local ephemeral UI input (e.g. user typing a name): use remember { mutableStateOf("") } (or hoist to parent). Move to ViewModel as MutableStateFlow only if it must survive configuration changes, drive other logic, or be shared across composables.
    - Form fields across multiple composables in one screen: hoist state to screen-level composable or keep in that screen’s ViewModel as StateFlow if validation/business rules apply.
    - List loaded once from API then immutable: load into ViewModel, expose as StateFlow<List<T>> for consistency; UI collects. If it truly never changes after load you can pass it directly once.
    - Continuously updating API data (e.g. current weather, live prices): repository exposes a StateFlow<Data>; 
      - ViewModel forwards or transforms it; Compose uses collectAsState().
    - Filterable list (base + user query): - Base list from API/repo: StateFlow<List<T>> in ViewModel. - User query: local mutableStateOf(query) (or StateFlow if shared across multiple subtrees). 
      - Filtered result: derivedStateOf { base.filter(matches(query)) } in UI if cheap;
      - or compute in ViewModel and expose another StateFlow if expensive or reused.
    - Expensive or asynchronous filtering/paging: perform in ViewModel (combine flows) and expose as StateFlow<PagingData<T>> or StateFlow<List<T>>.
  - Use LiveData in legacy code or when lifecycle-awareness is needed without Flow.
  - If static data that never changes, consider using plain constants or immutable data structures. NOT StateFlow/LiveData/State.
  - SharedFlow:
    - One-time events (navigation, toasts)
- Talking points: State is for UI layer, StateFlow for app state, LiveData for legacy/lifecycle-aware needs.

### Emitting vs Setting Values
- StateFlow: set value with `stateFlow.value = newValue` (synchronous).
- LiveData: set value with `liveData.value = newValue` (synchronous) or `postValue(newValue)` (asynchronous).
- State: update with `state.value = newValue` (synchronous).
- Emitting is how producers (ViewModel, Repository, Service) send values into flows (StateFlow/SharedFlow).
  - prefer using `.value = ` for emitting new values in StateFlow
  - explicit `emit() or tryEmit()` for SharedFlow
    - emit() is suspending -> use in coroutine, waits if buffer full
    - tryEmit() is non-suspending -> use outside coroutine (callbacks / listeners in background threads)
      - may drop or return false if buffer rules prevent emission
- Collecting is how consumers (UI, other services) receive those values.

```kotlin
@Composable
fun CounterScreen(viewModel: CounterViewModel) {
    // stateflow in ViewModel collected as State
    // collect as state is listening to changes in the StateFlow and updating the composable when it changes
    // can also use collectAsStateWithLifecycle() from lifecycle-runtime-compose library to auto-manage lifecycle
    val count by viewModel.count.collectAsState() // StateFlow collected as State
    // this is equivalent to this in live data
    // val count by viewModel.count.observeAsState()
    // local state example
    val localToggle = remember { mutableStateOf(false) } // local State

    Column {
        Text("Count: $count")
        Button(onClick = { viewModel.increment() }) {
            Text("Increment")
        }
    }
}
// viewmodel
class CounterViewModel : ViewModel() {
    // -------------- STATEFLOW ---------------------
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.value += 1 // emitting new value
    }
    
    // -------------- SHARED FLOW -------------------
    private val _events = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()
    
    // SharedFlow event from coroutine
    fun sendEventAsync(msg: String) {
        viewModelScope.launch {
            _events.emit(msg) // suspends if buffer full
        }
    }

    // SharedFlow event from a non-suspending callback
    fun onExternalCallback(msg: String) {
        _events.tryEmit(msg) // fire-and-forget; may drop if buffer full
    }
}
```

---

## What performance tools can you use in Android Studio
- CPU Profiler: inspect CPU usage, trace method calls, find hotspots.
- Memory Profiler: detect allocations, track memory growth, locate leaks.
- Network Profiler: see network requests, payload sizes, and timing.
- Layout Inspector / Layout Validation: debug UI hierarchy and rendering issues.
- Systrace / System Tracing: for complex UI or system-level performance problems.
- Leak detection: LeakCanary (library) to automatically detect leaks in development builds.
- Logcat and Debugger: traditional tools for logging and stepping through code.
- Benchmark and instrumentation: Jetpack Macrobenchmark, AndroidX Benchmark for measuring real metrics.
- Talking points: measurable metrics > guesses; reproduce problems with consistent traces; use profilers in emulators and real devices.
- Analytics tools: Firebase Performance Monitoring, etc.
  - Crashlytics

### Local
- icons, Strings, drawables/images, 
- heavy lists, algorithms, 
- nested views, nested navigations, 
- animations
- retaining static references


### Remote 
- internet connection, 
- rapid calls to the same api, 
- loading extensive lists of data, 
- not handling debounce


### Code Architecture
- loose coupling
- reusable components
- not having pagination
- monolithic code
- battery usage -> animations, background tasks, network calls, location updates, storing data on the device

---

## WorkManager vs JobScheduler vs AlarmManager
1. understand background tasks (e.g., periodic sync, deferred work, one-off tasks)
2. then start working below
3. see how background work relates to below schedulers

- AlarmManager:
  - Triggers an Intent at a specific time. Good for time-based alarms but not reliable for background work needs when respecting Doze/optimizations.
- JobScheduler:
  - System service for scheduling background jobs with constraints (network, charging). Works well on API 21+.
  - Better than AlarmManager for deferrable work and system batching.
- WorkManager (Jetpack):
  - ensures task finishes even if app exits or device restarts
    - can schedule tasks, can start/execute tasks when app not running
  - High-level library that chooses the best underlying scheduler (JobScheduler, AlarmManager, or Firebase JobDispatcher) based on API level.
  - Supports constraints, retries, chaining, periodic work, and guaranteed execution (even after app restart).
  - Preferred for most background tasks that need guaranteed execution and constraint handling.
- When to use: 
  - AlarmManager for precise time alarms (with caveats); 
  - JobScheduler/WorkManager for deferrable background tasks. 
  - Use WorkManager in most app-level background scenarios for reliability and simplicity.

WorkManager is similar to a Service
- Service stops when app is killed
- Service focuses on foreground tasks
- WorkManager focuses on deferrable, guaranteed background tasks

---

## What are your favorite Jetpack libraries or tools
- Compose Navigation: official library for handling navigation in Compose apps.
- there are many others like Paging 3, Hilt, Room, DataStore, Lifecycle, Material, etc.

---

## General idea of using Dependency Injection (DI)
- DI is a pattern that supplies an object with its dependencies rather than creating them internally.
- Benefits: easier testing (inject test doubles), better separation of concerns, more modular and reusable code.
- Common frameworks in Android: Hilt (built on Dagger), Dagger, Koin.
- Best practices: inject at boundaries (e.g., activities/fragments/viewmodels), prefer constructor injection, keep modules small and focused, avoid service locators.
- Talking points: how DI improves testability and modularity; example flow with Hilt providing a Repository into a ViewModel.
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideRepository(apiService: ApiService, dao: MyDao): MyRepository {
        return MyRepositoryImpl(apiService, dao)
    }
}
```

---

## Different kinds of scopes for coroutines
- `GlobalScope`: application-wide, not tied to lifecycle — usually avoid because it can leak work.
- `CoroutineScope` + structured concurrency: prefer creating scopes tied to lifecycle owners.
  - this is the custom scope you create for specific tasks
  - manage its job and cancel when done
  - prefer over global scope as it avoids memory leaks
- `lifecycleScope` (AndroidX Lifecycle): tied to LifecycleOwner (Activity/Fragment); canceled when the lifecycle is destroyed.
- `viewModelScope` (KTX): tied to the ViewModel lifecycle; cancelled when ViewModel is cleared.
- `supervisorScope` and `SupervisorJob`: control failure propagation for child coroutines.
  - TODO: explain more about these two? 
- Best practice: launch UI work in `lifecycleScope`/`viewModelScope`; use explicit scopes for long-running background work and cancel appropriately.
- When to use :
  - GlobalScope: avoid unless truly app-wide work.
  - COMPOSE:
    - CoroutineScope (rememberCoroutineScope()): 
      - UI event handlers tied to the composable’s lifetime: snackbars, sheets, animations, scroll, haptic feedback.
      - Launch from callbacks (onClick, etc.); cancelled when composable leaves composition.
    - LifecycleScope: NEVER in Compose; use rememberCoroutineScope() instead.
    - ViewModelScope: for ViewModel-bound work.
    - SupervisorScope: inside ViewModel, Repository, LaunchedEffect where one failure shouldn’t cancel siblings.
  - FRAGMENT (XML or Compose):
    - CoroutineScope: Repository/Use Case scope -> logic outside lifecycle (e.g., long-running tasks). Care, must manage cancellation.
    - LifecycleScope: 
      - Collect flows or run suspend work tied to lifecycle. Prefer repeatOnLifecycle and viewLifecycleOwner.lifecycleScope for view-bound collection.
      - Use for XML UI updates, non-Compose side effects, dialogs, permission flows.
    - SupervisorScope: 
      - In Fragment or VM to isolate failures among concurrent children (e.g., loading multiple panels; one error doesn’t nuke all).
      - Often paired with SupervisorJob() at scope creation.

```kotlin
// Repository: custom CoroutineScope with SupervisorJob for long-running, non-UI work
class Repo(
    private val api: ApiService,
    private val dao: MyDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    fun refresh() {
        // network + db 
        scope.launch {
            val data = api.list()
            dao.replaceAll(data)
        }
    }
    fun clear() {
        scope.cancel() // cancel ongoing work when no longer needed
    }
    
    // suspend functions for data loading, viewModel is using a coroutine to call these,
    // so they need to be suspended
    suspend fun loadHeader(): Header = withContext(dispatcher) { /* ... */ }
    suspend fun loadList(): List<Item> = withContext(dispatcher) { /* ... */ }
    suspend fun loadOptionalBadge(): Badge = withContext(dispatcher) { /* ... */ }
}

// Simple UI state placeholder
data class DashboardUiState(
    val header: Header? = null,
    val list: List<Item> = emptyList(),
    val badge: Badge? = null
)
// lifecycleScope example
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            // UI-related work (XML -> do not use with Compose)
            // use Dispatchers.Main by default
            // used to get data for UI from ViewModel or Repository
        }
    }
}
// viewModelScope example
class DashboardViewModel(
    private val repo: Repo
) : ViewModel() {
    private val _ui = MutableStateFlow(DashboardUiState())
    val ui: StateFlow<DashboardUiState> = _ui

    fun loadParallel() {
        viewModelScope.launch {
            supervisorScope {
                // use async when combining multiple concurrent results
                val a = async { repo.loadHeader() }
                val b = async { repo.loadList() }
                val c = async { repo.loadOptionalBadge() } // may fail independently
                _ui.value = _ui.value.copy(
                    header = runCatching { a.await() }.getOrNull(),
                    list = runCatching { b.await() }.getOrDefault(emptyList()),
                    badge = runCatching { c.await() }.getOrNull()
                )
            }
        }
    }
    
    fun loadSequential() {
        viewModelScope.launch {
            val header = repo.loadHeader()
            val list = repo.loadList()
            val badge = repo.loadOptionalBadge()
            _ui.value = DashboardUiState(header, list, badge)
        }
    }
    
    fun refresh() = viewModelScope.launch { repo.refresh() }
}
// Compose rememberCoroutineScope example
@Composable
fun DashboardScreen(vm: DashboardViewModel, snackbarHost: SnackbarHostState) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    // One-off events (e.g., SharedFlow)
    LaunchedEffect(Unit) {
        // vm.events.collect { e -> /* navigate/snackbar */ }
    }

    val cs = rememberCoroutineScope()
    Button(
        onClick = {
            cs.launch {
                snackbarHost.showSnackbar("Refreshed")
                vm.refresh()
            }
        }
    ) { Text("Refresh") }
    // ... render from ui
}
```

### Launch vs Async
- `launch`: starts a coroutine that does not return a result (returns a job); used for fire-and-forget tasks.
  - e.g., updating UI, making network calls without needing a return value
- `async`: starts a coroutine that returns a `Deferred<T>`; used for concurrent tasks that produce a result.
  - e.g., fetching data from multiple sources simultaneously (network, database)
- Use `await()` on the `Deferred` to get the result.
- Talking points: structured concurrency, cancellation, and error handling.

- Job is a handle to a coroutine. It represents the coroutine's lifecycle and can be used to cancel or join it.
  - very common example of cancel a job is when you're typing in a search box and you want to cancel the previous search request when a new character is typed

- WithContext: provides context for coroutine execution (e.g., Dispatcher, Job).
  - e.g., `withContext(Dispatchers.IO) { ... }` to switch to IO thread for blocking operations.
  - not creating a new coroutine, just changing context within an existing one

- When to use:
  - Use `launch` for tasks that don't need a result (UI updates, side effects).
  - Use `async` for tasks that need to return a value (data fetching, computations).
    - use when combining multiple concurrent results (see above ViewModel example)
    - Inside a launch when you need parallel subtasks and final result applied as a side effect (UI/state update). 
    - Inside a suspend function using coroutineScope {} or supervisorScope {} to run parallel work and return a combined result. 
    - Directly on a scope (viewModelScope.async {}) if you really need a Deferred, but exposing Deferred is usually DISCOURAGED; prefer a suspend function.
  - Use `withContext` to switch contexts within a coroutine without launching a new one.

```kotlin
// Repository: async without launch (structured concurrency)
suspend fun loadCombined(): Combined = coroutineScope {
    val a = async { fetchA() }
    val b = async { fetchB() }
    Combined(a.await(), b.await())
}

// Direct async (rare; avoid leaking Deferred)
fun prefetch(): Deferred<Unit> = viewModelScope.async {
    repo.primeCaches()
}
// THIS IS UNSAFE
suspend fun fetchData(): List<Person> = viewModelScope.async {
    api.getPeople()
}.await()
// use this instead ->  withContext does not use launch under the hood. 
// It does not create a new child coroutine; it suspends the current one, switches dispatcher, runs the block, then resumes with the result. 
// It reuses the existing Job (same cancellation & structured concurrency). launch creates a new coroutine with its own Job and returns immediately.
suspend fun fetchData(): People = withContext(Dispatchers.IO) {
    api.getPeople()
}
// view model
class MyViewModel(
    private val repo: Repo,
    private val api: ApiService
): ViewModel() {
    private var _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people.asStateFlow()
    
    private var _dashboardUi = MutableStateFlow<DashboardUiState>(DashboardUiState())
    val dashboardUi: StateFlow<DashboardUiState> = _dashboardUi.asStateFlow()
    
    init {
        // NOTE: suspend functions must be called from a coroutine or another suspend function
        // they need to be wrapped in a launch (async is to be avoided in most cases)
        // this will be called sequentially, use separate launch blocks for parallelism
        viewModelScope.launch {
            _dashboardUi.value = repo.loadCombined() 
            _people.value = repo.fetchData()
        }
        // two separate viewModelScopes will run in parallel independently -> like using supervisorScope
        viewModelScope.launch {
            _dashboardUi.value = repo.loadCombined()
        }
        viewModelScope.launch {
            _people.value = fetchData()
        }
        // Single parent with two child launches: siblings are tied together; if one fails, the other is cancelled (unless you use supervisorScope)
        // If you need to wait for both and then proceed, use async/await inside supervisorScope.
        viewModelScope.launch {
            // these are called in parallel
            launch {
                _dashboardUi.value = repo.loadCombined()
            }
            launch {
                _people.value = fetchData()
            }
        }
        // Using supervisorScope to isolate failures: if one child fails, the other continues
        viewModelScope.launch {
            supervisorScope {
                launch {
                    _dashboardUi.value = repo.loadCombined()
                }
                launch {
                    _people.value = fetchData()
                }
            }
        }
    }
}
```

---

## What is recomposition in Jetpack Compose
- Recomposition is Compose's process of re-running composable functions when their inputs (state) change.
- It's not the same as re-creating views; Compose compares the UI tree and applies minimal updates to the UI.
- Avoid unnecessary recompositions by using `remember`, `derivedStateOf`, and small composable functions.
- Prefer immutable state and single source of truth (ViewModel exposing StateFlow) so Compose can efficiently recompose.
- Talking points: explain how state flows into composables, selective recomposition, and performance implications.

---

## Remember vs LaunchedEffect vs SideEffect in Compose
- Side effects: operations that interact with the outside world or need lifecycle control (I/O, navigation, timers, launching coroutines, updating a ViewModel/Flow, platform APIs). 
  - Compose functions should be pure and fast; effect handlers exist so side‑effects run in a controlled way (not during composition, with proper cancellation/cleanup).
- `remember`: stores a value across recompositions. Use for caching expensive calculations or objects that should persist as long as the composable is in memory.
- `LaunchedEffect`: runs a suspend function when the key(s) change. Use for side effects that need to run in a coroutine scope tied to the composable lifecycle (e.g., data loading).
- `SideEffect`: runs after every successful recomposition. Use for non-suspending side effects that need to happen after the UI is updated (e.g., logging, updating external state).
- When to use:
  - Use `remember` for state that should persist across recompositions.
    - for non-suspending cached state or expensive object initializations
  - Use `LaunchedEffect` for one-off or repeated side effects that depend on state.
    - use for suspend work, collecting Flows, network calls, etc.
  - Use `SideEffect` for non-suspending actions that need to run after recomposition.
    - use to sync compose state with external APIs or logging, must run after UI update
  - use `DisposableEffect` when you need setup and teardown tied to the composable lifecycle.
    - e.g., registering/unregistering listeners, starting/stopping animations
  - TODO: so state changing and recomposition are similar. like state change usually triggers recomposition? so wouldn't these both be triggered by state change?
- Talking points: lifecycle of each, how they relate to recomposition, and examples of use cases.

---

## What is the difference between lambda and higher-order functions in Kotlin
- Higher-order functions are functions that take other functions as parameters or return functions.
- Lambdas are anonymous functions that can be passed as arguments to higher-order functions.
- Example of higher-order function:
```kotlin
fun operateOnNumbers(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}
```

---

## What is your preferred architecture pattern for Android apps and why (MVVM, MVI, etc.)
- Architecture depends on the use case, team familiarity, and project complexity.
  - direct the conversation by giving a scenario
    - if we have multiple features with several developers, I would prefer MVVM with Clean Architecture principles

- Preferred architecture: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- Reasons:
  - Clear separation of concerns: View handles UI, ViewModel manages state and business logic, Model handles data.
  - ViewModel lifecycle-aware: survives configuration changes, reducing UI-related bugs.
  - Testability: ViewModels and Use Cases can be unit tested without Android dependencies.
  - Scalability: easy to add features and maintain codebase as app grows.
  - Integration with Jetpack libraries: works well with LiveData/StateFlow, Navigation, and Room.
- MVI (Model-View-Intent) is also a solid choice for apps with complex state management and unidirectional data flow, but MVVM is often more straightforward for typical app architectures.
  - Reasons:
    - Unidirectional data flow can simplify reasoning about state changes.
    - Intent-based actions can make side effects clearer.
    - However, it can introduce more boilerplate and complexity for simple use cases.
- Talking points: contrast with MVI (unidirectional data flow) and when it might be preferred (e.g., complex state management).

---

## What are the SOLID principles
1. Single Responsibility Principle: a class should have only one reason to change.
2. Open/Closed Principle: software entities should be open for extension but closed for modification.
3. Liskov Substitution Principle: subclasses should be substitutable for their base classes without altering correctness.
4. Interface Segregation Principle: clients should not be forced to depend on interfaces they do not use.
5. Dependency Inversion Principle: high-level modules should not depend on low-level modules; both should depend on abstractions.

---

## What are the differences between Kotlin and Java, what do you like about Kotlin
- Null Safety: Kotlin has built-in null safety features (nullable types, safe calls) that reduce NPEs.
  - at compile time rather than runtime
- Conciseness: Kotlin's syntax is more concise, reducing boilerplate (data classes, type inference, extension functions).
  - higher order functions reduce verbosity
- Coroutines: Kotlin's native support for coroutines simplifies asynchronous programming.
  - Java uses threads, callbacks, or CompletableFutures, executor services 
  - TODO: why are coroutines better than these? 
    - no deadlocks, lightweight, structured concurrency, no callback hell, no thread management, and easier to read
    - NOTE: most coroutine work is android-specific with lifecycleScope, viewModelScope, etc.
- Extension Functions: Kotlin allows adding functions to existing classes without inheritance.
  - this helps with cleaner APIs and DSLs and be open for extension without modifying original class
  - extend a library you don't own - e.g., adding utility functions to String, List, etc.
    - TODO: what is an example of extending a library you didn't own?
- Higher-Order Functions and Lambdas: Kotlin has first-class support for functional programming constructs.
  - Java has lambdas since Java 8, but Kotlin's syntax is more concise and integrated.
  - Java does not have higher-order functions as first-class citizens like Kotlin
- Interoperability: Kotlin is fully interoperable with Java, allowing gradual migration.
- Smart Casts: Kotlin automatically casts types when safe, reducing explicit casting.

- When asked what they like about Kotlin, they can mention any of the above points, emphasizing how these features improve productivity, code safety, and maintainability.

---

## what are scope functions in Kotlin (let, run, also, apply, with) and when to use each
- `let`: use for null-checking and scoping. The object is `it`. Returns the lambda result.
- `run`: use for executing a block with the object as `this`. Returns the lambda result.
- `also`: use for side effects (logging, debugging) with the object as `it`. Returns the original object.
- `apply`: use for configuring an object with `this`. Returns the original object.
- `with`: use for calling multiple methods on an object without repeating its name. The object is `this`. Returns the lambda result.
- When to use:
  - `let`: null safety, chaining calls.
  - `run`: object initialization, executing blocks with `this`.
  - `also`: side effects without altering the object.
  - `apply`: object configuration, builder patterns.
  - `with`: grouping multiple calls on an object.

```kotlin
val person = Person().apply {
    name = "Alice"
    age = 30
}.also {
    Log.d("Person", "Created person: $it")
}
val length = name?.let {
    it.length
} ?: 0
val result = with(person) {
    "Name: $name, Age: $age"
}
val upperName = person.run {
    name.toUpperCase()
}
```

---

## What is the difference between DI and Dependency Inversion Principle
- Dependency Injection (DI) is a design pattern where an object's dependencies are provided externally rather than the object creating them itself. It promotes loose coupling and enhances testability by allowing dependencies to be swapped easily (e.g., with mocks in tests).
- Dependency Inversion Principle (DIP) is a design principle not a pattern
- DIP = principle, DI = pattern (applied implementation of the principle)

---
 
## What are the different Android app components
- Activities: UI screens that users interact with. They handle user input and display data.
- Fragments: reusable UI components that can be combined within activities. They have their own lifecycle.
- Services: background components that perform long-running operations without a UI. They can run in the foreground or background.
- Broadcast Receivers: components that listen for system-wide or app-specific broadcast messages (intents) and respond accordingly.
- Content Providers: components that manage shared app data and provide a standard interface for data access across apps.
- When to use:
  - Activities for full-screen UI.
  - Fragments for modular UI components within activities.
  - Services for background tasks (e.g., music playback, data sync).
  - Broadcast Receivers for responding to system events (e.g., connectivity changes).
  - Content Providers for sharing data between apps (e.g., contacts, media).

---

## How to choose an architecture for an app
- none of the patters are bad, just depends on the use case
- consider app complexity, team familiarity, scalability needs, testability requirements
- MVVM: good for most apps, clear separation of concerns, integrates well with Jetpack libraries
- MVI: good for complex state management, unidirectional data flow, but can introduce more boilerplate
- Clean Architecture: good for large, complex apps needing strict separation of layers
- When choosing, consider:
  - Team expertise: choose a pattern the team is comfortable with.
  - App complexity: simpler patterns for simple apps, more structured for complex apps.
  - Testability: prefer patterns that facilitate unit testing.
  - Maintainability: choose patterns that promote clean, modular code.
- MVC and MVP are not ideal due to tight coupling and harder testability.
  - since apps now have multiple features and different data sources, these patterns can lead to bloated controllers/presenters

---

## How can you articulate what a coroutine is in an interview
Thread 
- physical implementation of concurrency
  - directing hardware to run multiple tasks simultaneously
  - you either have success or failure on the task -> cannot pause and resume
  - Blocking

Coroutine
- lightweight, logical unit of concurrency
  - directing software to manage multiple tasks cooperatively
  - suspend and resume execution without blocking threads or losing progress on the task
  - Non-blocking
  - more control 
    - Scope - how long the task should live
      - CoroutineScope - Custom scope you create for specific tasks
      - GlobalScope - lives as long as the app is running
      - ViewModelScope - tied to ViewModel lifecycle
      - LifecycleScope - tied to Activity/Fragment lifecycle
      - SupervisorScope - controls failure propagation for child coroutines
        - nested coroutines can fail independently without cancelling the parent
        - dashboard has 3 different APIs that decide how the dashboard looks, if one API fails the other two can still load
    - Dispatcher - Where the task should run (Main, IO, Default, Unconfined)
      - Unconfined - free resources (combined into a pool, combines IO and Default if they are not being used)
        - Main always has something running on it, so will not be used for Unconfined
      - Default - uses system resources for CPU-intensive tasks (e.g., sorting, parsing)
    - Builders - How the task should start/execute (launch, async)
      - RunBlocking - will block the current thread until the coroutine task completes
        - if run on Main thread, will freeze the UI (ANR - Application Not Responding)
      - Launch - starts a coroutine that does not return a result (returns a job); used for fire-and-forget tasks.
        - completes tasks/jobs one after another in sequence
      - Async - starts a coroutine that returns a Deferred<T>; used for concurrent tasks
        - is not completed in sequence, can run multiple tasks at the same time (concurrently)
          - e.g., loading data from multiple network sources simultaneously
      - Join and Mutex
        - these are ways to combine coroutines together 
        - Join - wait for a coroutine to finish before proceeding
        - Mutex - mutual exclusion, lock 2 (or more) jobs to complete their task before proceeding

---

## Storage options in Android (SharedPreferences, DataStore, Room, File Storage)
- language, dark mode, font size, etc.

- SharedPreferences vs DataStore:
  - SharedPreferences: key-value pairs, synchronous API, prone to data corruption, not type-safe, does not support coroutines natively.
    - because it is not asynchronous, it can block the main thread if used improperly -> ANR 
  - DataStore: key-value (Preferences DataStore) or typed objects (Proto DataStore), asynchronous API using coroutines, safer and more robust.
    - specific to device, not cloud-synced
    - Protocol (Proto) Buffers -> need to define schema (protocol buffer files, .proto), more setup but type-safe and efficient
      - let gradle generate code from .proto files, auto generate java/kotlin classes based on schema at compile time
  - Prefer DataStore for new apps due to better performance and safety.
- Local vs External File Storage:
  - Local (internal) storage: private to the app, not accessible by other apps, good for sensitive data.
    - Scoped Storage -> permission to only access a specific folder/file type/ media type, etc.
  - External storage: shared space, accessible by other apps, requires permissions, good for large files (images, videos).
- Remote DB (Firebase) vs Local DB (Room):
  - Firebase Realtime Database / Firestore: cloud-hosted, real-time syncing, offline support, good for apps needing real-time collaboration or cloud storage.
  - Room: local SQLite abstraction, type-safe, compile-time checks, good for structured local data storage.
- Android Keystore:
  - Securely store cryptographic keys and sensitive data.
  - Use for storing encryption keys, tokens, or any sensitive information.

---

# Kotlin Multi-Platform -> Now is Compose Multi-Platform
- initially google made kotlin for just android development, now kotlin is used for backend, web, desktop, and multiplatform development
  - kotlin can now do spring boot, javascript, native (ios, mac, windows, linux), and android
- compose multi-platform allows you to share UI code across android, desktop, and web
  - compose is a kotlin library, not just a framework for android
- kotlin and compose are turned into bitcode that other platforms can understand and run
- benefits: share code across platforms, faster development, consistent UI/UX
[Documentation](https://www.jetbrains.com/compose-multiplatform/)
[Youtube Tutorial](https://www.youtube.com/watch?v=WT9-4DXUqsM)

---

## Apollo vs Retrofit
- Retrofit: is a Restful API client
- Apollo: is a GraphQL API client
- Ktor: is a general-purpose HTTP client that can be used for both REST and GraphQL APIs
  - for KMP projects
- When to use:
  - Use Retrofit for RESTful APIs with fixed endpoints and standard HTTP methods.
  - Use Apollo for GraphQL APIs where you need flexible queries, mutations, and subscriptions.
  - Use Ktor when you need a lightweight, multiplatform HTTP client for both REST and GraphQL.

---

## How do you handle background tasks in Android
- if the app is in the foreground and the task is short-lived (a few seconds), use coroutines with lifecycleScope or viewModelScope
- for longer tasks or tasks that need to run when the app is in the background, use WorkManager
- for periodic tasks, use WorkManager with periodic work requests 

---

## Difference between mutableStateOf and derivedStateOf in Compose
- `mutableStateOf`: creates a mutable state holder that triggers recomposition when its value changes.
  - use when you have a single piece of state that can change independently
- `derivedStateOf`: creates a state that is derived from other states. It only recomposes when the underlying states change.
  - use when you want to compute a value based on other states, avoiding unnecessary recompositions
- When to use:
  - Use `mutableStateOf` for independent state variables.
  - Use `derivedStateOf` for computed state that depends on other state variables.
  - This helps optimize performance by reducing recompositions.

```kotlin
var firstNumber: Int by mutableStateOf(0)
var secondNumber: Int by mutableStateOf(0)
// sum will only recompute when firstNumber or secondNumber change
val sum: Int by derivedStateOf { firstNumber + secondNumber }
```

---

## Serialization vs Parcelable in Android
- Serialization:
  - TODO: is this java serialization or kotlin serialization?
  - Converts an object into a byte stream for storage or transmission.
  - Slower, uses reflection, more overhead.
  - Use for long-term storage or network transmission.
- Parcelable:
  - Android-specific interface for efficient object serialization.
  - Faster, less overhead, requires boilerplate code.
  - Use for passing data between activities/fragments via Intents or Bundles.
- When to use:
  - Use Parcelable for inter-component communication within Android apps.
  - Use Serialization for network transmission or long-term storage.

```kotlin
// Parcelable example
@Parcelize
data class User(val name: String, val age: Int) : Parcelable

// Serialization example (Kotlinx Serialization)
@Serializable
data class User(val name: String, val age: Int)
```

--- 

## State vs Stateless in Compose
- State:
  - Holds mutable data that can change over time.
  - Triggers recomposition when updated.
  - Use for UI elements that need to reflect changing data (e.g., form inputs, toggles).
- Stateless:
  - Does not hold any mutable data.
  - Rely on parameters passed from parent composables.
  - Use for reusable UI components that do not manage their own state (e.g., buttons, icons).
- When to use:
  - Use stateful composables for managing local UI state.
  - Use stateless composables for reusable, pure UI components.

```kotlin
@Composable
fun Screen() {
    var count by remember { mutableStateOf(0) }
    Column {
        StatefullCounter() // stateful
        StatelessCounter(count, onIncrement = { count++ }) // stateless
    }
}
// Stateful composable
@Composable
fun StatefullCounter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
// Stateless composable
@Composable
fun StatelessCounter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}
```

---

## What is the Keystore in Android
- Android Keystore is a system that allows secure storage of cryptographic keys and sensitive data.
- It provides hardware-backed security on supported devices, protecting keys from extraction.
- Use cases: storing encryption keys, tokens, or any sensitive information that needs protection.

```kotlin
// Example: Generating and storing a key in the Keystore
val keyGenParams = KeyGenParameterSpec.Builder(
    "my_key_alias",
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM) // this is more secure than ECB
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // GCM does not require padding
    .build()
val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
keyGenerator.init(keyGenParams)
val secretKey = keyGenerator.generateKey()
```

---

## Migrate from Java to Kotlin
[Tutorial](https://www.udacity.com/course/kotlin-for-android-developers--ud888?utm_source=chatgpt.com)

- First moved to gradle 8.13 
  - added kotlin plugin and kotlin stdlib dependency

- Code -> Convert Java File to Kotlin File 
  - can get tripped up on nullability, so double-check the converted code
  - also might need help with some lambda syntax

- change Note -> kept @JvmField for public fields that need to be accessed from java code
  - otherwise kotlin will generate getters/setters that java code won't recognize
  - notice it did not convert to data class automatically, so I manually changed it to data class

- When we say something is an "expression” in Kotlin, we mean that:
    - it returns a value

### ActivityMain
- `import kotlinx.android.synthetic.main.content_main.*` this required an extension plugin for kotlin, 
  - that allows kotlin to walk through all the xml layouts and creates synthetic source files, which then creates extension vals for every view with an id.
  - DEPRECATED -> use ViewBinding or findViewById instead
  - add `buildFeatures { viewBinding = true }` to gradle file

### NotesAdapter (RecyclerView Adapter)
- ensure new overrides have correct nullability annotations
- can reduce boilerplate in multiple places, but to run just ensure nullability

### NoteDatabase
- old migration -> use Anko -> deprecated
- new migration -> use Room
- .use() -> closable resource management

### Questions 
- when migrating, is it best to update to jump technology? Room is way easier to use than Anko SQLite
- after going through the migration, I actually felt like it was best to just rewrite in a side by side screen
  - essentially as long as all the functions that other files need are present and return the same types, the internal implementation can be completely different
--- 



Tips for answering in interviews
- Structure answers: definition, how it works, when to use it, and a short example or anecdote.
- Keep answers concise but show depth: state trade-offs, pitfalls, and best practices.
- If asked for code, give a small snippet and explain intent rather than writing long implementations.
[GraphQL](https://www.postman.com/devrel/graphql-examples/request/1nxdux9/spacex)



--- 

# Kal Questions
Describe last project
App Description
Team
Tech contribution
Role and responsibilities as a lead
Explain MVVM architecture
MVVM vs MVP
Explain Clean code architecture
Clean code architecture
MVP VS MVVM VS MVI
Normal class vs  data class
lateint vs lazy
What are sealed classes?
Stateflow vs Livedata
Supervisor job vs job
Stateflow vs shared flow
Performance monitoring tools used to optimise apps
Experience using Workmanager and use case
Why to use dependency injection..libraries
What are coroutines? Difference types of scopes in coroutines
What are Dispatchers in coroutines and types

Compose:
Why compose
What is launched effect compose
What is dispose effect
When does recomposition occur
Ways to avoid recomposition
What is state hoisting
Types of side effects
What is CompositionLocal

# Kal
## LiveData vs StateFlow
**Lifecycle awareness:**
- LiveData is lifecycle-aware, automatically managing subscriptions based on activity/fragment lifecycle states. 
- StateFlow is not lifecycle-aware by itself; you need to manually collect it within lifecycle-aware scopes (e.g., repeatOnLifecycle).

**State management:** 
- Both hold state and emit the latest value to observers. 
- StateFlow requires an initial state when created, avoiding null values. 
- LiveData can hold nullable states.

**Threading and operators:** 
- StateFlow supports powerful Kotlin Flow operators and can specify threads via flowOn(). 
- LiveData transformations are more limited and run on the main thread.

**Performance:** 
- StateFlow is generally more performant and flexible, especially with multiple subscribers and complex transformations.

**Use cases:** 
- LiveData is still convenient for simple UI updates and lifecycle management. 
- StateFlow is recommended for new projects, especially those using Kotlin Coroutines extensively or Jetpack Compose.

## Avoiding recomposition in Jetpack Compose
**Use stable and immutable data:**
- Prefer immutable data classes and mark them with @Immutable or use @Stable annotations to help Compose skip recomposition when data hasn't changed.

**Leverage remember and rememberSaveable:**
- Cache state or expensive computations with these to avoid recomputation on every recomposition.

**Use derivedStateOf:**
- For computed values based on other states, wrap them in derivedStateOf to trigger recomposition only when inputs actually change.

**Key parameter and remember in Lazy lists:**
- Use keys for items in LazyColumn and remember expensive items to avoid unnecessary recompositions during scrolling.

**Use SideEffect, LaunchedEffect, and DisposableEffect carefully:**
- Properly manage side effects to prevent recompositions triggered by unexpected state changes.

## Why SingleLiveEvent?
Normal LiveData will re-emit the last value to new observers, which can cause unwanted repeated events (e.g., showing a toast multiple times on screen rotation).
SingleLiveEvent ensures the event is consumed only once by one observer, preventing duplicate executions.

## MVI

Use a sealed class or enum for Intents.
Use a data class for the immutable Model state.

The ViewModel or equivalent layer processes Intents, performs business logic, and emits new Model states, often using Kotlin Flow or StateFlow.

The Composable or traditional View observes the Model state and re-renders
Aligns well with Kotlin coroutines and Flow to handle asynchronous state changes reactively.
Better testability because state transformations are pure and isolated.

```kotlin
sealed class CounterIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
}
sealed class CounterState {
    data class Success(val count: Int) : CounterState()
    data class Error(val message: String) : CounterState()
}
```

## 
SharedFlow: Configurable hot flow for shared events and replay.
StateFlow: State holder and hot flow for UI state with current value.
Emitters: emit(), tryEmit(), or value for pushing data.
Collectors: Subscribers receiving ongoing data streams.
Replay Cache: Buffer enabling recent values replay for new collectors.

---

Why compose
What is launched effect compose
What is dispose effect
When does recomposition occur
Ways to avoid recomposition
What is state hoisting
Types of side effects
What is CompositionLocal

--- 

asked about how I pioneered compose -> search, -> where else did you do something like that? -> 
some features never made it to production, but at Ally bank -> ...


# Will Daily Routine
- not every day is the same, but this is a general outline of a typical day
- Jr -> look at tickets and changes and responding to PRs
- Sr -> what are the changes being made by backend team -> update the point of contact with other teams (UI/UX, QA, backend, iOS, etc.)
- work with scrum master / PM to prioritize tasks and blockers for the team
  - figure out time estimates for tasks

- 9am start day
- 9:30 standup with offshore (30min - 1 hour)
  - scrum master runs this
  - giving a daily update on what I did yesterday, what I'm doing today, any blockers
- 10:30 grooming session -> prioritize backlog items, clarify requirements with PMs, estimate effort with team
  - with android team + either QA or UI/UX
  - scrum master leads this
  - help assign point values to tasks
  - identify dependencies or blockers
- 11:30 code review
  - me solo doing this
  - can be with a pair programming session if needed
- 12:30 lunch
- 1:30-5 get my own jira tasks done or assisting others on blockers or mentoring juniors

Bi-Week:
- monday: sprint planning -> substitute for grooming and possibly both, but lose end of day work
- friday: retrospective -> substitute for grooming, but lose end of day work if we do both
- product demo - as needed (typically based on how many features are done) typical 1 time every other month



---

Koin -> runtime -> has more errors at runtime if something is not configured properly
Hilt -> compile time -> catches more errors at compile time if something is not configured properly
- this means that most people prefer Hilt over Koin for larger projects where catching errors early is important
- KMP / KMM -> Koin is preferred since Hilt does not support KMP/KMM yet
  - need to be more careful with Koin since it is runtime errors
  - Ktor as well