# Navigation Options Guide
<!-- Table of contents for quick jumps within this file -->
- [Architectural pattern fit (MVC, MVP, MVVM, MVI, Clean)](#architectural-pattern-fit-mvc-mvp-mvvm-mvi-clean)
- [Scope (Activity vs Fragment vs NavGraph)](#scope-activity-vs-fragment-vs-navgraph)
- [Use Cases (Choose Approach Based on Needs)](#use-cases-choose-approach-based-on-needs)
- [Navigation Dependencies (Gradle)](#navigation-dependencies-gradle)
- [Navigation Actions](#navigation-actions)
- [Layout XML and Kotlin (Syntax)](#layout-xml-and-kotlin-syntax)
- [Navigation Triggers](#navigation-triggers)
- [Comparison of Navigation Approaches (Pros and Cons)](#comparison-of-navigation-approaches-pros-and-cons)
- [Additional Notes (quick recap)](#additional-notes-quick-recap)

---

## Architectural pattern fit (MVC, MVP, MVVM, MVI, Clean)
- MVC (classic Android)
  - Controller (Activity/Fragment) performs navigation via FragmentManager or NavController.
  - Keep navigation triggers in the Controller; Views (XML) should not navigate themselves.

- MVP
  - View forwards user actions to Presenter. Presenter decides navigation intent but should not hold Android types; expose a callback/event back to View, and the View executes NavController or FragmentManager.

- MVVM
  - ViewModel emits one-off navigation events (SharedFlow/LiveData wrapper). Fragment/Activity observes and calls NavController.
  - Avoid holding NavController in ViewModel.

- MVI
  - View emits Intents; ViewModel reduces to State and emits Effects for navigation. Fragment subscribes to Effects and navigates via NavController. Unidirectional flow.

- Clean Architecture
  - Navigation stays in Presentation (Activity/Fragment). Domain is unaware of navigation. Data is unaware.
  - Presentation may adopt MVVM or MVI; both pair well with the Navigation Component.

---

## Scope (Activity vs Fragment vs NavGraph)
- Activity scope
  - Owns NavHostFragment and NavController setup, AppBarConfiguration, Toolbar/Drawer/BottomNav wiring, onSupportNavigateUp().

- Fragment scope
  - Calls findNavController() to navigate in response to UI or observed ViewModel events. Avoid storing NavController in fields; get it on demand.

- NavGraph scope
  - ViewModels can be scoped to a navigation graph (navGraphViewModels). Useful for multi-step flows (login, checkout, onboarding). Graph can have nested graphs.

- Back stack scope
  - Use popUpTo/popUpToInclusive and launchSingleTop to shape the back stack (e.g., clear login after success).

---

## Comparison of Navigation Approaches (Pros and Cons)
- Navigation Component
  - Pros: graph visualization; Safe Args; deep link handling; consistent back/Up; animated transitions; UI components integration; nested graphs; testing helpers.
  - Cons: learning curve; XML/Gradle setup; very bespoke flows may feel constrained.

- Manual FragmentManager
  - Pros: full control; no additional dependencies; good for tiny demos or highly customized flows.
  - Cons: verbose; error-prone arg passing; manual back/Up handling; no type-safe args; scalability issues.

- Multi-Activity
  - Pros: isolation between features; simple intent-based entry points.
  - Cons: shared state harder; transitions and shared elements across activities are trickier; more boilerplate.

- Navigation UI Components
  - Pros: standard Material patterns; minimal code with setupWithNavController; AppBarConfiguration manages Up vs drawer.
  - Cons: structure must match top-level destinations; advanced behaviors (multiple back stacks) require extra setup.

---

## Use Cases (Choose Approach Based on Needs)

### Jetpack Navigation Component (Nav Graph)
- Use when: most apps; type-safe args (Safe Args); deep links; animations; clear back stack control; multiple UI surfaces (drawer/bottom nav).
- Why: centralizes navigation, handles back/Up correctly, works with lifecycle and tooling (graph preview).
- Avoid when: extremely custom back stack requirements or non-fragment UIs where FragmentManager fits better; or a tiny app where manual is simpler.
- Core pieces: NavHostFragment, NavController, NavGraph XML, actions, Safe Args, AppBarConfiguration.
- Typical flow: User action -> ViewModel emits UiEvent -> Fragment consumes and calls findNavController().navigate(Directions).
- Back stack tips: use popUpTo/popUpToInclusive and launchSingleTop to avoid duplicates and clear stacks (e.g., after login).
- Deep links: declare in nav graph for consistent routing from outside the app.

### Manual FragmentManager / FragmentTransaction (and Intent)
- Use when: lightweight demos or highly custom transitions/back logic not suited to NavComponent.
- Why: full control of transactions and back stack.
- Avoid when: app grows; manual arg passing and back handling become error-prone.
- Fragments: parentFragmentManager.beginTransaction().replace(...).addToBackStack(name).commit().
- Activities: startActivity(Intent(this, TargetActivity::class.java)); optionally finish() current.

### Multi-Activity vs Single-Activity
- Multi-Activity: use for truly distinct areas (settings, external intents) or legacy flows.
- Single-Activity: preferred modern approach with fragments + NavComponent; simpler shared ViewModels and transition control.

### Navigation UI Components (drawer, bottom nav, app bar, tabs)
- Use when: top-level destinations (AppBarConfiguration) and standard Material navigation patterns.
- Why: built-in integration with NavController via setupWithNavController().
- Avoid when: bespoke designs that don’t map to these surfaces.

### Deep links / app links / notifications
- Use when: need external entry points (URLs, intents) or navigate from notifications. Prefer NavComponent deepLink support.

---

## Navigation Dependencies (Gradle)
- Built-in without Navigation Component:
  - Activity-to-Activity via Intent
  - FragmentManager transactions (androidx.fragment is included/transitive in most setups; add fragment-ktx for Kotlin extensions).
- Add these for Jetpack Navigation Component (Nav Graph, Safe Args, UI components):
```gradle
// Module-level build.gradle.kts (app module)
dependencies {
  implementation(libs.androidx.navigation.fragment) // NavHostFragment, NavController for fragments
  implementation(libs.androidx.navigation.ui)       // AppBarConfiguration, setupWithNavController helpers
}

// Project-level build.gradle.kts (settings for Safe Args plugin)
// buildscript { dependencies { classpath("androidx.navigation:navigation-safe-args-gradle-plugin:<version>") } }

// Module-level build.gradle.kts: apply the plugin
plugins {
  id("androidx.navigation.safeargs.kotlin")
}
```

--- 

## Navigation Actions
### Actions
- What is an action?
  - A link defined in the nav graph XML that connects a source destination to a target destination.
  - Optional attributes shape back stack and animations (popUpTo, inclusive, launchSingleTop, restoreState, enter/exit animations).

### Arguments 
- Define on destinations in nav graph XML.
- Types: primitive (int, long, string, boolean, float, etc.), enum, Parcelable, Serializable, arrays, lists.
- defaultValue attribute for optional args.
- Purpose/use cases: pass IDs, filters, serialized lightweight models, or flags needed by the target; avoid passing heavy objects/contexts.
```xml
<!-- res/navigation/nav_graph.xml -->
<fragment
    android:id="@+id/detailFragment"
    android:name="com.example.DetailFragment">
  <argument
      android:name="id"
      app:argType="long"
      android:defaultValue="-1" />
</fragment>
```

- Passing args
  - Directions (Safe Args, type-safe):
```kotlin
val action = ExampleFragmentDirections.actionExampleToDetail(id = 42L)
findNavController().navigate(action)
```
  - Bundle (by id):
```kotlin
val args = bundleOf("id" to 42L)
findNavController().navigate(R.id.action_example_to_detail, args)
```

### Safe Args setup
- Enable the Safe Args Gradle plugin (see Navigation Dependencies above). Sync project; Directions/Args classes will be generated.

### Global actions
- Declare under the root <navigation> in /res/navigation/nav_graph.xml to be callable from any destination.

- Receiving args in Fragment and passing to ViewModel
```kotlin
// DetailFragment.kt
class DetailFragment : Fragment(R.layout.fragment_detail) {
  private val args: DetailFragmentArgs by navArgs()
  private val vm: DetailViewModel by viewModels() // SavedStateHandle will contain nav args
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Option A: let ViewModel read from SavedStateHandle itself
    // Option B: pass explicitly if your VM doesn’t use SavedStateHandle
    vm.onLoad(args.id)
  }
}

// Hilt ViewModel (SavedStateHandle injected automatically)
@HiltViewModel
class DetailViewModel @Inject constructor(private val state: SavedStateHandle) : ViewModel() {
  val idFromArgs: Long = state["id"] ?: -1L
  fun onLoad(id: Long) { /* load with id */ }
}

// Vanilla ViewModel (SavedStateHandle available via default factory)
class DetailViewModel2(private val state: SavedStateHandle) : ViewModel() {
  val idFromArgs: Long = state["id"] ?: -1L
}
```

---

## Layout XML and Kotlin (Syntax)
### Toolbar (MaterialToolbar)
- Options: 
  - ActionBar (legacy)
  - Toolbar/MaterialToolbar (recommended)
  - CollapsingToolbarLayout (coordinator behaviors)
  - Compose TopAppBar (Compose apps)
- Scope:
  - Activity-level is global
  - Fragment-level is per-screen
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:title="@string/app_name" />
```
// Kotlin wiring (Activity-level)
```kotlin
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var appBarConfig: AppBarConfiguration
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)
    val navController = findNavController(R.id.nav_host_fragment)
    appBarConfig = AppBarConfiguration(setOf(R.id.homeFragment, R.id.dashboardFragment))
    setupActionBarWithNavController(navController, appBarConfig)
  }
  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
  }
}
```

### Drawer + NavigationView
- Recommended: DrawerLayout + NavigationView at Activity root; bind to NavController with setupWithNavController.
  - avoid inside fragments.
- Alternatives: 
  - NavigationRailView for large screens
  - in Compose: ModalNavigationDrawer.

```xml
<androidx.drawerlayout.widget.DrawerLayout
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
  <!-- content + toolbar + NavHost here -->
  <com.google.android.material.navigation.NavigationView
      android:id="@+id/nav_view"
      android:layout_width="wrap_content"
      android:layout_height="match_parent"
      android:layout_gravity="start"
      app:menu="@menu/drawer_menu" />
</androidx.drawerlayout.widget.DrawerLayout>
```
// Kotlin wiring (Activity-level)
```kotlin
val navController = findNavController(R.id.nav_host_fragment)
val appBarConfig = AppBarConfiguration(
  topLevelDestinationIds = setOf(R.id.homeFragment, R.id.dashboardFragment),
  drawerLayout = binding.drawerLayout
)
setupActionBarWithNavController(navController, appBarConfig)
binding.navView.setupWithNavController(navController)
```

### BottomNavigationView
- Set up in Activity; typical pattern is a single NavHost. 
- Advanced: multiple NavHosts (one per tab) to preserve each back stack.
- Alternatives: 
  - BottomAppBar + FAB
  - NavigationRail for tablets
  - Compose: NavigationBar.
```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottom_nav"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_menu" />
```
// Kotlin wiring (Activity-level)
```kotlin
val navController = findNavController(R.id.nav_host_fragment)
binding.bottomNav.setupWithNavController(navController)
```

### Menus 
- Options: 
  - Toolbar options menu (Activity/Fragment)
  - PopupMenu, Contextual Action Mode (ActionMode)
  - NavigationView menu
  - BottomNavigationView menu
```xml
<!-- res/menu/bottom_menu.xml -->
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item android:id="@+id/homeFragment" android:title="@string/home" android:icon="@drawable/ic_home" />
  <item android:id="@+id/dashboardFragment" android:title="@string/dashboard" android:icon="@drawable/ic_dashboard" />
</menu>
```

- Kotlin wiring examples
```kotlin
// Toolbar options menu in a Fragment (MenuHost API)
class ExampleFragment : Fragment(R.layout.fragment_example) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
      }
      override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> { findNavController().navigate(R.id.settingsFragment); true }
        else -> false
      }
    }, viewLifecycleOwner, Lifecycle.State.STARTED)
  }
}

