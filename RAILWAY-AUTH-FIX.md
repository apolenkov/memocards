# Railway Docker Authentication Fix

## Проблема
Railway получает 401 Unauthorized при попытке скачать образ из GitHub Container Registry.

## Решение 1: Сделать Образ Публичным (Рекомендуемый)

1. **Перейдите в GitHub** → Packages → memocards
2. **Нажмите на пакет** memocards
3. **Settings** → **Change visibility** → **Public**
4. **Подтвердите** изменение

## Решение 2: Настроить Авторизацию в Railway

Если хотите оставить образ приватным:

1. **В Railway Dashboard**:
   - Перейдите в ваш проект
   - Settings → Variables
   - Добавьте переменную: `DOCKER_USERNAME` = ваш GitHub username
   - Добавьте переменную: `DOCKER_PASSWORD` = ваш GitHub Personal Access Token

2. **Обновите Dockerfile**:
```dockerfile
FROM ghcr.io/apolenkov/memocards:latest
# Railway автоматически использует DOCKER_USERNAME и DOCKER_PASSWORD
```

## Решение 3: Использовать Публичный Базовый Образ

Если образ еще не существует, используйте временный подход:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Рекомендация
Используйте **Решение 1** - сделайте образ публичным. Это проще и безопаснее для open-source проекта.
