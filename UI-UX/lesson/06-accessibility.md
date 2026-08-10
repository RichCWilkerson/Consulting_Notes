# Accessibility and Inclusive Design

## Why this matters

Accessibility makes products usable by people with disabilities and improves resilience for everyone. It is a design and engineering responsibility, not a final compliance check.

## Core concepts

- **WCAG:** Widely used accessibility guidance organized around perceivable, operable, understandable, and robust experiences.
- **Keyboard access:** Interactive controls must be reachable and usable without a pointer.
- **Focus management:** Keyboard and assistive-technology focus must be visible, logical, and moved intentionally.
- **Semantic structure:** Headings, labels, landmarks, roles, and status updates communicate meaning to assistive technology.
- **Accessible forms:** Labels, instructions, validation, errors, and required fields must be clear and programmatically connected.

## Think through

1. Can the workflow be completed with keyboard only?
2. What would a screen reader announce for each control and status change?
3. Is focus visible and placed logically after a modal, error, or dynamic update?
4. Does every non-text cue have a text or structural alternative?
5. Are contrast, target size, motion, and zoom considered?

## Practice

Audit a login or checkout flow against keyboard navigation, labels, focus order, error messages, contrast, and zoom.

## Deliverable

Create an accessibility checklist and prioritized remediation list with the issue, impacted users, severity, recommendation, and owner.

## Common interview questions

### How do you incorporate accessibility into your design process?

I include it from the first flow and component decisions rather than adding it at the end. I specify keyboard behavior, focus order, labels, error handling, contrast, and responsive behavior in design reviews and component documentation. I also partner with engineers and QA to validate real implementation with keyboard navigation and assistive technology.

### How would you explain the value of accessibility to a stakeholder focused on speed?

Accessibility reduces exclusion and legal risk, but it also improves quality for all users: clear forms, resilient interactions, readable content, and keyboard-friendly workflows are broadly useful. Building it into reusable components is faster and cheaper than repeatedly fixing issues late in individual features.

## Key terms / cheat sheet

- **WCAG:** Web Content Accessibility Guidelines.
- **Focus indicator:** A visible marker showing the active keyboard target.
- **Semantic HTML:** Elements that communicate their meaning to browsers and assistive technology.

## Scenario challenge

Audit a modal form with required fields and dynamic validation. Define keyboard order, focus behavior, error announcement, contrast, and how focus returns on close.

## Common pitfalls

- Checking color contrast but ignoring keyboard and screen-reader behavior.
- Moving focus unpredictably after dialogs or dynamic updates.
- Adding placeholder text instead of persistent field labels.

## Review checklist

- [ ] Every action is keyboard reachable with a visible focus indicator.
- [ ] Labels, errors, and status changes are understandable to assistive technology.
- [ ] Color, motion, zoom, and target size have been considered.

## Portfolio evidence

Show the audit, prioritized findings, component specifications, and results from implementation validation.

## Flashcards

**Q:** Why is a visible focus indicator necessary?  
**A:** Keyboard users need to know which element will receive their next action.

**Q:** Why are placeholders not sufficient labels?  
**A:** They disappear during input and may not be reliably announced as a field's purpose.

## Resources

- [W3C: WCAG 2.2](https://www.w3.org/TR/WCAG22/)
- [WebAIM: Keyboard Accessibility](https://webaim.org/techniques/keyboard/)

## Reflection log

- Could you complete your most-used workflow with keyboard only?
- Which accessibility practice would reduce the most risk in your current work?
