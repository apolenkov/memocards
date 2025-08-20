# Logging Configuration

## Overview

The application is configured for file logging using Logback. Logs are saved in the `logs/` directory with automatic rotation and archiving.

## Log Structure

```
logs/
├── application.log          # Main application log
├── error.log               # Error logs (ERROR level only)
├── critical-errors.log     # Critical errors with full stack trace
├── sql.log                 # SQL queries and parameters
├── security.log            # Security events
└── archive/                # Old logs archive
    ├── application.2024-01-15.0.log
    ├── error.2024-01-15.0.log
    ├── critical-errors.2024-01-15.0.log
    ├── sql.2024-01-15.0.log
    └── security.2024-01-15.0.log
```

## Rotation Settings

- **Main log (application.log)**: 100MB, 30 days, 3GB total size
- **Errors (error.log)**: 100MB, 30 days, 1GB total size
- **Critical errors (critical-errors.log)**: 50MB, 90 days, 2GB total size
- **SQL log (sql.log)**: 100MB, 30 days, 1GB total size
- **Security (security.log)**: 100MB, 30 days, 1GB total size
- **Archive format**: `{log_name}.{date}.{index}.log`

## Logging Levels

### Dev Profile
- **Console**: Enabled with short format
- **Files**: Detailed format with timestamps
- **SQL**: DEBUG level for debugging
- **Application**: DEBUG for dev components

### Production Profile
- **Console**: Disabled
- **Files**: File logging only
- **SQL**: WARN level (errors only)
- **Application**: INFO level

## Log Formats

### Console (dev)
```
14:30:25.123 [http-nio-8080-exec-1] INFO  c.v.flow.server.VaadinServlet - Request received
```

### File
```
2024-01-15 14:30:25.123 [http-nio-8080-exec-1] INFO  com.vaadin.flow.server.VaadinServlet - Request received
```

## Specialized Loggers

### SQL Logging
- **org.hibernate.SQL**: SQL queries
- **org.hibernate.type.descriptor.sql.BasicBinder**: Query parameters

### Security
- **org.springframework.security**: Authentication and authorization events

### Vaadin
- **com.vaadin**: UI framework events

## Critical Errors Duplication

Critical errors (ERROR level) are automatically duplicated to a special file `critical-errors.log` with the following features:

- **Full stack trace**: Includes detailed exception information
- **Long-term storage**: 90 days instead of standard 30
- **Larger size**: 2GB total size for critical errors
- **Automatic duplication**: All ERROR logs from key components

### Components with error duplication:
- **Application**: `org.apolenkov.application`
- **Spring Security**: `org.springframework.security`
- **Vaadin**: `com.vaadin`
- **Spring MVC**: `org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver`
- **Transactions**: `org.springframework.transaction`
- **Database**: `org.springframework.jdbc`

## Monitoring and Maintenance

### Check log sizes
```bash
du -sh logs/
du -sh logs/archive/
```

### Clean old logs
```bash
# Remove logs older than 30 days
find logs/archive/ -name "*.log" -mtime +30 -delete
```

### Real-time log monitoring
```bash
# Main log
tail -f logs/application.log

# Error log
tail -f logs/error.log

# Critical errors
tail -f logs/critical-errors.log

# SQL log
tail -f logs/sql.log

# Security log
tail -f logs/security.log
```

## Logging Level Configuration

To change logging levels at runtime:

```yaml
logging:
  level:
    org.apolenkov.application: DEBUG
    org.springframework.security: DEBUG
    com.vaadin: INFO
```

## Troubleshooting

### Logs are not created
1. Check access rights to the `logs/` directory
2. Ensure `logback-spring.xml` is in `src/main/resources/`
3. Verify the application is running with the correct profile

### Too many logs
1. Reduce logging level for corresponding packages
2. Configure more aggressive rotation in `logback-spring.xml`
3. Use filters to exclude unnecessary messages

### Logs take up too much space
1. Reduce `maxHistory` in rotation settings
2. Reduce `totalSizeCap` to limit total size
3. Configure automatic cleanup of old logs
