.PHONY: help start stop restart logs clean build run dev dev-auth test test-with-db full-clean-run clean-run deep-clean-run lint-css lint-css-fix format check ci

help: ## Show help
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Infrastructure targets
start:
	docker-compose up -d postgres
	@echo "PostgreSQL started. Waiting for readiness..."
	@until docker-compose exec -T postgres pg_isready -U postgres; do sleep 1; done
	@echo "PostgreSQL is ready!"

stop:
	docker-compose stop postgres

restart: stop start

logs:
	docker-compose logs -f postgres

clean:
	docker-compose down -v
	@echo "All PostgreSQL data removed"

# Application targets
build:
	./gradlew clean build

ci: ## Clean, check and build (CI pipeline)
	./gradlew clean check build

run: start
	@echo "Starting application..."
	./gradlew bootRun

dev: start ## Run in development mode
	@echo "Starting in development mode..."
	./gradlew bootRun --args='--spring.profiles.active=dev'

dev-auth: start ## Run with auto-login enabled
	@echo "Starting with auto-login enabled..."
	DEV_AUTO_LOGIN_ENABLED=true ./gradlew bootRun --args='--spring.profiles.active=dev'

clean-dev: clean start ## Fast clean and run (Gradle clean only)
	@echo "Cleaning (fast) and running..."
	@kill -9 $$(lsof -t -i:8080) 2>/dev/null || true; \
    rm -rf logs; \
    ./gradlew cleanQuick bootRun

deep-clean-dev: clean start ## Deep clean (Vaadin + frontend) and run
	@echo "Deep cleaning and running..."
	@kill -9 $$(lsof -t -i:8080) 2>/dev/null || true; \
    rm -rf logs; \
    ./gradlew deepClean bootRun

full-clean-dev: clean start ## Full clean, format, check, build and run
	@echo "Performing full clean, format, check, build and run..."
	@kill -9 $$(lsof -t -i:8080) 2>/dev/null || true; \
    rm -rf logs; \
    ./gradlew deepClean spotlessApply check build bootRun

# Test targets
test:
	./gradlew test

# Quality & Lint targets
lint-css: ## Run CSS linter (stylelint via Gradle)
	./gradlew lintCss

lint-css-fix: ## Run CSS linter with auto-fix
	./gradlew lintCssFix

format: ## Apply code formatters (Spotless)
	./gradlew spotlessApply

check: ## Run full verification (Spotless check, CSS lint, tests, coverage)
	./gradlew check
