# ADR-0001: Adopt Vaadin 24 and Spring Data JDBC

## Status
Accepted

## Context
- Server-side UI with strong Java ergonomics desired.
- Prefer explicit SQL control over JPA for performance predictability.

## Decision
- Use Vaadin 24 for UI (server push, i18n, Lumo tokens).
- Use Spring Data JDBC with explicit SQL and RowMappers.

## Consequences
- Clear layering; predictable queries; simpler mental model vs JPA.
- Server-side UI reduces client JS complexity; PWA support remains.
