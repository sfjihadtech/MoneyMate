// =============================================================================
// File: recentTransaction.controller.js
// Purpose: HTTP controller handlers for recent Transaction.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const { recentTransactionsSchema } = require("../validators/recentTransaction.validator");

// ============================================================
// Get Recent Transactions
// ============================================================

// -----------------------------------------------------------------------------
// Section: getRecentTransactions
// Purpose: Handles the get Recent Transactions part of this backend module.
// -----------------------------------------------------------------------------
async function getRecentTransactions(req, res) {
    try {
        const userId = req.userId;

        // Get limit from query parameter
        const limit = Number(req.query.limit || 10);

        // Validate limit
        const validation = recentTransactionsSchema.safeParse({
            limit,
        });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Invalid limit",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        // Get user's transactions
        const transactions = await db.orm.public.Transaction
            .where({ userId })
            .all();

        // Sort by newest transaction first
        transactions.sort((a, b) => {
            return new Date(b.occurredAt) - new Date(a.occurredAt);
        });

        // Return only requested number of transactions
        const recentTransactions = transactions
            .slice(0, limit)
            .map((transaction) => ({
                id: transaction.id,
                accountId: transaction.accountId,
                categoryId: transaction.categoryId,
                type: transaction.type,
                amount: transaction.amount,
                merchant: transaction.merchant,
                paymentMethod: transaction.paymentMethod,
                notes: transaction.notes,
                receiptUrl: transaction.receiptUrl,
                occurredAt: transaction.occurredAt,
            }));

        return res.status(200).json({
            success: true,
            message: "Recent transactions retrieved successfully",
            data: {
                transactions: recentTransactions,
                count: recentTransactions.length,
            },
        });
    } catch (error) {
        console.error("Get recent transactions error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve recent transactions",
        });
    }
}

module.exports = {
    getRecentTransactions,
};
