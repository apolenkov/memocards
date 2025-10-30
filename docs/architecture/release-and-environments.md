# Release & Environments

## Environments
| Env | Purpose | Differences |
|-----|---------|-------------|
| local | Dev on laptop | Direct ports, hot reload |
| dev | Team testing | Public behind Nginx+Traefik |
| prod | Production | Monitoring, stricter limits |

## Release Flow
- Build with Jib → push image → deploy via compose/ansible.
- Tags: semantic app version; image tags immutable.

## Rollback
- Keep N-2 images; rollback via compose to previous tag.

## Feature Flags
- (Future) Externalize flags; document ownership and blast radius.
