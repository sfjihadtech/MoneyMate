// =============================================================================
// File: notification.service.js
// Purpose: Business/service layer for notification.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");


// ============================================================
// Create Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createNotification
// Purpose: Handles the create Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createNotification({
    userId,
    type,
    title,
    message,
    relatedEntityType = null,
    relatedEntityId = null,
}) {
    return db.orm.public.Notification.create({
        userId,
        type,
        title,
        message,
        relatedEntityType,
        relatedEntityId,
    });
}


// ============================================================
// Find Existing Unread Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: findExistingUnreadNotification
// Purpose: Handles the find Existing Unread Notification part of this backend module.
// -----------------------------------------------------------------------------
async function findExistingUnreadNotification({
    userId,
    type,
    relatedEntityType = null,
    relatedEntityId = null,
}) {
    const notifications =
        await db.orm.public.Notification
            .where({
                userId,
                type,
                isRead: false,
            })
            .all();

    return (
        notifications.find((notification) => {
            return (
                notification.relatedEntityType ===
                    relatedEntityType &&
                notification.relatedEntityId ===
                    relatedEntityId
            );
        }) || null
    );
}


// ============================================================
// Create or Update Existing Unread Notification
//
// If an unread notification already exists for the same
// entity, update its title/message instead of creating
// a duplicate.
// ============================================================

// -----------------------------------------------------------------------------
// Section: createNotificationIfNotExists
// Purpose: Handles the create Notification If Not Exists part of this backend module.
// -----------------------------------------------------------------------------
async function createNotificationIfNotExists({
    userId,
    type,
    title,
    message,
    relatedEntityType = null,
    relatedEntityId = null,
}) {
    const existing =
        await findExistingUnreadNotification({
            userId,
            type,
            relatedEntityType,
            relatedEntityId,
        });

    if (existing) {
        await db.orm.public.Notification
            .where({
                id: existing.id,
                userId,
            })
            .update({
                title,
                message,
                relatedEntityType,
                relatedEntityId,
            });

        const updatedNotification =
            await db.orm.public.Notification.first({
                id: existing.id,
                userId,
            });

        return {
            created: false,
            updated: true,
            notification: updatedNotification,
        };
    }

    const notification =
        await createNotification({
            userId,
            type,
            title,
            message,
            relatedEntityType,
            relatedEntityId,
        });

    return {
        created: true,
        updated: false,
        notification,
    };
}


// ============================================================
// Bill Due Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createBillDueNotification
// Purpose: Handles the create Bill Due Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createBillDueNotification({
    userId,
    bill,
    currency = "BDT",
}) {
    return createNotificationIfNotExists({
        userId,
        type: "bill_due",

        title: `${bill.name} Due Soon`,

        message:
            `Your ${bill.name} of ${currency} ` +
            `${Number(bill.amount)} is due soon.`,

        relatedEntityType: "bill",
        relatedEntityId: bill.id,
    });
}


// ============================================================
// Budget Warning Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createBudgetWarningNotification
// Purpose: Handles the create Budget Warning Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createBudgetWarningNotification({
    userId,
    budget,
    categoryName,
    percentage,
}) {
    return createNotificationIfNotExists({
        userId,
        type: "budget_warning",

        title: "Budget Warning",

        message:
            `You have used ${percentage}% of your ` +
            `${categoryName} budget.`,

        relatedEntityType: "budget",
        relatedEntityId: budget.id,
    });
}


// ============================================================
// Budget Exceeded Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createBudgetExceededNotification
// Purpose: Handles the create Budget Exceeded Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createBudgetExceededNotification({
    userId,
    budget,
    categoryName,
}) {
    return createNotificationIfNotExists({
        userId,
        type: "budget_exceeded",

        title: "Budget Exceeded",

        message:
            `You have exceeded your ` +
            `${categoryName} budget.`,

        relatedEntityType: "budget",
        relatedEntityId: budget.id,
    });
}


// ============================================================
// Savings Progress Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: createSavingsProgressNotification
// Purpose: Handles the create Savings Progress Notification part of this backend module.
// -----------------------------------------------------------------------------
async function createSavingsProgressNotification({
    userId,
    goal,
    percentage,
}) {
    return createNotificationIfNotExists({
        userId,
        type: "savings_progress",

        title: "Savings Progress",

        message:
            `You have reached ${percentage}% of your ` +
            `${goal.name} savings goal.`,

        relatedEntityType: "savings_goal",
        relatedEntityId: goal.id,
    });
}


// ============================================================
// Export Notification Service
// ============================================================
module.exports = {
    createNotification,
    findExistingUnreadNotification,
    createNotificationIfNotExists,
    createBillDueNotification,
    createBudgetWarningNotification,
    createBudgetExceededNotification,
    createSavingsProgressNotification,
};
