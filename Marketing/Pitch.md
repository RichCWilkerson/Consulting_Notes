# Pitch

Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

In my current role I was tasked with modernizing and scaling the app,
1. I architected and implemented a scalable Android app using Clean Architecture and MVVM, working with the team to identify module boundaries, then breaking the codebase into feature-based Gradle modules -> improving feature release cycles and reducing build times by about 30%.
3. To modernize the UI, I introduced Jetpack Compose, starting with a hybrid XML + Compose approach before fully migrating new features, which cut UI development time and improved design parity with Figma.
5. On the backend side, I implemented secure payment and authentication flows using tokenization, SSL pinning, and biometric verification, and
6. improved app performance by reducing load times by about 25% through lazy loading, image optimization with Coil, and background initialization.
7. I’ve also set up CI/CD pipelines using Github Actions, ensuring smooth, automated testing and deployments across QA and production.
8. Along the way, I’ve mentored developers, conducted code reviews, and helped shape best practices across the team.

I really enjoy building useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name]
would be a fantastic place to continue growing my career and contributing to meaningful innovation in mobile development.



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
