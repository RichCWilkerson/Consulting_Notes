# JUnit4 Unit Testing Guide (Android, JVM-only)

We’ll keep tests JVM-only (src/test) using JUnit4, Mockito, Truth, Robolectric (only when needed for Room), and Coroutines Test.

---

## Table of Contents
1. Goals & Scope
2. Quick Setup (Gradle deps + source sets)
3. Test Anatomy (AAA) and Assertions
4. Mockito Basics (Kotlin-friendly)
5. Coroutines & Flow Testing
6. Network Client Testing (Retrofit) with MockWebServer
7. Repository Testing (combining DAO + API)
8. Room Testing Options (Mock vs In-memory with Robolectric)
9. ViewModel + Use Case Testing
10. Best Practices & Checklist
11. How to Run Tests
12. Minimal Examples (copy-paste starters)

---

## 2) Quick Setup

Add these to `app/build.gradle.kts` (versions are examples; align to your BOM/versions file):

```kotlin
dependencies {
    // JUnit 4
    testImplementation("junit:junit:4.13.2")

    // Truth assertions
    testImplementation("com.google.truth:truth:1.4.4")

    // Mockito (core + kotlin + inline to mock finals)
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Coroutines test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Flow testing helper (optional but great)
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // Robolectric (only if you need Android SDK in unit tests, e.g., Room)
    testImplementation("org.robolectric:robolectric:4.13")

    // Room testing helper (optional but helpful for migrations)
    testImplementation("androidx.room:room-testing:2.6.1")
}
```

Source sets you’ll use:
- JVM unit tests: `app/src/test/java/...`
- Instrumented tests (not used for this guide): `app/src/androidTest/java/...`

---

## 3) Test Anatomy (AAA) and Assertions

- AAA pattern:
  - Arrange: set up subject under test (SUT) and test doubles (mocks/fakes).
  - Act: call the function under test.
  - Assert: verify results and interactions.

- Use Google Truth:
  - used for assertions:
```kotlin
//import com.google.common.truth.Truth.assertThat
assertThat(result).isEqualTo(expected)
assertThat(list).containsExactlyElementsIn(expectedList).inOrder()
assertThat(exception).hasMessageThat().contains("boom")
```

- JUnit4 lifecycle annotations:
  - `@Before` runs before each test.
  - `@After` runs after each test.
  - `@BeforeClass`/`@AfterClass` run once per class (must be `@JvmStatic` inside companion).
  - `@Rule` for rules (e.g., `InstantTaskExecutorRule` if you use LiveData in JVM tests).
  - `@RunWith(MockitoJUnitRunner::class)` optional for Mockito; or prefer `MockitoRule`.

---

## 4) Mockito Basics (Kotlin-friendly)

- Create mocks:
```kotlin
//import org.mockito.kotlin.mock
val api: PokemonApi = mock()
```

- Stubbing and verification:
```kotlin
//import org.mockito.kotlin.whenever
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.times

whenever(api.getPokemon("pikachu")).thenReturn(Pokemon("pikachu"))
val result = api.getPokemon("pikachu")
verify(api, times(1)).getPokemon("pikachu")
```

- For suspend functions use `whenever(runBlocking { ... })` or better: use Coroutines Test (`runTest`) and stub suspend directly:
```kotlin
runTest {
    whenever(api.getPokemon("pikachu")).thenReturn(Pokemon("pikachu"))
}
```

Tip: Prefer constructor injection in your production code so you can pass mocks/fakes easily.

---

## 5) Coroutines & Flow Testing

- Use `kotlinx-coroutines-test`:
```kotlin
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.UnconfinedTestDispatcher

@RunWith(MockitoJUnitRunner::class)
class ExampleTest {
    @Test
    fun example() = runTest {
        // by default, runTest provides a TestScope + TestDispatcher
    }
}
```

- Testing Flow with Turbine:
```kotlin
//import app.cash.turbine.test

@Test
fun stateFlow_emits_expected_states() = runTest {
    viewModel.state.test {
        assertThat(awaitItem()).isEqualTo(State.Loading)
        assertThat(awaitItem()).isEqualTo(State.Data(listOf("a")))
        cancelAndIgnoreRemainingEvents()
    }
}
```

