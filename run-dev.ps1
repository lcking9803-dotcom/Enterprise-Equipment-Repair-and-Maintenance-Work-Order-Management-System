$ErrorActionPreference = "Stop"
$backendPath = Join-Path $PSScriptRoot "backend"

Push-Location $backendPath
try {
    mvn package
    if ($LASTEXITCODE -ne 0) {
        throw "后端构建或测试失败，应用未启动。"
    }
    java -jar "target\enterprise-maintenance-1.0.0.jar"
}
finally {
    Pop-Location
}
