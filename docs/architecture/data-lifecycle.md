# Data Lifecycle

## Schema Migrations
- Tooling: Flyway. Naming: `V{version}__{description}.sql`.
- Policy: One change per migration. Never modify applied migrations.

## Backups & Restore
- Backups: daily full, 7-day retention (configure in infra).
- Restore test: monthly verification of restore process.
- Objectives: RTO 30 min; RPO 15 min.

## Data Evolution
- Backward-compatible migrations preferred; index management tracked.
- Large changes: plan with maintenance window.
