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

## Pitch:
Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

At Neiman Marcus, I was brought in to modernize and scale the app.
I:
- Re‑architected the app into Clean MVVM Architecture with feature‑based Gradle modules, which sped up builds and made releases more predictable.
- Led the move to Jetpack Compose, starting with a hybrid XML+Compose approach and then fully composing new features, which cut UI development time and improved design parity with Figma.
- Improved performance and stability by profiling with Android Studio and Firebase, then introducing lazy loading, Coil for images, and better background initialization.
- Hardened security with SSL pinning, token-based auth, and biometrics, and set up CI with GitHub Actions for automated testing.
- led and developed a KMM module for sign-up and login as a PoC to evaluate cross-platform code sharing for iOS and Android.

Before that, at Ally Bank, I worked on the "One Ally" ecosystem, bringing banking, auto, investing, and mortgage into a single app.
There I:
- Implemented secure login and authentication flows combining biometrics with MFA, ensuring compliance with PCI-DSS, FDIC, GFCR, and CFPB.
- Built modular, Kotlin-based features for snapshot, fund transfer, and bill pay using MVVM, Coroutines, Retrofit, and Room with Jetpack Compose.
- Also developed the mobile check deposit feature using CameraX, image processing, and secure upload.

I really enjoy collaborating with other engineers to build useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name]
would be a fantastic place to continue growing my career and contribute.





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
  - onCreate()
    - Called once when the Activity is first created.
    - Inflate the layout, initialize UI/view binding, set up ViewModels, DI, and one-time configuration.
    - Do not start heavy work here on the main thread; offload long operations.
  - onStart()
    - Activity becomes visible but not yet in the foreground.
    - Register UI-visible resources: start observing data that affects UI, register broadcast receivers/listeners needed while visible.
    - Lightweight work only; user still can’t interact.
  - onResume()
    - Activity is now in the foreground and the user can interact.
    - Start anything that should run only while the Activity is in focus: animations, camera preview, location updates, sensors, etc.
    - This is where you ensure UI is in its final interactive state.
  - onPause()
    - Another Activity partially obscures this one; it’s losing focus but might still be visible.
    - Commit lightweight, immediate state (e.g., current text, scroll position), pause animations, stop things that must not run without focus (e.g., camera, video recording).
    - Keep it fast; onPause() must complete quickly because the next Activity’s onResume() waits for it.
  - onStop()
    - Activity is no longer visible to the user.
    - Release or detach more expensive resources: unregister receivers, stop location updates, pause ongoing work that only makes sense when visible.
    - Persist deeper state if needed (e.g., draft form, last screen) to survive process death.
  - onRestart()
    - Called when an Activity is coming back to the foreground after onStop().
    - Use it for simple re-initialization or logging; most of your “resume UI” logic stays in onStart() / onResume().
    - Many apps leave this empty and rely on onStart()/onResume().
    - jumps to onStart() next.
  - onDestroy()
    - Final callback before the Activity is destroyed (user finishes it or system kills it).
    - Clean up remaining resources not already released: cancel coroutines, dispose observers, clear references to avoid leaks.
    - Remember: it is not guaranteed to be called if the process is killed; critical state should already be saved earlier (onPause()/onStop()).

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
starts at 18:30
2nd round with Anna


