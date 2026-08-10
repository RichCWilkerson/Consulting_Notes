# Android Security: Storage & Secrets (Data at Rest)

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md) (token storage + logout)
- [`6_Permissions_Privacy.md`](./6_Permissions_Privacy.md) (data minimization)
- [`9_Build_Release_SupplyChain.md`](./9_Build_Release_SupplyChain.md) (secrets-in-app, backups, build variants)

---

## Why you care (what goes wrong)
Common incidents in mobile:
- refresh tokens stored in plaintext SharedPreferences
- PII cached “for convenience” and restored to another device via backup/transfer
- sensitive screens appear in recents thumbnails or screenshots
- secrets embedded in the app (API keys, client secrets) are extracted from the APK

Default mindset:
- **assume the device can be lost**
- **assume some users are rooted**
- **assume APKs are reverse engineered**

---

## What counts as a “secret” vs “sensitive”

### Secrets (high value)
- refresh tokens, session IDs
- private keys (should be non-exportable)
- passwords (avoid handling)

### Sensitive data (high impact)
- PII/PHI
- payment-related artifacts
- internal identifiers that could be abused

### Not secrets (but still worth protecting)
- feature flags, UI configuration
- public API base URLs

Rule of thumb:
- if disclosure enables account takeover, payment fraud, or privacy breach → treat as secret/sensitive.

---

## Baseline defaults

### 1) Don’t store secrets unless you must
Ask:
- can I re-fetch from server?
- can I shorten lifetime?
- can I store a reference instead of the value?

### 2) Keep plaintext lifetime short
- Prefer in-memory only when feasible.
- Clear on logout.
- Avoid writing secrets to disk “just for debugging.”

### 3) Use the right tool: Keystore vs encrypted storage
**Android Keystore** stores *cryptographic keys* (often hardware-backed), not arbitrary strings.
You typically:
1) generate/store a key in Keystore
2) use that key to encrypt data you store elsewhere (encrypted prefs/datastore/db/files)

### 4) Backups/restore are part of your threat model
Android can back up and restore app data via:
- device-to-device transfer
- cloud restore (depending on settings)

If tokens or sensitive caches are restored onto a new device, you can accidentally create a “session cloning” scenario.

Mitigations:
- exclude sensitive files from backup
- rotate sessions on restore / treat restore as “new device”

---

## Storage options (pragmatic selection guide)

### Small key-value secrets
- Prefer Jetpack Security’s encrypted key-value storage for small secrets.

Watch-outs:
- handle migration and corruption (power loss / OS bugs happen)
- define clear behavior when decrypt fails (usually: force re-auth)

### Larger caches / databases
- If you store PII in a local DB, consider database encryption.

Watch-outs:
- performance and indexing costs
- key management and rotation

### Files
- Don’t roll your own crypto.
- If you must store sensitive files, ensure:
  - encrypted at rest
  - stored in appropriate internal storage locations
  - excluded from backups if necessary

---

## Example: encrypted TokenStore shape (data-layer friendly)

This is a *shape* example, not a full implementation, to show where encryption concerns belong.

```kotlin
interface TokenStore {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun setTokens(accessToken: String, refreshToken: String)
    suspend fun clear()
}

/**
 * Implementation note:
 * - Back this with Jetpack Security or an encrypted DataStore.
 * - Keys live in Keystore; encrypted store holds ciphertext.
 * - Failure mode should be safe: if decrypt fails, clear and force re-auth.
 */
class EncryptedTokenStore(/* encrypted prefs/datastore dependencies here */) : TokenStore {
    override suspend fun getAccessToken(): String? {
        // read from encrypted storage
        return null
    }

    override suspend fun getRefreshToken(): String? {
        // read from encrypted storage
        return null
    }

    override suspend fun setTokens(accessToken: String, refreshToken: String) {
        // write to encrypted storage
    }

    override suspend fun clear() {
        // delete from encrypted storage
    }
}
```

### Safe failure mode pattern
When decryption/storage reads fail:
- clear auth state
- force re-auth

```kotlin
suspend fun <T> safeReadOrClear(onClear: suspend () -> Unit, block: suspend () -> T): T? {
    return try {
        block()
    } catch (t: Throwable) {
        onClear()
        null
    }
}
```

---

## Backups: what seniors double-check
- Are we relying on default auto backup behavior?
- Do we have explicit backup rules?
- What happens if a token is restored on a different device?

For many apps:
- exclude tokens and session artifacts from backups
- prefer server-driven session management and forced re-auth on suspicious restores

---

## UI leakage (often overlooked)

### Recents thumbnails & screenshots
- `FLAG_SECURE` prevents screenshots and hides content in the recents screen.
- Trade-offs:
  - users can’t take screenshots for support
  - accessibility/UX impacts

Use it selectively for screens that display:
- full account numbers
- health results
- sensitive personal documents

### Notifications
- Don’t put secrets in notification text.
- Use redacted/lock-screen safe notifications when appropriate.

### Clipboard
- Clipboard is global. Avoid copying secrets to clipboard.

---

## Common footguns
- **Hardcoding secrets:** anything shipped can be extracted.
- **Storing refresh tokens in plaintext:** common and high impact.
- **Treating encryption as authorization:** encryption protects at rest; it doesn’t decide if user is allowed.
- **Backup surprises:** “we didn’t think backups would include that.”

---

## PR / review checklist (Storage & Secrets)
- [ ] No secrets are hardcoded or shipped as “hidden” values.
- [ ] Sensitive values aren’t stored unless necessary; plaintext lifetime is minimized.
- [ ] Secret/sensitive persisted data is encrypted using platform-backed keying (Keystore for keys).
- [ ] Failure mode is safe (decrypt/read failure forces re-auth; no silent fallbacks to insecure storage).
- [ ] Sensitive files/tokens are excluded from backups or otherwise mitigated.
- [ ] Sensitive UI is protected where appropriate (`FLAG_SECURE`, notification redaction).

---

## Interview practice questions (with strong answers)

1) What does Android Keystore store?
> Keystore stores cryptographic keys, not arbitrary strings. The typical pattern is to generate a key in Keystore and use it to encrypt/decrypt data that you persist elsewhere (encrypted preferences/datastore/db).

2) If you encrypt local data, is the app now “secure”?
> Encryption protects confidentiality at rest, but it doesn’t solve authorization, logic abuse, rooted-device compromise, or server-side issues. It reduces impact if the device is lost or data is extracted, but you still need short token lifetimes, rotation, server enforcement, and data minimization.

3) What’s the risk with backups?
> Backups can move sensitive data to cloud restore or a new device. If you back up tokens, you can accidentally clone sessions onto another device. The mitigation is to exclude sensitive artifacts from backup and/or treat restores like a new device and force re-auth.

---

## Notes for seniors (threat model realism)
- Root detection is not a reliable enforcement control; treat it as a risk signal.
- If a device is fully compromised, assume local secrets can be stolen eventually. Your real defense is:
  - short token lifetimes
  - refresh token rotation
  - anomaly detection and server-side revocation
  - minimizing what’s stored in the first place.
