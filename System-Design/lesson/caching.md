

## Fast reads, cheaper cloud bill

![Screenshot 2026-06-06 at 10.12.33 AM.png](../System-Design-Images/Screenshot%202026-06-06%20at%2010.12.33%E2%80%AFAM.png)

At its core, caching is about temporarily storing frequently-accessed data in a fast, in-memory store so future reads don't have to hit a slower underlying system.

Done well, caching makes slow operations feel instant. It cuts latency for your users, reduces load on your database, and shrinks your cloud bill. Done poorly, it causes stale data, hard-to-reproduce bugs, and system-wide inconsistencies.

> There are 2 hard problems in computer science: cache invalidation, naming things, and off-by-one errors.

### Where to put the cache

The right layer depends on where your latency is actually coming from. Measure first, then cache.

- Between server and DB, Use this when the database itself is the bottleneck. You cache the results of expensive queries. Easy to invalidate because there's one clear source of truth.
- At the API or edge, Use this when the bottleneck is server processing, not the database. If two identical GET requests come in within 30 seconds, you only process one. CDNs fall into this category for static assets.
- On the client, Use this for data that rarely changes. Profile photos, configuration flags, reference data. Also cuts network requests entirely.

In a real system, you'll often have all three. Each one defends the layer behind it.

### Invalidation strategies

![Screenshot 2026-06-06 at 10.16.25 AM.png](../System-Design-Images/Screenshot%202026-06-06%20at%2010.16.25%E2%80%AFAM.png)

As soon as you add a cache, you have to decide when to keep data and when to evict it. Your choice is the difference between a fast app and a confused one.

- **TTL (time to live)**, Set a timer when data enters the cache. When the timer expires, the data is evicted. Dead simple, but can serve stale data for the full TTL if someone writes during that window.
- **Write-through**, Every write updates both the cache and the database. Reads are always fresh, but writes now pay for two hops instead of one. The safest default for most systems.
- **Write-around**, Writes go to the database only. The stale cache entry is evicted so the next read refreshes it. Good when writes are rare and reads are hot.
- **Write-behind**, Writes hit the cache first, then flush to the database asynchronously. Extremely fast for the client, but you risk losing a write if the cache crashes before the flush. Only use this when you can accept that risk.

These are not mutually exclusive. Write-through plus TTL is a common combination: TTL protects you from bugs, write-through keeps things fresh.

### What caches cost you

Nothing is ever free. A cache adds its own class of problems, and you should be able to name them.

- Stale reads vs fresh reads. Pick your poison for each dataset based on how much wrong data the user can tolerate.
- Cache misses are still slow. Plan for the cold path, not just the hot one.
- A cache outage can cause a thundering herd, where every request suddenly hits the database at once. Mitigate with request coalescing, staggered TTLs, and read replicas.
- Write-behind is the fastest option and the most dangerous. If you acknowledge a write before it's durable, a crash loses real customer data.
- Cache inconsistency is a nightmare to debug. A user sees old data in one tab and new data in another, and it's never the same two requests.

### What to listen for

- **"Reduce latency"** or **"read-heavy"**, Add a cache layer in front of the database.
- **"Identical repeated requests"**, API-level cache, keyed by the request signature.
- **"Low-latency writes"**, Write-behind, and name the crash risk out loud.
- **"Hot key"** or **"top creator"**, Mention cache warming, replication, or shard splitting.

Always explain your invalidation strategy. "I'd use a write-through cache with a 5-minute TTL as a safety net." That's what separates a junior answer from a senior one. A lot of candidates add a cache and forget to say how data ever leaves it.

### Questions

#### Which cache write strategy keeps the cache and database in lockstep at the cost of slower writes?

1. Write-around 
2. Write-through ✓
3. Write-behind 
4. Read-through

> Write-through updates both cache and DB on every write, keeping them in sync. 
> The cost is an extra hop on the write path.

#### Where should you place a cache when the database itself is the bottleneck?

1. In the client only, since the database is downstream of the application server anyway 
2. In the DNS layer, since DNS caching is the cheapest place to absorb repeated queries 
3. Between the server and the database (e.g., Redis) ✓
4. Inside the load balancer, which already sits in front of every database connection

> A server-DB cache absorbs expensive query results and protects the DB from repeat reads. 
> That is the placement that helps when the DB is hot.

#### What is a "thundering herd," and when does it happen?

1. A spike of chat users joining the same room at once and exhausting connection pools 
2. When a cache layer is unreachable, every request floods the database simultaneously ✓
3. A load-balancer failover that briefly sends every connection to a single backup node 
4. When too many concurrent writes hit a single primary leader during a traffic spike

> A thundering herd is what happens when a cache goes cold or down: 
> every request that would have hit cache now hits the DB at once. 
> Mitigate with request coalescing, staggered TTLs, and read replicas.