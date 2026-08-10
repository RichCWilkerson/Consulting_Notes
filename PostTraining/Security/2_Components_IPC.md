# Android Security: Components & IPC

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md) (deep links + step-up auth)
- [`6_Permissions_Privacy.md`](./6_Permissions_Privacy.md) (permission-protecting surfaces)
- [`7_WebView_UntrustedContent.md`](./7_WebView_UntrustedContent.md) (intent://, URL handling)

---

## Why you care (what goes wrong)
Android is unusual compared to many platforms because your app provides **addressable entry points** (components) that other apps and the system can invoke.

Real-world failure modes:
- An `Activity` meant for internal navigation is **exported** (explicitly or via an intent filter) and another app launches it with malicious extras.
- A deep link route triggers a privileged action (“upgrade plan”, “reset password”, “transfer funds”) without re-auth.
- A `ContentProvider` or file-sharing flow leaks sensitive files because the URI grants are too broad.
- A mutable `PendingIntent` is altered by another app, causing your app to execute with different data later.

---

## Baseline defaults (good for most apps)

### 1) Minimize exported components
- Prefer `android:exported="false"` unless you have a concrete reason.
- If you add an `<intent-filter>`, the component is often implicitly “public”. Treat it as an API.
- For components that must be exported:
  - gate them with a permission (prefer `signature` protection level when the caller is *your* app suite), and/or
  - require an authenticated user/session, and
  - validate all incoming inputs.

**Mental model:** any exported component is callable by *any* app on the device unless you’ve enforced a protection mechanism.

### 2) Prefer explicit intents for internal app navigation
- Internal navigation should generally be `Intent(context, TargetActivity::class.java)`.
- If you’re using deep links for internal navigation, treat every deep link as **untrusted input** anyway (links can be forged).

### 3) Validate all untrusted inputs (Intents, Bundles, URIs)
Treat these as attacker-controlled:
- `Intent` extras (`String`, `Parcelable`, `Serializable`)
- Deep link `Uri` + query params
- `ClipData`
- `ContentResolver` input (URIs from other apps)
- `Bundle` arguments passed to fragments (especially if created from incoming intents)

Validation should cover:
- presence/absence
- type correctness
- range limits (length, numeric ranges)
- allowlists (enums, supported schemes/hosts/paths)
- canonicalization (paths, URLs)

### 4) `PendingIntent` hygiene
- Use `FLAG_IMMUTABLE` by default.
- Use `FLAG_MUTABLE` only when required (some notification behaviors / inline replies).
- Minimize privileges: the `PendingIntent` should target the narrowest component and carry the minimum data needed.

**Threat intuition:** a mutable `PendingIntent` can enable *confused deputy* behavior where someone changes what your app will do later.

### 5) Safe file sharing
- Prefer `FileProvider` over `file://` URIs.
- Use `FLAG_GRANT_READ_URI_PERMISSION`/`FLAG_GRANT_WRITE_URI_PERMISSION` only as needed and only for the shortest-lived share.
- Avoid granting directory-wide access when you only need a single file.

---

## Common footguns (worth memorizing)
- **Intent filters make components public APIs.** Adding `<intent-filter>` without thinking about exported access is a top Android security footgun.
- **“But the caller is our app.”** Unless it’s explicit intent *and* same-process/same-app, assume other apps can invoke it.
- **Deep links are not authentication.** A verified App Link proves domain ownership, not that the user is authorized for an action.
- **Mutable PendingIntent by accident.** Old code samples and copy/paste still use mutable flags.
- **URI permission grants are sticky.** If you grant too broadly, you can leak more than intended.

---

## Attack surfaces & how to harden them

### Activities
Typical threats:
- intent injection (extras that crash or alter logic)
- launching “internal screens” directly (skipping preconditions)

Mitigations:
- make non-entry activities non-exported
- for exported entry activities:
  - validate intent extras
  - enforce authentication before showing sensitive UI
  - for “dangerous actions”, require step-up auth (re-enter password, biometrics, recent login)

### Services
Typical threats:
- other apps binding/starting services to perform work or read data

Mitigations:
- avoid exported services unless truly needed
- if exported, require permission and validate all incoming data
- be careful with “intent services” that accept file URIs or command parameters

### BroadcastReceivers
Typical threats:
- receiving broadcasts from arbitrary apps to trigger behaviors

Mitigations:
- prefer manifest receivers only for system broadcasts you truly need
- if a receiver is app-internal, require a permission or use explicit broadcasts
- validate payloads (this includes actions and extras)

### ContentProviders
Typical threats:
- data leakage (readable by other apps)
- uncontrolled writes (other apps can modify your state)
- SQL injection-like problems via selection args (depends on implementation)

Mitigations:
- don’t export providers unless required
- use URI permission grants for narrowly-scoped sharing
- validate paths and IDs; avoid path traversal patterns
- apply least privilege on read/write operations

---

## Deep links & App Links (practical guidance)

### When to use what
- **Verified App Links (https + assetlinks.json):** best default for deep linking into the app.
- **Custom schemes:** okay for non-sensitive flows but easier to spoof and collide.

### Rules of thumb for safe routing
- Parse and validate the URI early.
- Use an allowlist for:
  - scheme (`https`)
  - host(s)
  - path patterns
- Avoid executing “state-changing actions” directly from the link.
  - Prefer: deep link → route to a screen → user confirms → app checks auth → request goes to server.
- Re-auth (step-up) for sensitive operations even if link is verified.

### Common “confused deputy” examples
- `myapp://transfer?amount=1000&to=attacker`
- `https://example.com/reset_password?token=...` opened in-app without verifying user state

---

## Example: deep link allowlist + intent validation (Android-shaped)

Goal: validate untrusted input early and fail safe.

```kotlin
private val allowedHosts = setOf("example.com", "www.example.com")

fun isAllowedDeepLink(uri: android.net.Uri): Boolean {
    // only allow "https" scheme and known hosts
    if (uri.scheme != "https") return false
    if (uri.host !in allowedHosts) return false

    // Keep path checks simple. Prefer allowlisting known routes.
    val path = uri.path.orEmpty()
    return path.startsWith("/account/") || path.startsWith("/settings/")
}

fun requireStringExtra(intent: android.content.Intent, key: String, maxLen: Int = 256): String? {
    val value = intent.getStringExtra(key) ?: return null
    if (value.isBlank()) return null
    if (value.length > maxLen) return null
    return value
}
```

How this is used in an exported entry activity (conceptually):
- parse `intent.data` → check `isAllowedDeepLink`
- validate every `extra` you use
- if route triggers a sensitive flow: enforce session + step-up auth (`5_Auth_Sessions.md`)

---

## Pattern: input validation checklist (Intent/URI)
When you touch incoming data, ask:
- Is this value required? What’s the fallback if missing?
- Is the type and encoding what I expect?
- What are the max lengths? (avoid huge strings causing memory issues)
- Is it an allowlist or a free-form string?
- If it’s an ID: is it strictly numeric/UUID and validated?
- If it influences file/network access: is it constrained to safe locations/hosts?

---

## PR / review checklist (Components & IPC)
- [ ] Every `Activity/Service/Receiver/Provider` has an explicit exported decision (and reason if exported).
- [ ] Exported components validate all extras/URIs/ClipData.
- [ ] Deep links have strict allowlists; sensitive actions require step-up auth.
- [ ] No implicit intents for internal navigation.
- [ ] `PendingIntent` uses `FLAG_IMMUTABLE` unless there’s a documented need for mutable.
- [ ] File sharing uses `FileProvider`; URI grants are narrowly scoped and time-limited.

---

## Interview practice questions (with strong answers)

1) What does “exported” mean and why is it risky?
> An exported component is callable by other apps. That makes it part of your public attack surface. If the component assumes “only our app calls this,” attackers can invoke it with malicious Intents/URIs, potentially bypassing preconditions, crashing the app, or triggering privileged actions.

2) How do you secure deep links?
> I treat deep links as untrusted input. I prefer verified App Links, then I enforce strict allowlists for scheme/host/path, validate each query param, and avoid performing state-changing actions directly from a link. For sensitive routes I require an authenticated session and step-up auth.

3) What’s a confused deputy in Android?
> It’s when your app is tricked into doing something privileged on behalf of a less-privileged caller. Classic Android examples are mutable `PendingIntent`s or exported components that perform privileged work based on attacker-controlled extras.

---

## Notes for seniors (what to watch in real apps)
- **Library-added components:** some SDKs add providers/receivers via manifest merge. Always review the merged manifest in release builds.
- **Testing surfaces:** write at least a couple of tests that start exported components with malformed intents/URIs and assert safe failure.
- **Threat modeling by component:** treat each exported entry point like a mini public API: contract, auth, validation, logging.
