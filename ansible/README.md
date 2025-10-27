# Ansible Infrastructure Management

Автоматизация развертывания Memocards приложения на VPS.

## Структура

```
ansible/
├── playbooks/          # Ansible playbooks
│   ├── setup.yml      # Полная установка инфраструктуры
│   └── deploy.yml     # Быстрое обновление приложения
├── roles/             # Ansible roles
│   ├── common/        # Базовая настройка системы
│   ├── docker/        # Установка Docker
│   ├── nginx/         # Настройка Nginx с SSL
│   └── app/           # Развертывание приложения
├── inventory/         # Inventory файлы
│   ├── hosts.yml      # Определение хостов
│   └── group_vars/    # Переменные для групп хостов
└── secrets.yml        # Зашифрованные секреты (Ansible Vault)
```

## Использование

### Полная установка инфраструктуры

Первый раз на новом сервере:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --ask-vault-pass
```

Что выполняется:
- Базовая настройка системы (firewall, swap, пользователи)
- Установка Docker и Docker Compose
- Настройка Nginx с автоматическим SSL (Let's Encrypt)
- Развертывание приложения и PostgreSQL
- Настройка backup и мониторинга

### Быстрое обновление приложения

Для деплоя новой версии:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/deploy.yml --ask-vault-pass
```

### Развертывание конкретной версии

```bash
# Конкретная версия
ansible-playbook -i inventory/hosts.yml playbooks/deploy.yml --ask-vault-pass \
  --extra-vars "app_image=ghcr.io/apolenkov/memocards:1.0.0"

# Конкретный коммит (SHA)
ansible-playbook -i inventory/hosts.yml playbooks/deploy.yml --ask-vault-pass \
  --extra-vars "app_image=ghcr.io/apolenkov/memocards:abc1234"
```

### Запуск отдельных ролей

```bash
# Только установка Docker
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags docker

# Только настройка Nginx
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags nginx

# Только развертывание приложения
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags app
```

### Dry-run (проверка без изменений)

```bash
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --ask-vault-pass --check
```

## Требования

### На локальной машине

- Ansible 2.9+
- Python 3.8+
- SSH доступ к целевому серверу

### На целевом сервере

- Ubuntu 22.04+ (или Debian)
- Root или sudo доступ
- Минимум 2GB RAM, 20GB диск

## Управление секретами (Ansible Vault)

Секреты хранятся в `secrets.yml` и зашифрованы через Ansible Vault.

### Просмотр секретов

```bash
ansible-vault view secrets.yml
```

### Редактирование секретов

```bash
ansible-vault edit secrets.yml
```

### Создание нового файла секретов

```bash
ansible-vault create secrets.yml
```

### Смена пароля Vault

```bash
ansible-vault rekey secrets.yml
```

## Переменные

### Group vars (`inventory/group_vars/all.yml`)

Основные переменные конфигурации:
- `domain` - доменное имя
- `app_port` - порт приложения (по умолчанию 8080)
- `app_image` - Docker образ приложения
- `app_deploy_path` - путь для развертывания
- `backup_enabled` - включить backup (по умолчанию true)
- `monitoring_enabled` - включить мониторинг (по умолчанию true)

### Vault secrets (`secrets.yml`)

Зашифрованные секреты:
- `vault_vps_password` - пароль для VPS
- `vault_github_token` - токен для GitHub Container Registry
- `vault_duckdns_token` - токен для DuckDNS (динамический DNS)
- `vault_db_password` - пароль для PostgreSQL

## Роли

### common
Базовая настройка системы:
- Обновление пакетов
- Настройка firewall (UFW)
- Создание swap файла
- Настройка часового пояса
- Создание директорий для приложения

### docker
Установка Docker:
- Удаление старых версий Docker
- Установка Docker Engine
- Установка Docker Compose v2
- Настройка Docker daemon
- Добавление пользователя в группу docker
- Вход в GitHub Container Registry

### nginx
Настройка веб-сервера:
- Установка Nginx
- Настройка SSL через Let's Encrypt
- Автоматическое обновление SSL сертификатов
- Настройка проксирования к приложению
- Настройка DuckDNS для динамического DNS

### app
Развертывание приложения:
- Копирование docker-compose.yml
- Генерация .env файла
- Pull Docker образа
- Запуск контейнеров
- Health check
- Настройка backup (ежедневно в 2:00)
- Настройка мониторинга (каждые 5 минут)

## Полезные команды

### Проверка подключения

```bash
ansible -i inventory/hosts.yml vps -m ping --ask-pass
```

### Выполнение произвольной команды

```bash
ansible -i inventory/hosts.yml vps -a "docker ps" --ask-pass
```

### Сбор фактов о системе

```bash
ansible -i inventory/hosts.yml vps -m setup --ask-pass
```

### Проверка синтаксиса playbook

```bash
ansible-playbook playbooks/setup.yml --syntax-check
```

### Список всех задач

```bash
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --list-tasks
```

### Список всех тегов

```bash
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --list-tags
```

## Troubleshooting

### Проблема: "Authentication failure"

Убедитесь, что:
- SSH ключ добавлен на сервер
- Или используете `--ask-pass` для ввода пароля
- Пароль в `secrets.yml` корректный

### Проблема: "Permission denied"

Проверьте:
- Пользователь имеет sudo права
- Используете `become: yes` в playbook
- Пароль sudo корректный

### Проблема: "Failed to pull Docker image"

Проверьте:
- GitHub token актуален
- Образ существует в GitHub Container Registry
- Есть доступ к интернету на сервере

### Логи на сервере

```bash
# Логи приложения
docker logs -f memocards-app-prod

# Логи PostgreSQL
docker logs -f memocards-postgres-prod

# Логи Nginx
tail -f /var/log/nginx/error.log
tail -f /var/log/nginx/access.log
```

## CI/CD Integration

Playbook `deploy.yml` предназначен для использования в CI/CD пайплайнах:

```yaml
# .github/workflows/deploy.yml
- name: Deploy to VPS
  run: |
    ansible-playbook -i ansible/inventory/hosts.yml \
      ansible/playbooks/deploy.yml \
      --vault-password-file <(echo "${{ secrets.ANSIBLE_VAULT_PASSWORD }}") \
      --extra-vars "app_image=ghcr.io/apolenkov/memocards:${{ github.sha }}"
```
