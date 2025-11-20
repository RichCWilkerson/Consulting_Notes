
# Background
- born 11/11/1990
- graduated hs 2009
- started degree 2009

# Pitch

Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

In my current role I was tasked with modernizing and scaling the app,
1. I architected and implemented a scalable Android app using Clean Architecture and MVVM, working with the team to identify module boundaries, then breaking the codebase into feature-based Gradle modules -> improving feature release cycles and reducing build times by about 30%.
3. To modernize the UI, I introduced Jetpack Compose, starting with a hybrid XML + Compose approach before fully migrating new features, which cut UI development time and improved design parity with Figma.
5. On the backend side, I implemented secure payment and authentication flows using tokenization, SSL pinning, and biometric verification, and
6. improved app performance by reducing load times by about 25% through lazy loading, image optimization with Coil, and background initialization.
7. I’ve also set up CI/CD pipelines using Github Actions, ensuring smooth, automated testing and deployments across QA and production.
8. Along the way, I’ve mentored developers, conducted code reviews, and helped shape best practices across the team.

I really enjoy building useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name] 
would be a fantastic place to continue growing my career and contributing to meaningful innovation in mobile development.

## Additions
1. I also worked on AR-based virtual try-on and personalized shopping experiences, integrating ARCore SDK to enhance user engagement.
      → removed for now, was overwhelmed with amount of information and pitch went long → will add it back into pitch when I’m more comfortable with all the other topics and can then look into this tech
2. I also led the adoption of Kotlin Multiplatform Mobile (KMM) to share business logic and analytics across Android and iOS, collaborating closely with our offshore team in India.
      → tempted to change this to current prototyping and presenting use case for the team

---

# Cross-Examine

---

## 🧩 Architecture & Technical Depth

### Ensure MVVM is understood
- Model: data layer (repositories, data sources, network, DB)
- View: UI layer (Activities, Fragments, Composables)
- ViewModel: presentation layer (state management, business logic, exposes state to View via StateFlow/LiveData)
- Unidirectional data flow: View observes ViewModel state; ViewModel interacts with Model; Model provides data to ViewModel.

- migrated a few workflows from MVP to MVVM
  - separated data/business logic from UI
  - ViewModel exposes StateFlow for View to observe
  - used Coroutines for async data fetching in ViewModel
  - improved testability by mocking ViewModel and Model layers

### Monolith -> Modular Clean Architecture Narrative
Narrative STAR:
Situation
Worked in a large, legacy Android codebase with tight coupling, long build times, and poor feature ownership.
Goal: modernize the architecture to improve scalability, testability, and team velocity.
Took technical leadership on the migration to a Clean Architecture + modularized structure (Kotlin, Coroutines, Flow, Hilt, Room, Retrofit, Compose).

Task
Design the new architecture, define module boundaries, and lead the phased migration strategy.
Create new Gradle modules and dependency rules.
Establish DI patterns, shared core components, and stable navigation/data flows.
Mentor the team on the new architecture and best practices.

Action
1. Deep analysis + boundary identification
  - Mapped out existing packages, identified natural seams (home, shop, designers, account, shopping bag).
  - Catalogued shared resources (theme, typography, networking, domain models).
  - Flagged risky areas where features tightly depended on each other.

2. Created clear module structure
  - Built core-ui, core-domain, core-network, core-designsystem, and individual feature-* modules.
  - Enforced strict dependency rules:
    - Features → Core (one-direction)
    - Features cannot depend on each other
    - Core has zero feature awareness

3. Ensured each feature was independently navigable
To avoid cross-module fragment leaks and backstack bugs:
  - Each feature module got its own Activity, its own NavHost, and its own navGraph. 
  - Exposed only entry points (like FeatureEntry interfaces) to the app shell.
  - Define navigation contracts in core-navigation so modules used typed routes, not raw fragment transactions.
    - TODO: what does this mean?

