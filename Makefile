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
.PHONY: help start stop restart logs clean build run dev dev-auth test format check ci \
        code-quality code-quality-full code-quality-chars clean-frontend clean-quick deep-clean \
        coverage coverage-verify deps npm-install vaadin-prepare dev-setup quality-check dev-quality \
        clean-dev deep-clean-dev full-clean-dev lint-css lint-css-fix spotless-check sonarlint \
        spotbugs checkstyle vaadin-clean vaadin-build-frontend

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

clean: ## Remove all PostgreSQL data
	$(DOCKER_COMPOSE) down -v
	@echo "All PostgreSQL data removed"

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

dev-auth: start ## Run with auto-login enabled
	@echo "Starting with auto-login enabled..."
	DEV_AUTO_LOGIN_ENABLED=true $(GRADLE) bootRun --args='$(PROFILE_DEV)'

# =============================================================================
# DEVELOPMENT WORKFLOWS - Clean & Run Combinations
# =============================================================================
# Common cleanup function
define cleanup_and_run
	@echo "$(1)"
	@kill -9 $$(lsof -t -i:$(APP_PORT)) 2>/dev/null || true
	@rm -rf logs
	$(2)
endef

clean-dev: clean start ## Fast clean and run (Gradle clean only)
	$(call cleanup_and_run,Cleaning (fast) and running...,$(GRADLE) cleanQuick bootRun)

deep-clean-dev: clean start ## Deep clean (Vaadin + frontend) and run
	$(call cleanup_and_run,Deep cleaning and running...,$(GRADLE) deepClean bootRun)

full-clean-dev: clean start ## Full clean, format, check, build and run
	$(call cleanup_and_run,Performing full clean, format, check, build and run...,$(GRADLE) deepClean spotlessApply check build bootRun)

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

code-quality-chars: ## Run character-based quality checks (non-English, i18n, translations)
	$(GRADLE) codeQualityChars

code-quality-full: ## Run all code quality checks including CSS linting
	$(GRADLE) codeQualityFull

lint-css: ## Run CSS linter (stylelint via Gradle)
	$(GRADLE) lintCss

lint-css-fix: ## Run CSS linter with auto-fix
	$(GRADLE) lintCssFix

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
# CLEANING TASKS - Different Levels of Cleanup
# =============================================================================
clean-frontend: ## Clean Vaadin frontend artifacts and caches
	$(GRADLE) cleanFrontend

clean-quick: ## Fast clean (Gradle build/ only). Preserves node_modules and Vaadin caches
	$(GRADLE) cleanQuick

deep-clean: ## Full clean including Vaadin artifacts and caches
	$(GRADLE) deepClean

vaadin-clean: ## Clean Vaadin project completely
	$(GRADLE) vaadinClean

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

# =============================================================================
# CI/CD PIPELINE
# =============================================================================
ci: ## Clean, check and build (CI pipeline)
	$(GRADLE) clean check build

# =============================================================================
# COMBINED WORKFLOWS - Advanced Development Patterns
# =============================================================================
dev-quality: dev-setup quality-check ## Setup dev environment and run quality checks
	@echo "Development environment setup and quality check complete!"

# =============================================================================
# UTILITY TARGETS
# =============================================================================
# Kill any process using the app port
kill-port: ## Kill any process using the application port
	@kill -9 $$(lsof -t -i:$(APP_PORT)) 2>/dev/null || echo "No process found on port $(APP_PORT)"

# Show current project status
status: ## Show current project status
	@echo "Project: flashcards"
	@echo "Port: $(APP_PORT)"
	@echo "Database: $$(if docker-compose ps postgres | grep -q "Up"; then echo "Running"; else echo "Stopped"; fi)"
	@echo "Port $(APP_PORT): $$(if lsof -i:$(APP_PORT) >/dev/null 2>&1; then echo "In use"; else echo "Available"; fi)"
