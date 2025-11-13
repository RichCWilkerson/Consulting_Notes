# Multimodule Android: Structure, Generics with Retrofit, and MVI (Week 6)

This guide lays out a simple, pragmatic multimodule setup based on your idea:
- app (entry point)
- feature:firstlettercocktail
- feature:searchcocktail
- core:ui (sharedcomponents)
- core:domain (shareddomain)
- core:data (optional but recommended for Retrofit, repositories, DTOs, mappers)

It also shows where to use generics (Retrofit-safe calls + state mapping), how MVI fits, and minimal Gradle wiring. Keep it small, then iterate.

---

## TL;DR: Is your structure OK?
Yes—with one tweak. Splitting shared parts out of the app is a best practice. I'd group shared modules as "core":
- app
- feature:firstlettercocktail
- feature:searchcocktail
- core:ui (what you called sharedcomponents)
- core:domain (what you called shareddomain)
- core:data (new; for Retrofit, repositories, DTOs, mappers; depends on domain)

Why add core:data?
- Keeps networking, caching, DTOs, and repository implementations out of features and app.
- Enables true reusability across features.
- Provides a natural place for generic helpers (safeApiCall, ResponseState, mapData, etc.).

If you want the absolute minimum: you can omit core:data and put a tiny repository in each feature. But the moment two features share the same API, you’ll want a common data module.

---

## Dependency graph (one-way arrows only)
- app → feature:firstlettercocktail, feature:searchcocktail, core:ui, core:domain (and core:data if using DI from app)
- feature modules → core:domain, core:ui (and core:data if they call repositories directly)
- core:data → dependency: core:domain
  - Retrofit, DTOs, mappers -> domain models, repository implementations
- core:domain → no module dependencies (pure Kotlin)
  - domain models, use-case interfaces, generic ResponseState, repository contracts, sealed classes (response states)
- core:ui → dependency: core:domain (for domain models in UI components)
  - reusable Compose components (cards, loading, error)

TODO: what is the difference between repository implementation and contract? 
TODO: is repository contract a use case interface?

Runtime flow (MVI): UI/Feature → Domain (use case/repo) → Data (impls) → back to Domain → UI
Key rule: Nothing depends on app. Core modules are leaf-like and stable.

---

## Module responsibilities
- core:domain
  - Pure Kotlin: domain models, use-case interfaces, sealed classes (ResponseState)
  - No Android or Retrofit dependencies
- core:data
  - Retrofit service, DTOs, mappers, repository implementations
  - Generic network helpers (safeApiCall<T>), ResponseState mapping utilities
  - Depends on domain
- core:ui
  - Reusable Compose components (cards, loading, error)
  - No domain/data logic
- feature:firstlettercocktail and feature:searchcocktail
  - MVI presentation (Intents, State, ViewModel) + feature-specific use cases
  - Calls repositories (from core:data) and returns domain models
  - TODO: what is a feature project structure? data, di, domain, ui folders inside feature?
- app
  - Navigation wiring, DI wiring, and top-level theme
  - TODO: like a bottom navigation bar to each feature?
  - TODO: if i want to go from one feature to another, does that go in app or feature?

---

## Where to put generics (Retrofit and state)
- Return generic types from repository helpers and map them in a consistent way
- Keep network error handling once in a reusable function

### In core:domain
```kotlin
// Generic UI/data-state for use across app & features
sealed class ResponseState<out T> {
    object Loading : ResponseState<Nothing>()
    data class Success<out T>(val data: T) : ResponseState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ResponseState<Nothing>()
}

// Generic mapper to transform ResponseState<T> -> ResponseState<R>
inline fun <T, R> ResponseState<T>.mapData(transform: (T) -> R): ResponseState<R> = when (this) {
    is ResponseState.Success -> ResponseState.Success(transform(data))
    is ResponseState.Loading -> ResponseState.Loading
    is ResponseState.Error -> this
}
```

