# Resources:
[KMP Testing](https://www.youtube.com/watch?v=tAMu-RPqkok&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=6)


# Testing
## Overview
- Testing is about **proving behavior**, not just increasing coverage numbers.
- A healthy Android/KMM testing strategy usually looks like a **pyramid**:
  - Many **fast unit tests** (domain, repositories, ViewModels).
  - Fewer **integration tests** (Android components, DB + network wiring).
  - A small number of **UI / end-to-end tests** for critical flows.
- Goal: catch regressions early, enable safe refactors, and keep release confidence high.

---

## Why Testing is Important
1. Ensures code correctness and reliability.
2. Helps catch bugs early in the development process.
3. Facilitates refactoring and code maintenance.
4. Improves code quality and design.
5. Provides documentation for expected behavior (tests are living examples).
6. Enables safer CI/CD and faster release cycles.

---

## Best Practices
- **Test behavior, not implementation details**
  - Assert on outputs and visible side effects, not private fields or exact method calls whenever possible.
- **Keep unit tests fast and deterministic**
  - No network, no real disk I/O, no sleeps.
  - Mock or fake slow dependencies.
- **Arrange–Act–Assert pattern**
  - Arrange (setup data and mocks), Act (call the function), Assert (verify result/side effects).
- **Use dependency injection**
  - Makes it easy to swap in mocks/fakes in tests (Hilt/Dagger, constructor injection).
- **One logical assertion per test**
  - A test can have multiple `assert` calls, but it should verify a single behavior.
- **Name tests clearly**
  - `fun login_withInvalidPassword_emitsErrorState()`.
- **Focus tests where they matter most**
  - Domain/business logic, error handling, state machines, and complex UI logic.

---

## Writing Code for Testability
TODO: what are some strategies to write code that is easy to test?

---

## Types of Testing
1. **Unit Testing** 
   - JUnit, Mockito, MockK, Turbine, kotlinx-coroutines-test.
2. **Integration Testing**
   - Robolectric, instrumented tests hitting real DB or network (test servers).
3. **UI Testing**
   - Espresso, Compose Testing, UI Automator.
4. **End-to-End Testing**
   - UI Automator, Detox (for RN), full app flows hitting real backend or staging.

---

## Unit Testing

Unit tests focus on **small pieces of logic in isolation**, with their dependencies mocked or faked.

### Core libraries
1. **JUnit**
   - Base framework for writing and running tests in Java/Kotlin.
   - `@Test`, `@Before`, `@After`, assertions like `assertEquals`, `assertTrue`.
2. **Mockito**
   - Mocking framework for Java/Kotlin.
   - Create mocks of interfaces/classes to control behavior and verify interactions.
3. **MockK**
   - Kotlin-first mocking framework.
   - Supports mocking final classes, extension functions, coroutines, etc.
4. **kotlinx-coroutines-test**
   - Official library for testing coroutines (`runTest`, `StandardTestDispatcher`, virtual time).
5. **Turbine**
   - Library for testing Kotlin `Flow` emission sequences in a concise way.

### Example: ViewModel unit test with MockK and coroutines
```kotlin
class LoginViewModelTest {

    private val userRepository: UserRepository = mockk()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = LoginViewModel(userRepository, dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials emits Success state`() = runTest {
        coEvery { userRepository.login("user@example.com", "password") } returns User(id = "123")

        viewModel.login("user@example.com", "password")

        // Advance coroutines
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Success(User("123")), viewModel.uiState.value)

        coVerify { userRepository.login("user@example.com", "password") }
    }
}
```

### Example: Repository unit test with kotlinx-coroutines-test
```kotlin
class BillsRepositoryImplTest {

    private val apiDataSource: BillsDataSource = mockk()
    private val cacheDataSource: BillsDataSource = mockk()
    private val billsCache: BillsCache = mockk(relaxed = true)

    private lateinit var repository: BillsRepositoryImpl

    @Before
    fun setup() {
        repository = BillsRepositoryImpl(apiDataSource, cacheDataSource, billsCache)
    }

    @Test
    fun `returns cached bills when cache is not empty`() = runTest {
        val cached = listOf(Bill(id = "1"))
        coEvery { cacheDataSource.getUpcomingBills() } returns cached

        val result = repository.getUpcomingBills()

        assertEquals(cached, result)
        // verify API is not called when cache has data
        coVerify(exactly = 0) { apiDataSource.getUpcomingBills() }
    }

