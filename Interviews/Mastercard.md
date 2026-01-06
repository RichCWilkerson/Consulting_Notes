Current Address:
750 Fort Worth Avenue • Dallas TX 75208
Phone:
(214) 329-1138
Email:
richardchristianwilkerson@gmail.com

Vendor Name    -    TechnologyHub
Interviewing with Implementor  -  OPUS

Location: Austin TX


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




# Interview Questions
1. Experience writing SDKs? Describe one you wrote.
  - I’ve primarily written **feature modules** and reusable libraries inside apps, but the same principles apply to building SDKs.
  - My mental model of an SDK on Android:
    - Built and shipped as an **AAR (Android Archive)** rather than an APK.
    - Designed as a **separate module** with a clear boundary so it doesn’t accidentally depend on app-specific code or resources.
  - Key considerations when designing an SDK:
    - **Isolation / Module structure**
      - Create a dedicated Gradle module for the SDK (e.g., `:core-sdk`, `:payments-sdk`).
      - Avoid references to the host app module – the SDK should be reusable across multiple apps.
    - **Public vs Private API surface**
      - Treat the SDK’s **public API** as its "surface" – the classes, methods, and properties app developers interact with.
      - Use Kotlin’s `internal` visibility (or package-private in Java) to hide implementation details.
      - Keep the public surface **small, cohesive, and stable** to avoid breaking integrators.
    - **Dependency management**
      - Minimize external dependencies (e.g., OkHttp, Retrofit, Coroutines) to reduce conflicts with the host app.
      - If dependencies are necessary:
        - Choose conservative versions and avoid forcing strict version locks.
        - For public SDKs, consider **shading/relocating** internal libraries if conflicts are likely.
          - Shading = relocating and renaming dependency classes into your own package to avoid clashes with the host app’s versions.
    - **Initialization strategy**
      - Either:
        - Provide an explicit `Sdk.init(context, config)` entry point so the host app controls when initialization happens, or
        - Use automatic initialization via a `ContentProvider` (useful for analytics/telemetry SDKs) while still allowing opt-out.
      - Make initialization **idempotent** and safe to call multiple times.
    - **Lifecycle awareness**
      - Be aware of host lifecycle so the SDK can manage resources and avoid leaks.
      - Use `LifecycleObserver` / `DefaultLifecycleObserver` or `ProcessLifecycleOwner` to automatically pause/resume background work when the app goes to foreground/background.
    - **API design quality**
      - Idiomatic:
        - In Kotlin, prefer **suspend functions and Flows** over callback-only APIs where possible.
        - In Java, keep callback interfaces clean and avoid leaky abstractions.
      - Fluent:
        - Use builders or configuration DSLs for complex setup (e.g., `SdkConfig.Builder()` or Kotlin DSL-style `SdkConfig { ... }`).
      - Failure-resistant:
        - The SDK should **never crash the host app**. Catch internal exceptions, log internally, and expose errors via results/callbacks.
        - Prefer sealed `Result` types or error callbacks instead of throwing unchecked exceptions across the boundary.
    - **Testing & sample apps**
      - Unit tests for core logic.
      - Integration tests where a sample app consumes the SDK and runs common flows.
      - Provide **one or more sample apps** in the repo that show real-world integration.
    - **Documentation & discoverability**
      - Clear setup steps (Gradle, permissions, ProGuard/R8 rules).
      - Usage examples for the main scenarios.
      - Generated API docs (Dokka / Javadoc) for the public surface.
    - **Minimize footprint & performance impact**
      - Avoid large resource bundles; keep images/strings minimal.
      - Use R8/ProGuard to shrink and obfuscate.
      - Profile the SDK to ensure it doesn’t introduce jank, leaks, or excessive network/battery usage.

---

2. How do you establish a Bluetooth connection between a mobile device and a peripheral device using BLE?
- Get the right permissions and pre‑checks:
    - Android < 12: `ACCESS_FINE_LOCATION` for scanning.
    - Android ≥ 12: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (and use `neverForLocation` if not doing location).
    - Check that Bluetooth is enabled and, if required, that location services are on.

- Scan for peripherals:
    - Use `BluetoothLeScanner` with `ScanFilter` (e.g., by service UUID) and `ScanSettings` to limit noise and battery impact.
    - Surface discovered devices to the user or auto‑select by identifier.

