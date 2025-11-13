# Task
- Create 3 Activities
  - experiment with LinearLayout, RelativeLayout, and ConstraintLayout design
  - Use Intents to navigate between activities
  - (optional) back button handling
  - (optional) ViewModel to persist data across configuration changes

# Activity
## Lifecycle
### OnCreate
- Initialize UI components
- which includes setting up buttons, text fields, and other interactive elements
### OnStart
- build the UI and load any necessary data
### OnResume
- refresh the UI and ensure everything is up to date
- display to the user and take interactions
### OnPause
- save any changes made by the user
### OnStop
- release resources that are not needed while the activity is not visible
- undo the UI
### OnDestroy
- clean up any remaining resources

## Layout
- when you do configuration changes it recreates the activity (THIS IS IMPORTANT)
    - rotate scree, change language, change font size, change theme, etc.


## Launch Modes
- Activities have different launch modes that control how new instances are created and how they interact with the back stack.
- These modes are specified in the AndroidManifest.xml using the android:launchMode attribute.
### Standard (default)
- Behavior: Every launch creates a new instance, even if one already exists in the back stack.
- Back stack: Multiple instances can stack up.
- (A->B->A->C creates two A instances in the stack.)

### SingleTop
- Behavior: If an existing instance is already at the top of its task’s back stack, that instance is reused and onNewIntent() is called. Otherwise, a new instance is created.
- Mental model: Like a stack of plates—if the top plate is the one you need, reuse it; if not, put another plate on top.
- (A->B->A->C then launching A again calls onNewIntent() on the existing top A.)
    - A was not at the top when launched the second time, so a new instance was created.
- (A->B->(call B again, B is already at top) calls onNewIntent() on B.)

### SingleTask
- Behavior: If an instance already exists in any task, that task is brought to the foreground and the intent is delivered to the existing instance via onNewIntent(). Any activities above it in that task are destroyed. If no instance exists, a new task is created with this activity as its root.
- Note: Often used for “main” entry points or deep links where you want a single logical instance.
- (A->B->C->D->(call B again) -> stack removes C and D, brings B to top, calls onNewIntent() on B.)

### SingleInstance
- Behavior: The activity is the only one in its task; if it launches another activity, that other activity appears in a different task.
- Status: Deprecated in newer Android versions. Prefer SingleTask with an appropriate taskAffinity or follow current platform guidance in the docs.
- (A-> B launches B in a new task; A remains alone in its own task.)

### Scenario
- just logged on, then press back, where do we want to go?
    - Standard: back goes to previous activity in the stack.
    - SingleTop: back goes to previous activity in the stack; if the top is reused, it behaves like standard.
    - SingleTask: back goes to the activity below in the same task; if it was brought to front, it goes back to whatever was below it before.
    - SingleInstance: back exits the app since it’s the only activity in its task.

### Tips
- Use logs including the activity’s hashCode() to see whether the same instance handled the Intent (same hashCode) or a new instance was created.
- For deep links and “home” flows, singleTask is typically preferred to avoid duplicate stacks.
- For notification taps that might re-open the same screen, singleTop prevents duplicate instances when the screen is already on top.




## UI and Views
### LinearLayout
- arranges elements in a single row (horizontal) or column (vertical)
- android:orientation="vertical" - default is vertical but can be horizontal
  - vertical - will always be vertical UI (top to bottom) - default
- horizontal - always align left to right
- weight - how much space each element takes up (1 = equal space, 2 = double space)
- gravity - align elements inside the layout (center, start, end, top, bottom)
- layout_gravity - align the layout itself inside its parent (center, start, end, top, bottom)

### RelativeLayout
- can align based on parent or sibling views
- use drag and drop in design view to create relationships
  - tweak in XML