4. Hilt + DI consistency across modules
Team struggled early with cross-module Hilt scoping failures.
  - Standardized DI using:
    - @Module in core for interfaces 
    - @Binds in feature modules for implementations 
    - Component scopes documented clearly
  - Held a workshop after teammates repeatedly created circular dependencies and multi-binding errors.

5. Incremental migration strategy
To avoid a “big bang” rewrite, phased the migration:
  - Each feature retained temporary legacy Activities during transition.
  - Used ComposeView inside legacy XML screens to slowly roll in Compose.
  - Shared ViewModels across modules via Hilt-assisted factories.
  - Feature flags used to ship partially migrated modules safely.
    - TODO: what are feature flags? how do they help safely migrate?

6. Team failures / bottlenecks + resolutions

Failure 1: Accidental circular dependencies
Some devs imported feature modules into each other, causing build failures and confusion.
Resolution:
  - Added lint & Gradle checks to enforce allowed dependencies. 
  - Created a diagram and documentation explaining the allowed graph. 
  - Code reviews specifically checked dependency direction.

Failure 2: Theme/style fragmentation
Teams recreated colors, paddings, or typography inside feature modules.
This caused UI inconsistency and unnecessary duplication.
Resolution:
  - Moved all styling, typography, spacing, shape tokens into core-designsystem. 
  - Introduced a pre-commit check to prevent re-declaring colors/styles.
  - Setup flow for adding new tokens to core-designsystem. (Talk to UI/UX first, then core module PR, then feature module PRs to consume.)

Failure 3: Build time spikes
Early modularization actually made things worse, because some modules weren’t isolated and changes cascaded.
Resolution:
  - Split core into smaller, more stable modules. 
  - Introduced API/implementation separation for modules. 
  - After stabilization, Gradle was able to build only the affected modules → resulting in the 30% improvement.

Failure 4: Team confusion around navigation
Some devs attempted to navigate directly to other features' screens → broke modular boundaries.
Resolution:
  - Introduced FeatureEntry interfaces with typed parameters. 
  - App shell resolves entries using DI; features never reference each other.

Failure 5: Resistance to change
Some teammates were anxious about Compose, modularization, and new DI patterns.
Resolution:
  - Ran weekly workshops with live coding. 
  - Pair programmed with resistant teammates. 
  - Documented patterns, anti-patterns, and template files they could copy.

After 2–3 iterations, team skill and confidence noticeably improved.
Result
Build times improved by ~30% because Gradle could rebuild only the modules changed, instead of the entire monolithic app. Modular boundaries reduced interdependencies and enabled better incremental builds.
Team velocity increased: features were isolated, easier to reason about, and safer to iterate on.
Testability improved thanks to clear separation of concerns and stable contracts between layers.
Consistency and reuse increased through shared theming, UI components, and models in the core module.
Delivered a scalable, maintainable architecture that supports long-term feature growth and parallel development.

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
  - Each module needs to use @Binds for core DI interfaces to bind implementations in feature modules.
  - If DI is specific to that module, we create a new DI @Module for those @Provides
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
  Action: inventory of shared code, moved only interfaces/DTOs/utilities into a core module, 
  - implemented features in feature modules and bound implementations via Hilt modules (or entry points when needed). Enforced Gradle dependency direction and used DI (Provider/Lazy) to break runtime cycles. 
  - For navigation, defined a Navigator interface in core implemented by the app module; features expose routes or deep links to avoid direct feature-to-feature deps.
    - core:navigation is the best practice for navigation contracts
      - App will implement Navigator interface to navigate between features
        - 
    - A deep link is a URL or URI that opens your app directly to a specific screen, often with parameters.
      - Opening from push notifications, emails, or web links.
        Routing users from marketing campaigns to a specific screen.
        Supporting web-to-app handoff and app-to-app navigation.
  Impact: reduced coupling, improved build times and testability, and made ownership and boundaries explicit.

