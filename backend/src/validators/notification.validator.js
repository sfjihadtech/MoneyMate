// =============================================================================
// File: notification.validator.js
// Purpose: Validation schema and request validation rules for notification.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Notification Types
// ============================================================
const notificationTypeSchema = z.enum([
    "bill_due",
    "budget_warning",
    "budget_exceeded",
    "savings_progress",
    "general",
]);


// ============================================================
// Create Notification Validation
// ============================================================
const createNotificationSchema = z.object({
    type: notificationTypeSchema,

    title: z
        .string({
            message: "Notification title is required",
        })
        .trim()
        .min(
            1,
            "Notification title is required"
        )
        .max(
            150,
            "Notification title is too long"
        ),

    message: z
        .string({
            message: "Notification message is required",
        })
        .trim()
        .min(
            1,
            "Notification message is required"
        )
        .max(
            500,
            "Notification message is too long"
        ),

    relatedEntityType: z
        .string()
        .trim()
        .max(
            50,
            "Related entity type is too long"
        )
        .optional()
        .nullable(),

    relatedEntityId: z
        .number({
            message:
                "Related entity ID must be a number",
        })
        .int(
            "Related entity ID must be an integer"
        )
        .positive(
            "Related entity ID must be positive"
        )
        .optional()
        .nullable(),
});


// ============================================================
// Notification List Query Validation
// ============================================================
const notificationListQuerySchema = z.object({
    limit: z
        .number({
            message: "Limit must be a number",
        })
        .int(
            "Limit must be an integer"
        )
        .min(
            1,
            "Limit must be at least 1"
        )
        .max(
            100,
            "Limit cannot exceed 100"
        ),

    unreadOnly: z
        .boolean({
            message:
                "Unread only must be true or false",
        }),
});


// ============================================================
// Export Notification Validation Schemas
// ============================================================
module.exports = {
    createNotificationSchema,
    notificationListQuerySchema,
};
