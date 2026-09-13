// =============================================================================
// File: bill.controller.js
// Purpose: HTTP controller handlers for bill.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    createBillSchema,
    updateBillSchema,
} = require("../validators/bill.validator");

const {
    createBillDueNotification,
} = require("../services/notification.service");


// ============================================================
// Bill Due Soon Configuration
// ============================================================
const BILL_DUE_SOON_DAYS = 7;


// ============================================================
// Check If Bill Is Due Soon
// ============================================================

// -----------------------------------------------------------------------------
// Section: isBillDueSoon
// Purpose: Handles the is Bill Due Soon part of this backend module.
// -----------------------------------------------------------------------------
function isBillDueSoon(bill) {
    if (bill.status !== "upcoming") {
        return false;
    }

    const dueDate = new Date(bill.dueDate);

    if (Number.isNaN(dueDate.getTime())) {
        return false;
    }

    const now = new Date();

    const dueSoonLimit = new Date(
        now.getTime() +
        BILL_DUE_SOON_DAYS * 24 * 60 * 60 * 1000
    );

    return (
        dueDate >= now &&
        dueDate <= dueSoonLimit
    );
}


// ============================================================
// Remove Bill Due Notifications
// ============================================================

// -----------------------------------------------------------------------------
// Section: removeBillDueNotifications
// Purpose: Handles the remove Bill Due Notifications part of this backend module.
// -----------------------------------------------------------------------------
async function removeBillDueNotifications(
    userId,
    billId
) {
    const notifications =
        await db.orm.public.Notification
            .where({
                userId,
                type: "bill_due",
            })
            .all();

    const matchingNotifications =
        notifications.filter(
            (notification) =>
                notification.relatedEntityType ===
                    "bill" &&
                notification.relatedEntityId ===
                    billId
        );

    for (const notification of matchingNotifications) {
        await db.orm.public.Notification
            .where({
                id: notification.id,
                userId,
            })
            .delete();
    }
}


// ============================================================
// Create Automatic Bill Due Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: maybeCreateBillDueNotification
// Purpose: Handles the maybe Create Bill Due Notification part of this backend module.
// -----------------------------------------------------------------------------
async function maybeCreateBillDueNotification(
    userId,
    bill
) {
    if (!isBillDueSoon(bill)) {
        return;
    }

    const user =
        await db.orm.public.User.first({
            id: userId,
        });

    await createBillDueNotification({
        userId,
        bill,
        currency:
            user?.currency || "BDT",
    });
}


// ============================================================
// Create Bill
// ============================================================

// -----------------------------------------------------------------------------
// Section: createBill
// Purpose: Handles the create Bill part of this backend module.
// -----------------------------------------------------------------------------
async function createBill(req, res) {
    try {
        const userId = req.userId;

        const validation =
            createBillSchema.safeParse(
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
            name,
            amount,
            dueDate,
            status,
        } = validation.data;

        const bill =
            await db.orm.public.Bill.create({
                userId,
                name,
                amount,
                dueDate,
                status:
                    status || "upcoming",
            });


        // ====================================================
        // Automatic Due-Soon Notification
        // ====================================================
        try {
            await maybeCreateBillDueNotification(
                userId,
                bill
            );
        } catch (notificationError) {
            console.error(
                "Automatic bill notification error:",
                notificationError
            );
        }


        return res.status(201).json({
            success: true,
            message:
                "Bill created successfully",
            data: {
                bill: {
                    id: bill.id,
                    name: bill.name,
                    amount: bill.amount,
                    dueDate: bill.dueDate,
                    status: bill.status,
                    createdAt: bill.createdAt,
                    updatedAt: bill.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Create bill error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to create bill",
        });
    }
}


// ============================================================
// Get All Bills
// ============================================================

// -----------------------------------------------------------------------------
// Section: getBills
// Purpose: Handles the get Bills part of this backend module.
// -----------------------------------------------------------------------------
async function getBills(req, res) {
    try {
        const userId = req.userId;

        const bills =
            await db.orm.public.Bill
                .where({
                    userId,
                })
                .all();

        bills.sort(
            (a, b) =>
                new Date(a.dueDate) -
                new Date(b.dueDate)
        );

        return res.status(200).json({
            success: true,
            message:
                "Bills retrieved successfully",
            data: {
                bills: bills.map(
                    (bill) => ({
                        id: bill.id,
                        name: bill.name,
                        amount: bill.amount,
                        dueDate: bill.dueDate,
                        status: bill.status,
                        createdAt:
                            bill.createdAt,
                        updatedAt:
                            bill.updatedAt,
                    })
                ),
            },
        });
    } catch (error) {
        console.error(
            "Get bills error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve bills",
        });
    }
}


// ============================================================
// Get Bill By ID
// ============================================================

// -----------------------------------------------------------------------------
// Section: getBillById
// Purpose: Handles the get Bill By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getBillById(req, res) {
    try {
        const userId = req.userId;
        const billId =
            Number(req.params.id);

        if (
            !Number.isInteger(billId) ||
            billId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message: "Invalid bill ID",
            });
        }

        const bill =
            await db.orm.public.Bill.first({
                id: billId,
                userId,
            });

        if (!bill) {
            return res.status(404).json({
                success: false,
                message: "Bill not found",
            });
        }

        return res.status(200).json({
            success: true,
            message:
                "Bill retrieved successfully",
            data: {
                bill: {
                    id: bill.id,
                    name: bill.name,
                    amount: bill.amount,
                    dueDate: bill.dueDate,
                    status: bill.status,
                    createdAt: bill.createdAt,
                    updatedAt: bill.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Get bill error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve bill",
        });
    }
}


