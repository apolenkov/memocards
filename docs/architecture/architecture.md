# Memocards - Architecture Documentation

## System Overview

Memocards is a flashcard learning application built with Clean Architecture principles, leveraging Spring Boot 3.5, Vaadin 24.9, and PostgreSQL 16.

Note: Diagrams are Mermaid blocks maintained in this file. Update inline and review via IDE preview or MR diffs (include date of change in commit message).

---

## C4 Model Diagrams

### Level 1: System Context

```mermaid
graph TB
    User[👤 User] 
    Admin[👨‍💼 Admin]
    Browser[🌐 Web Browser]
    
    subgraph "Memocards Application"
        App[Memocards<br/>Spring Boot + Vaadin]
        DB[(🗄️ PostgreSQL)]
    end
    
    User -->|Create/Study Cards| Browser
    Admin -->|Manage Content| Browser
    Browser -->|HTTPS| App
    App -->|JDBC| DB
    
    style App fill:#4285f4,stroke:#357ae8,stroke-width:2px
    style DB fill:#336791,stroke:#245a7c,stroke-width:2px
```

**Actors:**
- **Users**: Create decks, study cards, track progress
- **Admins**: Manage news, moderate content
- **Web Browser**: PWA-capable, offline support

**External Systems:**
- **PostgreSQL 16**: Database (VPS or cloud)

---

### Level 2: Container Diagram

```mermaid
graph TB
    subgraph "Browser"
        UI[Vaadin 24.9<br/>Server-Side UI<br/>@Push<br/>@SessionScope Cache]
    end
    
    subgraph "Infrastructure Layer (Host)"
        Nginx[Nginx<br/>Edge Proxy<br/>SSL Termination]
        Traefik[Traefik<br/>Internal Router<br/>Docker Labels]
    end
    
    subgraph "Application Container"
        subgraph "Views Layer"
            DeckView[Deck Views]
            PracticeView[Practice View]
            StatsView[Stats View]
            AuthView[Auth Views]
        end
        
        subgraph "Service Layer"
            CardService[Card Service<br/>@Transactional]
            DeckService[Deck Service<br/>@Transactional]
            StatsService[Stats Service<br/>Caching]
        end
        
        subgraph "Infrastructure Layer"
            JDBC[JDBC Adapters<br/>+ RowMappers]
            Cache[Caffeine Cache<br/>@UIScope/SessionScope]
            Events[Spring Events<br/>Cache Invalidation]
        end
    end
    
    subgraph "Database"
        PG[(PostgreSQL 16<br/>HikariCP Pool)]
    end
    
    subgraph "Monitoring Containers"
        Prometheus[Prometheus<br/>Metrics Scraping]
        Grafana[Grafana<br/>Dashboards]
        Loki[Loki<br/>Log Aggregation]
    end
    
    UI -->|HTTPS| Nginx
    Nginx -->|HTTP :8080| Traefik
    Traefik -->|Route Matching| DeckView
    
    DeckView --> DeckService
    DeckService --> JDBC
    JDBC --> PG
    
    DeckService --> Events
    Events --> Cache
    Cache --> StatsService
    
    CardService -->|/actuator/prometheus| Prometheus
    Prometheus --> Grafana
    Loki --> Grafana
    Traefik -->|/grafana| Grafana
    Traefik -->|/prometheus| Prometheus
    
    style Nginx fill:#009639,stroke:#007028,stroke-width:2px
    style Traefik fill:#24a1c1,stroke:#1a7c94,stroke-width:2px
    style CardService fill:#4caf50,stroke:#388e3c,stroke-width:2px
    style Cache fill:#ff9800,stroke:#f57c00,stroke-width:2px
    style PG fill:#336791,stroke:#245a7c,stroke-width:2px
```

**Technologies:**
- **Edge Proxy**: Nginx (SSL termination, security headers)
- **Internal Router**: Traefik v3.0 (dynamic service discovery)
- **UI**: Vaadin 24.9 with server push
- **Business Logic**: Spring Boot services with @Transactional
- **Data Access**: Spring Data JDBC with explicit SQL control
- **Cache**: Caffeine + custom @UIScope/@SessionScope caches
- **Events**: Spring Events for decoupled cache invalidation
- **Monitoring**: Prometheus, Grafana, Loki, Promtail

---

### Level 3: Component Diagram

