# Default Super Admin Credentials

This file contains the default Super Admin credentials initialized automatically upon application startup.

> **Security:** `Admin@123` is only a *first-login* password. Because it is public, the backend
> refuses every request from this account until a new password is chosen (the login page
> redirects to **Change Password** automatically). The forgot-password page can never reset an
> admin account. Additional admins can be created from the admin dashboard (**Create Account**).

---

## Credentials

- **Username / User ID:** `admin`
- **Password:** `Admin@123` (must be changed at first login)
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
   - Create Student, Lecturer or Admin accounts with a temporary password (`POST /api/admin/users`);
     the new user must change it at first login.
3. **Own Password:**
   - Change password at any time (`PUT /api/users/me/password`, requires the current password).
4. **Automated Notification Dispatch:**
   - On approval or rejection, automated notification records are generated for the booking applicant.

