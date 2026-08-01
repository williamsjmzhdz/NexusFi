#!/usr/bin/env bash
set -euo pipefail

HEALTH_URL='http://localhost:8080/api/v1/auth/health'
RESTART=0
DB_PASSWORD_ARG=''

for arg in "$@"; do
  case "$arg" in
    --restart) RESTART=1 ;;
    --password=*) DB_PASSWORD_ARG="${arg#--password=}" ;;
  esac
done

is_running() {
  local body
  body=$(curl -s -m 3 -o - -w '' "$HEALTH_URL" 2>/dev/null || true)
  [[ "$body" == "OK" ]]
}

port_owner_pid() {
  lsof -t -i :8080 -sTCP:LISTEN 2>/dev/null | head -n1
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$REPO_ROOT"

if [[ "$RESTART" -eq 0 ]] && is_running; then
  echo "NexusFi is already running locally."
  echo "API base URL: http://localhost:8080/api/v1"
  echo "Health check: $HEALTH_URL"
  exit 0
fi

PID="$(port_owner_pid || true)"

if [[ -n "$PID" ]]; then
  if [[ "$RESTART" -eq 1 ]] && is_running; then
    echo "Restart requested. Stopping current NexusFi process on port 8080 (PID $PID)..."
    kill "$PID"
    sleep 2
  else
    echo "Port 8080 is already in use."
    echo "Process: $(ps -p "$PID" -o comm=) (PID $PID)"
    echo "If this is NexusFi, keep using the running instance or rerun with --restart."
    exit 1
  fi
fi

DB_PASSWORD="${DB_PASSWORD_ARG:-${DB_PASSWORD:-}}"

if [[ -z "$DB_PASSWORD" ]]; then
  read -r -s -p "Enter local PostgreSQL password for user 'postgres': " DB_PASSWORD
  echo
fi

if [[ -z "$DB_PASSWORD" ]]; then
  echo "DB password is required." >&2
  exit 1
fi

export DB_PASSWORD

echo "Starting NexusFi with local dev profile..."
echo "Database: postgres@localhost:5432/nexusfi"
echo "API base URL: http://localhost:8080/api/v1"
echo "Health check: $HEALTH_URL"
echo

JAVA_HOME="${JAVA_HOME:-$HOME/.jdks/temurin-17}" ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
