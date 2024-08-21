/**
 * Executes an AJAX call using a FormData object.
 * @param {String} method - The HTTP method to use (e.g., "POST", "GET").
 * @param {String} url - The URL to which the request is sent.
 * @param {FormData} formData - A FormData object containing the data to be sent.
 * @param {HTMLElement} responseTag - The HTML element where error messages will be displayed.
 * @param {Function} callback - The callback function to be executed on a successful request.
 */
function makeCall(method, url, formData, responseTag, callback) {
    var request = new XMLHttpRequest();

    request.onreadystatechange = function() {
        if (request.readyState === XMLHttpRequest.DONE) {
            if (request.status >= 200 && request.status < 300) {
                // Call was successful
                callback(request);
            } else {
                // Error handling
                let errorMessage = request.responseText.trim();
                responseTag.textContent = errorMessage || `Error ${request.status}: ${request.statusText}`;
            }
        }
    };

    request.open(method, url, true);  // true for asynchronous request
    request.send(formData);
}
