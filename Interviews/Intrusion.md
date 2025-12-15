Role: Android Developer

Location: Dallas-Ft Worth, TX
Client: Intrusion
Vendor: Myridius

Duration: 6-month with contract to hire
implementation partner Myridius and end client Intrusion

Scope:
Contractor will be working to help the client create an endpoint application for Android to complement their Security product

Skillset:
- Android developer
- C#, C++, Rust, Kotlin, Javascript, Zero Trust, Swift
- Strong Networking knowledge
- Security knowledge

website: https://www.intrusion.com/
app: https://play.google.com/store/apps/details?id=com.intrusion.endpoint&hl=en

ABOUT:
- Intrusion Inc. (ticker: INTZ) is a cybersecurity company based in Plano, Texas. 
  - It provides cybersecurity solutions, including network intrusion detection and prevention systems. 
- Protect business endpoint devices with real-time threat intelligence. 
- Intrusion Shield Endpoint safeguards your device by blocking malicious websites and unwanted background traffic. 
  - It monitors traffic and checks them against Intrusion's extensive threat intelligence database. 
  - If a connection attempt is flagged as malicious or high-risk, Shield Endpoint immediately blocks it, keeping your endpoint secure. 
- Intrusion Shield Endpoint's traffic filter is enabled by a loopback VPN for the purpose of filtering traffic from other applications and applying real-time threat intelligence. 
  - It does not connect to a remote VPN server but routes traffic locally through the threat filter.

---

## Kal
### Architecture & Development
- Architected modular Android app components using MVVM/Clean architecture, enabling scalability and maintainability for luxury e-commerce experiences.
- Integrated Kotlin Multiplatform (KMM) and Swift modules for shared business logic between Android and iOS platforms.
- Implemented native performance-critical features in C++ (JNI) and Rust, optimizing image rendering, catalog browsing, and secure data encryption layers.

---

### Networking & Security
- Implemented a Zero Trust mobile security framework — including certificate pinning, token-based authentication (OAuth2), and mutual TLS communication for API traffic.
- Enhanced app network stack using OkHttp interceptors, gRPC, and caching with encryption-at-rest for sensitive PII data. 
- Conducted threat modeling and vulnerability assessments to safeguard personally identifiable and financial data in line with PCI-DSS and GDPR regulations. 
- Developed advanced telemetry and real-time session validation for detecting anomalies (e.g., session hijacks, MITM attempts).

--- 

### Cross-Platform & Interoperability
- Integrated JavaScript-based dynamic catalog components inside Android (via WebView bridge) ensuring secure sandboxing and data flow validation. 
- Worked with Swift/Kotlin shared logic to unify APIs across platforms while maintaining native UX specific to Android Compose. 
- Developed feature-flagged modules allowing dynamic rollout and rollback of new shopping experiences (styling advisors, wishlist sync) through remote config.
  - TODO: what does it mean by "feature-flagged modules"? how does this allow for dynamic rollout/rollback?

--- 

## My Adaption
750 Fort Worth Avenue • Dallas TX 75208

### Pitch
Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

At Neiman Marcus, I was brought in to modernize and scale the app.
I:
- Re‑architected the app into Clean MVVM Architecture with feature‑based Gradle modules, which sped up builds and made releases more predictable.
- Improved performance and stability by profiling with Android Studio and Firebase, then introducing lazy loading, Coil for images, and better background initialization.
- Hardened security with SSL pinning, token-based auth, and biometrics, and set up CI with GitHub Actions for automated testing.
- led and developed a KMM module for sign-up and login as a PoC to evaluate cross-platform code sharing for iOS and Android.
- Implemented native performance-critical features in C++ (JNI), optimizing image rendering,

Before that, at Ally Bank, I worked on the "One Ally" ecosystem, bringing banking, auto, investing, and mortgage into a single app.
There I:
- Implemented a Zero Trust mobile security framework — including certificate pinning, token-based authentication (OAuth2), and mutual TLS communication for API traffic.
- Enhanced app network stack using OkHttp interceptors, gRPC, and caching with encryption-at-rest for sensitive PII data.
- Conducted threat modeling and vulnerability assessments to safeguard personally identifiable and financial data in line with PCI-DSS and GDPR regulations.
- Developed advanced telemetry and real-time session validation for detecting anomalies (e.g., session hijacks, MITM attempts).

