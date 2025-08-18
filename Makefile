.PHONY: help start stop restart logs clean build run

help: ## Показать справку
	@echo "Доступные команды:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

start: ## Запустить PostgreSQL в Docker
	docker-compose up -d postgres
	@echo "PostgreSQL запущен. Ожидание готовности..."
	@until docker-compose exec -T postgres pg_isready -U postgres; do sleep 1; done
	@echo "PostgreSQL готов!"

stop: ## Остановить PostgreSQL
	docker-compose stop postgres

restart: stop start ## Перезапустить PostgreSQL

logs: ## Показать логи PostgreSQL
	docker-compose logs -f postgres

clean: ## Очистить все данные PostgreSQL
	docker-compose down -v
	@echo "Все данные PostgreSQL удалены"

build: ## Собрать приложение
	./gradlew clean build

run: start ## Запустить приложение (сначала PostgreSQL)
	@echo "Запуск приложения..."
	./gradlew bootRun

dev: start ## Запустить в режиме разработки
	@echo "Запуск в режиме разработки..."
	./gradlew bootRun --args='--spring.profiles.active=dev'

test: ## Запустить тесты
	./gradlew test

test-with-db: start ## Запустить тесты с базой данных
	@echo "Запуск тестов с PostgreSQL..."
	./gradlew test
