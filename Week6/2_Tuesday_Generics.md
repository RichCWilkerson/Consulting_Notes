# Week 6 — Kotlin Generics, Variance, and Practical Patterns for a Multi‑Module Android App

This note gives you: a quick mental model of generics, variance (`in`/`out`), how `it` works in lambdas, and a minimal but practical way to use generics with Retrofit, repositories, and MVI across modules.

---

## 1) Generics in 5 minutes: a mental model

- Why: reuse logic for multiple types while keeping type safety.
- What: parameterize types and functions with type parameters like `T`, `R`.
- Where: collections (`List<T>`), repositories (`Repository<T>`), network envelopes (`ApiResult<T>`), mappers (`Mapper<From, To>`), UI state (`UiState<T>`).

Examples:
- Generic class
  
  class Box<T>(val value: T)
  
- Generic function
  
  fun <T> firstOrNull(list: List<T>): T? = list.firstOrNull()
  
- Type constraints
  
  fun <T : Any> toNonNullString(value: T?): String = value?.toString() ?: "<null>"
  
- Reified type (inline) for reflection-like ops 
  - TODO: what is a reflection-like op?
  
  inline fun <reified T> isType(value: Any): Boolean = value is T

Keep it simple: start concrete, then generalize when you have duplication.

---

## 2) Variance: invariance, covariance, contravariance

- Invariance (default): `MutableList<Dog>` is NOT a `MutableList<Animal>`. Why: could add a `Cat` to a dog list.
- Covariance (`out`): producer-only. `List<Dog>` IS a `List<Animal>` because you only read (produce) `Animal`s. Mark type parameter with `out`.
- Contravariance (`in`): consumer-only. `Comparator<Animal>` can compare `Dog`. Mark type parameter with `in`.

Kotlin keywords:
- `out T` means “you can take `T` out” (produce), never consume `T` as a parameter.
  - return type
- `in T` means “you can put `T` in” (consume), never produce `T` as a return type.
  - parameter type

Examples:
- Covariant interface
  
  interface Source<out T> { fun get(): T }
  
  val animals: Source<Animal> = object : Source<Dog> { override fun get() = Dog() }
  
- Contravariant interface
  
  interface Sink<in T> { fun put(value: T) }
  
  val dogSink: Sink<Dog> = object : Sink<Animal> { override fun put(value: Animal) { /* ... */ } }

Function types are naturally variant:
- `(in) -> (out)`: `Function1<in P, out R>`
  - Params are contravariant, return types are covariant.

PECS (from Java, still applies): “Producer Extends, Consumer Super” ≈ Producer → `out`, Consumer → `in`.

---

## 3) The `it` keyword in lambdas

- `it` is the implicit name of a single parameter lambda.
- If the lambda has exactly one parameter and you don’t name it, Kotlin exposes it as `it`.

Examples and gotchas:
- Simple map
  
  listOf("a", "bb").map { it.length } // it: String
    - it here is the current element of the list.
  
- Scoping functions
  
  val name = user?.let { it.fullName } // let uses it; apply/run use this
  
TODO: how do we know when to use this vs it in scoping functions?
  data.apply { 
      // here, this == data, not it
      id = id.trim()
  }
  
- Nesting: prefer explicit names to avoid shadowing
  
  items.map { item ->
      item.tags.filter { tag -> tag.startsWith("A") }
  }
  
- Trailing lambda with receivers
  
  buildString {
      // here, this: StringBuilder
      append("Hello")
  }

Rule of thumb: if nested or unclear, name the parameter instead of using `it`.

---

## 4) `in`/`out` on your own generics (where and why)

Use `out` when your type parameter is only returned/produced:

interface ReadOnlyRepository<out T> {
    suspend fun get(id: String): T?
}

Use `in` when your type parameter is only consumed:

interface Saver<in T> {
    suspend fun save(value: T)
}

Mixed read/write typically must be invariant (no `in`/`out`), e.g., `MutableList<T>`.

---

## 5) Building blocks you’ll actually use

### 5.1 Generic result/state wrappers

- Wrap any payload or error in a generic container usable across layers.

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>()
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

Note: both are covariant (`out T`) so you can upcast `UiState<List<Cocktail>>` to `UiState<Collection<DomainItem>>` if needed.

### 5.2 Generic mappers

