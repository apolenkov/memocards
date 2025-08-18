# Настройка PostgreSQL для Flashcards

## Обзор

## Быстрый старт

### 1. Запуск PostgreSQL

```bash
# Запустить PostgreSQL
make start

# Или вручную
docker-compose up -d postgres
```

### 2. Запуск приложения

```bash
# Запустить приложение (автоматически запустит PostgreSQL)
make run

# Или в режиме разработки
make dev

# Или вручную
./gradlew bootRun
```

## Доступные команды

```bash
make help          # Показать справку
make start         # Запустить PostgreSQL
make stop          # Остановить PostgreSQL
make restart       # Перезапустить PostgreSQL
make logs          # Показать логи PostgreSQL
make clean         # Очистить все данные
make build         # Собрать приложение
make run           # Запустить приложение
make dev           # Запустить в режиме разработки
make test          # Запустить тесты
make test-with-db  # Запустить тесты с базой данных
```

## Конфигурация базы данных

### Dev профиль
- **URL**: `jdbc:postgresql://localhost:5432/flashcards`
- **Пользователь**: `postgres`
- **Пароль**: `postgres`

### JPA профиль (для тестов)
- **URL**: `jdbc:postgresql://localhost:5432/flashcards`
- **Пользователь**: `postgres`
- **Пароль**: `postgres`

### Prod профиль
- **URL**: `${DB_URL}` (из переменных окружения)
- **Пользователь**: `${DB_USER}` (из переменных окружения)
- **Пароль**: `${DB_PASS}` (из переменных окружения)

## Структура Docker

```
postgres:
  image: postgres:15-alpine
  ports:
    - "5432:5432"
  environment:
    POSTGRES_DB: flashcards
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
```

## Миграции

Все миграции теперь совместимы с PostgreSQL:
- Используется `BIGSERIAL` для автоинкрементных ID
- Поддерживаются все возможности PostgreSQL
- Flyway автоматически применяет миграции

## Устранение неполадок

### PostgreSQL не запускается
```bash
# Проверить логи
make logs

# Перезапустить
make restart
```

### Ошибки подключения
```bash
# Проверить статус
docker-compose ps

# Проверить готовность
docker-compose exec postgres pg_isready -U postgres
```

### Очистка данных
```bash
# Полная очистка (удалит все данные!)
make clean
```

## Переменные окружения

Для продакшена можно переопределить настройки:

```bash
export DB_URL=jdbc:postgresql://your-host:5432/your-db
export DB_USER=your-user
export DB_PASS=your-password
```

## Безопасность

⚠️ **Внимание**: В dev среде используются простые пароли. Для продакшена обязательно используйте сложные пароли и ограничьте доступ к базе данных.