```mermaid
graph TB
    subgraph "Infrastructure Layer (External)"
        Nginx[Nginx<br/>Edge Proxy]
        Traefik[Traefik Router<br/>Docker Labels]
    end
    
    subgraph "Views Layer"
        DeckPages[Deck Pages<br/>DeckListView<br/>DeckEditorView<br/>CardManagementView]
        PracticePages[Practice Pages<br/>PracticeView<br/>PracticeSession]
        StatsPages[Stats Pages<br/>DashboardView]
        AdminPages[Admin Pages<br/>NewsManagementView]
    end
    
    subgraph "Domain Layer"
        Ports[Repository Interfaces<br/>CardRepository<br/>DeckRepository<br/>StatsRepository<br/>NewsRepository]
        Events[Domain Events<br/>ProgressChangedEvent<br/>DeckModifiedEvent]
        Models[Domain Models<br/>Card, Deck, User<br/>News, PracticeDirection]
    end
    
    subgraph "Service Layer"
        CardUseCase[CardUseCaseService<br/>+ Validation<br/>+ Event Publishing]
        DeckUseCase[DeckUseCaseService<br/>+ CRUD Operations]
        StatsUseCase[StatsService<br/>+ Caching Strategy]
        NewsUseCase[NewsService<br/>+ Content Management]
        AuthUseCase[AuthService<br/>+ Password Reset]
    end
    
    subgraph "Infrastructure (Internal)"
        Adapters[JDBC Adapters<br/>CardJdbcAdapter<br/>DeckJdbcAdapter<br/>StatsJdbcAdapter<br/>NewsJdbcAdapter<br/>UserJdbcAdapter]
        CacheLayer[Cache Layer<br/>KnownCardsCache<br/>UserDecksCache<br/>PaginationCountCache]
    end
    
    subgraph "Database & Monitoring"
        PG[(PostgreSQL 16)]
        Prometheus[Prometheus]
        Grafana[Grafana]
    end
    
    Nginx --> Traefik
    Traefik --> DeckPages
    Traefik --> PracticePages
    Traefik --> StatsPages
    Traefik --> AdminPages
    
    DeckPages --> CardUseCase
    PracticePages --> CardUseCase
    StatsPages --> StatsUseCase
    AdminPages --> NewsUseCase
    
    CardUseCase --> Ports
    DeckUseCase --> Ports
    StatsUseCase --> Ports
    NewsUseCase --> Ports
    
    Ports --> Adapters
    Adapters --> PG
    
    CardUseCase --> Events
    Events --> CacheLayer
    CacheLayer --> StatsUseCase
    
    CardUseCase -->|Metrics| Prometheus
    Prometheus --> Grafana
    
    style Traefik fill:#24a1c1,stroke:#1a7c94,stroke-width:2px
    style CardUseCase fill:#4caf50,stroke:#388e3c,stroke-width:2px
    style Events fill:#ff9800,stroke:#f57c00,stroke-width:2px
    style CacheLayer fill:#ff9800,stroke:#f57c00,stroke-width:2px
    style PG fill:#336791,stroke:#245a7c,stroke-width:2px
```

**Key Components:**
- **UseCase Services**: Business logic, validation, transaction boundaries
- **JDBC Adapters**: Data access with explicit SQL control
- **Cache Layer**: Event-driven invalidation, multi-scope (UI/Session)
- **Infrastructure**: Nginx (edge) + Traefik (internal routing)

---

## Database Schema (ER Diagram)

```mermaid
erDiagram
    Users ||--o{ Decks : "owns"
    Users ||--o{ UserRoles : "has"
    Decks ||--o{ Cards : "contains"
    Decks ||--o{ DeckDailyStats : "tracks"
    Users ||--o{ UserSettings : "configures"
    Users ||--o{ PracticeSettings : "configures"
    Decks ||--o{ CardProgress : "tracks"
    
    Users {
        bigserial id PK
        varchar email UK
        varchar password_hash
        varchar name
        timestamp created_at
    }
    
    Decks {
        bigserial id PK
        bigint user_id FK
        varchar title
        varchar description
        timestamp created_at
        timestamp updated_at
    }
    
    Cards {
        bigserial id PK
        bigint deck_id FK
        varchar front_text
        varchar back_text
        varchar example
        varchar image_url
        timestamp created_at
        timestamp updated_at
    }
    
    CardProgress {
        bigint card_id PK,FK
        bigint user_id PK,FK
        timestamp last_reviewed
        int repetition_count
        int ease_factor
    }
    
    DeckDailyStats {
        bigint deck_id PK,FK
        date date PK
        int sessions
        int viewed
        int correct
        int hard
        int easy
    }
```

