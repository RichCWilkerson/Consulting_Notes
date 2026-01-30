# Resources:


# Screen Scaffolds & Structure in Jetpack Compose


High-level containers that define the structure of a screen: `Scaffold`, app bars, navigation components, drawers, and sheets.

Includes (covering ~95% of common structures):
- Scaffolds: `Scaffold`, `BottomSheetScaffold`
- App bars: `TopAppBar` variants (Small/Medium/Large/CenterAligned), search bars
- Navigation surfaces: `NavigationBar`, `NavigationRail` (Material 3), legacy `BottomNavigation`
- Drawers: `ModalNavigationDrawer`, `DismissibleNavigationDrawer`
- Sheets: `ModalBottomSheet`, `BottomSheetScaffold` (partially overlaps Scaffolds category)
- FAB & Snackbars: treated as **slot content** in scaffolds rather than separate layout structures.

### Category overview
**Scaffolds**
- Purpose: Provide the **overall screen skeleton** and common slots (top bar, bottom bar, FAB, snackbar host, content).
- Typical choice: one scaffold pattern per screen hierarchy. You generally **don’t nest multiple scaffolds deeply**; instead you pick the one that matches your screen’s needs (standard vs bottom-sheet-centric).
  - Each Feature/Screen composable often owns its own `Scaffold`.
  - Alternatively, you can have a single app-level scaffold around your `NavHost` if your app has truly global chrome.
**App bars**
- Purpose: Provide a consistent area at the top of the screen for **title, navigation, and actions**.
- Typical choice: you pick **one app bar variant per screen** (e.g. `CenterAlignedTopAppBar` or `SmallTopAppBar`), possibly with scroll behavior; you don’t stack multiple top app bars.
- App bars vs toolbars vs action bars:
  - In traditional View-based Android:
    - *ActionBar* and *Toolbar* were classes used with activities/fragments.
  - In Compose with Material 3:
    - You typically use `TopAppBar` composables instead of ActionBar/Toolbar widgets.
    - `NavigationBar` is a **bottom navigation component**, not a top app bar.
**Navigation surfaces**
- Purpose: Let the user **switch between top-level destinations** (e.g., tabs, bottom nav, rail).
- Typical choice: For a given screen, you usually pick **one primary navigation surface**: bottom bar (`NavigationBar`), rail (`NavigationRail`), or drawer as your main nav surface. Using all three together on a single phone screen is unusual and often confusing.
**Drawers**
- Purpose: Provide **off-screen navigational or contextual content** that slides in from the side.
- Typical choice: One drawer per major navigation hierarchy. Drawers are often used instead of, not in addition to, a bottom navigation bar.
**Sheets**
- Purpose: Provide **temporary surfaces** anchored to the bottom of the screen, usually for supplementary actions or content.
- Typical choice: You decide between sheet patterns (`ModalBottomSheet`, `BottomSheetScaffold`) depending on whether the sheet is **ephemeral/modal** or **structural** to the screen.

> NOTE: on pitfalls and performance: some concerns (like over-recomposition or insets) recur across a category, but important components still get their **own** pitfalls and performance notes below, especially where behavior differs.

---

## Scaffolds
### Scaffold
- **What it is**: A high-level layout component that provides **slots** (named content areas) for common screen elements: top bar, bottom bar, floating action button, snackbar host, and main content.
  - A “slot” in this context is just a parameter that accepts composable content, e.g. `topBar = { TopAppBar(...) }`. `Scaffold` arranges these slots in a standard Material layout.
- **When to use**:
  - Most standard application screens that follow Material patterns.
    - "Material patterns" here means the layouts and component combinations recommended by the Material Design guidelines (e.g., top app bar + FAB + bottom navigation) and implemented by the Material 3 Compose library.
  - When you need consistent placement of app bars, bottom navigation, and FABs across screens.
    - `Scaffold` helps by giving you the same slots on every screen; if each screen uses `Scaffold(topBar = ..., bottomBar = ..., floatingActionButton = ...)`, those elements will align and behave consistently across the app.
