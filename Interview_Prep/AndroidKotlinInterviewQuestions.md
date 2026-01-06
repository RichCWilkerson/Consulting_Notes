# Android + Kotlin Interview Questions (Q1…Qn)

This is a sequential list of questions for group practice. Questions are renumbered in order without moving content. Many include quick code examples.

## TODOs: Add these to list where they need to go
1. what is the purpose of the manifest file:
- handles: permissions, main components (activities, services, receivers, providers), hardware, metadata, deeplinks
2. What is LiveData and how does it work -> how does it compare to StateFlow?
- LiveData: Lifecycle-aware observable data holder class, notifies observers when data changes, respects lifecycle states, Android specific component
- StateFlow: Part of Kotlin Coroutines, observable data holder, always has a value, can be collected in any coroutine scope, not lifecycle-aware by default
3. What is the lifecycle of a Fragment?
- onAttach() -> onCreate() -> onCreateView() -> onViewCreated() -> onStart() -> onResume() -> (active) -> onPause() -> onStop() -> onDestroyView() -> onDestroy() -> onDetach()
4. What is the difference of NavHost, NavController, NavGraph?
- NavHost: Container for navigation, hosts destinations (fragments)
- NavController: Manages app navigation, handles fragment transactions, back stack
- NavGraph: XML resource defining navigation paths, destinations, actions
5. What is the difference between Serializable and Parcelable?
- Serializable: Java interface, slower, uses reflection, easier to implement
- Parcelable: Android interface, faster, requires boilerplate code, optimized for Android IPC
6. What is the difference between a Service and an IntentService?
- Service: Runs on main thread, needs manual threading for long tasks
- IntentService: Subclass of Service, handles intents on a background thread, stops itself when done
7. What is withContext in Kotlin Coroutines?
- withContext: Switches coroutine context, used to change dispatcher (e.g., IO to Main), suspends until block completes
8. What is the difference between launch and async in Kotlin Coroutines?
- launch: Starts a coroutine, returns Job, used for fire-and-forget tasks 
  - Job: can be cancelled or joined, no result returned 
    - TODO: get a better explanation of Job (like cronjob?)
- async: Starts a coroutine, returns Deferred, used for tasks that return a result
9. What is a CoroutineScope?
- Defines the lifecycle of coroutines, manages their cancellation, tied to a specific context (e.g., Main, IO)
10. What is a Dispatcher in Kotlin Coroutines, what are the dispatchers and what is their primary use case?
- Dispatcher: Determines the thread(s) a coroutine runs on 
- Dispatchers.Main: Main thread, UI updates
- Dispatchers.IO: Background thread, I/O operations (in/out, database, network, file)
- Dispatchers.Default: Background thread, CPU-intensive tasks (sorting, parsing)
- Dispatchers.Unconfined: Starts in current thread, resumes in caller thread, not confined to any specific thread, used for testing
11. What are the DI annotations for Hilt, where should they be used, and what is their purpose?
- @HiltAndroidApp: on Application class, initializes Hilt
- @AndroidEntryPoint: on Activities/Fragments, enables injection
- @Module: on class, defines a module for providing dependencies (factory making the DIs)
- @InstallIn: on module class, specifies component scope (e.g., SingletonComponent)
- @Provides: on method, provides a dependency instance (specific "arm" factory is making)
- @Binds: on abstract method, binds an interface to its implementation
- @Inject: on constructor/property, requests a dependency to be injected
- @HiltViewModel: on ViewModel class, enables Hilt injection in ViewModels
12. What are the different Launch modes in Navigation Component and when to use them?
- standard: default, creates new instance each time
- singleTop: reuses top instance if it exists, otherwise creates new
- singleTask: reuses existing instance in back stack, pops to it
- singleInstance: like singleTask but in a separate task
- Use cases: standard for most, singleTop for idempotent actions, singleTask for unique screens (e.g., profile)
13. What is a RecylerView and how does it work?
- RecyclerView: Flexible view for displaying large data sets, recycles item views for efficiency (iterates)
- Adapter: Binds data to item views (4 main methods)
  - ViewHolder: Holds item view references (person of people), binding logic to views
  - onCreateViewHolder: Inflates item layout, creates ViewHolder
  - onBindViewHolder: Binds data to ViewHolder views
  - getItemCount: Returns total item count
