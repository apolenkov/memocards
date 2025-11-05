# Custom Business Metrics для Prometheus

**Руководство по добавлению бизнес-метрик в Memocards**

---

## 📋 ОБЗОР

**Цель**: Добавить бизнес-метрики в Prometheus для мониторинга использования приложения и эффективности обучения.

**Текущее состояние**: 
- ✅ Spring Boot Actuator с Prometheus endpoint
- ✅ Micrometer интегрирован
- ✅ Пример: `CacheMetricsCollector` (cache metrics)

**Что нужно добавить**:
- Бизнес-метрики (пользователи, деки, карточки, практика)

---

## 🎯 РЕКОМЕНДУЕМЫЕ МЕТРИКИ

### 1. **Entity Counts (Gauge)**
- `memo.users.total` - общее количество пользователей
- `memo.decks.total` - общее количество дек
- `memo.cards.total` - общее количество карточек

### 2. **Practice Session Metrics (Counter + Timer)**
- `memo.practice.sessions.total` - количество завершенных сессий
- `memo.practice.sessions.duration` - длительность сессий (timer)
- `memo.practice.cards.viewed` - количество просмотренных карточек
- `memo.practice.answers.correct` - правильные ответы
- `memo.practice.answers.hard` - неправильные ответы

### 3. **Learning Progress Metrics (Gauge)**
- `memo.learning.progress.percent` - процент изученных карточек по деке
- `memo.learning.known.cards.total` - общее количество изученных карточек

### 4. **User Activity Metrics (Counter)**
- `memo.users.active.daily` - активные пользователи за день
- `memo.users.registrations.total` - общее количество регистраций

---

## 💻 ПРИМЕР РЕАЛИЗАЦИИ

### 1. Создать `BusinessMetricsCollector`

```java
package org.apolenkov.application.service.stats.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Service for collecting business metrics.
 * Uses Micrometer for metrics collection and Spring Events for decoupled monitoring.
 */
@Component
public class BusinessMetricsCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessMetricsCollector.class);

    private static final String APPLICATION_TAG = "application";
    private static final String APPLICATION_VALUE = "memo";

    private final MeterRegistry meterRegistry;
    private final Counter practiceSessionsCounter;
    private final Counter practiceCardsViewedCounter;
    private final Counter practiceAnswersCorrectCounter;
    private final Counter practiceAnswersHardCounter;
    private final Timer practiceSessionTimer;

    // Dependencies для получения данных
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final StatsRepository statsRepository;

    public BusinessMetricsCollector(
            final MeterRegistry meterRegistryValue,
            final UserRepository userRepositoryValue,
            final DeckRepository deckRepositoryValue,
            final CardRepository cardRepositoryValue,
            final StatsRepository statsRepositoryValue) {
        this.meterRegistry = meterRegistryValue;
        this.userRepository = userRepositoryValue;
        this.deckRepository = deckRepositoryValue;
        this.cardRepository = cardRepositoryValue;
        this.statsRepository = statsRepositoryValue;

        // Initialize counters
        this.practiceSessionsCounter = Counter.builder("memo.practice.sessions.total")
                .description("Total number of completed practice sessions")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        this.practiceCardsViewedCounter = Counter.builder("memo.practice.cards.viewed")
                .description("Total number of cards viewed in practice sessions")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        this.practiceAnswersCorrectCounter = Counter.builder("memo.practice.answers.correct")
                .description("Total number of correct answers")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        this.practiceAnswersHardCounter = Counter.builder("memo.practice.answers.hard")
                .description("Total number of hard answers")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        this.practiceSessionTimer = Timer.builder("memo.practice.sessions.duration")
                .description("Practice session duration")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        // Initialize gauges (will be updated by scheduled task)
        Gauge.builder("memo.users.total", () -> getTotalUsers())
                .description("Total number of users")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        Gauge.builder("memo.decks.total", () -> getTotalDecks())
                .description("Total number of decks")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        Gauge.builder("memo.cards.total", () -> getTotalCards())
                .description("Total number of cards")
                .tag(APPLICATION_TAG, APPLICATION_VALUE)
                .register(meterRegistry);

        LOGGER.info("BusinessMetricsCollector initialized");
    }

    /**
     * Records practice session metrics.
     */
    @EventListener
    public void onPracticeSessionRecorded(final SessionStatsDto sessionData) {
        try {
            // Record session counter
            practiceSessionsCounter.increment();

            // Record cards viewed
            practiceCardsViewedCounter.increment(sessionData.viewed());

            // Record correct/hard answers
            practiceAnswersCorrectCounter.increment(sessionData.correct());
            practiceAnswersHardCounter.increment(sessionData.hard());

            // Record session duration
            practiceSessionTimer.record(Duration.ofMillis(sessionData.sessionDurationMs()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Practice session metrics recorded: deckId={}, viewed={}, correct={}, hard={}, durationMs={}",
                        sessionData.deckId(),
                        sessionData.viewed(),
                        sessionData.correct(),
                        sessionData.hard(),
                        sessionData.sessionDurationMs());
            }

        } catch (RuntimeException e) {
            LOGGER.error("Failed to record practice session metrics: {}", sessionData, e);
        }
    }

    /**
     * Updates entity count gauges periodically.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRateString = "${app.monitoring.business-metrics.update-interval-ms:300000}")
    public void updateEntityCounts() {
        try {
            // Gauges automatically call supplier functions
            // No need to manually update, just log
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Entity counts updated: users={}, decks={}, cards={}",
                        getTotalUsers(),
                        getTotalDecks(),
                        getTotalCards());
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to update entity counts", e);
        }
    }

    // ==================== Private Helper Methods ====================

    private long getTotalUsers() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            LOGGER.warn("Failed to get total users count", e);
            return 0;
        }
    }

    private long getTotalDecks() {
        try {
            return deckRepository.count();
        } catch (Exception e) {
            LOGGER.warn("Failed to get total decks count", e);
            return 0;
        }
    }

    private long getTotalCards() {
        try {
            return cardRepository.count();
        } catch (Exception e) {
            LOGGER.warn("Failed to get total cards count", e);
            return 0;
        }
    }
}
```

