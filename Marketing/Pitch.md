Background:
- bachelors in Dec 2011
- born 11/11/1988 -> 37 today
- graduated high school 2007 


# Pitch

Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

At Neiman Marcus, I was brought in to modernize and scale the app. 
I:
- Re‑architected the app into Clean Architecture with feature‑based Gradle modules, which sped up builds and made releases more predictable.
- Led the move to Jetpack Compose, starting with a hybrid XML+Compose approach and then fully composing new features, which cut UI development time and improved design parity with Figma.
- Improved performance and stability by profiling with Android Studio and Firebase, then introducing lazy loading, Coil for images, and better background initialization.
- Hardened security with SSL pinning, token-based auth, and biometrics, and set up CI with GitHub Actions for automated testing.

Before that, at Ally Bank, I worked on the "One Ally" ecosystem, bringing banking, auto, investing, and mortgage into a single app. 
There I:
- Implemented secure login and authentication flows combining biometrics with MFA, ensuring compliance with FDIC, GFCR, and CFPB.
- Built modular, Kotlin-based features for account management, fund transfers, and bill pay using MVVM, Coroutines, Retrofit, and Room with Jetpack Compose.
- mobile check deposit

I really enjoy collaborating with other engineers to build useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name]
would be a fantastic place to continue growing my career and contribute.



## Neiman Marcus - Lead Android Developer
2023 Oct - Present

1. Architected Clean Architecture + MVVM structure:
  - Broke the legacy monolith into feature and core Gradle modules, separating UI, domain, and data layers. This allowed for faster release cycles and independent feature development.
2. Introduced Jetpack Compose:
  - Started with ComposeView inside XML fragments, then migrated new features entirely to Compose. Used Figma-to-Compose integration to ensure pixel-perfect design fidelity.
3. Performance Optimization:
  - Used Android Studio Profiler and Firebase Performance Monitoring to detect slow startup routines.
  - Implemented lazy loading, deferred initialization, and switched from Glide to Coil for image rendering, reducing load time by ~25%.
4. Security Enhancements:
  - Added SSL Pinning (OkHttp CertificatePinner), Tokenization (Auth0), and Biometric authentication using the AndroidX Biometric API.
5. CI/CD Automation:
Configured Github Actions for pull request validation


CLEAN -> create a dependency graph, identify circular dependencies

### Challenge
✔ LeakCanary
Identified memory leaks & stuck references.
✔ Android Studio Profiler
Showed heap usage, bitmap size, reference chains.
✔ Firebase Crashlytics
Confirmed the crash signature and validated the fix.

Key learning from challenge:
That incident reinforced for me the importance of treating production analytics as part of the development lifecycle, not as an afterthought.
Going forward, I make sure that every feature includes:
-  Clear telemetry and crash monitoring hooks from day one.
-  A staging environment with representative test data and devices for real-world simulation.
-  Regular memory and performance profiling as part of release readiness.
   It also deepened my appreciation for cross-functional collaboration — I coordinated with QA and backend to ensure image sizes and caching headers were optimized at both ends.


## Ally Bank - Senior Android Developer
2021 Apr - 2023
Detroit, MI

- "One Ally" Ecosystem -> banking, auto, investing, mortgage all in one app
- move to CLEAN modules
- look into compose for new features

1. Secure Login & Authentication:
  - Integrated Android Biometric APIs for fingerprint and face authentication, combined with MFA (SMS/Email OTP) and end-to-end encryption.
2. Account Management:
  - Built modular Kotlin-based features for viewing balances, transaction histories, and account summaries using MVVM and Clean Architecture.
3. Fund Transfers & Bill Pay:
  - Implemented the transfer flow using Retrofit for secure API communication, Coroutines for async processing, and Room for caching transaction data.


### Challenge: FDIC Compliance and auditability
- Security teams and auditors required clear evidence of how login, MFA, biometrics, and encryption were implemented.
- **Solution:**
    - Documented the **end-to-end login sequence**, data flows, and storage locations:
        - Used `Confluence` for written runbooks and implementation notes.
        - Created sequence diagrams and data-flow diagrams in `draw.io` (diagrams.net) and linked them from tickets and Confluence.
        - Kept architecture decisions in lightweight `ADR` pages so security and audit could see when/why changes were made.
            - ADR = Architecture Decision Record
    - Added structured logging (without PII/secrets) around auth flows so operations and audit teams could trace login attempts and MFA challenges end-to-end:
        - On Android, logged key auth events (login start/success/failure, MFA challenge/verify, biometric success/fallback) to a centralized pipeline via the existing logging SDK (built on top of `Timber` + backend log aggregation).
            - Log.d styled logs -> normalized eventType, timestamp, deviceId, appVersion, userId (hashed), eventDetails (non-PII) -> sent to backend logging system (SIEM)
                - SIEM = Security Information and Event Management
            - called the logging SDK at auth points (login start, login success, login failure, mfa challenge sent, mfa verified, biometric success, biometric fallback)
        - Used `Firebase Crashlytics` for crash-level visibility and stability around the login/MFA flows, but routed security/audit events to the bank’s internal log/`SIEM` rather than third‑party analytics.
            - no 3rd party -> don't send PII or sensitive info to Firebase, only crash stack traces and non-PII metadata
            - regulatory compliance says we need to keep sensitive logs in-house


