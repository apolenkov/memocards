# Operations Runbooks

## High 5xx Error Rate
- Check Grafana panel: Error Rate.
- Inspect recent deploy; rollback if needed.
- Review app logs in Loki for stacktraces.

## DB Connection Issues
- Verify Postgres health; container logs.
- Check pool exhaustion metrics.

## Disk Space Low
- Check volumes for logs; rotate/trim.

## Alert Ownership
- Primary: Platform owner; Secondary: On-call developer.
