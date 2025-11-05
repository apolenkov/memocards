# Архитектурные улучшения

**Подробное описание архитектурных улучшений для Memocards infrastructure**

---

## 🔴 1. Prometheus Security (Docker Socket Proxy)

### Текущая проблема

**Текущая конфигурация:**
```yaml
prometheus:
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro  # Прямой доступ к Docker socket
  user: "0:999"  # Работает от root с docker group
```

**Проблемы:**
1. **Полный доступ к Docker API**: Prometheus имеет доступ ко всем Docker API операциям
2. **Root privileges**: Prometheus работает от root (user: "0:999")
3. **Security risk**: Если Prometheus скомпрометирован, злоумышленник получит полный доступ к Docker
4. **Principle of least privilege**: Нарушение принципа минимальных привилегий

**Риски:**
- Злоумышленник может остановить/перезапустить любые контейнеры
- Возможность создания/удаления контейнеров
- Доступ к файловой системе хоста через volume mounts
- Возможность выполнения команд в контейнерах

---

### Решение: Docker Socket Proxy

**Что такое docker-socket-proxy?**

`docker-socket-proxy` — это прокси-сервер, который:
- Ограничивает доступ к Docker API только необходимыми операциями
- Фильтрует запросы к Docker socket
- Позволяет Prometheus только читать информацию о контейнерах (не изменять)

**Преимущества:**
- ✅ Минимальные привилегии (read-only доступ)
- ✅ Безопасность (нет root, нет полного доступа)
- ✅ Соответствие best practices
- ✅ Изоляция от Docker API

**Ограничения:**
- Prometheus может только **читать** информацию о контейнерах
- Не может создавать/удалять/останавливать контейнеры
- Только операции, необходимые для Service Discovery

---

### Реализация

#### Шаг 1: Добавить docker-socket-proxy в docker-compose.infrastructure.yml

```yaml
  docker-socket-proxy:
    image: tecnativa/docker-socket-proxy:latest
    container_name: memocards-docker-socket-proxy
    profiles:
      - prod
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
    environment:
      # Разрешить только необходимые операции для Prometheus
      CONTAINERS: 1      # Чтение информации о контейнерах
      IMAGES: 0         # Не нужен доступ к образам
      NETWORKS: 0       # Не нужен доступ к сетям
      VOLUMES: 0        # Не нужен доступ к volumes
      EXEC: 0           # Не нужен доступ к exec
      EVENTS: 1         # События для Service Discovery
      PING: 1           # Health check
    restart: unless-stopped
    mem_limit: 32m
    cpus: 0.1
    networks:
      internal:
        aliases:
          - docker-socket-proxy
```

#### Шаг 2: Обновить Prometheus конфигурацию

**Было:**
```yaml
prometheus:
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro
  user: "0:999"
```

**Стало:**
```yaml
prometheus:
  volumes:
    # Убрать прямой доступ к Docker socket
    # - /var/run/docker.sock:/var/run/docker.sock:ro  # УДАЛИТЬ
  user: "nobody"  # Изменить с root на nobody
  environment:
    # Использовать docker-socket-proxy вместо прямого доступа
    DOCKER_HOST: tcp://docker-socket-proxy:2375
```

#### Шаг 3: Обновить prometheus.yml

**Было:**
```yaml
docker_sd_configs:
  - host: unix:///var/run/docker.sock
```

**Стало:**
```yaml
docker_sd_configs:
  - host: tcp://docker-socket-proxy:2375
```

#### Шаг 4: Обновить Promtail (тоже использует Docker socket)

```yaml
promtail:
  volumes:
    # Убрать прямой доступ
    # - /var/run/docker.sock:/var/run/docker.sock:ro  # УДАЛИТЬ
  environment:
    DOCKER_HOST: tcp://docker-socket-proxy:2375
```

**Обновить promtail.yml:**
```yaml
clients:
  - url: http://loki:3100/loki/api/v1/push
server:
  http_listen_port: 9080
positions:
  filename: /var/lib/promtail/positions.yaml
scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: tcp://docker-socket-proxy:2375  # Использовать proxy
        refresh_interval: 15s
```

---

### Сложность реализации

**Оценка**: 🔶 **Средняя** (4-6 часов работы)

**Требуется:**
1. ✅ Добавить docker-socket-proxy в docker-compose
2. ✅ Обновить Prometheus конфигурацию
3. ✅ Обновить Promtail конфигурацию
4. ✅ Протестировать Service Discovery
5. ✅ Обновить Ansible роль monitoring
6. ✅ Обновить документацию

**Риски:**
- ⚠️ Service Discovery может не работать сразу (нужно проверить)
- ⚠️ Promtail может не обнаруживать контейнеры (нужно проверить)
- ⚠️ Требуется тестирование всех сценариев

**Преимущества:**
- ✅ Значительное улучшение безопасности
- ✅ Соответствие best practices
- ✅ Минимальные привилегии