```kotlin
// core:navigation
interface CheckoutNavigator {
    fun openCheckoutFromBag()
    fun openOrderSummary(orderId: String)
}

interface AuthNavigator {
    fun openLogin(returnTo: ReturnDestination? = null)
    fun openAccountCreation()
}

sealed interface ReturnDestination {
    data class ProductDetails(val productId: String) : ReturnDestination
    object Home : ReturnDestination
}

// TODO: where is the DI module implemented?
// app module?
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigatorModule {

    @Binds
    abstract fun bindCheckoutNavigator(
        impl: CheckoutNavigatorImpl
    ): CheckoutNavigator

    @Binds
    abstract fun bindAuthNavigator(
        impl: AuthNavigatorImpl
    ): AuthNavigator
}
```


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


### AR-based Virtual Try-On
“AR-based virtual try-on essentially combines ARCore’s SDK motion tracking, environmental understanding, and depth data with real-time 3D rendering. The pipeline is straightforward: you create anchors, attach 3D assets, use the depth mesh for occlusion accuracy, and render through Sceneform or a custom GL pipeline. The key challenges are model optimization, frame-rate stability, and smoothing the tracking data

“The Depth API gives you a real-time depth map from the camera. That allows you to understand how far every pixel in the scene is from the device. With that information, you can do proper occlusion—so if a user walks in front of a virtual object, the virtual object disappears behind them instead of just floating on top. It’s essential for realistic virtual try-on.”
1. Generates per-pixel depth data
2. Enables occlusion and realistic layering
3. Helps scale 3D objects naturally based on distance

“Sceneform is the higher-level 3D rendering engine for ARCore. It abstracts away most of the complexity of OpenGL and Filament, so you can load 3D models, apply materials, handle lighting, and attach objects to anchors without manually managing the render pipeline.
It supports physically-based rendering (PBR), animation playback, and real-time lighting updates, which makes it ideal for virtual try-on because the 3D asset looks realistic and reacts to ARCore’s light estimation. Sceneform basically handles the heavy lifting—render loops, shaders, model loading—so developers can focus on the AR logic itself instead of low-level graphics code.”

“The Face Mesh API gives you a dense mesh of 468 landmark points across the face. These points include eyes, eyebrows, nose bridge, chin, and cheek contours. Virtual try-on uses these landmarks to anchor items like glasses or makeup textures so they stick naturally to the user’s face—even as they rotate, tilt, or move.” NOTE:
1. Works without needing a depth sensor.
2. Supports deformation → so models can conform to facial structure.
3. Stable tracking even in lower lighting.

“Anchors are ARCore’s way of locking a virtual object into a stable position in the real world. Trackables like planes, faces, or feature points define where anchors can be placed. Once an anchor is created, ARCore keeps it updated as the camera moves, so the object doesn’t drift or slide.”
1. Anchors update pose every frame
2. Virtual objects are always attached to an anchor
3. Prevents jitter and drift in 3D placement

“16ms Frame Budget — Hitting 60 FPS. AR is extremely performance-sensitive. You basically have a 16 millisecond budget per frame to maintain a smooth 60 FPS experience. That includes camera capture, depth calculations, mesh tracking, and rendering. Any part of the pipeline that goes over budget causes jitter or drift in the AR object.”
1. ARCore uses asynchronous updates to help stay within budget
2. 60 FPS is ideal for stable tracking, especially with face meshes

“Light Estimation lets ARCore analyze the brightness, color temperature, and direction of light in the environment. With that data, you can light your 3D models so they match the real world. It makes virtual objects blend naturally instead of looking ‘pasted on.’”
1. Supports Environmental HDR for realistic reflections
2. Soft shadows and highlights react to actual room lighting

---

# Migration XML -> Compose Narratives

