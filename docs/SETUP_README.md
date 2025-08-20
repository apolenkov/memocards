# PostgreSQL Setup for Flashcards

## Overview

## Quick Start

### 1. Start PostgreSQL

```bash
# Start PostgreSQL
make start

# Or manually
docker-compose up -d postgres
```

### 2. Start Application

```bash
# Start application (will automatically start PostgreSQL)
make run

# Or in development mode
make dev

# Or manually
./gradlew bootRun
```

## Available Commands

```bash
make help          # Show help
make start         # Start PostgreSQL
make stop          # Stop PostgreSQL
make restart       # Restart PostgreSQL
make logs          # Show PostgreSQL logs
make clean         # Clean all data
make build         # Build application
make run           # Run application
make dev           # Run in development mode
make test          # Run tests
make test-with-db  # Run tests with database
```

## Database Configuration

### Dev Profile
- **URL**: `jdbc:postgresql://localhost:5432/flashcards`
- **User**: `postgres`
- **Password**: `postgres`

### JPA Profile (for tests)
- **URL**: `jdbc:postgresql://localhost:5432/flashcards`
- **User**: `postgres`
- **Password**: `postgres`

### Prod Profile
- **URL**: `${DB_URL}` (from environment variables)
- **User**: `${DB_USER}` (from environment variables)
- **Password**: `${DB_PASS}` (from environment variables)

## Docker Structure

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

## Migrations

All migrations are now compatible with PostgreSQL:
- Uses `BIGSERIAL` for auto-incrementing IDs
- Supports all PostgreSQL capabilities
- Flyway automatically applies migrations

## Troubleshooting

### PostgreSQL won't start
```bash
# Check logs
make logs

# Restart
make restart
```

### Connection errors
```bash
# Check status
docker-compose ps

# Check readiness
docker-compose exec postgres pg_isready -U postgres
```

### Data cleanup
```bash
# Full cleanup (will delete all data!)
make clean
```

## Environment Variables

For production, you can override settings:

```bash
export DB_URL=jdbc:postgresql://your-host:5432/your-db
export DB_USER=your-user
export DB_PASS=your-password
```

## Security

⚠️ **Warning**: Simple passwords are used in dev environment. For production, always use complex passwords and restrict database access.
