# Android Security: Permissions & Privacy

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`4_Storage_Secrets.md`](./4_Storage_Secrets.md) (data minimization / retention)
- [`8_Logging_Telemetry.md`](./8_Logging_Telemetry.md) (PII in logs/analytics)
- [`2_Components_IPC.md`](./2_Components_IPC.md) (permission-protecting entry points)

---

## Why you care (what goes wrong)
Permissions and privacy problems often become:
- excessive data collection (higher breach impact)
- app store policy violations
- compliance risk (GDPR/CCPA/HIPAA-like)
- user trust issues and churn

Security mindset:
- the best way to protect data is to **not collect it**.

---

## Baseline defaults

### 1) Least privilege
- request the minimum permissions needed
- avoid “just in case” permissions
- remove permissions when features are removed

### 2) Just-in-time permission requests
- request at the moment a feature needs it
- display rationale in product language (not developer language)
- handle denial gracefully (fallbacks, limited mode)

### 3) Prefer scoped APIs and system pickers
Modern Android encourages “scoped access”:
- photo picker instead of broad media permissions (when available)
- document picker instead of filesystem access
- location: prefer approximate when precise isn’t necessary

### 4) Minimize identifiers
- avoid device identifiers unless you have a clear business and privacy justification
- be careful with advertising identifiers and analytics IDs

---

## Privacy-by-design (what senior engineers should drive)

### Data minimization
Ask for each piece of data:
- do we need it for product value?
- can we make it optional?
- can we compute it server-side without storing on device?

### Retention and deletion
- define retention windows for cached data
- ensure “logout” and “delete account” actually clear local state
- for regulated contexts, be explicit about export/delete flows

### Consent and transparency
- keep consent aligned with actual behavior
- avoid hidden collection through third-party SDKs

---

## Example: just-in-time permission request with safe fallback

This is intentionally high-level and focuses on the security UX shape.

```kotlin
// Pseudocode-ish, but Android-real.
fun onUserTapsNearbyStores() {
    // Prefer approximate location unless you truly need precise.
    if (hasLocationPermission()) {
        showNearbyStores()
        return
    }

    // 1) Explain in product language (rationale screen/dialog).
    showPermissionRationale(
        title = "Find stores near you",
        message = "We use your location to show nearby store inventory. You can continue without it.",
        onContinue = { requestLocationPermission() },
        onNotNow = { showStoreSearchManualMode() }
    )
}
```

Key points:
- don’t ask on app start
- give a fallback path
- don’t request more privileges than needed

---

## Common footguns
- **Requesting permissions on app start.** Users deny, and you’ve burned trust.
- **Asking for broad storage/media access when a picker would do.** This increases your blast radius.
- **Treating analytics like “not user data.”** Analytics often becomes PII through identifiers and event payloads.
- **Third-party SDK permission creep.** SDK updates can introduce new permissions/collection.

---

## PR / review checklist (Permissions & Privacy)
- [ ] Every permission has a clear product justification.
- [ ] Permissions are requested just-in-time with a user-facing rationale.
- [ ] Scoped APIs/pickers are used when possible instead of broad permissions.
- [ ] Collected identifiers are minimized and documented.
- [ ] Data retention/deletion behavior is defined (logout/delete account clears local sensitive state).
- [ ] Third-party SDK data collection is reviewed (what data leaves device?).

---

## Interview practice questions (with strong answers)

1) Why is least privilege important on Android?
> Permissions expand your blast radius. If your app is compromised or your logs/telemetry leak, collecting less data and having fewer permissions reduces impact. It also improves user trust and reduces policy/compliance risk.

2) How do you decide between approximate vs precise location?
> Default to approximate if it satisfies the product requirement. Only request precise if you can justify why the user benefit requires it. Request it just-in-time and provide a fallback.

3) What’s a common permission UX mistake?
> Requesting permissions on cold start. Users don’t have context yet, deny the request, and now you’ve taught them to say no.

---

## Notes for seniors
- Treat privacy as a feature requirement, not a legal afterthought: it affects architecture (what data exists, where it flows, and how long it lives).
- Most “security work” is trade-offs. Being explicit about the trade-off (UX vs risk) is what makes it senior-level.