// ============================================================
// Update Bill
// ============================================================

// -----------------------------------------------------------------------------
// Section: updateBill
// Purpose: Handles the update Bill part of this backend module.
// -----------------------------------------------------------------------------
async function updateBill(req, res) {
    try {
        const userId = req.userId;
        const billId =
            Number(req.params.id);

        if (
            !Number.isInteger(billId) ||
            billId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message: "Invalid bill ID",
            });
        }

        const validation =
            updateBillSchema.safeParse(
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

        const existingBill =
            await db.orm.public.Bill.first({
                id: billId,
                userId,
            });

        if (!existingBill) {
            return res.status(404).json({
                success: false,
                message: "Bill not found",
            });
        }

        const {
            name,
            amount,
            dueDate,
            status,
        } = validation.data;

        await db.orm.public.Bill
            .where({
                id: billId,
                userId,
            })
            .update({
                name,
                amount,
                dueDate,
                status,
            });

        const updatedBill =
            await db.orm.public.Bill.first({
                id: billId,
                userId,
            });


        // ====================================================
        // Automatic Notification Handling
        // ====================================================
        try {
            if (
                updatedBill.status !==
                "upcoming"
            ) {
                await removeBillDueNotifications(
                    userId,
                    billId
                );
            } else {
                await maybeCreateBillDueNotification(
                    userId,
                    updatedBill
                );
            }
        } catch (notificationError) {
            console.error(
                "Automatic bill notification error:",
                notificationError
            );
        }


        return res.status(200).json({
            success: true,
            message:
                "Bill updated successfully",
            data: {
                bill: {
                    id: updatedBill.id,
                    name: updatedBill.name,
                    amount:
                        updatedBill.amount,
                    dueDate:
                        updatedBill.dueDate,
                    status:
                        updatedBill.status,
                    createdAt:
                        updatedBill.createdAt,
                    updatedAt:
                        updatedBill.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Update bill error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to update bill",
        });
    }
}


// ============================================================
// Delete Bill
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteBill
// Purpose: Handles the delete Bill part of this backend module.
// -----------------------------------------------------------------------------
async function deleteBill(req, res) {
    try {
        const userId = req.userId;
        const billId =
            Number(req.params.id);

        if (
            !Number.isInteger(billId) ||
            billId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message: "Invalid bill ID",
            });
        }

        const bill =
            await db.orm.public.Bill.first({
                id: billId,
                userId,
            });

        if (!bill) {
            return res.status(404).json({
                success: false,
                message: "Bill not found",
            });
        }


        // ====================================================
        // Delete Related Notifications
        // ====================================================
        try {
            await removeBillDueNotifications(
                userId,
                billId
            );
        } catch (notificationError) {
            console.error(
                "Delete bill notification error:",
                notificationError
            );
        }


        // ====================================================
        // Delete Bill
        // ====================================================
        await db.orm.public.Bill
            .where({
                id: billId,
                userId,
            })
            .delete();


        return res.status(200).json({
            success: true,
            message:
                "Bill deleted successfully",
        });
    } catch (error) {
        console.error(
            "Delete bill error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to delete bill",
        });
    }
}


// ============================================================
// Export Bill Controller
// ============================================================
module.exports = {
    createBill,
    getBills,
    getBillById,
    updateBill,
    deleteBill,
};
