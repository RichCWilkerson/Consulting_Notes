# Resources:


# Compose Patterns

High-level UI patterns built from multiple composables (layouts, widgets, scaffolds, modifiers, effects).

This file focuses on how to *combine* composables to solve common UI problems.

---

## Template per pattern
For each pattern you document, aim to capture:
- **Name**
- **Problem it solves**
- **Key building blocks** (layouts, widgets, scaffolds, modifiers, effects)
- **State & data flow** (where state lives, how it’s updated)
- **Common pitfalls**
- **Variations** (optional)

---

## Forms
- **Problem it solves**: Collecting and validating user input across multiple fields, with clear feedback and predictable behavior.
- **Key building blocks**:
  - Layouts: `Column`, `LazyColumn` (for long forms)
  - Widgets: `TextField` / `OutlinedTextField`, `Checkbox`, `RadioButton`, `Switch`, `Button`
  - Scaffolds: `Scaffold` (app bar, actions), `TopAppBar`
  - Modifiers: `padding`, `fillMaxWidth`, focus-related modifiers
  - Effects: `LaunchedEffect` for one-off events (e.g., scroll to first error)
- **State & data flow**:
  - Prefer state hoisting: keep form field values and validation state in a view model or parent composable.
  - Use single-source-of-truth for each field; avoid duplicating state in both UI and view model.
- **Common pitfalls**:
  - Performing heavy validation logic on every keystroke.
  - Poor focus management between fields; not using IME actions and `FocusRequester`.
  - Inconsistent error display and messaging.
- **Variations**:
  - Single-step vs multi-step forms.
  - Inline vs dialog-based forms.

---

## List + Detail
- **Problem it solves**: Presenting a list of items and a detailed view for a selected item.
- **Key building blocks**:
  - Layouts: `LazyColumn` / `LazyRow`, `Row`, `Column`
  - Widgets: `Card`, `Image`, `Text`, `Button`
  - Navigation: `NavigationBar`, Navigation Compose (separate file)
  - Modifiers: `clickable`, `padding`, `fillMaxSize`
- **State & data flow**:
  - Keep list data and selection state in a shared view model or navigation graph scope.
  - Pass only IDs/navigation arguments between screens when using Navigation Compose.
- **Common pitfalls**:
  - Duplicating state between list and detail screens instead of sharing via a view model.
  - Poor handling of configuration changes and process death if state isn’t properly persisted.
- **Variations**:
  - Single-pane (phone) vs dual-pane (tablet/desktop) layouts.

---

## Search screen
- **Problem it solves**: Letting users filter or search through a dataset with responsive feedback.
- **Key building blocks**:
  - Layouts: `Column`, `LazyColumn`
  - Widgets: `TextField`/`OutlinedTextField` for the search box, list items, loading and empty state UI.
  - Modifiers: `fillMaxWidth`, `padding`, `verticalScroll` or lazy layout modifiers.
  - Effects: debounced search using coroutines/flows, `LaunchedEffect`.
- **State & data flow**:
  - Keep search query and results state in a view model.
  - Use debouncing or throttling to avoid hitting APIs on every keystroke.
- **Common pitfalls**:
  - No clear loading/empty/error states.
  - Not canceling in-flight requests when queries change.
  - Janky UI when recomputing results on the main thread.
- **Variations**:
  - Local filtering vs remote API search.
  - Suggestions/autocomplete vs plain search.
