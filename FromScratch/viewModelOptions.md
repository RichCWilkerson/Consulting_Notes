# ViewModel Options Guide
## Architectural pattern fit (MVC, MVP, MVVM, MVI, Clean)
### MVC (classic Android):
- Controller: 
  - Activity/Fragment orchestrates UI events, navigation, and coordinates Model ↔ View. Keep business logic minimal.
- View: 
  - XML layouts and View classes; renders state; no business logic.
- Model: 
  - POJOs, repositories, data sources.
### MVP:
- Presenter: holds UI logic, talks to Model, pushes data to View via an interface (no Android types).
- View: interface implemented by Activity/Fragment; forwards user input to Presenter; renders via callbacks.
- Model: repositories/data sources.
### MVVM:
- ViewModel: exposes UI state (LiveData/StateFlow) and one‑off events (SharedFlow/Event). No references to View/Context/NavController.
- View (Activity/Fragment): observes state/events, updates UI, invokes ViewModel methods.
- Model: repositories/data sources.
### MVI (Model‑View‑Intent):
- View: renders State, emits user Intents (button clicks, text changes) to ViewModel.
- Intent: sealed class representing user actions or external inputs (e.g., Refresh, ClickItem(id)).
- Reducer: pure function (State × Intent -> State). Encodes how intents change state.
- State: immutable snapshot of the whole screen (use data class; exposed as StateFlow).
- Effect (aka Side‑effect/One‑off): navigation, toast, analytics; exposed as SharedFlow.
- Store (on Android, usually the ViewModel): holds StateFlow, applies reducer, performs effects via use cases/repos.
### Clean Architecture (layers):
- Presentation: MVVM/MVI using StateFlow/SharedFlow. No IO; translates domain models to UI models.
  - Avoid LiveData outside presentation.
- Domain: use cases (pure Kotlin), business rules, expose Flow<T>.
- Data: repositories/gateways implementing interfaces; IO details (network/DB/cache).
- Prefer Kotlin Flow across boundaries (between layers).

--- 

## Scope
- SavedStateHandle: for process death restoration and argument access.
- Pick scope intentionally:
## Fragment Scope: viewModels()
- cached until Fragment is finished (not just view destroyed).
- access args via SavedStateHandle.
- can only access ViewModel from that Fragment.
## Activity/Shared(between fragments) Scope: activityViewModels()
- cached until Activity is finished.
- access args via SavedStateHandle in Activity.
- shared between all fragments in that Activity.
- good for shared state (e.g., user session, cart).
## Application Scope: AndroidViewModel (with Application context)
- cached until app process is killed.
- avoid unless you need Application context.
- good for global state (e.g., theme, user session).
## NavGraph Scope: navGraphViewModels(graphId)
- cached until nav graph is popped off back stack.
- good for sharing state between fragments in a specific navigation flow.
- access args via SavedStateHandle in any fragment in the graph.
- useful for multi-step flows (e.g., onboarding, checkout).

--- 

## LiveData vs SharedFlow vs StateFlow
### Use Cases (Choose Approach Based on Needs)
- LiveData (legacy, good starting point)
    - MVVM presentation layer primitive (UI <-> ViewModel).
    - Simple apps; heavy DataBinding/Java interop.
    - Fine for state management in the UI layer only; avoid in domain/data layers.
    - Use an Event wrapper for one‑offs (e.g., navigation, toast).
- SharedFlow
    - One‑off UI events in MVVM/MVI (navigation, toast, snackbar).
    - In Clean, keep SharedFlow in the presentation layer; domain should expose Kotlin Flow, not LiveData.
    - Prefer MutableSharedFlow with replay=0 and extraBufferCapacity=1
        - replay=0 avoids re‑emitting old events to new collectors.
        - extraBufferCapacity=1 allows emitting without suspending if no collectors are active.
    - Collect with repeatOnLifecycle.
- StateFlow
    - UI state holder for MVVM/MVI screens (single source of truth for the view).
    - In Clean, expose Flow from domain and turn it into StateFlow in the ViewModel.
    - Collect with repeatOnLifecycle(STARTED).

### LiveData
- Lifecycle-awareness:
    - is Android specific -> not Kotlin coroutines
    - observers auto‑start/stop with LifecycleOwner.
      - it is lifecycle-aware because it is android specific
- Delivery semantics:
    - always delivers the latest value to active observers; caches one value.
- Threading :
    - setValue on Main; postValue from background (coalesces rapid posts).
        - meaning if you call postValue multiple times quickly, only the last one is delivered.
- API surface and operators:
    - minimal operators; great with DataBinding/Java.
- Pros:
    - Lifecycle‑aware by default (no manual cancelling).
    - Simple, minimal boilerplate; great with DataBinding; Java friendly.
    - Survives config changes via ViewModel.
