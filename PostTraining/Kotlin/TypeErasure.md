# Resources:
- [Medium - inline, reified, and more](https://proandroiddev.com/kotlin-inline-reified-to-solve-type-erasure-and-a-practical-guide-on-noinline-crossinline-and-63ecbf693250)
- [Medium - Type Erasure in Kotlin](https://medium.com/@AlexanderObregon/how-kotlin-handles-type-erasure-on-the-jvm-36d03d83ca82)

# Overview of Kotlin Type Erasure on the JVM
**Type Erasure**:
On the JVM, generic type arguments are not preserved at runtime (they are erased).
The bytecode typically only sees the raw type (e.g. List instead of List<String>).

Practical consequences:
- Generics are **mostly compile-time**: they help with type safety and better APIs, but the JVM usually doesn’t know the concrete type arguments.
- The erased type is either `Object` or the **upper bound** of the type parameter (e.g. `T : Number` becomes `Number`).
- Some frameworks simulate reified generics using metadata, reflection, or codegen.

## Generics with Type Erasure and Inline Reified Types
**How does code know what T is in a generic function?**
- At compile time the compiler fully knows T and uses it for:
    - Type checking (e.g. preventing you from passing a List<Int> where List<String> is expected).
    - Generating correct calls and casts.
- At runtime that information is normally gone due to erasure, so:
    - You cannot do `if (value is T)` in a normal generic function.
    - You cannot reflect on the concrete T without extra help (like passing Class<T> or KClass<T>).
- Kotlin’s `inline fun <reified T>` is the main escape hatch:
    - Because the function is inlined, the compiler can substitute the actual type and keep it in the bytecode, so `value is T` and `T::class` are allowed.

```kotlin
fun <T> printType(value: T) {
    println(T::class.simpleName) // This will cause an error
}
```
- To retain type information, a common workaround is passing the type explicitly:
```kotlin
fun <T> printType(value: T, clazz: Class<T>) {
    println(clazz.simpleName)
}
```
- or with less boilerplate using reified:
```kotlin
inline fun <reified T> printType(value: T) {
    println(T::class.simpleName)
}
```

## Additional Use Cases
**reified enables handling different return types dynamically**
```kotlin
fun displayData(marks: Int): Int {
    return marks
}
fun displayData(marks: Int): String {
    return "Congratulations! You scored more than 90%!"
}
// Error: Overloaded functions must differ in parameter types, not return types.
```

- using reified for dynamic return types:
```kotlin
inline fun <reified T> displayData(marks: Int): T {
    return when (T::class) {
        Int::class -> marks as T
        String::class -> "Congratulations! You scored more than 90%!" as T
        else -> throw IllegalArgumentException("Unsupported type")
    }
}
```

### Limitations of Reified 
1. Only works with inline functions.
2. Cannot Be Used with Complex Generic Types
```kotlin
inline fun <reified T> printList(list: List<T>) {
    // Won't work as expected for List<Int> or similar types
}
```
3. Cannot Be Used in Class-Level Generics
```kotlin
class SampleClass<reified T> { ... } // This will not work
```

## Detailed Explanation of Type Erasure
**Where does type erasure happen?**
- Classes, interfaces, and functions with generics all compile down to erased versions:
    - `class Box<T>` becomes a raw `Box` in bytecode, with `Object` (or a bound) used where `T` appears.
    - `fun <T> foo(arg: T)` becomes a method taking `Object` (or the upper bound).
- Upper bounds are respected:
    - `class Box<T : Number>` erases `T` to `Number`.
> So yes, all generic type parameters are subject to erasure on the JVM, with the exception of
> special cases that embed the type in metadata and are interpreted by libraries
> (e.g. reflection, some serializers), or Kotlin reified inline helpers.

**What is affected by type erasure?**
- **Overloads**:
    - You cannot overload only by differing generic parameters.
    - `fun process(list: List<Int>)` and `fun process(list: List<String>)` erase to the same JVM signature `process(List)`.
- **`is` / `instanceof` checks**:
    - `value is List<String>` is forbidden / warned, because at runtime only `List` is known.
    - You can only reliably check the raw type: `value is List<*>`.
- **Array creation**:
    - `Array<T>` is special; you cannot do `Array<T>(10)` safely without passing the class: 
      - `java.lang.reflect.Array.newInstance(T::class.java, 10)` pattern, or use Kotlin helpers.
- **Reflection / serialization**:
    - Libraries like Gson, Moshi, kotlinx.serialization, Retrofit, etc. need extra information to know `T` at runtime:
        - Type tokens (`object : TypeToken<List<User>>() {}`),
        - Passing `KClass`/`Class` explicitly,
        - Or using Kotlin-specific reified helpers like `inline fun <reified T> fromJson(...)`.

**What should a senior Kotlin/Java developer know about type erasure?**
- **Design limitations**:
    - Don’t rely on generics for runtime behavior unless you supply explicit type tokens or use reified inline functions.
    - Avoid APIs that require knowing `T` at runtime without providing a way to pass it.
- **API compatibility**:
    - Be careful adding generic overloads; due to erasure, they may clash at the JVM level or behave unexpectedly from Java.
- **Interop**:
    - When exposing Kotlin APIs to Java, remember that `List<String>` and `List<Any>` both compile to `List` in bytecode, so extra constraints may come only from annotations (`@NotNull`, `@Nullable`) or documentation.
- **Performance and safety**:
    - Generics are mostly a compile-time feature on the JVM: they don’t usually cost at runtime, but they also don’t protect you from all runtime `ClassCastException`s if you lie to the compiler (unchecked casts, raw types).
- **Patterns to work around erasure**:
    - `inline fun <reified T> parse(...)` style helpers.
    - Explicit `Class<T>` / `KClass<T>` parameters in constructors or factory methods.
    - Type tokens for complex generic hierarchies where reified is not an option (e.g., non-inline APIs, Java libraries).

## Common Pitfalls
- **Unsafe casts hidden by generics**
  - Example: mixing raw Java collections and Kotlin generics can compile but throw at runtime:
    - Java adds a `List` to a map, Kotlin reads it as `List<String>` and hits `ClassCastException`.
  - Senior-level takeaway: treat unchecked cast warnings as real smell, especially when interoperating with legacy Java.

- **Incorrect `is` checks on parameterized types**
  - `if (list is List<String>)` does not behave the way many expect; the `<String>` part is ignored at runtime.
  - Prefer `list is List<*>` plus element-level checks if needed.

- **Overload/erasure conflicts**
  - Kotlin may allow an overload in source, but the JVM signatures collide after erasure, forcing `@JvmName` or redesign.
  - Be very careful with overloaded extension functions on generics that are intended to be called from Java.

- **Losing type info in callbacks and coroutines**
  - Generic callbacks or suspending functions that need to know `T` at runtime must either:
    - be `inline` with `reified` type params, or
    - accept an explicit `KClass<T>` / `Type` parameter.
  - This shows up often in network/data layers (e.g. `suspend fun <T> callApi()` APIs).

- **Complex nested generics in reflection**
  - Frameworks that rely on reflection (DI, JSON, ORM) often can’t infer nested types without explicit hints, especially for `List<List<T>>`, `Map<K, V>`, etc.
  - You’ll see patterns like `object : TypeToken<Map<String, List<User>>>() {}` to preserve that shape.

## Common Interview Questions
- **Explain type erasure on the JVM and why it exists.**
  - Historical reasons (backwards compatibility with pre-generics Java).
  - How it lets older bytecode run with newer generic-aware code.

- **What are the limitations of generics in Java/Kotlin due to type erasure?**
  - No runtime access to generic type arguments in most cases.
  - No overloading purely by generic parameter types.
  - Restrictions on `instanceof` / `is` with parameterized types.

- **How do Kotlin `reified` type parameters work and when would you use them?**
  - Inline functions, compiler substitutes the concrete type.
  - Typical use cases: generic `fromJson<T>()`, DI resolution, generic mappers.

- **How can you work around type erasure when building libraries or frameworks?**
  - Accept `Class<T>` / `KClass<T>` or `Type`.
  - Use type tokens for deeply nested generics.
  - Provide `inline reified` helpers on top of lower-level APIs.

- **Compare Java and Kotlin’s approach to type erasure.**
  - Same underlying JVM model (erasure) and the same fundamental limitations.
  - Kotlin adds language features (`reified`, nullability, variance annotations) that make working with erased generics safer and more expressive.
