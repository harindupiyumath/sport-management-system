import { api } from "./api.js";

export async function loadNotifications() {
  const container = document.getElementById("notificationsList");
  if (!container) return;

  try {
    const notifications = await api("/notifications");
    if (!notifications || notifications.length === 0) {
      container.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">No notifications yet.</p>`;
      return;
    }

    container.innerHTML = notifications.map(n => `
      <div class="card" style="margin-bottom: 0.75rem; padding: 1rem; border-left: 4px solid ${n.read ? "var(--border)" : "var(--secondary)"}; background: ${n.read ? "#fff" : "#f0f9ff"};">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem;">
          <div>
            <p style="font-size: 0.9rem; font-weight: ${n.read ? "normal" : "600"}; color: var(--text-main); margin-bottom: 0.25rem;">
              ${n.message}
            </p>
            <span style="font-size: 0.75rem; color: var(--text-muted);">${new Date(n.createdAt).toLocaleString()}</span>
          </div>
          ${!n.read ? `
            <button class="btn btn-sm btn-outline mark-read-btn" data-id="${n.id}">Mark read</button>
          ` : ""}
        </div>
      </div>
    `).join("");

    container.querySelectorAll(".mark-read-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.getAttribute("data-id");
        try {
          await api(`/notifications/${id}/read`, "PUT");
          loadNotifications();
        } catch (err) {
          alert("Error marking notification as read: " + err.message);
        }
      });
    });

  } catch (err) {
    container.innerHTML = `<p style="color: var(--danger); font-size: 0.9rem;">Failed to load notifications: ${err.message}</p>`;
  }
}