I really enjoy collaborating with other engineers to build useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name]
would be a fantastic place to continue growing my career and contribute.


--- 

gRPC = Google Remote Procedure Call, a high-performance, open-source universal RPC framework that uses HTTP/2 for transport and Protocol Buffers as the interface description language.
- substitute for REST -> more efficient, lower-latency communication, especially for streaming and bidirectional workflows.
- uses .proto files to define service methods and message types, which can then be compiled into client and server code in multiple languages.
- HTTP/2 instead of HTTP/1.1

threat modeling = a structured approach to identifying and mitigating potential security threats in a system by analyzing its architecture, components, and data flows.
- MITM, malware, rooted devices, token theft, replay attacks
- outcomes: cert pinning, token handling, device integrity checks, logging separation
vulnerability assessments = systematic evaluations of a system to identify security weaknesses that could be exploited by attackers.
- automated scans, manual code reviews, penetration testing
- 

PCI-DSS = Payment Card Industry Data Security Standard, a set of security standards designed to ensure that all companies that accept, process, store or transmit credit card information maintain a secure environment.
GDPR = General Data Protection Regulation, a regulation in EU law on data protection and privacy for all individuals within the European Union and the European Economic Area.
- no PII in logs, encryption at rest, data minimization
- keystore + encrypted DataStore for sensitive data (tokens, PII)

real-time session validation = continuous monitoring and verification of user sessions to detect anomalies or suspicious activities that may indicate security threats, such as session hijacking or unauthorized access.
- telemetry events (login success/failure, MFA challenges, pinning failures, device integrity failures)
- short-lived access tokens + server-side session validation
- token refresh patterns, impossible geolocation, abnormal device fingerprints
- cert pinning failures (potential MITM) 
- OUTCOME: 
  - backend fraud/security teams can detect anomalies in near real time

### Architecture and Cross-Platform at Neiman Marcus
OpenGL rendering and image processing were performance-critical, so I worked with C++ (JNI) modules to implement those features natively while exposing clean Kotlin interfaces.
- Performed some image post-processing (e.g., color correction, downsampling, maybe background blur) off the UI thread.
- Exposed a small, stable API to Kotlin through JNI so the Android layer just said things like renderFrame(), setZoom(level), or loadImage(uri).
- This was reusable across Android, iOS, and web platforms since the core C++ logic was shared.

### Networking & Security at Ally Bank
At Ally I helped push the mobile app toward a more Zero Trust, security-first posture.

On the networking side, I hardened the Android client so it never blindly trusted the environment. 
I implemented SSL/SPKI pinning on top of TLS 1.2+ using OkHttp and networkSecurityConfig, so the app only talked to Ally’s real backends. 
All API calls were fronted by OAuth2 access/refresh tokens and signed JWTs, and the app treated tokens as the only way to access APIs – user credentials were used once at login and never stored. 
For higher-risk services we worked with backend teams to support mutual TLS, so both the client and server authenticated each other with certs, not just the server.

I refactored the network stack around OkHttp interceptors and Retrofit so security was centralized and testable rather than sprinkled throughout the codebase. 
Interceptors handled things like automatically attaching OAuth2/JWT headers, refreshing tokens on 401s, enforcing pinning, and normalizing logging so that no PII or secrets ever hit logs. 
For some latency-sensitive, streaming, or bidi workflows we integrated gRPC from the mobile client to specific backend services, again wrapped with TLS, pinning, and the same auth model.

For data at rest, I treated PII and financial data as “encrypt by default”. 
Anything that needed to be cached locally (for example, parts of the Snapshot dashboard or recent transfers) went through Room or DataStore with encryption on top: keys in Android Keystore (TEE/StrongBox where available), and persistence via Jetpack Security primitives. 
That let us support offline/low-latency UX while still complying with PCI-DSS/GDPR expectations around encryption at rest, key management, and data minimization.

Before implementing these changes, I worked with security and architecture teams to run lightweight threat modeling on the main flows: login, MFA, biometrics, transfers, mobile check deposit. 
We walked through attack paths like MITM, token theft, session hijacking, rooted/emulated devices, and replay attacks. The outcomes turned into concrete controls in the app: stricter cert/pinning policies, hardened token handling, device integrity checks (Play Integrity/SafetyNet), and clearer separation between PII and non-PII logging.