// Drawer / BottomNav wiring in Activity
setupActionBarWithNavController(navController, appBarConfig)
binding.navView.setupWithNavController(navController)
// binding.bottomNav.setupWithNavController(navController)
```

---

## Navigation Triggers
- User actions (button clicks, menu selections)
- ViewModel events (SharedFlow, LiveData)
- Lifecycle events (e.g., onViewCreated)
- External events (deep links, notifications)

### Activity -> Activity
- Standard: Intent to target Activity.
- Other options: 
  - Activity Result API (registerForActivityResult) for results
  - TaskStackBuilder for notifications/back stack
  - deep links via Intent with data URI
```kotlin
startActivity(Intent(this, TargetActivity::class.java))
// Optional: finish() to remove current from back stack
```

### Activity -> Fragment
- Standard: navigate via the Activity’s NavController to a destination in its NavHost.
- Other options: 
  - FragmentManager transactions (replace/add) if not using NavComponent.
    - this might happen when the Activity hosts multiple fragments outside a NavHost.
```kotlin
val navController = findNavController(R.id.nav_host_fragment)
navController.navigate(R.id.homeFragment)
```

### Fragment -> Fragment
- Standard: findNavController().navigate() using Directions (Safe Args) or destination id + Bundle.
- Other options: 
  - parentFragmentManager/childFragmentManager transactions for custom/nested flows.
```kotlin
val action = ExampleFragmentDirections.actionExampleToDetail(42L)
findNavController().navigate(action)
// or by id + Bundle
findNavController().navigate(R.id.action_example_to_detail, bundleOf("id" to 42L))
```

### Fragment -> Activity
- Standard: startActivity(Intent(...)) from Fragment, optionally finish Activity.
- Other options: 
  - Activity Result API for returning data
  - finishAndRemoveTask() for exit flows
  - startActivity with flags for task behavior.
```kotlin
requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
requireActivity().finish()
```

### Additional Trigger Syntax
- Back stack shaping example (clear login)
  - can be completed via XML attributes on the action or via navOptions in code.
- popUpTo: removes destinations up to the specified one from the back stack.
- inclusive: if true, also removes the specified destination.
- launchSingleTop: if true, avoids multiple copies of the destination on top of the stack
```kotlin
findNavController().navigate(
  ExampleFragmentDirections.actionLoginToHome(),
  navOptions {
    popUpTo(R.id.loginFragment) { inclusive = true }
    launchSingleTop = true
  }
)
```
```xml
<action
    android:id="@+id/action_login_to_home"
    app:destination="@id/homeFragment"
    app:popUpTo="@id/loginFragment"
    app:popUpToInclusive="true"
    app:launchSingleTop="true" />
