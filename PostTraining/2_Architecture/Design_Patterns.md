# Design Patterns (Android + Kotlin)

## Resources
- Refactoring.Guru (pattern intent, diagrams, tradeoffs): https://refactoring.guru/design-patterns
- *Design Patterns: Elements of Reusable Object-Oriented Software* (GoF)
- *Head First Design Patterns* (great for intuition)
- Android architecture guidance: https://developer.android.com/topic/architecture
- Kotlin coroutines guide: https://kotlinlang.org/docs/coroutines-overview.html

---

## Overview
Design patterns are reusable solutions to recurring design problems. They’re **not** requirements or “best code,” and they can absolutely be misapplied.

A practical Android framing:
- **Patterns are about dependency direction and boundaries.**
- Prefer **composition over inheritance**.
- Prefer patterns that **reduce coupling** and **make tests cheaper**.

### Categories (GoF = Gang of Four)
- **Creational**: how objects are created (e.g., Singleton, Factory, Builder)
- **Structural**: how objects are composed (e.g., Adapter, Decorator, Facade)
- **Behavioral**: how objects communicate (e.g., Observer, Strategy, Command)

---

## How to choose the right pattern (quick guide)
- Need to **delay creation** or swap implementations? → **Factory** / DI
- Need a **complex object with many optional properties**? → **Builder** (or Kotlin DSL)
- Need to **wrap/translate an API** (especially legacy/3rd party)? → **Adapter**
- Need to **add behavior without subclassing**? → **Decorator**
- Need a **single simplified entry point** to a subsystem? → **Facade**
- Need runtime **algorithm selection**? → **Strategy**
- Need **state-dependent behavior** without sprawling `when` branches? → **State**
- Need to propagate **events/updates**? → **Observer** (Flow/LiveData)

---

## Pattern quality bar (what “good” looks like)
### Best practices
- Keep the **intent** obvious in naming: `*Factory`, `*Adapter`, `*Decorator`, `*Facade`.
- Make dependencies explicit (constructor injection) instead of hidden globals.
- Prefer **interfaces** at boundaries and **sealed types** for domain state.
- For Android: keep framework classes (Activity/Fragment) thin; move logic behind testable interfaces.

### Common pitfalls
- Pattern-as-religion: applying patterns where a function would do.
- Over-abstracting too early (too many layers, too many interfaces).
- Hiding side effects (network/db) behind “nice” types without making them testable.
- “Singleton everywhere” → implicit dependencies, hard tests, tricky lifecycle.

---

## Creational patterns

### 1) Singleton
**Intent:** exactly one instance (or one per process), globally accessible.

**Android use cases**
- OkHttpClient / Retrofit instance
- Database instance (Room)

**Best practices**
- Prefer **DI container**-managed singletons over manual global access.
- Ensure thread safety and consider process death.

**Pitfalls**
- Becomes a hidden dependency and makes tests brittle.
- Can accidentally hold references to Context/Views → leaks.

**Example (Kotlin object, safe if stateless):**
```kotlin
object AppClock {
    fun nowMs(): Long = System.currentTimeMillis()
}
```

**Example (Room singleton via DI is preferred)**
If not using DI, at least keep `applicationContext` only.

---

### 2) Factory Method / Simple Factory
**Intent:** create objects without exposing creation logic.

**Android use cases**
- Selecting API implementations (real vs fake)
- Creating ViewModel with runtime arguments (when not using Hilt)

**Example: ViewModel factory (no DI)**
```kotlin
class UserViewModelFactory(
    private val repo: UserRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repo, userId) as T
        }
        error("Unknown ViewModel class: $modelClass")
    }
}
```

**Pitfalls**
- Central factory grows into a “god object.” Split by feature or type.

---

### 3) Abstract Factory
**Intent:** create families of related objects.

**Android use cases**
- Build a set of clients per environment/tenant (Auth + Api + Telemetry)
- Multi-brand apps creating theme/styling strategies

