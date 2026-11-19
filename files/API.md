# API.md – REST API Contract (Frontend ⇄ Backend)

Single source of truth for how the HTML/JS frontend (`FRONTEND.md`) talks to the Spring Boot backend (`BACKEND.md`, `SPRINGBOOT.md`).

- **Base URL:** `http://localhost:8080/api`
- **Format:** JSON (`Content-Type: application/json`)
- **Auth:** `Authorization: Bearer <JWT>` on every endpoint except `/auth/**`
- **Roles:** `STUDENT`, `LECTURER`, `ADMIN`
- **Dates/Times:** date `YYYY-MM-DD`, time `HH:mm` (24h). Bookable hours 07:00–19:00.

## Error format (all endpoints)

```json
{ "status": 400, "message": "Passwords do not match", "timestamp": "2026-11-03T10:15:30" }
```

| Code | Meaning |
|---|---|
| 400 | Validation error (weak password, bad time, missing field) |
| 401 | Not logged in / wrong credentials / expired token |
| 403 | Role not allowed |
| 404 | Resource not found |
| 409 | Conflict (ID already exists, slot already booked, duplicate booking) |

---

## 1. Auth (public)

### POST `/auth/signup`
```json
// request
{
  "role": "STUDENT",                 // STUDENT | LECTURER
  "userId": "242203",
  "faculty": "TECHNOLOGY",           // APPLIED_SCIENCES | BUSINESS_STUDIES | TECHNOLOGY
  "password": "Abc@1234",
  "confirmPassword": "Abc@1234"
}
// 201 response
{ "message": "Successfully signed up. Hi 242203!", "userId": "242203" }
```
Errors: 400 (weak password / mismatch / bad faculty), 409 (ID exists).

### POST `/auth/login`
```json
// request
{ "userId": "242203", "password": "Abc@1234" }
// 200 response
{
  "token": "eyJhbGciOi...",
  "user": { "userId": "242203", "role": "STUDENT", "faculty": "TECHNOLOGY" }
}
```
Errors: 401 (wrong ID/password).

### POST `/auth/forgot-password`
```json
// request
{ "userId": "242203", "newPassword": "New@1234", "confirmPassword": "New@1234" }
// 200 response
{ "message": "Password successfully changed" }
```
Errors: 400 (weak / mismatch), 404 (unknown ID).

> Logout is client-side (delete token from `sessionStorage` after the OK/Cancel popup). No endpoint needed with stateless JWT.

---

## 2. Sports and Slots (STUDENT, LECTURER, ADMIN)

### GET `/sports?type=INDOOR` (or `OUTDOOR`)
```json
[
  { "id": 1, "name": "Badminton", "facility": "Indoor Court", "type": "INDOOR" },
  { "id": 2, "name": "Volleyball", "facility": "Indoor Court", "type": "INDOOR" }
]
```

### GET `/sports/{sportId}/slots?date=2026-11-10`
```json
{
  "sportId": 1,
  "date": "2026-11-10",
  "slots": [
    { "start": "07:00", "end": "08:00", "status": "AVAILABLE" },
    { "start": "08:00", "end": "09:00", "status": "BOOKED" },
    { "start": "09:00", "end": "10:00", "status": "PENDING" }
  ]
}
```
Always 12 slots (07:00–19:00). Frontend rules:
- `AVAILABLE` → selectable
- `BOOKED` → disabled, show "Not available"
- `PENDING` → disabled for STUDENT, selectable for LECTURER

---

## 3. Bookings (STUDENT, LECTURER)

### POST `/bookings`
```json
// request
{ "sportId": 1, "date": "2026-11-10", "start": "10:00", "end": "11:00" }
// 201 response
{
  "id": 15, "sportId": 1, "sportName": "Badminton", "facility": "Indoor Court",
  "date": "2026-11-10", "start": "10:00", "end": "11:00", "status": "PENDING"
}
```
Errors: 400 (outside 07:00–19:00 / past date), 409 (slot BOOKED, or duplicate).
Lecturer requesting a slot with a PENDING student request → created normally; the student's request becomes `REJECTED` and the student gets a notification.