- android:layout_alignParentTop="true" - align to the top of the parent
- android:layout_alignParentBottom="true" - align to the bottom of the parent
- android:layout_alignParentStart="true" - align to the start of the parent (left in LTR, right in RTL)
- android:layout_alignParentEnd="true" - align to the end of the parent (right in LTR, left in RTL)
- android:layout_centerInParent="true" - center in the parent
- android:layout_centerHorizontal="true" - center horizontally in the parent
- android:layout_centerVertical="true" - center vertically in the parent
- android:layout_above="@id/viewId" - position above another view
- android:layout_below="@id/viewId" - position below another view
- android:layout_toStartOf="@id/viewId" - position to the start of another view
- android:layout_toEndOf="@id/viewId" - position to the end of another view
- android:layout_alignTop="@id/viewId" - align top with another view
- android:layout_alignBottom="@id/viewId" - align bottom with another view
- android:layout_alignStart="@id/viewId" - align start with another view

### ConstraintLayout
- more flexible and powerful than LinearLayout and RelativeLayout
    - major benefit is all views are flat hierarchy (no nested views)
- drag and drop in design view to create constraints
    - tweak in XML
- guideline - invisible lines to help align views (horizontal or vertical)
- barrier - dynamic guidelines that adjust based on the size of views
- chain - group of views that can be aligned and spaced together
- bias - adjust the position of a view between two constraints (0 = start, 1 = end)
- dimensionRatio - maintain a specific aspect ratio for a view (width:height)
- goneMargin - margin to apply when a view is set to GONE (not visible and not taking up space)
- layout_constraintTop_toTopOf="parent" - constrain top to top of parent
- layout_constraintBottom_toBottomOf="parent" - constrain bottom to bottom of parent
- layout_constraintStart_toStartOf="parent" - constrain start to start of parent
- layout_constraintEnd_toEndOf="parent" - constrain end to end of parent
- layout_constraintTop_toBottomOf="@id/viewId" - constrain top to bottom of another view
- layout_constraintBottom_toTopOf="@id/viewId" - constrain bottom to top of another view
- layout_constraintStart_toEndOf="@id/viewId" - constrain start to end of another view
- layout_constraintEnd_toStartOf="@id/viewId" - constrain end to start of another view
- layout_constraintHorizontal_bias="0.5" - horizontal bias (0 = start, 1 = end)

## Link View Element IDs (XML) to Activity/Fragment (Kotlin)
- findViewById - find a view by its ID (deprecated in favor of View Binding)
    - findViewById<Button>(id = R.id.button).setOnClickListener - set a click listener on a button
    - findViewById<TextView>(id = R.id.textView).text = "Hello World" - set text on a TextView
- View Binding - type-safe way to access views (enabled in build.gradle)

- setContentView - set the layout for the activity
- onClick - handle button clicks
- onTouch - handle touch events
- onLongClick - handle long button presses

## Intents
- Intent - messaging object used to request an action from another app component (activity, service, broadcast receiver)
- Explicit Intent - specify the target component by name (used to start activities within the same app)
    - A to B activity
    - Need to specify additional Activities in the manifest file. So when we try to use Intent to go from A to B, it knows about B because it is in the manifest file
- Implicit Intent - do not specify the target component (used to start activities in other apps)
    - like pdf viewer, it asks which app to use to open the pdf
- startActivity - start a new activity, means navigating to a new screen (Activity A to Activity B)
- finish - close the current activity and return to the previous one
  - by popping it off the stack
  - preferred: onBackPressedDispatcher.onBackPressed() - programmatically trigger the back button behavior

## Bindings
- View Binding - type-safe way to access views (enabled in build.gradle)
    - using the UI components directly in the activity or fragment without needing to use findViewById
    - enable it by mentioning it in the build.gradle file(module level)
- Data Binding - bind UI components in layouts to data sources in the app (enabled in build.gradle)
    - two way communication: using the UI to update the data source and using the data source to update the UI
