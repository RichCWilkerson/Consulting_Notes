# Team Leadership — Detailed Breakdown

Outcomes
- Deliver predictably, raise code quality, grow engineers.

---

## Pushing a New Feature

If an interviewer wants to see how I **take ownership of ideas** and handle **ambiguous requirements**, I can frame it like this:

1. **Start from the problem, not the solution**
   - Example: “Our monolithic Android app had slow builds, inconsistent architecture, and hard-to-test features. Releases were risky.”
   - Metrics that led to the idea:
     - Build times, crash rate, PR lead time, incident count, or onboarding feedback.

2. **Shape and document the proposal**
   - Write a short **proposal / RFC**:
     - Problem statement.
     - Goals (e.g., move to Clean MVVM architecture, reduce build time by X%, improve modularity).
     - High-level design (modules, responsibilities, dependency rules).
     - Risks and non-goals.
   - Keep it 1–3 pages so it’s understandable by engineers, PMs, and design.

3. **Collaborate and get buy-in**
   - Share the document with **PM, design, backend, QA, and leadership**.
   - Walk through:
     - Why this matters now (tie to product goals: faster iteration, stability, ability to ship new features).
     - Impact and trade-offs (short-term disruption vs long-term velocity and quality).
   - Incorporate feedback and **adjust scope** (e.g., phased rollout instead of big-bang rewrite).

4. **Plan an incremental path**
   - Instead of “re-architect everything,” define **phases**:
     - Phase 1: Extract 1–2 critical features into new Clean MVVM modules.
     - Phase 2: Move shared components (networking, design system, analytics).
     - Phase 3: Gradual migration of remaining screens.
   - Translate phases into **epics and stories** with clear acceptance criteria.
   - Example task breakdown:
     - Set up base module structure and DI graph.
     - Migrate one feature end-to-end (UI → ViewModel → UseCase → Repository).
     - Add tests/telemetry around the migrated path.

5. **Execution and quality during development**
   - Define a **Definition of Done** for each migrated piece:
     - Tests updated (unit + integration where relevant).
     - Telemetry and basic logging in place.
     - Documentation/README for the module.
   - Code review guidelines:
     - Small PRs focused on one slice of migration.
     - Architecture and ownership enforced via review (no backsliding into old patterns).

6. **Measure success post-launch**
   - Define up-front how you’ll know the change worked:
     - Build time reduction.
     - Crash/incident rate in migrated areas.
     - PR lead time and ease of adding new features.
   - Use telemetry and CI metrics to monitor improvements.
   - Collect qualitative feedback: “Is it easier to work in this module? Can new devs ramp faster?”

> Interview sound bite: 
> “When I push a new architectural change, I start with the pain we’re seeing—like slow builds and fragile releases in a monolith. 
> I document the proposal, socialize it with product, design, and backend, and refactor in phases instead of a big-bang rewrite. 
> Each phase has clear stories, a Definition of Done including tests and telemetry, and we track metrics like build time and crash rate to confirm it’s actually helping. 
> That way the team and stakeholders see the value, not just a refactor for its own sake.”

---

## Process

- **Shape work**: product discovery, PRDs, acceptance criteria, Definition of Done.

- **Sprint rituals**: planning, daily standup, backlog grooming, review, retro
  - **Backlog grooming** (also called refinement):
    - Regularly reviewing and updating the product/engineering backlog so that upcoming work is:
      - Well-defined (clear titles/descriptions).
      - Estimated (rough sizing, like story points or T-shirt sizes).
      - Prioritized (what’s next vs later).
    - As a lead, you help clarify technical dependencies, split oversized stories, and flag risks before sprint planning.
  - **Sprint review vs retrospective**:
    - **Sprint Review**:
      - Focus: **What did we deliver?**
      - Audience: broader (PM, design, stakeholders).
      - Activities: demo completed work, discuss feedback, align on upcoming priorities.
    - **Sprint Retrospective**:
      - Focus: **How did we work?**
      - Audience: usually the **team only** (safe space).
      - Activities: discuss what went well, what didn’t, and agree on concrete improvements to process/communication/quality.