### 2. Создать Event для Practice Session

```java
package org.apolenkov.application.service.stats.event;

import org.apolenkov.application.domain.dto.SessionStatsDto;

/**
 * Event published when practice session is recorded.
 * Used for metrics collection.
 */
public record PracticeSessionRecordedEvent(SessionStatsDto sessionData) {
    public static PracticeSessionRecordedEvent of(final SessionStatsDto sessionData) {
        return new PracticeSessionRecordedEvent(sessionData);
    }
}
```

### 3. Обновить `StatsService` для публикации события

```java
@Override
@Transactional
public void recordSession(final SessionStatsDto sessionData) {
    // ... existing code ...

    statsRepository.appendSession(sessionData, today);

    // Publish event for metrics collection
    eventPublisher.publishEvent(new PracticeSessionRecordedEvent(sessionData));

    // Publish event for cache invalidation (existing)
    eventPublisher.publishEvent(new ProgressChangedEvent(this, sessionData.deckId()));

    // ... existing code ...
}
```

### 4. Добавить конфигурацию в `application.yml`

```yaml
app:
  monitoring:
    business-metrics:
      update-interval-ms: ${BUSINESS_METRICS_UPDATE_INTERVAL_MS:300000}  # 5 minutes
```

---

## 📊 PROMETHEUS QUERIES

После реализации метрики будут доступны в Prometheus:

```promql
# Общее количество пользователей
memo_users_total

# Общее количество дек
memo_decks_total

# Общее количество карточек
memo_cards_total

# Количество завершенных сессий
memo_practice_sessions_total

# Средняя длительность сессий (секунды)
rate(memo_practice_sessions_duration_seconds_sum[5m]) / rate(memo_practice_sessions_duration_seconds_count[5m])

# Количество просмотренных карточек
memo_practice_cards_viewed_total

# Процент правильных ответов
rate(memo_practice_answers_correct_total[5m]) / rate(memo_practice_cards_viewed_total[5m]) * 100

# Количество сессий за час
rate(memo_practice_sessions_total[1h]) * 3600
```

---

## 📈 GRAFANA DASHBOARD

Добавить панели в `dashboard-memocards.json`:

```json
{
  "title": "Business Metrics",
  "panels": [
    {
      "title": "Total Users",
      "targets": [{
        "expr": "memo_users_total"
      }]
    },
    {
      "title": "Practice Sessions Rate",
      "targets": [{
        "expr": "rate(memo_practice_sessions_total[5m])"
      }]
    },
    {
      "title": "Average Session Duration",
      "targets": [{
        "expr": "rate(memo_practice_sessions_duration_seconds_sum[5m]) / rate(memo_practice_sessions_duration_seconds_count[5m])"
      }]
    },
    {
      "title": "Correct Answer Rate",
      "targets": [{
        "expr": "rate(memo_practice_answers_correct_total[5m]) / rate(memo_practice_cards_viewed_total[5m]) * 100"
      }]
    }
  ]
}
```

---

## ✅ ШАГИ РЕАЛИЗАЦИИ

1. ✅ Создать `BusinessMetricsCollector` класс
2. ✅ Добавить зависимости (repositories)
3. ✅ Создать `PracticeSessionRecordedEvent`
4. ✅ Обновить `StatsService` для публикации события
5. ✅ Добавить конфигурацию в `application.yml`
6. ✅ Добавить панели в Grafana dashboard
7. ✅ Протестировать метрики

---

## 🔍 ИНТЕГРАЦИЯ С СУЩЕСТВУЮЩЕЙ ИНФРАСТРУКТУРОЙ

**Уже есть:**
- ✅ Spring Boot Actuator с `/actuator/prometheus`
- ✅ Prometheus scraping из `prometheus.yml`
- ✅ Grafana dashboard

**Нужно добавить:**
- Бизнес-метрики в код приложения
- Панели в Grafana dashboard

**Метрики автоматически появятся в Prometheus** после добавления кода!

---

## 📝 ПРИМЕЧАНИЯ

- **Gauge метрики** обновляются автоматически при каждом scrape (Micrometer вызывает supplier)
- **Counter метрики** инкрементируются при событиях
- **Timer метрики** записывают длительность операций
- Все метрики имеют тег `application=memo` для фильтрации

---

## 🎯 РЕЗУЛЬТАТ

После реализации:
- ✅ Бизнес-метрики в Prometheus
- ✅ Дашборды в Grafana
- ✅ Мониторинг использования приложения
- ✅ Аналитика эффективности обучения

