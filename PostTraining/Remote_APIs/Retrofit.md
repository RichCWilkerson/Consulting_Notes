# Resources:
- [Retrofit – Square Docs](https://square.github.io/retrofit/)
- [OkHttp – Square Docs](https://square.github.io/okhttp/)
- [Moshi – JSON library](https://github.com/square/moshi)
- [Gson – JSON library](https://github.com/google/gson)

---

# Retrofit Overview

Retrofit is a **type‑safe HTTP client for Android and Java** built on top of OkHttp.

For a senior Android dev, it’s the standard tool for:
- Defining **HTTP APIs as interfaces** with annotations.
- Converting HTTP responses into **Kotlin/Java data classes** via converters (Moshi, Gson, Scalars, etc.).
- Integrating cleanly with **coroutines** and other async primitives.

> Mental model: Retrofit turns a REST (or HTTP) API into a strongly‑typed Kotlin interface. OkHttp does the actual HTTP work; Retrofit takes care of request/response marshalling and error mapping.

---

## Annotations

Core method annotations:
- `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PATCH` – specify HTTP method and path.
  - `@POST` = create
  - `@PUT` = update/replace
  - `@PATCH` = update/modify
    - different from `@PUT` in that it only sends changed fields
- `@HEAD`, `@OPTIONS` – less common but supported.

Parameter annotations:
- `@Path` – bind a function parameter into the URL path.
  - Example: `@GET("users/{id}") fun getUser(@Path("id") userId: String)`
- `@Query` – add query parameters to the URL.
  - Example: `@GET("search") fun search(@Query("q") query: String, @Query("page") page: Int)`
- `@QueryMap` – dynamic map of query params.
- `@Body` – send an object as the request body (typically JSON via converter).
  - Example: `@POST("login") suspend fun login(@Body request: LoginRequest): LoginResponse`
- `@Header` – add a header per‑call.
  - Example: `@GET("user") fun getUser(@Header("Authorization") token: String)`
- `@HeaderMap` – dynamic map of headers.

Form and multipart:
- `@FormUrlEncoded` – for form submissions (`application/x-www-form-urlencoded`).
  - Use `@Field` and `@FieldMap` inside.
- `@Multipart` – for file uploads.
  - Use `@Part` and `@PartMap` for each part.
  - Often used with `MultipartBody.Part` for file and `RequestBody` for text parts.

Other useful annotations:
- `@Streaming` – stream large responses (e.g., file downloads) without loading entire body into memory.
- `@Headers` – static headers defined on the method.
- `@HTTP` – custom HTTP method when you need something unusual.

---

## Common Use Cases

### 1. JSON REST API with Coroutines

- Most common setup in modern Kotlin apps:
  - Retrofit + OkHttp + Moshi/Gson + suspend functions.

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

Usage in a ViewModel:

```kotlin
class UserViewModel(private val service: UserService) : ViewModel() {
    private val _state = MutableStateFlow<UserState>(UserState.Loading)
    val state: StateFlow<UserState> = _state

    fun loadUser(id: String) {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { service.getUser(id) }
                _state.value = UserState.Success(user)
            } catch (e: Exception) {
                _state.value = UserState.Error(e)
            }
        }
    }
}
```

### 2. Multipart File Upload (e.g., photo upload)

```kotlin
interface PhotoApi {
    @Multipart
    @POST("uploadPhoto")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): PhotoUploadResponse
}

fun createPhotoPart(photoFile: File): MultipartBody.Part {
    val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
}
```

### 3. Form URL Encoded (e.g., legacy login or OAuth)

```kotlin
interface AuthApi {
    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse
}
```

### 4. Streaming Downloads (large files)

```kotlin
interface FileApi {
    @Streaming
    @GET("files/{id}")
    suspend fun downloadFile(@Path("id") id: String): Response<ResponseBody>
}

suspend fun saveFile(api: FileApi, id: String, dest: File) {
    val response = api.downloadFile(id)
    if (!response.isSuccessful) error("Download failed: ${response.code()}")

    response.body()?.byteStream().use { input ->
        dest.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
}
```

### 5. Headers 

---

## Common Pitfalls

### 1. Not handling errors consistently

- Throwing raw `HttpException`, `IOException`, or generic `Exception` everywhere.
- Mixing **HTTP‑level errors** (4xx/5xx) with **network‑level errors** (timeouts, no connectivity).

Better:
- Wrap Retrofit calls in a helper that returns a **sealed Result type** or your own `NetworkResult`.
- Map exceptions and error bodies to domain‑specific errors.

### 2. Leaking Retrofit/OkHttp concerns into UI

- Passing `Response<T>` or `Call<T>` all the way to Activities/Fragments.
- UI needing to know about HTTP codes and error parsing.

Better:
- Keep Retrofit in a **data layer (repository)**.
- Expose domain models and UI‑friendly error types to ViewModels/UI.

### 3. Using `Call<T>` everywhere instead of coroutines

- `Call<T>` is fine, but in Kotlin it’s much cleaner to use `suspend` functions.
- Mixing callbacks and coroutines leads to messy code and harder error handling.

Better:
- Use `suspend` APIs in your Retrofit interfaces.
- Wrap them in `try/catch` and expose clean states to the UI.

### 4. Misusing `@FormUrlEncoded` / `@Multipart`

- Combining `@Body` with `@FormUrlEncoded` or `@Multipart` incorrectly.
- Forgetting to use `@Field`/`@Part` with the right content type.

Better:
- For JSON body: just `@Body` with a converter.
- For forms: `@FormUrlEncoded` + `@Field`.
- For file uploads: `@Multipart` + `@Part` with `MultipartBody.Part`.

### 5. Blocking the main thread

- Calling `execute()` on `Call<T>` on the main thread.
- Doing heavy response processing on the UI thread.

Better:
- Use `suspend` + coroutines with `Dispatchers.IO` for network and parsing.
- Update UI state on the main thread only via ViewModel/Compose state.

### 6. Not configuring timeouts / interceptors

- Relying on defaults for timeouts in production.
- Not using OkHttp interceptors for auth/logging/retries.

Better:
- Configure `connectTimeout`, `readTimeout`, `writeTimeout` on `OkHttpClient`.
- Use interceptors for:
  - Auth headers.
  - Secure logging (redacted).
  - Retry/idempotency.
  - Error mapping.

---

## Interview Questions

### 1. How would you explain Retrofit to a non‑Android engineer?

**High-Level**
- It’s a library that lets you define REST APIs as Kotlin interfaces, and it turns HTTP responses into typed models.

**Talking Points**
- Uses annotations (`@GET`, `@POST`, `@Body`, `@Query`) to describe the API.
- Uses converters (Moshi/Gson) to serialize/deserialize JSON.
- Uses OkHttp behind the scenes for actual HTTP.

**Succinct Answer**
> Retrofit is a type‑safe HTTP client for Android. I define an interface with annotations like `@GET("users/{id}")`, and Retrofit generates the implementation that performs the network call and parses the JSON into my `User` data class using Moshi or Gson.

---

### 2. How do you handle errors with Retrofit and coroutines?

**High-Level**
- Wrap `suspend` calls in `try/catch` and map exceptions to domain errors.

**Talking Points**
- Catch `HttpException` for non‑2xx.
- Catch `IOException` for network issues.
- Parse error bodies when available.
- Expose a sealed `Result` or `NetworkResult` to the UI.

**Succinct Answer**
> With suspend functions I wrap Retrofit calls in `try/catch`. `HttpException` tells me about HTTP errors like 400/500, and `IOException` tells me about network issues like timeouts. I parse any error body into an `ApiError` model and wrap everything in a sealed `Result` type so my ViewModel can render success, validation errors, or retryable network failures in a consistent way.

---

### 3. When would you use `@Multipart` vs `@FormUrlEncoded` vs `@Body`?

**High-Level**
- `@Body` for JSON, `@FormUrlEncoded` for form posts, `@Multipart` for files.

**Talking Points**
- `@Body` → single object, often JSON, with converter factory.
- `@FormUrlEncoded` + `@Field` → traditional form posts.
- `@Multipart` + `@Part` → file uploads and mixed binary/text.

**Succinct Answer**
> I use `@Body` when I’m sending a JSON object, `@FormUrlEncoded` and `@Field` when the server expects classic form parameters, and `@Multipart` with `@Part` when I need to upload files or a mix of binary and text parts.

---

### 4. How do Retrofit and OkHttp relate, and where do interceptors fit in?

**High-Level**
- Retrofit sits on top of OkHttp. OkHttp does the HTTP; Retrofit handles marshalling.

**Talking Points**
- Retrofit uses an `OkHttpClient` under the hood.
- You configure interceptors on that client for auth, logging, retries, pinning.
- Retrofit interfaces remain clean, focused on the API surface.

**Succinct Answer**
> Retrofit delegates all HTTP work to OkHttp. I configure an `OkHttpClient` with interceptors for things like auth headers, SSL pinning, logging, and retry logic, then give that client to Retrofit. The Retrofit interface stays clean and just describes the API; interceptors handle the cross‑cutting concerns.

---

## Examples

### Basic Retrofit setup with OkHttp and Moshi

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    // timeouts, certificate pinning, retry, etc.
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val api = retrofit.create(MyApi::class.java)
```

### Helper for safe API calls

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val error: ApiError?) : NetworkResult<Nothing>()
    data class NetworkError(val exception: IOException) : NetworkResult<Nothing>()
    data class UnknownError(val throwable: Throwable) : NetworkResult<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e: HttpException) {
        val code = e.code()
        val errorBody = e.response()?.errorBody()?.string()
        val apiError = errorBody?.let { parseApiError(it) }
        NetworkResult.HttpError(code, apiError)
    } catch (e: IOException) {
        NetworkResult.NetworkError(e)
    } catch (e: Throwable) {
        NetworkResult.UnknownError(e)
    }
}
```

Use in a repository:

```kotlin
class UserRepository(private val api: UserService) {
    suspend fun getUser(id: String): NetworkResult<User> = safeApiCall {
        api.getUser(id)
    }
}
```

> Interview takeaway: 
> Know how Retrofit sits on top of OkHttp, 
> how annotations map to HTTP requests, 
> how to handle errors with coroutines, 
> when to use the different body annotations (`@Body`, `@FormUrlEncoded`, `@Multipart`), 
> and how to integrate interceptors on the OkHttp client. 
> That’s usually enough to demonstrate senior‑level understanding.
