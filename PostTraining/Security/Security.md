# Resources: 
- [Essential Security Practices - Medium](https://medium.com/deuk/android-app-defense-essential-security-practices-for-developers-8ed905a026ea)
- [Android Credential Manager - Developers](https://developer.android.com/identity/credential-manager)
  - Simplifies the sign-in and sign-up processes by automating credential storage and retrieval
  - offering a more secure alternative to manual credential handling or local storage methods, which are susceptible to security breaches.

# Mobile Security — Detailed Breakdown

Goal: Protect user data and app integrity; comply with regulations.

Areas
- Data encryption
- OAuth2 and JWT
- [Biometric authentication](https://developer.android.com/identity/sign-in/biometric-auth)
- Compliance (PCI DSS, GDPR)

API Keys
- Hardcoding API keys in source code or including them in version control systems exposes them to the risk of unauthorized access. Instead, Android developers should use approaches like BuildConfig and local.properties files to inject API keys into their applications dynamically.
- Using separate keys for different environments (development, testing, production) also minimizes the risk of exposure and limits the potential damage if a key is compromised.

---

## Data Encryption

TODO: add UUID / idempotency keys to this file where they would fit in. context of android is to general. need to pass the idempotency key in the header of the request, and generate a new one for each request. this is to prevent duplicate requests from being processed by the server. the UUID can be used as a unique identifier for the user or device, and can be stored securely in the Keystore/Keychain.

At Rest
- Android: EncryptedSharedPreferences, SQLCipher/Room with SafeRoom, File encryption APIs
  - TODO: what are each of these? when to use which? where do you implement them (data layer)?
- iOS: Keychain, NSFileProtection, Core Data encryption solutions
- Key management: store secrets in Keystore/Keychain; use hardware-backed keys
  - TODO: what are hardware-backed keys? how to use them?
  - TODO: how do you store secrets in Keystore? examples?

In Transit
- Enforce TLS 1.2+; certificate pinning (OkHttp/Alamofire)
  - TODO: what is certificate pinning? how to implement it?
  - TODO: what is TLS 1.2+? why is it important?
  - TODO: do okhttp and alamofire do the same thing here?
- Block cleartext traffic via Network Security Config / ATS
  - TODO: what is cleartext traffic? why block it?
  - TODO: what are Network Security Config and ATS?

Secrets
- Never bundle API secrets; use backend token exchange or DPoP/MTLS when needed
  - TODO: why never bundle API secrets? 
  - TODO: what is backend token exchange? DPoP? MTLS? when to use each?
- Use remote config/feature flags for toggles, not secrets
  - TODO: remote in this context means the remote repo? like github secrets?
  - TODO: what are feature flags?
  - TODO: why use these for toggles but not secrets?

## OAuth2 and JWT
- Use Authorization Code with PKCE for mobile
  - TODO: what is PKCE? how to implement it?
- Avoid implicit flow; never store client_secret in app
  - TODO: what is implicit flow? why avoid it?
- Store refresh tokens securely (Keystore/Keychain) and rotate regularly
- JWT best practices: short expiry, RS256/ES256, validate aud/iss/exp/nbf, clock skew
  - TODO: what is short expiry here? how short?
  - TODO: what are RS256/ES256?
  - TODO: what are aud/iss/exp/nbf?
  - TODO: what is clock skew?
- Token binding to device where possible; consider Attestation
  - TODO: what is token binding? how to implement it?
  - TODO: what is attestation in this context?

## Biometric Authentication
- Android BiometricPrompt; CryptoObject for strongbox-backed keys
  - TODO: what is CryptoObject? how to use it?
  - TODO: what are strongbox-backed keys?
- iOS LocalAuthentication; evaluatePolicy with fallback handling
- UX: clear fallback path (PIN/Passcode); accessibility considerations

## Compliance
PCI DSS
TODO: what is PCI DSS? is it an acronym for something?
- Don’t store PAN; use tokenization; rely on PCI-compliant providers
  - TODO: what is PAN in this context?
  - TODO: what are PCI-compliant providers? why rely on them?
- Limit scope: use web-based payment sheets when possible (e.g., Google/Apple Pay)
  - TODO: instead of building our own payment sheets, use google/apple pay?
  - TODO: how do you implement that?

GDPR
TODO: what is GDPR? is it an acronym for something?
- Data minimization, consent, right to be forgotten; Data Processing Agreements
  - TODO: what is data minimization?
  - TODO: what are Data Processing Agreements?
- Privacy by design: purpose limitation, retention policies
  - TODO: are these privacy policies we define ourselves?

## Threat Modeling and Hardening
- STRIDE/LINDDUN workshop per feature
  - TODO: what are STRIDE and LINDDUN?
  - TODO: how to conduct a workshop for these?
  - TODO: why use these specifically?
- Root/jailbreak detection as signal (don’t block blindly)
  - TODO: what is root/jailbreak detection? why is it important?
  - TODO: what does "don't block blindly" mean here?
- Obfuscation (R8/ProGuard) and integrity checks (Play Integrity/DeviceCheck)
  - TODO: what are R8 and ProGuard? how to use them?
  - TODO: what are Play Integrity and DeviceCheck?
  - TODO: what are integrity checks in this context?
  - TODO: why is obfuscation important?
- Secure logging: redact PII; network logs only in dev
  - TODO: what is PII?
  - TODO: how to implement secure logging that redacts PII?
- Runtime permissions: just-in-time prompts with rationale
  - TODO: what are runtime permissions? examples?
  - TODO: what are just-in-time prompts with rationale?

## Testing and Verification
- Security linters (MobSF), dependency scanning (OWASP Dependency-Check)
- Pen-tests; fuzz critical inputs; SCA for native libs
- Incident response plan: secrets rotation, forced logout, kill switch

---

## Android Engineer Notes
- Use Android Keystore with hardware-backed keys when available; wrap SharedPreferences with EncryptedSharedPreferences for small secrets.
- Configure Network Security Config to block cleartext and add cert pinning for critical hosts via OkHttp CertificatePinner.
  - TODO: what is cleartext here?
  - TODO: what is cert pinning?
- Implement OAuth2 PKCE with AppAuth or an audited flow; store refresh tokens in EncryptedSharedPreferences or Keystore-backed storage.
  - TODO: what is an audited flow?
- Integrate BiometricPrompt with CryptoObject for key unlocking; provide clear fallback to device credentials.
- Add a security checklist to PRs: no logging of PII, HTTPS only, tokens not persisted in logs, obfuscation rules updated.
