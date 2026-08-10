
## Glossary
CDN: Content Delivery Network, a distributed network of servers that cache content close to users for faster delivery.
Curl One-liner: A simple command-line request using the curl tool, often used for testing APIs.
- like `curl -X GET https://api.example.com/v1/users/123`
HMAC (Hash-based Message Authentication Code): A cryptographic technique that combines a secret key with a hash function to ensure data integrity and authenticity.

## The contract you ship outlives your code

An API is the contract between client and server. It's how the outside world asks your service to do work, and once you ship v1 it's far harder to change than the implementation behind it. Your API decisions outlive most of your code.

Three protocols dominate today: REST, GraphQL, and gRPC. Each one trades developer experience, network efficiency, and tooling for the others. Picking the right one matters, but a good API is mostly about predictability, idempotency, and clear errors, not which protocol you wrote on the whiteboard.

## Things every API has to handle

- **Resource modeling**, Name your entities and their identifiers up front. /users/{id}/orders/{id} is boring and unambiguous, which is exactly what you want. 
- **Versioning**, URL prefix (/v1/), header (Accept-Version), or schema-version field. Pick one and stick with it. v1 will live forever, plan for it. 
- **Idempotency keys**, Any non-GET that a client might retry needs an Idempotency-Key header so retries don't double-charge or double-create. 
- **Pagination**, Cursor-based for anything that grows or changes (feeds, search). Offset-based only for static, bounded lists. 
- **Errors**, Structured: an error code, a human message, and a request id. The code is for clients to switch on; the message is for the human reading the log. 
- **Auth**, Header (Authorization: Bearer ...), never a query param. Query strings end up in CDN logs, browser history, and screenshots.

## REST vs GraphQL vs gRPC

- **REST**, HTTP verbs over resources. Cacheable at the CDN, debuggable with curl, every framework speaks it. Boring is good. The right default for almost any external API. 
- **GraphQL**, One endpoint, the client picks the fields it wants. Perfect when one backend feeds heterogeneous frontends (web, iOS, Android, partners). Cost: complex caching, N+1 risk on the resolver layer, custom tooling for everything. 
- **gRPC**, Protobuf over HTTP/2 with bidirectional streaming. Fastest on the wire, strict schema, generated clients in every language. Great for internal service-to-service. Browser support needs a gRPC-Web shim.

BFF (backend-for-frontend) is a useful middle ground: a thin GraphQL or REST service per client, calling internal gRPC services behind it. The client gets exactly what it needs; internal services stay strict.
- TODO: what does a BFF look like in practice? is this like mid-tier services?

## What you give up

- **REST** is easy to onboard, but you fetch more than you need (over-fetching) or make many round-trips for related resources (under-fetching). 
- **GraphQL** is flexible, but the server now has to defend itself from expensive queries (depth limits, query complexity scoring, persisted queries in production). 
- **gRPC** wins on performance, but you lose the HTTP ecosystem: no easy CDN caching, no browser DevTools network tab, no curl one-liners.

Versioning is painful no matter what. Most teams promise a clean v2 migration and never actually deprecate v1.

Idempotency is the difference between a retry and a duplicate charge. If you skip it, you will eventually corrupt customer data.

## What to listen for

- **"Mobile and web with different needs"**, GraphQL or BFF. Avoid making mobile do 5 round-trips to render one screen.
- **"Internal microservices"**, gRPC for hot paths, REST for management and admin endpoints.
- **"Public API for third parties"**, REST plus an OpenAPI spec. Stable URLs, documented error codes, generous rate limits.
- **"Webhooks" or "outbound POST"**, Signed bodies (HMAC), idempotency keys, retries with exponential backoff, a delivery log the customer can replay from.

Always say what happens on retry. "POST /payments accepts an Idempotency-Key, so a duplicate request returns the original response without creating a second charge." That sentence is worth more than naming any protocol.

## Questions

### What problem does an idempotency key solve?

1. It encrypts the request body in transit between client and origin server 
2. It guarantees the response body is cached at every CDN node for a fixed window 
3. It ensures a retried request does not create a duplicate side effect ✓
4. It validates that the user is authenticated and authorized for the requested action

> Idempotency keys let clients safely retry. 
> If the same key arrives twice, the server returns the original result instead of charging twice.

### A mobile app and a web app need very different fields from the same backend. The cleanest fit is...

1. gRPC, since protobuf is fast on the wire and generated clients work on every platform 
2. GraphQL or a backend-for-frontend (BFF) layer per client ✓
3. REST with one rigid resource shape and a separate flag per platform on each endpoint 
4. WebSockets, since the persistent connection lets each client subscribe to the fields it needs

> GraphQL and BFF let each client pick the fields it needs without forcing the backend to expose every shape. 
> Both reduce over-fetching and under-fetching.

### What is the main downside of GraphQL?

1. It cannot be used over HTTP, so every client needs a custom transport layer 
2. It has no native way to authenticate users, so every server reimplements auth from scratch 
3. The server must defend itself from expensive queries (depth limits, complexity scoring, persisted queries) ✓
4. It only supports JSON-RPC, which makes it hard to model resources cleanly on the server

> GraphQL's flexibility is also its risk. 
> A single client query can fan out into many DB calls. 
> Production servers protect themselves with depth limits, complexity scoring, and persisted queries.
