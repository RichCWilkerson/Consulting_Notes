# Resources:

- [Paging 3 Youtube](https://www.youtube.com/watch?v=AasI-0IRXUM&pp=ygUacGhpbGlwcCBsYWNrbmVyIHBhZ2luYXRpb24%3D)
- [Compose Paging Youtube](https://www.youtube.com/watch?v=D6Eus3f6U9I&t=1s&pp=ygUacGhpbGlwcCBsYWNrbmVyIHBhZ2luYXRpb24%3D)
- [KMP Paging Youtube](https://www.youtube.com/watch?v=SCEiPchkvJo&pp=ygUacGhpbGlwcCBsYWNrbmVyIHBhZ2luYXRpb24%3D)

- [XML - Medium](https://medium.com/@chaharchandresh/paging-3-android-pagination-retrofit-and-coroutines-f925a504a692)

- [Compose - Medium](https://medium.com/@me.zahidul/mastering-android-pagination-with-paging-3-jetpack-compose-9c8bad8ee98f)

- [Official Docs](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
  - Note the Paging library is the same thing, Paging 3 is just version 3 of the Paging library.
    - this version is a huge improvement over version 2 as it is Kotlin-first and coroutine-supported.

# Paging 3
## Overview
- Kotlin-first, coroutine-supported API

- key performance features of Paging 3 is its smart prefetching. 
  - The program doesn’t procrastinate allowing the user to expend all options; it pre-loads the following page behind the scenes so as to be ready in time when the user scrolls down and checks on it.

---

## Steps to implement Paging in Android using Paging 3 Library:
Dependencies:
- Check latest version [here](https://developer.android.com/jetpack/androidx/releases/paging)
```kotlin
dependencies {
    implementation ("androidx.paging:paging-compose:3.3.6") // for Jetpack Compose
    implementation ("androidx.paging:paging-runtime-ktx:3.3.6") // for ViewModel and LiveData
}
```

### Example Implementation:

1. Define the PagingSource:
   - This can either be from a network source (like Retrofit) or local database (like Room).
   - 2 override functions, one is load() and the other is getRefreshKey().
```kotlin
// app/data/source/ImagePagingSource.kt
// source will be grouped with other data sources in the data layer (e.g., network sources, database sources, etc.)
// can name it /paging/ if preferred separately
class ImagePagingSource(
    private val apiService: ApiService 
) : PagingSource<Int, ImageListModel>() { // specify key type (Int for page number) and value type (ImageListModel for data model)
// PagingSource<Key, Value> -> PagingSource returns a list of Value items based on Key type

    // number of pages to keep in memory for prefetching
    private val numOfOffScreenPage: Int = 4 

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ImageListModel> {

        // default to page 1 if key is null
        val pageIndex = params.key ?: 1

        // number of items to load, provided in Repository layer or Usecase layer
        val pageSize = params.loadSize 
        return try {
            // NOTE: this requires setting up paging on our server side, if we don't have control over server API,
            // we may need to implement custom logic to handle pagination based on the API's capabilities.
            // similar to how we would do it with Room database queries.
            val responseData = apiService.fetchImages(pageIndex, pageSize) 

            /*
            // if your server is not set up for paging:
            val allPosts = userService.getAllPosts()

            // calculate starting index for the current page
            // we want to start at 0 index for the data for page 1
            val fromIndex = (pageIndex - 1) * pageSize 
            
            // if starting index exceeds total items, return empty page
            if (fromIndex >= allPosts.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (pageIndex == 1) null else pageIndex - 1,
                    nextKey = null
                )
            }

            // calculate end index for the current page 
            // minOf(1, 5) means return the smaller of the two values, here it will return 1
            // we use this to avoid going out of bounds of the list
            // either we have page size number of items, or we reach the end of the list
            val toIndex = minOf(fromIndex + pageSize, allPosts.size)
            
            // subList allows us to define a range within the list to return
            val pageData = allPosts.subList(fromIndex, toIndex)

            // pass the data to successful LoadResult.Page
            LoadResult.Page(
                data = pageData,
                // previous page key, null if on first page, allows for manipulating UI based on prevKey (e.g., disabling prev button)
                prevKey = if (pageIndex == 1) null else pageIndex - 1, 
                nextKey = if (toIndex >= allPosts.size) null else pageIndex + 1
            )
             */
            
            
            LoadResult.Page(
                data = responseData.body()!!,
                prevKey = if (pageIndex == 1) null else pageIndex - 1,
                nextKey = if (responseData.body()!!.isEmpty()) null else pageIndex + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // called when invalidating the PagingSource, used to determine the key for refreshing data
    // e.g., when user performs swipe-to-refresh action in the UI
    override fun getRefreshKey(state: PagingState<Int, ImageListModel>): Int? {
        // state is the current paging state including loaded pages and anchor position
        // anchorPosition is the most recently accessed index in the list
        return state.anchorPosition?.let { anchor ->
            // closestPageToPosition finds the page closest to the anchor position, at index 23 of pages of size 10, it would be page 3 (items 21-30)
            // .plus(numOfOffScreenPage) -> we want to refresh starting a few pages before the anchor to ensure smooth UX
            state.closestPageToPosition(anchor)?.prevKey?.plus(numOfOffScreenPage)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(numOfOffScreenPage)
        }
    }
}
```

- **load() Function:**
  - data is retrieved either from a local database or a remote API, depending on the implementation. 
  - The load() method receives LoadParams as a parameter, which provides access to the current page key (key), the number of items to load (loadSize), and the placeholdersEnabled flag.

- **LoadResult** must be returned to the Paging framework. The LoadResult can be one of three types:
  - LoadResult.Page – Used when data is successfully loaded. It contains the list of items along with optional prevKey and nextKey.
  - LoadResult.Error – Used when an error occurs during data fetching.
  - LoadResult.Invalid – Used when the result is invalid. This return type can be used to terminate future load requests.

2. Repository Layer:
   - call the Pager object which connects to the PagingSource. It exposes a Flow<PagingData<T>> to the ViewModel, allowing the UI to observe paginated data.
   - Inside the Pager, we define the page size and supply the PagingSource. 
   - The repository doesn’t fetch data directly—it delegates that to the PagingSource’s load method. 
   - The LoadResult return by PagingSource is automatically transformed into PagingData for the UI.

```kotlin
class ImageRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ImageRepository, NetworkCallback() {

    override fun getImages(
        pageSize: Int,
        enablePlaceHolders: Boolean,
        prefetchDistance: Int,
        initialLoadSize: Int,
        maxCacheSize: Int
    ): Flow<PagingData<ImageListModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = enablePlaceHolders,
                prefetchDistance = prefetchDistance,
                initialLoadSize = initialLoadSize,
                maxSize = maxCacheSize
            ), pagingSourceFactory = {
                ImagePagingSource(apiService)
            }
        ).flow
    }
}
```

3. Usecase Layer:
   - The Use Case acts as an intermediary between the ViewModel and the Repository. 
   - It abstracts the business logic and simply invokes the repository’sgetImages() method. 
   - The Use Case returns a Flow<PagingData<T>>, keeping the ViewModel decoupled from data source implementations.
```kotlin
class ImageLoadingUseCase(
    private val imageRepository: ImageRepository
) {
    fun fetchImages(): Flow<PagingData<ImageListModel>> {
        return imageRepository.getImages(
            pageSize = 20,
            enablePlaceHolders = false,
            prefetchDistance = 10,
            initialLoadSize = 20,
            maxCacheSize = 2000
        )
    }
}
```

4. ViewModel Layer:
   - In the ViewModel layer, we collect the paginated data flow from the Use Case and expose it to the UI. 
   - Typically, this is done using Flow<PagingData<T>> and collected with collectAsLazyPagingItems() In Compose.
```kotlin
fun ImageViewModelImpl(
    private val imageUseCase: ImageLoadingUseCase
) : ImageViewModel {
    val getImageList = imageUseCase.fetchImages().cachedIn(viewModelScope)
}
```

5. UI/Compose Layer:
   - In the UI layer, LazyColumn is used to display paginated items efficiently. 
   - The items() block consumes imageListItems, which is a LazyPagingItems<T> from the Paging 3 library. 
   - Each item is accessed by index (position) and rendered inside a Card. 
   - An image is loaded asynchronously using AsyncImage, and metadata (like author name) is overlaid using a Column inside a Box. 
   - Paging handles automatic loading of more items when the user scrolls to the end. 
   - The UI reacts to paging state updates (like loading or errors) when managed properly with collectAsLazyPagingItems().

```kotlin
val imageListItems = viewModel.getImageList.collectAsLazyPagingItems()

@Composable
fun ImageListScreen(
    imageListItems: LazyPagingItems<ImageListModel>?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(imageListItems!!.itemCount) {position->
            var itemValue = imageListItems[position]
            Card (
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Transparent)
            ) {
                Box {
                    itemValue?.download_url?.let {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .aspectRatio(itemValue.width!!*1.0f / itemValue.height!!),
                            model = itemValue.download_url.toString(),
                            contentDescription = "Image",
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray)
                            .height(30.dp)
                            .align(Alignment.BottomStart)
                            .padding(start = 10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            color = Color.White,
                            text = itemValue?.author.toString()
                        )
                    }
                }
            }
        }
    }
}
```

---

## Common Pitfalls


---

## Best Practices


---

## Interview Questions