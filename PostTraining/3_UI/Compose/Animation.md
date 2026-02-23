# Resources:
- [Snapping Effects - Medium](https://medium.com/deuk/android-compose-ux-techniques-implementing-snapping-effects-with-lazyrow-2c3fae9e3db1)
- [Animated Dynamic Tab Selector - Medium](https://medium.com/deuk/android-task-app-implementation-series-animated-dynamic-tab-selector-df241d3b5543)
- [Build Dynamic Bank Card - Medium](https://medium.com/deuk/intermediate-android-compose-bank-card-ui-371d14ea7843)
- [Custom Circular Progress Bar - Medium](https://medium.com/deuk/intermediate-android-compose-tutorial-building-a-custom-circular-progress-bar-88d316963a57)

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

### Avoid unnecessary recompositions
- Use `remember` to store animation state that shouldn't reset on recomposition.
- State should not be read inside the composable function -> will cause recomposition and restart animation -> causes performance issues and janky animations

- graphicsLayer is a modifier that allows you to apply transformations (like rotation, scaling, translation) directly to the composable without affecting its layout. This can help avoid unnecessary recompositions when animating properties like rotation, as the animation state is managed within the graphicsLayer instead of the composable itself.
  - this only fixes recompositions related to the drawing phase of composables, not those related to layout or measurement phases. So if your animation affects layout (e.g., changing size, position, etc.), you may still encounter recompositions.
```kotlin
@Composable
fun RotatingIcon() {
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Loading",
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                rotationZ = remember { Animatable(0f) }.value // add rotation state to graphicsLayer to avoid recomposition of entire composable
            }
            .rotate(remember { Animatable(0f) }.value) // remove rotation state from composable to avoid recomposition of entire composable
    )
        Text("Loading...")
}
```

Use this section to accumulate real-world gotchas as you encounter them.


# Common animation patterns
## Rotating animation

```kotlin
@Composable
fun RotatingLoadingArrow(){
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, // Start at 0 degrees
        targetValue = 360f, // Rotate full circle to 360 degrees
        animationSpec = infiniteRepeatable( // Repeat indefinitely
            animation = tween(1000, easing = LinearEasing) // Rotate over 1 second with linear easing (constant speed)
        )
    )

    Icon(
        imageVector = Icons.Default.ArrowUpward,
        contentDescription = "Loading",
        modifier = Modifier.rotate(rotation)
    )
}
```


## Fading animation

## Scaling animation

## Sliding animation


TODO: ect.