---

## 🟡 2. Staging Environment

### Зачем нужна Staging Environment?

**Преимущества:**
1. **Тестирование перед production**: Проверка изменений в среде, близкой к production
2. **Раннее обнаружение проблем**: Нахождение багов до развертывания в production
3. **Безопасность production**: Защита production данных от тестов
4. **CI/CD интеграция**: Автоматическое тестирование перед production
5. **Обучение**: Возможность экспериментировать без риска

**Сценарии использования:**
- Тестирование новых версий приложения
- Проверка конфигурационных изменений
- Тестирование миграций БД
- Проверка производительности
- Обучение команды

---

### Текущая архитектура

**Текущая ситуация:**
- ✅ Только production окружение
- ✅ Все изменения развертываются напрямую в production
- ⚠️ Нет безопасной среды для тестирования

**Риски:**
- ❌ Баги попадают напрямую в production
- ❌ Нет возможности безопасно тестировать
- ❌ Риск downtime в production
- ❌ Нет возможности откатиться

---

### Реализация Staging Environment

#### Вариант 1: Отдельный VPS (Рекомендуется)

**Архитектура:**
```
Production VPS:
  - memocards.duckdns.org (production)
  - Все сервисы с профилем prod

Staging VPS:
  - staging.memocards.duckdns.org (staging)
  - Все сервисы с профилем staging
  - Отдельная БД (staging)
  - Тестовые данные
```

**Преимущества:**
- ✅ Полная изоляция от production
- ✅ Не влияет на production
- ✅ Можно экспериментировать безопасно
- ✅ Реальная среда для тестирования

**Недостатки:**
- ❌ Дополнительные затраты (еще один VPS)
- ❌ Требуется поддержка двух окружений
- ❌ Сложнее синхронизация конфигураций

**Стоимость:**
- VPS: ~$5-10/месяц (в зависимости от провайдера)
- Дополнительный домен: бесплатно (staging.memocards.duckdns.org)

---

#### Вариант 2: Тот же VPS, разные сети

**Архитектура:**
```
Один VPS:
  - memocards.duckdns.org (production)
    - Docker network: memocards-prod
    - БД: memocards-prod_postgres_data
  - staging.memocards.duckdns.org (staging)
    - Docker network: memocards-staging
    - БД: memocards-staging_postgres_data
```

**Преимущества:**
- ✅ Нет дополнительных затрат
- ✅ Легче синхронизировать конфигурации
- ✅ Один VPS для обоих окружений

**Недостатки:**
- ⚠️ Риск конфликтов ресурсов (CPU, RAM)
- ⚠️ Если VPS упадет, оба окружения не работают
- ⚠️ Менее изолировано

**Стоимость:**
- Дополнительные затраты: 0 (используем тот же VPS)
- Но нужно больше ресурсов (CPU, RAM)

---

#### Вариант 3: Docker Compose Profiles

**Архитектура:**
```
Один VPS, разные docker-compose profiles:
  - Production: docker compose --profile prod up
  - Staging: docker compose --profile staging up
```

**Преимущества:**
- ✅ Нет дополнительных затрат
- ✅ Простое переключение между окружениями
- ✅ Легко управлять через Ansible

**Недостатки:**
- ⚠️ Не может работать одновременно (или сложно)
- ⚠️ Риск конфликтов портов
- ⚠️ Менее изолировано

---

### Рекомендуемая реализация

**Выбор: Вариант 2 (Тот же VPS, разные сети)**

**Причины:**
1. ✅ Нет дополнительных затрат
2. ✅ Достаточная изоляция через Docker networks
3. ✅ Легко управлять через Ansible
4. ✅ Можно запускать оба окружения одновременно

**Структура:**
```
infrastructure/
├── ansible/
│   ├── inventory/
│   │   ├── hosts.yml (production)
│   │   └── hosts-staging.yml (staging)
│   ├── playbooks/
│   │   ├── setup.yml (production)
│   │   └── setup-staging.yml (staging)
│   └── group_vars/
│       ├── all.yml (production)
│       └── all-staging.yml (staging)
├── docker-compose.yml (общий)
├── docker-compose.infrastructure.yml (общий)
└── docker-compose.staging.yml (staging overlay)
```

---

### Реализация шаг за шагом

#### Шаг 1: Создать docker-compose.staging.yml

```yaml
version: '3.8'

services:
  postgres:
    container_name: memocards-postgres-staging
    environment:
      POSTGRES_DB: memocards_staging
      POSTGRES_USER: memocards_user
      POSTGRES_PASSWORD: ${DB_PASSWORD_STAGING}
    networks:
      - staging-internal

  app:
    container_name: memocards-app-staging
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/memocards_staging
      VAADIN_PRODUCTION_MODE: false  # Разрешить debug в staging
    labels:
      - "traefik.http.routers.app-staging.rule=Host(`staging.memocards.duckdns.org`)"
    networks:
      - staging-internal

networks:
  staging-internal:
    name: memocards-staging-internal
    external: false
```

