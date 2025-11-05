# Troubleshooting Guide

**Complete troubleshooting guide for Memocards infrastructure issues**

---

## 📋 Quick Reference

### Common Issues

| Issue | Quick Fix |
|-------|-----------|
| Application not responding | `docker logs -f memocards-app-prod` |
| Database connection failed | `docker ps | grep postgres` |
| Traefik dashboard gray | Check Traefik routing in `docker-compose.infrastructure.yml` |
| Prometheus no data | Check Prometheus targets: `http://localhost:9090/targets` |
| Grafana login failed | Check credentials in `secrets.yml` |
| Backup failed | Check backup logs: `tail -f /opt/memocards/backups/logs/backup.log` |

---

## 🔍 Application Issues

### Application Not Responding

**Symptoms:**
- Application returns 502/503/504 errors
- Health check fails: `https://memocards.duckdns.org/actuator/health`

**Diagnosis:**
```bash
# Check container status
docker ps | grep memocards-app-prod

# Check application logs
docker logs --tail 100 memocards-app-prod

# Check health endpoint directly
docker exec memocards-app-prod wget -qO- http://localhost:8080/actuator/health

# Check resource usage
docker stats memocards-app-prod --no-stream
```

**Common Causes:**
1. **Out of memory**: Check `mem_limit` in `docker-compose.yml`
2. **Database connection failed**: Check PostgreSQL container
3. **Application startup failed**: Check application logs for errors
4. **Port conflict**: Check if port 8080 is in use

**Solutions:**
```bash
# Restart application
docker compose -p memocards-prod restart app

# Check memory usage
docker stats memocards-app-prod

# Increase memory limit (edit docker-compose.yml)
APP_MEMORY_LIMIT=2000m docker compose -p memocards-prod up -d app

# Check database connection
docker exec memocards-postgres-prod psql -U memocards_user -d memocards -c "SELECT 1;"
```

---

### Database Connection Errors

**Symptoms:**
- `Connection refused` errors in application logs
- `Timeout waiting for connection` errors
- Application fails to start

**Diagnosis:**
```bash
# Check PostgreSQL container
docker ps | grep postgres

# Check PostgreSQL logs
docker logs --tail 100 memocards-postgres-prod

# Test database connection
docker exec memocards-postgres-prod psql -U memocards_user -d memocards -c "SELECT version();"

# Check network connectivity
docker exec memocards-app-prod ping -c 3 postgres
```

**Common Causes:**
1. **PostgreSQL container not running**: Container crashed or stopped
2. **Network issue**: Containers not on same network
3. **Authentication failed**: Wrong credentials in `.env`
4. **Database not initialized**: First-time setup issue

**Solutions:**
```bash
# Restart PostgreSQL
docker compose -p memocards-prod restart postgres

# Check network
docker network inspect memocards-internal

# Verify credentials
docker exec memocards-postgres-prod psql -U memocards_user -d memocards -c "SELECT 1;"

# Recreate database (⚠️ DESTRUCTIVE)
docker compose -p memocards-prod down postgres
docker volume rm memocards-prod_postgres_data
docker compose -p memocards-prod up -d postgres
```

---

## 🌐 Traefik Issues

### Traefik Dashboard Gray Areas

**Symptoms:**
- Traefik dashboard shows gray areas
- Dashboard cannot load data
- API calls fail

**Diagnosis:**
```bash
# Check Traefik container
docker ps | grep traefik

# Check Traefik logs
docker logs --tail 100 memocards-traefik-prod

# Test Traefik API
curl -u admin:password http://localhost:8080/api/overview

# Check routing
curl -H "Host: memocards.duckdns.org" http://localhost:8080/traefik/dashboard/
```

**Common Causes:**
1. **API routing misconfigured**: Check `docker-compose.infrastructure.yml` labels
2. **Basic auth failed**: Check `.htpasswd` file
3. **PathPrefix issues**: Traefik v3.0 doesn't support `basePath`

**Solutions:**
```bash
# Restart Traefik
docker compose -p memocards-prod restart traefik

# Verify routing configuration
grep -A 5 "traefik-api" docker-compose.infrastructure.yml

# Check .htpasswd
docker exec memocards-traefik-prod cat /etc/traefik/.htpasswd

# Regenerate .htpasswd (via Ansible)
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags monitoring --ask-vault-pass
```

---

### Application Not Accessible via Traefik

**Symptoms:**
- Application accessible directly but not via Traefik
- 404 errors when accessing via domain
- Traefik logs show "no route found"

