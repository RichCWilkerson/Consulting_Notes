# Task
- Create 3 Activities
  - experiment with LinearLayout design

# Android Concepts Overview

## 1) Main Components
- Activity — Entry point that hosts UI and interacts with the system.
    - Key responsibilities: set content view, manage fragments, handle permissions/intents, coordinate navigation.
    - Lifecycle (primary callbacks):
        - You can follow the lifecycle by using `log.d` in each method below to see how it works.
        - onCreate → initialize the Activity, inflate UI, restore state.
            - We put our views, ViewModel, and Navigation setup here. This is initialization code that runs once.
        - onStart → Activity is becoming visible.
            - Typically, we start observing ViewModel data here to update the UI. With listeners, we might register them here.
        - onResume → Activity is in the foreground and interactive.
            - Here, we resume any paused UI updates, animations, or listeners.
        - onPause → Another Activity is taking focus; save lightweight state.
        - onStop → Activity no longer visible; free heavy resources.
            - Here, we stop observing ViewModel data to avoid unnecessary updates when not visible.
        - onRestart → Coming back to foreground after stop.
            - Here, we can re-initialize resources released in onStop.
        - onDestroy → Activity is about to be destroyed (may be due to config change or finish()).
            - Here, we clean up any resources that won't be needed anymore. like closing database connections or stopping background threads.
        - Tip: For config changes (rotation), prefer ViewModel to hold UI data; use onSaveInstanceState for small UI state.

- Fragment — Reusable UI/controller that lives within an Activity.
    - Lifecycle (UI-related):
        - onCreate (non-UI init) → onCreateView (inflate layout) → onViewCreated (bind views/observers) → onStart → onResume → onPause → onStop → onDestroyView (cleanup view refs) → onDestroy → onDetach.
    - Always clear view references in onDestroyView to avoid leaks in Fragments.
    - Works great with: ViewModel, LiveData/Flow, Navigation, ViewBinding.

- Service — Perform work without a UI.
    - Types: Foreground (ongoing notification), Background (limited on modern Android), Bound (client-server in-process).
    - Key callbacks: onCreate, onStartCommand, onBind/onUnbind, onDestroy.
    - Use cases: playback, location tracking, long-running tasks (prefer WorkManager for deferrable/background work).

- BroadcastReceiver — Respond to system/app-wide broadcasts.
    - Static vs dynamic registration: Manifest (app not running) vs registerReceiver/unregisterReceiver in code.
    - Examples: connectivity changes, boot completed, custom app events.

- ContentProvider — Share app data via a well-defined URI API (CRUD via query/insert/update/delete).
    - Commonly used by system (e.g., Contacts) and for inter-app data access.



## 2) Project Files and Structure

Matches the structure of this repo (Kotlin + XML with Navigation and ViewModels).

- AndroidManifest.xml
    - Declares: permissions, activities (with intent-filters), services, receivers, providers, application-level meta-data (e.g., default Nav graph), and features/hardware.
    - Runtime permissions: declare in manifest + request at runtime for dangerous permissions (camera, location, etc.).

- src/
    - main/java/... → App code (Activities, Fragments, ViewModels, adapters, etc.).
    - androidTest/java/... → Instrumented tests (run on device/emulator).
    - test/java/... → JVM unit tests (no Android framework).

- res/ (resources)
    - layout/ (XML layouts) - one per screen/component(fragment).
        - describes the layout structure and view hierarchy of each screen (login, signup, etc.).
    - values/ (XML for strings, colors, dimensions, styles)
        - strings.xml — all user-facing text (support localization).
        - colors.xml — color definitions.
        - dimens.xml — standard spacing/font sizes.
        - styles.xml/themes.xml — app-wide visual styles and themes.
    - drawable/ (bitmaps/vector drawables)
        - this is where we put images and vector assets used in the app.
    - mipmap/ (app icons)
        - this is specifically for app launcher icons in various resolutions.
    - navigation/ (Navigation graphs)
        - like an api gateway for your app's UI flow.
        - which paths are allowed between screens (fragments/activities).
        - use the ids and actions defined here to navigate in code.
    - menu/ (menu XMLs for toolbars, navigation drawers)
        - defines menu items for toolbars and navigation drawers.
    - xml/ (misc XML configs).
    - Qualifiers (e.g., values-night/, layout-sw600dp/) load best-match resources for device configs.

- Gradle (Kotlin DSL)
    - settings.gradle.kts — project settings and module includes.
    - build.gradle.kts (project) — repositories, plugins management.
    - app/build.gradle.kts (module) — android block (compileSdk, minSdk, targetSdk), dependencies, buildTypes, viewBinding/dataBinding flags, etc.
    - gradle/libs.versions.toml — centralized version catalog of dependencies and plugins.


## 3) UI Basics

- Layouts
    - LinearLayout — simple stacking (horizontal/vertical). Easy, but can nest deeply if overused.
    - RelativeLayout — position views relative to each other/parent (older; mostly superseded by ConstraintLayout).
    - ConstraintLayout — flexible and flat hierarchies; use constraints, chains, bias; prefer for complex screens.
    - Note: Jetpack Compose is the modern UI toolkit (declarative); this project uses XML views, which is still common.

- Views (common)
    - TextView, EditText, Button, ImageView, CheckBox, RadioButton, RecyclerView (preferred vs ListView), ProgressBar, etc.

- Dimensions and positioning
    - dp (density-independent pixels) — use for layout sizes/margins/padding.
    - sp (scale-independent pixels) — use for text sizes (respects user font scaling).
    - px (pixels) — avoid hardcoding; convert when absolutely necessary.
    - Start/End vs Left/Right — prefer Start/End for RTL support.
    - Accessibility: touch targets ≥ 48dp; ensure color contrast; support font scaling (sp).