14. What is ANR and how to avoid it?
- ANR (Application Not Responding): Occurs when main thread is blocked for 5 seconds
- Avoid by offloading long tasks to background threads (e.g., using Coroutines, AsyncTask, WorkManager)

## Questions
Q1. What is Kotlin?
- Kotlin is a modern, statically typed language for JVM/Android (also JS/Native) by JetBrains; interoperable with Java.

Q2. How is Kotlin different from Java?
- More concise and expressive, null safety, data classes, extension functions, coroutines, smart casts.

Q3. What are the advantages of using Kotlin?
- Concise syntax, null safety, Java interop, coroutines, functional features, great tooling.
Example:
```kotlin
// Extension + data class concision
data class User(val id: Int, val name: String)
fun String.screaming() = uppercase()
println("hello".screaming()) // HELLO
```

Q4. What is the difference between == and === in Kotlin?
- == checks structural equality (equals), === checks referential equality (same instance).
Example:
```kotlin
val a = "Hello"
val b = ("Hel" + "lo")
val c = a
println(a == b)   // true (same value)
println(a === b)  // may be false (different refs)
println(a === c)  // true (same ref)
```

Q5. What are the basic data types in Kotlin? Give examples.
- Numbers (Int, Long, Float, Double, Short, Byte), Boolean, Char, String.
```kotlin
val n: Int = 42; val pi: Double = 3.14; val ok: Boolean = true; val ch: Char = 'A'; val s: String = "Hi"
```

Q6. What is the difference between val and var?
- val is read‑only reference (no reassignment); var is mutable.
```kotlin
val x = 1 // x = 2 // error
var y = 1; y = 2 // ok
```

Q7. How does type inference work in Kotlin?
- Compiler infers types from initializers; the inferred type is then fixed.
```kotlin
val name = "Ana" // String
val count = 3     // Int
```

Q8. What is the difference between immutable and mutable variables?
- Immutable: val (no reassignment). Mutable: var (reassignable).

Q9. What is const and when can you use it?
- Compile‑time constant for top‑level or object/companion vals of primitive/String.
```kotlin
const val MAX = 100
```

Q10. What are value classes and why use them?
- @JvmInline value class wraps a single value with zero-ish overhead for type safety.
- TODO: @JvmInline is a value class?
- TODO: is a value class the same as an inline class?
```kotlin
@JvmInline value class UserId(val value: Int)
fun load(u: UserId) {}
```

Q11. What are nullable types?
- Append ? to allow nulls: String?; non‑nullable by default.

Q12. How do you handle nullability (safe call, Elvis, safe cast, non‑null assertion)?
```kotlin
val s: String? = get()
val len1 = s?.length              // safe call
val len2 = s?.length ?: 0         // Elvis default
val asStr: String? = any as? String // safe cast
val len3 = s!!.length             // throws if null
len54?.let { println(it) }          // let if non-null
if (s != null) println(s.length) // smart cast
```

Q13. What does the Elvis operator do?
- Returns the left if non‑null, otherwise the right default: a ?: default.

Q14. What are smart casts?
- After checks (if/when), compiler treats a value as non‑null or subtype in that branch.
```kotlin
fun printLen(x: String?) {
  if (x != null) println(x.length) // x smart‑cast to String
}
```

Q15. When (not) to use the non‑null assertion !!?
- Rarely; it throws NPE on null. Prefer safe patterns.

Q16. What does requireNotNull do?
- Throws IllegalArgumentException if null; returns non‑null value otherwise.
```kotlin
val name = requireNotNull(input) { "Name required" }
```