- Connect and discover services:
    - Call `connectGatt(...)` to connect as a GATT client.
    - In `BluetoothGattCallback.onConnectionStateChange`, on `STATE_CONNECTED` call `discoverServices()`.
    - In `onServicesDiscovered`, obtain the required `BluetoothGattService` and `BluetoothGattCharacteristic` instances by UUID.

- Interact with characteristics:
    - For reads/writes, call `readCharacteristic` / `writeCharacteristic` and handle results in `onCharacteristicRead` / `onCharacteristicWrite`.
    - For streaming updates, enable notifications/indications via `setCharacteristicNotification` and write the CCC descriptor.

- Manage reliability:
    - Queue GATT operations (one at a time) and handle timeouts.
    - Handle `onConnectionStateChange` for `STATE_DISCONNECTED` by applying a reconnect/backoff strategy when appropriate.
    - Request a larger MTU with `requestMtu()` if you need to send larger payloads.

- Clean up:
    - When the session is finished, stop notifications, call `disconnect()`, then `close()` on `BluetoothGatt` to free resources.



# Job Description
Job Description – SDK Developer (Bluetooth/BLE)
Location: St. Louis, MO || Hybrid
Client: Mastercard

Role Overview

We are seeking an experienced SDK Developer with strong expertise in building end-to-end software development kits and deep knowledge of Bluetooth and Bluetooth Low Energy (BLE) frameworks. 
The ideal candidate will design, develop, and optimize SDK solutions that integrate seamlessly with our product ecosystem while ensuring high performance, security, and reliability.

Key Responsibilities

Design, develop, and maintain end-to-end SDKs, including architecture, implementation, versioning, and release processes.
Build robust, scalable, and reusable SDK components for mobile and connected devices.
Integrate and optimize Bluetooth/BLE communication within applications and device platforms.
Work closely with cross-functional teams (mobile, firmware, QA, product) to ensure seamless integration.
Troubleshoot and resolve issues related to connectivity, performance, and compatibility.
Contribute to technical documentation, coding standards, and best practices.
Participate in code reviews and provide constructive feedback.
Stay updated with emerging technologies, Bluetooth advancements, and SDK frameworks.
Required Skills & Qualifications

Strong hands-on experience in end-to-end SDK development.
Solid understanding of Bluetooth and BLE frameworks, protocols, and communication flows.
Experience with mobile platforms (iOS, Android) or embedded systems is a plus.
Proficiency in programming languages such as Java, Kotlin, Swift, Objective-C, or C/C++.
Strong problem-solving skills and ability to work in a fast-paced environment.
Experience working with APIs, connectivity modules, and device communication layers.
Good understanding of software architecture, design patterns, and debugging tools.
Preferred Qualifications
Experience with IoT ecosystems or connected consumer products.
Knowledge of firmware communication interfaces.
Experience with performance tuning and low-level debugging.
Familiarity with version control systems (Git) and CI/CD pipelines.


## Technology
Bluetooth and NFC are required for the majority of positions

NFC:
- *Technology*: Based on RFID; operates at 13.56 MHz.
- *Range*: Very short range, which helps with security and intentional user actions.
- *Common uses*:
  - Contactless payments (phone -> terminal: Google Pay, Apple Pay).
  - Tapping cards/badges for access or transit.
  - Reading/writing NFC tags (stickers, cards) to store small data (URLs, IDs, config).
  - Device pairing or bootstrap (tap to pair with speakers, etc.).

## Interviews

### Round 1

#### [Justin](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-12-23_R1_Justin_Mastercard.webm&bucket=storage-solution)


