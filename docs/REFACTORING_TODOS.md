# Refactoring Follow-up Tasks

Список задач после масштабного рефакторинга (Batch операции + Кэширование).

---

## 🔴 КРИТИЧНО (Priority 1)

### ISSUE-001: Добавить индекс на user_roles(user_id)
**Статус**: ✅ DONE (миграция V5 создана)  
**Описание**: Отсутствует индекс на user_roles(user_id), что вызывает Seq Scan при JOIN в запросе SELECT_USER_WITH_ROLES_BY_EMAIL.  
**Решение**: Миграция V5__add_user_roles_user_id_index.sql уже создана.  
**Проверка**:
```sql
EXPLAIN ANALYZE
SELECT u.*, ARRAY_AGG(ur.role)
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
WHERE u.email = 'test@test.com'
GROUP BY u.id;
-- Должен быть Index Scan, НЕ Seq Scan
```

---

### ISSUE-002: Исправить cache invalidation для email change
**Статус**: ❌ TODO  
**Приоритет**: P1 - CRITICAL  
**Описание**: При изменении email пользователя старый email остается в кэше.

**Проблема**:
```java
@CacheEvict(value = "usersByEmail", key = "#user.email")
public User save(final User user) { ... }
// Evict происходит для НОВОГО email, старый остается в кэше!
```

**Решение**:
```java
@CacheEvict(value = "usersByEmail", allEntries = true)
public User save(final User user) {
    // Evict весь кэш - безопаснее для user updates
}
```

**Файл**: `src/main/java/org/apolenkov/application/infrastructure/repository/jdbc/adapter/UserJdbcAdapter.java`  
**Строка**: ~208

**Альтернативное решение** (более сложное, но эффективнее):
```java
@Caching(evict = {
    @CacheEvict(value = "usersByEmail", key = "#user.email"),
    @CacheEvict(value = "usersByEmail", key = "#oldEmail")
})
public User save(final User user, final String oldEmail)
```

---

### ISSUE-003: Добавить userCache.clear() после user update
**Статус**: ❌ TODO  
**Приоритет**: P1 - CRITICAL  
**Описание**: RequestScopedUserCache не очищается после изменения пользователя, что может привести к stale data в рамках одного request.

**Проблема**:
```java
// Request start
User user = getCurrentUser(); // Cached в RequestScopedUserCache
user.setEmail("new@email.com");
userRepository.save(user); // Сохранено в БД + Caffeine cache evicted
User user2 = getCurrentUser(); // ❌ Вернет СТАРЫЕ данные из RequestScopedUserCache!
```

**Решение 1** (временное - для текущей архитектуры):
```java
// В UserUseCaseService или UserRepository.save()
userCache.clear(); // Очистить request-scoped cache
```

**Решение 2** (правильное - изменить архитектуру):
```java
// Избавиться от RequestScopedUserCache, использовать только Caffeine с коротким TTL (1 min)
@Cacheable(value = "users", key = "#email", expire = "1m")
```

**Файл**: 
- `src/main/java/org/apolenkov/application/service/user/UserUseCaseService.java`
- `src/main/java/org/apolenkov/application/infrastructure/repository/jdbc/adapter/UserJdbcAdapter.java`

---

## ⚠️ ВАЖНО (Priority 2)

### ISSUE-004: Написать тесты для batch операций
**Статус**: ❌ TODO  
**Приоритет**: P2 - HIGH  
**Описание**: После добавления batch операций (countByDeckIds, getKnownCardIdsBatch, saveAll) нет тестов для проверки корректности и производительности.

**Требуемые тесты**:

1. **FlashcardUseCaseServiceTest**:
```java
@Test
void shouldCountByDeckIdsBatch() {
    // Given: 3 decks with 10, 20, 30 cards
    List<Long> deckIds = List.of(1L, 2L, 3L);
    
    // When
    Map<Long, Long> counts = flashcardUseCase.countByDeckIds(deckIds);
    
    // Then
    assertThat(counts).hasSize(3);
    assertThat(counts.get(1L)).isEqualTo(10);
    assertThat(counts.get(2L)).isEqualTo(20);
    assertThat(counts.get(3L)).isEqualTo(30);
}

@Test
void shouldHandleEmptyDeckIdsList() {
    Map<Long, Long> counts = flashcardUseCase.countByDeckIds(List.of());
    assertThat(counts).isEmpty();
}

@Test
void shouldExcludeDecksWithZeroCards() {
    // Given: deck 1 has 10 cards, deck 2 has 0 cards
    Map<Long, Long> counts = flashcardUseCase.countByDeckIds(List.of(1L, 2L));
    
    // Then: только deck 1 в результате
    assertThat(counts).hasSize(1);
    assertThat(counts).containsKey(1L);
    assertThat(counts).doesNotContainKey(2L);
}
```

2. **StatsServiceTest**:
```java
@Test
void shouldGetKnownCardIdsBatch() {
    // Given: deck 1 has cards [1,2,3] known, deck 2 has cards [4,5] known
    Map<Long, Set<Long>> knownCards = statsService.getKnownCardIdsBatch(List.of(1L, 2L));
    
    // Then
    assertThat(knownCards.get(1L)).containsExactlyInAnyOrder(1L, 2L, 3L);
    assertThat(knownCards.get(2L)).containsExactlyInAnyOrder(4L, 5L);
}
```

3. **UserJdbcAdapterTest** (integration):
```java
@Test
void shouldSaveAllUsers() {
    List<User> users = createTestUsers(100);
    List<User> saved = userRepository.saveAll(users);
    
    assertThat(saved).hasSize(100);
    assertThat(saved).allMatch(u -> u.getId() != null);
}
```

**Файлы**:
- `src/test/java/org/apolenkov/application/service/card/FlashcardUseCaseServiceTest.java`
- `src/test/java/org/apolenkov/application/service/StatsServiceTest.java`
- `src/test/java/org/apolenkov/application/infrastructure/repository/jdbc/adapter/UserJdbcAdapterTest.java`

---

### ISSUE-005: Написать тесты для RequestScopedUserCache
**Статус**: ❌ TODO  
**Приоритет**: P2 - HIGH  
**Описание**: RequestScopedUserCache не покрыт тестами.

**Требуемые тесты**:
```java
@Test
void shouldCacheUserInRequestScope() {
    User user = new User();
    user.setId(1L);
    user.setEmail("test@test.com");
    
    userCache.set(user);
    User cached = userCache.get();
    
    assertThat(cached).isEqualTo(user);
}

@Test
void shouldReturnNullWhenCacheEmpty() {
    assertThat(userCache.get()).isNull();
}

@Test
void shouldClearCache() {
    userCache.set(testUser);
    userCache.clear();
    assertThat(userCache.get()).isNull();
}

@Test
void shouldThrowExceptionWhenSetNull() {
    assertThatThrownBy(() -> userCache.set(null))
        .isInstanceOf(IllegalArgumentException.class);
}
```

**Файл**: `src/test/java/org/apolenkov/application/config/cache/RequestScopedUserCacheTest.java` (создать)

---

### ISSUE-006: Написать тесты для Caffeine cache
**Статус**: ❌ TODO  
**Приоритет**: P2 - HIGH  
**Описание**: Проверить корректность работы Caffeine cache (TTL, eviction, hit rate).

**Требуемые тесты**:
```java
@SpringBootTest
class CacheIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Test
    void shouldCacheUserByEmail() {
        // Given
        User user = createTestUser("cached@test.com");
        userRepository.save(user);
        
        // When - первый вызов
        Optional<User> result1 = userRepository.findByEmail("cached@test.com");
        // When - второй вызов (должен быть из кэша)
        Optional<User> result2 = userRepository.findByEmail("cached@test.com");
        
        // Then - проверяем, что в кэше
        Cache cache = cacheManager.getCache("usersByEmail");
        assertThat(cache.get("cached@test.com")).isNotNull();
    }
    
    @Test
    void shouldEvictCacheOnSave() {
        // Given - user в кэше
        userRepository.findByEmail("evict@test.com");
        
        // When - update user
        User user = userRepository.findByEmail("evict@test.com").get();
        user.setName("New Name");
        userRepository.save(user);
        
        // Then - кэш очищен
        Cache cache = cacheManager.getCache("usersByEmail");
        assertThat(cache.get("evict@test.com")).isNull();
    }
}
```