Q17. What does let do and when to use it?
- Execute a block with it and return the block result; great for nullable chains.
```kotlin
val upper = name?.let { it.uppercase() }
```

Q18. What does also do and when to use it?
- For side‑effects with it; returns the original object.
```kotlin
val list = mutableListOf<Int>().also { it += listOf(1,2,3) }
```

Q19. What does apply do and when to use it?
- Configure receiver this and return the receiver (builder/initialization).
```kotlin
val sb = StringBuilder().apply { append("a"); append("b") }
```

Q20. What does run do and when to use it?
- Execute with receiver this and return result.
```kotlin
val msg = StringBuilder().run { append("hi"); toString() }
```

Q21. How do you use run with nullable receivers?
```kotlin
val result = maybeObj?.run { toString() } ?: "null"
```

Q22. apply vs also: when to choose which?
- apply for configuration (this, returns receiver). also for side‑effects/logging (it, returns receiver).
- TODO: add example with outputs

Q23. let vs apply: what’s the difference?
- let returns block result (uses it); apply returns the receiver (uses this).
- TODO: add example with outputs

Q24. run vs let: what’s the difference?
- Both return block result; run uses this receiver, let uses it parameter.
- TODO: add example with outputs

Q25. What does with do?
- with(obj) executes with receiver and returns block result.
```kotlin
val len = with("hello") { length } // 5
```

Q26. What is a lambda expression?
```kotlin
val sum = { a: Int, b: Int -> a + b }
println(sum(2,3)) // 5
```

Q27. What are higher‑order functions (HOFs)?
- the idea that a variable can hold a function
- functions can take variables as parameters, and with higher-order functions:
  - we are allowing functions to be passed as parameters to other functions
  - we can also return functions from other functions
```kotlin
fun applyTwice(x: Int, f: (Int) -> Int) = f(f(x))
println(applyTwice(3) { it + 1 }) // 5
```

Q28. What are function types?
- Notation: (A, B) -> R; can be parameters, return types, or variables.
- TODO: I have no idea what this is asking
- TODO: add example with outputs

Q29. What are function references and where to use them?
- Use :: to refer to named functions as values.
```kotlin
fun inc(x: Int) = x + 1
val ref: (Int) -> Int = ::inc
```

Q30. What is a higher‑order function with receiver and why useful for DSLs?
```kotlin
fun String.wrap(transform: String.() -> String) = transform()
println("hi".wrap { "<$this>" }) // <hi>
```

Q31. What is function composition?
- Combine functions into pipelines.
```kotlin
val add1: (Int) -> Int = { it + 1 }
val dbl: (Int) -> Int = { it * 2 }
val composed = { x: Int -> dbl(add1(x)) }
```

Q32. What collection types does Kotlin provide?
- List, Set, Map (immutable interfaces + mutable variants).
```kotlin
val xs = listOf(1,2,3)
val ys = mutableListOf(1,2,3).apply { add(4) }
```

Q33. What’s the difference between a List and an Array?
- List is resizable abstraction; Array has fixed size and explicit type.

Q34. How do you create an empty list?
```kotlin
val empty: List<Int> = listOf()
```

Q35. Immutable vs mutable lists: how do you modify?
```kotlin
val ro = listOf(1,2,3)
val mu = ro.toMutableList().apply { add(4) }
```

Q36. What are destructuring declarations?
```kotlin
data class Point(val x: Int, val y: Int)
val (x, y) = Point(3,4)
```

Q37. first() vs firstOrNull()?
- first() throws on empty; firstOrNull() returns null.

Q38. What are generics and why use them?
```kotlin
class Box<T>(val value: T)
```

Q39. What is invariance, covariance (out), and contravariance (in)?
- TODO: provide definitions
- invariance:
- covariance:
- contravariance:
```kotlin
interface Producer<out T> { fun get(): T }
interface Consumer<in T> { fun put(x: T) }
```

