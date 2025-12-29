## About the Company
Vizio doesn't have one specific "Vizio Android app" for general use, but rather several official and third-party apps for controlling your TV, casting content, and accessing free TV, primarily through their SmartCast platform; the main official one is the VIZIO Mobile app, which lets you control your Vizio TV, access WatchFree+, and cast, while many other Google Play Store options offer similar remote and casting features, like "Remote for Vizio SmartCast".
https://www.vizio.com/en/mobile
United States
Mobile
https://play.google.com/store/apps/details?id=com.vizio.vue.launcher&hl=en_US&gl=US
VIZIO | WatchFree+ - Apps on Google Play
Live TV, Entertainment Discovery, and Device Control
VIZIO | WatchFree+ - Apps on Google Play
WATCHFREE+ MOBILE: Free live channels, anytime, anywhere.
• Stream 300+ Free Live Channels: Watch news, sports, movies, and shows on your mobile device —no VIZIO TV needed
• Personalize Your Experience: Create favorite channels lists and get personalized recommendations
• Stay Connected: Keep up with local sports, news, and entertainment on the go
• Easy to Start: Just download the app and create a free VIZIO account to start watching
• Smart Navigation: Find what you want faster with the category jump feature
• Pick Up Where You Left Off: Seamlessly switch between devices when you start watching on a VIZIO TV

TV and ENTERTAINMENT CONTROL: Transform your phone into a powerful entertainment hub.
• Universal Search: Find what to watch across streaming services in one place
• Smart Recommendations: Discover new shows and movies based on your interests
• Voice Control: Launch apps and find content hands-free
• Save Time. Stream More: Organize apps and manage subscriptions in one location
• Share & Connect: Cast photos and videos to your TV with VIZIOgram


Education:

Bachelor's or Master’s degree in Computer Science, Computer or Electrical Engineering, Mathematics, or a related field.

Bachelor’s Degree or greater in Computer Science, Electronics & Communications, Electrical Engineering, or related field.
At least 7 years of professional experience in Android development.
Experience in architecture, design, prototyping, software development, code review and unit testing for mobile applications.
Strong proficiency in Kotlin and Java.
Deep understanding of Android SDK, Jetpack Components, and Material Design Guidelines.
Experience with frameworks like Coroutines, RxJava, or Flow.
Knowledge of RESTful APIs, GraphQL, and web services.
Proficiency in version control tools such as Git.
Experience with tools like Android Studio, ADB, and Android Profiler.
Familiarity with dependency management tools like Gradle.
Knowledge of RESTful APIs and integration with backend services.
Proficiency in version control tools such as Git.
Experience with Android Watch OS development
Experience working on peer-to-peer connectivity frameworks like Bluetooth, Bonjour, NFC etc
Familiar with mobile app design patterns like OOPS, MVC, MVVM, VIPER etc.
Extensive experience with IP-based communications and protocols, example: HTTP, TLS, REST, Websockets, JSON, XML, Protobufs, Flatbuffers, RTP, RTSP, HLS

# About Me:

Current Address:
750 Fort Worth Avenue • Dallas TX 75208
Phone:
(214) 329-1138
Email:
richardchristianwilkerson@gmail.com

currently in Dallas

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


# Technology

## Connect to other devices / IPC on Android
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
> “On Android, connecting to other devices or services is often about IPC rather than just Bluetooth. 
> For simple one‑way events I use Intents and BroadcastReceivers. 
> If I need a stable API across processes—like exposing a carrier service or SDK to other apps—I’d define an AIDL interface or use a bound service with Messenger, which rides on top of Binder/IBinder. 
> That way clients can call into our service in a type‑safe way while the system handles process boundaries.”

If they explicitly mean **hardware or external devices**:
- Mention:
    - Bluetooth/BLE GATT for accessories.
    - Wi‑Fi Direct / local network APIs.
    - USB (Accessory/Host) for wired devices.
- Tie it back to IPC patterns if your app exposes those capabilities to other apps via services or AIDL.

## Protocols and Data Formats

IP Streaming HLS vs RTP/RTSP
- HLS: HTTP-based adaptive streaming, \.m3u8 playlists + media segments. 
- RTP: low-latency audio/video transport over UDP (sequence numbers, timestamps). 
- RTSP: control protocol (SETUP/PLAY/PAUSE) often paired with RTP.

- HLS is what you’ll most likely see for WatchFree+ and mobile streaming. 
- RTP/RTSP more for cameras, ultra low latency, or internal media pipelines.

