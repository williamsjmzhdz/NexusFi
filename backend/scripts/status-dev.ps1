$ErrorActionPreference = 'Stop'

$healthUrl = 'http://localhost:8080/api/v1/auth/health'

function Test-NexusFiRunning {
    param(
        [string]$Url
    )

    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
        return [PSCustomObject]@{
            IsRunning = ($response.StatusCode -eq 200 -and $response.Content -eq 'OK')
            StatusCode = $response.StatusCode
            Content = $response.Content
        }
    }
    catch {
        return [PSCustomObject]@{
            IsRunning = $false
            StatusCode = $null
            Content = $null
        }
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

$health = Test-NexusFiRunning -Url $healthUrl
$portOwner = Get-Port8080Owner

Write-Host 'NexusFi local dev status' -ForegroundColor Cyan
Write-Host '------------------------' -ForegroundColor Cyan
Write-Host 'API base URL: http://localhost:8080/api/v1' -ForegroundColor DarkGray
Write-Host "Health URL: $healthUrl" -ForegroundColor DarkGray
Write-Host ''

if ($health.IsRunning) {
    Write-Host 'Status: RUNNING' -ForegroundColor Green
    if ($portOwner) {
        Write-Host "PID: $($portOwner.ProcessId)" -ForegroundColor DarkGray
        Write-Host "Process: $($portOwner.ProcessName)" -ForegroundColor DarkGray
    }
    exit 0
}

if ($portOwner) {
    Write-Host 'Status: PORT 8080 IN USE (not confirmed as NexusFi)' -ForegroundColor Yellow
    Write-Host "PID: $($portOwner.ProcessId)" -ForegroundColor DarkGray
    Write-Host "Process: $($portOwner.ProcessName)" -ForegroundColor DarkGray
    if ($portOwner.CommandLine) {
        Write-Host "Command line: $($portOwner.CommandLine)" -ForegroundColor DarkGray
    }
    exit 1
}

Write-Host 'Status: STOPPED' -ForegroundColor Yellow
exit 0

