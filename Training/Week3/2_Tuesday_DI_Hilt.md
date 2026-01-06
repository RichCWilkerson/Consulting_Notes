# Task
do what we did yesterday, but use Dependency Injection (Hilt) to provide the Retrofit instance
- create a new project with empty activity


# Communication between Fragments
## Safe Args
- 2 main ways to pass data between fragments using Safe Args:
  - Bundler (pass with navController
    - can be easier for simple data types
    - less type-safe, more error-prone
    - use key values
  - Define in nav_graph.xml
    - 
## ViewModel (recommended)
- use Activity scope ViewModel to share data between fragments



# Dependency Injection 
- replaceable components without changing dependent code
  - action figure with interchangeable arms (sword, shield, etc)
- attempts to implement Inversion of Control (IoC) principle (DIP - Dependency Inversion Principle - SOLID)
  - high-level modules should not depend on low-level modules; both should depend on abstractions
  - abstractions should not depend on details; details should depend on abstractions

## History
- Dagger was most popular 
- google adopted the library -> added simpler features for Android -> called it Hilt 
- Added Annotations for android components (Activity, Fragment, Service, etc)

- NOTE: hilt knows at compile time what to inject where
  - no reflection at runtime -> better performance
- NOTE: DI allows your project to scale and test better
  - easier to swap implementations for testing (mocking, fakes, etc)

## Setup
- use library called Hilt -> 
- Gradle import plugin -> project level
- Gradle import library -> module level
- Code:
  - Create an Application class -> annotate with @HiltAndroidApp (marks entry point for Hilt)
    - applies the rules, config, and properties of Hilt to the entire app
  - Define Application class in Manifest
  - Annotations:
    - @Module (on class) -> a central point/repository where possible dependency injections are defined
      - @InstallIn (on class) -> define scope of the module
        - SingletonComponent -> application scope
        - ActivityComponent -> activity scope
        - FragmentComponent -> fragment scope
    - @Provides / @Binds (on method / fun) -> to declare and define what needs to be injected
      - name of function doesn't matter, return type MATTERS - Hilt is looking for return type to know what to inject
      - @Provides -> use when you can create a NEW instance in the method itself
      - @Binds -> use when you want to combined an EXISTING interface to its implementation
    - @Inject -> use the defined injections in the code 
      - Constructor injection
        - annotate constructor with @Inject constructor()
      - Property injection
        - annotate property with @Inject lateinit var property: Type
  - Android components (Activity, Fragment, Service, etc) need to be annotated with @AndroidEntryPoint (on class)
    - Hilt will generate code for dependency injection for these components
  - @HiltViewModel (on class) -> for ViewModels
    - use @Inject constructor() in ViewModel
    - use by viewModels() or by activityViewModels() to get instance of ViewModel
      - need to add dependency for hilt navigation fragment ktx

## Follow along
1. Add dependencies (Hilt library and Hilt navigation fragment ktx)
    - add library -> build.gradle.kts (Module: app)
      - apply plugin at the top
      - apply dependencies in dependencies block at the bottom (typically)
        - ksp -> for annotation processing, it is a compiler plugin, ksp affects how our code is compiled
        - need to also install ksp plugin in project level build.gradle.kts 
          - must match version of kotlin being used in the project
          - find the latest stable versions of these (usually github)
    - add plugin -> build.gradle.kts (Project)
2. create Application class
    - annotate with @HiltAndroidApp
    - define in Manifest
```kotlin
// Application.kt
@HiltAndroidApp
class Application : Application() {
}
```
```xml
<!-- AndroidManifest.xml -->
<application
    android:name=".Application" />
```

3. create a dependency to inject - @Modules and @Provides :
- 
```kotlin
// di/AppModule.kt
@Module
// define scope of di module -> how long it should live (singleton, activity, fragment, etc)
@InstallIn(SingletonComponent::class) // documentation has list of scope options
class AppModule {
    // Remember the return type matters, not the function name --> Gson will be injected where needed
    @Provides
    fun provideString(): Gson {
        return GsonBuilder().create()
    }
    
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor // 
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    @Provides
    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }
    
    @Provides
    fun provideRickService(
        retrofit: Retrofit
    ): RickService {
        return retrofit.create(RickService::class.java)
    }
}
```

4. 
- initialize the viewModel in the fragment/activity
```kotlin
// ui/rickmortylist/RickMortyViewModel.kt

@HiltViewModel
class RickMortyViewModel @Inject constructor(
    private val apiService: RickMortyService,
) : ViewModel() {
    private val _charactersList = MutableLiveData<List<Character>>()
    val charactersList: LiveData<List<Character>> = _charactersList
    
    fun getCharactersList() {
        viewModelScope.launch {
            val result = apiService.getAllCharacters()
            if (!result.results.isNullOrEmpty()) {
                _charactersList.postValue(result.results)
            } else {
                // handle empty or error case
            }
        }
    }

    
}

// ui/rickmortylist/RickMortyListFragment.kt

// need to also add 
@AndroidEntryPoint // because viewModel is being injected here 
class RickMortyListFragment : Fragment(R.layout.fragment_rick_morty_list) {
    private val viewModel: RickMortyViewModel by viewModels() // hilt will provide the instance
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 
        viewModel.charactersList.observe(viewLifecycleOwner) { characters ->
            // update UI
            updateUI(characterResult.results ?: listOf())
        }
        
        viewModel.getCharactersList()
        
        private fun updateUI(characters: List<ResultModel?>) {
            // update RecyclerView adapter or other UI elements
            binding.recyclerView.adapter = CharacterAdapter(characters)
            // etc
            
        }
    }
}

```


# Additional Notes
- using binds
- going to need a better example

```kotlin
interface Repository {
    suspend fun getAllCharacters(): RickMortyModel
    suspend fun getCharacterById(id: Int): ResultModel
}

class RepositoryImpl @Inject constructor(
    private val apiService: ApiService
) {
    override suspend fun getAllCharacters(): RickMortyModel

    override suspend fun getCharacterById(
        id: Int
    ): ResultModel
}

```