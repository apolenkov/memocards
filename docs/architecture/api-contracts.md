# API Contracts

## Versioning
- If/when public API appears: semantic versioning, header-based or URL-based.
- Deprecation policy: announce, dual-run period, removal window (90 days).

## Idempotency
- Critical writes must be idempotent (idempotency-key or natural key).

## Errors
- Envelope:
  - `code` (machine-readable), `message` (localized safe text), `correlationId`
- Taxonomy:
  - `validation_error` (400), `not_found` (404), `conflict` (409), `unauthorized` (401), `forbidden` (403), `server_error` (500)
- Mapping: business exceptions → taxonomy above; log correlationId in MDC.

## Compatibility
- Backward-compatible changes preferred; breaking changes gated by version.

## Links
- Error handling config: `src/main/java/org/apolenkov/application/config/error/ErrorHandlingConfiguration.java`
