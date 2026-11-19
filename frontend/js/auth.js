import { api } from "./api.js";
import { isStrongPassword, passwordsMatch } from "./validators.js";

// Helper for UI alerts
function showAlert(message, isError = false) {
  const alertBox = document.getElementById("alertBox");
  if (!alertBox) return;
  alertBox.className = `alert ${isError ? "alert-danger" : "alert-success"}`;
  alertBox.textContent = message;
  alertBox.style.display = "block";
}

function clearAlert() {
  const alertBox = document.getElementById("alertBox");
  if (alertBox) alertBox.style.display = "none";
}

// 1. SIGNUP PAGE
const signupForm = document.getElementById("signupForm");
if (signupForm) {
  const passwordInput = document.getElementById("password");
  const confirmInput = document.getElementById("confirmPassword");
  const passwordFeedback = document.getElementById("passwordFeedback");
  const confirmFeedback = document.getElementById("confirmFeedback");

  passwordInput?.addEventListener("input", () => {
    if (passwordInput.value && !isStrongPassword(passwordInput.value)) {
      passwordFeedback.textContent = "Password must have uppercase, lowercase, number, symbol & min 8 chars.";
    } else {
      passwordFeedback.textContent = "";
    }
  });

  confirmInput?.addEventListener("input", () => {
    if (confirmInput.value && !passwordsMatch(passwordInput.value, confirmInput.value)) {
      confirmFeedback.textContent = "Passwords do not match.";
    } else {
      confirmFeedback.textContent = "";
    }
  });

  signupForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();

    const role = document.getElementById("role").value;
    const userId = document.getElementById("userId").value.trim();
    const faculty = document.getElementById("faculty").value;
    const password = passwordInput.value;
    const confirmPassword = confirmInput.value;

    if (!userId || !password || !confirmPassword) {
      showAlert("Please fill in all required fields.", true);
      return;
    }

    if (!isStrongPassword(password)) {
      showAlert("Password does not meet the complexity requirements.", true);
      return;
    }

    if (!passwordsMatch(password, confirmPassword)) {
      showAlert("Passwords do not match.", true);
      return;
    }

    const submitBtn = document.getElementById("submitBtn");
    submitBtn.disabled = true;

    try {
      const res = await api("/auth/signup", "POST", {
        role,
        userId,
        faculty,
        password,
        confirmPassword
      });

      showAlert(`Successfully signed up. Hi ${userId}! Redirecting to login...`, false);
      setTimeout(() => {
        window.location.href = "login.html";
      }, 2000);
    } catch (err) {
      showAlert(err.message, true);
      submitBtn.disabled = false;
    }
  });
}

// 2. LOGIN PAGE
const loginForm = document.getElementById("loginForm");
if (loginForm) {
  const userIdInput = document.getElementById("userId");
  const rememberCheckbox = document.getElementById("rememberMe");

  // Pre-fill remembered username
  const savedUser = localStorage.getItem("remembered_username");
  if (savedUser && userIdInput) {
    userIdInput.value = savedUser;
    if (rememberCheckbox) rememberCheckbox.checked = true;
  }

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();

    const userId = userIdInput.value.trim();
    const password = document.getElementById("password").value;

    if (!userId || !password) {
      showAlert("Please enter both username and password.", true);
      return;
    }

    const loginBtn = document.getElementById("loginBtn");
    loginBtn.disabled = true;

    try {
      const data = await api("/auth/login", "POST", { userId, password });
      
      // Save token and user details to sessionStorage
      sessionStorage.setItem("token", data.token);
      sessionStorage.setItem("user", JSON.stringify(data.user));

      // Handle remember me
      if (rememberCheckbox && rememberCheckbox.checked) {
        localStorage.setItem("remembered_username", userId);
      } else {
        localStorage.removeItem("remembered_username");
      }

      // Default/admin-assigned password: user must choose their own first
      if (data.mustChangePassword) {
        sessionStorage.setItem("mustChangePassword", "true");
        window.location.href = "change-password.html";
        return;
      }
      sessionStorage.removeItem("mustChangePassword");

      // Role based redirect
      if (data.user.role === "ADMIN") {
        window.location.href = "admin-dashboard.html";
      } else {
        window.location.href = "dashboard.html";
      }
    } catch (err) {
      showAlert(err.message || "Invalid credentials", true);
      loginBtn.disabled = false;
    }
  });
}

// 3. FORGOT PASSWORD PAGE
const forgotForm = document.getElementById("forgotForm");
if (forgotForm) {
  const newPasswordInput = document.getElementById("newPassword");
  const confirmPasswordInput = document.getElementById("confirmPassword");
  const passwordFeedback = document.getElementById("passwordFeedback");
  const confirmFeedback = document.getElementById("confirmFeedback");

  newPasswordInput?.addEventListener("input", () => {
    if (newPasswordInput.value && !isStrongPassword(newPasswordInput.value)) {
      passwordFeedback.textContent = "Password must have uppercase, lowercase, number, symbol & min 8 chars.";
    } else {
      passwordFeedback.textContent = "";
    }
  });

  confirmPasswordInput?.addEventListener("input", () => {
    if (confirmPasswordInput.value && !passwordsMatch(newPasswordInput.value, confirmPasswordInput.value)) {
      confirmFeedback.textContent = "Passwords do not match.";
    } else {
      confirmFeedback.textContent = "";
    }
  });

  forgotForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearAlert();

    const userId = document.getElementById("userId").value.trim();
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    if (!userId || !newPassword || !confirmPassword) {
      showAlert("Please fill in all fields.", true);
      return;
    }

    if (!isStrongPassword(newPassword)) {
      showAlert("Password does not meet complexity requirements.", true);
      return;
    }

    if (!passwordsMatch(newPassword, confirmPassword)) {
      showAlert("Passwords do not match.", true);
      return;
    }

    const submitBtn = document.getElementById("submitBtn");
    submitBtn.disabled = true;

    try {
      await api("/auth/forgot-password", "POST", {
        userId,
        newPassword,
        confirmPassword
      });

      showAlert("Password successfully changed. Redirecting to login...", false);
      setTimeout(() => {
        window.location.href = "login.html";
      }, 2000);
    } catch (err) {
      showAlert(err.message, true);
      submitBtn.disabled = false;
    }
  });
}

// 4. Logout setup
export function setupLogout(modalId = "logoutModal", confirmBtnId = "confirmLogoutBtn", cancelBtnId = "cancelLogoutBtn") {
  const modal = document.getElementById(modalId);
  const confirmBtn = document.getElementById(confirmBtnId);
  const cancelBtn = document.getElementById(cancelBtnId);
  const logoutTrigger = document.getElementById("logoutTrigger");

  if (logoutTrigger && modal) {
    logoutTrigger.addEventListener("click", (e) => {
      e.preventDefault();
      modal.classList.add("show");
    });
  }

  if (cancelBtn && modal) {
    cancelBtn.addEventListener("click", () => {
      modal.classList.remove("show");
    });
  }

  if (confirmBtn) {
    confirmBtn.addEventListener("click", () => {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("user");
      window.location.href = "login.html";
    });
  }
}

