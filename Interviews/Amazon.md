Client Name    -    Amazon
Implementor/Prime Vendor Name    -    Accenture
Vendor Name    -    Vdart
Date    -    12/3/2025
Time    -    4:30
Duration    -    30
Mode    -    Zoom
Interview with    -    Vendor
Round    -    R1
Live Coding    -    Yes
Meeting Link    -    https://www.google.com/url?q=https%3A%2F%2Fvdart.zoom.us%2Fj%2F95294342681&sa=D&source=calendar&usd=2&usg=AOvVaw0r3X6JEYmGgR2W_15b3CYP
FE location (for this interview)    -    Texas

- Bad audio and no video
http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-05_R1_Willard_Amazon.mkv&bucket=storage-solution


http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-11_R1_Jack_Amazon.mkv&bucket=storage-solution


## Role:
Kuiper belt (ice astroids surrounding our solar system)

Android Developer
Location: Redmond, WA (100% onsite)
Will accept candidate willing to relocate but they need to understand this is a short-term role with possible extension not guaranteed extension.
Work Auth: GC or USC only

Job Description

BASIC QUALIFICATIONS
• 4+ years experience in professional, non-internship software development
• Android mobile application development experience in Kotlin
• Experience in full software development life cycle, including coding standards, code reviews, source control management, build processes, testing, and operations experience

PREFERRED QUALIFICATIONS
• Bachelor's degree in computer science or equivalent
• Experience in Compose Multiplatform or other cross-platform mobile development
• iOS mobile application development experience in Swift
• Experience with deployments to the Play Store

## S3 Notes:
### Will
- publishing to play store -> 
  - signed AAB (Android App Bundle) 
  - track based releases (internal, alpha, beta, production)
  - R8 and Proguard for code shrinking and obfuscation

You ensure the release build type is correctly configured:
- signing configs (keystore, passwords managed securely)
- minifyEnabled/shrinkResources setup
- variant‑specific flags (logging off, crash reporting on, feature flags).

Why AAB signing (APK split for smaller download size, dynamic features, etc.)

difference between debug and release builds (keystores, logging, crash reporting, optimizations)
- debug - debug keystore, logging enabled, no optimizations
- release - production keystore, logging disabled, optimizations enabled (R8/Proguard)

Track‑based rollout awareness
- Internal track for QA/feature testing.
- Alpha/beta for limited external users or internal dogfood.
- Staged rollout to production to reduce blast radius of defects.
- How you use crashes/analytics during early tracks to decide whether to promote/rollback.

R8/Proguard expertise
- Writing/maintaining proguard-rules.pro to:
  - keep models used via reflection (e.g., JSON, DI frameworks).
  - avoid stripping entry points used by libraries (Firebase, Koin/Hilt, RN, etc.).
- Watching for issues that only appear in minified builds and knowing how to debug them.
  - common pitfalls: missing keep rules, obfuscated method names in reflection, serialization issues.

- experience with cross platform flutter or react native? 
- how would you decide to use cross platform vs native? 
  - performance requirements
  - team skillset
  - time to market
  - maintenance considerations
  - user experience expectations
  - like the parity between iOS and Android apps
  - no mistranslations for Strings, colors, dimens, etc.

### Jack
- primary language at recent job? Kotlin

- explain current app 

- where are you currently located? Dallas TX

- willing to relocate? yes

- what are data classes? 
  - like POJOs but with more features
  - auto generated equals(), hashCode(), toString(), copy(), componentN() functions
  - concise class to hold data
    - commonly used to represent API responses, database entities, etc.

- explain difference between async and launch coroutines
  - async returns a Deferred<T> which is a future result that can be awaited()
    - perform concurrent tasks that return a result
  - launch returns a Job which represents a coroutine that does not return a result
    - fire and forget tasks, don't expect a return value
    - starts a coroutine runs independently

- explain null handling in Kotlin vs Java
  - elvis operator ?:
  - safe call operator ?.
  - non-null assertion operator !!
  - nullable types vs non-nullable types
  - let scope function for null checks

- what are extension functions in Kotlin?
  - extending existing classes with new functionality without inheriting or modifying the original class
  - e.g. 3rd party libraries 
  - e.g. adding utility functions to String, List, etc.
  - if i want to extend the String class to add a function to check if it's a valid email

- exposure to cross platform?
  - most recently using KMM (Kotlin Multiplatform Mobile) for __
  - used RN in Zoom for some features
  - if you want to go back to 2017 I was working with Flutter/Dart

- questions to ask interviewer
  - what is the project we're looking at? is it Kotlin, KMM, RN?
  - has the team already started?
  - when can i expect feedback?
  - how many rounds or what are next steps?

- what other technologies do you have experience with?

## Second Round Questions:
### Kal
1) what is the android activity lifecycle
   - onCreate, onStart, onResume, onPause, onStop, onDestroy, onRestart
2) what is mvvm
    - Model-View-ViewModel
    - Model: data layer (network, database)
    - View: UI layer (Activities, Fragments, Composables)
    - ViewModel: mediator between Model and View, holds UI state, handles business logic
3) do you have experience in jetpack compose
    - yes, used it in recent projects for building UI declaratively
    - advantages: less boilerplate, easier state management, better tooling, ui caches (better performance)
      - story: in neiman marcus app we had performance issues with xml due to heavy media usage 
        - -> my migrating certain screens to compose we were able to improve performance and stability by reducing unnecessary recompositions and leveraging ui caches
4) what does remember do
    - used to store state across recompositions in compose
    - You can mention rememberSaveable if you want a bonus point: survive configure changes
