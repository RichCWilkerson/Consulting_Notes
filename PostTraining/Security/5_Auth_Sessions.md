# Android Security: Authentication & Sessions

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`3_Network.md`](./3_Network.md) (token transport, TLS)
- [`4_Storage_Secrets.md`](./4_Storage_Secrets.md) (token storage)
- [`7_WebView_UntrustedContent.md`](./7_WebView_UntrustedContent.md) (don’t do auth in WebView)

---

## Why you care (what goes wrong)
Auth/session bugs are often “one bug away” from account takeover:
- refresh tokens stored insecurely → attacker replays sessions
- long-lived access tokens → loss = long-lived compromise
- “logout” only clears UI, not tokens/caches
- client trusts token claims for authorization (“admin=true”) without server enforcement

Mindset:
- the **server** enforces authorization
- the **client** protects tokens, handles UX, and provides risk signals

---

## Token vocabulary (use consistent language)
- **Access token**: sent on API requests; should be short-lived.
- **Refresh token**: used to obtain new access tokens; higher value; rotate and protect strongly.
- **Session ID**: server-stored session reference; conceptually similar to an access token.
- **ID token (OIDC)**: identifies the user to the client; not typically used for API authorization.

Rule of thumb:
- treat tokens as **opaque** in the client. Don’t rely on parsing JWTs for business logic.

---

## Baseline defaults (modern standard)

### 1) Use OAuth2 Authorization Code + PKCE (or a managed provider)
- For mobile, the modern standard is **Authorization Code** with **PKCE**.
- Avoid implicit flows.
- Never ship a `client_secret` in an app.

Implementation guidance:
- Prefer audited, standard libraries and system browser-based flows.
- Prefer Custom Tabs / browser UX over embedded WebViews.

### 2) Short-lived access tokens + robust refresh
- Access tokens: short-lived (minutes is common).
- Refresh tokens: rotate regularly; treat as a long-lived credential.

Refresh behavior (typical):
- on 401/expired token → attempt refresh
- if refresh succeeds → retry original request once
- if refresh fails → clear session state + send user to login

### 3) Secure logout
Logout typically means:
- clear tokens from memory
- clear encrypted persisted token state
- clear sensitive caches
- revoke session server-side when supported

### 4) Step-up auth for sensitive actions
Even logged-in users may need to re-authenticate to:
- change password/email
- view or export sensitive documents
- perform financial actions

Options:
- “recent login” checks
- biometric confirmation via `BiometricPrompt`
- re-enter password / passkey verification

---

## Android-auth implementation patterns (practical)

### Browser-based auth (recommended)
Benefits:
- reduces phishing risk vs embedded controls
- leverages system cookies/SSO
- inherits secure browser updates

Rules:
- don’t intercept credentials in the app
- keep your redirect URI handling strict and validated

### Credential Manager / passkeys (high level)
Credential Manager improves UX and security by encouraging:
- passkeys
- managed password flows
- federated identity

From an app-security perspective:
- it reduces credential handling and encourages safer primitives
- it doesn’t replace the need for secure session/token handling

---

## Example: Retrofit/OkHttp session handling (production-shaped)

This example is intentionally UI-free and focuses on:
- attaching the **access token**
- refreshing on 401 using a **single-flight** refresh
- safe failure behavior (refresh fails → clear session and force re-auth)

Why this is a good example for this doc:
- It shows the *actual* places where Android teams accidentally leak tokens (interceptors/loggers) and where session bugs occur (refresh concurrency, infinite loops, unsafe fallbacks).
- It illustrates the client’s responsibilities clearly: attach credentials, refresh safely, clear on failure.

> Notes:
> - Keep the real `TokenStore` encrypted and backed by Keystore-managed keys (see [`4_Storage_Secrets.md`](./4_Storage_Secrets.md)).
> - OkHttp interceptors and authenticators are not `suspend`. A common pattern is:
>   - `SessionManager` does the suspending work (storage + refresh)
>   - a thread-safe in-memory `AccessTokenProvider` is used by the interceptor

### Token models

