# =============================================================================
# VARIABLES & CONFIGURATION
# =============================================================================
GRADLE := ./gradlew
DOCKER_COMPOSE := docker-compose
APP_PORT := 8080

# =============================================================================
# PHONY TARGETS
# =============================================================================
.PHONY: help start stop restart logs clean build test format check \
        code-quality coverage deps npm-install vaadin-prepare dev-setup \
        lint-css lint-yaml spotless-check sonarlint spotbugs checkstyle \
        vaadin-build-frontend vaadin-clean erase docker docker-stop \
        docker-logs docker-status jib jib-push

# =============================================================================
# HELP
# =============================================================================
help: ## Show help
	@echo "Available commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-25s\033[0m %s\n", $$1, $$2}'

# =============================================================================
# INFRASTRUCTURE - Database Management
# =============================================================================
start: ## Start PostgreSQL database
	$(DOCKER_COMPOSE) up -d postgres
	@echo "PostgreSQL started. Waiting for readiness..."
	@until $(DOCKER_COMPOSE) exec -T postgres pg_isready -U postgres; do sleep 1; done
	@echo "PostgreSQL is ready!"

stop: ## Stop PostgreSQL database
	$(DOCKER_COMPOSE) stop postgres

restart: stop start ## Restart PostgreSQL database

logs: ## Show PostgreSQL logs
	$(DOCKER_COMPOSE) logs -f postgres

erase: ## Remove all PostgreSQL data
	$(DOCKER_COMPOSE) down -v
	@echo "All PostgreSQL data removed"

# =============================================================================
# CLEANUP UTILITIES
# =============================================================================
clean: ## Clean up environment
	@echo "Cleaning up environment..."
	@kill -9 $$(lsof -t -i:$(APP_PORT)) 2>/dev/null || true
	@rm -rf logs
	$(GRADLE) clean

# =============================================================================
# APPLICATION - Build & Run
# =============================================================================
build: ## Build application
	$(GRADLE) clean build

build-prod: ## Build application for production
	@echo "Building application for production..."
	$(GRADLE) clean build -Pvaadin.productionMode=true

# =============================================================================
# DEVELOPMENT SETUP - Environment Preparation
# =============================================================================
dev-setup: start npm-install vaadin-prepare ## Setup development environment
	@echo "Development environment setup complete!"

# =============================================================================
# TESTING
# =============================================================================
test: ## Run unit tests
	$(GRADLE) test

# =============================================================================
# CODE QUALITY - Static Analysis & Linting
# =============================================================================
code-quality: ## Run all code quality checks (SonarLint, SpotBugs, Checkstyle)
	$(GRADLE) codeQuality

lint-css: ## Run CSS linter (stylelint via Gradle)
	$(GRADLE) lintCss

lint-css-fix: ## Run CSS linter with auto-fix
	$(GRADLE) lintCssFix

lint-yaml: ## Run YAML linter for Ansible files
	$(GRADLE) lintYaml

lint-yaml-fix: ## Run YAML linter with auto-fix (prettier + yamllint)
	$(GRADLE) lintYamlFix

code-quality-full: ## Run all code quality checks including CSS and YAML linting
	$(GRADLE) codeQualityFull

# =============================================================================
# CODE FORMATTING & STYLE
# =============================================================================
format: ## Apply code formatters (Spotless)
	$(GRADLE) spotlessApply

spotless-check: ## Check code formatting without applying changes
	$(GRADLE) spotlessCheck

# =============================================================================
# COMPREHENSIVE VERIFICATION
# =============================================================================
check: ## Run full verification (Spotless check, CSS lint, tests, coverage)
	$(GRADLE) check

quality-check: code-quality-full coverage ## Run comprehensive quality checks and coverage
	@echo "Quality check complete!"

# =============================================================================
# TEST COVERAGE
# =============================================================================
coverage: ## Generate test coverage report
	$(GRADLE) jacocoTestReport

coverage-verify: ## Verify test coverage meets requirements
	$(GRADLE) jacocoTestCoverageVerification

# =============================================================================
# DEPENDENCY MANAGEMENT
# =============================================================================
deps: ## Show managed dependency versions
	$(GRADLE) managedVersions

npm-install: ## Install npm dependencies
	$(GRADLE) npmInstall

vaadin-prepare: ## Prepare Vaadin frontend
	$(GRADLE) vaadinPrepareFrontend

vaadin-build-frontend: ## Build Vaadin frontend bundle (dev mode)
	$(GRADLE) vaadinBuildFrontend

vaadin-build-prod: ## Build Vaadin frontend bundle for production
	@echo "Building Vaadin frontend for production..."
	$(GRADLE) clean vaadinBuildFrontend -Pvaadin.productionMode=true

vaadin-clean: ## Clean Vaadin generated files
	$(GRADLE) vaadinClean

# =============================================================================
# CODE ANALYSIS - Individual Tools
# =============================================================================
sonarlint: ## Run SonarLint analysis for main and test classes
	$(GRADLE) sonarlintMain sonarlintTest

spotbugs: ## Run SpotBugs analysis for main and test classes
	$(GRADLE) spotbugsMain spotbugsTest

checkstyle: ## Run Checkstyle analysis for main and test classes
	$(GRADLE) checkstyleMain checkstyleTest

# =============================================================================
# DEVELOPMENT COMMANDS
# =============================================================================
dev: start ## Start application in development mode
	@echo "Starting application in development mode..."
	$(GRADLE) bootRun --args="--spring.profiles.active=dev"

