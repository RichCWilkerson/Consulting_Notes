# Clean
- Domain vs Data layer separation
  - Domain layer:
    - Pure business logic
    - No Android or networking dependencies
    - Easy to unit-test
    - Entity models
  - Data layer:
    - Handles data sources (network, database)
    - Implements repository interfaces from domain layer
- Use Cases:
  - Encapsulate specific business actions
  - Called by ViewModels to perform operations
  - take user input -> send to repository -> return result
- Why choose Clean:
  - separation of layers
  - doesn't mean you can't use MVVM or other patterns

# MVI
- Model-View-Intent pattern
  - Model: represents the state of the UI
  - View: renders the UI based on the Model
  - Intent: user actions that trigger state changes
- Unidirectional data flow:
  - User interacts with View -> generates Intent
  - Intent processed -> updates Model
  - Model change -> View re-renders
- Declarative UI:
  - View is a function of the Model
  - Easier to reason about UI state

# Java in Android
- java has more legacy code and libraries
- if you are using a lot of java libraries, it might be easier to use java
- compose is not supported in java
- java can be converted in the IDE to kotlin easily
- no co-routines in java

# Ali
## Pagination
- loading data in chunks instead of all at once
- improves performance and user experience
- Android Paging Library:
  - handles pagination logic
  - supports RecyclerView integration
  - supports different data sources (network, database)
  - dependencies (2)
    - paging-runtime and paging-compose (if using compose)

- add parameters to API calls for page number and size
  - this will then be used with logic to load 1 page with a size of how many item to fetch

```kotlin
pager = Pager(
    config = PagingConfig(pageSize = 20),
    pagingSourceFactory = { MyPagingSource(apiService) }
).flow.cachedIn(viewModelScope)

// Compose pagination for suspending functions
val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
// fallback when waiting for data
if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
    // initial loading
    CircularProgressIndicator()
} else {
    LazyColumn {
        items(lazyPagingItems) { item ->
            // display item
        }
        // handle loading more
        lazyPagingItems.apply {
            when (loadState.append) {
                is LoadState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    val e = lazyPagingItems.loadState.append as LoadState.Error
                    item {
                        Text(text = "Error: ${e.error.localizedMessage}")
                    }
                }
                else -> {}
            }
        }
    }
}
```

## Caching
- Use Room or SharedPreferences to store data locally
- Check local cache before making network requests

## Leak Canary
- Detect memory leaks in your app
  - like if you forget to unregister a listener or callback
  - can lead to crashes or poor performance
  - helps identify and fix leaks during development
- Add dependency and initialize in Application class
- user trying to endless sroll on our lazy column pagination -> memory leak


# Adam Feedback
- summary after each topic, ask for questions
- if you start repeating yourself, stop and move on