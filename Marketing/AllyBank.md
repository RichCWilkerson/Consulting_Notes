
## Why was I hired?
I was brought on as a Senior Android Developer to help modernize and stabilize Ally’s mobile banking app. At the time, they were migrating much of their legacy Java codebase to Kotlin and introducing new security and compliance-driven features like MFA and biometric authentication. They needed senior developers who could both implement these critical features and improve the overall architecture and performance of the app.


## What was the state of the project when I joined?
When I joined, the app was fully functional and had a large active user base, but there were issues with legacy Java code, inconsistent architecture patterns, and some performance bottlenecks in network calls and UI rendering.
The production environment was stable but heavily regulated — deployments required multi-stage approvals due to financial compliance. CI/CD pipelines were in place but needed optimization for faster build and test cycles. Overall, the app was solid, but there was a clear focus on refactoring, modernization, and enhancing security.


## What were my responsibilities?
Secure Login & Authentication:
Integrated Android Biometric APIs for fingerprint and face authentication, combined with MFA (SMS/Email OTP) and end-to-end encryption.
Account Management:
Built modular Kotlin-based features for viewing balances, transaction histories, and account summaries using MVVM and Clean Architecture.
Fund Transfers & Bill Pay:
Implemented the transfer flow using Retrofit for secure API communication, Coroutines for async processing, and Room for caching transaction data.
UI Modernization:
Built some new features with compose, compose was still maturing during this time, but gave our team leverage on knowing its use cases
Performance Optimization:
Reduced network latency and startup time by optimizing Retrofit calls and adding caching layers