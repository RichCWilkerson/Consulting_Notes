# Resources:
- []()


# Android Security (Overview)

This file is the **high-level map**:
- what you’re protecting,
- the most common ways Android apps get attacked,
- the baseline practices you should apply to almost every app,
- and where to go next for deeper dives.

> Scope note: This content is *Android app* focused (client + how it interacts with backends).
> Backend security matters a lot, but the goal here is to cover what an Android engineer can materially influence.

---

## Glossary (shared vocabulary)

Use these terms consistently across the `2_...`–`9_...` files.

- **Attack surface**: Any entry point where untrusted parties can influence your app.
  - Examples: exported components, deep links, WebView, network requests, local storage, logs.
- **Untrusted input**: Any data that can be controlled or influenced by an attacker.
  - Examples: `Intent` extras, deep-link `Uri`, `ClipData`, data from other apps via `ContentResolver`, network responses.
- **Exported component**: An Android component (`Activity`, `Service`, `BroadcastReceiver`, `ContentProvider`) callable by other apps.
- **Confused deputy**: Your app is tricked into doing something privileged on behalf of a less-privileged caller.
  - Common on Android: mutable `PendingIntent`, deep link routing that triggers actions.

### Token terminology
- **Access token**: Sent on API calls. Should be short-lived.
- **Refresh token**: Used to obtain new access tokens. Higher value. Protect and rotate.
- **Session ID / session token**: Server-stored session handle (often equivalent to an access token from the client’s perspective).
- **ID token (OIDC)**: Identifies the user to the client. Not typically used for API authorization.

### Data classification
- **PII**: Personally Identifiable Information.
- **PHI**: Protected Health Information.
- **Secret**: A value that enables access (tokens, credentials, private keys).

---

## How to think about Android security

Security work is easiest when you organize it by **attack surface** (the thing that can be attacked) instead of by “security techniques”.

Why this works for Android developers:
- Android security risks map directly to platform concepts (components, intents, permissions, storage, WebView, build/signing).
- It mirrors real incidents (“we leaked tokens via logs”, “an exported Activity was abused”, “cleartext traffic was intercepted”).
- It makes reviews actionable: you can scan each surface and ask “did we harden this?”.

That said, deep-dive files can still teach practical techniques (encryption, input validation, pinning) *in the context* of the surface where they apply.

---

## What you’re protecting (assets)

Think in terms of assets and impact:
- **User data (PII/PHI)**: names, emails, location, health data, identifiers.
- **Credentials & session material**: passwords (avoid handling directly), OAuth codes, access/refresh tokens, session IDs.
- **Financial / regulated data**: payment data, invoices, transaction history.
- **Business logic & entitlements**: premium access, pricing rules, feature flags.
- **App integrity**: repackaging/tampering, hooking, dynamic instrumentation.

---

## Threat model assumptions (what to assume attackers can do)

A reasonable “default” model for most consumer apps:
- The network can be hostile (public Wi‑Fi, proxies, captive portals).
- Other apps on the device may be malicious.
- The device can be shared (family tablet), lost, or temporarily accessed.
- Some users run rooted devices and/or attackers can reverse engineer APKs.

Important mindset:
- **The client is not a trust boundary.** Anything the app can do, an attacker can often script, replay, or modify.
- Treat most client-side checks as **risk signals** and UX controls, not hard security gates.

---

## A secure baseline (do this for almost every app)

This baseline is not “everything security-related”. It’s the **minimum set of practices that pays off for almost any Android app**, because they mitigate high-frequency, high-impact issues.

### Baseline checklist (skim this in PRs)
- [**Components/IPC:**](./2_Components_IPC.md) exported components are intentional and protected; input validation for all untrusted `Intent`/URI inputs.
- [**Network:**](./3_Network.md) HTTPS only; cleartext blocked; secure defaults (no debug interceptors in release).
- [**Storage:**](./4_Storage_Secrets.md) don’t store secrets unless necessary; encrypt sensitive persisted data; exclude sensitive data from backups.
- [**Auth/sessions:**](./5_Auth_Sessions.md) OAuth2 code + PKCE (or managed auth); short-lived access tokens; refresh token rotation; predictable logout.
- [**Privacy/permissions:**](./6_Permissions_Privacy.md) least privilege; request permissions just-in-time; avoid collecting unnecessary identifiers.
- [**WebView/untrusted content:**](./7_WebView_UntrustedContent.md) prefer Custom Tabs; strict allowlists; minimal JS interfaces; `FLAG_SECURE` for sensitive screens.
- [**Logging/telemetry:**](./8_Logging_Telemetry.md) no secrets/PII in logs, analytics, or crash reports; release logging is intentionally minimal.
- [**Build/release & deps:**](./9_Build_Release_SupplyChain.md) debug features off in release; dependency hygiene; basic hardening (R8/minify where appropriate).

> Relationship to the “Security PR / Code Review Checklist (General)” below:
> - The checklist section is the **what**.
> - The numbered sections right after this are the **why** (brief rationale) and **where** (what file/topic typically owns it).

