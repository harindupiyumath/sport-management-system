# BACKEND.md – Sport Management System (Java + Spring Boot)

CMIS 2113 – OOP Mini Project | Wayamba University of Sri Lanka
Spring Boot setup details are in `SPRINGBOOT.md`. Endpoints are in `API.md`.

---

## 1. Tech

| Item | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation) |
| Database | MySQL |
| Auth | JWT (stateless) + BCrypt password hashing |
| Build | Maven |

---

## 2. Package Structure

```
com.wayamba.sportms
├── SportManagementApplication.java
├── config/            SecurityConfig, CorsConfig, JwtFilter
├── controller/        AuthController, SportController, BookingController,
│                      NotificationController, UserController, AdminController
├── service/           AuthService, BookingService, SlotService,
│                      NotificationService, UserService
├── repository/        UserRepository, SportRepository, FacilityRepository,
│                      BookingRepository, NotificationRepository
├── model/             User (abstract), Student, Lecturer, Admin,
│                      SportsFacility (abstract), IndoorCourt, OutdoorGround,
│                      Sport, Booking, Notification, enums
├── dto/               SignupRequest, LoginRequest, BookingRequest, ...
├── exception/         custom exceptions + GlobalExceptionHandler
└── util/              JwtUtil, PasswordValidator
```

---

## 3. OOP Concepts Mapping (required by the assignment)

| Concept | Where it is used |
|---|---|
| **Encapsulation** | All entity fields `private`; access through getters/setters; booking `status` changed only via `Booking.approve()`, `reject()`, `cancel()` |
| **Inheritance** | `Student`, `Lecturer`, `Admin` extend abstract `User`; `IndoorCourt`, `OutdoorGround` extend abstract `SportsFacility` |
| **Polymorphism** | `User.getUserType()`, `User.getBookingPriority()` overridden in each subclass (Lecturer = 2, Student = 1); `SportsFacility.getFacilityType()` |
| **Abstraction** | `abstract class User`, `abstract class SportsFacility`; service interfaces (e.g. `BookingService`) hide implementation |
| Collections | `List<Booking>`, `List<TimeSlot>` in services |
| Exception handling | Custom exceptions + `@RestControllerAdvice` |

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")
public abstract class User {
    @Id private String userId;          // student id / lecturer id = username
    private String faculty;
    private String passwordHash;

    public abstract String getUserType();
    public abstract int getBookingPriority();   // polymorphism
    // getters/setters ...
}

@Entity @DiscriminatorValue("LECTURER")
public class Lecturer extends User {
    public String getUserType() { return "LECTURER"; }
    public int getBookingPriority() { return 2; }
}

@Entity @DiscriminatorValue("STUDENT")
public class Student extends User {
    public String getUserType() { return "STUDENT"; }
    public int getBookingPriority() { return 1; }
}
```

---

## 4. Database Schema (MySQL)

```sql
CREATE DATABASE sport_ms;

users(user_id PK, role ENUM('STUDENT','LECTURER','ADMIN'), faculty, password_hash,
      created_at)

facilities(id PK, name, type ENUM('INDOOR','OUTDOOR'))

sports(id PK, name, facility_id FK -> facilities.id)

bookings(id PK, user_id FK, sport_id FK, booking_date DATE,
         start_time TIME, end_time TIME,
         status ENUM('PENDING','APPROVED','REJECTED','CANCELLED'),
         created_at)

notifications(id PK, user_id FK, message, is_read BOOLEAN, created_at)
```

Seed data: one Admin user, 3 faculties as constants (enum `Faculty`), a few indoor sports (Badminton, Volleyball, Table Tennis, Carrom) and outdoor sports (Cricket, Football, Netball, Athletics).

---

## 5. Business Rules

### 5.1 Authentication
- **Signup**: `userId` unique (else 409), faculty must be one of `APPLIED_SCIENCES`, `BUSINESS_STUDIES`, `TECHNOLOGY`, role = STUDENT or LECTURER, password == confirmPassword, password strength rule below. Store only the BCrypt hash. Return a greeting message with the username.
- **Login**: verify userId + password, return JWT + `{userId, role, faculty}`. Invalid → 401.
- **Forgot password**: userId + newPassword + confirmPassword → validate → update hash.
  (Simplified as per the assignment: no email/OTP step. Mention this limitation in the report.)
- Password rule (regex): `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$`

### 5.2 Time slots
- Working hours **07:00 – 19:00**, 1-hour slots → 12 slots per day.
- Slot status for a given sport + date:
  - `AVAILABLE` – no active booking
  - `PENDING` – a pending request exists
  - `BOOKED` – an APPROVED booking exists
- Reject bookings outside the hours, in the past, or on a `BOOKED` slot.

### 5.3 Booking flow
`User creates request (PENDING) → Admin approves / rejects → Notification sent to the user`
- User can cancel own PENDING/APPROVED booking → status `CANCELLED`, slot becomes available.
- Admin **approve** → `APPROVED` (fail if the slot already has an approved booking). Admin **reject** → `REJECTED`.
- Duplicate booking (same user, sport, date, time) → 409.

### 5.4 Lecturer priority
If a lecturer requests a slot that has a **PENDING student** request:
1. Lecturer's request is created as PENDING.
2. The student's pending request for that slot is set to `REJECTED` and the student is notified ("Slot given to a lecturer").
3. An already **APPROVED** booking is never overridden.
Implemented with `getBookingPriority()` comparison in `BookingService`.

### 5.5 Privacy
- `/notifications` and `/users/me` always use the user id from the JWT, never from a request parameter.
- Only ADMIN can call `/admin/**` (admin sees info of all users).

### 5.6 Notifications (created by the service layer)
Booking submitted, approved, rejected, cancelled, bumped by lecturer priority.

---

## 6. Error Handling

`GlobalExceptionHandler` returns the same JSON for every error:

```json
{ "status": 409, "message": "Time slot already booked", "timestamp": "2026-11-03T10:15:30" }
```

Custom exceptions: `SlotUnavailableException`, `DuplicateBookingException`, `InvalidCredentialsException`, `UserAlreadyExistsException`, `ValidationException`, `ResourceNotFoundException`.

---

## 7. Build Order (suggested)

1. Project setup + DB connection (`SPRINGBOOT.md`)
2. Entities + repositories + seed data
3. Auth (signup / login / forgot) + JWT security
4. Sports + slots endpoints
5. Booking create / cancel + lecturer priority
6. Admin endpoints + notifications
7. Exception handler + testing with Postman
