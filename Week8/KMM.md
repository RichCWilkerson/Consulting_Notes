# Useful tools:
A helpful tool to tell you if your environment is set up correctly is Kdoctor: https://github.com/Kotlin/kdoctor.
- It will inform you if you are missing any required components.

Kotlin Multiplatform Plugin
Xcode

KMP Environment Setup:
https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html#possible-issues-and-solutions

# Switch Branches
1) See remote branches
git fetch origin
git branch -a

2) Create a local branch that tracks remote/<branch>
git fetch origin
git checkout -b <branch> origin/<branch>

# Why?
- stable
- truly native performance for both platforms
- share busniess logic and infra code
- smaller app size
- faster development
- duplication can cause discrepancies and bugs

# What is KMP?
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

