# Resources:

- [official docs](https://developer.android.com/develop/ui/compose/phases)


# Overview

## Jetpack Compose Lifecycle Phases
1. Enter the Composition — When Jetpack compose runs the composables first time, It keeps track of Composables used to describe the UI and builds a tree-structure of all composables that’s called Composition.
2. Recomposition — It’s the phase when any state changes which eventually impacts the UI, Jetpack Compose smartly identifies those Composables and recomposes only them without the need to update all Composables.
3. Leave the Composition — It’s the last phase when the UI is no longer visible so it removes all resources consumed.


## Jetpack Compose Phases (Rendering)
1. Composition: What UI to show. Compose runs composable functions and creates a description of your UI. 
2. Layout: Where to place UI. This phase consists of two steps: measurement and placement. Layout elements measure and place themselves and any child elements in 2D coordinates, for each node in the layout tree. 
3. Drawing: How it renders. UI elements draw into a Canvas, usually a device screen.