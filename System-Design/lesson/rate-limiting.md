

## A bouncer for your API

Rate limiting bounds how often a single caller can hit your service. It protects you from abuse, runaway clients, accidental DoS, and your own buggy retries. Without it, one bad client (or one viral request) can take everyone else down with it.

With it, the offending caller gets a 429 Too Many Requests and the rest of your traffic keeps moving. Most services need rate limiting before they think they do.

## Where to enforce the limit

- **At the edge / CDN**, Cheapest place. The request never reaches your origin, so you never pay for the compute. Cloudflare, Fastly, and AWS WAF all offer rate limit rules.
- **At the API gateway**, Central place to enforce per-key or per-user limits, easy to update without redeploying services. AWS API Gateway, Kong, Envoy. 
- **In the service**, Finest-grained control (per endpoint, per user, per resource), but couples rate logic into business code and runs after you have already paid for the request.

Production systems usually layer all three. Coarse limits at the edge to soak up bots, finer limits at the gateway for per-customer quotas, and the most specific rules in the service itself.

## The four algorithms

![Screenshot 2026-06-27 at 4.47.25 PM.png](../System-Design-Images/Screenshot%202026-06-27%20at%204.47.25%E2%80%AFPM.png)

- **Fixed window**, Counter resets every N seconds. Trivial to implement with one Redis INCR plus EXPIRE. Allows up to 2x the limit at the boundary (the last second of one window plus the first second of the next). 
- **Sliding window**, Weighted average of the current window and the previous one. Smooths out the boundary problem with one extra counter. The common production default. 
- **Token bucket**, Tokens drip in at a steady rate, every request consumes one. Allows short bursts up to the bucket size, then enforces the steady rate. Used by Stripe, AWS, GitHub. 
- **Leaky bucket**, A queue that drains at a constant rate. Smooths bursts at the cost of higher latency for queued requests. Good when you care more about downstream stability than per-request latency.

## What rate limiting costs

Distributed rate limits need a shared counter (Redis is the usual answer). That is an extra hop on every request, plus a hot key per limited resource.

Strict per-second accuracy is expensive at scale. Most production systems run approximately right with a sliding window plus eventual consistency between replicas.

You will block legitimate traffic eventually. Be ready to whitelist large customers, expose the limits in headers (X-RateLimit-Remaining, X-RateLimit-Reset), and respond with a Retry-After.

IP-based rate limiting is the wrong default for any service behind a corporate NAT or carrier-grade NAT. One office with 500 employees looks like one IP.

Failing open (allow when the rate-limit store is down) is a security risk; failing closed is an availability risk. Pick deliberately and document it.

## What to listen for

- **"Public API"**, Token bucket per API key, surfaced in response headers. Tiered buckets per pricing plan if it is a SaaS product.
- **"Login endpoint"**, Much stricter limit per IP plus username, plus account lockout after N failures. This is also a security control, not just a stability one.
- **"Webhook fanout"**, Leaky bucket on the consumer side so a downstream slow service does not drown your queue.
- **"Per-user fairness"**, Per-user buckets, not just per-IP. Otherwise one heavy user starves the rest.

Always say where the counter lives ("Redis with INCR + EXPIRE per key, replicated across two AZs") and what happens when that store is unreachable. The state-management question is where most candidates hand-wave.

## Questions

### What does the token bucket algorithm primarily allow for?

1) Strict fixed-rate request flow with no bursts, since tokens arrive on a precise schedule 
2) Short bursts up to the bucket size, then a steady refill rate ✓
3) Drop-the-newest-request behavior under load, so existing in-flight requests finish first 
4) Per-IP rate limiting only, since the bucket is keyed by the source address of the request

> Token bucket allows bursts up to the bucket size and enforces a steady rate over time. 
> That is why it is the default for most APIs.

### Which is the cheapest place to enforce rate limits for a public API?

1) In the database via constraints on a counter table updated by every endpoint 
2) In every microservice that handles requests, with a shared Redis counter across services 
3) At the edge or CDN layer, before the request reaches origin compute ✓
4) In the client SDK, so the application server never sees the over-limit calls at all

> Edge enforcement is cheapest because the request never reaches your origin compute. 
> Production systems then layer finer limits inside.

### Why is IP-based rate limiting a poor default for most services?

1) It is too expensive to compute, since hashing the IP per request burns measurable CPU 
2) NAT (corporate or carrier-grade) makes many users share one IP 
3) IPv6 is not supported by most rate-limit stores, so half the address space is invisible 
4) It violates GDPR, since the IP address is classified as personal data in every region

> Behind a NAT, hundreds of users can share one IP. 
> Per-user identity (API key, user_id) is more reliable than IP.



