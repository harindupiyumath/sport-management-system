# Backend & Database Comprehensive Test Report

**Project:** Sport Management System (Wayamba University)  
**Database:** MySQL / MariaDB (`sport_ms`)  
**Backend Framework:** Spring Boot 3.4.3 (Java 17, Spring Data JPA, Spring Security, JWT)  
**Frontend Framework:** Vanilla HTML5, CSS3, JavaScript (Fetch API)  
**Test Suite Result:** **35 / 35 Passed (100% Pass Rate)**

---

## 1. Database Creation & Schema Verification

The database was initialized using MariaDB 10.11 on port `3306`:
```sql
CREATE DATABASE sport_ms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Upon backend startup, Spring Boot Data JPA automatically created the relational schema mapping all OOP inheritance and association models:

| Table | Purpose | OOP / JPA Mapping Details |
|---|---|---|
| `users` | User accounts | Single Table Inheritance (`Student`, `Lecturer`, `Admin`) with discriminator `role` |
| `facilities` | Sports venues | Single Table Inheritance (`IndoorCourt`, `OutdoorGround`) with discriminator `facility_type` |
| `sports` | Sports list | Many-to-One association with `SportsFacility` |
| `bookings` | Court / ground reservations | Many-to-One associations with `User` and `Sport` |
| `notifications` | User alerts | Many-to-One association with `User` |

### Initial Seed Data Verified
1. **Super Admin:** Seeded automatically (`admin` / `Admin@123`, role: `ADMIN`, faculty: `TECHNOLOGY`, priority: `999`).
2. **Facilities:** 
   - `Indoor Court` (`INDOOR`)
   - `Outdoor Ground` (`OUTDOOR`)
3. **Sports:**
   - **Indoor:** Badminton, Volleyball, Table Tennis, Carrom
   - **Outdoor:** Cricket, Football, Netball, Athletics

---

## 2. Comprehensive Test Case Execution Results

| # | Test Category | Test Case Description | Expected Result | Actual Result | Status |
|---|---|---|---|---|:---:|
| 1 | **Frontend UI** | HTTP 200: `index.html` | 200 OK | 200 OK | **PASS** |
| 2 | **Frontend UI** | HTTP 200: `login.html` | 200 OK | 200 OK | **PASS** |
| 3 | **Frontend UI** | HTTP 200: `signup.html` | 200 OK | 200 OK | **PASS** |
| 4 | **Frontend UI** | HTTP 200: `forgot-password.html` | 200 OK | 200 OK | **PASS** |
| 5 | **Frontend UI** | HTTP 200: `dashboard.html` | 200 OK | 200 OK | **PASS** |
| 6 | **Frontend UI** | HTTP 200: `admin-dashboard.html` | 200 OK | 200 OK | **PASS** |
| 7 | **Authentication** | Default Super Admin Login | 200 OK + JWT with `ADMIN` role | 200 OK, Token generated | **PASS** |
| 8 | **Authentication** | Login with Incorrect Password | 401 Unauthorized | 401 Unauthorized | **PASS** |
| 9 | **Authentication** | Valid Student Registration (`242203`) | 201 Created + Success message | 201 Created | **PASS** |
| 10 | **Authentication** | Signup with Password Mismatch | 400 Bad Request ("Passwords do not match") | 400 Bad Request | **PASS** |
| 11 | **Authentication** | Signup with Weak Password | 400 Bad Request (Complexity violation) | 400 Bad Request | **PASS** |
| 12 | **Authentication** | Signup with Duplicate User ID | 409 Conflict ("User ID already exists") | 409 Conflict | **PASS** |
| 13 | **Authentication** | Invalid Faculty Rejection | 400 Bad Request ("Invalid faculty") | 400 Bad Request | **PASS** |
| 14 | **Authentication** | Lecturer 1 Signup & Login (`L001`, `APPLIED_SCIENCES`) | 201 Created + 200 OK with `LECTURER` role | 200 OK, Token received | **PASS** |
| 15 | **Authentication** | Lecturer 2 Signup & Login (`L002`, `BUSINESS_STUDIES`) | 201 Created + 200 OK with `LECTURER` role | 200 OK, Token received | **PASS** |
| 16 | **Authentication** | Student 2 Signup & Login (`242301`, `APPLIED_SCIENCES`) | 201 Created + 200 OK with `STUDENT` role | 200 OK, Token received | **PASS** |
| 17 | **Authentication** | Forgot Password Reset & Re-login | 200 OK + Successful login with new password | 200 OK, New token acquired | **PASS** |
| 18 | **User Profile** | GET `/api/users/me` (Own Profile) | 200 OK with user details | 242203 / STUDENT / TECHNOLOGY | **PASS** |
| 19 | **Security / Privacy** | Profile API Excludes Password Field | Password field omitted from response | No password leaked in DTO | **PASS** |
| 20 | **Sports & Facilities** | Query Indoor Sports | 4 sports (Badminton, Volleyball, Table Tennis, Carrom) | 4 indoor sports returned | **PASS** |
| 21 | **Sports & Facilities** | Query Outdoor Sports | 4 sports (Cricket, Football, Netball, Athletics) | 4 outdoor sports returned | **PASS** |
| 22 | **Slot Management** | Query Initial Slots (07:00–19:00) | 12 AVAILABLE slots returned | 12 AVAILABLE slots returned | **PASS** |
| 23 | **Booking Operations** | Student Books Available Slot | 201 Created & status=`PENDING` | Status: PENDING | **PASS** |
| 24 | **Slot Management** | Slot Status reflects `PENDING` | Slot query shows status `PENDING` | Status: PENDING | **PASS** |
| 25 | **Booking Operations** | Prevent Duplicate Booking by Same User | 409 Conflict ("You already have a booking") | 409 Conflict | **PASS** |
| 26 | **Lecturer Priority** | Lecturer Overrides Pending Student Request | Lecturer=`PENDING`, Student=`REJECTED`, Notification sent | Priority rule cleanly enforced | **PASS** |
| 27 | **Admin Operations** | Admin Approves Booking -> Slot BOOKED | Slot status locks to `BOOKED` | Slot status: BOOKED | **PASS** |
| 28 | **Lecturer Priority** | Approved Slot is Non-Overridable | 409 Conflict ("Time slot is already booked") | 409 Conflict | **PASS** |
| 29 | **Outdoor Sports** | Student Books Outdoor Football Slot | 201 Created & status=`PENDING` | Status: PENDING, Sport: Football | **PASS** |
| 30 | **Admin Operations** | Admin Rejects Booking -> Notification & Slot Released | Status `REJECTED`, Notification sent, Slot returns to `AVAILABLE` | Status: REJECTED, Slot: AVAILABLE | **PASS** |
| 31 | **Booking Operations** | User Cancels Own Booking (Slot Released) | Status `CANCELLED` & Slot returns to `AVAILABLE` | Status: CANCELLED, Slot: AVAILABLE | **PASS** |
| 32 | **Security / RBAC** | Block Student from Admin Endpoints | 403 Forbidden | 403 Forbidden | **PASS** |
| 33 | **Security / RBAC** | Reject Invalid JWT Token | 401 / 403 | 401/403 Rejected | **PASS** |
| 34 | **Admin Operations** | Admin Lists All Registered Users | Returns all accounts across roles | List returned | **PASS** |
| 35 | **Notification System**| Mark Notification as Read | `is_read = true` in database and response | Read state updated | **PASS** |

---

## 3. Summary & Conclusion

- **Total Tests Executed:** 35
- **Passed:** 35 (100%)
- **Failed:** 0
- **Database Consistency:** Verified via direct MariaDB query checks for all tables (`users`, `facilities`, `sports`, `bookings`, `notifications`).
- **All Core Functional Flows:** Authentication, Role-based redirects, Lecturer Priority override, Admin Approval/Rejection, Outdoor/Indoor sports bookings, and Security/RBAC have been thoroughly verified and stabilized.
