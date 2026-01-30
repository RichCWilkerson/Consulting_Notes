# Resources:


# Widget Composables in Jetpack Compose

Catalog of core widget/primitive composables: things that display content or capture input.

Focus on the 90–95% of widgets a senior Android engineer uses regularly. Rare or obscure components can be mentioned briefly with a short use case.

Includes (non-exhaustive):
- Text & typography: `Text`, `ClickableText`, `SelectionContainer`
- Images & icons: `Image`, `Icon`
- Buttons: `Button`, `OutlinedButton`, `TextButton`, `IconButton`, FAB variants
- Inputs: `TextField`, `OutlinedTextField`, `Checkbox`, `RadioButton`, `Switch`, `Slider`

---

## Template per component
For each widget you document, aim to capture:
- **Name**
- **Category** (e.g., Text, Button, Input)
- **What it is** (1–2 sentences)
- **When to use** (and when not to)
- **Common pitfalls** (state, focus, performance, accessibility)
- **Performance notes** (if relevant)
- **Related modifiers** (just names; details go in `Modifiers.md`)
- **Figma / Material mapping** (what it’s called in design tools)
- **Examples / snippets** (optional)

The sections below use this template for a few core widgets as examples.

---

## Text
- **Category**: Text / Display
- **What it is**: Basic text rendering composable. Non-interactive by default.
- **When to use**:
  - Display labels, body text, headings, helper/error messages.
  - Any static or read-only textual content.
- **Common pitfalls**:
  - Long text truncation: forgetting `maxLines` and `overflow = TextOverflow.Ellipsis` can cause layout issues.
  - Styling scatter: mixing inline `style` vs Material typography tokens inconsistently; prefer using theme typography (`MaterialTheme.typography`) for consistency.
  - Accessibility: insufficient contrast or using text as the only signal (e.g., missing icons or contentDescription elsewhere).
- **Performance notes**:
  - Frequent recomposition of large blocks of text can be expensive; prefer splitting text into smaller composables when only part of it changes.
  - Heavy styling (lots of spans) is more costly; cache where appropriate.
- **Related modifiers**:
  - `padding`, `fillMaxWidth`, `background`, `clickable`, `semantics`, `alpha`
- **Figma / Material mapping**:
  - Figma: “Text” layers
  - Material: Typography styles (Display, Headline, Title, Body, Label)

---

## Button
- **Category**: Button / Action
- **What it is**: High-emphasis clickable surface to trigger primary actions.
- **When to use**:
  - Primary actions on a screen or within a section.
  - Use variants (`OutlinedButton`, `TextButton`) for medium/low emphasis actions.
- **Common pitfalls**:
  - Overusing primary buttons: too many high-emphasis buttons reduces clarity.
  - Not handling enabled/disabled states consistently (logic in view model vs UI).
  - Nested click targets (e.g., `clickable` parent and `Button` child) causing double-handling or gesture conflicts.
- **Performance notes**:
  - Buttons themselves are cheap; the work usually comes from what happens in `onClick` (e.g., heavy operations on main thread).
- **Related modifiers**:
  - `padding`, `fillMaxWidth`, `weight`, `width`, `height`, `testTag`
- **Figma / Material mapping**:
  - Figma: “Button / Primary”, “Button / Secondary” etc.
  - Material: `FilledButton`, `OutlinedButton`, `TextButton`, FABs

---

## TextField / OutlinedTextField
- **Category**: Input / Forms
- **What it is**: Single-line or multi-line text input field, often with label, placeholder, leading/trailing icons, and error state.
- **When to use**:
  - User text entry in forms, search fields, settings, etc.
  - `OutlinedTextField` is common for forms; `TextField` (filled) is more prominent.
- **Common pitfalls**:
  - State hoisting: keeping the text state locally instead of in a parent/view model, making validation and reuse harder.
  - Focus handling: forgetting to manage focus for multi-field forms; consider `FocusRequester` and `ImeAction`.
  - Error messaging: not surfacing validation errors clearly (missing `isError` and helper/error text).
  - Performance: recomputing heavy logic in `onValueChange` on every keystroke.
- **Performance notes**:
  - Avoid complex business logic in `onValueChange`; debounce or move to view model when possible.
  - Be mindful of recomposition of entire forms on each character; structure composables to limit the recomposition scope.
- **Related modifiers**:
  - `fillMaxWidth`, `padding`, `onFocusChanged`, `testTag`, `semantics`
- **Figma / Material mapping**:
  - Figma: “Text Input / Filled”, “Text Input / Outlined”
  - Material: Filled text field, Outlined text field

---

## Checkbox
- **Category**: Input / Selection
- **What it is**: Binary selection control typically used in lists or settings.
- **When to use**:
  - Yes/no, on/off-type boolean options where the label is adjacent.
  - Multi-select lists where each item can be independently toggled.
- **Common pitfalls**:
  - Forgetting to make the whole row clickable (label + checkbox) for better usability.
  - Having multiple sources of truth for checked state (UI vs view model).
  - Not providing proper semantics / content descriptions for accessibility when labels are non-textual.
- **Performance notes**:
  - Cheap to render; focus on keeping state consistent and minimizing unnecessary recomposition of large lists.
- **Related modifiers**:
  - `clickable`, `padding`, `semantics`, `testTag`
- **Figma / Material mapping**:
  - Figma: “Checkbox” component
  - Material: Checkbox