- Cons:
    - Poor fit for one‑off events (needs Event/SingleLiveEvent patterns).
    - Limited operators; harder for complex streams.
    - postValue coalesces updates (intermediate values can be lost).
- Syntax:
    - LiveData<T> and MutableLiveData<T>.
    - observe(viewLifecycleOwner) { value -> ... }.
    - setValue(value) on Main; postValue(value) from background.
    - No built‑in operators; use Transformations.map/switchMap for simple cases.
    - No direct coroutine support; use liveData builder for simple cases.
        - builder lets you run a suspend function and emit the result.
- Setup:
```kotlin
// Activity/Fragment
class ExampleFragment : Fragment(R.layout.fragment_example) {
    // initialize ViewModel scoped to this Fragment
    private val vm: ExampleViewModel by viewModels()

  // Only difference for Activity:
  // all VM initialization and observation code goes in onCreate()
  
  // TODO: is there anything viewModels need to do on onCreateView?
  
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // viewLifecycleOwner is provided by Fragment; it represents the lifecycle of the Fragment's view
        // observe(this) would tie to the Fragment's lifecycle, which can lead to leaks if the view is destroyed
        // this is how we pass data from ViewModel to Fragment
        // whenever data changes (in ViewModel), render() is called, passing the new data to update the UI
        vm.items.observe(viewLifecycleOwner) { items ->
            render(items)
        }
        // load initial data (e.g., initial state or fetch from repository)
        // good for one-time setup calls like loading data when the view is created
        vm.load()
    }
    // render function to update the UI with the list of items
    // this is where you would update the RecyclerView adapter or other UI elements
    // TODO: is this essentially like JSX? rendering the UI based on state here?
    private fun render(items: List<Item>) { /* update UI */ }
}

// ViewModel
// extends ViewModel to hold UI data and state
class ExampleViewModel : ViewModel() {
    // private mutable LiveData to hold the list of items
    private val _items = MutableLiveData<List<Item>>()
    // public immutable LiveData for the UI to observe
    val items: LiveData<List<Item>> = _items

    // define how the private MutableLiveData updates the observable LiveData
    // here is just an example of loading data asynchronously from a repository
    // viewModelScope is a CoroutineScope tied to the ViewModel's lifecycle
    fun load() = viewModelScope.launch {
        val data = repository.getItems()
        _items.value = data
    }
}

// Other files as needed
// Example data class and repository interface
data class Item(val id: Long, val name: String)
// notice suspend function for coroutine support
interface Repository { suspend fun getItems(): List<Item> }
```


### SharedFlow
- Lifecycle-awareness:
    - is not Android -> only Kotlin coroutines
    - not lifecycle‑aware
    - collect with viewLifecycleOwner.repeatOnLifecycle(...) to avoid leaks.
- Delivery semantics:
    - default replay=0 (no sticky), can configure replay>0 and buffer; may drop or suspend depending on buffer policy.
    - if no collectors are active, emissions may be dropped or suspended based on buffer settings.
- Threading :
    - emit from any coroutine
    - full coroutines/Flow operators.
- API surface and operators:
    - full Flow operators (map, debounce, combine, etc.)
    - better for Kotlin/coroutines pipelines.
- Pros:
    - Great for events; configurable replay/buffer; coroutine operators.
    - Works in non‑Android layers; test‑friendly.
    - Multiple collectors supported.
- Cons:
    - Not lifecycle‑aware; must collect with repeatOnLifecycle.
    - replay=0 can miss events if no active collectors; needs careful buffering.
    - More plumbing compared to LiveData for simple UI.
- Syntax:
    - SharedFlow<T> and MutableSharedFlow<T>(replay=0, extraBufferCapacity=1).
    - collect { value -> ... } in repeatOnLifecycle.
    - emit(value) from any coroutine.
    - Full Flow operators (map, filter, debounce, etc.) available.
    - Use asSharedFlow() to expose read‑only SharedFlow.
- Setup:
```kotlin
// Activity/Fragment
class ExampleFragment : Fragment(R.layout.fragment_example) {
    private val vm: ExampleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.events.collect { event ->
                    when (event) {
                        is UiEvent.NavigateToDetail ->
                            findNavController().navigate(
                                ExampleFragmentDirections.actionExampleToDetail(event.id)
                            )
                        UiEvent.ShowToast ->
                            Toast.makeText(requireContext(), "Hello!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        view.findViewById<Button>(R.id.cta).setOnClickListener {
            vm.onCtaClicked()
        }
    }
}

// ViewModel
sealed class UiEvent {
    data class NavigateToDetail(val id: Long) : UiEvent()
    object ShowToast : UiEvent()
}

class ExampleViewModel : ViewModel() {
    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun onCtaClicked() {
        _events.tryEmit(UiEvent.ShowToast)
    }

    fun openDetail(id: Long) = viewModelScope.launch {
        _events.emit(UiEvent.NavigateToDetail(id))
    }
}

// Other files as needed
// None required beyond navigation args if using Safe Args
```