**Key Relationships:**
- `Users` → `Decks` (1:N): User owns multiple decks
- `Decks` → `Cards` (1:N): Deck contains multiple cards
- `Users` + `Cards` → `CardProgress` (N:M): Tracks user progress per card

---

## Infrastructure

This project uses a hybrid edge + dynamic routing setup. See Deployment Architecture below for the single authoritative infrastructure diagram. Routing specifics are documented in the Routing Rules table.

## Cache Invalidation Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as DeckView
    participant Service as DeckService
    participant Cache as KnownCardsCache
    participant Events as Spring Events
    participant DB as PostgreSQL
    
    User->>UI: Practice card (mark as known)
    UI->>Service: markAsKnown(cardId)
    
    Service->>DB: UPDATE card_progress
    Service->>Events: Publish ProgressChangedEvent
    
    Events->>Cache: onProgressChanged()
    Cache->>Cache: invalidate(deckId)
    
    Note over Cache: Cache cleared immediately<br/>NO cooldown!
    
    Events->>UI: UI.access() after 300ms debounce
    UI->>UI: loadCurrentPage()
    
    Note over UI: User sees updated stats<br/>via server push
```

**Key Principles:**
1. **Immediate Invalidation**: Event → Cache cleared (0ms)
2. **UI Debouncing**: 300ms delay before refresh (UX)
3. **Server Push**: `UI.access()` for real-time updates
4. **No Cooldown in Cache Layer**: Data consistency > performance

---

## Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant UI as LoginView
    participant Security as Spring Security
    participant DB as PostgreSQL
    participant Session as Vaadin Session
    
    User->>UI: Enter credentials
    UI->>Security: authenticate(email, password)
    
    Security->>DB: SELECT * FROM users WHERE email=?
    DB-->>Security: User data
    
    Security->>Security: BCryptPasswordEncoder.matches()
    
    alt Valid credentials
        Security->>Session: Create session
        Security-->>UI: Redirect to /decks
        Note over Session: Session stored<br/>@UIScope caches isolated
    else Invalid credentials
        Security-->>UI: Error message
    end
```

**Security Features:**
- BCrypt password hashing
- Spring Security session management
- MDC logging with userId
- Audit trail for auth events

---

## Deployment Architecture

### Hybrid Architecture (Production)

```mermaid
graph TB
    subgraph "User Browser"
        Browser[Chrome/Firefox<br/>PWA Installed]
    end
    
    subgraph "VPS Server (Host)"
        Nginx[Nginx<br/>SSL Termination<br/>Port 80/443<br/>Security Headers]
    end
    
    subgraph "Docker Network: memocards-network"
        Traefik[Traefik v3.0<br/>Internal Router<br/>Port 8080<br/>Docker Labels Auto-Discovery]
        
        subgraph "Application Services"
            App[Memocards App<br/>:8080<br/>Traefik Labels]
            Postgres[PostgreSQL 16<br/>:5432]
        end
        
        subgraph "Monitoring Services"
            Grafana[Grafana<br/>:3000<br/>/grafana<br/>Traefik Labels]
            Prometheus[Prometheus<br/>:9090<br/>/prometheus<br/>Traefik Labels]
            Loki[Loki<br/>:3100<br/>Log Aggregation]
            Promtail[Promtail<br/>Log Shipping]
        end
    end
    
    Browser -->|HTTPS 443| Nginx
    Nginx -->|HTTP :8080<br/>Single Proxy Pass| Traefik
    
    Traefik -->|PathPrefix /<br/>Host memocards.duckdns.org| App
    Traefik -->|PathPrefix /grafana<br/>StripPrefix Middleware| Grafana
    Traefik -->|PathPrefix /prometheus| Prometheus
    Traefik -->|PathPrefix /traefik| Traefik[Traefik Dashboard<br/>:8082]
    
    App -->|JDBC| Postgres
    App -->|/actuator/prometheus| Prometheus
    Promtail -->|Logs| Loki
    Prometheus --> Grafana
    Loki --> Grafana
    
    style Nginx fill:#009639,stroke:#007028,stroke-width:2px
    style Traefik fill:#24a1c1,stroke:#1a7c94,stroke-width:2px
    style App fill:#4285f4,stroke:#357ae8,stroke-width:2px
    style Postgres fill:#336791,stroke:#245a7c,stroke-width:2px
    style Grafana fill:#f46800,stroke:#e55600,stroke-width:2px
```

**Deployment Stack (Hybrid Architecture):**
- **Edge Proxy**: Nginx (SSL termination, security headers, rate limiting)
- **Internal Router**: Traefik v3.0 (dynamic routing via Docker labels)
- **Application**: Docker container (Jib multi-arch image)
- **Database**: PostgreSQL 16 with SCRAM-SHA-256
- **Monitoring**: Prometheus + Grafana + Loki + Promtail

