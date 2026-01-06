These are the steps of what code to add when implementing a new API in an Android application:

## 1. **Research the API**: 
- Understand the purpose of the API
- its endpoints
- request/response formats
- authentication methods


## 2. **Set Up Dependencies**:
- Add necessary libraries to your `build.gradle` file (e.g., Retrofit, OkHttp, Gson/Moshi for JSON parsing)
- Sync your project to download the dependencies

---

# Data Layer

## 3. **Create Data Models**:
- Define Kotlin data classes that represent the JSON structure of the API responses
  - data/dto/ApiResponse.kt
    - or swap model for dto based on your architecture preferences

```kotlin
// data/dto/ApiResponse.kt
data class ApiResponse(
    val id: Int,
    val name: String,
    val description: String
)
```

## 4. **Set Up Retrofit Object**:
- Create a Retrofit instance with the base URL of the API
- Configure converters (e.g., GsonConverterFactory) for JSON parsing
  - data/network/RetrofitInstance.kt
    - swap network for api or remote based on your architecture preferences
    - file can be called ApiClient, ApiHelper, ApiRetrofit, ApiDetails, etc.

```kotlin
// data/network/RetrofitInstance.kt
// NOTE: use an object to create a singleton Retrofit instance
object RetrofitInstance {
    private const val BASE_URL = "https://api.example.com/"
    private const val PERSONS_ENDPOINT = "persons/"
    private const val POSTS_ENDPOINT = "posts/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
```

## 5. **Define API Service Interface**:
- Create an interface that defines the API endpoints and their HTTP methods
  - data/network/ApiService.kt

```kotlin
// data/network/ApiService.kt
interface ApiService {
    @GET(PERSONS_ENDPOINT)
    suspend fun getPersons(): List<ApiResponse>

    @GET(POSTS_ENDPOINT)
    suspend fun getPosts(): List<ApiResponse>
}
```

## 6. **Create RepositoryImpl**:
- Implement a repository class that uses the ApiService to fetch data
  - data/repository/ApiRepositoryImpl.kt
- NOTE: you'll create an interface for this repository in the domain layer
- Repository will handle data operations and can be used by ViewModels or Use Cases
- A single repository will handle all API calls for a specific API

```kotlin
// data/repository/ApiRepositoryImpl.kt
class ApiRepositoryImpl(private val apiService: ApiService) : ApiRepository {
    
    // Example of using StateFlow to hold data
    // TODO: is this necessary here? Should it be in ViewModel instead?
    // Answer: Usually no. Let repositories expose suspend functions or cold Flows.
    // Keep StateFlow/MutableStateFlow in ViewModel (UI layer) where lifecycle and
    // configuration-change handling belong.
    private val postFlow = MutableStateFlow<List<ApiResponse>>(emptyList())
  
    override suspend fun fetchPersons(): List<Person> {
        // Map DTO -> Domain here in the data layer (data depends on domain)
        return apiService.getPersons().map { it.toDomainModel() }
    }

    // use Flow for streams of data (e.g., DB). For simple one-shot network calls,
    // a suspend function returning a List is fine.
    override suspend fun fetchPosts(): List<Post> {
        return apiService.getPosts().map { it.toDomainPost() }
    } 
}
```

## 7. **Create Mappers (if needed)**:
- If your data models differ from your domain models, create mapper functions to convert between them
  - data/mapper/ApiMapper.kt
  - this helps keep layers separate and maintainable
  - you can create extension functions for mapping

```kotlin
// data/mapper/ApiMapper.kt
fun ApiResponsePerson.toDomainModel(): Person {
    return Person(
        id = this.id,
        name = this.name,
        description = this.description
    )
}
```

--- 

# Domain Layer

## 8. **Create Domain Models**:
- Define domain models that represent the core business entities
- How your app will use the data
  - domain/model/Person.kt
  - domain/model/Post.kt
```kotlin
// domain/model/Person.kt
data class Person(
    val id: Int,
    val name: String,
    val description: String
)
```


## 9. **Create Repository Interface**:
- Define an interface for the repository in the domain layer
  - domain/repository/ApiRepository.kt
  - this allows for abstraction and easier testing

