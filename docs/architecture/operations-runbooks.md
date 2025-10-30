# Operations Runbooks

## High 5xx Error Rate
- Check Grafana panel: Error Rate.
- Inspect recent deploy; rollback if needed.
- Review app logs in Loki for stacktraces.

Commands:
- `docker compose logs app --since=10m`
- `docker compose ps`

## High Latency (p95)
- Check Prometheus panels (HTTP latency, DB time)
- Verify DB health and slow queries
- Check cache hit/miss metrics

Commands:
- `docker compose logs app --since=10m | grep -i warn`
- `docker compose logs postgres --since=10m`

## DB Connection Issues
- Verify Postgres health; container logs.
- Check pool exhaustion metrics.

Commands:
- `docker compose logs postgres --since=10m`
- `psql -h localhost -p 5432 -U <user> -d <db> -c "select 1"`

## Prometheus Down
- Check container status; validate config paths
- Verify scrape targets reachable

Commands:
- `docker compose logs prometheus --since=10m`
- `curl -s http://localhost:9090/prometheus/-/healthy`

## Loki Ingest Delay
- Check promtail connectivity and positions
- Validate Loki readiness

Commands:
- `docker compose logs promtail --since=10m`
- `docker compose logs loki --since=10m`

## Disk Space Low
- Check volumes for logs; rotate/trim.

Commands:
- `du -sh ./logs`
- `find ./logs -type f -mtime +7 -delete`

## Alert Ownership
- Primary: Platform owner; Secondary: On-call developer.
