

## The lost update problem

![Screenshot 2026-06-28 at 7.49.49 PM.png](../System-Design-Images/Screenshot%202026-06-28%20at%207.49.49%E2%80%AFPM.png)

Two users open the same record at the same time, both edit it, both save. Without concurrency control, the second save silently overwrites the first. The first user thinks their change went through. It did not.

This shows up everywhere: inventory counts, document edits, account balances, ticket assignments. Concurrency control is how the database (and your app) make sure simultaneous writes either coordinate or fail safely.

## Pessimistic vs optimistic

- **Pessimistic locking**, Acquire a lock on the row before reading or writing. Other writers block until you release. Safe and easy to reason about. Throughput drops under contention and deadlocks become a real risk. SELECT ... FOR UPDATE in Postgres or MySQL.
- **Optimistic concurrency**, Read freely, attach a version number or timestamp on write, and let the database reject the update if the version moved. The losing client retries. Cheaper when conflicts are rare, expensive when they are not.

Pick pessimistic when conflicts are common and retries are costly (inventory, payments). Pick optimistic when most edits do not collide (collaborative documents, profile pages). Most real systems use both, per table.

## Isolation levels and MVCC

Relational databases offer isolation levels that control what concurrent transactions can see. Higher isolation prevents more anomalies and costs more throughput.

- **Read committed**, You only see committed data. The default for Postgres. Allows non-repeatable reads (the same query twice can return different rows mid-transaction).
- **Repeatable read**, A row you read once stays the same for the whole transaction. The default for MySQL InnoDB. Prevents non-repeatable reads but allows phantom rows.
- **Serializable**, Transactions behave as if they ran one at a time. The safest level and the slowest. Postgres uses serializable snapshot isolation (SSI), which aborts conflicts instead of locking.
- **MVCC**, Multi-version concurrency control. Readers see a snapshot from when the transaction started; writers create a new version. Readers never block writers. Postgres, MySQL InnoDB, Oracle.
- **Distributed locks**, When the work spans services, use a lock manager (Redis, ZooKeeper, etcd). Always set a TTL so a crashed holder cannot deadlock the system.

## What concurrency control costs

Higher isolation means more aborts under contention. Be ready to retry serializable conflicts in a loop with backoff, capped at a few attempts.

Deadlocks happen when two transactions lock the same resources in opposite orders. The database picks a victim and aborts it. Always order your locks consistently to keep this rare.

Distributed locks across services need a TTL plus a fencing token to be safe. A holder can pause (GC, network) past the TTL and a second holder takes over, creating two holders at once.

Optimistic retries cause thundering herds on hot rows. A celebrity row with a million viewers loses repeatedly. Switch to pessimistic locking or a queue for that row, or move it out of the hot path.

Long transactions hold versions in MVCC, bloating the database. Keep transactions short.

## What to listen for

- **"Inventory at checkout"**, Pessimistic lock on the SKU row, or a decrement that fails on negative stock. Never read-then-write without a guard.
- **"Concurrent document edits"**, Optimistic concurrency with a version number, or operational transforms / CRDTs for character-level merges.
- **"Once-only job"**, Insert a row in a jobs table with a unique constraint, or a distributed lock with a TTL plus fencing token. The DB enforces uniqueness.
- **"Phantom reads"**, Repeatable read or serializable isolation. Justify the tradeoff with throughput numbers.

Name the isolation level out loud. "Postgres default is read committed; for the inventory transaction I would use a SELECT ... FOR UPDATE inside a serializable block" is what a senior answer sounds like.

## Questions

### What does optimistic concurrency assume?

1) That every write conflicts at some point and must be retried by the application layer 
2) That conflicts are rare, so we let writes proceed and detect collisions via a version/timestamp on commit ✓
3) That all reads happen on a replica so the primary never sees concurrent transactions 
4) That the database always uses two-phase locking internally to serialize concurrent updates

> Optimistic concurrency assumes conflicts are uncommon. 
> You read freely, write with a version, and the database aborts the loser of any actual collision.

### In Postgres, the default isolation level is...

1) Serializable 
2) Read uncommitted 
3) Read committed ✓
4) Repeatable read

> Postgres defaults to read committed. 
> Serializable is available (SSI) but opt-in.

### Why does a distributed lock with only a TTL still allow two holders at once?

1) The TTL is always too long for production workloads, so contention windows overlap by default 
2) If the holder pauses past the TTL, another client can acquire the lock, and without a fencing token downstream services cannot tell which holder is authoritative 
3) Distributed locks ignore TTLs and keep the key until the holder explicitly releases it 
4) TTLs do not work with Redis because key expiry is best-effort and runs asynchronously on a sweep

> A TTL alone allows lock takeover. 
> A fencing token (monotonically increasing) lets downstream services reject stale holders so only one writer actually wins.