### In core:data
```kotlin
// Generic safe call wrapper for any Retrofit call
suspend inline fun <T> safeApiCall(crossinline call: suspend () -> retrofit2.Response<T>): ResponseState<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ResponseState.Success(body)
            else ResponseState.Error("Empty body")
        } else {
            ResponseState.Error("HTTP ${response.code()}: ${response.message()}")
        }
    } catch (t: Throwable) {
        ResponseState.Error(t.message ?: "Unknown error", t)
    }
}
```

Use safeApiCall<T> for any endpoint, then map DTOs to domain with mapData.

---

## Minimal data layer example (TheCocktailDB)

### DTOs and API (core:data)
```kotlin
// CocktailApi.kt
interface CocktailApi {
    // Search by name: /search.php?s=margarita
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): Response<CocktailResponseDto>

    // Search by first letter: /search.php?f=m
    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): Response<CocktailResponseDto>
}

// CocktailResponseDto.kt
data class CocktailResponseDto(
    val drinks: List<CocktailDto>?
)

// CocktailDto.kt
data class CocktailDto(
    val idDrink: String?,
    val strDrink: String?,
    val strDrinkThumb: String?
)
```

### Domain models (core:domain)
```kotlin
// Cocktail.kt (domain)
data class Cocktail(
    val id: String,
    val name: String,
    val thumbnailUrl: String?
)
```

### Mappers (core:data)
```kotlin
fun CocktailDto.toDomain(): Cocktail = Cocktail(
    id = idDrink.orEmpty(),
    name = strDrink.orEmpty(),
    thumbnailUrl = strDrinkThumb
)

fun CocktailResponseDto.toDomainList(): List<Cocktail> = drinks?.map { it.toDomain() } ?: emptyList()
```

### Repository contract (core:domain)
```kotlin
interface CocktailRepository {
    suspend fun searchByName(query: String): ResponseState<List<Cocktail>>
    suspend fun searchByFirstLetter(letter: String): ResponseState<List<Cocktail>>
}
```

### Repository implementation (core:data)
```kotlin
class CocktailRepositoryImpl(
    private val api: CocktailApi
) : CocktailRepository {
    override suspend fun searchByName(query: String): ResponseState<List<Cocktail>> =
        safeApiCall { api.searchByName(query) }.mapData { it.toDomainList() }

    override suspend fun searchByFirstLetter(letter: String): ResponseState<List<Cocktail>> =
        safeApiCall { api.searchByFirstLetter(letter) }.mapData { it.toDomainList() }
}
```

Generics used:
- safeApiCall<T> is generic over T and works for any Response<T>
- ResponseState<T> is generic and reusable across all features
- mapData transforms ResponseState<T> -> ResponseState<R> generically

---

## MVI in features

Think in 4 parts: Intent, State, Effect (optional), Reducer (in ViewModel).

### Contract (feature layer)
```kotlin
// Intent
sealed interface CocktailIntent {
    data class SearchByName(val query: String) : CocktailIntent
    data class SearchByFirstLetter(val letter: String) : CocktailIntent
    data object Retry : CocktailIntent
}

// State
data class CocktailState(
    val isLoading: Boolean = false,
    val items: List<Cocktail> = emptyList(),
    val error: String? = null
)

// Effect (one-off events like snackbars)
sealed interface CocktailEffect {
    data class ShowMessage(val message: String) : CocktailEffect
}
```

### ViewModel (feature layer)
```kotlin
class CocktailViewModel(
    private val repository: CocktailRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CocktailState())
    val state: StateFlow<CocktailState> = _state.asStateFlow()

    private val _effects = Channel<CocktailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun process(intent: CocktailIntent) {
        when (intent) {
            is CocktailIntent.SearchByName -> searchByName(intent.query)
            is CocktailIntent.SearchByFirstLetter -> searchByFirstLetter(intent.letter)
            CocktailIntent.Retry -> reload()
        }
    }

    private fun searchByName(query: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        when (val res = repository.searchByName(query)) {
            is ResponseState.Loading -> _state.update { it.copy(isLoading = true) }
            is ResponseState.Success -> _state.update { it.copy(isLoading = false, items = res.data) }
            is ResponseState.Error -> {
                _state.update { it.copy(isLoading = false, error = res.message) }
                _effects.send(CocktailEffect.ShowMessage(res.message))
            }
        }
    }

    private fun searchByFirstLetter(letter: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        when (val res = repository.searchByFirstLetter(letter)) {
            is ResponseState.Loading -> _state.update { it.copy(isLoading = true) }
            is ResponseState.Success -> _state.update { it.copy(isLoading = false, items = res.data) }
            is ResponseState.Error -> {
                _state.update { it.copy(isLoading = false, error = res.message) }
                _effects.send(CocktailEffect.ShowMessage(res.message))
            }
        }
    }

    private fun reload() {
        val current = _state.value
        if (current.items.isNotEmpty()) return // nothing to reload here; define your own rule
    }
}
```