I also added telemetry and real-time session validation hooks so we could actually see and react to attacks in production. The app emitted structured, non-PII events (login start/success/failure, MFA challenges, biometric fallback, pinning failures, device integrity failures) via our logging SDK into the bank’s SIEM rather than third-party analytics. Combined with short-lived access tokens and server-side session validation, this allowed backend fraud and security teams to detect anomalies in near real time – things like impossible geolocation, abnormal device fingerprints, repeated pinning failures (potential MITM), or suspicious token reuse patterns that hint at session hijacks.

In practice, my role was to translate those Zero Trust and compliance requirements (PCI-DSS, GDPR, FFIEC/CFPB/FDIC) into concrete Android implementations: certificate/SPKI pinning, mutual TLS where needed, OAuth2/JWT token flows, encrypted caching of sensitive data, device integrity checks, and telemetry that security teams could actually use to investigate and respond to incidents.

---


## Prep & Study Guide
### What this role is really about
- Build an **Android endpoint agent** that complements an existing **security product/platform**.
- Likely responsibilities:
  - Implement a robust Android app in **Kotlin** with solid architecture and DI (Hilt).
  - Integrate tightly with backend **security services** over secure network protocols.
  - Collaborate with teams using **C#/C++/Rust/Swift/JavaScript** (backend, native agents, iOS, web console).
  - Understand and apply **networking** and **security** best practices in a mobile context.
  - Help support or implement **Zero Trust**-style features (continuous verification, device posture, least privilege).


### What to Study
#### 1. Android & Kotlin (with a security/agent mindset)
- **Architecture**
  - MVVM, Repository pattern, use cases / domain layer.
  - Multi-module setup: `:app`, `:core:network`, `:core:security`, `:feature:*`.
  - Dependency Injection with **Hilt**: modules, scopes (Singleton, ActivityRetained, ViewModel), testing.
- **Background work**
  - Difference between **foreground service** and **WorkManager**; when to use which.
  - Handling periodic sync and long-running tasks under Doze / background limits.
    - **Doze / background limits (simple):** Android aggressively restricts background work to save battery. 
      - **Doze**: when the device is idle, network and background jobs are deferred and batched.
      - **Background limits**: apps have tighter restrictions on services, alarms, and jobs when not in the foreground.
      - For an endpoint agent, you typically use **WorkManager** for periodic work and a **foreground service** for truly continuous tasks, respecting these limits instead of fighting them.
  - Battery-aware scheduling, constraints (Wi-Fi only, charging, idle windows).
- **Networking on Android**
  - Retrofit + OkHttp + coroutines.
  - Timeouts, retries, exponential backoff, cancellation.
    - **Timeouts:** how long the client waits for connect/read/write before failing. Avoid infinite hangs.
    - **Retries:** re-attempt failed requests (typically for transient errors like timeouts/5xx), with limits.
      - 5xx = server error, worth retrying.
      - 4xx = client error, usually not retried except maybe 429 (rate limit).
      - 401 = unauthorized, trigger token refresh instead of retry.
        - TODO: why not retry 401 directly?
    - **Exponential backoff:** wait longer between retries (e.g., 1s, 2s, 4s, 8s) to avoid hammering the server.
    - **Cancellation:** cooperative cancellation with coroutines so network calls stop when the scope (e.g., ViewModel) is cancelled.
      - TODO: what does cooperative cancellation mean here?
  - Network monitoring basics (latency, throughput, error rates) and how to surface that for debugging.
    - **Latency:** time between request and response. High latency = slow user experience.
    - **Throughput:** how much data per second; relevant for large payloads or poor networks.
    - **Error rates:** % of calls failing (4xx/5xx/timeouts). Sudden spikes often indicate outages or misconfig.
    - In practice: add **structured logs/metrics** around key calls (start/end timestamps, status codes, error types) and send them to an observability tool (Datadog, Splunk, etc.) so backend and mobile teams can debug issues end-to-end.
