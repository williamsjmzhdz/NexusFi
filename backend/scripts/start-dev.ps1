param(
    [string]$DbPassword,
    [switch]$Restart
)

$ErrorActionPreference = 'Stop'

$healthUrl = 'http://localhost:8080/api/v1/auth/health'

function Test-NexusFiRunning {
    param(
        [string]$Url
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
        return $response.StatusCode -eq 200 -and $response.Content -eq 'OK'
    }
    catch {
        return $false
    }
}

function Get-Port8080Owner {
    $connection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1

    if (-not $connection) {
        return $null
    }

    $owningProcessId = $connection.OwningProcess
    $process = Get-Process -Id $owningProcessId -ErrorAction SilentlyContinue
    $cimProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $owningProcessId" -ErrorAction SilentlyContinue

    [PSCustomObject]@{
        ProcessId = $owningProcessId
        ProcessName = $process.ProcessName
        CommandLine = $cimProcess.CommandLine
    }
}

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $repoRoot

if ((-not $Restart) -and (Test-NexusFiRunning -Url $healthUrl)) {
    Write-Host 'NexusFi is already running locally.' -ForegroundColor Green
    Write-Host 'API base URL: http://localhost:8080/api/v1' -ForegroundColor DarkGray
    Write-Host "Health check: $healthUrl" -ForegroundColor DarkGray
    return
}

$portOwner = Get-Port8080Owner

if ($portOwner) {
    if ($Restart -and (Test-NexusFiRunning -Url $healthUrl)) {
        Write-Host "Restart requested. Stopping current NexusFi process on port 8080 (PID $($portOwner.ProcessId))..." -ForegroundColor Yellow
        Stop-Process -Id $portOwner.ProcessId -Force
        Start-Sleep -Seconds 2
    }
    else {
        Write-Host 'Port 8080 is already in use.' -ForegroundColor Red
        Write-Host "Process: $($portOwner.ProcessName) (PID $($portOwner.ProcessId))" -ForegroundColor Yellow
        if ($portOwner.CommandLine) {
            Write-Host "Command line: $($portOwner.CommandLine)" -ForegroundColor DarkGray
        }
        Write-Host 'If this is NexusFi, keep using the running instance or rerun with -Restart.' -ForegroundColor DarkGray
        Write-Host 'Example:' -ForegroundColor DarkGray
        Write-Host '  .\start-dev.ps1 -Restart' -ForegroundColor DarkGray
        exit 1
    }
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

$env:DB_PASSWORD = $DbPassword

Write-Host 'Starting NexusFi with local dev profile...' -ForegroundColor Cyan
Write-Host 'Database: postgres@localhost:5432/nexusfi' -ForegroundColor DarkGray
Write-Host 'API base URL: http://localhost:8080/api/v1' -ForegroundColor DarkGray
Write-Host "Health check: $healthUrl" -ForegroundColor DarkGray
Write-Host ''

& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

