# Resources:


# Modifiers in Jetpack Compose

Modifiers decorate or change the behavior of composables: layout, drawing, input handling, semantics, animation, and more.

This file groups important modifiers by responsibility. Individual components will reference modifier names; deep explanations live here.

---

## Concepts
- **Modifier chain**: Modifiers are applied in order, forming a chain that Compose uses when measuring, laying out, drawing, and handling input.
- **Immutability**: Modifiers are immutable; chaining returns new modifier instances.
- **Scope-specific modifiers**: Some modifiers are only available in certain scopes (e.g., `RowScope.weight`).

---

## Layout modifiers
Examples: `padding`, `size`, `fillMaxWidth`, `fillMaxHeight`, `fillMaxSize`, `wrapContentSize`, `weight`, `offset`.

For each modifier family you care about, you can capture:
- What it does conceptually.
- Gotchas (e.g., order of `padding` vs `background`, interaction with parent constraints).
- Typical usage patterns.

---

## Drawing & appearance modifiers
Examples: `background`, `border`, `clip`, `alpha`, `shadow`, `graphicsLayer`.

Use this section to note:
- Layering and performance implications (`graphicsLayer`, shadows).
- How clipping interacts with click targets and semantics.

---

## Input & interaction modifiers
Examples: `clickable`, `combinedClickable`, `pointerInput`, `draggable`, `scrollable`.

Capture:
- Differences between `clickable` and `combinedClickable`.
- Gesture detection patterns and cancellation behavior.

---

## Scroll & nested scroll modifiers
Examples: `verticalScroll`, `horizontalScroll`, `nestedScroll`, `scrollable`.

Useful notes:
- When to use scroll modifiers vs lazy layouts.
- Nested scroll coordination between app bars, lists, and sheets.

---

## Focus & keyboard modifiers
Examples: `focusRequester`, `onFocusChanged`, `focusable`, `imePadding`.

Capture:
- Patterns for form navigation and IME actions.
- Avoiding focus traps.

---

## Semantics & accessibility modifiers
Examples: `semantics`, `clearAndSetSemantics`, `contentDescription`, testing tags.

Useful for:
- Making custom components accessible and testable.
- Overriding or hiding internal semantics when necessary.

---

## Animation-related modifiers
Examples: `animateContentSize`, `animateItemPlacement` (for lazy layouts).

Capture:
- When to prefer simple implicit layout animations vs explicit `Animatable`.
- Performance considerations when animating layout changes.