#### [Jack](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-12-30_R1_Jack_Mastercard.mkv&bucket=storage-solution)
- Pitch
- Experience writing SDKs
  - I’ve primarily written **feature modules** and reusable libraries inside apps, but the same principles apply to building SDKs.
  - My mental model of an SDK on Android:
    - Built and shipped as an **AAR (Android Archive)** rather than an APK.
    - Designed as a **separate module** with a clear boundary so it doesn’t accidentally depend on app-specific code or resources.
  - Key considerations when designing an SDK:
    - **Isolation / Module structure**
      - Create a dedicated Gradle module for the SDK (e.g., `:core-sdk`, `:payments-sdk`).
      - Avoid references to the host app module – the SDK should be reusable across multiple apps.
    - **Public vs Private API surface**
      - Treat the SDK’s **public API** as its "surface" – the classes, methods, and properties app developers interact with.
      - Use Kotlin’s `internal` visibility (or package-private in Java) to hide implementation details.
      - Keep the public surface **small, cohesive, and stable** to avoid breaking integrators.
    - **Dependency management**
      - Minimize external dependencies (e.g., OkHttp, Retrofit, Coroutines) to reduce conflicts with the host app.
      - If dependencies are necessary:
        - Choose conservative versions and avoid forcing strict version locks.
        - For public SDKs, consider **shading/relocating** internal libraries if conflicts are likely.
          - Shading = relocating and renaming dependency classes into your own package to avoid clashes with the host app’s versions.
    - **Initialization strategy**
      - Either:
        - Provide an explicit `Sdk.init(context, config)` entry point so the host app controls when initialization happens, or
        - Use automatic initialization via a `ContentProvider` (useful for analytics/telemetry SDKs) while still allowing opt-out.
      - Make initialization **idempotent** and safe to call multiple times.
    - **Lifecycle awareness**
      - Be aware of host lifecycle so the SDK can manage resources and avoid leaks.
      - Use `LifecycleObserver` / `DefaultLifecycleObserver` or `ProcessLifecycleOwner` to automatically pause/resume background work when the app goes to foreground/background.
    - **API design quality**
      - Idiomatic:
        - In Kotlin, prefer **suspend functions and Flows** over callback-only APIs where possible.
        - In Java, keep callback interfaces clean and avoid leaky abstractions.
      - Fluent:
        - Use builders or configuration DSLs for complex setup (e.g., `SdkConfig.Builder()` or Kotlin DSL-style `SdkConfig { ... }`).
      - Failure-resistant:
        - The SDK should **never crash the host app**. Catch internal exceptions, log internally, and expose errors via results/callbacks.
        - Prefer sealed `Result` types or error callbacks instead of throwing unchecked exceptions across the boundary.
    - **Testing & sample apps**
      - Unit tests for core logic.
      - Integration tests where a sample app consumes the SDK and runs common flows.
      - Provide **one or more sample apps** in the repo that show real-world integration.
    - **Documentation & discoverability**
      - Clear setup steps (Gradle, permissions, ProGuard/R8 rules).
      - Usage examples for the main scenarios.
      - Generated API docs (Dokka / Javadoc) for the public surface.
    - **Minimize footprint & performance impact**
      - Avoid large resource bundles; keep images/strings minimal.
      - Use R8/ProGuard to shrink and obfuscate.
      - Profile the SDK to ensure it doesn’t introduce jank, leaks, or excessive network/battery usage.

---

- **Let’s say I have an SDK – what are the steps to make it usable in other apps?**
  - At a high level:
    1. **Build** the SDK as an **AAR**.
    2. **Publish** that AAR to a Maven repository (public or private).
    3. **Document** how to depend on and initialize it.

  - **Build the SDK as an AAR**
    - Configure a library module (`com.android.library`) and run `./gradlew assembleRelease`.
    - This produces an `.aar` that contains compiled code, resources, and manifest snippets.

  - **Publish to a Maven repository**
    - Options:
      - **Maven Central** – gold standard for public/open-source libraries.
        - More setup overhead: GPG signing, POM metadata, Sonatype account.
        - Requirements typically include:
          - GPG-signed artifacts.
          - Source and Javadoc JARs alongside the AAR.
          - Proper groupId/artifactId/version naming.
        - There is an approval + sync process that can take some time.
      - **JitPack** – very easy for GitHub-hosted projects.
        - Builds artifacts directly from Git tags/branches.
        - Great for quick sharing or internal use, but less formal than Maven Central.
      - **Private Maven repositories** (common in enterprises):
        - Hosted via **Artifactory**, **Nexus**, or **GitHub Packages**.
        - Controlled access, ideal for internal SDKs.
        
    - Narrative: At Ally Bank we consumed an internal logging/analytics SDK exposed as a private Maven dependency, which let us log events internally without sending sensitive PII to third‑party services.
    ```kotlin
        val Logger = AnalyticsSdk.getLogger()
        viewModelScope.launch {
            // logic of user successfully logging in via biometric auth
            Logger.logEvent("UserLoggedIn", mapOf("method" to "biometric"))
        }
    ```

  - **Process for a private Maven repository example**
    - Publish the AAR to the private repo (via Gradle publishing plugin or CI pipeline).
    - Consumer apps add the private repo to `settings.gradle` or `build.gradle`:
      ```kotlin
      repositories {
          maven {
              url = uri("https://artifactory.mycompany.com/maven")
              credentials {
                  username = findProperty("repoUser") as String
                  password = findProperty("repoPassword") as String
              }
          }
      }
      ```
    - Add the SDK dependency:
      ```kotlin
      dependencies {
          implementation("com.mycompany:sdk-core:1.0.0")
      }
      ```

  - **Using JitPack as a quick path**
    - Tag a release in GitHub.
    - JitPack builds the AAR from that commit and exposes it via its Maven endpoint.
    - Apps then add the JitPack repository and dependency coordinates.
    - It’s very convenient for **internal or pre-release** SDKs.

  - **After publishing, you must provide:**
    - Clear **Gradle snippets** for consumers.
    - Any required **ProGuard/R8 rules**.
    - Initialization and usage examples.
    - Optionally, a sample app that demonstrates integration end-to-end.

