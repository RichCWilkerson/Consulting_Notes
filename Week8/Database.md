# Overview of Databases in Android Development
- Local databases are primarily used for **caching, local storage, and offline capabilities**.
- Common categories in Android:
  - **SQL-based**: SQLite, Room, SQLDelight.
  - **NoSQL / Document**: Realm, Firebase Realtime Database, Firebase Firestore.

---

## Migrations and Schema Evolution
- **Why migrations matter**:
  - Schema changes (adding/removing columns, changing types) can break existing installations.
  - Proper migrations ensure users don’t lose data when updating the app.
- **Migration strategies**:
  - **Room**: Use `Migration` classes to define step-by-step changes between versions.
  - **SQLDelight**: Write raw SQL migration scripts for each version change.
  - **Realm**: Use Realm’s built-in migration APIs to handle schema changes.
  - **Firebase**: No local schema, but consider data structure changes in your backend and client code.
- **Best practices**:
  - **Test migrations** with real or realistic data.
  - Keep migrations **idempotent** and **reversible** if possible.
    - idempotent: running the migration multiple times has the same effect as running it once.
    - reversible: you can roll back to a previous version if needed.
  - Document schema changes and maintain a version history.
  - Migrate in small steps rather than large jumps.
  
  - Use **Feature Flags** if deploying significant schema changes.
      - migrations are tied to app versions, not directly to feature flags. 
      - Feature flags help you control usage of new features that depend on the new schema, and mitigate risk, but they don’t replace schema migrations.
**PATTERN:**
1. Schema-first, feature-later
  - Version N:
    - Add backwards-compatible schema changes:
      - Add new tables/columns with safe defaults.
      - Avoid destructive changes (no dropping/renaming critical columns yet).
    - Migration runs automatically during app upgrade but feature is off (flag = false).
  - Version N+1 (or later):
    - Enable the feature in code behind a feature flag:
      - Remote config / A/B platform / custom toggles.
    - Turn on gradually:
      - Internal / canary users → small % of users → full rollout.
2. Backwards-compatible
  - New code behind a flag writes into new structures.
  - Old code continues to work against old structures (for a transition period).
  - After you’re confident:
    - Remove old paths,
    - Add a second migration that does the destructive cleanup (drop old columns, etc.).
3. Rollback strategy
  - If the schema migration itself is broken:
    - You cannot safely roll back the DB version on user devices in general.
    - Instead, you:
      - Fix the migration in a new app version,
      - Ship that version quickly,
      - Possibly add recovery logic to repair partial/incorrect migrations.
  - If the migration is fine but the feature logic is broken:
    - Turn the feature flag off:
      - New tables/columns still exist but aren’t actively used.
      - No data loss; you can patch logic in a later version.

## Security Considerations for Databases in Android

- **Threat model**:
  - Assume the device can be lost, stolen, or rooted.
  - Anything stored in plain text can potentially be read by an attacker.

- **Key concepts**:
  - **Data at rest encryption**: encrypting the DB file or key-value store on disk.
  - **Key protection**: where and how you store the encryption keys.

- **Tools & patterns**:
  - **Android Keystore**:
    - Secure storage for *cryptographic keys* (often hardware-backed, non-exportable).
    - Typically used to store a master key that encrypts your DB or preferences.
  - **Encrypted storage libraries**:
    - SQLCipher for encrypting SQLite/Room databases.
    - `EncryptedSharedPreferences` / Encrypted DataStore for key–value data.
  - **Firebase Security Rules**:
    - For Firebase Realtime DB / Firestore, access control is enforced on the **backend**, not the client.
    - Android app authenticates (e.g., Firebase Auth / OIDC), then rules decide what’s allowed.

- **When to use encrypted databases / stores**:
  - Storing sensitive user information (PII, financial, health data).
  - Compliance (GDPR, HIPAA, PCI, internal security policy).
  - Protecting data in case of device theft or loss.

