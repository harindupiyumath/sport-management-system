import { API_BASE_URL } from "./config.js";

export async function api(path, method = "GET", body = null) {
  const token = sessionStorage.getItem("token");
  const options = {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token && { Authorization: "Bearer " + token }),
    },
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  let res;
  try {
    res = await fetch(API_BASE_URL + path, options);
  } catch {
    // Network-level failure (server down, sleeping, unreachable, or blocked by CORS).
    // The browser's own message here is just "Failed to fetch".
    throw new Error(
      "Cannot reach the server. If the site was idle, the server may be waking up - please try again in a minute."
    );
  }

  if (res.status === 204) {
    return null;
  }

  // Expired / invalid token on a protected endpoint -> session is over, go back to login.
  // (/auth/* is excluded because a wrong password on login also returns 401.)
  if (res.status === 401 && token && !path.startsWith("/auth/")) {
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("user");
    window.location.href = "login.html";
    throw new Error("Your session has expired. Please log in again.");
  }

  const data = await res.json().catch(() => ({}));

  // Logged in with a default/admin-assigned password: only changing it is allowed
  if (res.status === 403 && data.passwordChangeRequired) {
    sessionStorage.setItem("mustChangePassword", "true");
    if (!window.location.pathname.endsWith("change-password.html")) {
      window.location.href = "change-password.html";
    }
    throw new Error("You must change your password first.");
  }

  if (!res.ok) {
    throw new Error(data.message || `Request failed with status ${res.status}`);
  }
  return data;
}

// Escape text before inserting it into innerHTML templates (prevents HTML/script injection)
export function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

