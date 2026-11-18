# Memocards

Веб-приложение для изучения слов по карточкам: колоды, карточки, режим тренировки, статистика,
админка новостей, интерфейс на трех языках (ru, en, es).

Выпускной проект курса OTUS "Java Developer. Professional", 2025.

> Демо-стенд выключен. Ниже описан локальный запуск.

## Стек

- Java 21, Spring Boot 3.5, Spring Security, Spring JDBC
- Vaadin 24 (серверный UI)
- PostgreSQL 16, миграции Flyway
- Caffeine для кэша, инвалидация через Spring Events
- JUnit 5, Mockito, Testcontainers; Checkstyle, SpotBugs, PMD, Spotless
- Docker-образ собирается через Jib; Prometheus, Loki и Grafana для мониторинга;
  деплой через Ansible

## Архитектура

Гексагональная архитектура (порты и адаптеры): домен не зависит от инфраструктуры.

```text
views/ (Vaadin UI)
   │
   ▼
service/ (сценарии, @Transactional)
   │
   ▼
domain/port/ (интерфейсы репозиториев)  ◄── domain/, model/ (сущности, события)
   ▲
   │ реализует
infrastructure/repository/jdbc/ (JDBC-адаптеры, SQL)
   │
   ▼
PostgreSQL (схема в db/migration, Flyway)
```

Документы:

- [architecture.md](docs/architecture/architecture.md): диаграммы C4 (контекст, контейнеры,
  компоненты), схема БД, потоки аутентификации и инвалидации кэша, деплой
- [ADR](docs/architecture/adr/README.md): архитектурные решения
- [security.md](docs/architecture/security.md),
  [testing-strategy.md](docs/architecture/testing-strategy.md),
  [observability.md](docs/architecture/observability.md),
  [ci-cd.md](docs/architecture/ci-cd.md),
  [non-functional-requirements.md](docs/architecture/non-functional-requirements.md),
  [slo.md](docs/architecture/slo.md)
- [CONTRIBUTING.md](docs/CONTRIBUTING.md): как устроена разработка

## Локальный запуск

Нужны JDK 21 (Temurin или `brew install openjdk@21`) и Docker с Docker Compose.

### Демо одной командой

```bash
make demo         # PostgreSQL в Docker и приложение с демо-данными на http://localhost:8080
make demo-stop    # остановить контейнеры, том с данными остается
```

Скрипт `scripts/demo.sh` создает `.env` из `env.sample`, если его нет, поднимает базу, собирает
jar с production-сборкой интерфейса и печатает адрес и демо-логины, когда приложение готово.
Первый запуск занимает несколько минут: Gradle скачивает зависимости и собирает фронтенд.
Приложение останавливается по Ctrl+C.

### Запуск для разработки

```bash
git clone https://github.com/apolenkov/memocards.git
cd memocards
cp env.sample .env      # значения по умолчанию годятся для локальной машины

make start              # PostgreSQL 16 в Docker, порт 127.0.0.1:5432
make dev                # ./gradlew bootRun с профилем dev
```

Приложение откроется на http://localhost:8080. При `SEED_DEMO_DATA=true` (так в `env.sample`)
создаются два пользователя: `user@example.com` / `user` и `admin@example.com` / `admin`.

Приложение и база в контейнерах (`docker-compose.yml` + `docker-compose.dev.yml`):

```bash
make jib                # собрать образ локально через Jib
make docker             # поднять app + postgres, приложение на 127.0.0.1:8080
make docker-stop
```

Мониторинг (Prometheus, Loki, Promtail, Grafana из `docker-compose.infrastructure.yml`):
`make stack-up`, остановка: `make stack-down`.

## Тесты и проверки

```bash
./gradlew test              # модульные тесты
./gradlew integrationTest   # интеграционные, нужен Docker (Testcontainers)
./gradlew check             # тесты и статический анализ
```

## Релизы

34 тега релизов, от `v0.1.0` до `v0.9.1`. Версию сборки JGitver берет из тега.

## Лицензия

См. [LICENSE](LICENSE).
