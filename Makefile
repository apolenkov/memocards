# =============================================================================
# VARIABLES & CONFIGURATION
# =============================================================================
GRADLE := ./gradlew
DOCKER_COMPOSE := docker-compose
APP_PORT := 8080
PROFILE_DEV := --spring.profiles.active=dev

# =============================================================================
# PHONY TARGETS
# =============================================================================
.PHONY: help start stop restart logs clean build run dev test format check \
        code-quality code-quality-full code-quality-chars \
        coverage coverage-verify deps npm-install vaadin-prepare dev-setup quality-check \
        lint-css lint-css-fix spotless-check sonarlint \
        spotbugs checkstyle vaadin-build-frontend erase

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

run: start ## Start application with database
	@echo "Starting application..."
	$(GRADLE) bootRun

dev: start ## Run in development mode
	@echo "Starting in development mode..."
	$(GRADLE) bootRun --args='$(PROFILE_DEV)'


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

code-quality-full: ## Run all code quality checks including CSS linting
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

vaadin-build-frontend: ## Build Vaadin frontend bundle
	$(GRADLE) vaadinBuildFrontend

# =============================================================================
# CODE ANALYSIS - Individual Tools
# =============================================================================
sonarlint: ## Run SonarLint analysis for main and test classes
	$(GRADLE) sonarlintMain sonarlintTest

spotbugs: ## Run SpotBugs analysis for main and test classes
	$(GRADLE) spotbugsMain spotbugsTest

checkstyle: ## Run Checkstyle analysis for main and test classes
	$(GRADLE) checkstyleMain checkstyleTest