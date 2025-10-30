# SLO / SLI / SLA

## Scope
- Product: Memocards application (user-facing) and supporting services.
- Ownership: Platform owner (Ops) + Product owner (App).

## SLI and Targets
| Area | SLI | Target (SLO) | Measurement | Source |
|------|-----|---------------|------------|--------|
| Availability | Uptime (30d) | 99.9% | Blackbox HTTP probe | Prometheus + Grafana |
| Latency | p95 TTFB `/decks` | ≤ 400 ms | Histogram | Prometheus (Actuator) |
| Errors | 5xx rate | ≤ 0.5% | Rate over 5m | Prometheus |
| DB | p95 query time | ≤ 50 ms | Exporter/Actuator | Prometheus |
| Logging | Ingest delay | ≤ 5 s | Push to Loki | Promtail/Loki |

## SLA (external)
- If applicable; otherwise “N/A”.

## Dashboards & Alerts
- Dashboards: Grafana → “Memocards Overview”.
- Alerting rules: Prometheus alertmanager (future) or Grafana alerts.

## Review Cadence
- Quarterly SLO review and thresholds adjustment based on usage.
