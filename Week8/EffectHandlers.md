# Effect Handlers
[Code Lab](https://developer.android.com/codelabs/jetpack-compose-advanced-state-side-effects#0)
## Side Effects in Android (Kotlin) - Specific to Coroutines and Jetpack Compose
Side effects are any operations that touch the outside world or mutate state beyond a function's local variables. 
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
    sealed class {
        data class ShowSnackbar(val message: String): ScreenEvents()
        data class Navigate(val route: String): ScreenEvents()
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

---

## DisposableEffect
- **Purpose**: Set up work that requires cleanup (registering listeners/receivers, acquiring resources) and dispose it when the keys change or the composable leaves composition.
- **Lifecycle**: Runs an effect on enter; if keys change, it disposes the previous effect then re‑runs; disposes on leaving composition.
- **Use for**: Register/unregister callbacks, sensors, broadcast receivers; manage resources with a clear acquire/release.
- **Notes**:
  - Choose keys that uniquely identify what’s being managed so disposal/re‑setup happens correctly.
  - To avoid stale captures in callbacks, prefer capturing latest values via stable references (e.g., rememberUpdatedState in surrounding code), not by re‑creating listeners every recomposition.

---

## rememberCoroutineScope
- **Purpose**: Provides a CoroutineScope tied to the composable’s lifecycle for launching work from event handlers (e.g., onClick) rather than during composition.
- **Lifecycle**: The scope is canceled when the composable leaves the composition.
- **Use for**: User‑initiated tasks started from callbacks; short‑lived work tied to the UI element.
- **Notes**:
  - Don’t launch coroutines directly in the composable body; trigger them from events or in LaunchedEffect.
  - For work that should outlive the composable (e.g., across rotations), use viewModelScope instead.

---

## produceState
- **Purpose**: Bridge asynchronous sources into a Compose State<T> using a coroutine that updates the state value over time.
- **Lifecycle**: Starts when entering composition; restarts when keys change; cancels on leaving composition.
- **Use for**: Ad‑hoc async computations or callback‑based APIs where you want to expose a single State to the UI.
- **Notes**:
  - Prefer collectAsState()/collectAsStateWithLifecycle for Flow/LiveData; use produceState when those aren’t available.
  - Initialize with a sensible default; handle cancellation and errors to avoid stuck UI states.

---

## snapshotFlow
- **Purpose**: Convert reads of Compose state into a cold Flow that emits when the read state changes.
- **Behavior**: Captures Snapshot state reads inside its block and emits a new value whenever any of those reads change; applies distinct‑until‑changed semantics.
- **Use for**: Integrating Compose state changes with Flow operators (debounce/buffer/combine) or external reactive consumers.
- **Notes**:
  - Collect snapshotFlow in a coroutine (e.g., via LaunchedEffect or a ViewModel) and keep the block lightweight.
  - Be mindful of rapid emissions; apply backpressure operators (debounce/sample) if needed.



