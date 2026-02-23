1. repositoryLayer vs the UseCases?
> Repository is the data access layer, responsible for fetching and caching data from various sources (network, database, etc.) and exposing it to the domain layer.
> It abstracts away the details of data retrieval and storage.

> UseCases (or Interactors) are part of the domain layer and represent specific business actions or operations that can be performed.
> They orchestrate the flow of data between the repository and the UI, applying business rules and logic as needed.
> UseCases typically call one or more repository methods to get the data they need to execute their logic.
> Examples like filtering, sorting, combining data from multiple repositories, or validating inputs before passing them to the repository would be handled in the UseCase layer, not the repository itself.

2. How do we test our viewmodel?
> To test a ViewModel, you can use JUnit for unit testing and Mockito or MockK for mocking dependencies.
> You want to setup either fake or mocked versions of your repositories or use cases that the ViewModel depends on, so you can control their behavior and test the ViewModel in isolation.
> mocks test behavior (verify that certain methods were called with specific arguments), while fakes provide a working implementation that can be used for more realistic testing.

3. what can cause the app to have slow start up times?
> Slow startup times can be caused by:
> - Heavy initialization in the Application class (e.g. initializing large libraries, doing network calls, loading images on the main thread, running heavy computation on main thread, or loading large data sets on the main thread).
> - Running too much code in the `onCreate` of the main Activity, especially if it’s doing work on the main thread.
> - Not using lazy initialization for components that aren’t needed immediately.
> - Not using baseline profiles or AOT compilation, which can lead to more JIT compilation at startup.
> - Loading large resources (e.g. images, fonts) synchronously on the main thread during startup.
> - Not using splash screens or placeholders to give feedback while loading.

4. What is CLEAN and how is it good in testing?
> CLEAN architecture is a software design pattern that emphasizes separation of concerns and independence of layers.
> It typically consists of:
> - **Entities**: Core business objects that are independent of any framework or technology.
> - **Use Cases**: Application-specific business rules that orchestrate the flow of data and implement the core logic of the application.
> - **Interface Adapters**: Convert data from the format most convenient for the use cases and entities to the format most convenient for external agencies such as databases, web services, or the UI.
> - **Frameworks and Drivers**: The outermost layer that includes frameworks and tools such as the UI, database, web frameworks, etc.
    > The main benefit of CLEAN architecture in testing is that it allows you to test the core business logic (entities and use cases) in isolation from the UI and external dependencies.
    > You can easily mock the interface adapters and frameworks when testing the use cases, which leads to faster and more reliable tests.
    > It also promotes better organization and maintainability of the codebase, making it easier to understand and modify over time.

5. what can cause ANR issues in android?
> ANR (Application Not Responding) issues occur when the main thread of an Android application is blocked for too long, typically more than 5 seconds.
> Common causes of ANR include:
> - Performing long-running operations (e.g. network calls, database queries, file I/O) on the main thread.
> - Deadlocks or synchronization issues that block the main thread.
> - Infinite loops or excessive computations on the main thread.
> - Not responding to system events (e.g. not handling broadcast receivers or services properly).
> - Heavy initialization in the `onCreate` of the main Activity or Application class.
> - Not using background threads or coroutines for tasks that should be off the main thread.

6. What is a result class in Android?
> A Result class is a common pattern used to represent the outcome of an operation that can either succeed or fail.
> It typically has two states:
> - Success: Contains the successful result of the operation (e.g. data, response).
> - Failure: Contains information about the error that occurred (e.g. exception, error message).
    > The Result class allows you to handle success and failure cases in a more structured way, often using sealed classes or a generic wrapper.
    > It can be used in repositories, use cases, or any layer where you want to represent the outcome of an operation that may fail, such as network requests, database operations, or any business logic that can encounter errors.

