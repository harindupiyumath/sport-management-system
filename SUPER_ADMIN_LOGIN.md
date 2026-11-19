# Default Super Admin Credentials

This file contains the default Super Admin credentials initialized automatically upon application startup.

---

## Credentials

- **Username / User ID:** `admin`
- **Password:** `Admin@123`
- **Role:** `ADMIN`
- **Faculty:** `TECHNOLOGY`
- **Booking Priority:** `999` (Super Administrative priority)

---

## Authentication Endpoint

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Request Body:**
  ```json
  {
    "userId": "admin",
    "password": "Admin@123"
  }
  ```

---

## Administrative Capabilities

1. **Review Pending Requests:**
   - View booking requests from Students and Lecturers.
   - Accept/Approve booking requests (`PUT /api/admin/bookings/{id}/approve`).
   - Reject/Cancel booking requests (`PUT /api/admin/bookings/{id}/reject`).
2. **User Management:**
   - View all registered users across faculties and roles (`GET /api/admin/users`).
3. **Automated Notification Dispatch:**
   - On approval or rejection, automated notification records are generated for the booking applicant.

