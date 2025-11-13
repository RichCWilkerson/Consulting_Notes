# Android Project Structure Patterns

This document shows several recommended package/project structures for Android apps, all rooted under `com.example.appname`. For each pattern you'll find a tree example, pros/cons, when to use it, and small code snippets mapping to common components (Retrofit, Room, Hilt, ViewModel, Fragment).

Checklist
- Add multiple package layout patterns.
- Provide pros/cons and use-cases for each.
- Map your current layout to the patterns and give recommendations.
- Include small, copy-paste-friendly code snippets.

---

## Your current layout (example)
You mentioned you use:
- top level: `/data`, `/di`, `/ui`
- inside `/data`: `/dtos`, `/mappers`, `/remote` (apis), `/local` (Room), `/model` (api/domain models)
- `/di` holds Hilt @Module files
- `/ui` holds a directory per screen (e.g. `/home` contains `HomeFragment`, `HomeAdapter`, `HomeViewModel`, etc.)

This is a solid feature-oriented hybrid that works well for medium apps. Below are alternative patterns and recommendations.

---

## 1) Layered (package-by-layer)
Good for: small apps or when teams prefer separating concerns by technical layers.

Structure:
```
com.example.appname
├─ data
│  ├─ remote
│  │  ├─ api
│  │  └─ dto
│  ├─ local
│  │  └─ db
│  └─ repository
├─ domain
│  ├─ model
│  └─ usecase
├─ ui
│  ├─ main
│  └─ settings
├─ di
└─ util
```
Pros:
- Clear separation of technical concerns (data vs domain vs ui).
- Easy to find all data-related classes.
Cons:
- Can group unrelated features together (e.g., auth + payments both under `data`).

When to use:
- Small apps or teams that prefer strict separation by layer.

Short example (Repository interface in `domain` + impl in `data`):
```kotlin
// domain/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: String): User
}

// data/UserRepositoryImpl.kt
class UserRepositoryImpl(
    private val api: UserApi,
    private val dao: UserDao
): UserRepository {
    override suspend fun getUser(id: String) = ...
}
```

---

## 2) Feature-first (package-by-feature / package-by-screen)
Good for: medium-to-large apps where features are independent and evolve separately.

Structure:
```
com.example.appname
├─ features
│  ├─ home
│  │  ├─ HomeFragment.kt
│  │  ├─ HomeViewModel.kt
│  │  ├─ HomeAdapter.kt
│  │  └─ di (module for feature)
│  └─ profile
│     ├─ ProfileFragment.kt
│     └─ ProfileViewModel.kt
├─ core
│  ├─ network
│  └─ database
└─ app (Application class)
```
Pros:
- Each feature holds everything needed to work (UI, VM, adapters, local components, DI bindings).
- Easier to extract into modules later.
Cons:
- Small shared utilities may be duplicated if not factored into `core`.

When to use:
- Apps with many independent features or teams owning different features.

Map to your layout:
- Your `/ui/<screen>` approach maps well to this. Move feature-specific data and mappers into the feature folder if they are not reusable.

---

## 3) Clean Architecture (recommended for larger apps)
Good for: large apps requiring testability, clear boundaries, and independent evolution.

Structure:
```
com.example.appname
├─ data (implementation)
│  ├─ remote
│  └─ local
├─ domain (pure business rules)
│  ├─ model
│  ├─ repository (interfaces)
│  └─ usecases
├─ presentation
│  ├─ featureA
│  └─ featureB
├─ di
└─ core
```
Pros:
- Strong separation: domain layer has no Android or networking dependencies.
- Easy to unit-test business logic.
Cons:
- More boilerplate and initial complexity.

Typical flow:
UI -> ViewModel -> UseCase -> Repository (domain interface) -> Data (remote/local) -> network/db

Example mapping and code:
```kotlin
// domain/GetUserUseCase.kt
class GetUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: String) = repo.getUser(id)
}

// di/NetworkModule.kt (Hilt)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides
  fun provideRetrofit(ok: OkHttpClient): Retrofit =
    Retrofit.Builder().baseUrl(BASE)
      .addConverterFactory(GsonConverterFactory.create())
      .client(ok)
      .build()

  @Provides
  fun provideUserApi(retrofit: Retrofit): UserApi =
    retrofit.create(UserApi::class.java)
}
```