7. result vs sealed class?
> A Result class is a specific implementation of a sealed class that is designed to represent the outcome of an operation that can either succeed or fail.
> the Result class is built to be used generically across the codebase to represent success and failure outcomes of specific operations like network calls, database operations, or any business logic that can encounter errors.
> A sealed class is a more general concept in Kotlin that allows you to define a closed hierarchy of classes.
> You can use a sealed class to represent any kind of state or outcome, not just success or failure.
> For example, you could have a sealed class that represents the state of a UI component (e.g. Loading, Success, Error) or the different types of events in an event-driven architecture.
> The Result class is a specific use case of a sealed class that focuses on representing success and failure outcomes, while a sealed class can be used for a wider range of scenarios.

8. how do you make the state immutable?
> To make state immutable in Android, you can use data classes and expose only read-only properties.
> For example, you can define a data class to represent the state of your UI, and only provide getters for the properties, without any setters or mutable references.
> You can also use `val` instead of `var` to ensure that the properties cannot be reassigned after initialization.
> In a ViewModel, you can use `LiveData` or `StateFlow` to expose the state to the UI, and only update the state internally within the ViewModel using private mutable properties.
> This way, the UI can observe the state but cannot modify it directly, ensuring immutability from the perspective of the UI layer.

9. .update instead of .value in stateflows?
> The `.update` function is a convenient extension function for `MutableStateFlow` that allows you to update the value of the state flow in an atomic way.
> When you call `.update`, it takes a lambda that receives the current value of the state flow and returns the new value.
> This is useful because it ensures that the update is thread-safe and avoids potential race conditions that can occur when multiple threads are trying to update the state flow at the same time.
> .value is a direct assignment to the state flow, which can lead to race conditions if multiple threads are updating the state flow simultaneously.

10. different types to handle race conditions for coroutines / threads?
> To handle race conditions in coroutines or threads, you can use various synchronization mechanisms such as:
> - Mutex: A mutual exclusion lock that allows only one coroutine or thread to access a critical section of code at a time.
> - Atomic variables: Use `AtomicInteger`, `AtomicBoolean`, etc. from the `java.util.concurrent.atomic` package to perform atomic operations on shared variables without the need for locks.
> - Channels: Use Kotlin Coroutines Channels to communicate between coroutines and ensure that only one coroutine is processing a certain piece of data at a time.
> - Actors: Use the actor model in Kotlin Coroutines to encapsulate state and ensure that only one coroutine can access the state at a time by sending messages to the actor.
> - Synchronized blocks: In Java, you can use `synchronized` blocks to ensure that only one thread can access a critical section of code at a time, although this is less common in Kotlin with coroutines.
> - Coroutines with structured concurrency: Use `withContext` to switch to a specific dispatcher (e.g. `Dispatchers.IO`) for certain operations, and ensure that you are not blocking the main thread or shared resources.
> - Using `StateFlow` or `SharedFlow` with proper scoping and lifecycle management to ensure that updates to shared state are handled in a thread-safe manner.

11. How do you test coroutines?
> To test coroutines, you can use the `kotlinx-coroutines-test` library, which provides tools for testing coroutine-based code.
> You can use `runBlockingTest` to run your test code in a coroutine context, and use `TestCoroutineDispatcher` to control the execution of coroutines and simulate delays or timeouts.
> You can also use `advanceTimeBy` to simulate the passage of time and test time-dependent code, and `runCurrent` to execute any pending coroutines immediately.
> Additionally, you can use `TestCoroutineScope` to manage the lifecycle of your test coroutines and ensure that they are properly cleaned up after the test runs.
> When testing ViewModels or other classes that use coroutines, you can inject a `TestCoroutineDispatcher` or `TestCoroutineScope` to control the execution of coroutines and make your tests deterministic and reliable.
> You can also use libraries like `MockK` or `Mockito` to mock dependencies and verify that certain coroutine-based methods were called with the expected arguments.
> NOTE: MockK has built-in support for coroutines, allowing you to easily mock suspend functions and verify their behavior in your tests.

12. What do you use rules in testing?
> Rules in testing are used to set up and tear down the test environment before and after each test runs.
> They can be used to provide common setup code, manage resources, or apply specific configurations for your tests.
> For example, in Android testing, you can use `ActivityTestRule` to launch an activity before each test and close it afterward, or `InstantTaskExecutorRule` to ensure that LiveData updates happen synchronously during tests.
> Rules help to reduce boilerplate code and ensure that your tests are consistent and isolated, as they can handle common setup and teardown tasks automatically.

