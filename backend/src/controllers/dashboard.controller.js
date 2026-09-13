// =============================================================================
// File: dashboard.controller.js
// Purpose: HTTP controller handlers for dashboard.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

// ============================================================
// Get Dashboard Summary
// ============================================================

// -----------------------------------------------------------------------------
// Section: getDashboardSummary
// Purpose: Handles the get Dashboard Summary part of this backend module.
// -----------------------------------------------------------------------------
async function getDashboardSummary(req, res) {
    try {
        const userId = req.userId;

        // Get user's accounts
        const accounts = await db.orm.public.Account
            .where({ userId })
            .all();

        // Calculate total balance
        const totalBalance = accounts.reduce(
            (sum, account) => sum + Number(account.balance),
            0
        );

        // Get all transactions
        const transactions = await db.orm.public.Transaction
            .where({ userId })
            .all();

        // Calculate total income and expense
        const totalIncome = transactions
            .filter((transaction) => transaction.type === "income")
            .reduce((sum, transaction) => sum + Number(transaction.amount), 0);

        const totalExpense = transactions
            .filter((transaction) => transaction.type === "expense")
            .reduce((sum, transaction) => sum + Number(transaction.amount), 0);

        // Get savings goals
        const savingsGoals = await db.orm.public.SavingsGoal
            .where({ userId })
            .all();

        // Calculate total saved amount
        const totalSavings = savingsGoals.reduce(
            (sum, goal) => sum + Number(goal.savedAmount),
            0
        );

        // Get upcoming bills
        const bills = await db.orm.public.Bill
            .where({
                userId,
                status: "upcoming",
            })
            .all();

        // Calculate upcoming bills total
        const upcomingBillsTotal = bills.reduce(
            (sum, bill) => sum + Number(bill.amount),
            0
        );

        return res.status(200).json({
            success: true,
            message: "Dashboard summary retrieved successfully",
            data: {
                summary: {
                    totalBalance,
                    totalIncome,
                    totalExpense,
                    totalSavings,
                    upcomingBillsTotal,
                },
                counts: {
                    accounts: accounts.length,
                    transactions: transactions.length,
                    savingsGoals: savingsGoals.length,
                    upcomingBills: bills.length,
                },
            },
        });
    } catch (error) {
        console.error("Get dashboard summary error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve dashboard summary",
        });
    }
}

module.exports = {
    getDashboardSummary,
};
