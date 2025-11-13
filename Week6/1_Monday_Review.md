# Task
- learn 2 things that we taught each other last week

- MVI Architecture Pattern + CLEAN Architecture

- Common Modules
  - use Generics
    - Retrofit will not take a specific data model class, but a generic type T
    - Repository will also use generic type T
    - because we have multiple different APIs that return different data models

```kotlin
sealed class ResponseState<out T> {
    object Loading : ResponseState<Nothing>() // Nothing means no data, similar to Void / Unit
    data class Success<out T>(val data: T) : ResponseState<T>()
    data class Error(val message: String) : ResponseState<Nothing>()
}

ResponseState<User>
ResponseState<List<Post>>
// etc.
```


## Notes
- New module
  - Phone and Tablet -> new app
  - Android Library module -> shared code with the app (view models, repositories, data sources, utils, models)
  - Android Native module -> shared code with iOS (repositories, data sources, utils, models)
  - Dynamic Feature module -> on demand feature (ex: payment feature) - based on user ability to have it or not
  - Instant Dynamic Feature module -> no installation required, run directly from Play Store (ex: games)
  - Automotive module -> for car systems
  - Wear OS module -> for smartwatches
  - TV module -> for Android TV apps
  - Java or Kotlin module -> pure code module without Android dependencies. similar to Android Library module, without Android SDK dependencies
  - Baseline Profile module -> performance optimization module
  - Benchmark module -> performance testing module

- We can also handle modules in Project Structure:
  - File -> Project Structure -> Modules



### DI
- each module can have its own DI setup
- can also have a common DI setup in common module