# Flashcards — Spring Boot + Vaadin (Clean Architecture)

Приложение для изучения языков с карточками, реализованное на Spring Boot и Vaadin.

### Слои и структура
- `src/main/java/org/apolenkov/application/usecase` — интерфейсы сценариев (use cases)
- `src/main/java/org/apolenkov/application/model` — доменные модели и инварианты
- `src/main/java/org/apolenkov/application/domain/port` — порты доступа к данным (интерфейсы)
- `src/main/java/org/apolenkov/application/infrastructure/repository/jpa` — адаптеры/сущности Spring Data JPA
- `src/main/java/org/apolenkov/application/service` — прикладные сервисы/фасады/презентеры
- `src/main/java/org/apolenkov/application/views` — Vaadin View’ы и компоненты

### Запуск (Dev)
```bash
./gradlew clean bootRun
```
Откройте `http://localhost:8080`.

### Профили
- `dev`: H2 in‑memory + Flyway, JPA адаптеры, Vaadin dev‑режим (vite, HMR)
- `jpa`: Testcontainers Postgres для интеграционных окружений
- `prod`: внешняя БД Postgres, строгий CSP

Активный профиль настраивается в `application.yml`.

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
- Spotless (форматирование) — `./gradlew spotlessApply`
- Jacoco отчёты — `build/reports/jacoco`
- OWASP Dependency Check — `./gradlew dependencyCheckAnalyze`

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
- Публичные страницы: `/`, `/login`, `/register`, `/error`; остальное — под ролью `ROLE_USER`/`ROLE_ADMIN`
- Logout: переход на `/logout` (Spring Security инвалидирует сессию/куки; CSRF включён)
- CSRF: HttpOnly cookie через `CookieCsrfTokenRepository`
- Куки локали: `HttpOnly`, `Secure`, `SameSite=Lax` (через `SameSiteCookieFilter`)
- CSP: строгий в prod (без `object-src`, `frame-ancestors 'none'`), ослабленный в dev (vite/ws)

### Локализация
- Все тексты UI через `getTranslation(...)` (Vaadin `I18NProvider`)
- Файлы: `src/main/resources/i18n/messages_*.properties` (`en`, `ru`, `es`)
- Выбор языка хранится в `preferredLocale` cookie и восстанавливается при инициализации UI

#### Конвенции i18n‑ключей
- Формат: `domain.section.key[.subkey]` в `lowercase` через точки.
- Группы:
  - `app.*` — общие свойства приложения (например, название)
  - `main.*` — навигация/шапка/общие элементы UI
  - `error.*` — страницы/сообщения об ошибках (например, `error.403`, `error.500`)
  - `auth.*` — логин/регистрация/валидация
  - `deck*` — страницы и действия для колод и карточек
  - `practice.*` — практика
  - `stats.*` — статистика
  - `settings.*` — настройки
  - `dialog.*`, `common.*` — общие диалоговые элементы и универсальные подписи

Примеры:
- `app.title` — человекочитаемое имя приложения.
  - Используется как заголовок страницы (см. `Application` с `@PageTitle("app.title")`).
  - Используется в брендинге шапки (`TopMenu` → ссылка на главную).
- `common.cancel` — универсальная кнопка Отмена (используется в диалогах по всему UI).
- `error.500` — заголовок страницы ошибок (см. `ErrorView`).

Рекомендации:
- Не дублировать смысл: для однотипных элементов (например, Отмена) используйте один ключ (`common.cancel`).
- Не вкладывать HTML в значения; форматирование делайте через компоненты.
- Плейсхолдеры — через `{0}`, `{1}` и т. д. с `MessageFormat`.
- Новые ключи добавлять сразу во все поддерживаемые языки. Если перевода нет, временно допускается английское значение.

Проверка и поиск пропусков:
- При отсутствии ключа используется сам код ключа (см. `AppI18NProvider#setUseCodeAsDefaultMessage(true)`),
  что облегчает визуальный поиск не‑переведённых мест.
- Полный перечень ключей смотрите в `messages_*.properties`. В данном README перечислены только основные группы — не все ключи задокументированы поимённо.

#### Критические ключи
- `app.title` — название приложения (отображается в тайтле окна и в шапке).
- `main.*` — элементы шапки/навигации.
- `error.403`, `error.500` — заголовки страниц ошибок.
- `common.cancel` — единый ключ отмены в диалогах.

#### Часто используемые ключи (шпаргалка)

| Ключ | Где используется | Примечание |
|---|---|---|
| `app.title` | `Application` (`@PageTitle`), `TopMenu` | Брендинг/титул страницы |
| `main.decks` | `TopMenu`, `DecksView` | Пункт меню/назад к списку |
| `main.stats` | `TopMenu`, `StatsView` | Переход в статистику |
| `main.settings` | `TopMenu`, `PracticeSettingsView` | Переход в настройки |
| `auth.login.*` | `LoginView` | Лейблы/тексты логина |
| `auth.register.*` | `RegisterView` | Регистрация |
| `deck.*` | `DeckView`, диалоги карт | Колоды/карточки/столбцы/сообщения |
| `practice.*` | `PracticeView` | Сессия практики, прогресс |
| `stats.*` | `StatsView` | Итоги/по колодам |
| `settings.*` | `PracticeSettingsView` | Настройки практики |
| `dialog.save`, `common.cancel` | Все диалоги | Единые подписи кнопок |

### Полезные команды
- Просмотр управляемых версий зависимостей: `./gradlew managedVersions`
- UI‑тесты (TestBench, по тегу `ui`): `./gradlew uiTest`

