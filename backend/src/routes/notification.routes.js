// =============================================================================
// File: notification.routes.js
// Purpose: Express route definitions for notification.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    createNotification,
    getNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    deleteNotification,
} = require("../controllers/notification.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");


const router = express.Router();


// ============================================================
// Create Notification
// ============================================================
router.post(
    "/",
    authenticateToken,
    createNotification
);


// ============================================================
// Get Notifications
// ============================================================
router.get(
    "/",
    authenticateToken,
    getNotifications
);


// ============================================================
// Mark All Notifications as Read
// IMPORTANT:
// Must come before /:id/read
// ============================================================
router.put(
    "/read-all",
    authenticateToken,
    markAllNotificationsAsRead
);


// ============================================================
// Mark Single Notification as Read
// ============================================================
router.put(
    "/:id/read",
    authenticateToken,
    markNotificationAsRead
);


// ============================================================
// Delete Notification
// ============================================================
router.delete(
    "/:id",
    authenticateToken,
    deleteNotification
);


// ============================================================
// Export Notification Routes
// ============================================================
module.exports = router;
