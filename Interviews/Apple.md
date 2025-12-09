# Apple Android Architect Interview

Android Architect
Location: Cupertino, CA/Seattle, WA/Austin, TX- (Onsite)
Type: Contract

In-person interview will be there for final round.

Client: Infosys/Apple

Job description :

Key Responsibilities
Define and evolve the architecture for complex Android applications.
Lead development of scalable, maintainable, and modular mobile solutions.
Collaborate with product and design teams to translate business requirements into technical specifications.
Review code and provide guidance to Android developers on best practices, code quality, and performance optimization.
Establish architectural guidelines, patterns (MVVM, MVI, Clean Architecture), and coding standards across Android projects.
Advocate for modern development practices such as dependency injection (Dagger/Hilt), Jetpack Compose, and Kotlin coroutines.
Integrate with RESTful APIs, real-time services, and cloud-based backends (Firebase, AWS, etc.).
Own performance monitoring, debugging, and crash analytics strategies.
Stay up to date with Android platform changes and recommend enhancements accordingly.

Required Qualifications

Bachelor's or Master s degree in Computer Science, Engineering, or a related field.
8+ years of Android development experience, with at least 2+ years in an architect or technical lead role.
Expert in Kotlin and Android SDK.
Strong understanding of architectural patterns: MVVM, MVI, Clean Architecture.
Hands-on experience with Jetpack libraries, Compose, Room, Navigation, etc.
Proficiency in Dependency Injection using Dagger or Hilt.
Solid understanding of multi-module architecture, modularization, and app scalability.
Familiarity with CI/CD, Gradle build optimizations, unit testing, and instrumentation testing.


## S3 Interviews
1. scope functions, what are their return types, when to use which one
  - apply and also return the object itself
  - let, run, with return the lambda result 

2. experience with room database, how to do migrations
  - 2 approachs
    - destructive migration - delete and recreate -> lose user data (not for production)
    - add nullable columns, create new tables, copy data over, drop old tables
      - eventually non-null in future versions 
    - also have migration scripts that define how to migrate from one version to another
    - also have automatic migration for simple changes (add column, rename column, etc) - has been added in recent versions of Room ()
- In Room, this is done with Migration objects that contain SQL scripts (or calls) describing how to transform the schema and data
ANSWER:
  When I change my Room schema, I don’t want to drop the DB and lose user data, so I define explicit migration scripts. In Room, that means creating Migration(from, to) objects and implementing the migrate method with SQL. 
  For example, if I add a new column, I might do an ALTER TABLE to add a nullable column and then optionally backfill existing rows.
  I register these migrations in Room.databaseBuilder via addMigrations(...). Room will then automatically run the right sequence of migrations, e.g. 1 → 2 → 3, when the app updates.
  In development I might use fallbackToDestructiveMigration, but in production I prefer explicit migrations and I write migration tests using the exported schema to ensure I can upgrade safely through all intermediate versions.

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Schema change
        database.execSQL(
            "ALTER TABLE User ADD COLUMN lastLogin INTEGER"
        )
        // 2. Optional: backfill / default values
        database.execSQL(
            "UPDATE User SET lastLogin = strftime('%s','now') WHERE lastLogin IS NULL"
        )
    }
}

// In your Room setup:
Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app.db"
)
    .addMigrations(MIGRATION_1_2)
    .build()
```

3. coding challenge
```kotlin
// given a list of integers, return the duplicates and their counts 
// e.g. [1,2,2,3,4,4,4] -> {2:2, 4:3}

fun main() {
    val numbers = listOf(1, 2, 2, 3, 4, 4, 4)
    val duplicates = findDuplicates(numbers)
    println(duplicates) // Output: {2=2, 4=3}
}

fun findDuplicates(numbers: List<Int>): Map<Int, Int> {
    val counts = mutableMapOf<Int, Int>()
    for (number in numbers) {
        counts[number] = counts.getOrDefault(number, 0) + 1
    } 
    /* Option using forEach
    numbers.forEach { number ->
        counts[number] = counts.getOrDefault(number, 0) + 1
    }
     */
    
    return counts.filter { it.value > 1 }
}
```

4. create a compose screen with a list of items and 2 buttons at the bottom

```kotlin