```kotlin
// domain/repository/ApiRepository.kt
interface ApiRepository {
    suspend fun fetchPersons(): List<Person>
    suspend fun fetchPosts(): List<Post>
    // If you truly need streams, expose Flow in the domain API and implement it in data.
    // e.g., fun observePosts(): Flow<List<Post>>
}
```

## 10. **Create Use Cases (Optional)**:
- If following Clean Architecture, create use case classes that encapsulate specific actions
  - domain/usecase/FetchPersonsUseCase.kt
  - domain/usecase/FetchPostsUseCase.kt
  - Use cases interact with the repository to fetch data
  - This step is optional but recommended for better separation of concerns
  - Use cases can be grouped in a single file if they are small and related
  - Use cases can also be implemented as functions in the repository interface if preferred
  - Use cases can also be implemented as extension functions on the repository interface if preferred

```kotlin
// domain/usecase/FetchPersonsUseCase.kt
class FetchPersonsUseCase(private val repository: ApiRepository) {
    suspend operator fun invoke(): List<Person> {
        return repository.fetchPersons()
    }
}

// domain/usecase/FetchPostsUseCase.kt
class FetchPostsUseCase(private val repository: ApiRepository) {
    suspend operator fun invoke(): List<Post> {
        return repository.fetchPosts()
    }
}

// Flow version (optional)
class ObservePostsUseCase(private val repository: ApiRepository) {
    operator fun invoke(): Flow<List<Post>> {
        return repository.observePosts()
    }
}
```

---

# Presentation/UI Layer / ViewModel

## 11. **Update ViewModel**:
- Inject the repository or use cases into your ViewModel
- Use LiveData, State, or StateFlow to expose data to the UI
  - LiveData is more traditional(xml)
  - StateFlow is preferred for Jetpack Compose (multiple updates over time, e.g., DB changes, news feed, etc.)
  - State is preferred for one-shot reads in Compose (single API call)
  - ui/viewmodel/ApiViewModel.kt

```kotlin
// ui/viewmodel/ApiViewModel.kt
class ApiViewModel(private val fetchPersonsUseCase: FetchPersonsUseCase) : ViewModel() {
    private val _persons = MutableLiveData<List<Person>>()
    val persons: LiveData<List<Person>> get() = _persons
    fun loadPersons() {
        viewModelScope.launch {
            val result = fetchPersonsUseCase()
            _persons.value = result
        }
    }
}
```

## 12. **Update UI Components**:
- Update your Activity/Fragment to observe the ViewModel data and update the UI accordingly
  - ui/composeScreen/ApiScreen.kt

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

## 13. **Handle Errors and Loading States**:
- Implement error handling and loading states in your ViewModel and UI
- This improves user experience and provides feedback during network operations
- you can use sealed classes or Result wrappers to represent different states

