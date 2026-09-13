// =============================================================================
// File: billReminder.service.js
// Purpose: Business/service layer for bill Reminder.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const cron = require("node-cron");

const { db } = require("../prisma/db.ts");

const {
    createBillDueNotification,
} = require("./notification.service");


// ============================================================
// Configuration
// ============================================================

// Bill due soon threshold
const BILL_DUE_SOON_DAYS =
    Number(
        process.env.BILL_DUE_SOON_DAYS || 7
    );

// Default:
// Every day at 8:00 AM
const BILL_REMINDER_CRON =
    process.env.BILL_REMINDER_CRON ||
    "0 8 * * *";

// MVP timezone.
// Can be changed later from .env.
const CRON_TIMEZONE =
    process.env.CRON_TIMEZONE ||
    "Asia/Kuala_Lumpur";


// ============================================================
// Check If Bill Is Due Soon
// ============================================================

// -----------------------------------------------------------------------------
// Section: isBillDueSoon
// Purpose: Handles the is Bill Due Soon part of this backend module.
// -----------------------------------------------------------------------------
function isBillDueSoon(bill) {
    if (
        !bill ||
        bill.status !== "upcoming"
    ) {
        return false;
    }

    const dueDate =
        new Date(bill.dueDate);

    if (
        Number.isNaN(
            dueDate.getTime()
        )
    ) {
        return false;
    }

    const now =
        new Date();

    const dueSoonLimit =
        new Date(
            now.getTime() +
            BILL_DUE_SOON_DAYS *
                24 *
                60 *
                60 *
                1000
        );

    return (
        dueDate >= now &&
        dueDate <= dueSoonLimit
    );
}


// ============================================================
// Delete Specific Bill Due Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: removeBillDueNotification
// Purpose: Handles the remove Bill Due Notification part of this backend module.
// -----------------------------------------------------------------------------
async function removeBillDueNotification(
    notificationId
) {
    await db.orm.public.Notification
        .where({
            id: notificationId,
        })
        .delete();
}


// ============================================================
// Run Bill Reminder Scan
// ============================================================

// -----------------------------------------------------------------------------
// Section: runBillReminderScan
// Purpose: Handles the run Bill Reminder Scan part of this backend module.
// -----------------------------------------------------------------------------
async function runBillReminderScan() {
    console.log(
        "[Bill Reminder] Scan started..."
    );

    try {
        // ====================================================
        // Get All Bills
        // ====================================================
        const bills =
            await db.orm.public.Bill.all();


        // ====================================================
        // Get Existing Bill Notifications
        // ====================================================
        const billNotifications =
            await db.orm.public.Notification
                .where({
                    type: "bill_due",
                })
                .all();


        // ====================================================
        // Active notification keys
        //
        // Format:
        // userId|billId
        // ====================================================
        const activeBillKeys =
            new Set();


        let createdOrUpdatedCount = 0;
        let removedCount = 0;


        // ====================================================
        // Process Bills
        // ====================================================
        for (const bill of bills) {
            if (!isBillDueSoon(bill)) {
                continue;
            }

            const key =
                `${bill.userId}|${bill.id}`;

            activeBillKeys.add(key);


            // ================================================
            // Get User Currency
            // ================================================
            const user =
                await db.orm.public.User.first({
                    id: bill.userId,
                });

            if (!user) {
                continue;
            }


            // ================================================
            // Create / Update Notification
            //
            // notification.service already prevents
            // duplicate unread notifications.
            // ================================================
            await createBillDueNotification({
                userId: bill.userId,
                bill,
                currency:
                    user.currency || "BDT",
            });

            createdOrUpdatedCount += 1;
        }


        // ====================================================
        // Cleanup Old / Invalid Bill Notifications
        //
        // Examples:
        // - bill already paid
        // - bill deleted
        // - bill due date moved beyond 7 days
        // - bill already expired
        // ====================================================
        for (
            const notification
            of billNotifications
        ) {
            if (
                notification.relatedEntityType !==
                "bill"
            ) {
                continue;
            }

            if (
                !notification.relatedEntityId
            ) {
                continue;
            }

            const key =
                `${notification.userId}|${notification.relatedEntityId}`;

            if (
                activeBillKeys.has(key)
            ) {
                continue;
            }

            await removeBillDueNotification(
                notification.id
            );

            removedCount += 1;
        }


        // ====================================================
        // Scan Result
        // ====================================================
        const result = {
            success: true,
            checkedBills:
                bills.length,

            activeDueSoonBills:
                activeBillKeys.size,

            createdOrUpdated:
                createdOrUpdatedCount,

            removedNotifications:
                removedCount,

            scannedAt:
                new Date().toISOString(),
        };

        console.log(
            "[Bill Reminder] Scan completed:",
            result
        );

        return result;
    } catch (error) {
        console.error(
            "[Bill Reminder] Scan failed:",
            error
        );

        throw error;
    }
}


// ============================================================
// Start Scheduled Bill Reminder Scanner
// ============================================================

// -----------------------------------------------------------------------------
// Section: startBillReminderScheduler
// Purpose: Handles the start Bill Reminder Scheduler part of this backend module.
// -----------------------------------------------------------------------------
function startBillReminderScheduler() {
    // Validate cron expression
    if (
        !cron.validate(
            BILL_REMINDER_CRON
        )
    ) {
        console.error(
            "[Bill Reminder] Invalid cron expression:",
            BILL_REMINDER_CRON
        );

        return null;
    }

    const task =
        cron.schedule(
            BILL_REMINDER_CRON,

            async () => {
                try {
                    await runBillReminderScan();
                } catch (error) {
                    console.error(
                        "[Bill Reminder] Scheduled scan error:",
                        error
                    );
                }
            },

            {
                timezone:
                    CRON_TIMEZONE,
            }
        );


    console.log(
        `[Bill Reminder] Scheduler started (${BILL_REMINDER_CRON}, ${CRON_TIMEZONE})`
    );

    return task;
}


// ============================================================
// Export Bill Reminder Service
// ============================================================
module.exports = {
    isBillDueSoon,
    runBillReminderScan,
    startBillReminderScheduler,
};
