# Data Handling — Detailed Breakdown

Goal: Reliable, secure, and evolvable data layers.

Topics
- GraphQL
- RESTful APIs
- Caching, pagination, offline

---

## GraphQL
When to use
- Multiple clients with diverse data needs; reduce over/under-fetching

Client Patterns
- Apollo Kotlin/Android; Apollo iOS; for RN: Apollo Client/urql/Relay
- Fragments for reuse; persisted queries for performance and security
  - TODO: what are fragments in this context?
  - TODO: what are persisted queries?
  - TODO: how do persisted queries improve security and performance?
- Cache policies: normalized caches; eviction strategies; cache-first vs network-only
  - TODO: does this mean caching is default?

Schema and Server
- Schema-first design; version by deprecation, not path
  - TODO: what does it mean to version by deprecation, not path?
  - TODO: schema-first design means we define the schema we want, not rely on the API to define it?
- Directives for auth/caching; data loaders to batch N+1 queries
  - TODO: what are directives in this context?
  - TODO: what are data loaders and N+1 queries?
- Federation for microservices; schema registry and checks in CI
  - TODO: what is federation in this context?

Security
- Depth/complexity limits; query cost analysis
- Input validation; allow-list persisted queries in mobile contexts

## RESTful APIs
Design
- Resource-oriented; consistent naming and error shapes (RFC7807)
- Use ETags/Last-Modified for caching; HATEOAS optional but helpful
  - TODO: what is HATEOAS?
  - TODO: what are ETags and Last-Modified in this context?

Client Patterns
- Retrofit/OkHttp + Moshi/Kotlinx/Gson Serialization
- Request/response DTOs, mappers to domain models
- Retry with backoff and idempotency keys for POST-like operations
  - TODO: what is retry with backoff?
  - TODO: what are idempotency keys? POST is idempotent already, right?

## Caching, Pagination, Offline
- Local cache: Room/SQLDelight; key-based invalidation; single source of truth
- Pagination: page-key or cursor-based; use Paging 3 on Android
- Offline-first: queue mutations, conflict resolution strategies (last-write-wins, CRDT if needed)
- Sync: schedule with WorkManager; handle connectivity changes

## Observability and Reliability
- Correlate network requests with trace IDs; structured logs
- Add metrics: request latency, error rate, cache hit/miss, pagination load states
- Circuit breakers and fallback content; exponential backoff with jitter

## Testing
- Contract tests with MockWebServer; snapshot tests for GraphQL responses
- Golden data sets; seed databases; failure injection

---

## Android Engineer Notes
- Retrofit + OkHttp best practices: timeouts, interceptors for auth/retry, and Moshi/Kotlinx adapters; enable HTTP caching with Cache + ETags.
- Prefer Paging 3 with RemoteMediator for network + DB sync; treat Room/SQLDelight as source of truth.
- For GraphQL, consider Apollo Kotlin with normalized cache and persisted queries; validate schema changes via CI.
- Implement offline-first queues for mutations with idempotency keys; reconcile conflicts with clear strategy (e.g., server-wins for protected fields).
- Add structured logging and trace IDs to correlate app requests with backend logs; export basic metrics (latency, cache hit/miss).
