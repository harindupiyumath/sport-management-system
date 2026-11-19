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

  const res = await fetch(API_BASE_URL + path, options);

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

