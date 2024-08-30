/**
 * Executes an AJAX call using a FormData object.
 * @param {String} method - The HTTP method to use (e.g., "POST", "GET").
 * @param {String} url - The URL to which the request is sent.
 * @param {FormData} formData - A FormData object containing the data to be sent.
 * @param {Function} callback - The callback function to be executed on a successful request.
 */
function makeCall(method, url, formData, callback) {
	var request = new XMLHttpRequest();

	request.onreadystatechange = function() {
		if (request.readyState === XMLHttpRequest.DONE) {
			if (request.status >= 200 && request.status < 300) {
				// Call was successful
				callback(request);
			} else if (request.status === 403) {
				// Unauthorized
				alert("User not logged");
				window.location.href = "login.html";
			} else {
				// Error handling
				let errorMessage = request.responseText.trim();
				alert(`Error ${request.status}: ${request.statusText}\n${errorMessage}`);
			}
		}
	};

	request.onerror = function() {
		alert('Network Error: Unable to complete the request.');
	};

	request.open(method, url, true);  // true for asynchronous request
	request.send(formData);
}

// Makecall for json
function makeJsonCall(method, url, data, callback) {
    var request = new XMLHttpRequest();

    request.onreadystatechange = function() {
        if (request.readyState === XMLHttpRequest.DONE) {
            if (request.status >= 200 && request.status < 300) {
                callback(request);
            } else if (request.status === 401) {
                alert("User not logged");
                window.location.href = "login.html";
            } else {
                let errorMessage = request.responseText.trim();
                alert(`Error ${request.status}: ${request.statusText}\n${errorMessage}`);
            }
        }
    };

    request.onerror = function() {
        alert('Network Error: Unable to complete the request.');
    };

    request.open(method, url, true);
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(data);  // data should be a JSON string
}



/**
 * Retrieves all the information of the user from the session.
 * Conversion to json needed because sessionStorage can only contain String
 */
function getUser() {
	return JSON.parse(window.sessionStorage.getItem("user"));
}