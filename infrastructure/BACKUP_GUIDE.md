# Database Backup Guide

**Complete guide for Memocards PostgreSQL database backups**

---

## 📋 Overview

Memocards infrastructure includes automated database backups with:
- **Daily backups**: Created at 02:00 UTC, retained for 7 days
- **Weekly backups**: Created on Sunday at 03:00 UTC, retained for 28 days
- **Monthly backups**: Created on the 1st day of month at 04:00 UTC, retained for 365 days
- **Automatic cleanup**: Old backups removed daily at 05:00 UTC

---

## 🚀 Quick Start

### Automatic Backups

Backups are configured automatically when running `setup.yml`:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/setup.yml --ask-vault-pass
```

This will:
1. Create backup directories (`/opt/memocards/backups/`)
2. Install backup scripts
3. Configure cron jobs for automatic backups
4. Set up verification and cleanup scripts

### Manual Backup

To create a manual backup:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/backup-manual.yml --ask-vault-pass
```

With specific backup type:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/backup-manual.yml \
  --ask-vault-pass \
  --extra-vars "backup_type=weekly"
```

---

## 📁 Backup Structure

Backups are stored in `/opt/memocards/backups/`:

```
backups/
├── daily/          # Daily backups (retained 7 days)
│   └── memocards_daily_YYYYMMDD_HHMMSS.sql.gz
├── weekly/         # Weekly backups (retained 28 days)
│   └── memocards_weekly_YYYYMMDD_HHMMSS.sql.gz
├── monthly/        # Monthly backups (retained 365 days)
│   └── memocards_monthly_YYYYMMDD_HHMMSS.sql.gz
└── logs/           # Backup logs
    ├── backup.log
    └── cleanup.log
```

---

## 🔧 Backup Configuration

### Backup Script

The backup script (`/opt/memocards/backups/backup-db.sh`) performs:
1. **Container check**: Verifies PostgreSQL container is running
2. **Backup creation**: Uses `pg_dump` with custom format (binary, compressed)
3. **Integrity verification**: Validates backup file structure
4. **Logging**: Records all operations to `backup.log`

### Retention Policy

- **Daily backups**: 7 days
- **Weekly backups**: 28 days
- **Monthly backups**: 365 days
- **Global retention**: 30 days (configurable via `backup_retention_days`)

### Cron Schedule

```bash
# Daily backup
0 2 * * * /opt/memocards/backups/backup-db.sh daily

# Weekly backup (Sunday 3 AM)
0 3 * * 0 /opt/memocards/backups/backup-db.sh weekly

# Monthly backup (1st day 4 AM)
0 4 1 * * /opt/memocards/backups/backup-db.sh monthly

# Cleanup (5 AM daily)
0 5 * * * /opt/memocards/backups/cleanup-backups.sh
```

---

## ✅ Verification

### Check Backup Integrity

```bash
# On VPS
/opt/memocards/backups/verify-backup.sh /opt/memocards/backups/daily/memocards_daily_YYYYMMDD_HHMMSS.sql.gz
```

### List Recent Backups

```bash
# Daily backups
ls -lht /opt/memocards/backups/daily/

# Weekly backups
ls -lht /opt/memocards/backups/weekly/

# Monthly backups
ls -lht /opt/memocards/backups/monthly/
```

### View Backup Logs

```bash
# Backup operations
tail -f /opt/memocards/backups/logs/backup.log