Serialization: JSON vs Protobufs vs Flatbuffers
- JSON: human-readable, easy, heavier.
- Protobufs: binary, schema-based, compact and fast; great for APIs and telemetry.
- FlatBuffers: binary, zero-copy reads; good for real-time / high-frequency data.

> For simple REST APIs JSON is fine, but for high-throughput or latency-sensitive paths—like player telemetry or control messages between the app and a backend—I’d lean on Protobufs: 
> define the schema in \.proto files, generate Kotlin classes, and get compact, strongly typed messages. 
> If we had extremely tight performance constraints, like an on-device engine or a very chatty control plane, FlatBuffers could make sense because you can read directly from the buffer without full deserialization.


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
> “FlatBuffers is a binary format optimized for zero‑copy access. Instead of fully deserializing into objects like Protobufs, you can read directly from a byte buffer. It’s useful when you need very low latency or you’re repeatedly traversing large datasets—common in games or some real‑time telemetry—though in typical mobile apps Protobufs or JSON are more common.”

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
> “RTSP is more about controlling a streaming session—issuing commands like PLAY, PAUSE, and TEARDOWN—while the actual media usually flows over RTP. 
> If I had to integrate an IP camera feed into an Android app, I’d likely use a library that handles RTSP/RTP and focus on session setup, error handling, and UI.”

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
      - TODO: what are DRM integration options for HLS on Android?

#### Exoplayer:
- ExoPlayer is a popular open-source media player library for Android that supports HLS natively.
- It’s built on top of Android’s low-level media APIs but adds:
  - Better format support (HLS/DASH/SmoothStreaming/MP4, etc.).
  - Adaptive bitrate streaming, buffering control, and track selection.
  - Pluggable components (renderers, data sources, DRM, caching).
  - A stable, testable API surface that you control instead of relying on device-specific `MediaPlayer` quirks.

**Quick high-level usage**
- Typical flow in an Activity/Fragment:
  1. Create a `ExoPlayer` instance.
  2. Create a `MediaItem` pointing at your stream/asset (e.g., HLS `.m3u8`).
  3. Set the media item on the player and prepare.
  4. Attach the player to a `PlayerView` in your layout for UI.
  5. Release the player in `onStop`/`onDestroy`.

**Basic setup (Gradle)**
- In `build.gradle` (module):
  - Using the ExoPlayer BOM:
    - Add the BOM in the `dependencies` block:
      - `implementation(platform("com.google.android.exoplayer:exoplayer-bom:2.19.1"))`
    - Then add the core and UI modules you need:
      - `implementation("com.google.android.exoplayer:exoplayer")`
      - `implementation("com.google.android.exoplayer:exoplayer-ui")`

**Basic usage (Kotlin)**
- In your Activity/Fragment:
  - Create and attach the player:
    - `val player = ExoPlayer.Builder(context).build()`
    - `binding.playerView.player = player`
  - Build a media item:
    - `val mediaItem = MediaItem.fromUri(hlsUrl)`
  - Prepare and start playback:
    - `player.setMediaItem(mediaItem)`
    - `player.prepare()`
    - `player.playWhenReady = true`
  - Release when done:
    - `override fun onStop() { super.onStop(); player.release() }`

KAL
> Migrated from MediaPlayer to ExoPlayer in multi-module architectures, combining with Room for metadata caching and RxJava/Flow for reactive state updates.
> Handled edge cases like network interruptions by persisting playback position via ViewModel/onSaveInstanceState(), ensuring 99% resumption success across rotations and process death.


Interview angle:
> “For video playback on Android I default to ExoPlayer rather than the raw `MediaPlayer` because it gives me first-class HLS/DASH support, adaptive bitrate, better error handling, and a pluggable architecture for DRM and caching. 
> Setup is straightforward: add the Gradle dependency, build an `ExoPlayer` instance, point it at an HLS URL via a `MediaItem`, attach it to a `PlayerView`, and manage its lifecycle in `onStart`/`onStop`.”

## Peer-to-Peer Connectivity Frameworks

- Bluetooth/BLE: pairing, GATT for structured data exchange. 
- Wi‑Fi Direct: P2P connections without an AP. 
- Bonjour/mDNS: service discovery on local network. 
- NFC: quick pairing / bootstrapping (tap to connect).

Phone ↔ TV discovery and pairing:
  - Use mDNS/SSDP to discover TVs on LAN.
  - Optionally use BLE or NFC for initial pairing or out-of-band auth.