```

- OnClick navigation (Fragment)
```kotlin
binding.nextBtn.setOnClickListener {
  findNavController().navigate(ExampleFragmentDirections.actionExampleToNext())
}
```

- Manual Fragment transaction
```kotlin
parentFragmentManager.beginTransaction()
  .setReorderingAllowed(true)
  .replace(R.id.container, DetailFragment.newInstance(id))
  .addToBackStack(null)
  .commit()
```

---

## Setup Nav Graph (recommended)
This section shows the minimal, end-to-end setup for the Navigation Component in a single-Activity app: where each piece lives and how it connects.

### 1) Add a NavHost to your Activity layout
```xml
<!-- res/layout/activity_main.xml -->
<!-- CoordinatorLayout : 
        - coordinates scrolling/insets between children
        - enables `AppBarLayout` collapse/expand with content scroll
        - anchors/moves `FloatingActionButton` and offsets it for `Snackbar`.
 -->
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

<!-- Toolbar at the top of the screen -->
  <com.google.android.material.appbar.MaterialToolbar
      android:id="@+id/toolbar"
      app:title="@string/app_name" />

  <!-- NavHost container: displays destinations from the navigation graph -->
  <!-- app:navGraph links this host to your graph resource -->
  <fragment
      android:id="@+id/nav_host_fragment"
      android:name="androidx.navigation.fragment.NavHostFragment"
      android:layout_marginTop="?attr/actionBarSize"
      app:defaultNavHost="true"
      app:navGraph="@navigation/nav_graph" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 2) Create the navigation graph XML
