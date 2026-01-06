# Resources:
- [OkHttp Interceptors – Docs](https://square.github.io/okhttp/interceptors/)
- [OkHttp Recipes – Square](https://square.github.io/okhttp/recipes/)
- [Retrofit Docs – Square](https://square.github.io/retrofit/)

---

# Interceptors

## Purpose

In Android networking, **interceptors** are hooks that let you **observe, modify, or short‑circuit** HTTP requests and responses.

For a senior Android dev, interceptors are your primary tools for:
- Centralizing **cross‑cutting concerns** (auth headers, logging, retries, metrics) instead of duplicating logic in every API call.
- Enforcing **security policies** (TLS, SSL pinning, redacting logs) in one place.
- Implementing **idempotent retries** and consistent error handling for all network requests.

On Android the most important implementation is **OkHttp interceptors**, often used under the hood by **Retrofit**.

> Mental model: Interceptors are like middleware sitting between your app and the network stack. They see every request/response and can modify, retry, or reject them.

---

## Types of Interceptors (OkHttp)

OkHttp defines two main categories:

1. **Application interceptors** (`addInterceptor`)  
   - Run **before** network I/O.
   - See **all** requests, including those served from cache.
   - Appropriate for:
     - Auth headers.
     - Logging.
     - Idempotency keys.
     - Request/response transformations.

2. **Network interceptors** (`addNetworkInterceptor`)  
   - Run **around actual network calls**.
   - See data as it goes over the wire (after caching, redirects, retries).
   - Appropriate for:
     - Low‑level inspection of network traffic.
     - Enforcing TLS versions / blocking cleartext.
     - Modifying headers that must be applied *only* on network requests.

Related but separate concepts:

- **Authenticator (`Authenticator`)**
  - Special hook for handling **401 Unauthorized** responses.
  - Typically used to **refresh tokens** and retry the original request.

- **Certificate pinning (`CertificatePinner`)**
  - Configured on the `OkHttpClient`, not an interceptor itself.
  - Used to pin server certificates/public keys to mitigate MITM attacks.

---

## Common Use Cases

### 1. Authentication & Common Headers (Application Interceptor)

- Add `Authorization` bearer tokens to every request.
- Inject app version, locale, device info, correlation IDs.

```kotlin
val authInterceptor = Interceptor { chain ->
    val original = chain.request()

    val newRequest = original.newBuilder()
        .header("Authorization", "Bearer ${tokenProvider.accessToken}")
        .header("X-App-Version", BuildConfig.VERSION_NAME)
        .header("X-Device-Id", deviceIdProvider.deviceId)
        .build()

    chain.proceed(newRequest)
}
```

### 2. Secure Logging (Application Interceptor)

- Log requests/responses for debugging **without leaking PII or secrets**.
- Typical pattern: `HttpLoggingInterceptor` with a custom logger that redacts.

```kotlin
val loggingInterceptor = HttpLoggingInterceptor { rawMessage ->
    val message = redactSensitive(rawMessage)
    Log.d("SecureHttp", message)
}.apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.BASIC
    }
}
```

Where `redactSensitive` might:
- Strip or mask `Authorization`, `Set-Cookie`, and any PII-ish fields.
- Truncate large bodies.

### 3. Retry & Idempotency (Application Interceptor)

- Implement **exponential backoff** for transient network errors.
- Only retry **idempotent** operations (e.g., GET/PUT, or POST with idempotency key).

```kotlin
val retryInterceptor = Interceptor { chain ->
    var attempt = 0
    val maxRetries = 3
    var request = chain.request()

    // Optionally mark idempotent requests explicitly via a header or tag
    val isIdempotent = request.method == "GET" ||
        request.header("X-Idempotent") == "true"

    while (true) {
        try {
            return@Interceptor chain.proceed(request)
        } catch (e: IOException) {
            if (!isIdempotent || ++attempt > maxRetries) throw e
            Thread.sleep(500L * attempt) // simple backoff
        }
    }
}
```

- You can also set an **Idempotency-Key** header per operation:

```kotlin
val idempotencyInterceptor = Interceptor { chain ->
    val original = chain.request()
    if (original.method == "POST") {
        val key = UUID.randomUUID().toString()
        val newRequest = original.newBuilder()
            .header("Idempotency-Key", key)
            .build()
        chain.proceed(newRequest)
    } else {
        chain.proceed(original)
    }
}
```

### 4. Error Mapping (Application Interceptor)

- Turn raw HTTP/JSON errors into **domain-specific exceptions** or error models.

```kotlin
val errorMappingInterceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())

    if (response.isSuccessful) return@Interceptor response

    // Example: map 4xx/5xx to custom exceptions
    val bodyString = response.body?.string().orEmpty()
    val apiError = parseApiError(bodyString) // your JSON error model

    throw when (response.code) {
        400 -> BadRequestException(apiError)
        401 -> UnauthorizedException(apiError)
        403 -> ForbiddenException(apiError)
        404 -> NotFoundException(apiError)
        in 500..599 -> ServerErrorException(apiError)
        else -> ApiException(apiError)
    }
}
```

In a ViewModel or repository you can catch these exceptions and expose UI‑friendly states.

### 5. Token Refresh (Authenticator)

- Use `Authenticator` (not a regular interceptor) to handle **401 Unauthorized**:

```kotlin
class TokenRefreshAuthenticator(
    private val tokenProvider: TokenProvider
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops
        if (responseCount(response) >= 2) return null

        synchronized(tokenProvider) {
            val newToken = tokenProvider.refreshToken() ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
```

> Interview note: `Authenticator` is **specifically** for 401 handling and runs only after a response is received, while interceptors can run on every request regardless of status.

### 6. TLS / SSL Pinning (Client Config + Network Interceptors)

- Use `CertificatePinner` on the client to pin the server’s certificate or public key:

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.yourbank.com", "sha256/BASE64_PIN_HERE")
    .build()
```

- Optionally add a **network interceptor** to assert TLS version / block cleartext:

```kotlin
val securityNetworkInterceptor = Interceptor { chain ->
    val request = chain.request()
    require(request.isHttps) { "Cleartext HTTP is not allowed" }
    chain.proceed(request)
}
```

---

## Common Pitfalls

- **Doing everything in interceptors**
  - Interceptors are powerful, but not all logic belongs there. Keep business logic in repositories/use‑cases, and use interceptors only for cross‑cutting networking concerns.

- **Logging sensitive data**
  - Using `HttpLoggingInterceptor` at `BODY` level in production can leak tokens, PII, and card data.
  - Always:
    - Restrict verbose logging to debug builds.
    - Redact/strip sensitive headers and bodies.

- **Infinite retry / token refresh loops**
  - Failing to cap retries in an `Authenticator` or retry interceptor can cause loops and hammer the backend.
  - Always track prior responses and attempts; bail out after a small number of retries.

- **Retrying non‑idempotent operations**
  - Blindly retrying POSTs that create resources can result in duplicate side effects.
  - Use idempotency keys and server‑side support before retrying anything that changes state.

- **Misusing application vs network interceptors**
  - Application interceptors see cached responses; network interceptors don’t.
  - Choose the right type based on whether you care about **all responses** or **only those that hit the network**.

- **Not handling timeouts and cancellations**
  - Ignoring `IOException` or cancellation in interceptors can surface as confusing errors.
  - Make sure to distinguish between user cancellation, network timeouts, and server errors.

---

## Tools & Libraries

### OkHttp Interceptors
- **OkHttp** is the foundation of most modern Android HTTP stacks (including Retrofit).
- Retrofit delegates all HTTP work to an underlying `OkHttpClient`, which is where you configure interceptors.

Pros:
- Battle‑tested, widely used, great docs.
- Interceptors give full control over request/response.
- Plays well with **Retrofit**, **Moshi/Gson**, and coroutines.

Cons:
- Easy to overuse: stuffing too much logic into interceptors can make debugging harder.
- Incorrect interceptor design (especially retries and token refresh) can lead to subtle bugs.

Examples:
```kotlin
val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    val newRequest = original.newBuilder()
        .header("Authorization", "Bearer ${tokenProvider.accessToken}")
        .header("X-App-Version", BuildConfig.VERSION_NAME)
        .build()
    chain.proceed(newRequest)
}

val loggingInterceptor = HttpLoggingInterceptor { message ->
    // Redact tokens / PII here
    Log.d("SecureHttp", message)
}.apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.BASIC
    }
}

val retryInterceptor = Interceptor { chain ->
    var attempt = 0
    val maxRetries = 3
    var request = chain.request()

    while (true) {
        try {
            return@Interceptor chain.proceed(request)
        } catch (e: IOException) {
            if (++attempt > maxRetries || request.method != "GET") throw e
            Thread.sleep(500L * attempt) // simple backoff
        }
    }
}

val client = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .addInterceptor(retryInterceptor)
    .certificatePinner(
        CertificatePinner.Builder()
            .add("api.yourbank.com", "sha256/BASE64_PIN_HERE")
            .build()
    )
    .authenticator(TokenRefreshAuthenticator(tokenProvider))
    .build()

// --------------------------------------------------------
// Authenticator skeleton
class TokenRefreshAuthenticator(
    private val tokenProvider: TokenProvider
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null // give up

        synchronized(tokenProvider) {
            val newToken = tokenProvider.refreshToken() ?: return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
```

> Interview takeaway: Know the difference between application vs network interceptors, when to use an Authenticator, and how to use interceptors for auth, logging, retries, error mapping, and security (pinning). That’s usually enough to demonstrate senior‑level understanding.
