
## What an Index Does
Why do databases use indexes?

A) To compress data
B) To speed up lookups by avoiding a full scan of the table ✓
C) To enforce foreign key constraints
D) To replicate data across nodes

> An index is a separate data structure (often a B tree) that lets the database find rows by a key without scanning the whole table. 
> The cost is extra storage and slower writes, since the index has to be kept in sync. 
> Pick indexes that match your real query patterns.


##  A hash index offers O(1) average-time lookups, yet most databases default to B-tree indexes for general-purpose columns. Why?

A) Hash indexes cannot enforce a uniqueness constraint on columns
B) Hash indexes serve only equality lookups, not ranges or sorts ✓
C) B-tree indexes always consume far less disk space to store
D) Hash indexes are never crash-safe in any production database

> A hash index maps a key to a bucket, so it answers equals queries quickly but cannot serve ranges (greater than, less than, BETWEEN) or return rows in sorted order. 
> A B-tree keeps keys ordered, so one structure handles equality, ranges, prefix matches, and ORDER BY. 
> That versatility is why it is the default.

## Database Query Bug
An app loads 100 blog posts with one query, then runs an extra query per post to fetch each author, for 101 queries total. What is this pattern called, and what is the usual fix?

A) The N+1 query problem; batch it with a join or one IN query ✓
B) A full table scan; fix it by adding a tighter LIMIT clause
C) A cartesian product; fix it by adding a WHERE join clause
D) Write amplification; fix it by dropping the extra indexes

> Fetching a list with one query and then issuing another query per item is the N+1 problem: 
> 1 query for the list plus N for the details. 
> The network round trips dominate the runtime even when each query is fast. 
> The fix is to load the related data in one shot, either with a JOIN or by collecting the ids and using a single WHERE id IN (...) query, often exposed as eager loading in an ORM.


## When an index hurts
You add five indexes to a table that receives heavy bulk inserts. Write throughput drops sharply. What is the most likely cause?

A) Every insert must also update all five indexes, adding write cost ✓
B) The indexes corrupt and then rebuild fully on every single insert
C) Indexes force the whole table into single-threaded write mode
D) The planner disables itself once a table holds over three indexes

> Each index is a separate structure the database keeps in sync. 
> On every insert, update, or delete, each affected index must also be modified, so more indexes means more write work per row. 
> On write-heavy tables this write amplification can outweigh any read benefit, which is why you index selectively rather than indexing every column.


## SQL vs NoSQL Basics
Which statement best describes a typical difference between a SQL and a NoSQL database?

A) SQL databases cannot run on Linux
B) NoSQL databases never use indexes
C) SQL databases enforce a fixed schema and support joins, while many NoSQL databases trade these for flexible schemas and easier horizontal scaling
D) NoSQL is always faster than SQL

> SQL systems like Postgres and MySQL focus on relational data, joins, and ACID transactions. 
> NoSQL is a broad family (document, key value, wide column, graph) that often relaxes these in exchange for things like flexible schemas or easier sharding. 
> Neither is universally better; it depends on the workload.


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


## B-tree vs LSM Tree
Which workload is generally a better fit for an LSM tree storage engine compared to a B tree?

A) Read heavy with many small lookups
B) Write heavy with high insert and update volume ✓
C) Workloads that need disk space efficiency above all
D) Workloads with no indexes

> LSM trees buffer writes in memory and flush them as sorted files, then merge in the background. 
> That makes writes very fast. 
> The tradeoff is that reads may have to look in several files, and you pay for compaction. 
> B trees update in place, which is great for reads but more expensive on writes.


## SQL Evaluation Order

SQL is written SELECT first but evaluated in a different order. 
The engine starts with FROM and JOIN to build the working set, 
applies WHERE to filter rows, 
groups with GROUP BY, 
filters groups with HAVING, 
then computes SELECT expressions, 
sorts with ORDER BY, 
and finally trims with LIMIT. 

> This order explains why a column alias defined in SELECT cannot be referenced in WHERE, and why LIMIT alone does not speed up an unindexed ORDER BY.
> https://www.sisense.com/blog/sql-query-order-of-operations/


## B-tree vs hash indexes
A hash index offers O(1) average-time lookups, yet most databases default to B-tree indexes for general-purpose columns. Why?

A) Hash indexes cannot enforce a uniqueness constraint on columns
B) Hash indexes serve only equality lookups, not ranges or sorts ✓
C) B-tree indexes always consume far less disk space to store
D) Hash indexes are never crash-safe in any production database

> A hash index maps a key to a bucket, so it answers equals queries quickly but cannot serve ranges (greater than, less than, BETWEEN) or return rows in sorted order. 
> A B-tree keeps keys ordered, so one structure handles equality, ranges, prefix matches, and ORDER BY. 
> That versatility is why it is the default.
> https://evgeniydemin.medium.com/postgresql-indexes-hash-vs-b-tree-84b4f6aa6d61


## Cache order

> The app always checks the cache first. 
> On a miss the cache has nothing to return, so the app falls back to the database for the real value. 
> It then stores that value in the cache so the next request for the same key is a fast hit, and finally returns the value to the caller. 
> Populating the cache after the miss is what makes future reads cheap.
> https://docs.aws.amazon.com/AmazonElastiCache/latest/dg/Strategies.html