---

- **The SDK is now available – how does an app actually use it?**
  - From the app developer’s point of view:
    1. **Add the dependency**
       - In `build.gradle` / `build.gradle.kts`:
       ```kotlin
       dependencies {
           implementation("com.mycompany:sdk-core:1.0.0")
       }
       ```
    2. **Initialize the SDK**
       - Typically in `Application.onCreate` or early in the first screen’s ViewModel/Activity, following docs:
       ```kotlin
       class MyApp : Application() {
           override fun onCreate() {
               super.onCreate()
               MySdk.init(
                   context = this,
                   apiKey = BuildConfig.MY_SDK_API_KEY,
                   config = MySdkConfig(/*...*/)
               )
           }
       }
       ```
    3. **Use the public API**
       - Call into the SDK’s main entrypoints for the required flows, e.g.:
       ```kotlin
       mySdk.trackEvent("CheckoutStarted")
       mySdk.showPaymentSheet(fragmentManager)
       ```
    4. **Handle callbacks / async results**
       - Depending on the SDK design, integrate with:
         - Callbacks / listeners.
         - Coroutines (`suspend` functions).
         - Flows/observables.
    5. **Test integration**
       - Verify behavior across navigation, configuration changes, network conditions, and error paths.

---

- **I want to restrict some functionality for specific users of the SDK – how would I do that?**

  - **API keys / license tokens**
    - Require an API key or license token on initialization.
    - SDK sends this to your backend, which returns which features are enabled for that tenant/user.

  - **Feature flags / entitlements**
    - Represent features in the SDK as **flags** or an `Entitlements` object.
    - The host app passes **user or tenant info** into the SDK, and the SDK:
      - Calls your backend with the API key + user context.
      - Receives a config/entitlements payload.
      - Enables/disables features at runtime based on that config.

  - **Licensing**
    - For paid tiers, validate a license token periodically (online) or via signed offline license files.
    - Deny or gracefully downgrade features if the license is invalid/expired.

  - **User roles**
    - Allow the host app to supply a role (e.g., `ADMIN`, `AGENT`, `VIEWER`) or permissions set.
    - Internally, gate features based on these roles – but **never trust the client alone**; always validate on the backend for security.

  - **Architectural vs runtime restrictions**
    - **Architectural / modularization**:
      - Ship a **core** SDK plus feature modules (`sdk-core`, `sdk-analytics`, `sdk-payments`, etc.).
      - Customers only add modules for the features they’ve licensed.
    - **Runtime / feature flags**:
      - Keep a single artifact but enable/disable features at runtime via entitlements.
      - Typical flow:
        1. Developer initializes SDK with API key or license token.
        2. SDK calls your backend to validate and fetch entitlements.
        3. Backend returns a config object listing enabled features.
        4. SDK stores config and gates all public APIs accordingly.

  - **Documentation**
    - Clearly document:
      - How to obtain/configure keys or licenses.
      - How feature flags map to visible behavior.
      - What error codes/behaviors to expect when a feature is disabled.

---

