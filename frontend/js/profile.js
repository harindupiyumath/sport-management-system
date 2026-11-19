import { api, escapeHtml } from "./api.js";

export async function loadUserProfile() {
  const container = document.getElementById("profileContainer");
  if (!container) return;

  try {
    const profile = await api("/users/me");
    container.innerHTML = `
      <div class="card">
        <h3 style="margin-bottom: 1rem; color: var(--primary);">User Profile</h3>
        <p style="margin-bottom: 0.5rem;"><strong>User ID:</strong> ${escapeHtml(profile.userId)}</p>
        <p style="margin-bottom: 0.5rem;"><strong>Role:</strong> ${profile.role}</p>
        <p style="margin-bottom: 0.5rem;"><strong>Faculty:</strong> ${profile.faculty || "N/A"}</p>
        <p style="margin-bottom: 0.5rem;"><strong>Member Since:</strong> ${profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : "N/A"}</p>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p style="color: var(--danger); font-size: 0.9rem;">Failed to load profile: ${err.message}</p>`;
  }
}

