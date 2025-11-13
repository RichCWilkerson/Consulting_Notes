# Team Leadership — Detailed Breakdown

Outcomes
- Deliver predictably, raise code quality, grow engineers.

Core Areas
- Process
- People
- Code Quality
- Delivery

---

## Process
- Shape work: product discovery, PRDs, acceptance criteria, Definition of Done
- Sprint rituals: planning, daily standup, backlog grooming, review, retro
  - TODO: what is backlog grooming?
  - TODO: diff between sprint review and retro?
- Metrics: lead time, cycle time, WIP, defect rate, deployment frequency
  - TODO: what are each of these metrics? what do they measure? why important?
- Working agreements: focus time, code review SLAs, branching strategy
  - TODO: what is branching strategy? examples for leads to implement?
  - TODO: code review SLAs: what are they? examples?
- Risk management: identify, track, mitigate; pre-mortems/post-mortems
  - TODO: what is pre-mortem vs post-mortem?

Artifacts
- Team charter and responsibilities matrix (RACI)
  - TODO: what is RACI?
  - TODO: example of team charter?
- Roadmap (quarterly) + Sprint goal (bi-weekly)
  - TODO: what does a roadmap look like for a mobile engineering team?
  - TODO: example of sprint goal?
- Incident playbooks and on-call runbooks
  - TODO: what is the diff between playbook and runbook?

## People
- 1:1s: weekly/bi-weekly with notes and action items
- Growth: Individual Growth Plans tied to ladder
- Feedback: SBI model; praise publicly, coach privately
- Hiring: scorecards, structured interviews, code exercise rubric
- Inclusion: rotate liaisons, ensure meeting equity, async-first docs

## Code Quality
- Definition of Ready/Done includes tests, docs, telemetry
- Code review guidelines: small PRs, checklist (naming, complexity, tests)
- Tech debt budget: reserve 10–20% capacity; maintain a debt backlog
- Architecture decision records (ADRs) in repo
- Secure coding: OWASP Mobile Top 10 awareness

## Delivery
- CI/CD: trunk-based or short-lived branches; pre-commit hooks
- Testing pyramid: unit > integration > E2E; contract tests for APIs
- Observability: logging, metrics, tracing; dashboards and SLOs
- Release management: feature flags, staged rollouts, fast rollback

## Ritual Templates
- Retro prompts: What to start/stop/continue? What surprised us? What’s still confusing?
- Incident template: Summary, Impact, Timeline, Root Cause, Actions, Owners, Due dates
- PR checklist: tests updated, screenshots/video, perf impact, security review

## Anti-Patterns to Avoid
- Big-bang rewrites without guardrails
- Hero culture; knowledge silos
- Process cargo-culting without measuring outcomes

## 30/60/90 Plan for a New Lead
- 30: Listen, map systems, define quality bars, stabilize delivery
- 60: Remove top 2 bottlenecks; standardize reviews; introduce metrics
- 90: Align roadmap, invest in people growth, reduce incident rate

---

## Android Engineer Notes
- Keep standards pragmatic: Kotlin style guide, Compose best practices, and API review checklist tailored to your codebase.
- Codify review expectations with a lightweight PR template and a small "Definition of Done" that includes tests and screenshots for UI changes.
- Track a few engineering health metrics in CI (lint warnings, unit test count/coverage trend, build time) and review weekly.
- Start ADRs for significant tech decisions (e.g., choose RN vs Flutter vs KMM for a feature); 1–2 pages max.
- Run monthly brown bags to share findings from cross-platform experiments and reduce knowledge silos.