### Willard
[NextCloud](https://nextcloud-talk-recordings-itc-eit.s3.amazonaws.com/recordings/interviews/25-11-25-9-00PM-%20R1%20-%20Willard%20C%20-%20Amazon%23-%231459485/Recording%202025-11-25%2020-52-27.webm?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAWIHTASZ3FCQNXFGX%2F20251212%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251212T160115Z&X-Amz-Expires=604800&X-Amz-Signature=15600e5e6d26b4341386228cfd872ccfb2d747ee358dfa4a47f9d6577e5fc67e&X-Amz-SignedHeaders=host)
starts at 8
2nd round with Anna


## Final Round Questions:


### Mike
1. How would you test Android app features end-to-end to ensure UI behaves correctly during user interactions, including orientation changes and back navigation? 
   - I use **instrumentation UI tests** to exercise real user flows:
     - **Espresso** or the **Compose Testing Library** for in-app UI interactions (clicks, swipes, text input, back navigation).
     - **UI Automator** when I need to interact outside my app (system dialogs, notifications, permission prompts).
   - I design tests around **critical user journeys**:
     - Example: login → browse products → add to cart → checkout.
     - Assert visibility, content, and state at each step.
   - For **orientation changes and configuration changes**: 
     - Most frameworks don’t auto-rotate for you; in practice you **manually rotate** the device/emulator from the test:
       - Instrumentation tests can call `activityScenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }` or use `UiDevice.setOrientationLeft()` / `setOrientationNatural()`.
       - In Compose tests, you can similarly adjust orientation via the Activity or use a custom test rule that recreates the Activity to mimic configuration change.
     - After rotation, I assert that:
       - State is preserved (e.g., cart contents, scroll position, form fields).
       - No duplicate network calls or duplicated fragments.
         - Duplicate fragments can happen if orientation changes trigger Activity/Fragment recreation but the code re-adds a Fragment on `onCreate` **without checking savedInstanceState**. Good tests make sure back navigation and rotation don’t accidentally stack the same Fragment multiple times.
     - In Compose, I verify that state is coming from `ViewModel`/`rememberSaveable` so recomposition + recreation behaves correctly.
   - I run these tests as part of CI so that regressions in navigation/back handling or rotation are caught before release.

2. When designing a cross-platform Android app that validates sign-in, how would you ensure consistent business logic and proper error handling?
  - I’d centralize auth and sign-in rules in a **KMM shared module** so Android and iOS use the same logic:
    - Use Kotlin Multiplatform Mobile (KMM) for input validation, API calls, token handling, and error mapping.
    - Expose a clean API like `signIn(email: String, password: String): SignInResult` that both platforms call.
  - The shared module owns the **core sign-in pipeline**:
    - Input validation (email format, password length/complexity, required fields).
    - Networking with Ktor (calling the auth endpoint, parsing responses).
    - Mapping backend responses into domain models (user session, tokens, profile flags).
    - Interpreting server error codes into domain errors, e.g. `InvalidCredentials`, `AccountLocked`, `MfaRequired`, `NetworkError`, `ServerError`.
  - For **error handling and consistency**:
    - Return a sealed result type from the shared module (e.g. `Success(session)` / `Error(reason)`), so Android and iOS share the same error categories while mapping them to platform-specific UI messages.
    - Log failures in a consistent, privacy-safe way (masked email, region, app version, error type) from the shared layer to help debug cross-platform issues.
    - Add unit tests in the KMM module that cover happy path, invalid input, network errors, and edge cases so behavior is identical across platforms.
  - For **platform-specific concerns**, use `expect` / `actual`:
    - Things like secure token storage, biometrics, and push notification registration stay platform-specific but are accessed through expected interfaces defined in KMM.
    - The shared auth logic calls those abstractions, so the business flow stays shared while implementations differ per platform.
  - If KMM/shared code isn’t an option:
    - I’d push as much of the sign-in logic as possible into a **backend service** (validation rules, error codes) and keep mobile thin.
    - At minimum, maintain a **shared spec** for auth behavior and error contracts, and add automated tests so Android and iOS stay aligned.

3. How do you test the domain and repository layers in an Android app to ensure high code coverage and proper handling of Kotlin coroutines or Flow? 
   - **Testing domain and repositories**:
     - Use **JUnit** for unit tests with **MockK/Mockito** to mock data sources (API, DB, cache).
     - Keep these tests JVM-only (no Android dependencies) so they’re fast and run in CI on every push.
   - **Coroutines**:
     - Use `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`) to control virtual time.
     - Inject a `CoroutineDispatcher` into use cases/repositories so tests can pass a `TestDispatcher` and avoid relying on `Dispatchers.IO/Main`.
     - Verify proper cancellation and error propagation by simulating failures in mocked data sources.
   - **Flow**:
     - Use the **Turbine** library (`flow.test { ... }`) to assert emissions:
       - Check the sequence: `Loading` → `Success` or `Loading` → `Error`.
       - Verify that certain actions (e.g., refresh) trigger new emissions, but no unintended duplicates.
         - Duplicates can happen if the repository re-emits the same state on every call, or if the ViewModel maps streams in a way that causes redundant emissions. I usually assert that making the same call twice without data changes does **not** produce extra `Success` states, or that UI transformations use `distinctUntilChanged`-style behavior where appropriate.
     - Test edge cases: empty lists, errors, retries, and backpressure where relevant.
       - **Backpressure** in Flows means controlling how producers behave when consumers are slow. For `Flow`, this shows up with operators like `buffer`, `conflate`, and `debounce`. In tests, I simulate slow collectors (e.g., with `delay` in the test) and assert that, with `conflate`, only the latest value is delivered, or with `buffer`, upstream isn’t blocked.
       - Common **retry** mechanisms in Flows include `retry`, `retryWhen`, or wrapping the flow in a use case that re-subscribes on failure. In tests, I mock the data source to fail the first N times, then succeed, and assert that:
         - The flow retries the expected number of times.
         - The final emission is `Success` or `Error` according to the policy.
   - Overall, I aim for tests that validate **both happy-path and failure-path behavior** so that concurrency, retries, and error mapping are all covered.
   - **Libraries**:
     - `kotlinx-coroutines-test` and Turbine are **separate libraries**:
       - `kotlinx-coroutines-test` is the **official** way from the Kotlin coroutines library to test suspending code and coroutine scheduling.
       - Turbine (from CashApp) is a popular, lightweight library to test `Flow` emissions ergonomically.
     - Alternatives for Flow testing:
       - Manual collection into a list (`toList()`) and asserting on the list.
       - Custom test collectors or using regular JUnit assertions inside `launch` blocks, but Turbine tends to be much cleaner.

