# Resources
[GraphQL Free API Resources](https://www.apollographql.com/blog/8-free-to-use-graphql-apis-for-your-projects-and-demos)

[Youtube walkthrough of GraphQL basics](https://www.youtube.com/watch?v=ME3LH2bib3g)

[countries API](https://www.youtube.com/redirect?event=video_description&redir_token=QUFFLUhqa1c1TURmYVpycDdKUjJ6eEN4YzVnNEZLUm4xZ3xBQ3Jtc0tsbG5aQjJNV3dsUWNlQWtTS1Flbk0xTllxOGpHQ1p5SDhjRUhNaVRHd0V1MEk4Y19SdTJIMWpWTlk2YmxiTUY4NEozU2dYakNEZUc0YnRSQ3N1ZnhLWldNcU9hbi14YXJCbmFXdDlpNEU5eHoyQ1VzRQ&q=https%3A%2F%2Fstudio.apollographql.com%2Fpublic%2Fcountries%2Fhome%3Fvariant%3Dcurrent&v=ME3LH2bib3g)

[initial source code](https://www.youtube.com/redirect?event=video_description&redir_token=QUFFLUhqazh4ME9XbEZ3NWEzbS1CRUY5YzRrbWktOXhfQXxBQ3Jtc0ttT2NScVZBOFRBNi12TTlRWXNhb0FlclRvRnlKcC1tQ1NrQTI3ZGV1YjNHMnk3OTVCZ2owQjRUclY3S25PV1ZPTjV4ek1nY3JwbmVRS3ZuOHZ1S2UwMWJZRmswM2YzZHp3SzBhaFlGcEFQUnVLR1ZYQQ&q=https%3A%2F%2Fgithub.com%2Fphilipplackner%2FGraphQlCountriesApp%2Ftree%2Finitial&v=ME3LH2bib3g)

[final source code](https://www.youtube.com/redirect?event=video_description&redir_token=QUFFLUhqbXBIWGRRTXRLMXBQcWlyUUx5aF96N3FsNVpGUXxBQ3Jtc0tuYVl6U0hqcDBNdjdjbU1xY0UzN2hiY2w0WjRNZVFYanRNbUoyMGRlQ19IZ193LW4zRDQzYkhzRzVtTk9OazF1YjB3Zl93bmxEdFdoZEhPMEh6MlE5RElCczFLMTdzaHVDSUxZY1V0QVJ5ZURVUFNVWQ&q=https%3A%2F%2Fgithub.com%2Fphilipplackner%2FGraphQlCountriesApp&v=ME3LH2bib3g)




# GraphQL Basics
**QUERY** = read only
**MUTATION** = change data
- You put this in a .graphql file under app/src/main/graphql/....
- Apollo generates a class based on the mutation name
- May need auth headers (JWT, OAuth2) via an OkHttp interceptor.
- Often return enough data to update the cache/UI (e.g., the updated entity).
- With Apollo:
  - Mutations can automatically update the normalized cache if IDs match.
  - You can do manual cache writes if needed for optimistic updates.
**SUBSCRIPTION** = real time updates (server to client push)
- subscription sets up a long-lived operation.
- It listens for favoriteCountChanged events for a given countryCode.
- Every time the server emits an event, you get countryCode and favoriteCount.
- You need:
  - Apollo configured with WebSocket support.
  - Same auth token mechanism, but passed during WebSocket connection.
- Subscriptions are useful for:
  - Chats, notifications, dashboards, counters, stock prices, etc.
- Consider:
  - Lifecycle: start/stop subscription in onStart/onStop or ViewModel scope. 
  - Network: subscriptions use WebSockets; handle reconnects and offline. 
  - Power: continuous connections drain battery; use only where real-time is needed.
- Treat them as a Flow/stream of updates in your ViewModel.
- Combine with existing StateFlow/UI state.
- Be careful with:
  - Cleaning up when the screen is closed.
  - Not overusing real-time features where polling or refresh is enough.
- Error handling
  - GraphQL responses can have both data and errors.
  - Mutations and subscriptions can fail for:
    - Network issues.
    - Auth (401/403).
    - Business rules (validation errors in errors array).
  - Map these to your domain-level error model and expose via ViewModel state.


REST Limitations:
- **Overfetching/underfetching** – REST endpoints often return fixed payloads; the client may get more data than it needs or need multiple calls to assemble one screen.
- **Multiple endpoints** – different resources live behind different URLs, so a complex screen (e.g., product + reviews + recommendations) may require several network calls.
- **Versioning issues** – changes are often handled with `/v1`, `/v2`, etc., which can fragment clients and make deprecation hard.

How GraphQL solves these issues:
- **Single endpoint** – typically `/graphql` for all queries/mutations; the *shape of the query* defines the data, not the URL.
- **Flexible queries** – the client asks for exactly the fields it needs, which reduces overfetching and underfetching.
- **Strong typing** – the schema defines types, fields, and nullability; clients get type-safe models and better tooling/auto-complete.
- **Introspection** – the schema is queryable at runtime, which enables tools like GraphiQL, Apollo Studio, and codegen.
- **Real-time updates with subscriptions** – supports server→client push (e.g., live updates) over WebSockets or similar transports.

> NOTE: GraphQL is about **how** you expose and query data, not about the underlying database or microservices. Under the hood, resolvers can still call REST, SQL, NoSQL, etc.

### REST vs GraphQL availability 
- **Are all REST APIs available as GraphQL?**
  - No. GraphQL is **opt-in**. A backend must expose a GraphQL schema and endpoint.
  - Many systems are still REST-only; some expose both REST and GraphQL; some are GraphQL-only.
- **How do we know if an API supports GraphQL?**
  - API docs will explicitly mention a GraphQL endpoint (e.g., `/graphql`) and provide a schema or a GraphQL IDE (GraphiQL, Apollo Studio, Playground).
  - You might see `.graphql`/`.graphqls` schema files or SDL in the docs.
  - If the docs only describe REST endpoints (`GET /users`, `POST /orders`), it’s REST-only.
- **Do we need to work with backend to set up GraphQL?**
  - Yes. Exposing GraphQL requires backend work:
    - Designing the schema (types, queries, mutations, subscriptions).
    - Implementing resolvers that map GraphQL fields to underlying services/DBs.
    - Securing the endpoint (auth, authorization, complexity limits, rate limiting).
  - As a mobile dev, you typically **collaborate** with backend to:
    - Shape the schema around app use cases (avoid chatty queries, model relationships well).
    - Add or adjust fields when the app needs new data.
    - Align on versioning/deprecation strategies for fields.

## Common Pitfalls
**Overly complex queries from the client**
- GraphQL makes it easy to ask for "everything"; deeply nested queries can be slow and expensive.
- Best practice: design focused queries per screen/use case; use pagination and limits.

**N\+1 query problem on the server**
- Naive resolvers can issue a DB or REST call per item (e.g., 100 products → 100 extra queries instead of 1 batched query that fetches all 100 at once).
- This happens when the resolver for a field (e.g., `product.reviews`) is implemented as "load reviews for this product" and is run once per product instead of being batched.
- Backend mitigates this with batching/data loaders (e.g., "load reviews for all these productIds in one query") and better resolver design.
- As a client dev, be aware that:
    - Very **nested** queries (`user → orders → products → reviews`) can trigger N\+1 if the backend is naive.
    - Very **large lists** (big page sizes, no pagination) make N\+1 even worse because there are more items to resolve.
    - The issue is mostly a **backend implementation** problem, but you should avoid designing queries that request huge nested graphs if the UI does not need them.

**Lack of performance/complexity limits**
- Without query depth/complexity limits, a malicious or buggy client can send huge, deeply nested queries that are expensive to resolve even if they are not repeated.
- A **complexity limit** is a server\-side rule that assigns a "cost" to fields and rejects queries whose total cost is too high.
    - **Max depth**: how many nested levels a query can have (e.g., `user → friends → friends → friends` up to 3 levels).
    - **Max cost**: each field adds some cost (e.g., `user` cost 1, `friends` cost 10 per level); the server enforces a global max cost per query.
- This is configured in the **backend GraphQL setup** (middleware, server config, or library options).
- As a client dev, the takeaway is:
    - Keep queries focused and not arbitrarily deep.
    - Expect that very complex queries might be rejected by the server with a specific error.

**Leaking internal models directly to clients**
- Treat the GraphQL schema as a **public contract**, not as a direct mirror of internal DB tables or microservice responses.
- Avoid exposing internal field names or structures that may change often.
- Use **additive changes** and `@deprecated` fields instead of breaking changes, because changing or removing fields can break all existing clients.

**Overusing GraphQL for simple use cases**
- For very simple, static endpoints (e.g., health checks, feature flags, simple POSTs with no complex relationships), REST can be simpler to implement and maintain.
- Many real systems are hybrid:
    - REST for infra/utility calls and some legacy services.
    - GraphQL for rich, client\-driven product data where overfetching/underfetching are real problems.

**Caching is different from REST**
- REST often leverages **HTTP caching** with status codes and headers (`ETag`, `Cache\-Control`, etc.).
    - GraphQL still uses HTTP and can use status codes/headers for transport\-level issues (e.g., `200` vs `401` vs `500`), but the response body is usually a single `POST /graphql` with a JSON payload that is not very cache\-friendly for standard HTTP proxies/CDNs.
- GraphQL clients (Apollo, Relay) therefore rely heavily on **client\-side normalized caches**:
    - "Normalized" means entities are stored by ID (e.g., `Country:US`) in a key\-value store on the client.
    - Any query that returns `Country:US` will read/update the same cached object, so different screens can share and update data consistently.
- As a mobile dev with Apollo, you should know at a high level how **Apollo caching and invalidation** work:
    - Apollo can use an in\-memory cache or a persistent store (e.g., SQLite via `apollo-normalized-cache-sqlite`).
    - Each query has a **fetch policy**:
        - `CacheFirst`: read cache, then network if not present.
        - `NetworkFirst`: try network, fall back to cache on failure.
        - `CacheOnly` / `NetworkOnly`: force one or the other.
    - When a mutation succeeds, Apollo can:
        - Update the cache automatically if the mutation returns entities with IDs that are already in the cache.
        - Or you can write manual cache updates in the mutation callback.
    - You control "refresh" behavior by:
        - Choosing appropriate fetch policies per screen.
        - Explicitly refetching queries when the user pulls to refresh or navigates back to a screen.
        - Optionally clearing or resetting the store on logout.

### Does one need to be careful about SQL injection with GraphQL? What other security concerns are there?
GraphQL itself does **not** eliminate SQL injection; it still depends on how resolvers access the database.
- If resolvers use parameterized queries/ORMs safely, the risk is low.
- If resolvers concatenate user input into raw SQL strings, SQL injection is still possible.
Other common GraphQL security concerns:
- **Authorization**:
    - Ensuring that only allowed users can access certain fields/types (e.g., admin\-only fields).
    - Field\-level auth on resolvers (e.g., check role/permissions before returning specific fields).
- **Query abuse / DoS**:
    - Extremely complex or deeply nested queries used to exhaust server resources.
    - Mitigated via depth/complexity limits, rate limiting, and timeouts.
- **Introspection exposure**:
    - Public introspection in production can reveal your whole schema.
    - Some teams restrict or disable introspection in prod or require auth for it.
- **Input validation**:
    - Even with strong typing, you still need validation on business rules (string length, allowed values, etc.).

### Example of backend GraphQL implementation \- high level
At a very high level, a backend GraphQL stack typically looks like this:

- **Schema definition**
    - Define types, queries, mutations, and subscriptions in SDL (Schema Definition Language).
    - Example:
        - `type Country { code: ID!, name: String!, capital: String }`
        - `type Query { country(code: ID!): Country, countries: [Country!]! }`

- **Resolvers**
    - Implement resolver functions for fields in the schema.
    - Example:
        - `Query.country` resolver takes `code` and loads a country from DB/REST.
        - `Country.continent` resolver fetches the related continent from another service.

- **Data access layer**
    - Resolvers call into repositories/services that:
        - Execute SQL/ORM queries.
        - Call existing REST APIs.
        - Combine and transform data as needed.
    - Use data loaders/batching to avoid N\+1 (e.g., "load many countries by code" instead of one DB call per code).

- **Server runtime**
    - A GraphQL server library (e.g., Apollo Server, graphql\-java, graphql\-kotlin) that:
        - Parses and validates queries against the schema.
        - Executes resolvers.
        - Applies middleware for logging, auth, and error handling.
        - Enforces depth/complexity limits and timeouts.

- **Transport / auth**
    - Expose a single HTTP endpoint (often `/graphql`).
    - Accept queries via `POST` (and sometimes `GET` for simple queries).
    - Read auth tokens (e.g., JWT) from headers and attach the user context to resolvers.
        - same as REST of using OAuth2/OIDC/Android Identity APIs for auth, store in Encrypted DataStore or Keystore, attach to headers
        - both use interceptor/OkHttp client
        - difference REST can use different endpoints with different auth requirements, GraphQL uses one endpoint so backend must handle auth/authorization per field/type
          - NOTE: main difference is how the backend handles auth


## Interview Questions
**Conceptual**
- "What problems does GraphQL solve compared to REST? When would you *not* use it?"
  > GraphQL mainly solves overfetching, underfetching, and chatty APIs. 
  > With REST you often hit multiple endpoints to build a single screen and each endpoint returns a fixed payload. 
  > GraphQL lets the client shape the response: one request, one endpoint, and only the fields you need. 
  > It also gives you a strongly typed schema and introspection, which improves tooling and makes evolving the API easier.
  
  > I would not use GraphQL for very simple, low-change APIs where a couple of REST endpoints are enough, or in infra-heavy areas like file uploads or webhooks where REST fits better. 
  > It can also be overkill if the team does not have the backend capacity to design and maintain a GraphQL schema properly.
- "Explain overfetching and underfetching. How does GraphQL address them?"
  > Overfetching is when an endpoint returns more data than the client needs, for example a full user object when the UI only shows the name. 
  > Underfetching is the opposite: one endpoint cannot provide all required data, so the client has to chain multiple calls, which increases latency. 
  
  > With GraphQL, the client specifies exactly which fields it wants in one query. 
  > Because the query is flexible, the server only sends what is requested, reducing overfetching. 
  > And because a query can traverse relationships, the client can usually assemble a full screen with a single round trip, reducing underfetching.
- "What is a GraphQL schema, and why is strong typing important?"
  > The schema is the contract between client and server. It defines types, fields, relationships, and which operations are allowed: queries, mutations, and subscriptions. 
  > It is written in the Schema Definition Language and is strongly typed, including nullability. 
  
  > Strong typing is important because it allows tooling to generate type-safe models on the client, validates queries at build or runtime, and makes API evolution safer. 
  > On Android, it means the GraphQL client can generate Kotlin models that match the schema, so most integration issues are caught at compile time rather than as runtime crashes.
- "How do queries, mutations, and subscriptions differ in GraphQL?"
  > Queries are read-only operations used to fetch data. 
  > Mutations change data on the server and usually return the updated objects so the client can update its cache. 
  > Subscriptions are long-lived operations, often over WebSockets, used for real-time updates where the server pushes data when something changes. 
  
  > In practice, I use queries for normal screen loads, mutations for user actions such as creating or updating items, and subscriptions when the UI needs live updates like chats, notifications, or dashboards.

**Mobile/Android-specific**
- "How would you integrate GraphQL into an Android app using Apollo? Where does it fit in Clean Architecture?"
  > In a Clean Architecture setup I treat Apollo as part of the data layer. 
  > I define .graphql files for my queries and mutations, let Apollo generate models, and then wrap the Apollo client in a repository implementation. 
  > That repository maps Apollo DTOs into domain models and exposes suspend functions or flows. 
  
  > The domain layer only depends on repository interfaces and domain models, so it does not know about Apollo or GraphQL. 
  > Use cases orchestrate repository calls. The presentation layer (ViewModel + Compose) calls use cases and observes UI state. 
  > If I ever switch to REST, I only replace the repository implementation, not the rest of the app.
- "How do you map generated GraphQL models (DTOs) to your domain models? Why not use them directly everywhere?"
  > I treat the generated GraphQL models as DTOs that belong to the data layer. 
  > I create extension functions or mappers that convert those DTOs into stable domain models used throughout the domain and UI layers. 
  > This keeps the rest of the app independent from GraphQL-specific concerns like nullable fields, field naming, or schema changes.
  
  > Using DTOs directly everywhere couples the app to the API contract. 
  > Any schema change, even just a nullable field or renamed property, would leak into the whole codebase. 
  > By mapping once at the boundary, I can evolve the API and the app more independently and keep business logic and UI code cleaner.
- "How do you handle errors and loading states with GraphQL responses in a ViewModel + Compose setup?"
  > In the ViewModel I wrap GraphQL calls in a coroutine and expose a state holder, usually a StateFlow with a data class that includes isLoading, data, and error. 
  > When starting the request I set isLoading to true and clear any previous error. 
  > When the response comes back, I inspect both data and errors from Apollo. 
  > I map GraphQL or network errors into a domain-level error type and update the state accordingly.
  
  > In Compose, I collect the state and branch on it: show a progress indicator when loading, show error UI or a snackbar when there is an error, and render the list or details when data is available. 
  > That keeps error and loading handling declarative and testable.
- "How would you cache GraphQL responses on mobile and control when data is refreshed?"
  > Apollo provides a normalized cache, for example backed by SQLite. 
  > On the client I enable that cache and configure fetch policies like CacheFirst, NetworkFirst, or CacheOnly depending on the screen. 
  > For list screens where data does not change frequently, I might use CacheFirst so the UI is instant and then trigger a background refresh. 
  > For highly dynamic data, I lean towards NetworkFirst but still fall back to cache when offline.
  
  > On top of that, I can add my own domain-level caching rules: for example, store timestamps in the repository or use a use case that decides whether to fetch from network based on staleness. 
  > That gives a good balance between responsiveness, offline support, and data freshness.

**Backend collaboration / design**
- "How would you work with backend teams to design GraphQL queries that fit your app’s screens?"
  > I start from the UX and screens, not from individual endpoints. 
  > For each screen I describe the data shape and interactions: which entities, which relationships, and what pagination or filters are needed. 
  > Then I work with backend to design queries and mutations that match those use cases in as few round trips as possible.
  
  > We also discuss constraints: maximum depth, expected list sizes, and performance. 
  > If a single query would be too expensive, we might split it into a main query plus smaller follow-up queries. 
  > The goal is a schema that is expressive enough for the client, but still efficient and maintainable on the server side.
- "What would you watch out for when requesting nested data to avoid performance problems on the server?"
  > Deeply nested queries and large lists are the main risk. 
  > They can trigger N+1 problems on the backend if resolvers are not batched well. 
  > As a client developer I try to keep queries focused: limit depth, always request pagination rather than unbounded lists, and avoid asking for heavy subtrees that the UI does not actually need.
  
  > If I know a particular field is expensive, I make it a separate query that loads on demand instead of bundling it into every request. 
  > I also align with backend on query complexity limits, so I do not ship a mobile client that can accidentally send queries the server will reject or struggle with.
- "How do auth and authorization typically work with GraphQL (e.g., JWT in headers, field-level auth on the server)?"
  > From the client's perspective, GraphQL usually uses the same mechanism as REST: I attach tokens, often JWTs, in the HTTP headers, such as Authorization: Bearer \<token\>. Apollo supports setting these headers via interceptors. 
  > The client does not usually handle field-level authorization directly; it just sends the token.
  
  > On the server side, middleware authenticates the request and resolvers enforce authorization. 
  > That can be at the type or field level: for example some fields only available to admin roles. 
  > If the user is not allowed, the server either omits those fields or returns GraphQL errors, which the client needs to handle gracefully, for example by hiding certain UI elements.

**Trade-offs & real-world usage**
- "Have you worked on a project that migrated from REST to GraphQL? What changed for the Android client?"
  > A typical change for the Android client is moving from multiple REST calls per screen to one or two GraphQL queries. 
  > That simplifies the data layer: fewer network calls to orchestrate, less manual merging of responses, and fewer ad-hoc DTOs. 
  > We introduce the GraphQL client, define .graphql files, and generate models instead of hand-writing them.
  
  > The biggest visible difference is on the UI side: screens load with fewer round trips and handling partial data becomes clearer because GraphQL separates data and errors. 
  > The main trade-off is an initial learning curve and a tighter collaboration loop with backend because the schema now becomes a shared contract for many clients.
- "In a large app, would you standardize on GraphQL or use both REST and GraphQL? Why?"
  > In a large app I expect a hybrid approach. 
  > For rich, client-driven data where overfetching and underfetching hurt, GraphQL works very well and I would standardize on it for core product data. 
  > For simple or infrastructure-type endpoints, like feature flags, logging, or health checks, REST is often simpler and already provided by other services.
  
  > The key is to keep the architecture clean: hide whether a feature is backed by REST or GraphQL behind repository interfaces. 
  > That way the choice of protocol is an implementation detail, and the rest of the app focuses on domain logic rather than transport concerns.


## GraphQL Free API and Practice
- the countries API has a place to practice queries
  - go to development tab on left drawer -> explore -> run queries

```kotlin
query GetCountries {
  countries {
    code
    name
    languages {
      name
    }
    continent {
      name
    }
  }
}
```



---
# Walkthrough:
- only things you need for GraphQL
1. Gradle setup
2. Schema
3. Queries
4. Data layer (Domain and UI will be same to REST)
5. DI (Apollo client and repository implementation)

## Gradle Setup
NOTE: apollo not easy to setup for gradle groovy DSL, recommend using kotlin DSL instead
- add apollo plugin to build.gradle (project level)
- add apollo plugin to build.gradle (app level)
- add apollo configuration block to app level build.gradle
- add dependencies to app level build.gradle
- add android studio plugin for graphql

```kotlin
// :app build.gradle
plugins {
    id("com.apollographql.apollo3").version("4.0.0")
}
//
apollo {
    service("countries") {
        // this is where the graphql endpoints are defined
        packageName.set("com.example.graphqlcountriesapp")
    }
}
dependencies {
    // required for apollo
    implementation("com.apollographql.apollo3:apollo-runtime:4.0.0")
    // optional - for caching
    implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:4.0.0")
}
```

## Schema
- We also need a schema
  - go to the countries API page -> click on the Schema tab (schema definition) on the left drawer -> grab the https link
  - or you can download the schema as a .graphqls file
  - or: use the CLI `./gradlew :app:downloadApolloSchema --endpoint="https://countries.trevorblades.com/" --schema="src/main/graphql/com/example/graphqlcountriesapp/schema.graphqls" --service="countries"`
    - this will generate a new schema file in the specified location

## Queries / Mutations / Subscriptions
- in the app/src/main/graphql/com/example/graphqlcountriesapp/ we will define our queries
- create a new GraphQL file (e.g. Countries.graphql)

NOTE: apollo will auto generate the necessary classes based on the queries defined in the .graphql files
- careful using them -> treat them as DTOs and map them to your domain models

```kotlin
query CountriesQuery {
    countries {
        name
        capital
        code
        emoji
    }
}
```
```kotlin
query CountryQuery($country_code: ID!) {
    country(code: $country_code) {
        code
        name
        capital
        emoji
        currency
        languages {
            name
        }
        continent {
            name
        }
    }
}
```
```kotlin
// It calls a field toggleFavoriteCountry with an input object.
// It returns the updated country with code, name, and isFavorite.
mutation ToggleFavoriteCountry($countryCode: ID!, $isFavorite: Boolean!) {
    toggleFavoriteCountry(input: { countryCode: $countryCode, isFavorite: $isFavorite }) {
    country {
        code
        name
        isFavorite
    }
}
}

```
```kotlin
subscription OnFavoriteCountChanged($countryCode: ID!) {
    favoriteCountChanged(countryCode: $countryCode) {
    countryCode
    favoriteCount
    }
}
```

## Domain layer
- create domain models
- create a repository interface
- OPTIONAL: create use cases if needed
  - if we need to filter or sort data, we can create use cases for that logic here
  - note we don't need one for country details since it's just a single item fetch

> NOTE: nothing will change in the **domain layer** if we decide to use REST instead of GraphQL – the repository interface and domain models stay the same.

> NOTE: In Philip Lackner's video he sometimes calls the concrete Apollo implementation a "client". In Clean Architecture terms:
> - **ApolloClient** = low-level HTTP/GraphQL client.
> - **Repository** = your app's abstraction over data sources (it may use Apollo, Retrofit, Room, etc.).
> - So they’re related, but not exactly the same thing.

```kotlin
// Domain models

data class SimpleCountry(
    val code: String,
    val name: String,
    val capital: String,
    val emoji: String
)

data class DetailedCountry(
    val code: String,
    val name: String,
    val capital: String,
    val emoji: String,
    val currency: String,
    val languages: List<String>,
    val continent: String,
    val isFavorite: Boolean = false
)

data class FavoriteCountUpdate(
    val countryCode: String,
    val favoriteCount: Int
)

// Repository interface – this is what the rest of the app depends on
interface ApolloCountryRepository {
    // Queries
    suspend fun getCountries(): List<SimpleCountry>
    suspend fun getCountryByCode(code: String): DetailedCountry?

    // Mutation – toggle favorite state for a country
    suspend fun updateFavoriteStatus(code: String, isFavorite: Boolean): DetailedCountry?

    // Subscription – stream real-time favorite count updates
    fun subscribeToFavoriteCountChanges(countryCode: String): Flow<FavoriteCountUpdate>
}

// OPTIONAL use case example
class GetCountriesUseCase(
    private val repository: ApolloCountryRepository
) {
    // operator keyword allows us to call the use case like a function
    suspend operator fun invoke(): List<SimpleCountry> {
        return repository
            .getCountries()
            .sortedBy { it.name } // example sorting logic
    }
}
```

## Data layer
- create mappers from DTOs (generated Apollo models) to domain models
- create an `ApolloClient`
- create a repository implementation

> NOTE: if we want to filter or sort we should use a use case in the domain layer for that logic – keep the repository implementation focused on **fetching and mapping data**.

```kotlin
// Mappers from generated GraphQL classes to domain models

fun CountriesQuery.Country.toSimpleCountry(): SimpleCountry {
    return SimpleCountry(
        code = code,
        name = name,
        capital = capital ?: "N/A",
        emoji = emoji
    )
}

fun CountryQuery.Country.toDetailedCountry(): DetailedCountry {
    return DetailedCountry(
        code = code,
        name = name,
        capital = capital ?: "No capital",
        emoji = emoji,
        currency = currency ?: "No currency",
        languages = languages.mapNotNull { it.name },
        continent = continent.name,
        isFavorite = isFavorite ?: false // assuming schema has this field
    )
}
```

```kotlin
// Apollo Repository Implementation
// If we switch to REST, we change this class and keep the interface the same.

class ApolloCountryRepositoryImpl(
    private val apolloClient: ApolloClient
) : ApolloCountryRepository {

    override suspend fun getCountries(): List<SimpleCountry> {
        val response = apolloClient
            .query(CountriesQuery())
            .execute()

        return response.data
            ?.countries
            ?.map { it.toSimpleCountry() }
            ?: emptyList()
    }

    override suspend fun getCountryByCode(code: String): DetailedCountry? {
        val response = apolloClient
            .query(CountryQuery(country_code = code))
            .execute()

        return response.data
            ?.country
            ?.toDetailedCountry()
    }

    override suspend fun updateFavoriteStatus(code: String, isFavorite: Boolean): DetailedCountry? {
        val response = apolloClient
            .mutation(ToggleFavoriteCountryMutation(countryCode = code, isFavorite = isFavorite))
            .execute()

        return response.data
            ?.toggleFavoriteCountry
            ?.country
            ?.toDetailedCountry()
    }

    override fun subscribeToFavoriteCountChanges(countryCode: String): Flow<FavoriteCountUpdate> {
        return apolloClient
            .subscription(OnFavoriteCountChangedSubscription(countryCode = countryCode))
            .toFlow()
            .map { response ->
                val data = response.data?.favoriteCountChanged
                if (data != null) {
                    FavoriteCountUpdate(
                        countryCode = data.countryCode,
                        favoriteCount = data.favoriteCount
                    )
                } else {
                    // Simple error case – in real apps you may want a sealed result type
                    throw IllegalStateException("No data from subscription")
                }
            }
    }
}
```

## UI Layer

```kotlin
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val countryRepository: ApolloCountryRepository
) : ViewModel() {

    data class CountriesState(
        val countries: List<SimpleCountry> = emptyList(),
        val isLoading: Boolean = false,
        val selectedCountry: DetailedCountry? = null,
    )

    private val _countriesState = MutableStateFlow(CountriesState())
    val countriesState: StateFlow<CountriesState> = _countriesState.asStateFlow()

    init {
        // Load initial list
        viewModelScope.launch {
            _countriesState.update { it.copy(isLoading = true) }
            val countries = getCountriesUseCase()
            _countriesState.update { it.copy(countries = countries, isLoading = false) }
        }

        // Example: subscribe to favorite count changes for a couple of countries
        viewModelScope.launch {
            countryRepository
                .subscribeToFavoriteCountChanges("US")
                .collect { update ->
                    Log.d("CountriesViewModel", "Favorite count for ${update.countryCode} is now ${update.favoriteCount}")
                }
        }
    }

    fun selectCountry(code: String) {
        viewModelScope.launch {
            val details = countryRepository.getCountryByCode(code)
            _countriesState.update { it.copy(selectedCountry = details) }
        }
    }

    fun dismissCountryDialog() {
        _countriesState.update { it.copy(selectedCountry = null) }
    }
}
```

```kotlin
@Composable
fun CountriesScreen(
    countriesState: CountriesViewModel.CountriesState,
    onCountryClick: (code: String) -> Unit,
    onDismissDialog: () -> Unit
){
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (countriesState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(countriesState.countries) { country ->
                    CountryListItem(
                        country = country,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCountryClick(country.code)
                            }
                            .padding(16.dp)
                    )
                }
            }
            if(countriesState.selectedCountry != null){
                CountryDetailDialog(
                    country = countriesState.selectedCountry,
                    onDismiss = onDismissDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White)
                        .clip(RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
private fun CountryListItem(
    country: SimpleCountry,
    modifier: Modifier = Modifier,
){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = country.emoji,
            fontSize = 32.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = country.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Capital: ${country.capital}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CountryDetailDialog(
    country: DetailedCountry,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
){
    // this will allow this to not recompute every recomposition
    val joinedLanguages = remember(country.languages) {
        country.languages.joinToString(", ")
    }

    Dialog(
        // onDismissRequest is called when the user taps outside the dialog or presses the back button
        // built into Dialog composable from androidx.compose.ui.window
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = country.emoji,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = country.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Continents: ${country.continent}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Capital: ${country.capital}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Currency: ${country.currency}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Languages: ${joinedLanguages}")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
```
```kotlin
// Activity Example
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GraphQLCountriesAppTheme {
                val viewModel = hiltViewModel<CountriesViewModel>()
                val countriesState by viewModel.countriesState.collectAsState()
                CountriesScreen(
                    countriesState = countriesState,
                    onCountryClick = { code ->
                        viewModel.selectCountry(code)
                    },
                    onDismissDialog = {
                        viewModel.dismissCountryDialog()
                    }
                )
            }
        }
    }
}
```


## DI
- Add root app class with `@HiltAndroidApp`
- Add `@AndroidEntryPoint` to activities/fragments
- Add `@HiltViewModel` to ViewModels
- Create modules for providing dependencies

> NOTE: For learning, it’s fine to hard-code the GraphQL URL and skip real auth. In a real app, you would inject the token provider and read a token from secure storage.

```kotlin
// AppModule.kt

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // Simple auth example – in a real app, inject a token provider (e.g., from Encrypted DataStore or Keystore)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()

                // If you have a token, attach it here
                // val token = tokenProvider.getAccessToken()
                // if (token != null) {
                //     requestBuilder.header("Authorization", "Bearer $token")
                // }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://countries.trevorblades.com/graphql")
            .okHttpClient(okHttpClient)
            // Optional: enable normalized cache
            // normalized cache allows caching of query results for offline support and performance
            // .normalizedCache(SqlNormalizedCacheFactory(context, "apollo.db"))
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloCountryRepository(apolloClient: ApolloClient): ApolloCountryRepository {
        return ApolloCountryRepositoryImpl(apolloClient)
    }

    @Provides
    @Singleton
    fun provideGetCountriesUseCase(repository: ApolloCountryRepository): GetCountriesUseCase {
        return GetCountriesUseCase(repository)
    }
}
```

> At this stage, the key ideas to remember as an Android dev are:
> - Queries = read, Mutations = write/change, Subscriptions = real-time updates.
> - `.graphql` files → Apollo generates Kotlin classes → you map them to your own domain models.
> - Repositories hide whether you use GraphQL or REST.
> - DI (Hilt) wires up `ApolloClient`, repository, and use cases.
> - Auth headers are usually added via an OkHttp interceptor; for learning, you can stub or skip them.
