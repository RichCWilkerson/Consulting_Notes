# Testing — Detailed Breakdown

Goal: Fast feedback, high confidence, and maintainable tests.

Layers
- Unit tests
- Integration tests
- UI/E2E tests

---

## Android: JUnit, Mockito/MockK
- Use JUnit4/5; prefer MockK for Kotlin (clear/verify semantics)
  - TODO: benefit of MockK over Mockito with Kotlin?
- Structure: Given-When-Then naming; single assertion principle when possible
  - fun `givenValidInput_whenCalculatingSum_thenReturnsCorrectResult`()
  - only test one logical behavior per test 
- Coroutines: use runTest/TestScope; Turbine for Flow testing
  - TODO: what is runTest/TestScope?
  - TODO: what is Turbine? another library?
- Time: TestCoroutineScheduler for virtual time
  - TODO: what is TestCoroutineScheduler?

## Integration Testing
- Android: Robolectric for headless component tests
  - TODO: what is a headless component test?
- Database: Room/SQLDelight with in-memory drivers; test migrations
  - TODO: what are in-memory drivers?
- Network: MockWebServer; contract tests for APIs; WireMock for complex flows
- DI: provide test modules/fakes; use Hilt test rules if using Hilt
  - TODO: what are Hilt test rules?

## UI Testing
- Espresso for View/Compose UI; use IdlingResource or compose test rules
  - TODO: what is IdlingResource?
  - TODO: what are compose test rules?
- Snapshot tests for Compose (Paparazzi/Shot) when appropriate
  - TODO: what are snapshot tests?
- Accessibility checks in tests (content descriptions, touch targets)
  - TODO: what are accessibility checks?

## Cross-Platform
- Flutter: flutter test, golden tests; integration_test package
- React Native: Jest + React Testing Library; Detox for E2E
- KMM: Common tests for shared code; platform tests for UI layers

## Test Data and Reliability
- Builders/fixtures; mother objects; avoid brittle shared fixtures
- Randomized testing with seeds; property-based tests for critical logic
- Flake reduction: await idle, retry with limits, stabilize network/time

## CI and Coverage
- Run unit tests on every PR; UI tests on merge/nightly
  - TODO: are UI tests long and that's why they run on merge/nightly?
- Fail builds on high-severity lint; track coverage trends (avoid chasing 100%)
  - TODO: what is high-severity lint?
  - TODO: what is a good coverage percentage to aim for?
- Generate artifacts: videos/screens for failed UI tests; HTML reports
  - TODO: are we saying to record videos of replicating failed UI tests?

## Minimal Checklists
- Each bug gets a regression test
  - write a test that fails before the fix and passes after
- Public API changes require tests and docs updates
- Performance-sensitive logic gets microbenchmarks

---

## Android Engineer Notes
- Compose UI tests: use semantics and test tags; avoid relying on implementation details; snapshot tricky visuals with Paparazzi.
  - TODO: what are semantics and test tags?
  - TODO: what does relying on implementation details mean?
  - TODO: what are tricky visuals?
- Use Turbine for Flow testing and runTest for coroutines; control time with TestCoroutineScheduler.
- Layered test strategy: unit tests for use cases/reducers, Robolectric for viewmodels and lightweight components, Espresso/Compose for UI flows.
  - TODO: what are reducers?
- CI hygiene: shard UI tests, collect videos/screenshots on failure, and quarantine flaky tests with auto-retry + alerts.
- Keep tests fast: favor deterministic fakes over mocks; use MockWebServer and in-memory DB for integration tests.
  - TODO: what are deterministic fakes? why are they better than mocks?
  - TODO: what is an in-memory DB in this context? seed data?