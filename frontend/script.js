const API_BASE_URL = "http://localhost:8080";

// ------------------------------
// ROLE SELECTION
// ------------------------------

let selectedRole = "ARTIST";

const roleButtons = document.querySelectorAll(".role-btn");

roleButtons.forEach(button => {
button.addEventListener("click", () => {

    roleButtons.forEach(btn => {
        btn.classList.remove("active");
    });

    button.classList.add("active");

    // Login page uses:
    // Artist -> ARTIST
    // Listener -> USER
    selectedRole = button.dataset.role;
});

});

// ------------------------------
// LOGIN
// ------------------------------

const loginForm = document.getElementById("loginForm");
const message = document.getElementById("message");

loginForm.addEventListener("submit", async (event) => {

event.preventDefault();

const email = document.getElementById("email").value.trim();
const password = document.getElementById("password").value;

message.textContent = "";
message.className = "";

if (!email || !password) {
    showMessage("Please enter your email and password.", "error");
    return;
}

const loginButton = loginForm.querySelector(".login-btn");

loginButton.disabled = true;
loginButton.textContent = "Logging in...";

try {

    const response = await fetch(`${API_BASE_URL}/api/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            userEmail: email,
            password: password
        })
    });

    // Backend returns JWT as plain text
    const responseText = await response.text();

    if (!response.ok) {

        let errorMessage = "Invalid email or password.";

        try {
            const errorData = JSON.parse(responseText);

            if (errorData.message) {
                errorMessage = errorData.message;
            }
        } catch {
            if (responseText) {
                errorMessage = responseText;
            }
        }

        throw new Error(errorMessage);
    }

    const token = responseText.trim();

    if (!token) {
        throw new Error("Login failed. No token was received.");
    }

    // ------------------------------
    // SAVE JWT
    // ------------------------------

    localStorage.setItem("melodiaToken", token);

    // ------------------------------
    // GET CURRENT USER
    // ------------------------------

    const userResponse = await fetch(`${API_BASE_URL}/api/user/me`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });

    if (!userResponse.ok) {
        localStorage.removeItem("melodiaToken");
        throw new Error("Could not retrieve your account information.");
    }

    const user = await userResponse.json();
        if (user.role !== selectedRole) {
        localStorage.removeItem("melodiaToken");
        throw new Error(
            "This account is not a " +
            (selectedRole === "ARTIST" ? "Artist" : "Listener") +
            " account."
        );
        }
    // Save user information too
    localStorage.setItem("melodiaUser", JSON.stringify(user));

    // ------------------------------
    // CHECK BACKEND ROLE
    // ------------------------------

    const backendRole = String(user.role || "").toUpperCase();

    if (backendRole === "ARTIST") {

        showMessage("Login successful! Opening Artist dashboard...", "success");

        setTimeout(() => {
            window.location.href = "artist.html";
        }, 500);

    } else if (backendRole === "USER") {

        showMessage("Login successful! Opening Listener dashboard...", "success");

        setTimeout(() => {
            window.location.href = "listener.html";
        }, 500);

    } else {

        localStorage.removeItem("melodiaToken");
        localStorage.removeItem("melodiaUser");

        throw new Error("Unknown account role received from the server.");
    }

} catch (error) {

    console.error("Login error:", error);

    showMessage(
        error.message || "Something went wrong. Please try again.",
        "error"
    );

} finally {

    loginButton.disabled = false;
    loginButton.textContent = "Login";
}


});

// ------------------------------
// MESSAGE FUNCTION
// ------------------------------

function showMessage(text, type) {

message.textContent = text;

if (type === "success") {
    message.style.color = "#4ade80";
} else {
    message.style.color = "#f87171";
}

}
