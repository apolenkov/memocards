# Data Lifecycle

## Schema Migrations
- Tooling: Flyway. Naming: `V{version}__{description}.sql`.
- Policy: One change per migration. Never modify applied migrations.

## Backups & Restore
- Backups: daily full, 7-day retention (configure in infra).
- Restore test: monthly verification of restore process.
- Objectives: RTO 30 min; RPO 15 min.

### Restore Steps (compose)
1. Stop app access (maintenance)
2. Create fresh Postgres container with restore volume
3. Restore dump into target DB
4. Point app to restored DB or swap volumes
5. Verify smoke tests; reopen traffic

## Data Evolution
- Backward-compatible migrations preferred; index management tracked.
- Large changes: plan with maintenance window.

## Links
- Flyway SQL: [db/migration](../../src/main/resources/db/migration/)
- JDBC adapters: [`*JdbcAdapter`](../../src/main/java/org/apolenkov/application/infrastructure/repository/jdbc/adapter/)