5) coroutines, rx java , threading
    - coroutines: lightweight threads for async programming in **Kotlin**, 
      - advantages over threads: less memory overhead, structured concurrency, easier to read and maintain, pause and resume
      - When you’d use them: network calls, database I/O, parallel work in a ViewModel using viewModelScope, Flow for streams.
    - rx java: reactive programming library for composing async and event-based programs using observable sequences, advantages: powerful operators for transforming streams, backpressure handling, good for complex event chains
      - Downsides: steeper learning curve, can be overkill if you just need simple async.
      - When you’d use it: legacy codebases that are already Rx-heavy, or very complex event/stream pipelines until you migrate to Flow.
    - threading: managing multiple threads for concurrent execution, use cases: background tasks, network calls, ui updates
      - Benefits: full control, but easy to get wrong (leaks, race conditions, hard cancellation).
      - On Android you mostly hide this behind coroutines/Rx or higher-level APIs instead of managing threads directly.
    - “On Android today I default to Kotlin coroutines and Flow for async work, because they give me structured concurrency, cancellation, and readable code. 
      I still understand RxJava and have used it for complex reactive pipelines in older codebases, but I wouldn’t start a new project with it unless the team is heavily invested in Rx. 
      Raw threads I reserve for very low-level cases; in app code I generally let coroutines or Rx manage threading on top of executors.”
6) what is launch and async
    - launch: starts a new coroutine that does not return a result, returns a Job, used for fire-and-forget tasks
      - story: for single api calls or database writes in the view model where we don't need a return value 
    - async: starts a new coroutine that returns a result, returns a Deferred<T>, used for concurrent tasks that produce a value
      - story: we have an async case for the home screen to load the promotions, user info, recommendations, and categories in parallel using async, then awaited all results before updating the UI state
7) a unit test fails in the cicd pipeline, how would you go about debugging
    - check the error message and stack trace to identify the failure point
    - run the test locally to reproduce the issue
    - check recent code changes that may have caused the failure
    - verify test dependencies and environment setup
    - add logging or breakpoints to isolate the problem
    - fix the issue and rerun the test to confirm
    - investigate flaky tests if the failure is intermittent
    - This is a good walkthrough. In an interview, keep it succinct and emphasize: reproduce locally, isolate root cause, fix with a test, and prevent regressions (e.g., by tightening assertions or improving test data). They want to hear that you treat CI failures as a priority and don’t just “re-run until green.”
8) android flavors
    - product flavors allow you to create different versions of your app from a single codebase
    - used for different environments (dev, staging, prod), feature variants (free vs paid), or branding (white-label apps)
    - configured in the build.gradle file with specific settings for each flavor (applicationId, resources, dependencies)
    - build variants are combinations of product flavors and build types (debug, release)
    - Interviewers usually want to hear that you’ve used flavors in practice, not just definitions. 
      - For example: “At Neiman Marcus we had `dev`, `uat`, and `prod` flavors with different base URLs, feature flags, and analytics keys. QA used the UAT flavor against staging backends, while `prod` was locked down and tied to production services.”
9) agile methodology
    - iterative approach to software development focused on collaboration, flexibility, and customer feedback
    - story: at neiman marcus we followed agile practices with 2-week sprints, daily standups, sprint planning, and retrospectives
      - This is a good story. You can add 1–2 specifics: how you broke work into user stories, how you used sprint reviews to get feedback from stakeholders, and how retrospectives led to concrete improvements (e.g., smaller PRs, better estimation, or more realistic sprint planning).
10) leading teams and mentorship
    - experience leading small teams of 4 engineers
    - mentored junior developers through code reviews, pair programming, and regular check-ins
    - focused on knowledge sharing and fostering a collaborative environment
    - story: in my current role we've had a single junior dev join the team, i paired with them on onboarding, helped them understand our architecture and coding standards, and reviewed their code to provide constructive feedback
      - It helps to have a specific success story, e.g., “Over a few months, they went from only taking small bug fixes to owning an entire feature (like the wishlist or profile screen) end-to-end. I supported them by co-writing the first feature, then gradually stepping back while still reviewing and unblocking them.”
      - the junior dev was already into animations and ui/ux so i paired with them on implementing some of the parallax effects on the home page which helped them grow their skills and confidence
      - they went from only taking small bug fixes to owning a feature even if it was just the parallax effects on the home page
![img.png](img.png)

### James
[S3](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-07-James-Halsten-Amazon-R1-P2.mkv&bucket=storage-solution)
- video does not exist


### Mike
[S3](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-12_R1_Mike_Amazon.mkv&bucket=storage-solution)
- starts at 6ish
- Mike is muted 
- this is a round 1

- are you primarily kotlin or java?

- what are data classes in kotlin?
  - concise way to create classes that hold data
  - less boilerplate than regular classes
  - auto generates equals(), hashCode(), toString(), copy(), componentN() functions
    - he follows up asking about functions of data classes, these above

- familiar with launch and async in coroutines?
- where would you use them?
- using combine()?

