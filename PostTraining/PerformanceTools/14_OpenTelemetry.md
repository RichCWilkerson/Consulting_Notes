# Resources:
- [Medium](https://horovits.medium.com/observability-for-mobile-with-opentelemetry-2eb847c41941)
- [Medium](https://medium.com/lumigo/exploring-the-new-frontiers-of-opentelemetry-android-b55979097538)
  - 
- [Medium - Ktor Observability](https://medium.com/@shinyDiscoBall/unlocking-observability-2-0-with-ktor-a-practical-guide-to-opentelemetry-integration-97fdda156fda)
  - more focused on the server side


# Telemetry

3 pillars of telemetry: **logs**, **metrics**, and **traces**.
- Each pillar is a zoom in of a previous one allowing you to drill down and gain more information about the context you were looking at before.
1. **Metrics**: numerical values that represent data over time (e.g., CPU usage, request count).
  - Throughput, latency, error or success rates are metrics you would collect to gain basic understanding of performance of an HTTP based service
    - Any spikes in latency could be an indication of your system working under stress or simply a regression that was introduced with the latest release.
  - As a developer you are in charge of defining system level indicators and nothing stops you from tracking the booking requests, cart checkouts, user logins, etc. as your business KPIs.
2. **Traces**: give you more information about the runtime. is a collection of hierarchically arranged spans that measure execution time between two points in your code.
  - Call to a database or external service, cache refresh, or some complex calculation are good examples of spans your application should be collecting
  - also support context propagation which means you can gain insights into performance of an external system that a different team in your organisation operates.
    - You can propagate headers to your message consumers and measure the latency between publishing and delivery
  - because they are snapshots in time of a single operation, they can be aggregated and drawn on a graph
    - Now you can directly drill down from these graphs into the traces and gather even more context of what happened under the hood of your application.
3. **Logs**: allow you to capture human readable and context rich information that is beyond the simple span attributes.
  - have mostly dynamic structure and do not adhere to any semantic standards
  - Logs can store anything, from request parameters, all the way to processed entities attributes, results of calculations etc.
    - difference between a span and a log lies mostly in the purpose
      - Spans are mostly used to understand the performance, latency, and the dependencies between the services
      - Logs can be used to capture application state and behaviour over time.
    - Logs might be helpful to understand why one span was slower than the other. If you log information like the amount of records processed, comparing them with the span length might reveal some potential bottlenecks, e.g. show you that your application scales exponentially.

OpenTelemetry unifies these three pillars under one roof and calls them [signals](https://opentelemetry.io/docs/concepts/signals/)
- The advantage? Instead of finding a provider for each of the pillars and doing the correlation of events yourself, you get all the tools from one service.
- it is vendor agnostic, meaning if you decide to host it yourself, you are free to do so.

## Overview
- **Telemetry** = collecting measurements about your app **in production**:
    - Usage (screens, features, funnels).
    - Reliability (crashes, ANRs, error rates).
    - Performance (cold start, screen render times, network latency).
- Used for:
    - Detecting regressions after releases.
    - Prioritizing work (what users actually use / where they struggle).
    - Capacity planning and backend monitoring.
- As a **senior Android dev**, you should:
    - Design **what** to measure, not just “send logs everywhere”.
    - Understand **client vs server** responsibilities.
    - Ensure telemetry is **privacy‑aware** and **configurable** (feature flags, sampling).
    - Use telemetry data to drive decisions (rollbacks, refactors, performance work).

---

## Technologies

You rarely build telemetry from scratch; you integrate SDKs and sometimes add a thin abstraction.
- technologies to consider for android telemetry:
    - **Firebase** (Analytics, Crashlytics, Performance Monitoring).
    - **Sentry** (crash reporting, performance monitoring).
    - **Datadog** (RUM, logs, traces, metrics).
    - **Amplitude / Mixpanel / Segment** (product analytics).
    - **New Relic** (APM, RUM).
    - **Bugsnag** (crash reporting).
    - **OpenTelemetry** (open standard for traces, metrics, logs).

Common categories:

1. **Crash & error reporting**
    - Firebase Crashlytics, Sentry, Datadog, Bugsnag.
    - Uncaught exceptions, ANRs, non‑fatal errors.

2. **Analytics / product telemetry**
    - Firebase Analytics, Amplitude, Mixpanel, Segment, custom event pipelines.
    - Screen views, events, funnels, A/B experiments.

3. **Performance monitoring**
    - Firebase Performance Monitoring, Datadog RUM, New Relic, custom traces.
    - Startup time, network latency, slow screens, jank.

4. **Logging & tracing**
    - Structured logs via Timber, custom logger, Datadog logs, Stackdriver, etc.
    - Distributed tracing using **OpenTelemetry** / proprietary tracing SDKs.

5. **Backend observability (out of app scope but important)**
    - Prometheus, Grafana, Datadog, New Relic, etc.
    - As a client dev, you care how your telemetry correlates with server metrics.

For interviews, you don’t need to be an expert in each vendor, but you should:
- Be able to describe **what you’d instrument**.
- Explain how you’d structure events and measure success/failure.

---

## Steps to implement Telemetry in Android

Think in layers: **events & metrics design → client implementation → transport → analysis**.

### 1. Define goals and signals
- Start from **questions**, not tools:
    - What do we need to know? Login success rate? Checkout dropout? API error rates? Startup performance?
    - What decisions will this data drive? Rollback, refactor, UX changes, capacity planning.
- Define **key metrics**:
    - Example product KPIs: conversion, retention, feature adoption.
    - Example reliability KPIs: crash‑free sessions, error rate per endpoint.
    - Example performance KPIs: p95 cold start, p95 screen load, p95 API latency.
        - TODO: is p95 the most important/impactful metric to track?

### 2. Design an event schema
- Design **event names** and **properties**:
    - `auth_login_attempt`, properties: `method`, `result`, `error_code`.
    - `checkout_started`, `checkout_completed`, properties: `cart_size`, `has_promo`, `payment_method`.
- Standardize:
    - Naming conventions (snake_case / camelCase, verbs vs nouns).
    - Required vs optional properties.
    - Versioning (e.g., `schema_version`).

### 3. Implement a telemetry/analytics abstraction
- Avoid sprinkling SDK calls everywhere:
    - Create an interface, e.g., `Telemetry`, `AnalyticsTracker`, `EventLogger`.
```kotlin
interface Telemetry {
    fun logEvent(name: String, properties: Map<String, Any?> = emptyMap())
    fun logError(name: String, throwable: Throwable? = null, properties: Map<String, Any?> = emptyMap())
    fun setUserId(userId: String?)
}
```
- Production implementation uses chosen SDK(s):
```kotlin
class FirebaseTelemetry @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : Telemetry {

    override fun logEvent(name: String, properties: Map<String, Any?>) {
        val bundle = Bundle().apply {
            properties.forEach { (k, v) ->
                when (v) {
                    is String -> putString(k, v)
                    is Int -> putInt(k, v)
                    is Long -> putLong(k, v)
                    is Double -> putDouble(k, v)
                    is Boolean -> putBoolean(k, v)
                    // is this exhaustive enough? or is else required?
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun logError(name: String, throwable: Throwable?, properties: Map<String, Any?>) {
        // Forward to Crashlytics or logging backend
        val t = throwable ?: Exception(name)
        FirebaseCrashlytics.getInstance().recordException(t)
        // Optionally log as event too
        logEvent("error_$name", properties)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
        FirebaseCrashlytics.getInstance().setUserId(userId ?: "")
    }
}
```

### 4. Inject telemetry into your app layers
- Use DI (Hilt/Dagger/Koin) to inject `Telemetry` into:
    - ViewModels.
    - Use cases.
    - Occasionally Activities/Fragments/Composables (prefer going through ViewModel).

```kotlin
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val telemetry: Telemetry,
    // other deps
) : ViewModel() {

    fun onCheckoutStarted(cartSize: Int) {
        telemetry.logEvent(
            name = "checkout_started",
            properties = mapOf("cart_size" to cartSize)
            // to is a Kotlin keyword used for named parameters
            // TODO: to keyword is equivalent to what?
        )
    }
}
```

### 5. Implement performance and trace telemetry
- Use performance SDK or custom timestamps:
    - Measure cold start, screen transitions, network calls.
- Example (manual timing):
```kotlin
suspend fun loadHomeScreen(): HomeUiState {
    val start = SystemClock.elapsedRealtime()
    return try {
        val data = repo.fetchHomeData()
        val duration = SystemClock.elapsedRealtime() - start
        telemetry.logEvent("home_load_success", mapOf("duration_ms" to duration))
        HomeUiState.Success(data)
    } catch (t: Throwable) {
        val duration = SystemClock.elapsedRealtime() - start
        telemetry.logError("home_load_error", t, mapOf("duration_ms" to duration))
        HomeUiState.Error
    }
}
```

### 6. Wire up crash & error reporting
- Initialize crash reporting SDK early (Application class).
- Add **non‑fatal** error reporting where you handle exceptions but still want visibility.
- Ensure PII handling & redaction are aligned with policy.

### 7. Configure sampling & environments
- Don’t send full telemetry from dev/local builds:
    - Tag environment: `dev`, `qa`, `prod`.
    - Possibly disable or route to separate projects for dev.
- Use **sampling** for high-volume events to control cost and noise.

### 8. Validate, monitor, iterate
- After releasing telemetry:
    - Validate events are coming in as expected (names, properties, cardinality).
    - Build dashboards for key metrics.
    - Adjust schemas if needed (with versioning).

---

## Example Implementation

High-level wiring using Hilt and a `Telemetry` abstraction:

```kotlin
// Telemetry interface (shared)
interface Telemetry {
    fun logEvent(name: String, properties: Map<String, Any?> = emptyMap())
    fun logError(name: String, throwable: Throwable? = null, properties: Map<String, Any?> = emptyMap())
}

// Prod implementation
@Singleton
class ProdTelemetry @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : Telemetry {

    override fun logEvent(name: String, properties: Map<String, Any?>) {
        val bundle = Bundle().apply {
            properties.forEach { (k, v) ->
                when (v) {
                    is String -> putString(k, v)
                    is Int -> putInt(k, v)
                    is Long -> putLong(k, v)
                    is Double -> putDouble(k, v)
                    is Boolean -> putBoolean(k, v)
                }
            }
        }
        analytics.logEvent(name, bundle)
    }

    override fun logError(name: String, throwable: Throwable?, properties: Map<String, Any?>) {
        val t = throwable ?: Exception(name)
        crashlytics.recordException(t)
        // Optionally forward as analytics event
        logEvent("error_$name", properties)
    }
}

// Hilt module
@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryModule {

    @Binds
    @Singleton
    abstract fun bindTelemetry(impl: ProdTelemetry): Telemetry
}

// Usage in ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val telemetry: Telemetry,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun onLoginClicked(username: String) {
        telemetry.logEvent("login_attempt", mapOf("username_present" to username.isNotBlank()))
        viewModelScope.launch {
            runCatching { authRepository.login(username) }
                .onSuccess {
                    telemetry.logEvent("login_success")
                }
                .onFailure { e ->
                    telemetry.logError("login_failure", e)
                }
        }
    }
}
```

For testing:
- Provide a **fake Telemetry** implementation that records events in memory so tests can assert on them.

```kotlin
class FakeTelemetry : Telemetry {
    val events = mutableListOf<Pair<String, Map<String, Any?>>>()
    val errors = mutableListOf<Pair<String, Throwable?>>()

    override fun logEvent(name: String, properties: Map<String, Any?>) {
        events += name to properties
    }

    override fun logError(name: String, throwable: Throwable?, properties: Map<String, Any?>) {
        errors += name to throwable
    }
}
```

---

## Common Pitfalls

1. **Event explosion / no schema**
    - Random event names and properties everywhere.
    - Hard to analyze; dashboards become noisy and inconsistent.

2. **Logging PII or secrets**
    - Sending emails, tokens, or raw payloads to telemetry backends.
    - Violates privacy and can break compliance (GDPR, PCI, etc.).

3. **Too much client-side logic in telemetry**
    - Complex branching logic around events makes code harder to maintain.
    - Prefer simple, well-defined events; do complex analysis server-side.

4. **No distinction between environments**
    - Dev and QA traffic polluting production dashboards.
    - Hard to tell whether anomalies are real.

5. **Not sampling high-volume events**
    - Logging every scroll or keystroke → huge bills and noisy data.

6. **Ignoring performance impact**
    - Synchronous logging on main thread.
    - Excessive disk writes or network calls in tight loops.

7. **Telemetry that no one uses**
    - Events that aren’t hooked up to dashboards or decisions.
    - Wasted effort and noise; make sure each event has a consumer.

---

## Best Practices

1. **Start from questions / decisions**
    - Design telemetry around what the team needs to know.
    - Tie each metric to a decision or alert.

2. **Centralize telemetry APIs**
    - Use a single abstraction (`Telemetry`, `Analytics`) rather than calling SDKs all over.
    - Makes it easier to swap vendors or adjust behavior.

3. **Use structured, versioned events**
    - Consistent naming and property keys.
    - Include `schema_version` if you expect evolution.

4. **Protect user privacy**
    - No secrets, tokens, raw payment data, or unnecessary PII.
    - Hash or bucket where needed (e.g., `age_range` instead of `age`).

5. **Separate dev/test/prod environments**
    - Different API keys or projects.
    - Tag events with environment.

6. **Use sampling thoughtfully**
    - Full fidelity on low-volume, high-value events (login, purchase).
    - Sample high-volume events where exact counts aren’t needed.

7. **Monitor health of telemetry itself**
    - Alert on drops in event volume (SDK misconfigured, app update broke logging).
    - Ensure changes in schemas are reflected in dashboards.

8. **Document your event schema**
    - Keep a simple doc or data contract describing events and properties.
    - Helps backend, data, and product teams consume your telemetry.

9. **Test telemetry flows**
    - Unit tests for critical event emission (e.g., login, checkout).
    - Sanity checks in QA/staging to verify events show up as expected.

---

## Interview Questions

1. **How would you design telemetry for a login + checkout flow?**
    - Hint: key events (`login_attempt`, `login_success/failure`, `checkout_started/completed`), error codes, durations, funnels.

2. **How do you avoid logging sensitive user data while still getting useful telemetry?**
    - Hint: avoid PII/secrets, use hashing or bucketing, follow company policies.

3. **What’s your approach to integrating analytics and crash reporting into an Android app?**
    - Hint: central abstraction, DI, early initialization, environment separation.

4. **How do you ensure telemetry doesn’t hurt app performance?**
    - Hint: async/batched sending, no heavy work on main thread, sampling, minimal payloads.

5. **Describe a time you used telemetry to debug or improve a feature.**
    - Hint: tie to a story (e.g., drop in conversion, spike in errors, performance regression).

6. **How would you test that important telemetry events are being sent correctly?**
    - Hint: fake Telemetry in unit tests, QA/staging verification, schema validation.

7. **How would you design telemetry for an automotive or offline-prone app?**
    - Hint: local buffering, retry logic, batching when connectivity is available, tagging events with environment and timestamps.


---

# OpenTelemetry
## Overview