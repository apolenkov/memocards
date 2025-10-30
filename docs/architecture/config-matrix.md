# Configuration Matrix

| Key | Default | Profile | Owner | Notes |
|-----|---------|---------|-------|-------|
| app.cache.user.ttl-minutes | 30 | all | App | Caffeine user cache TTL |
| app.cache.user.max-size | 1000 | all | App | Caffeine user cache size |
| app.cache.known-cards.ttl-ms | 300000 | all | App | Session cache TTL |
| app.cache.known-cards.max-size | 1000 | all | App | Session cache size |
| app.ui.search.debounceMs | 300 | all | App | Debounce for search inputs |
| spring.threads.virtual.enabled | true | prod | App | Virtual threads (SB 3.2+) |
| management.endpoints.web.exposure.include | health,info,prometheus | all | App | Actuator exposure |