- **Permissions & privacy**
  - Dangerous permissions and runtime request flow.
    - **Relevant dangerous permissions for a security/endpoint app (examples):**
      - `android.permission.ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` – device location (if part of risk posture).
      - `android.permission.READ_PHONE_STATE` – basic device state/ID (varies by API level and privacy rules).
      - `android.permission.CAMERA` – if you capture images for diagnostics.
      - `android.permission.READ_EXTERNAL_STORAGE` (legacy) – increasingly replaced by scoped storage.
    - **Runtime request flow (high level):**
      1. Declare permission in `AndroidManifest.xml`.
      2. At runtime, check `ContextCompat.checkSelfPermission`.
      3. If not granted, call `requestPermissions` (or Compose/Accompanist equivalent).
      4. Handle the result (granted/denied/"don’t ask again") and adapt behavior (show explanation, degrade gracefully).
  - Scoped storage basics.
    - **Scoped storage (simple):** A modern Android storage model that **limits broad filesystem access**.
      - Apps access only their own app-specific directories by default.
      - For shared media (images, video, audio), use `MediaStore` and specific APIs instead of raw file paths.
      - For a security app, the key idea is: you cannot freely scan the entire filesystem; you must respect these boundaries.
  - High-level understanding of VPN-style apps using `VpnService` (for an "endpoint" flavor).
    - **`VpnService` (simple):** Android API that lets an app act as a **user-space VPN**.
      - The app can create a virtual network interface and route device traffic through it.
      - A security/endpoint agent might use this to inspect, filter, or tunnel traffic to a security gateway.
      - For interviews, know that it enables per-app/on-device VPN behavior, but requires careful UX (always-on VPN, notifications, battery tradeoffs).


#### 2. Networking Fundamentals (beyond just Retrofit)
- **Core concepts**
  - TCP vs UDP; connection setup, reliability vs speed.
    - TCP is connection-oriented and reliable, while UDP is connectionless and faster but less reliable.
    - UDP will drop packets if the network is congested, while TCP will retransmit lost packets.
  - HTTP/HTTPS basics: methods, status codes, headers, caching.
    - Important headers/mechanisms:
      - **Auth:** `Authorization: Bearer <token>` for access tokens.
      - **Caching:** `Cache-Control`, `ETag`, `If-None-Match`, `Expires` – control whether responses can be reused.
      - **Content:** `Content-Type` (e.g., `application/json`), `Accept`, `Content-Length`.
      - For security apps, also know `X-Request-ID` / correlation IDs for tracing.
  - DNS resolution, latency, bandwidth, timeouts.
    - Focus on:
      - DNS: name → IP lookup; misconfig or slow DNS increases latency.
      - Latency: RTT impacts perceived performance of every API call.
      - Bandwidth: low bandwidth links need smaller payloads and careful retries.
      - Timeouts: must be tuned so the app is responsive but not too aggressive on flaky networks.
- **TLS and secure channels**
  - What a **TLS handshake** does at a high level.
    - Client and server agree on a secure connection by:
      - Negotiating protocol/cipher suite.
      - Server proving its identity via a certificate signed by a trusted CA.
      - Exchanging keys to derive a shared symmetric key.
      - Then all subsequent HTTP traffic is encrypted with that shared key.
  - Role of **CAs** and server certificates.
    - **Certificate Authority (CA):** trusted third party that signs server certificates.
    - Device/OS ships with a trust store of root CAs.
    - When connecting via HTTPS, the client checks that:
      - The certificate is signed by a trusted CA.
      - The certificate is not expired/revoked.
      - The certificate’s hostname matches the requested domain.
  - Forward secrecy and why TLS 1.2+ / 1.3 is preferred.
    - **Forward secrecy (simple):** even if an attacker later steals the server’s long-term private key, they **cannot decrypt past sessions**, because each session used its own temporary (ephemeral) keys.
    - TLS 1.2 and especially 1.3 make forward secrecy and modern ciphers the default, reducing attack surface.
- **Certificate pinning**
  - Why TLS alone can be vulnerable to MITM with compromised CAs.
  - How to implement pinning on Android:
    - OkHttp `CertificatePinner`.
    - Network Security Config XML.
  - How to handle pin rotation + failure modes (what happens if pin is wrong/expired).
    - **Pin rotation (simple):** you keep multiple valid pins (current + next) so you can rotate certificates without breaking clients.
    - If the pin is wrong/expired, the TLS handshake will fail **even if** the certificate is otherwise valid; the app must:
      - Treat it as a hard failure (no silent fallback),
      - Show a clear error, and
      - In practice, you’d release an app update with updated pins.

