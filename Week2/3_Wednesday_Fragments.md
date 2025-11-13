# Task
- Create an “Empty Views Activity” Project Template
- Convert it to have Navigation Graph implementation
- Once you have the exact same views with navigation (Home, Gallery, SlideShow) → (feel free to use different names & styles & icons & menus)
- Then, add detail page for each which should navigate further

## Fragments
- can display multiple fragments at once (on a single view)
- Fragment lifecycle is tied to its host Activity but has its own view lifecycle.
    - different from Activity lifecycle.
    - onAttach -> onCreate -> onCreateView -> onViewCreated -> onStart -> onResume -> onPause -> onStop -> onDestroyView -> onDestroy -> onDetach
    - New lifecycles:
        - onAttach: fragment is associated with its activity.
        - onCreateView: inflate the fragment's layout.
        - onViewCreated: view setup (e.g., view binding, observers).
        - onDestroyView: clean up view-related resources (e.g., nullify binding).
        - onDetach: fragment is disassociated from its activity.
- cannot have a fragment without an activity. because fragments are tied to the activity lifecycle.
- Fragments should not be used as a replacement for Activities. Use them for modular UI components within an Activity.
- Always clear view references in onDestroyView to avoid memory leaks.
- recommended to use 1 activity for entire app, and multiple fragments for different screens.
    - similar to single page web apps. is used to reduce complexity of multiple activities. and performance.
    - this doesn't mean you can't have multiple activities, but it's a common pattern.
    - easier to manage navigation and shared ViewModels. Use navigation graph to handle transitions.
        - navigation graph is an XML file that defines all the possible paths through your app's UI.
        - can visually show you the flow between screens.
    - NavHost: holds the current visible fragment.
    - NavController: manages navigation and back stack. A -> B -> C, back goes to B, back again goes to A.
        - NavGraph has already determined possible paths.
- We initialize our views in onCreateView.

- res/navigation has a visual of connections between fragments
    - similar to api gateway - we can see all the possible paths through our app
    - we can add actions to connect fragments either by dragging or by right clicking a fragment and selecting "Add Action"
    - or by editing the XML directly
    - use NavController to navigate between fragments
    - can determin app:startDestination in nav_graph.xml to set the initial fragment
        - same as launchMode in activities

        - can create a new fragment inside the navigation graph.
            - when prompted for the package, use the file directory you want your fragment to be in.
            - we added .ui.login to the package name to put it in the ui/login directory. while selecting a login fragment.
            - grab the action id and add it to the button's onClickListener to navigate. - example in SlideShowFragment.kt

### 
- Notice /res/layout/activity_main.xml has a DrawerLayout as the root view.
    - This is the container for the entire screen, including the sidebar and main content area.
    - Inside the DrawerLayout, we have a NavigationView which represents the sidebar menu.
    - The NavigationView contains a header layout (nav_header_main.xml) and a menu (drawer_menu.xml).
        - res/menu - is where we define menu items for sidebar nav. drawer_menu.xml
        - this is just the visual part (View)
        - we grab id's from activity_main.xml to handle click events in MainActivity.kt
    - The main content area is represented by a FragmentContainerView which will host our fragments.

- app_bar_main.xml - is the layout for the top app bar (toolbar).
    - This includes the title and any action items (like settings).
    - We set this toolbar as the ActionBar in MainActivity.kt using setSupportActionBar().
    - will be visible on all screens since it's part of the main activity layout.

- we can nest xml id's inside parents to create a hierarchy
    - example: nav_header_main.xml is nested inside NavigationView in activity_main.xml
    - this allows us to easily reference and manipulate these views in code.

- NavHost - defaultNavHost="true" in FragmentContainerView
    - This tells the NavController to manage navigation within this container.
    - The NavController is obtained in MainActivity.kt using findNavController().
    - NavGraph links fragments and defines navigation paths.
        - use the file path to point to the correct /res/navigation/mobile_navigation.xml

- res/menu must be the same as the navigation graph (res/navigation)

#### Fragment:
- extend Fragment and pass the layout resource ID to the constructor (e.g., Fragment(R.layout.fragment_example)).
- override lifecycle methods as needed:
- onAttach →
   - Purpose: Fragment gets its Context/Activity. 
   - Do: DI, get callbacks/parent interfaces. Avoid heavy work.
- onCreate → 
   - Purpose: Non‑UI init; survives view recreation. 
   - Do: read arguments, setHasOptionsMenu, create adapters/models not tied to views, start long‑lived ops independent of the view.
