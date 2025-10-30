# Security & Privacy

## Threat Model
- Relevant OWASP Top 10: A01 Broken Access Control, A03 Injection, A07 Identification and Authentication Failures.

## Authentication & Sessions
- Spring Security with session; cookies: Secure, HttpOnly, SameSite=Lax.
- Passwords: BCrypt.

## Authorization
- UI routes annotated; service-level checks; do not rely on visibility.

## Secrets Management
- No secrets in code; loaded via env/profiles (@Value). Rotation policy: quarterly or on incident.

## Headers & Policies
- HSTS, CSP (default-src 'self'), X-Content-Type-Options, X-Frame-Options.

## Data Protection
- PII minimal in logs; redaction for emails/user ids when required.
- Data retention: see [data-lifecycle.md](./data-lifecycle.md).

## Security Checklist (pre-prod)
- TLS: valid cert; HSTS enabled; strong ciphers
- Cookies: Secure, HttpOnly, SameSite set
- AuthZ: roles on routes and service methods verified
- SQL: parameterized queries only (JDBC)
- Secrets: sourced via env/@Value; no secrets in repo
- Dependencies: OWASP dep check clean; image scan clean
- CSP: default-src 'self' (tighten as needed)
- Logs: no PII; requestId/userId in MDC

## Links
- Security config: [SecurityConfig.java](../../src/main/java/org/apolenkov/application/config/security/SecurityConfig.java)
- Audit aspect: [SecurityAuditAspect.java](../../src/main/java/org/apolenkov/application/config/security/SecurityAuditAspect.java)