#### 3. Security & Zero Trust
- **Core security concepts**
  - Confidentiality, integrity, authenticity.
  - Symmetric vs asymmetric encryption (AES vs RSA/ECC).
    - **Symmetric:** one shared key for encrypt + decrypt (fast, used for bulk data) – e.g., AES.
    - **Asymmetric:** key pair (public/private); public key encrypts or verifies, private key decrypts or signs (slower, used for key exchange and signatures) – e.g., RSA, ECC.
  - Hashing vs encryption vs signing (SHA-256, HMAC, digital signatures).
    - **Hashing:** one-way function (e.g., SHA-256). Used for integrity checks; you can’t recover original data.
    - **Encryption:** reversible; protects confidentiality. You can decrypt if you have the key.
    - **Signing:** proves authenticity + integrity. Data is signed with a private key; verified with a public key (digital signatures) or shared secret (HMAC).
  - Common attacks at a high level: MITM, replay, token theft, phishing.
    - **MITM:** attacker sits between client and server, intercepting/modifying traffic. Defenses: TLS, cert validation, pinning.
    - **Replay:** attacker reuses a captured valid request/token. Defenses: nonces, timestamps, short-lived tokens.
    - **Token theft:** attacker steals access/refresh tokens (via malware, logs, insecure storage). Defenses: secure storage, least privilege tokens, rotation, scopes.
    - **Phishing:** tricking users into giving credentials to a fake site/app. Defenses: OAuth/OIDC, device-bound tokens, good UX.
- **Auth & tokens**
  - OAuth2 / OIDC basics: authorization code + PKCE for native apps.
    - **OIDC basics:** extension on OAuth2 that adds an identity layer (ID token) so the client can know *who* the user is, not just get an access token.
    - **Authorization code + PKCE (simple):**
      - Native app opens system browser for login.
      - App sends a random `code_challenge` derived from a secret `code_verifier`.
      - After user logs in, server returns an **authorization code** to the app.
      - App exchanges the code for tokens, sending the original `code_verifier`.
      - Server checks that `code_verifier` matches the earlier `code_challenge` – this prevents an attacker who steals the code from using it.
  - JWT structure: header, payload, signature; common claims (sub, exp, iat, scope).
    - You don’t need to hand-parse JWTs, but understanding structure helps you debug and discuss security:
      - **Header:** alg, typ.
      - **Payload:** claims like `sub` (user id), `exp` (expiry), `iat` (issued at), `scope` (permissions).
      - **Signature:** ensures token hasn’t been tampered with.
    - As a client dev, you **treat JWTs as opaque**, but knowing these fields helps with logging, debugging, and discussions with backend/security.
  - Access token vs refresh token lifetimes and storage.
    - Typical patterns (can vary):
      - **Access token:** short-lived, e.g., 5–15 minutes. Stored in memory only, used for API calls.
      - **Refresh token:** longer-lived, e.g., days to weeks. Stored securely on device (Keystore + encrypted storage). Used only to get new access tokens.
  - Secure token storage on Android: Keystore + EncryptedSharedPreferences / encrypted DataStore.
- **Zero Trust principles**
  - "Never trust, always verify"; no implicit trust based on network location.
  - Continuous verification of:
    - User identity.
    - Device posture (OS version, app version, security state).
    - Context (location, network, risk score).
  - Least privilege: grant only the minimum access needed, and re-evaluate over time.
  - How a mobile endpoint participates:
    - Sends telemetry (device info, app version, security signals) to the Zero Trust controller.
      - **Zero Trust controller:** the central service that evaluates signals (user, device, context) and decides whether to allow, step-up-auth, or block a request.
    - Receives policies/decisions from backend and enforces them in the app.
      - Example policies:
        - Require MFA when logging in from a new device or risky network.
        - Block access if OS is too old or device is rooted.
        - Restrict certain actions (e.g., wire transfers) to corporate network/VPN only.
- **Mobile security best practices**
  - R8/ProGuard for shrinking + obfuscation; keep rules for reflection-based libs.
  - Avoid logging PII or secrets; use structured, redacted logging.
    - **Redacted:** replace sensitive values with non-identifying placeholders, e.g. `user_id=12345` instead of email, or `token=***redacted***`. The goal is useful debugging without exposing secrets.
  - Detecting rooted/jailbroken devices at a high level and applying policy (warn, restrict, or block).
    - **Rooted/jailbroken:** user (or malware) has elevated privileges, can bypass OS protections, tamper with app or traffic.
    - Detection (high level): look for known root binaries, writable system partitions, debug flags, abnormal environment.
      - TODO: how? android we write code to detect for these things?
    - Policy: based on risk appetite, you might
      - Show a warning,
      - Restrict sensitive actions, or
      - Block usage entirely for high-risk features.


