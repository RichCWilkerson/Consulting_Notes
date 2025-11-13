
# Pitch

Hi, my name is Christian Wilkerson, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

In my current role,
1. I architected and implemented a scalable Android app using Clean Architecture and MVVM, breaking the codebase into feature-based Gradle modules for faster release cycles and easier maintenance.
3. To modernize the UI, I introduced Jetpack Compose, starting with a hybrid XML + Compose approach before fully migrating new features, which cut UI development time and improved design parity with Figma.
5. On the backend side, I implemented secure payment and authentication flows using tokenization, SSL pinning, and biometric verification, and
6. improved app performance by reducing load times by about 25% through lazy loading, image optimization with Coil, and background initialization.
7. I’ve also set up CI/CD pipelines using GitLab CI, ensuring smooth, automated testing and deployments across QA and production.
8. Along the way, I’ve mentored developers, conducted code reviews, and helped shape best practices across the team.

I’m deeply passionate about building scalable, secure, and user-centric apps that blend strong architecture with great design. As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name] would be a fantastic place to continue growing my career and contributing to meaningful innovation in mobile development.



## Additions
1. I also worked on AR-based virtual try-on and personalized shopping experiences, integrating ARCore SDK to enhance user engagement.
      → removed for now, was overwhelmed with amount of information and pitch went long → will add it back into pitch when I’m more comfortable with all the other topics and can then look into this tech
2. I also led the adoption of Kotlin Multiplatform Mobile (KMM) to share business logic and analytics across Android and iOS, collaborating closely with our offshore team in India.
      → tempted to change this to current prototyping and presenting use case for the team

## why migrate from xml to compose?
1. plays nicer with Kotlin libraries → tying coroutines to a layout, better integration with modern dev
2. less boilerplate → common/core components
3. previews
4. performance → compose only re-renders what changes
5. modern models → MVI, not sure it is possible with xml

---

# Cross-Examine

---

## 🧩 Architecture & Technical Depth
### Why did you choose Clean Architecture over other patterns like MVI or MVP?
- Be ready to discuss testability, separation of concerns, and scalability for large teams.
Tip: Tie it back to modularization and code ownership.

### Can you walk me through how your Gradle modules are structured and communicate?
- Explain “core”, “feature”, “data”, “domain” modules, and dependency directions.
- core: ui, theming, utils, domain models, networking
- feature: specific app features like the home, shop, designers, account, and shopping bag

### How do you manage dependency injection across modules?
- If you used Dagger/Hilt/Koin, discuss scope, component hierarchies, and avoiding circular dependencies.
- scope:
  - Use @Singleton / @InstallIn(SingletonComponent::class) for app-level singletons (network, DB, analytics).
  - Use @ActivityRetainedScoped, @ActivityScoped, @FragmentScoped, @ViewModelScoped for lifecycle-bound objects (UI controllers, per-flow caches, viewmodel-scoped helpers).
  - Prefer @InstallIn matching the lifecycle where the dependency is used to avoid leaking longer-lived objects into short-lived scopes.
- component hierarchies = TODO?
  - Hilt maps lifecycles: SingletonComponent → ActivityRetainedComponent → ActivityComponent → FragmentComponent / ViewModelComponent. Install modules into the component matching intended lifetime.
  - Put shared abstractions (interfaces, DTOs) in core module. Implementations live in feature modules and are bound to the interfaces via DI modules installed in the appropriate component.
  - Use @EntryPoint sparingly for cross-module access or expose factories from core to keep boundaries explicit.
  - Avoid circular dependencies by keeping modules focused and using interfaces in core.
    - features never depend on each other directly; they depend on core abstractions.
- modularization:
  - need to bind implementation to interface in core module via DI module

```kotlin
// core module
// kotlin
@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db").build()
}

// feature module
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureBindsModule {
    @Binds
    @Singleton
    abstract fun bindFeatureRepository(
        impl: FeatureRepositoryImpl
    ): FeatureRepository
}
// feature view model
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repo: FeatureRepository
) : ViewModel() {
    // viewmodel logic
}
```

### What were the key challenges of implementing modularization, and how did you handle shared resources or navigation between modules?
- Challenge: migrating to Clean Architecture while introducing Jetpack Compose meant rewriting UI and refactoring modules at the same time.
  Action: migrated incrementally — kept temporary per-feature Activities, used ComposeView/AndroidView interop, shared ViewModels and feature flags to isolate rollout. Opted for small, focused PRs and CI checks to avoid big-bang changes.
  Impact: allowed teams to ship UI changes safely while progressively converting screens to Compose with minimal regressions.
