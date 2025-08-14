# Flashcards — Spring Boot + Vaadin (Clean Architecture)

Приложение для изучения языков с карточками, реализованное на Spring Boot и Vaadin.

### Слои и структура
- `application/usecase` — интерфейсы сценариев (use cases)
- `domain` — доменные модели и инварианты
- `infrastructure/repository` — хранилища (in‑memory/JPA)
- `ui/view` — представления Vaadin
- `ui/presenter` — презентеры/фасады для UI

### Запуск (Dev)
```bash
./gradlew clean bootRun
```
Откройте `http://localhost:8080`.

Профили по умолчанию: `dev,memory`. Dev использует H2 + Flyway, memory‑реализации репозиториев.

### Prod‑сборка
```bash
./gradlew clean build -Pvaadin.productionMode
```
Jar будет в `build/libs/`. Переменные БД для prod передаются через `application.yml` (профиль `prod`).

### Тесты
- Юнит/интеграционные:
```bash
./gradlew test
```
- UI (Vaadin TestBench — запускаются отдельно и требуют локального браузера):
```bash
./gradlew uiTest
```

### Качество/линтеры/покрытие
- Spotless (форматирование) — запускается автоматически перед компиляцией
- Jacoco отчёты — публикуются в `build/reports/jacoco`

### Безопасность
- Spring Security: публичные страницы (`/`, `/login`, `/register`), защищённые — в остальном
- Logout — через MVC‑контроллер, очистка сессии и remember‑me
- CSP заголовки: строгий в prod, ослабленный в dev

### Локализация
- i18n ключи для landing/login/register и UI навигации
- Выбор языка хранится в cookie и восстанавливается при старте UI