```kotlin
/** Treat tokens as opaque. Don’t parse JWTs for authorization decisions on the client. */
data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)
```

### Token storage abstraction

```kotlin
interface TokenStore {
    suspend fun getTokens(): Tokens?
    suspend fun setTokens(tokens: Tokens)
    suspend fun clear()
}

/**
 * Real implementation should use encrypted storage.
 * Keeping it abstract lets you swap EncryptedSharedPreferences vs Encrypted DataStore.
 */
```

### API for refreshing

```kotlin
interface AuthApi {
    // Example shape; your backend may differ.
    suspend fun refresh(refreshToken: String): Tokens
}
```

### SessionManager (single-flight refresh)

```kotlin
class SessionManager(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) {
    private val refreshMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun currentTokensOrNull(): Tokens? = tokenStore.getTokens()

    /**
     * Refresh tokens exactly once even if multiple requests get 401 concurrently.
     *
     * Security properties:
     * - Prevents refresh storms (availability + rate limiting).
     * - Ensures all callers converge on the same new token set.
     * - Centralizes refresh failure behavior (clear session → re-auth).
     */
    suspend fun refreshTokensSingleFlight(): Tokens? = refreshMutex.withLock {
        val current = tokenStore.getTokens() ?: return null

        return try {
            val newTokens = authApi.refresh(current.refreshToken)
            tokenStore.setTokens(newTokens)
            newTokens
        } catch (t: Throwable) {
            // Safe failure mode: don’t keep retrying forever with bad tokens.
            tokenStore.clear()
            null
        }
    }

    suspend fun logout() {
        // Optional: call server revoke endpoint first.
        tokenStore.clear()
        // Also clear sensitive caches elsewhere (DB, in-memory state).
    }
}
```

### AccessTokenProvider (for non-suspending interceptors)

```kotlin
class AccessTokenProvider {
    private val accessTokenRef = java.util.concurrent.atomic.AtomicReference<String?>(null)

    fun get(): String? = accessTokenRef.get()

    /**
     * Call this whenever tokens change (login, refresh, logout).
     * This keeps network-layer reads fast and avoids blocking in interceptors.
     */
    fun update(tokens: Tokens?) {
        accessTokenRef.set(tokens?.accessToken)
    }

    fun clear() {
        accessTokenRef.set(null)
    }
}
```

### OkHttp: attach access token

```kotlin
class AccessTokenInterceptor(
    private val tokenProvider: AccessTokenProvider,
) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val token = tokenProvider.get()

        if (token.isNullOrBlank()) return chain.proceed(request)

        val authed = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authed)
    }
}
```

### OkHttp: refresh on 401 using Authenticator

```kotlin
class TokenRefreshAuthenticator(
    private val sessionManager: SessionManager,
    private val tokenProvider: AccessTokenProvider,
) : okhttp3.Authenticator {

    override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
        // Avoid infinite retry loops.
        if (responseCount(response) >= 2) return null

        val newTokens = kotlinx.coroutines.runBlocking { sessionManager.refreshTokensSingleFlight() } ?: return null
        tokenProvider.update(newTokens)

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: okhttp3.Response): Int {
        var r: okhttp3.Response? = response
        var result = 1
        while (r?.priorResponse != null) {
            result++
            r = r.priorResponse
        }
        return result
    }
}
```

### Retrofit wiring (high level)

```kotlin
fun createAuthedOkHttpClient(
    accessTokenInterceptor: AccessTokenInterceptor,
    authenticator: TokenRefreshAuthenticator,
): okhttp3.OkHttpClient = okhttp3.OkHttpClient.Builder()
    .addInterceptor(accessTokenInterceptor)
    .authenticator(authenticator)
    .build()

fun createRetrofit(okHttpClient: okhttp3.OkHttpClient): retrofit2.Retrofit = retrofit2.Retrofit.Builder()
    .baseUrl("https://example.com/")
    .client(okHttpClient)
    // converters, call adapters...
    .build()
```

