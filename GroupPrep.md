## Amazon Group Prep

### Interviews
R2 - Cherlan -> http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?region=ap-south-1&bucketlist=&bucket=storage-solution&county=auth&accesskey=AKIAXQREBX3WM2AAHXHV&secretkey=RG7ERERB%2FVqeNwPfyYXjpgfY2RjJaCUNXI6pvl%2BC
R2 - Xavier -> http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?region=ap-south-1&bucketlist=&bucket=storage-solution&county=auth&accesskey=AKIAXQREBX3WM2AAHXHV&secretkey=RG7ERERB%2FVqeNwPfyYXjpgfY2RjJaCUNXI6pvl%2BC

### Job Description
Client – Accenture/Amazon

Title - Android Developer

Location - Redmond, WA

Job Description/ Roles & Responsibilities

Role: Android Developer

Must have -

Android Mobile Application

Kotlin and Java

Java, C#, C++

Mission - Looking for a passionate and talented Android Software Development Engineer who has experience building world-class mobile apps. You will be responsible for developing mobile applications that interact seamlessly with our devices and delight our customers.

Key job responsibilities:
Design, develop and maintain our Android mobile applications
Work with team members to investigate design approaches, prototype technology and evaluate technical feasibility
Lead architecture and design of features, from conception to launch
Help to improve engineering processes and tools to increase team effectiveness
Be part of an agile development process to deliver high-quality software

Basic Qualification:
Experience as a mentor, tech lead or leading an engineering team
Experience leading the architecture and design (architecture, design patterns, reliability and scaling) of new and current systems
Experience in professional, non-internship software development
Experience programming with at least one modern language such as Java, C++, or C# including object-oriented design
Android mobile application development experience in Kotlin and Java

Preferred Qualification:
Bachelor's degree in computer science or equivalent
Experience with full software development life cycle, including coding standards, code reviews, source control management, build processes, testing, and operations
Proficient understanding of code versioning tools such as Git
Experience with Reactive programming frameworks
Experience optimizing apps for performance
Experience writing testable code and automated tests
Experience with deployments to the Play Store
Experience with REST architecture for web services


### Code Example:
```kotlin
interface MyStockApi {
// ??? should be replaced with the HTTP method annotation, the endpoint path,
// and the expected data type.

// Using a list of Stock data objects for a basic ticker.
@GET("stocks/current")
suspend fun getStocks(): List<StockData>
}

// Data class to represent a single stock item.
data class StockData(
    val symbol: String, // e.g., "AMZN"
    val price: Double,
    val change: Double,
    val changePercent: Double
)
// request should be GET
// Reason: Retrieving existing data (the current stock quotes) without modifying any state on the server is the fundamental purpose of a GET (idempotent, read-only) request in REST architecture.
```


### Potential Questions
1. you have an API for cart
- walk through network, domain, ui layers
- if someone press + on an item in the cart, how do you handle that?
- when they mention API -> means what endpoints you would use and what you would expect to get back
  - they are not defining it, we get to define it and build around it
- when they mention what tools would you use -> retrofit, okHttp, moshi/gson, coroutines/flow

- Optimistic loading is fine because we typically are using loading screens and shimmer placeholders if we didn't get the data
- use the retry logic

![img.png](img.png)
![img_1.png](img_1.png)

- can use something like exceladraw to draw the architecture

---

## Kal Dec 1
### Android Permission Types
1. Normal Permissions
  - pose low risk, 
  - granting access to isolated app-level features without impacting user data or other apps. 
  - The system automatically grants them upon installation if declared in the manifest—no runtime request needed. 
  - Examples include INTERNET or VIBRATE.

2. Dangerous Permissions
  - runtime permissions 
  - access private user data or device controls, like location or camera, 
  - requiring explicit user approval at runtime via prompts.

3. Signature Permissions
  - are granted only to apps signed with the same certificate as the declaring app or OS, 
  - enabling secure inter-app communication without user prompts. 
  - They suit privileged services like VPN or autofill.

### difference between @Provides and @Binds in Dagger/Hilt
1. @Provides 
  - is used on methods within a module to specify how to create an instance of a dependency. 
  - It allows for more complex logic in instance creation.
2. @Binds 
  - is used on abstract methods within a module to bind an interface to its implementation. 
  - It is more concise and efficient when you simply want to tell Dagger/Hilt which implementation to use for a given interface.

### Solid Principles with Android Examples
1. Single Responsibility Principle (SRP)
  - A retrofit service interface should only handle network operations like GET, while a separate repository class manages data caching and business logic.
2. Open/Closed Principle (OCP)
  - An abstract ViewModel class can be extended to add new features without modifying the existing code, allowing for easy scalability.
3. Liskov Substitution Principle (LSP)
  - A custom Button class that extends the standard Android Button should be usable anywhere a Button is expected without altering the app's behavior.
