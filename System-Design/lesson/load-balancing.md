

## One door, many rooms behind it

![Screenshot 2026-06-28 at 5.42.26 PM.png](../System-Design-Images/Screenshot%202026-06-28%20at%205.42.26%E2%80%AFPM.png)

A load balancer sits in front of a pool of servers and decides which one each request should go to. Clients only know one address. The balancer hides the rest of the fleet.

Without one, you are stuck with a single server (cap on traffic, one crash takes you down). With one, you can add capacity by adding boxes, deploy without downtime, and route around unhealthy instances automatically.

Every modern web app has at least one load balancer in front of the application tier. Most have several layered together (DNS-level, regional, per-service).

## L4 vs L7

- **Layer 4 (TCP/UDP)**, Routes by IP and port. The balancer never opens the request, so it works for any protocol and adds almost no latency. Great for raw TCP services, gRPC streams, and game servers. AWS NLB and HAProxy in TCP mode are typical.
  - L4 is what you pick when raw throughput matters more than features, or when the traffic is not HTTP at all.
  - L4 health checks are a TCP ping.

- **Layer 7 (HTTP/HTTPS)**, Routes by URL path, host header, cookies, or body inspection. The balancer terminates TLS, reads the request, and can rewrite headers, apply WAF rules, and run per-route logic. AWS ALB, NGINX, Envoy, Cloudflare. 
  - L7 is what most app teams pick because it understands HTTP. 
  - L7 health checks hit a real path like /health and look at the response code.

## Routing algorithms

- **Round-robin**, Each new request goes to the next server in the list. Simple, even, and ignores load. Fine when every request takes about the same time. 
- **Least connections**, Send the request to whichever server has the fewest open connections right now. Better when request durations vary a lot (long uploads, slow queries). 
- **Least response time**, Pick the server with the lowest recent average latency. Great in mixed-instance fleets where some boxes are faster than others.
- **Consistent hash**, Hash a key from the request (user id, session, cache key) and route to a specific server. Useful when the destination has a warm in-memory cache for that key. 
- **Sticky sessions**, After the first request, future requests from the same client go to the same server. Needed for stateful protocols like WebSockets, painful for everything else.

## What load balancers cost

The balancer itself is a network hop and a dependency to monitor. A crashed balancer takes down the whole fleet behind it, so plan for redundancy (multi-AZ, multi-region).

L7 balancers terminate TLS, so private keys live there. Rotation and certificate automation become operational tasks.

Sticky sessions defeat horizontal scaling. If one user generates 90% of the traffic, one server gets crushed and the rest sit idle.

Health checks need to be honest. A check that only pings the port misses a server with a hung database connection. Test the real path.

Bad routing decisions are subtle. Round-robin to a hot cache misses; least-connections to a warming server overwhelms it.

## What to listen for

- **"Route by path or host"**, L7 (ALB, NGINX, Envoy). Mention TLS termination and WAF integration.
- **"Game server" or "raw TCP" or "millions of connections"**, L4 (NLB, HAProxy TCP). Lower latency, protocol agnostic.
- **"WebSockets" or "persistent sessions"**, Sticky sessions on the LB, or a pub/sub backbone so any server can deliver to any client.
- **"Multi-region"**, Global load balancer (Cloudflare, Route 53, AWS GLB) plus regional balancers behind it. Mention DNS-level failover.

Name the layer and the algorithm in the same sentence. "L7 ALB with least-connections, health checks against /health, multi-AZ failover" is a senior answer in one breath.

## Questions

### What is the main difference between an L4 and an L7 load balancer?

1) L4 supports HTTPS natively while L7 has to be paired with a separate TLS terminator 
2) L4 routes by IP and port without opening the request; L7 routes by HTTP attributes like path or host ✓
3) L4 is always faster than L7 because it uses UDP instead of TCP for every connection 
4) L7 is only used for static assets, while every dynamic API call must traverse an L4 balancer

> L4 balances TCP/UDP without reading the payload. 
> L7 terminates the connection, reads HTTP, and can route by path, host, cookies, and so on.

### When does sticky-session routing become a problem?

1) When TLS is required end-to-end, since session affinity breaks the TLS handshake on rotation 
2) When one heavy user concentrates load on a single server and the rest sit idle ✓
3) When the load balancer runs in only one availability zone instead of being multi-region 
4) When the application uses cookies for anything other than session identity tokens

> Stickiness pins traffic per client to one server. 
> A heavy client or a popular session creates a hot box while peers are underused.

### Why is a TCP-port health check not always sufficient?

1) It does not work with IPv6 because most load balancers still require IPv4 health checks 
2) It cannot tell that the app crashed if the port is still open (e.g., a hung worker holding the socket) ✓
3) It violates HTTP/2 multiplexing rules and forces fallback to plain HTTP/1.1 connections 
4) It only works for stateful servers, since stateless servers do not bind a long-lived port

> A bare TCP check confirms the socket is open. 
> A real /health probe verifies that the app process and its dependencies still work.

