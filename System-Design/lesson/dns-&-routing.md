

## Names to addresses

![Screenshot 2026-06-28 at 7.08.26 PM.png](../System-Design-Images/Screenshot%202026-06-28%20at%207.08.26%E2%80%AFPM.png)

DNS turns a human name like api.example.com into an IP address your machine can connect to. Every request starts here. If DNS is down or slow, nothing else matters.

Modern DNS does much more than name lookup. It steers traffic to the nearest region, fails over between healthy origins, splits traffic for canaries, and gates load via weighted records. It is the first routing layer in any global system.

## How a lookup actually flows

- **Recursive resolver**, Your client's first hop (usually your ISP, 1.1.1.1, or 8.8.8.8). It does the work of walking the DNS tree and hands back an answer. 
- **Root servers**, Tell the resolver which servers handle .com, .org, .io. There are 13 logical roots, anycasted worldwide. 
- **TLD servers**, For .com, point the resolver to the authoritative nameserver for example.com. 
- **Authoritative nameserver**, Owns the actual records for your domain. Route 53, Cloudflare, NS1. Returns A, AAAA, CNAME, MX, TXT entries. 
- **TTL**, Every record has a time-to-live. Resolvers cache the answer for that long. Short TTLs mean fast failover; long TTLs mean fewer lookups and slower change.

## Routing policies

- **Simple**, One record, one address. The default for small services. 
- **Weighted**, Split traffic by percentage across multiple endpoints. Used for blue/green and canary deploys at the DNS layer. 
- **Geo-location**, Return a different IP based on where the user is. EU users hit Frankfurt, US users hit Virginia. 
- **Latency-based**, Send each user to the region with the lowest measured latency from their resolver. The right default for global apps. 
- **Failover (health-checked)**, Primary endpoint answers when healthy. If health checks fail, the resolver switches to the backup. RTO is bounded by TTL plus check interval. 
- **Anycast**, Many physical locations share one IP. The network routes the client to the closest one. The backbone of CDNs and public DNS.

## What DNS costs you

TTLs are global. Lower them before a planned change, raise them after. Forget to lower the TTL and your "5 minute" failover becomes "5 hour".

Resolvers ignore your TTL sometimes (corporate caches, mobile carriers, broken middleboxes). Test your failover under conditions you cannot fully control.

Geo-routing keys on the resolver's IP, not the user's. Users behind a far-away resolver (corporate VPN, public DNS) can land in the wrong region.

DNS amplification is a classic DDoS vector. Lock down recursion, use DNSSEC where appropriate, and watch for poisoning.

Propagation delays are measured in minutes-to-hours. If you need second-level routing, do it at an L7 balancer behind a stable DNS name, not in DNS itself.

## What to listen for

- **"Global users"**, Latency-based or geo routing, anycast in front, regional ALBs behind the names.
- **"Failover" or "region goes dark"**, Health-checked DNS records with a short TTL, plus L7 failover at the app tier for faster reaction.
- **"Canary rollout"**, Weighted records (e.g., 95/5) or weighted target groups on the LB. Mention TTL implications.
- **"Custom subdomains per tenant"**, Wildcard A/CNAME records plus dynamic certificate issuance (Let's Encrypt, ACM).

Always name the TTL when you propose a DNS change. "Drop TTL to 60 seconds the day before, flip the record, raise it back to an hour" is the operationally aware answer.

## Questions

### What does the TTL on a DNS record control?

1) The total lifetime of the domain registration before it must be renewed at the registrar 
2) How long resolvers cache the answer before asking again ✓
3) The encryption strength of the response between the resolver and the authoritative server 
4) Whether the record is authoritative or has been served by a caching intermediary

> TTL is the cache lifetime at the resolver. 
> Lower TTL means faster failover and more lookups; 
> higher TTL means fewer lookups and slower change.

### You need users in Europe to land in your Frankfurt region and US users to land in Virginia. The cleanest fit is...

1) Use a single IP behind a CDN and let the CDN route silently based on the request hostname 
2) Latency-based or geo-location DNS routing ✓
3) Anycast routing only, with a single record that points at one global IP across regions 
4) Round-robin DNS with both region IPs in the same record and the client picking at random

> Geo or latency routing returns a different IP based on where the resolver is. 
> That sends each region of users to the right origin.

### Why is DNS-level failover usually measured in minutes, not seconds?

1) Authoritative nameservers respond slowly because every query walks the full delegation chain 
2) Resolvers and intermediate caches respect (or sometimes exceed) the record's TTL ✓
3) DNS is single-threaded in most resolvers, so global updates serialize through one queue 
4) DNSSEC validation is slow when a record changes, since every signature has to be recomputed

> A failover propagates as caches expire. 
> TTLs plus resolver behavior define how long stale answers linger after you flip the record.

