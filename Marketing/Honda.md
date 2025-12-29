## Resume
Honda Motor Group, Torrance, CA				 Jul 2019 – Mar 2021  
Senior Android Developer

https://play.google.com/store/apps/details?id=com.honda.hondalink.connect&hl=en-US

At Honda, I specialized in developing and stabilizing Android applications for in-car infotainment systems, contributing to a connected and intuitive driving experience. My work focused on integrating mobile-quality Android experiences into a constrained, vehicle-controlled environment.

- Designed and maintained core Android applications for Honda's automotive infotainment units, including navigation, media playback, hands-free communication, and vehicle status displays.
- Customized and extended Android Automotive / in-vehicle Android builds to create a branded and driver-friendly experience tailored for Honda vehicles.
- Implemented robust Bluetooth functionality for device pairing, audio streaming, and hands-free calling, plus USB integration for media and device connectivity.
- Helped build and refine screens that display real-time vehicle data (fuel efficiency, tire pressure, maintenance alerts) by integrating with the car's onboard diagnostics and vehicle HAL interfaces.
- Collaborated closely with hardware and platform engineers to ensure seamless integration of Android software with vehicle hardware components, optimizing for responsiveness and reliability under constrained resources.
- Adhered to automotive safety and UX guidelines, implementing secure coding practices and minimizing driver distraction through clear HMI patterns and voice-first interactions where appropriate.
- Worked with HMI designers to create intuitive, glanceable interfaces optimized for touch and voice, taking into account driving context and regulatory constraints.

Tools: Android (Automotive / embedded builds), Android SDK, Kotlin, Java, Android Studio, Bluetooth (A2DP, HFP, AVRCP), USB integration, GPS, Audio focus / AudioManager, Vehicle Data Interfaces (HAL, OBD-II), HMI design, AOSP customization,  
System services, performance optimization, multithreading, IPC (Binder, AIDL), Retrofit / REST APIs, JSON, XML, MVVM, dependency injection (Dagger), Git, Jenkins, Agile/Scrum, unit testing (JUnit, Mockito), logcat / system tracing.

---

## Why was I brought to Honda initially?
I was brought in as a Senior Android Developer to help modernize and harden Honda’s in-car infotainment applications during a major platform transition.

The team needed experienced Android engineers who could:
- Apply **mobile best practices** (architecture, testing, performance) to an **embedded / Automotive** context.
- Improve **stability, modularity, and testability** of existing apps that had grown quickly with tight coupling to system services.
- Help bridge the gap between **Android app teams** and **platform / hardware teams**, speaking both in terms of Android APIs and vehicle constraints.

Even without prior Automotive OS experience, my background in Android app architecture, UI development, and performance tuning made me a good fit to translate consumer app practices into a vehicle environment.

---

## What was the state of the project when I joined?
When I joined, the HondaLink Connect platform was already functional and in production, but several areas needed attention:

- **Stability and performance:**  
  - Occasional freezes and slow screen transitions on older head units.  
  - Intermittent Bluetooth and media playback issues.

- **Architecture and modularity:**  
  - Large, tightly coupled modules with logic spread across Activities, Services, and system callbacks.  
  - Strong coupling to specific vehicle services and hardware assumptions, making it harder to reuse or test components.

- **Environment and process:**  
  - Builds and deployments handled through Jenkins; code review via Gerrit.  
  - A hybrid Agile process with some ceremonies in place, but documentation was uneven and onboarding new engineers was slow.

My first tasks focused on:
- Understanding the ecosystem (Automotive builds, vehicle services, Bluetooth stack).  
- Stabilizing existing modules that were causing production incidents.  
- Gradually introducing cleaner architecture patterns (MVVM, repository layers) to improve maintainability and testability.

---

## What were my responsibilities?
I contributed across multiple layers, with a focus on stabilization, refactoring, and feature enhancements rather than pure greenfield work.

Key responsibilities and contributions:

- **Architectural refactoring:**  
  - Refactored existing UI modules toward a more modular **MVVM** architecture, improving separation of concerns and enabling unit tests around ViewModels and repositories.  
  - Introduced clearer boundaries between the UI layer, domain logic, and data sources (vehicle data, Bluetooth, network APIs).

- **Media and audio lifecycle:**  
  - Helped build and enhance media playback components, including better lifecycle handling for audio sessions and smoother transitions between sources (Bluetooth, USB, radio, in-app media).  
  - Worked with **AudioManager**, audio focus, and Bluetooth profiles (A2DP, HFP) to avoid conflicts and dropped audio during source switching.

- **Vehicle data integration:**  
  - Assisted with vehicle data display screens, consuming real-time data exposed by embedded systems via **AIDL / Binder** interfaces.  
  - Implemented data caching, fallback behavior, and error handling so the UI remained responsive even if data streams dropped or the vehicle bus was noisy.

- **Networking and data layer:**  
  - Improved networking reliability using Retrofit with structured error handling and retries where appropriate.  
  - Ensured the UI could gracefully handle intermittent connectivity as vehicles moved between coverage areas.

Overall, my work blended hands-on coding, cross-layer debugging, and collaboration with platform/hardware teams to make existing features more robust and maintainable.

---

## Challenges faced

1. **Learning curve: Automotive / embedded constraints**  
   - Coming from traditional Android, I had to quickly understand how Android behaves in an embedded, vehicle-controlled environment (custom builds, limited resources, strict UX rules).  
   - I addressed this by pairing with system and firmware engineers, reading selected **AOSP documentation** relevant to our features (e.g., audio, Bluetooth, input), and using bench setups to safely simulate vehicle signals.

2. **System stability and resource constraints**  
   - Infotainment units had tighter **CPU and memory budgets** than modern phones. Patterns that are fine on a flagship device could cause jank or watchdog resets in a car.  
   - I optimized UI rendering, background threading, and data refresh intervals through profiling (Android Profiler, trace logs) and async patterns (coroutines / background handlers where appropriate).

3. **Cross-team coordination**  
   - Distributed teams in the U.S. and Japan worked on platform, firmware, and app layers. Misalignment on **API contracts, firmware versions, or feature flags** could lead to subtle integration bugs.  
   - I helped by documenting behavior and expectations more thoroughly in Confluence, capturing protocol details in design docs, and tracking API dependencies explicitly in JIRA tickets.

4. **Bluetooth and vehicle data bugs**  
   - Bluetooth stack issues and vehicle data glitches often spanned multiple layers (phone, head unit, firmware, network).  
   - I participated in debugging sessions using **logcat, Bluetooth/HAL logs, and bench hardware**, learning how to correlate events across logs and verify edge cases like long drives, frequent reconnects, and source switching.

**Notes / clarifications for interviews:**
- Some typical **mobile patterns** (e.g., heavy background work, aggressive animations, large in-memory caches) had to be toned down for **infotainment units** due to resource and safety constraints. The focus was smooth, predictable behavior over flashy UI.
- As a senior Android dev on this kind of project, it’s realistic to say you **read targeted AOSP docs** (audio focus, Bluetooth APIs, input, windowing) rather than “all of AOSP.” Mention specific areas tied to your stories.
- Working with teams in Japan is plausible; keep it focused on **platform/firmware / Android system teams**, not necessarily “any Japanese team.” Emphasize time zones, documentation, and clear specs.
- Joining cross-team debugging sessions for **Bluetooth pairing/disconnection** or **vehicle data issues** is absolutely realistic for a senior app dev on an automotive project.

---

## What did I do?

### Early phase (first 6–8 months): Assessing, adapting, stabilizing
When I first joined Honda, my focus was on understanding the unique environment — moving from consumer Android to an Automotive / embedded head unit and learning how it interfaced with vehicle hardware.

My tangible contributions in this phase centered on stabilization and modernization of existing code rather than net-new features.