- Two-way Data Binding - allows for automatic updates between the UI and data sources (e.g., EditText and ViewModel)
- Binding Adapters - custom binding logic for specific view attributes (e.g., loading images with Glide or Picasso)
- ViewBinding (recommended for XML views)
    - Enable in module Gradle: buildFeatures { viewBinding = true }.
    - Activity pattern: create binding in onCreate and setContentView(binding.root).
    - Fragment pattern: create _binding in onCreateView, use binding in onViewCreated, set _binding = null in onDestroyView.

- DataBinding (optional advanced)
    - Adds binding expressions in XML; can bind LiveData directly. Slightly more setup than ViewBinding.


# When creating a new screen:
## AndroidManifest.xml:
- only if a new Activity is created. Fragments do not need to be declared here.
    - must add activities to the manifest file.

## res/layout:
- create a new XML layout file for the screen.

## Activity kt file:
- create a new Activity class (Kotlin) per screen

### Extend AppCompatActivity
- to access lifecycle methods and support library features.

### Setup viewBinding (recommended over findViewById)
- setup build.gradle (module)
    - enable viewBinding in android block with `buildFeatures {viewBinding { enabled = true }}`
    - allows for android to generate binding classes for each XML layout file.
        - follows naming convention of converting snake_case XML file names to PascalCase with "Binding" suffix.
            - e.g. activity_main.xml -> ActivityMainBinding
- setup Fragment.kt:
    - `private lateinit var binding: ActivityYourLayoutBinding` at the top of the class
        - lateinit allows you to initialize the binding variable later in onCreate.
        - This is necessary because the binding depends on the layout inflater which is only available in onCreate.
            - initialize binding in onCreate with binding = `ActivityYourLayoutBinding.inflate(layoutInflater)`
- Attach binding as our layout
    - setContentView(binding.root)
        - if you were using findViewById, you would do setContentView(R.layout.your_layout_file)
        - `R` is a generated class that contains references to all resources in your project.