**Notes**
This is often replaced by DI modules (Hilt/Dagger) that supply a “family” of bindings.

---

### 4) Builder
**Intent:** construct a complex object step-by-step.

**Android use cases**
- OkHttp request building
- Complex UI models

**Kotlin note**
Kotlin’s `copy()` and default parameters reduce builder need. Builders still shine with validation.

**Example (validated builder):**
```kotlin
class CheckoutRequest private constructor(
    val cartId: String,
    val shippingAddressId: String?,
    val promoCode: String?
) {
    class Builder(private val cartId: String) {
        private var shippingAddressId: String? = null
        private var promoCode: String? = null

        fun shippingAddressId(value: String) = apply { shippingAddressId = value }
        fun promoCode(value: String) = apply { promoCode = value }

        fun build(): CheckoutRequest {
            require(cartId.isNotBlank())
            return CheckoutRequest(cartId, shippingAddressId, promoCode)
        }
    }
}
```

---

### 5) Prototype
**Intent:** create new objects by copying an existing instance.

**Android use cases**
- Immutable UI state duplication with slight changes

**Kotlin replacement**
Data classes give this for free:
```kotlin
data class UiState(val items: List<String> = emptyList(), val isLoading: Boolean = false)
val next = old.copy(isLoading = true)
```

---

## Structural patterns

### 1) Adapter
**Intent:** convert one interface into another expected by clients.

**Android use cases**
- Mapping network DTOs → domain models
- Wrapping 3rd-party SDK callbacks into `Flow`

**Example: callback → Flow adapter**
```kotlin
interface SdkListener { fun onEvent(value: String) }
interface Sdk { fun setListener(listener: SdkListener) }

fun Sdk.events(): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.callbackFlow {
    val listener = object : SdkListener {
        override fun onEvent(value: String) { trySend(value).isSuccess }
    }
    setListener(listener)
    awaitClose { setListener(object : SdkListener { override fun onEvent(value: String) {} }) }
}
```

**Pitfalls**
- Hiding errors: decide whether mapping failures throw, return null, or use `Result`.

---

### 2) Facade
**Intent:** provide a simplified API for a complex subsystem.

**Android use cases**
- A `CheckoutFacade` hiding cart, inventory, payment, analytics.
- One entry point for “user session” orchestration.

**Example: facade over multiple services**
```kotlin
class CheckoutFacade(
    private val cart: CartService,
    private val payment: PaymentService,
    private val analytics: Analytics
) {
    suspend fun submitOrder(cartId: String): OrderResult {
        analytics.track("checkout_submit")
        val total = cart.calculateTotal(cartId)
        return payment.charge(total)
    }
}
```

**Pitfalls**
- Facade becoming a god object. Keep it cohesive around one user journey.

---

### 3) Decorator
**Intent:** add behavior by wrapping an object (without subclassing).

**Android use cases**
- Add caching, retries, logging, metrics around repositories or API clients.
- OkHttp interceptors are effectively a decorator-like pipeline.

**Example: repository decorator (telemetry)**
```kotlin
class TelemetryUserRepository(
    private val delegate: UserRepository,
    private val telemetry: Telemetry
) : UserRepository {
    override suspend fun getUser(id: String): User {
        return telemetry.time("user.get") { delegate.getUser(id) }
    }
}
```

**Pitfalls**
- Too many wrappers → hard to debug. Prefer structured composition and good naming.

---

### 4) Proxy
**Intent:** stand-in for another object (lazy, access control, caching).

**Android use cases**
- Lazy-loading expensive resources
- Permission-gated operations
- Local cache fronting a network client

**Note**
Many “repository” implementations are effectively proxies: they decide local vs remote.

---

### 5) Composite (bonus – common in UI)
**Intent:** treat individual objects and compositions uniformly.

