Current Address:
750 Fort Worth Avenue • Dallas TX 75208
Phone:
(214) 329-1138
Email:
richardchristianwilkerson@gmail.com


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




## Job Description
Client Name    -    Verizon
Implementor/Prime Vendor Name    -    NA
Vendor Name    -    Globallogic
Date    -    12/23/2025
Time    -    3:30
Duration    -    60
Mode    -    Teams
Interview with    -    Vendor
Round    -    R1
Live Coding    -    Yes
Meeting Link    -    waiting
FE location (for this interview)    -    Idaho
Contract type    -    -
Anything else that FE should know before heading into the interview?    -    Open to relocate
Title: Lead Android Developer (Kotlin & Jetpack Compose)
Location: Alpharetta, GA (3 Days onsite)

Job Description

Experience in developing Android App(s) in Play Store, Jetpack Compose, Compose UI.
Experience with Gradle, Android Studio, JAVA, Kotlin
Knowledge of MVC, MVVP and Viper design patterns, Object-Oriented Programming (OOP) through SOLID principles and development best practices
Strong analytical and debugging skills
Consumer mobile application development experience
A background in building mobile applications that utilize web-services (either REST-based, JSON, Thrift or other services)
Good knowledge on material design
Experience and thorough understanding of Agile Software Development
Proficiency in the use of JIRA/Confluence in product development
Experience in source control tools like Git.
Good knowledge of the continuous deployment and integration process.
Good written and verbal communication skills


Job Responsibilities:

Develop applications for the Android platform
Ensure the performance, quality, and responsiveness of applications
Identify and correct bottlenecks and fix bugs ·
Help maintain code quality
Developing solutions to address complex problems.
Ability to Share technical solutions and product ideas with the broader team through design review, code review, and show and tell
Participate in brainstorming sessions and contribute ideas to our technology, algorithms and products.



## Round 1 Interviews

