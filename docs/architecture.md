# Memocards - Architecture Documentation

## System Overview

Memocards is a flashcard learning application built with Clean Architecture principles, leveraging Spring Boot 3.5, Vaadin 24.9, and PostgreSQL 16.

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
        UI[Vaadin 24.9<br/>Server-Side UI<br/>@Push<br/>@UIScope Cache]
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
    
    UI --> DeckView
    DeckView --> DeckService
    DeckService --> JDBC
    JDBC --> PG
    
    DeckService --> Events
    Events --> Cache
    Cache --> StatsService
    
    style CardService fill:#4caf50,stroke:#388e3c,stroke-width:2px
    style Cache fill:#ff9800,stroke:#f57c00,stroke-width:2px
    style PG fill:#336791,stroke:#245a7c,stroke-width:2px
```

**Technologies:**
- **UI**: Vaadin 24.9 with server push
- **Business Logic**: Spring Boot services with @Transactional
- **Data Access**: Spring Data JDBC with explicit SQL control
- **Cache**: Caffeine + custom @UIScope/@SessionScope caches
- **Events**: Spring Events for decoupled cache invalidation

---

### Level 3: Component Diagram

```mermaid
graph TB
    subgraph "Views Layer"
        DeckPages[Deck Pages<br/>DeckListView<br/>DeckEditorView<br/>CardManagementView]
        PracticePages[Practice Pages<br/>PracticeView<br/>PracticeSession]
        StatsPages[Stats Pages<br/>DashboardView]
    end
    
    subgraph "Domain Layer"
        Ports[Repository Interfaces<br/>CardRepository<br/>DeckRepository<br/>StatsRepository]
        Events[Domain Events<br/>ProgressChangedEvent<br/>DeckModifiedEvent]
        Models[Domain Models<br/>Card, Deck, User<br/>PracticeDirection]
    end
    
    subgraph "Service Layer"
        CardUseCase[CardUseCaseService<br/>+ Validation<br/>+ Event Publishing]
        DeckUseCase[DeckUseCaseService<br/>+ CRUD Operations]
        StatsUseCase[StatsService<br/>+ Caching Strategy]
    end
    
    subgraph "Infrastructure"
        Adapters[JDBC Adapters<br/>CardJdbcAdapter<br/>DeckJdbcAdapter<br/>StatsJdbcAdapter]
        CacheLayer[Cache Layer<br/>KnownCardsCache<br/>UserDecksCache<br/>PaginationCountCache]
    end
    
    DeckPages --> CardUseCase
    CardUseCase --> Ports
    Ports --> Adapters
    
    CardUseCase --> Events
    Events --> CacheLayer
    CacheLayer --> StatsUseCase
    
    style CardUseCase fill:#4caf50,stroke:#388e3c,stroke-width:2px
    style Events fill:#ff9800,stroke:#f57c00,stroke-width:2px
    style CacheLayer fill:#ff9800,stroke:#f57c00,stroke-width:2px
```

**Key Components:**
- **UseCase Services**: Business logic, validation, transaction boundaries
- **JDBC Adapters**: Data access with explicit SQL control
- **Cache Layer**: Event-driven invalidation, multi-scope (UI/Session)

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

```mermaid
graph TB
    subgraph "User Browser"
        Browser[Chrome/Firefox<br/>PWA Installed]
    end
    
    subgraph "VPS Server"
        Nginx[Nginx Reverse Proxy<br/>SSL/TLS]
        
        subgraph "Docker Network"
            App[Memocards App<br/>:8080]
            Postgres[PostgreSQL 16<br/>:5432]
        end
    end
    
    subgraph "Monitoring"
        Prometheus[Prometheus<br/>Metrics Scraping]
        Grafana[Grafana<br/>Dashboards]
    end
    
    Browser -->|HTTPS 443| Nginx
    Nginx -->|HTTP| App
    App -->|JDBC| Postgres
    
    App -->|/actuator/prometheus| Prometheus
    Prometheus --> Grafana
    
    style App fill:#4285f4,stroke:#357ae8,stroke-width:2px
    style Postgres fill:#336791,stroke:#245a7c,stroke-width:2px
```

**Deployment Stack:**
- **Reverse Proxy**: Nginx (SSL termination)
- **Application**: Docker container (Jib multi-arch image)
- **Database**: PostgreSQL 16 with SCRAM-SHA-256
- **Monitoring**: Prometheus + Grafana

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
| **Monitoring** | Actuator + Prometheus | Metrics, health checks |
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

- `docs/architecture.md` - This document
- `grafana/dashboard-memocards.json` - Monitoring dashboard
- `README.md` - Project overview
- `CONTRIBUTING.md` - Contributor guide
