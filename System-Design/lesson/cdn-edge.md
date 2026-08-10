

## A cache, geographically distributed

![Screenshot 2026-06-27 at 6.03.44 PM.png](../System-Design-Images/Screenshot%202026-06-27%20at%206.03.44%E2%80%AFPM.png)

A CDN is a network of caches placed close to users worldwide. When the user requests an asset, they hit the nearest edge node. If it's cached, the response comes back in milliseconds without ever touching your origin.

Originally for static assets (images, JS, CSS, video), but modern CDNs (Cloudflare, Fastly, CloudFront) also cache HTML, run JavaScript at the edge, and proxy API responses. It is the cheapest performance win in any architecture. If you are not using one, you should be.

## Push vs pull, and the cache key

- **Pull (lazy)**, The CDN fetches from origin on the first miss for any new URL, then caches per the TTL on the response. The default for almost everything; you change nothing on your side beyond setting the right Cache-Control headers. 
- **Push (eager)**, You upload to the CDN directly, the content is everywhere before the first request. Useful for very large or rarely-accessed files where a cold-miss is unacceptable. 
- **Cache key**, The function of URL plus relevant headers (Vary, Cookie, Accept-Encoding) that identifies a cached entry. Get this wrong and either you cache too aggressively (one user sees another's content) or not at all (zero hit rate).

## What you cache, and where

- **Static assets**, Hashed filenames (app.a1b2c3.js) plus Cache-Control: public, max-age=31536000, immutable. The browser and CDN cache forever; the URL changes when the content changes. 
- **HTML**, Short TTL (5-60 seconds) plus stale-while-revalidate so the user never waits on origin. Combine with cache tags (surrogate-keys) for surgical invalidation. 
- **API responses**, Cache GETs by URL plus auth scope. Fastly Varnish-style configs and Cloudflare Workers KV are common targets. Be very deliberate about cache keys for any authenticated response. 
- **Edge compute**, Run logic at the edge (auth checks, A/B test routing, response shaping) so the user gets a response in the same hop, with no origin trip at all. Cloudflare Workers, Vercel Edge Functions, Lambda@Edge.

## What CDNs cost you

Invalidation is hard. Purges propagate over seconds-to-minutes; expect stale content during a deploy. Fight this with versioned URLs, not purges.

Cache poisoning. If the cache key forgets a relevant header (Authorization, Cookie, Accept-Language), one user can see another user's content. Test cache keys explicitly in CI.

Edge compute introduces new failure modes. A bad worker deploy can take down every user worldwide in seconds. Roll out gradually and have a kill switch.

Egress is metered. A traffic spike helps you (the CDN absorbs it). A buggy cache config that misses 100% of the time bills you for everything plus origin compute.

Geo-restrictions, GDPR, and data residency complicate edge compute. Some data is not allowed to leave a region.

## What to listen for

- **"Static site" or "media-heavy app"**, CDN with immutable URLs and Cache-Control: immutable. Mention versioned filenames.
- **"Global users"**, Multi-region origin plus CDN. Mention regional failover and how the CDN routes to the nearest healthy origin.
- **"Logged-in user content"**, Cache key includes auth scope, OR private cache (Cache-Control: private), OR do not cache, depending on freshness vs latency.
- **"Live video" or "VOD streaming"**, CDN delivery with adaptive bitrate (HLS or DASH) and edge transcoding for thumbnails or alt streams.

Always state your cache TTL and your invalidation strategy in one breath. "5-minute TTL with stale-while-revalidate, plus surrogate-key purge on write" is a complete answer.

## Questions

### A CDN primarily delivers value by...

1) Encrypting all traffic between client and origin so the origin can skip TLS termination 
2) Caching content at edge nodes geographically close to users ✓
3) Generating dynamic HTML server-side at the edge to skip the origin render entirely 
4) Replacing the application server entirely so requests never need an origin to fall back on

> CDNs are caches placed close to users. 
> Hits return in milliseconds without ever touching the origin.

### For static JS bundles, the right Cache-Control approach is...

1) max-age=0 with must-revalidate, so the browser always checks origin before serving 
2) no-store, since JavaScript changes on every deploy and stale code can break sessions 
3) Hashed filenames with max-age=1 year, immutable ✓
4) max-age=60 across all bundles, since users notice stale code within a minute or two

> Hash the filename (app.a1b2c3.js) so any change generates a new URL, then cache the asset effectively forever with `immutable`.

### When does cache poisoning typically happen?

1) The CDN runs out of disk space and starts evicting recent entries on every request 
2) An attacker corrupts data at the origin and pushes the bad payload upstream into edge caches 
3) The cache key forgets a relevant header (e.g., Authorization), so one user sees another's response ✓
4) The TTL is too short, so the edge constantly refetches and accepts whatever origin returns

> A bad cache key is the classic poisoning vulnerability. 
> If the auth scope is not part of the key, private data leaks across users.


