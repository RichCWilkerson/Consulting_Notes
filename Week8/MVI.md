# MVI vs MVVM

[MVI Medium](https://medium.com/@mohammedkhudair57/mvi-architecture-pattern-in-android-0046bf9b8a2e)


## Same
- these are presentational architectural patterns
- both patterns separate UI from business logic
- both use model (data and domain logic)
- both use view (what is displayed to the user)
  - xml or composable

## Differences
- MVVM uses ViewModel to handle business logic and state management
- MVI uses Intent to represent user actions and a single state object to represent the entire UI state

- MVVM allows for two-way data binding between View and ViewModel
- MVI follows unidirectional data flow, where state changes are propagated from the Model to the View

- MVVM can lead to more complex state management due to multiple sources of truth
- MVI simplifies state management by having a single source of truth for the UI state

- MVVM can be more flexible in terms of how data is presented and updated
- MVI provides a more predictable and testable architecture due to its unidirectional data flow

- MVI should not be used with xml -> every time the state changes, the whole xml would need to be recomposed and this is not efficient
  - compose only updates the parts of the UI that changed
- MVVM can be used with both xml and compose

- NOTE: from below, MVI uses sealed classes for actions and state objects, while MVVM uses functions and individual state variables
  - this is the biggest difference in implementation between the two patterns

- MVI is immutable -> every time the state changes, a new state object is created
- MVVM is mutable -> state variables can be updated directly



---

# MVVM Example
```kotlin
// Example of MVVM Screen

// this is a pattern to where the Root holds the state and passes it down to the Screen
@Composable
fun MvvmScreenRoot(
    navController: NavController,
    viewModel: MyViewModel = hiltViewModel()
) {
    MvvmScreen(
        postDetails = viewModel.postDetails,
        isLoading = viewModel.isLoading,
        isPostLiked = viewModel.isPostLiked,
        onToggleLike = { viewModel.toggleLike() },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun MvvmScreen(
    postDetails: PostDetails,
    isLoading: Boolean,
    isPostLiked: Boolean,
    onToggleLike: () -> Unit,
    onBackClick: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display post details
    }
}

// ViewModel
class MyViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    var postDetails by mutableStateOf<Post?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isPostLiked by mutableStateOf(false)
        private set

    fun toggleLike() {
        postDetails?.let {
            postDetails = it.copy(isLiked = !it.isLiked)
        }
    }
    
    init {
        savedStateHandle.get<String>("postId")?.let { postId ->
            loadPost(postId)
        }
    }
    
    private fun loadPost(postId: String) {
        viewModelScope.launch {
            // Simulate loading
            delay(1000)
            postDetails = Post(id = postId, content = "Sample Content", isLiked = false)
            isLoading = false
        }
    }
}
```

---

# MVI Example
```kotlin
@Composable
fun MviScreenRoot(
    navController: NavController,
    viewModel: MyMviViewModel = hiltViewModel()
) {
    MviScreen(
        // all possible states are contained in a single state object -> see below for MyMviState
        state = viewModel.state,
        // all possible user actions are represented as Intents -> see below for MviAction
        onAction = { action -> 
            when (action) {
                // ViewModel should not know about navigation, so handle it here
                is MviAction.BackClick -> navController.popBackStack() // navigateUp() if using nested nav graphs
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun MviScreen(
    state: MyMviState,
    onAction: (MviAction) -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display post details
    }
}


// MviState -> replace the whole state of the screen if anything changes
data class MyMviState(
    val postDetails: Post? = null,
    val isLoading: Boolean = false,
    val isPostLiked: Boolean = false,
    val email: String = "",
    val isValidEmail: Boolean = false
)

// MviAction -> all possible user actions
sealed class MviAction {
    data object ToggleLike : MviAction()
    data object BackClick : MviAction()
}


// ViewModel
class MyMviViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    var state by mutableStateOf(MyMviState())
        private set
    
    init {
        // snapshotFlow to observe changes in email field when using a single state object in MVI
            // snapshotFlow creates a cold Flow, emits values when the value inside changes
            // use from a coroutine to react to Compose state changes
            // commonly used with map, onEach, filter, distinctUntilChanged, debounce, collectLatest, etc.
        // non-blocking way to react to state changes
        // need to use map and onEach because it is not just one change, but a flow of changes
        // NOTE: only use for streamed changes, not one-time events (e.g., submit button click)
        snapshotFlow { state.email }
            .map { 
                // Validator.isValidEmail(it) // replace with your email validation logic
            }
            .onEach {
                state = state.copy(isValidEmail = it) // side-effect
            }
            .launchIn(viewModelScope) // launchIn to launch the flow in the ViewModel's scope
        
        savedStateHandle.get<String>("postId")?.let { postId ->
            loadPost(postId)
        }
    }
    
    // can also be called handleIntent
    private fun onAction(action: MviAction) {
        when (action) {
            is MviAction.ToggleLike -> toggleLike()
            is MviAction.BackClick -> backClick()
          // else is not needed since all actions are handled (exhaustive when)
            else -> Unit
        }
    }
    
    private fun toggleLike() {
        state.postDetails?.let {
            state = state.copy(
                postDetails = it.copy(isLiked = !it.isLiked))
        }
    }
    
    private fun loadPost(postId: String) {
        viewModelScope.launch {
            // Simulate loading
            delay(1000)
            state = state.copy(
                postDetails = Post(id = postId, content = "Sample Content", isLiked = false),
                isLoading = false
            )
        }
    }
    
}

```

# Additional Notes
- Push business logic (search, persistence, validation) into use‑cases/repositories, not the ViewModel.

- If a feature follows a set of steps (e.g., making an appointment, purchasing an item), consider using a single:
  - ViewModel - handle all Actions with a `when`
  - Action sealed class - represent all possible user actions
  - State data class - represent all possible states of the appointment/purchase flow

```kotlin
// Example of Appointment MVI State
data class AppointmentState(
    private val patient: Patient,
    private val doctor: Doctor,
    private val date: LocalDate?,
    private val time: LocalTime?,
    private val isLoading: Boolean,
    private val errorMessage: String?
)
// Example of Appointment MVI Actions
sealed class AppointmentAction {
    data class SelectDoctor(val doctor: Doctor) : AppointmentAction()
    data class SelectDate(val date: LocalDate) : AppointmentAction()
    data class SelectTime(val time: LocalTime) : AppointmentAction()
    data object ConfirmAppointment : AppointmentAction()
    data object CancelAppointment : AppointmentAction()
    data object BackClick : AppointmentAction()
    data object FilterDoctors : AppointmentAction()
    data object MapViewClick : AppointmentAction()
}
```

- NOTE: do not use "on" prefix for Action names (e.g., use SelectDate instead of OnSelectDate) because Actions represent what happened, not the handler for what happened.