13. How do you test your Composables?
> To test Composables, you can use the `androidx.compose.ui.test` library, which provides tools for testing Jetpack Compose UI components.
> You can use `createComposeRule` to create a test rule for your Composable tests, and use `setContent` to set the Composable content that you want to test.
> You can then use various testing APIs to interact with the UI and verify its behavior, such as `onNodeWithText`, `onNodeWithTag`, `performClick`, and `assertIsDisplayed`.
> You can also use `Semantics` properties to provide additional information about your Composables, which can help with testing and accessibility.
> When testing Composables, you can also use `TestCoroutineDispatcher` to control the execution of any coroutines used within your Composables, and use `runBlockingTest` to run your tests in a coroutine context if needed.
> Additionally, you can use `MockK` or `Mockito` to mock any dependencies that your Composables might have, such as ViewModels or repositories, to ensure that your tests are focused on the UI behavior rather than the underlying logic.

14. How do you test your XML views?
> To test XML views, you can use the Android Testing Support Library, which provides tools for testing Android UI components.
> You can use `ActivityTestRule` to launch the activity that contains the XML views you want to test, and then use `Espresso` to interact with the UI and verify its behavior.
> With Espresso, you can use methods like `onView`, `withId`, `perform`, and `check` to find views, perform actions (e.g. click, type text), and assert conditions (e.g. is displayed, has text).
> You can also use `ViewMatchers` to find views based on various criteria, such as text, content description, or position in the view hierarchy.
> When testing XML views, you can also use `TestCoroutineDispatcher` to control the execution of any coroutines used within your activity or fragments, and use `runBlockingTest` to run your tests in a coroutine context if needed.
> Additionally, you can use `MockK` or `Mockito` to mock any dependencies that your activity or fragments might have, such as ViewModels or repositories, to ensure that your tests are focused on the UI behavior rather than the underlying logic.
> NOTE: When testing XML views, it's important to ensure that your tests are not flaky and can run reliably across different devices and configurations.

inner vs internal class
> An inner class in Kotlin is a nested class that has access to the members of its outer class. It is declared with the `inner` keyword and can access the properties and functions of the outer class. An inner class holds a reference to an instance of the outer class, which allows it to interact with the outer class's members.
> An internal class in Kotlin is a class that is visible within the same module. It is declared with the `internal` keyword and can be accessed from any code within the same module, but not from outside the module. An internal class does not have access to the members of any outer class, as it is not nested within another class.
> differences:
> - An inner class can access the members of its outer class, while an internal class cannot.
> - An inner class holds a reference to an instance of the outer class, while an internal class does not.
> - An inner class is declared with the `inner` keyword, while an internal class is declared with the `internal` keyword.

protected class vs internal class
> A protected class in Kotlin is a class that is visible to its subclasses and to other classes in the same package. It is declared with the `protected` keyword and can be accessed from within the class itself, from its subclasses, and from other classes in the same package. However, it cannot be accessed from outside the package or from non-subclass classes.
> An internal class in Kotlin is a class that is visible within the same module. It is declared with the `internal` keyword and can be accessed from any code within the same module, but not from outside the module. An internal class does not have access to the members of any outer class, as it is not nested within another class.
> differences:
> - A protected class can be accessed from its subclasses and from other classes in the same package, while an internal class can be accessed from any code within the same module.
> - A protected class is declared with the `protected` keyword, while an internal class is declared with the `internal` keyword.

where would you use flow and
where would you use livedata

what is back pressure handling
> Backpressure handling is a mechanism to manage the flow of data in a reactive stream when the producer is emitting items faster than the consumer can process them.
It helps to prevent overwhelming the consumer and allows it to handle the incoming data at its own pace.
In Kotlin Coroutines, Flow provides built-in support for backpressure handling through operators like `buffer`, `conflate`, and `collectLatest`, which allow you to control how the flow of data is managed when there is a mismatch between the producer and consumer speeds.
> For example, `buffer` allows you to buffer a certain number of items before processing them, `conflate` keeps only the latest item and drops the previous ones if the consumer is slow, and `collectLatest` cancels the previous collection if a new item is emitted before the previous one is processed.