### StateFlow
- Lifecycle-awareness:
    - not lifecycle‑aware by itself
    - collect with viewLifecycleOwner.repeatOnLifecycle(...) in Fragments/Activities.
- Delivery semantics:
    - hot state holder; always has a current value (requires an initial value).
    - conflated updates: if values change quickly, collectors see the latest value; late subscribers immediately receive the current value.
- Threading:
    - update from any coroutine
    - designed for coroutines/Flow pipelines.
- API surface and operators:
    - StateFlow<T> and MutableStateFlow<T> with value and update operations.
    - Works with all Flow operators; easy to combine with other Flows.
- Pros:
    - Ideal for UI state in MVVM/MVI; single source of truth for the screen.
    - Always delivers the latest state to new collectors; simple rendering.
    - Kotlin/Flow‑native; usable in non‑Android layers (domain).
- Cons:
    - Requires an initial value; may need a placeholder state (e.g., Loading/Empty).
    - Conflation means intermediate values can be skipped (usually desirable for UI).
    - Not lifecycle‑aware; must be collected with lifecycle helpers in UI.
        - helpers include: repeatOnLifecycle (preferred) or flowWithLifecycle. Avoid launchWhenStarted/Resumed.
- Syntax:
    - ViewModel: use MutableStateFlow(initialState) privately; expose StateFlow via .asStateFlow().
    - Emit: state.value = newState or state.update { it.copy(...) }.
    - Convert from Flow: someFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState).
    - Collect: viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { state.collect { render(it) } } }
- Setup:
```kotlin
// Activity/Fragment
class ExampleFragment : Fragment(R.layout.fragment_example) {
    private val vm: ExampleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state -> render(state) }
            }
        }
        vm.load()
    }

    private fun render(state: UiState) { /* update UI */ }
}

// ViewModel
data class UiState(
    val loading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

class ExampleViewModel(
    private val repository: Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, error = null) }
        runCatching { repository.items() }
            .onSuccess { list -> _uiState.update { it.copy(loading = false, items = list) } }
            .onFailure { e -> _uiState.update { it.copy(loading = false, error = e.message) } }
    }

    // Example converting a cold Flow from domain to StateFlow
    val itemsFromDomain: StateFlow<List<Item>> = repository.itemsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

// Repository interface for data access 
interface Repository {
    suspend fun items(): List<Item>
    fun itemsFlow(): Flow<List<Item>>
}
```

---

## Additional Notes 
### DO NOT hold View/Context/NavController in a ViewModel
- Reasons:
  - Lifecycle leaks: 
    - ViewModels outlive Fragment views
      - holding view or context tied to a view/activity can leak memory.
  - Testability: 
    - UI types make unit testing harder
      - pure ViewModels are platform‑free.
  - Separation of concerns: 
    - ViewModel: models state and business logic
    - View: performs rendering and navigation.

- Safe alternatives:
  - Navigation: 
    - expose a SharedFlow<UiEvent> like NavigateToDetail(id)
    - the Fragment collects and calls findNavController().
  - Toast/Snackbar: 
    - expose UiEvent.ShowToast(UiText) and let the Fragment render it.
  - Resources/strings: 
    - avoid Context in ViewModel; pass IDs or use a UiText abstraction:
    - UiText = sealed class { StringRes(@StringRes id, vararg args) | Dynamic(String) }.
    - The Fragment maps UiText to a String via getString(id, args) or uses the dynamic string directly.
  - Application context: 
    - if you truly need a context not tied to UI (e.g., DataStore)
      - inject @ApplicationContext or use AndroidViewModel(Application). 
        - Never use AndroidViewModel for UI‑related tasks.

### Avoid complex logic in the ViewModel
- keep it focused on UI-related data and state.

### Use dependency injection (e.g., Hilt) 
- to provide dependencies to your ViewModel.
- you can annotate your ViewModel with @HiltViewModel and use @Inject constructor to get dependencies.
- this helps manage the lifecycle of dependencies and makes testing easier.

- When VM needs constructor args, provide a ViewModelProvider
    - use a ViewModelFactory to create ViewModels with parameters.
    - with Hilt, you can use @AssistedInject to handle constructor parameters.


### Use sealed classes or enums to represent UI states and events.
- this helps make your code more readable and maintainable by clearly defining the different states and events your UI can be in. 

### Use data classes to represent complex data structures.
  - data classes automatically provide useful methods like equals(), hashCode(), and toString(), making them ideal for representing data.

### Use coroutines for asynchronous operations.
- coroutines provide a simple and efficient way to handle background tasks without blocking the main thread.
- use viewModelScope to launch coroutines that are tied to the ViewModel's lifecycle


## Additional 
- Add Authorization / Authentication ViewModel
- Add Settings ViewModel