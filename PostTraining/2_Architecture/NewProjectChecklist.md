# New Project Checklist

## 1. Product & Scope
- [ ] Define target users and primary use cases
- [ ] List supported platforms (phone / tablet / wearables / Auto / TV)
- [ ] Capture any regulatory or domain constraints (finance, health, geography, age)
- [ ] Agree on success metrics (activation, engagement, retention, revenue, etc.)
- [ ] List **must-have** vs **nice-to-have** features for MVP
- [ ] Write down explicit **non-goals** to prevent scope creep
- [ ] Decide offline expectations (none / read-only / full offline)
- [ ] Set basic performance expectations (startup time, smooth scrolling, perceived latency)
- [ ] Decide accessibility bar (TalkBack, font scaling, contrast, motion reduction)
- [ ] Decide localization strategy (languages, RTL, regional formats)

## 2. Architecture & Technical Choices
- [ ] Choose overall app architecture (layered + unidirectional data flow, MVVM/Clean)
- [ ] Decide state management (ViewModel, state holders, coroutines/Flow)
- [ ] Choose DI solution (Hilt / Dagger / Koin / other)
- [ ] Sketch module structure (core, design-system, feature modules, data, networking, analytics)
- [ ] Choose navigation approach (Jetpack Navigation, Compose Navigation, custom)
- [ ] Choose networking stack (Retrofit/OkHttp, gRPC, GraphQL, WebSockets)
- [ ] Choose persistence strategy (Room, DataStore, SQLDelight, key–value, file)
- [ ] Define high-level API contracts (request/response shapes, pagination, error model)
- [ ] Define auth flows (OIDC / OAuth2 / custom) and token handling
- [ ] Decide behavior on backend failures (retries, offline cache, user-facing errors)
- [ ] Decide feature-flagging approach (remote config, kill switches, staged rollouts)

## 3. Infrastructure, Tooling & Quality
- [ ] Create repo and branch strategy (main, develop, feature branches)
- [ ] Set up CI (build, unit tests, lint, static analysis)
- [ ] Configure basic instrumentation/UI tests in CI (smoke suite)
- [ ] Set up environment configs (dev / QA / stage / prod)
- [ ] Set up signing & keystores for debug / internal / release builds
- [ ] Decide release channels (internal, alpha/beta, production)
- [ ] Choose code style and linters (Ktlint / Spotless / Detekt / IDE settings)
- [ ] Define **Definition of Done** (tests, docs, analytics, monitoring, accessibility)
- [ ] Write a testing strategy (unit, integration, UI, contract/API tests)
- [ ] Choose monitoring stack (Crashlytics/Sentry, analytics, APM, logs, dashboards)

## 4. Security, Privacy & Compliance
- [ ] Do a quick threat model (what data, where it lives, who might attack it)
- [ ] Decide what is stored on device vs on server
- [ ] Choose secure storage (EncryptedSharedPreferences, EncryptedFile, keystore)
- [ ] Decide stance on rooted/jailbroken devices and emulators
- [ ] List likely fraud/abuse vectors for your domain
- [ ] Define secrets management (API keys, certificates, configs)
- [ ] Decide on certificate pinning and network hardening (TLS versions, HSTS)
- [ ] Separate environments (dev vs prod keys and backends)
- [ ] Note data retention and deletion policies
- [ ] Check legal/compliance needs (GDPR/CCPA, consent flows, privacy policy, T&Cs)

## 5. UX, Design & Content
- [ ] Confirm design system / component library (tokens, typography, color, spacing)
- [ ] Decide basic navigation pattern (single-activity, bottom nav, tabs, drawer)
- [ ] Map core user flows (onboarding, sign-in, main tasks, empty/error states)
- [ ] Set guidelines for motion/animation (durations, easing, reduced motion options)
- [ ] Set copy guidelines (tone, terminology, error message style)
- [ ] Plan asset pipeline (icons, images, illustration formats, density buckets)

## 6. Team, Process & Docs
- [ ] Clarify roles and ownership (tech lead, feature owners, QA, PM, design)
- [ ] Agree on ceremonies (standups, planning, retro, demos)
- [ ] Set up backlog structure (epics, stories, tech debt, spikes)
- [ ] Create a project `README` (what it does, how to build/run, main links)
- [ ] Document local dev setup (Android Studio version, SDK, emulators/devices, env vars)
- [ ] Choose where docs live (wiki, Notion, repo `/docs`, etc.)

## 7. Launch & Post-Launch
- [ ] Define initial launch scope (features, markets, platforms)
- [ ] Decide rollout plan (dogfood, beta, staged rollout)
- [ ] Draft store listing basics (name, description, screenshots, privacy labels)
- [ ] Choose key analytics events and funnels for launch
- [ ] Plan support channels (help center, email, in-app feedback)
- [ ] Schedule post-launch review (date, metrics to review, backlog grooming)
