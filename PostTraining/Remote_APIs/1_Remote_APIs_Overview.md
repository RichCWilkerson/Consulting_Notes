# Resources
- [Retrofit](/PostTraining/Remote_APIs/Retrofit.md)
- [Interceptors](/PostTraining/Remote_APIs/Interceptors.md)
- [GraphQL](/PostTraining/Remote_APIs/GraphQL.md)
- Android official: [Connect to the network](https://developer.android.com/training/basics/network-ops)

# Remote API Overview
This is a **high‑level overview** of common ways an Android app talks to **remote services**:
- REST APIs, gRPC, WebSockets, GraphQL
- Data formats: JSON, Protobuf, XML
- Mapping between network DTOs and domain models
- Libraries like Retrofit, OkHttp interceptors, Ktor, Apollo, and more.

---

## What is an API?

**API = Application Programming Interface**
- A **contract** that defines **how clients talk to a service**: endpoints, methods, parameters, headers, request/response shapes, error codes.
- For Android networking, usually this means **HTTP/HTTPS APIs** exposed by a backend.

From a mobile point of view:
- You **don’t control the server**; you integrate with it.
- You care about:
  - **Transport** (HTTP/2, HTTP/3, gRPC, WebSocket).
  - **Data format** (JSON, Protobuf, GraphQL query/response).
  - **Semantics** (idempotency, retries, pagination, auth).

Interview framing:
> “On Android, an API is the contract between the app and backend services: 
> which endpoints exist, what data they expect, and what they return. 
> My job is to consume those APIs reliably and securely, handling errors, offline, and performance.”

---

## IPC vs Remote API


**IPC (Inter‑Process Communication)** notes available [here](/PostTraining/IPC_And_Services/IPC.md)
- Scope: **components on the same device** (your app ↔ system services ↔ other apps).
- Examples: Intents, BroadcastReceivers, Binder/AIDL, Messenger.
- Use when:
  - Talking to **system services** (Telephony, Bluetooth, Location).
  - Exposing a service from your app for other apps on the device.

**Remote API**
- Scope: **your device ↔ remote server** over network (internet, intranet, cloud).
- Usually uses **HTTP(S)** (REST, GraphQL, gRPC over HTTP/2) or **persistent connections** (WebSockets).
- Use when:
  - Calling your backend, payment gateway, analytics, 3rd‑party APIs.

Mental model:
- IPC: “talk to another **process** on this device”.
- Remote API: “talk to another **machine/service** over the network”.

---

## Major Remote API Styles

### 1. REST (Representational State Transfer)

- The dominant pattern for HTTP APIs.
- Resources exposed via URLs (`/users`, `/accounts/{id}`, `/transactions`).
- Uses HTTP verbs: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`.
- Data format: usually **JSON**.

Android usage:
- Typically consumed via **Retrofit + OkHttp**.
- Works well with DTOs, `suspend` functions, and error mapping.

**Pros**
- Ubiquitous; almost all backends support REST.
- Easy to debug with tools like Postman, curl, or browser.
- Great ecosystem and community.

**Cons**
- Over‑fetching / under‑fetching: mobile often gets **more data than needed** or multiple calls.
- Versioning can get messy (`/v1`, `/v2`, header‑based versions).

---

### 2. gRPC

- High‑performance RPC framework built on **HTTP/2 + Protocol Buffers**.
- You define contracts in `.proto` files; tooling generates client/server stubs.
- On Android often used for **internal or high‑throughput** services.

Android usage:
- Good fit for **microservices** and real‑time/bidirectional streaming from backend.
- Smaller payloads (Protobuf vs JSON) and better performance on flaky networks.

**Pros**
- Strongly typed contracts; codegen for multiple languages.
- Built‑in streaming (client, server, bidirectional).
- Very efficient in bandwidth and CPU.

**Cons**
- Harder to debug manually (binary Protobuf payloads).
- Requires more infra and tooling alignment with backend.
- Browser support is limited (more backend/internal than public APIs).

---

### 3. WebSockets

- Full‑duplex, long‑lived connection over a single TCP channel.
- Good for **real‑time updates**: chat, notifications, collaborative apps, trading, sports scores.

Android usage:
- Often implemented with **OkHttp’s WebSocket API** or libraries on top.
- You listen to `onMessage`, `onFailure`, `onClosing` and push events into your app state.

**Pros**
- Low‑latency push from server → client.
- Efficient for frequent, small messages.

**Cons**
- More complex **connection lifecycle** (reconnect, backoff, resubscribe).
- Harder to run through some proxies/firewalls.
- You must design your own **message protocol** (JSON, Protobuf, custom framing).

---

### 4. GraphQL

> See: [GraphQL details](/PostTraining/Remote_APIs/GraphQL.md)

- Query language for APIs that lets the client specify **exactly** which fields it needs.
- Single endpoint (often `/graphql`) with multiple operations.

Android usage:
- Commonly used with **Apollo Android** client.
- Strong schemas and codegen into Kotlin models.

**Pros**
- Eliminates over‑fetching/under‑fetching; great for mobile constraints.
- Strong typing and introspection.

**Cons**
- More complex server setup and caching story.
- Can lead to very heavy queries if not controlled (N+1 problems on backend).

---

### 5. Other Patterns

- **SSE (Server‑Sent Events)** – server → client one‑way streaming over HTTP.
- **Polling / Long Polling** – simpler “fake real‑time” when WebSockets/gRPC streaming are not available.
- **SOAP** – legacy XML‑based protocol; mostly seen when integrating with older enterprise systems.

---

## Common Data Formats
- **JSON**
  - Most common for REST and GraphQL.
  - Human‑readable, widely supported, but can be verbose.
- **XML**
  - Legacy enterprise and SOAP services.
  - More verbose; schema‑driven (XSD); still present in older banking/telecom backends.
- **Protocol Buffers (Protobuf)**
  - Compact binary format used by **gRPC** and some high‑performance APIs.
  - Requires a schema (`.proto` files); not human‑readable but very efficient.
- **Form‑encoded / Multipart**
  - `application/x-www-form-urlencoded` and `multipart/form-data` for forms and **file uploads** (images, PDFs, etc.).
  - Very common in Retrofit when dealing with image upload or mixed text+file payloads.

How to talk about this:
> “On Android I mostly work with JSON over REST, occasionally multipart for uploads, and Protobuf for gRPC. 
> I care about how each format impacts payload size, parsing cost, and debuggability on mobile.”

---

## Mapping (DTOs → Domain Models)
It's common to map request/response data to **domain models** in your app.
- [Mapping Notes](/PostTraining/Remote_APIs/Mapping.md)

General idea:
- **Network DTOs** represent the exact shape of API requests/responses.
  - Match the backend contract 1:1.
  - Include things like raw enums, error codes, nullable fields, pagination wrappers.
- **Domain models** represent the app’s internal data structures.
  - Cleaned‑up types, defaults applied, business names instead of API names.
  - Shaped for **UI and business logic**, not for transport.
- You **map between them in a data layer** (repositories, data sources):
  - This decouples your app from API changes (renamed fields, added fields, version bumps).
  - Lets you centralize defensive handling (nulls, missing fields, defaults).

Interview framing:
> “I keep network DTOs and domain models separate. 
> DTOs match the wire format; domain models match the app’s needs. 
> Repositories are responsible for mapping between them so backend changes don’t leak all the way into the UI.”

---

## Tools and Libraries (Android‑side)

You’ll cover specifics in other files; this is the bird’s‑eye view.

### Core HTTP stack
- **OkHttp**
  - De‑facto standard HTTP client on Android.
  - Features: connection pooling, TLS, HTTP/2, interceptors, WebSocket support.

### High‑level API clients
- **Retrofit** ([details](/PostTraining/Remote_APIs/Retrofit.md))
  - Type‑safe HTTP client built on OkHttp.
  - Maps endpoints to Kotlin interfaces, integrates with coroutines/Flows, converters (Moshi/Gson), RxJava.

- **Ktor Client**
  - Kotlin‑native HTTP client, good for **multiplatform** (Android, iOS, JVM, JS).
  - Coroutine‑first, flexible pipelines.

- **Apollo Android (GraphQL)**
  - Generates Kotlin models and operations from `.graphql` files.
  - Handles cache, normalization, and GraphQL‑specific behaviors.

### Cross‑cutting support
- **Interceptors** ([details](/PostTraining/Remote_APIs/Interceptors.md))
  - Mostly **OkHttp interceptors** for:
    - Auth headers / tokens
    - Logging (with redaction)
    - Retry & backoff policies
    - Idempotency keys, custom error mapping

- **Serialization**
  - Moshi, Gson, Kotlinx Serialization, Protobuf.
  - Choice impacts performance, type‑safety, and nullability handling.

---

## Pros and Cons by Approach (Mobile‑centric)

At a high level:

- **REST**
  - ✅ Simple, universally supported, easy to debug.
  - ❌ Over‑fetching / under‑fetching; versioning pain.

- **GraphQL**
  - ✅ Flexible queries, great for complex UIs; reduces round‑trips.
  - ❌ Requires strong schema discipline; can over‑query; more complex caching.

- **gRPC**
  - ✅ Very efficient, strongly typed, good for streaming.
  - ❌ Heavier tooling; usually internal; harder to debug manually.

- **WebSockets**
  - ✅ Real‑time, push‑based.
  - ❌ Requires custom protocol design, robust reconnect logic, and careful resource handling.

Interview framing:
> “Most mobile apps I’ve built use REST + Retrofit, but I’m comfortable reasoning about when GraphQL, gRPC, or WebSockets are a better fit, especially around real‑time data, performance, and over‑fetching.”

---

## When to Use Which Approach

Heuristics for architecture conversations:

- **REST**
  - Default choice for **CRUD‑style** mobile APIs.
  - Great when you want simplicity, debuggability, and wide tooling support.

- **GraphQL**
  - UIs that need to **aggregate data from many resources** (e.g., complex home/dashboard screens).
  - Multiple platforms (web/iOS/Android) with different data needs.

- **gRPC**
  - Internal communication between backend services — you might consume it from Android if you’re within that ecosystem.
  - High‑performance, low‑latency, streaming use cases (trading, telemetry).

- **WebSockets / SSE / Long Polling**
  - Real‑time updates where **latency matters** more than strict request/response semantics.
  - Chat, live scores, notifications, collaborative editing.

Also consider:
- **Backend maturity**: what infra does the org already support?
- **Debuggability**: what will on‑call / debugging look like?
- **Offline & caching**: which patterns fit your caching story best?

---

## Common Pitfalls (Android‑side)

- **Ignoring mobile constraints**
  - Over‑fetching huge payloads; not paginating.
  - Not handling slow/flaky networks, captive portals, or offline.

- **Tight coupling to API shape**
  - Using network DTOs directly in UI/domain; makes refactors painful.
  - Avoid by mapping to **domain models** in a data layer.

- **Poor error handling**
  - Treating all errors the same; not distinguishing network vs server vs client errors.
  - Not surfacing actionable messages to the user (retry, contact support, etc.).

- **Security gaps**
  - Missing TLS pinning where appropriate.
  - Logging sensitive data in plain text.
  - Inconsistent auth/token refresh handling.

- **No timeouts / backoff / retry strategy**
  - Hanging calls; hammering the backend on failures.
  - Not using exponential backoff + jitter for idempotent requests.

- **Overusing custom protocols**
  - Reinventing REST or gRPC on top of raw sockets or WebSockets without strong justification.

---

## Best Practices for Remote APIs in Android

- **Separation of concerns**
  - Keep networking in a **data layer** (repositories, data sources).
  - Map network DTOs → domain models.

- **Use mature clients**
  - Prefer Retrofit/OkHttp, Apollo, or Ktor over hand‑rolled HTTP.

- **Apply robust networking policies**
  - Timeouts, retries (with idempotency awareness), circuit breakers where appropriate.
  - Cache control where it makes sense (OkHttp cache, Room cache).

- **Design for offline & poor networks**
  - Local caching of critical data.
  - Clear UI states: loading, empty, error, partial data.

- **Security first**
  - HTTPS everywhere; consider **TLS pinning** for high‑risk apps.
  - Don’t log secrets; redact tokens/PII.
  - Centralize auth/token logic with interceptors.

- **Observability**
  - Structured logging of requests (without PII).
  - Metrics for latency, error rates, and retry counts.

---

## Interview Questions & Talking Points

Use this section to practice:

1. **How does your Android app typically talk to backends?**
> Emphasize REST + Retrofit, data layer, error handling, and interceptors.

2. **When would you use WebSockets or gRPC from Android?**
> Real‑time vs high‑throughput, and how you’d manage connection lifecycle.

3. **How do you handle flaky networks and timeouts?**
> Timeouts, retries with backoff, caching, and UI strategies.

4. **How do you keep your app decoupled from backend changes?**
> DTO → domain mapping, feature flags, backwards‑compatible server changes.

5. **Security for remote APIs in a banking/fintech context?**
> TLS, pinning, auth tokens, logging discipline, and interceptors.

---