**Файл**: `src/test/java/org/apolenkov/application/config/cache/CacheIntegrationTest.java` (создать)

---

### ISSUE-007: Написать тесты для PracticeView inline логики
**Статус**: ❌ TODO  
**Приоритет**: P2 - MEDIUM  
**Описание**: После удаления PracticePresenter логика перенесена в PracticeView, но тесты не написаны.

**Проблема**: Сложно тестировать UI компоненты напрямую.

**Решение 1** (временное):
Тестировать через PracticeSessionService и PracticeSessionManager:
```java
@Test
void shouldCalculateCompletionMetrics() {
    PracticeSession session = createCompletedSession();
    SessionCompletionMetrics metrics = sessionService.calculateCompletionMetrics(session);
    
    assertThat(metrics.totalCards()).isEqualTo(10);
    assertThat(metrics.sessionMinutes()).isGreaterThan(0);
}
```

**Решение 2** (правильное):
Вернуть Presenter для testability:
```java
@Component
public class PracticeViewPresenter {
    // Вся бизнес-логика из PracticeView
    // Легко тестировать без UI
}
```

**Файл**: Обсудить с командой - стоит ли возвращать Presenter?

---

### ISSUE-008: Создать Logging Policy документ
**Статус**: ❌ TODO  
**Приоритет**: P2 - MEDIUM  
**Описание**: Нет единого стандарта использования уровней логирования (DEBUG/INFO/WARN/ERROR).

**Требуется**:
Создать `.cursor/rules/logging-policy.mdc` с политикой:

```markdown
# LOGGING POLICY

## Уровни логирования

### DEBUG - Технические детали для разработчиков
- Method entry/exit (только для сложных методов)
- Cache hits/misses
- Query results count (например: "Retrieved 150 users")
- Performance metrics (например: "Batch saved 1000 users in 250ms")
- SQL queries (в dev профиле)

### INFO - Бизнес-события
- User actions (login, logout, create deck, complete practice session)
- Service initialization ("UserService initialized")
- Configuration loaded ("Loaded config: batchSize=100")
- Background jobs started/completed

### WARN - Восстанавливаемые проблемы
- Validation failures
- Not found resources (если ожидаются)
- Deprecated features used
- Fallback scenarios triggered
- Retryable errors

### ERROR - Системные ошибки
- Database connection failures
- Integration failures (external API down)
- Unexpected exceptions
- Data corruption detected
- Unrecoverable errors

### AUDIT (separate logger) - Security & Compliance
- Authentication events (login success/failure)
- Authorization decisions (access granted/denied)
- Data modifications (create/update/delete sensitive data)
- Access to sensitive data (view user profile, export data)
- Configuration changes (by admin)

## Примеры

### ✅ ПРАВИЛЬНО
```java
LOGGER.debug("Batch saving {} users", users.size());
LOGGER.info("User registered: email={}, userId={}", email, userId);
LOGGER.warn("Deck not found: deckId={}", deckId);
LOGGER.error("Failed to connect to database", exception);
AUDIT_LOGGER.info("User logged in: username={}", username);
```

### ❌ НЕПРАВИЛЬНО
```java
LOGGER.info("Entering method saveUser"); // DEBUG
LOGGER.warn("User logged in"); // AUDIT
LOGGER.error("Validation failed: email is empty"); // WARN
LOGGER.debug("User deleted deck"); // INFO или AUDIT
```

## Параметризация

### ✅ ПРАВИЛЬНО - структурированное логирование
```java
LOGGER.info("Deck created: deckId={}, title='{}', userId={}", 
    deck.getId(), deck.getTitle(), deck.getUserId());
