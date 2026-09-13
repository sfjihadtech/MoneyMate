// =============================================================================
// File: budgetNotification.service.js
// Purpose: Business/service layer for budget Notification.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    createBudgetWarningNotification,
    createBudgetExceededNotification,
} = require("./notification.service");


// ============================================================
// Remove Budget Notification By Type
// ============================================================

// -----------------------------------------------------------------------------
// Section: removeBudgetNotification
// Purpose: Handles the remove Budget Notification part of this backend module.
// -----------------------------------------------------------------------------
async function removeBudgetNotification(
    userId,
    budgetId,
    type
) {
    const notifications =
        await db.orm.public.Notification
            .where({
                userId,
                type,
            })
            .all();

    const matchingNotifications =
        notifications.filter(
            (notification) =>
                notification.relatedEntityType ===
                    "budget" &&
                notification.relatedEntityId ===
                    budgetId
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
// Calculate Budget Spending
// ============================================================

// -----------------------------------------------------------------------------
// Section: calculateBudgetSpending
// Purpose: Handles the calculate Budget Spending part of this backend module.
// -----------------------------------------------------------------------------
async function calculateBudgetSpending(
    userId,
    budget
) {
    const transactions =
        await db.orm.public.Transaction
            .where({
                userId,
            })
            .all();

    const spent = transactions
        .filter((transaction) => {
            if (
                transaction.type !== "expense" ||
                transaction.categoryId !==
                    budget.categoryId
            ) {
                return false;
            }

            const date =
                new Date(
                    transaction.occurredAt
                );

            if (
                Number.isNaN(
                    date.getTime()
                )
            ) {
                return false;
            }

            return (
                date.getMonth() + 1 ===
                    budget.month &&
                date.getFullYear() ===
                    budget.year
            );
        })
        .reduce(
            (sum, transaction) =>
                sum +
                Number(
                    transaction.amount
                ),
            0
        );

    return spent;
}


// ============================================================
// Evaluate Single Budget Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: evaluateBudgetNotification
// Purpose: Handles the evaluate Budget Notification part of this backend module.
// -----------------------------------------------------------------------------
async function evaluateBudgetNotification(
    userId,
    budget
) {
    const category =
        await db.orm.public.Category.first({
            id: budget.categoryId,
            userId,
        });

    const categoryName =
        category?.name ||
        "Unknown Category";

    const budgetAmount =
        Number(budget.amount);

    const spent =
        await calculateBudgetSpending(
            userId,
            budget
        );

    const percentage =
        budgetAmount > 0
            ? Number(
                  (
                      (spent /
                          budgetAmount) *
                      100
                  ).toFixed(2)
              )
            : 0;


    // ========================================================
    // Budget Exceeded: 100%+
    // ========================================================
    if (percentage >= 100) {
        // Warning is no longer needed
        await removeBudgetNotification(
            userId,
            budget.id,
            "budget_warning"
        );

        await createBudgetExceededNotification({
            userId,
            budget,
            categoryName,
        });

        return {
            budgetId: budget.id,
            spent,
            percentage,
            status: "exceeded",
        };
    }


    // ========================================================
    // Budget Warning: 80% - 99.99%
    // ========================================================
    if (percentage >= 80) {
        // Remove old exceeded notification
        // if spending was reduced below 100%
        await removeBudgetNotification(
            userId,
            budget.id,
            "budget_exceeded"
        );

        await createBudgetWarningNotification({
            userId,
            budget,
            categoryName,
            percentage,
        });

        return {
            budgetId: budget.id,
            spent,
            percentage,
            status: "warning",
        };
    }


    // ========================================================
    // Safe: Below 80%
    // Remove warning/exceeded notifications
    // ========================================================
    await removeBudgetNotification(
        userId,
        budget.id,
        "budget_warning"
    );

    await removeBudgetNotification(
        userId,
        budget.id,
        "budget_exceeded"
    );

    return {
        budgetId: budget.id,
        spent,
        percentage,
        status: "safe",
    };
}


// ============================================================
// Evaluate Budgets For Category + Month + Year
// ============================================================

// -----------------------------------------------------------------------------
// Section: evaluateBudgetsForTransaction
// Purpose: Handles the evaluate Budgets For Transaction part of this backend module.
// -----------------------------------------------------------------------------
async function evaluateBudgetsForTransaction({
    userId,
    categoryId,
    occurredAt,
}) {
    if (!categoryId || !occurredAt) {
        return [];
    }

    const date =
        new Date(occurredAt);

    if (
        Number.isNaN(
            date.getTime()
        )
    ) {
        return [];
    }

    const month =
        date.getMonth() + 1;

    const year =
        date.getFullYear();

    const budgets =
        await db.orm.public.Budget
            .where({
                userId,
                categoryId,
                month,
                year,
            })
            .all();

    const results = [];

    for (const budget of budgets) {
        const result =
            await evaluateBudgetNotification(
                userId,
                budget
            );

        results.push(result);
    }

    return results;
}


// ============================================================
// Export Budget Notification Service
// ============================================================
module.exports = {
    calculateBudgetSpending,
    evaluateBudgetNotification,
    evaluateBudgetsForTransaction,
    removeBudgetNotification,
};
