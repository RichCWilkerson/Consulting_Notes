





## Match Observability Concept
Match each observability concept to its definition.

1. Trace = The end to end record of one request flowing through multiple services
2. Log = A timestamped event message describing something that happened 
3. Span = A single unit of work inside a trace, with a start and end time
4. Metric = A numeric measurement over time, such as requests per second or queue depth 

> These are the three pillars of observability (metrics, logs, traces) plus the building block of traces. 
> Each answers a different question. 
> Metrics tell you what is happening at a high level. 
> Logs tell you what specifically happened at a moment. 
> Traces tell you where the time went across services.

## Match SLI / SLO / SLA / Error Budget
Match each reliability term to its definition.

1. SLI = The actual measurement, like p99 latency or success rate
2. Error budget = The amount of unreliability you can spend before missing the SLO
3. SLA = The contractual promise to a customer, often with penalties
4. SLO = The internal target you aim for, often slightly stricter than the SLA

> Indicators are what you measure. SLI (service level indicator)
> Objectives are the targets you aim for. SLO (service level objective)
> Agreements are external promises with consequences. SLA (service level agreement)
> Error budget is the idea that pulls them together: 
> if you have spent your budget, slow down on risky changes; 
> if you have plenty left, ship more aggressively.


## Liveness vs Readiness Probes
What is the difference between a "liveness" probe and a "readiness" probe in a system like Kubernetes?

A) They are the same thing
B) Liveness checks if the process is still alive (and restarts it if not). 
Readiness checks if it is ready to receive traffic (and removes it from the load balancer if not). ✓
C) Readiness only runs at startup
D) Liveness checks only the database

> Liveness failure means "this thing is wedged, restart it." 
> Readiness failure means "do not send it traffic right now, but do not kill it." 
> Mixing these up is a common cause of restart loops or traffic going to nodes that are not actually ready yet.


## TLS Handshake Steps
Put these steps of a basic TLS 1.2 handshake (RSA key exchange) in order.

1. Client sends ClientHello with supported cipher suites 
2. Server responds with ServerHello and its certificate 
3. Client verifies the certificate against trusted CAs 
4. Client generates a random pre master secret and encrypts it with the server's public key 
5. Both sides derive the symmetric session keys from the pre master secret 
6. Encrypted application data flows over the symmetric session

> The asymmetric handshake exists only so that both sides can safely agree on a symmetric session key. 
> After that, both sides switch to fast symmetric encryption for the actual data. 
> TLS 1.3 simplified this further with fewer round trips and no RSA key exchange, but the core idea is the same.


## Letting users upload straight to the bucket
Your web app lets users upload large video files. You do not want those files streaming through your application servers, but you also cannot make the storage bucket publicly writable. How do you let the browser upload directly to object storage in a secure way?

A) Make the bucket public so anyone can write to it
B) Have your backend hand the browser a presigned URL ✓
C) Stream every upload through your API server and then forward it to the bucket
D) Embed your permanent storage credentials in the frontend JavaScript

> A presigned URL is the standard answer. 
> Your backend, which holds the real credentials, signs a URL that grants permission to upload one specific object for a short window. 
> It hands that URL to the browser, and the browser uploads directly to the storage service without the bytes ever passing through your servers. 
> This keeps your servers out of the heavy data path while never exposing long lived credentials or opening the bucket to the world. 
> Making the bucket public invites abuse and overwrites. 
> Proxying every upload through your API works but wastes bandwidth and server resources, which is the cost you were trying to avoid. 
> Embedding permanent credentials in frontend code leaks them to every visitor.
> https://docs.aws.amazon.com/AmazonS3/latest/userguide/ShareObjectPreSignedURL.html


## Adding Nodes to a Cache Cluster
Your cache cluster maps each key to a server with hash(key) % N, where N is the server count. When you add one server, why does this scheme cause a sudden flood of cache misses?

A) The new server starts empty and must be warmed up before it helps
B) Changing N remaps almost every key ✓
C) Hash functions slow down noticeably once N grows past a threshold
D) The modulo operation overflows and scatters keys to random servers

> With hash(key) % N, the server a key lands on depends on N, so going from 4 to 5 servers reshuffles nearly every key and the cached copies are suddenly on the wrong node. 
> Consistent hashing fixes this by placing servers and keys on a ring, so adding a node only moves the keys near it (about 1/N of them). 
> That is why large caches and shard maps use consistent hashing instead of plain modulo.
> https://www.toptal.com/developers/big-data/consistent-hashing

