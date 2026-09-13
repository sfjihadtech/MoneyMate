// =============================================================================
// File: notification.controller.js
// Purpose: HTTP controller handlers for notification.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    createNotificationSchema,
    notificationListQuerySchema,
} = require("../validators/notification.validator");


// ============================================================
// Create Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createNotification
// Purpose: Handles the create Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createNotification(req, res) {
    try {
        const userId = req.userId;

        const validation =
            createNotificationSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const {
            type,
            title,
            message,
            relatedEntityType,
            relatedEntityId,
        } = validation.data;

        const notification =
            await db.orm.public.Notification.create({
                userId,
                type,
                title,
                message,
                relatedEntityType:
                    relatedEntityType ?? null,
                relatedEntityId:
                    relatedEntityId ?? null,
            });

        return res.status(201).json({
            success: true,
            message:
                "Notification created successfully",
            data: {
                notification: {
                    id: notification.id,
                    type: notification.type,
                    title: notification.title,
                    message: notification.message,
                    isRead: notification.isRead,
                    relatedEntityType:
                        notification.relatedEntityType,
                    relatedEntityId:
                        notification.relatedEntityId,
                    readAt: notification.readAt,
                    createdAt:
                        notification.createdAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Create notification error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to create notification",
        });
    }
}


// ============================================================
// Get Notifications
// ============================================================

// -----------------------------------------------------------------------------
// Section: getNotifications
// Purpose: Handles the get Notifications part of this backend module.
// -----------------------------------------------------------------------------
async function getNotifications(req, res) {
    try {
        const userId = req.userId;

        const limit = Number(
            req.query.limit || 20
        );

        const unreadOnly =
            String(
                req.query.unreadOnly || "false"
            ).toLowerCase() === "true";

        const validation =
            notificationListQuerySchema.safeParse({
                limit,
                unreadOnly,
            });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid notification query",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const query = {
            userId,
        };

        if (unreadOnly) {
            query.isRead = false;
        }

        const notifications =
            await db.orm.public.Notification
                .where(query)
                .all();

        notifications.sort((a, b) => {
            return (
                new Date(b.createdAt) -
                new Date(a.createdAt)
            );
        });

        const result =
            notifications
                .slice(0, limit)
                .map((notification) => ({
                    id: notification.id,
                    type: notification.type,
                    title: notification.title,
                    message:
                        notification.message,
                    isRead:
                        notification.isRead,
                    relatedEntityType:
                        notification.relatedEntityType,
                    relatedEntityId:
                        notification.relatedEntityId,
                    readAt:
                        notification.readAt,
                    createdAt:
                        notification.createdAt,
                }));

        const unreadCount =
            notifications.filter(
                (notification) =>
                    !notification.isRead
            ).length;

        return res.status(200).json({
            success: true,
            message:
                "Notifications retrieved successfully",
            data: {
                notifications: result,
                count: result.length,
                unreadCount,
            },
        });
    } catch (error) {
        console.error(
            "Get notifications error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve notifications",
        });
    }
}


// ============================================================
// Mark Notification as Read
// ============================================================

// -----------------------------------------------------------------------------
// Section: markNotificationAsRead
// Purpose: Handles the mark Notification As Read part of this backend module.
// -----------------------------------------------------------------------------
async function markNotificationAsRead(
    req,
    res
) {
    try {
        const userId = req.userId;
        const notificationId =
            Number(req.params.id);

        if (
            !Number.isInteger(notificationId) ||
            notificationId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid notification ID",
            });
        }

        const notification =
            await db.orm.public.Notification.first({
                id: notificationId,
                userId,
            });

        if (!notification) {
            return res.status(404).json({
                success: false,
                message:
                    "Notification not found",
            });
        }

        if (!notification.isRead) {
            await db.orm.public.Notification
                .where({
                    id: notificationId,
                    userId,
                })
                .update({
                    isRead: true,
                    readAt:
                        new Date().toISOString(),
                });
        }

        const updatedNotification =
            await db.orm.public.Notification.first({
                id: notificationId,
                userId,
            });

        return res.status(200).json({
            success: true,
            message:
                "Notification marked as read",
            data: {
                notification:
                    updatedNotification,
            },
        });
    } catch (error) {
        console.error(
            "Mark notification as read error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to update notification",
        });
    }
}


// ============================================================
// Mark All Notifications as Read
// ============================================================

// -----------------------------------------------------------------------------
// Section: markAllNotificationsAsRead
// Purpose: Handles the mark All Notifications As Read part of this backend module.
// -----------------------------------------------------------------------------
async function markAllNotificationsAsRead(
    req,
    res
) {
    try {
        const userId = req.userId;

        const notifications =
            await db.orm.public.Notification
                .where({
                    userId,
                    isRead: false,
                })
                .all();

        const readAt =
            new Date().toISOString();

        for (const notification of notifications) {
            await db.orm.public.Notification
                .where({
                    id: notification.id,
                    userId,
                })
                .update({
                    isRead: true,
                    readAt,
                });
        }

        return res.status(200).json({
            success: true,
            message:
                "All notifications marked as read",
            data: {
                updatedCount:
                    notifications.length,
            },
        });
    } catch (error) {
        console.error(
            "Mark all notifications as read error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to update notifications",
        });
    }
}


// ============================================================
// Delete Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteNotification
// Purpose: Handles the delete Notification part of this backend module.
// -----------------------------------------------------------------------------
async function deleteNotification(req, res) {
    try {
        const userId = req.userId;
        const notificationId =
            Number(req.params.id);

        if (
            !Number.isInteger(notificationId) ||
            notificationId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid notification ID",
            });
        }

        const notification =
            await db.orm.public.Notification.first({
                id: notificationId,
                userId,
            });

        if (!notification) {
            return res.status(404).json({
                success: false,
                message:
                    "Notification not found",
            });
        }

        await db.orm.public.Notification
            .where({
                id: notificationId,
                userId,
            })
            .delete();

        return res.status(200).json({
            success: true,
            message:
                "Notification deleted successfully",
        });
    } catch (error) {
        console.error(
            "Delete notification error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to delete notification",
        });
    }
}


// ============================================================
// Export Notification Controller
// ============================================================
module.exports = {
    createNotification,
    getNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    deleteNotification,
};
