# ADR-0002: Nginx + Traefik Hybrid Proxy Strategy

## Status
Accepted

## Context
- Need stable SSL termination and security headers.
- Need dynamic discovery/routing for containers without Nginx reloads.

## Decision
- Nginx as edge proxy (SSL, headers), Traefik as internal router.

## Consequences
- Stability + automation. Single proxy-pass from Nginx to Traefik.
- Traefik labels control routing; easier service onboarding.
