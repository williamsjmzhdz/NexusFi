param(
    [string]$DbPassword,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
Set-Location $repoRoot

if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    throw 'psql was not found in PATH. Install PostgreSQL client tools or open a terminal where psql is available.'
}

if (-not $DbPassword) {
    $DbPassword = $env:DB_PASSWORD
}

if (-not $DbPassword) {
    $securePassword = Read-Host "Enter local PostgreSQL password for user 'postgres'" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        if ($bstr -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
    }
}

if (-not $DbPassword) {
    throw 'DB password is required.'
}

$env:PGPASSWORD = $DbPassword

Write-Host 'This will permanently delete ALL local NexusFi data from database: nexusfi' -ForegroundColor Yellow
Write-Host 'Affected tables: users, categories, income_records, expense_records, transfers, movements' -ForegroundColor Yellow
Write-Host 'Identities will be restarted so Postman can run from a clean state.' -ForegroundColor Yellow
Write-Host ''

if (-not $Force) {
    $confirmation = Read-Host "Type RESET to continue"
    if ($confirmation -ne 'RESET') {
        Write-Host 'Reset cancelled.' -ForegroundColor Yellow
        exit 0
    }
}

$truncateSql = @"
TRUNCATE TABLE
    transfers,
    movements,
    expense_records,
    income_records,
    categories,
    users
RESTART IDENTITY CASCADE;
"@

Write-Host 'Resetting local data...' -ForegroundColor Cyan
psql -h localhost -U postgres -d nexusfi -v ON_ERROR_STOP=1 -c $truncateSql | Out-Host

Write-Host ''
Write-Host 'Verifying row counts...' -ForegroundColor Cyan
psql -h localhost -U postgres -d nexusfi -v ON_ERROR_STOP=1 -c "SELECT 'users' AS table_name, COUNT(*) AS row_count FROM users UNION ALL SELECT 'categories', COUNT(*) FROM categories UNION ALL SELECT 'income_records', COUNT(*) FROM income_records UNION ALL SELECT 'expense_records', COUNT(*) FROM expense_records UNION ALL SELECT 'transfers', COUNT(*) FROM transfers UNION ALL SELECT 'movements', COUNT(*) FROM movements ORDER BY table_name;" | Out-Host

Write-Host ''
Write-Host 'Local NexusFi data reset complete.' -ForegroundColor Green