4. Interface Segregation Principle (ISP)
   - Separate interfaces for different functionalities, such as a LocationProvider interface for location services and a NetworkProvider interface for network operations, prevent clients from being forced to implement methods they don't use.
5. Dependency Inversion Principle (DIP)
  - A ViewModel should depend on an abstract Repository interface rather than a concrete implementation, allowing for easier testing and flexibility in changing data sources.

### 2 parts of your team are discussing different libraries to solve a problem how do you mediate?
1. Clarify requirements and constraints
  - What problem are we solving exactly?
  - Requirements: performance, offline support, API surface, platform support, licensing, learning curve, maintenance, security. 
  - Constraints: deadlines, existing tech stack, team experience.

2. Define evaluation criteria
  - Agree upfront on 4–6 criteria, for example:
    - API ergonomics / developer productivity
    - Performance (startup, memory, latency)
    - Reliability and stability
    - Testability and tooling
    - Community support / maintenance (stars, releases, issues)
    - Integration with our existing architecture (DI, coroutines/Flow, Compose, etc.)

3. Prototyping / POC (proof of concept)
  - Build two small POCs with identical scope:
    - Same use case (e.g., same network call, same caching strategy, same UI flow).
    - Same constraints (runs on min SDK we support, works with our DI, logging, etc.).
  - Keep each POC time-boxed (e.g., 0.5–1 day each).

4. Measure and compare
  - Collect objective metrics:
    - Build time, method count / APK size delta. 
    - Simple perf numbers: startup impact, request latency, memory footprint. 
    - Error handling behavior and ease of debugging.
  - Collect subjective developer experience:
    - How easy was it to integrate? 
    - How readable and testable is the code? 
    - How much boilerplate is required?

5. Facilitate a neutral decision
  - Put findings into a short comparison doc:
    - Summary table: Library A vs Library B across criteria.
    - Pros/cons list for each. 
    - Risks (e.g., smaller community, breaking changes, migration cost).
  - Drive the team toward consensus:
    - Focus on project goals, not personal preference.
    - If tradeoffs are close, pick the option that:
      - Aligns better with long-term maintainability and 
      - Fits your existing architecture/tooling.

6. Document and share the decision 
  - Record why the library was chosen, what tradeoffs were accepted, and any migration plan if you need to switch later.
  - This makes future disagreements easier to resolve.


---


## Lyft Challenge

Display 12 cards in the grid
Only 6 images should be used, effectively displaying each image twice: ie two heart cards, two star cards, etc
Shuffle the cards randomly on each run

```kotlin
//import android.graphics.drawable.Icon
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.shape.CornerSize
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.snapshots.SnapshotStateList
//import androidx.compose.runtime.toMutableStateList
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContentView()
        }
    }
}

val images = listOf(
    Icons.Default.Face,
    Icons.Default.Favorite,
    Icons.Default.Star,
    Icons.Default.ShoppingCart,
    Icons.Default.Home,
    Icons.Default.ThumbUp,
)

// You can update with your own data structure if needed.
// This is just an example of simplest data structure for recomposition.
val cells: SnapshotStateList<Any> = listOf<Any>().toMutableStateList()

/**

A simple [Composable] function that displays a grid of buttons.
The grid is 3 columns wide and has 4 rows.
Important:
Manually import LazyVerticalGrid and GridCells.
This can happen because different versions of Compose have placed these classes in different packages.
*/
@Composable
private fun ContentView() {

    remember { cells } //Recomposition is triggered after you change this object
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(12) { index ->
            CardButton()
        }
    }
}

@Composable
private fun CardButton() {
    Button(
        modifier = Modifier.aspectRatio(1.0f),
        colors = ButtonDefaults.buttonColors(
        containerColor = ColorBlue,
        contentColor = Color.White,
        ),
        shape = RoundedCornerShape(CornerSize(10.dp)),
        onClick = { /*TODO*/ }
    ) {
        Image(imageVector = images[0], "", Modifier.fillMaxSize())
    }
}

private val ColorBlue = Color.Blue.copy(red = 0.2f, blue = 0.9f, green = 0.3f, alpha = 0.8f)
private val ColorYellow = Color.Yellow.copy(red = 0.9f, blue = 0.2f, green = 0.77f, alpha = 0.9f)
private val ColorGreen = Color.Green.copy(red = 0.02f, blue = 0.16f, green = 0.70f, alpha = 0.8f)
```

## Five Pack
About the job
Description:
Five Pack is seeking a Sr. Android Developer who will create and maintain exciting Android applications by working closely with talented engineers and clients to design and build the next generation of mobile applications. 
Skilled in agile project methodologies, this role will aid the team in streamlining development, increase project quality, reduce risk, and improve the overall project predictability.