what is debounce
> Debounce is a technique used to limit the rate at which a function is executed. It ensures that a function is only called after a certain amount of time has passed since the last time it was invoked.
In Kotlin Coroutines, you can use the `debounce` operator on a Flow to achieve this behavior.
For example, if you have a search input field and you want to perform a search operation only after the user has stopped typing for a certain period of time, you can use `debounce` to delay the search operation until the user has finished typing, preventing unnecessary searches and improving performance.
> The `debounce` operator takes a time duration as a parameter and will emit the latest value from the upstream flow only if there has been no new value emitted for that duration. This is particularly useful for handling user input or events that may occur in rapid succession, allowing you to reduce the number of operations performed and improve the responsiveness of your application.
> For example, if you have a search input field and you want to perform a search operation only after the user has stopped typing for a certain period of time, you can use `debounce` to delay the search operation until the user has finished typing, preventing unnecessary searches and improving performance.


what are the different dispatchers

flatmap latest, what does it do
> `flatMapLatest` is an operator in Kotlin Coroutines that transforms the items emitted by a flow into another flow, and emits the latest values from the most recently transformed flow.
When a new item is emitted by the upstream flow, `flatMapLatest` cancels the previous flow and starts collecting from the new flow.
This is particularly useful when you want to switch to a new data source or operation based on user input or events, and you only care about the latest result.
For example, if you have a search input field and you want to perform a search operation based on the user's input, you can use `flatMapLatest` to switch to a new search operation each time the user types something new, ensuring that you only get results for the latest input.
> `flatMapLatest` is a combination of `flatMap` and `switchMap` from other reactive libraries. It allows you to transform each emitted item into a new flow, and ensures that only the latest flow is collected, canceling any previous flows that are still active. This can help to improve performance and responsiveness in scenarios where you want to react to user input or events that may occur in rapid succession.
> For example, if you have a search input field and you want to perform a search operation based on the user's input, you can use `flatMapLatest` to switch to a new search operation each time the user types something new, ensuring that you only get results for the latest input.
> This is particularly useful in scenarios like autocomplete search, where you want to provide real-time feedback to the user based on their input, and you want to ensure that you are only processing the most recent input to avoid unnecessary work and improve the user experience.
> In summary, `flatMapLatest` allows you to transform emitted items into new flows and ensures that only the latest flow is collected, making it ideal for scenarios where you want to react to user input or events that may occur in rapid succession, such as search operations or real-time updates.

what are channels and where would we use it

What happens when you start from one activity and you go to another activity

what is the difference between a hot flow and a cold flow

Explain to me what feetures you worked on recently

why do we use usecases

why would we use datastore over sharedpreferences

how would you test composables?
> To test Composables, you can use the `androidx.compose.ui.test` library, which provides tools for testing Jetpack Compose UI components.
> You can use `createComposeRule` to create a test rule for your Composable tests, and use `setContent` to set the Composable content that you want to test.
> You can then use various testing APIs to interact with the UI and verify its behavior, such as `onNodeWithText`, `onNodeWithTag`, `performClick`, and `assertIsDisplayed`.
> You can also use `Semantics` properties to provide additional information about your Composables, which can help with testing and accessibility.
> - example: you can set a `testTag` on your Composable and then use `onNodeWithTag` in your tests to find that Composable and perform actions or assertions on it.
    > When testing Composables, you can also use `TestCoroutineDispatcher` to control the execution of any coroutines used within your Composables, and use `runBlockingTest` to run your tests in a coroutine context if needed.
    > Additionally, you can use `MockK` or `Mockito` to mock any dependencies that your Composables might have, such as ViewModels or repositories, to ensure that your tests are focused on the UI behavior rather than the underlying logic.


Explain some design patterns

how do you architect a large scale application?

What if the app as 2 complex screen states to manage?
- MVI is preferred for complex screen states as it provides a clear separation of concerns and a unidirectional data flow, which can help manage the complexity of the UI state.

