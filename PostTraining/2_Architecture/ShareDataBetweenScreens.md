# Resources
- https://www.youtube.com/watch?v=h61Wqy3qcKg

> Scope: Compose-first patterns (not XML-specific), focused on the most common + maintainable approaches.

# Sharing data between screens (Compose)
Most approaches overlap conceptually: you’re selecting **a state owner + a scope**.
- **Nav args**: data is part of the *navigation state* (destination identity).
- **Shared ViewModel**: data is part of a *back stack scope* (graph / destination owner).
- **Stateful dependencies (DI/singletons)**: data lives in an injected state holder (app/user/session scope).
- **CompositionLocal**: dependency injection via the Compose tree (UI scope).
- **Persistent storage**: data lives beyond the process (disk), then is observed.

A good rule: **the longer you need the data to live, the “lower” (more right) it should live** (UI → VM → repository → storage).

---

## Navigation args
Use when the data is:
- small,
- serializable,
- part of *what screen you’re on* (IDs, filters, deep link params),
- effectively read-only for that screen instance.

### Advantages
- Easy to implement.
- Works great with deep links.
- **Survives process death** (within limits): Navigation stores destination state in the underlying Android state restoration mechanism.
  - Practically: the back stack + args are saved into a `Bundle` via Saved State Registry and restored after process recreation.

### Limitations / gotchas
- **Where is it saved?**
  - Ultimately it’s stored in Android’s saved instance state (`Bundle`) and may also be backed by on-disk state depending on OEM/OS version, but you should treat it as **bounded, best-effort state restoration**, not durable storage.
    - **Bounded**: the system enforces size limits. If you store too much, you risk crashes (TransactionTooLargeException) or the state simply not being restored.
    - **Best-effort**: restoration is not a contractual data store. Under memory pressure, OEM quirks, or edge cases, the OS may not restore every bit of state exactly as you expect.
    - **Not durable storage**: it’s not designed for long-term persistence (reboots, long time gaps, user clearing app data). Use Room/DataStore for durable persistence.
- **Size limits**
  - There’s no official “nav args limit”, but you’re constrained by Binder/Bundle transaction sizes (a few MB at most, but practically much less to avoid OOMs).
  - Rule of thumb: keep args tiny (IDs, short strings). Avoid large objects/arrays/JSON blobs.
  - If you need more than a few KB, pass an **ID** and fetch from a repository/cache (ViewModel, Local Cache (Room), or DI state holder).
- **“Stateless / can’t be observed” — what does that mean?**
  - Nav args are **inputs** that define a destination instance.
    - Snapshot vs pointer (mental model):
      - **Snapshot**: nav args are values captured at navigation time. They don’t update automatically if the underlying data changes.
      - **Pointer**: pass an **identifier** (e.g., `userId=123`) and treat it like a pointer by using it to read live data from a repository/DB.
      - In practice: avoid passing a whole `User` object. Pass `userId` and observe `User` from the data source.
  - They don’t form an observable stream and don’t update reactively. If the data changes, you typically navigate again with new args.
  - In other words: args are *configuration*, not *state that evolves over time*.
- **What if `screenB/` is used but the route expects `{message}`?**
  - If your route is `"screenB/{message}"`, navigation needs a value for `{message}`. `screenB/` won’t match, and you’ll get an `IllegalArgumentException` at runtime.
  - If you want an optional arg, define a query param with a default (`"screenB?message={message}"`) and set `defaultValue`. See the example below.
- **What does `navArgument("message")` do?**
  - It declares metadata (name/type/default/nullability) so the Navigation runtime can parse/validate args and so you can retrieve them safely.
  - The *name* must match `{message}` in the route.

### Quick URI pattern
- Required arg: `route = "screenB/{message}"`
- Optional arg: `route = "screenB?message={message}"` + `defaultValue` in `navArgument`
- Multiple optional args: `route = "screenC?message={message}&userId={userId}"` + corresponding `navArgument`s
  - Yes—query params work like a normal query string: keep adding `&key={key}`.
  - Alternative when arguments are required: use path segments (`"details/{userId}/{tab}"`).