- **Common pitfalls**:
  - Mismanaging padding and insets: forgetting to use the `paddingValues` from the `Scaffold` content lambda, causing content to render under app bars or navigation bars.
    - `paddingValues` is automatically provided by `Scaffold` to its `content` lambda. It encodes the space taken by the top bar, bottom bar, and any insets that `Scaffold` is managing. Applying `Modifier.padding(paddingValues)` to your root content ensures that what you render won’t be hidden underneath these surfaces.
  - Nesting `Scaffold` inside another `Scaffold`, leading to confusing behavior and duplicated surfaces; prefer a single scaffold per screen.
    - Common patterns:
      - **One scaffold per top-level destination**: Each screen composable owns its own `Scaffold` (most common for simple apps).
      - **Single app-level scaffold**: A root `Scaffold` in your main host (e.g., around `NavHost`) with screen content inside. This is useful when app bars and bottom navigation are truly global.
      - Multiple scaffolds are usually only warranted when you have distinct sub-flows that need different chrome; even then, keep nesting shallow and avoid overlapping areas.
  - Confusing snackbar host state and snackbar placement; centralize snackbar handling at an appropriate level.
    - `Scaffold` exposes `snackbarHost` and expects a `SnackbarHostState` (often remembered via `remember { SnackbarHostState() }` or `rememberScaffoldState().snackbarHostState`). If each screen creates its own host state independently, you can end up with snackbars that are hard to coordinate or appear in unexpected places. Prefer defining snackbar host state at a level that matches the scope of your messages (per app, per nav graph, or per major flow), and pass it down.
- **Performance notes**:
  - `Scaffold` itself is not heavy; the main concern is how often **its children** (top bar, bottom bar, content) recompose.
  - Compose does skip unchanged subtrees, but you still want to:
    - Keep fast-changing state (scroll positions, transient UI state) as close as possible to the composables that use it.
    - Avoid putting rapidly changing state in a place that forces re-running expensive slots (e.g., recomputing a complex `TopAppBar` on every character typed in a `TextField` in the content).
  - This is effectively good state hoisting and distribution: global-ish state (like current destination) can live near the scaffold; noisy, local state should live deeper in the tree.
- **Related modifiers**:
  - `fillMaxSize` – have the scaffold occupy the entire screen.
  - `padding` – apply `paddingValues` inside the content, or add extra content padding.
  - `nestedScroll` – connect **scrollable children** (like `LazyColumn`) with scroll-aware parents (like `TopAppBar` scroll behavior or pull-to-refresh containers).
    - In this context it does **not** control keyboard behavior or keep `NavigationBar` visible; it’s about sharing scroll events up the hierarchy so parents can react (e.g., app bar elevation/collapse).
    - You still get nested scrolling between scrollable content (e.g., `LazyColumn` inside `Scaffold`) without manual `nestedScroll`; you add `nestedScroll` when a parent (like an app bar) wants to participate in the scroll.
    - Scroll behavior options depend on the app bar type; Material 3 provides helpers like `pinnedScrollBehavior`, `exitUntilCollapsedScrollBehavior`, which you use with `TopAppBarDefaults`.
  - `windowInsetsPadding` – handle **system bar insets** when you step outside the default behavior.
    - *Window insets* represent parts of the screen occupied by system UI: status bar, navigation bar/gesture area, IME (keyboard), etc.
    - `Scaffold` handles a sensible default for its own bars; you only reach for `windowInsetsPadding` when you:
      - Draw behind system bars (e.g., full-bleed content) and need to manually pad.
      - Opt out of default inset handling and want fine-grained control.

Below is an attempt for "best practices" example of using `Scaffold` in a way that:
- Keeps **scaffold-level state** near the scaffold itself (title, destinations, scroll behavior).
- Delegates **frequently recomposing content** to a separate composable.
- Applies **`paddingValues`** correctly to avoid content underlapping top/bottom bars.
- Centralizes **snackbar handling** via a shared `SnackbarHostState`.

