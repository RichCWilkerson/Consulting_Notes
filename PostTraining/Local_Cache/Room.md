# Room

## Resources

- [Official Room docs](https://developer.android.com/training/data-storage/room)
- [Room Kotlin samples](https://github.com/android/architecture-components-samples)

---

## What is Room?

Room is the Android Jetpack persistence library that sits on top of SQLite and provides:

- A type‑safe Kotlin/Java API instead of hand‑written `SQLiteOpenHelper` code.
- Compile‑time validation of SQL queries.
- Integration with coroutines, `Flow`, LiveData, and Paging.
- Clear separation between schema (`@Entity`), access layer (`@Dao`), and database (`@Database`).

Conceptually: **Entities → DAOs → Database**.

Room is not a replacement for:

- A full ORM with complex runtime mapping logic.
- A network/cache sync solution by itself.

---

## Pros and Cons of Room

### Pros
- **Compile‑time safety**: SQL is checked at compile time; broken queries fail the build.
- **Kotlin‑first**: Coroutines support (`suspend`, `Flow`), nullability, and data classes.
- **Migrations**: Structured migrations with versioning and schema export.
- **Interoperability**: Works well with Repository pattern, UseCases, and DI frameworks.
- **Testability**: In‑memory databases for unit tests, Robolectric/instrumented tests with the same schema.

### Cons / Trade‑offs
- **SQLite‑bound**: You still need to understand basic SQLite (indices, joins, transactions).
- **Schema rigidity**: Changing schemas requires migrations; careless changes can be painful.
- **Learning curve**: Advanced features (relations, multi‑db, multi‑instance, FTS, etc.) add complexity.
- **No automatic sync**: You still have to design offline/online sync and conflict resolution.

---

## When to Use Room

Room is a good fit when:

- You need **structured, queryable local data** (joins, filters, ordering, paging).
- You care about **offline‑first** or **read performance**.
- Data model is **shared across multiple screens** and evolves with app versions.

Room may *not* be ideal when:

- You only need to store a few key‑value pairs → prefer `DataStore`.
- The data is **write‑heavy, short‑lived**, and doesn’t need advanced querying → consider an in‑memory cache.
- You need **full‑text search** or **vector search** beyond what SQLite/Room can reasonably handle → external service or specialized storage.

---

## Annotations in Room

### Entity Layer

- `@Entity` – Marks a class as a database table.
  - Key options: `tableName`, `indices`, `primaryKeys`, `foreignKeys`.
- `@PrimaryKey` – Marks the primary key column.
  - Commonly used with `autoGenerate = true` for surrogate keys.
- `@ColumnInfo` – Customizes column metadata (e.g., `name`, `typeAffinity`, `defaultValue`).
- `@Ignore` – Excludes a field or constructor from persistence.
- `@Embedded` – Flattens another POJO’s fields into the parent entity.
- `@Relation` – Declares relationships (1‑to‑1, 1‑to‑many) on a **POJO used as a query result**, not on the entity itself.
- `@ForeignKey` – Declares a foreign key constraint from one entity to another.
- `@Index` – Adds an index on one or more columns for faster lookups.
- `@TypeConverters` – Associates custom type converters with an entity.

### DAO Layer

- `@Dao` – Marks an interface/abstract class as a Data Access Object.
- `@Insert` – Generates an insert statement.
  - Conflict strategies via `onConflict = OnConflictStrategy.REPLACE` (or `ABORT`, `IGNORE`, etc.).
- `@Update` – Generates an update statement.
- `@Delete` – Generates a delete statement.
- `@Query` – Executes an arbitrary SQL query (SELECT/UPDATE/DELETE, etc.).
- `@Transaction` – Wraps multiple DAO calls in a single SQL transaction.
- `@RawQuery` – Executes a raw SQL query. Use sparingly; you lose some compile‑time guarantees.
- `@RewriteQueriesToDropUnusedColumns` – Optimize queries by dropping unused columns from the result set.
- `@MapInfo` – Helps map results of certain queries into `Map` types.
- `@Upsert` – Inserts or updates on conflict (Room 2.5+).

### Database Layer

- `@Database` – Marks the abstract database class.
  - Declares entities, version, and `exportSchema`.
- `@TypeConverters` – Applies type converters to the whole database.

### Migration

Room supports two main migration approaches:

- **Manual migrations** – Implement `Migration(startVersion, endVersion)` and override `migrate(db: SupportSQLiteDatabase)`.
- **Auto‑migrations** – Let Room compute schema diffs when possible using `@AutoMigration`.

Key annotations:

- `@AutoMigration` – Attach auto migrations to the `@Database` annotation for supported schema changes.
- Migration helper annotations like `@RenameColumn`, `@DeleteColumn`, `@RenameTable`, `@AddColumn` are used *inside auto‑migration specs* to describe more complex changes.

---

## Common Pitfalls

- **Blocking the main thread**
  - Forgetting to mark DAO methods as `suspend` or returning `Flow`/`LiveData`.
    - Flow should not be a suspend function; it’s already asynchronous and observable.
  - Accidentally using `allowMainThreadQueries()` in production.

- **Migrations not tested**
  - Schema changes without migrations → runtime `IllegalStateException` on startup.
  - Relying on destructive migration for user data you actually care about.

- **Incorrect relationships**
  - Misusing `@Relation` on entities instead of on POJOs.
  - N+1 queries instead of using a single `@Transaction` + relational mapping.

- **Leaky abstractions**
  - Exposing Room entities directly to UI instead of mapping to domain models.
  - Packing too much logic into DAOs rather than into repositories/use cases.

- **Schema / index issues**
  - Missing indices on frequently filtered columns → slow queries.
  - Over‑indexing write‑heavy tables → unnecessary write overhead.

---

## Best Practices

- **Design from use‑cases, not tables**
  - Start from the queries/screens you need, then design entities and indices.

- **Separate layers cleanly**
  - Entities stay close to the persistence model.
  - Use mappers to convert between Entity ↔ Domain ↔ UI models.

- **Coroutines + Flow**
  - Prefer `suspend` for one‑shot ops and `Flow` for observable queries.
  - Combine with Paging 3 for large lists.

- **Migrations as first‑class citizens**
  - Bump DB version with each schema change.
  - Keep migrations alongside schema changes in the same PR.
  - Use `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2, ...)`.
  - Export schema (`room { schemaDirectory(...) }`) and keep it under VCS.

- **Testing**
  - Use in‑memory DB (`Room.inMemoryDatabaseBuilder`) for fast unit tests.
  - Add migration tests using `MigrationTestHelper`.

- **Performance**
  - Use `EXPLAIN QUERY PLAN` for slow queries.
  - Avoid unnecessary `@Transaction` boundaries.
  - Prefer bulk operations (`@Insert(onConflict = ...)` with lists) over many single‑row calls.

---

## Interview‑Style Questions / Prompts

- How does Room differ from `SQLiteOpenHelper` and what problems does it solve?
- Explain how you’d design migrations for a frequently changing schema.
- How do you model 1‑to‑many and many‑to‑many relationships in Room?
- When would you use `Flow` vs `suspend` DAO functions?
- How do you structure your layers around Room (DAO, Repository, UseCase, UI)?
- How do you keep migrations safe and testable across multiple versions?

---

## Minimal Setup / Steps

1. **Add Room dependencies and plugins** (Kotlin + KSP example):

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
    id("androidx.room")
    alias(libs.plugins.ksp)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    testImplementation("androidx.room:room-testing:2.8.4")
}
```

2. **Define an entity**:

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
)
```

3. **Create a DAO**:

```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY name")
    fun observeUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity): Long

    @Delete
    suspend fun delete(user: UserEntity)
}
```

4. **Create the database**:

```kotlin
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

5. **Build the database instance** (typically in a DI module):

```kotlin
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app.db"
)
    .fallbackToDestructiveMigrationOnDowngrade() // use cautiously
    .build()

val userDao = db.userDao()
```

This should be enough context for a senior Android dev to discuss or extend Room usage in architecture, offline‑first design, and migration strategy.
