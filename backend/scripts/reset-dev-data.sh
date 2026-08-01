#!/usr/bin/env bash
set -euo pipefail

FORCE=0
DB_PASSWORD_ARG=''

for arg in "$@"; do
  case "$arg" in
    --force) FORCE=1 ;;
    --password=*) DB_PASSWORD_ARG="${arg#--password=}" ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$REPO_ROOT"

if ! command -v psql >/dev/null 2>&1; then
  echo "psql was not found in PATH. Install PostgreSQL client tools first." >&2
  exit 1
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

export PGPASSWORD="$DB_PASSWORD"

echo "This will permanently delete ALL local NexusFi data from database: nexusfi"
echo "Affected tables: users, categories, income_records, expense_records, transfers, movements"
echo "Identities will be restarted so Postman can run from a clean state."
echo

if [[ "$FORCE" -eq 0 ]]; then
  read -r -p "Type RESET to continue: " CONFIRMATION
  if [[ "$CONFIRMATION" != "RESET" ]]; then
    echo "Reset cancelled."
    exit 0
  fi
fi

echo "Resetting local data..."
psql -h localhost -U postgres -d nexusfi -v ON_ERROR_STOP=1 -c "
TRUNCATE TABLE
    transfers,
    movements,
    expense_records,
    income_records,
    categories,
    users
RESTART IDENTITY CASCADE;
"

echo
echo "Verifying row counts..."
psql -h localhost -U postgres -d nexusfi -v ON_ERROR_STOP=1 -c "SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users UNION ALL SELECT 'categories', COUNT(*) FROM categories UNION ALL SELECT 'income_records', COUNT(*) FROM income_records UNION ALL SELECT 'expense_records', COUNT(*) FROM expense_records UNION ALL SELECT 'transfers', COUNT(*) FROM transfers UNION ALL SELECT 'movements', COUNT(*) FROM movements ORDER BY table_name;"

echo
echo "Local NexusFi data reset complete."