    @Test
    fun `fetches from api and saves to cache when cache is empty`() = runTest {
        val apiBills = listOf(Bill(id = "2"))
        coEvery { cacheDataSource.getUpcomingBills() } returns emptyList()
        coEvery { apiDataSource.getUpcomingBills() } returns apiBills

        val result = repository.getUpcomingBills()

        assertEquals(apiBills, result)
        coVerify { apiDataSource.getUpcomingBills() }
        coVerify { billsCache.saveBills(apiBills) }
    }
}
```

### Example: Testing a Flow with Turbine
```kotlin
@Test
fun `repository emits Loading then Success`() = runTest {
    val repo = BillsRepositoryImpl(fakeApi, fakeCache)

    repo.getUpcomingBillsFlow().test {
        assertEquals(BillsUiState.Loading, awaitItem())
        val success = awaitItem()
        assertTrue(success is BillsUiState.Success)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Integration Testing

Integration tests verify how **multiple components work together** (e.g., ViewModel + Repository + DB, or an Activity with real resources).

1. **Robolectric**
   - Runs Android component tests on the JVM.
   - Can instantiate Activities, use resources, and run some framework code without an emulator.

### Example: Robolectric test
```kotlin
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Test
    fun `clicking button shows toast`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java)
            .create()
            .start()
            .resume()
            .get()

        val button = activity.findViewById<Button>(R.id.myButton)
        button.performClick()

        // Assert on Toast text or ViewModel state depending on design
    }
}
```

Integration tests can also be **instrumented** (running on device/emulator) when Robolectric is not enough—e.g., when testing Room with a real DB file or WorkManager.

---

## UI Testing

UI tests verify **screens and interactions** from the user’s perspective.

1. **Espresso**
   - For XML-based or View-based UIs.
   - Interact with views by ID/text and assert on their state.
2. **Compose Testing**
   - For Jetpack Compose UIs.
   - Uses semantics and test tags to find elements.
3. **UI Automator**
   - For interactions across apps / system UI (e.g., notifications, permissions dialogs).

### Example: Espresso test
```kotlin
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Test
    fun login_withValidCredentials_navigatesToHome() {
        ActivityScenario.launch(LoginActivity::class.java)

        onView(withId(R.id.emailEditText)).perform(typeText("user@example.com"))
        onView(withId(R.id.passwordEditText)).perform(typeText("password"))
        onView(withId(R.id.loginButton)).perform(click())

        onView(withId(R.id.homeRootView)).check(matches(isDisplayed()))
    }
}
```

### Example: Compose UI test
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun addItem_updatesList() {
    composeTestRule.setContent {
        MyScreen()
    }

    composeTestRule.onNodeWithTag("AddButton").performClick()
    composeTestRule.onNodeWithText("New Item").assertIsDisplayed()
}
```

---

## End-to-End Testing

End-to-end (E2E) tests exercise **full user flows** against a real or staging backend.

1. **UI Automator**
   - Drive the app and system UI end to end.
2. **Detox** (React Native)
   - E2E testing for React Native apps.

These tests are slower and more brittle, so keep them to **critical happy paths**: login, basic navigation, checkout.

---

## KMM (Kotlin Multiplatform) Testing

KMM lets you share code, but you still need good testing across targets.

### Shared tests
- You can write tests once in the `commonTest` source set and run them on:
  - JVM (Android host).
  - iOS simulator.
- Typical targets: business logic, validation, domain models, shared networking/parsing.

```kotlin
// commonTest
class AuthValidatorTest {

    @Test
    fun `invalid email returns error`() {
        val result = AuthValidator.validateEmail("not-an-email")
        assertTrue(result is ValidationResult.Invalid)
    }
}
```

### Platform-specific KMM tests
- For `expect/actual` implementations, test **actual** implementations per platform:
  - Android: JUnit/Robolectric/instrumented.
  - iOS: XCTest.

### Test strategy
- Shared module tests:
  - Validation, business rules, error mapping.
- Android-specific tests:
  - Integration with Android components (`ViewModel`, DI wiring, storage).
- iOS-specific tests:
  - Interop layers and platform-specific logic.

---

## Common Pitfalls

- **Flaky tests**
  - Tests depending on timers, network, or real services.
  - Fix by using virtual time (`runTest`), fakes, and stable test data.
- **Over-mocking**
  - Tests that mirror implementation instead of behavior.
  - Favor fakes and domain-focused assertions.
- **Slow UI/E2E tests**
  - Huge suites that run on every PR.
  - Keep a small **smoke suite** for PRs and run the full suite on schedule.
- **Ignoring Kotlin coroutines/Flow specifics**
  - Not using `kotlinx-coroutines-test`, leading to flaky timing issues.
- **Not testing error paths**
  - Many tests cover only the happy path.
  - Always test network failures, empty results, and edge cases.

---

## Common Interview Questions

1. **How do you decide what to unit test vs what to UI test?**

> Answer: Put most logic (validation, mapping, state machines) in unit tests. Use UI tests for wiring and critical flows only.

2. **How do you test coroutines and Flow on Android?**

> Answer: Use `kotlinx-coroutines-test` for suspending functions and dispatchers; use Turbine or manual collection for Flow emissions.

3. **How do you test a ViewModel in an MVVM architecture?**

> Answer: Inject fakes/mocks for repositories, trigger ViewModel actions, assert on exposed state (LiveData/StateFlow).

4. **What’s the difference between unit, integration, and UI tests?**

> Answer: Scope and dependencies: single unit vs multiple components vs full UI/user flow.

5. **How do you avoid flaky UI tests?**

> Answer: Use idling resources/Compose test rules, avoid sleeps, stabilize test data, and keep E2E tests minimal and focused.

6. **In a KMM project, how do you test shared logic?**

> Answer: Write tests in `commonTest` that run on multiple targets, and add platform-specific tests where needed.

7. How would you test Android app features end-to-end to ensure UI behaves correctly during user interactions, including orientation changes and back navigation? 

8. How do you test the domain and repository layers in an Android app to ensure high code coverage and proper handling of Kotlin coroutines or Flow?
