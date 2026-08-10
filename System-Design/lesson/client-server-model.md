
## The basic shape of every web app
![Screenshot 2026-06-04 at 5.45.08 PM.png](../System-Design-Images/Screenshot%202026-06-04%20at%205.45.08%E2%80%AFPM.png)

Clients make requests. Servers handle them. Put a load balancer in front and that traffic spreads across many identical servers, so one box going down doesn't take the whole app down with it.

Pretty much every design question you'll ever see, from a chat app like WhatsApp to a live streaming platform like Twitch, starts with some version of this pattern. The more detail you layer on top, the more specialized the design gets, but the skeleton stays the same.

The client doesn't care how many servers exist. It just talks to a single address and trusts the load balancer to route things.

### The pieces you need to know
- Client, What your user runs. A browser, iOS app, or CLI tool. It knows where to send requests and how to render the response.
- Load balancer, Spreads incoming traffic across a pool of servers (AWS ALB, GCP LB). Does health checks so dead boxes stop receiving traffic.
- Server, Stateless compute that runs your business logic. Adding or removing a server should be invisible to clients.
- Backend services, Databases, caches, queues, and 3rd-party APIs. These live behind the servers and hold the data and side effects.

Understanding this layout drives decisions about where logic lives (client vs server), what to cache, how to scale, and what happens when any one piece fails.

### Stateful vs Stateless Servers
- Stateless, No memory between requests. Each request carries everything the server needs (auth token, IDs, params). Any server can handle any request, so you can scale horizontally, redeploy safely, and recover from crashes without losing sessions.
- Stateful, Keeps session or in-memory data between requests. Gives you very low latency and lets clients send less per request, but the client is now locked to one specific server. A crash loses whatever was in memory.

In interviews and production cloud systems, stateless is the default unless you have a very specific reason to go stateful (multiplayer games, persistent WebSocket connections, real-time collaboration).

Note the nuance: a stateless server can still use a database to track users and their data. The key is that the server itself holds nothing between requests. The database holds the truth.

### Tradeoffs
This pattern gives you scale and resilience, but it's not free.
- Stateless servers need to reach an external store for almost every request. That's an extra network hop and another thing that can fail or slow down.
- The load balancer is another hop, another dependency to monitor, and another piece you pay for.
- You now have to keep servers interchangeable. No local files, no in-memory state, no assumptions about who you talked to last time.
- Sticky sessions exist as a workaround, but they reintroduce most of the pain of stateful systems and make scaling lopsided.

For any app past a handful of users, the tradeoff is obvious. You eat the hops in exchange for being able to grow without rewriting everything.

### What this looks like in a question
This is the starting skeleton for almost every design question. Draw it first, then layer on specifics. Don't spend more than a minute on the base.

- Trigger, "Design the backend for..." means draw client, LB, server, DB.
- Trigger, "What if a server crashes?" means stateless plus horizontal scaling plus health checks.
- Trigger, "Session data" or "multiplayer" means stateful may be justified for that specific service.

State your assumption ("I'll assume stateless servers behind an ALB") and move on. Don't burn time re-justifying the basics. The interviewer cares about what you do on top of the skeleton, not the skeleton itself.
- ALB (Application Load Balancer) is a specific type of load balancer that operates at the application layer (Layer 7). It can route traffic based on content, such as URL paths or headers, which makes it ideal for web applications.

### Question
1. What is the role of a load balancer in a typical client/server architecture?

- It spreads incoming traffic across a pool of servers and removes dead ones from rotation
  - A load balancer routes requests across servers and uses health checks to skip dead ones. It does not store data or render responses.

2. Why do most modern web apps prefer stateless servers?

- Any server can handle any request, which makes scaling and recovery clean
  - With stateless servers, the load balancer can route any request to any server. That makes horizontal scaling, redeploys, and crash recovery clean.

3. What is the main downside of a stateless server design?

- Each request needs an extra hop to fetch session or context state from an external store
  - Stateless servers must reach an external store (DB, cache, JWT) for any per-user context. That extra hop is the cost of horizontal scalability.

