# Interaction Design

## Why this matters

Interaction design defines how an experience behaves, not only how it looks. It makes screens, controls, transitions, states, and edge cases feel predictable.

## Core concepts

- **Affordance:** A cue that suggests how an interface element can be used.
- **Progressive disclosure:** Reveal complexity when it becomes relevant rather than all at once.
- **Recognition over recall:** Show relevant options and context instead of making users remember them.
- **System states:** Default, hover, focus, pressed, selected, disabled, loading, empty, success, and error.
- **Responsive behavior:** Preserve task intent across screen sizes and input methods.

## Think through

1. What action is primary on this screen, and is it visually clear?
2. What happens before, during, and after a user action?
3. What does the user see when data is missing, delayed, invalid, or unavailable?
4. Can users undo or safely confirm consequential actions?
5. How will the flow behave on touch, keyboard, and narrow screens?

## Practice

Design a create-and-submit form. Document validation, saving, errors, cancellation, confirmation, success, and retry behavior.

## Deliverable

Create a task flow and annotated wireframes that cover the happy path plus loading, empty, validation-error, and failure states.

## Common interview questions

### How do you design for edge cases without overcomplicating the interface?

I prioritize edge cases by likelihood and consequence. I make high-risk cases explicit in the primary design, such as validation errors or irreversible actions, while using progressive disclosure for uncommon complexity. I document the states and decisions with engineering early so we avoid discovering critical behavior during implementation.

### How do you know whether a flow is intuitive?

I do not rely on my own familiarity with the design. I define the user task and success criteria, then test representative users with a prototype or live product. I observe whether they understand the next step, recover from problems, and complete the task efficiently; I combine that evidence with completion rate, error rate, and qualitative feedback.

## Key terms / cheat sheet

- **Affordance:** A cue suggesting how an element can be used.
- **Progressive disclosure:** Revealing complexity only when it is relevant.
- **Empty state:** The experience shown when no data or content is available.

## Scenario challenge

Design a bulk-edit flow for 500 records. Specify selection, confirmation, permissions, progress, partial failure, undo, and audit behavior.

## Common pitfalls

- Designing only static screens instead of behavior between states.
- Hiding destructive consequences or making recovery impossible.
- Treating mobile responsiveness as merely shrinking a desktop layout.

## Review checklist

- [ ] Primary actions and their consequences are clear.
- [ ] Loading, empty, error, success, and disabled states are designed.
- [ ] The flow works with touch, keyboard, and different screen sizes.

## Portfolio evidence

Show annotated flows, state designs, prototype observations, and the interaction tradeoffs you made.

## Flashcards

**Q:** What does progressive disclosure prevent?  
**A:** Overwhelming users with complexity before it is needed.

**Q:** Why design empty states?  
**A:** They explain what is missing and help users take the next productive action.

## Resources

- [Nielsen Norman Group: Error-Message Guidelines](https://www.nngroup.com/articles/error-message-guidelines/)
- [Material Design: Interaction](https://m3.material.io/foundations/interaction)

## Reflection log

- Which edge case would create the highest user or business risk in your flow?
- What state is commonly missing from designs you have reviewed?