Q40. What are reified type parameters and when do you need inline?
- inline + reified let you access T at runtime (T::class), useful for type checks and factories.
- this is new to me
```kotlin
inline fun <reified T> typeName() = T::class.simpleName
```

Q41. What does crossinline enforce?
- Prevents non‑local return from an inline lambda that’s invoked later (e.g., inside Runnable).
- TODO: provide an example

1. Without inline: the compiler allocates a lambda object, passes it to the function, the function calls the lambda via an interface call.
2. With inline: the compiler copies the function body into the call site, and also inlines the lambda body. No lambda object allocation, no virtual call.
3. Non‑local return: in an inline lambda you can return from the caller function.
4. crossinline: forbids non‑local returns (needed when the lambda is invoked later or from a different control flow).
5. noinline: prevents inlining a specific lambda so it can be stored or passed around.
6. Reified generics: only possible with inline; lets you use T::class at runtime.
```kotlin
// 1) Non-inline vs inline HOF
fun <T> applyTwiceNI(x: T, f: (T) -> T): T {
    // Without inline:
    // - A lambda object is allocated at call site
    // - This function is called, then it calls f(...) twice
    var r = f(x)
    r = f(r)
    return r
}

inline fun <T> applyTwice(x: T, f: (T) -> T): T {
    // With inline:
    // - The body of applyTwice AND the body of 'f' are copied at the call site
    // - No lambda allocation, no virtual lambda call
    var r = f(x)
    r = f(r)
    return r
}

// 2) Non-local return enabled by inlining
fun findFirstEven(xs: List<Int>): Int {
    // Standard library forEach is inline, so 'return it' jumps out of findFirstEven
    xs.forEach {
        if (it % 2 == 0) return it  // non-local return from the caller
    }
    return -1
}

// 3) crossinline prevents non-local return when the lambda is invoked later
inline fun runAsync(crossinline block: () -> Unit) {
    // The lambda is called in another context (Thread); non-local return would be illegal
    Thread { block() }.start()
}

// 4) noinline allows storing/passing a lambda (cannot be inlined)
inline fun doTwice(
    crossinline action: () -> Unit,
    noinline onFinish: () -> Unit
) {
    action()
    action()
    // Because 'onFinish' is noinline, we can store or pass it
    val later = onFinish
    later()
}

// 5) Reified generic example: simple Service Locator using T::class
object ServiceLocator {
    private val services = mutableMapOf<kotlin.reflect.KClass<*>, Any>()

    fun <T : Any> put(type: kotlin.reflect.KClass<T>, instance: T) {
        services[type] = instance
    }

    inline fun <reified T : Any> get(): T {
        // Reified gives access to T::class at runtime (normally erased)
        return services[T::class] as T
    }
}

fun main() {
    // Demo: inline vs non-inline
    val r1 = applyTwiceNI(3) { it + 1 }   // lambda object allocated
    val r2 = applyTwice(3) { it + 1 }     // inlined; no lambda allocation
    println("applyTwiceNI: $r1, applyTwice: $r2") // 5, 5

    // Demo: non-local return via inline forEach
    println(findFirstEven(listOf(1, 3, 4, 7))) // 4

    // Demo: crossinline/noinline
    runAsync { println("Running async") }
    doTwice(
        action = { println("Action") },
        onFinish = { println("Finished") }
    )

    // Demo: reified lookup
    ServiceLocator.put(String::class, "Hello")
    ServiceLocator.put(Int::class, 42)
    val s: String = ServiceLocator.get()
    val i: Int = ServiceLocator.get()
    println("From ServiceLocator -> s: $s, i: $i")
}

```


Q42. What are data classes used for?
- Auto generate equals/hashCode/toString/copy/componentN for value types.
- often used for DTOs, models, etc.
- often 
```kotlin
data class User(val id: Int, val name: String)
println(User(1, "A")) // toString auto
```

