

## Two ways to organize a backend

![Screenshot 2026-06-28 at 4.08.18 PM.png](../System-Design-Images/Screenshot%202026-06-28%20at%204.08.18%E2%80%AFPM.png)

- **Monolith**, One deployable unit, shared codebase, usually one database. Simple, fast to develop, easy to debug. 
- **Microservices**, Many small services, independent deploys, often independent databases. Scales teams and traffic independently.

The choice shapes hiring, CI/CD, on-call, and how fast features ship. It is an organizational decision more than a technical one. Start as a monolith and split when you have a concrete reason.

## Reasons to split (or not)

- **Different scaling needs**, Service A handles 10k QPS, service B handles 100. Splitting lets you size them independently. 
- **Different teams**, Conway's Law: your architecture mirrors your org chart. Two teams sharing one codebase fight; two teams owning two services ship. 
- **Different reliability needs**, A payment service cannot afford to share a deploy with a recommendation engine. 
- **Different data shapes**, An analytics service might need a column store; the user service needs Postgres. Forcing them into one schema is worse than splitting.

If none of those apply yet, you do not need microservices. Premature splitting is the most common architectural mistake at small companies.

## Patterns that come with splitting

- **Service-to-service auth**, Internal mTLS or signed tokens (JWT). Never trust the network alone, even inside the cluster.
- **Saga pattern**, Distributed "transactions" with compensating actions per step. Replaces ACID across services. Used heavily in payments, ordering, and travel booking. 
- **API gateway**, Single ingress point. Handles auth, rate limiting, request routing, response shaping. AWS API Gateway, Kong, Envoy. 
- **Service mesh**, Sidecar proxies (Istio, Linkerd) handle mTLS, retries, circuit breaking, traces. Free at the network layer, expensive in operational complexity. 
- **Choreography vs orchestration**, Events on a bus vs a central coordinator calling services in order. Orchestration is easier to reason about; choreography scales further.

## What microservices cost

Distributed-systems problems hit you everywhere: retries, idempotency, partial failures, traces. Every cross-service call can fail in three new ways.

Local dev is harder. You cannot run 40 services on a laptop, so you mock or run a partial stack. Onboarding takes weeks instead of days.

Debugging requires distributed tracing and centralized logs from day one, or you cannot answer 'what broke?'

Latency adds up. Five 5ms hops is a 25ms baseline before you do any real work. Coalesce calls aggressively.

Each service is a deployable, an alerting target, an on-call rotation, and a place secrets and dependencies live. Multiply by N services.

> Microservices solve organizational problems by introducing technical ones.

## What to listen for

- **"Small team, MVP"**, Monolith. Do not pre-split. Be willing to defend that against an interviewer pushing you toward microservices.
- **"Multiple teams, separate scaling"**, Microservices, owned by team boundaries. Service per team, not service per noun.
- **"Distributed transaction across services"**, Sagas with compensating actions. Never two-phase commit across regions.
- **"How do services find each other"**, Service discovery (Consul, AWS Cloud Map) or a gateway. Mention health checks.

Be explicit about when not to split. A senior signal is recognizing that microservices are mostly a tax until your team is large enough to amortize it.

## Questions

### Conway's Law roughly states...

1) Software architecture mirrors organizational communication structure ✓
2) Per-call latency stacks up as services hop, capping the realistic depth of any service graph 
3) Microservices always outperform monoliths once the team grows past a single squad of engineers 
4) Single-leader systems cannot scale past a few thousand QPS without sharding the write path

> Conway's Law: a system's structure ends up reflecting team boundaries. 
> Architectural decisions are often organizational decisions in disguise.

### A small team building an MVP should usually choose...

1) Microservices from day one, since splitting later means rewriting interfaces and data models 
2) A monolith, to ship fast and split only when there's a concrete reason ✓
3) Pure serverless, since the per-request pricing model is effectively free for low traffic 
4) Microservices with a service mesh from day one, since the sidecar handles all retries and auth

> Premature splitting is the most common architectural mistake at small companies. 
> Start as a monolith and split when team size or scaling needs justify it.

### What replaces ACID transactions across microservices?

1) Two-phase commit coordinated by a transaction manager across every participating service 
2) The saga pattern with compensating actions ✓
3) Stronger Postgres locks held across the entire request, including outbound service calls 
4) Service mesh retries with exponential backoff, since retries are equivalent to rollbacks

> There is no ACID across services. 
> Sagas chain operations with compensating actions on failure (e.g., if the charge succeeded but shipping failed, reverse the charge).