Narrative:
Situation
- Legacy Android app with XML layouts struggling to keep up with modern UI demands, slow iteration cycles, and inconsistent design fidelity.
- Goal: modernize the UI layer by migrating to Jetpack Compose to improve development speed, design consistency, and performance.
- Took technical leadership on the migration from XML to Compose, defining a phased strategy, establishing best practices, and mentoring the team through the transition.
Task
- Design the migration approach, create reusable Compose components, and lead the incremental rollout.
- Establish state management patterns, theming strategies, and interoperability with existing XML screens.
- Mentor the team on Compose concepts, best practices, and performance optimizations.
Action
- Assessment and Planning
  - Audited existing XML layouts to identify high-impact screens and common patterns.
  - Prioritized screens for migration based on complexity, user impact, and team readiness.
  - Created a migration roadmap with milestones and checkpoints.
- Incremental Migration Strategy
  - Started with hybrid XML + Compose screens using ComposeView to embed Compose in existing layouts.
  - Migrated one screen or feature at a time, keeping ViewModels and business logic unchanged.
  - Used feature flags to safely roll out Compose screens and allow easy rollback if issues arose.
- Established State Management
  - Standardized on StateFlow in ViewModels as the single source of truth.
  - XML screens collected StateFlows with lifecycleScope; Compose screens used collectAsState().
  - Shared ViewModels across XML and Compose to maintain consistent state.
- Theming and Design Fidelity
  - Created a core design system module with Material3 tokens (colors, typography, shapes).
  - Implemented reusable Compose UI components (Buttons, TextFields, Cards) in the core module.
  - Used Compose Previews to validate design fidelity against Figma mockups.
Result
- Successfully migrated key screens to Jetpack Compose, improving UI development speed by ~30% and reducing design inconsistencies.
- Enabled faster iteration cycles with Compose Previews and reusable components.
- Improved app performance by leveraging Compose’s efficient rendering and state management.


## why migrate from xml to compose?
1. plays nicer with Kotlin libraries → tying coroutines to a layout, better integration with modern dev
2. less boilerplate → common/core components
    - reusable components
    - more consistent design system
3. previews -> see UI changes without running the app
4. performance → compose only re-renders what changes
    - coroutines + flows → better state management and is easier to create background tasks
    - coroutines can suspend (pause) without blocking the main thread