- Centralize transformations (DTO → Domain, Domain → UI) and reuse for lists.

interface Mapper<in From, out To> {
    fun map(from: From): To
}

fun <From, To> Mapper<From, To>.mapList(list: List<From>): List<To> = list.map(::map)

Example:

// DTO (data layer)
data class CocktailDto(
    val idDrink: String?,
    val strDrink: String?,
    val strAlcoholic: String?,
    val strDrinkThumb: String?,
    val strCategory: String?,
    val strInstructions: String?,
    val strGlass: String?
)

// Domain (domain layer)
data class Cocktail(
    val id: String,
    val name: String,
    val alcoholic: Boolean,
    val image: String?,
    val category: String?,
    val instructions: String?,
    val glass: String?
)

class CocktailDtoToDomain : Mapper<CocktailDto, Cocktail> {
    override fun map(from: CocktailDto) = Cocktail(
        id = from.idDrink.orEmpty(),
        name = from.strDrink.orEmpty(),
        alcoholic = from.strAlcoholic.equals("Alcoholic", ignoreCase = true),
        image = from.strDrinkThumb,
        category = from.strCategory,
        instructions = from.strInstructions,
        glass = from.strGlass
    )
}

### 5.3 Generic network envelope and call wrapper (Retrofit)

- Many APIs wrap payloads the same way. Express that with a generic envelope.

// Example envelope like CocktailDB
// { "drinks": [ ... ] }
data class Envelope<out T>(val drinks: T?)

// Retrofit service can use Envelope<T>
interface CocktailApiService {
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): Response<Envelope<List<CocktailDto>>>

    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): Response<Envelope<List<CocktailDto>>>
}

// One safeApiCall for all endpoints
suspend inline fun <T> safeApiCall(crossinline call: suspend () -> Response<T>): ApiResult<T> =
    try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null) ApiResult.Success(body)
        else ApiResult.Error("HTTP ${'$'}{response.code()}: ${'$'}{response.message()}")
    } catch (t: Throwable) {
        ApiResult.Error(t.message ?: "Unknown error", t)
    }

// Transform the envelope into domain once, generically
inline fun <Dto, Domain> ApiResult<Envelope<List<Dto>>>.mapEnvelope(
    mapper: Mapper<Dto, Domain>
): ApiResult<List<Domain>> = when (this) {
    is ApiResult.Success -> ApiResult.Success(mapper.mapList(this.data.drinks.orEmpty()))
    is ApiResult.Error -> this
}

This removes duplication: all list endpoints share the same plumbing.

---

## 6) Putting it into your modules cleanly

Recommended modules (as discussed in your project):
- core:domain — pure Kotlin
  - Domain models (e.g., `Cocktail`)
  - Repository interfaces (e.g., `CocktailRepository` returning `ApiResult<List<Cocktail>>`)
  - Result types (`ApiResult`, optional `UiState` if you prefer keep it in UI)
- core:data — Android/Retrofit/OkHttp
  - DTOs (`CocktailDto`), Retrofit services, `Envelope<T>`
  - Mappers DTO → Domain (implement `Mapper<Dto, Domain>`)
  - Repository implementations (use `safeApiCall` + `mapEnvelope`)
  - Depends on `core:domain`
- core:ui — Compose-only
  - Reusable components (e.g., `CocktailCard`)
  - Option A: depend on domain model directly (`Cocktail`)
  - Option B: define `CocktailUi` and map domain → UI in features
- feature:firstlettercocktail, feature:searchcocktail
  - ViewModel (MVI), use cases (optionally), screens
  - Depend on `core:domain` (+ `core:ui`), not on `core:data`
- app
  - DI wiring (bind `CocktailRepository` to `CocktailRepositoryImpl`), navigation

Flow vs dependencies:
- Runtime MVI flow: UI → VM/UseCase → Domain Repo → Data → back as `ApiResult<List<Cocktail>>` → reduce to `UiState` → UI
- Compile-time deps: `core:data → core:domain`; `feature → core:domain (+ core:ui)`; `core:ui → core:domain` (if using domain model); `app` depends on everything to wire DI.

---

## 7) Minimal repository using the generic pieces

// Domain
interface CocktailRepository {
    suspend fun searchByName(query: String): ApiResult<List<Cocktail>>
    suspend fun searchByFirstLetter(letter: String): ApiResult<List<Cocktail>>
}

