# Resources:


# Layout Composables in Jetpack Compose

Layout composables are responsible for measuring and placing their children.

Includes (non-exhaustive):
- Core layouts: `Row`, `Column`, `Box`, `BoxWithConstraints`, `Spacer`
- Lazy layouts: `LazyRow`, `LazyColumn`, `LazyVerticalGrid`
- Paging layouts: `Pager` (Horizontal/Vertical), tab/pager combos
- Advanced layouts: `ConstraintLayout`, `FlowRow`, `FlowColumn`, `SubcomposeLayout`

---

## Template per layout component
For each layout composable you document, aim to capture:
- **Name**
- **Category** (e.g., Core layout, Lazy layout, Advanced layout)
- **What it is** (1–2 sentences)
- **When to use** (and when not to)
- **Common pitfalls** (constraints, nesting, scroll, measurement)
- **Performance notes** (allocation, measurement cost, lazy vs non-lazy)
- **Related modifiers** (just names; details go in `Modifiers.md`)
- **Figma / Material mapping** (if applicable)
- **Examples / snippets** (optional)

The sections below use this template for a few core layouts as examples.

---

## Row
- **Category**: Core layout
- **What it is**: Lays out children horizontally in a single row, respecting horizontal arrangement and vertical alignment.
- **When to use**:
  - Simple horizontal arrangements: icon + text, button groups, form fields with labels.
  - When the number of children is small and fixed or bounded.
- **Common pitfalls**:
  - Forgetting to use `weight` for flexible children, causing cramped or uneven layouts.
  - Combining `fillMaxWidth()` on multiple children without `weight`, leading to unexpected overflows or clipping.
  - Deeply nested `Row`/`Column` hierarchies increasing layout complexity; consider flattening or using `Box`/`ConstraintLayout` for complex cases.
- **Performance notes**:
  - Cheap to measure and layout when the number of children is small.
  - Many deeply nested rows/columns can add up; profile complex hierarchies if performance is a concern.
- **Related modifiers**:
  - `padding`, `fillMaxWidth`, `wrapContentWidth`, `weight`, `background`, `clickable`
- **Figma / Material mapping**:
  - Often corresponds to horizontal auto-layout frames or rows in design tools.

---

## Column
- **Category**: Core layout
- **What it is**: Lays out children vertically in a single column, respecting vertical arrangement and horizontal alignment.
- **When to use**:
  - Vertical stacking of elements: forms, lists of settings, content sections.
  - When the number of children is small or moderate and all content can reasonably be composed at once.
- **Common pitfalls**:
  - Using `Column` instead of `LazyColumn` for long or unbounded lists, causing unnecessary composition and rendering work.
  - Overusing `fillMaxHeight()` leading to unexpected stretching; `wrapContentHeight()` is often more appropriate.
  - Not handling insets (status bar, navigation bar); prefer using insets-aware padding when needed.
- **Performance notes**:
  - Fine for bounded content; avoid for long, scrollable lists.
  - Consider `verticalScroll` for scrollable content that is still bounded, but switch to `LazyColumn` for large data sets.
- **Related modifiers**:
  - `padding`, `fillMaxWidth`, `wrapContentHeight`, `verticalScroll`, `weight`
- **Figma / Material mapping**:
  - Often corresponds to vertical auto-layout frames or stacks.

---

## LazyColumn
- **Category**: Lazy layout
- **What it is**: A vertically scrolling list that composes and lays out only the visible items and a small buffer, similar to `RecyclerView`.
- **When to use**:
  - Long or potentially unbounded lists of homogeneous or heterogeneous items.
  - Any list where performance and memory usage matter (most production lists).
- **Common pitfalls**:
  - Forgetting to provide stable keys for items that change position, leading to state mismatch during recomposition (`items(..., key = { ... })`).
  - Doing heavy work in item content without memoization, causing jank during scroll.
  - Nesting multiple scrollable containers (e.g., `LazyColumn` inside a `Column` with `verticalScroll`) leading to confusing scroll behavior.
- **Performance notes**:
  - Designed for performance, but still requires care: minimize allocations in item lambdas, avoid unnecessary recomposition.
  - Use `remember` and `derivedStateOf` where appropriate inside items to cache expensive computations.
- **Related modifiers**:
  - `fillMaxSize`, `padding`, `weight`, `nestedScroll`, `testTag`
- **Figma / Material mapping**:
  - Represents scrollable lists; in design tools often modeled as repeated list items in a scroll frame.

---

## Box
- **Category**: Core layout
- **What it is**: Stacks children on top of each other, allowing precise alignment of each child within the same bounds.
- **When to use**:
  - Overlays (e.g., badge over icon, loading spinner over content).
  - Simple absolute/relative positioning where `Row`/`Column` would be awkward.
- **Common pitfalls**:
  - Overusing `Box` with many children where a more structured layout (e.g., `ConstraintLayout`) would be clearer.
  - Forgetting that later children are drawn on top of earlier ones, causing visual overlap issues.
- **Performance notes**:
  - Similar cost to `Row`/`Column` for small child counts.
- **Related modifiers**:
  - `fillMaxSize`, `align`, `matchParentSize`, `padding`, `background`
- **Figma / Material mapping**:
  - Similar to a frame with absolutely positioned children or overlays.
