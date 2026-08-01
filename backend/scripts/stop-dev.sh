#!/usr/bin/env bash
set -euo pipefail

HEALTH_URL='http://localhost:8080/api/v1/auth/health'
FORCE=0

for arg in "$@"; do
  case "$arg" in
    --force) FORCE=1 ;;
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

PID="$(port_owner_pid || true)"

if [[ -z "$PID" ]]; then
  echo "NexusFi is not running locally on port 8080."
  exit 0
fi

if ! is_running && [[ "$FORCE" -eq 0 ]]; then
  echo "Port 8080 is in use, but it does not look like NexusFi."
  echo "Process: $(ps -p "$PID" -o comm=) (PID $PID)"
  echo "Refusing to stop it automatically."
  echo "If you really want to stop whatever is on 8080, rerun with --force."
  exit 1
fi

echo "Stopping NexusFi on port 8080 (PID $PID)..."
kill "$PID"
sleep 2

if port_owner_pid >/dev/null 2>&1 && [[ -n "$(port_owner_pid || true)" ]]; then
  echo "Port 8080 is still in use after stop attempt." >&2
  exit 1
fi

echo "NexusFi local dev server stopped."
