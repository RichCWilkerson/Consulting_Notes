# Resources
- [Building Modular Monoliths with Kotlin and Spring (JetBrains)](https://blog.jetbrains.com/kotlin/2026/02/building-modular-monoliths-with-kotlin-and-spring/)

## Quick clarifications (Android-dev friendly)

### Is this purely backend, or does it couple with web/mobile apps?
A *modular monolith* (and Spring Modulith) is a **backend-only** architectural approach.

- It’s about structuring a **single server-side deployable** into well-defined *modules*.
- Your Android / web client **doesn’t become “part of the modulith”**. Clients still integrate over a network boundary (REST/GraphQL/gRPC).
- What you *do* share across client/server is **contract and domain concepts** (e.g., GraphQL schema, OpenAPI spec, events, DTOs), not runtime modules.

### When would this matter to an Android developer?
- Cleaner backend boundaries usually mean **cleaner APIs** for mobile.
- Better maintainability and testing on backend teams translates to **fewer breaking changes**, clearer versioning, and better incident response.

---

## Modular Monoliths ("Modulith")
> NOTE: It’s worth being comfortable with Spring Boot fundamentals (Java *or* Kotlin) before diving into Spring Modulith.

A **modular monolith** is:
- **One deployment unit** (one app/process)
- split into **modules aligned to business capabilities**
- with **enforced boundaries** (compile-time + runtime verification)

### Benefits
1. **Simplified deployment**
   - A single artifact simplifies releases: no service-mesh concerns, no distributed rollbacks, fewer coordinated deployments.
2. **Reliable testing**
   - In-process calls make integration testing faster and less brittle than networked service tests.
3. **Stronger domain modeling**
   - Modules map to business capabilities and ownership. Communication is constrained to public APIs/events.
4. **In-process communication**
   - Direct invocation reduces latency and failure modes compared to remote calls.

---

## Spring Modulith
Spring Modulith is a framework that helps you build modular monoliths in Java/Kotlin with Spring Boot.

It focuses on:
- defining modules (usually via **package structure**)
- enforcing module boundaries
- providing tooling for verification, documentation, and observability

---

## Integrating Spring Modulith

### 1) Add dependencies
```gradle
// Can be used from Java or Kotlin Spring Boot projects.
dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.modulith:spring-modulith-starter-core:1.4.3")

    // Often useful in real systems:
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:1.4.3")
}
```

### 2) Define a module-friendly package layout
The key is: **packages become your module boundaries**.

Example (conceptual):
- `com.example.app.order`
- `com.example.app.product`
- `com.example.app.payment`

### 3) Enable Modulith
Add the Modulith annotation to your application class.

```kotlin
@org.springframework.modulith.Modulithic
@SpringBootApplication
class Application
```

### 4) Declare module metadata (Java `package-info.java` vs Kotlin)
Spring Modulith commonly uses `package-info.java` because it’s a standard Java mechanism for attaching annotations to packages.

#### TODO answer: what’s the Kotlin equivalent, and is it still needed?
- Kotlin **does not** have a first-class `package-info.kt` equivalent for package annotations in the same way.
- In practice, teams using Kotlin + Modulith usually still add a small **`package-info.java`** file *just for module annotations*.
- So yes: **it’s still commonly needed** if you want to annotate the *package (module)* itself (e.g., `@ApplicationModule(allowedDependencies = …)`).

Example:
```java
// order/package-info.java
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"product"})
@org.springframework.lang.NonNullApi
package com.example.springmonolith.order;
```

And:
```java
// product/package-info.java
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
@org.springframework.lang.NonNullApi
package com.example.springmonolith.product;
```

Notes:
- `allowedDependencies = {}` means the module should not depend on other modules.
- The verification tests will flag violations if code in that module reaches into disallowed modules.

---

## Spring Modulith features
### Module verification / structure checks
Spring Modulith can:
- check for cyclic dependencies
- validate that modules only depend on other modules through allowed/public API packages
- enforce explicit dependency rules

A common test pattern:
```kotlin
class ModularityTests {

    @Test
    fun verifiesModularStructure() {
        org.springframework.modulith.core.ApplicationModules
            .of(Application::class.java)
            .verify()
    }
}
```

If `product` is configured to disallow inter-module dependencies and it tries to access another module, you’ll see a violation like:
```
— TRUNCATED OUTPUT —
ModularityTests > verifiesModularStructure() FAILED
    org.springframework.modulith.core.Violations at ModularityTests.kt:20
```

### Prefer events for cross-module communication
One way to preserve boundaries is to publish **application/domain events** rather than call into internal code of other modules.

```kotlin
// order module
@Service
class OrderService(private val events: ApplicationEventPublisher) {

    fun completeOrder(orderId: String) {
        events.publishEvent(OrderCompleted(orderId))
    }
}

data class OrderCompleted(val orderId: String)
```

Another module reacts:
```kotlin
// product module
@Component
class InventoryPolicy {

    @org.springframework.modulith.ApplicationModuleListener
    fun on(event: OrderCompleted) {
        println("Updating inventory for order: ${event.orderId}")
    }
}
```

---

## Module-level testing
Modulith supports writing integration tests scoped to a single module.

A typical approach is `@ApplicationModuleTest(s)` (depending on the version/artifacts).

Example:
```kotlin
@org.springframework.modulith.test.ApplicationModuleTest
class ProductModuleTests {

    @Test
    fun testProductServiceGreeting() {
        val greeting = productService.getGreeting()
        assertTrue(greeting.contains("Product Module"))
    }
}
```

If the `order` module depends on `product`, include it:
```kotlin
@org.springframework.modulith.test.ApplicationModuleTest(extraIncludes = ["product"])
class OrderModuleTests {

    @Autowired
    private lateinit var orderService: OrderService

    @Test
    fun testOrderServiceGreeting() {
        val greeting = orderService.getGreeting()
        assertTrue(greeting.contains("Order Module"))
    }
}
```

> Minor cleanup: your original notes had `@ApplicationModuleTests` vs `@ApplicationModuleTest` inconsistently. The exact annotation name comes from the Modulith test starter/version; rely on what your project imports.

---

## Documentation and observability
### Documentation
Spring Modulith can generate:
- PlantUML component diagrams
- module catalogs/tables

```kotlin
class DocumentationTests {
    private val modules = org.springframework.modulith.core.ApplicationModules.of(Application::class.java)

    @Test
    fun writeDocumentationSnippets() {
        org.springframework.modulith.docs.Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }
}
```

### Observability
Spring Modulith integrates with Micrometer to capture spans for module interactions.
Those spans can be exported to tracing systems (e.g., Zipkin/Jaeger) to visualize:
- which modules call which
- event flows
- runtime hotspots

---

## Practical notes that are easy to miss

### API boundaries: public vs internal packages
A useful convention:
- `com.example.app.order` (module root)
- `com.example.app.order.api` (public types exposed to other modules)
- `com.example.app.order.internal` (implementation details)

Then:
- other modules depend only on `*.api`
- internal packages stay private (verification can enforce this)

### Data boundaries and transactions
A modulith often uses **one database**, but you still want module ownership:
- Prefer each module “owning” its tables and mapping.
- Cross-module reads are sometimes unavoidable, but treat them as an exception.
- For workflows spanning modules (order → payment → inventory), consider:
  - domain events + eventual consistency
  - compensating actions
  - keeping one module the “orchestrator” but only calling other modules via their public API/event contracts

### Migration path to microservices
A huge selling point is being able to **start as a modulith** and extract modules later.
Signals a module is ready to split:
- independent scaling needs
- independent deployment needs
- different uptime/SLA/security requirements
- high change rate and strong team ownership

---

## When to use modular monoliths
It isn’t universally the right choice.

### Good fit when
- **Early-stage development / limited resources**
  - You get strong boundaries without distributed systems overhead.
- **Complex business domains**
  - Clear module ownership reduces accidental coupling.
  - Example: insurance platform with policy, claims, and customer support modules.

### Not a great fit when
- **Independent scaling needs dominate**
  - Example: catalog/recommendations vs orders/payments in e-commerce.
- **Teams need diverse tech stacks**
  - Example: ML services in Python/Go while the rest is Kotlin/Java.