---

## 4) Multi-module (Gradle modules per feature / layer)
Good for: large teams, apps with boundaries, reusability across apps, parallel builds.

Example modules:
- :app (contains Android app / navigation / DI wiring)
- :core (utilities, base UI components)
- :network (Retrofit/OkHttp, models)
- :features:home
- :features:profile
- :data (optional shared data layer)

Pros:
- Faster CI with parallel builds, clearer ownership, versioned modules.
Cons:
- More complex Gradle setup, can increase cognitive overhead.

When to use:
- Big apps, multiple teams, or when you want to reuse features across apps.

---

## 5) Hexagonal/Ports-and-Adapters
Good for: extreme separation and testability. Domain defines ports (interfaces) and adapters implement them.

Structure resembles clean architecture; domain is the center, adapters are data/presentation.

---

## Naming and Small Conventions
- Base package: `com.example.appname` then either add `feature` or `layer` level.
- Feature packages: `com.example.appname.feature.home` or `com.example.appname.home`.
- Keep public interfaces (repositories/use-cases) in `domain` to avoid leaking implementation details.
- Use `impl` or `remote`/`local` suffixes for concrete implementations (e.g., `UserRepositoryImpl`).

---

## Where to put DI (Hilt) bindings
- App-level singletons and networking: `di/network/NetworkModule.kt` installed in `SingletonComponent`.
- Feature-specific modules: inside feature package `feature/home/di/HomeModule.kt` installed in `ViewModelComponent` or `ActivityRetainedComponent` depending on scope.

Example Hilt module location and snippet:
```
com.example.appname
└─ di
   └─ NetworkModule.kt
```
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides @Singleton
  fun provideOkHttp(): OkHttpClient =
    OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
      .build()

  @Provides @Singleton
  fun provideRetrofit(ok: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl("https://api.example.com/")
      .client(ok)
      .addConverterFactory(GsonConverterFactory.create())
      .build()

  @Provides
  fun provideUserApi(retrofit: Retrofit): UserApi =
    retrofit.create(UserApi::class.java)
}
```

If the module is feature-scoped, install in `ViewModelComponent` and provide `@Provides fun provideRepo(...): FeatureRepository` without `@Singleton`.

---

## Quick mapping to your exact structure (practical advice)
Your current layout (data/di/ui) is a good starting point. A few adjustments to keep it scalable:
- Move `repository` interfaces to `domain` (or `data/repository` if you prefer) and implementations under `data/repository/impl`.
- Keep DTOs in `data/remote/dto` and mappers next to DTOs or in `data/mappers`.
- If a mapper is only used by one feature, consider housing it inside that feature package.
- Keep `di` as thin wiring layer — don't put business logic there.

Example small tree matching your environment:
```
com.example.appname
├─ data
│  ├─ dtos
│  ├─ mappers
│  ├─ remote
│  │  └─ PokemonApi.kt
│  ├─ local
│  │  └─ PersonDao.kt
│  └─ repository
├─ di
│  └─ NetworkModule.kt
└─ ui
   ├─ home
   │  ├─ HomeFragment.kt
   │  ├─ HomeViewModel.kt
   │  └─ HomeAdapter.kt
   └─ profile
```

---

## Example: small code snippets
Retrofit + OkHttp + HttpLogging interceptor (single source, can be provided by Hilt):
```kotlin
val okHttp = OkHttpClient.Builder()
  .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
  .build()

val retrofit = Retrofit.Builder()
  .baseUrl("https://pokeapi.co/api/v2/")
  .addConverterFactory(GsonConverterFactory.create())
  .client(okHttp)
  .build()

val api = retrofit.create(PokemonService::class.java)
```

Room singleton (preferred pattern for thread-safety and lifecycle):
```kotlin
@Database(...)
abstract class AppDatabase : RoomDatabase() { ... }

object DatabaseProvider {
  @Volatile private var INSTANCE: AppDatabase? = null

  fun get(context: Context): AppDatabase =
    INSTANCE ?: synchronized(this) {
      INSTANCE ?: Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app_db"
      ).build().also { INSTANCE = it }
    }
}
```

