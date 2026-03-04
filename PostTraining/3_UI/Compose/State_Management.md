# Resources:
- [Ranking Compose State Management Approaches - Youtube](https://www.youtube.com/watch?v=Zwmcr6duzhY&t=308s)

## State Management Approaches
1. Remember - F
2. MVI - S+
3. MVVM - A
4. Sealed classes - 
5. MVI / MVVM hybrid


### Remember
- isn't saved on configuration changes, so not recommended for anything more than simple UI state that can be easily recreated
- not meant for meaningful state management, more for temporary state that doesn't need to survive process death or configuration changes

- rememberSavable can persist across configuration changes, but still not ideal for complex state management
  - data must be Parcelable or Serializable, which can be cumbersome
  - still not designed for complex state management or side effects
  - not ideal for handling asynchronous operations, error states, or complex UI logic

- this method forces business logic into the composable, which can lead to bloated and hard-to-maintain code

```kotlin
@Composable
fun RememberStateScreen() {
    // This state will be lost on configuration changes (e.g., screen rotation)
    var items by remember { mutableStateOf(listOf<String>(emptyList())) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
}
```


### MVI
- a single source of truth for state, with unidirectional data flow
- ViewModel exposes a StateFlow representing the UI state, and the UI collects this state to render itself
- user interactions are sent as events to the ViewModel, which processes them and updates the state accordingly
- promotes separation of concerns and makes it easier to manage complex UI logic and side effects

```kotlin
// State
data class UiState(
    val items: List<ListIem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
data class ListItem(
    val id: String, 
    val title: String
)
// Events
sealed class UiEvent {
    object LoadItems : UiEvent()
    data class ItemClicked(val itemId: String) : UiEvent()
}  


// ViewModel
class MviViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.LoadItems -> loadItems()
            is UiEvent.ItemClicked -> handleItemClick(event.itemId)
        }
    }
}

// UI
@Composable
fun MviStateScreen(viewModel: MviViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // Render UI based on uiState
}
```


### MVVM



### Sealed classes



### MVI / MVVM hybrid