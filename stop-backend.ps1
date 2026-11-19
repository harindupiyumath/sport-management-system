# Stop any process listening on port 8080 (or custom port)
param(
    [int]$Port = 8080
)

$connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
if ($connections) {
    $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        $proc = Get-Process -Id $p -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "Stopping process $($proc.ProcessName) (PID: $p) on port $Port..." -ForegroundColor Yellow
            Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
            Write-Host "Successfully stopped PID $p. Port $Port is now free." -ForegroundColor Green
        }
    }
} else {
    Write-Host "No process is listening on port $Port." -ForegroundColor Green
}

