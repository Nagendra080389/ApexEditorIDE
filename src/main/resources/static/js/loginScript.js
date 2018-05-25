$(document).ready(function() {
    recoverFromError();
});

function recoverFromError() {
    var errorType = consumeErrorCookies("ERROROAUTH");
    var errorDesc = consumeErrorCookies("ERROROAUTHDESC");
    if (errorType && errorDesc) {
        errorType = errorType.replace(/\_/g, ' ');
        errorDesc = errorDesc.replace(/\+/g, ' ');
        var errorSuffix = '';
        if (errorType === 'invalid grant' && errorDesc === 'ip restricted or invalid login hours') {
            errorSuffix = '\n\nYou are encountering this error because the Salesforce org you ' + 'are trying to login to has IP restrictions and ApexEditor has a dynamic IP address ' + 'that does not meet those restrictions.';
        }
        alert('Failed to login :(\n\nReason: ' + errorType + ' - ' + errorDesc + errorSuffix + '\n\nIf you feel this is an error please email admin.');
    }
    createCookie("ERROROAUTH", "", -1);
    createCookie("ERROROAUTHDESC", "", -1);
}
function createCookie(name, value, days) {
    var date = new Date();
    var expiryMs = days;
    date.setTime(date.getTime() + expiryMs);
    var expires = "; expires=" + date.toGMTString();
    document.cookie = name + "=" + value + expires + "; path=/";
}

function consumeErrorCookies(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) {
            return c.substring(nameEQ.length, c.length)
        };
    }
    return null;
}