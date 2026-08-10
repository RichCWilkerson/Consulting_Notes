# Local Cache Synchronization

## Short answer

An offline-first app normally reads from its local database, then synchronization brings that database closer to the server's state. A scheduler such as WorkManager decides **when to attempt** work; a synchronization coordinator decides **whether work is due and what the result means**; a transaction decides **when the new data and its continuation token become one committed state**.

The most important rule is: **do not advance sync metadata until the related data is safely committed**.

---

## The responsibilities are different

```text
WorkManager / app start / user refresh
              |
              v
     Synchronization coordinator
        |                  |
        v                  v
  Remote source       Local database
  HTTP + DTOs         rows + sync metadata
```

### WorkManager owns triggering

WorkManager can express constraints such as:

- A network is connected.
- The network is unmetered.
- The battery is not low.
- Storage is not low.
- The device is charging.

It does not own the feature's freshness rule, DTO mapping, database transaction, or conflict resolution. Periodic work is also not an exact alarm. Android can delay it because constraints are unmet or the system is conserving resources.

### The coordinator owns policy

The synchronization coordinator answers questions such as:

- Is the cache old enough to refresh?
- Which continuation token should be sent?
- Is this failure transient or permanent?
- Can the same response be safely applied twice?
- When is the sync considered successful?

Keeping this logic outside a Worker makes it reusable from app start, pull-to-refresh, tests, and future platforms.

### Room owns atomic persistence

Room's transaction should apply the remote changes and update the sync metadata together. If the app crashes halfway through, either both changes commit or neither does.

Bad sequence:

1. Save the new sync token.
2. Begin saving records.
3. App crashes.
4. Next request starts after records that were never saved.

Safe sequence:

1. Fetch and validate the response.
2. Begin a database transaction.
3. Apply the records.
4. Save the new sync token and success time.
5. Commit the transaction.

---

## Wi-Fi first, then any network

WorkManager cannot take one request and dynamically change its network constraint from unmetered to connected after a deadline. Use two unique requests that share the same stored last-success time:

```text
around 18 hours
  unmetered request runs
  -> coordinator checks last success
  -> syncs if due

at or after 24 hours
  any-connected-network request runs
  -> coordinator checks the same last success
  -> no-op if the first request already succeeded
  -> otherwise performs the fallback sync
```

This is a preference window, not an exact schedule. For example, the unmetered request may wait because no unmetered network is available, and the fallback may run later than 24 hours because Android still controls execution.

Why not enqueue the cellular fallback only after the Wi-Fi worker fails? An unmet constraint means the first worker may simply remain queued; it has not executed and therefore cannot schedule its fallback. Independent unique work avoids that dead end.

---

## Continuation tokens, cursors, and timestamps

A backend may provide one of several ways to ask for changes:

- **Cursor or continuation token**: an opaque server-issued value. Store it but do not interpret it.
- **Version number**: request changes after a monotonically increasing server version.
- **Updated timestamp**: request rows modified after a time. Easier to understand, but clock precision, equal timestamps, and clock skew can create gaps.
- **ETag**: validates whether a representation changed and may produce HTTP `304 Not Modified`; it is not automatically a multi-page change cursor.

Prefer a server-issued cursor/version when available. If pagination is involved, clarify whether each page has a continuation token and when a completed-cycle token becomes safe to persist.

---

## Upsert is not the whole sync policy

An upsert answers: "What should happen to an incoming record with this primary key?" It does not answer:

- Was a missing record deleted, or was it merely not in this page?
- Should a local user field be overwritten?
- Did the server move a record outside the current filter?
- How do multiple devices edit the same user state?

For VoteWithMe-style data, public civic records and user state should be separate. A public-data refresh can upsert a title or status without replacing the user's favorite or position.

For remote deletions, the API needs an explicit contract such as tombstones or deleted IDs. Never infer deletion from absence in a partial or filtered response.

---

## Idempotency and concurrency

An operation is idempotent when applying the same successful response again leaves the same final state. Primary-key upserts and a transactionally stored continuation token help make a download sync idempotent.

Use unique WorkManager names to avoid duplicate schedules. Also guard the coordinator with a process-level mutex because app-start, periodic, and user refresh triggers may overlap while the process is alive. For multiple processes or multiple devices, a mutex is not sufficient; the database and backend contract must provide the relevant guarantees.

---

## Failure classification

### Retry with backoff

Usually retry:

- No connectivity after execution began.
- Timeout or temporary DNS failure.
- HTTP 429 rate limiting, while respecting `Retry-After` when possible.
- HTTP 5xx server failure.

Exponential backoff spaces repeated attempts so the app does not hammer a failing network or service.

### Do not retry indefinitely

Usually treat as permanent until code, account state, or configuration changes:

- A response does not match the required contract.
- Mapping or validation rejects required data.
- Authentication is invalid and cannot be refreshed.
- The requested endpoint or resource is permanently unavailable.

Do not log full payloads or user positions merely to diagnose a failure. Record safe operational facts such as result category, duration, source name, and item count.

---

## Migration versus synchronization

These solve different version problems:

- A **Room migration** transforms an old on-device schema into the schema expected by a newer app version.
- **Synchronization** transforms locally cached content toward the backend's current content.

Destructive migration can be reasonable in a prototype when every row is disposable. It must be removed before real offline-only user changes exist, because synchronization cannot restore data the backend never received.

---

## Practical checklist

Before enabling remote synchronization, be able to answer:

1. What local table is the UI's read source of truth?
2. What makes the data stale enough to refresh?
3. Does the API return snapshots, pages, or deltas?
4. How are deletions represented?
5. Which fields belong to the server, the user, or both?
6. What token proves the last fully committed sync point?
7. Can applying the same response twice cause damage?
8. Which failures retry, and which require intervention?
9. Can two triggers execute concurrently?
10. What safe diagnostics prove that sync is healthy?

---

## VoteWithMe example

The planned Civic flow uses:

- Room as the normal read source.
- Separate tables for public civic records and local favorite/position state.
- An unmetered attempt before the 24-hour freshness deadline.
- An any-network fallback once the last successful sync is at least 24 hours old.
- Battery-not-low and storage-not-low constraints.
- A shared coordinator that stores incoming records and the new token in one transaction.
- A disabled remote implementation until the API contract exists.

That last point is deliberate scaffolding: the scheduler and database rules can be built and tested without pretending that a remote contract has already been decided.

## Official resources

- [WorkManager persistent work overview](https://developer.android.com/develop/background-work/background-tasks/persistent)
- [Define work requests, constraints, periodic work, and backoff](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
- [Room transactions](https://developer.android.com/reference/kotlin/androidx/room/Transaction)
- [Offline-first Android data layer](https://developer.android.com/topic/architecture/data-layer/offline-first)
