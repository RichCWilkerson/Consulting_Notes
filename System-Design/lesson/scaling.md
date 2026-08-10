

## Two ways to grow

![Screenshot 2026-06-09 at 10.21.35 PM.png](../System-Design-Images/Screenshot%202026-06-09%20at%2010.21.35%E2%80%AFPM.png)

Every system outgrows its initial capacity eventually. When that happens, you have two options:

- **Vertical (scale up)**, Make one machine bigger. More CPU, more RAM, faster disk. One powerful box doing all the work.
- **Horizontal (scale out)**, Add more machines behind a load balancer. Many smaller boxes sharing the work.

The choice isn't just about capacity. It shapes your failure story, your deploy process, and your cloud bill.

### Vertical scaling

On the cloud, vertical scaling is as simple as paying more for a beefier instance type. 
You swap a t3.medium for an m5.8xlarge and call it a day. 
Works well for monoliths, internal tools, personal projects, and anything that doesn't need to serve huge traffic.

- Simple. No orchestration, no load balancer, no fleet to manage. Your code doesn't change.
- Hardware hits a ceiling. Eventually you run out of motherboard slots, CPU sockets, or available instance sizes.
- Diminishing returns to price. Top-tier hardware is priced exponentially, so two mid-tier boxes usually outperform one top-tier box for the same money.
- Single point of failure. One crash and your application is offline. There's no second instance to take the traffic.


### Horizontal scaling

Instead of scaling one beefy server, you run more copies of the same server behind a load balancer. 
For anything that needs to grow past a single box, this should be your default.

- Near-infinite scale. You're limited by your budget and the amount of compute physically available in the world.
- One machine dies, traffic shifts to the others. Your users don't notice.
- Cloud providers make this easy. Autoscaling groups, ECS, Kubernetes. You only pay for what you use, and the fleet grows with demand.
- Requires stateless servers (or sticky sessions, which reintroduce most of the stateful pain). Session data has to live in a database, cache, or token.


#### What horizontal costs

Scaling out solves capacity and reliability, but you pay for it in operational complexity.

- You now manage a fleet. Health checks, autoscaling policies, rolling deploys, canaries. Each of those has to work.
- Cross-server coordination (distributed locks, shared caches, session stickiness) gets harder as the fleet grows.
- The orchestration layer (Kubernetes, ECS, autoscaling groups) costs money and time. You'll also need monitoring that understands your fleet, not just one box.
- Debugging is harder. Logs are scattered across N instances, so you need centralized logging, tracing, and metrics to reconstruct a single user's request.


### What to listen for

- **"Millions of users"** or **"viral"**, Horizontal, with autoscaling.
- **"Internal tool"** or **"low traffic"**, Vertical is fine. Don't over-engineer.
- **"What if the server crashes?"**, Horizontal, stateless, with health checks and a multi-AZ load balancer.

Back your choice with a rough TPS estimate. "At 1k TPS a single box is fine. At 100k TPS we'll need roughly 100 stateless instances behind an ALB." Showing the math is half the signal.


### Questions

#### Vertical scaling means...

1. Adding more identical machines to a fleet that sits behind a single load balancer 
2. Making one machine bigger (more CPU, RAM, faster disk) ✓
3. Switching the primary database from a SQL engine to a NoSQL document store 
4. Adding a CDN in front of static assets so the origin server handles less traffic

> Vertical scaling means making one machine bigger. You pay more for a beefier instance type, and that one box handles all the traffic.

#### Your service must handle 100k TPS. Which approach is the right starting point?

1. Vertical scaling with the largest available instance, since one big box is easier to manage 
2. Horizontal scaling with stateless servers behind a load balancer ✓
3. Sharding the application code across multiple smaller repos and CI pipelines per team 
4. Replacing HTTP with gRPC over HTTP/2, since the binary protocol absorbs traffic spikes

> Past a certain scale, vertical hits a hardware ceiling and creates a single point of failure. 
> Horizontal with stateless servers is the senior default at high TPS.

#### What is the main operational cost of horizontal scaling?

1. You must rewrite the application in Go to take advantage of cheap goroutine concurrency 
2. You manage a fleet, with health checks, autoscaling, deploys, and centralized logs/traces ✓
3. You can no longer use a relational database because writes cannot be sharded safely 
4. You can no longer cache anything client-side because each request hits a different server

> Horizontal scaling solves capacity but you now manage a fleet. 
> Health checks, deploys, and observability all become required.