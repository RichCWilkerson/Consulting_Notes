# Figma, Prototyping, and AI-Assisted Practice

## Why this matters

Tools should make reasoning, collaboration, and validation faster; they do not replace those activities. For this role, Figma fluency supports systems work, clear handoff, and realistic prototypes.

## Core concepts

- **Auto layout:** Rules that allow components and screens to adapt to changing content and sizes.
- **Variables and modes:** Reusable values and theme or state variations, often used for design tokens.
- **Components and variants:** Reusable building blocks and their controlled configurations.
- **Prototyping:** Simulate the critical flow well enough to test understanding and behavior.
- **Dev Mode and handoff:** Give engineers inspectable specs, assets, states, and behavior without treating tools as the entire source of truth.
- **AI-assisted work:** Useful for research transcription, clustering, drafting documentation, generating alternatives, and checking coverage; always validate outputs and protect sensitive data.

## Think through

1. What question must this prototype answer?
2. Which interactions must be realistic, and which can remain static?
3. Can the file structure, naming, and component properties be understood by another designer?
4. What source evidence can verify an AI-generated synthesis or recommendation?
5. Does the approved tool and data-handling policy allow the input?

## Practice

Build a responsive form component with variables, variants, validation states, and a clickable prototype for a successful and failed submission.

## Deliverable

Create a Figma file with a page structure, reusable component, prototype flow, and implementation notes for engineering.

## Common interview questions

### How do you use Figma effectively with engineers?

I use components, variables, clear naming, and documented states so the file communicates behavior rather than just static pixels. I review flows with engineering before handoff, use prototypes and annotations for unclear interactions, and keep design-system assets aligned with the implemented component library. The goal is a shared understanding, not a file thrown over a wall.

### How do you use AI in UX work responsibly?

I use it to accelerate low-risk work such as clustering notes, drafting a research summary, generating alternatives, or identifying documentation gaps. I validate every output against source evidence, disclose appropriate use, and follow organizational policy about customer data, research recordings, and proprietary information. AI can speed synthesis, but it cannot replace user research or accountable judgment.

## Key terms / cheat sheet

- **Auto layout:** Layout rules that adapt a design to content and size changes.
- **Variant:** A controlled configuration of a reusable component.
- **Design-to-code alignment:** Keeping design-system behavior consistent between Figma and implementation.

## Scenario challenge

An engineer says a prototype omits states needed for implementation. Update the handoff plan to specify component properties, validation, loading, error, responsive, and accessibility behavior.

## Common pitfalls

- Treating a polished static frame as sufficient interaction documentation.
- Creating disconnected Figma components that do not match code.
- Sending sensitive research or product data to an unapproved AI tool.

## Review checklist

- [ ] Components have clear names, properties, states, and content rules.
- [ ] The prototype answers a specific validation question.
- [ ] Handoff covers responsive behavior, accessibility, and non-happy paths.

## Portfolio evidence

Show the component structure, prototype, handoff annotation, and an example of feedback that improved implementation quality.

## Flashcards

**Q:** What should a prototype validate?  
**A:** A specific question about understanding, behavior, or a critical interaction, not every possible detail.

**Q:** What makes AI-assisted synthesis trustworthy?  
**A:** It is checked against source evidence and used within approved data-handling rules.

## Resources

- [Figma: Auto Layout](https://help.figma.com/hc/en-us/articles/360040451373-Guide-to-auto-layout)
- [Figma: Dev Mode](https://help.figma.com/hc/en-us/articles/15023124644247-Guide-to-Dev-Mode)

## Reflection log

- Which interaction state would an engineer be unable to infer from your current design file?
- What task could AI accelerate without replacing your judgment or exposing sensitive data?