- **Metrics**: lead time, cycle time, WIP, defect rate, deployment frequency
  - **Lead time**:
    - Time from a feature/idea being requested to it being in production.
    - Measures how quickly the team can respond to business needs.
  - **Cycle time**:
    - Time from when work **starts** (in progress) to when it’s **completed**.
    - Measures how efficiently the team executes once they pick something up.
  - **WIP (Work In Progress)**:
    - Number of tasks currently in progress.
    - Too much WIP often means context switching and slower throughput.
  - **Defect rate**:
    - Number of bugs/issues per release or per story/feature.
    - Indicates quality and effectiveness of testing/reviews.
  - **Deployment frequency**:
    - How often you successfully deploy to production (e.g., weekly, daily).
    - Higher frequency (with low defect rate) usually means healthier CI/CD and smaller, safer changes.

- **Working agreements**: focus time, code review SLAs, branching strategy
  - **Branching strategy**:
    - Agreed conventions for how code flows from dev → main → release.
    - Examples:
      - **Trunk-based development**:
        - Everyone merges small, frequent changes into `main`; feature flags used for incomplete work.
      - **GitFlow**:
        - Long-lived `develop` branch, feature branches, release branches, and hotfix branches.
      - As a lead, you choose something **simple enough** for the team size and release cadence, and document it.
  - **Code review SLAs** (Service Level Agreements):
    - Shared expectations for **how quickly reviews should happen** and **what a good review looks like**.
    - Examples:
      - “At least one review within 24 business hours for normal PRs; urgent fixes called out explicitly.”
      - “Reviewers leave concrete feedback on correctness, readability, and tests; authors keep PRs under ~300 lines where possible.”

- **Risk management**: identify, track, mitigate; pre-mortems/post-mortems
  - **Pre-mortem**:
    - Before starting a major project or launch, the team imagines it has **failed badly** and asks: “What likely went wrong?”
    - You list potential failure modes (e.g., API not ready, performance too slow, UX confusion) and add mitigations or checks to the plan.
  - **Post-mortem** (a.k.a. incident review):
    - After a real incident or failure, you analyze what happened
      - Timeline, root cause, contributing factors.
      - What worked well, what didn’t.
      - Action items to prevent or reduce impact next time.
    - Blameless and focused on system/process improvement, not finger-pointing.

---

Artifacts

- **Team charter and responsibilities matrix (RACI)**
  - **RACI** stands for:
    - **R**esponsible – who does the work.
    - **A**ccountable – who owns the outcome and makes final decisions.
    - **C**onsulted – whose input is needed.
    - **I**nformed – who needs to be kept in the loop.
  - **Team charter** example:
    - A short doc that answers:
      - Why does this team exist? (mission)
      - What are our main responsibilities and boundaries?
      - How do we work? (working agreements, meeting cadence, tooling)
      - How do we measure success?
    - For a mobile team: “We own the Android and iOS apps for X product; we are responsible for feature delivery, app quality, and app store releases; we collaborate closely with backend, design, and product.”

- **Roadmap (quarterly) + Sprint goal (bi-weekly)**
  - **Roadmap** for a mobile team:
    - High-level view of major themes and initiatives over a quarter/half:
      - Example:
        - Q1: Authentication modernization (MFA), performance improvements, groundwork for KMM.
        - Q2: New product feature, modularization, observability upgrades.
    - Not a detailed Gantt chart; more like prioritized themes and big bets.
  - **Sprint goal** example:
    - One or two **clear outcomes** for the sprint, not a list of tickets.
      - “Enable biometric login for 50% of sign-in flows and ship to internal beta.”
      - “Split product listing into a feature module and stabilize build times under X minutes.”

- **Incident playbooks and on-call runbooks**
  - **Playbook**:
    - High-level **“if X happens, we follow this script”** document.
    - Example: “If checkout errors spike above threshold, page on-call, roll back to previous version, and enable emergency feature flag.”
  - **Runbook**:
    - More detailed, step-by-step instructions for **how** to carry out actions.
    - Example: exact commands/links to:
      - Check logs and dashboards.
      - Roll back a release.
      - Clear specific caches or restart services.

---

## People

- 1:1s: weekly/bi-weekly with notes and action items.
- Growth: Individual Growth Plans tied to ladder (e.g., specific behaviors/skills for Senior vs Staff).
- Feedback: SBI model (Situation–Behavior–Impact); praise publicly, coach privately.
- Hiring: scorecards, structured interviews, code exercise rubric.
- Inclusion: rotate liaisons, ensure meeting equity, async-first docs.

---