[SME Text Only](https://nextim-vert.vercel.app/facilitate/Android/82d9a20f-077b-4d25-8595-f363a94e1c66/fe)



## Technology

### Viper
VIPER is an architectural pattern used in software development, particularly in iOS and Android applications. It stands for View, Interactor, Presenter, Entity, and Router. VIPER is designed to separate concerns within an application, making it easier to manage, test, and maintain.
- View: The View is responsible for displaying the user interface and handling user interactions. It communicates with the Presenter to receive data and updates.
- Interactor: The Interactor contains the business logic of the application. It handles data manipulation, network requests, and other operations that are not directly related to the user interface.
- Presenter: The Presenter acts as a mediator between the View and the Interactor. It receives input from the View, processes it, and updates the View with the results from the Interactor.
- Entity: The Entity represents the data model of the application. It defines the structure of the data used by the Interactor and Presenter.
- Router: The Router is responsible for navigation and routing within the application. It handles transitions between different screens or modules.

- is similar to MVVM - presenter is like ViewModel, interactor is like use cases
- entity is like data models
- router is like navigation component or coordinator pattern

Example:
1. User taps Login on the View. 
2. View calls Presenter.onLoginClicked(username, password) and does nothing else. 
3. Presenter validates basic UI rules and calls Interactor.login(username, password). 
4. Interactor talks to repository/API, maps responses to Entities, and returns success/failure to Presenter via an output/callback. 
5. Presenter converts Entities or errors into view models and tells View to show loader, error message, or success state. 
6. On success, Presenter asks Router to navigate to Home, passing any needed data


### Background services
- they like to collect user data on how they use their phones 
- what are the limitations of android 14 in background services
In Android 14, there are several limitations and changes regarding background services to improve battery life and user experience:
1. Background Service Restrictions: Android 14 continues to enforce restrictions on background services to prevent apps from running indefinitely in the background. Apps are encouraged to use WorkManager or JobScheduler for background tasks.
2. Foreground Service Requirements: Apps that need to run services in the foreground must display a persistent notification to inform users about the ongoing activity.
3. Background Location Access: Android 14 introduces stricter controls on background location access. Apps must request permission to access location data while running in the background.
4. Battery Optimization: Android 14 includes enhanced battery optimization features that limit background activity for apps that are not frequently used.
5. Doze Mode Enhancements: Android 14 further refines Doze mode, which restricts background activities when the device is idle.

- Foreground Service (FGS) Restrictions: You can't start an FGS with certain permissions (like location) if your app is already in the background; the system throws an exception immediately instead of letting it run with limited access.

- can use it for foreground tasks with notification 
- 1 time vs periodic tasks

### Headless app
A **headless app** (or headless component) is code that runs **without a visible UI**. On Android this usually means:
- Services, workers, or broadcast receivers that run in the background.
- Logic exposed as an SDK or library used by other apps, not directly by end users.

Examples in a carrier or telecom context:
- A background service that listens for network state changes or SIM events and reports metrics.
- A headless module that manages authentication, encryption, or telemetry for other apps on the device.

Key points for interviews:
- On modern Android, truly long‑running headless work is heavily restricted:
  - Use **foreground services** (with notification) for user‑visible long tasks.
  - Use **WorkManager** for deferrable/background work (sync, uploads, analytics).
- Separate **UI layer** (Activities/Fragments/Composables) from **headless business logic** (ViewModels, use cases, repositories, services) so the core logic can run without a screen and be reused/tested more easily.

---

### MDM (Mobile Device Management)
MDM = tools and protocols used by enterprises or carriers to **remotely manage devices and apps**.

Common capabilities:
- Enroll devices and apply **policies** (password rules, screen lock, encryption).
- Control **app installation and updates** (whitelisting/blacklisting, managed Play Store).
  - TODO: what does whitelisting/blacklisting mean in this context?
- Configure **Wi‑Fi, VPN, email**, and other system settings.
- Enforce **security**: remote wipe, lock, jailbreak/root detection, corporate vs personal profile separation.

Android‑specific concepts:
- **Android Enterprise / Work Profile**:
  - Creates a separate, managed profile on the device for corporate data/apps.
  - MDM/EMM solutions (Intune, Workspace ONE, MobileIron, etc.) use this to isolate corporate apps from personal ones.
- **Device Owner / Profile Owner apps**:
  - Special admin apps that can enforce device or profile policies.

As an Android engineer you usually don’t build an entire MDM, but you should know:
- How your app behaves on **managed devices**:
  - Respecting policies (e.g., disallow screenshots, camera restrictions).
  - Handling cases where app install/update is controlled by MDM.
- How to integrate with **Android Enterprise** if you ship a managed app:
  - Using managed configurations (app config pushed by MDM).
  - Handling conditional access (e.g., blocked on compromised devices).

Interview angle:
> “I’m familiar with how Android Enterprise and MDM solutions manage apps via work profiles and device/profile owners. 
> When building apps for managed environments, I make sure we respect policies like no‑screenshots, handle managed configurations from MDM, and test on devices enrolled in common MDMs so we don’t break in enterprise setups.”

---

### Connect to other devices / IPC on Android
Often, “connect to other devices” in a Verizon‑type role can mean **talking to other processes or system services**, not just Bluetooth/Wi‑Fi. On Android this is “**inter‑process communication (IPC)**”.

Core IPC mechanisms:
- **Intents & Bundles**
  - Fire‑and‑forget messages for starting Activities/Services or sending broadcasts.
  - `Intent` carries data via `Bundle` (key–value pairs).

- **Broadcast Receivers**
  - Receive system or app‑defined broadcast Intents.
  - Used for events like connectivity changes, SIM state, boot completed, SMS received, etc.
  - In a telecom context, you might listen for network state or carrier config changes.

- **AIDL (Android Interface Definition Language)**
  - Define **typed interfaces** for bound services that can be used across process boundaries.
  - Generates stubs and proxies so clients can call methods as if they were local.
  - Used when you need structured two‑way communication between apps/processes (e.g., your app talking to a carrier service running in another process).

- **Messenger / Handler**
  - Higher‑level wrapper around Binder using `Message` objects.
  - Good for simpler request/response or command patterns without writing full AIDL.

- **IBinder**
  - The low‑level interface on which AIDL and Messenger are built.
  - Passed in Intents, bound services, or callbacks so one component can call methods on another process.

How to explain this in an interview:
> “On Android, connecting to other devices or services is often about IPC rather than just Bluetooth. For simple one‑way events I use Intents and BroadcastReceivers. If I need a stable API across processes—like exposing a carrier service or SDK to other apps—I’d define an AIDL interface or use a bound service with Messenger, which rides on top of Binder/IBinder. That way clients can call into our service in a type‑safe way while the system handles process boundaries.”

If they explicitly mean **hardware or external devices**:
- Mention:
  - Bluetooth/BLE GATT for accessories.
  - Wi‑Fi Direct / local network APIs.
  - USB (Accessory/Host) for wired devices.
- Tie it back to IPC patterns if your app exposes those capabilities to other apps via services or AIDL.

### Protobufs
**Protocol Buffers (Protobufs)** are a **binary serialization format** and schema language from Google.
- You define your data structures in a `.proto` file and compile them to generate strongly typed classes for multiple languages.
- Compared to JSON:
  - More compact on the wire (binary, not text).
  - Faster to parse and generate.
  - Stronger typing and a clear schema, which helps with backward/forward compatibility.
- Commonly used in:
  - gRPC APIs.
  - High-throughput, low-latency services where payload size matters.
- For an Android engineer:
  - You might use Protobufs for
    - Network payloads (e.g., with gRPC or custom endpoints).
    - Local storage/caching formats.
  - Key concepts: fields with numeric tags, optional vs repeated fields, schema evolution (adding/removing fields without breaking old clients).

Interview angle:
> “Protobufs give us a compact, strongly typed binary format. We define schemas in `.proto` files, generate Kotlin/Java classes, and get better performance and compatibility than ad‑hoc JSON. I’d use them where bandwidth and latency matter or when I’m standardizing data contracts across services and clients.”

---

### Flatbuffers
**FlatBuffers** is another **binary serialization format** designed by Google, optimized for **zero‑copy reads**.
- Unlike Protobufs, FlatBuffers allows reading data **directly from a byte buffer** without an expensive deserialization step.
- Good for:
  - Games and graphics.
  - Real‑time or performance‑critical code on constrained devices.
  - Situations where you read many small objects repeatedly.
- Trade-offs:
  - API can be more complex to work with than Protobufs.
  - Less ubiquitous in typical REST/gRPC backends, more common in engines or specialized pipelines.

For Android/video/telecom context:
- You might see FlatBuffers in:
  - Low‑latency telemetry pipelines.
  - On‑device processing engines where GC pressure matters.

Interview angle:
> “FlatBuffers is a binary format optimized for zero‑copy access. 
> Instead of fully deserializing into objects like Protobufs, you can read directly from a byte buffer. 
> It’s useful when you need very low latency or you’re repeatedly traversing large datasets—common in games or some real‑time telemetry—though in typical mobile apps Protobufs or JSON are more common.”

---

### RTP
**RTP (Real-time Transport Protocol)** is a protocol for **real-time audio and video transport** over IP networks.
- Runs typically over **UDP**.
- Handles:
  - Sequencing (packet order).
  - Timestamps (for playback timing and synchronization across streams).
  - Payload type identification (what codec is used, e.g., H.264, Opus).
- Often paired with RTCP (Real-time Control Protocol) for quality feedback and control.

In a streaming/telecom context:
- RTP is **not** something you hand‑craft in typical Android app code; it’s usually implemented by media frameworks (e.g., WebRTC, media servers, SIP/VoIP stacks).
- As a client dev, you should know that:
  - RTP is the underlying transport for many low-latency voice/video systems.
  - Jitter, packet loss, and ordering are handled by RTP-aware libraries and jitter buffers.

Interview angle:
> “RTP is the real‑time transport layer under a lot of voice/video systems—carrying timestamped audio/video packets over UDP so players can reconstruct streams in order and on time. 
> On Android I typically consume it through higher‑level libraries like WebRTC or SIP stacks rather than implementing RTP by hand.”

---

### RTSP
**RTSP (Real Time Streaming Protocol)** is an **application-layer control protocol** for streaming media.
- Think of it as the "remote control" for media sessions:
  - SETUP, PLAY, PAUSE, TEARDOWN commands.
- Typically used to **control** streaming over RTP/UDP or sometimes over TCP.

In practice:
- Common in IP cameras, surveillance systems, and some legacy streaming servers.
- On Android, you might:
  - Use a player or library that speaks RTSP to control a camera/stream.
  - Or build a client that sends RTSP commands to a backend, which delivers media via RTP.

Interview angle:
> “RTSP is more about controlling a streaming session—issuing commands like PLAY, PAUSE, and TEARDOWN—while the actual media usually flows over RTP. If I had to integrate an IP camera feed into an Android app, I’d likely use a library that handles RTSP/RTP and focus on session setup, error handling, and UI.”

---

### HLS
**HLS (HTTP Live Streaming)** is an **adaptive bitrate streaming protocol** originally from Apple, widely used for video over the web and mobile.
- Key ideas:
  - Media is split into small **HTTP segments** (e.g., 2–10 seconds each).
  - A **playlist/manifest** (`.m3u8`) describes which segments to download and their bitrates.
  - Client can switch between different quality levels based on network conditions (adaptive bitrate).
- Runs over plain **HTTP/HTTPS**, making it friendly to CDNs and firewalls.

On Android:
- HLS is supported by players like **ExoPlayer** and the platform media APIs.
- As an app dev you:
  - Point the player at an HLS manifest URL.
  - Handle playback errors, buffering, and quality restrictions.
  - Optionally customize the player UI and DRM integration.

Interview angle:
> “HLS is the de-facto standard for HTTP-based adaptive streaming. The server exposes an `.m3u8` manifest and chunked media segments at multiple bitrates, and the client player picks the right quality based on network conditions. On Android I’d typically use ExoPlayer to play HLS streams and focus on error handling, offline support, and integrating with the rest of the app.”

---



## Behavioral Questions

### Leadership (Lead Role)
- Can you describe a time when you had to lead a team through a challenging project? How did you handle it?
- How do you motivate your team members to achieve their best performance?
- Can you give an example of a difficult decision you had to make as a leader? What was the outcome?
- How do you handle conflicts within your team?
- How do you prioritize tasks and manage time effectively for yourself and your team?
