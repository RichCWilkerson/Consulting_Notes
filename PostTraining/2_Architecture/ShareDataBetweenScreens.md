# Only Compose
[youtube](https://www.youtube.com/watch?v=h61Wqy3qcKg)

## Navigation Args
Advantages:
- easy to implement
- values survive process death by default -> meaning they are saved in the saved state handle 
  - if the app is killed in the background, when the user returns to the app, the nav args will still be available
    - TODO: is this being saved to disk on the bundle? where and what are its limitations?
  - in view models or composable states, this data will be lost by default unless saved in the saved state handle
- disadvantages:
  - not suited for complex data (parcelables, large data objects) (especially in Compose where nav args are strings) 
    - TODO: what are the size limitations?
  - not convenient between many screens (like screen 1 to screen 5) -> pass from 1 to 2, 2 to 3, etc.
  - stateless -> cannot be updated or observed for changes
    - TODO: expound on what this means. if it's saving data that data is state, no? I get that it is not the same as StateFlow or DataFlow. Are we saying it is read-only? or that while the data is in the NavArgs it is not accessible until it has been passed to a variable? 

```kotlin
@Composable
fun NavArgsExample() {
    val navController = rememberNavController()
    NavHost(
        navController, 
        startDestination = "screenA"
    ) {
        composable("screenA") {
            // TODO: we are passing this navigate logic to the screen
            ScreenA(onNavigate = {
                navController.navigate("screenB/HelloFromA")
            })
        }
        composable(
            // ScreenB is expecting a message - this must be a string
            // TODO: what happens if no message is passed and it's just "screenB/"? it looks like the composable ScreenB allows for message to be null, but does the NavHost allow it?
            "screenB/{message}",
            // TODO: arguments lets us get the data out of the url? or is it just the name of a variable we are passing and can be named anything?
            // TODO: because navArgument("argumentId") is what gives us access to the url, and say we are expecting a StringType?
            arguments = listOf(navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            // Retrieve the argument from the backStackEntry
            val message = backStackEntry.arguments?.getString("message")
            // Pass the argument to ScreenB
            ScreenB(message = message)
        }
    }
}

@Composable
fun ScreenA(onNavigate: (String) -> Unit) {
    // if onNavigate already equals navController.navigate("screenB/HelloFromA"), then why are we passing a string to onNavigate?
    Button(onClick = onNavigate("HelloFromA")) {
        Text("Go to Screen B")
    }
}

@Composable
fun ScreenB(message: String?) {
    Text(text = message ?: "No message received")
}
```

## Shared ViewModel
- share viewmodel between multiple composables/screens
- TODO: if i by default setup a view model per NavHost, even if no data is being stored in it, just an empty ViewModel file, will that impact the performance or memory in any meaningful way?
- TODO: is it best practice to have each feature-module that has ui screens to have its own NavHost with a ViewModel? 

- TODO: we can pass in the view model here either by passing it in to the NavHost, declaring it in the NavHost with `by viewModels()` or with hilt using `by hiltViewModels`?

````kotlin
@Composable
fun SharedViewModelExample() {
    val navController = rememberNavController()
    NavHost(
        navController, 
        startDestination = "onboarding"
    ) {
        navigation(
            startDestination = "personal_details",
            route = "onboarding"
        ) {
            composable("personal_details") { entry -> // TODO: what is entry? every composable is able to call what as entry?
                // TODO: is it best to declare a shared view model like this? shouldn't it be declared outside the composable parameters and then passed in as a parameter?
                val sharedViewModel = entry.sharedViewModel<OnboardingViewModel>(navController)
                val state by sharedViewModel.sharedState.collectAsStateWithLifecycle()
                
                PersonalDetailsScreen(
                    sharedState = state,
                    onNavigate = {
                        navController.navigate("terms_and_conditions")
                    }
                )
            }
            composable("terms_and_conditions") { entry ->
                val sharedViewModel = entry.sharedViewModel<OnboardingViewModel>(navController)
                val state by sharedViewModel.sharedState.collectAsStateWithLifecycle()
                
                AddressDetailsScreen(
                    viewModel = sharedViewModel,
                    onFinish = {
                        // Handle finish action
                    }
                )
            }
        }
    }
````


## Sharing Stateful Dependencies


## Using Composition Locals


## Persistent Storage
