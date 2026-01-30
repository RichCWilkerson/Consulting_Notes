# Resources:


# Compose Pitfalls

Cross-cutting issues and common mistakes that apply to many composables (layouts, widgets, scaffolds, etc.).

This file focuses on *what goes wrong* and how to avoid it.

---

## Over-recomposition
- **Symptom**: UI feels janky or slow; profiler shows frequent recomposition of large parts of the tree.
- **Common causes**:
  - Passing changing lambdas or objects without `remember` to child composables.
  - Keeping too much state at the top level instead of closer to where it’s used.
  - Using `mutableStateOf` for large or frequently changing objects instead of more granular state.
- **Mitigations**:
  - Hoist state thoughtfully and avoid unnecessary observers.
  - Use `remember` and `derivedStateOf` to cache derived values.
  - Split large composables into smaller ones so only the necessary parts recompose.

---

## Misplaced state (state hoisting mistakes)
- **Symptom**: Hard-to-follow data flow, bugs when multiple components try to own the same state.
- **Common causes**:
  - Keeping mutable state inside leaf composables that need to be controlled from above.
  - Duplicating the same state in multiple places (view model + UI local).
- **Mitigations**:
  - Follow state hoisting patterns: prefer single source of truth and pass state + events down.
  - Make leaf composables stateless where possible, with state managed by parents.

---

## Layout & scroll issues
- **Symptom**: Content clipped, overlapping app bars, awkward nested scrolling.
- **Common causes**:
  - Forgetting to apply insets-aware padding when using app bars and navigation bars.
  - Nesting multiple scrollable containers without understanding how gestures are handled.
  - Using non-lazy layouts (`Column` with `verticalScroll`) for large lists.
- **Mitigations**:
  - Use `LazyColumn`/`LazyRow` for long lists.
  - Be deliberate with scroll containers and use `nestedScroll` where appropriate.
  - Respect system bars using insets APIs and modifiers.

---

## Modifier order confusion
- **Symptom**: Unexpected sizes, padding, or click areas; visuals not matching designs.
- **Common causes**:
  - Not realizing that modifier order matters (e.g., `background().padding()` vs `padding().background()`).
  - Mixing layout and drawing modifiers in unclear sequences.
- **Mitigations**:
  - Remember that modifiers are applied in order; experiment and document patterns that work well.
  - Keep related modifiers grouped for readability.

---

## List performance issues
- **Symptom**: Janky scrolling, dropped frames, high CPU usage.
- **Common causes**:
  - Doing heavy work per item without memoization.
  - Missing stable keys for items that change order or content.
  - Overly complex item layouts causing expensive measure/layout.
- **Mitigations**:
  - Use `items(..., key = ...)` with stable identifiers.
  - Avoid heavy work in item lambdas; use `remember`, move logic to view model when possible.
  - Profile and simplify list item layouts when necessary.