4. Can you describe a time when you encountered a non-crashing but impactful bug in production, and how you approached trade-offs between speed and maintainability in fixing it? 
    - In production, some Neiman Marcus users reported that valid promo codes were showing as “not applicable” at checkout. The app never crashed, but it clearly affected conversion and customer trust.
    - It was hard to reproduce: it only happened for certain combinations of **region, currency, and promo rules**.
    - **Investigation**:
      - I worked with analytics and logging to slice the data by promo code, country, currency, and cart composition.
      - We discovered a pattern: the issue correlated with a new backend field that had been added to promo responses.
        - Concretely, the backend started sending a new field (for example, a `promoScope` or `eligibilityReason` flag). Our client mapper didn’t recognize it, so the discount logic fell back to a conservative branch meant for “unknown promo types,” which rejected some legitimate promos instead of treating the new flag as compatible.
      - On the client side, our discount logic was falling back to a conservative path whenever that field was unrecognized, which effectively rejected some valid promos.
    - **Short-term fix (speed)**:
      - Implemented a **targeted fix** in the existing promo calculation class, behind a feature flag.
        - The feature flag wasn’t per-user; it allowed us to **toggle the new interpretation logic on/off** quickly. We could enable it for all users once we were confident, and disable it if we saw unexpected behavior in specific regions or promo campaigns.
      - Updated the mapping layer to correctly interpret the new field while keeping the previous behavior as a fallback.
        - The mapping layer here is the code that converts **raw API response models** into **domain models** the promotion engine understands. Fixing it meant updating that conversion so new backend fields mapped to the correct domain enums/flags instead of falling into “unknown.”
      - Paired with QA to create **focused regression tests** around checkout flows using high‑value promos, and monitored metrics closely after rollout.
    - **Long-term improvement (maintainability)**:
      - Once the immediate issue was stable, I refactored the discount logic into a dedicated `PromotionEngine` with:
        - Clear separation between raw API models and domain models.
        - Unit tests that covered combinations of currency, region, and promo types.
      - We also **documented the incident** and added observability (structured logs & dashboards) for promo failures and fallback paths, so future changes in backend fields would surface faster.
        - By observability I mean better **logging, metrics, and dashboards**. On Android we logged structured events (promo ID, masked user ID, region, error code) via our analytics/observability stack (e.g., Firebase Analytics/Crashlytics, Datadog/New Relic, or a custom logging pipeline). On the backend, those signals were aggregated into dashboards so product/engineering could quickly spot spikes in promo failures.
    - This approach balanced **speed to protect revenue** with a **follow-up refactor** that made the system more robust and easier to evolve.

5. How do you ensure scalability, maintainability, and faster release cycles in large Android projects? 
    - **Modular architecture**:
      - Split the app into **feature-based Gradle modules** (e.g., `feature-cart`, `feature-profile`, `feature-search`) plus shared core modules.
      - This speeds up builds, enforces boundaries, and allows teams to work more independently.
    - **Clean architecture / MVVM**:
      - Use a layered approach (UI → ViewModel → UseCases/Repositories → Data sources) so business logic is testable and not tied to the UI framework.
      - Rely on unidirectional data flow (e.g., StateFlow) for predictable state management.
    - **CI/CD pipelines**:
      - Configure CI to run unit tests, lint, and instrumentation tests (or a **smoke suite**) on every PR.
        - A smoke suite is a **small set of high-value tests** (end-to-end or integration) that exercise the most critical flows—like login, basic navigation, and a simple checkout—so you quickly know if a build is fundamentally broken without running the entire test matrix every time.
      - Use track-based Play Store releases (internal, alpha, beta, production) with staged rollouts to reduce blast radius.
    - **Code quality practices**:
      - Enforce code reviews, static analysis (Detekt, ktlint), and shared coding standards.
      - Encourage small, focused PRs and good documentation of modules and contracts.
    - **Feature flags and configuration**:
      - Use feature flags/remote config so new features can be tested and gradually rolled out without blocking releases.