Q43. What are extension functions and their limitations?
- Static resolution; cannot access private members; cannot be overridden.
```kotlin
fun String.onlyVowels() = filter { it in "aeiou" }
```

Q44. Companion objects vs Java static; how to expose static to Java?
- companion object is singleton per class; 
  - TODO: how is it different from a static class in Java?
```kotlin
class Utils { companion object { @JvmStatic fun ping() = "pong" } }
```

Q45. What are sealed classes and why useful with when?
- sealed: stops subclassing outside file/module; 
  - use case: represent restricted hierarchies (e.g., Result, Expr).
```kotlin
sealed class Shape

class Circle(val radius: Double) : Shape()
class Rectangle(val width: Double, val height: Double) : Shape()
class Triangle(val base: Double, val height: Double) : Shape()

fun getArea(shape: Shape): Double = when (shape) {
  is Circle -> Math.PI * shape.radius * shape.radius
  is Rectangle -> shape.width * shape.height
  is Triangle -> 0.5 * shape.base * shape.height
}

fun main() {
  val circle = Circle(5.0)
  val rectangle = Rectangle(3.0, 4.0)
  val triangle = Triangle(2.0, 5.0)

  println(getArea(circle)) // Output: 78.53981633974483
  println(getArea(rectangle)) // Output: 12.0
  println(getArea(triangle)) // Output: 5.0
}
```

Q46. How do sealed classes pair with an exhaustive when?
- Compiler can ensure all subclasses are handled (no else needed in same file/module).

Q47. What are object expressions (anonymous objects)?
```kotlin
val listener = object : Runnable { override fun run() { println("Go") } }
```

Q48. What is a primary constructor?
- declared in class header
```kotlin
class Person(val name: String, val age: Int) {
    fun greet() {
        println("Hello, my name is $name and I'm $age years old.")
    }
}
```

Q49. What are secondary constructors and when to use them?
```kotlin
class Person { 
    var name = ""
    constructor(name: String){ 
        this.name = name 
    } 
}
```

Q50. init vs constructor: what runs when?
- init runs after the constructor for all construction paths (primary/secondary).
- init is used for common initialization logic. 
```kotlin
class Person(val name: String) {
  init { 
      println("Created person: $name") 
  }
  constructor(): this("Unknown") // calls primary, then init
}
```

Q51. Extension vs member functions: differences?
- Members can access private state and be open/override
- extensions are static helpers.
```kotlin
class Box(val value: Int) {
  fun double() = value * 2 // member
}
fun Box.triple() = value * 3 // extension
```

Q52. What are extension properties?
```kotlin
val String.initial: Char get() = first()
```

Q53. What are sealed interfaces?
- Restricted implementors like sealed classes but for interfaces.

Q54. What is operator overloading and how to define an operator function?
```kotlin
data class Vec(val x:Int,val y:Int){ operator fun plus(o:Vec)=Vec(x+o.x,y+o.y) }
```

Q55. What is the operator modifier for?
- Marks functions that implement operator semantics (+, -, [], etc.).

Q56. What does this refer to in Kotlin?
- The current receiver: class instance or extension receiver within a scope.

Q57. What is lateinit and when should you use it?
```kotlin
lateinit var name: String
// Must assign before access, reference types only
```

Q58. How to check if a lateinit property was initialized?
```kotlin
if (::name.isInitialized) println(name)
```

Q59. What is lazy initialization (by lazy) and when to use it?
```kotlin
val expensive: String by lazy { compute() }
```

Q60. lateinit vs lazy: key differences?
- lateinit: var, manual init, refs only, not thread‑safe by default.
- lazy: val, auto on first access, configurable thread‑safety, any type.

Q61. What are delegates (overview)?
- Reuse behavior via delegation (lazy/observable/vetoable/custom, and class delegation).

Q62. What does the by keyword do (class/property delegation)?
```kotlin
interface Printer { fun print(s:String) }
class Console: Printer { override fun print(s:String)=println(s) }
class Logger(pr: Printer): Printer by pr
```