- If your ViewModel launches on `viewModelScope`, prefer injecting a `CoroutineDispatcher` and using it in your code (e.g., `withContext(io)`), then provide a TestDispatcher in tests.

---

## 6) Network Client Testing (Retrofit) with MockWebServer

Goal: Verify your Retrofit service parses responses and hits correct paths.

```kotlin
// Production: Retrofit API
interface PokemonApi {
    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): Pokemon
}

data class Pokemon(val name: String)
```

```kotlin
// Test using MockWebServer
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import com.google.common.truth.Truth.assertThat
//import kotlinx.coroutines.test.runTest

class PokemonApiTest {
    private val server = MockWebServer()

    private lateinit var api: PokemonApi

    @Before fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokemonApi::class.java)
    }

    @After fun tearDown() {
        server.shutdown()
    }

    @Test fun getPokemon_parsesBodyAndUsesPath() = runTest {
        val body = """{"name":"pikachu"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val result = api.getPokemon("pikachu")

        assertThat(result.name).isEqualTo("pikachu")
        val recorded = server.takeRequest()
        assertThat(recorded.path).isEqualTo("/pokemon/pikachu")
        assertThat(recorded.method).isEqualTo("GET")
    }
}
```

---

## 7) Repository Testing (combining DAO + API)

Approach:
- Mock both API and DAO. Test repository logic (merging, caching, errors).
- Verify interactions order/conditions.

```kotlin
class PokemonRepository(
    private val api: PokemonApi,
    private val dao: PokemonDao
) {
    suspend fun fetch(name: String): Pokemon {
        val local = dao.get(name)
        if (local != null) return local
        val remote = api.getPokemon(name)
        dao.insert(remote)
        return remote
    }
}
```

```kotlin
@RunWith(MockitoJUnitRunner::class)
class PokemonRepositoryTest {
    private val api: PokemonApi = mock()
    private val dao: PokemonDao = mock()
    private lateinit var repo: PokemonRepository

    @Before fun setUp() {
        repo = PokemonRepository(api, dao)
    }

    @Test fun fetch_usesLocalWhenAvailable() = runTest {
        val local = Pokemon("pikachu")
        whenever(dao.get("pikachu")).thenReturn(local)

        val result = repo.fetch("pikachu")

        assertThat(result).isEqualTo(local)
        verify(api, times(0)).getPokemon(any())
    }

    @Test fun fetch_callsApiAndCachesWhenMissing() = runTest {
        whenever(dao.get("eevee")).thenReturn(null)
        val remote = Pokemon("eevee")
        whenever(api.getPokemon("eevee")).thenReturn(remote)

        val result = repo.fetch("eevee")

        assertThat(result).isEqualTo(remote)
        verify(dao).insert(remote)
    }
}
```

---

## 8) Room Testing Options (Mock vs In-memory with Robolectric)

You have two unit-test-friendly options:

A) Purely mock the DAO interfaces (fastest, isolates repository logic). Recommended for most repository tests.

B) Use an in-memory Room DB with Robolectric (verifies queries, relations, migrations):
```kotlin
//import androidx.room.Room
//import androidx.test.core.app.ApplicationProvider
//import org.robolectric.annotation.Config

@Config(sdk = [34])
class PokemonDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PokemonDao

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // only in tests
            .build()
        dao = db.pokemonDao()
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun insertAndGet_roundTrip() {
        val p = PokemonEntity(name = "mew")
        dao.insert(p)
        val loaded = dao.get("mew")
        assertThat(loaded?.name).isEqualTo("mew")
    }
}
```

Notes:
- Put these tests under `src/test` (not `androidTest`) because Robolectric lets them run on the JVM.
- If you only need to test repository logic, prefer mocked DAO.

---

## 9) ViewModel + Use Case Testing

Strategy:
- Inject a TestDispatcher into ViewModel/use-cases.
- Mock repository/use-case dependencies.
- Observe StateFlow with Turbine (or LiveData with `InstantTaskExecutorRule`).

