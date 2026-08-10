

## Webhook Reliability
When designing a webhook system that sends events to third party servers, what is the most important reliability practice on the consumer side?

A) Trust the first delivery and ignore duplicates
B) Treat webhook handlers as idempotent and dedupe by the delivery id ✓
C) Refuse to use HTTPS
D) Poll the producer instead of trusting the webhook

> Webhooks may be retried after timeouts or crashes, so the same event can arrive more than once. 
> Idempotent handlers, often keyed by the event id, make sure that processing the same delivery twice has no extra effect. 
> The producer side complement is exponential backoff with a sane retry cap.


## Cursor vs Offset Pagination
Why is cursor based pagination usually preferred over offset based pagination for very large lists?

A) Offset pagination breaks if the table has indexes
B) Cursor pagination stays stable as items are inserted or deleted, and avoids slow "OFFSET 1000000" scans ✓
C) Cursor pagination always returns more items per page
D) Offset pagination cannot return JSON

> With offsets, deep pages get slow because the database still has to walk past all the skipped rows, and the results shift if rows are inserted or removed. 
> Cursors carry an opaque pointer (often the id or timestamp of the last seen row), so the server can jump straight to the next slice and stay stable under churn.


## Match Auth Method to Characteristic
Match each authentication method to its core characteristic

- Session cookie tied to a server side store → Easy to revoke; every request needs a server side lookup 
- JWT → Self contained signed payload; harder to revoke before it expires 
- OAuth 2.0 access token → Issued by an authorization server after a user grants scopes to a third party app 
- API key → Long lived static secret, usually used for server to server access

> Each method fits a different setting. 
> Session cookies are great for first party web apps with a backend. 
> JWTs scale because they need no lookup, but you trade revocation. 
> OAuth is the standard for third party access on behalf of a user. 
> API keys are the simplest way to identify a calling system, not a person.




