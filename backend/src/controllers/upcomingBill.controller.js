// =============================================================================
// File: upcomingBill.controller.js
// Purpose: HTTP controller handlers for upcoming Bill.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const {
    upcomingBillsSchema,
} = require("../validators/upcomingBill.validator");


// ============================================================
// Get Upcoming Bills
// ============================================================

// -----------------------------------------------------------------------------
// Section: getUpcomingBills
// Purpose: Handles the get Upcoming Bills part of this backend module.
// -----------------------------------------------------------------------------
async function getUpcomingBills(req, res) {
    try {
        const userId = req.userId;

        // Default limit = 5
        const limit = Number(req.query.limit || 5);

        // Validate query
        const validation = upcomingBillsSchema.safeParse({
            limit,
        });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Invalid limit",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        // Get user's upcoming bills
        const bills = await db.orm.public.Bill
            .where({
                userId,
                status: "upcoming",
            })
            .all();

        const now = new Date();

        // Keep only future/current bills,
        // sort nearest due date first
        const upcomingBills = bills
            .filter((bill) => {
                const dueDate = new Date(bill.dueDate);

                return dueDate >= now;
            })
            .sort((a, b) => {
                return (
                    new Date(a.dueDate) -
                    new Date(b.dueDate)
                );
            })
            .slice(0, limit)
            .map((bill) => ({
                id: bill.id,
                name: bill.name,
                amount: bill.amount,
                dueDate: bill.dueDate,
                status: bill.status,
                createdAt: bill.createdAt,
                updatedAt: bill.updatedAt,
            }));

        return res.status(200).json({
            success: true,
            message: "Upcoming bills retrieved successfully",
            data: {
                bills: upcomingBills,
                count: upcomingBills.length,
            },
        });
    } catch (error) {
        console.error("Get upcoming bills error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve upcoming bills",
        });
    }
}


// ============================================================
// Export Upcoming Bills Controller
// ============================================================
module.exports = {
    getUpcomingBills,
};