- onCreateView →
   - Purpose: Inflate the view hierarchy. 
   - Do: inflate ViewBinding/DataBinding only. No observers/listeners yet.
- onViewCreated → 
   - Purpose: View is ready. 
   - Do:
     - bind views, 
     - set adapters/listeners
     - observe LiveData/Flow with viewLifecycleOwner
     - launch coroutines with repeatOnLifecycle(STARTED)
     - restore view state
     - trigger initial UI updates.
- onStart → 
   - Purpose: Fragment is visible. 
   - Do: register receivers, sensors, start location/camera preview, connect to services. Pair with onStop.
   - Use onStart/onStop for external resources
- onResume → 
   - Purpose: Fragment is in foreground and interactive. 
   - Do: start/resume animations, exclusive focus work, begin input. Pair with onPause.
   - onResume/onPause for UI‑only/transient work.
- onPause → 
   - Purpose: Losing focus. 
   - Do: pause animations, commit lightweight state, hide keyboard, stop transient UI work.
   - onResume/onPause for UI‑only/transient work.
- onStop → 
   - Purpose: No longer visible. 
   - Do: unregister receivers/sensors, stop camera/location/services, persist heavier state.
   - Use onStart/onStop for external resources
- onDestroyView → 
   - Purpose: View hierarchy going away. 
   - Do: nullify binding, clear view references/callbacks, adapter.view = null, remove pending handlers. Keep non‑view state.
   - Never hold view references past onDestroyView; always null binding.
- onDestroy → 
   - Purpose: Fragment finishing for good. 
   - Do: final cleanup not tied to views, cancel fragment‑scoped jobs.
- onDetach → 
   - Purpose: Lost Activity. 
   - Do: clear Activity/callback refs.

```kotlin
class ExampleFragment : Fragment(R.layout.fragment_example) {

    private var _binding: FragmentExampleBinding? = null
     // initialize on onCreateView, clear in onDestroyView
    private val binding get() = _binding!!

    // Fragment-scoped VM
    private val uiVm: UiViewModel by viewModels()
    // Activity-shared VM
    private val authVm: AuthViewModel by activityViewModels()
    // NavGraph-scoped VM (shared across a graph)
    private val sharedVm: SharedVm by navGraphViewModels(R.id.main_graph)
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Non‑UI init: args, menu, adapters independent of views
        // setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // UI wiring: adapters, listeners
        binding.recycler.adapter = ExampleAdapter()

        // Observe with view lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    uiVm.uiState.collect { state ->
                        // render UI
                    }
                }
                launch {
                    uiVm.events.collect { event ->
                        // one‑off events
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Register receivers/sensors/start camera or location
    }

    override fun onResume() {
        super.onResume()
        // Resume animations / acquire focus
    }

    override fun onPause() {
        super.onPause()
        // Pause animations / save lightweight UI state
    }

    override fun onStop() {
        super.onStop()
        // Unregister receivers/sensors/stop camera or location
    }

    override fun onDestroyView() {
        // Clear view references to avoid leaks
        binding.recycler.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
```


# Common Initializations:
## Navigation
### Activity:
- Wire the graph in onCreate.
- Get the NavController via findNavController(R.id.nav_host_fragment_content_main).
- Set up AppBarConfiguration.
  - Call setupActionBarWithNavController and connect NavView/BottomNav with setupWithNavController.
## Fragment:
- Call findNavController() in response to UI or observed ViewModel events (typically in onViewCreated). Prefer Safe Args directions.
## ViewModel:
- Do not hold a NavController; emit navigation events that the Fragment observes.
  - Emit navigation events via a SharedFlow or LiveData in the ViewModel.

```kotlin
// UiEvent.kt
sealed class UiEvent {
    data class GoToDetail(val id: Long) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    object ShowToast : UiEvent()
}
```

```kotlin
// ExampleViewModel.kt
class ExampleViewModel : ViewModel() {
    // extraBufferCapacity allows emitting without suspending if no collectors
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun onCtaClicked() = viewModelScope.launch {
        _events.emit(UiEvent.ShowToast)
    }

    fun goToDetail(id: Long) = viewModelScope.launch {
        _events.emit(UiEvent.GoToDetail(id))
    }

    fun showError(msg: String) = viewModelScope.launch {
        _events.emit(UiEvent.ShowError(msg))
    }
}
```

- What is viewLifecycleOwner  
   - It is the LifecycleOwner tied to a Fragment’s view (from onCreateView to onDestroyView). Use it to scope UI work so collectors/observers stop when the view is destroyed, avoiding leaks.