```kotlin
@Composable
fun MyScreen(
    viewModel: MyScreenViewModel = hiltViewModel(),
    // SnackbarHostState is owned at the screen level here;
    // you could also choose to inject or pass this from a higher level
    // (e.g., nav-graph- or app-level) if you want cross-screen snackbars.
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // UI state that affects the scaffold chrome (e.g., title, which destinations
    // are visible, whether to show FAB) lives at this level. This avoids
    // having each sub-composable manage its own duplicated copy.
    val uiState by viewModel.uiState.collectAsState()

    // Scroll behavior for the top app bar. Keeping this here means the
    // behavior is created once per screen composition, not on every recomposition.
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            // Connecting the nested scroll behavior lets the app bar respond
            // (e.g., elevate or collapse) as content below it scrolls.
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
        topBar = {
            // Top-level app bar for this screen. By basing its content on uiState,
            // you control titles and actions from a single source of truth.
            // In practice, you usually have one view model per *screen* (feature),
            // and that screen often owns a Scaffold. The Scaffold isn't a "base screen"
            // by itself, but a layout shell that your screen content lives inside.
            TopAppBar(
                // Visually, a TopAppBar has three main regions:
                // - navigationIcon (start/left)
                // - title (center or start, depending on variant)
                // - actions (end/right)
                // Navigation icon is often a back button or drawer toggle;
                // title is the screen title; actions are secondary actions
                // like settings, search, or overflow menus.
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onBackClicked() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // "Actions" are clickable icons/text on the right side
                    // of the app bar, used for primary secondary actions
                    // (e.g., search, settings, refresh). Keep these fairly
                    // stable so you don't cause unnecessary recompositions.
                    if (uiState.showSettings) {
                        IconButton(onClick = { viewModel.onSettingsClicked() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            // NavigationBar is the main navigation surface on this screen.
            // The items are driven by uiState.destinations, which should be
            // stable or change infrequently.
            // A common pattern is to model destinations as a sealed class or
            // enum-like structure with route, icon, and label; the NavHost
            // then uses the route when you navigate.
            NavigationBar {
                uiState.destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination.route == uiState.currentRoute,
                        onClick = {
                            // In apps using Navigation Compose, this callback
                            // typically delegates to the view model, which
                            // calls navController.navigate(destination.route)
                            // with appropriate navigation options.
                            viewModel.onDestinationSelected(destination)
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            // The FAB is shown conditionally based on screen state.
            // It's part of the Scaffold's slot API, not a separate layout type.
            if (uiState.showFab) {
                FloatingActionButton(onClick = { viewModel.onFabClicked() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        },
        snackbarHost = {
            // Centralized snackbar host. Any part of this screen (or its children)
            // that has access to snackbarHostState can show a snackbar that
            // appears in this same location.
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        // This padding accounts for top/bottom bars and any insets handled by Scaffold.
        // Applying it once at the top-level container keeps content safely within
        // the visible area and avoids manual hard-coded offsets.
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Delegating most of the UI to MyScreenContent keeps Scaffold
            // from being recreated or reconfigured on every small state change.
            MyScreenContent(
                state = uiState,
                onEvent = viewModel::onEvent,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun MyScreenContent(
    state: MyScreenUiState,
    onEvent: (MyScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Content that may recompose frequently (e.g., text fields, lists, buttons)
    // lives here. Scaffold above remains relatively stable, which helps
    // limit the scope of recomposition and keeps your frame times predictable.

    // This function can:
    // - Render primary content (e.g., LazyColumn of items).
    // - Trigger events (onEvent) that update the view model state.
    // - Use snackbarHostState to show feedback based on results.
}
```

---

### BottomSheetScaffold 
- **What it is**: A variant of `Scaffold` that integrates a persistent bottom sheet into the screen layout.
  - Provides slots similar to `Scaffold` (topBar, floatingActionButton, etc.) plus a bottom sheet slot controlled by state.
- **When to use**:
  - When the bottom sheet is a **structural part of the screen**, not just a transient dialog.
  - When you want the sheet to participate in the layout and scrolling (e.g., content above, sheet partially expanded below).