**1. Assessing the existing state and identifying bottlenecks**
- Performed focused code reviews and profiling to understand where performance issues came from (UI freezes, inconsistent vehicle data updates, memory leaks).  
- Mapped dependencies between the Android app layer and hardware interfaces (Vehicle HAL, OBD-II, Bluetooth stack), creating diagrams and notes that helped the team reason about failure modes.

**2. Stabilizing and refactoring critical modules**
- Refactored legacy, tightly coupled Java code into modular **MVVM** components using ViewModels and repository layers where appropriate.  
- Reduced memory leaks by auditing long-lived Services, cleaning up listeners and callbacks, and tightening lifecycle management for audio and media components.

**3. Improving reliability of the development pipeline**
- Helped QA and DevOps teams fine-tune Jenkins jobs, ensuring consistent CI builds across hardware variants and firmware versions.  
- Added basic automated checks for regressions in key flows (launch, Bluetooth connect, media playback).

### Later phase (next 8–10 months): Optimization, process, and cross-team improvements
Once I gained confidence in the ecosystem, I shifted toward performance optimization, process improvement, and mentoring newer team members.

**1. Redefining development and testing workflows**
- Advocated for more structured code reviews and Confluence-based documentation so new developers could onboard faster and understand platform nuances.  
- Helped clarify module ownership within the team — defining who owned which features and their tests, reducing “no one owns this bug” situations.

**2. Enhancing app performance and responsiveness**
- Tuned media playback and UI rendering to reduce frame drops under constrained hardware (e.g., avoiding heavy work on the main thread, batching updates, reducing overdraw).  
- Reworked parts of the network and data layers using Retrofit with better caching and error handling so the app behaved predictably during connectivity changes while driving.  
- Incrementally introduced **LiveData and ViewModels** in places where lifecycle-aware state management significantly reduced bugs and leaks.

---

## Technical failure in production

### Example: Bluetooth audio disconnects in production vehicles

**Context**  
Shortly after I joined, I was asked to help stabilize Bluetooth connectivity in the infotainment system. Users were reporting **intermittent Bluetooth audio disconnects**, especially:
- After long drives.  
- When switching between hands-free calls and streaming media.  
This was my first deep dive into Automotive Bluetooth and audio focus interactions.

**Why it happened**  
The root cause was a **race condition** between the audio service and the Bluetooth service lifecycle:
- When users switched between A2DP (media) and HFP (hands-free), the system would quickly release and reacquire audio focus.  
- In certain timing windows, audio focus was released before the reconnect request completed, causing dropped connections or delayed reconnection.  
- The issue was more visible on specific phone models and firmware combinations.

Our initial QA environment, using a narrow set of lab devices, didn’t fully reproduce these real-world timing issues.

**How I rectified it**

1. **Manual reproduction and log analysis**  
   - Paired multiple phones (different Android and iOS versions) and reproduced the disconnects during realistic usage (long sessions, frequent source switching).  
   - Captured and analyzed **logcat traces, Bluetooth stack logs, and audio focus events** directly from the in-car unit to identify the problematic sequence.

2. **Collaboration with hardware and platform teams**  
   - Worked with platform / firmware engineers to understand the exact sequence between Bluetooth HAL events and `AudioManager` focus requests.  
   - Together we confirmed that audio focus was occasionally **released too early**, before the reconnect path had completed.
     - TODO: why would it be released too early? How do we know this?

3. **Code fix and testing**  
   - Implemented a small **debounce and state synchronization mechanism** in the Bluetooth connection manager so that disconnect and reconnect events could not overlap in an unsafe way.  
     - TODO: how does a small debounce help here? What exactly does it do?
     - TODO: state synchronization here is ensuring that STATE_CONNECTED is only set after audio focus is fully acquired?
   - Verified the fix through extended in-car testing and regression runs using different phones and firmware builds.

4. **Process improvement**  
   - Documented the failure flow and added targeted test cases for switching between call and media sessions.  
   - Proposed more **real-world simulation scenarios** for QA, including mixed phone models, longer driving sessions, and repeated connect/disconnect cycles.

---

## Technology (study notes for this profile)

These are technologies that commonly show up in Android Automotive / embedded Android projects. They’re good areas to deepen as you build narratives around this role.