**Diagnosis:**
```bash
# Check Traefik routing
curl -H "Host: memocards.duckdns.org" http://localhost:8080/

# Check application container
docker ps | grep memocards-app-prod

# Check Traefik labels
docker inspect memocards-app-prod | grep -A 10 traefik

# Test direct access
docker exec memocards-app-prod wget -qO- http://localhost:8080/actuator/health
```

**Common Causes:**
1. **Host header mismatch**: Domain doesn't match Traefik rule
2. **Traefik labels missing**: Check `docker-compose.yml` labels
3. **Application not on same network**: Check network configuration

**Solutions:**
```bash
# Verify Traefik labels in docker-compose.yml
grep -A 5 "traefik.enable" docker-compose.yml

# Check network
docker network inspect memocards-internal | grep memocards-app-prod

# Restart application
docker compose -p memocards-prod restart app

# Verify routing
curl -H "Host: memocards.duckdns.org" http://localhost:8080/actuator/health
```

---

## 📊 Monitoring Issues

### Prometheus No Data

**Symptoms:**
- Prometheus shows "No data" for targets
- Targets show as "DOWN" in Prometheus UI
- Metrics not collected

**Diagnosis:**
```bash
# Check Prometheus container
docker ps | grep prometheus

# Check Prometheus logs
docker logs --tail 100 memocards-prometheus-prod

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check Prometheus configuration
docker exec memocards-prometheus-prod cat /etc/prometheus/prometheus.yml
```

**Common Causes:**
1. **Targets not discovered**: Docker Service Discovery not working
2. **Network issue**: Prometheus can't reach targets
3. **Configuration error**: Invalid `prometheus.yml`
4. **Docker socket access**: Prometheus can't access `/var/run/docker.sock`

**Solutions:**
```bash
# Restart Prometheus
docker compose -p memocards-prod restart prometheus

# Check Docker socket access
docker exec memocards-prometheus-prod ls -la /var/run/docker.sock

# Verify configuration
docker exec memocards-prometheus-prod promtool check config /etc/prometheus/prometheus.yml

# Check network
docker network inspect memocards-internal | grep prometheus

# Reload Prometheus configuration
curl -X POST http://localhost:9090/-/reload
```

---

### Grafana Not Showing Data

**Symptoms:**
- Grafana dashboards show "No data"
- Datasources not working
- Queries return empty results

**Diagnosis:**
```bash
# Check Grafana container
docker ps | grep grafana

# Check Grafana logs
docker logs --tail 100 memocards-grafana-prod

# Test Prometheus datasource
curl -u admin:admin http://localhost:3000/api/datasources

# Check Loki datasource
curl -u admin:admin http://localhost:3000/api/datasources/uid/loki
```

**Common Causes:**
1. **Datasource not configured**: Check `provisioning/datasources/prometheus.yml`
2. **Prometheus not accessible**: Check Grafana network connectivity
3. **Query syntax error**: Check dashboard queries
4. **Time range issue**: Check time range in Grafana

**Solutions:**
```bash
# Restart Grafana
docker compose -p memocards-prod restart grafana

# Check datasource configuration
cat infrastructure/grafana/provisioning/datasources/prometheus.yml

# Test Prometheus connection from Grafana
docker exec memocards-grafana-prod wget -qO- http://prometheus:9090/api/v1/query?query=up

# Recreate datasource (via Ansible)
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags monitoring --ask-vault-pass
```

---

### Loki Logs Not Showing

**Symptoms:**
- Loki queries return empty results
- Logs not collected from containers
- Promtail not shipping logs

**Diagnosis:**
```bash
# Check Loki container
docker ps | grep loki

# Check Promtail container
docker ps | grep promtail

# Check Loki logs
docker logs --tail 100 memocards-loki-prod

# Check Promtail logs
docker logs --tail 100 memocards-promtail-prod

# Test Loki API
curl http://localhost:3100/ready

# Test Promtail targets
docker exec memocards-promtail-prod cat /var/lib/promtail/positions.yaml
```

**Common Causes:**
1. **Promtail not discovering containers**: Docker Service Discovery not working
2. **Log path mismatch**: Promtail can't read container logs
3. **Loki not accessible**: Network connectivity issue
4. **Configuration error**: Invalid `promtail.yml`