- **Common pitfalls**:
  - Complex state management for sheet expansion/collapse; be careful to centralize sheet state in a view model or parent.
  - Gesture conflicts between the sheet and inner scrollable content.
- **When to use BottomSheetScaffold vs ModalBottomSheet**:
  - Use **BottomSheetScaffold** when the sheet is part of the screen’s continuous layout (e.g., a map with a persistent info panel).
  - Use **ModalBottomSheet** when you want a temporary, modal surface over existing content (e.g., picking an option), more like a dialog.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScaffoldExample(
    viewModel: SheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            // This is the persistent sheet content. It is part of the screen’s
            // layout, not a transient dialog. Keep its state in the view model
            // so it survives configuration changes.
            SheetContent(uiState = uiState, onEvent = viewModel::onSheetEvent)
        },
        sheetPeekHeight = 80.dp,
        topBar = {
            SmallTopAppBar(title = { Text("Bottom sheet screen") })
        }
    ) { paddingValues ->
        // Main content above the sheet. The sheet will slide over/under this
        // depending on the configuration, but it is all one continuous layout.
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ScreenContent(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }
    }
}
```

---

## App Bars
### TopAppBar (CenterAlignedTopAppBar, SmallTopAppBar, etc.)
- **Category**: App bar / Navigation
- **What it is**: A top app bar providing title, navigation, and action slots.
- **When to use**:
  - Primary navigation and actions for a screen.
  - To provide consistent branding, screen titles, and actions across your app.
- **Common pitfalls**:
  - Handling scroll behavior incorrectly (e.g., missing `nestedScroll` connection for scroll-based elevation/appearance changes).
  - Cramming too many actions into the bar, hurting usability and discoverability.
  - Inconsistent back navigation icons/behavior across screens.
- **Performance notes**:
  - Typically lightweight; just ensure actions don’t trigger heavy work on the main thread.
- **Related modifiers**:
  - `windowInsetsPadding`, `background`, `testTag`
- **When to use Small vs CenterAligned vs Large app bars (Material 3)**:
  - **SmallTopAppBar**: Default choice for most screens with limited vertical space.
  - **CenterAlignedTopAppBar**: When you want the title visually centered (e.g., simple apps with few actions).
  - **Medium/Large**: For top-level destinations where you want more prominent titles and/or additional content in the bar area.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarExample(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    SmallTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}
```

---

## Navigation Surfaces
### NavigationBar vs NavigationRail
- **NavigationBar** (bottom bar):
  - Best for phones and narrow devices.
  - 3–5 top-level destinations that users frequently switch between.
- **NavigationRail** (side rail):
  - Best for tablets, foldables, and desktop-sized layouts where you have horizontal space.
  - Can support more destinations without crowding, paired with wider content areas.

### NavigationBar
- **Category**: Navigation / Bottom bar
- **What it is**: A bottom navigation bar for switching between top-level destinations.
- **When to use**:
  - 3–5 top-level destinations that users frequently switch between.
  - When destinations are peers (not hierarchical parent/child relationships).
- **Common pitfalls**:
  - Using bottom navigation for too many destinations, making labels cramped and navigation unclear.
  - Resetting navigation stack incorrectly on tab change; be deliberate about per-tab back stacks when using Navigation Compose.
  - Mixing `NavigationBar` with gestures or other bottom UI elements without considering insets and overlap.
- **Performance notes**:
  - The bar itself is cheap; complexity lies in how you manage content per destination (e.g., separate NavHost per tab vs single NavHost).
- **Related modifiers**:
  - `fillMaxWidth`, `padding`, `windowInsetsPadding`, `testTag`

```kotlin
@Composable
fun NavigationBarExample(
    destinations: List<Destination>,
    currentRoute: String?,
    onDestinationSelected: (Destination) -> Unit
) {
    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = destination.route == currentRoute,
                onClick = { onDestinationSelected(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
```

### NavigationRail
- **Category**: Navigation / Side rail
- **What it is**: A vertical navigation component typically placed at the left edge of the screen on larger devices.
- **When to use**:
  - Tablet, foldable, desktop-sized layouts where horizontal space is available.
  - When you want persistent navigation that does not consume vertical space like a bottom bar.
