# Resources:

# API → UI: adding a new endpoint (high-level)
These are practical steps for wiring a new API endpoint through an Android app (data → domain → presentation). The examples assume a layered setup (MVVM / Clean-ish) and Kotlin coroutines.

## 1. **Research the API**
- Purpose, ownership, SLAs
- Endpoints, pagination, filtering, sorting
- Request/response shapes, error model, idempotency
- Auth (OAuth/OIDC, API keys), token refresh behavior
- Caching semantics (ETag/Cache-Control) and rate limits

## 2. **Set up dependencies**
- Add libraries (Retrofit, OkHttp, Moshi/Gson, Kotlinx Serialization, etc.)
- Add logging only for debug builds
- Prefer `converter-moshi` + `@JsonClass(generateAdapter = true)` for strong typing (optional)

---

# Data layer (network + persistence)

## 3. **Create DTOs (network models)**
- Define Kotlin `data class`es that match the API payload.
- Keep DTOs in the data layer (`data/dto`) and map them to domain models.

```kotlin
// data/dto/PersonDto.kt
data class PersonDto(
    val id: Int,
    val name: String,
    val description: String
)
```

## 4. **Set up Retrofit**
- Create a Retrofit instance with your base URL.
- Configure converters.
- In real apps, prefer providing Retrofit/OkHttp via DI (Hilt/Koin) rather than a global singleton object.

```kotlin
// data/network/RetrofitInstance.kt
// NOTE: For learning only. In production prefer DI-provided singletons.
object RetrofitInstance {
    private const val BASE_URL = "https://api.example.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
```

## 5. **Define the API service interface**
- Define endpoints and HTTP methods.
- Prefer returning a concrete DTO type instead of a generic `ApiResponse` for everything.

```kotlin
// data/network/ApiService.kt
interface ApiService {
    @GET("persons")
    suspend fun getPersons(): List<PersonDto>

    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
```

## 6. **Implement the repository (data → domain mapping)**
- Repository implementations live in the data layer.
- They coordinate *data sources* (network, cache, DB) and map DTOs/Entities to domain.
- Avoid `MutableStateFlow` inside repositories unless you’re building an in-memory cache that’s explicitly part of your data layer contract.
  - In most apps: repositories expose **suspend functions** (one-shot) or **cold Flows** (streams); the ViewModel owns hot state (`StateFlow`, `MutableStateFlow`) and UI state.

```kotlin
// data/repository/ApiRepositoryImpl.kt
class ApiRepositoryImpl(
    private val apiService: ApiService
) : ApiRepository {

    override suspend fun fetchPersons(): List<Person> {
        // Map DTO -> Domain here in the data layer (data depends on domain)
        return apiService.getPersons().map { it.toDomain() }
    }

    override suspend fun fetchPosts(): List<Post> {
        return apiService.getPosts().map { it.toDomain() }
    }
}
```

## 7. **Create mappers (DTO ↔ domain)**
- Keep mapping functions close to the DTOs (or in a dedicated mapper file) and make them boring and deterministic.

```kotlin
// data/mapper/ApiMappers.kt
fun PersonDto.toDomain(): Person = Person(
    id = id,
    name = name,
    description = description
)
```

---

# Domain layer

## 8. **Create domain models**
- Domain models represent your app’s business concepts and should not depend on Retrofit/Room/Android.

```kotlin
// domain/model/Person.kt
data class Person(
    val id: Int,
    val name: String,
    val description: String
)
```

## 9. **Create repository interfaces**
- Domain defines the interface so presentation doesn’t know/care how data is fetched.

```kotlin
// domain/repository/ApiRepository.kt
interface ApiRepository {
    suspend fun fetchPersons(): List<Person>
    suspend fun fetchPosts(): List<Post>

    // If you truly need streams, expose Flow in the domain API and implement it in data.
    // e.g., fun observePosts(): Flow<List<Post>>
}
```

## 10. **Create use cases (optional, but useful at scale)**
Use cases are helpful when:
- you combine multiple repositories,
- you add business rules/validation,
- you want a stable API for the ViewModel,
- you need consistent threading / error mapping policies.

