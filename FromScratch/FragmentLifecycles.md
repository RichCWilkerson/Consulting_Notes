# Fragment lifecycles (Android)

This guide summarizes the Fragment lifecycle, the separate View lifecycle, and where to put work safely.

Key idea: a Fragment has its own lifecycle, and its View hierarchy (created in onCreateView) has a shorter, nested lifecycle. Always clean up anything tied to the View in onDestroyView.

- Fragment lifecycle (object): onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume → onPause → onStop → onDestroyView → onDestroy → onDetach
- View lifecycle (viewLifecycleOwner): starts at onCreateView/onViewCreated and ends at onDestroyView

Recommended responsibilities by callback

- onAttach(context)
  - Fragment attached to host Activity / parent Fragment; DI entry point
  - Do: acquire context-only dependencies that don’t require a View (e.g., Navigator, Analytics)
  - Don’t: touch views or require activity fully created UI

- onCreate(savedInstanceState)
  - One-time Fragment setup. Good for reading arguments, creating non-view state, getting ViewModels
  - Use activityViewModels() for shared state with Activity; viewModels() for per-fragment
  - Don’t: inflate or reference views

- onCreateView(inflater, container, savedInstanceState)
  - Inflate and return the Fragment’s root view
  - If using ViewBinding: binding = FragmentFooBinding.inflate(inflater, container, false)

- onViewCreated(view, savedInstanceState)
  - Views exist; initialize UI, adapters, listeners, and start observing LiveData/Flow with viewLifecycleOwner
  - Good place to restore ephemeral UI (scroll position) and set up transitions

- onStart()
  - UI becomes visible

- onResume()
  - Fragment in foreground and interactive. Start animations, camera, sensors that require focus.

- onPause()
  - Pause transient work that shouldn’t continue while partially obscured

- onStop()
  - UI no longer visible. Release resources that are visible-only.

- onRestart()
  - Fragment coming back to foreground after being stopped

- onDestroyView()
  - View hierarchy is being destroyed; clean up anything tied to the View
  - Set binding = null, remove callbacks, clear adapter references if they hold the view/context

- onDestroy()
  - Fragment is finishing; release non-view resources owned by the fragment object

- onDetach()
  - Fragment no longer associated with host

View vs Fragment lifecycle: where to observe and launch

- Observing LiveData/Flow: always use viewLifecycleOwner (not this) so observers are removed when the view is destroyed
  - LiveData: liveData.observe(viewLifecycleOwner) { … }
  - Kotlin Flow: viewLifecycleOwner.lifecycleScope.launch { viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { flow.collect { … } } }
- Coroutines
  - viewLifecycleOwner.lifecycleScope for work tied to the view
  - lifecycleScope (fragment’s own) for work that can outlive the view but not the fragment

State retention best practices

- ViewModel holds state across configuration change and process recreation
- SavedStateHandle (in a SavedStateViewModel) persists small, serializable values
- savedInstanceState for quick UI restore. Avoid storing large objects or references.
- setRetainInstance(true) is deprecated; prefer ViewModel

Common sequences

- Add fragment (not on back stack): onAttach → onCreate → onCreateView → onViewCreated → onStart → onResume
- Navigate to another fragment and previous goes to back stack (hidden): previous onPause → onStop; new fragment runs attach/create/…/resume
- Pop back stack to previous: previous onStart → onResume; current onPause → onStop → onDestroyView → onDestroy → onDetach (if removed)
- Configuration change (e.g., rotation): onPause → onStop → onDestroyView → onCreateView → onViewCreated → onStart → onResume (Fragment object usually survives when using FragmentManager, but its view is recreated)

Menu and toolbar

- Use MenuHost + addMenuProvider in onViewCreated with viewLifecycleOwner
- Avoid setHasOptionsMenu; it’s deprecated in modern APIs

Child fragments

- childFragmentManager manages nested fragments inside this fragment’s view
- Ensure child fragments are added after onViewCreated and cleaned up by onDestroyView

DialogFragment notes

- Lifecycle is similar but view may be hosted in a dialog window; manage dialog-specific resources accordingly

Typical skeleton (ViewBinding + Flows)

```
class FooFragment : Fragment(R.layout.fragment_foo) {
  private var _binding: FragmentFooBinding? = null
  private val binding get() = _binding!!
  private val viewModel: FooViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    _binding = FragmentFooBinding.bind(view)

    // LiveData
    viewModel.items.observe(viewLifecycleOwner) { items ->
      binding.recycler.adapter.submitList(items)
    }

    // Flow
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.events.collect { event -> /* handle */ }
      }
    }

    binding.button.setOnClickListener { /* … */ }
  }

  override fun onDestroyView() {
    binding.recycler.adapter = null // avoid leaking old view
    _binding = null
    super.onDestroyView()
  }
}
```

Pitfalls to avoid

- Holding a reference to a view/binding past onDestroyView → memory leaks
- Observing with fragment lifecycle (this) instead of viewLifecycleOwner → observers survive view recreation and touch null views
- Launching long coroutines in view scope without cancellation checks; prefer repeatOnLifecycle
- Emitting navigation events after onDestroyView; guard with isAdded/isResumed or use a single-shot Event consumed in onViewCreated
- Keeping adapters that hold the old context/view; null them in onDestroyView if necessary

Quick checklist

- Get ViewModels in onCreate
- Inflate in onCreateView or use constructor layout id + bind in onViewCreated
- Observe with viewLifecycleOwner
- Use viewLifecycleOwner.lifecycleScope + repeatOnLifecycle
- Null binding and clear adapters in onDestroyView
- Persist small UI state in onSaveInstanceState; keep domain/UI state in ViewModel


# Activity Lifecycle
onCreate — one-time setup: inflate UI, init ViewModels, register observers.
onStart — UI becoming visible; start UI-visible resources.
onResume — interactive; start animations, foreground sensors.
onPause — commit transient changes, stop expensive updates.
onStop — release UI resources that can be recreated later.
onRestart — coming back to foreground after being stopped.
onDestroy — final cleanup (only for non-config change destruction).