- **Do you have any payment experience?**
  - Yes – at Ally Bank I worked on **fund transfers and bill pay flows**:
    - Implemented transfer flows using **Retrofit** (secure HTTPS APIs), **Coroutines** for async work, and **Room** to cache transaction history.
    - Used **WorkManager** to schedule background syncs of transaction history and bill payments so data stayed consistent and retriable.
    - Collaborated with backend/security teams to align with **PCI-DSS** and internal security guidelines (encrypted storage, secure logging, least-privilege access).
    - OkHttp interceptors to add auth tokens and log requests securely 
      - Network Interceptors:
        - enforce TLS 1.2+ versioning, block cleartext, or add custom headers.
        - attach per-request diagnostic/metrics if needed
        - configure `CertificatePinner`
      - Authentication
        - `Authenticator` specifically for refresh tokens on 401 responses
          - refreshes tokens once, rebuilds the request with new Authorization header, retries original request.
          - avoids infinite loops by checking if the request has already been retried.
      - `HttpLoggingInterceptor` = debug only, used with custom logger that redacts sensitive info.
      - Application Interceptors
        - Auth header interceptor:
          - Adds `Authorization` bearer tokens and common headers (app version, locale, correlation IDs).
        - Retry / Idempotency interceptor:
          - Add Idempotency-Key header so backend can safely retry without duplicates.
          - Implement exponential backoff for transient network errors on idempotent calls (GET, PUT).
        - Error handling interceptor:
          - Handled various error states (network failures, server errors, validation errors) gracefully in the UI.
            - converts 4xx/5xx to domain-specific exceptions handled in ViewModel.

- **Have you worked on any payment processing systems?**
  - Yes, at Ally Bank I worked on the fund transfer and bill pay flows inside the One Ally app. My focus was on building secure, reliable money movement rather than card‑present payments:
    - I implemented the end‑to‑end Android flows for scheduling and executing transfers/bill payments using Kotlin, Coroutines, Retrofit, and Room, integrating with Ally’s internal payment APIs.
    - The client was designed so we never handled raw card data on device. All sensitive payment details were tokenized by backend/payment gateways, and the app only worked with opaque tokens and identifiers.
    - On the networking side, I worked with backend teams to define idempotent APIs for money‑movement operations, then implemented matching client logic:
      - Used idempotency keys and WorkManager jobs so retries (due to network failures/app kills) would not create duplicate transfers or duplicate bill payments.
      - Centralized retry and error handling using OkHttp interceptors and WorkManager to distinguish between transient failures (safe to retry) and hard business errors (insufficient funds, limits, etc.).
    - I built UX and state handling for pending / in‑progress transactions, including:
      - Surfacing clear statuses (pending, processing, completed, failed) based on backend responses.
      - Periodically reconciling local state with the server so users always saw the latest status of scheduled transfers and bill payments.
    - Throughout, we aligned with internal security and compliance requirements (e.g., PCI‑DSS principles, strong auth, encrypted storage, secure logging) to make sure money‑movement features were both safe and user‑friendly.

---

- **What is the process of communicating with Bluetooth between a mobile device and a peripheral using BLE?**

  - **Bluetooth Payment Workflow (BLE GATT)**
    - Most modern payment terminals expose a **BLE GATT** interface; your app acts as the GATT client.
    1. Scan for nearby terminals using `BluetoothLeScanner` + `ScanFilter` (ideally filtered by service UUID).
    2. Connect to the selected terminal via `connectGatt(...)`.
       - **Pairing vs connection**:
         - Many payment SDKs handle secure pairing internally; your app typically just connects as a GATT client.
    3. Send transaction details (amount, currency, merchant ID, etc.) by **writing to a specific GATT characteristic** defined by the payment provider.
       - e.g. `gatt.writeCharacteristic(transactionCharacteristic)`.
    4. The terminal handles card interaction (tap/swipe/insert) and communicates with its processor/acquirer.
    5. The terminal sends back an encrypted **payment token or authorization code** via another characteristic/notification.
    6. Your app receives this token in `onCharacteristicChanged` and forwards it to your backend/gateway over HTTPS.
    7. Your backend returns success/failure; the app reflects this in the UI.

  - **Permissions & pre-checks**
    - Android < 12: `ACCESS_FINE_LOCATION` for BLE scans.
    - Android 12+: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (mark as `neverForLocation` if appropriate).
    - Check that Bluetooth is enabled; for some flows also ensure location services are on.

  - **Connection lifecycle**
    - BLE can be flaky; robust handling is crucial:
      - Auto-reconnect when signal is lost briefly.
      - Handle **time-outs** and surface meaningful errors.
      - Monitor battery levels where possible; warn if reader/device is low.
      - Support **firmware update flows** if required by the payment provider.

  - **Security**
    - All data over BLE should be **encrypted and application-layer authenticated**, often using symmetric keys (e.g., AES) negotiated through the provider’s SDK.
    - Sensitive data at rest on the device should be encrypted (Android Keystore, hardware-backed where available).
    - Many payment SDKs detect **rooted/jailbroken devices** and block functionality to prevent tampering.