```

### ❌ НЕПРАВИЛЬНО - конкатенация строк
```java
LOGGER.info("Deck created: " + deck.getId() + ", title: " + deck.getTitle());
```

## Performance considerations

- НЕ вычислять параметры для логов, если уровень отключен:
```java
if (LOGGER.isDebugEnabled()) {
    LOGGER.debug("Expensive computation: {}", expensiveMethod());
}
```

- Использовать lazy evaluation для SLF4J:
```java
LOGGER.debug("Result: {}", () -> expensiveComputation()); // Java 8+
```
```

**Файл**: `.cursor/rules/logging-policy.mdc` (создать)

---

## 🟡 МОЖНО (Priority 3)

### ISSUE-009: Упростить двухуровневое кэширование
**Статус**: ❌ TODO  
**Приоритет**: P3 - LOW  
**Описание**: RequestScopedUserCache + Caffeine Cache - избыточная сложность.

**Анализ**:
- Request-scoped: нужен для multiple calls within same request
- Application-level: нужен для cross-request caching

**Вопрос**: Можно ли заменить одним Caffeine с коротким TTL (1 min)?

**Бенчмарк**:
```java
// Текущая схема:
// Request 1: DB → Caffeine (write) → RequestScoped (write) → return
// Request 1 (2nd call): RequestScoped (read) → return (fast!)
// Request 2: Caffeine (read) → return (medium fast)

// Упрощенная схема (только Caffeine):
// Request 1: DB → Caffeine (write) → return
// Request 1 (2nd call): Caffeine (read) → return (fast?)
// Request 2: Caffeine (read) → return (fast)

// Нужно замерить разницу в latency
```

**Решение**: Провести A/B тестирование.

---

### ISSUE-010: Добавить cache metrics monitoring
**Статус**: ❌ TODO  
**Приоритет**: P3 - LOW  
**Описание**: Нет мониторинга эффективности кэша (hit rate, eviction rate).

**Решение**:
```java
@Configuration
public class CacheMetricsConfiguration {
    
    @Bean
    public MeterBinder caffeineCacheMetrics(CacheManager cacheManager) {
        return binder -> {
            CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
            caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof CaffeineCache) {
                    com.github.benmanes.caffeine.cache.Cache nativeCache = 
                        ((CaffeineCache) cache).getNativeCache();
                    CaffeineCacheMetrics.monitor(binder, nativeCache, cacheName);
                }
            });
        };
    }
}
```

**Метрики**:
- `cache.requests{cache=usersByEmail,result=hit}`
- `cache.requests{cache=usersByEmail,result=miss}`
- `cache.evictions{cache=usersByEmail}`
- `cache.size{cache=usersByEmail}`

**Endpoint**: `/actuator/metrics/cache.requests`

---

### ISSUE-011: Оптимизировать PracticeView.loadDeck
**Статус**: ❌ TODO  
**Приоритет**: P3 - LOW  
**Описание**: PracticeView делает несколько запросов при загрузке:

```java
sessionService.loadDeck(deckId)  // 1 SQL
sessionService.getNotKnownCards(deckId)  // 1 SQL + stats queries
```

**Решение**:
```java
public record PracticeSessionData(Deck deck, List<Flashcard> notKnownCards) {}

public PracticeSessionData loadDeckWithCards(long deckId) {
    // 1 объединенный запрос вместо 2-3
    String sql = """
        SELECT d.*, 
               array_agg(f.*) as flashcards,
               array_agg(kc.card_id) as known_card_ids
        FROM decks d
        LEFT JOIN flashcards f ON f.deck_id = d.id
        LEFT JOIN known_cards kc ON kc.card_id = f.id
        WHERE d.id = ?
        GROUP BY d.id
    """;
    // ...
}
```

**Профит**: 2-3 SQL → 1 SQL

---

## 📊 Summary

**Всего задач**: 11  
**Критичных (P1)**: 3  
**Важных (P2)**: 5  
**Можно отложить (P3)**: 3

**Estimated effort**: 
- P1: ~4 часа
- P2: ~16 часов (2 дня)
- P3: ~8 часов (1 день)

**Total**: ~3-4 дня работы

