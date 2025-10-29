# Memocards

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24+-blue.svg)](https://vaadin.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

🌐 **[Live Demo](https://memocards.duckdns.org)** — Test with `user@example.com` / `user`

Flashcard learning application. Built with Java 21, Spring Boot 3.x, Vaadin 24+, PostgreSQL.

---

## What's Inside?

| Feature                | Technology                            | Implementation                                        |
|------------------------|---------------------------------------|-------------------------------------------------------|
| **Clean Architecture** | Hexagonal (Ports & Adapters)          | Domain → Service → Infrastructure → Views             |
| **Java 21**            | Virtual Threads, Records, Text Blocks | Records for DTOs, Text Blocks for SQL queries         |
| **Caching**            | @UIScope + Caffeine                   | Per-tab isolation, event-driven invalidation          |
| **Security**           | Spring Security                       | Form auth, @RolesAllowed, parameterized queries       |
| **i18n**               | ResourceBundle                        | 3 languages: en, es, ru                               |
| **Monitoring**         | Grafana + Prometheus + Loki           | Real-time metrics & logs dashboard (admin-only, tabs) |
| **Deployment**         | Docker + Ansible                      | Automated VPS deployment                              |

## Project Structure

```
src/main/java/org/apolenkov/application/
├── domain/              # Pure business logic (ports, events, enums)
│   ├── port/           # Repository interfaces (contracts)
│   ├── event/          # Domain events (cache invalidation)
│   └── model/          # Enums (FilterOption, PracticeDirection)
├── model/              # Domain entities (Card, Deck, User, News)
├── service/            # Business logic (@Transactional here)
│   ├── card/          # Card use cases
│   ├── deck/          # Deck operations
│   ├── user/          # User management
│   ├── stats/         # Statistics with caching
│   └── auth/          # Authentication & password reset
├── infrastructure/     # Technical layer (JDBC adapters)
│   └── repository/     # SQL queries (text blocks) + RowMappers
├── views/              # Vaadin UI layer
│   ├── auth/          # Login, Register, Password Reset
│   ├── deck/          # Deck CRUD + card management
│   ├── practice/       # Flashcard practice sessions
│   ├── stats/         # Progress statistics
│   ├── admin/         # Admin panel (news)
│   └── core/          # Layout, navigation, error handling
└── config/            # Spring configuration
    ├── security/       # Spring Security setup
    ├── cache/         # Caching strategies
    ├── seed/          # Demo data
    └── vaadin/        # PWA configuration
```

**Key principle:** Each layer can only depend on layers below it. Views → Service → Infrastructure → Database.

---

## 🛠️ Tech Stack

<details>
<summary><b>Backend</b> — Java 21 + Spring Boot 3.x + PostgreSQL 16</summary>

- **Java 21**: Virtual Threads, Records, Pattern Matching, Text Blocks
- **Spring Boot 3.x**: DI, Security, Transactions
- **Spring Data JDBC**: Explicit SQL control
- **PostgreSQL 16**: Flyway migrations
- **Caffeine**: High-performance caching
- **Spring Events**: Decoupled cache invalidation

</details>

<details>
<summary><b>Frontend</b> — Vaadin 24+ (server-side)</summary>

- **Vaadin 24+**: Server-side UI, PWA-ready
- **Lumo Theme**: Material Design inspired
- **i18n**: ResourceBundle (en, es, ru)
- **Responsive**: Mobile-first CSS Grid/Flexbox

</details>

<details>
<summary><b>Architecture</b> — Clean Architecture + Hexagonal</summary>

- **Layer Separation**: domain → service → infrastructure → views
- **Ports & Adapters**: Repository interfaces + JDBC adapters
- **SOLID Principles**: DI, single responsibility
- **Event-Driven**: Immediate cache updates via Spring Events

</details>

<details>
<summary><b>Testing & Quality</b> — TDD + Static Analysis</summary>

- **JUnit 5 + Mockito**: Unit tests
- **TestContainers**: Real PostgreSQL in integration tests
- **Checkstyle + SpotBugs + SonarLint**: Code quality gates

</details>

<details>
<summary><b>DevOps</b> — Docker + Ansible + CI/CD</summary>

- **Docker**: Jib containerless builds
- **Ansible**: Automated VPS deployment
- **GitHub Actions**: CI/CD pipelines
- **Monitoring**: Spring Actuator + Prometheus metrics

</details>

---

## 🚀 Quick Start

<details>
<summary><b>Local Development Setup</b> (click to expand)</summary>

**Prerequisites:** Java 21+, Docker, Docker Compose

```bash
# 1. Clone & configure
git clone <repository-url> && cd memo
cp env.sample .env  # Edit: set DB_PASSWORD, demo passwords

# 2. Start PostgreSQL
docker-compose up -d postgres

# 3. Run app
./gradlew bootRun

# 4. Open http://localhost:8080
# Login: user@example.com / user
```

**Docker Compose (full stack):**

```bash
# Start everything at once (recommended):
make stack-up
# This will: 1) start app (creates network), 2) start infrastructure (uses network)

# Or manually:
make docker          # Start app (creates memocards-network)
make infra-up        # Start infrastructure (uses existing network)
```

**Project Structure:**
- `docker-compose.yml` — application (app + postgres) **creates network**
- `docker-compose.infrastructure.yml` — monitoring (prometheus + loki + promtail + grafana) **uses network**
- `infrastructure/` — monitoring configuration files
- `infrastructure/ansible/` — deployment automation (playbooks, roles, inventory)
- Shared network: `memocards-network` (created by docker-compose.yml, used by infrastructure)

</details>

<details>
<summary><b>Testing & Configuration</b> (click to expand)</summary>

**Run tests:**

```bash
./gradlew test              # Unit tests
./gradlew integrationTest   # Integration (requires Docker)
./gradlew check             # All checks (tests + quality)
```

**Configuration via `.env` file** (see `env.sample`):

- Cache TTL/size settings
- UI debounce & pagination
- Security thresholds
- Spring profiles: dev, test, prod

</details>

---

## Technical Decisions

| Decision             | Rationale                                              |
|----------------------|--------------------------------------------------------|
| **Spring Data JDBC** | Explicit SQL control, no N+1 queries, easier debugging |
| **Text Blocks**      | Readable multi-line SQL, Java 15+ syntax               |
| **@UIScope Cache**   | Per-browser-tab cache isolation (Vaadin-specific)      |
| **Virtual Threads**  | Java 21 platform threads for concurrency               |
| **TestContainers**   | Real PostgreSQL in tests, no database mocking          |
| **Records**          | Immutable DTOs, reduced boilerplate                    |
| **Spring Events**    | Decoupled cache invalidation, no polling               |

<details>
<summary><b>🔧 Development Workflow</b> — Common commands (click to expand)</summary>

```bash
./gradlew bootRun                    # Run with auto-reload
./gradlew test                       # Run tests
./gradlew codeQuality                # Checkstyle + SpotBugs
./gradlew spotlessApply              # Auto-format code
./gradlew vaadinBuildFrontend        # Build production bundle
./gradlew jibDockerBuild             # Build Docker image
```

</details>

---

## 🗺️ Roadmap

<details>
<summary>Future enhancements (click to expand)</summary>

- [ ] Deck sharing & import/export
- [ ] AI-powered card generation (GPT-4 API)
- [ ] Progress analytics dashboard
- [ ] Mobile apps (Vaadin Hilla)

</details>

---

## How to Study This Codebase

**Suggested order:**

1. `views/` — UI layer (Vaadin components)
2. `service/` — Business logic (@Transactional boundaries)
3. `infrastructure/repository/` — JDBC adapters (SQL execution)
4. `model/` + `domain/` — Domain entities

**Suggested tasks:**

- Add a new field to Card entity (observe layer propagation)
- Implement new REST endpoint (follow existing pattern)
- Add cache invalidation (use Spring Events example)

**Topics covered:**

- Clean Architecture implementation (layers in practice)
- Layer dependencies and separation
- Transaction boundaries (@Transactional on services)
- Testing: unit tests + integration tests with TestContainers
- Java 21 features: Records, Text Blocks, Pattern Matching

**Suggested flow for students:**

1. Open `DeckView` → trace UI to service call
2. Follow user action: "Create Deck" → `DeckService.create()` → `DeckRepository.save()`
3. Explain `@Transactional` placement (service layer, not repository)
4. Demonstrate cache invalidation: event → listener → cache.clear()

---

## Contributing

- Bug reports → [GitHub Issues](../../issues)
- Pull Requests → See [CONTRIBUTING.md](CONTRIBUTING.md)
- Translations → add new languages
- Features → suggest improvements

---

## License

Source-available for learning and contributions. Forking and commercial use prohibited. See [LICENSE](LICENSE).

---

[Live Demo](https://memocards.duckdns.org) • [Issues](../../issues) • [Contributing](CONTRIBUTING.md)

