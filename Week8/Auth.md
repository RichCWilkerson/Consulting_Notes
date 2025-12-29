# Android Authentication & Security Overview

[cleartext doc](https://developer.android.com/privacy-and-security/risks/cleartext-communications#:~:text=Allowing%20cleartext%20network%20communications%20in,numbers%2C%20or%20other%20personal%20information)


there is an interface in Java, 

Think of Android auth/security in **four layers**:
1. **Identity & Tokens** – Who is the user? What are they allowed to do?
2. **On-device Authentication** – How does the user unlock the app or confirm actions?
3. **Data Protection** – How do we protect data at rest and in transit?
4. **Session & Access Control** – How do we manage logins, sessions, and permissions over time?

Below is a grouped overview tuned for Android, with high-level explanations, pros/cons, and what’s considered standard today.

---

## Compliance
PCI-DSS - for hardware and software that handles credit card data.
- PCI DSS (Payment Card Industry Data Security Standard) is a global set of security rules for businesses handling credit/debit card info, 
- aiming to cut fraud by protecting cardholder data through strong controls like encryption, access limits, and secure networks, enforced by major card brands under the


- HIPAA - for healthcare data in the US.
GDPR - EU data protection and privacy regulation.

### OWASP (Web & Mobile Security Guidelines)
- **What it is:**
  - OWASP (Open Worldwide Application Security Project) publishes community-driven guidance like the **OWASP Top 10** for web and **OWASP MASVS/MAS Top 10** for mobile.
- **Why it matters for Android:**
  - Gives a checklist of common risks (injection, insecure storage, insufficient logging, etc.) and mitigations.
  - Many enterprise security reviews will reference OWASP Top 10 or MASVS as target standards.
- **How to use it as an Android dev:**
  - Map your practices in this file back to OWASP items: secure storage, strong auth, TLS, logging without secrets, etc.
  - When talking to security teams, you can say "we align with OWASP guidance for mobile" and be specific about which risks you’re addressing.

HTTPS - defined in the manifest file to ensure all network traffic is secure.
- enforce HTTPS by default it is true (HTTPS) `android:usesCleartextTraffic="true"` in the application tag of the AndroidManifest.xml file.
  - set to false if you want HTTP

- refresh token -> session management -> generates a new refresh token
  - if you are not authorized -> no refresh token -> go back to login screen
  - if change device -> in header, sync user info 
  - keystore -> don't store tokens, this is for c
  - encrypted datastore -> for runtime storage of tokens, app session token 
    - when i open the app -> sends session token to server to check if still valid 

- 401 -> refresh token -> get new access token 
  - if refresh token expired -> go back to login screen
  - clear tokens and caches
  - 201 -> successful let them have a session token 

- retry policy -> exponential backoff
  - number of retries
  - timer of each retry

  

## 1. Identity & Tokens (AuthN & AuthZ)
**Goal:** Prove who the user is (authentication) and what they can do (authorization).

### OAuth2 (Authorization)
- **What it is:**
  - A framework that lets a user grant a client (your app) limited access to resources on a server without sharing credentials.
  - Produces **access tokens** (short-lived) and often **refresh tokens** (longer-lived session tokens).
- **Where you see it in Android:**
  - "Sign in with X" flows.
  - Custom bank / enterprise backends issuing access tokens for APIs.
- **Pros:**
  - Widely adopted standard; fits microservices and mobile well.
  - Scopes let you do fine-grained access control.
- **Cons:**
  - Spec is large; easy to misconfigure (wrong grant types, long-lived tokens, etc.).
- **Current standard:**
  - **Use the Authorization Code flow with PKCE** for mobile apps; avoid implicit flows.

### OpenID Connect (OIDC) – Identity on top of OAuth2
- **What it is:**
  - A thin layer on top of OAuth2 that adds **ID tokens** and a standard way to get user profile data.
  - Answers: "Who is this user?" not just "Is this request allowed?".
- **Where you see it:**
  - Login flows with dedicated identity providers (Okta, Auth0, Azure AD, Cognito, Firebase, etc.).
- **Pros:**
  - Standardized user info (subject id, email, etc.).
  - Works well with SSO.
- **Cons:**
  - More moving parts (discovery endpoints, ID token validation).
- **Current standard:**
  - **Preferred for modern login/SSO**. Use OIDC with OAuth2 instead of rolling your own auth.
    - SSO = Single Sign-On (user logs in once, accesses multiple apps like Google, Facebook, etc.)

### JWT (JSON Web Token)
- **What it is:**
  - A token format (header + payload + signature, base64-encoded JSON) often used for access or ID tokens.
- **Where you see it:**
  - Access tokens returned from OAuth/OIDC; `Authorization: Bearer <jwt>`.
- **Pros:**
  - Self-contained claims; easy to inspect and validate signature server-side.
  - Works well across services.
- **Cons:**
  - Easy to misuse (too long-lived, too many sensitive claims, not rotating keys).
  - On the **client**, you typically treat JWT as **opaque data**; avoid parsing it for business logic.
- **Current standard:**
  - Common for access/ID tokens, especially in microservice environments.

### Android Identity / Credential Manager / Google Identity Services
- **What it is:**
  - Android/Google APIs to simplify sign-in: passwords, passkeys, federated sign-in.
  - Wraps OAuth/OIDC flows and password managers.
- **Where you see it:**
  - "Sign in with Google", passkeys, one-tap sign-in.
- **Pros:**
  - Better UX, fewer password flows.
  - Integrates with device-level identity features.
- **Cons:**
  - Ties you more closely to Google’s ecosystem.
- **Current standard:**
  - Recommended way to implement modern sign-in UX on Android (especially passkeys).
  - Not for sensitive enterprise apps that need custom IdPs (Bank, Healthcare).

### Firebase Auth
- **What it is:**
  - Managed authentication service by Google (email/password, phone, social providers).
- **Where you see it:**
  - Startups, smaller apps, or when you don’t want to manage your own auth backend.
- **Pros:**
  - Quick to integrate; supports multiple identity providers.
- **Cons:**
  - Ties you into Firebase ecosystem; less control than a custom OIDC server.

### Enterprise / Legacy Identity Concepts (Know at High Level)
- **SAML** – XML-based single sign-on protocol (often between enterprise IdP and web backends). Mobile apps usually get OAuth/OIDC tokens from a gateway instead of dealing with SAML directly.
- **SSO (Single Sign-On)** – Pattern where user signs in once and can access multiple apps. Typically implemented today using OAuth2/OIDC (or SAML in older systems).
- **LDAP, Kerberos** – Backend/enterprise auth for internal systems. Mobile apps usually only see the resulting access tokens, not these protocols directly.

---

## 2. On-device Authentication / Factors
**Goal:** Verify that *the right person* is using the already-authenticated account.

### Biometric Auth (Fingerprint/Face via `BiometricPrompt` / AndroidX Biometric)
- **What it is:**
  - APIs to use fingerprint/face/device credential to unlock secrets or confirm sensitive actions.
- **Typical Android use:**
  - Unlock an encrypted token or key stored in Keystore.
  - Confirm high-risk actions (payments, transfers) without re-entering password.
  - app does not get biometric data; just a yes/no from the system (device)
- **Pros:**
  - Great UX, strong security (especially with hardware-backed sensors).
- **Cons:**
  - Not available / enrolled on all devices; must have fallback (PIN/password).

### MFA (Multi-Factor Authentication)
- **What it is:**
  - Using **two or more factors**: something you know (password), something you have (device, OTP), something you are (biometric).
- **Where implemented:**
  - Usually in the **backend + identity provider** (OIDC/OAuth2/Firebase Auth); Android app just orchestrates the UX: enter OTP, approve push, etc.
    - OTP (One-Time Password) = temporary code sent via SMS, email, or authenticator app.
    - The backend validates the OTP/second factor and then issues the normal OAuth2/OIDC token response (access/ID/refresh tokens).
- **Role of Android Developer:**
  - Build the UX to:
    - Let the user choose a factor (SMS, authenticator app, push, etc.).
    - Collect the second factor (code input, biometric prompt, push approval screen).
    - Send the factor (OTP or challenge response) to the backend via API.
  - Handle the server response:
    - On success: store tokens securely (Keystore + encrypted storage) and treat it like a normal login.
    - On failure: show errors, allow retries, or fallbacks to other factors.

- **Pros:**
  - Huge security improvement over password only.
- **Cons:**
  - More friction; must design UX carefully (fallbacks, recovery).

---

## 3. Data Protection (At Rest & In Transit)
**Goal:** Protect user data and secrets from theft or tampering.

### On-device Storage / Obfuscation

#### Android Keystore
- **What it is:**
  - OS-provided secure storage for **cryptographic keys**, often hardware-backed.
  - doesn't store strings, makes keys non-exportable (hard to extract even on rooted devices)
- **Use in Android:**
  - Generate a key in Keystore; use it to encrypt/decrypt tokens or PII via EncryptedSharedPreferences/DataStore/SQLCipher.
  - for runtime use of tokens, not for long term storage
    - does not protect plaintext data or hard-coded secrets
- **Work Flow:**
  - Generate a symmetric key in Keystore.
  - Use that key with EncryptedSharedPreferences or encrypted DataStore / DB.
  - Store secrets (refresh tokens, session IDs, user data) in those encrypted stores.
- **Pros:**
  - Keys are non-exportable; harder to steal even on rooted devices.
- **Cons:**
  - API can be confusing; some edge cases across Android versions.

#### EncryptedSharedPreferences / DataStore Encryption
- **What it is:**
  - Jetpack Security APIs (`EncryptedSharedPreferences`) and DataStore with encryption wrappers.
  - Use Keystore-managed master key to encrypt key–values.
    - encrypt data at rest with key-value or proto datastore using a master key from keystore
    - encrypt values accessed with Keystore keys
- **Use in Android:**
  - Store refresh tokens, user settings, and other secrets at rest.
    - at rest means when the app is closed or device is off
- **Pros:**
  - Simple to use; built on Keystore.
- **Cons:**
  - Still local storage: if the device is fully compromised, an attacker may tamper with data; use in combination with server-side checks.

#### SQLCipher / Room + Encryption
- **What it is:**
  - Full database encryption layer over SQLite/Room.
- **Use in Android:**
  - Protect large local caches that may contain PII or sensitive business data.
- SQLCipher uses SQLite with AES encryption. 

#### R8/ProGuard (Code Obfuscation)
- **What it is:**
  - Tools that shrink, optimize, and obfuscate your app code.
- **Role in security:**
  - Not authentication, but helps make reverse-engineering harder.
- **Pros:**
  - Smaller APK/AAB, more difficult to analyze code.
- **Cons:**
  - Misconfigured rules can break reflection-based libraries.


### Transport / Backend Trust

#### TLS/SSL, HTTPS
- **What it is:**
  - Protocols that encrypt data in transit and authenticate the server.
- **Use in Android:**
  - All network calls should use `https://`.
- **Pros:**
  - Standard, battle-tested way to secure HTTP.
- **Cons:**
  - Misconfig (weak ciphers, old TLS versions) can reduce security.

#### gRPC over HTTP/2
- **What it is:**
  - A high-performance RPC framework that typically uses HTTP/2 as transport with TLS.
  - Defines services and messages via protobuf, then generates client stubs.
- **Relation to auth/security:**
  - Still relies on **TLS** for encryption in transit.
  - Auth is usually handled via the same concepts: OAuth2/OIDC access tokens in metadata (e.g., `Authorization: Bearer <token>`), or mTLS in some enterprise setups.
- **Pros:**
  - Strong typing, efficient binary encoding, streaming support.
  - Good fit for microservice backends; mobile clients can use gRPC or gRPC-Web through a gateway.
- **Cons:**
  - More complex to debug than plain REST/JSON; tooling/logging requires some extra setup.
  - On Android, you often need an adapter layer or use gRPC libraries directly; not every backend is gRPC-ready.

#### SSL Pinning (Certificate / Public Key Pinning)
- **What it is:**
  - Extra check that the server’s certificate or public key matches a known set of pins.
- **Why:**
  - Protects against some man-in-the-middle (MITM) attacks, even if a CA is compromised.
    - CA = Certificate Authority (issues SSL certificates to websites and services)
      - e.g. DigiCert, Let's Encrypt, GlobalSign
- **Use in Android:**
  - OkHttp `CertificatePinner` or Network Security Config.
- **Pros:**
  - Stronger server identity assurance.
- **Cons:**
  - Operational overhead: you must handle cert rotation and pin updates with app releases.

#### End-to-End Encryption
- **What it is:**
  - Data is encrypted on the client and only decrypted on the final recipient; intermediaries (even your own servers) can’t read it.
- **Use in Android:**
  - Messaging apps, highly sensitive fields.
- **Pros:**
  - Maximum confidentiality.
- **Cons:**
  - Complex key management and UX; not necessary for all apps.

- how do you build your request, how do you handle tokens, 
- what needs to be on the server side?
- lifecycle -> 
- end-to-end means different things based on the interviewer question
  - local vs network security covers this

#### VPN (Network-level)
- **What it is:**
  - Virtual Private Network, usually outside of app code.
- **Relation to Android apps:**
  - Some enterprise apps must work correctly when devices are on VPN; security posture may depend on VPN presence.

---

## 4. Session Management & Authorization
**Goal:** Manage login state and what a user can access.

### Session Management on Mobile
- **Token refresh:**
  - Use short-lived access tokens and longer-lived refresh tokens.
  - On 401/expiry, call the refresh endpoint, rotate tokens, and update secure storage.
    - 401 = Unauthorized (HTTP status code), triggered by invalid/expired access token or wrong password?
- **Secure logout:**
  - Clear tokens from memory and encrypted storage.
  - Wipe sensitive caches (encrypted DB/SharedPreferences) and in-memory state.
- **Revocation:**
  - Backend should be able to revoke tokens (e.g., on password change, device lost) and force re-login.

### Authorization Patterns
- **RBAC (Role-Based Access Control):**
  - Users have roles (admin, user, support) and permissions based on those roles.
  - Roles/scopes often encoded in access tokens (`scope`, `roles` claims) and enforced server-side.
- **Scope-based access:**
  - Tokens have scopes like `read:accounts`, `write:payments`.
  - Client requests minimal scopes needed; server enforces them.

---

## Summary: What’s Standard Today
- **For login and identity:** OIDC on top of OAuth2, with short-lived access tokens and long-lived refresh tokens.
- **For token format:** JWT is very common but should be treated as opaque on the client.
- **For Android sign-in UX:** Credential Manager / Google Identity / Firebase Auth (depending on stack).
- **For device auth:** AndroidX Biometric (`BiometricPrompt`) + PIN/password fallback.
- **For secure storage:** Keystore + EncryptedSharedPreferences/DataStore; SQLCipher for sensitive DBs.
- **For transport security:** HTTPS with modern TLS, optionally SSL pinning for high-security apps.
- **For code protection:** R8/ProGuard with carefully maintained rules.

---

## Common Pitfalls (Android Auth/Security)
- Storing tokens or secrets in plain text (SharedPreferences, files, logs).
- Using long-lived access tokens and never rotating them.
- Parsing JWTs on the client and making security decisions solely on the client side.
- Rolling your own crypto instead of using well-tested libraries.
- Relying only on biometrics without a fallback (PIN/password) or backend checks.
- Not handling 401/403 responses gracefully (e.g., infinite retry loops).
- Logging PII, tokens, or secrets in Crashlytics/analytics.
- Forgetting to enable R8/ProGuard in release builds, or breaking libraries with incorrect rules.

---

## Interview Questions & Thought Process
Use these to practice explaining your understanding.

### Conceptual
- **"Explain the difference between authentication and authorization."**
- **"Why is Authorization Code + PKCE recommended for mobile apps?"**
- **"What are access tokens and refresh tokens? How do you handle them on Android?"**
- **"What is the Android Keystore, and when would you use it?"**

### Android-specific
- **"How would you implement secure login and token storage in an Android app?"**
  - Talk about OIDC/OAuth2, short-lived access tokens, refresh tokens in encrypted storage, Keystore, and BiometricPrompt for unlocking.
- **"How do you implement biometric authentication for sensitive actions?"**
  - Mention AndroidX Biometric, tying it to keys in Keystore, and fallbacks.
- **"How would you handle logout securely?"**
  - Clearing tokens, caches, and possibly notifying backend to revoke sessions.
- **"How would you detect and handle a 401 from the API?"**
  - Refresh token flow, token rotation, or re-login if refresh fails.

### Security & Networking
- **"What is SSL pinning, and when would you use it?"**
- **"How do you protect sensitive data at rest on Android?"**
- **"What are some common mobile security pitfalls you’ve seen, and how did you address them?"**

### Trade-offs & Use Cases
- **"When would you choose Firebase Auth vs a custom OAuth2/OIDC backend?"**
  - Firebase for speed/simplicity; custom for stricter compliance, control, and integration with existing IdPs.
    - IdPs = Identity Providers. IdPs (Okta, Auth0, etc.) for enterprise apps. 
- **"How do you balance UX and security for MFA and biometrics?"**
  - Step-up auth for high-risk actions, sensible timeouts, clear messaging to users.

---

## How to Think About Implementing This in an Android App
- Start with **requirements**:
  - What compliance standards apply (PCI-DSS, GDPR, internal security policies)?
  - How sensitive is the data (banking, healthcare vs simple to-do app)?
- Choose **identity provider** and flow:
  - OIDC + OAuth2 is the default; decide between Firebase/Auth0/Okta/custom.
- Design **token handling**:
  - Access vs refresh tokens, lifetimes, storage (Keystore + encrypted prefs/DataStore).
- Add **device-level protections**:
  - BiometricPrompt for unlocking secrets and confirming high-risk actions.
- Secure **transport and backend trust**:
  - Always HTTPS; consider SSL pinning for high-risk apps.
- Plan for **logging and monitoring**:
  - Structured logging without PII/secrets; crash/analytics tools configured carefully.
- Implement **session management**:
  - Refresh flows, logout, and revocation behavior.

Use this file as a map: if an interviewer goes deep in one area (e.g., OAuth2, Keystore, or biometrics), you can speak to the basics, trade-offs, and how you’d apply it in a real Android project.


---

## Additional Notes
- Enforced strict no-cache and no-screenshot policies for sensitive screens using FLAG_SECURE, along with clipboard and screen-recording restrictions to reduce data exfiltration risk in banking flows.