- example of an extension function
  - String.isValidEmail(): Boolean {
      return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
  - ProductDto.toDomainModel(): Product {
      return Product(id = this.id, name = this.name, price = this.price)
    }

- where are you currently located?
  - dallas tx
- project is coming to an end?
- 

### Janvi - Interviewer Anna
[S3](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-21_R2_Janvi_Amazon.mkv&bucket=storage-solution)
- starts at 3:50

- Any experience with KMM and Compose?
  - I imagine she'll ask about my experience with KMM -> have narrative ready
  - weekend homework is finishing the KMM videos

- do you have any iOS experience? 
  - not looking for you to code, just working with iOS devs on cross platform projects
  - say yes, working with iOS devs on KMM project and other places where we had to coordinate api contracts, ui/ux parity, etc.
    - this would be normal for a lead role
    - have i written swift? no, but I've read some swift code while working with iOS devs

- you are tasked with building a compose screen with multiple cards, how would you handle recompositions?
  - break down into smaller composables for each card -> only recomposes if state for that card changes
  - state hoisting -> only pass down the data necessary for that card
  - use remember for local state within the card
  - use keys in lazy lists to avoid unnecessary recompositions
  - use derivedStateOf for expensive calculations based on state
  - launch side effects in LaunchedEffect or rememberCoroutineScope 
  - marking composables as @Stable or @Immutable if their inputs don't change often or at all

- are you familiar with side effects in compose? what do you use them for?
  - they are code that runs in response to state changes or lifecycle events that we may not want to run on every recomposition
  - we use effect handlers like LaunchedEffect, SideEffect, DisposableEffect, rememberCoroutineScope to manage side effects
    - `rememberCoroutineScope` to launch coroutines tied to the composable's lifecycle
    - `DisposableEffect` to run cleanup code when a composable leaves the composition -> useful for cancelling coroutines or unregistering listeners
    - `SideEffect` to run code after every successful recomposition -> useful for logging or analytics
    - `derivedStateOf` to optimize expensive calculations based on state changes
    - `LaunchedEffect` to run suspend functions when certain keys change -> useful for one-time operations like fetching data or animations

- what are lifecycles of activities?
  - onCreate, onStart, onResume, onPause, onStop, onRestart, onDestroy, 

- what is setContent in android? Why is it called in onCreate and not in onStart or onResume?
  - setContent is the entry point that attaches a Compose Composition to your Activity (or Fragment/ComposeView) and tells it: “here is the root composable tree.” After that, recomposition is driven purely by state changes, not by calling setContent again.
  - Those callbacks can run many times (e.g., when you navigate away and come back, or on configuration/lifecycle transitions).
    - Each call would tear down and rebuild the whole composition tree.
    - That defeats Compose’s optimizations and can cause bugs (lost remembered state, flicker, duplicated side effects).

- how many times can onCreate be called in the lifecycle of an activity?
  - only once when the activity is first created
  - if the activity is destroyed and recreated (e.g., due to configuration changes), onCreate will be called again for the new instance
    - can use ViewModel or rememberSaveable to persist state across recreations
    - remember is only for recompositions, not recreations

- what is the MVVM pattern?
  - Model-View-ViewModel
  - Model has our data and domain layers where we have our API calls, database access, business logic, use cases, etc.
  - View is our UI layer where we have our activities, fragments, composables that display data and handle user interactions
  - ViewModel is the mediator between Model and View, holds UI state, handles business logic, exposes data to the View via LiveData or StateFlow, and processes user actions from the View
  - advantage is unidirectional data flow, separation of concerns, easier testing, better state management
    - with unidirectional, data flows from Model -> ViewModel -> View, and user actions flow from View -> ViewModel -> Model

- if you want to get data from a database what layer would that happen in?
  - model layer -> different parts, but I usually build with a repository-pattern where the repository is the central point for data access for our view models, if it comes from a remote or local source the repository handles that logic

- if you want to run multiple calls concurrently how would you do that?
  - using coroutines for concurrency and asynchronous programming

- do you know the difference between launch and async?
  - launch starts a new coroutine that does not return a result, returns a Job, used for fire-and-forget tasks
    - save to database, logging, updating UI state
  - async starts a new coroutine that returns a result, returns a Deferred<T>, used for concurrent tasks that produce a value
    - if we have a dashboard with multiple api calls to load user info, notifications, messages, etc. we can use async to start all calls concurrently and then await all results before updating the UI
    - when we need to perform multiple independent network requests in **parallel** and **combine** their results

- code challenge, what is the result of running this?
  - RESULT: A C D B
```kotlin
fun main() = runBlocking {
    launch {
        println("A")
        delay(50)
        println("B")
    }
    launch {
        println("C")
        delay(10)
        println("D")
    }
}
```

- do you work with a gradle build system for android? Are you familiar with the difference between build types vs flavors?
  - build types define different versions of the app for different purposes (debug, release)
    - debug build type has debugging features enabled, logging, no code shrinking
    - release build type has optimizations enabled, code shrinking with R8/Proguard, signing configs for production
  - flavors define different variants of the app for different environments or features (dev, staging, prod)
    - each flavor can have its own applicationId, resources, dependencies
    - add different constants through BuildConfig for each flavor (e.g., API endpoints, feature flags)
    - or exclude certain features or modules for specific flavors
  - build variants are combinations of build types and flavors (e.g., devDebug, prodRelease)

- how do you use AI tools in your day to day work?
  - At Neiman Marcus we’re encouraged to use AI tools, but always within clear company guidelines around security and data privacy. Day to day, I treat AI as an assistant, not an author.
  - For brainstorming, I might use AI to generate ideas for feature implementations or UI designs, then critically evaluate and adapt those suggestions.
  - sometimes for boilerplate or creating fake data for tests, I might use AI to generate code snippets, but I always review and modify them to ensure they meet our coding standards and security requirements.
  - We've used some github-copilot for assisting with PR reviews
    - it doesn't replace code reviews, but it can help catch common issues or suggest improvements faster - we can look at what it finds and decide if it's valid or not
  - Our enterprise Copilot is configured to sanitize snippets—stripping out keys, secrets, and identifiers—and follows our governance policies so nothing leaks outside the org

- if we were to ask you to lead a team? (novice, mid, senior) how comfortable would you be? what kind of tasks would you give them?
  - i would feel very comfortable leading a team
  - i would focus on clear communication of goals, setting expectations, and providing support
    - help quickly unblock any technical issues
    - facilitate collaboration and knowledge sharing
    - why certain approach or architecture decisions were made -> help them grow and be more independent over time
  - i would assign tasks based on each team member's strengths and growth areas
    - for junior devs, i would give them smaller, well-defined tasks with clear requirements and provide mentorship
    - for mid-level devs, i would give them more complex features or components to own end-to-end
    - for senior devs, i would involve them in architecture decisions, code reviews, and mentoring others

### Xavier
[S3](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-11-21_R2_Xavier_Amazon.mkv&bucket=storage-solution)
5 min video


### Daniel
[NextCloud](https://nextcloud-talk-recordings-itc-eit.s3.amazonaws.com/recordings/interviews/12-12-25-6-00PM-%20R1%20-%20Daniel%20A%20-%20Amazon%23-%231462182%23-%23INTERVIEW%23-%23TC/Recording%202025-12-12%2017-49-14.webm?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAWIHTASZ3FCQNXFGX%2F20251212%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251212T185301Z&X-Amz-Expires=604800&X-Amz-Signature=361a753053843f415bba3e4b5cda3ac662e8d1b48eec7da04f888459d3870789&X-Amz-SignedHeaders=host)



### Willard
[NextCloud](https://nextcloud-talk-recordings-itc-eit.s3.amazonaws.com/recordings/interviews/25-11-25-9-00PM-%20R1%20-%20Willard%20C%20-%20Amazon%23-%231459485/Recording%202025-11-25%2020-52-27.webm?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAWIHTASZ3FCQNXFGX%2F20251212%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251212T160115Z&X-Amz-Expires=604800&X-Amz-Signature=15600e5e6d26b4341386228cfd872ccfb2d747ee358dfa4a47f9d6577e5fc67e&X-Amz-SignedHeaders=host)



## Final Round Questions:
### Time where you had to collab with a designer or a PM
What they want:
- Can you communicate well with non-engineers?
- Do you push back respectfully and negotiate scope/UX/technical constraints?
- Do you think in terms of user impact and business goals, not just code?

ANSWER:
- Context: “We were redesigning the home page with richer media and personalization.”
- Collaboration: “Design wanted a different complex animations for each section and PM wanted it live before a seasonal campaign.”
  - horizontal pager with parallax images
    - Parallax images are images that move at a different speed than the foreground content as the user scrolls or swipes, creating a depth effect (background moves more slowly than the foreground). In our case, product images in the background scroll slightly offset from the cards/text to give a premium, layered feel.
- What you did: “I walked them through performance constraints on low-end devices and proposed picking 2 animations for reusability and a phased rollout. I also suggested we reuse existing components to hit the date.”
- Result: “We shipped on time, hit the campaign window, and the experience still felt premium without blowing up engineering effort.”

### How did you deal with maintainability?
What they want:
- Do you design/code so others can work on it easily later?
- Can you explain concrete practices, not just “we follow clean code”?
- Do you think about refactoring, modularization, tests, and reducing tech debt?
  - tech debt is any code that is not maintainable or scalable in the long term 
    - some decisions are made to hit deadlines but need to be revisited later so we tag them as tech debt
    - examples: duplicated code, lack of tests, poor architecture, lack of documentation, etc.

NOTES:
- modularization (CLEAN)
  - built modules around features (auth, product listing, cart, checkout)
  - faster build times, clearer ownership boundaries, easier testing
  - have debated breaking product listing into smaller modules (search, filters, recommendations) but held off for now due to overlap
- separation of concerns (MVVM, repository pattern)
  - Repository pattern -> hides if data comes from network or local db (cache). One source of truth for data.
  - use cases -> verbs (GetProducts, AddToCart, etc.) that orchestrate repositories and business logic. Rules live here (pricing, inventory checks, eligibility, retries analytics hooks, etc.)
- shared composable functions and themes
  - shared card component for product listing
  - shared button and dropdown components 
  - catalog of shared horizontal pager component with different parallax effects we can plug and play with
- documentation
  - reasoning for architecture decisions 
  - how to add new features or components
- CI
  - static analysis with detekt/ktlint
  - unit tests for ViewModels and repositories
  - UI tests for critical flows with Espresso

ANSWER:
    I’ve treated maintainability as a combination of architecture, reuse, and guardrails, not just ‘clean code’.
Architecturally, we broke the app into a few feature‑based modules—auth, product listing, cart, checkout—rather than a single monolith. That gave us clearer ownership and faster incremental builds without overwhelming the team with dozens of tiny modules. Each feature used a consistent MVVM + use case + repository structure: ViewModels handle UI state, use cases encode business rules like GetProductList or PlaceOrder, and repositories hide whether data comes from network, cache, or local DB. That separation made it easy to change backends or data sources without touching the UI.
To keep the UI maintainable, we invested in shared composable components and theming: a single product card, shared button and dropdown components, and a reusable horizontal pager with pluggable parallax effects. That reduced duplicated UI logic and kept design changes localized.
From a process perspective, we enforced standards with CI: Detekt/ktlint for style and smells, unit tests around ViewModels and repositories, and Espresso tests for critical flows like login and checkout. We treated tech debt explicitly—legacy god classes, duplicated networking code, and inconsistent UI components were tracked in the backlog and refactored incrementally as we touched those areas.
As a lead, my focus was to make sure new work fit these patterns, that modules stayed loosely coupled, and that we always had enough tests and automation in place so other engineers could safely extend the app without fear of breaking core flows.”

### Example of a short cut you took when you were under a time crunch
What they want:
- You are honest about tradeoffs and not dogmatic.
- You know when to cut corners and how to contain the risk.
- You always think in terms of: impact, risk, and follow-up cleanup.

Intentional shortcuts:
- Duplicating a small piece of code instead of over-abstracting to hit a deadline.
- Shipping without full test coverage but adding smoke tests for the critical path.
- Implementing a simpler UX variant for v1, planning v2 once metrics are validated.

ANSWER:
- Context: “We had to add a new promo banner flow before Black Friday, but the ideal solution required refactoring the navigation stack.”
- Shortcut: “To hit the date, I added a small, isolated navigation path and repeated some logic instead of refactoring everything.”
- Risk management: “I wrapped it behind a feature flag, added basic UI tests, and documented it as tech debt in our backlog.”
- Follow-up: “After the sale, we refactored the navigation properly and removed the duplication. The short-term hack let us capture the revenue opportunity without destabilizing the rest of the app.”

---

Live coding
1. android with payment dates with an api
2. design how you would fetch the data from the api

--- 

Follow up questions for regarding the live coding
1. Why is  "Loading" an object when it comes to a sealed class
   - In a sealed class hierarchy that models UI state (e.g., `Success`, `Error`, `Loading`), we often make `Loading` an `object` because it has **no data** and there is only one logical instance of it. Using an `object` avoids unnecessary allocations and makes intent clear: `Loading` is just a marker state, not a value that changes.
2. Api how would you make that available to multiple view models
   - use repository pattern and DI with Hilt
   - The interviewer is looking for separation of concerns and DI: define a single `Repository` (or API service) interface, provide a singleton instance via Hilt, and inject it into any ViewModel that needs it. That way, multiple ViewModels share the same API client/repository instead of each creating its own, and you can easily mock it in tests.
3. How would we use hilt
   - In addition to explaining `@Inject`, `@Module`, `@Provides`, and `@Singleton`, mention the big picture:
   - TODO: I imagine this is more than explaining @Inject, @Module, @Provides, @Singleton, etc., what else should I cover?


## Questions I want to Ask
1. Are you trying to build something similar to Starlink?
2. Are there any features beyond what they have in Starlink that you are trying to build?
3. Cool feature for checking for obstacles when setting up the dish
    - how would you build that feature?
    - CameraX, ML Kit, ARCore?
    - ARCore for environment mapping?

PROBLEM:
- capture images of specific views above the dish
- analyze images for obstacles (trees, buildings, power lines)
- provide real-time feedback to user during setup
- suggest optimal dish placement (90% clear view of sky)

TECH:
- ARCore for Environment Mapping
  - Use ARCore for:
    - Device pose tracking as the user pans around.
    - Building a point cloud / mesh of the environment.
    - Defining a hemisphere above the dish (virtual sky dome) and marking directions that are blocked by geometry.
  - This is where your “orbs for % sky scanned” idea fits:
    - Divide the sky dome into sectors.
    - Mark a sector as “scanned” when the camera has covered that direction with enough ARCore tracking quality.
    - Color sectors as clear or blocked based on detected obstacles.

- ML Kit or TensorFlow Lite
  - Run an on-device object segmentation/detection model on frames sampled from the camera/ARCore feed.
  - Focus classes: trees, buildings, poles, etc.
  - For a simpler narrative, you can say:
    - “We use a lightweight TFLite model to estimate whether each sky sector is blocked or not, based on the pixels in that direction, and combine that with ARCore’s depth/geometry if available.”

- Real-time feedback and UX
  - As the user moves:
    - Update a sky coverage visualization (e.g., circular radar with colored sectors and a % sky clear number).
    - Overlay AR markers where obstacles are detected.
    - Show messages: “Rotate a bit left; this area has 95% clear view.”

“I’d combine ARCore and on-device ML. ARCore gives me a 3D understanding of the environment and where the user is pointing the phone; I can model the sky as a dome of sectors and track which ones have been scanned. Periodically I run a lightweight TFLite model on frames to detect obstacles like trees and buildings in each direction.
As the user sweeps the phone, I update a percent sky clear metric and visualize it as colored sectors in AR, highlighting blocked areas. From that, I can recommend the best dish orientation or position that achieves something like 90% sky visibility.”

CHALLENGES:
- ARCore tracking reliability
  - Poor lighting, glossy surfaces, or fast movement can break tracking.
  - You might say: you’d show a “tracking quality” indicator and prompt users to slow down or move to better lighting.
- Device fragmentation
  - Not all Android devices support ARCore or have the same camera/IMU quality.
  - You’d mention having a non-AR fallback flow (simple compass + static guidance) and gating AR behind a capability check.
- Performance / battery
  - Continuous camera, ARCore, and ML inference can overheat devices and drain battery.
  - You’d talk about throttling frame processing, using lightweight TFLite models, and stopping processing when the phone is idle.
- Model accuracy / false positives
  - ML model might misclassify clouds or sky noise as obstacles.
  - You’d mention calibrating thresholds, running A/B tests, and always giving users manual override or “this is an estimate” messaging.
- UX confusion
  - Users might not understand how to “scan the sky” or why sectors aren’t filling.
  - You’d describe onboarding tooltips, progress indicators for % sky scanned, and clear guidance like “slowly sweep left/right until the circle is full”.

## Qualification Notes:
Experience in full software development life cycle (SDLC) including:
- coding standards
  - “On each team I’ve been on, we’ve had agreed‑upon Kotlin/Android coding standards—things like package structure, naming, nullability practices, and how we structure ViewModels, repositories, and use cases.
  - We enforced those with ktlint/Detekt and consistent formatter rules in CI, so style issues were mostly automated. That let code reviews focus on correctness, architecture, and performance instead of bikeshedding about formatting.”
- code reviews
  - “I’ve been both a frequent author and reviewer of PRs. We kept PRs small and focused, and used code review to enforce design decisions—like keeping business logic out of Activities/Fragments, or ensuring new APIs are testable and documented.
  - I try to give concrete, actionable feedback and explain the why (readability, performance, or long‑term maintainability), and I’m equally comfortable receiving feedback and iterating quickly.”
- source control management
  - “Day‑to‑day I work in Git feature branches, using pull requests as the main integration point.
  - We followed a trunk‑based / Git‑flow hybrid depending on the team: short‑lived feature branches, protected main/develop, and hotfix branches for urgent production issues.
  - I’m comfortable resolving complex merges, managing release branches, and tagging versions tied to Play Store releases or backend API versions.”
- build processes
  - “On Android I’ve owned Gradle configuration for flavors and build types—setting up separate dev/uat/prod environments, enabling/disabling logging and crash reporting per build type, and wiring things like code shrinking (R8/ProGuard) and signing configs for release.
  - I’ve also worked with CI (GitHub Actions/Jenkins) to automate builds on every PR, run tests, generate artifacts, and upload signed AABs to internal testing tracks in the Play Console.”
- testing
  - “I usually think in terms of a pyramid: unit tests for ViewModels/repositories, integration tests for networking and persistence, and UI tests for critical flows.
  - On Android I’ve used JUnit and MockK/Mockito for unit tests, Espresso for UI tests, and sometimes Robolectric for headless runs.
  - In KMM projects we ran shared tests on the JVM and iOS simulator targets. CI runs these suites on each PR so we catch regressions early before they hit QA or production.”
- operations experience
  - “On the operations side, I’ve been involved from build to production: configuring crash reporting (Crashlytics/Sentry), performance monitoring (Firebase Performance/Macrobenchmark in lab), and feature flags.
  - For releases, I’ve handled Play Store uploads, track‑based rollouts (internal → alpha/beta → production), and monitored crash‑free sessions and vitals after each release.
  - When issues came up, I used logs, stack traces, and analytics to triage, create focused fixes, and coordinate hotfix releases with product and QA.”


## Preparation Topics:
- Neiman - KMM (Kotlin Multiplatform Mobile), Ktor, Koin,
  - challenge with KMM - did i use compose multiplatform? no
  - which modules are KMM? STRINGS, COLORS, DIMENS (font sizes, paddings, margins, etc.),
- Honda - BLE, AAOP (Android Automotive Open Project),
- Zoom - RN bridging concepts, native modules,
- Gradle - Flavors vs Variants add to BuildTypes in gradle
  - flags for different build types -> can enable/disable logging, crash reporting, payments, etc.
  - Flavors are prod, uat, dev
    - uat - are where dev and prod endpoints are combined
      - use for testing features before pushing to prod
  - Variants are debug, release


---

## KMM (Kotlin Multiplatform Mobile)
At Neiman Marcus, we **did not rewrite the entire app in KMM**. Instead, we used KMM very deliberately for **shared, non-UI modules** where code reuse delivered the most value without fighting each platform’s native UI.

### What we actually shared with KMM

When I joined, the app was 100% native on both Android and iOS. Both platforms had their own implementations of:
- Input validation (email, password, forms)
- Serialization / deserialization
- Networking calls and error mapping
- Local data caching
- Theming values (colors, spacing, typography constants)

That duplication led to inconsistencies—especially in **authentication**, where Android and iOS could behave differently against the same backend.
Leadership wanted to validate KMM on a **contained but business‑critical slice** of functionality, so we chose **sign‑up / sign‑in** as our first KMM pilot.

We focused KMM on a few core areas:

- **Auth and business logic**
  - Shared **field validation rules** (email format, password policy, error messages).
  - Shared **request/response models** and domain models for sign‑up / sign‑in and basic session handling.
  - Networking layer implemented with **Ktor** (REST calls, auth headers, retries, error mapping).
    - TODO: should we be using GraphQL instead of REST to reduce payload size and parsing time? Since KMM is known for being slower on serialization/deserialization.
  - Centralized **error types** for auth (invalid credentials, locked account, network/server issues) so Android and iOS reacted consistently.
    - TODO: this is just 200 vs 400 vs 500 error codes from backend right? or is there more to it?

- **Design system foundations & config**
  - Shared models for **colors, typography, spacing, and component tokens** so Android and iOS stayed visually consistent.
  - Centralized design tokens (e.g., `PrimaryColor`, `ErrorColor`, `BodyTextSmall`, `SpacingXL`) inside KMM and mapped them to platform‑specific types (Android `Color`/`TextStyle`, iOS `UIColor`/`UIFont`).
  - Shared **formatting and localization rules** relevant to auth (masking, error copy rules), while each platform still used its own string resources.

- **Utilities and cross‑cutting concerns**
  - Shared helpers for **analytics event definitions**, logging contracts, date/time utilities, and currency formatting.
  - Each platform wired these contracts into its own logging/analytics SDKs.

Native UI layers remained **platform‑idiomatic**:
- Android used **Jetpack Compose** for the auth and shopping flows, built on top of shared KMM view‑model‑like state.
- iOS used **SwiftUI/UIKit**, consuming the same shared models and business logic.

Only a subset of the app (auth + some core business rules and design tokens) was migrated to KMM during this phase; other modules stayed fully native.

---

### My role with KMM at Neiman Marcus

I was brought in to help **design and implement the shared KMM layer** for authentication and related core logic, and to integrate it cleanly with existing native apps.

Concretely, I:

- Worked with Android, iOS, and backend engineers to **identify high‑ROI areas for sharing**—starting with sign‑up/sign‑in validation, networking, and error handling—rather than trying to move everything to KMM at once.
- Defined the **KMM module boundaries** so that Android and iOS could adopt shared code incrementally: auth and core design tokens first, then additional business rules where it made sense.
- Implemented the shared **auth business logic layer** in KMM: validation, Ktor networking, DTOs, error mapping, and basic session models.
- Developed native Android UI with **Jetpack Compose** and wired it to KMM state using ViewModels, coroutines, and Flows.

On Android, the structure looked like:
- **KMM shared module** → exposes auth APIs and domain models.
- **Android ViewModels** → adapt KMM flows and results into Compose‑friendly state.
- **Compose UI** → renders fields, errors, loading state, and navigation based on ViewModel state.

I also partnered with the iOS team to:
- Expose the same shared logic to Swift/SwiftUI via KMM‑generated frameworks.
- Shape KMM APIs so they felt **Swifty** (e.g., wrapping some lower‑level KMM types in iOS‑friendly facades).

---

### Android‑only responsibilities

While KMM owned the shared logic, several concerns remained strictly Android‑specific and I owned those pieces:

- **Secure token storage**
  - KMM defined an abstract interface for session persistence via `expect/actual`.
  - On Android, I implemented the `actual` using **Android Keystore** plus encrypted DataStore to store refresh tokens and session identifiers securely.

- **Lifecycle, navigation, and side effects**
  - KMM is UI‑agnostic and doesn’t know about `Activity`, `NavController`, or `WorkManager`.
  - I handled:
    - Navigating from sign‑in → home flows after successful authentication.
    - Showing snackbars, dialogs, and other Material components in response to KMM events.
    - Scheduling background work (e.g., analytics flush, token refresh) through WorkManager based on shared business events.

- **Platform‑specific integrations**
  - Android‑only features such as **biometrics**, OS‑level notifications, and SSL pinning were implemented in the Android layer, consuming shared KMM domain rules where applicable.

---

### Performance and stability work

Early in the KMM rollout, we encountered performance and stability challenges, especially around threading and initialization:

- Ktor and coroutines needed correct dispatcher configuration to avoid blocking UI threads on either Android or iOS.
  - TODO: what does this mean exactly? it sounds like iOS has issues with coroutines? if we put API call on IO dispatcher, does that block main thread on iOS?
  - TODO: is this a common KMM issue or specific to our codebase?
- Initial JSON serialization/deserialization was heavier than necessary on some devices.
- First‑time initialization of the shared module added overhead during app startup.
  - TODO: is that because KMM uses SKIA compiled code which has some cold start overhead?

What I did to address this:

- **Dispatcher tuning:** ensured all network and parsing work happened on background dispatchers, with only UI updates on the main thread.
- **Serialization optimization:** standardized on **kotlinx.serialization** with preconfigured serializers in Ktor to reduce overhead.
  - TODO: would it be better to use GraphQL instead of REST to reduce payload size and parsing time?
- **Lazy initialization:** deferred non‑critical KMM initialization until the user actually entered auth flows, rather than on cold start.
  - TODO: just want a couple examples of what kind of initialization we deferred?
- **Interop coordination:** worked with iOS developers so Swift concurrency and KMM coroutines interacted cleanly, preventing UI freezes when calling into shared code.

As a result, we reduced auth flow latency by roughly **15–20%** across both platforms compared to the initial KMM prototype.

---

### Security and consistency across platforms

We kept a clear split of responsibilities to balance **shared consistency** with **platform‑specific security**:

- **Shared (KMM) layer:**
  - Input sanitization and validation logic for sign‑up/sign‑in.
  - Parsing and domain rule enforcement for auth responses.
  - Ensuring consistent security‑relevant checks (e.g., lockout rules, error handling) across Android and iOS.

- **Android layer:**
  - Keystore‑based token storage, SSL/TLS configuration, biometrics integration, and secure logging/redaction.
  - Ensuring no sensitive data was written to logcat and that crash reports were scrubbed of secrets.

- **iOS layer (collaboration):**
  - Similar responsibilities via Keychain, iOS networking stack, and iOS biometrics, aligned with the same shared business rules.

This approach let us **share what should be identical** while respecting each platform’s security model and APIs.

---

### Collaboration and CI/CD

KMM only works well if both platforms and backend agree on contracts. I helped drive that alignment:

- **Shared contracts:** co‑designed API models, error types, and business rules with backend, Android, and iOS so everyone consumed the same domain objects.
  - TODO: I just want some clarity on the idea of shared contracts. is this just about agreeing on API request/response models and error codes? or is there more to it?
- **iOS interop:** worked with iOS devs to generate and wrap KMM frameworks so they integrated naturally into Swift/SwiftUI.
- **Unified testing:**
  - Shared unit tests in the KMM module (run on JVM and iOS simulator targets).
  - Android instrumentation tests for auth UI flows.
  - iOS XCTest coverage around interop layers.

- **CI integration (GitHub Actions):**
  - Every KMM change triggered builds for Android and iOS targets.
  - Ran the shared test suite plus key platform‑specific tests.
  - Published the updated shared artifact/framework for both apps to consume.

This made KMM a **first‑class part of the delivery pipeline** rather than an experiment living off to the side.

---

### Short interview summary (KMM experience)

"At Neiman Marcus we had fully native Android and iOS apps that duplicated a lot of logic, especially around authentication.
I helped lead a Kotlin Multiplatform Mobile pilot focused on the sign‑up/sign‑in flows.
We used KMM to share the non‑UI parts—validation rules, Ktor networking, DTOs, error mapping, and some design tokens—while keeping Jetpack Compose on Android and SwiftUI on iOS for the UI.
On Android I built the Compose auth screens and introduced a bridge from KMM into ViewModels and UI state, and I owned Android‑only concerns like Keystore‑backed secure storage, navigation, lifecycle integration, and background work.
We hit some early performance and interop issues, but by tuning dispatchers, using kotlinx.serialization, and deferring heavy initialization, we reduced auth latency and made the shared module feel native on both platforms.
I also worked closely with the iOS and backend teams on shared contracts and testing, and we wired KMM into GitHub Actions so every change was built and tested on both platforms.
The result was less duplicated logic, more consistent behavior across platforms, and a clear path to extend KMM beyond auth if we chose to."



---



## React Native
At Zoom, I contributed to the development, enhancement, and optimization of the mobile application, delivering high-quality user experiences during a period of massive user growth.
My work spanned both Android (native) and React Native modules, ensuring seamless cross-platform performance, optimized rendering, and real-time responsiveness across features.

•	Led development of major React Native modules (profiles, feeds, content creation, messaging), ensuring scalable cross-platform experiences across Android and iOS.
•	Integrated and optimized Zoom’s real-time meeting features—video/audio sessions, participant workflows, chat, and screen sharing—using the native Android SDK and React Native layers, with a focus on UI performance, threading, and ensuring correct state sync with the underlying WebRTC native engine.
•	Improved performance and scalability during massive global user growth by optimizing rendering devices
•	Enhanced reliability through deep performance profiling (Perfetto, Systrace), reducing ANRs and crash rates and resolving rendering and navigation bottlenecks under heavy load.
•	Implemented predictable state management with Redux and integrated RESTful APIs and proprietary Zoom SDKs for authentication, meeting management, cloud recording, notifications, and analytics.
•	Ensured cross-platform consistency by maintaining robust React Native ↔ Native bridges and aligning shared JS/TS modules with Android-specific behaviors.
•	Delivered accessible, high-quality UI/UX using React Native and native Android views, supported by comprehensive testing (Jest, JUnit, Espresso) to ensure stability across phones and tablets.

---

### Primary Narrative (React Native–Focused)

**“Yes — at Zoom, a significant part of my work involved React Native.
I wasn’t just writing React Native UI; I owned several cross-platform modules and built the native ↔ RN bridges that connected them to Zoom’s native Android SDK.**

I led the development of major React Native features like profiles, feeds, messaging, and content creation, ensuring they behaved consistently on both Android and iOS while still integrating deeply with Zoom’s native video, audio, and meeting workflows.

A big part of my role was optimizing the RN performance layer:

* reducing unnecessary bridge crossings
* rewriting unstable bridges
* making sure JS state stayed in sync with the native WebRTC engine
* and improving rendering times during heavy real-time video usage.

I also implemented predictable state management with Redux, wired up authentication and meeting APIs, and rebuilt parts of the navigation and lifecycle handling to reduce ANRs and crashes under massive user growth.
Overall, my cross-platform experience wasn’t superficial — I worked at the intersection of RN and native code and often had to debug both sides at once.”**

---

### Follow-Up Questions + Killer Answers

#### **1. How exactly did you use React Native at Zoom?**

Use this:

“I owned multiple RN screens end-to-end — profile, settings, messaging, content creation.
But the more complex work was building and stabilizing the native ↔ React Native bridges so those screens could interact with the Zoom native SDK.

This included:
* exposing native meeting APIs to the JS layer
* ensuring lifecycle events flowed correctly
* handling threading and state sync
* optimizing performance during UI-heavy or video-heavy tasks.”**

---

#### **2. What challenges did you face using React Native in a real-time application?**

Use these three:

##### **Challenge 1 — Bridge Race Conditions**

**Problem:** Early RN bridges weren’t lifecycle-aware → JS would request meeting state before the native SDK had initialized.
**Fix:** Rewrote bridges to use standardized thread marshaling + defensive lifecycle checks.
**Result:** Eliminated inconsistent meeting states and reduced random UI freezes.

---

##### **Challenge 2 — Performance Bottlenecks**

**Problem:** RN rerendering would sometimes collide with native video rendering.
**Fix:**

* reduced bridge calls
* batched events
* memoized and stabilized RN components
* shifted heavy work back to native threads
  **Result:** Cut rendering stalls during large meetings and improved perceived smoothness.

---

##### **Challenge 3 — Device Fragmentation (2018 Android ecosystem)**

**Problem:** Samsung + Xiaomi models crashed during gallery ↔ active-speaker transitions.
**Fix:** Improved surface lifecycle timing + added decoder fallback logic.
**Result:** Crash rate dropped significantly in international markets.

---

#### **3. Can you explain how the React Native bridge works?**

“React Native runs JS in a separate VM.
When JS needs native work — camera access, navigation, WebRTC events — it serializes the call and sends it across the bridge to native code.
Native executes the work and then sends results back over the bridge.

Performance comes down to minimizing bridge crossings and ensuring heavy operations run natively or on background threads.”**

---

#### **4. How did you ensure cross-platform consistency?**

“I kept JS/TS modules shared across both platforms, but wrapped platform-specific behavior behind native modules.
I tested each feature on both iOS and Android, validated UI parity, and ensured Redux state updates behaved identically across platforms.”

---

#### **5. Why would you choose React Native over native?**

“RN is great for shared UI, fast iteration, and productivity when both platforms share similar flows.
But anything performance-critical — video, hardware acceleration, deep system APIs — I implemented natively.
At Zoom we used a hybrid approach: cross-platform where possible, native where necessary.”

---

### Short “Story Nuggets” You Can Drop in an Interview

#### **Story: Fixing lifecycle-race issues between RN and Android native**

* Early RN integration didn’t handle Android lifecycle correctly
* Meeting state arrived before RN was ready
* JS layer displayed stale or incorrect participant state
* **You rewrote the bridge to enforce lifecycle-safe event ordering**

---

#### **Story: Improving performance during large meetings**

* RN components rerendered unnecessarily during high-frequency events
* You added memoization, batched events, and pushed heavy operations into native
* **Result: Smoother transitions between gallery/active speaker**

---

#### **Story: Debugging fragmentation issues with hardware decoders**

* Some OEM devices couldn’t keep up with surface switches
* You implemented fallback rendering paths + timing fixes
* **Result: Reduced crashes in major global markets**

---

### A Polished, Interview-Ready “Summary Statement”

**“Yes, I have substantial React Native experience.
At Zoom, I developed major RN modules, maintained native ↔ RN bridges, and optimized cross-platform performance during a period of massive user growth.
Most importantly, I worked in a hybrid environment — building shared JS modules while also integrating tightly with native Android features like WebRTC, meetings, and navigation.
This gave me hands-on experience balancing cross-platform efficiency with native-level reliability and performance.”**

---