---

### 1) Components & IPC (Android-specific)
Why it’s baseline: Android apps are made of components that other apps can potentially invoke. 
Misconfigured exports and unvalidated inputs are a common source of real vulnerabilities.

- Minimize and review **exported components** (`Activity`, `Service`, `Receiver`, `Provider`).
  - Assume exported components can be invoked by *any* app unless protected.
- Prefer **explicit intents** for internal app navigation.
- Validate *all* untrusted inputs:
  - `Intent` extras, deep-link URIs, `ClipData`, `Parcelable`/`Serializable` payloads, and `ContentProvider` query parameters.
- `PendingIntent` hygiene:
  - Use **`FLAG_IMMUTABLE`** by default.
  - Only use `FLAG_MUTABLE` when the platform requires it (for example, certain notification actions).
  - Quick intuition: a mutable `PendingIntent` can allow a recipient to alter what your app will execute later.

### 2) Platform & manifest hygiene
- Keep **`targetSdk` current** and fix behavior changes (many are security related).
- Review backup behavior:
  - Android can automatically back up some app data (device-to-device transfer / cloud restore).
  - Exclude sensitive data (tokens, secrets, sensitive caches) from backups, or disable backup if appropriate for your risk profile.

### 3) Data at rest
- Don’t store secrets unless you must.
  - This means: don’t hardcode secrets in the app and don’t persist secrets unnecessarily on-device.
- For sensitive values that must persist:
  - store **keys** in Android Keystore,
  - store data in an **encrypted store** (e.g., encrypted preferences / encrypted database / encrypted DataStore),
  - keep the plaintext lifetime short.

Plaintext lifetime guidance (rule of thumb):
- Aim for **in-memory only** and clear it on logout.
- If you must persist a value, persist the **minimum** needed.
- “Short” depends on the asset:
  - **Access tokens:** should expire quickly (minutes) and be refreshable.
  - **Refresh tokens / long sessions:** assume they’re high value; protect them strongly and rotate.
  - **PII caches:** keep only what you need for UX; prefer server re-fetch over long-lived local copies when feasible.

UI leakage:
- Avoid showing secrets in notifications.
- Consider `FLAG_SECURE` for particularly sensitive screens (trade-off: prevents screenshots and appearance in the Recents screen).

### 4) Data in transit
- Use HTTPS everywhere and **block cleartext** traffic (unless you have a very explicit reason).
- Use modern TLS defaults (OkHttp will generally do the right thing).
- Consider certificate pinning only when you can support:
  - a rotation plan,
  - safe fallback strategy,
  - and incident response.

### 5) Authentication & sessions
- Prefer **OAuth2 Authorization Code + PKCE** (or managed IdP solutions, e.g. OpenID Connect, Firebase Auth).

Token/credential naming (to reduce confusion):
- **Access token** (often a “Bearer token”, sometimes a JWT): sent on each request; should be short-lived.
- **Refresh token**: used to obtain new access tokens; longer-lived; high value.
- **Session token / session ID**: sometimes used instead of JWT; conceptually similar to an access token, but often server-stored.
- **ID token** (OIDC): identifies the user to the client; not typically used as an API authorization token.

Session behavior:
- Handle expiry predictably (refresh on 401/expired token).
- On refresh failure: clear local session state and return user to login.
- Consider “new device” scenarios and session revocation.

### 6) Privacy & permissions
Why it’s baseline: over-collection and over-permissioning increases breach impact and compliance risk, and it often harms UX.

- Request the **least privilege** set of permissions.
- Request permissions **just-in-time** with user-visible rationale.
- Prefer platform “pickers” and scoped APIs over broad filesystem access.
- Minimize identifiers; treat advertising IDs and device identifiers carefully.

### 7) Logging, analytics, crash reporting
- Never log secrets (tokens, auth codes, passwords, full card numbers).
- Redact PII in logs and analytics events.
- Ensure debug tooling (Stetho/Chucker-like interceptors, verbose HTTP logs) is **off in release**.

What’s generally safe(ish) to log in release:
- High-level events without sensitive payloads (e.g., “login_success”, “purchase_screen_viewed”).
- Aggregated performance metrics (latency, success rates).
- Error codes that *don’t* include user-provided input, tokens, or full URLs with query params.

### 8) Build/release hardening & dependency hygiene
- Remove debug endpoints, test credentials, dev feature flags.
- Treat dependency updates as security work (keep up with critical fixes).
- Use R8/minification where appropriate (hardening + size), but don’t rely on obfuscation as a security boundary.

---

## Common Android attack vectors (and what to do about them)

This isn’t exhaustive (Android is a big platform), but it covers the most common and most actionable vectors for typical app teams.

