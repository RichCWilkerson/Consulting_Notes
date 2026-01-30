# Resources:


# Animation in Jetpack Compose

Compose provides a range of animation APIs, from simple state-based animations to complex gesture-driven motion.

This file focuses on concepts, core APIs, patterns, and pitfalls. Individual components may link here for deeper animation notes.

---

## Concepts
- **Implicit vs explicit animations**: Implicit animations automatically animate between state changes; explicit animations give you fine-grained control.
- **Single-value vs transition-based**: `animate*AsState` vs `updateTransition` / `Transition`.
- **One-shot vs continuous**: Triggered animations vs animations driven by gestures, physics, or time.

---

## Core implicit animation APIs
Examples: `animate*AsState` (e.g., `animateFloatAsState`, `animateColorAsState`).

Capture for each:
- Typical use cases.
- Pitfalls (e.g., recomposition triggers, multiple concurrent animations).

---

## Transition-based animations
Examples: `updateTransition`, `Transition`, `AnimatedVisibility`, `Crossfade`.

Notes:
- When to move from simple `animate*AsState` to transitions.
- Coordinating multiple animated properties.

---

## Gesture- and physics-based animations
Examples: `Animatable`, decay animations, fling/drag patterns.

Capture:
- Patterns for draggable sheets, swipable cards, etc.
- Handling interruption, cancellation, and snapping.

---

## Layout & item animations
Examples: `animateContentSize`, `animateItemPlacement` in lazy layouts.

Notes:
- When layout animations are sufficient vs when you need full control.
- Performance considerations in lists.

---

## Common animation pitfalls
- Animations restarting unnecessarily due to state changes or recomposition.
- Blocking the main thread in animation callbacks.
- Confusing UX from conflicting gestures and animations.

Use this section to accumulate real-world gotchas as you encounter them.
