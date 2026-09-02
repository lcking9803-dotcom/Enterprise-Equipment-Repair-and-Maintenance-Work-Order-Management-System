# ASCII-only script for Windows PowerShell 5.1 compatibility.
$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$runtimeDir = Join-Path $projectRoot ".runtime"
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"
$backendJar = Join-Path $backendDir "target\enterprise-maintenance-1.0.0.jar"
$viteScript = Join-Path $frontendDir "node_modules\vite\bin\vite.js"

New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null

function Test-PortListening([int]$port) {
    return $null -ne (Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1)
}

function Save-PortProcessRecord([int]$port, [string]$serviceName) {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction Stop | Select-Object -First 1
    $process = Get-Process -Id $listener.OwningProcess -ErrorAction Stop
    $record = [ordered]@{
        pid = $process.Id
        name = $process.ProcessName
        startTime = $process.StartTime.ToString("o")
    }
    $record | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $runtimeDir "$serviceName.json")
}

if (-not (Test-Path $backendJar)) {
    throw "Backend JAR not found: $backendJar. Run 'mvn package' in the backend folder first."
}

$startedBackend = $false
if (-not (Test-PortListening 8080)) {
    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if (-not $javaCommand) {
        throw "Java was not found. Install Java 17 and verify that 'java -version' works."
    }
    $backendProcess = Start-Process -FilePath $javaCommand.Source `
        -ArgumentList @("-jar", "target\enterprise-maintenance-1.0.0.jar") `
        -WorkingDirectory $backendDir -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $runtimeDir "backend.log") `
        -RedirectStandardError (Join-Path $runtimeDir "backend-error.log")
    $startedBackend = $true
    Write-Host "[1/2] Starting backend..." -ForegroundColor Cyan
}
else {
    Write-Host "[1/2] Backend is already running." -ForegroundColor Green
}

$nodeCommand = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeCommand) {
    $codexNode = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe"
    if (Test-Path $codexNode) {
        $nodePath = $codexNode
    }
    else {
        throw "Node.js was not found. Install Node.js 20 and verify that 'node -v' works."
    }
}
else {
    $nodePath = $nodeCommand.Source
}

if (-not (Test-Path $viteScript)) {
    throw "Frontend dependencies are missing. Run 'npm install' in the frontend folder."
}

$startedFrontend = $false
if (-not (Test-PortListening 5173)) {
    $frontendProcess = Start-Process -FilePath $nodePath `
        -ArgumentList @("node_modules\vite\bin\vite.js", "--host", "127.0.0.1") `
        -WorkingDirectory $frontendDir -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $runtimeDir "frontend.log") `
        -RedirectStandardError (Join-Path $runtimeDir "frontend-error.log")
    $startedFrontend = $true
    Write-Host "[2/2] Starting frontend..." -ForegroundColor Cyan
}
else {
    Write-Host "[2/2] Frontend is already running." -ForegroundColor Green
}

$ready = $false
for ($attempt = 1; $attempt -le 30; $attempt++) {
    if ((Test-PortListening 8080) -and (Test-PortListening 5173)) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $ready) {
    throw "Startup timed out. Check backend-error.log and frontend-error.log in the .runtime folder."
}

if ($startedBackend) {
    Save-PortProcessRecord 8080 "backend"
}
if ($startedFrontend) {
    Save-PortProcessRecord 5173 "frontend"
}

Write-Host "System is ready: http://localhost:5173" -ForegroundColor Green
Write-Host "Admin account: admin / Admin@123" -ForegroundColor Yellow
Start-Process "http://localhost:5173"