```kotlin
// domain/usecase/FetchPersonsUseCase.kt
class FetchPersonsUseCase(private val repository: ApiRepository) {
    suspend operator fun invoke(): List<Person> = repository.fetchPersons()
}

class FetchPostsUseCase(private val repository: ApiRepository) {
    suspend operator fun invoke(): List<Post> = repository.fetchPosts()
}

// Flow version (optional)
class ObservePostsUseCase(private val repository: ApiRepository) {
    operator fun invoke(): Flow<List<Post>> = repository.observePosts()
}
```

---

# Presentation layer (ViewModel + UI)

## 11. **Update ViewModel**
- ViewModel owns UI state + lifecycle.
- With Compose, prefer `StateFlow` (or `MutableStateFlow`) for screen state; LiveData is fine for legacy XML.

```kotlin
// ui/viewmodel/ApiViewModel.kt
class ApiViewModel(
    private val fetchPersonsUseCase: FetchPersonsUseCase
) : ViewModel() {

    private val _persons = MutableLiveData<List<Person>>()
    val persons: LiveData<List<Person>> get() = _persons

    fun loadPersons() {
        viewModelScope.launch {
            _persons.value = fetchPersonsUseCase()
        }
    }
}
```

## 12. **Update UI components**
- Observe ViewModel state and render.

```kotlin
// ui/composeScreen/ApiScreen.kt
@Composable
fun ApiScreen(viewModel: ApiViewModel = hiltViewModel()) {
    val persons by viewModel.persons.observeAsState(emptyList())
    LazyColumn {
        items(persons) { person ->
            Text(text = person.name)
        }
    }
}
```

## 13. **Handle loading + error states**
- Put *UI state* in the presentation layer (feature module), not in domain.
- Avoid over-modeling. A simple sealed `UiState<T>` works well for one-shot loads.

```kotlin
// ui/model/UiState.kt
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : UiState<Nothing>()
}

@HiltViewModel
class ApiViewModel @Inject constructor(
    private val fetchPersons: FetchPersonsUseCase
) : ViewModel() {

    // Guidance on State vs StateFlow:
    // - If the UI only needs the *latest value* and you set it a few times (loading→success/error), either works.
    // - Prefer StateFlow when you want:
    //   - consistent collection APIs (collectAsStateWithLifecycle)
    //   - multiple collectors
    //   - easy combination/transforms (map, combine, debounce)
    // - `mutableStateOf` is great for a small Compose-only ViewModel, but StateFlow scales better across modules.
    private val _personsState = MutableStateFlow<UiState<List<Person>>>(UiState.Loading)
    val personsState: StateFlow<UiState<List<Person>>> = _personsState.asStateFlow()

    fun loadPersons() {
        viewModelScope.launch {
            _personsState.value = UiState.Loading
            runCatching { fetchPersons() }
                .onSuccess { _personsState.value = UiState.Success(it) }
                .onFailure { _personsState.value = UiState.Error(it.message ?: "Unknown error", it) }
        }
    }
}

@Composable
fun ApiScreen(viewModel: ApiViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadPersons()
    }

    val personsState by viewModel.personsState.collectAsStateWithLifecycle()

    when (val state = personsState) {
        is UiState.Loading -> CircularProgressIndicator()
        is UiState.Success -> LazyColumn {
            items(state.data) { person ->
                Text(text = person.name)
            }
        }
        is UiState.Error -> Text(text = "Error: ${state.message}")
    }
}
```

---

# Dependency injection (Hilt)

## 14. **Set up DI**
- Hilt provides dependencies by calling your `@Provides` functions; you don’t `@Inject` inside module objects.
- To customize OkHttp (auth headers, telemetry, logging), provide an `OkHttpClient` and pass it to Retrofit.

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // interceptors, timeouts, certificate pinning, etc.
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // define BASE_URL in one place (BuildConfig or a constants object)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideApiRepository(apiService: ApiService): ApiRepository = ApiRepositoryImpl(apiService)
}

// di/UseCaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideFetchPersonsUseCase(repository: ApiRepository): FetchPersonsUseCase =
        FetchPersonsUseCase(repository)
}
```

---

# Additional options

## 15. **Pagination (optional)**
- If the API supports pagination, use [Paging 3](/)
- If you need offline caching + paging, use Room + `RemoteMediator`.

