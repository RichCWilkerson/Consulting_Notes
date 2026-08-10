

## The most important decision you'll make

Your database defines how you store, retrieve, and scale your data. It has huge implications for performance, consistency, availability, and cost.

A wrong decision here leads to every developer's least favorite task: a database migration. Moving from one database to another at scale often means rewriting queries, retraining the team, and running two systems in parallel for months while you copy billions of rows.

Pick carefully the first time. If you're not sure, read on.

![Screenshot 2026-06-05 at 11.43.36 PM.png](../System-Design-Images/Screenshot%202026-06-05%20at%2011.43.36%E2%80%AFPM.png)

In any distributed data system, you can only guarantee two of these three at the same time:

- Consistency, Every read returns the most recent write. If you save something on one node, reading from any other node sees it right away.
- Availability, Every request gets a response, even if the data might be stale.
- Partition tolerance, The system keeps operating when the network between nodes breaks or drops messages.

Partitions happen in the real world (cables get cut, switches fail, regions get isolated), so in a distributed system P is a given. That means you're really picking between C and A during a partition.

CP systems (like CockroachDB) stay consistent but may reject requests during a partition. AP systems (like Cassandra) stay available but may return stale data.

### The 6 types you should know

- Relational (SQL), Data in structured tables with rows, columns, and foreign keys. ACID-compliant, so transactions are atomic and safe. Great when correctness matters above all: banking, orders, inventory, billing. Leans CP. Examples: PostgreSQL, MySQL, SQL Server.
- Key-Value, Opaque values looked up by a unique key. No schema, minimal overhead, extremely low latency even under heavy load. Perfect for caches, session storage, shopping carts, leaderboards, and personalization. Usually AP. Examples: Redis, DynamoDB, Memcached.
- Document, Semi-structured JSON or BSON documents with flexible schema. Each record can look different, so it's fast to iterate on and natural for user profiles, content, and rich product data. Can be tuned for CP or AP. Examples: MongoDB, Firebase, Couchbase.
- Wide-Column, Tables organized as column families, essentially a giant 2D hash map. Rows can have different columns, so sparse and time-series data fits naturally. Built for very high write throughput and huge datasets. Fast by primary key, slow for ad-hoc queries. AP. Examples: Cassandra, HBase, Google Bigtable.
- Graph, Nodes and edges instead of rows. Built for traversing relationships (friends of friends, paths, cycles). Shines on social networks, fraud detection, recommendation engines, and knowledge graphs. CAP behavior varies. Examples: Neo4j, Amazon Neptune.
- Vector, Stores high-dimensional embeddings and retrieves by similarity using approximate nearest neighbor (ANN) search. The backbone of AI-era apps: semantic search, RAG for LLMs, recommendation from embeddings, image and audio similarity. Usually AP. Examples: Pinecone, Weaviate, Milvus, pgvector.

### What you give up with each pick

Every database type is optimized for something, which means it's pessimized for something else.

- Strict schema (SQL) gives you integrity and safe migrations. It also slows you down when the shape of your data is still changing. 
- Flexible schema (NoSQL) lets you move fast. It pushes data integrity into your application code, and your team has to be disciplined about it. 
- Wide-column optimizes writes. Relational optimizes joins and ad-hoc queries. Pick based on your read vs write ratio, not what's trendy. 
- Strong consistency costs latency. Coordination across nodes (quorums, leader election, sync replication) is never free. 
- Vector and graph databases are fantastic at their specialty and mediocre at everything else. Don't use them as your primary store for regular transactional data.

The good news: most real systems use more than one. A relational database for transactions, Redis for caching, Pinecone for semantic search, and so on.

### What to listen for

- "Financial transactions" or "inventory", Relational (CP).
- "High write throughput" or "time-series", Wide-column (AP).
- "Caching" or "session" or "leaderboard", Key-Value (AP).
- "Flexible profile data" or "content", Document.
- "Social graph" or "recommendations", Graph.
- "Semantic search" or "embeddings" or "RAG", Vector.

Always justify your choice with CAP. "I'd pick Cassandra here because writes dominate, we need linear scale, and we can tolerate eventual consistency on reads." That one sentence is worth more than naming the right database.

### Questions

#### During a network partition in a distributed database, you must pick between which two properties?

1. Consistency and Performance 
2. Consistency and Availability ✓
3. Durability and Speed 
4. Replication and Sharding

> Partition tolerance is a given in any distributed system, so during a partition you choose between C (refuse stale reads) and A (serve them anyway).

#### Which database type is the safest pick for an e-commerce checkout system?

1. Wide-column (Cassandra), since checkout needs the highest possible write throughput 
2. Vector, since item embeddings drive most product recommendations on the page 
3. Relational (CP), for ACID transactions on inventory and payment ✓
4. Graph (Neo4j), since orders are naturally a graph of users, products, and shipping

> Checkout demands strict consistency and atomicity across inventory, payment, and order rows. 
> SQL/relational with ACID transactions is the right pick.

#### When does a wide-column database (e.g., Cassandra) outperform a relational one?

1. When you need many-table joins with foreign-key constraints enforced by the database 
2. When the workload is dominated by ad-hoc analytical queries against historical data 
3. When the workload is dominated by very high write volume keyed by primary key ✓
4. When you need strict foreign keys, cascading deletes, and serializable transactions

> Wide-column stores like Cassandra optimize for huge write throughput by primary key. 
> They are weaker on joins and ad-hoc queries.