- **Keystore vs Encrypted DataStore (mental model)**:
  - **Keystore** → protects **keys** at runtime.
  - **Encrypted DataStore / EncryptedSharedPreferences / SQLCipher** → protect **data at rest**, using keys (often stored in Keystore).
  - Compile-time constants like endpoints in code cannot be truly “protected” by Keystore; rely on obfuscation + backend design instead.

---

## Determining Which Database to Use

Factors a senior Android dev should consider:

- **Data structure & querying needs**
  - Highly relational, complex joins → SQL/Room/SQLDelight.
  - Document-style, tree/JSON structures → Realm, Firestore, Realtime DB.
  - Need for full-text search, custom indexing, or complex queries.

- **Offline & sync strategy**
  - Is local DB the **source of truth** or just a cache of server data?
  - How do you handle conflicts (last-write-wins, merge strategies)?
  - Do you need **near-realtime sync** (Firestore/Realtime DB) or periodic sync (Room + WorkManager)?

- **Scalability & multi-platform**
  - Does data model need to be shared across platforms (iOS, web, desktop)?
    - SQLDelight (for KMP), Firestore, etc.

- **Tooling & team familiarity**
  - Room is common and well-supported by Jetpack.
  - SQLDelight requires more SQL comfort but gives more control.
  - Realm / Firebase may have steeper learning curves but strong features.

- **Performance requirements**
  - Data volume, query complexity, write frequency.
  - Need for reactive streams (Flow / LiveData / Rx) and backpressure.

- **Development time & maintenance**
  - How heavy are migrations?
  - How easy is it to refactor schema and keep things type-safe?

- **Use-case examples**:
  - Product catalog cache with filters, sorting → Room or SQLDelight.
  - Local-only notes app with rich objects and links → Room/SQLDelight; Realm can work too.
  - Chat-like realtime feed → Firebase Realtime DB or Firestore + local persistence.
  - Cross-platform shared model for KMP app → SQLDelight.

---

## Database Options for Android Development

### SQLDelight Database
- **What it is**:
  - A library that generates Kotlin (and multiplatform) APIs from SQL statements.
  - Uses SQLite under the hood.
- **Strengths**:
  - SQL-first: you write SQL, get **type-safe** Kotlin APIs generated.
  - Great for **Kotlin Multiplatform** (Android, iOS, desktop).
  - Very explicit control over queries and indexes.
- **Considerations**:
  - Requires comfort with SQL and schema design.
  - Migrations are managed via SQL migration files.
  - Less “batteries-included UI integration” than Room but very powerful for complex domains.
- **Good when**:
  - You need **KMP** support and strong SQL control.
  - Complex queries, joins, and performance tuning are important.

### Room Database
- **What it is**:
  - Room is Jetpack’s ORM layer over SQLite.
  - Common choice for local cache and offline storage.
- **Strengths**:
  - Compile-time verification of SQL queries.
  - Integration with coroutines (`suspend` functions) and Flow (`Flow<T>` return types).
  - Works well with ViewModel, Paging 3, and other Jetpack components.
  - Handles migrations via annotated schema versioning and `Migration` classes.
- **Considerations**:
  - Still SQLite underneath; you must understand indices, transactions, and query design.
  - Migrations can become complex in large apps; schema drift and migration testing are important.
- **Good when**:
  - You want a **standard** local DB for Android-only app.
  - Team is comfortable with Room, DAOs, and Jetpack patterns.

### Realm Database
- **What it is**:
  - An object-oriented, NoSQL-like database.
  - Stores objects directly; no SQL layer.
- **Strengths**:
  - Simple model definitions, reactive APIs.
  - Good performance for many mobile patterns.
  - Automatic change notifications for observed queries.
- **Considerations**:
  - Locks you into Realm’s ecosystem and APIs.
  - Migrations and schema evolution are Realm-specific.
  - Less control over low-level queries compared to SQL.
- **Good when**:
  - You want a reactive, object-centric DB with minimal SQL.
  - You accept the trade-off of adopting a proprietary persistence layer.