- Challenge: isolating core components and avoiding circular module dependencies.
  Action: inventory of shared code, moved only interfaces/DTOs/utilities into a core module, implemented features in feature modules and bound implementations via Hilt modules (or entry points when needed). Enforced Gradle dependency direction and used DI (Provider/Lazy) to break runtime cycles. For navigation, defined a Navigator interface in core implemented by the app module; features expose routes or deep links to avoid direct feature-to-feature deps.
  Impact: reduced coupling, improved build times and testability, and made ownership and boundaries explicit.

### How do you ensure your architecture supports offline-first or low-connectivity scenarios?
1. Core approach
   - Always read from the local DB first and drive UI from a Flow/LiveData backed by Room (single source of truth). Sync with network in the background.
2. Key components and patterns
   - Repository + NetworkBoundResource / RemoteMediator (Paging 3) to coordinate network <-> DB.
   - Room for local cache and conflict markers (pending/failed). Expose Flows to ViewModels.
   - WorkManager for reliable background sync (Constraints, uniqueWork, exponential backoff, retry).
   - Optimistic updates: write locally, mark pending, return UI update immediately, reconcile when server responds.
   - Connectivity handling: NetworkCallback / ConnectivityManager to trigger opportunistic syncs.
   - Conflict resolution: timestamps, server‑authoritative merge, or domain-specific merge rules.
3. Operational concerns
   - Security: encrypt sensitive data (EncryptedSharedPreferences or SQLCipher).
   - Observability: metrics and logging for sync success/failure.
   - Testing: unit + integration tests that simulate offline/online transitions.

---

## 🎨 Jetpack Compose & UI Modernization
### What challenges did you face integrating Jetpack Compose into an existing XML codebase?
- Common Issues:
  - State sync - keeping View-system state and Compose state consistent (ViewModels/Flows help).
  - Lifecycle & scopes - correct ViewModel / saved state owners when embedding.
  - Theming & styling - matching XML themes, typography, colors.
  - Input, focus, accessibility - keyboard, accessibility nodes, touch handling edge cases.
  - Performance & recomposition - avoid heavy work on Compose recompositions; optimize lists/paging.
  - Tooling / tests / CI - adding Compose increases build/test surface and requires new test patterns.
  - Module boundaries & DI - wiring Compose screens across modularized features can expose dependency issues.
- Discuss interoperability via ComposeView and AndroidView.
  - ComposeView is a View subclass that hosts Compose UI inside the traditional View hierarchy (useful in XML layouts or existing Activities/Fragments). It is not "just an XML layout" — it's a bridge that runs a Compose runtime inside a View.
  - AndroidView is a Compose API that embeds an existing Android View (e.g., WebView, custom view) inside a Compose tree. It lets Compose hosts reuse old Views.- chances we moved directly to Compose? And no intermediary ComposeView?
- Incremental (recommended): use ComposeView to add Compose screens to existing XML flows, convert per-screen/feature, keep ViewModel/repo layer unchanged. 
  - Pros: lower risk, smaller PRs, easier rollback.
```kotlin
// XML (placed in res/layout/some_layout.xml) - shown as comment for context:
// <androidx.compose.ui.platform.ComposeView
//     android:id="@+id/compose_root"
//     android:layout_width="match_parent"
//     android:layout_height="match_parent" />

// Activity hosting a ComposeView
class HostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.some_layout)

        val composeView: ComposeView = findViewById(R.id.compose_root)
        composeView.setContent {
            // Compose content inside existing XML/Activity
            MyComposeScreen(viewModel = obtainViewModel())
        }
    }
}

// Compose embedding an existing Android View
@Composable
fun WebHost(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    }, update = { webView ->
        if (webView.url != url) webView.loadUrl(url)
    })
}
```

### How did you handle state management across XML and Compose screens?
- expose a single source of truth as a StateFlow in a shared ViewModel. 
  - XML code collects the flow with lifecycleScope and Compose uses collectAsState(). 
  - Create the ViewModel at the Activity scope so both XML fragments and Compose hosted in that Activity share the same instance.

```kotlin
// activity
class HostActivity : AppCompatActivity() {
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        val composeView: ComposeView = findViewById(R.id.compose_root)
        composeView.setContent {
            val state by viewModel.uiState.collectAsState()
            MyComposeScreen(state = state, onChange = { viewModel.updateText(it) })
        }
    }
}
// fragment 
class XmlFragment : Fragment(R.layout.fragment_xml) {
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                view.findViewById<TextView>(R.id.textView).text = state.text
            }
        }

        view.findViewById<Button>(R.id.button).setOnClickListener {
            viewModel.updateText("Updated from XML")
        }
    }
}
// compose
@Composable
fun MyComposeScreen(state: UiState, onChange: (String) -> Unit) {
    Column {
        Text(text = state.text)
        Button(onClick = { onChange("Updated from Compose") }) {
            Text("Send")
        }
    }
}
```

