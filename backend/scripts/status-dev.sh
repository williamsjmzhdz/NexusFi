#!/usr/bin/env bash
set -euo pipefail

HEALTH_URL='http://localhost:8080/api/v1/auth/health'

is_running() {
  local body
  body=$(curl -s -m 3 -o - -w '' "$HEALTH_URL" 2>/dev/null || true)
  [[ "$body" == "OK" ]]
}

port_owner_pid() {
  lsof -t -i :8080 -sTCP:LISTEN 2>/dev/null | head -n1
}

echo "NexusFi local dev status"
echo "------------------------"
echo "API base URL: http://localhost:8080/api/v1"
echo "Health URL: $HEALTH_URL"
echo

PID="$(port_owner_pid || true)"

if is_running; then
  echo "Status: RUNNING"
  if [[ -n "$PID" ]]; then
    echo "PID: $PID"
    echo "Process: $(ps -p "$PID" -o comm=)"
  fi
  exit 0
fi

if [[ -n "$PID" ]]; then
  echo "Status: PORT 8080 IN USE (not confirmed as NexusFi)"
  echo "PID: $PID"
  echo "Process: $(ps -p "$PID" -o comm=)"
  exit 1
fi

echo "Status: STOPPED"
exit 0