- Other useful patterns:
  - **Typed routes** (Navigation 2.7+): prefer typed route objects when available to reduce stringly-typed routing.
  - **Encode values**: if you’re passing arbitrary strings, URL-encode them (spaces, slashes, `?`, `&`).
  - **Results back to previous screen**: use `SavedStateHandle` on the previous back stack entry (common “picker returns selection” pattern).
  - **Route building helpers**: centralize route creation (e.g., `Routes.ScreenC.create(message, userId)`) to avoid stringly-typed call sites.
  - **Don’t pass secrets**: never pass access tokens / PII in routes (they can show up in logs / analytics).
  - **Prefer IDs over blobs**: use args to select data, then load from repo/DB.

### Example (safe + optional argument)
```kotlin
@Composable
fun NavArgsExample() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "screenA"
    ) {
        composable("screenA") {
            ScreenA(
                onNavigateToB = { msg ->
                    navController.navigate("screenB?message=$msg")
                },
                onNavigateToC = { msg, userId ->
                    navController.navigate("screenC?message=$msg&userId=$userId")
                }
            )
        }

        composable(
            route = "screenB?message={message}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val message = entry.arguments?.getString("message")
            ScreenB(message = message)
        }

        composable(
            // Multiple query params: `?a={a}&b={b}`.
            route = "screenC?message={message}&userId={userId}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("userId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { entry ->
            val message = entry.arguments?.getString("message")
            val userId = entry.arguments?.getInt("userId") ?: 0
            ScreenC(message = message, userId = userId)
        }
    }
}

@Composable
fun ScreenA(
    onNavigateToB: (String) -> Unit,
    onNavigateToC: (String?, Int) -> Unit
) {
    Column {
        Button(onClick = { onNavigateToB("HelloFromA") }) {
            Text("Go to Screen B")
        }
        Button(onClick = { onNavigateToC("FromA", 123) }) {
            Text("Go to Screen C")
        }
    }
}

@Composable
fun ScreenB(message: String?) {
    Text(text = message ?: "No message received")
}

@Composable
fun ScreenC(message: String?, userId: Int) {
    Text(text = "message=$message userId=$userId")
}
```

---

## Shared ViewModel (NavGraph-scoped)
Use when:
- multiple screens need to read/write the same state (wizard/onboarding, cart edits, multi-step form),
- you want changes to be reactive across screens,
- you don’t want to thread args through many destinations (e.g. 5+ screens in a flow, being passed 1 by 1).

### Answers to your TODOs
- **“How do I get the VM in the NavHost? pass it, `viewModel()`, or Hilt?”**
  - With Hilt + Navigation Compose, the usual is `val vm: MyVm = hiltViewModel()`.
  - For a shared VM across a nested graph, you scope it to the **parent graph back stack entry** and call `hiltViewModel(parentEntry)`.
  - Passing VMs as parameters works too, but tends to make APIs noisier and complicates previews/tests unless you abstract.
- **What is `entry` in `composable { entry -> ... }`?**
  - That’s a `NavBackStackEntry` representing a *single occurrence* of a destination on the navigation back stack.
  - Think of it as: **destination route + its arguments + its saved state container + its lifecycle owner**.
  - “Destination instance” means: the *combination* of (route + args) that exists as a node on the back stack.
    - It’s not a Kotlin object instance of your composable function.
    - It’s closer to “an instance of navigation state”: unique args, its own SavedState, its own Lifecycle.

### Example (shared VM scoped to the onboarding graph)
```kotlin
@Composable
fun SharedViewModelExample() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        navigation(
            startDestination = "personal_details",
            route = "onboarding"
        ) {
            composable("personal_details") { entry ->
                // Scope to the parent graph, not the leaf destination.
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("onboarding")
                }
                val vm: OnboardingViewModel = hiltViewModel(parentEntry)

                val state by vm.sharedState.collectAsStateWithLifecycle()

                PersonalDetailsScreen(
                    sharedState = state,
                    onNavigate = { navController.navigate("terms_and_conditions") }
                )
            }

            composable("terms_and_conditions") { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry("onboarding")
                }
                val vm: OnboardingViewModel = hiltViewModel(parentEntry)

                val state by vm.sharedState.collectAsStateWithLifecycle()

                TermsAndConditionsScreen(
                    sharedState = state,
                    onAccept = {
                        vm.acceptTerms()
                        // finish flow
                    }
                )
            }
        }
    }
}
```

---

## Sharing stateful dependencies (DI-scoped state holders)
Use when the state is *not specific to a single nav flow*:
- user session/auth token state
- cart state (depending on architecture)
- feature flags
- “current store”, “selected country”, A/B bucketing

