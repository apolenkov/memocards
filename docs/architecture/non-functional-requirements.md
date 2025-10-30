# Non-functional Requirements

## Performance
- p95 latencies: TTFB `/decks` ≤ 400 ms, save card ≤ 200 ms.
- Concurrency: target 300 RPS sustained, 600 RPS burst.
- Limits: max deck size 10k cards; request payload ≤ 1 MB.

## Availability & Resilience
- Targets: Availability 99.9%; RTO 30 min; RPO 15 min.
- Backoff/retries for idempotent operations; circuit breaker (future).

## Scalability
- Vertical first (container limits); horizontal feasible behind Traefik.
- Stateful DB scales vertically; read replicas considered later.

## Security
- See `security.md`.

## Compliance
- OWASP Top 10 coverage; PII minimal logging; data retention policy.
