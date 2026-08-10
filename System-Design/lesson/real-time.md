

## Pushing instead of pulling

Real-time means the server pushes data to the client as soon as it's available. Chat, live scores, presence indicators, collaborative editing, live dashboards, multiplayer games.

The alternative is polling, where the client asks "anything new?" on a timer. Polling is fine for low-frequency updates and dead simple to build, but it falls apart when latency or efficiency matter at scale.

The real protocols for push are short polling, long polling, server-sent events (SSE), and WebSockets. Picking the right one depends on whether you need bidirectional traffic and how chatty the channel is.

## The four protocols

![Screenshot 2026-06-27 at 5.20.01 PM.png](../System-Design-Images/Screenshot%202026-06-27%20at%205.20.01%E2%80%AFPM.png)

- **Short polling**, Client requests every N seconds. Wastes bandwidth and battery, but works through any proxy and is trivial to implement. Fine for status pages and slow-moving dashboards. 
- **Long polling**, Server holds the request open until data arrives or it times out, then the client immediately reconnects. Cheap, reliable, traverses corporate proxies cleanly. The fallback when WebSockets are blocked. 
- **Server-sent events (SSE)**, One-way HTTP stream from server to client. Auto-reconnects with built-in event ids. Limited to text, but native browser support and simpler than WebSockets when you only need server-to-client. 
- **WebSockets**, Bidirectional, persistent TCP connection upgraded from HTTP. Full-duplex, binary-safe, the default when both directions matter.

## Fan-out patterns

- **Direct delivery**, Server pushes to a connected client by user_id. Works for chat DMs, notifications, and personal alerts.
- **Pub/sub topic**, Clients subscribe to a topic, the server publishes once and the broker fans out to every subscriber (Redis Pub/Sub, NATS, Kafka). The default for group chats, live channels, and broadcast updates.
- **Sticky vs nonsticky**, WebSockets need the load balancer to keep a client on the same server (sticky sessions), or you need a shared pub/sub layer so any server can publish to any connected client. Nonsticky scales better but costs an extra hop.
- **Presence + typing**, Track presence in Redis with a short TTL refreshed by the client every few seconds. Don't write presence to your primary database; it changes too often.

## What real-time costs

Connections are stateful. A million idle users is still a million connection objects holding RAM on a server. Plan for capacity per server (~50k WebSockets is a common ceiling).

Scaling horizontally requires a pub/sub backbone so any node can deliver to any client. Now you have two systems to monitor (your app and the pub/sub layer).

Mobile networks drop connections constantly. The client must reconnect with backoff and replay missed events from a sequence number, or you get gaps users will notice.

Auth is harder. Tokens expire mid-connection. Plan for a re-auth mechanism that does not drop the socket.

Backpressure: a slow client can fill the server's outgoing buffer. Either drop messages, queue with a cap, or kick the client.

## What to listen for

- **"Chat" or "messaging"**, WebSockets plus a pub/sub backbone (Redis or Kafka) for fanout. Presence in Redis with TTL. Persist messages to a database for history.
- **"Live feed" or "stock ticker"**, SSE is enough. One-way, simpler than WebSockets, native browser support.
- **"Multiplayer game" or "collaborative document"**, WebSockets, sticky sessions, server holds short-lived authoritative state. Operational transforms or CRDTs for concurrent edits.
- **"Push notifications"**, SSE or WebSockets, batched on the wire so a busy user gets one envelope, not 50 separate frames.

Always describe how you scale connections horizontally. "Each app server handles ~50k WebSockets; we publish to Redis, every server subscribes, so any user on any server gets the message." That sentence covers the bulk of the design.

## Questions

### What is the key advantage of WebSockets over Server-Sent Events (SSE)?

1) WebSockets work in older browsers, while SSE requires modern HTTP/2 support 
2) WebSockets support bidirectional communication; SSE is server-to-client only ✓
3) WebSockets handle requests synchronously, while SSE responses always arrive asynchronously 
4) WebSockets are encrypted by default in the protocol, while SSE traffic ships in plain text

> WebSockets are full-duplex over a persistent TCP connection. 
> SSE is server-to-client only.

### Which protocol is sufficient for a one-way live stock ticker in the browser?

1) Short polling 
2) Server-Sent Events (SSE) ✓
3) Long polling with custom backoff 
4) gRPC streaming

> SSE is purpose-built for one-way server-to-client streams, with auto-reconnect and event ids. 
> Simpler than WebSockets when you do not need both directions.

### What is the main scaling cost of a WebSocket-based system?

1) Connections are stateful, so each idle user holds RAM on a specific server ✓
2) Browsers limit you to one open WebSocket per origin, so traffic per user is capped 
3) WebSockets cannot be load-balanced because the upgrade handshake breaks L7 routing 
4) The protocol is text-only, so binary payloads must be base64-encoded on every frame

> Connections persist. 
> A million idle users is a million connection objects in memory across the fleet, requiring careful capacity planning per server.