// Data
class CocktailRepositoryImpl(
    private val api: CocktailApiService,
    private val mapper: Mapper<CocktailDto, Cocktail> = CocktailDtoToDomain()
) : CocktailRepository {
    override suspend fun searchByName(query: String): ApiResult<List<Cocktail>> =
        safeApiCall { api.searchByName(query) }.mapEnvelope(mapper)

    override suspend fun searchByFirstLetter(letter: String): ApiResult<List<Cocktail>> =
        safeApiCall { api.searchByFirstLetter(letter) }.mapEnvelope(mapper)
}

Notes:
- No `Dispatchers.IO` needed around Retrofit suspend calls (OkHttp handles it). Use it if you perform heavy CPU work.
- Keep DTOs and Retrofit in `core:data`; never leak DTOs to UI/feature.

---

## 8) MVI glue with a generic `UiState<T>`

// Feature VM state (domain model or UI model)
data class CocktailListState(
    val query: String = "",
    val state: UiState<List<Cocktail>> = UiState.Idle
)

// Reducer example
private fun reduceLoading() = _state.update { it.copy(state = UiState.Loading) }

private fun reduceResult(result: ApiResult<List<Cocktail>>) = _state.update {
    when (result) {
        is ApiResult.Success -> it.copy(state = UiState.Success(result.data))
        is ApiResult.Error -> it.copy(state = UiState.Error(result.message))
    }
}

Why generics help here:
- `UiState<T>` works for any screen; no duplication.
- `ApiResult<T>` works for any repository call; no duplication.
- `Mapper<From, To>` composes transformations with minimal boilerplate.

---

## 9) Quick checklist for your cocktail app

- Use `core:data` models as DTOs (Gson `@SerializedName`) — they are already DTOs.
- Map DTO → Domain in `core:data` via `Mapper<CocktailDto, Cocktail>`.
- Repository returns `ApiResult<List<Cocktail>>` (domain type), not DTOs.
- `core:ui` either:
  - depends on `core:domain` and takes `Cocktail`, or
  - defines `CocktailUi` and features map `Cocktail → CocktailUi`.
- Use generic `Envelope<T>`, `safeApiCall`, `UiState<T>`, `Mapper<in, out>` to remove duplication.
- Remember variance: producer types use `out`, consumer types use `in`.

---

## 10) Extra tips and pitfalls

- Don’t over-generalize. Extract generics after the second repetition.
- Avoid variance on types that both read and write (will force invariance).
- Prefer explicit names instead of nested `it` to keep code readable.
- Keep domain free of Android/Retrofit/Gson; keep data free to depend on domain.
- Tests: unit-test mappers and the `safeApiCall` branch logic with fake `Response<T>`.

You now have a small, reusable toolkit to keep layers clean and remove boilerplate with a few well-chosen generics.

---

## 11) One‑pager: runtime flow vs. compile‑time dependencies

ASCII map to settle the "flow vs dependency" confusion:

Runtime flow (MVI, at run time):

UI → ViewModel/UseCase → Domain Repository (interface) → Data (impl, Retrofit/DB) → Result
   → Reduce to UiState → UI

Compile‑time dependencies (Gradle module arrows, at build time):

core:data  →  core:domain
core:ui    →  core:domain
feature:*  →  core:domain (+ core:ui)
app        →  feature:*, core:domain, core:ui, core:data (for DI wiring only)
core:domain →  (no deps)

Your module names (valid Gradle names):
- feature:firstlettercocktail
- feature:searchcocktail
- core:data (Retrofit, DTOs, mappers, repo impls) → depends on core:domain
- core:domain (models, repo interfaces, ApiResult)
- core:ui (Compose components; optionally depends on core:domain if using domain models in UI)

Tiny DI wiring sketch (pseudo‑Koin/Hilt‑like):

// Provide repo impl once (app module)
val dataModule = module {
    single<CocktailApiService> { retrofit.create(CocktailApiService::class.java) }
    single<Mapper<CocktailDto, Cocktail>> { CocktailDtoToDomain() }
    single<CocktailRepository> { CocktailRepositoryImpl(get(), get()) }
}

// Feature consumes only the interface
class SearchViewModel(
    private val repo: CocktailRepository
) : ViewModel() { /* ... */ }
