# ASCII-only script for Windows PowerShell 5.1 compatibility.
$ErrorActionPreference = "Stop"

$runtimeDir = Join-Path $PSScriptRoot ".runtime"

foreach ($serviceName in @("frontend", "backend")) {
    $recordPath = Join-Path $runtimeDir "$serviceName.json"
    if (-not (Test-Path $recordPath)) {
        Write-Host "$serviceName has no process recorded by the startup script." -ForegroundColor DarkYellow
        continue
    }

    $record = Get-Content -Raw $recordPath | ConvertFrom-Json
    $process = Get-Process -Id $record.pid -ErrorAction SilentlyContinue
    if ($process -and $process.ProcessName -eq $record.name `
            -and $process.StartTime.ToString("o") -eq $record.startTime) {
        Stop-Process -Id $process.Id
        Write-Host "$serviceName stopped." -ForegroundColor Green
    }
    else {
        Write-Host "$serviceName was already stopped; no unrelated process was changed." -ForegroundColor DarkYellow
    }
    Remove-Item -LiteralPath $recordPath -Force
}