- lifecycleScope
   - It’s a CoroutineScope provided by AndroidX. Use viewLifecycleOwner.lifecycleScope inside Fragments so jobs cancel at onDestroyView. fragment.lifecycleScope lasts until onDestroy.
- launch is a co-routine?
   - launch { ... } starts a new coroutine in that scope and returns a Job. In a lifecycleScope, it runs on Main by default.
- lifecycle
   - viewLifecycleOwner.lifecycle is the Lifecycle for the Fragment’s view. States flow from CREATED → STARTED → RESUMED and back, and become DESTROYED at onDestroyView (while the Fragment may still be alive).
- repeatOnLifecycle params
   - repeatOnLifecycle(Lifecycle.State.STARTED) comes from androidx.lifecycle. You pick the minimum active state (usually STARTED) so the block runs only while the lifecycle is at least that state and cancels when it drops below.
- uiVm / events / collect
   - uiVm is your ViewModel. events is typically a Flow<UiEvent> (often SharedFlow). collect { ... } is the Flow terminal operator that receives values. For LiveData you’d use observe(...) instead.
- when with multiple events
   - Model events as a sealed class and handle each case in a when. Make it exhaustive (no else) so the compiler warns if you add a new event and forget to handle it.
- is ShowToast the fallback "else"?
   - No. It’s just one case. If your when covers all sealed subclasses, no else is needed. Use else only when you can’t be exhaustive.


### Navigation notes (separate from streams)

- Navigation using NavController (Fragments).
    - NavController (Fragments): use findNavController().navigate(R.id.action_current_to_target).

- we need to bind our drawer layout
- binding.navView.setupWithNavController(navController)
    - connects the BottomNavigationView with the NavController to handle navigation automatically when items are selected.
    - setSupportActionBar(binding.appBarMain.toolbar)
        - sets the Toolbar as the ActionBar for the activity, allowing it to display the title and handle menu items.
    - setupActionBarWithNavController(navController, appBarConfiguration/drawerLayout)
        - this sets up the ActionBar (top app bar)/drawer to work with the NavController and handle the Up button correctly based on the AppBarConfiguration.

- State management:
    - ViewBinding: initialize in onCreateView and clean up in onDestroyView.
    - ViewModel: obtain instance using by viewModels() or ViewModelProvider.

cmd + option + L to reformat code in Android Studio
- xml tabs children
- essentially a linter inside android studio

- study different kinds of toolbars, but not super important
    - actionbar vs toolbar
    - look up how to set up a toolbar (setSupportActionBar(toolbar), binding, etc.)

- need to tell in our onCreate what the main fragments are vs sub fragments
    - main fragments - home, dashboard, notifications
    - sub fragments - everything else
    - this is done in the AppBarConfiguration setup
        - AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications), drawerLayout)
        - this tells the NavController which fragments are top level destinations (main fragments)
        - this affects the behavior of the Up button and the drawer icon in the toolbar
```kotlin
appBarConfiguration = AppBarConfiguration(
    setOf(
        R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
    ), drawerLayout
)
```
- need to setup nav up in onSupportNavigateUp
    - this handles the Up button behavior in the toolbar (stack vs drawer)
```kotlin
override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment_content_main)
  // if we are on a top level destination (main fragment), open the drawer instead of going back
  // else, go back in the stack
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
```

- if you use fragment container view, you need to use findNavController(R.id.nav_host_fragment_content_main) to get the NavController
    - if you use NavHostFragment directly, you can use NavHostFragment.findNavController(this)

- use fragments for visuals is preferred with 1 activity
    - intent will be used to access 3rd party apps (camera, gallery, payment etc.)


- You cannot have a Fragment without an Activity
- You can replace/use a lot of fragment with same Activity instance
- It’s recommended to use one Activity for entire app
- You can display multiple fragments at once
- It has easier navigation use cases
    - Navigation Graph → retains all possible destinations
    - NavHost → holds the current visible Fragment
        - XML
            - Name
            - defaultNavHost
            - navGraph
    - NavController → handles the navigation aspect of moving from Fragment A to Fragment B

- Lifecycle (onCreateView, onViewCreated, onDestoryView)
    - onAttach
    - onCreate
    - onCreateView
    - onViewCreated
    - onStart
    - onResume
    - onPause
    - onStop
    - onDestroyView
    - onDestroy
    - onDetach

XML

- android: → is for default view settings provided by android library
- app: → is for specific view properties, also for custom libraries
- tools: → only for developer settings, does not have any impact on user
- Theme Settings: https://developer.android.com/develop/ui/views/theming/themes