```kotlin
// Example of a sealed class for representing UI states
// TODO: where should this go?
// Answer: In the presentation layer (feature module) next to the ViewModel,
// or in a shared "core:ui" module if used across many features. Avoid putting UI state in domain.
// ui/model/UiState.kt (recommended)
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// In ViewModel
// ui/viewmodel/ApiViewModel.kt

@HiltViewModel
class ApiViewModel @Inject constructor(
    private val fetchPersons: FetchPersonsUseCase
): ViewModel() {
    // TODO: shouldn't this use StateFlow instead of LiveData?
    // Answer: With Compose, prefer StateFlow. LiveData also works, but StateFlow integrates
    // naturally with collectAsState(). Example StateFlow version:
    // TODO: prefer State over StateFlow when working with an API that is read only once
    // StateFlow is for Data streams that can change over time (e.g., DB updates).

    // State version:
    private val _personsState = mutableStateOf<UiState<List<Person>>>(UiState.Loading)
    val personsState: State<UiState<List<Person>>> = _personsState

    // StateFlow version:
    private val _personsState = MutableStateFlow<UiState<List<Person>>>(UiState.Loading)
    val personsState: StateFlow<UiState<List<Person>>> = _personsState.asStateFlow()

    // LiveData version:
    private val _personsState = MutableLiveData<UiState<List<Person>>>()
    val personsState: LiveData<UiState<List<Person>>> = _personsState

    fun loadPersons() {
        viewModelScope(Dispatchers.IO).launch {
            _personsState.value = UiState.Loading
            try {
                val result = fetchPersons()
                _personsState.value = UiState.Success(result)
            } catch (e: Exception) {
                _personsState.value = UiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

// In UI
// ui/composeScreen/ApiScreen.kt
@Composable
fun ApiScreen(viewModel: ApiViewModel = hiltViewModel()) {
    // Using LiveData:
    // val personsState by viewModel.personsState.observeAsState(UiState.Loading)
    // Using StateFlow:
    // val personsState by viewModel.personsState.collectAsStateWithLifecycle(UiState.Loading)
    // Using State:
    // Kick off the load when the composable is first launched
    // avoids re‑running on recomposition, re‑runs only if the key changes, and cancels when the composable leaves
    // Once the data is pulled from the API, we have cached it in ViewModel state, surviving recompositions and configuration changes
    LaunchedEffect(Unit) {
        viewModel.loadPersons() 
    }
    val personsState by viewModel.personsState
    when (personsState) {
        is UiState.Loading -> {
            CircularProgressIndicator()
        }
        is UiState.Success -> {
            val persons = (personsState as UiState.Success<List<Person>>).data
            LazyColumn {
                items(persons) { person ->
                    Text(text = person.name)
                }
            }   
        }
        is UiState.Error -> {
            val message = (personsState as UiState.Error).message
            Text(text = "Error: $message")
        }
    }
}
```

---

# Dependency Injection

## 14. **Set Up Dependency Injection**:
- If using a DI framework (e.g., Hilt, Dagger), set up modules to provide instances of Retrofit, ApiService, Repository, and Use Cases
  - di/NetworkModule.kt
  - di/RepositoryModule.kt
  - di/UseCaseModule.kt
- add necessary annotations to your classes for injection (e.g., @AndroidEntryPoint - App, @EntryPoint - Activity, @HiltViewModel - ViewModel)
- `@Inject constructor` where needed to allow Hilt to provide dependencies to view models, repositories, etc.

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // import BASE_URL from wherever it's defined
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    // TODO: do i need to inject retrofit here?
    // Answer: No, you do not need to use `@Inject constructor` in `@Module` objects.
    // Hilt injects the parameters automatically; you just declare them as function parameters.
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideApiRepository(apiService: ApiService): ApiRepository {
        return ApiRepositoryImpl(apiService)
    }
}

