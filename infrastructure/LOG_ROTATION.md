# Log Rotation Guide

**Complete guide for log rotation and management in Memocards infrastructure**

---

## 📋 Overview

Memocards infrastructure uses multiple log sources:
- **Application logs**: Spring Boot application logs (via Docker)
- **Container logs**: Docker container logs
- **System logs**: VPS system logs (syslog, auth.log, etc.)
- **Backup logs**: Database backup operation logs
- **Monitoring logs**: Prometheus, Loki, Grafana logs

---

## 🔄 Docker Log Rotation

### Default Configuration

Docker daemon handles container log rotation automatically. Configuration is set in `/etc/docker/daemon.json`:

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

This means:
- **Max size**: 10 MB per log file
- **Max files**: 3 files per container
- **Total per container**: ~30 MB

### Application Logs

Application logs are stored in `/opt/memocards/logs/` (mounted volume):

```
logs/
├── application.log
├── errors.log
├── warnings.log
├── sql.log
├── security.log
├── audit.log
└── archive/          # Archived logs
```

**Rotation**: Handled by application (Spring Boot Logback configuration)

### Container Logs

Container logs are managed by Docker daemon:

```bash
# View container logs
docker logs memocards-app-prod

# View last 100 lines
docker logs --tail 100 memocards-app-prod

# Follow logs
docker logs -f memocards-app-prod
```

**Location**: `/var/lib/docker/containers/<container-id>/`

---

## 🔧 Log Rotation Configuration

### Docker Daemon Log Rotation

Edit `/etc/docker/daemon.json`:

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3",
    "compress": "true"
  }
}
```

**Apply changes**:
```bash
sudo systemctl restart docker
```

### Application Log Rotation

Application logs are rotated by Logback (configured in `src/main/resources/logback-spring.xml`):

- **Size-based rotation**: 10 MB per file
- **Time-based rotation**: Daily rotation
- **Archived logs**: Kept for 30 days
- **Compression**: Gzip compression for old logs

### System Log Rotation

System logs are managed by `logrotate`:

```bash
# Check logrotate configuration
cat /etc/logrotate.conf

# Check specific logrotate configs
ls -la /etc/logrotate.d/
```

**Common system logs**:
- `/var/log/syslog`
- `/var/log/auth.log`
- `/var/log/nginx/access.log`
- `/var/log/nginx/error.log`

---

## 📊 Monitoring Logs

### Loki Log Retention

Loki retention is configured in `infrastructure/loki/loki.yml`:

```yaml
limits_config:
  retention_period: 720h  # 30 days
```

**To change retention**:
1. Edit `infrastructure/loki/loki.yml`
2. Update `retention_period` (in hours)
3. Redeploy infrastructure:
   ```bash
   ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --ask-vault-pass
   ```

### Prometheus Logs

Prometheus logs are minimal (errors only). Logs are stored in container logs.

### Grafana Logs

Grafana logs are stored in container logs and Grafana data directory.

---

## 🧹 Manual Log Cleanup

### Clean Application Logs

```bash
# On VPS
cd /opt/memocards/logs

# Remove old logs (older than 30 days)
find . -name "*.log.*" -mtime +30 -delete

# Compress old logs
gzip *.log.* 2>/dev/null || true
```

### Clean Docker Container Logs

```bash
# Clean logs for stopped containers
docker system prune -f

# Clean specific container logs (requires container restart)
docker logs --tail 0 -f memocards-app-prod > /dev/null 2>&1 &
# Then truncate log file (advanced, not recommended)
```

### Clean System Logs

```bash
# Use logrotate manually
sudo logrotate -f /etc/logrotate.conf

# Or clean specific logs
sudo truncate -s 0 /var/log/syslog
```

---

## 📋 Log Rotation Best Practices

1. **Size limits**: Set appropriate `max-size` for Docker logs (10-50 MB)
2. **File limits**: Limit number of log files (3-5 files)
3. **Compression**: Enable compression for old logs
4. **Retention**: Set retention based on storage capacity
5. **Monitoring**: Monitor log directory sizes

---

## 🔍 Monitoring Log Sizes

### Check Log Sizes

```bash
# Application logs
du -sh /opt/memocards/logs/

# Docker container logs
docker system df -v | grep -i log

# System logs
du -sh /var/log/
```

### Check Loki Storage

```bash
# Loki data directory
du -sh /var/lib/docker/volumes/memocards-prod_loki_data/_data/
```

### Check Prometheus Storage

```bash
# Prometheus data directory
du -sh /var/lib/docker/volumes/memocards-prod_prometheus_data/_data/
```

---

## 🛠️ Troubleshooting

### Logs Growing Too Large

**Problem**: Log directory consuming too much disk space

**Solution**:
1. Check log sizes:
   ```bash
   du -sh /opt/memocards/logs/* | sort -h
   ```

2. Clean old logs:
   ```bash
   find /opt/memocards/logs/ -name "*.log.*" -mtime +30 -delete
   ```

3. Adjust rotation settings:
   - Docker: Edit `/etc/docker/daemon.json`
   - Application: Edit `logback-spring.xml`
   - Loki: Edit `infrastructure/loki/loki.yml`

### Log Rotation Not Working

**Problem**: Logs not rotating automatically

**Solution**:
1. **Docker logs**: Check daemon.json configuration
2. **Application logs**: Check Logback configuration
3. **System logs**: Check logrotate status:
   ```bash
   systemctl status logrotate
   ```

### Disk Space Issues

**Problem**: Disk full due to logs

**Solution**:
```bash
# Check disk usage
df -h

# Clean old logs
find /opt/memocards/logs/ -name "*.log.*" -mtime +7 -delete

# Clean Docker logs
docker system prune -f

# Clean system logs (if needed)
sudo journalctl --vacuum-time=7d
```

---

## 📚 Configuration Files

- **Docker**: `/etc/docker/daemon.json`
- **Application**: `src/main/resources/logback-spring.xml`
- **Loki**: `infrastructure/loki/loki.yml`
- **System**: `/etc/logrotate.conf`, `/etc/logrotate.d/*`

---

**Last Updated**: 2025-01-XX  
**Status**: Production Ready ✅

