# Start Backend Script
param(
    [int]$Port = 8080
)

# 1. Load database settings from a .env file (project root or backend folder), if present.
#    Format: one KEY=VALUE per line, e.g.
#      DB_URL=jdbc:postgresql://localhost:5432/sport_ms
#      DB_USERNAME=postgres
#      DB_PASSWORD=yourpassword
#    .env.local is written by `neon deploy` / `neon env pull` (DATABASE_URL, DATABASE_URL_UNPOOLED).
foreach ($envFile in @("$PSScriptRoot\.env", "$PSScriptRoot\.env.local", "$PSScriptRoot\backend\.env")) {
    if (Test-Path $envFile) {
        Write-Host "[INFO] Loading settings from $envFile" -ForegroundColor Cyan
        Get-Content $envFile | ForEach-Object {
            $line = $_.Trim()
            if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
                $key, $value = $line.Split("=", 2)
                Set-Item -Path "Env:$($key.Trim())" -Value $value.Trim().Trim('"')
            }
        }
    }
}

# 1a. JWT signing secret: generate a random one on first run and keep it in .env (git-ignored)
if (-not $env:JWT_SECRET) {
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $env:JWT_SECRET = -join ($bytes | ForEach-Object { $_.ToString("X2") })
    Add-Content -Path "$PSScriptRoot\.env" -Value "JWT_SECRET=$($env:JWT_SECRET)" -Encoding ascii
    Write-Host "[INFO] Generated a new JWT_SECRET and saved it to .env" -ForegroundColor Yellow
}

# 1b. Neon: convert the postgresql:// URL into the JDBC settings Spring Boot reads.
#     Use the DIRECT (unpooled) URL: the backend keeps its own HikariCP pool, and Hibernate
#     runs schema updates at startup, which need session state that PgBouncer (-pooler) doesn't keep.
$neonUrl = if ($env:DATABASE_URL_UNPOOLED) { $env:DATABASE_URL_UNPOOLED } else { $env:DATABASE_URL }
if (-not $env:DB_URL -and $neonUrl) {
    $uri = [System.Uri]($neonUrl -replace '^postgres(ql)?://', 'http://')
    $userInfo = $uri.UserInfo.Split(":", 2)
    $env:DB_USERNAME = [System.Uri]::UnescapeDataString($userInfo[0])
    $env:DB_PASSWORD = [System.Uri]::UnescapeDataString($userInfo[1])
    $port = if ($uri.IsDefaultPort) { 5432 } else { $uri.Port }
    $env:DB_URL = "jdbc:postgresql://$($uri.Host):$port$($uri.AbsolutePath)?sslmode=require"
    Write-Host "[INFO] Using Neon database at $($uri.Host)$($uri.AbsolutePath)" -ForegroundColor Cyan
}

# 2. If using a local PostgreSQL (default), make sure it is running on 5432
$dbUrl = if ($env:DB_URL) { $env:DB_URL } else { "jdbc:postgresql://localhost:5432/sport_ms" }
if ($dbUrl -match "localhost|127\.0\.0\.1") {
    $pgConn = Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue
    if (-not $pgConn) {
        Write-Host "[INFO] PostgreSQL is not detected on port 5432. Attempting to start the service..." -ForegroundColor Yellow
        $svc = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($svc) {
            try { Start-Service $svc.Name -ErrorAction Stop; Start-Sleep -Seconds 2 }
            catch { Write-Warning "Could not start $($svc.Name) (try running as Administrator): $_" }
        } else {
            Write-Warning "No PostgreSQL Windows service found. Please start PostgreSQL manually."
        }
    }
}
if (-not $env:DB_PASSWORD) {
    Write-Warning "DB_PASSWORD is not set. Put it in a .env file or run: `$env:DB_PASSWORD = 'yourpassword'"
}

# 3. Free port 8080 if an existing instance is still lingering
$connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
if ($connections) {
    $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($p in $pids) {
        $proc = Get-Process -Id $p -ErrorAction SilentlyContinue
        if ($proc -and $proc.ProcessName -eq "java") {
            Write-Host "[INFO] Freeing port $Port from previous Java process (PID: $p)..." -ForegroundColor Yellow
            Stop-Process -Id $p -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 1
        }
    }
}

# 4. Make sure a JDK is available (the Maven wrapper needs JAVA_HOME)
if (-not $env:JAVA_HOME -and (Test-Path "C:\Program Files\Apache NetBeans\jdk\bin\java.exe")) {
    $env:JAVA_HOME = "C:\Program Files\Apache NetBeans\jdk"
}

Write-Host "[INFO] Starting Spring Boot Backend on port $Port..." -ForegroundColor Green
Set-Location -Path "$PSScriptRoot\backend"
.\mvnw.cmd spring-boot:run
