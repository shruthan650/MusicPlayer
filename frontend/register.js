const API_BASE_URL = "http://localhost:8080";

let selectedRole = "ARTIST";

const roleButtons = document.querySelectorAll(".role-btn");

roleButtons.forEach(button => {

    button.addEventListener("click", function () {

        roleButtons.forEach(btn => {
            btn.classList.remove("active");
        });

        this.classList.add("active");

        selectedRole = this.dataset.role;

    });

});


const registerForm = document.getElementById("registerForm");
const message = document.getElementById("message");
const registerButton = document.getElementById("registerButton");


registerForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email =
        document.getElementById("email").value.trim();

    const password =
        document.getElementById("password").value;

    const confirmPassword =
        document.getElementById("confirmPassword").value;

    const terms =
        document.getElementById("terms").checked;


    message.textContent = "";


    if (password !== confirmPassword) {

        showMessage(
            "Passwords do not match.",
            "error"
        );

        return;
    }


    if (password.length < 6) {

        showMessage(
            "Password must contain at least 6 characters.",
            "error"
        );

        return;
    }


    if (!terms) {

        showMessage(
            "Please agree to the Terms & Conditions.",
            "error"
        );

        return;
    }


    registerButton.disabled = true;
    registerButton.textContent = "Creating Account...";


    try {

        // This matches User.java exactly
        const userData = {

            userEmail: email,

            password: password,

            role: selectedRole

        };


        const response = await fetch(
            `${API_BASE_URL}/api/register`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(userData)
            }
        );


        const responseText =
            await response.text();


        if (!response.ok) {

            let errorMessage =
                "Registration failed.";

            try {

                const errorData =
                    JSON.parse(responseText);

                if (errorData.message) {
                    errorMessage =
                        errorData.message;
                }

            } catch {

                if (responseText) {
                    errorMessage =
                        responseText;
                }

            }

            throw new Error(errorMessage);
        }


        showMessage(
            "Account created successfully!",
            "success"
        );


        setTimeout(() => {

            window.location.href = "index.html";

        }, 1000);


    } catch (error) {

        console.error(
            "Registration error:",
            error
        );

        showMessage(
            error.message ||
            "Unable to connect to the server.",
            "error"
        );

    } finally {

        registerButton.disabled = false;

        registerButton.textContent =
            "Create Account";
    }

});


function showMessage(text, type) {

    message.textContent = text;

    if (type === "success") {

        message.style.color = "#4ade80";

    } else {

        message.style.color = "#f87171";
    }

}