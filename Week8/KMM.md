# Udemy Course:
[Udemy](https://www.udemy.com/course/kotlin-multiplatform-masterclass/?couponCode=NVD20PMUS)

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)

## Useful tools:
A helpful tool to tell you if your environment is set up correctly is Kdoctor: https://github.com/Kotlin/kdoctor.
- It will inform you if you are missing any required components.

Kotlin Multiplatform Plugin
Xcode

KMP Environment Setup:
https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html#possible-issues-and-solutions

## Switch Branches
1) See remote branches
git fetch origin
git branch -a

2) Create a local branch that tracks remote/<branch>
git fetch origin
git checkout -b <branch> origin/<branch>

## Why?
- stable
- truly native performance for both platforms
- share busniess logic and infra code
- smaller app size
- faster development
- duplication can cause discrepancies and bugs

## What is KMP?
KMP -> Computer apps, web apps, server apps, android and iOS apps

can choose to build common logic in Kotlin and share it across platforms
UI can be built using native frameworks

Kotlin Multiplatform Mobile (KMM) is an SDK that allows developers to share code between Android and iOS applications. 
It enables the use of Kotlin for cross-platform development, allowing for a single codebase for business logic and infrastructure while maintaining native performance and user experience on both platforms.

everything up to the viewmodel can be completely shared

recently we can use it to share UI code as well
- Compose Multiplatform
  - not recommended for large enterprise apps yet
  - still maturing

keep ui native for now
- business logic usually stays the same over time
- innovation usually occurs in the UI layer and mobile specific features




# Philipp Lackner Youtube Playlist

## What is KMP? and What is Compose Multiplatform?
[Youtube](https://www.youtube.com/watch?v=RSBO1C_Du2U&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk)

### KMP (Kotlin Multiplatform)
Kotlin Multiplatform lets you **share code across multiple platforms** (Android, iOS, web, desktop, backend) while still compiling to **native code** on each platform. You typically:
- Share **business logic, networking, data storage, validation, and utilities** in Kotlin.
- Keep **UI and platform-specific integrations** (e.g., notifications, camera, biometrics) in each platform’s native framework.

Key points:
- Single **codebase and test base** for shared logic → fewer duplicated bugs.
- Native performance on each platform (no big runtime bridge).
- Kotlin as the common language, Gradle as the build system (often with Kotlin DSL build scripts).

#### Targets
In a KMP project you define one or more **targets** (Android, iOS, web, desktop, etc.), and the same Kotlin source is compiled differently per target:
- Android → JVM bytecode
- iOS → Native binaries (via Kotlin/Native)
- Web → JavaScript
- Desktop → JVM bytecode or native binaries (depending on setup)

You also get access to **platform-specific APIs** from shared code via "expect/actual" declarations or platform-specific source sets. For example, you can:
- Target platform APIs like Camera, GPS, push notifications, file system, etc.
- Hide those behind shared interfaces so higher-level business logic stays platform-agnostic.

> Contrast with Flutter: Flutter uses a single Dart UI layer and a **bridge** to talk to native code, whereas KMP compiles directly to native artifacts and integrates more naturally with existing Android/iOS stacks.

#### Limitations / Trade-offs
- Shared (common) code can only use **Kotlin multiplatform libraries** (no direct Java-only libraries in `commonMain`).
  - If you plan to adopt KMP, prefer pure Kotlin libraries from the start.
- You still need **platform-specific code** where APIs differ (e.g., iOS vs Android permissions, navigation, native UI).
- A **Mac is required** to compile and run iOS apps:
  - You can write shared code anywhere, but you need macOS + Xcode to build and run on iOS simulators/devices.

### Compose Multiplatform
Compose Multiplatform takes the same declarative UI model as Jetpack Compose and lets you **share UI code** across platforms (Android, desktop, web, and gradually iOS).

- Goal: write UI once in Compose and run it on multiple platforms.
- Status: mature for **Android and desktop**, still **maturing for iOS and web**.

For production today:
- Many teams use **KMP for shared logic** and keep **UI native** per platform.
- Compose Multiplatform is great for internal tools, prototypes, and some production apps, but for large/critical enterprise apps you’ll still see more conservative adoption while the ecosystem stabilizes.


## Build CMP / KMP Project
[Youtube](https://www.youtube.com/watch?v=vvP5vnmzY84&list=PLQkwcJG4YTCS55alEYv3J8CD4BXhqLUuk&index=2)

kdoctor 
