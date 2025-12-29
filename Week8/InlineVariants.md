# Resources

[Youtube](https://www.youtube.com/watch?v=Itb5fu4UVt4)  
[Medium – inline / noinline / crossinline](https://medium.com/android-news/inline-noinline-crossinline-what-do-they-mean-b13f48e113c2)

---

# Inline, `noinline`, `crossinline` in Kotlin – Overview

- Primarily used for **utility functions that take lambdas** (higher-order functions).
  - Most Android/Kotlin devs consume libraries (Kotlin stdlib, coroutines, Flow, Compose) that already use these heavily.
- Goal: **optimize performance** and **enable special behavior** by controlling how lambdas are compiled.
- **`inline`** copies the function body (and some lambdas) into the **call site**, avoiding function call + lambda allocation overhead.
- **`noinline`** keeps a lambda parameter as a **normal function object**, so it can be **stored or passed around**.
- **`crossinline`** prevents **non-local returns** from a lambda parameter so it can be safely called later (e.g., from another lambda, thread, or coroutine).
- You can mark **functions**, **properties**, and certain **classes** (via value classes/@JvmInline) for inlining, but in practice you mostly inline **functions that take lambdas**.

> Mental model: `inline` is a compile-time optimization and feature switch. It can make hot path higher‑order utilities cheaper and unlock features like `reified` generics and non‑local returns.

---

## Normal vs Inline Functions – Basic Example

```kotlin
fun main() {
    val list = (1..100).toList()
    list.normalForEach {
        println(it)
    }
}

fun <T> List<T>.normalForEach(action: (T) -> Unit) {
    for (item in this) {
        action(item)
    }
}
```

Remember: Kotlin compiles to JVM bytecode (Java bytecode). Decompiling the JVM version looks roughly like this:

```java
public static final void main() {
    List<Integer> list = CollectionsKt.toList((Iterable) RangesKt.until(1, 100));
    normalForEach(list, MainKt::main$lambda$0); // function reference is passed
}

public static final void normalForEach(@NotNull List $this$normalForEach,
                                       @NotNull Function1 action) {
    Intrinsics.checkNotNullParameter($this$normalForEach, "$this$normalForEach");
    Intrinsics.checkNotNullParameter(action, "action");
    Iterator iterator = $this$normalForEach.iterator();
    while (iterator.hasNext()) {
        Object item = iterator.next();
        action.invoke(item); // lambda call per item
    }
}
```

- The lambda is compiled to a **Function1 object** and invoked for each element.
- There’s overhead for the **function object allocation** and the **`action.invoke`** call.

### Inline Version

```kotlin
fun main() {
    val list = (1..100).toList()
    list.inlineForEach {
        println(it)
    }
}

inline fun <T> List<T>.inlineForEach(action: (T) -> Unit) {
    for (item in this) {
        action(item)
    }
}
```

Decompiled Java code for the inline version (simplified):

> NOTE: the lambda body is effectively **inlined into the call site**, not called via a function reference.

```java
public static final void main() {
    List<Integer> list = CollectionsKt.toList((Iterable) RangesKt.until(1, 100));
    List $this$inlineForEach = list;
    int $i$f$inlineForEach = 0;
    Iterator iterator = $this$inlineForEach.iterator();
    while (iterator.hasNext()) {
        Object item = iterator.next();
        int it = ((Number) item).intValue();
        System.out.println(it); // body pasted here
    }
}
```

- No `Function1` object is created for the lambda.
- No `invoke` call per element – just straight-line code.

---

## Inline + Coroutines

Inline is especially useful with coroutines and suspend functions.

```kotlin
fun main() {
    val list = (1..100).toList()

    CoroutineScope(Dispatchers.Default).launch {
        // If inlineForEach were NOT inline, this would need a suspend lambda
        // because delay is suspend and must be called from a suspend context.
        // By inlining, the compiler pastes this body into the coroutine, so
        // delay is still called from within a coroutine.
        list.inlineForEach {
            delay(100)
            println(it)
        }
    }
}

inline fun <T> List<T>.inlineForEach(action: (T) -> Unit) {
    for (item in this) {
        action(item)
    }
}
```

- Without `inline`, `inlineForEach` would either:
  - Need a `suspend` lambda type, or
  - Not be able to call `delay` from the lambda.
- With `inline`, the body is moved **into the coroutine**, so calling `delay` is valid.

---

## Inline + Reified Generics

```kotlin
// This will NOT compile
fun <T> T.printClassName() {
    // Error: cannot use 'T' as reified type parameter. Use a class instead.
    // Because generics are erased at runtime, T::class is not available.
    println(T::class.simpleName)
}

// Works because T is reified in an inline function
inline fun <reified T> T.inlinePrintClassName() {
    println(T::class.simpleName)
}
```

**Why this works:**

- Normally, JVM erases generic types at runtime, so `T::class` is unavailable.
- In an **inline** function, the compiler knows the **concrete T** at each callsite and can replace `T::class` with the real class.
- `reified` means “don’t erase the type argument here – bake the actual type into the inlined code instead”.

> Reified type parameters are one of the biggest **practical** reasons to use `inline` in Kotlin APIs (JSON helpers, DI helpers, repository helpers, etc.).

---

## Non-Local Returns, `crossinline`

```kotlin
fun main() {
    val list = (1..10).toList()

    list.normalForEach {
        println(it)
        return@normalForEach // returns from lambda only, NOT from main
    }

    list.inlineForEach {
        println(it)
        return // non-local return: returns from main, because function is inline
        // You can use return@inlineForEach to return only from the lambda body.
    }
}
```

- With **non-inline** lambdas, `return` can only exit the lambda itself.
- With **inline** higher-order functions, a `return` in the lambda can “jump out” of the **calling function** – this is a **non-local return**.

This becomes a problem if the lambda is called **later**, e.g., in a coroutine or another thread:

```kotlin
inline fun executeAsync(crossinline action: () -> Unit) {
    CoroutineScope(Dispatchers.Default).launch {
        // Without crossinline, this is not allowed:
        // "Can't inline 'action' here: it may contain non-local returns."
        action()
    }
}
```

**Why we need `crossinline` here:**

- If `action` could do a non-local `return`, it might try to return from `executeAsync` **after** `executeAsync` has already finished (because the coroutine is delayed / outlives the caller).
- That’s invalid – the call stack is gone – so the compiler forbids inlining a lambda that might contain a non-local return **into a different coroutine/thread context**.
- Marking the parameter `crossinline` tells the compiler: “This lambda is **not allowed** to do a non-local return; normal returns only.”

In other words:

> `crossinline` is how you say: "I want this lambda inlined, but it will be used in a context where non-local returns would be unsafe, so disallow them."

---

## `noinline` – Let a Lambda Escape

```kotlin
inline fun performOperations(
    operation1: () -> Unit,
    noinline operation2: () -> Unit
) {
    operation1()       // inlined
    operation2()       // NOT inlined: called via function reference
}
```

- `operation1` is fully inlined at each callsite.
- `operation2` stays as a **function object** and can be:
  - Stored in a field or local variable.
  - Passed to another function.
  - Used in APIs that require a Function reference.
- This keeps your inline function flexible and avoids **huge bytecode** if a lambda is large or rarely used.

---

## Inline Properties & Value Classes

```kotlin
// Under the hood, this effectively inlines the getter logic.
inline val <T> List<T>.lastItem: T
    get() = get(lastIndex)

fun main() {
    val list = listOf(1, 2, 3)
    println(list.lastItem)
    // Compiles down roughly to: println(list.get(list.size - 1))
}
```

```kotlin
// Typically inline is also used with value classes (@JvmInline)
// Value classes wrap a single value and provide type safety without a full object allocation.

@JvmInline
value class Month(val month: Int)

// At compile time, the compiler tries to represent Month as just an Int in many places.
// e.g., val currentMonth: Month = Month(5)  can often be optimized to working with plain Ints.
```

---

## Common Use Cases

- **Small, frequently used higher-order utilities**
  - e.g., collection helpers (`forEach`, `map`, `filter`-style), small Flow/Compose helpers.
  - Avoids lambda allocation and call overhead on hot paths.

- **Coroutines helpers**
  - Inline builders/utilities that wrap coroutines or flows where:
    - You want to call suspend functions from a lambda without marking the parameter as `suspend`.
    - You want to avoid capturing extra objects in each call.

- **Generic helpers with reified types**
  - JSON parsing: `inline fun <reified T> String.fromJson(): T`.
  - DI helpers: `inline fun <reified T> inject(): T`.
  - Reflection helpers: `inline fun <reified T> isOfType(value: Any): Boolean = value is T`.

- **Non-local control flow**
  - Early-exit from loops/searches: `inline fun <T> List<T>.firstMatching(...)` allowing `return` from caller.

- **`noinline` for flexibility**
  - Mixed behavior: inline some lambdas for performance, but keep others as regular function objects when they need to be stored, passed, or kept small in bytecode.

---

## Common Pitfalls

- **Code size / bytecode bloat**
  - Every callsite gets its own copy of the inlined body.
  - For **large** inline functions or many callsites, this increases bytecode size and can hurt instruction cache.
  - Rule of thumb: keep inline functions **small and focused**.

- **Debugging can be harder**
  - Stack traces and stepping through code may look confusing because the code is inlined.
  - You might not see the inline function as a distinct frame.

- **Non-local returns confusion**
  - Newcomers often don’t realize that `return` inside an inline lambda can exit the **caller**, not just the lambda.
  - This can make control flow harder to reason about if overused.

- **Misusing `crossinline`**
  - Adding `crossinline` “just to make the compiler happy” without understanding **why** can hide real design issues.
  - If you’re calling a lambda in another coroutine/thread, double-check whether you actually need non-local returns at all.

- **Over-inlining everything**
  - Inlining functions that **don’t take lambdas** or aren’t on hot paths usually provides little benefit.
  - Inline is most valuable for **small higher-order functions and reified generics**.

- **`noinline` forgetting behavior**
  - `noinline` lambdas can’t use non-local returns, and they still allocate function objects.
  - Don’t assume `noinline` gets the same performance characteristics as normal inline lambdas.

---

## Interview-Oriented Q&A

### 1. Explain `inline`, `noinline`, and `crossinline` in Kotlin.

**High-Level**
- `inline`: copy the function body to the callsite; avoids some overhead and enables reified generics and non-local returns.
- `noinline`: keep a specific lambda parameter as a function object; allows storing and passing it.
- `crossinline`: inline the lambda but disallow non-local returns so it can be called later (e.g. in another coroutine).

**Details / Talking Points**
- Inline is mainly used for **small higher-order functions** (e.g. library utilities, collection extensions).
- `noinline` is typically used when **most** lambdas should be inlined, but one particular parameter needs to **escape** (be stored/passed) or you want to avoid code bloat.
- `crossinline` is often required when you:
  - Inline a lambda but call it from a different execution context (another thread, coroutine, callback).
  - Need to guarantee that `return` from that lambda doesn’t try to exit the outer function.

**Succinct Answer**
> `inline` tells the compiler to paste the function body and its lambdas into each callsite, which reduces lambda allocation overhead and lets me use features like reified generics and non-local returns. `noinline` marks a specific lambda parameter that shouldn’t be inlined so I can safely store or pass it as a regular function object. `crossinline` is used when a lambda is inlined but will be called later from another context, so non-local returns would be unsafe; it disables non-local returns for that parameter.

---

### 2. When would you use `reified` type parameters in an inline function?

**High-Level**
- When you need the **actual type** of a generic parameter at runtime.
- Especially for **JSON serialization/deserialization**, DI, reflection, and type-safe helpers.

**Details / Talking Points**
- JVM normally erases generic type parameters.
- `inline fun <reified T> ...` lets you call `T::class`, `value is T`, etc.
- Examples:
  - `inline fun <reified T> Gson.fromJson(json: String): T`.
  - `inline fun <reified T> Context.getSystemServiceCompat(): T`.

**Succinct Answer**
> I use reified type parameters when I need to inspect or use the actual generic type at runtime, like converting JSON into a specific type or building helpers that do `value is T`. Because generics are normally erased on the JVM, this is only possible inside inline functions where the compiler can substitute the real type at each callsite.

---

### 3. What performance benefits do inline functions provide, and when would you avoid them?

**High-Level**
- Benefit: avoid extra allocations and calls for small, frequently used higher-order functions.
- Avoid: large bodies that are rarely called – they can **inflate bytecode**.

**Details / Talking Points**
- Inline removes the need to allocate a lambda object and call it through `invoke`.
- In tight loops or hot code paths, that can be a measurable win.
- But duplicating a large body at many callsites can:
  - Increase APK size.
  - Potentially hurt instruction cache / startup.

**Succinct Answer**
> I get the most benefit from inline on small, frequently used higher-order functions because it avoids creating lambda objects and extra calls. 
> However, I avoid inlining large or complex functions that are used in many places because that can bloat my bytecode and hurt overall performance more than it helps.

---

### 4. How would you explain `crossinline` to a teammate who’s new to coroutines?

**High-Level**
- It’s about **where a `return` is allowed to jump** from a lambda.
- With `crossinline`, you can’t use `return` to exit the calling function.

**Details / Talking Points**
- In a normal inline lambda, `return` can exit the outer function (non-local return).
- If the lambda is called from a coroutine or another thread, that’s unsafe.
- `crossinline` marks the parameter so the compiler **blocks** non-local returns, but still inlines the body.

**Succinct Answer**
> I’d tell them that `crossinline` is a safety switch for inlined lambdas that are going to be called later, like inside a coroutine. 
> In a normal inline lambda, a `return` can exit the outer function, but if the lambda is run later that return would be invalid. 
> `crossinline` disables those non-local returns so you can still inline the lambda but safely call it from another context.

---

### 5. How do you decide between `inline` and just writing a normal higher-order function?

**High-Level**
- Ask: Is this **small**, **called a lot**, or does it need **reified types / non-local returns**?

**Details / Talking Points**
- Inline when:
  - The function is a small helper used in many places, especially with lambdas.
  - You want `reified` generics or non-local returns.
- Keep it normal when:
  - It’s not performance-critical.
  - The function body is large, complex, or rarely used.

**Succinct Answer**
> I inline a function when it’s a small helper that’s called frequently with lambdas or when I need reified generics or non-local returns. 
> If it’s not performance-critical or the body is big and complex, I keep it as a regular higher-order function so I don’t risk bytecode bloat or harder debugging.
