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
### Wills
- publishing to play store -> 
  - signed AAB (Android App Bundle) 
  - track based releases (internal, alpha, beta, production)
  - R8 and Proguard for code shrinking and obfuscation

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
  - e.g. use for 

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

