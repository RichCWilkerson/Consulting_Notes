# Android Security: Build, Release, Supply Chain

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`3_Network.md`](./3_Network.md) (debug tooling removal)
- [`4_Storage_Secrets.md`](./4_Storage_Secrets.md) (secrets-in-app)
- [`8_Logging_Telemetry.md`](./8_Logging_Telemetry.md) (release logging)

---

## Why you care (what goes wrong)
Many security incidents are “shipping mistakes”:
- debug endpoints, test accounts, or verbose logging included in release
- a third-party SDK update starts collecting more data (or introduces a vulnerability)
- an attacker repackages/tampers with the app and bypasses client-side checks

Senior mindset:
- security is partly an engineering discipline, partly a release discipline.

---

## Baseline defaults

### 1) Release builds are locked down
- Debug-only tooling is excluded from release.
- Logging is minimal and redacted.
- Feature flags have safe defaults in release.

### 2) Secrets don’t belong in the app
- Anything shipped can be extracted.
- Don’t embed:
  - API secrets
  - signing keys
  - long-lived credentials

Use server-side token exchange and short-lived tokens.

### 3) Dependency hygiene is security work
- Keep dependencies updated.
- Minimize third-party SDKs.
- Review what each SDK can access (permissions, network, identifiers).

---

## Build variants & configuration pitfalls
- Ensure debug-only dependencies aren’t present in release.
- Be careful with manifest merges: libraries can add providers/receivers/permissions.
- Treat “build config toggles” as security-sensitive (it’s easy to accidentally flip them).

---

## Example: keep debug tooling out of release (Gradle-shaped)

```kotlin
// build.gradle.kts (conceptual)
android {
    buildTypes {
        debug {
            // debug-only logging/inspection allowed here
        }
        release {
            // keep release locked down
            isMinifyEnabled = true
            // proguardFiles(...)
        }
    }
}

dependencies {
    // Debug-only network inspection tool.
    debugImplementation("com.example:network-inspector:1.0.0")

    // Release should not include it.
    releaseImplementation("com.example:network-inspector-noop:1.0.0")
}
```

> The key security point is not the specific library names: its enforcing this at build time so we forgot to remove it cant happen.

---

## App signing (conceptual)
- Signing is the root of trust for updates.
- Protect signing keys and CI/CD secrets.
- Be deliberate about who can produce release artifacts.

---

## Supply chain risk (third-party SDKs)
Questions seniors ask before approving SDKs:
- What data does it collect and transmit?
- Does it add exported components or permissions?
- How often is it updated and maintained?
- What happens if you need to remove it quickly?

Practical governance:
- maintain an inventory of major SDKs
- treat SDK updates like invasive changes (review + testing)

---

## Tampering & integrity signals (realistic framing)
- Attackers can repackage apps, hook methods, and modify runtime behavior.
- Obfuscation (R8) helps, but it’s not a security boundary.
- Play Integrity can provide a **risk signal** (device/app integrity), but:
  - don’t rely on it as the only gate
  - design your backend to handle bypasses

Use integrity signals for:
- adaptive friction (step-up auth)
- fraud/risk scoring
- additional verification for high-risk actions

---

## PR / review checklist (Build/Release/Supply Chain)
- [ ] Debug tooling and verbose logging are excluded from release.
- [ ] No test credentials, debug endpoints, or dev toggles ship in prod.
- [ ] Manifest merge reviewed for exported components and permissions.
- [ ] Dependencies updated with security in mind; major SDK changes reviewed.
- [ ] Secrets are not embedded in the app.
- [ ] Integrity/tamper checks (if present) are treated as risk signals, not single points of failure.

---

## Interview practice questions (with strong answers)

1) Why is secret in app not a real secret?
> Anything shipped in the APK/AAB can be extracted (reverse engineering, dynamic instrumentation). So client secrets, API secrets, or long-lived credentials in the app should be treated as compromised by default.

2) Whats the supply chain risk for mobile apps?
> Dependencies and SDKs can have vulnerabilities or malicious behavior, and SDK updates can change data collection or add exported components/permissions via manifest merge. A senior process includes inventory, review, and fast rollback.

3) Whats a mature view of Play Integrity / tamper checks?
> Useful as a risk signal, not a hard gate. Attackers can bypass client checks, so the backend should treat integrity as one input to risk scoring and step-up auth rather than the only validation.

---

## Notes for seniors
- Mature security practice is mostly about repeatable processes: safe defaults, automation, reviews, and rapid incident response.
- The fastest way to improve security posture is often: remove debug surfaces, reduce data collection, and keep dependencies current.