prod: start vaadin-build-prod ## Start application in production mode
	@echo "Starting application in production mode..."
	$(GRADLE) bootRun --args="--spring.profiles.active=prod"

# =============================================================================
# DIAGNOSTICS & VERIFICATION
# =============================================================================
check-app: ## Check if application is running
	@echo "Checking application status..."
	@curl -s -o /dev/null -w "%{http_code}" http://localhost:$(APP_PORT) || echo "Application not responding"

logs-app: ## Show application logs
	@tail -f logs/application.log

logs-errors: ## Show error logs
	@tail -f logs/errors.log

status: ## Show application and database status
	@echo "=== Application Status ==="
	@curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:$(APP_PORT) || echo "Application: Not running"
	@echo "=== Database Status ==="
	@$(DOCKER_COMPOSE) exec -T postgres pg_isready -U postgres 2>/dev/null && echo "Database: Ready" || echo "Database: Not ready"

# =============================================================================
# DOCKER COMMANDS
# =============================================================================
docker: ## Start application with Docker Compose
	@echo "Starting application with Docker Compose..."
	$(DOCKER_COMPOSE) up -d
	@echo "Application started!"
	@echo "Application: http://localhost:$(APP_PORT)"
	@echo "Database: localhost:5432"

docker-stop: ## Stop all Docker Compose services
	@echo "Stopping all Docker Compose services..."
	$(DOCKER_COMPOSE) down
	@echo "All services stopped!"

docker-logs: ## Show Docker Compose logs
	$(DOCKER_COMPOSE) logs -f app

docker-status: ## Show Docker Compose services status
	@echo "=== Docker Compose Services Status ==="
	@$(DOCKER_COMPOSE) ps
	@echo ""
	@echo "=== Application Health Check ==="
	@curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:$(APP_PORT) || echo "Application: Not responding"

# =============================================================================
# INFRASTRUCTURE COMMANDS - Monitoring (Prometheus, Loki, Promtail, Grafana)
# =============================================================================
infra-up: ## Start infrastructure monitoring services
	@echo "Starting infrastructure services..."
	docker compose -f docker-compose.infrastructure.yml up -d
	@echo "Infrastructure services started!"
	@echo "Grafana: http://localhost:3000"
	@echo "Prometheus: http://localhost:9090"

infra-down: ## Stop infrastructure monitoring services
	@echo "Stopping infrastructure services..."
	docker compose -f docker-compose.infrastructure.yml down
	@echo "Infrastructure services stopped!"

infra-logs: ## Show infrastructure services logs
	docker compose -f docker-compose.infrastructure.yml logs -f

infra-status: ## Show infrastructure services status
	@echo "=== Infrastructure Services Status ==="
	@docker compose -f docker-compose.infrastructure.yml ps

# =============================================================================
# FULL STACK COMMANDS - Application + Infrastructure
# =============================================================================
stack-up: ## Start full stack (app creates network, then infrastructure)
	@echo "Starting full stack..."
	@echo "Step 1: Starting application (creates network)..."
	@make docker
	@echo "Step 2: Starting infrastructure (uses network)..."
	@make infra-up
	@echo "✅ Full stack started!"

stack-down: ## Stop full stack (infrastructure first, then app)
	@echo "Stopping full stack..."
	@echo "Step 1: Stopping infrastructure..."
	@make infra-down
	@echo "Step 2: Stopping application..."
	@make docker-stop
	@echo "✅ Full stack stopped!"

stack-status: ## Show full stack status
	@make docker-status
	@echo ""
	@make infra-status

network-create: ## Create shared Docker network (manual, usually not needed)
	@echo "Creating shared Docker network..."
	@docker network create memocards-network --subnet=172.20.0.0/16 --driver=bridge 2>/dev/null || echo "Network already exists"
	@echo "Network created! (Note: network will be created automatically by docker-compose.yml)"

network-rm: ## Remove shared Docker network
	@echo "Removing shared Docker network..."
	@docker network rm memocards-network 2>/dev/null || echo "Network does not exist"
	@echo "Network removed!"

# =============================================================================
# JIB CONTAINERIZATION COMMANDS
# =============================================================================
jib: ## Build Docker image with Jib (local)
	@echo "Building Docker image locally..."
	$(GRADLE) jibDockerBuild
	@echo ""
	@echo "Docker image built successfully!"
	@echo "Available tags:"
	@docker images ghcr.io/apolenkov/memocards --format "  - {{.Repository}}:{{.Tag}} ({{.ID}}, {{.CreatedSince}})"

jib-push: ## Build and push Docker image to registry
	@echo "Building and pushing Docker image..."
	@if [ -f .env ]; then \
		set -a && \
		. ./.env && \
		set +a && \
		$(GRADLE) jib \
			-PGITHUB_TOKEN="$$GITHUB_TOKEN" \
			-PGITHUB_ACTOR="$$GITHUB_ACTOR" \
			-PGITHUB_REPOSITORY="$$GITHUB_REPOSITORY" \
			-PGITHUB_SHA="$$GITHUB_SHA"; \
	else \
		$(GRADLE) jib; \
	fi
	@echo "Docker image pushed successfully!"

docker-images: ## Show available Docker images
	@echo "=== Available Memocards Images ==="
	@docker images ghcr.io/apolenkov/memocards --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}"

docker-clean: ## Remove old Docker images (keep latest 3)
	@echo "Cleaning old Docker images..."
	@docker images ghcr.io/apolenkov/memocards --format "{{.ID}}" | tail -n +4 | xargs -r docker rmi -f || true
	@echo "Cleanup completed!"
