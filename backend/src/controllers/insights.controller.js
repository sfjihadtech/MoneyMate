// =============================================================================
// File: insights.controller.js
// Purpose: HTTP controller handlers for insights.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const { monthlySummarySchema } = require("../validators/insights.validator");

// ============================================================
// Get Monthly Summary
// ============================================================

// -----------------------------------------------------------------------------
// Section: getMonthlySummary
// Purpose: Handles the get Monthly Summary part of this backend module.
// -----------------------------------------------------------------------------
async function getMonthlySummary(req, res) {
    try {
        const userId = req.userId;

        // Get month and year from query parameters
        const month = Number(req.query.month);
        const year = Number(req.query.year);

        // Validate month and year
        const validation = monthlySummarySchema.safeParse({
            month,
            year,
        });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Invalid month or year",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        // Get all user transactions
        const transactions = await db.orm.public.Transaction
            .where({ userId })
            .all();

        // Filter transactions for requested month
        const monthlyTransactions = transactions.filter((transaction) => {
            const date = new Date(transaction.occurredAt);

            return (
                date.getMonth() + 1 === month &&
                date.getFullYear() === year
            );
        });

        // Calculate income
        const totalIncome = monthlyTransactions
            .filter((transaction) => transaction.type === "income")
            .reduce((sum, transaction) => sum + Number(transaction.amount), 0);

        // Calculate expense
        const totalExpense = monthlyTransactions
            .filter((transaction) => transaction.type === "expense")
            .reduce((sum, transaction) => sum + Number(transaction.amount), 0);

        // Calculate net amount
        const netAmount = totalIncome - totalExpense;

        return res.status(200).json({
            success: true,
            message: "Monthly summary retrieved successfully",
            data: {
                month,
                year,
                summary: {
                    totalIncome,
                    totalExpense,
                    netAmount,
                },
                transactionCount: monthlyTransactions.length,
            },
        });
    } catch (error) {
        console.error("Get monthly summary error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve monthly summary",
        });
    }
}


// ============================================================
// Get Category Expense Breakdown
// ============================================================

// -----------------------------------------------------------------------------
// Section: getCategoryExpenseBreakdown
// Purpose: Handles the get Category Expense Breakdown part of this backend module.
// -----------------------------------------------------------------------------
async function getCategoryExpenseBreakdown(req, res) {
    try {
        const userId = req.userId;

        const month = Number(req.query.month);
        const year = Number(req.query.year);

        // Validate month and year
        const validation = monthlySummarySchema.safeParse({
            month,
            year,
        });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Invalid month or year",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        // Get user's transactions
        const transactions = await db.orm.public.Transaction
            .where({ userId })
            .all();

        // Get user's categories
        const categories = await db.orm.public.Category
            .where({ userId })
            .all();

        // Filter expense transactions for selected month
        const monthlyExpenses = transactions.filter((transaction) => {
            const date = new Date(transaction.occurredAt);

            return (
                transaction.type === "expense" &&
                date.getMonth() + 1 === month &&
                date.getFullYear() === year
            );
        });

        // Total expense
        const totalExpense = monthlyExpenses.reduce(
            (sum, transaction) => sum + Number(transaction.amount),
            0
        );

        // Group expenses by category
        const categoryMap = {};

        for (const transaction of monthlyExpenses) {
            const categoryId = transaction.categoryId;

            const category = categories.find(
                (item) => item.id === categoryId
            );

            const key = categoryId ?? "uncategorized";

            if (!categoryMap[key]) {
                categoryMap[key] = {
                    categoryId: categoryId,
                    name: category ? category.name : "Uncategorized",
                    icon: category ? category.icon : null,
                    amount: 0,
                    transactionCount: 0,
                };
            }

            categoryMap[key].amount += Number(transaction.amount);
            categoryMap[key].transactionCount += 1;
        }

        // Convert object to array and calculate percentages
        const breakdown = Object.values(categoryMap)
            .map((item) => ({
                ...item,
                percentage:
                    totalExpense > 0
                        ? Number(
                              ((item.amount / totalExpense) * 100).toFixed(2)
                          )
                        : 0,
            }))
            .sort((a, b) => b.amount - a.amount);

        return res.status(200).json({
            success: true,
            message: "Category expense breakdown retrieved successfully",
            data: {
                month,
                year,
                totalExpense,
                categories: breakdown,
            },
        });
    } catch (error) {
        console.error("Get category expense breakdown error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve category expense breakdown",
        });
    }
}



// ============================================================
// Get Budget Progress
// ============================================================

// -----------------------------------------------------------------------------
// Section: getBudgetProgress
// Purpose: Handles the get Budget Progress part of this backend module.
// -----------------------------------------------------------------------------
async function getBudgetProgress(req, res) {
    try {
        const userId = req.userId;

        const month = Number(req.query.month);
        const year = Number(req.query.year);

        // Validate month and year
        const validation = monthlySummarySchema.safeParse({
            month,
            year,
        });

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Invalid month or year",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        // Get budgets for selected month
        const budgets = await db.orm.public.Budget
            .where({
                userId,
                month,
                year,
            })
            .all();

        // Get user's transactions
        const transactions = await db.orm.public.Transaction
            .where({ userId })
            .all();

        // Get user's categories
        const categories = await db.orm.public.Category
            .where({ userId })
            .all();

        const budgetProgress = budgets.map((budget) => {
            const category = categories.find(
                (item) => item.id === budget.categoryId
            );

            const spent = transactions
                .filter((transaction) => {
                    const date = new Date(transaction.occurredAt);

                    return (
                        transaction.type === "expense" &&
                        transaction.categoryId === budget.categoryId &&
                        date.getMonth() + 1 === month &&
                        date.getFullYear() === year
                    );
                })
                .reduce(
                    (sum, transaction) =>
                        sum + Number(transaction.amount),
                    0
                );

            const budgetAmount = Number(budget.amount);
            const remaining = budgetAmount - spent;

            const percentage =
                budgetAmount > 0
                    ? Number(
                          ((spent / budgetAmount) * 100).toFixed(2)
                      )
                    : 0;

            let status = "safe";

            if (percentage >= 100) {
                status = "exceeded";
            } else if (percentage >= 80) {
                status = "warning";
            }

            return {
                budgetId: budget.id,
                categoryId: budget.categoryId,
                categoryName: category
                    ? category.name
                    : "Unknown Category",
                categoryIcon: category
                    ? category.icon
                    : null,
                budgetAmount,
                spent,
                remaining,
                percentage,
                status,
            };
        });

        const totalBudget = budgetProgress.reduce(
            (sum, item) => sum + item.budgetAmount,
            0
        );

        const totalSpent = budgetProgress.reduce(
            (sum, item) => sum + item.spent,
            0
        );

        return res.status(200).json({
            success: true,
            message: "Budget progress retrieved successfully",
            data: {
                month,
                year,
                totalBudget,
                totalSpent,
                totalRemaining: totalBudget - totalSpent,
                budgets: budgetProgress,
            },
        });
    } catch (error) {
        console.error("Get budget progress error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve budget progress",
        });
    }
}




module.exports = {
    getMonthlySummary,
    getCategoryExpenseBreakdown,
    getBudgetProgress,
};
