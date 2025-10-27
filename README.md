# Memocards

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24+-blue.svg)](https://vaadin.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

🌐 **[Live Demo](https://memocards.duckdns.org)** — Test with `user@example.com` / `user`

Flashcard learning application built with **Java 21, Spring Boot 3.x, Vaadin 24+, and PostgreSQL**. 
Demonstrates **Clean Architecture**, **multi-tier caching**, and modern Java patterns.

**Perfect for:** Junior developers learning enterprise patterns, teachers explaining architecture to students.

**165+ Java classes, 35+ tests, deployed to production VPS.**

---

## What's Inside?

| Feature | Technology | Why |
|---------|-----------|-----|
| **Clean Architecture** | Hexagonal (Ports & Adapters) | Strict layer separation, zero circular dependencies |
| **Java 21** | Virtual Threads, Records, Text Blocks | Modern Java features in production |
| **Multi-tier Cache** | @UIScope + Caffeine | Fast UI, immediate invalidation |
| **Security** | Spring Security + OWASP | SQL injection prevention, XSS protection |
| **i18n** | 3 languages (en, es, ru) | Production-ready localization |
| **DevOps** | Docker + Ansible | Automated deployment |

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
- **Spring Data JDBC**: Explicit SQL control (JPA alternative for simpler projects)
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

<details>
<summary><b>📂 Project Structure</b> — Clean Architecture layers (click to expand)</summary>

```
src/main/java/org/apolenkov/application/
├── domain/           # Pure business logic (ports, events, enums)
├── model/            # Domain entities (Card, Deck, User, News)
├── service/          # Use cases (@Transactional boundaries)
├── infrastructure/   # JDBC adapters + SQL queries (text blocks)
├── views/            # Vaadin UI layer (auth, deck, practice, stats)
└── config/           # Spring configuration (security, cache, PWA)
```

**165+ classes** with strict layer separation and zero circular dependencies.

</details>

<details>
<summary><b>⚡ Key Features</b> — Multi-tier caching, security, i18n (click to expand)</summary>

**Multi-Tier Caching:**
- @SessionScope (user data across tabs) + @UIScope (deck data per tab)
- Caffeine for pagination counts
- Event-driven invalidation (Spring Events) + TTL fallback (1-5min)

**Security:**
- Spring Security (form auth + remember-me)
- @RolesAllowed on routes (USER, ADMIN)
- OWASP compliance (SQL injection prevention, XSS protection)

**Internationalization:**
- 3 languages (en, es, ru) via ResourceBundle
- Real-time language switcher
- Vaadin getTranslation() integration

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
./gradlew jibDockerBuild    # Build image
docker-compose up -d        # Run everything
docker-compose logs -f app  # View logs
```

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

## Why These Technologies?

| Decision | Why (Simple Explanation) |
|-----------|--------------------------|
| **Spring Data JDBC** | You write SQL explicitly → see what happens, no N+1 surprises |
| **Text Blocks** | SQL looks clean, not concatenated strings |
| **@UIScope Cache** | Each browser tab has its own cache |
| **Virtual Threads** | Java 21 feature for handling many concurrent requests |
| **TestContainers** | Real PostgreSQL in tests (not mocks) → high confidence |
| **Records** | Immutable DTOs with zero boilerplate |
| **Spring Events** | Update cache immediately when data changes (no polling) |

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

- [ ] Spaced repetition algorithm (SM-2, Anki-style)
- [ ] Audio pronunciation + Image uploads
- [ ] Deck sharing & import/export
- [ ] AI-powered card generation (GPT-4 API)
- [ ] Progress analytics dashboard
- [ ] Mobile apps (Vaadin Hilla)

</details>

---

## Learning Path for Juniors

**Start here if you're new to Java web development:**

1. **Basic understanding** — Start with `views/` folder (UI)
2. **Service layer** — See how `service/` implements business logic
3. **Database access** — Learn `infrastructure/repository/` (JDBC adapters)
4. **Domain model** — Study `model/` and `domain/` (business entities)

**Try these exercises:**
- Add a new card field (note how changes propagate through layers)
- Implement a new API endpoint (follow existing patterns)
- Add cache invalidation for your feature (use Spring Events)

## For Educators

**This project is ideal for teaching:**
- ✅ Clean Architecture in practice (not just theory)
- ✅ How layers depend on each other
- ✅ Transaction boundaries and service layer patterns
- ✅ Testing strategies (unit + integration with real database)
- ✅ Modern Java features (Records, Text Blocks, Pattern Matching)

**Teaching tips:**
1. Start with `DeckView` → see how UI calls services
2. Trace flow: User clicks "Create Deck" → which service method? → which repository?
3. Explain why `@Transactional` is on service, not repository
4. Show how cache invalidation works via events (decoupled!)

## About

Built as a portfolio project to demonstrate:
- Clean Architecture in real-world application
- Java 21 features in production
- Caching strategies and performance optimization
- Full deployment cycle (code → Docker → VPS)

**Why source-available, not open-source?** This is a learning portfolio. Code is open for study and contributions, but forking would dilute the project's educational purpose.

---

## Contributing

Contributions welcome:
- 🐛 Bug reports → [GitHub Issues](../../issues)
- 💻 Pull Requests → See [CONTRIBUTING.md](CONTRIBUTING.md)
- 🌍 Translations (add new languages)
- 💡 Feature suggestions

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup.

---

## License

**Source Available License** — You can view the code and contribute improvements, but independent forks and commercial use are not permitted.  
See [LICENSE](LICENSE) for details.

---

<div align="center">

⭐ Found this helpful for learning? Star the repo!

[Live Demo](https://memocards.duckdns.org) • [Report Bug](../../issues) • [Learn More](CONTRIBUTING.md)

</div>

