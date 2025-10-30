# CI/CD

## Pipeline Stages
1. Lint & static checks
2. Unit tests
3. Build image (Jib)
4. Vulnerability scan (image & deps)
5. Integration tests (Testcontainers)
6. Deploy (manual gate for prod)

## Security
- Principle of least privilege; protected secrets; signed images.

## Policies
- Mandatory green pipeline for main; conventional commits or similar.

## Quality Gates
- Unit + integration tests must pass
- Coverage ≥ 70% (adjust per module)
- OWASP Dependency Check: no HIGH/Critical vulns
- Image scan: no HIGH/Critical
- Lint: zero errors (Spotless/Checkstyle/SpotBugs)