**Architecture Benefits:**
- ✅ Nginx: Stable SSL termination, security, edge proxy
- ✅ Traefik: Dynamic service discovery, automatic routing
- ✅ No Nginx reloads: Adding services only requires Traefik labels
- ✅ Best of both worlds: Stability + Automation

---

## Internal Routing Architecture

### Traefik Routing Rules

| Service | Rule | Priority | Middleware |
|---------|------|----------|------------|
| **App** | `Host(memocards.duckdns.org) && !PathPrefix(/grafana\|/prometheus\|/traefik)` | 1 | None (fallback) |
| **Grafana** | `PathPrefix(/grafana)` | 10 | `stripPrefix(/grafana)` |
| **Prometheus** | `PathPrefix(/prometheus)` | 10 | None |
| **Traefik Dashboard** | `PathPrefix(/traefik) \|\| PathPrefix(/api)` | 10 | None |

### Service Discovery

All services use **Docker labels** for automatic Traefik discovery:

```yaml
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.service-name.rule=PathPrefix(`/service`)"
  - "traefik.http.routers.service-name.entrypoints=web"
  - "traefik.http.services.service-name.loadbalancer.server.port=8080"
```

**Advantages:**
- ✅ No manual Nginx configuration
- ✅ Dynamic routing without reloads
- ✅ Easy to add new services
- ✅ Automatic health checks

---

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | Vaadin 24.9 | Server-side rendering, PWA |
| **Business Logic** | Spring Boot 3.5 | Dependency injection, transactions |
| **Data Access** | Spring Data JDBC | Explicit SQL control |
| **Database** | PostgreSQL 16 | Relational database |
| **Cache** | Caffeine + Custom | Multi-layer caching |
| **Events** | Spring Events | Decoupled invalidation |
| **Security** | Spring Security | Authentication, authorization |
| **Monitoring** | Actuator + Prometheus + Grafana + Loki | Metrics, logs, dashboards |
| **Reverse Proxy** | Nginx + Traefik (Hybrid) | SSL termination + internal routing |
| **Deployment** | Docker + Ansible | Automated deployment |
| **CI/CD** | GitHub Actions | Automated testing, building |

---

## Architecture Principles

### 1. Clean Architecture
- **Dependency Rule**: Dependencies point inward
- **Layer Independence**: Domain has no external dependencies
- **Testability**: Each layer tested independently

### 2. SOLID Principles
- **Single Responsibility**: One class, one reason to change
- **Dependency Inversion**: Depend on abstractions (Repository interfaces)
- **Open/Closed**: Extensible via interfaces

### 3. Performance
- **Event-Driven Caching**: Immediate invalidation, no polling
- **Connection Pooling**: HikariCP with optimal settings
- **N+1 Prevention**: Batch operations for multiple items
- **Virtual Threads**: Java 21 concurrent processing

### 4. Security
- **Parameterized Queries**: SQL injection prevention
- **HSTS Headers**: HTTPS enforcement
- **BCrypt Hashing**: Secure password storage
- **OWASP Compliance**: Automated scanning

---

## Files

### Documentation Index

- Architecture Overview (this file)
- SLO/SLI/SLA: [slo.md](./slo.md)
- Non-functional Requirements: [non-functional-requirements.md](./non-functional-requirements.md)
- Observability: [observability.md](./observability.md)
- Security & Privacy: [security.md](./security.md)
- Data Lifecycle: [data-lifecycle.md](./data-lifecycle.md)
- API Contracts: [api-contracts.md](./api-contracts.md)
- Release & Environments: [release-and-environments.md](./release-and-environments.md)
- CI/CD: [ci-cd.md](./ci-cd.md)
- Testing Strategy: [testing-strategy.md](./testing-strategy.md)
- Operations Runbooks: [operations-runbooks.md](./operations-runbooks.md)
- UX/i18n/a11y: [ux-i18n-a11y.md](./ux-i18n-a11y.md)
- ADRs: [adr/](./adr/)
- Glossary: [glossary.md](./glossary.md)
- Ownership: [ownership.md](./ownership.md)
- Config Matrix: [config-matrix.md](./config-matrix.md)

Other:
- Grafana dashboard JSON: [dashboard-memocards.json](../grafana/dashboard-memocards.json)
- Project overview: [README.md](../README.md)
- Contributor guide: [CONTRIBUTING.md](../CONTRIBUTING.md)

