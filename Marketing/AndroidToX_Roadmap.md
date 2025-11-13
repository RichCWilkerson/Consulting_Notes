# Android-to-X Roadmap — 6 Weeks

Audience: Kotlin Android engineer expanding into Flutter, React Native, and KMM while strengthening leadership, performance, security, data, and testing.

Principles
- Timebox daily: 60–90 min focused learning, 30–60 min practice
- Ship small working slices weekly; journal learnings
- Profile, test, and write a short ADR for each significant decision

Week 1 — Crosswalk + Flutter Foundations
- Read Android Developer Crosswalk
- Build a small Flutter app: list + detail + network + local cache
- Pick state mgmt (Riverpod or BLoC); set up theme + navigation
- Write 5–8 widget/unit tests; profile scroll and cold start

Week 2 — React Native Foundations
- Build the same feature set in RN (TypeScript)
- Set up React Navigation + React Query; choose state mgmt (Zustand/Redux)
- Add Jest tests; one Detox E2E; profile and fix re-renders

Week 3 — KMM Foundations
- Create KMM shared: Ktor + serialization + SQLDelight + DI
- Android UI in Compose, iOS UI in SwiftUI; hook to shared repo/use cases
- Add common tests and platform tests; publish framework to iOS app

Week 4 — Performance & Security
- Add macrobenchmarks to Android app; address jank/memory issues
- Implement TLS pinning; secure token storage (Keystore/Keychain)
- Add biometric auth with proper fallbacks; incident playbook draft

Week 5 — Data Handling & Offline
- Introduce GraphQL or improve REST with ETags/caching
- Add offline queue + conflict resolution; background sync via WorkManager
- Observability: metrics for cache hit/miss, latency; dashboards

Week 6 — Leadership & Testing Scale-Up
- Define team quality bars; PR checklist; small standards doc
- Expand test suite: integration tests, CI workflows, flaky test triage
- Write an ADR comparing Flutter vs RN vs KMM for your context and recommend next steps

Extras (stretch)
- Explore multiplatform analytics wrapper; feature flags SDK comparison
- Investigate SPM distribution for KMM; add Crashlytics symbols for iOS

