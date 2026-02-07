# Resources:
- [Medium - Beginner's Guide](https://medium.com/@bhoomigadhiya/side-effects-in-jetpack-compose-a-beginners-guide-249ba977e4d2)
- [Medium - Complete Guide](https://proandroiddev.com/complete-guide-to-side-effects-in-jetpack-compose-5be32b09514a)
- [Medium - 8 Side Effects](https://medium.com/@manishrana366/the-8-compose-side-effects-every-android-developer-must-master-in-2025-b6b251e9573f)


# Effect Handlers
[Code Lab](https://developer.android.com/codelabs/jetpack-compose-advanced-state-side-effects#0)
## Side Effects in Android (Kotlin) - Specific to Coroutines and Jetpack Compose
Side effects refers to any action that happens beyond the scope of the function. Using side effects in Jetpack Compose involves handling tasks that occur outside of the normal UI rendering process.
For example, opening a new screen when the user taps on a button is a side effect. This is because the new screen is opened by the operating system, outside of the scope of the composable function that displays the button.

In Android, these are routine but should be isolated and done off the main thread when they involve I/O.

### Common Android side effects
- Network calls (e.g., Retrofit/HttpUrlConnection)
- Database reads/writes (Room, ContentResolver/MediaStore)
- File I/O (internal/external storage, SAF URIs)
- Preferences/state stores (SharedPreferences, DataStore)
- Notifications and starting/stopping foreground services
- Starting activities, services, broadcasts; updating UI state
- Accessing hardware or sensors (camera, location, Bluetooth, microphone)
- Scheduling background work (WorkManager, AlarmManager)
- Logging/analytics; reading time/clock or random numbers

### Simple guidelines
- Keep core/business logic pure; perform effects at the app edges (ViewModel, Repository, Worker, Service, Receiver).
- Use coroutines for I/O: `Dispatchers.IO` for blocking I/O; switch back to Main for UI updates.
- Respect lifecycle and cancellation (e.g., `viewModelScope`, `lifecycleScope`).
- Handle errors and timeouts (`try/catch`, `withTimeout`), and surface user-visible failures appropriately.
- Don’t block the main thread with I/O; avoid long work in callbacks or onCreate.
- Request and check runtime permissions before effectful ops (e.g., camera, location, notifications on 33+).

### Where side effects live:
- XML: lifecycle methods (onCreate/onStart/onResume/onStop/onDestroy) and listeners; 
  - observe LiveData and mutate views directly (findViewById, viewBinding).
- Compose: use effect APIs to run side effects safely with recomposition: 
  - LaunchedEffect, SideEffect, DisposableEffect, rememberCoroutineScope, produceState, snapshotFlow.
  - composable function bodies should remain side-effect free -> gate side effects behind these effect handlers (above).

### Triggers:
- XML: you decide when to run (lifecycle callbacks and explicit calls).
- Compose: key-based effects run when keys change (state/inputs), avoiding re-running on every recomposition.

### Testing tip
- Hide side effects behind interfaces (e.g., `ProfileApi`, `FileStore`) and inject fakes in tests so core logic remains deterministic.

### Tiny example (ViewModel: fetch then update UI)
```kotlin
class ProfileViewModel(
    private val api: ProfileApi,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun loadProfile(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = api.getProfile(userId) // network = side effect
                withContext(Dispatchers.Main) {
                    _state.value = UiState.Loaded(profile) // UI state update
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) { _state.value = UiState.Error("Network error") }
            }
        }
    }
}
@Composable
fun LoadProfileButton() {
    var text by remember { mutableStateOf("Click to load") }
    Button(
        // every time the button is clicked, text state changes, causing recomposition 
        // -> side effect (network call) would re-run on every recomposition -> BAD
        // to make matters worse, it can recompose in many scenarios beyond just button clicks
        onClick = { text+= "!" }
    ) {
        makeNetworkCall() // side effect -> BAD -> will be called on every recomposition
        Text(text)
    }
}
```

--- 

## LaunchedEffect
- **Purpose**: Run suspend/coroutine work in response to entering composition or when specified keys change.
- **Lifecycle**: Launches when the composable first enters the composition; cancels and re‑launches when any key changes; cancels when the composable leaves the composition.
- **Use for**: One‑shot loads tied to the screen/state (fetch data, start animation, collect a Flow, request focus) triggered by stable keys.
- **Notes**:
  - requires a key (or keys) to control when it restarts and a coroutine block.
  - LaunchedEffect is a composable function itself; it cannot be called from non‑composable code.
  - Avoid unstable or frequently changing keys to prevent unintended relaunch loops.
  - Prefer ViewModel scopes for long‑lived business logic that must survive configuration changes.
  - If you just need to react after every recomposition without suspend, use SideEffect instead.
  - Not scoped to the composable function body; it runs independently in its own coroutine. (will not be canceled when composable function leaves composition)
    - can lead to memory leaks if not used carefully -> use rememberCoroutineScope for running coroutines tied to the composable lifecycle

LaunchedEffect composable is not idempotent. This means that it can produce different results if it is executed multiple times.

### Code
```kotlin
@Composable
fun ProfileScreen(userId: String, viewModel: ProfileViewModel = viewModel()) {
    val profileState by viewModel.state.collectAsState()

    // LAUNCHED EFFECT: runs when userId (key) changes and will cancel any previous coroutine if triggered before completion
    // LaunchedEffect(key1 = userId, key2 = planeId, block = { ... })
    // we can move the `block = { ... }` part outside since it's the last parameter
    LaunchedEffect(userId) {
        // this block is the coroutine 
        viewModel.loadProfile(userId) // side effect: network call
    }
    
    // key1 = true means this will run once when entering composition (won't re-run unless the composable leaves and re-enters)
    // re-entering could happen on navigation back to this screen or recomposition if this composable is removed and re-added
    LaunchedEffect(key1 = true) {
        // now this is not collected on every recomposition, only once when entering composition
        viewModel.sharedFlow.collect { event ->
            when (event) {
                is LaunchedEffectViewModel.ScreenEvents.ShowSnackbar -> {
                    // Show snackbar with event.message
                }
                is LaunchedEffectViewModel.ScreenEvents.Navigate -> {
                    // Navigate to event.route
                }
            }
        }
    }

    when (profileState) {
        is UiState.Loaded -> {
            val profile = (profileState as UiState.Loaded).profile
            Text("Welcome, ${profile.name}!")
        }
        is UiState.Error -> {
            val message = (profileState as UiState.Error).message
            Text("Error: $message")
        }
        else -> {
            Text("Loading...")
        }
    }
}
// ViewModel
class LaunchedEffectViewModel: ViewModel() {
    private val _sharedFlow = MutableSharedFlow<ScreenEvents>()
    val sharedFlow = _sharedFlow.asSharedFlow()
    
    init {
        viewModelScope.launch {
            // EMIT events to the shared flow
            _sharedFlow.emit(ScreenEvents.ShowSnackbar("Hello from ViewModel"))
        }
    }
    sealed class ScreenEvents {
        data class ShowSnackbar(val message: String): ScreenEvents()
        data class Navigate(val route: String): ScreenEvents()
    }
}

// Animation example
@Composable 
fun Animation(counter: Int) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = counter) {
        // animateTo is a suspend function, so we can call it inside LaunchedEffect (coroutine)
        animatable.animateTo(
            targetValue = counter.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
    }
}

```
 
---

## SideEffect
- **Purpose**: Run non‑suspending work after Compose applies the latest changes (after a successful recomposition commit).
- **Lifecycle**: Executes on the main thread after every successful recomposition of the hosting composable.
- **Use for**: Updating external, non‑Compose state to mirror Compose state (e.g., imperative APIs, adapters, analytics markers).
- **Notes**:
  - Don’t perform heavy or blocking work here; it runs often.
  - Not for launching coroutines or I/O; use LaunchedEffect or a ViewModel for that.

- FIRST OFF -> not used often
- typically for 3rd party libraries or external systems that need to be informed of state changes in Compose
- common use cases:
  - updating analytics systems with the latest UI state
  - firebase user (not a compose state) properties -> need to update the firebase user properties when compose state changes
  - informing imperative UI frameworks (e.g., RecyclerView adapters) of state changes

The SideEffect composable is idempotent, which means that it will always produce the same results, even if it is executed multiple times.

```kotlin
@Composable
fun SideEffectDemo(counter: Int) {
    // This composable displays a counter and updates an external analytics system
    Text("Counter: $counter")
    // SideEffect runs after every recomposition
    SideEffect {
        // Update external analytics system with the latest counter value
        AnalyticsSystem.logCounterValue(counter)
    }
}
```

### SideEffect vs LaunchedEffect
- SideEffect is for:
  - non-suspending work that runs after every recomposition.
  - idempotent operations.
- LaunchedEffect is for:
  - launching coroutines that run when keys change or on entering composition.
  - non-idempotent operations that may have side effects like network calls or database operations.

---

## DisposableEffect
- **Purpose**: Set up work that requires cleanup (registering listeners/receivers, acquiring resources) and dispose it when the keys change or the composable leaves composition.
- **Lifecycle**: Runs an effect on enter; if keys change, it disposes the previous effect then re‑runs; disposes on leaving composition.
- **Use for**: Register/unregister callbacks, sensors, broadcast receivers; manage resources with a clear acquire/release.
- **Notes**:
  - Choose keys that uniquely identify what’s being managed so disposal/re‑setup happens correctly.
  - To avoid stale captures in callbacks, prefer capturing latest values via stable references (e.g., rememberUpdatedState in surrounding code), not by re‑creating listeners every recomposition.

- works like LaunchedEffect but for non-coroutine side effects that need cleanup
- What needs to be disposed of?`
  - listeners (e.g., location updates, sensor updates, broadcast receivers)
  - resources (e.g., camera, microphone, file handles)
  - anything that needs to be cleaned up to avoid memory leaks or unnecessary resource usage

```kotlin
@Composable
fun DisposableEffectDemo() {
    val lifecycleOwner = LocalLifecycleOwner.current
    // this is BAD -> observer needs to be disposed of when the composable leaves composition
    // we will wrap this in DisposableEffect to ensure proper cleanup
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                // Handle onStart event
            }
            Lifecycle.Event.ON_STOP -> {
                // Handle onStop event
            }
            else -> {}
        }
    }
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // Handle onStart event
                }
                Lifecycle.Event.ON_STOP -> {
                    // Handle onStop event
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // onDispose block is called when the composable leaves composition or key changes
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
```

---

## rememberUpdatedState
- **Purpose**: Capture the latest value of a variable in a stable reference that can be used in long‑lived callbacks or effects without causing re‑creation.
- **Behavior**: Returns a State<T> that always reflects the most recent value passed to it.
- **Use for**: Avoiding stale captures in callbacks registered in DisposableEffect or other long‑lived contexts.
- **Notes**:
    - Use inside DisposableEffect or similar to ensure callbacks see the latest values without needing to re‑register them.
    - Helps prevent memory leaks or unnecessary re‑registrations due to changing dependencies.

- You need rememberUpdatedState ONLY WHEN: A coroutine runs longer than one composition AND depends on changing values.
- When you do NOT need rememberUpdatedState: If you want the coroutine to restart when the value changes

```kotlin
@Composable
fun RememberUpdatedStateDemo(
    searchQuery: String,
    filter: String,
) {
    val viewModel: SearchViewModel = viewModel()
    // without rememberUpdatedState, filter could be stale if it changes
    val currentFilter by rememberUpdatedState(newValue = filter)
    
    // LaunchedEffect runs only once, rememberUpdatedState ensures that the latest filter is used
    
    // LaunchedEffect will rerun only when searchQuery changes
    // filter changes will not restart LaunchedEffect (won't cancel and relaunch the search)
    // but the latest filter value will be used the next time the search is performed because of rememberUpdatedState
    // LaunchedEffect captures values at the moment it starts, not the moment it reruns.
    // LaunchedEffect only responds to keys. Everything not in the key can become stale.
    // rememberUpdatedState is how you keep non-key values fresh without causing restarts.
    LaunchedEffect(key1 = searchQuery) {
        delay(5000L) // wait for 5 seconds
        viewModel.search(searchQuery, currentFilter) // use latest filter value
    }
}
```

---

## rememberCoroutineScope
- **Purpose**: Provides a CoroutineScope tied to the composable’s lifecycle for launching work from event handlers (e.g., onClick) rather than during composition.
- **Lifecycle**: The scope is canceled when the composable leaves the composition.
- **Use for**: User‑initiated tasks started from callbacks; short‑lived work tied to the UI element.
- **Notes**:
  - Don’t launch coroutines directly in the composable body; trigger them from events or in LaunchedEffect.
  - For work that should outlive the composable (e.g., across rotations), use viewModelScope instead.

- a lifecycle aware coroutine scope, only use on callbacks (e.g. onClick) or LaunchedEffect to avoid launching on every recomposition
  - could be used for a search button to launch a search when clicked

- Why do we create a val myScope = rememberCoroutineScope() instead of just calling rememberCoroutineScope().launch {...} directly in the onClick?
  - because rememberCoroutineScope() creates a new scope every time it's called, so if we call it directly in the onClick, we would be creating a new scope every time the button is clicked, 
  - which is not what we want. By creating a val myScope = rememberCoroutineScope(), we create a single scope that can be reused for multiple clicks.

```kotlin
@Composable
fun SearchButton(viewModel: SearchViewModel = viewModel()) {
    val myScope = rememberCoroutineScope()
    // DO NOT launch coroutine here in the composable body (BAD!!!)
    myScope.launch {
        viewModel.performSearch() 
    }
    
    Button(onClick = {
        // launch a coroutine in response to button click
        myScope.launch {
            viewModel.performSearch() // side effect: network call
        }
    }) {
        Text("Search")
    }
}
    

```

---


## produceState
- **Purpose**: Bridge asynchronous sources into a Compose State<T> using a coroutine that updates the state value over time.
- **Lifecycle**: Starts when entering composition; restarts when keys change; cancels on leaving composition.
- **Use for**: Ad‑hoc async computations or callback‑based APIs where you want to expose a single State to the UI.
- **Notes**:
  - Prefer collectAsState()/collectAsStateWithLifecycle for Flow/LiveData; use produceState when those aren’t available.
  - Initialize with a sensible default; handle cancellation and errors to avoid stuck UI states.

- very similar to LaunchedEffect, but instead of just running a coroutine, it produces a State<T> that can be observed in the UI
- useful for creating state from asynchronous data sources or computations:
    - 

```kotlin
@Composable 
fun ProduceSateDemo(countUpTo: Int) : State<Int> {
    return produceState(initialValue = 0) {
        while(value < countUpTo) {
            delay(1000L) // wait for 1 second -> delay is a suspend function
            value += 1 // update the state value
        }
    }
}
// similar to using flow, these two are equivalent
@Composable
fun FlowDemo(countUpTo: Int) : State<Int> {
    return flow<Int> {
        var value = 0
        while(value < countUpTo) {
            delay(1000L)
            value += 1
            emit(value)
        }
    }.collectAsState(initial = 0)
}
```

---

## derivedStateOf
- **Purpose**: Create a state that derives its value from other states, recalculating only when dependencies change.
- **Behavior**: Lazily computes the derived value and caches it until any of the source states change.
- **Use for**: Optimizing expensive computations based on multiple state reads to avoid unnecessary recompositions.
- **Notes**:
  - Use inside composables or remember blocks to create derived state.
  - Helps improve performance by preventing redundant calculations during recompositions.
  - Be cautious of overusing; only apply when the derived computation is non-trivial.
  - Ideal for scenarios like filtering/sorting lists based on multiple state inputs.
  - Can be combined with remember to cache the derived state across recompositions.

```kotlin
@Composable
fun DerivedStateOfDemo() {
    var counter by remember { mutableStateOf(0) }
    // every time recomposition happens, this line will be re-executed
    // imagine if this computation is expensive (e.g., filtering a large list)
    // every time counter changes, counterText will be recomputed concatenating the string
    // this will lead to results like: "The counter is 0123..."
    val counterText = "The counter is $counter"
    
    // to avoid recomputing counterText on every recomposition, we can use derivedStateOf
    // this will only recompute when counter changes and provide a cached value otherwise
    val optimizedCounterText by derivedStateOf {
        "The counter is $counter"
    }
    
    Button(onClick = { counter++ }) {
        Text(counterText)
        Text(optimizedCounterText)
    }
}
```

---

## snapshotFlow
- **Purpose**: Convert reads of Compose state into a cold Flow that emits when the read state changes.
  - flow has backpressure operators (debounce, buffer, etc.) that can be applied to control emissions 
- **Behavior**: Captures Snapshot state reads inside its block and emits a new value whenever any of those reads change; applies distinct‑until‑changed semantics.
- **Use for**: Integrating Compose state changes with Flow operators (debounce/buffer/combine) or external reactive consumers.
- **Notes**:
  - Collect snapshotFlow in a coroutine (e.g., via LaunchedEffect or a ViewModel) and keep the block lightweight.
  - Be mindful of rapid emissions; apply backpressure operators (debounce/sample) if needed.

```kotlin
@Composable
fun SnapshotFlowDemo() {
    val scaffoldState = rememberScaffoldState()
    
    LaunchedEffect(key1 = scaffoldState) {
        snapshotFlow { scaffoldState.snackbarHostState }
            .mapNotNull { it.currentSnackbarData?.message }
            .distinctUntilChanged()
            .collect { message ->
                println("New snackbar message: $message")
            }
    }
}
```

javni with will
ivan with armando
