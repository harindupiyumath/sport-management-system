import { api } from "./api.js";
import { setupLogout } from "./auth.js";
import { loadNotifications } from "./notifications.js";
import { loadUserProfile } from "./profile.js";

// Route Guard
const userStr = sessionStorage.getItem("user");
const token = sessionStorage.getItem("token");
if (!userStr || !token) {
  window.location.href = "login.html";
  // Assigning location.href does not stop the script; abort so we don't crash on JSON.parse(null)
  throw new Error("Not logged in - redirecting to login");
}

const currentUser = JSON.parse(userStr);
if (currentUser.role === "ADMIN") {
  window.location.href = "admin-dashboard.html";
  throw new Error("Admin user - redirecting to admin dashboard");
}

// State
let currentFacilityType = "INDOOR"; // INDOOR or OUTDOOR
let currentSport = null;
let selectedSlot = null;

// DOM elements
const welcomeUserElem = document.getElementById("welcomeUser");
if (welcomeUserElem) welcomeUserElem.textContent = `${currentUser.userId} (${currentUser.role})`;

const indoorBtn = document.getElementById("indoorBtn");
const outdoorBtn = document.getElementById("outdoorBtn");
const sportsListContainer = document.getElementById("sportsList");
const datePicker = document.getElementById("datePicker");
const slotsContainer = document.getElementById("slotsContainer");
const selectedSportTitle = document.getElementById("selectedSportTitle");
const bookBtn = document.getElementById("bookBtn");
const myBookingsTableBody = document.getElementById("myBookingsTableBody");
const bookingAlert = document.getElementById("bookingAlert");

// Date picker default to today (local date - toISOString() would give the UTC date,
// which is "yesterday" in Sri Lanka between 00:00 and 05:30)
const now = new Date();
const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
datePicker.value = today;
datePicker.min = today;

// Setup Logout Modal
setupLogout("logoutModal", "confirmLogoutBtn", "cancelLogoutBtn");

// Tab Switching (Booking / My Bookings / Notifications / Profile)
const tabs = document.querySelectorAll(".tab-btn");
const views = {
  bookingTab: document.getElementById("bookingView"),
  myBookingsTab: document.getElementById("myBookingsView"),
  notificationsTab: document.getElementById("notificationsView"),
  profileTab: document.getElementById("profileView")
};

tabs.forEach(tab => {
  tab.addEventListener("click", () => {
    tabs.forEach(t => t.classList.remove("active"));
    tab.classList.add("active");

    Object.values(views).forEach(v => { if (v) v.style.display = "none"; });

    const viewId = tab.id.replace("Tab", "View");
    const targetView = document.getElementById(viewId);
    if (targetView) targetView.style.display = "block";

    if (tab.id === "myBookingsTab") {
      loadMyBookings();
    } else if (tab.id === "notificationsTab") {
      loadNotifications();
    } else if (tab.id === "profileTab") {
      loadUserProfile();
    }
  });
});

// Facility Type Toggle (Indoor / Outdoor)
indoorBtn.addEventListener("click", () => {
  if (currentFacilityType === "INDOOR") return;
  currentFacilityType = "INDOOR";
  indoorBtn.classList.add("active");
  outdoorBtn.classList.remove("active");
  loadSports();
});

outdoorBtn.addEventListener("click", () => {
  if (currentFacilityType === "OUTDOOR") return;
  currentFacilityType = "OUTDOOR";
  outdoorBtn.classList.add("active");
  indoorBtn.classList.remove("active");
  loadSports();
});

// Load Sports
async function loadSports() {
  try {
    sportsListContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 0.85rem;">Loading sports...</p>`;
    const sports = await api(`/sports?type=${currentFacilityType}`);
    
    if (!sports || sports.length === 0) {
      sportsListContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 0.85rem;">No sports available.</p>`;
      return;
    }

    sportsListContainer.innerHTML = sports.map(s => `
      <div class="sport-card ${currentSport && currentSport.id === s.id ? 'active' : ''}" data-id="${s.id}" data-name="${s.name}" data-facility="${s.facility}">
        <span>${s.name}</span>
        <span style="font-size: 0.75rem; color: var(--text-muted);">›</span>
      </div>
    `).join("");

    sportsListContainer.querySelectorAll(".sport-card").forEach(card => {
      card.addEventListener("click", () => {
        sportsListContainer.querySelectorAll(".sport-card").forEach(c => c.classList.remove("active"));
        card.classList.add("active");
        currentSport = {
          id: card.getAttribute("data-id"),
          name: card.getAttribute("data-name"),
          facility: card.getAttribute("data-facility")
        };
        selectedSportTitle.textContent = `${currentSport.name} (${currentSport.facility})`;
        selectedSlot = null;
        bookBtn.disabled = true;
        loadSlots();
      });
    });

    // Auto-select first sport if none selected
    if (!currentSport && sports.length > 0) {
      const firstCard = sportsListContainer.querySelector(".sport-card");
      firstCard?.click();
    } else if (currentSport) {
      const existingCard = sportsListContainer.querySelector(`.sport-card[data-id="${currentSport.id}"]`);
      if (existingCard) {
        existingCard.classList.add("active");
        loadSlots();
      } else {
        const firstCard = sportsListContainer.querySelector(".sport-card");
        firstCard?.click();
      }
    }
  } catch (err) {
    sportsListContainer.innerHTML = `<p style="color: var(--danger); font-size: 0.85rem;">Failed: ${err.message}</p>`;
  }
}