#### Шаг 2: Создать Ansible inventory для staging

**infrastructure/ansible/inventory/hosts-staging.yml:**
```yaml
all:
  children:
    staging:
      hosts:
        staging-server:
          ansible_host: your-staging-server-ip
          ansible_user: root
          ansible_ssh_private_key_file: ~/.ssh/id_rsa
```

#### Шаг 3: Создать group_vars для staging

**infrastructure/ansible/inventory/group_vars/all-staging.yml:**
```yaml
# Staging environment configuration
environment: staging
domain: staging.memocards.duckdns.org

# Database
db_name: memocards_staging
db_user: memocards_user
# db_password stored in secrets-staging.yml

# Application
app_image: ghcr.io/apolenkov/memocards:staging
app_version: staging

# Monitoring
prometheus_enabled: true
grafana_enabled: true
```

#### Шаг 4: Создать playbook для staging

**infrastructure/ansible/playbooks/setup-staging.yml:**
```yaml
---
- name: Setup Memocards Staging Environment
  hosts: staging
  become: yes
  vars_files:
    - ../inventory/group_vars/all-staging.yml
    - ../secrets-staging.yml
  roles:
    - common
    - docker
    - nginx
    - monitoring
    - app
    - backup
```

#### Шаг 5: Обновить Nginx для staging

**infrastructure/nginx/memocards-staging.conf:**
```nginx
server {
    listen 443 ssl http2;
    server_name staging.memocards.duckdns.org;

    ssl_certificate /etc/letsencrypt/live/staging.memocards.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/staging.memocards.duckdns.org/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

### Сложность реализации

**Оценка**: 🔴 **Высокая** (2-3 дня работы)

**Требуется:**
1. ✅ Создать docker-compose.staging.yml
2. ✅ Создать Ansible inventory для staging
3. ✅ Создать group_vars для staging
4. ✅ Создать playbook для staging
5. ✅ Настроить Nginx для staging
6. ✅ Настроить SSL для staging.memocards.duckdns.org
7. ✅ Настроить DNS для staging
8. ✅ Протестировать развертывание
9. ✅ Обновить документацию
10. ✅ Настроить CI/CD для staging

**Риски:**
- ⚠️ Нужно поддерживать два окружения
- ⚠️ Риск конфликтов ресурсов (если один VPS)
- ⚠️ Сложнее синхронизация конфигураций

**Преимущества:**
- ✅ Безопасное тестирование
- ✅ Защита production
- ✅ Раннее обнаружение проблем
- ✅ CI/CD интеграция

---

## 📊 Сравнение улучшений

| Критерий | Prometheus Security | Staging Environment |
|----------|-------------------|-------------------|
| **Приоритет** | 🔴 Критичный | 🟡 Важный |
| **Сложность** | 🔶 Средняя | 🔴 Высокая |
| **Время** | 4-6 часов | 2-3 дня |
| **Риски** | Низкие | Средние |
| **Преимущества** | Безопасность | Тестирование |
| **Стоимость** | 0 | 0-10$/мес |

---

## 🎯 Рекомендации

### Приоритет 1: Prometheus Security

**Рекомендация**: ✅ **Реализовать в первую очередь**

**Причины:**
1. ✅ Критичная проблема безопасности
2. ✅ Средняя сложность (быстро реализовать)
3. ✅ Нет дополнительных затрат
4. ✅ Значительное улучшение безопасности

**Когда реализовать:**
- Следующий спринт (1-2 недели)
- Перед production deployment
- После всех остальных улучшений

---

### Приоритет 2: Staging Environment

**Рекомендация**: ⚠️ **Реализовать позже**

**Причины:**
1. ⚠️ Высокая сложность (2-3 дня)
2. ⚠️ Требует дополнительных ресурсов
3. ⚠️ Нужно поддерживать два окружения
4. ✅ Но очень полезно для production

**Когда реализовать:**
- Когда нужна безопасная среда для тестирования
- Когда есть время на поддержку двух окружений
- Когда есть команда для поддержки

---

## 📚 Дополнительные ресурсы

### Prometheus Security
- [Docker Socket Proxy](https://github.com/Tecnativa/docker-socket-proxy)
- [Prometheus Security Best Practices](https://prometheus.io/docs/guides/basic-auth/)
- [Docker Socket Security](https://docs.docker.com/engine/security/#docker-daemon-attack-surface)

### Staging Environment
- [Docker Compose Profiles](https://docs.docker.com/compose/profiles/)
- [Ansible Best Practices](https://docs.ansible.com/ansible/latest/user_guide/playbooks_best_practices.html)
- [CI/CD Best Practices](https://www.redhat.com/en/topics/devops/what-is-ci-cd)

---

**Last Updated**: 2025-01-XX  
**Status**: Planning Phase 📋