```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- res/navigation/nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph"
    app:startDestination="@id/homeFragment">

  <!-- Home destination (start) -->
  <fragment
      android:id="@+id/homeFragment"
      android:name="com.example.allnotes.ui.home.HomeFragment"
      android:label="@string/home">
    <action
        android:id="@+id/action_home_to_detail"
        app:destination="@id/detailFragment" />
  </fragment>

  <!-- Detail destination with a typed argument and a deep link -->
  <fragment
      android:id="@+id/detailFragment"
      android:name="com.example.allnotes.ui.detail.DetailFragment"
      android:label="@string/detail">
    <argument
        android:name="noteId"
        app:argType="long"
        android:defaultValue="-1" />
    <deepLink
        app:uri="allnotes://note/{noteId}" />
  </fragment>

  <!-- Nested auth graph example (scopes shared VM to this flow) -->
  <navigation
      android:id="@+id/auth_graph"
      app:startDestination="@id/loginFragment">
    <fragment
        android:id="@+id/loginFragment"
        android:name="com.example.allnotes.ui.auth.LoginFragment"
        android:label="@string/login">
      <action
          android:id="@+id/action_login_to_home"
          app:destination="@id/homeFragment"
          app:popUpTo="@id/auth_graph"
          app:popUpToInclusive="true"
          app:launchSingleTop="true" />
    </fragment>
  </navigation>
</navigation>
```

