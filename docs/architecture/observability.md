# Observability

## Metrics
- Source: Spring Boot Actuator `/actuator/prometheus` (scrape 15s).
- Key metrics: HTTP server latency, error rates, DB connection pool, cache hit/miss.
- Dashboards: Grafana → Memocards Overview; links from SLO doc.

## Logging
- Stack: Promtail → Loki → Grafana.
- Format: structured, MDC includes requestId and userId.
- Retention: 14 days (adjust in Loki config).

## Tracing (future)
- OpenTelemetry SDK; propagation via HTTP headers.

## Alerting
- Prometheus/Grafana rules:
  - High error rate (>1% 5m)
  - Latency p95 above SLO for 15m
  - Instance down
- Runbooks: see `operations-runbooks.md`.

## Grafana Dashboards (links)
- Overview: `https://memocards.duckdns.org/grafana/d/<overview-uid>/<overview-slug>`
- Application (HTTP/Cache): `https://memocards.duckdns.org/grafana/d/<app-uid>/<app-slug>`
- Database (PostgreSQL): `https://memocards.duckdns.org/grafana/d/<db-uid>/<db-slug>`
- Logs (Loki Explore): `https://memocards.duckdns.org/grafana/explore?left=%5B%22now-1h%22,%22now%22,%22Loki%22,%7B%7D%5D`

Replace <...-uid>/<...-slug> with real dashboard UID/slug (напишите — подставлю сразу).

## Links
- Cache metrics collector: `src/main/java/org/apolenkov/application/service/stats/metrics/CacheMetricsCollector.java`
- Cache metrics logger: `src/main/java/org/apolenkov/application/config/monitoring/CacheMetricsLogger.java`
- Actuator/Prometheus: `build.gradle.kts` (dependencies), application config files
