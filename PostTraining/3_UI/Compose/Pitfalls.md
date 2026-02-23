# Resources:


# Compose Pitfalls

Cross-cutting issues and common mistakes that apply to many composables (layouts, widgets, scaffolds, etc.).

This file focuses on *what goes wrong* and how to avoid it.

---

## Over-recomposition
- **Symptom**: UI feels janky or slow; profiler shows frequent recomposition of large parts of the tree.
- **Common causes**:
  - Passing changing lambdas or objects without `remember` to child composables.
  - Keeping too much state at the top level instead of closer to where it’s used.
  - Using `mutableStateOf` for large or frequently changing objects instead of more granular state.
- **Mitigations**:
  - Hoist state thoughtfully and avoid unnecessary observers.
  - Use `remember` and `derivedStateOf` to cache derived values.
  - Split large composables into smaller ones so only the necessary parts recompose.

---

## Misplaced state (state hoisting mistakes)
- **Symptom**: Hard-to-follow data flow, bugs when multiple components try to own the same state.
- **Common causes**:
  - Keeping mutable state inside leaf composables that need to be controlled from above.
  - Duplicating the same state in multiple places (view model + UI local).
- **Mitigations**:
  - Follow state hoisting patterns: prefer single source of truth and pass state + events down.
  - Make leaf composables stateless where possible, with state managed by parents.

---

## Layout & scroll issues
- **Symptom**: Content clipped, overlapping app bars, awkward nested scrolling.
- **Common causes**:
  - Forgetting to apply insets-aware padding when using app bars and navigation bars.
  - Nesting multiple scrollable containers without understanding how gestures are handled.
  - Using non-lazy layouts (`Column` with `verticalScroll`) for large lists.
- **Mitigations**:
  - Use `LazyColumn`/`LazyRow` for long lists.
  - Be deliberate with scroll containers and use `nestedScroll` where appropriate.
  - Respect system bars using insets APIs and modifiers.

---

## Modifier order confusion
- **Symptom**: Unexpected sizes, padding, or click areas; visuals not matching designs.
- **Common causes**:
  - Not realizing that modifier order matters (e.g., `background().padding()` vs `padding().background()`).
  - Mixing layout and drawing modifiers in unclear sequences.
- **Mitigations**:
  - Remember that modifiers are applied in order; experiment and document patterns that work well.
  - Keep related modifiers grouped for readability.

---

## List performance issues
- **Symptom**: Janky scrolling, dropped frames, high CPU usage.
- **Common causes**:
  - Doing heavy work per item without memoization.
  - Missing stable keys for items that change order or content.
  - Overly complex item layouts causing expensive measure/layout.
- **Mitigations**:
  - Use `items(..., key = ...)` with stable identifiers.
  - Avoid heavy work in item lambdas; use `remember`, move logic to view model when possible.
  - Profile and simplify list item layouts when necessary.


---

## SideEffect mismanagement

```kotlin
// a separate composable is a button that increments count
// the ConditionalCallbackBad has a threshold of counts and calls the callback when the threshold is reached
@Composable
fun ConditionalCallback(
    modifier: Modifier = Modifier,
    onThresholdReached: (Int) -> Unit
) {
    var counter by remember { mutableIntStateOf(0) }
    
    // This is a side effect that will be triggered on every recomposition when the counter is updated, which can lead to multiple calls to the callback and potential performance issues. 
    // The callback should be called in a side effect that only runs when the counter changes and reaches the threshold, such as LaunchedEffect or SideEffect.
    // currently it will send multiple calls that will then cause processData to be called multiple times, even though it should only log the message once when the threshold is reached.
    if (counter >= 10) {
        onThresholdReached(counter)
    }
    // fix, use LaunchedEffect to only call the callback when the counter changes and reaches the threshold, and not on every recomposition.
    LaunchedEffect(counter >= 10) { 
        if (counter >= 10) {
            onThresholdReached(counter)
        }
    }
    
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Counter: $counter", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { counter++ }) {
            Text("Increment Counter")
        }
    }
}



@Composable
fun SideEffectDemo() {
    val scope = rememberCoroutineScope()
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        
        // 
        ConditionalCallback(
            onThresholdReached = {
                logMessages = logMessages + "Threshold callback fired with count $count"
                scope.launch {
                    processData()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // Display last 5 log messages
        logMessages.takeLast(5).forEach { msg -> 
            Text(msg, modifier = Modifier.padding(4.dp))
        }
    }
}

private suspend fun processData() {
    withContext(Dispatchers.Default) {
        var sum = 0L
        repeat(1_000_000) { sum += it }
    }
}
```