5. modern models → MVI, not sure it is possible with xml
   - declarative UI fits better with unidirectional data flow (MVI)

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
    - teach Effect APIs (remember, derivedStateOf, rememberUpdatedState) and @Stable annotations to minimize recompositions.
    - use profiling tools (Layout Inspector, Compose Profiler) to identify hotspots.
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
OWASP (Open Web Application Security Project) provides guidelines and best practices for securing mobile applications.
MASVS (Mobile Application Security Verification Standard) - https://owasp.org/www-project-mobile-application-security/
### Can you explain your approach to SSL pinning and why it’s important?
[SSL Pinning](https://www.youtube.com/watch?v=efIPpIYBNTc&pp=ygUTYW5kcm9pZCBzc2wgcGlubmluZw%3D%3D)
A Man-in-the-Middle (MitM) attack is when an attacker intercepts and potentially alters TLS traffic.
A malicious actor might:
  - Present a forged or compromised certificate that the system’s CA store would normally trust. 
  - Intercept sensitive API traffic (auth tokens, PII, payments). 
  - Use tools like Burp, mitmproxy, or Frida to inspect/modify requests. 
  - Reverse-engineer the APK to extract API keys or bypass TLS validation.
Pinning prevents this by making the app trust only a known server public key (SPKI) or cert hash. Even if a compromised/rogue CA issues a certificate, the app rejects it.

- name obfuscation and ProGuard/R8 minification can help hide pin values in the binary. 
  - Pins aren't a secret — they’re public keys. What we’re hiding is context (e.g., API hostnames), not the pins themselves.
  - “obfuscation makes RE more difficult but pins are not secrets.”

- what is certificate pinning lifecycle?
  - SSL pinning prevents MitM attacks by restricting which server certificates or public keys the app accepts. 
  - Implement pinning using SPKI/public-key hashes, include a pin lifecycle (staging, active, rotation, expiry monitoring), and design safe fallback/rollout and observability.

- Certificate pinning lifecycle (practical steps) -> emphasize operational safety: pinning outages can brick apps if mishandled.
  1. Generate SPKI SHA-256 hashes (not full certs — correct). 
  2. Ship at least two pins: current + backup for rotation. 
    - so cert re-issuance doesn’t break clients.
    - stage pins in a non-blocking environment (staging builds) and monitor errors.
  3. Staged rollout:
    - Soft-fail in debug/staging (log but don't block). 
    - Validate telemetry before enabling hard-fail.
  4. Production enforcement (fail-closed). 
  5. Key rotation strategy:
    - maintain a documented rotation plan and recovery process.
    - Update server with new key. 
    - Promote backup → primary. 
    - Add a new backup into next client release.
  6. Expiration monitoring:
    - Alerts for expiry, unexpected pin mismatches, or failure spikes.
  7. Operational fallback (emergency rollback):
    - Controlled “break-glass” remote config to temporarily relax enforcement.
    - Only with strict audit and access controls -> because this is a security risk.
  8. Telemetry:
    - Catch SSLPeerUnverifiedException and report hostname + pin mismatch.
  9. Avoid full-cert pinning so cert re-issuance doesn't break clients.
    - pin public keys (SPKI) to allow reissues by same CA/key pair.


- Use OkHttp CertificatePinner or TrustManager approach. 
  - Pin SPKI SHA-256, keep multiple pins, and integrate telemetry on SSLPeerUnverifiedException. 
  - Prefer certificates issued by a well‑managed CA and automate rotation tests. 
  - Consider libraries like TrustKit for policy management but evaluate maintenance overhead.
    - TrustKit for Android is largely outdated; mention it carefully:
      - TrustKit is less maintained on Android now; I would rely on OkHttp’s built-in pinning unless I need a custom TrustManager.
    - Used when:
      - Multiple hosts share pins dynamically 
      - You need analytics/telemetry before failure 
      - You need more complex validation (e.g., cert transparency)

Pinning is NOT always appropriate:
- Apps talking to multiple third-party APIs (Stripe, Firebase, Maps) → don’t pin.
- Apps that rely on certificate rotation via CDN (Akamai/Cloudflare) → pinning can break traffic.
- Apps needing zero-downtime key rotation or non-updatable IoT clients → special consideration required.

- Some teams use Certificate Transparency logging to detect rogue certificates without pinning.
  - It’s not a replacement, but mentioning it shows you understand tradeoffs.

```kotlin
// network package
// Example: OkHttp CertificatePinner with primary and backup SPKI pins
val certificatePinner = CertificatePinner.Builder()
    .add(
        "api.example.com",
        "sha256/AbCdEfGh..."
    )
    .add(
        "api.example.com",
        "sha256/NewBackupKey..."
    )
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
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
- what is OKTA?
  - Open standard for access delegation commonly used for token-based authentication and authorization.
  - Involves obtaining access tokens (short-lived) and refresh tokens (long-lived) to access protected resources.
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
- SQLCipher or other DB encryption can be used for more complex data storage needs for Room/SQLite.
  - this is for caching more than just tokens 
  - use for PII or sensitive user data

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
  - Neiman Marcus was a web-based e-commerce app that was not optimized for mobile performance. 
  - Slow product-list screen: long time-to-interactive and jank on first scroll. 
  - JANK = UI isn’t rendering smoothly (typically below 60fps or 120fps depending on the device)
    - Coil helps reduce image decode time and main-thread work 
    - placeholders/skeletons improve perceived performance 
    - Paging 3 + prefetch reduces initial bind work and speeds up scrolls.
  - Profiling using Android Studio Profiler, Systrace, and Macrobenchmark revealed that several screens were decoding overly large images on the main thread. This caused long GC pauses, cache misses, and repeated downscaling work.
    - We also observed:
      - Jank on older Android models and tablets during scroll and screen transitions
      - High image cache churn due to images being much larger than the device needed
      - Occasional OOM crashes on older Samsung devices
      - Slow startup due to Glide initialization running on the main thread

- Fix:
  - We collaborated with backend to introduce additional image breakpoints (e.g., 600 px and 1600 px). This reduced the amount of client-side downscaling, lowered memory usage, and increased cache hit rates.
    - Add different size variants on server, request smallest needed size per device/viewport. Then downsample images on server/CDN to reduce bytes.
      - was 900, 1200, and 1920px, we added 300px, 600px, and 1600 variants for mobile/high-density tablets.
      - used WebP format for better compression and quality.
  - Resize images to view size and enable disk/memory caching (Coil).
    - by specifying size in ImageRequest and using CachePolicy to prefer cached results -> reduced decode time and main-thread work.
  - Move heavy initialization off the main thread and lazy-initialize services.
    - use a coroutine or WorkManager to defer non-critical init.
  - Use Paging / prefetch and stable keys for lists to avoid rebinds.
    - reduced initial bind work and improved scroll performance.
  - After adding new size breakpoints and migrating to Coil, we measured ~30% faster image load times on average, especially on mid-range Android devices.
    - Measure with Macrobenchmark (deterministic) and Firebase Performance (RUM) to validate ~25% improvement.
  - also saw reduced jank during scroll, lower memory usage, and fewer OOM crashes on older devices. 10% fewer janky frames on older devices during scroll.
  - also Crash-free sessions on older devices improved from 94% → 99%, largely due to fewer OOMs and fewer large in-memory Bitmaps.
    - improvements came from coil (Coroutines, Better memory management, Bitmap pooling, Smarter downsampling, More predictable caching than Glide)
    - OOM fixes are due to better bitmap management and downsampling.

```kotlin
// ImageViewModel.kt
class ProductViewModel(
    application: Application,
    private val imageLoader: ImageLoader,
    private val repository: ProductRepository,
    private val displayMetricsProvider: () -> DisplayMetrics
) : AndroidViewModel(application) {

    // expose an immutable Int, not mutableState from VM
    private val _imageWidthPx = MutableStateFlow(calculateImageWidthPx(40))
    val imageWidthPx: StateFlow<Int> = _imageWidthPx

    private fun calculateImageWidthPx(percentWidth: Int): Int {
        val metrics = displayMetricsProvider()
        // widthPixels is already in px and respects current orientation
        val screenWidthPx = metrics.widthPixels
        return (screenWidthPx * (percentWidth / 100.0)).toInt()
    }

    // If repository needs the width, pass the current Int
    val products: Flow<PagingData<Product>> =
        repository.getProducts(imageWidthPx.value)
            .cachedIn(viewModelScope)

    fun loadProductImage(url: String, targetPx: Int): ImageRequest {
        return ImageRequest.Builder(getApplication())
            .data(url)
            .size(targetPx, targetPx) // or width x height
            .allowHardware(true)
            .build()
    }
}
```

- How Coil helps:
  - Configure a shared ImageLoader with an OkHttp disk cache and use image requests that specify size, enable hardware bitmaps, and prefer cached results to avoid main-thread decode.
  - NOTE: this happens at the UI layer, not in the ViewModel. ViewModel just provides the size to request.
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

### How do you manage environment variables with Github and signing configs securely in CI/CD pipelines?
- Use GitLab CI/CD variables (project/group/env scopes) for secrets; mark them protected and masked. Use the File variable type for binaries (keystore) so CI gets a temp path.
- GitHub Secrets is a secret store for GitHub Actions 
- Prefer short‑lived credentials (OIDC) or a dedicated secret manager (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager, Azure Key Vault) over static long‑lived secrets.
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