# Cleanup operations
tail -f /opt/memocards/backups/logs/cleanup.log
```

---

## 🔄 Restore Procedure

### Restore from Backup

1. **Stop application** (to prevent data conflicts):
   ```bash
   docker compose -p memocards-prod stop app
   ```

2. **Restore database**:
   ```bash
   # On VPS
   docker exec -i memocards-postgres-prod \
     pg_restore -U memocards_user -d memocards --clean --if-exists \
     < /opt/memocards/backups/daily/memocards_daily_YYYYMMDD_HHMMSS.sql.gz
   ```

3. **Verify restore**:
   ```bash
   docker exec memocards-postgres-prod \
     psql -U memocards_user -d memocards -c "SELECT COUNT(*) FROM decks;"
   ```

4. **Restart application**:
   ```bash
   docker compose -p memocards-prod start app
   ```

### Alternative: Full Container Recreate

If you need to restore from a complete backup:

1. **Stop all services**:
   ```bash
   docker compose -p memocards-prod stop
   ```

2. **Remove database volume** (⚠️ **DESTRUCTIVE**):
   ```bash
   docker volume rm memocards-prod_postgres_data
   ```

3. **Restore backup**:
   ```bash
   docker exec -i memocards-postgres-prod \
     pg_restore -U memocards_user -d memocards --clean --if-exists \
     < /opt/memocards/backups/daily/memocards_daily_YYYYMMDD_HHMMSS.sql.gz
   ```

4. **Start services**:
   ```bash
   docker compose -p memocards-prod start
   ```

---

## 📊 Monitoring

### Check Backup Status

```bash
# View last backup
ls -lht /opt/memocards/backups/daily/ | head -1

# Check cron jobs
crontab -l | grep backup-db

# View recent backup logs
tail -20 /opt/memocards/backups/logs/backup.log
```

### Backup Size

```bash
# Total backup size
du -sh /opt/memocards/backups/

# Per directory
du -sh /opt/memocards/backups/{daily,weekly,monthly}
```

---

## 🔒 Security

- **Backup files**: Stored with `0755` permissions (owner: root)
- **Backup logs**: Stored with `0644` permissions
- **Backup scripts**: Executable by root only
- **No sensitive data exposure**: Backups contain only database content

### Offsite Backup (Optional)

For production environments, consider:
1. **S3/Remote Storage**: Upload backups to S3, Google Cloud Storage, etc.
2. **Restic**: Use `restic` for encrypted backups (configured in `all.yml`)
3. **Manual transfer**: Periodically copy backups to external storage

---

## 🛠️ Troubleshooting

### Backup Fails

**Problem**: Backup script fails with "PostgreSQL container not running"

**Solution**:
```bash
# Check container status
docker ps | grep memocards-postgres-prod

# Start container if stopped
docker compose -p memocards-prod start postgres
```

### Backup Verification Fails

**Problem**: `verify-backup.sh` reports backup as invalid

**Solution**:
1. Check backup file size (should be > 0):
   ```bash
   ls -lh /opt/memocards/backups/daily/memocards_daily_*.sql.gz
   ```

2. Check file format:
   ```bash
   file /opt/memocards/backups/daily/memocards_daily_*.sql.gz
   # Should show: gzip compressed
   ```

3. If backup is valid but verification fails, the backup file is likely OK (verification is a best-effort check)

### No Space Left

**Problem**: Backup fails due to disk space

**Solution**:
```bash
# Check disk space
df -h /opt/memocards/backups/

# Clean old backups manually
/opt/memocards/backups/cleanup-backups.sh

# Or remove old backups manually
find /opt/memocards/backups/ -name "*.sql.gz" -mtime +30 -delete
```

### Cron Jobs Not Running

**Problem**: Automatic backups not executing

**Solution**:
```bash
# Check cron service
systemctl status cron

# Check cron logs
grep CRON /var/log/syslog | tail -20

# Verify cron jobs
crontab -l | grep backup
```

---

## 📚 Configuration

### Variables

Configure backup settings in `infrastructure/ansible/inventory/group_vars/all.yml`:

```yaml
backup_path: "/opt/memocards/backups"
backup_retention_days: 30
```

### Customization

To modify backup schedule, edit `/opt/memocards/backups/backup-db.sh` or use Ansible:

```bash
ansible-playbook -i inventory/hosts.yml playbooks/backup-manual.yml \
  --ask-vault-pass \
  --extra-vars "backup_type=weekly"
```

---

## ✅ Best Practices

1. **Regular testing**: Test restore procedure periodically
2. **Offsite backups**: Store backups in remote location
3. **Monitoring**: Monitor backup logs for failures
4. **Retention**: Adjust retention based on storage capacity
5. **Verification**: Regularly verify backup integrity

---

**Last Updated**: 2025-01-XX  
**Status**: Production Ready ✅

