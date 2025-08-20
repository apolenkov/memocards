.PHONY: help start stop restart logs clean build run

help: ## Show help
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

start: ## Start PostgreSQL in Docker
	docker-compose up -d postgres
	@echo "PostgreSQL started. Waiting for readiness..."
	@until docker-compose exec -T postgres pg_isready -U postgres; do sleep 1; done
	@echo "PostgreSQL is ready!"

stop: ## Stop PostgreSQL
	docker-compose stop postgres

restart: stop start ## Restart PostgreSQL

logs: ## Show PostgreSQL logs
	docker-compose logs -f postgres

clean: ## Clean all PostgreSQL data
	docker-compose down -v
	@echo "All PostgreSQL data removed"

build: ## Build application
	./gradlew clean build

run: start ## Run application (start PostgreSQL first)
	@echo "Starting application..."
	./gradlew bootRun

dev: start ## Run in development mode
	@echo "Starting in development mode..."
	./gradlew bootRun --args='--spring.profiles.active=dev'

test: ## Run tests
	./gradlew test

test-with-db: start ## Run tests with database
	@echo "Running tests with PostgreSQL..."
	./gradlew test

full-clean-run: ## Full clean, format, check, build and run
	@echo "Performing full clean, format, check, build and run..."
	 kill -9 $(lsof -t -i:8080) && rm -rf logs && ./gradlew clean spotlessApply
	 check build bootRun

clean-run: ## Clean and run
	@echo "Cleaning and running..."
	@kill -9 $$(lsof -t -i:8080) 2>/dev/null || true; \
    rm -rf logs; \
    ./gradlew clean bootRun
