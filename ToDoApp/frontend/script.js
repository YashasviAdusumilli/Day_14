const apiBase = "http://localhost:8888";
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");
const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");
const toast = document.getElementById("toast");

// Switch tabs
loginTab.addEventListener("click", () => {
  loginForm.classList.remove("hidden");
  registerForm.classList.add("hidden");
  loginTab.classList.add("tab-active");
  registerTab.classList.remove("tab-active");
  toast.classList.add("hidden");
});

registerTab.addEventListener("click", () => {
  loginForm.classList.add("hidden");
  registerForm.classList.remove("hidden");
  registerTab.classList.add("tab-active");
  loginTab.classList.remove("tab-active");
  toast.classList.add("hidden");
});

// Register
registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const name = document.getElementById("registerName").value.trim();
  const email = document.getElementById("registerEmail").value.trim();

  if (!name || !email) return showToast("Please enter name and email");

  try {
    const res = await fetch(`${apiBase}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email })
    });

    if (res.ok) {
      showToast("✅ Registered! Check email for password", false);
      setTimeout(() => {
        loginTab.click(); // Switch to login tab
      }, 2000);
    } else {
      const error = await res.text();
      showToast("❌ " + error);
    }
  } catch (err) {
    showToast("❌ Registration failed");
  }
});

// Login
loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const email = document.getElementById("loginEmail").value.trim();
  const password = document.getElementById("loginPassword").value;

  if (!email || !password) return showToast("Please enter credentials");

  try {
    const res = await fetch(`${apiBase}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });

    const data = await res.json();

    if (res.ok && data.token) {
      localStorage.setItem("token", data.token);
      window.location.href = "dashboard.html";
    } else {
      showToast("❌ " + (data.message || "Login failed"));
    }
  } catch (err) {
    showToast("❌ Server error");
  }
});

// Toast message
function showToast(message, isError = true) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  toast.classList.toggle("text-red-600", isError);
  toast.classList.toggle("text-green-600", !isError);
}