Q63. What are delegated properties (lazy/observable/vetoable/custom)?
```kotlin
var name: String by kotlin.properties.Delegates.observable("") { _,old,new -> println("$old->$new") }
```

Q64. How do custom getters/setters work? What is the backing field field?
```kotlin
var age = 0
  get(){ println("get"); return field }
  set(v){ println("set $v"); field = v }
```

Q65. When are delegated properties beneficial?
- Cross‑cutting concerns (caching, validation, persistence) without cluttering the class.

Q66. What is the when expression and how is it used?
```kotlin
val text = when(val x = 3){ 1->"one" in 2..4->"few" else->"many" }
```

Q67. What does downTo do?
```kotlin
for(i in 5 downTo 1) print(i) // 54321
```

Q68. What does until do?
```kotlin
for(i in 1 until 4) print(i) // 123
```

Q69. What are coroutines in Kotlin?
```kotlin
import kotlinx.coroutines.*
runBlocking {
  launch { delay(100); println("A") }
  println("B")
}
```

Q70. What does the suspend modifier mean?
- Function can suspend; can be called from coroutines/other suspend functions.

Q71. What does withContext do and when to switch dispatchers?
```kotlin
suspend fun load(): String = withContext(Dispatchers.IO){ api.get() }
```

Q72. How do you handle exceptions in coroutines (launch vs async)?
- launch: handle via CoroutineExceptionHandler; async: exceptions surface on await(). Don’t swallow CancellationException.

Q73. What is a Flow and how to collect it?
```kotlin
fun numbers() = kotlinx.coroutines.flow.flow { repeat(3){ emit(it); delay(50) } }
runBlocking { numbers().collect { println(it) } }
```

Q74. How to synchronize threads with synchronized blocks?
```kotlin
class Counter { private var c=0; fun inc(){ synchronized(this){ c++ } } }
```

Q75. What does @JvmStatic do and where to use it?
- Expose companion/object members as true static for Java callers.

Q76. What does @JvmOverloads generate?
- Overloads for default params for Java interop.

Q77. What is @JvmName used for?
- Rename generated JVM names to match Java expectations.

Q78. What are SAM conversions in Kotlin/Java interop?
- Pass lambdas to Java functional interfaces seamlessly.

Q79. What visibility modifiers exist and what do they mean?
- public, internal (module), protected (class+subclasses), private (file/class/object).

Q80. What does internal mean exactly?
- Visible within the same module only.

Q81. What does protected mean and where is it valid?
- Only on class members; visible in class and subclasses.

Q82. What are inline functions and why use them?
- Reduce call overhead for small HOFs; enable reified generics.

Q83. What is tailrec optimization?
```kotlin
tailrec fun fact(n:Int, acc:Int=1): Int = if(n==0) acc else fact(n-1, acc*n)
```

Q84. How do reified type parameters work? (see also Q40)
- Reified + inline let you access T at runtime (T::class), useful for type checks and factories.

Q85. What does “inlining” mean in Kotlin?
- Compiler substitutes inline function body at call site.

Q86. What are type‑safe builders (DSLs)?
```kotlin
fun html(block: StringBuilder.() -> Unit) = StringBuilder().apply(block).toString()
val str = html { append("<p>"); append("Hi"); append("</p>") }
```

Q87. What are Kotlin Contracts conceptually?
- Let functions declare behavior (e.g., returns true implies parameter non‑null) to help compiler smart casts.

Q88. What are infix functions and how to declare/call one?
```kotlin
infix fun Int.add(x:Int) = this + x
val r = 2 add 3
```

Q89. What are higher‑order extension functions?
- Extensions that take/return functions; powerful for DSLs and pipelines.

Q90. What is typealias and when to use it?
```kotlin
typealias OnClick = () -> Unit
```

Q91. What are default arguments and how do they help?
```kotlin
fun greet(name:String = "World") = println("Hello, $name!")
```

