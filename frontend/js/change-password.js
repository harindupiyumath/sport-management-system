import { api } from "./api.js";
import { isStrongPassword, passwordsMatch } from "./validators.js";

// Must be logged in
const userStr = sessionStorage.getItem("user");
const token = sessionStorage.getItem("token");
if (!userStr || !token) {
  window.location.href = "login.html";
  throw new Error("Not logged in - redirecting to login");
}

const currentUser = JSON.parse(userStr);
const forced = sessionStorage.getItem("mustChangePassword") === "true";
const homePage = currentUser.role === "ADMIN" ? "admin-dashboard.html" : "dashboard.html";

const alertBox = document.getElementById("alertBox");
const form = document.getElementById("changePasswordForm");
const currentInput = document.getElementById("currentPassword");
const newInput = document.getElementById("newPassword");
const confirmInput = document.getElementById("confirmPassword");
const passwordFeedback = document.getElementById("passwordFeedback");
const confirmFeedback = document.getElementById("confirmFeedback");
const backLink = document.getElementById("backLink");

function showAlert(message, isError) {
  alertBox.className = `alert ${isError ? "alert-danger" : "alert-success"}`;
  alertBox.textContent = message;
  alertBox.style.display = "block";
}

if (forced) {
  document.getElementById("changeIntro").textContent =
    "For security, you must set a new password before continuing.";
  showAlert("Your current password was set by the system or an administrator. Please choose your own password.", true);
  // A forced change can only be left by logging out
  backLink.textContent = "Log out";
  backLink.addEventListener("click", (e) => {
    e.preventDefault();
    sessionStorage.clear();
    window.location.href = "login.html";
  });
} else {
  backLink.textContent = "Back to dashboard";
  backLink.href = homePage;
}

newInput.addEventListener("input", () => {
  passwordFeedback.textContent = newInput.value && !isStrongPassword(newInput.value)
    ? "Password must have uppercase, lowercase, number, symbol & min 8 chars."
    : "";
});

confirmInput.addEventListener("input", () => {
  confirmFeedback.textContent = confirmInput.value && !passwordsMatch(newInput.value, confirmInput.value)
    ? "Passwords do not match."
    : "";
});

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  alertBox.style.display = "none";

  const currentPassword = currentInput.value;
  const newPassword = newInput.value;
  const confirmPassword = confirmInput.value;

  if (!currentPassword || !newPassword || !confirmPassword) {
    return showAlert("Please fill in all fields.", true);
  }
  if (!isStrongPassword(newPassword)) {
    return showAlert("New password does not meet the complexity requirements.", true);
  }
  if (!passwordsMatch(newPassword, confirmPassword)) {
    return showAlert("Passwords do not match.", true);
  }
  if (newPassword === currentPassword) {
    return showAlert("New password must be different from the current password.", true);
  }

  const submitBtn = document.getElementById("submitBtn");
  submitBtn.disabled = true;

  try {
    const data = await api("/users/me/password", "PUT", { currentPassword, newPassword, confirmPassword });
    // The backend returns a fresh token without the "must change password" restriction
    sessionStorage.setItem("token", data.token);
    sessionStorage.setItem("user", JSON.stringify(data.user));
    sessionStorage.removeItem("mustChangePassword");

    showAlert("Password changed successfully. Redirecting...", false);
    setTimeout(() => { window.location.href = homePage; }, 1500);
  } catch (err) {
    showAlert(err.message, true);
    submitBtn.disabled = false;
  }
});