### How did you ensure design fidelity between Figma and Compose?
- Theming strategy
  - Use Material3 tokens (color, typography, shape, elevation, spacing, radii, animation durations, scales) as single source of truth.
  - Put themes and design tokens in a core design system module consumed by features.
- Figma → tokens workflow
  - Export Figma tokens (Token Studio or Figma API) and map to Compose tokens (Kotlin or JSON).
    - same tokens (color, typography, spacing) -> manually add them to core module or use a codegen tool
  - Keep token names stable so devs and designers reference the same keys.
- Core UI library
  - Implement composable primitives (Buttons, TextFields, Chips, Cards) in core/ui with constrained props.
  - Provide variants and theming hooks so features compose rather than reimplement.
- Tooling and previews
  - Use Compose Previews with multiple configurations (light/dark, large/small font scale, RTL).
- Runtime concerns
  - Support dynamic color and system themes, font scaling, and accessibility contrast checks.
  - Version tokens and document migration steps when changing core styles.

### What were some performance pitfalls you ran into with Compose and how did you address them?
- common Compose performance pitfalls are:
  - excessive/unnecessary recompositions, 
  - unoptimized Lazy lists (missing keys, heavy item scopes, bad Paging 3 usage), 
  - snapshot state misuse — i.e., using mutable containers or non-observable state in a way that prevents fine-grained snapshot tracking or forces full-tree recompositions.
- Recomposition issues
  - Cause: capturing changing objects/closures or passing non-stable parameters.
  - Fix: mark types as @Stable / implement equals/hashCode, use remember/rememberUpdatedState/derivedStateOf, hoist state to ViewModel.
- Lazy lists / Paging 3
  - Cause: missing item keys, doing heavy work inside item Composables, using normal List instead of LazyPagingItems.
  - Fix: use items(items, key = { it.id }), move expensive work out of composition, use Paging’s collectAsLazyPagingItems() and lazyListState.
- Snapshot state misuse (what it is)
  - Problem: mutating a plain mutable collection or POJO that Compose can’t observe, or mutating state incorrectly so snapshots aren’t granular. That causes either no UI update or large unnecessary recompositions.
  - Fix: use Compose snapshot types (mutableStateOf, mutableStateListOf, mutableStateMapOf) or update immutable objects by replacing them atomically; use snapshotFlow to bridge Snapshot state to Flows when needed.

```kotlin
// kotlin
// Misuse: plain MutableList inside a ViewModel and no item keys in LazyColumn.
// This can cause missed updates or whole-list recomposition.

class MisuseViewModel {
    // plain mutable list — Compose won't track internal mutations reliably
    var items: MutableList<Item> = mutableListOf()
        private set

    fun add(item: Item) {
        items.add(item) // UI may not update or may trigger coarse recomposition
    }
}

@Composable
fun MisuseScreen(vm: MisuseViewModel) {
    // Passing the raw list; no stable keys -> expensive diffs / rebinds
    LazyColumn {
        items(vm.items) { item ->
            Text(item.text)
        }
    }
}

// Fixed: use snapshot-aware state and stable keys, or Paging integration.

class FixedViewModel {
    // snapshot-aware list
    private val _items = mutableStateListOf<Item>()
    val items: List<Item> get() = _items

    fun add(item: Item) {
        _items.add(item) // Compose sees fine-grained changes
    }

    // Immutable-replacement alternative:
    private val _state = mutableStateOf(ListState(emptyList()))
    val state: State<ListState> = _state
    fun replaceAll(new: List<Item>) { _state.value = _state.value.copy(items = new) }
}

data class Item(val id: Long, val text: String)
data class ListState(val items: List<Item>)

// Fixed Composable: use keys and avoid heavy work inside item lambdas
@Composable
fun FixedScreen(vm: FixedViewModel) {
    val items = vm.items
    LazyColumn {
        items(
            items = items,
            key = { it.id } // stable key prevents full rebinds when items reorder
        ) { item ->
            // keep item Composables small; remember heavy resources outside
            Text(item.text)
        }
    }
}

// Paging 3 integration: use collectAsLazyPagingItems() and provide keys
@Composable
fun PagingScreen(pagingFlow: Flow<PagingData<Item>>) {
    val lazyPagingItems = pagingFlow.collectAsLazyPagingItems()
    LazyColumn {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems.peek(index)?.id ?: index }
        ) { index ->
            val item = lazyPagingItems[index] ?: return@items
            Text(item.text)
        }
    }
}
```

---