// Load Slots
async function loadSlots() {
  if (!currentSport) return;
  const date = datePicker.value;
  if (!date) return;

  try {
    slotsContainer.innerHTML = `<p style="grid-column: 1 / -1; color: var(--text-muted);">Loading time slots...</p>`;
    const data = await api(`/sports/${currentSport.id}/slots?date=${date}`);
    renderSlots(data.slots || []);
  } catch (err) {
    slotsContainer.innerHTML = `<p style="grid-column: 1 / -1; color: var(--danger);">Failed to load slots: ${err.message}</p>`;
  }
}

function renderSlots(slots) {
  slotsContainer.innerHTML = "";
  selectedSlot = null;
  bookBtn.disabled = true;

  slots.forEach(slot => {
    const slotEl = document.createElement("div");
    slotEl.className = "slot-item";

    const isLecturer = currentUser.role === "LECTURER";
    let isSelectable = false;

    // Slots earlier today that have already started can't be booked
    const nowTime = new Date();
    const nowHHMM = `${String(nowTime.getHours()).padStart(2, "0")}:${String(nowTime.getMinutes()).padStart(2, "0")}`;
    const isPast = datePicker.value === today && slot.start <= nowHHMM;

    if (isPast) {
      slotEl.classList.add("booked");
      slotEl.innerHTML = `<span>${slot.start} - ${slot.end}</span><small>Time passed</small>`;
    } else if (slot.status === "AVAILABLE") {
      slotEl.classList.add("available");
      isSelectable = true;
      slotEl.innerHTML = `<span>${slot.start} - ${slot.end}</span><small style="color:var(--success);">Available</small>`;
    } else if (slot.status === "PENDING") {
      slotEl.classList.add("pending");
      if (isLecturer) {
        // Lecturer priority: can select pending slots
        isSelectable = true;
        slotEl.innerHTML = `<span>${slot.start} - ${slot.end}</span><small style="color:var(--warning);">Pending (Priority override)</small>`;
      } else {
        isSelectable = false;
        slotEl.classList.add("booked");
        slotEl.innerHTML = `<span>${slot.start} - ${slot.end}</span><small style="color:var(--text-muted);">Pending</small>`;
      }
    } else { // BOOKED
      slotEl.classList.add("booked");
      isSelectable = false;
      slotEl.innerHTML = `<span>${slot.start} - ${slot.end}</span><small>Not available</small>`;
      slotEl.title = "Not available";
    }

    if (isSelectable) {
      slotEl.addEventListener("click", () => {
        slotsContainer.querySelectorAll(".slot-item").forEach(s => s.classList.remove("selected"));
        slotEl.classList.add("selected");
        selectedSlot = slot;
        bookBtn.disabled = false;
      });
    }

    slotsContainer.appendChild(slotEl);
  });
}

datePicker.addEventListener("change", () => {
  if (currentSport) {
    loadSlots();
  }
});

// Book Button Handler
bookBtn.addEventListener("click", async () => {
  if (!currentSport || !selectedSlot) return;

  bookingAlert.style.display = "none";
  bookBtn.disabled = true;

  try {
    await api("/bookings", "POST", {
      sportId: currentSport.id,
      date: datePicker.value,
      start: selectedSlot.start,
      end: selectedSlot.end
    });

    bookingAlert.className = "alert alert-success";
    bookingAlert.textContent = `Booking request submitted for ${currentSport.name} on ${datePicker.value} from ${selectedSlot.start} to ${selectedSlot.end}! Status: Pending approval.`;
    bookingAlert.style.display = "block";

    loadSlots();
  } catch (err) {
    bookingAlert.className = "alert alert-danger";
    bookingAlert.textContent = err.message || "Failed to submit booking.";
    bookingAlert.style.display = "block";
    bookBtn.disabled = false;
  }
});

// Load My Bookings
async function loadMyBookings() {
  if (!myBookingsTableBody) return;
  try {
    myBookingsTableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">Loading bookings...</td></tr>`;
    const bookings = await api("/bookings/my");

    if (!bookings || bookings.length === 0) {
      myBookingsTableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--text-muted);">No bookings found.</td></tr>`;
      return;
    }

    myBookingsTableBody.innerHTML = bookings.map(b => {
      let badgeClass = "badge-pending";
      if (b.status === "APPROVED") badgeClass = "badge-approved";
      if (b.status === "REJECTED") badgeClass = "badge-rejected";
      if (b.status === "CANCELLED") badgeClass = "badge-cancelled";

      const canCancel = (b.status === "PENDING" || b.status === "APPROVED");

      return `
        <tr>
          <td><strong>${b.sportName}</strong></td>
          <td>${b.facility}</td>
          <td>${b.date}</td>
          <td>${b.start} - ${b.end}</td>
          <td><span class="badge ${badgeClass}">${b.status}</span></td>
          <td>
            ${canCancel ? `<button class="btn btn-sm btn-danger cancel-booking-btn" data-id="${b.id}">Cancel</button>` : "-"}
          </td>
        </tr>
      `;
    }).join("");

    myBookingsTableBody.querySelectorAll(".cancel-booking-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.getAttribute("data-id");
        if (confirm("Are you sure you want to cancel this booking?")) {
          try {
            await api(`/bookings/${id}`, "DELETE");
            loadMyBookings();
            loadSlots();
          } catch (err) {
            alert("Error cancelling booking: " + err.message);
          }
        }
      });
    });

  } catch (err) {
    myBookingsTableBody.innerHTML = `<tr><td colspan="6" style="color: var(--danger);">Failed to load bookings: ${err.message}</td></tr>`;
  }
}

// Initial load
loadSports();

