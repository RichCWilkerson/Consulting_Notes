# Android Security: WebView & Untrusted Content

Part of: [`1_Overview.md`](./1_Overview.md)

Related:
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md) (don’t do auth in WebView)
- [`2_Components_IPC.md`](./2_Components_IPC.md) (deep links, intent:// links)
- [`8_Logging_Telemetry.md`](./8_Logging_Telemetry.md) (avoid leaking URLs/tokens)

---

## Why you care (what goes wrong)
WebView turns your app into a miniature browser with extra privileges.
Common failures:
- untrusted content calls privileged Java/Kotlin via a JS interface
- mixed content loads insecure resources into secure pages
- `intent://` and deep links bounce users into unintended app flows
- file access settings allow local file reads or cross-origin access

Senior mindset:
- if you don’t need WebView, don’t ship WebView.

---

## Baseline defaults

### 1) Prefer safer alternatives
- **Custom Tabs** for web content and most authentication.
- Use WebView only when you need tight integration (offline HTML, embedded trusted content, complex rendering needs).

### 2) Treat all web content as untrusted unless you fully control it
Even “your” domain can serve user-generated content.

### 3) Strict navigation allowlists
- Allowlist schemes (`https`) and hosts.
- Consider allowlisting path patterns.
- Block unknown redirects.

### 4) Minimize JavaScript interfaces
- Avoid `addJavascriptInterface` unless absolutely necessary.
- If you must expose an interface:
  - keep it minimal
  - don’t expose direct filesystem/network primitives
  - require origin checks and strict message validation

---

## Common footguns
- **Doing login in WebView.** This increases phishing risk and breaks expected security properties.
- **Leaving WebView debugging enabled in release.**
- **Over-broad JS bridges.** One method that takes a string command can become “remote code execution inside your app.”
- **Allowing file access or universal access from file URLs** unless you 100% need it.

---

## Example: navigation allowlist in `shouldOverrideUrlLoading`

```kotlin
class AllowlistWebViewClient(
    private val allowedHosts: Set<String>,
) : android.webkit.WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: android.webkit.WebView,
        request: android.webkit.WebResourceRequest,
    ): Boolean {
        val uri = request.url

        // Block non-https and unknown domains.
        if (uri.scheme != "https") return true
        if (uri.host !in allowedHosts) return true

        return false // allow
    }
}
```

Key points:
- allowlist hosts/schemes
- treat redirects like any other navigation
- don’t blindly open `intent://` or custom schemes from WebView

---

## Practical hardening topics

### URL handling and redirects
- Validate every navigation request.
- Be careful about:
  - open redirects on your own domain
  - `intent://` URLs
  - custom scheme links that jump to other apps

### Downloads and file handling
- Don’t auto-download and open unknown file types.
- If you support downloads:
  - validate MIME type
  - store in safe locations
  - avoid executing/opening in privileged contexts

### Sensitive screens
If showing sensitive data in WebView, consider `FLAG_SECURE` (trade-offs apply).

---

## PR / review checklist (WebView)
- [ ] WebView is justified (could Custom Tabs work instead?).
- [ ] Navigation is allowlisted (scheme/host/path) and redirect-safe.
- [ ] JavaScript interfaces are minimized and validate inputs strictly.
- [ ] No auth flows are handled inside WebView.
- [ ] WebView debugging and verbose logging are disabled in release.
- [ ] Sensitive screens consider screenshot/recents leakage protections.

---

## Interview practice questions (with strong answers)

1) Why is auth in WebView discouraged?
> It increases phishing risk and breaks the security properties you get from system browser-based flows. Browser-based auth can leverage shared cookies/SSO and is more resistant to embedded credential interception.

2) What’s dangerous about `addJavascriptInterface`?
> It exposes native methods to JS running in the WebView. If untrusted content can execute JS, it can potentially call those methods and reach privileged app functionality. That’s why interfaces must be minimal, validated, and ideally avoided.

3) What does a good WebView security baseline look like?
> Prefer Custom Tabs, strict navigation allowlists, minimal JS interfaces, safe download handling, no debugging in release, and careful handling of deep links/custom schemes.

---

## Notes for seniors
- WebView security reviews pay off: it’s a concentrated attack surface with a long history of vulnerabilities.
- Treat your WebView bridge like a public API exposed to attacker-controlled inputs—because it is.
