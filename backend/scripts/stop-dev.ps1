param(
    [switch]$Force
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

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
Set-Location $repoRoot

$portOwner = Get-Port8080Owner

if (-not $portOwner) {
    Write-Host 'NexusFi is not running locally on port 8080.' -ForegroundColor Yellow
    return
}

$isNexusFiRunning = Test-NexusFiRunning -Url $healthUrl

if (-not $isNexusFiRunning -and -not $Force) {
    Write-Host 'Port 8080 is in use, but it does not look like NexusFi.' -ForegroundColor Red
    Write-Host "Process: $($portOwner.ProcessName) (PID $($portOwner.ProcessId))" -ForegroundColor Yellow
    if ($portOwner.CommandLine) {
        Write-Host "Command line: $($portOwner.CommandLine)" -ForegroundColor DarkGray
    }
    Write-Host 'Refusing to stop it automatically.' -ForegroundColor DarkGray
    Write-Host 'If you really want to stop whatever is on 8080, rerun with -Force.' -ForegroundColor DarkGray
    exit 1
}

Write-Host "Stopping NexusFi on port 8080 (PID $($portOwner.ProcessId))..." -ForegroundColor Cyan
Stop-Process -Id $portOwner.ProcessId -Force
Start-Sleep -Seconds 2

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Write-Host 'Port 8080 is still in use after stop attempt.' -ForegroundColor Red
    exit 1
}

Write-Host 'NexusFi local dev server stopped.' -ForegroundColor Green

