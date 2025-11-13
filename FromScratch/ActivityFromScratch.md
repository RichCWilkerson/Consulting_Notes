# When creating a new screen:

## AndroidManifest.xml:
- only if a new Activity is created. Fragments do not need to be declared here.
  - must add activities to the manifest file.

---

## res/layout:
- create a new XML layout file for the screen.

--- 

## Activity.kt file:
- create a new Activity class (Kotlin) per screen
- Extend AppCompatActivity 
  - to access lifecycle methods and support library features.

### Activity Lifecycle (onCreate, onStart, onResume, onPause, onStop, onDestroy)
- link to lifecycle notes to decide best practices for your use case.
- [Activity Lifecycle Notes](./activityLifecycle.md)

### Setup viewWiring (recommend viewBinding)
- link to viewWiring notes to decide best practices for your use case.
- [ViewWiring Setup Notes](viewWiringOptions.md)

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

--- 

## ViewModel.kt Setup (persisting UI data and state)
- link to VM notes to decide best practices for your use case.
- [ViewModel Setup Notes](viewModelOptions.md)

--- 

## Repository.kt Setup (data handling, API calls, database interactions)
- link to repository notes to decide best practices for your use case.