Qualifications:
5+ years experience with designing, building, and maintaining complex mobile apps on Android.
Experience with Android Jetpack, ViewModel, WorkManager, LiveData, Databinding, Material, Room (and Sqlite), Lifecycle.
Extensive Kotlin knowledge including coroutines, Flow, and multiplatform usage.
Skilled in Agile Methodologies of scrum, kanban, sprints, and ticket sizing.
Understanding of shift left fundamentals including test driven development and unit testing.
Proven experience performing Android builds and deploys to the Google PlayStore.

Responsibilities:
Collaborate with project managers, business analysts, designers, architects, quality analysts and others by attending meetings to develop applications and systems, and to obtain information on project limitations and capabilities, performance requirements and interfaces.
Translate business requirements, user needs and technical requirements into applications that are visually appealing, easy to use, and engaging for users.
Collaborate with software developers to integrate existing technologies (APIs) into a mobile application.
Assist with the software update process for existing applications, and roll-outs of software releases and development of prototypes.
Remain actively informed of competitor applications and industry trends.
Deliver on time as discussed and clearly communicate any changes in deadlines as soon as possible.
Troubleshoot issues and implement bug fixes for Android applications.
Apply the results from user research and usability testing to create new features and improve current features.
Develop and implement new features and functionality as required.

## America Airlines
- KMM or KMP are required -> need a story about your use case
  - built an authentication module that works on both android and iOS 
  - use ktor and koin

### Networking module (KMM)
Designed and implemented a shared KMM networking layer using Ktor client with pluggable engines for Android and iOS, enabling a single HTTP stack for all platforms while keeping platform-specific configuration minimal.
Introduced a clean data/domain separation with repositories and remote data sources in the shared module, so feature teams could consume typed APIs without knowing transport details.
Standardized API contracts using Kotlinx Serialization and sealed result types for success/error, reducing networking-related crashes and parsing issues across platforms.
Implemented cross-platform HTTP concerns (logging, timeouts, retry, connectivity checks, JSON configuration) once in shared code and exposed simple interfaces to Android/iOS, cutting duplicate networking code significantly.
Integrated the networking module with dependency injection (Koin/Hilt on Android, injected from shared on iOS), allowing easy mocking of clients and repositories for unit tests

Example:
Auth module (KMM)
Built a shared authentication module in KMM responsible for login flows, token storage/refresh, and session management, reused by both Android and iOS apps.
Implemented email/password and social login flows on top of the shared networking layer, encapsulating authentication endpoints behind a single AuthRepository API.
Designed a secure, multiplatform token storage abstraction (wrapping Keystore/EncryptedSharedPreferences on Android and Keychain on iOS) to persist access/refresh tokens and user session data.

### Launched vs Disposed Effect
- LaunchedEffect is used to launch a coroutine when the key changes or when the composable enters the composition. 
  - It is tied to the lifecycle of the composable and will be cancelled when the composable leaves the composition.
- DisposedEffect is used to perform cleanup actions when the composable leaves the composition.
  - It is not tied to the lifecycle of the composable and will not be cancelled when the composable leaves the composition.

- Use LaunchedEffect when you want to perform an action that should be tied to the lifecycle of the composable, such as loading data or starting an animation.
- Use DisposedEffect when you want to perform an action that should be performed when the composable leaves the composition, such as releasing resources or cancelling a network request.

### When to use a StateFlow vs a SharedFlow
- StateFlow is used to represent a state that can be observed and updated.
  - It is a hot flow that always has a current value and emits the current value to new subscribers.
  - It is typically used for UI state management, where the UI needs to react to changes in the state.
- SharedFlow is used to represent a stream of events that can be observed and emitted.
  - It is a hot flow that does not have a current value and emits events to all subscribers.
  - It is typically used for event-driven architectures, where multiple components need to react to events.
  - not always has the latest value like StateFlow -> like a queue of events

- Use StateFlow when you want to represent a state that can be observed and updated, such as UI state management.
  - e.g. for Loading, Success, Error states in ViewModel
- Use SharedFlow when you want to represent a stream of events that can be observed and emitted, such as event-driven architectures.
  - e.g. notifications, analytics, navigation events, etc.
  - could be good for something like stock price updating the price every second -> multiple subscribers might want to listen to the price updates

Kal
Use StateFlow when:
• You need to manage and share a single state with multiple collectors
• You want to access the current state of the flow at any time using the .value property
• You want to replay only the latest value to new subscribers
• You want to avoid emitting consecutive repeated values
• You want behavior similar to LiveData (with repeatOnLifecycle)
Use SharedFlow when:
• You need to share a stream of events among collectors without holding any state
• You want to emit and collect repeated values
• You want to replay more than the latest value for new subscribers
• You are doing a one-time event like showing a Snackbar

### 