## Zoom - Senior Android Developer
July 2019 - 2021

1. Real-time meeting workflow enhancements
   - Improved video/audio session handling
   - Refined participant management, mute/unmute flows, hand-raise, chat sync
   - Worked on RN ↔ Native bridge consistency
   - Optimized threading and minimized blocking calls during meetings
2. React Native module development
   - Profiles, feeds, content creation tools, messaging
   - Ensured predictable state management with Redux
   - Standardized JS <-> Native contracts
3. Performance improvements (pre- and mid-COVID growth)
   - Reduced overdraw and UI jank in video tiles
   - Memory usage optimizations during long sessions
   - Profiling with Perfetto/Systrace to identify hot paths
   - Improved recycling logic for meeting participant tiles


## Honda - Senior Android Developer
Jan 2018 - 2019
Torrance, CA

I contributed across multiple layers, focusing on stabilization, refactoring, and feature extension rather than creating everything from scratch.
Some highlights:
1. Migrations from Java to Kotlin for key modules, improving code safety, maintainability, and features like coroutines for async tasks.
2. Refactored existing UI modules into a more modular MVVM architecture, improving testability and separation of concerns.
   - View Model and Live Data introduced in 2017 made this more possible and started to look at how to implement it
3. Helped build and enhance media playback components, including better lifecycle handling for audio sessions and smoother transitions between sources (Bluetooth, USB, radio).
4. Assisted with vehicle data display screens, consuming real-time data provided by the embedded systems team through AIDL-based IPC interfaces.
   - AOSP knowledge helpful here
     - allows you to work with low-level system services and hardware abstraction layers (sensors, audio, display)


## How did you use a WorkManager?
### Neiman Marcus
“At Neiman Marcus, one of my early responsibilities was stabilizing background operations that were causing slow startups and occasional ANRs. 
A lot of analytics, personalization, and catalog-sync code ran on the main thread or used custom CoroutineScopes that weren’t lifecycle-aware—so if the app was backgrounded or the process died, the work was lost.

I introduced WorkManager as the unified scheduler for all non-urgent background tasks. For example:
- Catalog/Inventory delta syncs now run as a PeriodicWorkRequest, ensuring product availability data stays fresh without blocking startup.
- Personalized recommendation prefetching (used on the Home and PDP screens) runs as constraints-bound work—only on Wi-Fi + charging—to avoid impacting mobile data usage.
- Analytics batching moved to WorkManager so events are reliably uploaded even if the app is backgrounded or killed.

Migrating these workflows to WorkManager reduced startup time (by deferring non-critical initialization), improved battery efficiency, and significantly reduced dropped analytics events. 
It also simplified our architecture by consolidating background work into a single, observable pipeline.”

### Ally Bank
“At Ally, reliability and data consistency were absolutely critical because the app supported banking, auto, investing, and mortgage—all in one ecosystem. We often had data that needed to be synced or uploaded reliably, regardless of whether the user kept the app open.

I used WorkManager for several key flows:
- Secure message and document sync (e.g., statements, secure messages, notices) were uploaded/downloaded using WorkManager tasks with network + device-idle constraints. 
- For the Fund Transfer module, I used WorkManager to schedule transaction confirmation + audit logging to ensure every transfer generated a compliant event trail, even with intermittent connectivity.
  - This is not the transfer itself, which happens immediately (Coroutine), but the logging and confirmation step after.
- We also used WorkManager to retry failed API calls for low-priority actions—such as dismissing in-app alerts or updating user preferences—using exponential backoff to meet compliance and minimize server load.
  - TODO: i don't know what this means...

WorkManager’s guaranteed execution was a perfect fit for our regulated context because it allowed us to handle background work reliably while respecting Android Doze mode, network constraints, and encryption requirements.”