Q92. What are top‑level functions and how to import them?
- Declared at file scope; import with their package name.

Q93. How does string interpolation work?
```kotlin
val who = "Kotlin"; println("Hi, $who! length=${who.length}")
```

Q94. Android: How do you set up a NavHostFragment and AppBar?
XML:
```xml
<fragment
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph" />
```
Activity wiring:
```kotlin
class MainActivity: AppCompatActivity() {
  private lateinit var appBarConfig: AppBarConfiguration
  override fun onCreate(b: Bundle?) {
    super.onCreate(b); setContentView(R.layout.activity_main)
    val nav = findNavController(R.id.nav_host_fragment)
    appBarConfig = AppBarConfiguration(setOf(R.id.homeFragment))
    setupActionBarWithNavController(nav, appBarConfig)
  }
  override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp(appBarConfig)
}
```

Q95. Android: How do you navigate with Safe Args and receive arguments?
```kotlin
// navigate
val action = HomeFragmentDirections.actionHomeToDetail(noteId = 42L)
findNavController().navigate(action)
// receive
class DetailFragment: Fragment(R.layout.fragment_detail) {
  private val args: DetailFragmentArgs by navArgs()
  override fun onViewCreated(v: View, s: Bundle?) { println(args.noteId) }
}
```

Q96. Android: How do you share a ViewModel across a navigation graph and use SavedStateHandle?
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(private val state: SavedStateHandle): ViewModel() {
  val username = state.getStateFlow("username", "")
}
class LoginFragment: Fragment(R.layout.fragment_login) {
  private val vm: AuthViewModel by navGraphViewModels(R.id.auth_graph)
}
```

Q97. Android: How do you add an OkHttp logging interceptor to Retrofit, and what does create() do?
```kotlin
val client = OkHttpClient.Builder()
  .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
  .build()
val retrofit = Retrofit.Builder()
  .baseUrl("https://pokeapi.co/api/v2/")
  .addConverterFactory(GsonConverterFactory.create())
  .client(client)
  .build()
val service = retrofit.create(PokemonService::class.java) // generates a dynamic proxy that implements the interface
```

Q98. Android: What’s the Fragment view vs fragment lifecycle, and where to observe LiveData/Flow?
```kotlin
class FooFragment: Fragment(R.layout.fragment_foo) {
  private var _binding: FragmentFooBinding? = null
  private val binding get() = _binding!!
  override fun onViewCreated(v: View, s: Bundle?) {
    _binding = FragmentFooBinding.bind(v)
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { vm.flow.collect { /* update UI */ } }
    }
  }
  override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
```

Q99. Android: How do you use the Activity Result API?
```kotlin
private val pick = registerForActivityResult(ActivityResultContracts.GetContent()) { uri -> /* handle */ }
// call
button.setOnClickListener { pick.launch("image/*") }
```

Q100. Android: How do you enqueue a simple WorkManager job?
```kotlin
class SyncWorker(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
  override suspend fun doWork(): Result { /* sync */ return Result.success() }
}
WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
```

Q101. Android: How do you set up Room quickly (Entity/Dao/DB) and query with Flow?
```kotlin
@Entity data class Note(@PrimaryKey val id: Long, val text: String)
@Dao interface NoteDao { @Query("SELECT * FROM Note") fun all(): Flow<List<Note>> }
@Database(entities=[Note::class], version=1) abstract class AppDb: RoomDatabase() { abstract fun noteDao(): NoteDao }
```

Q102. Android: Which coroutine scopes do you use in ViewModel vs Fragment?
- ViewModel: viewModelScope for business logic. Fragment: viewLifecycleOwner.lifecycleScope for UI work tied to the view.

Q103. Android: How do you clear back stack after login with Navigation?
```kotlin
findNavController().navigate(
  R.id.homeFragment,
  null,
  navOptions { popUpTo(R.id.auth_graph) { inclusive = true }; launchSingleTop = true }
)
```

---