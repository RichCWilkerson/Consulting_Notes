

## Knowing why your system did what it did

Observability is what lets you answer "why is this user's request slow?" without redeploying with extra println.

Three pillars: 
- logs (discrete events)
- metrics (numerical aggregates), 
- traces (causally linked spans across services). 
Modern systems treat them as one combined signal tied together by a common request id.

The goal is not dashboards. It is reducing time-to-detect and time-to-resolve when something breaks, so the team can ship faster with confidence.

## The three pillars

- **Logs**, Structured (JSON), high cardinality, expensive to keep at full fidelity. Sample aggressively for non-error paths. The signal-to-noise ratio is usually worse than teams think. 
- **Metrics**, Pre-aggregated counters, gauges, histograms. Cheap, query-fast. Use them for SLOs, dashboards, and alerting. 
- **Traces**, A request's path across services with timing per span. Find the slow hop in a 50-service request in seconds. 
- **Common id**, Tie them together with one request_id (trace_id) flowing from gateway to every backend log line and span. Without it, you have three siloed signals instead of one.

## Stacks you will see

- **Self-hosted**, Prometheus + Grafana + Loki + Tempo, or the ELK stack (Elasticsearch, Logstash, Kibana). Cheap at small scale, ops-heavy at large.
- **SaaS**, Datadog, Honeycomb, New Relic, Splunk. Pay for ease and integrated UX. Bills can balloon at high cardinality, sometimes faster than your traffic grows.
- **OpenTelemetry**, The vendor-neutral instrumentation standard. Pick OTel libraries and you can swap backends without changing application code.
- **Error tracking**, Sentry, Bugsnag. Same idea as logs, optimized for stack traces, user impact, and grouping similar errors.

## What observability costs

Cardinality explodes cost. A user_id label on a metric with a million users is a million time series. Be ruthless about which labels you allow.

Sampling drops data. 1% sampling might miss the rare bug you are paged on. Use head sampling for normal traffic and tail sampling so error traces always come through.

Logs are the worst signal-to-noise. Every team thinks their logs are critical. They are wrong about half of them.

Vendor lock-in is real. Each SaaS has its own query language, dashboard format, and alert syntax. Migrating off Datadog is a quarter of someone's life.

SLO and SLA discipline is a culture problem more than a tools problem. Without it, dashboards become wallpaper.

## What to listen for

- **"Why is this slow?"**, Distributed tracing with a trace_id propagated end-to-end. Mention OpenTelemetry.
- **"How do you know it is broken?"**, SLOs on the four golden signals (latency, traffic, errors, saturation). Alert on burn rate, not just static thresholds.
- **"User reports a bug"**, Error tracking (Sentry) with breadcrumbs, plus session replay if it is a frontend issue.
- **"Cost is too high"**, Sampling strategies, dropping low-value labels, tiered hot/warm/cold log retention.

Always say what gets propagated and what gets aggregated. "Every request gets a trace_id at the gateway, propagated as a header, attached to every log line and span. Aggregated metrics live in Prometheus; raw logs in Loki for 14 days." That is a complete answer.

## Questions

### Which observability pillar is best for "find the slow hop in this request across 50 services"?

1) Logs 
2) Metrics 
3) Distributed traces ✓
4) Heap dumps

> Distributed traces show the full path of a request across services with per-span timing. 
> That is exactly the "where is the slow hop?" question.

### What ties logs, metrics, and traces together for a single request?

1) The HTTP status code, since one final response code summarizes the outcome of the request 
2) A common request_id (trace_id) propagated end-to-end ✓
3) The user's email, attached as a label on every log line and span emitted by the backend 
4) The wall-clock time of the request, since timestamps align signals from every service

> One id (request_id / trace_id) flowing from gateway to every backend log line and span is what unifies the three pillars.

### What is the main cost trap in observability platforms?

1) Disk space at the storage tier, since cold log archives grow faster than retention budgets 
2) High-cardinality labels (e.g., user_id) explode the time-series count and the bill ✓
3) The vendor SDK size, which inflates application binary size and slows down deploys 
4) Network bandwidth between the app and the collector, since every span ships over the wire

> Cardinality is the silent killer. 
> A user_id label on a metric with 1M users = 1M time series. 
> Be ruthless about which labels you allow.