### AOSP (Android Open Source Project)
- **What it is:** The open-source code base for Android, including system services, frameworks, and core apps.  
- **Why it matters here:** Automotive and infotainment systems often use **customized builds of Android**, so teams may:
  - Read AOSP source to understand how framework components (audio, Bluetooth, input, windowing) behave.  
  - Patch or extend system services for vehicle-specific needs.
- **How to talk about it:**
  - “I used AOSP source and documentation to understand how `AudioManager` and Bluetooth events flowed through the system so we could debug complex issues.”  
  - “Being comfortable reading framework code helped bridge gaps between app-layer assumptions and platform behavior.”

### AIDL (Android Interface Definition Language)
- **What it is:** A way to define interfaces for **IPC (inter-process communication)** over Binder.  
- **Why it matters here:** Automotive stacks often expose **vehicle data, platform services, or privileged operations** via AIDL interfaces that app processes consume.  
- **How it was used in this profile:**
  - Vehicle data (speed, fuel level, tire pressure, maintenance) may be provided by a lower-level service, and the app subscribes via AIDL.  
  - You handle connection, disconnection, and error states when that remote service is not available or slow.
- **Interview angle:**
  - Emphasize understanding of **Binder**, AIDL interfaces, versioning, and how to handle remote exceptions and lifecycle events from services.

### NDK (Native Development Kit)
- **What it is:** Tooling to write parts of your app in **C/C++** for performance or to integrate existing native libraries.  
- **Why it might show up in Automotive:**
  - Some media, navigation, or hardware-integration components are implemented in native code for performance or legacy reasons.  
  - Even if you don’t write C++ daily, you may integrate with or debug around native components (reading logs, understanding crashes).
- **How to position it:**
  - “I wasn’t writing large amounts of C++, but I collaborated with teams who owned native components and used NDK tooling / symbols to help trace issues across the Java/Kotlin and native boundary when needed.”

### C++ in Android projects
- **Where it is used:**
  - Performance-critical paths (e.g., some audio/video, codecs, navigation engines).  
  - Legacy libraries reused from other platforms (embedded Linux, in-house stacks).
- **What’s expected at senior Android level:**
  - Basic familiarity: reading C++ code, understanding interfaces, recognizing memory issues at a high level.  
  - Ability to work with colleagues who own native components and factor their constraints into app design.

### Bluetooth protocols (A2DP, HFP, AVRCP) and Bluetooth stack integration
- **A2DP (Advanced Audio Distribution Profile):**  
  - Used for streaming **high-quality audio** from the phone to the car (music, podcasts).  
- **HFP (Hands-Free Profile):**  
  - Used for **phone calls**, controlling call audio routing, and handling call states (ringing, active, held).  
- **AVRCP (Audio/Video Remote Control Profile):**  
  - Enables **play/pause/next/previous** controls and metadata (track info, album art) between the car and phone.

- **Integration points in an Automotive app:**
  - Managing **pairing and reconnection flows**.  
  - Handling **audio focus** correctly when switching between call and media audio.  
  - Updating UI based on Bluetooth and media metadata changes.  
  - Logging and diagnosing issues when phones behave differently (OEM differences, OS versions).

- **Interview framing:**
  - “I worked closely with the platform Bluetooth stack to ensure smooth transitions between music and phone calls, and to reduce disconnects. This required understanding A2DP/HFP/AVRCP behavior and coordinating with `AudioManager` and system services.”

---

### Additional topics worth knowing for this profile

- **Automotive UX / driver distraction guidelines:** High-level awareness that apps must minimize distraction, favor voice and large touch targets, and align with regional regulations.
- **Performance under constraints:** How to profile and optimize on lower-spec hardware (reduced animations, careful memory use, fewer background services).
- **Logging and observability:** Using logcat, structured logs, and sometimes system traces to diagnose issues that span app, framework, and firmware.

These notes should give you enough depth to build convincing narratives around this Honda profile while staying technically realistic for a Senior Android Developer between 2019–2021.
