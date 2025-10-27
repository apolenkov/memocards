# Memocards - Flashcard Learning Application

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24+-blue.svg)](https://vaadin.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License: Proprietary](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)

Modern flashcard learning application built with Java 21, Spring Boot 3.x, Vaadin 24+, and PostgreSQL. Demonstrates enterprise-grade clean architecture, caching strategies, and responsive UI design.

**This is a source-available educational project showcasing modern Java development practices.**

## Overview

Memocards helps users efficiently learn new words and phrases using spaced repetition. Create decks, add cards, practice, and track your progress with detailed statistics. Features internationalization support (English, Spanish, Russian) and a responsive, mobile-first design.

### Key Highlights
- 🏗️ **Clean Architecture** with strict layer separation
- ⚡ **Java 21 Features**: Virtual Threads, Records, Pattern Matching
- 🎨 **Modern UI**: Vaadin 24+ with mobile-first responsive design
- 🚀 **High Performance**: Multi-tier caching with event-driven invalidation
- 🔒 **Security**: Spring Security with role-based access control
- 🌍 **i18n Support**: English, Spanish, Russian
- 📦 **Containerized**: Docker + Docker Compose ready

## Technology Stack

### Backend
- **Java 21**: Virtual Threads, Records, Pattern Matching, Text Blocks
- **Spring Boot 3.x**: Dependency Injection, Security, Transactions
- **Spring Data JDBC**: Explicit SQL control (NOT JPA) for better performance
- **PostgreSQL 16**: Database with Flyway migrations
- **Spring Events**: Event-driven cache invalidation
- **Caffeine**: High-performance caching library

### Frontend
- **Vaadin 24+**: Server-side UI framework, PWA-ready
- **Lumo Theme**: Material Design inspired theme
- **i18n**: ResourceBundle-based internationalization (en, es, ru)
- **Responsive**: Mobile-first design with CSS Grid and Flexbox

### Architecture
- **Clean Architecture**: Strict layer separation (domain → usecase → infrastructure → views)
- **SOLID Principles**: Dependency inversion, single responsibility
- **Repository Pattern**: Ports + Adapters (Hexagonal Architecture)
- **Event-Driven**: Cache invalidation via Spring Events

### Testing
- **JUnit 5 + Mockito**: Unit and integration testing
- **TestContainers**: Real PostgreSQL for integration tests
- **Code Quality**: Checkstyle + SpotBugs + SonarLint

### DevOps
- **Docker + Docker Compose**: Containerized deployment
- **Jib**: Containerless Docker builds
- **GitHub Actions**: CI/CD ready
- **Prometheus**: Metrics collection

## Architecture

```
src/main/java/org/apolenkov/application/
├── domain/              # Pure business logic
│   ├── port/           # Repository interfaces (Hexagonal Architecture)
│   ├── usecase/         # Use cases contracts
│   ├── event/          # Domain events (cache invalidation)
│   └── model/          # Domain enums (FilterOption, PracticeDirection)
├── model/              # Domain entities (Card, Deck, User, News)
├── service/            # Use case implementations + business services
│   ├── card/           # Card operations
│   ├── deck/            # Deck operations
│   ├── user/            # User management
│   ├── stats/           # Statistics tracking + caching
│   └── auth/             # Authentication
├── infrastructure/     # Technical implementations
│   └── repository/     # JDBC adapters + SQL queries (text blocks)
├── views/              # Vaadin UI layer
│   ├── auth/           # Login, Register, Password Reset
│   ├── deck/            # Deck CRUD + Card management
│   ├── practice/        # Practice sessions (flashcards)
│   ├── stats/           # Progress statistics
│   ├── admin/           # Admin panel (news management)
│   └── core/            # Shared UI (layout, navigation, errors)
└── config/             # Spring configuration
    ├── security/        # Spring Security setup
    ├── cache/           # Caffeine + custom caching
    ├── seed/            # Demo data initialization
    └── vaadin/          # Vaadin PWA shell
```

## Key Features

### Multi-Tier Caching
- **@SessionScope**: User data caching across tabs
- **@UIScope**: Deck data caching per browser tab
- **Caffeine**: High-performance pagination count cache
- **Event-Driven Invalidation**: Immediate cache updates via Spring Events
- **TTL Fallback**: Time-based expiration (1-5 minutes)
- **Metrics**: Track hit/miss rates for performance monitoring

### Database Design
- **Spring Data JDBC**: Explicit SQL control with text blocks
- **Flyway Migrations**: Versioned schema management (V1-V6)
- **Text Blocks**: Readable, maintainable SQL queries
- **RowMapper Pattern**: Thread-safe DTO mapping
- **Batch Operations**: Efficient bulk updates

### Security
- **Spring Security**: Form-based authentication + remember-me
- **Role-Based Access**: @RolesAllowed on routes (USER, ADMIN)
- **Password Reset**: Secure token-based password recovery
- **OWASP Compliance**: SQL injection prevention, XSS protection

### Internationalization
- **ResourceBundle**: messages_en.properties, messages_es.properties, messages_ru.properties
- **UI Language Switcher**: Real-time language switching
- **Demo Data**: Multilingual card examples
- **getTranslation()**: Vaadin i18n integration

## Quick Start

### Prerequisites
- Java 21+ (JDK recommended: Eclipse Temurin, Amazon Corretto)
- Docker + Docker Compose
- (Optional) Node.js 20+ for frontend development

### Local Development

1. **Clone repository**
   ```bash
   git clone <repository-url>
   cd memo
   ```

2. **Configure environment**
   ```bash
   cp env.sample .env
   # Edit .env with minimal setup:
   DB_PASSWORD=your_secure_password
   DEMO_ADMIN_PASSWORD=admin
   DEMO_USER_PASSWORD=user
   ```

3. **Start PostgreSQL**
   ```bash
   docker-compose up -d postgres
   ```

4. **Run application**
   ```bash
   ./gradlew bootRun
   ```

5. **Open browser**
   ```
   http://localhost:8080
   ```

6. **Demo credentials**
   - **User**: `user@example.com` / `user`
   - **Admin**: `admin@example.com` / `admin`

### Docker Compose (Full Stack)

```bash
# Build Docker image
./gradlew jibDockerBuild

# Run everything (app + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

## Testing

```bash
# Unit tests
./gradlew test

# Integration tests (requires Docker)
./gradlew integrationTest

# Code quality checks
./gradlew codeQuality

# All checks (tests + quality)
./gradlew check
```

## Configuration

All configuration is environment-based via `.env` file. See `env.sample` for available options.

### Key Settings

**Cache Configuration**:
```properties
# Cache TTL (milliseconds)
CACHE_DECKS_TTL=60000           # 1 minute for deck cache
CACHE_KNOWN_CARDS_TTL=300000    # 5 minutes for known cards
CACHE_PAGINATION_COUNT_TTL=60000 # 1 minute for pagination

# Cache size
CACHE_USER_MAX_SIZE=1000
CACHE_DECKS_MAX_SIZE=1000
```

**UI Configuration**:
```properties
SEARCH_DEBOUNCE_MS=300          # Search field debounce
PAGINATION_PAGE_SIZE=50         # Items per page
```

**Security Configuration**:
```properties
MAX_FAILED_ATTEMPTS=5           # Login failure threshold
FAILED_ATTEMPTS_WINDOW=15       # Reset window (minutes)
```

### Spring Profiles
- **dev**: Development mode (DEBUG logging, hot reload)
- **test**: Test mode (INFO logging)
- **prod**: Production mode (WARN logging, optimized)

## Project Structure

- **165+ Java classes**: Well-organized across layers
- **Clean Architecture boundaries**: Strictly enforced, no circular dependencies
- **Repository Pattern**: Ports define contracts, Adapters provide implementations
- **Service Layer**: @Transactional boundaries on business operations
- **Event-Driven**: Spring Events for decoupled communication

## Notable Technical Decisions

1. **Spring Data JDBC over JPA**: Explicit SQL control, better performance, simpler debugging
2. **Text Blocks for SQL**: Java 15+ feature for multi-line, readable SQL queries
3. **@UIScope Caching**: Vaadin-specific per-tab caching for optimal UX
4. **Virtual Threads**: Java 21 feature for high scalability (enabled in application.yml)
5. **TestContainers**: Real PostgreSQL in tests (no mocking of database layer)
6. **Record DTOs**: Immutable data transfer objects with static factory methods
7. **Event-Driven Invalidation**: Immediate cache updates without polling

## Development Workflow

```bash
# Run with auto-reload
./gradlew bootRun

# Format code (Spotless)
./gradlew spotlessApply

# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Run SpotBugs
./gradlew spotbugsMain spotbugsTest

# Build production bundle
./gradlew vaadinBuildFrontend

# Build Docker image
./gradlew jibDockerBuild
```

## Contributing

We welcome contributions from the community! This is a source-available educational project, and we appreciate:

- 🐛 Bug reports and feature requests via [GitHub Issues](../../issues)
- 📝 Documentation improvements
- 💻 Code contributions via Pull Requests to the official repository
- 🌍 Translations for new languages
- 💡 Ideas for new features

**Note**: This project uses a proprietary license. You can view the source code and contribute improvements, but forking for independent use is not permitted. See [LICENSE](LICENSE) for details.

### How to Contribute

1. **Fork the repository** (for contribution purposes only)
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes** following our coding standards
4. **Run tests** (`./gradlew test`)
5. **Run code quality checks** (`./gradlew codeQuality`)
6. **Commit your changes** (`git commit -m 'Add amazing feature'`)
7. **Push to your branch** (`git push origin feature/amazing-feature`)
8. **Open a Pull Request** to the official repository

### Coding Standards

- Follow existing code style (Spotless auto-formatting)
- Write meaningful Javadoc for public APIs
- Add unit tests for new features
- Ensure all tests pass before submitting PR
- Keep commits atomic and well-described

### Code Quality Tools

All contributions are checked with:
- **Checkstyle**: Java code style
- **SpotBugs**: Bug detection
- **SonarLint**: Code quality analysis
- **Spotless**: Auto-formatting

Run all checks: `./gradlew check`

## Roadmap

Future enhancements planned:

- [ ] Mobile apps (iOS/Android) using Vaadin Hilla
- [ ] Spaced repetition algorithm (SM-2, Anki-style)
- [ ] Audio pronunciation support
- [ ] Image upload for cards
- [ ] Deck sharing and import/export
- [ ] AI-powered card generation
- [ ] Progress analytics dashboard
- [ ] Social features (leaderboards, challenges)

## Community

- 💬 **Discussions**: [GitHub Discussions](../../discussions)
- 🐛 **Issues**: [GitHub Issues](../../issues)
- 📧 **Contact**: [Create an issue](../../issues/new)

## Acknowledgments

Built with:
- [Vaadin](https://vaadin.com/) - Modern web framework
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [PostgreSQL](https://www.postgresql.org/) - Database
- [TestContainers](https://www.testcontainers.org/) - Integration testing
- [Lumo](https://vaadin.com/docs/latest/styling/lumo) - Design system

## License

This project is licensed under a proprietary Source Available License - see [LICENSE](LICENSE) file for details.

**TL;DR**: You can view the code and contribute improvements, but you cannot fork this project for independent use or commercial purposes. All releases are published exclusively by the project owner.

---

**⭐ If you find this project helpful, please give it a star!**

**🤝 Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.**

