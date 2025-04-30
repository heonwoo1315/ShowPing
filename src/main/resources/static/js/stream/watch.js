document.addEventListener('DOMContentLoaded', function () {
    const event = new Event('dataLoaded');
    window.dispatchEvent(event);
});