# Android Security: Logging, Analytics, Crash Reporting

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`3_Network.md`](./3_Network.md) (HTTP logging)
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md) (tokens)
- [`6_Permissions_Privacy.md`](./6_Permissions_Privacy.md) (PII/privacy)

---

## Why you care (what goes wrong)
Logging is one of the most common real-world data exfiltration paths:
- logs are visible via adb, OEM tooling, and sometimes other apps
- crash reporters often upload breadcrumbs automatically
- analytics can become a shadow data pipeline if event payloads contain PII

If it can be logged, assume it can leave the device.

---

## Baseline defaults

### 1) Never log secrets
Examples:
- access/refresh tokens
- auth codes
- passwords
- full card numbers

### 2) Treat PII as restricted
- Don’t log raw email/phone/address/location traces.
- Prefer coarse/aggregated metrics.

### 3) Release logging should be intentionally minimal
- No verbose HTTP logging.
- No debug-only libraries.
- No “temporary logs” left behind.

---

## Common footguns
- **Logging full URLs.** Query params often contain tokens or PII.
- **Logging headers.** `Authorization` and cookies are frequently leaked.
- **Crash reporters capturing network breadcrumbs.** Some integrations capture request/response payloads.
- **“It’s only in debug.”** Then someone accidentally ships debug settings in release.

---

## Practical patterns

### Redaction as a central utility
- Centralize redaction so teams don’t re-implement it.
- Redact:
  - auth headers
  - cookies
  - query params (or drop them)
  - known PII fields in JSON

### Structured logging (safer than string soup)
Prefer structured events with:
- a fixed schema
- strongly-typed fields
- clear classification of what’s allowed in release

### Safe event design rules
- Events should be meaningful without including raw user input.
- Prefer IDs that are not directly identifying (and avoid stable device IDs unless approved).
- Use sampling for performance events; avoid collecting raw payloads.

---

## Example: shared redaction helpers

```kotlin
object Redaction {
    private val secretHeaderNames = setOf("Authorization", "Cookie", "Set-Cookie")

    fun sanitizeUrl(url: String): String {
        // Drop query params to avoid leaking tokens/PII.
        return url.substringBefore('?')
    }

    fun sanitizeHeaders(headers: Map<String, String>): Map<String, String> {
        return headers.mapValues { (k, v) ->
            if (k in secretHeaderNames) "<redacted>" else v
        }
    }

    fun redactIfLooksLikeToken(value: String): String {
        // Heuristic only; do not rely on this as the only control.
        if (value.length > 24 && value.count { it == '.' } >= 2) return "<redacted>" // jwt-ish
        return value
    }
}
```

### Example: safe analytics event schema

```kotlin
data class AnalyticsEvent(
    val name: String,
    val props: Map<String, String>,
)

fun safeLoginFailedEvent(reasonCode: String): AnalyticsEvent {
    // Avoid raw error messages and raw user input.
    return AnalyticsEvent(
        name = "login_failed",
        props = mapOf(
            "reason_code" to reasonCode,
            "has_network" to "true",
        )
    )
}
```

---

## PR / review checklist (Logging & Telemetry)
- [ ] No secrets in logs/analytics/crash reports.
- [ ] URLs are sanitized; query params aren’t logged.
- [ ] HTTP request/response logging is disabled or heavily redacted in release.
- [ ] Crash reporting is configured to avoid sensitive attachments.
- [ ] Analytics events do not contain raw PII or user-generated free text.
- [ ] Debug logging/tooling cannot be enabled accidentally in release builds.

---

## Interview practice questions (with strong answers)

1) Why are logs a security risk?
> Logs are an exfiltration channel: they can be read locally (adb/OEM tools) and are often uploaded remotely (crash reports, analytics, backend traces). If secrets or PII make it into logs, assume they can leak.

2) What’s your rule for logging URLs?
> Don’t log full URLs with query params. Query params commonly contain tokens, emails, IDs, or other PII. If I must log, I log only the path/host and strip queries.

3) What does it’s only debug miss?
> The risk is a build/config mistake. Debug logging libraries and flags have a history of being accidentally shipped. Seniors treat release logging as a build-time constraint, not a convention.

---

## Notes for seniors
- Review logs as part of security review, not just debugging convenience.
- Treat telemetry changes like API changes: schema review, privacy review, approval flow, and regression tests.
