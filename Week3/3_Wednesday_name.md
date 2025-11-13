# Challenge
Virgin Money Developer Challenge
- see all their collegues contact details
  - on list the bottom nav is showns -> when you go to details screen the bottom nav is hidden and a back button is shown
- see which rooms in the office are currently occupied
  - toggle, occupied, free, and all

- implement bottom navigation bar with Navigation Graph

- must use MVVM architecture (ViewModel, LiveData/StateFlow, Repository pattern)
  - di, data (model, remote), ui (colleagueListFragment, colleagueDetailsFragment, officeRoomsFragment, mainActivity (welcome screen with bottom nav))

- API
  - BASE_URL
    - https://61e947967bc0550017bc61bf.mockapi.io/api/v1/
  - ENDPOINTS
    - "people"
    - "rooms"
      - isOccupied: boolean field -> our toggle from above

- dates need to be displayed in nice format -> use a library or do it yourself


## dependencies
### build.gradle.kts (Project) - plugins
- KSP (needed for hilt ksp): id("com.google.devtools.ksp") version "2.2.20-2.0.3" apply false
- HILT: id("com.google.dagger.hilt.android") version "2.57.1" apply false
### build.gradle.kts (Module: app) - plugins
- KSP: id("com.google.devtools.ksp")
- HILT: id("com.google.dagger.hilt.android")
### build.gradle.kts (Module: app) - dependencies
- Lifecycle (for lifecycleScope): 
    - implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
- Retrofit 2.x (valid latest stable): 
    - BASE: implementation("com.squareup.retrofit2:retrofit:2.11.0")
    - GSON: implementation("com.squareup.retrofit2:converter-gson:2.11.0")
- Gson: 
    - implementation("com.google.code.gson:gson:2.10.1")
- OkHttp:
    - BASE: implementation("com.squareup.okhttp3:okhttp:4.12.0")
    - INTERCEPTOR: implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
- Glide (images): 
    - implementation("com.github.bumptech.glide:glide:5.0.5")
- Hilt (Dependency Injection)
    - BASE: implementation("com.google.dagger:hilt-android:2.57.1")
    - KSP: ksp("com.google.dagger:hilt-android-compiler:2.57.1")
- Navigation:
    - implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    - implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
- Fragment KTX (for by viewModels()): 
    - implementation("androidx.fragment:fragment-ktx:1.8.5")
- Lifecycle ViewModel KTX (coroutines, etc.): 
    - implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
- LiveData KTX (for observe(viewLifecycleOwner) {}): 
    - implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
- RecyclerView (for iterating over list of: colleagues/rooms):
    - implementation("androidx.recyclerview:recyclerview:1.4.0")
### build.gradle.kts (Module: app) - android
- buildFeatures { viewBinding = true }

## Manifest
- define Application class
- define internet permission
  - `<uses-permission android:name="android.permission.INTERNET" />`
- define single activity (MainActivity) with intent filter for LAUNCHER and MAIN

## API
### JSON -> Kotlin data classes
- right click on /data/model -> New -> Kotlin data class from JSON
- paste in example JSON from API docs -> use both people and rooms endpoints
- advanced options -> GSON, allow nullable, suffix "Model"
- ensure no AnyModel types -> comment out fields you don't need or change types

### Setup Retrofit + OkHttp + Gson
- create /data/remote/RetrofitInstance.kt -> Modular for any API
- create /data/remote/VirginService.kt -> specific to Virgin API
  - define suspend functions for getPeople(), getPeopleDetails(), and getRooms()

## DI - Hilt -> Convert RetrofitInstance to be provided by Hilt
1. Add @HiltAndroidApp to your Application class and register it in AndroidManifest.xml.
2. Setup API service module
  - Add @Module and @InstallIn(SingletonComponent::class) annotations to the class
  - Add @Provides and dependencies (parameters) to each function 
    - ex: 
```kotlin
@Provides
fun provideOkhttpInstance(
loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient
// vs
private val okHttpClient = OkHttpClient.Builder()
  .addInterceptor(loggingInterceptor)
  .build()
```
3. Endpoints can be called from the original API service 
4. Add @AndroidEntryPoint to MainActivity and Fragments that use DI
5. Add @Inject to the constructor of classes you want Hilt to provide
  - ex: RetrofitInstance, VirginService, Repository classes
6. ViewModels that need DI
  - Add @HiltViewModel to the ViewModel class
  - Add @Inject to the constructor of the ViewModel
  - Use by viewModels() in the Fragment to get the ViewModel instance



## UI
### Setup Screens XML / Activities/Fragments
- MainActivity - activity_main.xml
  - FragmentContainerView for Navigation Graph
  - BottomNavigationView for bottom nav bar
- PeopleListFragment
  - fragment_people_list.xml
  - people_item.xml (for RecycleView item)
- PeopleDetailsFragment - fragment_people_details.xml
- RoomsListFragment - fragment_rooms_list.xml

### Setup RecycleView 
- 2 Adapters
  - PeopleAdapter
  - RoomsAdapter
- 2 ViewHolders
  - PersonViewHolder
  - RoomViewHolder
- 2 Item XML layouts
  - item_person.xml
  - item_room.xml

### Setup ViewModels
- PeopleListViewModel -> allows us to observe -> API gets added to ViewModel
- PeopleDetailsViewModel
- RoomsListViewModel

## Navigation
### Setup Navigation Graph
- create /res/navigation/nav_graph.xml
- populate fragments
  - action -> PeopleListFragment to PeopleDetailsFragment (with argument peopleId)

### Setup Bottom Navigation AppBar
- in MainActivity -> setup with Navigation Component
  - Will make it available in all fragments in the Activity
- PersonDetailsFragment -> hide bottom nav, show back button

1. private lateinit both the navController and appBarConfiguration (for back button support in bottom nav)
2. NavHostFragment -> FragmentContainerView in activity_main.xml
  - set navGraph to @navigation/nav_graph
  - set default navHost to true
3. BottomNavigationView in activity_main.xml
  - set menu to @menu/bottom_nav_menu
4. MainActivity.kt
  - val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
  - val navController = navHostFragment.navController
  - appBarConfiguration = AppBarConfiguration(setOf(<accessible screens from bottom nav>))
  - setupActionBarWithNavController(navController, appBarConfiguration)
  - binding.bottomNav.setupWithNavController(navController)
  - onSupportNavigateUp() - setup back button support 
    - not sure how this works when you hide the bottom nav in details screen


## Themes
- Branding
  - #C40202 - define in /res/values/colors.xml -> manage branding in themes.xml
  - in the midst of possible rebranding (not sure what to do with this info)

- REMOVE THE BANNER: add this to the theme -> NoActionBar inheritance
  -     <style name="Theme._40VirginCollleagues" parent="Theme.MaterialComponents.DayNight.NoActionBar">
  - don't put this in the MainActivity: setupActionBarWithNavController(navController, appBarConfiguration)


- Due Tomorrow



# Feedback
- don't have ui you don't need -> remove Home
- use placeholder in Glide for people that do not have pictures
- get back button on person details screen to work
- repository package in data -> done
- add date to person details screen and format it nicely

- error / loading / success states handled with sealed classes