# Operations Runbooks

## High 5xx Error Rate
- Check Grafana panel: Error Rate.
- Inspect recent deploy; rollback if needed.
- Review app logs in Loki for stacktraces.

Commands:
- `docker compose logs app --since=10m`
- `docker compose ps`

## DB Connection Issues
- Verify Postgres health; container logs.
- Check pool exhaustion metrics.

Commands:
- `docker compose logs postgres --since=10m`
- `psql -h localhost -p 5432 -U <user> -d <db> -c "select 1"`

## Disk Space Low
- Check volumes for logs; rotate/trim.

Commands:
- `du -sh ./logs`  `find ./logs -type f -mtime +7 -delete`

## Alert Ownership
- Primary: Platform owner; Secondary: On-call developer.
