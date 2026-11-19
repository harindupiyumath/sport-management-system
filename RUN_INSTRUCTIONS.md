# RUN_INSTRUCTIONS.md – Complete Execution & Testing Guide

This guide details all prerequisites, commands, database management steps, and test procedures for the **Sport Management System** (Spring Boot Backend + Vanilla JS Frontend + PostgreSQL).

---

## 1. Quick Start Architecture & Ports

| Service | Port | Base URL / Command |
|---|---|---|
| **PostgreSQL** | `5432` | Windows service `postgresql-x64-19` |
| **Spring Boot Backend** | `8080` | `http://localhost:8080/api` |
| **Frontend Web App** | `5500` | `http://localhost:5500/login.html` |

---

## 2. Step-by-Step Run Instructions

### Step 1: Make Sure PostgreSQL Is Running
PostgreSQL runs as a Windows service and normally starts automatically:
```powershell
Get-Service postgresql*            # Status should be "Running"
Start-Service postgresql-x64-19    # if it is stopped (run as Administrator)
```

---

### Step 2: Configure Database Credentials
The backend reads its connection settings from environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sport_ms` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | *(empty — must be set)* |

The easiest way is a `.env` file in the project root (loaded automatically by `start-backend.ps1`):
```
DB_PASSWORD=your_postgres_password
```
**Neon (current setup):** the project is linked to Neon (`.neon`, `neon.ts`). Run `neon env pull` (or `neon deploy`) to write `DATABASE_URL` / `DATABASE_URL_UNPOOLED` into `.env.local`. `start-backend.ps1` loads that file and converts the **direct (unpooled)** URL into `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` automatically, so no manual settings are needed. If `DB_URL` is set explicitly (e.g. in `.env`), it takes precedence, which is how you switch back to a local PostgreSQL.

---

### Step 3: Create or Reset Database
```powershell
$psql = "C:\Program Files\PostgreSQL\19\bin\psql.exe"

# Create database (once):
& $psql -h localhost -U postgres -c "CREATE DATABASE sport_ms;"

# OR clean reset (wipes previous test data):
& $psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS sport_ms;" -c "CREATE DATABASE sport_ms;"
```
Tables are created automatically by Hibernate when the backend starts.

---

### Step 3b: Configure Maven in PATH (If not already loaded)
Maven is located at `C:\Program Files\NetBeans-24\netbeans\java\maven\bin`:
```powershell
# Temporary (current session):
$env:Path += ";C:\Program Files\NetBeans-24\netbeans\java\maven\bin"

# Permanent (User level, run once):
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\NetBeans-24\netbeans\java\maven\bin", "User")
```

---

### Step 4: Start Backend Application (Terminal 2)
Simplest: run `.\start-backend.ps1` from the project root (loads `.env`, checks PostgreSQL, sets `JAVA_HOME`).

Or navigate to the `backend` folder and start Spring Boot (set `$env:DB_PASSWORD` first):
```powershell
cd C:\Users\Dinath\Desktop\SportManagementProject\backend
mvn spring-boot:run
```
*Or using the included wrapper:*
```powershell
cd C:\Users\Dinath\Desktop\SportManagementProject\backend
.\mvnw.cmd spring-boot:run
```
*Or using the direct Maven binary path:*
```powershell
cd C:\Users\Dinath\Desktop\SportManagementProject\backend
& "C:\Program Files\NetBeans-24\netbeans\java\maven\bin\mvn.cmd" spring-boot:run
```
> **Startup verification:** Wait until you see:
> `Started SportManagementApplication in ... seconds`
> `Default Super Admin seeded: admin / Admin@123`

---

### Step 5: Start Frontend HTTP Server (Terminal 3)
From the project root, launch the lightweight Python HTTP server on port 5500:
```powershell
cd C:\Users\Dinath\Desktop\SportManagementProject
py -m http.server 5500 -d frontend
```
*(Alternatively: open the `frontend/` directory in VS Code and click **"Open with Live Server"** on port 5500).*

---

### Step 6: Verify Active Services
Verify all 3 required ports are listening:
```powershell
Get-NetTCPConnection -LocalPort 8080,5500,5432 -ErrorAction SilentlyContinue | Select-Object LocalPort,State,OwningProcess
```
Expected output:
- `5432` — `Listen` (PostgreSQL)
- `8080` — `Listen` (Spring Boot)
- `5500` — `Listen` (Frontend Web Server)

---

## 3. Running Automated Tests

A comprehensive test suite of 35 automated end-to-end tests is provided in `run_tests.ps1`:
```powershell
cd C:\Users\Dinath\Desktop\SportManagementProject
powershell.exe -ExecutionPolicy Bypass -File run_tests.ps1
```
Results will display `[PASS]` for every case and generate a machine-readable summary in `test_results.json`:
```
==========================================
TOTAL TESTS: 35 | PASSED: 35 | FAILED: 0
==========================================
```

---

## 4. Database Inspection Commands

To view and verify records directly in PostgreSQL at any time:
```powershell
$psql = "C:\Program Files\PostgreSQL\19\bin\psql.exe"

# View all users across roles:
& $psql -h localhost -U postgres -d sport_ms -c "SELECT user_id, role, faculty, created_at FROM users;"

# View sports facilities and mapped sports:
& $psql -h localhost -U postgres -d sport_ms -c "SELECT f.name as facility, f.facility_type, s.name as sport FROM sports s JOIN facilities f ON s.facility_id = f.id;"

# View all bookings with their status:
& $psql -h localhost -U postgres -d sport_ms -c "SELECT b.id, b.user_id, s.name as sport, b.booking_date, b.start_time, b.status FROM bookings b JOIN sports s ON b.sport_id = s.id;"

# View audit notifications:
& $psql -h localhost -U postgres -d sport_ms -c "SELECT id, user_id, is_read, message FROM notifications;"
```

---

## 5. Web Application Access & Default Credentials

| Portal | URL | Credentials |
|---|---|---|
| **Frontend Login** | `http://localhost:5500/login.html` | — |
| **Super Admin** | `http://localhost:5500/login.html` | User: `admin` / Pass: `Admin@123` |
| **Student** | `http://localhost:5500/signup.html` | Register (e.g. ID: `242203`, Faculty: `TECHNOLOGY`) |
| **Lecturer** | `http://localhost:5500/signup.html` | Register (e.g. ID: `L001`, Faculty: `APPLIED_SCIENCES`) |

---

## 6. Common Issues & Solutions

| Issue | Cause | Solution |
|---|---|---|
| `Connection to localhost:5432 refused` | PostgreSQL is not running | Run `Start-Service postgresql-x64-19` (as Administrator) |
| `password authentication failed for user "postgres"` / `no password supplied` | `DB_PASSWORD` not set or wrong | Set it in `.env` or `$env:DB_PASSWORD = '...'` |
| `database "sport_ms" does not exist` | Database not created yet | Run the `CREATE DATABASE` command in Step 3 |
| `Port 8080 already in use` | Previous Java process didn't terminate | Run `Get-NetTCPConnection -LocalPort 8080 \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| `mvn: The term 'mvn' is not recognized` | Maven path not loaded into current PowerShell session | Run `$env:Path += ";C:\Program Files\NetBeans-24\netbeans\java\maven\bin"` |
| `User ID already exists (409 Conflict)` | DB has test users from previous test run | Reset the database cleanly using Step 2 command above |