```kotlin
class PokemonViewModel(
    private val repo: PokemonRepository,
    private val io: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state

    fun load(name: String) {
        viewModelScope.launch(io) {
            _state.value = State.Loading
            runCatching { repo.fetch(name) }
                .onSuccess { _state.value = State.Data(it) }
                .onFailure { _state.value = State.Error(it) }
        }
    }
}

sealed class State {
    data object Idle : State()
    data object Loading : State()
    data class Data(val pokemon: Pokemon) : State()
    data class Error(val error: Throwable) : State()
}
```

```kotlin
@RunWith(MockitoJUnitRunner::class)
class PokemonViewModelTest {
    private val repo: PokemonRepository = mock()

    @Test
    fun load_emitsLoadingThenData() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        whenever(repo.fetch("pikachu")).thenReturn(Pokemon("pikachu"))

        val vm = PokemonViewModel(repo, dispatcher)

        vm.state.test {
            vm.load("pikachu")
            assertThat(awaitItem()).isEqualTo(State.Idle)     // initial
            assertThat(awaitItem()).isEqualTo(State.Loading)
            // advance until coroutines complete
            advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(State.Data(Pokemon("pikachu")))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

For LiveData-based ViewModels:
```kotlin
@get:Rule val instant = InstantTaskExecutorRule()
```

---

## 10) Best Practices & Checklist

- Keep unit tests small, fast, and isolated. Prefer constructor injection to pass fakes/mocks.
- Write tests at the seam: mock boundaries (network/DB) to test your logic, use in-memory Room only when you need to verify SQL.
- Use `runTest` for coroutines; avoid `runBlocking` in tests unless necessary.
- One assertion group per behavior; name tests descriptively (backticks in Kotlin are great).
- Avoid Robolectric unless you need Android SDK behavior (e.g., Room, Resources).
- Use `mockito-inline` to mock final classes if required.
- Prefer fakes over mocks for complex behaviors (simpler maintenance, clearer intent).
- Don’t use Hilt instrumentation features for JVM unit tests; rely on constructor injection and test doubles instead.

---

## 11) How to Run Tests

From the terminal at project root:
```bash
./gradlew test
```
Or from Android Studio: right-click test class/method → Run.

---

## 12) Minimal Examples (copy-paste starters)

Basic JUnit4 + Truth:
```kotlin
class MathUtilsTest {
    @Test fun `sum adds numbers`() {
        val result = 2 + 3
        com.google.common.truth.Truth.assertThat(result).isEqualTo(5)
    }
}
```

Mockito + Coroutines:
```kotlin
@RunWith(MockitoJUnitRunner::class)
class GreetingUseCaseTest {
    private val repo: GreetingRepo = mock()

    @Test fun `returns greeting`() = kotlinx.coroutines.test.runTest {
        whenever(repo.getName()).thenReturn("World")
        val useCase = GreetingUseCase(repo)
        val result = useCase()
        com.google.common.truth.Truth.assertThat(result).isEqualTo("Hello, World")
    }
}
```

Flow + Turbine:
```kotlin
@Test fun `emits values then completes`() = kotlinx.coroutines.test.runTest {
    flowOf(1, 2, 3).test {
        com.google.common.truth.Truth.assertThat(awaitItem()).isEqualTo(1)
        com.google.common.truth.Truth.assertThat(awaitItem()).isEqualTo(2)
        com.google.common.truth.Truth.assertThat(awaitItem()).isEqualTo(3)
        awaitComplete()
    }
}
```

---

Notes you had before (updated):
- Use JUnit4, Truth, Mockito, Turbine; Robolectric only when Android SDK behavior is needed (e.g., Room tests on JVM).
- Hilt: for JVM unit tests, prefer constructor injection and manual wiring in tests. Reserve `@HiltAndroidTest` for instrumented tests.
- Naming tests: `methodName_state_expectedBehavior` or backticked natural language.
- Cleanup in `@After`: close DBs, stop MockWebServer, etc.
