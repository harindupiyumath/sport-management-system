import { api, escapeHtml } from "./api.js";
import { setupLogout } from "./auth.js";
import { isStrongPassword, passwordsMatch } from "./validators.js";

// Route Guard for Admin
const userStr = sessionStorage.getItem("user");
const token = sessionStorage.getItem("token");
if (!userStr || !token) {
  window.location.href = "login.html";
  // Assigning location.href does not stop the script; abort so we don't crash on JSON.parse(null)
  throw new Error("Not logged in - redirecting to login");
}

const currentUser = JSON.parse(userStr);
if (currentUser.role !== "ADMIN") {
  window.location.href = "dashboard.html";
  throw new Error("Not an admin - redirecting to dashboard");
}

// Elements
const adminWelcome = document.getElementById("adminWelcome");
if (adminWelcome) adminWelcome.textContent = `Super Admin: ${currentUser.userId}`;

const requestsTableBody = document.getElementById("requestsTableBody");
const usersTableBody = document.getElementById("usersTableBody");
const adminAlert = document.getElementById("adminAlert");
const statusFilter = document.getElementById("statusFilter");

// Setup Logout Modal
setupLogout("logoutModal", "confirmLogoutBtn", "cancelLogoutBtn");

// Tab switching
const adminTabs = document.querySelectorAll(".admin-tab-btn");
adminTabs.forEach(tab => {
  tab.addEventListener("click", () => {
    adminTabs.forEach(t => t.classList.remove("active"));
    tab.classList.add("active");

    document.getElementById("bookingsSection").style.display = tab.id === "tabBookings" ? "block" : "none";
    document.getElementById("usersSection").style.display = tab.id === "tabUsers" ? "block" : "none";
    document.getElementById("createSection").style.display = tab.id === "tabCreate" ? "block" : "none";

    if (tab.id === "tabUsers") {
      loadUsers();
    } else if (tab.id === "tabBookings") {
      loadBookings();
    }
  });
});

statusFilter?.addEventListener("change", () => {
  loadBookings();
});

// Load Bookings
async function loadBookings() {
  try {
    requestsTableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Loading requests...</td></tr>`;
    const filter = statusFilter.value;
    const url = filter ? `/admin/bookings?status=${filter}` : `/admin/bookings`;
    const bookings = await api(url);

    if (!bookings || bookings.length === 0) {
      requestsTableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">No booking requests found.</td></tr>`;
      return;
    }

    requestsTableBody.innerHTML = bookings.map(b => {
      let badgeClass = "badge-pending";
      if (b.status === "APPROVED") badgeClass = "badge-approved";
      if (b.status === "REJECTED") badgeClass = "badge-rejected";
      if (b.status === "CANCELLED") badgeClass = "badge-cancelled";

      const isPending = b.status === "PENDING";

      return `
        <tr>
          <td><strong>${escapeHtml(b.userId)}</strong></td>
          <td><span class="badge ${b.role === 'LECTURER' ? 'badge-approved' : 'badge-cancelled'}">${b.role}</span></td>
          <td>${b.sportName}</td>
          <td>${b.date}</td>
          <td>${b.start} - ${b.end}</td>
          <td><span class="badge ${badgeClass}">${b.status}</span></td>
          <td>
            ${isPending ? `
              <div style="display: flex; gap: 0.5rem;">
                <button class="btn btn-sm btn-success approve-btn" data-id="${b.id}">Accept</button>
                <button class="btn btn-sm btn-danger reject-btn" data-id="${b.id}">Reject</button>
              </div>
            ` : "-"}
          </td>
        </tr>
      `;
    }).join("");

    requestsTableBody.querySelectorAll(".approve-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.getAttribute("data-id");
        try {
          await api(`/admin/bookings/${id}/approve`, "PUT");
          showAlert("Booking accepted and approved successfully!", false);
          loadBookings();
        } catch (err) {
          showAlert(err.message, true);
        }
      });
    });

    requestsTableBody.querySelectorAll(".reject-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.getAttribute("data-id");
        try {
          await api(`/admin/bookings/${id}/reject`, "PUT");
          showAlert("Booking rejected.", false);
          loadBookings();
        } catch (err) {
          showAlert(err.message, true);
        }
      });
    });

  } catch (err) {
    requestsTableBody.innerHTML = `<tr><td colspan="7" style="color: var(--danger);">Failed to load bookings: ${err.message}</td></tr>`;
  }
}

// Load Users
async function loadUsers() {
  try {
    usersTableBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted);">Loading users...</td></tr>`;
    const users = await api("/admin/users");

    if (!users || users.length === 0) {
      usersTableBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--text-muted);">No users found.</td></tr>`;
      return;
    }

    usersTableBody.innerHTML = users.map(u => `
      <tr>
        <td><strong>${escapeHtml(u.userId)}</strong></td>
        <td><span class="badge ${u.role === 'ADMIN' ? 'badge-rejected' : u.role === 'LECTURER' ? 'badge-approved' : 'badge-pending'}">${u.role}</span></td>
        <td>${u.faculty || "TECHNOLOGY"}</td>
      </tr>
    `).join("");
  } catch (err) {
    usersTableBody.innerHTML = `<tr><td colspan="3" style="color: var(--danger);">Failed to load users: ${err.message}</td></tr>`;
  }
}

// Create Account (admin creates Student / Lecturer / Admin accounts)
const createForm = document.getElementById("createAccountForm");
createForm?.addEventListener("submit", async (e) => {
  e.preventDefault();

  const role = document.getElementById("newRole").value;
  const userId = document.getElementById("newUserId").value.trim();
  const faculty = document.getElementById("newFaculty").value;
  const password = document.getElementById("newPassword").value;
  const confirmPassword = document.getElementById("newConfirmPassword").value;

  if (!userId || !password || !confirmPassword) {
    return showAlert("Please fill in all fields.", true);
  }
  if (!isStrongPassword(password)) {
    return showAlert("Temporary password must have uppercase, lowercase, number, symbol & min 8 chars.", true);
  }
  if (!passwordsMatch(password, confirmPassword)) {
    return showAlert("Passwords do not match.", true);
  }

  const btn = document.getElementById("createAccountBtn");
  btn.disabled = true;
  try {
    const res = await api("/admin/users", "POST", { role, userId, faculty, password, confirmPassword });
    showAlert(res.message || `Account '${userId}' created.`, false);
    createForm.reset();
  } catch (err) {
    showAlert(err.message, true);
  } finally {
    btn.disabled = false;
  }
});

function showAlert(message, isError) {
  if (!adminAlert) return;
  adminAlert.className = `alert ${isError ? 'alert-danger' : 'alert-success'}`;
  adminAlert.textContent = message;
  adminAlert.style.display = "block";
  setTimeout(() => {
    adminAlert.style.display = "none";
  }, 4000);
}

// Initial load
loadBookings();