Pattern:
- Create a `@Singleton` (or `@ActivityRetainedScoped`) state holder that exposes `StateFlow`.
```kotlin
// UserSession.kt
@Singleton
class UserSession @Inject constructor() {
    // Why @Inject on an empty constructor?
    // - It tells Hilt/Dagger “you can construct this type”.
    // - With no params, it’s optional if you provide it in a @Module.
    // - It becomes useful the moment you add dependencies (Clock, Repo, Dispatcher, etc.).
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun login(username: String, password: String) {
        // perform login, then update _user
    }

    fun logout() {
        // perform logout, then update _user
    }
}
```

### Singleton vs ActivityRetainedScoped (Hilt)
- `@Singleton`
  - One instance for the whole app process.
  - Destroyed when the process dies.
  - Great for app/user/session-scoped state *in-memory*.
- `@ActivityRetainedScoped`
  - One instance per Activity **across configuration changes** (rotation).
  - Destroyed when the Activity is finished.
  - Useful when the state is tied to a host Activity (e.g., an auth flow), but shouldn’t be global.

Important: **neither scope survives process death**. If you need that, persist to Room/DataStore.

```kotlin
// Example: state holder tied to a single Activity instance (retained across rotation)
@ActivityRetainedScoped
class CheckoutSession @Inject constructor() {
    private val _draft = MutableStateFlow<CheckoutDraft?>(null)
    val draft: StateFlow<CheckoutDraft?> = _draft.asStateFlow()

    fun updateDraft(draft: CheckoutDraft) {
        _draft.value = draft
    }

    fun clear() {
        _draft.value = null
    }
}
```

- ViewModels read/transform it.
```kotlin
// MyViewModel.kt
@HiltViewModel
class MyViewModel @Inject constructor(
    userSession: UserSession
) : ViewModel() {

    val userState: StateFlow<User?> = userSession.user
        .map { user -> /* transform if needed */ user }
        // `stateIn` turns a Flow into a StateFlow with a replay(1) cache.
        // WhileSubscribed keeps the upstream running only while there are collectors.
        // The 5s timeout avoids rapid stop/start if the composable briefly leaves the screen.
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
```


Tradeoffs:
- ✅ Great for app- or user-scoped state.
- ✅ Easy to test if you inject interfaces.
- ⚠️ Beware of turning this into a global “god store”. Keep boundaries.

---

## Using CompositionLocals
Use when:
- you need a dependency available to part of the Compose tree,
- it’s primarily UI-level context/state (formatters, theming, simple feature toggles),
- you want to avoid parameter threading.

Guidance:
- Prefer `CompositionLocal` for *dependencies*, not large mutable app state.
- If it’s business state, prefer ViewModel/DI state holders.

```kotlin
// Theme.kt
// CompositionLocal basics:
// - A CompositionLocal is a typed key used to implicitly pass data down the Compose tree.
// - You *provide* a value with CompositionLocalProvider.
// - You *read* a value with `LocalX.current`.
//
// staticCompositionLocalOf vs compositionLocalOf
// - staticCompositionLocalOf: does NOT track reads for invalidation. Changing the provided value
//   won't automatically trigger recomposition of consumers. Use for values that rarely change
//   (or should behave like constants), e.g., loggers, formatters, DI-like dependencies.
// - compositionLocalOf: tracks reads. Updating the provided value will recompose consumers.
//   Use for values that can change at runtime and should update UI, e.g., theme colors if
//   you truly swap themes dynamically.
//
// Most app theme values are stable in practice; many examples use staticCompositionLocalOf.
val LocalThemeColors = staticCompositionLocalOf { defaultColors }

@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    // Compute the theme colors for this composition (light/dark/dynamic/etc.)
    val colors = /* calculate theme colors */

    // Make `colors` available to everything under this subtree.
    CompositionLocalProvider(LocalThemeColors provides colors) {
        content()
    }
}

@Composable
fun ThemedChip(text: String) {
    val colors = LocalThemeColors.current
    Text(text = text, color = colors.onSurface)
}
```

Common APIs seniors should know about:
- `CompositionLocalProvider(LocalX provides value) { ... }`
- `LocalX.current`
- `staticCompositionLocalOf { default }` vs `compositionLocalOf { default }`
- `provides` (sets) vs `providesDefault` (rarely needed; sets only if not already provided)

Guidance:
- Use CompositionLocals for *ambient dependencies* (formatters, analytics, theme primitives).
- Avoid using them as a cross-feature state store. If it’s business state, prefer ViewModel/repository.
