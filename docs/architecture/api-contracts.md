# API Contracts

## Versioning
- If/when public API appears: semantic versioning, header-based or URL-based.
- Deprecation policy: announce, dual-run period, removal window (90 days).

## Idempotency
- Critical writes must be idempotent (idempotency-key or natural key).

## Errors
- Consistent error envelope with code, message, correlationId.

## Compatibility
- Backward-compatible changes preferred; breaking changes gated by version.