**Android use cases**
- UI trees (View hierarchy, Compose tree)
- Menu structures

---

## Behavioral patterns

### 1) Observer
**Intent:** automatically notify dependents of changes.

**Android mapping**
- `Flow`, `StateFlow`, `SharedFlow`, LiveData

**Example: StateFlow as observer**
```kotlin
class UserViewModel(private val repo: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = repo.getUser(userId)
            _state.value = UiState(userName = user.name, isLoading = false)
        }
    }
}

data class UiState(val userName: String = "", val isLoading: Boolean = false)
```

**Pitfalls**
- Memory leaks with manual listeners; prefer lifecycle-aware streams.
- “Event” vs “State” confusion. Use `StateFlow` for state, `SharedFlow`/channels for one-offs.

---

### 2) Strategy
**Intent:** define a family of algorithms and make them interchangeable.

**Android use cases**
- Different pricing or promotion strategies
- Different sorting/filtering logic

**Example: strategy for pricing**
```kotlin
fun interface PricingStrategy {
    fun finalPrice(cents: Long): Long
}

class Promo10Percent : PricingStrategy {
    override fun finalPrice(cents: Long) = (cents * 90) / 100
}

class NoPromo : PricingStrategy {
    override fun finalPrice(cents: Long) = cents
}

class CheckoutCalculator(private val strategy: PricingStrategy) {
    fun total(cents: Long) = strategy.finalPrice(cents)
}
```

**Pitfalls**
- Explosion of small classes; Kotlin `fun interface` helps.

---

### 3) Command
**Intent:** encapsulate a request as an object.

**Android use cases**
- Toolbar actions / undo stacks
- Queued work (WorkManager is command-ish)

**Example: command for user actions**
```kotlin
fun interface Command {
    suspend fun execute()
}

class TrackEventCommand(private val analytics: Analytics, private val name: String) : Command {
    override suspend fun execute() { analytics.track(name) }
}
```

---

### 4) State
**Intent:** change behavior when internal state changes, without big conditionals.

**Android use cases**
- Checkout flow (`Idle`, `Loading`, `Success`, `Error`)
- Media player states

**Kotlin-first approach**
Use sealed types for state representation:
```kotlin
sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Error(val message: String) : LoadState
    data class Data(val value: String) : LoadState
}
```

---

### 5) Template Method
**Intent:** define algorithm skeleton, let subclasses override steps.

**Android note**
Use carefully: inheritance-heavy patterns don’t play as well with composition + DI.
Often replaced by higher-order functions.

---

## Android-specific pattern mappings (what you’ll say in interviews)
- **Repository Pattern** (not GoF, but common in Android): abstracts data sources; often uses Proxy/Facade internally.
- **Use Case / Interactor**: application-specific command; usually a thin class around one business action.
- **Dependency Injection (DI)**: not a pattern category here, but it replaces/implements many factories/singletons.
- **MVI/MVVM**: architectural patterns; they use Observer, Strategy (reducers), Command (intents) concepts.

---

## Common pitfalls in Android codebases
- Putting patterns *inside* Activities/Fragments instead of behind interfaces.
- “Repository does everything” (network, db, mapping, caching, business logic). Split responsibilities.
- Overusing inheritance for UI base classes. Prefer composition, delegates, and extension functions.
- Ignoring lifecycle and cancellation: make sure coroutines are scoped properly.

---

## Checklist for reviewing a pattern in PRs
- Does it reduce coupling or just add layers?
- Are the boundaries testable with fakes?
- Is ownership clear (who creates, who disposes, who listens)?
- Are errors and cancellations explicit?
- Is naming aligned with intent?

---

## Quick reference list
### Creational
- Singleton, Factory Method, Abstract Factory, Builder, Prototype

### Structural
- Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy

### Behavioral
- Chain of Responsibility, Command, Interpreter, Iterator, Mediator, Memento, Observer, State, Strategy, Template Method, Visitor
