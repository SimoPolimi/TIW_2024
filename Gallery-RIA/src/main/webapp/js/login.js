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

        function showRegistration() {
            loginDiv.style.display = "none";
            registrationDiv.style.display = "block";
        }

        function showLogin() {
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
        const ErrorMessage = document.getElementById("loginError");

        loginForm.addEventListener("submit", function(event) {
            event.preventDefault();


            if (loginForm.checkValidity()) {
                const formData = new FormData(loginForm);

                makeCall(
                    "POST", 
                    "CheckLogin", 
                    formData, 
                    ErrorMessage, 
                    function(req) {
                        try {
                            const user = JSON.parse(req.responseText);
                            sessionStorage.setItem("user", JSON.stringify(user));
                            location.href = "home.html";
                        } catch (e) {
                            console.error("Failed to parse response:", e);
                            ErrorMessage.textContent = "An error occurred while processing the response.";
                        }
                    }, 
                    false, // false because FormData is not JSON
                    true  // true because login request
                );
            } else {
                loginForm.reportValidity();
            }
        });
    });
})();

// Registration
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(function() {
    document.addEventListener("DOMContentLoaded", function() {
        const registrationForm = document.getElementById("registrationForm");
        const registrationError = document.getElementById("registrationError");

        registrationForm.addEventListener("submit", function(event) {
            event.preventDefault();

            if (registrationForm.checkValidity()) {
                const formData = new FormData(registrationForm);

                makeCall(
                    "POST", 
                    "CheckRegistration", 
                    formData, 
                    registrationError, 
                    function(req) {
                        try {
                            const response = JSON.parse(req.responseText);
                            if (response.success) {
                                showLogin(); // Shows login after registration
                            } else {
                                registrationError.textContent = response.message || "An error occurred.";
                            }
                        } catch (e) {
                            console.error("Failed to parse response:", e);
                            registrationError.textContent = "An error occurred while processing the response.";
                        }
                    }, 
                    false, 
                    false
                );
            } else {
                registrationForm.reportValidity();
            }
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
    const email = document.querySelector('input[name="email"]').value;
    const password = document.querySelector('input[name="password"]').value;
    const loginError = document.getElementById("loginError");

    if (!email || !password) {
        loginError.textContent = "All fields are required.";
        return false;
    }

    if (!isValidEmail(email)) {
        loginError.textContent = "Email not valid.";
        return false;
    }
    return true;
}

function validateRegistrationForm() {
    const username = document.querySelector('input[name="username"]').value;
    const email = document.querySelector('input[name="email"]').value;
    const password = document.querySelector('input[name="password"]').value;
    const confirmPassword = document.querySelector('input[name="confirmPassword"]').value;
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
    return true;
}
