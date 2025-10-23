# Railway Deployment Dockerfile
# This is a lightweight wrapper that pulls the pre-built Jib image from GitHub Container Registry

FROM ghcr.io/apolenkov/memocards:latest

# Railway will inject environment variables automatically
# No need to set CMD or ENTRYPOINT as the base image already has them

EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