How does clean architecture help in maintainability and testing?
> Clean architecture promotes separation of concerns and independence of layers, which makes it easier to maintain and test the codebase.
> By isolating the business logic in the use cases and entities, you can test the core functionality of the application without relying on the UI or external dependencies.
> It also allows for easier modifications and additions to the codebase, as changes in one layer do not affect other layers, leading to better maintainability over time.

how do you handle configuration changes in Android?
> can use configuration change API - `onConfigurationChanged` in the activity to handle specific configuration changes without restarting the activity.
> Use ViewModel to retain data across configuration changes, as ViewModels are designed to survive configuration changes and can hold the UI-related data that needs to be preserved.
> Use `savedInstanceState` to save and restore small amounts of data during configuration changes, such as the current state of the UI or user input.
> Use `android:configChanges` in the manifest to specify which configuration changes you want to handle manually, but this is generally not recommended as it can lead to more complex code and potential issues if not handled correctly.
> Use `rememberSaveable` in Jetpack Compose to save and restore state across configuration changes in a more composable way.

how do you combine multiple api calls in a repository?
> You can combine multiple API calls in a repository using Kotlin Coroutines and the `async` and `await` functions to perform the calls concurrently.
> For example, you can use `coroutineScope` to create a new coroutine scope and then use `async` to start multiple API calls in parallel.
> You can then use `await` to get the results of each API call once they are completed, and combine the results as needed before returning them from the repository.
> This approach allows you to optimize the performance of your API calls by making them concurrently, while still maintaining a clean and organized code structure in your repository.
> you have merge, combine, zip operators in flow that can also be used to combine multiple flows of data from different API calls in a reactive way.

difference between channel and flow
> A Channel in Kotlin Coroutines is a non-blocking primitive for communication between coroutines. It allows you to send and receive values between coroutines in a thread-safe way, and can be used for scenarios where you need to implement producer-consumer patterns or manage a stream of events. Channels can be buffered or unbuffered, and they support various operations such as `send`, `receive`, and `close`.
> A Flow in Kotlin Coroutines is a cold asynchronous data stream that emits values sequentially. It is designed for handling streams of data that can be computed asynchronously, and it provides a rich set of operators for transforming, combining, and managing the flow of data. Flows are built on top of coroutines and can be collected to receive the emitted values. Unlike Channels, Flows are not meant for direct communication between coroutines, but rather for representing a stream of data that can be observed and manipulated.
> differences:
> - Channels are used for communication between coroutines, while Flows are used for representing asynchronous data streams.
> - Channels can be buffered or unbuffered, while Flows are always cold and do not emit values until collected.
> - Channels support operations like `send` and `receive`, while Flows provide a wide range of operators for transforming and managing data streams.
> - Channels can be closed to signal that no more values will be sent, while Flows do not have a built-in mechanism for completion and rely on the collector to handle the end of the stream.
> - Channels are more suitable for scenarios where you need to implement producer-consumer patterns or manage a stream of events, while Flows are better suited for handling streams of data that can be computed asynchronously and manipulated with various operators.
> - Channels can be used for both one-to-one and one-to-many communication between coroutines, while Flows are typically used for one-to-many scenarios where multiple collectors can observe the same stream of data. this is because a Channel will be consumed by a single receiver, while a Flow can be collected by multiple collectors, allowing for more flexible data flow management in your application.

how do you ensure thread safety?
> StateFlow over LiveData, use of `update` instead of direct assignment to ensure atomic updates, using `Mutex` for critical sections, using `Atomic` variables for shared state, and ensuring that you are not blocking the main thread with long-running operations.
> Additionally, you can use `withContext` to switch to a specific dispatcher for certain operations, and use `StateFlow` or `SharedFlow` with proper scoping and lifecycle management to ensure that updates to shared state are handled in a thread-safe manner.
> It's also important to avoid shared mutable state whenever possible, and to use immutable data structures or encapsulate mutable state within a single thread or coroutine to prevent race conditions.
> mutex, atomic variables, channels, actors, synchronized blocks, and structured concurrency with coroutines are all tools that can help you ensure thread safety in your Android applications.