### Firebase Realtime Database
- **What it is**:
  - A cloud-hosted JSON tree with real-time sync to clients.
  - Have built-in offline support without Room or SQLite. `FirebaseDatabase.getInstance().setPersistenceEnabled(true)`
    - Writes while offline are queued and sent to the server when connectivity returns.
    - Listeners will see cached data immediately, then live updates when online.
- **Strengths**:
  - Real-time updates; great for collaborative or live data apps.
  - Offline support on clients; automatic caching and resync.
- **Considerations**:
  - Data modeling in a large JSON tree can become tricky (denormalization, fan-out writes).
  - Security and access are managed via **Firebase Security Rules**.
  - Not relational; complex queries can be awkward.
- **Good when**:
  - You need **real-time** sync and can design your schema around that model.

### Firebase Firestore Database
- **What it is**:
  - A cloud-hosted **document database** (collections & documents) with real-time listeners.
  - Have built-in offline support without Room or SQLite.
- **Strengths**:
  - More structured than Realtime DB (collections, docs, subcollections).
  - Built-in offline persistence on Android.
  - Powerful querying with indexes.
- **Considerations**:
  - Must plan indexes carefully for performance and cost.
  - Security & rules are critical; misconfigurations can expose data.
- **Good when**:
  - You want **cloud + offline** with document-style data.
  - Need real-time listeners, sync, and cross-platform support.

---

## Common Pitfalls

- **Ignoring migrations**
  - Changing schema without migrations leads to crashes or data loss on upgrade.
  - Not testing migrations against **real user data** or realistic fixtures.

- **Doing DB work on the main thread**
  - Long queries or writes causing ANRs.
  - Not using `Dispatchers.IO`, `suspend` DAOs, or Flow properly.

- **Over- or under-normalization**
  - Too normalized → lots of joins, complex queries, hard to cache.
  - Too denormalized → data duplication, consistency problems, bigger DB.

- **Poor indexing & query design**
  - Missing indexes on frequently-filtered columns.
  - `SELECT *` everywhere, pulling more data than needed.

- **Security oversights**
  - Storing sensitive data unencrypted on disk.
  - Weak or missing Firebase Security Rules.
  - Hardcoding secrets in the APK instead of using backend + Keystore-based encryption keys.

- **Leaky abstractions**
  - Hiding database types too much; difficult to write efficient queries.
  - Putting business logic directly inside DAOs instead of in a domain layer.

- **Tight coupling between DB schema and UI**
  - UI breaks when schema changes.
  - No mapping layer between DB models and UI/domain models.

- **Not planning sync/conflict strategies**
  - Last-writer-wins everywhere can overwrite user changes.
  - No conflict resolution or merge strategy for offline edits.

---

## Common Interview Questions (Senior Android)

1. **How do you choose between Room, SQLDelight, Realm, and Firebase for local storage?**
   - Be ready to talk about data shape, offline/sync needs, performance, KMP, and team skills.

2. **Explain how you would design offline caching for a screen that lists products from a backend API.**
   - API → Repository → local DB (Room/SQLDelight) → ViewModel → UI.
   - Discuss cache invalidation, pagination, and sync.

3. **How do you handle database migrations in production apps?**
   - Strategies to add/remove columns, rename tables, backfill data.
   - Testing migrations and fallback strategies.

4. **How do you secure sensitive data stored locally on Android?**
   - Keystore for keys, encrypted DB/DataStore, threat model (lost/stolen devices).

5. **What are common performance issues with local databases and how do you debug them?**
   - Missing indexes, N+1 queries, large joins, doing heavy work on main thread.

6. **How do Firebase Realtime Database and Firestore differ, and when would you use each?**
   - Tree vs document model, querying, offline behavior, cost model.

7. **Describe a time you had to refactor or migrate a database layer (e.g., from raw SQLite/Anko to Room).**
   - Focus on planning, migration strategy, and minimizing downtime/data loss.

---