6. Can you describe a time when you had to take a deliberate technical shortcut in an Android project? How did you manage the risks? 
   - On one project we had a **new API endpoint** that marketing wanted to surface quickly for a limited-time campaign (e.g., a curated holiday collection in the Neiman Marcus app).
   - Given the tight deadline, we made a conscious decision to **ship a minimal implementation first**:
     - Version 1 had the core happy path only: fetch data from the new endpoint and display it.
     - We deferred full offline support, detailed error states, analytics fine-tuning, and some refactoring that we knew would be ideal.
   - To manage the risks:
     - I clearly **communicated the trade-offs** to product and stakeholders: what we were shipping now, what was intentionally missing, and what the user impact would be if the API failed.
     - We **documented the gaps** as explicit tech debt tickets (e.g., improved retry/backoff, offline caching, richer error UX).
     - Put the new feature behind a **feature flag**, so we could quickly disable it if the endpoint misbehaved without a full app release.
   - After the campaign:
     - We scheduled time to **harden the feature**: add proper error handling, offline/cache behavior, and analytics, and clean up any shortcuts in the implementation.
     - We also looked at what parts of the solution (network layer, UI components) could be **reused** for future seasonal campaigns or similar promo integrations.
   - This showed we could move fast when needed, but in a controlled way, and then come back to invest in maintainability once the immediate business goal was met.

7. How do you structure your Android app to make data sources easily swappable and testable? 
   - I use the **Repository pattern** to abstract data access away from the UI:
     - Define interfaces like `BillsRepository`, `UserRepository`, etc. that expose use-case-friendly methods.
     - Under the hood, repositories coordinate **data sources**: `ApiDataSource`, `CacheDataSource`, `DatabaseDataSource`, etc.
   - **Dependency Injection (Hilt/Dagger)**:
     - Use DI to provide concrete implementations at runtime and allow tests to inject fakes or mocks.
     - For example, in production provide `ApiBillsDataSource`, in tests provide an in-memory fake.
   - **Composition over inheritance**:
     - The repository composes multiple data sources and decides when to read/write from each.
     - This makes swapping a data source (e.g., moving from Room to DataStore for certain data) largely a wiring change.
   - **Testing**:
     - In unit tests, I mock or fake the data sources and assert that the repository correctly handles success/failure, caching logic, and ordering of calls.
       - Here “fake” can mean two things: either **test doubles** that implement the same interface but return in-memory data, or using a mocking framework (MockK/Mockito) to stub responses. I like using simple in-memory fakes for positive paths and mocks for more complex interaction verification.

8. Implement a testable repository in Android to fetch upcoming bills, allowing easy swapping of data sources like API or cache.  
   - I’d separate the concerns a bit more clearly so that **data sources** and **repository** are distinct:
```kotlin
interface BillsDataSource {
    suspend fun getUpcomingBills(): List<Bill>
}

class ApiBillsDataSource(private val apiService: ApiService) : BillsDataSource {
    override suspend fun getUpcomingBills(): List<Bill> {
        return apiService.fetchUpcomingBills()
    }
}

class CacheBillsDataSource(private val cache: BillsCache) : BillsDataSource {
    override suspend fun getUpcomingBills(): List<Bill> {
        return cache.getCachedBills()
    }
}

class BillsRepositoryImpl(
    private val apiDataSource: BillsDataSource,
    private val cacheDataSource: BillsDataSource,
    private val cache: BillsCache,
) : BillsRepository {

    override suspend fun getUpcomingBills(): List<Bill> {
        val cachedBills = cacheDataSource.getUpcomingBills()
        if (cachedBills.isNotEmpty()) {
            return cachedBills
        }

        val apiBills = apiDataSource.getUpcomingBills()
        if (apiBills.isNotEmpty()) {
            cache.saveBills(apiBills)
        }
        return apiBills
    }
}
```
   - This design:
     - Keeps `BillsRepository` as the abstraction the rest of the app uses.
     - Makes each data source testable in isolation.
     - Allows tests to easily swap in a fake API or fake cache implementation.

