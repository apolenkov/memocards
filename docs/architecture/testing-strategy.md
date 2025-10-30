# Testing Strategy

## Pyramid
- Unit tests (fast, isolated)
- Contract tests (if external APIs)
- Integration (Spring + Testcontainers, single reusable PostgreSQL)
- E2E (minimal critical paths)

## Data
- Deterministic fixtures; rollback via @Transactional.

## Performance
- Smoke perf checks on critical flows; budget regression alerts.
