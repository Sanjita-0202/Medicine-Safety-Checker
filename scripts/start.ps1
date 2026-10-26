param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'Install Java 25 or later and add it to PATH before starting.'
}
Push-Location -LiteralPath $projectRoot
try {
    if (-not $SkipBuild) {
        & .\mvnw.cmd install
        if ($LASTEXITCODE -ne 0) { throw 'Build or tests failed. The application was not started.' }
    }
    Write-Host 'Starting Medicine Safety Checker. The ignored root .env is loaded automatically.'
    & .\mvnw.cmd -pl backend spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Startup failed. Check the database credentials and Atlas network access.' }
} finally {
    Pop-Location
}