---

- **What kind of protocol should be used for Bluetooth payment processing?**
  - **BLE GATT (Generic Attribute Profile)** is the standard for structured, low-energy data exchange.
  - Payment terminals often define proprietary or standardized **services and characteristics** within GATT for:
    - Session setup.
    - Transaction details.
    - Status/authorization responses.
  - All payloads should be **encrypted** (e.g., AES) and signed where appropriate to ensure confidentiality and integrity.

- **What about Classic Bluetooth?**
  - Classic Bluetooth (BR/EDR) is typically used for continuous, higher‑bandwidth connections like audio streaming, game controllers, or legacy serial devices.
  - It often relies on profiles such as:
    - A2DP for audio streaming.
    - AVRCP for media control (play/pause/skip, metadata).
    - SPP/RFCOMM (Serial Port Profile) for simple serial-style data channels using sockets -> Hardware-level pairing and connections.

---

- **If I’m listening to music over Bluetooth, what protocol/profile is used?**
  - That’s typically **Classic Bluetooth**, not BLE:
    - **A2DP (Advanced Audio Distribution Profile)** for audio streaming.
    - **AVRCP (Audio/Video Remote Control Profile)** for playback controls (play/pause/skip, metadata).

  - High-level flow:
    - Discovery & pairing:
      - Devices pair via system UI or `BluetoothDevice.createBond()`, exchanging keys and establishing trust.
      - Like when you pair headphones or speakers.
    - UUID/service matching:
      - Phone acts as the **A2DP source** (streaming audio).
      - Headphones/speakers act as the **A2DP sink**.
      - Lower-level, RFCOMM sockets are used for some profiles (e.g., SPP), but A2DP manages audio streaming specifics.
    - Data streaming:
      - Audio is encoded (SBC, AAC, aptX, etc.) and streamed over the A2DP channel.
      - AVRCP is used for control signals and metadata.

---

- **If I just want to send small packets of data back and forth between two devices, what Bluetooth protocol should I use?**
  - **Bluetooth Low Energy (BLE) with GATT** is ideal for small, efficient, intermittent data.

  - Typical steps:
    1. **Scan** with `BluetoothLeScanner` + `ScanFilter` to find the peripheral (ideally filter by service UUID).
       - Always **stop scanning** once the device is found to save battery.
    2. **Connect** with `connectGatt(...)`, getting a `BluetoothGatt` instance.
    3. Call `gatt.discoverServices()` to retrieve available services/characteristics.
       - Handle results in `BluetoothGattCallback.onServicesDiscovered()`.
    4. **Read/Write/Notify**:
       - Read: `gatt.readCharacteristic(characteristic)` → result in `onCharacteristicRead()`.
       - Write: `gatt.writeCharacteristic(characteristic)` → result in `onCharacteristicWrite()`.
       - Notify: enable notifications with `gatt.setCharacteristicNotification(...)` and CCC descriptor writes; handle incoming data in `onCharacteristicChanged()`.

---

- **I have two screens: Screen 1 captures a photo, Screen 2 is a product screen with a placeholder for the photo. If I’m using Retrofit, how do I send the photo from Screen 1 to Screen 2?**

  - **High-level approach**
    1. Upload the captured photo from Screen 1 to the backend using a Retrofit **multipart** endpoint.
    2. Backend returns a **photo ID or URL**.
    3. Pass that ID/URL to Screen 2 via navigation arguments.
    4. In Screen 2, call another endpoint to fetch the product that references that photo, then load the image with Coil/Glide.

  - **1. Define Retrofit endpoints**
    - `POST /uploadPhoto` – upload image, returns `photoId` or `photoUrl`.
    ```kotlin
    @Multipart
    @POST("/uploadPhoto")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): PhotoUploadResponse

    data class PhotoUploadResponse(
        val photoId: String,
        val url: String
    )
    ```
    - `GET /getProductWithPhoto` – takes `photoId`, returns product with image URL.

  - **2. Upload the photo (Screen 1)**
  - TODO: do i need to use a content provider here? or do i need to check image type/size and change/compress before upload?
    ```kotlin
    val file = File(photoPath)
    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

    val description = "Sample photo".toRequestBody("text/plain".toMediaTypeOrNull())

    val response = api.uploadPhoto(body, description)
    val photoId = response.photoId
    ```
    - After upload, navigate to Screen 2 passing `photoId` as a parameter.

  - **3. Retrieve product data with photo (Screen 2)**
    ```kotlin
    suspend fun getProduct(photoId: String): Product {
        return api.getProductWithPhoto(photoId)
    }
    ```

  - **4. Display photo in Screen 2**
    - Use an image loader in Compose (Coil) or XML (Glide/Picasso):
    ```kotlin
    Image(
        painter = rememberAsyncImagePainter(product.photoUrl),
        contentDescription = null
    )
    ```

