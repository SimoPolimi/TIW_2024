// Switch from Login to Registration
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(function() {
    document.addEventListener("DOMContentLoaded", function() {
        const loginDiv = document.getElementById("loginDiv");
        const registrationDiv = document.getElementById("registrationDiv");
        const showRegistrationLink = document.getElementById("showRegistrationLink");
        const showLoginLink = document.getElementById("showLoginLink");

        // Gestione dei click per mostrare i moduli
        showRegistrationLink.addEventListener("click", function(event) {
            event.preventDefault();
            showRegistration();
        });

        showLoginLink.addEventListener("click", function(event) {
            event.preventDefault();
            showLogin();
        });

        window.showRegistration = function() {
            loginDiv.style.display = "none";
            registrationDiv.style.display = "block";
        }

        window.showLogin = function() {
            registrationDiv.style.display = "none";
            loginDiv.style.display = "block";
        }
    });
})();

// Login
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(function() {
    document.addEventListener("DOMContentLoaded", function() {
        const loginForm = document.getElementById("loginForm");

        loginForm.addEventListener("submit", function(event) {
            event.preventDefault();

            // Custom validate
            if (!validateLoginForm()) {
                return;
            }

            const formData = new FormData(loginForm);

            makeCall(
                "POST", 
                "CheckLogin", 
                formData, 
                function(req) {
                    try {
                        // Set user in session (json because sessionStorage can only contain String)
                        const user = JSON.parse(req.responseText);
                        sessionStorage.setItem("user", JSON.stringify(user));
                        location.href = "home.html";
                    } catch (e) {
                        console.error("Failed to parse response:", e);
                        alert("An error occurred while processing the response.");
                    }
                }
            );
        });
    });
})();

// Registration
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(function() {
    document.addEventListener("DOMContentLoaded", function() {
        const registrationForm = document.getElementById("registrationForm");

        registrationForm.addEventListener("submit", function(event) {
            event.preventDefault();

			// Custom validate
            if (!validateRegistrationForm()) {
                return;
            }

            const formData = new FormData(registrationForm);

            makeCall(
                "POST", 
                "Register", 
                formData, 
                function(req) {
                    try {
                        const response = JSON.parse(req.responseText);
                        if (response.status == "success") {
                            showLogin(); // Shows login after registration
                            alert("Registration successful");
                        } else {
                            alert(response.message || "An error occurred.");
                        }
                    } catch (e) {
                        console.error("Failed to parse response:", e);
                        alert("An error occurred while processing the response.");
                    }
                }
            );
        });
    });
})();

// Utility Functions
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

function isValidEmail(email) {
    const emailRegex = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
    return email && emailRegex.test(email);
}

function validateLoginForm() {
    const email = document.querySelector('#loginForm input[name="email"]').value.trim();
    const password = document.querySelector('#loginForm input[name="password"]').value.trim();
    const loginError = document.getElementById("loginError");

    if (!email || !password) {
        loginError.textContent = "All fields are required.";
        return false;
    }

    if (!isValidEmail(email)) {
        loginError.textContent = "Email not valid.";
        return false;
    }

    loginError.textContent = "";  // Clear any previous error
    return true;
}

function validateRegistrationForm() {
    const username = document.querySelector('#registrationForm input[name="username"]').value.trim();
    const email = document.querySelector('#registrationForm input[name="email"]').value.trim();
    const password = document.querySelector('#registrationForm input[name="password"]').value.trim();
    const confirmPassword = document.querySelector('#registrationForm input[name="confirmPassword"]').value.trim();
    const registrationError = document.getElementById("registrationError");

    if (!username || !email || !password || !confirmPassword) {
        registrationError.textContent = "All fields are required.";
        return false;
    }

    if (!isValidEmail(email)) {
        registrationError.textContent = "Email not valid.";
        return false;
    }

    if (password !== confirmPassword) {
        registrationError.textContent = "Passwords do not match.";
        return false;
    }

    registrationError.textContent = "";  // Clear any previous error
    return true;
}