// di/UseCaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideFetchPersonsUseCase(repository: ApiRepository): FetchPersonsUseCase {
        return FetchPersonsUseCase(repository)
    }
}
```

--- 

# Additional Options

## 15. **Pagination (Optional)**:
- If the API supports pagination, implement pagination logic using libraries like Paging 3
- This is especially useful for large datasets (10,000+ items)
- use Room DB with RemoteMediator if offline caching is needed

```kotlin
// data/paging/PersonsPagingSource.kt
class PersonsPagingSource(
  private val api: ApiService,
  private val pageSize: Int = 20
) : PagingSource<Int, Person>() {

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Person> {
    val page = params.key ?: 1
    return try {
      val dtos = api.getPersons(page = page, pageSize = params.loadSize.takeIf { it > 0 } ?: pageSize)
      val data = dtos.map { it.toDomainModel() }
      LoadResult.Page(
        data = data,
        prevKey = if (page == 1) null else page - 1,
        nextKey = if (data.isEmpty()) null else page + 1
      )
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, Person>): Int? {
    val anchor = state.anchorPosition ?: return null
    val page = state.closestPageToPosition(anchor) ?: return null
    return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
  }
}


// data/repository/ApiRepositoryImpl.kt
class ApiRepositoryImpl(
  private val apiService: ApiService
) : ApiRepository {

  override suspend fun fetchPersons(): List<Person> {
    return apiService.getPersons().map { it.toDomainModel() }
  }

  override suspend fun fetchPosts(): List<Post> {
    return apiService.getPosts().map { it.toDomainPost() }
  }

  override fun pagedPersons(): Flow<PagingData<Person>> {
    return Pager(
      config = PagingConfig(
        pageSize = 20, // how many items to load per page
        prefetchDistance = 2, // how far from the edge to prefetch -> meaning when to initialize loading the next page
        enablePlaceholders = false
      ),
      pagingSourceFactory = { PersonsPagingSource(apiService, pageSize = 20) }
    ).flow
  }
}


// domain/repository/ApiRepository.kt
interface ApiRepository {
  suspend fun fetchPersons(): List<Person>
  suspend fun fetchPosts(): List<Post>
  // TODO: why not suspend here?
  // Answer: Because PagingData is a stream of data that emits multiple values over time.
  fun pagedPersons(): Flow<PagingData<Person>> 
}

// TODO: should I create a use case for pagedPersons()?
// Answer: It's optional. If you want to keep your ViewModel thin and follow Clean 2_Architecture strictly, you can create a use case for it. 
// However, since paging is often closely tied to UI behavior, it's common to call it directly from the ViewModel.


// ui/viewmodel/ApiViewModel.kt
@HiltViewModel
class ApiViewModel @Inject constructor(
  private val repository: ApiRepository
) : ViewModel() {
    
    // TODO: why cachedIn?
    // Answer: cachedIn(viewModelScope) caches the loaded data in the ViewModel's scope.
    // This means that if the user rotates the device or navigates away and back to
    // the screen, the previously loaded pages are retained and not re-fetched from the network.
  val personsPaging: Flow<PagingData<Person>> =
    repository.pagedPersons().cachedIn(viewModelScope)
}


// ui/composeScreen/ApiScreen.kt

// NOTE: You typically don’t wrap Paging in your own Loading/Success/Error. 
    // Paging 3 exposes LoadState (refresh, append, prepend) that you render directly
// This is minimal example showing just the list of items
// The next code block shows how to handle LoadStates
@Composable
fun ApiScreen(viewModel: ApiViewModel = hiltViewModel()) {
    
    // Collect the PagingData as LazyPagingItems for use in LazyColumn
    // this allows for paging 3 to initialize the next pages as the user scrolls
  val lazyItems = viewModel.personsPaging.collectAsLazyPagingItems()

  LazyColumn {
    items(
      count = lazyItems.itemCount,
      key = { index -> lazyItems[index]?.id ?: index }
    ) { index ->
      val person = lazyItems[index]
      if (person != null) {
        Text(text = person.name)
      }
    }
  }
}

// ui/composeScreen/ApiScreen.kt
// Handling Load States

// Initial load / full refresh states
// if error -> button to retry 
// Kotlin
@Composable
fun ApiScreen(viewModel: ApiViewModel = hiltViewModel()) {
  val lazyItems = viewModel.personsPaging.collectAsLazyPagingItems()
  val loadState = lazyItems.loadState

  // Initial load / full refresh states
  when (val refresh = loadState.refresh) {
    is LoadState.Loading -> {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
      return
    }
    is LoadState.Error -> {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Error: ${refresh.error.message ?: "Unknown error"}")
          Spacer(Modifier.height(8.dp))
          Button(onClick = { lazyItems.retry() }) { Text("Retry") }
        }
      }
      return
    }
    else -> Unit
  }

  LazyColumn {
    if (lazyItems.itemCount == 0) {
      item {
        Box(
          Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) { Text("No people found") }
      }
    } else {
      items(
        count = lazyItems.itemCount,
        key = { index -> lazyItems[index]?.id ?: index }
      ) { index ->
        lazyItems[index]?.let { person ->
          Text(
            text = person.name,
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          )
        }
      }
    }

    // Footer: subsequent paging states (append). Add prepend if needed.
    // append is when loading more items at the end of the list
    item {
      when (val append = loadState.append) {
        is LoadState.Loading -> {
          Box(
            Modifier
              .fillMaxWidth()
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) { CircularProgressIndicator() }
        }
        is LoadState.Error -> {
          Row(
            Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Load more failed")
            TextButton(onClick = { lazyItems.retry() }) { Text("Retry") }
          }
        }
        else -> Unit
      }
    }
  }
}


```