## Code Quality

- Definition of Ready/Done includes tests, docs, telemetry.
- Code review guidelines: small PRs, checklist (naming, complexity, tests).
- Tech debt budget: reserve 10–20% capacity; maintain a debt backlog.
- Architecture decision records (ADRs) in repo.
- Secure coding: OWASP Mobile Top 10 awareness.
  - Common OWASP Mobile risks (simplified):
    1. **Improper platform usage** – misusing Android APIs (e.g., ignoring permissions model, insecure intents).
    2. **Insecure data storage** – storing sensitive data (tokens, passwords) in plaintext on device.
    3. **Insecure communication** – unencrypted or weakly encrypted network traffic; no TLS pinning where appropriate.
    4. **Insecure authentication** – weak login flows, poor session management.
    5. **Insufficient cryptography** – rolling your own crypto, using weak algorithms.
    6. **Insecure authorization** – failure to enforce proper access control on backend/API.
    7. **Client code tampering** – app not protected against reverse engineering or tampering (basic obfuscation, integrity checks).
    8. **Code quality / injection flaws** – classic issues like injection, unsafe deserialization.
    9. **Reverse engineering** – exposing secrets or internal logic that should be protected.
    10. **Extraneous functionality** – debug endpoints or features accidentally shipped to production.
  - As a lead, you don’t need to recite all 10, but you should show awareness of at least:
    - Secure storage (Keystore, EncryptedSharedPreferences/DataStore).
    - Secure network comms (TLS, pinning, no secrets in the app).
    - Proper auth/session management.

---

## Delivery

- CI/CD:
  - Trunk-based or short-lived branches.
  - Pre-commit hooks (lint, formatting) to keep main branch clean.
- Testing pyramid:
  - Unit > integration > E2E.
  - Contract tests for APIs (mock servers, schema validation).
- Observability:
  - Logging, metrics, tracing, dashboards and SLOs.
- Release management:
  - Feature flags.
  - Staged rollouts.
  - Fast rollback.

---

## Ritual Templates

- Retro prompts:
  - What to start/stop/continue?
  - What surprised us?
  - What’s still confusing?
- Incident template:
  - Summary.
  - Impact.
  - Timeline.
  - Root Cause.
  - Actions.
  - Owners.
  - Due dates.
- PR checklist:
  - Tests updated.
  - Screenshots/video for UI.
  - Perf impact considered.
  - Security review if touching auth/payments.

---

## Anti-Patterns to Avoid

- Big-bang rewrites without guardrails.
- Hero culture; knowledge silos.
- Process cargo-culting without measuring outcomes.

---

## 30/60/90 Plan for a New Lead

A **30/60/90 plan** outlines what you aim to accomplish in your **first 30, 60, and 90 days** in a new role:
- 30 days → learn and listen.
- 60 days → start changing systems.
- 90 days → drive measurable improvements.

- **30 days: Listen, map systems, define quality bars, stabilize delivery**
  - Understand team mission, architecture, and current pain points.
  - Build trust: 1:1s, join ceremonies, ask questions.
  - Observe existing processes and quality bars; avoid big changes yet.

- **60 days: Remove top 2 bottlenecks; standardize reviews; introduce metrics**
  - Identify and address 1–2 clear bottlenecks (e.g., flaky tests, slow code reviews, unclear ownership).
  - Introduce or refine code review guidelines and SLAs.
  - Start tracking a few key metrics (lead time, defect rate, build time) to establish a baseline.

- **90 days: Align roadmap, invest in people growth, reduce incident rate**
  - Work with PM/Design to align a realistic engineering roadmap with team capacity.
  - Create or refine individual growth plans for engineers.
  - Implement 1–2 systemic improvements to reduce incidents (better monitoring, on-call processes, incident playbooks).

---

## Android Engineer Notes

- Keep standards pragmatic: Kotlin style guide, Compose best practices, and API review checklist tailored to your codebase.
- Codify review expectations with a lightweight PR template and a small "Definition of Done" that includes tests and screenshots for UI changes.
- Track a few engineering health metrics in CI (lint warnings, unit test count/coverage trend, build time) and review weekly.
- Start ADRs for significant tech decisions (e.g., choose RN vs Flutter vs KMM for a feature); 1–2 pages max.
- Run monthly brown bags to share findings from cross-platform experiments and reduce knowledge silos.