What these parts are:
- NavHostFragment: a container view that displays destinations from a nav graph.
- NavController: the router owned by the NavHost; Fragments obtain it with findNavController().
- Navigation graph (XML): declares destinations and how to move between them (actions). Can include nested graphs for flows.
- Destinations: typically Fragments; can also be Activities or DialogFragments.
- Actions: edges between destinations; can configure back stack and animations.
- Arguments: typed inputs for destinations (consumed via navArgs or SavedStateHandle).
- Deep links: external entry points that route directly to a destination with args.
- Nested graphs: group destinations, share state, and scope ViewModels to the group.

### 3) Wire the Activity to the NavController and app bar
```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
  private lateinit var appBarConfig: AppBarConfiguration
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)

    val navController = findNavController(R.id.nav_host_fragment)
    // List top-level destinations here (no Up arrow when on them)
    appBarConfig = AppBarConfiguration(setOf(R.id.homeFragment))
    setupActionBarWithNavController(navController, appBarConfig)
  }
  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
  }
}
```

### 4) Navigate from a Fragment using Safe Args
```kotlin
// HomeFragment.kt
class HomeFragment : Fragment(R.layout.fragment_home) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.findViewById<View>(R.id.openDetailBtn).setOnClickListener {
      val action = HomeFragmentDirections.actionHomeToDetail(noteId = 123L)
      findNavController().navigate(action)
    }
  }
}

// DetailFragment.kt
class DetailFragment : Fragment(R.layout.fragment_detail) {
  private val args: DetailFragmentArgs by navArgs()
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val id = args.noteId // 123L
    // Use id...
  }
}
```

### 5) Scope a ViewModel to a navigation graph (multi-step flows)
- Used for login, onboarding, checkout flows. 
```kotlin
// Example: share VM across login screens inside @id/auth_graph
@HiltViewModel
class AuthViewModel @Inject constructor(private val state: SavedStateHandle) : ViewModel() {
  var username: String? = state["username"]
}

// In a Fragment inside auth_graph
class LoginFragment : Fragment(R.layout.fragment_login) {
  private val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_graph)
}
```

### Quick smoke test
- App builds, start destination shows.
- Toolbar title changes when navigating; Up button works.
- Tapping a button in Home navigates to Detail and noteId is received.
- Try deep link: adb shell am start -d "allnotes://note/42" <your.package>.
- After login success, navigating to Home pops the auth graph off the back stack.

### Troubleshooting
- IllegalArgumentException: navigation destination not found -> Ensure you’re calling findNavController() from a view in the NavHost and the action id exists.
- Directions class not generated -> Sync Gradle and confirm the Safe Args plugin is applied to the module.
- Up button closes the app -> Add top-level destinations to AppBarConfiguration.
- Back stack keeps duplicating screens -> Set launchSingleTop and/or popUpTo on actions where appropriate.

---

## Additional Notes (quick recap)
- Keep navigation calls in the View (Fragment/Activity). ViewModel emits events (SharedFlow).
- Use AppBarConfiguration with top-level destination IDs to get proper Up vs drawer behavior.
- After login, use popUpTo(nav_graph_start) { inclusive = true } to remove login from back stack.
- For bottom navigation with multiple back stacks, consider Navigation Component advanced samples (one NavHost per tab or using restoration APIs).
- For deep-links/app-links, declare in the nav graph and manifest; test with adb.
- Testing: use navigation-testing and a TestNavHostController in instrumentation; unit test ViewModel emits the right UiEvents.
- Compose apps: use androidx.navigation.compose; patterns are analogous (state in ViewModel, events trigger navigate()).
- Animations and options
  - In XML: app:enterAnim, exitAnim, popEnterAnim, popExitAnim on the action.
  - In code: navOptions { anim { enter = R.anim.slide_in_right; exit = R.anim.fade_out } }
