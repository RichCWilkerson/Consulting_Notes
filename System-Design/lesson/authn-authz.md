


## Two questions, two systems

- **Authentication (authn)**, Verifies who the caller is. Passwords, magic links, OAuth, hardware keys, biometrics.
- **Authorization (authz)**, Decides what that verified caller is allowed to do. Roles, permissions, resource ownership.

These get conflated all the time. Authn answers "prove who you are". Authz answers "now that I know who you are, can you do this". Most security bugs live in authz. Authn libraries are mature; authz is usually custom and fragile.

## Session cookies vs JWTs

![Screenshot 2026-06-28 at 7.34.02 PM.png](../System-Design-Images/Screenshot%202026-06-28%20at%207.34.02%E2%80%AFPM.png)

- **Server-side sessions**, After login, the server stores a session row in Redis or a DB. The client gets an opaque session id in a cookie. Logout, revocation, and "log out everywhere" are easy because the server owns the truth.
- **JWT (stateless)**, A signed token that contains the user id, expiry, and scopes. The server verifies the signature on each request without a database lookup. Fast and easy to spread across services, but revocation is hard because the token is its own truth until it expires.

Most production systems combine the two: short-lived JWTs for service-to-service calls, plus a session row in the database for users so the security team can force-logout anyone in one click. Pure JWT-only auth is a common interview mistake.


## OAuth, OIDC, RBAC, ABAC

- **OAuth 2.0**, An authorization protocol. The user grants a third-party app access to a scoped subset of their account ("read your repos"). The third party gets a token, not your password. Standard flows: authorization code with PKCE for native apps and SPAs, client credentials for service-to-service.
- **OIDC (OpenID Connect)**, An identity layer on top of OAuth. Adds an id_token (JWT) that says who the user is. "Sign in with Google" is OIDC.
- **RBAC (role-based)**, Users get roles (admin, editor, viewer). Each role has a fixed permission set. Easy to reason about, gets unwieldy past a few dozen roles.
- **ABAC (attribute-based)**, Permissions are computed from attributes (department, region, resource owner, time of day). Flexible, harder to audit. Used in cloud IAM systems.
- **ReBAC (relationship-based)**, Permissions follow relationships in a graph ("editor on this folder", "member of this team"). Google Zanzibar and OpenFGA. Common in collaboration products.

## Where security bugs live

Token expiry is a tradeoff. Long-lived tokens are convenient and dangerous. Short-lived tokens are safe but force a refresh mechanism. Access tokens of 5-15 minutes plus a long refresh token is the common pattern.

Revocation is hard with pure JWTs. A common compromise: keep a small denylist of revoked token ids in Redis, check it on every request. Adds a hop you wanted to avoid by going stateless.

Authz logic scattered across if-statements is how breaches happen. Centralize the policy ("can user X do action Y on resource Z") in one service or library that every endpoint must use.

Multi-factor (TOTP, WebAuthn, push) catches most credential-stuffing attacks. Phishing-resistant MFA (hardware keys, passkeys) catches the rest. Password-only is no longer acceptable for anything sensitive.

Service-to-service: prefer mTLS or signed JWTs over long-lived API keys in environment variables. API keys leak from logs, screenshots, and abandoned repos.

## What to listen for

- **"Sign in with Google"**, OIDC authorization code flow with PKCE. The id_token carries identity; the access_token authorizes the third party.
- **"Log out everywhere"**, Sessions backed by a database row, not pure JWT. Mention a token denylist if JWT is required for performance.
- **"Permissions per workspace"**, ReBAC. A graph of memberships (user -> team -> workspace -> resource).
- **"Internal microservices"**, Short-lived signed tokens (JWT) plus mTLS at the network layer. Never trust the network alone.
- **"Public API for partners"**, OAuth client credentials, scoped tokens, IP allowlist, per-key rate limits, audit logging.

Always split authn and authz when describing the design. "Authn is OIDC into a JWT, authz is a central policy service that resolves user-to-resource permissions per request" is the right shape.

## Questions

### What is the main downside of using only stateless JWTs for user sessions?

1) JWTs cannot be encrypted, so any sensitive claims travel in plaintext over the wire 
2) There is no easy way to revoke a token before its expiry (no central truth) ✓
3) JWTs are too large for HTTP headers and force a fallback to body-based auth on every call 
4) JWTs require a database lookup on every request to verify the signature against the user table

> A signed JWT is its own truth until it expires. 
> To revoke immediately you need a denylist or server-side sessions, which loses the original "no DB lookup" benefit.

### "Sign in with Google" is built on which protocol?

1) SAML 
2) OAuth 2.0 with OpenID Connect (OIDC) ✓
3) OAuth 1.0a 
4) Kerberos

> OAuth 2 plus OIDC. 
> The id_token (a JWT) carries identity, and the access_token authorizes API access.

### Which model fits "an editor on this folder, a member of this team, an owner of that project"?

1) RBAC 
2) ABAC 
3) ReBAC (relationship-based) ✓
4) Session cookies

> ReBAC encodes permissions as relationships in a graph. 
> Google Zanzibar and OpenFGA are common implementations.