#### 4. Cross-language / ecosystem awareness
- **C/C++/Rust**
  - Often used for low-level networking, packet inspection, cryptography, or existing security agents.
  - High-level understanding of NDK/JNI and how Kotlin can call into native libraries.
    - **NDK (Native Development Kit):** toolchain for building C/C++ code (.so libraries) for Android.
    - **JNI (Java Native Interface):** bridge that lets JVM (and thus Kotlin) code call native functions and vice versa.
    - Kotlin calls native libraries by declaring `external` functions that are implemented in C/C++ and loaded via `System.loadLibrary`.
  - Why security-critical or performance-sensitive code might live in Rust/C++ and be shared across platforms.
    - Rust/C++ provide:
      - High performance and low-level control (good for packet inspection, crypto).
      - Ability to reuse the same core engine across Android, iOS, desktop.
      - Rust adds stronger memory safety guarantees, which is attractive for security code.
- **C#, Swift, JavaScript**
  - C#: likely backend services or Windows endpoint.
  - Swift: iOS version of the endpoint agent.
  - JavaScript: admin dashboards, consoles, or configuration UIs.
  - Emphasize **shared contracts** (OpenAPI/Swagger, Protobuf/JSON schemas) and consistent auth/error-handling across platforms.
    - **Shared contracts:** a single, versioned description of APIs and data models that all platforms (Android, iOS, web, backend) follow.
      - Example: OpenAPI spec for REST endpoints, protobuf schemas for gRPC.
      - Important because it keeps everyone aligned on request/response shapes, error codes, and auth requirements, reducing subtle cross-platform bugs.

### Possible Interview Questions (with sample answers)
#### Android & Architecture
1. **"How would you architect an Android endpoint app that continuously communicates with a security backend?"**
  - I’d use a multi-module, MVVM-based architecture with a clear separation between UI, domain, and data. The `:core:network` module would encapsulate Retrofit/OkHttp + TLS/pinning, and a `:core:security` module would handle token storage, Zero Trust policies, and device posture reporting. For continuous communication, I’d use WorkManager for periodic sync and a foreground service only when truly necessary. Hilt would wire everything together, making it easy to swap implementations for tests.

2. **"When would you use a foreground service vs WorkManager for an agent-style app?"**
  - I’d use a foreground service when the app needs to do ongoing, user-visible work that must keep running, like an always-on VPN or active protection. It shows a persistent notification and tells the system the work is important. For periodic or deferrable tasks like sending telemetry, syncing policies, or health checks, I’d use WorkManager with appropriate constraints. That way I respect Doze/background limits and battery while still meeting security requirements.

3. **"How do you design your modules so that core security logic is reusable and testable?"**
  - I isolate security logic in a `:core:security` module that has no Android UI dependencies. It exposes interfaces for things like token storage, device posture collection, and policy evaluation. The Android app module provides platform-specific implementations (Keystore, DataStore, VpnService wrapper) via Hilt. This keeps security logic testable with plain unit tests and makes it reusable across features without duplicating code.

4. **"How would you inject and mock a networking or security component using Hilt?"**
  - In production, I define a Hilt module that provides singletons like `OkHttpClient`, `Retrofit`, and `SecurityManager`. For tests, I create a test Hilt module or use `@BindValue`/`@TestInstallIn` to replace those bindings with fakes or mocks. That lets me simulate different network/security scenarios—timeouts, 401s, policy blocks—without touching the real backend. The key is depending on interfaces, not concrete implementations, so swapping them is trivial.

#### Networking
1. **"Walk me through what happens when your app makes an HTTPS call to the backend. How is that secured?"**
  - The app uses OkHttp/Retrofit to open an HTTPS connection. Under the hood, TLS performs a handshake: negotiates cipher suites, verifies the server certificate using the OS trust store (and optionally pins), and derives a shared symmetric key. After that, all HTTP traffic is encrypted in transit. On top of TLS, we send an OAuth2 access token in the `Authorization` header. So we have transport security via TLS and application-level security via tokens and scopes.