**Solutions:**
```bash
# Restart Loki and Promtail
docker compose -p memocards-prod restart loki promtail

# Check log paths
docker exec memocards-promtail-prod ls -la /var/lib/docker/containers/

# Verify Promtail configuration
docker exec memocards-promtail-prod cat /etc/promtail/promtail.yml

# Check network
docker network inspect memocards-internal | grep promtail

# Test Loki query
curl -G -s "http://localhost:3100/loki/api/v1/query" --data-urlencode 'query={service="app"}'
```

---

## 💾 Backup Issues

### Backup Failed

**Symptoms:**
- Backup script exits with error
- Backup files not created
- Cron job not running

**Diagnosis:**
```bash
# Check backup logs
tail -f /opt/memocards/backups/logs/backup.log

# Check last backup
ls -lht /opt/memocards/backups/daily/ | head -5

# Test backup manually
/opt/memocards/backups/backup-db.sh daily

# Check cron jobs
crontab -l | grep backup

# Check PostgreSQL container
docker ps | grep postgres
```

**Common Causes:**
1. **PostgreSQL container not running**: Container stopped
2. **Insufficient disk space**: Check disk usage
3. **Permission denied**: Check backup script permissions
4. **Cron not running**: Check cron service

**Solutions:**
```bash
# Run backup manually
/opt/memocards/backups/backup-db.sh daily

# Check disk space
df -h /opt/memocards/backups/

# Fix permissions
chmod +x /opt/memocards/backups/*.sh
chown root:root /opt/memocards/backups/*.sh

# Restart cron service
systemctl restart cron

# Check cron logs
grep CRON /var/log/syslog | tail -20
```

---

### Backup Verification Failed

**Symptoms:**
- `verify-backup.sh` reports backup as invalid
- Backup file exists but verification fails
- Backup size is 0

**Diagnosis:**
```bash
# Check backup file
ls -lh /opt/memocards/backups/daily/memocards_daily_*.sql.gz

# Test backup file
file /opt/memocards/backups/daily/memocards_daily_*.sql.gz

# Run verification
/opt/memocards/backups/verify-backup.sh /opt/memocards/backups/daily/memocards_daily_*.sql.gz

# Check backup size
du -sh /opt/memocards/backups/daily/
```

**Common Causes:**
1. **Backup file corrupted**: File transfer interrupted
2. **Backup incomplete**: Backup script failed mid-way
3. **File format issue**: Backup not in expected format

**Solutions:**
```bash
# Recreate backup
/opt/memocards/backups/backup-db.sh daily

# Check backup integrity
gzip -t /opt/memocards/backups/daily/memocards_daily_*.sql.gz

# Extract and verify backup
gunzip -c /opt/memocards/backups/daily/memocards_daily_*.sql.gz | head -20
```

---

## 🔒 Security Issues

### Basic Auth Not Working

**Symptoms:**
- Can't access Prometheus/Grafana/Alertmanager
- Authentication fails
- 401 Unauthorized errors

**Diagnosis:**
```bash
# Check .htpasswd file
docker exec memocards-traefik-prod cat /etc/traefik/.htpasswd

# Test basic auth
curl -u admin:password http://localhost:8080/prometheus/

# Check Traefik logs
docker logs --tail 100 memocards-traefik-prod | grep -i auth
```

**Common Causes:**
1. **Wrong credentials**: Check `secrets.yml`
2. **.htpasswd not generated**: Ansible task failed
3. **Traefik not reading .htpasswd**: Volume mount issue

**Solutions:**
```bash
# Regenerate .htpasswd (via Ansible)
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags monitoring --ask-vault-pass

# Test credentials
htpasswd -nb admin password

# Manually create .htpasswd
docker exec memocards-traefik-prod htpasswd -nb admin password > /tmp/.htpasswd
docker cp /tmp/.htpasswd memocards-traefik-prod:/etc/traefik/.htpasswd
docker compose -p memocards-prod restart traefik
```

---

## 🐳 Docker Issues

### Container Won't Start

**Symptoms:**
- Container exits immediately
- `docker ps` doesn't show container
- Container status shows "Exited"

**Diagnosis:**
```bash
# Check container status
docker ps -a | grep memocards

# Check container logs
docker logs memocards-app-prod

# Check container exit code
docker inspect memocards-app-prod | grep -A 5 ExitCode

# Check resource limits
docker inspect memocards-app-prod | grep -A 5 Memory
```

**Common Causes:**
1. **Application startup failed**: Check application logs
2. **Out of memory**: Container killed by OOM
3. **Health check failed**: Health check timeout
4. **Port conflict**: Port already in use