- **Do we need any headers?**
  - Typically **yes** for real APIs:
    - Auth headers (e.g., `Authorization: Bearer <token>`).
    - Content-type (`multipart/form-data`, `application/json`).
  - Use **OkHttp Interceptors** to add common headers (auth tokens, app version, locale) in one place rather than per-call.

---

- **Just using a coroutine (no LiveData or Flow), how would we display data with Compose on the second screen once we have it?**

  - You still need a **state holder**, even if you don’t use LiveData/Flow.
  - A clean approach in Compose with coroutines only:

  ```kotlin
  @Composable
  fun ProductScreen(photoId: String, api: ProductApi) {
      var product by remember { mutableStateOf<Product?>(null) }
      var error by remember { mutableStateOf<Throwable?>(null) }

      // Launch a coroutine tied to this composable
      LaunchedEffect(photoId) {
          try {
              val result = withContext(Dispatchers.IO) {
                  api.getProductWithPhoto(photoId)
              }
              product = result
          } catch (e: Exception) {
              error = e
          }
      }

      when {
          error != null -> Text("Error loading product")
          product == null -> Text("Loading...")
          else -> ProductContent(product!!)
      }
  }
  ```

  - Notes:
    - No LiveData/Flow required; `mutableStateOf` + `LaunchedEffect` is enough.
    - We still offload network work to `Dispatchers.IO` and then update state on the main thread.
    - Using `LaunchedEffect` is better than launching from an Activity manually because it scopes the coroutine to the composable’s lifecycle.

  - If this were an SDK and you wanted to avoid forcing architecture on host apps, you might instead expose:
    - `suspend fun getProductWithPhoto(photoId: String): Product` and let the host decide how to call it, or

---

- **I’m working on a banking app; what security standards should I follow?**

  - **PCI-DSS (Payment Card Industry Data Security Standard)**
    - If you deal with **cardholder data**, you must follow PCI-DSS:
      - Prefer **tokenization** – never store raw card numbers on the device.
      - Use secure input components and avoid logging any sensitive data.
      - Ensure all card-related communication is over **TLS 1.2+**.

  - **Bank-specific / regulatory (FDIC, CFPB, etc.)**
    - FDIC/CFPB are more about **institution-level** compliance and consumer protection, but they drive internal policies:
      - Strong privacy and transparent data usage.
      - Clear consent flows for data sharing.
      - Proper disclosures for fees, terms, and dispute processes.

  - **Platform-level best practices**
    - **Transport security**:
      - Enforce HTTPS with TLS 1.2+.
      - Use **certificate pinning** (SSL pinning) for critical endpoints to mitigate MITM attacks.
    - **Storage security**:
      - Use **Android Keystore** (hardware-backed where possible) for keys.
      - Encrypt sensitive data at rest with modern ciphers.
    - **Auth & session management**:
      - Use strong auth: username+password + **MFA/biometrics** where appropriate.
      - Use short-lived access tokens with refresh tokens; securely store tokens in encrypted DataStore with encryption key stored in Keystore.
    - **Secure coding practices**:
      - Avoid storing secrets in source (use CI/secret management).
      - Keep dependencies up to date; monitor for CVEs.
      - Perform regular code reviews and static analysis (Detekt, Lint, etc.).
    - **Runtime hardening**:
      - Detect and handle rooted/jailbroken devices per risk appetite.
      - Obfuscate with R8/ProGuard; remove debug logs and dev-only flags in release.
