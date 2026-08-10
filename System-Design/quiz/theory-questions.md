
## CAP Theorem Basics
In the CAP theorem, what do C, A, and P stand for?

A) Caching, Availability, Persistence
B) Consistency, Availability, Partition tolerance ✓
C) Consistency, Atomicity, Performance
D) Consensus, Asynchrony, Partitioning

> CAP says that during a network partition, a distributed system has to choose between consistency (every read sees the latest write) and availability (every request gets a response). 
> Partition tolerance is essentially a given because real networks can drop messages, so the actual choice during a partition is C or A.


## What Is Consensus
In distributed systems, what does it mean for a group of nodes to "reach consensus"?

A) Every node runs the same code
B) Every node has the same hardware
C) Every node agrees on the same value, even if some nodes fail ✓
D) Every node responds within the same time window

> Consensus is about agreement. 
> The nodes need to settle on a single value (like the next entry in a log) and stick with it, even if some nodes crash or messages are slow. 
> Algorithms like Paxos and Raft are built to make this work.