| Attack surface | Typical issue | What goes wrong | Primary mitigations (Android-focused) | Where to fix |
|---|---|---|---|---|
| App components | Exported component abuse / intent injection | Another app starts your `Activity/Service/Receiver` with malicious extras | Avoid exporting unless needed; require permissions; validate all inputs; prefer explicit intents | Manifest + component code |
| PendingIntent | Mutable `PendingIntent` hijack | Another app mutates intent data or reuses it unexpectedly | Use `FLAG_IMMUTABLE` by default; minimal privileges; unique request codes | Notifications / alarms code |
| Deep links | Unverified links trigger sensitive actions | Phishing / unwanted navigation / account confusion | Prefer verified App Links; strict allowlists; re-auth for sensitive actions | Manifest + routing code |
| ContentProviders / file sharing | Data leakage / path traversal | Other apps can read or manipulate your data | Don’t export providers unless required; use `FileProvider`; narrow URI grants; validate paths | Manifest + provider/file code |
| Broadcasts | Implicit broadcasts received/sent broadly | Data leakage or unintended triggers | Use explicit broadcasts; restrict receivers; require permissions (signature if appropriate) | Manifest + receivers |
| WebView | JS bridge / mixed content | Untrusted content can call privileged JS interface | Prefer Custom Tabs; lock down WebView settings; strict URL allowlist; minimize JS interfaces | UI/web layer |
| Local storage | Plaintext tokens/PII | Data exfiltration (root, backup, adb, forensic tools) | Encrypt sensitive storage; keep plaintext lifetime short; don’t persist what you can re-fetch | Data layer |
| UI leakage | Screenshots/recents/notifications leak sensitive info | Shoulder surfing + forensic leakage | `FLAG_SECURE` for sensitive screens; redact notifications; avoid writing secrets to clipboard | UI layer |
| Backup/restore | Sensitive data ends up in cloud backups | Tokens restored to a new device/account | Backup rules; exclude sensitive files; rotate sessions on restore | Manifest + backup config |
| Network | Cleartext traffic / weak TLS | MITM intercepts credentials or data | Block cleartext; HTTPS everywhere; consider pinning for high risk | Network config + client |
| API usage | Client-trusted authorization | IDOR and privilege escalation | Server enforces authZ; client treats tokens as opaque; minimize client “role” assumptions | Backend + client assumptions |
| Logging/telemetry | Token/PII in logs | Leaks via crash reporters, adb, OEM tooling | Redaction; disable verbose logging in release; review analytics payloads | Logging + build types |
| Supply chain | Vulnerable/malicious dependency | Known CVEs or compromised SDK leaks data | Dependable update process; scan deps; minimize 3rd-party SDKs | Gradle/dependency mgmt |
| Tampering | Repackaging/hooking | Attackers bypass checks, automate flows | Don’t ship secrets; obfuscate; use Play Integrity as *risk signal*, not a single gate | Build + runtime |
| Permissions/privacy | Overbroad permissions | Increased blast radius + compliance risk | Least privilege; request just-in-time; use platform pickers and scoped APIs | Manifest + UX |

---

## Common footguns (worth memorizing)

- **“We didn’t export anything”… but we did.** Exported components (or implicit intent filters) are a top source of platform-level vulnerabilities.
- **Logging is data exfiltration.** If you log tokens/PII, assume it leaves the device.
- **Client-side checks aren’t authorization.** “User is premium” cannot be trusted unless the server enforces it.
- **Pinning without a plan causes outages.** Only pin if you can rotate safely.
- **Secret-in-app is not a secret.** Anything shipped can be extracted.

---

## Security PR / Code Review Checklist (General)

- [ ] No sensitive data in logs, analytics, or crash reports.
- [ ] Exported components are intentional and protected (or not exported).
- [ ] All `Intent` / deep-link / URI inputs are validated before use.
- [ ] `PendingIntent`s are immutable unless there’s a documented reason.
- [ ] Sensitive data is encrypted at rest (or not stored at all).
- [ ] Backup/restore risk is addressed (exclude sensitive files or disable backups when needed).
- [ ] Cleartext traffic is blocked; network calls are HTTPS.
- [ ] Auth/session flow handles: token expiry, refresh failure, forced logout.
- [ ] Deep links are verified (App Links) or strictly validated.
- [ ] WebView usage is justified and locked down.
- [ ] Dependencies reviewed/updated; major SDKs justified (ads/analytics).
- [ ] Build types: debug tooling off in release; hardening considered.

---

## Recommended organization for this directory

**Recommendation:** Organize by **attack surface** (components/IPC, storage, network, auth/sessions, WebView, permissions/privacy, logging, release/supply chain).

Learning order (good for juniors, still useful for seniors):
1. **Components & IPC** (most Android-specific footguns) = 2_Components_IPC.md
2. **Network security** = 3_Network.md
3. **Secure storage & secrets** = 4_Storage_Secrets.md
4. **Auth & sessions** = 5_Auth_Sessions.md
5. **Permissions & privacy** = 6_Permissions_Privacy.md
6. **WebView & untrusted content** = 7_WebView_UntrustedContent.md
7. **Logging/telemetry** = 8_Logging_Telemetry.md
8. **Release, signing, integrity, dependencies** = 9_Build_Release_SupplyChain.md

---