## 🔐 Security & Backend Integration
### Can you explain your approach to SSL pinning and why it’s important?
[SSL Pinning](https://www.youtube.com/watch?v=efIPpIYBNTc&pp=ygUTYW5kcm9pZCBzc2wgcGlubmluZw%3D%3D)
- what is MitM attack prevention?
  - A MitM (man-in-the-middle) attack intercepts TLS traffic and presents a forged certificate so the client trusts an attacker. 
  - Pinning prevents this by rejecting TLS chains that don’t match the pinned certificate or public-key hashes even if the system CA would accept them.
- what is certificate pinning lifecycle?
  - SSL pinning prevents MitM attacks by restricting which server certificates or public keys the app accepts. 
  - Implement pinning using SPKI/public-key hashes, include a pin lifecycle (staging, active, rotation, expiry monitoring), and design safe fallback/rollout and observability.
- Certificate pinning lifecycle (practical steps)
  1. Generate pins from the server’s public key (use SPKI SHA-256 hashes, not full certs).
  2. Ship at least two pins: the current key and a backup key (for rotation).
  3. Stage pins in a non-blocking environment (staging builds) and monitor errors.
  4. Enforce pinning in production once validated.
  5. Rotate keys regularly: publish new backup key, update servers, then switch primary pin in an app update.
  6. Monitor pin expiration and telemetry; maintain a documented rotation plan and recovery process.
  7. Fallback and operational handling
  8. Use backup pins (one active, one future/backup) so certificate re-issuance doesn’t break clients.
  9. Prefer fail-closed (reject on mismatch) for security, but use staged rollout and feature flags to avoid widespread outages.
  10. Provide an emergency rollback path (remote config that can relax enforcement) only with strict audit and access controls.
  11. Log and report pin failures (telemetry + alerts) to detect issues early.
  12. Avoid pinning entire certs; pin public keys (SPKI) to allow reissues by same CA/key pair.
- Use OkHttp CertificatePinner or TrustManager approach. 
  - Pin SPKI SHA-256, keep multiple pins, and integrate telemetry on SSLPeerUnverifiedException. 
  - Prefer certificates issued by a well‑managed CA and automate rotation tests. 
  - Consider libraries like TrustKit for policy management but evaluate maintenance overhead.
```kotlin
// network package
// Example: OkHttp CertificatePinner with primary and backup SPKI pins
val pinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // primary
    .add("api.example.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=") // backup
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(pinner)
    .addInterceptor { chain ->
        try {
            chain.proceed(chain.request())
        } catch (e: SSLPeerUnverifiedException) {
            // record telemetry/alert for pin failure
            // fail closed by rethrowing
            throw e
        }
    }
    .build()
```

### How did you implement token-based authentication, and how do you handle token refresh securely?
- implement OAuth2 (Authorization Code with PKCE) issuing short‑lived JWT access tokens and long‑lived refresh tokens; 
  - store refresh tokens encrypted (Keystore + EncryptedSharedPreferences), 
  - keep access token in memory, refresh on 401 with rotation and telemetry
- What to say about OAuth2 / JWT - OAuth2: use Authorization Code + PKCE for native apps; server returns access and refresh tokens.
  - JWT: access tokens can be JWTs containing claims (sub, exp, scopes); validate signature server-side. Treat JWT as opaque on client; don’t embed secrets.
  - Use short lifetimes for access tokens and rotate refresh tokens server-side.
- What is a refresh token flow - The client uses the refresh token to request a new access token when the access token expires.
  - Prefer refresh token rotation: server returns a new refresh token each refresh and invalidates the old one to prevent replay.
  - Handle failures by forcing re-auth (sign-in) and surfacing clear UX/telemetry.
- What is secure storage - Store refresh tokens encrypted: use EncryptedSharedPreferences (Jetpack Security) backed by Android Keystore (MasterKey).
  - Keep access token in memory (ViewModel/Repository). Persisted access tokens increase exposure.
  - Protect remote config or emergency unlocks with strict ACL and audit.
- [Credential Manager](https://www.youtube.com/watch?v=FULNucVxf94&pp=ygUVcGhpbGlwcCBsYWNrbmVyIG9hdXRo)
- [Security Practices](https://www.youtube.com/watch?v=VQvfvXD3ec4)
- [JWT Auth w/ Ktor - Long](https://www.youtube.com/watch?v=uezSuUQt6DY&pp=ygUVcGhpbGlwcCBsYWNrbmVyIG9hdXRo)

### What libraries or Android APIs did you use for biometric authentication, and how did you handle backward compatibility?
- Options:
  - AndroidX Biometric (androidx.biometric:biometric) — recommended; wraps BiometricPrompt and provides backward compatibility to API 23+.
  - Framework BiometricPrompt (platform) — same API on newer Android, but AndroidX is easier for compatibility.
  - Legacy FingerprintManager / FingerprintManagerCompat — only for very old devices; prefer AndroidX.
  - KeyguardManager / device credential (PIN/pattern) as a fallback.
  - Fallbacks: app PIN/password, EncryptedSharedPreferences or keystore-protected credentials.
- backward compatibility:
  - Use AndroidX Biometric library for API 23+ support.
  - For older devices (pre-23), fall back to app PIN/password or device credential (KeyguardManager).
  - Gracefully degrade UX: inform users when biometric isn’t available and provide alternative auth methods.
    - PIN/password fallback
- [Implement Biometric Auth](https://www.youtube.com/watch?v=_dCRQ9wta-I&pp=ygUaYW5kcm9pZHggYmlvbWV0cmljIHBoaWxpcHA%3D)

### How do you protect sensitive data stored on the device (e.g., tokens, user info)?
- EncryptedSharedPreferences: (Depricated -> use DataStore with encryption)
  - Jetpack Security wrapper that transparently encrypts preference keys and values using AES-GCM. 
  - It uses a MasterKey derived from a key stored in the Android Keystore so values at rest are encrypted and protected from filesystem access. 
  - Recommended for storing refresh tokens or other persisted secrets that must survive process restarts.
- Android Keystore: 
  - platform API that generates and stores cryptographic keys in a hardware-backed or system-protected area. 
  - Keys are non-exportable, can require user authentication (biometric/device credential) for usage, and are ideal for storing private keys or symmetric keys used for encryption/decryption of sensitive data.
- Best practices:
  - Minimize sensitive data storage; prefer in-memory when possible.
  - Use EncryptedSharedPreferences for persisted secrets (refresh tokens, user settings).
  - Use Keystore for cryptographic keys and operations.
  - Apply least privilege: restrict access to sensitive data to only necessary components.
  - Regularly review and rotate stored secrets as part of security hygiene.
- [Encrypt DataStore](https://www.youtube.com/watch?v=XMaQNN9YpKk&pp=ygUicGhpbGlwcCBFbmNyeXB0ZWRTaGFyZWRQcmVmZXJlbmNlcw%3D%3D)
- [Encryption Guide](https://www.youtube.com/watch?v=aaSck7jBDbw&pp=ygUicGhpbGlwcCBFbmNyeXB0ZWRTaGFyZWRQcmVmZXJlbmNlcw%3D%3D)
- [Protect API Keys](https://www.youtube.com/watch?v=-2ckvIzs0nU&pp=ygUicGhpbGlwcCBFbmNyeXB0ZWRTaGFyZWRQcmVmZXJlbmNlc9IHCQkDCgGHKiGM7w%3D%3D)
- [Code Lab](https://developer.android.com/codelabs/android-preferences-datastore#0)

---

## ⚙️ Performance Optimization & CI/CD
### How did you measure the 25% improvement in load times?
- [Profiler Analysis](https://www.youtube.com/watch?v=CQc-QDTmCoQ&pp=ygURcGhpbGlwcCBwcm9maWxlcnM%3D)
- [Boost Perf w/ Baseline Profiles](https://www.youtube.com/watch?v=hqYnZ5qCw8Y&pp=ygURcGhpbGlwcCBwcm9maWxlcnM%3D)
- [Benchmark your App](https://www.youtube.com/watch?v=XHz_cFwdfoM&pp=ygURcGhpbGlwcCBwcm9maWxlcnM%3D)
- [5 ways to Boost Perf](https://www.youtube.com/watch?v=epkAPnF5qrk&pp=ygUccGhpbGlwcCBmaXJlYmFzZSBwZXJmb3JtYW5jZQ%3D%3D)

- Tools:
  - Android Profiler: CPU , Memory , Network and Energy panes. Mention recording a startup/profile session, method traces for main-thread work, network bytes/latency, and frame/renderer thread hotspots. 
    - Use it to find expensive synchronous work blocking first draw.
  - Systrace (legacy): low-level system timeline (app threads, GPU, SurfaceFlinger). Mention using it to diagnose jank, GC pauses, thread scheduling, and end-to-end frame timing when Profiler is inconclusive.
    - TODO: is Systrace still relevant with newer tools? 
  - Firebase Performance Monitoring: real-user telemetry (percentiles, release cohorts), custom traces and network traces, automatic network timing and slow-request detection. 
    - Mention analyzable percentiles and alerts for regressions after deploys.
  - Jetpack Macrobenchmark & Startup Profiler: deterministic CI runnable measurements for cold/warm startup and screen navigation timings; mention you used these for reproducible before/after numbers.

- How to tie optimizations to measured metrics:
  - Define measurable metrics first: cold-start time, time-to-first-frame (TTFF), time-to-interactive, screen load latency (API -> render), network payload bytes, image decode/render time, frame drops / jank rate, 95th/99th percentile latencies.
  - Baseline > change > measure: capture baseline traces (Profiler / Systrace / Macrobenchmark) before changes; implement single optimization; re-measure same scenario. Report relative improvement on the same metric (e.g., cold start 1200ms -> 900ms = 25%).
  - Map each optimization to metric:
    - Lazy loading: reduces initial network payload and work on bind. Measure reduced initial bytes, fewer items bound, and lower time-to-interactive using network traces and CPU method profiles.
    - Coil image optimizations: measure image download time, decode time, cache hit rate and bytes saved (Network trace + custom Firebase trace). Show improved time to first meaningful paint for image-heavy screens.
    - Background initialization: measure main-thread blocking (method trace) and cold-start reductions. Moving work to background should drop blocking main-thread times in Profiler and improve startup macrobenchmark numbers.
  - E-Com optimizations:
    - Adaptive image delivery (size based on device / viewport), use WebP/AVIF, and server CDN resizing to minimize bytes.
    - Prioritize critical resources: load above-the-fold data first and defer product thumbnails below fold.
    - Skeleton / placeholder UI to improve perceived performance and time-to-meaningful-paint.
    - Pagination/prefetching for product lists (Paging 3 + prefetchDistance).
    - Cache strategy: aggressive HTTP caching, ETags, cache headers, OkHttp cache, disk cache for images.
    - Network stack: enable HTTP/2 or HTTP/3 (QUIC) and connection pooling / keep-alive to reduce latency.
    - Reduce APK startup overhead: baseline profiles, App Startup library, on-demand modules, R8/shrinking, avoid heavy static init.

- Measure:
  - Run controlled Macrobenchmark tests for cold/warm starts and key screens in CI to get reproducible numbers.
  - Capture Profiler and Systrace for root cause analysis of hotspots.
  - Deploy staged release and use Firebase Performance for RUM percentiles and regression alerts.
  - Use A/B or phased rollout to measure user-facing impact and validate hypotheses.

### Can you give an example of a performance bottleneck you found and how you fixed it?
- e.g., slow image loading, excessive main thread work, or inefficient layouts.
- Start:
  - Slow product-list screen: long time-to-interactive and jank on first scroll. 
  - Profiling (Android Profiler + Systrace + Macrobenchmark) showed large image decode on the main thread, many cache misses, and synchronous initialization in Application.onCreate() blocking startup.
- Fix:
  - Resize images to view size and enable disk/memory caching (Coil).
  - Move heavy initialization off the main thread and lazy-initialize services.
    - use a coroutine or WorkManager to defer non-critical init.
  - Use Paging / prefetch and stable keys for lists to avoid rebinds.
  - Measure with Macrobenchmark (deterministic) and Firebase Performance (RUM) to validate ~25% improvement.

- How Coil helps:
  - Configure a shared ImageLoader with an OkHttp disk cache and use image requests that specify size, enable hardware bitmaps, and prefer cached results to avoid main-thread decode.
```kotlin
// ProductImageLoader.kt 
object ProductImageLoader {
    fun create(context: Context): ImageLoader {
        val cacheDir = File(context.cacheDir, "image_cache")
        val okCache = Cache(cacheDir, 50L * 1024 * 1024) // 50MB

        val okHttp = OkHttpClient.Builder()
            .cache(okCache)
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(okHttp) // ensures shared connection pooling + disk cache
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, "coil_disk_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB
                    .build()
            }
            .componentRegistry {
                add(GifDecoder.Factory())
            }
            .memoryCacheSizePercent(0.12) // tune to app memory budget
            .respectCacheHeaders(true) // allow server cache-control/etag
            .build()
    }
}
// ProductItemImage.kt
fun buildProductImageRequest(context: Context, url: String, targetPx: Int) =
    ImageRequest.Builder(context)
        .data(url)
        .size(targetPx, targetPx) // avoid full\-size decode
        .allowHardware(true)
        .memoryCachePolicy(CachePolicy.ENABLED) // this -> avoid re-decode
        .diskCachePolicy(CachePolicy.ENABLED) // this -> avoid re-download
        .networkCachePolicy(CachePolicy.ENABLED) // this -> respect server caching
        .placeholder(android.R.color.darker_gray)
        .error(android.R.color.holo_red_dark)
        .build()
```

### What’s your approach to lazy loading and image optimization?
- Lazy loading: load only what’s needed (Paging 3 for lists, prefetchDistance tuning), defer heavy init, and load images on-demand as items become visible.
- Coil transformations: image-level operations (rounded corners, circle crop, blur, color filters) applied during decode/transform to reduce layout work and produce consistent thumbnails.
- Placeholder strategies: skeletons or LQIP (tiny blurred preview or dominant-color tile), solid color placeholders, and crossfade to reduce perceived latency.
- Caching with Coil: combine OkHttp HTTP cache + Coil DiskCache + memory cache, set CachePolicy and resized requests to avoid re-decode and reduce bytes.
    - similar concept to Room: single source of truth (disk cache) with in-memory cache for fast access.
      - Disk cache persists images on device storage, surviving app restarts, 
        - while memory cache stores images in RAM for quick access during the app session.
      - Disk cache is slower to access but larger in size; 
        - memory cache is faster but limited by available RAM.
- Image formats: prefer WebP/AVIF for smaller size and better quality; use progressive JPEGs for perceived speed.
- Network optimizations: HTTP/2 or HTTP/3, connection pooling, keep-alive, and adaptive image delivery (size based on device/viewport).
- Measure impact: use Macrobenchmark for load times, Firebase Performance for real-user metrics, and Profiler for decode times and cache hit rates.

### Walk me through your CI/CD setup in GitLab. What stages did you automate and how do you handle failed builds?
- CI -> build, test, lint, detekt, with cached Gradle, produce artifacts (APKs, test reports)
- CD -> staged deploys to internal testing, beta, then production with manual approval, protected branches, and secrets stored in GitLab CI variables. 
  - Failed jobs block later stages, flaky checks marked allow_failure, and production requires manual promotion and successful previous stages.
- failed builds:
  - notify team via Slack/email, auto-retry flaky jobs, require fixes before merging PRs, and maintain a healthy main branch with green builds.

### How do you manage environment variables with Gitlab (differences to Github Actions) and signing configs securely in CI/CD pipelines?
- Use GitLab CI/CD variables (project/group/env scopes) for secrets; mark them protected and masked. Use the File variable type for binaries (keystore) so CI gets a temp path.
- GitHub Secrets is a secret store for GitHub Actions 
  - Differences: 
    - GitHub has repo/org/environment secrets and more restrictive usage on PRs from forks; 
    - GitLab has project/group variables, File type, and runner-level integrations.
- Prefer short‑lived credentials (OIDC) or a dedicated secret manager (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager, Azure Key Vault) over static long‑lived secrets.
- **Signing**: don’t commit keystore. Store keystore as a File variable or base64 string and inject it during the job; store passwords as masked variables.
- **Encrypted files**: repo‑committed ciphertext (e.g., keystore.jks.enc) that CI decrypts using a secret passphrase stored in CI variables — useful if you must keep an encrypted blob in git, but less ideal than a secret manager.
- **Best practices**: least privilege, rotate, audit, protected branches/environments, avoid logging secrets, use File variables for binaries, use OIDC for cloud creds, prefer secret manager integrations.

---

## 👥 Leadership & Mentorship
### How do you approach mentoring junior developers?
- **Onboarding**: describe a checklist, small starter tasks, and a buddy for the first weeks so they ship something fast and build confidence.
- **Career growth**: mention one-on-ones, an individualized development plan, and mapping tasks to clear junior→mid→senior competencies.
- **Structured feedback**: explain timely PR reviews, rubric-based reviews, and focused actionable comments (not just “fix this”).
- **Pairing & shadowing**: say you pair on feature design, bug fixes and on-call rotations to transfer tacit knowledge.
- **Architecture & guidelines**: reference ADRs, coding standards, PR templates, and lightweight architecture reviews to teach tradeoffs.
- **Progressive ownership**: give small end-to-end tasks, then increase scope as competence grows; review failures as learning moments.
- **Psychological safety**: emphasize blameless postmortems, encourage questions, and reward improvement.
- **ADR (Architecture Decision Record)**: is a lightweight document that captures the context, decision, and consequences of significant architecture choices. 
  - It helps teams understand why certain decisions were made and provides a reference for future discussions.

### How do you balance your time between coding and leading?
- I balance coding and leading by protecting deep work, scheduling dedicated leadership time, delegating effectively, and prioritizing work that unblocks others. I use a repeatable rhythm and measurable SLAs to stay effective.
1. **Time blocks**: reserve uninterrupted blocks for focused coding (e.g., mornings) and separate blocks for meetings, 1:1s, and code reviews. 
2. **Office hours**: weekly “mentor hours” or daily 30-minute slots for ad-hoc help and pairing. 
3. **Delegation & ownership**: assign clear owners, document expectations, and review progress rather than doing the work myself. 
4. **Prioritization**: handle mentoring tasks immediately if they block delivery or are recurring; otherwise schedule them into leadership time. 
5. **Tools & metrics**: use calendar protections, PR SLAs, ticket priorities, and track PR turnaround and mentee progress to show impact.
- **SLA (Service Level Agreement)**: is a commitment to complete a task within a certain timeframe. 
  - For example, I might set an SLA to review PRs within 24 hours or respond to mentee questions within one business day. This helps ensure timely support without constant interruptions.

### What’s an example of a time you and another developer disagreed on a technical approach — how did you handle it?
STAR
“A good example of a technical disagreement was when our PM asked the iOS lead and me to evaluate Kotlin Multiplatform Mobile (KMM) for potential adoption.
After researching it, I saw strong long-term benefits in unifying business logic across Android and iOS — things like shared networking, analytics, and data models, which could reduce duplication and maintenance overhead in the future.
However, the iOS lead raised valid concerns — since most of our user base was on iOS, he was worried that introducing KMM could increase app size, slow down performance compared to native Swift, and require retraining existing staff.
From my perspective, my main concern wasn’t about performance but about introducing potential instability into an already mature codebase and the short-term cost of adoption versus the payoff.
We both presented our findings transparently to the PM and leadership team — pros, cons, and risk factors. In the end, we agreed to run a small POC first, testing KMM in a low-risk module to measure build times, binary size, and performance impacts before making a full commitment.
That approach allowed us to validate the technology objectively and maintain alignment across teams. What I took away from that experience was that technical disagreements are best resolved not by “winning,” but by grounding decisions in data and shared business goals, so everyone feels confident in the outcome.”**

#### “What was the result of that POC?”
“The POC gave us some valuable clarity. We implemented a small shared module in KMM — specifically, a network utility layer and some data models — to compare build times, app size, and performance against our existing native implementations.
What we found was that while KMM worked well on Android, the iOS build size increased noticeably, and integration with our Swift-based analytics framework introduced some friction. The shared code benefits were promising, but the tooling and build process still felt immature for production at our scale.
Based on that, we decided to pause full adoption and revisit KMM once the tooling matured. However, the POC itself wasn’t wasted — it helped us identify areas where we could improve parity between Android and iOS without full KMM adoption, like standardizing API models and documentation.
So in the end, we made a data-driven decision that supported our immediate business goals while keeping an eye on future opportunities for shared code.”

#### “What would you do differently next time?”
“If I were to do it again, I’d start by aligning earlier with the business side — clarifying what problem we were actually trying to solve. We initially focused on the technical promise of KMM, but it became clear that our real challenge was improving development efficiency, not code sharing for its own sake.
I’d also ensure that we defined success criteria before starting — things like acceptable build time thresholds, target app size limits, and measurable developer productivity gains. That would have made the evaluation even more objective and quicker to reach a decision.
The key takeaway for me was that technical innovation needs to be guided by clear business context and measurable outcomes, not just technical curiosity.”

### How do you ensure coding standards and best practices are followed across teams?
“I make sure coding standards and best practices are followed through a combination of process, automation, and culture.
On the process side, I’ve established clear code review guidelines that focus not just on correctness, but on readability, maintainability, and consistency across modules. Every feature branch goes through at least one peer review before merging.
From a tooling standpoint, I’ve integrated static analysis tools like Detekt and Ktlint into our CI pipeline, so any style or convention violations are caught automatically before code is merged. This reduces subjective feedback in reviews and ensures consistent quality across the team.
I also maintain internal documentation and onboarding guides that define patterns for our Clean Architecture, dependency injection, and Compose usage, so new developers can quickly understand our conventions.
And finally, I treat standards as an evolving practice — during retros or tech syncs, I encourage the team to discuss whether certain patterns still make sense or need refinement. That helps everyone feel ownership over the codebase, not just compliance.”

## 🚀 Strategic / Forward-Looking
### How do you see Android development evolving over the next few years, and how are you preparing for it?
- Talk about trends like AI/ML integration, Jetpack Compose adoption, or Kotlin Multiplatform.

### Have you explored Kotlin Multiplatform or Flutter for code sharing? What’s your take?
- Discuss pros/cons, use cases, and team readiness.
- what are some pros/cons of KMM vs Flutter?
  - Flutter: faster UI development, single codebase for UI and logic, but larger app size and less native feel
  - KMM: native UI, smaller app size, but more complex setup and platform-specifics
    - I think KMM will be better for teams that already have native apps and want to share logic
    - KMM allows for native UI development, which can lead to better performance and user experience on each platform.

### If you were to re-architect the app today, what would you do differently?
- I like the CLEAN modularization approach, but might explore MVI for state management and further optimize CI/CD pipelines.

### What’s one technology or approach you’d like to implement in your next role and why?
- I’d like to explore more AI/ML integration for personalized user experiences, as well as further adoption of Kotlin Multiplatform to streamline cross-platform development.


## 🔁 Behavioral / Situational Follow-Ups
### Tell me about a technical failure during production and how you handled it.
- Talk about rollback, monitoring, or postmortem improvements.
- what would be a good example to use here?

### How do you prioritize technical debt when you’re on tight deadlines?
- what is technical debt? 
- I focus on high-impact areas that affect performance, stability, or developer productivity first, while balancing feature delivery.
- I try to focus on blockers that slow down development or cause frequent bugs.

### How do you ensure knowledge sharing within your team?
- I am a big markdown enjoyer, so I create documentation for architecture decisions, coding standards, and onboarding guides.
- I will sometimes make a markdown files in the repo for specific topics that I want to share with the team
- I also encourage team members to present tech talks or lunch-and-learns on topics they are passionate about.




BA -> Business Analyst
PMO -> Project Management Officer / PM -> Project Manager