### Where to put this in a real app
A common “senior-friendly” organization:
- `:core:session` (or similar feature module)
  - `SessionManager`, `TokenStore`, logout/clear rules
- `:core:network`
  - OkHttp client creation + interceptors/authenticator

This keeps token handling centralized and makes it harder for feature teams to “just store the token somewhere.”

---

## Session lifecycle concerns seniors should cover

### Multi-device and revocation
- Assume users sign in on multiple devices.
- Provide server-side revocation (sign out all devices, revoke refresh tokens).
- Handle “token revoked” responses in the client predictably.

### “New device” and restore scenarios
- Backup/restore can move app data to a new device.
- Treat restored sessions as suspicious:
  - re-auth
  - rotate tokens
  - require step-up for high-risk actions

### Concurrency (the refresh storm problem)
In real apps, multiple requests can fail at once.
- Ensure only one refresh happens at a time.
- Queue/await refresh, then replay requests safely.

---

## Common footguns
- **Using ID token for API auth.** ID token identifies the user to the client; access token authorizes API calls.
- **Long-lived access tokens.** They turn every leak into a long incident.
- **Storing refresh tokens insecurely.** The most common high-impact mistake.
- **JWT parsing for authorization.** Client-side claim checks can be bypassed.

---

## Interview practice questions (with strong answers)

1) Explain “access token vs refresh token vs session ID vs ID token” in 60 seconds.
> Access tokens authorize API calls and should expire quickly. Refresh tokens mint new access tokens and are higher value, so they need stronger protection and rotation. A session ID is usually a server-stored session handle that behaves like an access token from the client perspective. An ID token (OIDC) is for identifying the user to the client and isn’t typically what you send to your APIs for authorization.

2) Walk me through what your app does when it receives a 401.
> First, we treat 401 as “access token invalid/expired.” The client triggers a single refresh attempt via our session manager (single-flight to avoid storms). If refresh succeeds, we retry the original request once with the new access token. If refresh fails, we clear local session state and route the user to login so we don’t loop or keep using a compromised/expired refresh token.

3) What’s a token refresh storm? How do you prevent it?
> A refresh storm happens when many concurrent requests fail and all attempt refresh simultaneously, causing rate-limits/outages and inconsistent token state. We prevent it by single-flight refresh (a mutex/lock or shared in-flight job) so only one refresh occurs at a time, and all callers reuse the resulting tokens.

4) What should happen on refresh failure?
> We fail closed: clear stored tokens and sensitive caches and require re-auth. Otherwise you risk infinite loops, repeated refresh attempts, or using inconsistent/partial auth state.

5) Why shouldn’t the client parse JWTs to make authorization decisions?
> Because the client is not a trust boundary. Even if the token is signed, an attacker can bypass client checks by instrumenting the app or replaying requests. Authorization decisions must be enforced server-side; the client should treat tokens as opaque and only use them to authenticate API calls.

6) How do you implement step-up auth for sensitive actions on Android?
> We treat it as a product + security requirement: sensitive actions require recent verification. On Android we can use `BiometricPrompt` as a UX gate and/or re-auth with the identity provider. The key point is the backend still enforces authorization; step-up is an extra layer to reduce risk for high-value operations.

---

## PR / review checklist (Auth & Sessions)
- [ ] Auth uses OAuth2 Authorization Code + PKCE (or vetted managed provider).
- [ ] No `client_secret` is shipped in the app.
- [ ] Access tokens are short-lived; refresh tokens are rotated and protected.
- [ ] Refresh is robust (single-flight; retry-once strategy; safe failure mode).
- [ ] Logout clears persisted secrets and sensitive caches; server revocation where possible.
- [ ] Sensitive actions require step-up auth.
- [ ] No auth tokens/PII appear in logs, analytics, crash reports.

---

## Notes for seniors (how to talk about “mobile auth security”)
- Emphasize that mobile security is a system: transport (TLS), storage (encryption + backups), session rotation, and server-side enforcement.
- Be explicit about what the client can’t guarantee (rooted devices, reverse engineering) and what mitigations are still worthwhile (short lifetimes, rotation, anomaly detection).
