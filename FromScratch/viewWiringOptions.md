# View Wiring Options Guide

<!-- Table of contents for quick jumps within this file -->
- [Architectural pattern fit \(MVC, MVP, MVVM, MVI, Clean\)](#architectural-pattern-fit-mvc-mvp-mvvm-mvi-clean)
- [Scope \(Activity vs Fragment vs RecyclerView\)](#scope-activity-vs-fragment-vs-recyclerview)
- [Use Cases \(Choose Approach Based on Needs\)](#use-cases-choose-approach-based-on-needs)
- [View wiring options \(findViewById, ViewBinding, DataBinding, Property Delegates, Compose\)](#view-wiring-options-findviewbyid-viewbinding-databinding-property-delegates-compose)
- [Common listener setup](#common-listener-setup)
- [Include and Merge layouts](#include-and-merge-layouts)
- [Additional Notes](#additional-notes)

---

TODO:
- when to use different design patters (Creational, Structural, Behavioral)
  - how to implement them within the context of android view wiring
- after covering RecyclerView learning, come back and see if any additional notes are needed for ViewHolder wiring.

---

## Architectural pattern fit (MVC, MVP, MVVM, MVI, Clean)
- MVC (classic Android)
  - Controller (Activity/Fragment) wires views and handles events. Any of the options work; historically findViewById, now prefer ViewBinding for type safety.
  - Keep business logic out of XML and view classes.

- MVP
  - View (Activity/Fragment) implements a View interface. Prefer ViewBinding or findViewById. If using DataBinding, keep expressions simple and avoid pushing logic into XML; Presenter should remain Android-free.

- MVVM
  - View observes ViewModel state/events and renders UI. ViewBinding is a great default; DataBinding can directly bind LiveData and supports two-way binding (e.g., text fields) with lifecycleOwner set.
  - Avoid business logic in XML; use BindingAdapters only for view-mapping concerns.

- MVI
  - View renders immutable State and emits Intents. ViewBinding keeps rendering explicit in render(state). DataBinding can be used but can blur reducer boundaries if heavy expressions are used.
  - Prefer explicit render() with ViewBinding for readability and unidirectional flow; use BindingAdapters sparingly.

- Clean Architecture
  - Presentation layer chooses the wiring. Prefer ViewBinding for clear boundaries. DataBinding is fine but keep domain/data layers Android-free and avoid leaking XML concerns outside presentation.

---

## Scope (Activity vs Fragment vs RecyclerView)
- Activity
  - Binding lives for the Activity lifetime. Hold a non-null binding property after setContentView(binding.root). Clear in onDestroy if you attach long-lived listeners to binding views.

- Fragment (critical)
  - View lifecycle differs from Fragment lifecycle. Create binding in onCreateView and set to null in onDestroyView to avoid leaks.
  - Always access binding only between onCreateView and onDestroyView. Use a backing nullable _binding and a non-null getter binding.

- RecyclerView ViewHolder
  - No lifecycleOwner. Create binding in onCreateViewHolder and use inside ViewHolder. Clean up in onViewRecycled if you attach listeners that reference external objects.

- Lifecycle owners in DataBinding
  - Set binding.lifecycleOwner = viewLifecycleOwner (Fragment) or this (Activity) to let LiveData updates auto-observe. For Flow/StateFlow, convert to LiveData or use BindingAdapters to collect; otherwise, collect in code.

---

## Use Cases (Choose Approach Based on Needs)
TODO: when would you use each option? why? this section is to help decide which to use based on architecture and needs.
- **ViewBinding** (recommended default)
    - Architectures: MVC, MVP, MVVM, MVI, Clean
    - Use when: most screens; explicit render code; RecyclerView/ViewHolder; want type‑safety without XML logic.
    - Why: simple, fast, clear ownership of UI updates; great Fragment ergonomics with proper cleanup.
    - Avoid when: you want XML‑driven two‑way forms or heavy declarative binds; consider DataBinding/Compose.
- DataBinding
    - Architectures: MVC, MVP (simple binds), MVVM, MVI (cautious), Clean
    - Use when: simple declarative binds; two‑way form fields; binding LiveData in XML with lifecycleOwner.
    - Why: reduces boilerplate for text/visibility/enabled, two‑way @={} for forms, BindingAdapters for view‑only glue.
    - Avoid when: complex expressions, business logic, or navigation in XML; prefer keeping logic in ViewModel/renderer.
- findViewById (legacy)
    - Architectures: MVC (legacy), MVP (simple), others (not recommended)

- Property Delegates
    - Architectures: any using Fragments + ViewBinding

- Compose (modern)
    - Architectures: MVVM, MVI, Clean (modern)

---

## View wiring options (findViewById, ViewBinding, DataBinding, Property Delegates, Compose)
### findViewById (baseline)
- Lifecycle-awareness
  - None. You manually find and hold view references; ensure you drop them when views are destroyed (Fragments).
- API surface
  - activity.findViewById<View>(R.id.foo) or view.findViewById(...) in Fragments.
- Pros
  - Zero setup, no generated code, clear and explicit.
- Cons
  - Verbose, unsafe casts (pre-Kotlin), nullability pitfalls, brittle with refactors, easy to leak in Fragments.
  - error prone: typos in R.id.foo compile but crash at runtime.
- Syntax
  - Activity: setContentView(R.layout.activity_main); val btn: Button = findViewById(R.id.btn)
  - Fragment: val tv: TextView = view.findViewById(R.id.title)
- Setup
```kotlin
// Activity
class MainActivity : AppCompatActivity(R.layout.activity_main) {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val btn: Button = findViewById(R.id.btn)
    btn.setOnClickListener { /* handle */ }
  }
}

// Fragment
class ExampleFragment : Fragment(R.layout.fragment_example) {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val title: TextView = view.findViewById(R.id.title)
    title.text = "Hello"
  }
}
```

---

### ViewBinding (recommended default)
- Lifecycle-awareness
  - Not automatic. In Fragments you must null binding in onDestroyView. In Activities binding lives to onDestroy.
- Delivery semantics / Threading / Operators
  - Not applicable.
- API surface
  - Generated binding classes for each layout; type-safe access to views. Inflate via XxxBinding.inflate and use binding.root.
- Pros
  - Type-safe, null-safe, minimal overhead, no XML logic, works with all layouts including <merge> and <include>.
- Cons
  - No expression binding; you still write render code; must manage Fragment view lifecycle to avoid leaks.
- Setup
  - Gradle: android { buildFeatures { viewBinding = true } }
    - allows Android to generate binding classes for each XML layout file.
    - Naming: activity_main.xml -> ActivityMainBinding; fragment_example.xml -> FragmentExampleBinding.
- Syntax
```kotlin
// Activity
class MainActivity : AppCompatActivity() {
  // lateinit allows you to initialize the binding variable later in onCreate.
  // This is necessary because the binding depends on the layout inflater which is only available in onCreate.
  private lateinit var binding: ActivityMainBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    // attach binding as our layout
    setContentView(binding.root)
    // set listeners on element ids
      // setOnClickListener for button clicks
      // setOnCheckedChangeListener for check changes (CheckBox, Switch, RadioButton)
      // addTextChangedListener for text changes (EditText, TextView)
    binding.cta.setOnClickListener { /* ... */ }

    // get/set values
    val currentTitle = binding.title.text.toString()
    binding.title.text = "Hi"

    // optional apply scope
    binding.apply {
      cta.setOnClickListener { /* ... */ }
      title.text = "Hello"
    }
  }
}

// Fragment
class ExampleFragment : Fragment(R.layout.fragment_example) {
  // Backing nullable property - critical for Fragment safety

  // backing nullable property - critical for Fragment safety
    // prevents using binding outside view lifecycle
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    // What are these parameters?
    // - inflater: creates View instances from XML.
    // - container: the parent ViewGroup that the fragment's view will eventually be attached to.
    // - savedInstanceState: prior state if the fragment is re-created.
    // Inflate with attachToParent=false: FragmentManager attaches it for you later.
    _binding = FragmentExampleBinding.inflate(inflater, container, false)
    return binding.root
  }

  // onViewCreated is where we bind to any view logic (listeners, rendering)
    // view is the root view returned from onCreateView (binding.root)
    // savedInstanceState is the Fragment state bundle (if any)
    // Wire listeners and render 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.cta.setOnClickListener { /* ... */ }
  }

  // critical: clear binding on onDestroyView to avoid leaking the view hierarchy
  // Fragment instance may outlive its view; clear binding to avoid leaks
    // View might be destroyed but Fragment still in memory (stack)
  override fun onDestroyView() {
    // Clear to avoid leaking the view hierarchy
    _binding = null
    super.onDestroyView()
  }
}

// RecyclerView ViewHolder
// Yes: RecyclerView uses binding at the ViewHolder level (not Activity/Fragment lifecycle)
class ItemVH(val binding: RowItemBinding) : RecyclerView.ViewHolder(binding.root)
class ItemsAdapter : ListAdapter<Item, ItemVH>(DIFF) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
    val binding = RowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ItemVH(binding)
  }
  override fun onBindViewHolder(holder: ItemVH, position: Int) {
    val item = getItem(position)
    holder.binding.title.text = item.name
    holder.binding.root.setOnClickListener { /* ... */ }
  }
}
```

### DataBinding (XML expressions and two-way binding)
- Lifecycle-awareness
  - Set binding.lifecycleOwner to a LifecycleOwner to observe LiveData automatically. For Flows, convert to LiveData or write BindingAdapters to collect.
- Delivery semantics
  - Expressions re-evaluate when observed data changes. Two-way binding via @={} syncs View and source property; beware feedback loops.
- Threading
  - UI updates must run on main; DataBinding evaluates expressions on the UI thread.
- API surface and operators
  - <layout><data> with variables/imports; binding expressions @{}, BindingAdapter/@InverseBindingAdapter for custom attributes.
- Pros
  - Reduces boilerplate for simple binds; integrates with LiveData; two-way binding for forms; good for declarative UI updates.
- Cons
  - Compile-time overhead; harder to debug; can hide logic in XML; team needs discipline; Flow support requires extra code.
- Setup
  - Gradle: android { buildFeatures { dataBinding = true } }
- Syntax
```xml
<!-- res/layout/fragment_example.xml -->
<layout xmlns:android="http://schemas.android.com/apk/res/android">
  <data>
    <variable name="vm" type="com.example.ExampleViewModel"/>
  </data>
  <LinearLayout>
    <TextView android:text='@{vm.title}'/>
    <EditText android:text='@={vm.query}'/>
    <Button android:onClick='@{() -> vm.onCtaClicked()}'/>
  </LinearLayout>
</layout>
```
```kotlin
class ExampleFragment : Fragment(R.layout.fragment_example) {
  private var _binding: FragmentExampleBinding? = null
  private val binding get() = _binding!!
  private val vm: ExampleViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentExampleBinding.inflate(inflater, container, false)
    return binding.root
  }
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.lifecycleOwner = viewLifecycleOwner
    binding.vm = vm
  }
  override fun onDestroyView() { _binding = null; super.onDestroyView() }
}

// BindingAdapter example (view-only mapping)
@BindingAdapter("visibleIf")
fun View.visibleIf(show: Boolean) { visibility = if (show) View.VISIBLE else View.GONE }
```

- Notes
  - Prefer simple expressions; move complex logic into the ViewModel or a BindingAdapter.
  - For StateFlow, expose LiveData via asLiveData() or use a BindingAdapter to collect.
  - Avoid referencing Fragment/Activity directly in XML.

---

### Property delegates (for safer Fragment binding)
- What are property delegates?
  - Kotlin allows properties to delegate their getter logic to another object (by someDelegate). Here, a ReadOnlyProperty returns the binding and auto-clears it on onDestroyView.
  - Where to put it: a small Kotlin file in a ui/binding or util package (same module). Reuse across Fragments.
- Idea
  - Use a small delegate to automatically clear binding at onDestroyView.
- Example (inline, no external libs)
```kotlin
class FragmentViewBindingDelegate<T : ViewBinding>(
  val fragment: Fragment,
  val bind: (View) -> T
) : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {
  private var binding: T? = null
  override fun onDestroy(owner: LifecycleOwner) { binding = null }
  override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
    val viewLifecycle = fragment.viewLifecycleOwner.lifecycle
    if (binding == null) {
      binding = bind(thisRef.requireView())
      viewLifecycle.addObserver(this)
    }
    return binding!!
  }
}
fun <T : ViewBinding> Fragment.viewBinding(bind: (View) -> T) =
  FragmentViewBindingDelegate(this, bind)

class ExampleFragment : Fragment(R.layout.fragment_example) {
  private val binding by viewBinding(FragmentExampleBinding::bind)
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.cta.setOnClickListener { /* ... */ }
  }
}
```

---

### Alternative: Jetpack Compose (brief note)
- Compose replaces XML + binding entirely with a declarative UI. 
- It’s outside the scope of this doc, but consider it for new screens. 
- Interop with Views via ComposeView or ViewBinding+Compose hybrid is possible.

---

## Common listener setup
- Clicks
  - Debounce prevents accidental double-taps by ignoring clicks that occur within a short interval (e.g., 500ms) since the last accepted click.
```kotlin
fun View.setSafeOnClickListener(intervalMs: Long = 500, block: (View) -> Unit) {
  var last = 0L
  setOnClickListener {
    val now = SystemClock.elapsedRealtime()
    if (now - last >= intervalMs) { last = now; block(it) }
  }
}
```

- Text changes
  - editText.addTextChangedListener { vm.onQueryChanged(it.toString()) }
  - Note: remove/add listeners only while the view is alive; in Fragments, set them in onViewCreated.

- Editor actions
  - editText.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) { vm.search(); true } else false
    }

- Back press
  - requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { vm.onBack() }

- RecyclerView item clicks
  - Prefer passing a click lambda from Fragment to Adapter: ItemsAdapter(onClick = { item -> vm.onItemClick(item) })

- DataBinding XML listeners
  - android:onClick='@{() -> vm.onCtaClicked()}' or custom attributes via @BindingAdapter("onDebouncedClick")

---

## Include and Merge layouts
### Include:
  - Reuse a child layout inside a parent. ViewBinding exposes the included binding as a field on the parent binding.
  - Access included views via the generated field name (derived from the include tag’s id or layout name).
```xml
<!-- parent.xml -->
<LinearLayout>
  <include
    android:id="@+id/header"
    layout="@layout/layout_header"/>
</LinearLayout>
<!-- child.xml -->
<!-- layout_header.xml -->
<LinearLayout>
  <TextView android:id="@+id/title" />
</LinearLayout>
```
```kotlin
val parent = ParentBinding.inflate(inflater)
parent.header.title.text = "Hello" // <binding>.<include_id>.<view_id>.<property>
```

### Merge:
  - `<merge></merge>` is an XML layout tag you put as the root of a child layout file in res/layout
  - With a <merge> root, either include it statically in the parent XML or inflate it dynamically into a container. Don’t do both. 
    - When inflating dynamically, pass the container and set attachToParent = true.
  - Using <merge> flattens the child views into the parent’s container, avoiding an extra wrapper.
```xml
<!-- parent.xml -->
<include layout="@layout/row_item"/>

<!-- child.xml -->
<!-- row_item.xml -->
<merge>
  <TextView android:id="@+id/title" />
</merge>
```
```kotlin
// Given `row_item.xml` has <merge> as its root and parent has a container view (e.g., binding.content)
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val parentBinding = ParentBinding.bind(view)
    // Attach merged children directly into this container
    RowItemBinding.inflate(layoutInflater, parentBinding.content, /* attachToParent = */ true)

    // Access merged views directly on the parent binding (flattened by <merge>)
    parentBinding.title.text = "Hello"
}
```

---

## Additional Notes
- Fragment safety
  - Never use binding after onDestroyView; prefer viewLifecycleOwner for collectors.
- BindingAdapters
  - BindingAdapters map custom XML attributes to Kotlin functions so you can write view-specific glue (e.g., app:visibleIf="@{vm.show}"). Place them in a Kotlin file in ui.binding (or similar).
  - Keep adapters UI-focused (formatting, visibility, loading images). Don’t call navigation or heavy logic from XML.
- Accessibility
  - contentDescription: describe non-text UI for screen readers; set in XML when static, or in code when dynamic.
  - Touch target size: ensure ≥48dp min height/width (use padding or minHeight); can be enforced in XML.
  - Text scaling: use sp for text; verify with large font settings. Support dynamic type.
  - Contrast and focus order: ensure sufficient contrast and logical focus traversal; set importantForAccessibility where needed.
- Testing
  - With ViewBinding, unit test presenters/ViewModels separately; use Robolectric/Instrumented tests for wiring if needed.