**Solutions:**
```bash
# Check logs
docker logs memocards-app-prod

# Increase memory limit
docker compose -p memocards-prod up -d --force-recreate app

# Check port conflicts
netstat -tulpn | grep 8080

# Restart container
docker compose -p memocards-prod restart app
```

---

### Network Connectivity Issues

**Symptoms:**
- Containers can't communicate
- Connection refused errors
- Services not discoverable

**Diagnosis:**
```bash
# Check network
docker network inspect memocards-internal

# Test connectivity
docker exec memocards-app-prod ping -c 3 postgres

# Check DNS resolution
docker exec memocards-app-prod nslookup postgres

# Verify network membership
docker network inspect memocards-internal | grep memocards-app-prod
```

**Common Causes:**
1. **Containers not on same network**: Check `docker-compose.yml`
2. **Network not created**: Network creation failed
3. **DNS resolution failed**: Docker DNS issue

**Solutions:**
```bash
# Recreate network
docker network rm memocards-internal
docker network create --driver bridge --subnet 172.20.0.0/16 memocards-internal

# Restart containers
docker compose -p memocards-prod restart

# Verify connectivity
docker exec memocards-app-prod ping -c 3 postgres
```

---

## 📈 Performance Issues

### High Memory Usage

**Symptoms:**
- Containers using excessive memory
- OOM (Out of Memory) errors
- System swapping

**Diagnosis:**
```bash
# Check memory usage
docker stats --no-stream

# Check system memory
free -h

# Check container memory limits
docker inspect memocards-app-prod | grep -A 5 Memory

# Check OOM events
dmesg | grep -i oom
```

**Solutions:**
```bash
# Increase memory limits (edit docker-compose.yml)
APP_MEMORY_LIMIT=2000m docker compose -p memocards-prod up -d app

# Reduce memory usage
# - Optimize application code
# - Reduce cache sizes
# - Adjust JVM heap size

# Check for memory leaks
docker stats --no-stream | grep memocards
```

---

### High CPU Usage

**Symptoms:**
- Containers using excessive CPU
- Slow response times
- System overloaded

**Diagnosis:**
```bash
# Check CPU usage
docker stats --no-stream

# Check system CPU
top -bn1 | head -20

# Check container CPU limits
docker inspect memocards-app-prod | grep -A 5 Cpu
```

**Solutions:**
```bash
# Increase CPU limits (edit docker-compose.yml)
APP_CPUS=2.0 docker compose -p memocards-prod up -d app

# Optimize application
# - Profile application code
# - Reduce unnecessary computations
# - Optimize database queries
```

---

## 🔧 Ansible Issues

### Playbook Fails

**Symptoms:**
- Ansible playbook exits with error
- Tasks fail during execution
- Deployment incomplete

**Diagnosis:**
```bash
# Run with verbose output
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml -vvv

# Check specific task
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --tags monitoring --ask-vault-pass

# Test connection
ansible -i inventory/hosts.yml all -m ping
```

**Common Causes:**
1. **Connection failed**: SSH not accessible
2. **Vault password wrong**: Check vault password
3. **Task failed**: Check task logs
4. **Missing dependencies**: Check requirements

**Solutions:**
```bash
# Test SSH connection
ssh root@your-server

# Check inventory
cat infrastructure/ansible/inventory/hosts.yml

# Run with debug
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml -vvv --ask-vault-pass

# Install requirements
ansible-galaxy install -r requirements.yml
```

---

## 📚 Additional Resources

### Useful Commands

```bash
# View all logs
docker compose -p memocards-prod logs --tail 100

# Restart all services
docker compose -p memocards-prod restart

# Check container status
docker compose -p memocards-prod ps

# Check resource usage
docker stats --no-stream

# Clean up Docker
docker system prune -f

# Check disk usage
df -h
du -sh /opt/memocards/*
```

### Log Locations

- **Application logs**: `/opt/memocards/logs/`
- **Backup logs**: `/opt/memocards/backups/logs/`
- **Container logs**: `docker logs <container-name>`
- **System logs**: `/var/log/syslog`

### Documentation

- **Backup Guide**: `infrastructure/BACKUP_GUIDE.md`
- **Log Rotation**: `infrastructure/LOG_ROTATION.md`
- **Deep Review**: `infrastructure/DEEP_REVIEW_CHECKLIST.md`

---

**Last Updated**: 2025-01-XX  
**Status**: Production Ready ✅