### Compose screen (feature layer) using core:ui components
```kotlin
@Composable
fun CocktailScreen(
    state: CocktailState,
    onIntent: (CocktailIntent) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SearchBar(
            placeholder = "Search by name",
            onSearch = { onIntent(CocktailIntent.SearchByName(it)) }
        )
        Spacer(Modifier.height(12.dp))
        Row {
            FirstLetterPicker(onPick = { onIntent(CocktailIntent.SearchByFirstLetter(it)) })
        }
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorWithRetry(
                message = state.error,
                onRetry = { onIntent(CocktailIntent.Retry) }
            )
            else -> LazyColumn {
                items(state.items) { cocktail -> CocktailCard(cocktail) }
            }
        }
    }
}
```

### Reusable UI (core:ui)
```kotlin
@Composable fun LoadingIndicator() { /* CircularProgressIndicator() */ }
@Composable fun ErrorWithRetry(message: String, onRetry: () -> Unit) { /* retry button */ }
@Composable fun CocktailCard(item: Cocktail) { /* image + text */ }
@Composable fun SearchBar(placeholder: String, onSearch: (String) -> Unit) { /* text field */ }
@Composable fun FirstLetterPicker(onPick: (String) -> Unit) { /* chips A-Z */ }
```

---

## Minimal Gradle wiring

### settings.gradle.kts
```kts
include(":app")
include(":core:domain")
include(":core:ui")
include(":core:data") // optional but recommended
include(":feature:firstlettercocktail")
include(":feature:searchcocktail")
```

---

## DI (simple first)
- Start with manual DI: create Retrofit in app and pass dependencies to features
- Later, add Hilt to core:data (for providers) and app (for component wiring)

### Manual DI sketch (in app)
```kotlin
val okHttp = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().setLevel(BODY))
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://www.thecocktaildb.com/api/json/v1/1/")
    .client(okHttp)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val api = retrofit.create(CocktailApi::class.java)
val repository: CocktailRepository = CocktailRepositoryImpl(api)

// Provide repository to ViewModels via your favorite pattern (factory, Hilt later)
```

---

## Navigation
- app owns navigation (e.g., Compose Navigation)
- Each feature exposes a Composable entry with a small contract function, e.g. `FirstLetterCocktailRoute(...)` and `SearchCocktailRoute(...)`
- app adds destinations and provides dependencies

---

## Testing and boundaries to watch
- Edge cases: empty results, null DTO fields, slow network/timeouts, offline/HTTP errors, rotation (state retention)
- Unit test mappers and safeApiCall
- ViewModel test: given repository returns Success/Error, state updates correctly

---

## Checklist to implement
1) Create modules: core:domain, core:ui, core:data, feature:firstlettercocktail, feature:searchcocktail
2) Add dependencies as per graph above
3) Put ResponseState and domain models in core:domain
4) Put Retrofit, DTOs, mappers, repositories, and safeApiCall in core:data
5) Build MVI contracts and ViewModels in features
6) Compose screens consume state and use core:ui components
7) Wire DI in app (manual first), add navigation

---

## FAQ
- Should shared stuff stay in app? No. Keep app thin—move shared code to core modules.
- Where do generics shine? In your ResponseState, safeApiCall, and mapData. Write them once and reuse everywhere.


