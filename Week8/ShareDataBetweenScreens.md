# Only Compose

## Navigation Args
Advantages:
- easy to implement
- values survive process death by default -> meaning they are saved in the saved state handle 
  - if the app is killed in the background, when the user returns to the app, the nav args will still be available
  - in view models or composable states, this data will be lost by default unless saved in the saved state handle
- disadvantages:
  - not suited for complex data (parcelables, large data objects) (especially in Compose where nav args are strings)
  - not convenient between many screens (like screen 1 to screen 5) -> pass from 1 to 2, 2 to 3, etc.
  - stateless -> cannot be updated or observed for changes

```kotlin
@Composable
fun NavArgsExample() {
    val navController = rememberNavController()
    NavHost(
        navController, 
        startDestination = "screenA"
    ) {
        composable("screenA") {
            ScreenA(onNavigate = {
                navController.navigate("screenB/HelloFromA")
            })
        }
        composable(
            "screenB/{message}",
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
            composable("personal_details") { entry ->
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
