# Task
- start from no activity project
  - use 1 activity and multiple fragments 
- use nav graph
- use an api to get data in a recycler view -> use a button on a specific item to navigate to a detail screen
  - setup retrofit, okhttp, gson/moshi, glide/coil
    - steps below
  - setup adapter, viewholder, layoutmanager 
    - steps below
  - use glide/coil to load images from url (optional)
  - use viewmodel to persist data (optional)
  - make it look nice (optional)

# Network and API
## Concepts
- REST (Representational State Transfer) — architectural style for designing networked applications.
    - Uses standard HTTP methods (GET, POST, PUT, DELETE).
    - Stateless communication: each request from client to server must contain all info needed.
    - Resources identified by URIs (Uniform Resource Identifiers).
    - Commonly uses JSON or XML for data exchange.
- HTTP (Hypertext Transfer Protocol) — protocol for transmitting hypermedia documents, such as HTML.
    - Methods: GET (retrieve), POST (create), PUT (update), DELETE (remove).
- API (Application Programming Interface) — set of rules and protocols for building and interacting with software applications.
    - RESTful APIs follow REST principles.
    - Endpoints: specific URLs where API resources can be accessed.

- Request Types:
  - [URL Parameters](https://google.com/articles/{id}/2)
    - what developers use to identify a specific resource
  - Query Parameters : https://google.com/articles?id=2&sort=asc
    - Appended to the end of the URL after a `?`.
    - Used for filtering, sorting, pagination, etc.
    - Multiple parameters separated by `&`.
    - not secure - don't use for sensitive data
  - Body Parameters
    - Not part of the URL (secure)
    - Sent in the body of POST/PUT requests
    - JSON/XML format 

- Response Code:
  - 200s → Success
  - 300s → Redirection
  - 400s → Client Errors (e.g., 404 Not Found, 401 Unauthorized)
  - 500s → Server Errors (e.g., 500 Internal Server Error)

- JSON - only has 2 types of data
  - {} → object, can have key-value pairs
  - [] → array

- Interceptors
  - helps you modify the request or response before reaching its destination
  - automatically intercepts/engages for every request/response 
  - can intercept an error and provide a fallback response for better user experience
  - can monitor, rewrite, and retry calls
  - useful for logging, modifying headers, adding authentication tokens, etc.

## Library Integration
- Retrofit — type-safe HTTP client for Android and Java.
    - Wrapper library to help integrate API calls
    - Define API endpoints as interfaces with annotations (@GET, @POST, @PUT, @DELETE).
    - Supports JSON parsing with converters (e.g., Gson, Moshi).
    - Handles async calls with Callbacks or Coroutines (suspend functions).
    - Everyone uses Retrofit for API calls - one line calls
      - without would be 20-40 lines of boilerplate code
- OkHttp — HTTP & HTTP/2 client for Android and Java applications.
    - library that handles the connection (requests and responses)
      - connection, interceptors, delay, retry, caching, etc.
    - Used by Retrofit under the hood.
    - Supports connection pooling, GZIP, caching, interceptors for logging or modifying requests/res
- Gson/Moshi — JSON serialization/deserialization libraries.
    - Convert JSON to Kotlin data classes and vice versa.
    - Gson is more established; Moshi has better Kotlin support (nullability, default values).

## ImageView -> does not load urls directly, we use a third party library to load them
- Glide — image loading and caching library for Android.
    - Efficiently loads images from URLs into ImageViews.
    - Supports placeholders, error images, transformations (cropping, rounding).
    - Handles caching and memory management.
- Coil — modern image loading library for Android backed by Kotlin Coroutines.
    - Simple and concise API for loading images.
    - Supports coroutines for async loading.
    - Built-in support for common transformations and caching.

## RecyclerView
- When you want to display a list of views
- RecyclerView
  - LayoutManager
    - LinearLayoutManager → vertical/horizontal scrolling list
    - GridLayoutManager → grid of items
    - StaggeredGridLayoutManager → grid with varying item sizes
  - Adapter → binds data to views 
    - handling individual UI items 
    - ViewHolder → holds references to item views for recycling
    - onBindViewHolder → binds data to item views
    - onCreateViewHolder → inflates item layout and creates ViewHolder
    - getItemCount → returns total number of items

## Steps to Setup Network/API
- imports to Gradle (module: app)
  - retrofit, converter (gson/moshi), okhttp, logging-interceptor, glide/coil
  - import individual gson dependency 
```gradle.kts
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.9.3")
```
- Pick an API (e.g., https://jsonplaceholder.typicode.com/)
  - https://github.com/public-apis/public-apis/blob/master/README.md
    - this is a list of free APIs for testing and learning
  - github.com/users -> provides JSON data of users for us to test
  - test and explore endpoints with Postman or similar tools
  - request data class
  - response data class
  - plugins -> Android Studio -> Settings -> Plugins -> JSON to Kotlin Class
    - transforms JSON response to Kotlin data class
    - Advanced settings -> use val, nullable, initialize with non-null values, annotations -> Gson, Extensions -> Suffix append -> Model
  - data/model/ model classes
  - data/remote
    - APIDetails.kt
      - api details, reference to retrofit instance
```kotlin
object APIDetails {
    const val BASE_URL = "https://api.github.com/"
    const val END_POINT = "users"
  
  // this is a builder pattern to create one instance of many objects
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
      .create<UserService>(UserService::class.java)
}
```
- BASE_URL = "https://api.github.com/"
- END_POINT = "users"
  - if multiple, name them accordingly
- UserService.kt
- setup GET, POST, PUT, DELETE methods
- uses annotations to define endpoints and parameters "@GET", "@POST", "@Path", "@Query", "@Body"
- suspend functions for coroutine support
- example:
```kotlin
interface UserService {
    @GET(APIDetails.END_POINT)
    suspend fun getUsers(): List<UserItemModel>
}
```

- Define Base URL and endpoints
  - base url: e.g., https://jsonplaceholder.typicode.com/
  - const
  - object
- Retrofit instance
  - singleton object
  - configure with base URL, converter factory, OkHttp client (with interceptors if needed)
  - define get, post, put, delete methods and retrofit does the rest
- Retrofit Builder -> instance
  - Builder
  - Retrofit Service Instance
- Use the Instance to trigger API calls
```kotlin
// viewModel
class UserViewModel : ViewModel() {
    private val _users = MutableLiveData<List<UserItemModel>>()
    val users: LiveData<List<UserItemModel>> = _users

    fun fetchUsers() {
        // use a co-routine to make the network call
        viewModelScope.launch {
            try {
                val userList = APIDetails.retrofit.getUsers()
                _users.value = userList
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
```
- Handle UI

## NOTE:
- your dependencies should be the same version for Retrofit and Converter(Gson/Moshi)
- OkHttp (logging interceptor) needs to have a compatible version with Retrofit


# Step By Step (Monday Morning)
- use PokeAPI (https://pokeapi.co/) - linked in his notes

## setup Dependencies (gradle - module: app)
- library imports go in module: app -> build.gradle.kts
- plugin imports go in project -> build.gradle.kts (this applies to the whole project)
- sync after adding dependencies, also make comments on what each dependency is for and what it is
  - can option+enter to add dependency to libs.versions.toml and give it a name 
- libs.versions.toml
  - centralized version catalog of dependencies and plugins.
  - the build.gradle.kts files reference the versions here
    - shows breakdown of "name", "group", "version", "module", etc.
- look for most stable version -> check github if not on documentation site
- notice legacy implementation 
  - current: implementation("com.squareup.retrofit2:retrofit:2.9.0")
  - legacy: implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    - no parentheses, and single quotes instead of double quotes
    - ensure you ues current syntax for Gradle Kotlin DSL
- Retrofit
- Converter (Gson)
- OkHttp
- Glide

## Pick an API 
- https://pokeapi.co/
  - provides JSON data of pokemon for us to test
- BASE_URL = "https://pokeapi.co/api/v2/"
- END_POINTs 
  - "/pokemon/{name or id}"
- Create data classes (using plugin)
  - directions given above on enabling and using the plugin (for JSON to Kotlin Class)
  - create a package: data/model
  - create a Kotlin file: PokemonModel.kt

- after the data classes are created
  - check for null safety and errors 
  - `AnyModel` happens when the data from the JSON has mixed types or null values
    - decide if you want to make it nullable or a specific type or comment it out if you don't need it
  - `@SerializedName("json_key")` is used when the JSON key is not a valid Kotlin variable name (e.g., contains hyphens, starts with a number, etc.)
    - this annotation maps the JSON key to the Kotlin property allowing you to use valid Kotlin names in your code

## Create Retrofit Instance and Service
- Service: create interface with properties of endpoints
- Retrofit Instance: using Retrofit.Builder() to create a singleton instance and align properties
  - BASE_URL
  - Converter (Gson)
  - Interceptor (OkHttp)
- create /data/remote
  - APIDetails.kt
  - PokemonService.kt
- add Interceptor (OkHttp) for logging
  - useful for debugging and seeing request/response details in logcat
  - can be added to the Retrofit instance if needed
  - optional, but recommended for development
  - can be removed or set to NONE for production builds
- make the service accessible by creating a public val `API_SERVICE` in APIDetails.kt
  - now you call `APIDetails.API_SERVICE.getPokemonByName("pikachu")` from anywhere in the app

```kotlin
// /data/remote/APIDetails.kt
object APIDetails {
    const val BASE_URL = "https://pokeapi.co/api/v2/"
    const val POKE_BY_NAME = "pokemon/{name}" // can be used if you want to hardcode endpoint
  
  // create logging interceptor to log request and response details
  // use apply to configure the interceptor with properties like log level, etc.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // log full request and response body in debug builds
        } else {
            HttpLoggingInterceptor.Level.NONE // no logging in release builds for security
        }
    }
  
  // create OkHttpClient and pass the logger into it
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // set connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // set read timeout
        .build()
  
  // this is a builder pattern to create one instance of many objects
    private val retrofit = Retrofit.Builder()
        // configure the instance with properties
        // set the base url for all endpoints
        .baseUrl(BASE_URL)
        // set the converter factory to parse JSON responses
        .addConverterFactory(GsonConverterFactory.create())
        // create an OkHttpClient with logging interceptor
        .client(okHttpClient)
        // build the Retrofit instance for the specified properties
        .build()

  // create the service interface for API calls
    val API_SERVICE: PokemonService = retrofit.create(PokemonService::class.java)
}
```

```kotlin
// /data/remote/PokemonService.kt
interface PokemonService {
    // this is our endpoint
    // {name} is a path parameter that will be replaced with actual pokemon name or id
    @GET("pokemon/{name}")
    // can use @GET(APIDetails.END_POINT) if you define END_POINT in APIDetails.kt for hardcoding variables
    // suspend function for coroutine support
    // @Path annotation to specify the path parameter we pass in from our function call 
      // We will end up calling getPokemonByName("pikachu") or getPokemonByName("25") in our Fragment/Activity/ViewModel
    // PokemonModel is the data class we created to map the JSON response
    suspend fun getPokemonByName(@Path("name") name: String): PokemonModel
}
```

## Use Instance to call API service (all endpoints defined will be accessible)
- best practice is to have instance as `suspend fun` 
- call `em` from a coroutine scope
  - ViewModelScope is best practice

```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    lateinit var binding: ActivityMainBinding
    
  override fun onCreate(savedInstanceState:) : Bundle? {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()

      // use viewBinding
      val binding = ActivityMainBinding.inflate(layoutInflater)
      setContentView(binding.root)
    
      
    
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container)) {/* ... */}
    
      lifecycleScope.launch {
          val pokemonByName = APIDetails.API_SERVICE.getPokemonByName("pikachu")
          val allPokemon = APIDetails.API_SERVICE.getAllPokemon(limit = 10, offset = 0)
          result?.let {
              binding.pokemon_info.text = result.toString() 
          }
      }
  }  
}


```

```xml
<!-- activity_main.xml -->
<LinearLayout
    android:id="@+id/main_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/pokemon_info"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!" />
</LinearLayout>
```

## LogCat
- `--> GET` - our request
- `<-- 200 OK` - response code
- this should be followed by the HTTP headers
- then the JSON response body

## Troubleshoot issues
- dependencies are not in sync
  - kotlin is not compatible with the version of retrofit or converter
- old version - our example was kotlin needed to be updated from 2.0.0 to 2.2.20 
  - fiexed it
- run again, we have the `ViewModel? = AnyModel()` - meaning there was null values in the JSON response
  - comment out the SerializedName and val lines associated with it if you don't need it
- we get too much json data
  - need to add: `uses-permission android:name="android.permission.INTERNET"` to AndroidManifest.xml



# RecyclerView Setup (XML)
- uses a list of items (data class)
- Reference
- Declaration
  - XML -> add RecyclerView widget to layout (Activity/Fragment)
  - Code -> Define:
    - LayoutManager
    - Adapter (inherit existing Adapter class -> override 3 methods below)
      - onCreateViewHolder -> Define the XML to be displayed (inflate)
        - LayoutInflater -> binds the XML layout to the ViewHolder
      - onBindViewHolder -> take care of actions on each item (binding data to views, click listeners, etc.)
      - getItemCount -> gives total number of items in the list


```xml
<!-- activity_main.xml -->
<LinearLayout
    android:id="@+id/main_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler_view_pokemon"
        android:layout_width="match_parent"
        android:layout_height="match_parent"    
         />
</LinearLayout>
```
```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    
    lateinit var binding: ActivityMainBinding
    
  override fun onCreate(savedInstanceState:) : Bundle? {
      super.onCreate(savedInstanceState)
      enableEdgeToEdge()

      // use viewBinding
      val binding = ActivityMainBinding.inflate(layoutInflater)
      setContentView(binding.root)
    
      
    
      ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container)) {/* ... */}
    
      lifecycleScope.launch {
          val pokemonByName = APIDetails.API_SERVICE.getPokemonByName("pikachu")
          val allPokemon = APIDetails.API_SERVICE.getAllPokemon(limit = 10, offset = 0)
          result?.let {
// --------------------- THIS IS WHERE THINGS CHANGE    ---------------------          
              binding.recycler_view_pokemon.apply {
                  // only thing to setup is the layoutManager is this line - done
                  layoutManager = LinearLayoutManager(this@MainActivity)
                  // adapter is custom - need to create a new class for it
                  // pass a lambda function so that every time an item is clicked, we can handle it in the Activity
                // have a fallback for null list with empty list elvis operator (?:)
                  adapter = PokemonAdapter(result.abilities ?: listOf(), {
                      // handle the action when an item is clicked
                      // if you start typing Toast, it will auto import the Toast library for you and give you a base
                      Toast.makeText(this@MainActivity, "Clicked: ${it.ability.name}", Toast.LENGTH_SHORT).show()
                  })
              }
          }
      }
  }  
}

// PokemonAdapter.kt
// Adapter requires overriding 3 methods (onCreateViewHolder, onBindViewHolder, getItemCount)
class PokemonAdapter(
    private val abilities: List<Ability>,
    private val onItemClick: (Ability) -> Unit // lambda function for item click handling
    // RecyclerView.Adapter requires a ViewHolder type (defined below) -> IMPORTANT
    // Now we need to create our own custom ViewHolder class -> this class is only used with this adapter, 
    // so we can define it in the same file 
) : RecyclerView.Adapter<PokemoneAdapter.PokemonViewHolder>() {
    override fun onCreateViewHolder(
      parent: ViewGroup, 
      viewType: Int
    ): PokemonViewHolder {
        // inflate the item layout and create a ViewHolder instance
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ability, parent, false)
        return PokemonViewHolder(itemView)
    }
  
    override fun onBindViewHolder(
      holder: PokemonViewHolder, 
      position: Int) {

        holder.binding.apply {
            val currentAbility = abilities[position] // or abilities.get(position) - both work, but this is recommended
            abilityName.text = currentAbility.ability.name
            abilityUrl.text = "URL: ${currentAbility.ability.url}" // string interpolation
            abilityDescription.text = currentAbility.ability.description ?: "No description available"
            abilityEffect.text = currentAbility.ability.effect ?: "No effect available"
            
            root.setOnClickListener {
                onItemClick.invoke(currentAbility) // call the lambda function with the clicked item
            }
        }
    }
  
  // example of an expression body syntax (inline return)
    override fun getItemCount() = abilities.size // return total number of items
    
    // ViewHolder class to hold references to item views for recycling
    // need to extend the ViewHolder class type to be used with the adapter
    // need to create a parameter for the itemView (the root view of the item layout)
    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: read documentation on init - seems confusing
        // only triggered when a new item view is created
        init {
          val binding = ItemAbilityBinding.bind(itemView)
          binding.apply {
            abilityName = binding.abilityName
            abilityUrl = binding.abilityUrl
            abilityDescription = binding.abilityDescription
            abilityEffect = binding.abilityEffect
            
            root.setOnClickListener {
                // handle item click if needed
            }
          }
        }
    }
}


// THIS IS AN EXAMPLE OF KOTLIN'S EXPRESSION BODY SYNTAX
// can be used when the function only has a single expression to return
// can be used for onCreateViewHolder above instead of the block body syntax
override fun onCreateViewHolder(
  parent: ViewGroup,
  viewType: Int
) = PokemonViewHolder(
  // parent reference is used to get the context for inflating the layout
    LayoutInflater.from(parent.context)
        // first parameter is the layout resource id
        // second parameter is the parent view group (RecyclerView)
        // third parameter is whether to attach the inflated view to the parent (false here)
        .inflate(R.layout.item_ability, parent, false)
)

```

```xml
<!-- item_ability.xml -->
<!-- This will create 2 columns and 2 rows to provide a description of a pokemon ability (name, url, description, and effect) -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="8dp">

    <LinearLayout
        android:layout_height="wrap_content"
        android:layout_width="match_parent"
        android:orientation="horizontal" >
      
<!--   Using width 0dp with weight 1 which allows us to create columns   -->
        <TextView
            android:id="@+id/ability_name"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Ability Name"
            android:textSize="16sp"
            android:textStyle="bold" />
        <TextView
            android:id="@+id/ability_url"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Ability URL"
            android:textSize="14sp" />
    </LinearLayout>
  
  <LinearLayout
          android:layout_height="wrap_content"
          android:layout_width="match_parent"
          android:orientation="horizontal" >

    <!--   Using width 0dp with weight 1 which allows us to create columns   -->
    <TextView
            android:id="@+id/ability_description"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Ability Name"
            android:textSize="16sp"
            android:textStyle="bold" />
    <TextView
            android:id="@+id/ability_effect"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Ability URL"
            android:textSize="14sp" />
  </LinearLayout>
  
</LinearLayout>

```