### GET `/bookings/my`
Returns the logged-in user's bookings (newest first), same object shape as above.

### DELETE `/bookings/{id}`
Cancels own booking (status → `CANCELLED`). `204 No Content`.
Errors: 403 (not your booking), 404, 409 (already cancelled/rejected).

---

## 4. Notifications (own only)

### GET `/notifications`
```json
[
  { "id": 3, "message": "Your Badminton booking on 2026-11-10 10:00 was approved", "read": false, "createdAt": "2026-11-03T09:30:00" }
]
```

### PUT `/notifications/{id}/read`
Marks as read. `200 { "message": "Marked as read" }`. A user cannot access another user's notification (404).

---

## 5. Profile (own only)

### GET `/users/me`
```json
{ "userId": "242203", "role": "STUDENT", "faculty": "TECHNOLOGY", "createdAt": "2026-10-20T08:00:00" }
```
No password hash is ever returned.

---

## 6. Admin (ADMIN only)

### GET `/admin/bookings?status=PENDING`
`status` optional: `PENDING | APPROVED | REJECTED | CANCELLED`.
```json
[
  { "id": 15, "userId": "242203", "role": "STUDENT", "sportName": "Badminton",
    "date": "2026-11-10", "start": "10:00", "end": "11:00", "status": "PENDING" }
]
```

### PUT `/admin/bookings/{id}/approve`
`200 { "message": "Booking approved" }` – sends a notification to the user.
Error 409 if the slot already has an approved booking.

### PUT `/admin/bookings/{id}/reject`
`200 { "message": "Booking rejected" }` – sends a notification to the user.

### GET `/admin/users`
```json
[
  { "userId": "242203", "role": "STUDENT", "faculty": "TECHNOLOGY" },
  { "userId": "L001", "role": "LECTURER", "faculty": "APPLIED_SCIENCES" }
]
```

---

## 7. Endpoint Summary

| Method | Endpoint | Access | Used by (frontend) |
|---|---|---|---|
| POST | `/auth/signup` | Public | signup.html |
| POST | `/auth/login` | Public | login.html |
| POST | `/auth/forgot-password` | Public | forgot-password.html |
| GET | `/sports?type=` | User | dashboard.js |
| GET | `/sports/{id}/slots?date=` | User | dashboard.js |
| POST | `/bookings` | Student/Lecturer | dashboard.js |
| GET | `/bookings/my` | Student/Lecturer | dashboard.js |
| DELETE | `/bookings/{id}` | Student/Lecturer | dashboard.js |
| GET | `/notifications` | User | notifications.js |
| PUT | `/notifications/{id}/read` | User | notifications.js |
| GET | `/users/me` | User | profile.js |
| GET | `/admin/bookings?status=` | Admin | admin.js |
| PUT | `/admin/bookings/{id}/approve` | Admin | admin.js |
| PUT | `/admin/bookings/{id}/reject` | Admin | admin.js |
| GET | `/admin/users` | Admin | admin.js |

---

## 8. Example Frontend Calls

```js
// login
const { token, user } = await api("/auth/login", "POST", { userId, password });
sessionStorage.setItem("token", token);
sessionStorage.setItem("user", JSON.stringify(user));

// load slots
const data = await api(`/sports/${sportId}/slots?date=${date}`);

// book
await api("/bookings", "POST", { sportId, date, start: "10:00", end: "11:00" });

// admin approve
await api(`/admin/bookings/${id}/approve`, "PUT");
```

## 9. Frontend ⇄ Backend Checklist

- [ ] Backend running on port 8080, MySQL database `sport_ms` created
- [ ] CORS origin matches Live Server URL
- [ ] Field names in JSON exactly as in this file
- [ ] Token sent in `Authorization` header
- [ ] Test every endpoint in Postman before wiring the page
