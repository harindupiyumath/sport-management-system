# FRONTEND.md – Sport Management System (HTML + CSS + JavaScript)

CMIS 2113 – OOP Mini Project | Wayamba University of Sri Lanka
Frontend talks to the Spring Boot backend only through the REST API described in `API.md`.

---

## 1. Tech

| Item | Choice |
|---|---|
| Markup / Style | HTML5, CSS3 (no framework needed; Flexbox/Grid) |
| Logic | Vanilla JavaScript (`fetch` API) |
| Run | VS Code **Live Server** (`http://127.0.0.1:5500`) |
| Backend URL | `http://localhost:8080/api` (set once in `js/config.js`) |

---

## 2. Folder Structure

```
frontend/
├── index.html                # redirects to login.html
├── signup.html
├── login.html
├── forgot-password.html
├── dashboard.html            # Student + Lecturer
├── admin-dashboard.html      # Admin only (different interface)
├── css/
│   ├── style.css             # global: colours, buttons, forms, cards
│   ├── auth.css              # signup / login / forgot pages
│   └── dashboard.css         # dashboard layout, slot grid, modal
├── js/
│   ├── config.js             # API_BASE_URL
│   ├── api.js                # fetch wrapper (adds JWT, handles errors)
│   ├── auth.js               # signup, login, forgot, logout
│   ├── validators.js         # password rules, confirm-password
│   ├── dashboard.js          # sports, slots, booking, cancel
│   ├── notifications.js
│   ├── profile.js
│   └── admin.js              # pending requests, users list
└── images/
```

---

## 3. Pages and Behaviour

### 3.1 Sign Up (`signup.html`)
Fields: **Role** (Student / Lecturer), **Student ID / Lecturer ID** (this becomes the username), **Faculty** (Applied Sciences, Business Studies, Technology), **Enter a Password**, **Confirm Password**.

- Hint under password: *"Minimum 8 characters including capital and simple letters, number and symbol."*
- Live validation: all rules met, and confirm == password.
- On success (`POST /auth/signup`): show **"Successfully signed up. Hi `<username>`!"** then auto-redirect to `login.html` after ~2 s.

### 3.2 Login (`login.html`)
- Fields: Username (the ID), Password.
- **Remember username** checkbox → store the username in `localStorage` (never the password). Pre-fill on next visit.
- **Log in** button → `POST /auth/login`. On success save the JWT and user info in `sessionStorage`, then:
  - role `ADMIN` → `admin-dashboard.html`
  - role `STUDENT` / `LECTURER` → `dashboard.html`
- Wrong or empty credentials → show an error message, stay on the page (cannot reach the dashboard).
- **Forgotten Your Password?** link → `forgot-password.html`.

### 3.3 Forgot Password (`forgot-password.html`)
- Fields: Username (ID), **New password**, **Confirm password**.
- Small hint under New password with the same password rule.
- On success (`POST /auth/forgot-password`): **"Password successfully changed"**, then auto-redirect to `login.html`. The user logs in with the new password.

### 3.4 Dashboard (`dashboard.html`) – Student and Lecturer
Top bar: logo, **Indoor / Outdoor selector at the top-right**, Notifications, Profile, Logout.

1. Click **Indoor** → list indoor sports; click **Outdoor** → list outdoor sports (`GET /sports?type=`).
2. Click a sport → panel on the same page with a **date picker** and **time-slot grid**.
3. Slot grid (`GET /sports/{id}/slots?date=`) shows **07:00 AM – 07:00 PM** only (1-hour slots).
   - `AVAILABLE` → clickable.
   - `BOOKED` → greyed out, `disabled`, tooltip/label **"Not available"**.
   - `PENDING` (held by a student) → disabled for students, still selectable for lecturers (lecturer priority).
4. **Book** button → `POST /bookings` → status shows as *Pending* until admin decides.
5. **My Bookings** list with a **Cancel** button (`DELETE /bookings/{id}`) for Pending/Approved bookings.
6. **Notifications** panel – only the logged-in user's own (`GET /notifications`).
7. **Profile** panel – only the logged-in user's own data (`GET /users/me`).
8. **Logout** button → confirmation popup:
   - **OK** → clear session, go to `login.html`.
   - **Cancel** → close popup; dashboard stays exactly as it was (do not reload the page or reset selections).

### 3.5 Admin Dashboard (`admin-dashboard.html`)
Different layout from the user dashboard:
- **Pending Requests** table (`GET /admin/bookings?status=PENDING`) with **Accept** and **Reject/Cancel** buttons.
- **All Users** table – Students, Lecturers (and Admins) information (`GET /admin/users`).
- Optional: all bookings with status filter, facility list.
- Logout with the same OK / Cancel popup.

---

## 4. Validation Rules (client side – backend validates again)

```js
// validators.js
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
export const isStrongPassword = (p) => PASSWORD_REGEX.test(p);
export const passwordsMatch = (a, b) => a === b;
```

Slots outside 07:00–19:00 are never rendered.

---

## 5. API Helper (`js/api.js`)

```js
import { API_BASE_URL } from "./config.js";

export async function api(path, method = "GET", body = null) {
  const token = sessionStorage.getItem("token");
  const res = await fetch(API_BASE_URL + path, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: "Bearer " + token }),
    },
    body: body ? JSON.stringify(body) : null,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || "Something went wrong");
  return data;
}
```

Use `<script type="module" src="js/auth.js">` in the pages.

---

## 6. Route Guards

At the top of `dashboard.js` / `admin.js`:

```js
const user = JSON.parse(sessionStorage.getItem("user") || "null");
if (!user || !sessionStorage.getItem("token")) location.href = "login.html";
if (isAdminPage && user.role !== "ADMIN") location.href = "dashboard.html";
```

This is only for convenience – real protection is done by the backend (JWT + roles).

---

## 7. CSS Notes

- Use CSS variables in `style.css` (`--primary`, `--danger`, `--bg`, `--card`).
- Slot colours: Available = green border, Booked = grey + `cursor:not-allowed`, Pending = orange, Selected = filled primary.
- Responsive: slot grid `display:grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));`
- Logout popup = a fixed-position overlay `div` toggled with a `.show` class.

---

## 8. Build Order (suggested)

1. `style.css` + Sign Up page
2. Login page + Forgot Password page
3. User dashboard (sports → slots → booking → cancel)
4. Notifications + Profile + Logout popup
5. Admin dashboard
6. Connect each page to the API (`API.md`) and test with the backend running
