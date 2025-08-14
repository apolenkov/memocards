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
- Spotless (форматирование) — запускайте явно: `./gradlew spotlessApply`
- Jacoco отчёты — публикуются в `build/reports/jacoco`

### Dependency locking
- Включён Gradle dependency locking для всех конфигураций
- Сгенерировать lock‑файлы:
```bash
./gradlew dependencies --write-locks
```
- Обновить одну/несколько зависимостей:
```bash
./gradlew build --update-locks group:artifact[,group2:artifact2]
```
Зафиксируйте lock‑файлы в VCS.

### Безопасность
- Публичные страницы: `/`, `/login`, `/register`; остальное — под ролью `USER`
- Logout: `POST /logout` с CSRF (кнопка в меню делает fetch POST)
- Куки локали: `HttpOnly`, `Secure`, `SameSite=Lax` (устанавливается фильтром)
- CSP: строгий в prod (без `object-src`, `frame-ancestors 'none'`), ослабленный в dev

### Локализация
- i18n ключи для всего UI, включая админку
- Выбор языка хранится в cookie и восстанавливается при старте UI

