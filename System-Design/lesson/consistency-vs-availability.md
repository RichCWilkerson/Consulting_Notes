

## The classic distributed-systems trade-off

![Screenshot 2026-06-11 at 11.18.32 PM.png](../System-Design-Images/Screenshot%202026-06-11%20at%2011.18.32%E2%80%AFPM.png)

CAP says: during a network partition, you can guarantee Consistency or Availability, but not both. Partitions are a given in any distributed system, so this choice is unavoidable.

Which side you pick shapes your database choice, replication strategy, user experience, and even the copy on your error screens. Getting this right is usually the difference between a system that just works and one that constantly surprises its users.

### Consistency (CP)

A consistent system guarantees that every read returns the most recent write. If I write a new value on one node and you read from a different node immediately after, you see my change. Stale reads are never allowed.

During a partition, a CP system may block requests rather than serve stale data. You pick C when correctness is worth more than uptime:

- Financial transactions. Bank balances and transfers can't silently disagree across nodes.
- Permissions, auth, security settings. A revoked token needs to stop working everywhere.
- Inventory. Don't sell an item you already sold. Oversell once and you owe customers a refund and an apology.

Implemented with distributed locks, write quorums, synchronous replication, and leader election. Think CockroachDB, Spanner, or PostgreSQL with synchronous replicas.

### Availability (AP)

An available system always responds to a request, even if it can't confirm the data is the most recent. The system stays up during partitions, and the replicas catch up later.

Pick AP when user experience and uninterrupted access matter more than absolute freshness:

- Social feeds and timelines. A 5-second-old post is fine. A blank feed is not.
- Caches. Stale data usually beats no data.
- Logging, analytics, metrics. You'd rather capture slightly out-of-order events than drop them entirely.

Built with asynchronous replication and eventual consistency. Think Cassandra, DynamoDB, and Redis replicas. Most consumer-facing systems default here.

### You can actually have both

![Screenshot 2026-06-11 at 11.24.30 PM.png](../System-Design-Images/Screenshot%202026-06-11%20at%2011.24.30%E2%80%AFPM.png)

Most real systems mix strategies per feature, not per system. You pick CP or AP where it matters, and let the rest of the app be fast.

- Browsing page, AP. Needs to be fast and responsive. Slightly stale prices are fine because the user is just looking.
- Checkout, CP. Must confirm the item is actually in stock before taking money. Overselling is worse than a slow checkout.
- Profile picture, Eventual. Nobody cares if one tab takes 10 seconds to show the new avatar.
- Password reset, Strong consistency. The old password must stop working everywhere, immediately, or you have a security bug.

The takeaway: don't argue about the system's "consistency model" as one decision. Argue per flow.

### What to say

The magic phrase: "Since we can't avoid partitions, we have to pick between C and A, and we can pick differently per feature." Say that and you've already passed the CAP question.

- "Real-time" does not mean "strongly consistent." Real-time usually means low latency, which is closer to AP. Be precise about what the user actually needs.
- Name the mechanisms on each side. Write quorums, WAL, asynchronous replication, leader election, read-your-own-writes.
- Call out explicitly where eventual consistency is good enough. Customers and PMs rarely understand it, so explaining tradeoffs clearly is the senior answer.

### Questions

#### What does an AP (available, partition-tolerant) system do during a network partition?

1. Refuses to serve requests on either side of the partition to avoid returning stale data 
2. Continues to respond, possibly with stale data ✓
3. Replicates synchronously across all nodes before acknowledging any reads or writes 
4. Locks all writes globally until the partition heals and the cluster regains a quorum

> AP systems prioritize availability. 
> During a partition they keep responding, accepting that some reads may be stale until the partition heals.

#### Which feature most clearly demands strong consistency (CP)?

1. Loading the homepage feed 
2. Showing a user's profile picture 
3. Inventory check at checkout ✓
4. Suggesting similar products

> Overselling is a real cost. 
> Inventory at checkout must be strongly consistent so you do not sell something twice.

#### What is the senior interview answer to "consistency vs availability"?

1. Always pick CP, since data correctness matters more than uptime in any real product 
2. Always pick AP, since user-visible uptime matters more than freshness in any real product 
3. Pick per-feature based on what each individual flow can tolerate ✓
4. Use Postgres for everything, since its tunable isolation handles both cases by default

> Real systems mix C and A per feature. 
> Browsing is AP, checkout is CP, avatars are eventually consistent. 
> The "per-feature" framing is the senior signal.