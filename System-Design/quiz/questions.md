
## REST vs GraphQL Tradeoff
A mobile client wants to fetch a user, their last 10 posts, and the comment counts on each post in a single round trip. Which API style fits this naturally?

A) REST, with one endpoint per resource
B) GraphQL, where the client picks exactly which fields and relations it needs ✓
C) Static file hosting
D) FTP

> REST tends to require multiple round trips when you need related resources. 
> GraphQL lets the client describe the whole shape it wants in one query, so the server returns just that. 
> This is great for mobile clients on slow networks. 
> The tradeoff is more complex caching and more server side cost.

## ACID
What does ACID stand for in databases?

A) Atomicity, Consistency, Isolation, Durability ✓
B) Availability, Caching, Indexing, Distribution
C) Async, Concurrent, Indexed, Durable
D) Atomicity, Concurrency, Isolation, Distribution

> ACID is the classic set of guarantees a transactional database tries to provide. 
> Atomicity means a transaction is all or nothing. 
> Consistency means it leaves the database in a valid state. 
> Isolation defines how concurrent transactions interact. 
> Durability means committed data survives crashes.

