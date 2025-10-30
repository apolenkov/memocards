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

## Links
- Cache metrics collector: `src/main/java/org/apolenkov/application/service/stats/metrics/CacheMetricsCollector.java`
- Cache metrics logger: `src/main/java/org/apolenkov/application/config/monitoring/CacheMetricsLogger.java`
- Actuator/Prometheus: `build.gradle.kts` (dependencies), application config files
