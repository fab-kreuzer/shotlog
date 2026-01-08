// Notification System for Shotlog Application
// This script provides reusable notification functionality across the application

// Function to create and show a notification
function showNotification(message, type) {
    // Create notification elements
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;

    const content = document.createElement('div');
    content.className = 'notification-content';
    content.textContent = message;

    const closeBtn = document.createElement('button');
    closeBtn.className = 'notification-close';
    closeBtn.innerHTML = '&times;';
    closeBtn.setAttribute('aria-label', 'Schließen');

    // Add elements to notification
    notification.appendChild(content);
    notification.appendChild(closeBtn);

    // Add notification to container
    const container = document.getElementById('notification-container');
    container.appendChild(notification);

    // Position the notification based on existing notifications
    positionNotification(notification);

    // Set up auto-dismiss timer (5 seconds)
    const dismissTimer = setTimeout(() => {
        dismissNotification(notification);
    }, 5000);

    // Set up close button event
    closeBtn.addEventListener('click', () => {
        clearTimeout(dismissTimer);
        dismissNotification(notification);
    });

    return notification;
}

// Function to dismiss a notification
function dismissNotification(notification) {
    notification.classList.add('hide');

    // Remove from DOM after animation completes
    notification.addEventListener('animationend', () => {
        notification.remove();
        // Reposition remaining notifications
        repositionNotifications();
    });
}

// Function to position notifications in a stack
function positionNotification(notification) {
    const container = document.getElementById('notification-container');
    const notifications = container.querySelectorAll('.notification:not(.hide)');
    const notificationHeight = notification.offsetHeight || 80; // Fallback height if not yet in DOM
    const margin = 10; // Margin between notifications

    // Calculate position based on number of active notifications
    const index = notifications.length - 1; // Current notification is the last one
    const bottomPosition = 20 + (index * (notificationHeight + margin));

    // Set the position
    notification.style.bottom = `${bottomPosition}px`;
}

// Function to reposition notifications when one is removed
function repositionNotifications() {
    const container = document.getElementById('notification-container');
    const notifications = container.querySelectorAll('.notification:not(.hide)');
    const notificationHeight = notifications.length > 0 ? notifications[0].offsetHeight : 80;
    const margin = 10;

    // Reposition each notification
    notifications.forEach((notification, index) => {
        const bottomPosition = 20 + (index * (notificationHeight + margin));
        notification.style.bottom = `${bottomPosition}px`;
    });
}

// Initialize notification container if it doesn't exist
document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('notification-container')) {
        const container = document.createElement('div');
        container.id = 'notification-container';
        document.body.appendChild(container);
    }
});
