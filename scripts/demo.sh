#!/usr/bin/env bash
# One-command local demo: PostgreSQL in Docker + the application with demo data.
# Usage: make demo   (or scripts/demo.sh). Stop: Ctrl+C, then `make demo-stop`.
# Needs JDK 21 and a running Docker engine. No secrets: values come from env.sample.
set -euo pipefail
cd "$(dirname "$0")/.."

# --- Java 21 (on macOS pick it via java_home, without touching the shell profile)
if [ -x /usr/libexec/java_home ] && JH="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
  export JAVA_HOME="$JH"
elif command -v brew >/dev/null 2>&1 && BP="$(brew --prefix openjdk@21 2>/dev/null)" \
  && [ -x "$BP/libexec/openjdk.jdk/Contents/Home/bin/java" ]; then
  # Homebrew formula openjdk@21 is keg-only and not visible to java_home
  export JAVA_HOME="$BP/libexec/openjdk.jdk/Contents/Home"
fi
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! "$JAVA" -version 2>&1 | grep -q 'version "21'; then
  echo "JDK 21 not found. Install Eclipse Temurin 21 or run: brew install openjdk@21" >&2
  exit 1
fi

# --- Docker
if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Start Docker Desktop or OrbStack (orb start) and retry." >&2
  exit 1
fi

# --- .env with local defaults
if [ ! -f .env ]; then
  cp env.sample .env
  echo "Created .env from env.sample"
fi
PORT="${SERVER_PORT:-}"   # a port given on the command line wins over .env
set -a
# shellcheck disable=SC1091
. ./.env
set +a
PORT="${PORT:-${SERVER_PORT:-8080}}"
URL="http://localhost:${PORT}"

# --- port must be free
if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port $PORT is busy. Stop the process on it or run: SERVER_PORT=8081 make demo" >&2
  exit 1
fi

# --- database (if 5432 is taken by another local PostgreSQL, use the next free port)
COMPOSE=(docker compose -f docker-compose.yml -f docker-compose.dev.yml --profile dev)
DB_PORT="${POSTGRES_PORT:-5432}"
if [ -z "$("${COMPOSE[@]}" ps -q postgres 2>/dev/null)" ]; then
  while lsof -nP -iTCP:"$DB_PORT" -sTCP:LISTEN >/dev/null 2>&1; do
    DB_PORT=$((DB_PORT + 1))
  done
else
  DB_PORT="$("${COMPOSE[@]}" port postgres 5432 | sed 's/.*://')"
fi
export POSTGRES_PORT="$DB_PORT"
DB_JDBC_URL="jdbc:postgresql://localhost:${DB_PORT}/${DB_NAME:-memocards}"
echo "Starting PostgreSQL on 127.0.0.1:${DB_PORT}..."
"${COMPOSE[@]}" up -d postgres
until "${COMPOSE[@]}" exec -T postgres pg_isready -U "${DB_USER:-memocards_user}" -d "${DB_NAME:-memocards}" >/dev/null 2>&1; do
  sleep 1
done
echo "PostgreSQL is ready."

# --- application jar (production UI bundle, no dev tools)
echo "Building application jar (first run takes a few minutes)..."
./gradlew bootJar -Pvaadin.productionMode=true --console=plain -q
JAR=""
for f in build/libs/*.jar; do
  [ -f "$f" ] || continue
  case "$f" in *-plain.jar) continue ;; esac
  if [ -z "$JAR" ] || [ "$f" -nt "$JAR" ]; then JAR="$f"; fi
done
[ -n "$JAR" ] || { echo "No application jar in build/libs" >&2; exit 1; }

mkdir -p logs
echo "Starting $JAR ..."
"$JAVA" -jar "$JAR" \
  --spring.profiles.active=prod \
  --spring.datasource.url="$DB_JDBC_URL" \
  --server.port="$PORT" \
  --vaadin.productionMode=true \
  --vaadin.launch-browser=false \
  --app.seed.demo.enabled=true \
  >logs/demo-console.log 2>&1 &
APP_PID=$!
trap 'echo; echo "Stopping application..."; kill "$APP_PID" 2>/dev/null || true; wait "$APP_PID" 2>/dev/null || true' INT TERM EXIT

for _ in $(seq 1 120); do
  if curl -fsS "$URL/actuator/health" >/dev/null 2>&1; then break; fi
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "Application exited. Last log lines:" >&2
    tail -n 40 logs/demo-console.log >&2
    exit 1
  fi
  sleep 2
done
curl -fsS "$URL/actuator/health" >/dev/null 2>&1 || { echo "Application did not become healthy, see logs/demo-console.log" >&2; exit 1; }

cat <<EOF

  Memocards is running: $URL

  Demo users:
    user@example.com  / ${DEMO_USER_PASSWORD:-user}
    admin@example.com / ${DEMO_ADMIN_PASSWORD:-admin}

  Logs: logs/demo-console.log
  Stop: Ctrl+C here, then "make demo-stop" to stop PostgreSQL (data volume is kept).

EOF
wait "$APP_PID"