2. **"How would you implement TLS certificate pinning in an Android app, and what are the trade-offs?"**
  - I’d either configure `CertificatePinner` in OkHttp or use a Network Security Config XML that pins the server’s public key or certificate hash. On each connection, OkHttp verifies both the normal certificate chain and the pin. The trade-offs are: better protection against MITM with rogue CAs, but more operational overhead. If you rotate certificates or change CAs, you must update the pins and ship a new app version; otherwise users will see hard failures even though TLS is valid.

3. **"How do you handle flaky networks, timeouts, and retry logic in your networking layer?"**
  - I configure sensible connect/read/write timeouts and centralize retry logic in the networking layer or an interceptor. For transient errors like timeouts or 5xx, I use limited retries with exponential backoff and jitter. For user-initiated actions, I surface clear errors instead of infinite retries. I also instrument calls with metrics—latency, error codes—so we can tune timeouts and backoff based on real-world behavior rather than guessing.

#### Security & Zero Trust
1. **"What does Zero Trust mean to you, and how would an Android client participate in a Zero Trust architecture?"**
  - Zero Trust means we don’t implicitly trust any user, device, or network—every request is continuously verified based on identity, device posture, and context. On Android, the client participates by securely authenticating the user, collecting device posture signals (OS version, app version, root status), and sending them to a Zero Trust controller. The backend makes the final allow/deny or step-up decision, and the app enforces it—for example, requiring MFA, blocking sensitive actions on rooted devices, or restricting access off corporate networks.

2. **"How would you store and refresh access/refresh tokens securely on Android?"**
  - I keep the short-lived access token in memory only and use it for API calls. The long-lived refresh token is stored encrypted at rest using the Android Keystore plus EncryptedSharedPreferences or encrypted DataStore. When a 401 or expiry is detected, I call a refresh endpoint with the refresh token, receive a new access token (and ideally a rotated refresh token), and update secure storage. All token handling code is centralized so we can audit and test it easily.

3. **"What are some common mobile security pitfalls, and how do you avoid them?"**
  - Common pitfalls include storing secrets in plain text or in the repo, accepting all SSL certificates, logging PII or tokens, and not obfuscating the app. I avoid these by using Keystore + encrypted storage for secrets, enforcing strict TLS validation with optional pinning, using structured logging with redaction, and enabling R8/proguard with appropriate keep rules. I also watch for rooted/jailbroken devices and apply policy when risk is too high.

4. **"How do you ensure your app remains secure even if the device is rooted or compromised?"**
  - You can’t fully trust a rooted device, but you can reduce risk. I detect signs of rooting at startup and periodically, then apply policy: warn the user, restrict high-risk actions, or block usage based on business requirements. I also minimize secrets on device, use hardware-backed Keystore when available, and design APIs so that sensitive operations require server-side checks and short-lived tokens, not just trusting the client.

#### Cross-team / Cross-platform
1. **"We already have a C++/Rust component that inspects traffic. How would you integrate it into the Android app?"**
  - I’d expose a clean Kotlin interface for the inspection engine and implement it using JNI/NDK to call into the existing C++/Rust library. The Android layer would handle lifecycle, threading, and error translation, while the native layer focuses on performance-critical work. For testing, I’d provide a pure Kotlin fake implementation so we’re not forced to load native code in unit tests.

2. **"How would you work with the iOS and backend teams to define a consistent security and auth model?"**
  - First, we’d agree on a single set of API contracts and auth flows—OpenAPI specs for endpoints, a shared OAuth2/OIDC model, and common error codes. We’d align on token lifetimes, scopes, and refresh behavior so Android, iOS, and web behave consistently. Then we’d document these in a central place (Confluence, API portal) and add contract tests or schema validation in CI so any change to the auth model is visible to all platforms.

3. **"How do you keep API contracts and data models in sync across Android, iOS, and web?"**
  - We use versioned contracts—OpenAPI/Swagger for REST or protobuf for gRPC—and treat them as the source of truth. Android and iOS generate or hand-write models based on those specs, and we run compatibility tests in CI. Any breaking change requires a new API version and a deprecation plan, rather than silently changing responses. This reduces surprises and ensures all clients evolve together.