9. How would you represent a loading state in Android when no additional data (like message or progress) is needed? 
   - I usually model UI state with a **sealed class** so states are explicit and exhaustively handled:
```kotlin
sealed class BillsUiState {
    object Loading : BillsUiState() // object = single instance, no payload
    data class Success(val bills: List<Bill>) : BillsUiState()
    data class Error(val message: String) : BillsUiState()
}
```
   - `Loading` is an `object` because:
     - It doesn’t carry extra data.
     - It represents a single, shared state, so a singleton is appropriate.
   - In Kotlin there is also a `data object` (since Kotlin 1.9), which is like an object but participates in generated functions; for this simple loading case, a plain `object` is sufficient.
   - In Compose or XML, I switch on this sealed class to render the correct UI for each state.

10. How can you structure shared business logic in Android so that multiple ViewModels can access it efficiently using StateFlow?  
11. How can shared business logic be implemented using StateFlow so it can be reused across multiple ViewModels?
   - The idea is to put **shared business logic and state** in a **separate class** (use case/manager), not in a particular ViewModel:
     - For example, a `SessionManager`, `CartManager`, or `VehicleSelectionManager`.
   - That class owns a **`MutableStateFlow`** internally and exposes a read-only `StateFlow`:
```kotlin
class SessionManager @Inject constructor() {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.LoggedOut)
    val sessionState: StateFlow<SessionState> = _sessionState

    suspend fun login(/* params */) {
        // update _sessionState based on result
    }

    fun logout() {
        _sessionState.value = SessionState.LoggedOut
    }
}
```
   - Multiple ViewModels **inject the same instance** (via Hilt/Dagger) and collect from `sessionState`:
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = sessionManager.sessionState
}
```
   - With Hilt, you typically provide `SessionManager` as a singleton in a module:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager = SessionManager()
}
```
   - Yes, the **UseCase/Manager holds the `MutableStateFlow` and exposes `StateFlow`**.
   - ViewModels usually **do not each have their own copies** of that shared state; they just collect or map the shared `StateFlow` into their own UI state.
   - This avoids duplication of business logic and ensures all ViewModels see the same source of truth.

12. How can you use Hilt to provide a singleton repository instance that can be injected into multiple ViewModels in an Android app? 
    - Define the repository implementation and mark it as injectable:
```kotlin
@Singleton
class BillsRepositoryImpl @Inject constructor(
    private val apiDataSource: ApiBillsDataSource,
    private val cacheDataSource: CacheBillsDataSource
) : BillsRepository {
    // ...implementation...
}
```
    - Create a Hilt module that tells Hilt how to bind the implementation to the interface:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBillsRepository(
        impl: BillsRepositoryImpl
    ): BillsRepository
}
```
    - Inject the repository into ViewModels using `@HiltViewModel` and constructor injection:
```kotlin
@HiltViewModel
class BillsViewModel @Inject constructor(
    private val billsRepository: BillsRepository
) : ViewModel() {
    // use billsRepository here
}
```
    - Hilt ensures there is **one singleton instance** of `BillsRepositoryImpl` in the `SingletonComponent`, and all ViewModels that depend on `BillsRepository` get that same instance.

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


When have you had a disagreement with a peer about a technical approach? How did you resolve it?
- aquisition by Sax Global
- at Ally Bank I owned a feature that I started writing in

---

Live coding
1. android with payment dates with an api
2. design how you would fetch the data from the api

---

Follow up questions for regarding the live coding
1. Why is  "Loading" an object when it comes to a sealed class
   - In a sealed class hierarchy that models UI state (e.g., `Success`, `Error`, `Loading`), we often make `Loading` an `object` because it has **no data** and there is only one logical instance of it. Using an `object` avoids unnecessary allocations and makes intent clear: `Loading` is just a marker state, not a value that changes.
   - objects are singletons in Kotlin, so we don’t need multiple instances of `Loading`. It also simplifies equality checks since all references to `Loading` point to the same instance.
2. Api how would you make that available to multiple view models
   - use repository pattern and DI with Hilt
   - The interviewer is looking for separation of concerns and DI: define a single `Repository` (or API service) interface, provide a singleton instance via Hilt, and inject it into any ViewModel that needs it. That way, multiple ViewModels share the same API client/repository instead of each creating its own, and you can easily mock it in tests.
   -
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