- **Common pitfalls**:
  - Using a rail on small screens where it crowds content.
  - Combining rail and bottom navigation on the same screen without a clear responsive layout strategy.
- **Performance notes**:
  - Similar considerations as `NavigationBar`; complexity lies in how you manage destinations and back stacks.
- **Related modifiers**:
  - `fillMaxHeight`, `padding`, `windowInsetsPadding`, `testTag`

```kotlin
@Composable
fun NavigationRailExample(
    destinations: List<Destination>,
    currentRoute: String?,
    onDestinationSelected: (Destination) -> Unit
) {
    NavigationRail {
        destinations.forEach { destination ->
            NavigationRailItem(
                selected = destination.route == currentRoute,
                onClick = { onDestinationSelected(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
```

---

## Drawers
### ModalNavigationDrawer vs DismissibleNavigationDrawer
- **ModalNavigationDrawer**:
  - Overlays content with a scrim when open; content behind is not directly interactable.
  - Good for apps where navigation is secondary and shouldn’t always be visible.
- **DismissibleNavigationDrawer**:
  - Drawer that sits alongside content and can be swiped away.
  - More appropriate on larger screens where persistent navigation is desired.

### ModalNavigationDrawer
- **Category**: Drawer / Navigation
- **What it is**: A side drawer that slides over content with a scrim, typically used for navigation.
- **When to use**:
  - As an alternative to bottom navigation for apps with many top-level destinations.
  - When you want navigation options accessible but not always visible.
- **Common pitfalls**:
  - Overloading drawers with too many unrelated actions.
  - Using drawers as the only way to reach critical actions (discoverability issues).

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalNavigationDrawerExample(
    drawerState: DrawerState,
    destinations: List<Destination>,
    currentRoute: String?,
    onDestinationSelected: (Destination) -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                destinations.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = destination.route == currentRoute,
                        onClick = { onDestinationSelected(destination) }
                    )
                }
            }
        }
    ) {
        content()
    }
}
```

### DismissibleNavigationDrawer
- **Category**: Drawer / Navigation
- **What it is**: A drawer that can be swiped in/out alongside the main content, more common on larger screens where persistent navigation is desired.
- **When to use**:
  - Large-screen layouts where navigation can remain visible or partially expanded.
  - As part of responsive layouts where the same destinations appear in a drawer on large screens and a bottom bar on small screens.
- **Common pitfalls**:
  - Not aligning drawer behavior with device size classes (e.g., always showing on phones).
  - Overcomplicating layouts by combining drawer, rail, and bottom nav without a clear strategy.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleNavigationDrawerExample(
    drawerState: DrawerState,
    destinations: List<Destination>,
    currentRoute: String?,
    onDestinationSelected: (Destination) -> Unit,
    content: @Composable () -> Unit
) {
    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                destinations.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = destination.route == currentRoute,
                        onClick = { onDestinationSelected(destination) }
                    )
                }
            }
        }
    ) {
        content()
    }
}
```

---

## Sheets
### ModalBottomSheet vs BottomSheetScaffold
- **ModalBottomSheet**:
  - Ephemeral, modal surface presented over existing content.
  - Good for short tasks, confirmations, pickers, or contextual actions.
- **BottomSheetScaffold**:
  - Structural, persistent sheet integrated into the screen’s layout.

### ModalBottomSheet
- **Category**: Sheet / Modal surface
- **What it is**: A modal bottom sheet that appears over the current screen, often dismissible by tapping outside or swiping down.
- **When to use**:
  - For transient, focused tasks that temporarily take over part of the screen.
  - As a lightweight alternative to a full-screen dialog or new screen.
- **Common pitfalls**:
  - Overusing modal sheets for primary flows, leading to confusing navigation.
  - Not handling back behavior and dismissal consistently.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetExample(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
    sheetContent: @Composable () -> Unit
) {
    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            sheetContent()
        }
    }

    // Underlying screen content remains defined outside the sheet,
    // since the sheet is an ephemeral layer over it.
    content()
}
```
