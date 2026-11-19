# Start Frontend Script - serves ./frontend on http://localhost:5500
param(
    [int]$Port = 5500
)

if (-not (Get-Command node -ErrorAction SilentlyContinue) -and (Test-Path "C:\Program Files\nodejs\node.exe")) {
    $env:Path = "C:\Program Files\nodejs;" + $env:Path
}
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Warning "Node.js is not installed. Install it with: winget install OpenJS.NodeJS.LTS"
    exit 1
}

Write-Host "[INFO] Open http://localhost:$Port/login.html (the backend must be running: .\start-backend.ps1)" -ForegroundColor Green
node "$PSScriptRoot\frontend-server.mjs" $Port
