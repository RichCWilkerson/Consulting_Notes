# Resources:

- [official docs](https://developer.android.com/develop/ui/compose/phases)


# Overview

## Concepts & Vocabulary
- **Composable**: A function annotated with `@Composable` that describes part of the UI.
  - Allows building UI declaratively by calling other composables.
- **Composition**: The process of running composable functions to build a UI tree.
- **Composable tree**: The hierarchical structure of composables that make up the UI.
- **Layout composable**: A composable whose primary responsibility is to measure and place children (e.g., `Row`, `Column`, `Box`, `LazyColumn`, `Pager`).
- **Widget / primitive**: A leaf-ish UI element such as `Text`, `Button`, `Image`, `TextField`, `Checkbox`, `Switch`, `Slider`.
- **Scaffold / screen shell**: Higher-level containers that structure a whole screen, such as `Scaffold`, app bars, navigation bars, and drawers.
- **Modifier**: A chained description of how to decorate or lay out a composable (size, padding, click handling, semantics, animation, etc.).
- **State**: Data that, when changed, causes recomposition of the parts of the UI that read it.
- **Recomposition**: Re-running composable functions when state changes, to update the UI.
- **Side-effect**: Work that touches the outside world or non-composable APIs (e.g., logging, navigation, coroutines, flows), managed with effect handlers like `LaunchedEffect`.


## Jetpack Compose Lifecycle Phases
1. Enter the Composition — When Jetpack compose runs the composables first time, It keeps track of Composables used to describe the UI and builds a tree-structure of all composables that’s called Composition.
2. Recomposition — It’s the phase when any state changes which eventually impacts the UI, Jetpack Compose smartly identifies those Composables and recomposes only them without the need to update all Composables.
3. Leave the Composition — It’s the last phase when the UI is no longer visible so it removes all resources consumed.


## Jetpack Compose Phases (Rendering)
1. Composition: What UI to show. Compose runs composable functions and creates a description of your UI. 
2. Layout: Where to place UI. This phase consists of two steps: measurement and placement. Layout elements measure and place themselves and any child elements in 2D coordinates, for each node in the layout tree. 
3. Drawing: How it renders. UI elements draw into a Canvas, usually a device screen.