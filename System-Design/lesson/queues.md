

## The same queue from DS&A class
![Screenshot 2026-06-07 at 1.32.00 PM.png](../System-Design-Images/Screenshot%202026-06-07%20at%201.32.00%E2%80%AFPM.png)

You learned about queues in an intro data structures course. They work exactly the same way here: requests come in, you place them in a queue, and the server processes them in order as it's able.

Imagine a web server that can handle 2 requests at a time. Without a queue, a third concurrent request gets dropped with a 429 error. Add a queue in front, and that third request waits its turn instead of failing.

### Why you want one

- **Traffic spikes**, Your best (and almost only) tool for handling bursts without over-provisioning. A queue absorbs load your servers can't yet serve.
- **Decoupling**, Producers and consumers scale independently. A slow consumer doesn't block a fast producer, and the other way around.
- **Async jobs**, Anything compute-heavy or slow (video processing, email sends, report generation) fits here.
- **Durability**, A durable queue (SQS, Kafka) persists messages to disk, so a consumer crash doesn't lose in-flight work.

The tradeoff: clients no longer get a synchronous result. You return a job ID, and they poll (or subscribe to a webhook) for the outcome. For most background work that's fine, and often preferred.

### Flavors you should know

![Screenshot 2026-06-07 at 1.35.04 PM.png](../System-Design-Images/Screenshot%202026-06-07%20at%201.35.04%E2%80%AFPM.png)

Not all queues are equal. Pick the one that matches your delivery guarantees and throughput.

- **SQS**, AWS's managed queue. Simple, durable, at-least-once delivery. The pragmatic default on AWS when you just need a queue.
- **Kafka**, High-throughput distributed log, not just a queue. Messages persist and can be replayed. Great for streaming pipelines, analytics, and event sourcing.
- **RabbitMQ**, Flexible routing (topics, fanout, direct). Good fit for complex pub/sub patterns and per-message priorities.
- **Priority queues**, Multiple lanes so fast, high-priority requests don't sit behind one huge job. Most managed queues support this natively or through multiple queue pairs.

### Pitfalls to watch for

Queues are powerful, but they introduce a handful of failure modes you need to design for.

- The ever-expanding queue. If producer rate exceeds consumer rate for long, the queue grows forever and customer latency tends toward infinity.
- Latency. Clients don't get a synchronous response, so UIs must be designed around async state (loading spinners, status polls, notifications).
- Ordering isn't always strict across multiple consumers. If strict order matters, use a single consumer, partitioning, or Kafka with keyed partitions.
- At-least-once delivery. Messages can be delivered more than once, so consumers must be idempotent or you'll double-charge, double-send, or corrupt data.

Common mitigations: autoscale consumers based on queue length, add priority lanes, push failed messages to a dead-letter queue, and rate-limit producers so the queue stays finite.

### What to listen for

- **"Viral post"** or **"traffic spike"**, Queue plus autoscaling consumers.
- **"Video upload"** or **"compute-heavy job"**, Producer / consumer pattern with job IDs the client polls.
- **"Order processing"** or **"payment"**, Durable queue, retries, and a dead-letter queue for poison messages.
- **"Real-time chat"** or **"live updates"**, Probably not a queue. Mention pub/sub, WebSockets, or Kafka streams instead.

Bonus points for naming SQS, Kafka, or RabbitMQ and explaining why you picked it. More bonus points for mentioning idempotency and DLQs without being asked.

### Questions

#### Why does adding a queue in front of a server improve resilience?

1. It reduces compute cost per request by batching identical calls into a single response 
2. It absorbs traffic bursts the server cannot yet handle, instead of dropping them ✓
3. It makes long-running responses synchronous so clients always see a result inline 
4. It enforces strong consistency across the application tier without distributed locks

> Queues smooth bursts. A request that would otherwise get a 429 waits its turn instead of failing.

#### When is Kafka a better fit than SQS?

1. When you only need a simple FIFO queue between two services in the same VPC 
2. When you want a fully managed SQS-style API on AWS without running your own brokers 
3. When you need to replay historical messages and stream them through analytics pipelines ✓
4. When you need at-most-once delivery and message acknowledgment is your responsibility

> Kafka is a distributed log; messages persist and can be replayed. 
> That is the right tool for streaming pipelines, analytics, and event sourcing.

#### At-least-once delivery means consumers must...

1. Run a single consumer per topic, since multiple consumers would each see the same message 
2. Be idempotent, since the same message can be delivered more than once ✓
3. Use sticky sessions on the load balancer to preserve message order across retries 
4. Process every batch of messages in parallel so duplicates can be skipped automatically

> At-least-once means duplicates are normal. 
> Consumers must be idempotent so they do not double-charge or double-send when the same message arrives twice.

