# Android Security: Network (TLS, Cleartext, API Safety)

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md) (token transport + refresh)
- [`8_Logging_Telemetry.md`](./8_Logging_Telemetry.md) (HTTP logging & redaction)
- [`9_Build_Release_SupplyChain.md`](./9_Build_Release_SupplyChain.md) (debug tooling in release)

---

## Why you care (what goes wrong)
Network issues are where many high-impact incidents happen:
- credentials/tokens leak over cleartext HTTP
- “accept-all-certs” debugging code ships to prod
- verbose interceptors capture auth headers and get uploaded via crash reports
- attackers proxy traffic (MITM) and replay tokens or manipulate responses

Baseline mindset:
- assume hostile networks (public Wi‑Fi, captive portals, proxies)
- assume someone will try to inspect/modify traffic

---

## Baseline defaults (do this for almost every app)

### 1) HTTPS everywhere + block cleartext
- Use `https://` for all endpoints.
- Block cleartext traffic via **Network Security Config** (preferred) rather than relying on “we don’t call http”.
- Avoid “temporary exceptions” that become permanent.

### 2) Don’t customize TLS unless you have a strong reason
- OkHttp + the platform TLS stack are usually the safest choice.
- Custom `TrustManager` / hostname verifiers are a common source of severe vulnerabilities.

### 3) Remove debug network tooling from release
This includes:
- HTTP logging interceptors that log headers/bodies
- traffic inspectors (Chucker-like tools)
- proxy settings and custom CAs

Enforce at build time (release variant) so it’s difficult to accidentally ship.

### 4) Treat tokens and PII as toxic waste
- Don’t put secrets in query params.
- Assume full URLs (including query params) can end up in logs, analytics, crash reports, backend traces.

---

## Common footguns
- **“Trust all certificates” left behind.** Any code that disables certificate validation is catastrophic when shipped.
- **Pinning without a rotation plan.** Pinning can protect against some attacks, but operationally it can brick your app during cert rotation.
- **Logging request bodies.** Many APIs carry PII in JSON bodies; logging them is exfiltration.
- **Using ID tokens as API auth tokens.** ID token is for client identity, not necessarily API authorization.

---

## Practical Android guidance

### Network Security Config: what it’s for
Use it to:
- block cleartext traffic
- define per-domain trust settings (carefully)
- (optionally) pin certs via config (less flexible than code)

Rule of thumb:
- keep it simple: “cleartext off” and minimal overrides.

### OkHttp + Retrofit: safe defaults vs dangerous customization
Safe-ish:
- modern OkHttp
- default hostname verification
- default certificate validation

High risk:
- custom trust managers
- accepting user-installed CAs for “debug convenience” in release
- disabling hostname verification

### Certificate pinning (when it’s worth it)
Pinning is most appropriate when:
- you’re in a high-risk threat model (finance, high-value accounts)
- you control the backend and have mature operational processes
- you can ship updates quickly and have monitoring + rollback

Operational requirements seniors should insist on:
- **pin rotation strategy** (multiple pins: current + next)
- defined **failure behavior** (how to fail safely)
- incident response plan (forced update, temporary bypass plan—rarely acceptable)

Avoid pinning “because security said so” without these safeguards—it can become an availability incident.

### Replay and idempotency (API safety)
Even with TLS, attackers (or flaky networks) can cause duplicate submissions.

For state-changing requests, consider:
- **idempotency keys** (unique per request) to prevent double-charge / double-submit
- retries with exponential backoff (and careful retry rules)

This is a security concern when replay can cause unauthorized or duplicated actions.

---

## Example: block cleartext traffic (Network Security Config)

`AndroidManifest.xml` (fragment)

```xml
<application
    android:name=".MyApp"
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false">

    <!-- activities/services/etc -->

</application>
```

`res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

> This is the most common “secure default”: deny cleartext globally, then add explicit exceptions only if you truly need them.

---

## Example: redacting HTTP logs (safe-ish dev logging)

```kotlin
class RedactingHttpLoggingInterceptor(
    private val enabled: Boolean,
) : okhttp3.Interceptor {

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()

        if (enabled) {
            // Never log request bodies by default; avoid query params.
            val url = request.url.newBuilder().query(null).build()

            val safeHeaders = request.headers.newBuilder()
                .removeAll("Authorization")
                .removeAll("Cookie")
                .build()

            // Log url + safeHeaders via your logging framework.
            // Example:
            // logger.info("HTTP ${request.method} $url headers=$safeHeaders")
        }

        return chain.proceed(request)
    }
}
```

---

## Token transport & headers
- Use `Authorization: Bearer <access_token>` headers; avoid query param tokens.
- Redact `Authorization`, cookies, and any other secrets from logs.
- Be careful with redirect handling: ensure you don’t leak headers to unexpected domains.

---

## PR / review checklist (Network)
- [ ] Cleartext traffic is blocked (network security config; no http endpoints).
- [ ] No custom trust manager / hostname verifier in production.
- [ ] Debug interceptors/inspectors are excluded from release builds.
- [ ] HTTP logging (if any) is redacted and disabled/minimal in release.
- [ ] Tokens are sent via headers, not query params.
- [ ] Sensitive endpoints have replay/duplicate protections where appropriate (idempotency keys, server-side checks).
- [ ] If pinning exists: documented rotation plan + multiple pins.

---

## Interview practice questions (with strong answers)

1) What is cleartext traffic, and why is it dangerous?
> Cleartext traffic is HTTP without TLS. Anyone on the network path can read or modify it (public Wi‑Fi, proxies, captive portals). For apps, it often means credentials, tokens, or PII can be intercepted or tampered with.

2) Why is “trust all certificates” code so risky?
> It disables certificate validation, which is the core guarantee of TLS server identity. In production it effectively turns HTTPS into “encrypted but unauthenticated,” enabling straightforward MITM attacks.

3) When would you use certificate pinning?
> Only for high-risk apps and only with an operational plan: multiple pins, rotation strategy, monitoring, and a safe failure posture. Pinning is a trade-off: it reduces some MITM risk but increases outage risk if mishandled.

---

## Notes for seniors (what to look for in incidents)
- Capture the “how did this ship?” story: release build type, manifest overrides, debug-only dependencies, and build config flags.
- Inspect what your crash reporter collects by default (some attach network breadcrumbs).
- Confirm that “network env switching” (dev/stage/prod) can’t be abused to point prod builds at attacker servers.