@Composable
fun ItemListScreen(items: List<String>, onButton1Click: () -> Unit, onButton2Click: () -> Unit) {
    
    val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Divider()
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onButton1Click) {
                Text("Button 1")
            }
            Button(onClick = onButton2Click) {
                Text("Button 2")
            }
        }
    }
}
```

5. how would you create a singleton class in kotlin
- use the object keyword or companion object inside a class

6. have you used DI frameworks like dagger or hilt
- annotations like @Inject, @Module, @Provides, @Singleton, @HiltAndroidApp, etc.
- @Binds vs @Provides
  - @Binds is used to bind an interface to its implementation. It must be an abstract function inside an abstract class annotated with @Module. It is more efficient because it generates less code.
  - @Provides is used to provide instances of classes. It can be used in regular classes annotated with @Module. It can contain logic to create the instance.

7. difference between coroutine scope and lifecycle scope
  - CoroutineScope:
    - A general-purpose scope for launching coroutines.
    - Not tied to any specific lifecycle.
    - You need to manage its lifecycle manually (cancel when no longer needed).
    - Example: CoroutineScope(Dispatchers.IO).launch { ... }

  - LifecycleScope:
    - A specialized CoroutineScope tied to the lifecycle of a component (Activity/Fragment).
    - Automatically cancels coroutines when the lifecycle is destroyed (e.g., onDestroy).
    - Helps prevent memory leaks by ensuring coroutines don’t outlive their context.
    - Example: lifecycleScope.launch { ... } in an Activity or Fragment.

8. Data dog vs splunt mint sdk
   - Datadog:
       - like firebase analytics/crashlytics but more enterprise-focused and more backend-focused
         - it is more -> end-to-end observability (not just mobile)
         - correlates mobile events with backend APM (Application Performance Monitoring) and infrastructure metrics
           - Example: trace a slow screen → slow API → specific microservice → DB query.
       - Full observability platform: logs, metrics, traces, RUM (Real User Monitoring).
       - Mobile SDK for Android/iOS to track:
           - Crashes and errors
           - Performance (startup time, slow renders, ANRs)
           - Network requests, backend latency, error rates
           - User sessions, views, and custom events
       - Strong integration with backend tracing (APM) and infrastructure metrics.
       - Good for end\-to\-end tracing: mobile → API → services → DB.

   - Splunk Mint SDK:
       - Legacy mobile monitoring SDK focused mainly on:
           - Crash reporting
           - Basic performance metrics
           - Simple user analytics
       - Less feature\-rich and not as tightly integrated with modern distributed tracing.
       - Often considered more "point solution" vs Datadog’s full observability stack.

9. Explain ViewModel or Usecase of ViewModel
  - ViewModel Core Advantages:
    - Survives configuration changes (screen rotations) by storing data outside Activity/Fragment lifecycle
    - Scoped to Activity/Fragment via ViewModelProvider – automatic cleanup prevents leaks
    - Promotes MVVM separation: UI logic in ViewModel, not in Views 
  - Data Persistence & State Management:
    - Holds UI state (StateFlow/LiveData) that restores on process death with SavedStateHandle

10. LiveData vs StateFlow
  - Use StateFlow for new Compose/MVI projects: 
    - expose StateFlow from repositories/ViewModels, 
    - collect in LaunchedEffect with lifecycle scoping for your multi-module apps.
  - When to Use StateFlow (Your Compose/MVVM projects):
    - New apps with Jetpack Compose and coroutines
    - Multi-module architectures needing data transformations 
    - Scaling to millions: efficient emissions prevent UI jank
    - MVI/Unidirectional Data Flow patterns

  - When to Keep LiveData:
    - lifecycle-aware data binding in XML layouts 
    - Legacy XML-based codebases (avoid migration cost)
    - Simple observe-use cases without complex operators
    - Teams unfamiliar with Kotlin Flows

11. What feature you developed in your current project 
  - Home screen with Jetpack Compose
  - Sign-in with KMM
  - Profiling and macro benchmarking image performance

12. MVVM Architecture

13. Clean Architecture 
  - Layers: Presentation (UI, ViewModel), Domain (UseCases, business logic), Data (Repositories, data sources)
  - Dependency Rule: Inner layers don’t depend on outer layers
  - Benefits: Testability, maintainability, separation of concerns
  - Modules: Separate modules for each layer for scalability

14. How can you implement offline support feature in android app 
  - Use Room database for local caching
  - Use WorkManager for background sync when online
  - Use NetworkBoundResource pattern to manage data flow between network and local cache
    - NetworkBoundResource:
      - Load data from local database first
      - If data is stale or missing, fetch from network
      - Save network response to local database
      - Expose data as Flow/LiveData to UI
  - Use Retrofit with OkHttp caching for network requests

15. what will you do if there is no cache data is available in app when the internet is off 
  - Show a user-friendly message indicating no data is available offline
  - Provide an option to retry fetching data when the internet is back
  - Use placeholder UI elements to indicate loading state
  - Log the event for analytics to understand offline usage patterns

16. Difference between Koin and Hilt 
  - Hilt:
    - Built on top of Dagger, uses compile-time code generation
    - More boilerplate, but better performance and type safety
    - Requires annotations like @Inject, @Module, @Component
    - Better suited for large projects with complex dependency graphs
  - Koin:
    - Lightweight, uses runtime reflection (runtime dependency injection framework)
    - Less boilerplate, easier to set up and use
    - Uses DSL for defining modules and dependencies 
      - DSL (Domain-Specific Language) allows you to define dependencies in a more readable way using Kotlin syntax
    - More flexible for small to medium projects or prototyping

17. What are the core components of koin? 
  - Modules: Define how to create and provide dependencies
  - Singletons: Define single instance dependencies
  - Factories: Define new instance dependencies
  - Scopes: Define lifecycle-aware dependencies
  - StartKoin: Initialize Koin in the application
```kotlin
// Example Koin module
val appModule = module {
    single { MyRepository() } // Singleton
    factory { MyViewModel(get()) } // New instance each time
}
// Starting Koin in Application class
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}
```

18. When we should not use ViewModel 
  - For short-lived UI components that don’t need to survive configuration changes (e.g., dialogs, toasts)
  - When the data is not UI-related or does not need to be retained across configuration changes
  - For simple data that can be managed directly in the Activity/Fragment without complexity
  - When using other state management solutions that better fit the use case (e.g., for global app state)

19. How to make flow as lifecycle aware 
  - Use lifecycleScope to launch coroutines tied to the lifecycle
  - Use repeatOnLifecycle to collect flows only when the lifecycle is at least in a certain state (e.g., STARTED)
  - use collectAsStateWithLifecycle() extension function in Compose to automatically manage collection based on lifecycle

20. If you have team of 5 people, how did you distribute the work 
  - Divide work based on expertise: UI, backend integration, database, testing, etc.
  - Assign clear responsibilities and ownership for each module or feature
  - Use version control branching strategies (e.g., feature branches) to manage parallel development
  - Conduct regular code reviews and sync meetings to ensure alignment and quality
  - Encourage collaboration and knowledge sharing among team members

22. How is the memory model guarantee the threat safety when switching the dispatchers? 
  - Kotlin Coroutines use a structured concurrency model that ensures thread safety by confining coroutines to specific dispatchers.
  - When switching dispatchers (e.g., from IO to Main), the coroutine context is preserved, and the state is safely transferred between threads.
  - The memory model guarantees that shared mutable state is accessed in a thread-safe manner, preventing data races.
    - data races occur when multiple threads access shared data concurrently without proper synchronization, leading to unpredictable results.

23. Frequently in the same coroutine, how could you make this possible? 
  - You can switch dispatchers within the same coroutine using withContext(). 
  - This allows you to change the execution context for specific blocks of code while maintaining the overall coroutine scope.

24. When coming to the inline functions, how does kotlin handle the inline functions? 
  - Kotlin handles inline functions by replacing the function call with the actual code of the function at compile time. 
  - This reduces the overhead of function calls, especially for higher-order functions that take lambdas as parameters.
  - Inline functions can also improve performance by allowing non-local returns from lambdas and enabling reified type parameters.

25. When coming to the Jetpack Compose, walk me through the three important phases of Compose. Composition, Layout, and Draw. 
  - Composition:
    - This is the first phase where the UI tree is built. 
    - Composable functions are called, and the UI elements are created based on the current state. 
    - The composition phase determines what to display on the screen.
  - Layout:
    - In this phase, the size and position of each UI element are calculated. 
    - The layout system measures the composables and determines how much space they need based on constraints from parent composables.
    - This phase ensures that each element is properly arranged within the available space.
  - Draw:
    - The final phase where the actual rendering of UI elements occurs. 
    - The draw phase takes the layout information and paints the UI elements onto the screen. 
    - This phase is responsible for the visual representation of the UI.

26. when coming to the UDF, this explains how UDF works in a compose-based architecture and why bidirectional state flow seems to be dangerous.
  - UDF (Unidirectional Data Flow) in Compose-based architecture means that data flows in a single direction: from the ViewModel to the UI.
  - Events from the UI (like user interactions) are sent back to the ViewModel, which updates the state and triggers recomposition.
  - Bidirectional state flow can be dangerous because it can lead to tight coupling between the UI and the ViewModel, making it harder to manage state changes and increasing the risk of inconsistent UI states.
  - UDF promotes a clear separation of concerns, making the architecture more predictable and easier to debug.

27. What is an SDK?
    - SDK stands for Software Development Kit. It is a collection of tools, libraries, documentation, and code samples that developers use to create applications for specific platforms or frameworks.
    - An SDK typically includes APIs (Application Programming Interfaces) that allow developers to interact with the underlying platform or service, as well as utilities for building, testing, and debugging applications.
    - Examples include the Android SDK for building Android apps (Activities, Views, networking, etc.) and the iOS SDK for building iOS apps.

28. When to use NDK/JNI in Android?
    - Use NDK (Native Development Kit) and JNI (Java Native Interface) when you need to:
      - Improve performance for compute-intensive tasks (e.g., game engines, image processing, physics simulations).
      - Reuse existing C/C++ libraries or codebases in your Android app.
      - Access low-level system features or hardware that are not available through the standard Android SDK.
      - Implement performance-critical components that require fine-grained control over memory and CPU usage.
    - However, using NDK/JNI adds complexity to the project, so it should be used judiciously and only when necessary.