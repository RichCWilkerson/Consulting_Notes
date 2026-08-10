

## Two different problems, two different tools

![Screenshot 2026-06-24 at 8.11.16 PM.png](../System-Design-Images/Screenshot%202026-06-24%20at%208.11.16%E2%80%AFPM.png)

**Replication**, Copies of the same data on different machines. Solves availability (a replica takes over when the leader dies) and read scale (route reads to the replicas).

**Sharding**, Different chunks of the data on different machines. Solves write scale and storage capacity. Each shard is its own primary database.

Most large systems use both: a sharded primary, with each shard replicated across availability zones. Replication without sharding hits a write ceiling; sharding without replication has no story for a node dying.


### Replication topologies

**Single-leader**, One node accepts writes, others stream changes from it. Easy mental model and the default for Postgres, MySQL, MongoDB. Writes don't scale past one box.

**Multi-leader**, Multiple nodes accept writes and sync between themselves. Higher write throughput and good for multi-region, but conflicts on the same row are now your problem to resolve (last-write-wins, CRDTs, application logic).

**Leaderless**, Every replica accepts reads and writes, a quorum reconciles disagreements. Dynamo-style. Cassandra and DynamoDB. AP-leaning, eventually consistent.

Sync vs async replication is the other axis. Sync replication blocks the write until at least one replica confirms, giving you durability at a latency cost. Async returns immediately and you can lose recent writes if the leader crashes before the replica catches up.


### Sharding strategies

**Range-based**, Shard by ranges of an indexed value (user_id 0-1M on shard A, 1M-2M on shard B). Range scans stay efficient. Skewed data creates hot shards (one celebrity user dominates a shard).

**Hash-based**, Hash the key, mod by shard count. Uniform distribution, no hot spots. Range scans become scatter-gather across every shard, which is slow and brittle.

**Geo-based**, Shard by region. Lowest latency for local users and a clean compliance story for data residency. Cross-region queries are expensive, and a user moving requires re-sharding.

**Consistent hashing**, Hash both keys and shard ids onto a ring. Adding or removing a shard only moves a fraction of keys, not the whole dataset. The default for distributed caches (Memcached, Redis Cluster) and DynamoDB.


### What you give up

Replication lag means reads from a replica may not see writes from 50ms ago. Read-your-own-writes needs to route to the leader for that user briefly, or use a session token to pin to the leader for a window.

Sharding makes joins, transactions, and unique constraints across shards slow or impossible. Pick a shard key you'll never need to join across.

Re-sharding at scale is brutal. The standard playbook is double-write to old and new, backfill historical data, dual-read while you verify, then cutover. It takes months and you only do it once if you can avoid it.

Hot shards (one celebrity, one viral video, one giant tenant) defeat any uniform scheme. Watch for skew and split aggressively when you see it.

Cross-shard transactions need sagas with compensating actions. There is no ACID across shards.


### What to listen for

**"Read-heavy"**, Single leader plus N read replicas. Round-robin reads across replicas, writes always to the leader.

**"Write-heavy" or "petabyte-scale"**, Sharded primary. Hash-based unless ranges naturally cluster (time-series, geo).

**"Strong consistency on writes"**, Single leader, sync replication to at least one replica before ack.

**"Multi-region"**, Multi-leader or per-region shards. Name the consistency model out loud (AP across regions, CP within a region is the common compromise).

State your shard key explicitly and justify it. "I would shard by user_id because every read in this product is keyed by user, so cross-shard queries are rare." Naming the key and the access pattern in the same sentence is the senior signal.


### Questions

#### What problem does sharding solve that replication does not?

1. Read scale, by spreading the read load across many identical copies of the data 
2. Availability when a node dies, by promoting one of the surviving copies to leader 
3. Write scale and storage capacity beyond what a single machine can hold ✓
4. Backups, by keeping a continuously up-to-date copy in a second availability zone

> Replication = many copies of the same data (read scale + availability). 
> Sharding = different data on different machines (write scale + storage).

#### You expect celebrity-style hot rows in your dataset. Which sharding strategy is most resilient to skew?

1. Range-based sharding by user_id so that ranges of similar IDs land on the same machine 
2. Geo-based sharding only, since users in one region rarely overlap with users in another 
3. Hash-based or consistent-hashing on user_id ✓
4. A single shard for everyone, since a global view sidesteps the hot-row problem entirely

> Range-based sharding clusters celebrity data into one shard, which becomes hot. 
> Hash-based and consistent-hashing distribute uniformly so no single shard overheats.

#### What does replication lag mean in practice?

1. Reads from a replica may not see writes from the leader that just landed 
2. Writes to the leader are slower than writes to a replica during normal operation 
3. Replicas are always faster than the leader because they serve only read traffic 
4. Replicas drop messages randomly when the network between leader and follower congests

> With async replication, a replica can be milliseconds-to-seconds behind. 
> Read-your-own-writes scenarios need to route to the leader briefly.

