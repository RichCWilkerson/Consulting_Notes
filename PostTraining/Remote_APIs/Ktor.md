# Resources:
- [Intro to Ktor - Medium](https://codemaker2016.medium.com/introduction-to-ktor-6e238fa20c3)
  - Not best examples, but decent overview
  - Includes both server and client (Android) side examples
- [Ktor with Compose - Medium](https://proandroiddev.com/using-ktor-in-jetpack-compose-e00b1b32eef0)
  - foundation of the notes
- [Additional thoughts on Ktor - Medium](https://medium.com/@laetuzg/developing-backend-with-a-mobile-developer-mindset-using-ktor-52a07997fb20)
  - server-side ktor with mobile mindset
  - just a story of lessons learned
- [OpenTelemetry with Ktor - Medium](https://medium.com/@shinyDiscoBall/unlocking-observability-2-0-with-ktor-a-practical-guide-to-opentelemetry-integration-97fdda156fda)
  - 

- [Official Docs - Create REST API with Ktor](https://ktor.io/docs/server-create-restful-apis.html)
- [Official Docs - Integrate a Database with Ktor](https://ktor.io/docs/server-integrate-database.html)


# Overview of Ktor
- Ktor is an open-source framework built by JetBrains
- written in Kotlin and designed for building asynchronous servers and clients
  - Allows for KMP (Kotlin Multiplatform) projects
- designed to be highly scalable and performant, making it suitable for building high-traffic web applications
- can run on traditional servers, containerized environments, and serverless platforms like AWS Lambda.


## Why Ktor?
- **Kotlin-first**: Seamless integration with Kotlin language features
  - Coroutines for asynchronous programming
- **Lightweight and Modular**: Choose only the components you need
  - Allows for picking and choosing features based on project requirements
  - Allows for extension functions and DSL for more customization and readability
- **Interoperability**: Easily integrates with existing Java libraries and frameworks
- 



## Steps to Create a Ktor Application - Server Side
- [Ktor Project Generator](https://start.ktor.io/settings)
  - allows you to select features, dependencies, and configurations for your project - similar to Spring Initializr
1. Use the generator as the starting point for your Ktor application
2. Create models representing your data structures (data classes in Kotlin)
3. Create a Service for the api logic (CRUD operations, business logic, etc.)
4. Define routes and endpoints using Ktor's routing DSL, map CRUD to endpoints

### On the Android Client Side
1. Add dependencies for Ktor client in your Android project
```kotlin
dependencies {
    // old version
    implementation("io.ktor:ktor-client-android:${ktor_version}")
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")
    implementation("io.ktor:ktor-client-logging:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("io.ktor:ktor-client-serialization:${ktor_version}")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    
    // -----------------------------------------------------------------------------
    
    // new version 1 year ago (Feb 2025)
    // these are the minimum required for Ktor client with JSON serialization
    // add other dependencies as needed for other features - like above
    // this is the main client functionality
    implementation("io.ktor:ktor-client-core:2.3.12")
    // this is the ktor engine for Android
    // An engine in ktor is responsible for handling the network requests and there are different variants of engines for different platforms
    implementation("io.ktor:ktor-client-android:2.3.12")
    
    // -----------------------------------------------------------------------------
    // Additional features
    // This allows for JSON serialization/deserialization using kotlinx.serialization
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")

    // This allows for serializing and deserializing JSON using kotlinx.serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

}
```
2. Add plugins
```kotlin
// project build.gradle (Project-level)
plugins {
    // required for kotlinx serialization
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
}
```
```kotlin
// app build.gradle (Module-level)
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'

    // required for kotlinx serialization
    id("org.jetbrains.kotlin.plugin.serialization")
}
```
3. Ensure internet permission is added to AndroidManifest.xml
4. Create a data class model representing the data structure with @Serializable annotation
   - Serialization with Compose:
     - Understand which Jetpack Compose version you are working with 
     - This will tell you which Kotlin version you need to work with 
     - Then you must find a corresponding kotlin-serialization library version that matches the Kotlin version you found in step #2
```kotlin
@Serializable
data class CarDetails(
    @SerialName("car_make")
    val make: String,
    @SerialName("car_model")
    val model: String,
    @SerialName("year_of_production")
    val productionYear: Int
)
```
5. Create an instance of Ktor HttpClient with necessary features
```kotlin
// initialize Ktor HttpClient with Android engine
// this provides examples of "installing" features to the client - each needing their own dependencies
val myHttpClient = HttpClient(Android) {
    // HttpTimeout feature allows us to configure timeouts for our requests - would need to add dependency for this feature
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
    }
    // Since we want to make our HttpClient also handle JSON, we need to install a configuration to this client. 
    // This object is known as the HttpClientConfig and allows us to configure how our HttpClient will work in many ways.
    install(ContentNegotiation) {
        json()
    }
}

```
6. Create a class responsible for making API calls using the HttpClient
```kotlin
// TODO: this goes in the /data/remote/ folder? similar to Retrofit API service?
val httpResponse: HttpResponse = 
    try {
        // specify type of request e.g. .get, .post, .put, .delete, etc.
        // holds a HttpRequestBuilder object - https://api.ktor.io/ktor-http/io.ktor.http/-u-r-l-builder/index.html
        myHttpClient.get {
            url {
                // can set HTTP vs HTTPS
                protocol = URLProtocol.HTTPS
                // base URL
                host = "www.google.com"
                // endpoint path
                encodedPath = "path/file.html"
            }
        }
    } catch (e: Exception) {
        //Handle exception in request
    }
// TODO: do i need a new httpResponse for each request type (e.g. get, post, etc.)? If that is the case, it should be getHttpResponse, postHttpResponse, etc.?
val postHttpResponse: HttpResponse = 
    try {
        myHttpClient.post {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.google.com"
                encodedPath = "path/file.html"
            }
            // set body for POST request
            // can use PostData class instance here
            // setBody can take different types of data - String, ByteArray, etc.
            setBody("body content goes here") 
        }
    } catch (e: Exception) {
        //Handle exception in request
    }
```
7. Handle response status and parse response body
```kotlin
// TODO: this goes where? in the /data/remote/ folder? or /domain/repository/ folder?
when (httpResponse.status.value) {
            in 200..299 -> {
                val carDetails = httpResponse.body() as CarDetails
            }
            else -> {
                // Handle various server errors
            }
        }
```


### Common Features / Dependencies / Plugins
- **Routing**: Define routes and endpoints for handling HTTP requests
- **Content Negotiation**: Handle different content types (JSON, XML, etc.)
- **Authentication**: Implement various authentication mechanisms (JWT, OAuth, etc.)
- **Database Integration**: Connect to databases using libraries like Exposed or Ktorm
- **Logging**: Integrate logging frameworks for monitoring and debugging
- **Testing**: Tools and libraries for writing unit and integration tests

### Example Ktor with Compose

TODO: what is the equivalent of Retrofit's interface-based API definition in Ktor?
```kotlin
interface UserService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): User
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient) // configured with interceptors, timeouts, etc.
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val userService: UserService = retrofit.create(UserService::class.java)
```

TODO: how do you use it with DI? I'd prefer using Koin as the example to fit better with KMP - a big use case for Ktor
```kotlin
    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @ProductRetrofit
    fun provideProductRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(PRODUCT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

@Provides
@Singleton
fun provideUserApi(
    @UserRetrofit retrofit: Retrofit
): UserService = retrofit.create(UserService::class.java)

@Provides
@Singleton
fun provideUserRepository(
    userService: UserService,
    postDao: PostDao
): UserRepository =
    UserRepositoryImpl(userService, postDao)

@Provides
@Singleton
fun providePostsLoadPagingUseCase(userRepository: UserRepository): GetPostsPagingUseCase {
    return GetPostsPagingUseCase(userRepository)
}
```

TODO: I assume the repository / use case layer is the same, but just clarifying.
TODO: are there any other differences in how you would structure the app layers when using Ktor vs Retrofit?
TODO: do I need to add anything to my activity / application class to initialize Ktor client?

