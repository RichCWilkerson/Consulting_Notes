
## News Feed Fanout
A social network is choosing between fanout on write (push) and fanout on read (pull) for its news feed. Which statement is true?

A) Push always uses less storage
B) Push fans out a post to all followers' feeds at write time, which is fast at read time but expensive for users with millions of followers ✓
C) Pull means the feed is computed once and then shared with everyone
D) Push is always preferred

> Push is great for normal users because the feed is already prebuilt at read time. 
> It blows up for celebrities with huge follower counts, since one post triggers millions of writes. 
> Many real systems use a hybrid: push for normal users, pull for celebrities.


## Match Product to Dominant Constraint
Match each product to the constraint that usually dominates its design.

- Real time chat → Low latency message delivery to many clients
- Video on demand streaming → Massive bandwidth and edge caching close to users
- Online payments → Strong consistency and full auditability
- Search autocomplete → Sub 100 ms responses on every keystroke

> The dominant constraint shapes everything else. 
> Chat needs persistent connections and fast pushes. 
> Video needs CDNs and adaptive bitrates. 
> Payments cannot lose money and must be auditable. 
> Autocomplete must feel instant, which pushes you toward in memory tries or specialized search indexes.


## Chat Message Delivery Steps
Put these steps of a basic chat message delivery in order.

1. Sender's app sends the message to the chat backend
2. Backend writes the message to durable storage
3. Backend pushes the message to any online recipient devices
4. Recipient's app acknowledges receipt back to the backend
5. Backend marks the message as delivered for that recipient

> Persisting the message before pushing means a recipient who comes back online later still gets it.
> The acknowledgement closes the loop so the backend can mark the message delivered (and later, read).
> Many real chat apps add an extra "queued" state for offline recipients.


## Geo Indexing for Ride Dispatch
A ride sharing app needs to find the nearest available drivers to a rider in real time. Which technique is commonly used?

A) A full table scan of the drivers table
B) Spatial indexing such as geohashes or quad trees, plus an in memory store of currently online drivers ✓
C) Sorting all drivers by name
D) Asking each driver if they are nearby

> Spatial indexes group nearby points so that "find the closest" queries do not have to look at every driver. 
> Geohashes turn coordinates into short string prefixes that share the same prefix when they are close. 
> The hot data usually lives in memory because positions update every few seconds.