## Dynamic forms
- LazyColumn/Row have keys, that even when the content changes (filter, sort, etc.), the composables will not be recreated -> leads to better performance (less jank)
- Don't need a Lazy layout to use keys, can use a `key` composable to provide a key for any composable, and then when the key changes, the composable will be recreated. This is useful for dynamic forms where the fields can change based on user input or other factors, and you want to ensure that the correct composables are recreated when the fields change.

```kotlin
@Composable
fun DynamicForm() {
    var fields by remember {
        mutableStateOf(
            listOf(
                FormField(id = "email", label = "Email", value = ""),
                FormField(id = "password", label = "Password", value = ""),
                FormField(id = "phone", label = "Phone", value = "")
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = { fields = fields.reversed() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Reverse Fields")
        }
        
        fields.forEach { field -> 
            FormFieldItem(label = field.label)
        }
        
        // fix
        fields.forEach { field -> 
            // ensure key's are stable and unique to the field
            key(field.id) { // use key to ensure that the correct composable is recreated when the fields change
                FormFieldItem(label = field.label)
            }
        }
    }
}
```

## MVI state management 
```kotlin

data class UserScreenState(
    val username: String,
    val email: String,
    val profileImageUrl: String,
    val followerCount: Int,
    val isVerified: Boolean,
    val lastLoginTimestamp: Long,
    val notificationCount: Int,
    val accountBalance: Double
)

@Composable
fun UserScreenState() {
    // this is generally in a VM, but there are other issues we are looking at here.
    var state by remember { 
        mutableStateOf(
            UserScreenState(
                username = "John Doe",
                email = "JohnDoe@gmail.com",
                profileImageUrl = "https://example.com/profile.jpg",
                followerCount = 1000,
                isVerified = true,
                lastLoginTimestamp = System.currentTimeMillis(),
                notificationCount = 5,
                accountBalance = 123.45
            )
        ) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Bad: UserHead does not need the entire state, it only needs 2 fields -> only pass those -> if one piece of the state changes, the UserHeader will only change if one of the fields we passed it changes
        UserHeader(state = state)
        
        // Good: only pass the fields that are needed -> if one piece of the state changes, the NotificationBadge will only change if the notificationCount changes
        NotificationBadge(notificationCount = state.notificationCount)
        
        Button(
            onClick = {
                state = state.copy(notificationCount = state.notificationCount + 1)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Increment Notifications")
        }
        
        Button(
            onClick = {
                state = state.copy(followerCount = state.followerCount + 1)
            }
        ) {
            Text("Increment Followers")
        }
    }
}

// bad state management 
@Composable
fun UserHeader(state: UserScreenState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Image(
            painter = rememberImagePainter(state.profileImageUrl),
            contentDescription = "Profile Image",
            modifier = Modifier.size(64.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(state.username, style = MaterialTheme.typography.h6)
            Text(state.email, style = MaterialTheme.typography.body2)
        }
    }
}

// fix: instead pass only the fields needed
@Composable
fun NotificationBadge(notificationCount: Int) {
    if (notificationCount > 0) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.Red, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notificationCount.toString(),
                color = Color.White,
            )
        }
    }
}
```

## Not using withContext to switch off main for heavy work

```kotlin
class LoadUserViewModel : ViewModel() {

    private val repository = FileRepository()

    private val _userData = MutableStateFlow<String?>("")
    val userData = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadUserData(userId: String) {
        // this is a common mistake -> did not move the heavy work to Dispatchers.IO or Default
        viewModelScope.launch {
            _isLoading.value = true

            val data = repository.loadUserData(userId)

            _userData.value = data
            _isLoading.value = false
        }
    }
}

// Repository
class FileRepository {

    // don't add withContext here, add it individually to the functions that need it, so that you can control which dispatcher they run on and avoid unnecessary context switching.
    suspend fun loadUserData(userId: String): String {
        delay(100)
        
        val fileContent = simulateBlockingFileRead()
        
        return processUserData(fileContent, userId)
    }
    
    private suspend fun simulateBlockingFileRead(): String = withContext(Dispatchers.IO) {
        val data = StringBuilder()
        repeat(1000000) { index ->
            data.append("Line $index\n")
        }
        data.toString()
    }
    
    private suspend fun processUserData(fileContent: String, userId: String): String = withContext(Dispatchers.Default) {
        // Simulate processing the file content to find user data
        val result = ""
        repeat(10_000) {
            result = fileContent.take(100) + userId
        }
        result
    }
}
```