- Setup binding.apply { // initialize views here }
    - use if you have multiple elements to initialize.
    - can drop "binding." prefix for each element inside the apply block.
    - reduces boilerplate code and potential for null pointer exceptions.

### Setup Listeners (buttons, text field changes, etc.)
- binding.elementId.setOnClickListener { // handle click }
- setOnClickListener { // handle click }
    - allows you to respond to button clicks or other view clicks.
- setOnCheckedChangeListener { _, isChecked -> // handle check change }
    - allows you to respond to check state changes in a CheckBox, Switch, or RadioButton.
- addTextChangedListener { // handle text change }
    - allows you to respond to text changes in an EditText or TextView.

### Setup Intent (navigation between activities):
- startActivity(Intent(this, TargetActivity::class.java))
    - this can be done inside a button click listener to navigate when the button is pressed.
    - use this@CurrentActivity
        - "this" refers to the current context
        - add "@" to explicitly specify which "this" you mean.
            - useful in nested scopes where "this" might refer to something else (like the listener).
- when going back ("Up" navigation)
    - use finish() or onBackPressedDispatcher.onBackPressed()
        - these both close (pop from stack) the current activity and return to the previous screen stack

### Setup ViewModel (persisting UI data and business logic)
- initialize a class that extends ViewModel to hold UI data and business logic.
    - naming convention: YourFeatureViewModel
    - helps separate UI code from data handling, making the code cleaner and easier to maintain.
    - data (variables, text input, etc.) survives configuration changes like screen rotations, app minimization, etc.
- initialize data field in ViewModel
    - use MutableLiveData for data that can change and needs to be observed by the UI.
        - best practice to make the MutableLiveData private and expose an immutable LiveData to the UI.
            - `private val _data = MutableLiveData<Type>()`
            - `val data: LiveData<Type> = _data`
    - need a separate field for each piece of data you want to track or use an object to group related data.
- initialize ViewModel in Activity
    - `private val viewModel: YourFeatureViewModel by viewModels()`
        - this uses the activity-ktx library to lazily initialize the ViewModel.
        - ensures the ViewModel is scoped to the Activity lifecycle.
- pass data between Activity and ViewModel
    - use viewModel.data.observe(this) { data -> // update UI with data }
        - this sets up an observer on the LiveData in the ViewModel.
        - whenever the data changes, the lambda function is called with the new data.
    - update data in ViewModel with viewModel.updateData(newValue)
        - create setter functions in your ViewModel to update the MutableLiveData.
        - call these functions from your Activity when you need to change the data (e.g., in response to user input).

#### Additional ViewModel notes:
- Dependencies:
    - lifecycle‑viewmodel‑ktx : for ViewModel support
    - lifecycle‑runtime‑ktx : for lifecycleScope and other lifecycle-aware components
    - (optional) lifecycle‑livedata‑ktx : if using LiveData
    - activity‑ktx : for viewModels() delegate
    - fragment‑ktx : for fragmentViewModels() delegate
- Use SavedStateHandle for process‑death restoration.
    - process‑death restoration: when the system kills your app to reclaim resources, it can later restore the app to its previous state.
    - SavedStateHandle allows you to save and restore small amounts of UI state data.
- Avoid holding references to Views, Contexts, or Activities in the ViewModel
    - if your ViewModel holds a reference to an Activity or View, it can prevent that Activity or View from being garbage collected when it's no longer needed.
    - this can lead to memory leaks and increased memory usage.
    - instead, use application context if needed (AndroidViewModel) or pass data through LiveData/StateFlow.
- Avoid complex logic in the ViewModel; keep it focused on UI-related data and state.
- Prefer StateFlow/SharedFlow for new code; LiveData is fine if you already use it.
    - StateFlow and SharedFlow are part of Kotlin's coroutines library and provide a more modern and flexible way to handle state and events.
    - StateFlow is a state holder that emits the current and new state updates to its collectors.
    - SharedFlow is a hot stream that can emit values to multiple collectors.
- Use sealed classes or enums to represent UI states and events.
    - this helps make your code more readable and maintainable by clearly defining the different states and events your UI can be in.
- Use data classes to represent complex data structures.
    - data classes automatically provide useful methods like equals(), hashCode(), and toString(), making them ideal for representing data.
- Use coroutines for asynchronous operations.
    - coroutines provide a simple and efficient way to handle background tasks without blocking the main thread.
    - use viewModelScope to launch coroutines that are tied to the ViewModel's lifecycle
- Model UI state and one‑off events (e.g., toasts/navigation) separately.
    - model UI state with StateFlow or LiveData.
    - model one‑off events with SharedFlow or an Event wrapper around LiveData.
- Fragment specifics:
    - observe with viewLifecycleOwner : ensures observers are tied to the Fragment's view lifecycle, preventing memory leaks.
    - use fragmentViewModels() for Fragment-scoped VMs.
    - use activityViewModels() for shared VMs across multiple fragments in the same activity.
        - "global" ViewModel for the activity.
- Use dependency injection (e.g., Hilt) to provide dependencies to your ViewModel.
    - you can annotate your ViewModel with @HiltViewModel and use @Inject constructor to get dependencies.
    - this helps manage the lifecycle of dependencies and makes testing easier.
- When VM needs constructor args, provide a ViewModelProvider
    - use a ViewModelFactory to create ViewModels with parameters.
    - with Hilt, you can use @AssistedInject to handle constructor parameters.

## Lifecycle methods:
- override lifecycle methods as needed:
    - onCreate → initialize the Activity, inflate UI (setContentView), restore state (ViewModel, Navigation setup).
    - onStart →
        - start observing ViewModel data (function TODO)
        - register listeners (setOnClickListeners).
    - onResume →
        - resume any paused UI updates, animations, or listeners (function TODO).
    - onPause →
        - save lightweight state (function TODO).
    - onStop →
        - stop observing ViewModel data (function TODO).
        - free heavy resources (function TODO).
    - onRestart →
        - re-initialize resources released in onStop (function TODO).
    - onDestroy →
        - clean up any resources that won't be needed anymore (function TODO).
            - like closing database connections or stopping background threads.

