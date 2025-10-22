# Jib Extra Files

This directory contains additional files that will be copied into the Docker container during the Jib build process.

## Structure

- `/app/` - Application files
- `/app/application.jar` - Main application JAR file

## Permissions

Files in this directory are automatically configured with appropriate permissions:
- `/app` - 755 (executable directory)
- `/app/application.jar` - 644 (readable JAR file)

## Security

The container runs as non-root user (1000:1000) for enhanced security.
